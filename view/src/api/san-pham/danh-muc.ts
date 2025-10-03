import axios from 'axios'

export interface NhaSanXuat {
  id: number
  tenNhaSanXuat: string
}

export interface XuatXu {
  id: number
  tenXuatXu: string
}

// ==================== DANH MỤC SẢN PHẨM (SẢN PHẨM CHÍNH) ====================
export interface DanhMucSanPham {
  id: number
  maSanPham?: string
  tenSanPham: string
  soLuongBienThe?: number
  giaNhoNhat?: number
  giaLonNhat?: number
  trangThai: boolean
  deleted: boolean
  idNhaSanXuat: number
  idXuatXu: number
  createAt?: string
  updateAt?: string
  createBy?: number
  updateBy?: number
}

export interface DanhMucResponse {
  data: DanhMucSanPham[] // Changed from content
  totalPages: number
  currentPage: number
  pageSize: number
  totalElements?: number // Will be calculated on frontend
}

export function getDanhMucSanPhamList(page: number, size: number = 10) {
  return axios.get<DanhMucResponse>(`/api/san-pham-management/paging?page=${page}&size=${size}`)
}

export function getDanhMucSanPhamById(id: number) {
  return axios.get<DanhMucSanPham>(`/api/san-pham-management/detail/${id}`)
}

export interface CreateDanhMucSanPhamRequest {
  tenSanPham: string
  idNhaSanXuat: number
  idXuatXu: number
  deleted?: boolean
  trangThai?: boolean
}

export function createDanhMucSanPham(data: CreateDanhMucSanPhamRequest) {
  return axios.post<DanhMucSanPham>('/api/san-pham-management/add', data)
}

export type UpdateDanhMucSanPhamRequest = CreateDanhMucSanPhamRequest

export function updateDanhMucSanPham(id: number, data: UpdateDanhMucSanPhamRequest) {
  return axios.put<DanhMucSanPham>(`/api/san-pham-management/update/${id}`, data)
}

export function deleteDanhMucSanPham(id: number) {
  return axios.put(`/api/san-pham-management/update/status/${id}`)
}

// ==================== LẤY DANH SÁCH NHÀ SẢN XUẤT CHO DROPDOWN ====================
export interface NhaSanXuatOption {
  id: number
  tenNhaSanXuat: string
}

export function getNhaSanXuatOptions() {
  return axios.get<NhaSanXuatOption[]>('/api/nha-san-xuat-management/playlist')
}

// ==================== LẤY DANH SÁCH XUẤT XỨ CHO DROPDOWN ====================
export interface XuatXuOption {
  id: number
  tenXuatXu: string
}

export function getXuatXuOptions() {
  return axios.get<XuatXuOption[]>('/api/xuat-xu-management/playlist')
}
