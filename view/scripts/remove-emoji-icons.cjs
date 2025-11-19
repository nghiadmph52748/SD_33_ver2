const path = require('node:path')
const fs = require('node:fs/promises')

const projectRoot = path.resolve(__dirname, '..')
const srcDir = path.join(projectRoot, 'src')
const applyChanges = process.argv.includes('--apply')
const emojiRegex =
  /[\u{1F300}-\u{1F6FF}\u{1F900}-\u{1F9FF}\u{1FA70}-\u{1FAFF}\u{1F100}-\u{1F1FF}\u{2700}-\u{27BF}\u{2600}-\u{26FF}\u{1F700}-\u{1F77F}\u{FE0F}\u{1F004}\u{1F0CF}]/gu
const allowedExtensions = new Set(['.vue', '.ts', '.js', '.json', '.scss', '.css'])

async function collectFiles(dir, files = []) {
  const entries = await fs.readdir(dir, { withFileTypes: true })
  for (const entry of entries) {
    if (entry.name === 'node_modules' || entry.name === 'dist') continue
    const fullPath = path.join(dir, entry.name)
    if (entry.isDirectory()) {
      await collectFiles(fullPath, files)
    } else if (allowedExtensions.has(path.extname(entry.name))) {
      files.push(fullPath)
    }
  }
  return files
}

async function processFile(filePath) {
  const original = await fs.readFile(filePath, 'utf-8')
  const matches = original.match(emojiRegex)
  if (!matches) return { filePath, removed: 0 }

  if (!applyChanges) {
    console.log(`[DRY-RUN] ${filePath} -> ${matches.length} emoji(s)`)
    return { filePath, removed: matches.length }
  }

  const updated = original.replace(emojiRegex, '')
  if (updated !== original) {
    await fs.writeFile(filePath, updated, 'utf-8')
    console.log(`[UPDATED] ${filePath} -> removed ${matches.length} emoji(s)`)
    return { filePath, removed: matches.length }
  }

  return { filePath, removed: 0 }
}

async function main() {
  const files = await collectFiles(srcDir)
  let totalRemoved = 0
  for (const file of files) {
    const { removed } = await processFile(file)
    totalRemoved += removed
  }

  const mode = applyChanges ? 'APPLY' : 'DRY-RUN'
  console.log(`\n[${mode}] Total emoji icons detected: ${totalRemoved}`)
  if (!applyChanges) {
    console.log('Re-run with "--apply" to remove the emojis.')
  }
}

main().catch((err) => {
  console.error('[ERROR] Failed to remove emoji icons:', err)
  process.exit(1)
})

