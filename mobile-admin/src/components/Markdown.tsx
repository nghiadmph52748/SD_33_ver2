import React from 'react'
import { Text, View, StyleSheet } from 'react-native'

interface MarkdownProps {
  content: string
}

// Very lightweight markdown renderer for RN
// Supports: headings (#/##/###), bold **text**, italic *text*, inline code `code`,
// fenced code blocks ```\n...\n```, unordered lists, and basic GitHub-style tables
export const Markdown: React.FC<MarkdownProps> = ({ content }) => {
  if (!content) return null

  // Split into blocks by triple newlines to reduce layout churn
  const blocks = splitIntoBlocks(content)

  return (
    <View>
      {blocks.map((block, index) => {
        if (block.type === 'code') {
          return (
            <View key={`code-${index}`} style={styles.codeBlock}>
              <Text style={styles.codeText}>{block.text}</Text>
            </View>
          )
        }
        if (block.type === 'table') {
          return <Table key={`tbl-${index}`} markdown={block.text} />
        }
        if (block.type === 'list') {
          return (
            <View key={`list-${index}`} style={styles.list}>
              {block.items.map((item: string, i: number) => (
                <View key={i} style={styles.listRow}>
                  <Text style={styles.bullet}>•</Text>
                  <Text style={styles.paragraph}>{renderInline(item)}</Text>
                </View>
              ))}
            </View>
          )
        }
        // paragraph/heading
        return (
          <Text key={`p-${index}`} style={getParagraphStyle(block.headingLevel)}>
            {renderInline(block.text)}
          </Text>
        )
      })}
    </View>
  )
}

function splitIntoBlocks(md: string): any[] {
  const lines = md.replace(/\r\n?/g, '\n').split('\n')
  const blocks: any[] = []
  let i = 0
  while (i < lines.length) {
    const line = lines[i]
    // fenced code
    if (/^```/.test(line)) {
      let j = i + 1
      const buf: string[] = []
      while (j < lines.length && !/^```/.test(lines[j])) {
        buf.push(lines[j])
        j++
      }
      blocks.push({ type: 'code', text: buf.join('\n') })
      i = j + 1
      continue
    }
    // table detection: header line with pipes followed by Markdown table separator
    if (line.includes('|') && i + 1 < lines.length) {
      const sep = lines[i + 1]
      const isSeparator = /^\s*\|?\s*(:?-{3,}:?\s*\|\s*)+(:?-{3,}:?\s*)\|?\s*$/.test(sep)
      if (isSeparator) {
        const tbl: string[] = [line]
        let j = i + 2 // skip separator row
        while (j < lines.length && lines[j].includes('|') && lines[j].trim() !== '') {
          tbl.push(lines[j])
          j++
        }
        blocks.push({ type: 'table', text: tbl.join('\n') })
        i = j
        continue
      }
    }
    // list
    if (/^\s*[-*]\s+/.test(line)) {
      const items: string[] = []
      let j = i
      while (j < lines.length && /^\s*[-*]\s+/.test(lines[j])) {
        items.push(lines[j].replace(/^\s*[-*]\s+/, ''))
        j++
      }
      blocks.push({ type: 'list', items })
      i = j
      continue
    }
    // heading
    const m = line.match(/^(#{1,3})\s+(.*)$/)
    if (m) {
      blocks.push({ type: 'paragraph', headingLevel: m[1].length, text: m[2] })
      i++
      continue
    }
    // paragraph: collect until blank
    const buf: string[] = []
    let j = i
    while (j < lines.length && lines[j].trim() !== '') {
      buf.push(lines[j])
      j++
    }
    blocks.push({ type: 'paragraph', headingLevel: 0, text: buf.join('\n') })
    i = j + 1
  }
  return blocks
}

function renderInline(text: string): React.ReactNode {
  // inline code
  const segments = [] as React.ReactNode[]
  const parts = text.split(/(`[^`]+`)/g)
  parts.forEach((part, idx) => {
    if (/^`[^`]+`$/.test(part)) {
      segments.push(
        <Text key={idx} style={styles.inlineCode}>
          {part.slice(1, -1)}
        </Text>
      )
    } else {
      // bold and italic
      let p = part
      // bold
      p = p.replace(/\*\*([^*]+)\*\*/g, (_, g1) => `\u0001${g1}\u0001`)
      // italic
      p = p.replace(/\*([^*]+)\*/g, (_, g1) => `\u0002${g1}\u0002`)
      const chunks = p.split(/(\u0001[^\u0001]+\u0001|\u0002[^\u0002]+\u0002)/)
      chunks.forEach((c, i2) => {
        if (/^\u0001/.test(c)) {
          segments.push(
            <Text key={`${idx}-${i2}`} style={styles.bold}>
              {c.slice(1, -1)}
            </Text>
          )
        } else if (/^\u0002/.test(c)) {
          segments.push(
            <Text key={`${idx}-${i2}`} style={styles.italic}>
              {c.slice(1, -1)}
            </Text>
          )
        } else if (c) {
          segments.push(
            <Text key={`${idx}-${i2}`} style={styles.paragraph}>
              {c}
            </Text>
          )
        }
      })
    }
  })
  return <>{segments}</>
}

