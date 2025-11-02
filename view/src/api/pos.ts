import axios, { AxiosRequestConfig } from 'axios'
import { getToken } from '@/utils/auth'

const API_BASE = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'

interface ApiEnvelope<T> {
  isSuccess?: boolean
  success?: boolean
  data: T
  message?: string
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

// ==================== POS APIs ====================

export interface PhieuGiamGiaResponse {
  id: number
  maPhieuGiamGia: string
  tenPhieuGiamGia: string
  loaiPhieuGiamGia: boolean
  giaTriGiamGia: number
  hoaDonToiThieu: number
  soLuongDung: number
  ngayBatDau: string
  ngayKetThuc: string
  trangThai: boolean
  moTa: string
  featured: boolean
  [key: string]: any
}

/** Get all active coupons */
export const getPosActiveCoupons = () =>
  requestJson<PhieuGiamGiaResponse[]>('/api/pos/coupons', {
    method: 'GET',
  })

/** Get active coupons for specific customer (public + personal) */
export const getPosActiveCouponsForCustomer = (idKhachHang: number) =>
  requestJson<PhieuGiamGiaResponse[]>(`/api/pos/coupons/for-customer/${idKhachHang}`, {
    method: 'GET',
  })

/** Create a new invoice */
export const createInvoice = (idNhanVien: number) =>
  requestJson<number>('/api/pos/create-invoice', {
    method: 'POST',
    params: { createBy: idNhanVien },
  })

/** Delete an invoice */
export const deleteInvoice = (idHoaDon: number, idNhanVien: number) =>
  requestJson<void>('/api/pos/delete-invoice', {
    method: 'DELETE',
    params: { idHoaDon, idNhanVien },
  })

/** Add product to invoice detail */
export const addProductToInvoice = (idHoaDon: number, idChiTietSanPham: number, soLuong: number, idNhanVien: number) =>
  requestJson<number>('/api/pos/add-product-to-invoice-detail', {
    method: 'POST',
    params: { idHoaDon, idChiTietSanPham, soLuong, idNhanVien },
  })

/** Update product quantity in invoice detail */
export const updateProductQuantityInInvoice = (idHoaDonChiTiet: number, soLuong: number, idNhanVien: number) =>
  requestJson<void>('/api/pos/update-product-quantity', {
    method: 'PUT',
    params: { idHoaDonChiTiet, soLuong, idNhanVien },
  })

/** Delete products from invoice detail */
export const deleteProductsFromInvoice = (idHoaDonChiTiet: number[], idNhanVien: number) =>
  requestJson<void>('/api/pos/delete', {
    method: 'DELETE',
    params: { idHoaDonChiTiet: idHoaDonChiTiet.join(','), idNhanVien },
  })

/** Update customer info for invoice */
export interface UpdateCustomerRequest {
  idHoaDon: number
  idKhachHang?: number
  tenKhachHang?: string
  soDienThoai?: string
  diaChiKhachHang?: string
  emailKhachHang?: string
  idNhanVien: number
}

export const updateCustomerForInvoice = (data: UpdateCustomerRequest) =>
  requestJson<number>('/api/pos/update-customer', {
    method: 'PUT',
    data,
  })

/** Update shipping method */
export const updateShippingMethod = (idHoaDon: number, idNhanVien: number) =>
  requestJson<void>('/api/pos/update-giao-hang', {
    method: 'PUT',
    params: { idHoaDon, idNhanVien },
  })

/** Update payment method */
export interface UpdatePaymentMethodRequest {
  idHoaDon: number
  idPTTT?: number
  idNhanVien: number
}

export const updatePaymentMethod = (data: UpdatePaymentMethodRequest) =>
  requestJson<void>('/api/pos/update-payment-method', {
    method: 'PUT',
    data,
  })

/** Update voucher for invoice */
export interface UpdateVoucherRequest {
  idHoaDon: number
  idPhieuGiamGia?: number
  idNhanVien: number
}

export const updateVoucher = (data: UpdateVoucherRequest) =>
  requestJson<number>('/api/pos/update-voucher', {
    method: 'PUT',
    data,
  })

/** Confirm and complete the entire order */
export interface ConfirmBanHangRequest {
  idHoaDon: number
  idKhachHang?: number
  tenKhachHang?: string
  soDienThoai?: string
  diaChiKhachHang?: string
  emailKhachHang?: string
  idPTTT?: number
  idPhieuGiamGia?: number
  idNhanVien: number
  [key: string]: any
}

export const confirmPosOrder = (data: ConfirmBanHangRequest) =>
  requestJson<void>('/api/pos/confirm', {
    method: 'POST',
    data,
  })
