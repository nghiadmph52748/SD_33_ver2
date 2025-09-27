const CSV_MIME = 'text/csv;charset=utf-8;'

const normaliseValue = (value: unknown): string => {
  if (value === null || value === undefined) {
    return ''
  }

  if (value instanceof Date) {
    return value.toISOString()
  }

  return String(value)
}

const escapeForCsv = (value: string): string => {
  const needsQuoting = value.includes(',') || value.includes('\n') || value.includes('\r') || value.includes('"')
  const escaped = value.replace(/"/g, '""')
  return needsQuoting ? `"${escaped}"` : escaped
}

const downloadCsv = (filename: string, header: string[], rows: unknown[][]) => {
  const csvLines = [header, ...rows].map((row) => row.map((cell) => escapeForCsv(normaliseValue(cell))).join(','))
  const csvContent = csvLines.join('\r\n')

  const blob = new Blob([csvContent], { type: CSV_MIME })
  const url = URL.createObjectURL(blob)

  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = filename
  anchor.style.display = 'none'

  document.body.appendChild(anchor)
  anchor.click()
  document.body.removeChild(anchor)

  URL.revokeObjectURL(url)
}

export default downloadCsv