const Table: React.FC<{ markdown: string }> = ({ markdown }) => {
  const lines = markdown.replace(/\r\n?/g, '\n').split('\n').filter(Boolean)
  if (lines.length < 2) return null
  const header = splitRow(lines[0])
  const body = lines.slice(2).map(splitRow)
  return (
    <View style={styles.table}>
      <View style={[styles.tableRow, styles.tableHeaderRow]}>
        {header.map((h, i) => (
          <View key={i} style={[styles.tableCell, styles.tableHeaderCell]}>
            <Text style={styles.tableHeaderText}>{h.trim()}</Text>
          </View>
        ))}
      </View>
      {body.map((row, r) => (
        <View key={r} style={styles.tableRow}>
          {row.map((c, i) => (
            <View key={i} style={styles.tableCell}>
              <Text style={styles.tableText}>{c.trim()}</Text>
            </View>
          ))}
        </View>
      ))}
    </View>
  )
}

function splitRow(line: string): string[] {
  const trimmed = line.trim()
  const noEdges = trimmed.replace(/^\|/, '').replace(/\|$/, '')
  return noEdges.split('|')
}

function getParagraphStyle(headingLevel: number) {
  if (headingLevel === 1) return styles.h1
  if (headingLevel === 2) return styles.h2
  if (headingLevel === 3) return styles.h3
  return styles.paragraph
}

const styles = StyleSheet.create({
  paragraph: { fontSize: 16, lineHeight: 22, color: '#0f172a' },
  bold: { fontWeight: '700', color: '#0f172a', fontSize: 16, lineHeight: 22 },
  italic: { fontStyle: 'italic', color: '#0f172a', fontSize: 16, lineHeight: 22 },
  h1: { fontSize: 20, fontWeight: '700', marginBottom: 6, color: '#0f172a' },
  h2: { fontSize: 18, fontWeight: '700', marginBottom: 6, color: '#0f172a' },
  h3: { fontSize: 16, fontWeight: '700', marginBottom: 4, color: '#0f172a' },
  inlineCode: {
    fontFamily: 'Menlo',
    backgroundColor: '#f1f5f9',
    borderWidth: 1,
    borderColor: '#e2e8f0',
    paddingHorizontal: 4,
    borderRadius: 4,
  },
  codeBlock: {
    backgroundColor: '#0f172a',
    padding: 10,
    borderRadius: 8,
    marginTop: 8,
  },
  codeText: { fontFamily: 'Menlo', color: '#e2e8f0', fontSize: 12, lineHeight: 18 },
  list: { marginTop: 4 },
  listRow: { flexDirection: 'row', gap: 8, marginBottom: 4, alignItems: 'flex-start' },
  bullet: { color: '#334155', marginTop: 2 },
  table: {
    borderWidth: 1,
    borderColor: '#e2e8f0',
    borderRadius: 8,
    overflow: 'hidden',
    marginTop: 8,
  },
  tableRow: { flexDirection: 'row' },
  tableHeaderRow: { backgroundColor: '#f1f5f9' },
  tableCell: { flex: 1, padding: 8, borderRightWidth: 1, borderRightColor: '#e2e8f0' },
  tableHeaderCell: { borderRightColor: '#e2e8f0' },
  tableHeaderText: { fontWeight: '700', color: '#0f172a' },
  tableText: { color: '#0f172a' },
})

export default Markdown


