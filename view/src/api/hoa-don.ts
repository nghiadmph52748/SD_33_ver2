import axios, { AxiosRequestConfig } from 'axios'
import { getToken } from '@/utils/auth'

const API_BASE = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'

interface ApiEnvelope<T> {
  isSuccess?: boolean
  success?: boolean
  data: T
  message?: string
}

export interface ThongTinDonHangModel {
  id?: number
  idHoaDon?: number
  idTrangThaiDonHang?: number
  tenTrangThaiDonHang?: string
  thoiGian?: string
  ghiChu?: string
  trangThai?: boolean
  deleted?: boolean
}

const api = axios.create({
  baseURL: API_BASE,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

const requestJson = async <T>(path: string, config?: AxiosRequestConfig): Promise<T> => {
  try {
    const response = await api.request({
      url: path,
      ...config,
    })

    console.log(response)

    const payload = response.data as ApiEnvelope<T>

    if (payload.data === undefined && !payload.message) {
      throw new Error('Malformed response payload')
    }

    const successFlag = payload.isSuccess ?? payload.success
    if (successFlag === false) {
      const normalizedMessage = (payload.message ?? '').toLowerCase()
      const isPositive = normalizedMessage.includes('success') || normalizedMessage.includes('thành công')
      if (!isPositive) {
        throw new Error(payload.message ?? 'Yêu cầu thất bại')
      }
    }

    return payload.data
  } catch (error) {
    if (axios.isAxiosError(error)) {
      throw new Error(`Request failed with status ${error.response?.status}`)
    }
    throw error
  }
}

export interface HoaDonApiModel {
  id?: number
  idKhachHang?: number
  idPhieuGiamGia?: number
  idNhanVien?: number
  idPhuongThucThanhToan?: number
  idTrangThaiDonHang?: number
  phiVanChuyen?: number
  tongTien?: number
  tongTienSauGiam?: number
  tenNguoiNhan?: string
  diaChiNhanHang?: string
  soDienThoaiNguoiNhan?: string
  emailNguoiNhan?: string
  ngayTao?: string
  ngayThanhToan?: string
  loaiDon?: boolean
  trangThai?: boolean
  deleted?: boolean
  ghiChu?: string
  createAt?: string
  createBy?: number
  updateAt?: string
  updateBy?: number
  tenNhanVien?: string
  maNhanVien?: string
  tenPhieuGiamGia?: string
  maPhieuGiamGia?: string
  maHoaDon?: string
  // Additional fields from response
  tenKhachHang?: string
  soDienThoaiKhachHang?: string
  soDienThoai?: string // From idKhachHang
  moTaLoaiDon?: string
  items?: any[]
  chiTietSanPham?: any[]
  thongTinDonHangs?: ThongTinDonHangModel[]
}

export interface BanHangTaiQuayRequest {
  idKhachHang?: number
  idPhieuGiamGia?: number
  idNhanVien?: number
  idPhuongThucThanhToan?: number
  idTrangThaiDonHang?: number
  danhSachSanPham?: Record<number, number> // key: idSanPham, value: soLuong
  phiVanChuyen?: number
  tongTien?: number
  tongTienSauGiam?: number
  tenNguoiNhan?: string
  diaChiNhanHang?: string
  soDienThoaiNguoiNhan?: string
  emailNguoiNhan?: string
  ngayTao?: string
  ngayThanhToan?: string
  loaiDon?: boolean
  trangThai?: boolean
  deleted?: boolean
  ghiChu?: string
  createAt?: string
  createBy?: number
  updateAt?: string
  updateBy?: number
  tenNhanVien?: string
  maNhanVien?: string
  tenPhieuGiamGia?: string
  maPhieuGiamGia?: string
}

export const fetchHoaDonList = () => requestJson<HoaDonApiModel[]>('/api/hoa-don-management/playlist')

export const fetchHoaDonById = (id: number) => requestJson<HoaDonApiModel>(`/api/hoa-don-management/${id}`)

export const createHoaDon = (data: Partial<BanHangTaiQuayRequest>) =>
  requestJson<HoaDonApiModel>('/api/hoa-don-management/add', {
    method: 'POST',
    data,
  })

export const updateHoaDon = (id: number, data: Partial<HoaDonApiModel>) =>
  requestJson<HoaDonApiModel>(`/api/hoa-don-management/update/${id}`, {
    method: 'PUT',
    data,
  })

export const deleteHoaDon = (id: number) =>
  requestJson<void>(`/api/hoa-don-management/delete/${id}`, {
    method: 'DELETE',
  })

// ==================== HOA DON CHI TIET APIs ====================
export interface HoaDonChiTietApiModel {
  idHoaDon: number
  idBienTheSanPham: number
  soLuong: number
  giaBan: number
  giaTriGiamGia?: number
  thanhTien: number
  ghiChu?: string
  trangThai?: boolean
  deleted?: boolean
  createAt?: string
  createBy?: number
  updateAt?: string
  updateBy?: number
}

export const createHoaDonChiTiet = (data: Partial<HoaDonChiTietApiModel>) =>
  requestJson<HoaDonChiTietApiModel>('/api/hoa-don-chi-tiet-management/add', {
    method: 'POST',
    data,
  })

// ==================== HINH THUC THANH TOAN APIs ====================
export interface HinhThucThanhToanApiModel {
  idHoaDon: number
  idPhuongThucThanhToan: number
  tienChuyenKhoan?: number
  tienMat?: number
  trangThai?: boolean
  deleted?: boolean
}

export const createHinhThucThanhToan = (data: Partial<HinhThucThanhToanApiModel>) =>
  requestJson<HinhThucThanhToanApiModel>('/api/hinh-thuc-thanh-toan-management/add', {
    method: 'POST',
    data,
  })

// ==================== THONG TIN HOA DON APIs ====================
export interface ThongTinHoaDonApiModel {
  idHoaDon: number
  idTrangThaiDonHang: number
  ghiChu?: string
}

export const createThongTinHoaDon = (data: Partial<ThongTinHoaDonApiModel>) =>
  requestJson<ThongTinHoaDonApiModel>('/api/thong-tin-hoa-don-management/add', {
    method: 'POST',
    data,
  })
