// Color mapping for Vietnamese color names to hex codes
export const colorMapping: Record<string, string> = {
  // Đỏ variants
  'Đỏ': '#FF0000',
  'Red': '#FF0000',
  'Đỏ đậm': '#8B0000',
  'Đỏ tươi': '#FF4444',
  
  // Xanh dương variants
  'Xanh dương': '#0000FF',
  'Blue': '#0000FF',
  'Xanh navy': '#000080',
  'Xanh da trời': '#87CEEB',
  
  // Xanh lá variants
  'Xanh lá': '#00FF00',
  'Green': '#008000',
  'Xanh lá cây': '#228B22',
  'Xanh lục': '#32CD32',
  
  // Vàng variants
  'Vàng': '#FFFF00',
  'Yellow': '#FFD700',
  'Vàng gold': '#FFD700',
  
  // Cam variants
  'Cam': '#FFA500',
  'Orange': '#FF8C00',
  
  // Hồng variants
  'Hồng': '#FFC0CB',
  'Pink': '#FF69B4',
  'Hồng đậm': '#FF1493',
  
  // Tím variants
  'Tím': '#800080',
  'Purple': '#9370DB',
  'Tím nhạt': '#DDA0DD',
  
  // Đen/Trắng/Xám
  'Đen': '#000000',
  'Black': '#000000',
  'Trắng': '#FFFFFF',
  'White': '#F5F5F5',
  'Xám': '#808080',
  'Gray': '#A9A9A9',
  'Xám đậm': '#696969',
  'Xám nhạt': '#D3D3D3',
  
  // Nâu variants
  'Nâu': '#8B4513',
  'Brown': '#A0522D',
  'Nâu đậm': '#654321',
  'Nâu nhạt': '#D2B48C',
  
  // Khác
  'Be': '#F5F5DC',
  'Beige': '#F5F5DC',
  'Kem': '#FFFDD0',
  'Cream': '#FFFDD0',
  'Bạc': '#C0C0C0',
  'Silver': '#C0C0C0',
  'Vàng gold': '#FFD700',
  'Gold': '#FFD700',
}

export function getColorHex(colorName: string | null | undefined): string {
  if (!colorName) return '#CCCCCC' // Default gray
  
  const normalized = colorName.trim()
  
  // Direct match
  if (colorMapping[normalized]) {
    return colorMapping[normalized]
  }
  
  // Case-insensitive match
  const lowerName = normalized.toLowerCase()
  const matchKey = Object.keys(colorMapping).find(key => key.toLowerCase() === lowerName)
  if (matchKey) {
    return colorMapping[matchKey]
  }
  
  // Partial match
  const partialKey = Object.keys(colorMapping).find(key => 
    lowerName.includes(key.toLowerCase()) || key.toLowerCase().includes(lowerName)
  )
  if (partialKey) {
    return colorMapping[partialKey]
  }
  
  // Default fallback
  return '#CCCCCC'
}






