import axios from './interceptor'
import type { HttpResponse } from './interceptor'

export interface ProductVariant {
  id: number
  idSanPham: number
  idKichThuoc: number
  tenKichThuoc?: string
  idMauSac: number
  tenMauSac?: string
  soLuong: number
  giaBan: number
  trangThai: boolean
}

export interface ProductImage {
  id: number
  idSanPham: number
  urlAnh: string
  trangThai: boolean
}

export interface BackendProduct {
  id: number
  maSanPham?: string
  tenSanPham: string
  moTa?: string
  giaBan?: number
  giaNhoNhat?: number
  giaLonNhat?: number
  idDanhMuc?: number
  tenDanhMuc?: string
  idNhaSanXuat: number
  tenNhaSanXuat?: string
  trangThai: boolean
  soLuongBienThe?: number
  bienThe?: ProductVariant[]
  hinhAnh?: ProductImage[]
}

// Frontend product interface matching existing store structure
export interface Product {
  id: string
  color: string
  description: string
  gender: 'Male' | 'Female'
  name: string
  review: string
  starrating: number
  price: number
  img: string
  sizes?: string[]
}

// Convert backend product to frontend format
function mapBackendToFrontend(backendProduct: BackendProduct): Product {
  // Get price - use giaBan if available, otherwise use giaNhoNhat or giaLonNhat
  const price = backendProduct.giaBan 
    || backendProduct.giaNhoNhat 
    || backendProduct.giaLonNhat 
    || 0
  
  // Get primary color from variants (if available)
  const primaryColor = backendProduct.bienThe?.[0]?.tenMauSac || 'Black'
  
  // Get available sizes from variants (if available)
  const sizes = backendProduct.bienThe
    ?.filter(v => v.trangThai && v.soLuong > 0)
    ?.map(v => v.tenKichThuoc || String(v.idKichThuoc))
    ?.filter((v, i, arr) => arr.indexOf(v) === i) // Remove duplicates
    || []

  // Get primary image (if available from detail endpoint)
  const primaryImage = backendProduct.hinhAnh?.[0]?.urlAnh || null
  
  // Map image URL to local asset path
  const imagePath = mapImageToLocalPath(backendProduct.tenSanPham, primaryImage)

  return {
    id: String(backendProduct.id),
    color: primaryColor,
    description: backendProduct.moTa || backendProduct.tenSanPham,
    gender: determineGender(backendProduct.tenDanhMuc || ''),
    name: backendProduct.tenSanPham,
    review: `Quality product from ${backendProduct.tenNhaSanXuat || 'GearUp'}`,
    starrating: 4, // Default rating, could be from backend
    price: price,
    img: imagePath,
    sizes: sizes.length > 0 ? sizes : undefined
  }
}

// Map product name to local image assets
function mapImageToLocalPath(productName: string, backendImageUrl: string | null): string {
  // If backend provided image URL, use it
  if (backendImageUrl && (backendImageUrl.startsWith('http') || backendImageUrl.startsWith('/'))) {
    return backendImageUrl
  }
  
  // Try to match product name to folder structure in assets/images
  const normalizedName = productName.toLowerCase()
    .replace(/\s+/g, '-')
    .replace(/[^a-z0-9-]/g, '')
  
  // Map known product names to image paths in public folder
  // Products are copied to public/products/images/
  const productImageMap: Record<string, string> = {
    'nike-alphafly-3': '/products/images/nike/nike-alphafly-3/image_1.png',
    'nike-alphafly-3-premium': '/products/images/nike/nike-alphafly-3-premium/image_1.png',
    'nike-pegasus-premium': '/products/images/nike/nike-pegasus-premium/image_1.png',
    'nike-pegasus-premium-lv8': '/products/images/nike/nike-pegasus-premium-lv8/image_1.png',
    'nike-shox-z': '/products/images/nike/nike-shox-z/image_1.png',
    'nike-shox-z-se': '/products/images/nike/nike-shox-z-se/image_1.png',
    'nike-vaporfly-4': '/products/images/nike/nike-vaporfly-4/image_1.png',
    'nike-zoom-fly-6': '/products/images/nike/nike-zoom-fly-6/image_1.png',
    'nike-zoom-fly-6-eliud-kipchoge': '/products/images/nike/nike-zoom-fly-6-eliud-kipchoge/image_1.png',
    'adizero-evo-sl-shoes': '/products/images/adidas/adizero-evo-sl-shoes/image_1.jpg',
    'adizero-evo-sl': '/products/images/adidas/adizero-evo-sl/image_1.jpg',
    'ultraboost-5x-shoes': '/products/images/adidas/ultraboost-5x-shoes/image_1.jpg',
    'team-court-20-shoes': '/products/images/adidas/team-court-20-shoes/image_1.jpg',
    'galaxy-6-shoes': '/products/images/adidas/galaxy-6-shoes/image_1.jpg',
    'ultraboost-20-shoes': '/products/images/adidas/ultraboost-20-shoes/image_1.jpg',
    '4dfwd-x-strung-running-shoes': '/products/images/adidas/4dfwd-x-strung-running-shoes/image_1.jpg',
    'duramo-sl-shoes': '/products/images/adidas/duramo-sl-shoes/image_1.jpg',
    'ultrarun-5-running-shoes': '/products/images/adidas/ultrarun-5-running-shoes/image_1.jpg',
  }
  
  // Check if we have a mapped path
  if (productImageMap[normalizedName]) {
    return productImageMap[normalizedName]
  }
  
  // Try generic nike/adidas path
  if (normalizedName.includes('nike')) {
    return `/products/images/nike/${normalizedName}/image_1.png`
  }
  if (normalizedName.includes('adidas') || normalizedName.includes('adizero')) {
    return `/products/images/adidas/${normalizedName}/image_1.jpg`
  }
  
  // Fallback to products folder
  return `/products/${normalizedName}.jpg`
}

