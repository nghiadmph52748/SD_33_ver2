import axios, { AxiosRequestConfig } from 'axios'
import { getToken } from '@/utils/auth'

const API_BASE = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'

interface ApiEnvelope<T> {
  isSuccess?: boolean
  success?: boolean
  data: T
  message?: string
}

export interface OrderStatusTimelineModel {
  id?: number
  idHoaDon?: number
  idTrangThaiDonHang?: number
  tenTrangThaiDonHang?: string
  thoiGian?: string
  ghiChu?: string
  trangThai?: boolean
  deleted?: boolean
}

export interface OrderStageApiModel {
  statusId?: number
  code: string
  name: string
  timestamp?: string
  reached?: boolean
  completed?: boolean
  current?: boolean
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

export interface InvoiceApiModel {
  id?: number
  idKhachHang?: number
  idPhieuGiamGia?: number
  idNhanVien?: number
  idPhuongThucThanhToan?: number
  idTrangThaiDonHang?: number
  phiVanChuyen?: number
  phuPhi?: number
  hoanPhi?: number
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
  tenKhachHang?: string
  soDienThoaiKhachHang?: string
  soDienThoai?: string
  moTaLoaiDon?: string
  items?: any[]
  chiTietSanPham?: any[]
  thongTinDonHangs?: OrderStatusTimelineModel[]
  orderStages?: OrderStageApiModel[]
}

export interface InvoiceRequestPayload {
  idKhachHang?: number
  idPhieuGiamGia?: number
  idNhanVien?: number
  idPhuongThucThanhToan?: number
  idTrangThaiDonHang?: number
  danhSachSanPham?: Record<number, number>
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

export const fetchInvoiceList = () => requestJson<InvoiceApiModel[]>('/api/invoice-management/playlist')

export const fetchInvoiceById = (id: number) => requestJson<InvoiceApiModel>(`/api/invoice-management/${id}`)

export const fetchInvoiceByCode = (code: string) => requestJson<InvoiceApiModel>(`/api/hoa-don-management/code/${encodeURIComponent(code)}`)

export const createInvoice = (data: Partial<InvoiceRequestPayload>) =>
  requestJson<InvoiceApiModel>('/api/invoice-management/add', {
    method: 'POST',
    data,
  })

export const updateInvoice = (id: number, data: Partial<InvoiceApiModel>) =>
  requestJson<InvoiceApiModel>(`/api/invoice-management/update/${id}`, {
    method: 'PUT',
    data,
  })

export const deleteInvoice = (id: number) =>
  requestJson<void>(`/api/invoice-management/delete/${id}`, {
    method: 'DELETE',
  })

export interface InvoiceLineItemPayload {
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

export const createInvoiceLineItem = (data: Partial<InvoiceLineItemPayload>) =>
  requestJson<InvoiceLineItemPayload>('/api/hoa-don-chi-tiet-management/add', {
    method: 'POST',
    data,
  })

export interface PaymentHistoryPayload {
  idHoaDon: number
  idPhuongThucThanhToan: number
  tienChuyenKhoan?: number
  tienMat?: number
  trangThai?: boolean
  deleted?: boolean
}

export const createPaymentHistory = (data: Partial<PaymentHistoryPayload>) =>
  requestJson<PaymentHistoryPayload>('/api/hinh-thuc-thanh-toan-management/add', {
    method: 'POST',
    data,
  })

export interface OrderInfoPayload {
  idHoaDon: number
  idTrangThaiDonHang: number
  ghiChu?: string
}

export const createOrderInfo = (data: Partial<OrderInfoPayload>) =>
  requestJson<OrderInfoPayload>('/api/thong-tin-hoa-don-management/add', {
    method: 'POST',
    data,
  })
