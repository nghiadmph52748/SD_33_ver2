import axios from 'axios'

// Interfaces
export interface NhanVienRequest {
  maNhanVien?: string
  tenNhanVien: string
  tenTaiKhoan?: string
  matKhau?: string
  ngaySinh?: string
  cccd?: string
  email: string
  soDienThoai: string
  thanhPho?: string
  quan?: string
  phuong?: string
  gioiTinh: boolean
  diaChiCuThe?: string
  idQuyenHan?: number
  tenQuyenHan?: string
  anhNhanVien?: string | null
  trangThai: boolean
  deleted?: boolean
  createAt?: string
  createBy?: number
}

export interface NhanVienResponse {
  id: number
  maNhanVien: string
  tenNhanVien: string
  tenTaiKhoan: string
  email: string
  soDienThoai: string
  ngaySinh: string
  gioiTinh: boolean
  thanhPho: string
  quan: string
  phuong: string
  diaChiCuThe: string
  tenQuyenHan: string
  trangThai: boolean
  anhNhanVien: string | null
}

// API Functions

/**
 * Lấy danh sách nhân viên
 */
export const layDanhSachNhanVien = () => {
  return axios.get<{ data: NhanVienResponse[] }>('/api/nhan-vien-management/playlist')
}

/**
 * Lấy chi tiết nhân viên theo ID
 */
export const layChiTietNhanVien = (id: string | number) => {
  return axios.get<{ data: NhanVienResponse }>(`/api/nhan-vien-management/detail/${id}`)
}

/**
 * Thêm nhân viên mới
 */
export const themNhanVien = (data: NhanVienRequest | FormData) => {
  return axios.post<{ success: boolean; message: string }>('/api/nhan-vien-management/add', data)
}

/**
 * Cập nhật thông tin nhân viên
 */
export const capNhatNhanVien = (id: string | number, data: NhanVienRequest | FormData) => {
  return axios.put<{ success: boolean; message: string }>(`/api/nhan-vien-management/update/${id}`, data)
}

/**
 * Cập nhật trạng thái nhân viên (toggle active/inactive)
 */
export const capNhatTrangThaiNhanVien = (id: string | number) => {
  return axios.put(`/api/nhan-vien-management/update/status/${id}`)
}

/**
 * Xuất Excel danh sách nhân viên
 */
export const xuatExcelNhanVien = (data: NhanVienResponse[]) => {
  // This function will be handled in the component using XLSX
  return data
}
