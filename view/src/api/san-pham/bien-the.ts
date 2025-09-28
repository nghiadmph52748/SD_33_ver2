import axios from 'axios'

// ==================== BIẾN THỂ SẢN PHẨM (CHI TIẾT SẢN PHẨM) ====================
export interface MauSac {
  id: number
  tenMauSac: string
  maMau: string
}

export interface KichThuoc {
  id: number
  tenKichThuoc: string
}

export interface DeGiay {
  id: number
  tenDeGiay: string
}

export interface ChatLieu {
  id: number
  tenChatLieu: string
}

export interface TrongLuong {
  id: number
  tenTrongLuong: string
}

export interface NhaSanXuat {
  id: number
  tenNhaSanXuat: string
}

export interface XuatXu {
  id: number
  tenXuatXu: string
}

export interface SanPham {
  id: number
  tenSanPham: string
  maSanPham?: string
  nhaSanXuat?: NhaSanXuat
  xuatXu?: XuatXu
}

export interface AnhSanPhamBienThe {
  id: number
  idAnhSanPham: number
  duongDanAnh: string
  tenAnh?: string
  mauAnh?: string
  trangThai: boolean
}

export interface BienTheSanPham {
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
  // Thông tin bổ sung từ entity (không có trong response full)
  ghiChu?: string
  createBy?: number
  updateBy?: number
}

export interface BienTheResponse {
  content: BienTheSanPham[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export function getBienTheSanPhamList(page: number, productId?: number) {
  if (productId) {
    // Lấy biến thể theo sản phẩm
    return axios.get<BienTheSanPham[]>(`/api/chi-tiet-san-pham-management/list/${productId}`)
  }
  // Lấy tất cả biến thể
  return axios.get<BienTheSanPham[]>(`/api/chi-tiet-san-pham-management/playlist`)
}

export function getBienTheSanPhamPage(page: number, productId?: number) {
  if (productId) {
    // Lấy biến thể theo sản phẩm
    return axios.get<BienTheResponse>(`/api/chi-tiet-san-pham-management/paging/${productId}?page=${page}`)
  }
  // Lấy tất cả biến thể
  return axios.get<BienTheResponse>(`/api/chi-tiet-san-pham-management/paging/all?page=${page}`)
}

export function getBienTheSanPhamById(id: number) {
  return axios.get<BienTheSanPham>(`/api/chi-tiet-san-pham-management/detail/${id}`)
}

export interface CreateBienTheSanPhamRequest {
  idSanPham: number
  idMauSac: number
  idKichThuoc: number
  idDeGiay: number
  idChatLieu: number
  idTrongLuong: number
  soLuong: number
  giaBan: number
  trangThai: boolean
  ghiChu?: string
  deleted?: boolean
}

export function createBienTheSanPham(data: CreateBienTheSanPhamRequest) {
  return axios.post<BienTheSanPham>('/api/chi-tiet-san-pham-management/add', data)
}

export type UpdateBienTheSanPhamRequest = CreateBienTheSanPhamRequest

export function updateBienTheSanPham(id: number, data: UpdateBienTheSanPhamRequest) {
  return axios.put<BienTheSanPham>(`/api/chi-tiet-san-pham-management/update/${id}`, data)
}

export function deleteBienTheSanPham(id: number) {
  return axios.put(`/api/chi-tiet-san-pham-management/update/status/${id}`)
}

// ==================== UPLOAD ẢNH CHO BIẾN THỂ ====================
// Note: uploadAnhBienThe bây giờ trả về array ID của ảnh đã upload lên cloud

export function uploadAnhBienThe(files: File[]) {
  const formData = new FormData()
  files.forEach((file, index) => {
    formData.append('file', file)
  })

  return axios.post<number[]>('/api/anh-san-pham-management/add-multi-image/cloud', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export function deleteAnhBienThe(idBienThe: number, idAnh: number) {
  return axios.put(`/api/chi-tiet-san-pham-anh-management/update/status/${idAnh}`)
}

// ==================== LẤY OPTIONS CHO DROPDOWN ====================
export function getSanPhamOptions() {
  return axios.get<SanPham[]>('/api/san-pham-management/playlist')
}

export function getMauSacOptions() {
  return axios.get<MauSac[]>('/api/mau-sac-management/playlist')
}

export function getKichThuocOptions() {
  return axios.get<KichThuoc[]>('/api/kich-thuoc-management/playlist')
}

export function getChatLieuOptions() {
  return axios.get<ChatLieu[]>('/api/chat-lieu-management/playlist')
}

export function getDeGiayOptions() {
  return axios.get<DeGiay[]>('/api/de-giay-management/playlist')
}

export function getTrongLuongOptions() {
  return axios.get<TrongLuong[]>('/api/trong-luong-management/playlist')
}

// ==================== LẤY ẢNH SẢN PHẨM THEO BIẾN THỂ ====================
export function getAnhBienThe(idBienThe: number) {
  return axios.get<AnhSanPhamBienThe[]>(`/api/chi-tiet-san-pham-anh-management/list/${idBienThe}`)
}

// ==================== THÊM/XÓA ẢNH CHO BIẾN THỂ ====================
export interface ThemAnhBienTheRequest {
  idChiTietSanPham: number
  danhSachAnh: number[] // Array of anh_san_pham IDs
}

export function themAnhChoBienThe(data: ThemAnhBienTheRequest) {
  return axios.post('/api/chi-tiet-san-pham-anh-management/add-multiple', data)
}

export function xoaAnhKhoiBienThe(idAnhBienThe: number) {
  return axios.put(`/api/chi-tiet-san-pham-anh-management/update/status/${idAnhBienThe}`)
}
