import axios from 'axios'

// ==================== BIẾN THỂ SẢN PHẨM (CHI TIẾT SẢN PHẨM) ====================
export interface MauSac {
  id: number
  tenMauSac: string
  maMau: string
}

export interface anhSanPham {
  id: number
  duongDanAnh: string
  tenAnh?: string
  mauAnh?: string
  trangThai: boolean
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
  data: BienTheSanPham[]
  currentPage: number
  totalPages: number
  pageSize: number
  totalElements: number
}

export interface ApiResponse<T> {
  data: T
  message: string
  success: boolean
}

export function getBienTheSanPhamList(page: number, productId?: number) {
  if (productId) {
    // Lấy biến thể theo sản phẩm
    return axios.get<BienTheSanPham[]>(`/api/chi-tiet-san-pham-management/list/${productId}`)
  }
  // Lấy tất cả biến thể
  return axios.get<BienTheSanPham[]>(`/api/chi-tiet-san-pham-management/playlist`)
}

export function getBienTheSanPhamPage(page: number, productId?: number, size: number = 10) {
  if (productId) {
    // Lấy biến thể theo sản phẩm
    return axios.get<ApiResponse<BienTheResponse>>(`/api/chi-tiet-san-pham-management/paging/${productId}?page=${page}&size=${size}`)
  }
  // Lấy tất cả biến thể
  return axios.get<ApiResponse<BienTheResponse>>(`/api/chi-tiet-san-pham-management/paging/all?page=${page}&size=${size}`)
}

export function getBienTheSanPhamById(id: number) {
  return axios.get<ApiResponse<BienTheSanPham>>(`/api/chi-tiet-san-pham-management/detail/${id}`)
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

// ==================== API CHO TRANG CHI TIẾT VÀ CẬP NHẬT ====================
// Sử dụng lại API đã có thay vì tạo alias mới
export const getBienTheById = getBienTheSanPhamById

export interface UpdateBienTheRequest {
  id: number
  idSanPham: number
  giaBan: number
  giaTriGiamGia?: number
  soLuong: number
  trangThai: boolean
  mauSac: number
  kichThuoc: number
  chatLieu: number
  deGiay: number
  trongLuong: number
  ghiChu?: string
  deleted: boolean
  createdAt: Date
  createdBy: number
  updatedAt: Date
  updatedBy: number
}

export function updateBienThe(data: UpdateBienTheRequest) {
  // Map to backend UpdateBienTheSanPhamRequest structure
  return updateBienTheSanPham(data.id, {
    idSanPham: data.idSanPham,
    idMauSac: data.mauSac,
    idKichThuoc: data.kichThuoc,
    idChatLieu: data.chatLieu,
    idDeGiay: data.deGiay,
    idTrongLuong: data.trongLuong,
    soLuong: data.soLuong,
    giaBan: data.giaBan,
    trangThai: data.trangThai,
    ghiChu: data.ghiChu,
    deleted: data.deleted,
    createAt: data.createAt,
    createBy: data.createBy,
    updateAt: data.updateAt,
    updateBy: data.updateBy,
  })
}

// ==================== UPLOAD ẢNH CHO BIẾN THỂ ====================
// Note: uploadAnhBienThe bây giờ trả về array ID của ảnh đã upload lên cloud

export function uploadAnhBienThe(files: File[], tenAnh: string, mauAnh: string, createBy: number) {
  const formData = new FormData()
  files.forEach((file) => {
    formData.append('file', file)
  })
  formData.append('tenAnh', tenAnh)
  formData.append('mauAnh', mauAnh)
  formData.append('createBy', createBy)
  return axios.post<number[]>('/api/anh-san-pham-management/add-multi-image/cloud', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export function getAnhSanPhamByMauAnh(mauAnh: string) {
  return axios.get<AnhSanPham[]>(`/api/anh-san-pham-management/filter?mauAnh=${mauAnh}`)
}

export function deleteAnhBienThe(idBienThe: number, idAnh: number) {
  return axios.put(`/api/chi-tiet-san-pham-anh-management/update/status/${idAnh}`)
}

// Delete image link (update status)
export function deleteAnhBienTheLink(imageDetailId: number) {
  return axios.put(`/api/chi-tiet-san-pham-anh-management/update/status/${imageDetailId}`)
}

// Get all image details to find IDs by URL matching
export function getAllAnhBienTheDetails() {
  return axios.get<AnhSanPhamBienThe[]>(`/api/chi-tiet-san-pham-anh-management/playlist`)
}

// Add images to variant (liên kết ảnh với biến thể)
export function addAnhToBienThe(idBienThe: number, imageIds: number[]) {
  return axios.post(`/api/chi-tiet-san-pham-anh-management/add`, {
    idChiTietSanPham: idBienThe,
    idAnhSanPhamList: imageIds,
    trangThai: true,
    deleted: false,
  })
}

// ==================== LẤY OPTIONS CHO DROPDOWN ====================
export function getSanPhamOptions() {
  return axios.get<SanPham[]>('/api/san-pham-management/playlist')
}

export function getMauSacOptions() {
  return axios.get<MauSac[]>('/api/mau-sac-management/list')
}

export function getKichThuocOptions() {
  return axios.get<KichThuoc[]>('/api/kich-thuoc-management/list')
}

export function getChatLieuOptions() {
  return axios.get<ChatLieu[]>('/api/chat-lieu-management/list')
}

export function getDeGiayOptions() {
  return axios.get<DeGiay[]>('/api/de-giay-management/list')
}

export function getTrongLuongOptions() {
  return axios.get<TrongLuong[]>('/api/trong-luong-management/list')
}

// ==================== LẤY ẢNH SẢN PHẨM THEO BIẾN THỂ ====================
// Note: Endpoint này không tồn tại trong controller, dữ liệu lấy từ getBienTheById response
export function getAnhBienThe(idBienThe: number) {
  return axios.get<AnhSanPhamBienThe[]>(`/api/chi-tiet-san-pham-management/get/${idBienThe}`)
}

// ==================== THÊM/XÓA ẢNH CHO BIẾN THỂ ====================
export interface ThemAnhBienTheRequest {
  idChiTietSanPham: number
  idAnhSanPhamList: number[] // Array of anh_san_pham IDs
  trangThai: true
  deleted: false
  createAt: Date
  createBy: number
}

export function themAnhChoBienThe(data: ThemAnhBienTheRequest) {
  return axios.post('/api/chi-tiet-san-pham-anh-management/add-multiple', data)
}

export function xoaAnhKhoiBienThe(idAnhBienThe: number) {
  return axios.put(`/api/chi-tiet-san-pham-anh-management/update/status/${idAnhBienThe}`)
}

// Sử dụng lại API đã có thay vì tạo alias
export const getMauSac = getMauSacOptions
export const getKichThuoc = getKichThuocOptions
export const getChatLieu = getChatLieuOptions
export const getDeGiay = getDeGiayOptions
export const getTrongLuong = getTrongLuongOptions
