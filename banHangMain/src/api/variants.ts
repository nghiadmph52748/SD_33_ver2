import axios from './interceptor'

export interface VariantImage {
  duongDanAnh: string
}

export interface ProductVariantResponse {
  id: number
  maChiTietSanPham?: string
  tenSanPham?: string
  tenMauSac?: string
  maMau?: string // Hex color code for the color variant
  tenKichThuoc?: string
  giaBan?: number // Original price
  giaTriGiamGia?: number // Discount percentage (0-100)
  idDotGiamGia?: number
  tenDotGiamGia?: string
  soLuong?: number
  trangThai?: boolean
  anhSanPham?: string[] | VariantImage[]
}

export async function fetchVariantsByProduct(productId: number): Promise<ProductVariantResponse[]> {
  try {
    const res = await axios.get(`/api/chi-tiet-san-pham-management/list/${productId}`)
    const data = Array.isArray(res?.data) ? res.data : []
    return data as ProductVariantResponse[]
  } catch (error) {
    console.error('Failed to fetch product variants', error)
    return []
  }
}

