import client from './client'

export interface ProductVariant {
  id: number
  maChiTietSanPham?: string
  tenSanPham?: string
  anhSanPham?: string[]
  tenNhaSanXuat?: string
  tenXuatXu?: string
  tenDeGiay?: string
  tenChatLieu?: string
  tenMauSac?: string
  tenKichThuoc?: string
  tenTrongLuong?: string
  soLuong: number
  giaBan: number
  idDotGiamGia?: number
  tenDotGiamGia?: string
  giaTriGiamGia?: number
  trangThai: boolean
  deleted?: boolean
  createAt?: string
  updateAt?: string
}

export interface ProductVariantPage {
  data: ProductVariant[]
  currentPage: number
  totalPages: number
  pageSize: number
  totalElements: number
}

export const fetchProductVariants = (page = 0, size = 20) =>
  client.get<ProductVariantPage, ProductVariantPage>(
    `/api/chi-tiet-san-pham-management/paging/all?page=${page}&size=${size}`
  )

export const fetchAllProductVariants = () =>
  client.get<ProductVariant[], ProductVariant[]>(`/api/chi-tiet-san-pham-management/playlist`)

export const fetchProductVariantById = (id: number) =>
  client.get<ProductVariant, ProductVariant>(`/api/chi-tiet-san-pham-management/detail/${id}`)
