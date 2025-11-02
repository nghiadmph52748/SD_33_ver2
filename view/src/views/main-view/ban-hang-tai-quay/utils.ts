// Shared utilities for POS view
export function formatCurrency(value: number): string {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(value || 0)
}

export function getProductDisplayName(record: { productName: string; tenMauSac?: string; tenKichThuoc?: string }): string {
  const parts: string[] = [record.productName]
  if (record.tenMauSac) parts.push(record.tenMauSac)
  if (record.tenKichThuoc) parts.push(record.tenKichThuoc)
  return parts.join(' - ')
}
