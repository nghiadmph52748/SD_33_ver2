import axios from 'axios'

export interface DiaChi {
  tenDiaChi?: string
  thanhPho: string
  quan: string
  phuong: string
  diaChiCuThe: string
  macDinh?: boolean
  deleted?: boolean
}

export interface DiaChiKhachHangResponse {
  id: number
  idKhachHang: number
  maDiaChi: string
  tenDiaChi: string
  thanhPho: string
  quan: string
  phuong: string
  diaChiCuThe: string
  deleted: boolean
}

export interface SaveCustomerAddressRequest {
  idKhachHang: number
  tenDiaChi?: string
  thanhPho: string
  quan: string
  phuong: string
  diaChiCuThe: string
  deleted?: boolean
}

export interface KhachHangRequest {
  tenKhachHang: string
  email: string
  matKhau?: string
  tenTaiKhoan?: string
  soDienThoai: string
  ngaySinh: string
  gioiTinh: boolean
  phanLoai?: string | null
  phanLoaiText?: string
  trangThai?: boolean
  deleted?: boolean
  listDiaChi?: DiaChi[]
}

export interface KhachHangResponse {
  id: number
  maKhachHang: string
  tenKhachHang: string
  tenTaiKhoan: string
  matKhau: string
  email: string
  soDienThoai: string
  gioiTinh: boolean
  ngaySinh: string
  phanLoai: string | null
  deleted: boolean
  tongDon: number
  tongChiTieu: number
  trangThai: boolean
  phanLoaiText: string
  listDiaChi: DiaChi[]
}

// API lấy danh sách khách hàng
export const layDanhSachKhachHang = () => {
  return axios.get<{ data: KhachHangResponse[] }>('/api/khach-hang-management/playlist')
}

// API lấy chi tiết khách hàng theo ID
export const layChiTietKhachHang = (id: number) => {
  return axios.get<{ data: KhachHangResponse }>(`/api/khach-hang-management/detail/${id}`)
}

// API thêm khách hàng mới
export const themKhachHang = (data: KhachHangRequest) => {
  return axios.post('/api/khach-hang-management/add', data)
}

// API thêm khách hàng nhanh
export const themKhachHangNhanh = (data: KhachHangRequest) => {
  return axios.post('/api/khach-hang-management/add/quick', data)
}

// API cập nhật khách hàng
export const capNhatKhachHang = (id: number, data: KhachHangRequest) => {
  return axios.put(`/api/khach-hang-management/update/${id}`, data)
}

// API cập nhật trạng thái khách hàng
export const capNhatTrangThaiKhachHang = (id: number) => {
  return axios.put(`/api/khach-hang-management/update/status/${id}`)
}

// API xuất Excel danh sách khách hàng
export const xuatExcelKhachHang = () => {
  return axios.get('/api/khach-hang-management/export-excel', {
    responseType: 'blob',
  })
}

export const luuDiaChiKhachHangNeuChuaCo = (data: SaveCustomerAddressRequest) => {
  return axios.post<{ data: DiaChiKhachHangResponse }>('/api/dia-chi-khach-hang-management/save-if-missing', data)
}