// Determine gender from category name
function determineGender(categoryName: string): 'Male' | 'Female' {
  const lower = categoryName.toLowerCase()
  if (lower.includes('nữ') || lower.includes('women') || lower.includes('female')) {
    return 'Female'
  }
  if (lower.includes('nam') || lower.includes('men') || lower.includes('male')) {
    return 'Male'
  }
  return 'Male' // Default
}

// Filter products to only sneakers
function isSneaker(product: BackendProduct): boolean {
  const categoryName = product.tenDanhMuc?.toLowerCase() || ''
  const productName = product.tenSanPham?.toLowerCase() || ''
  
  // Check if it's a sneaker based on category or name
  // More lenient matching - if it contains shoe-related keywords, it's a sneaker
  const sneakerKeywords = [
    'giày', 'giay', 'sneaker', 'sneakers', 
    'shoe', 'shoes', 'running', 'sport', 'athletic',
    'nike', 'adidas', 'puma', 'converse', 'vans'
  ]
  
  // Also exclude non-sneaker items
  const excludeKeywords = ['bag', 'túi', 'backpack', 'wallet', 'ví', 'accessory']
  const hasExclude = excludeKeywords.some(keyword => 
    categoryName.includes(keyword) || productName.includes(keyword)
  )
  
  if (hasExclude) return false
  
  return sneakerKeywords.some(keyword => 
    categoryName.includes(keyword) || productName.includes(keyword)
  )
}

export function getProductsList(): Promise<HttpResponse<BackendProduct[]>> {
  return axios.get('/api/san-pham-management/playlist')
}

export function getProductsPaging(page: number = 0, size: number = 20): Promise<HttpResponse<any>> {
  return axios.get('/api/san-pham-management/paging', {
    params: { page, size }
  })
}

export function getProductById(id: number | string): Promise<HttpResponse<BackendProduct>> {
  return axios.get(`/api/san-pham-management/detail/${id}`)
}

export async function getProductsByCategory(categoryId: number): Promise<HttpResponse<BackendProduct[]>> {
  const response = await getProductsList()
  // Response from interceptor is { success: true, data: [...], message: "..." }
  const allProducts = Array.isArray(response.data) ? response.data : []
  const filtered = allProducts.filter((p: BackendProduct) => p.idDanhMuc === categoryId)
  return {
    success: response.success,
    message: response.message,
    data: filtered
  }
}

// Get only sneaker products (filtered)
export async function getSneakerProducts(): Promise<Product[]> {
  try {
    const response = await getProductsList()
    // The interceptor returns the full ResponseObject: { success: true, data: [...], message: "..." }
    // So we need to access response.data to get the actual array
    // This matches how view project does it (see useThongKeData.ts line 34-35)
    const backendProducts: BackendProduct[] = Array.isArray(response.data) ? response.data : []
    
    if (!Array.isArray(response.data)) {
      return []
    }
    
    if (backendProducts.length === 0) {
      return []
    }
    
    // Filter to only active products first
    const activeProducts = backendProducts.filter((p: BackendProduct) => p.trangThai)
    
    // Filter to only sneakers
    const sneakers = activeProducts.filter(isSneaker)
    
    // If no sneakers found, show all active products (fallback)
    const productsToShow = sneakers.length > 0 ? sneakers : activeProducts
    
    // Map to frontend format
    const mapped = productsToShow.map(mapBackendToFrontend)
    return mapped
  } catch (error: any) {
    console.error('Error fetching sneaker products:', error)
    console.error('Error details:', error.response?.data || error.message)
    return []
  }
}

// Get all products mapped to frontend format
export async function getAllProducts(): Promise<Product[]> {
  try {
    const response = await getProductsList()
    
    // The interceptor returns the full ResponseObject: { success: true, data: [...], message: "..." }
    // So we need to access response.data to get the actual array
    const backendProducts: BackendProduct[] = Array.isArray(response.data) ? response.data : []
    
    return backendProducts
      .filter((p: BackendProduct) => p.trangThai)
      .map(mapBackendToFrontend)
  } catch (error: any) {
    console.error('Error fetching products:', error)
    console.error('Error details:', error.response?.data || error.message)
    return []
  }
}

