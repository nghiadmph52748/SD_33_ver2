import { getToken } from '@/utils/auth'

const DEFAULT_API_BASE = 'http://localhost:8080/api'
const resolveBaseUrl = () => {
  const configured = (import.meta as any).env?.VITE_API_BASE_URL?.trim?.()
  if (configured && configured.length > 0) {
    return configured.replace(/\/$/, '')
  }
  return DEFAULT_API_BASE
}

const API_BASE = resolveBaseUrl()

interface ApiEnvelope<T> {
  isSuccess?: boolean
  success?: boolean
  data: T
  message?: string
}

const buildUrl = (path: string) => {
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path
  }
  if (path.startsWith('/')) {
    return `${API_BASE}${path}`
  }
  return `${API_BASE}/${path}`
}

const requestJson = async <T>(path: string, init?: RequestInit): Promise<T> => {
  const token = getToken()
  const response = await fetch(buildUrl(path), {
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(init?.headers ?? {}),
    },
    ...init,
  })

  if (!response.ok) {
    throw new Error(`Request failed with status ${response.status}`)
  }

  const payload = (await response.json()) as ApiEnvelope<T>

  // For update/delete operations, data might be null but message indicates success
  if (payload.data === undefined && !payload.message) {
    throw new Error('Malformed response payload')
  }

  // Check success flag - if false, check if message indicates success
  const successFlag = payload.isSuccess ?? payload.success
  if (successFlag === false) {
    const normalizedMessage = (payload.message ?? '').toLowerCase()
    const isPositive = normalizedMessage.includes('success') || normalizedMessage.includes('thành công')
    if (!isPositive) {
      throw new Error(payload.message ?? 'Yêu cầu thất bại')
    }
    // If message indicates success despite isSuccess=false, treat as success
    // This handles the case where backend returns null data with success message
  }

  return payload.data
}

export interface PromotionApiModel {
  id: number
  maDotGiamGia: string
  tenDotGiamGia: string
  giaTriGiamGia: number | string | null
  ngayBatDau: string
  ngayKetThuc: string
  trangThai: boolean
  deleted: boolean
  createdAt?: string
  updatedAt?: string
}

export interface CouponApiModel {
  id: number
  maPhieuGiamGia: string
  tenPhieuGiamGia: string
  loaiPhieuGiamGia: boolean
  giaTriGiamGia: number | string | null
  hoaDonToiThieu: number | string | null
  soLuongDung: number | null
  soLuongDaDung?: number | null
  ngayBatDau: string
  ngayKetThuc: string
  trangThai: boolean
  moTa: string | null
  deleted: boolean
  idKhachHang?: number[]
  featured?: boolean
  createdAt?: string
  updatedAt?: string
}

export const fetchPromotionCampaigns = () => requestJson<PromotionApiModel[]>('/dot-giam-gia-management/playlist')

export const fetchCoupons = () => requestJson<CouponApiModel[]>('/phieu-giam-gia-management/playlist')

export interface CreatePromotionPayload {
  tenDotGiamGia: string
  giaTriGiamGia: number
  ngayBatDau: string
  ngayKetThuc: string
  trangThai: boolean
  deleted?: boolean
  lyDoThayDoi?: string
}

export const createPromotionCampaign = (payload: CreatePromotionPayload) =>
  requestJson<null>('/dot-giam-gia-management/add', {
    method: 'POST',
    body: JSON.stringify(payload),
  })

export const updatePromotionCampaign = (id: number, payload: CreatePromotionPayload) =>
  requestJson<null>(`/dot-giam-gia-management/update/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })

export const deletePromotionCampaign = (id: number) =>
  requestJson<null>(`/dot-giam-gia-management/delete/${id}`, {
    method: 'DELETE',
  })

export interface CreateCouponPayload {
  maPhieuGiamGia?: string
  tenPhieuGiamGia?: string
  loaiPhieuGiamGia: boolean
  giaTriGiamGia: number
  hoaDonToiThieu: number | null
  soLuongDung: number | null
  ngayBatDau: string
  ngayKetThuc: string
  trangThai: boolean
  moTa?: string | null
  deleted?: boolean
  idKhachHang?: number[]
  featured?: boolean
  lyDoThayDoi?: string
}

export const createCoupon = (payload: CreateCouponPayload) =>
  requestJson<null>('/phieu-giam-gia-management/add', {
    method: 'POST',
    body: JSON.stringify(payload),
  })

export const updateCoupon = (id: number, payload: CreateCouponPayload) =>
  requestJson<null>(`/phieu-giam-gia-management/update/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })

export const deleteCoupon = (id: number) =>
  requestJson<null>(`/phieu-giam-gia-management/delete/${id}`, {
    method: 'DELETE',
  })

// Customer API
export interface CustomerApiModel {
  id: number
  maKhachHang?: string
  tenKhachHang: string
  tenTaiKhoan?: string
  soDienThoai: string
  email?: string
  diaChi?: string
  ngaySinh?: string
  gioiTinh?: boolean
  matKhau?: string
  trangThai?: boolean | number
  phanLoai?: string | null
  phanLoaiText?: string | null
  tongChiTieu?: number | null
  tongDon?: number | null
  createdAt?: string
  updatedAt?: string
  // RFM fields from vw_khach_hang_rfm
  ordersCount?: number
  firstOrderAt?: string
  lastOrderAt?: string
  totalSpent?: number
  daysSinceLastOrder?: number
}

export const fetchCustomers = () => requestJson<CustomerApiModel[]>('/khach-hang-management/playlist')

export interface FetchCustomersBySegmentParams {
  segmentType?: 'behavior' | 'rfm' | 'event' | 'all'
  segmentKey?: string
  search?: string
  minOrdersCount?: number
  maxOrdersCount?: number
  minTotalSpent?: number
  maxTotalSpent?: number
  lastOrderFrom?: string
  lastOrderTo?: string
  page?: number
  pageSize?: number
  birthdayDays?: number
}

export interface PagedResponse<T> {
  content: T[]
  totalElements: number
  page: number
  size: number
  totalPages?: number
}

export const fetchCustomersBySegment = (params?: FetchCustomersBySegmentParams) => {
  const queryParams = new URLSearchParams()
  if (params?.segmentType) queryParams.append('segmentType', params.segmentType)
  if (params?.segmentKey) queryParams.append('segmentKey', params.segmentKey)
  if (params?.search) queryParams.append('search', params.search)
  if (params?.minOrdersCount !== undefined) queryParams.append('minOrdersCount', String(params.minOrdersCount))
  if (params?.maxOrdersCount !== undefined) queryParams.append('maxOrdersCount', String(params.maxOrdersCount))
  if (params?.minTotalSpent !== undefined) queryParams.append('minTotalSpent', String(params.minTotalSpent))
  if (params?.maxTotalSpent !== undefined) queryParams.append('maxTotalSpent', String(params.maxTotalSpent))
  if (params?.lastOrderFrom) queryParams.append('lastOrderFrom', params.lastOrderFrom)
  if (params?.lastOrderTo) queryParams.append('lastOrderTo', params.lastOrderTo)
  if (params?.page !== undefined) queryParams.append('page', String(params.page))
  if (params?.pageSize !== undefined) queryParams.append('pageSize', String(params.pageSize))
  if (params?.birthdayDays !== undefined) queryParams.append('birthdayDays', String(params.birthdayDays))
  
  const queryString = queryParams.toString()
  return requestJson<PagedResponse<CustomerApiModel>>(`/khach-hang-management/segment${queryString ? `?${queryString}` : ''}`)
}

// Coupon Product Detail API
export interface CouponProductDetailApiModel {
  id: number
  idPhieuGiamGia: number
  idChiTietSanPham: number
}

export const fetchCouponProductDetails = (couponId: number) =>
  requestJson<CouponProductDetailApiModel[]>(`/chi-tiet-phieu-giam-gia-management/by-phieu-giam-gia/${couponId}`)

// Coupon History API
export interface CouponHistoryApiModel {
  id: number
  idPhieuGiamGia: number
  idNhanVien: number
  tenNhanVien?: string
  maNhanVien?: string
  hanhDong: string
  moTaThayDoi?: string
  ngayThayDoi: string
}

export const fetchCouponHistory = (couponId: number) =>
  requestJson<CouponHistoryApiModel[]>(`/phieu-giam-gia-management/history/${couponId}`)

// Promotion Campaign History API
export interface PromotionHistoryApiModel {
  id: number
  idDotGiamGia: number
  idNhanVien: number
  tenNhanVien?: string
  maNhanVien?: string
  hanhDong: string
  moTaThayDoi?: string
  ngayThayDoi: string
}

export const fetchPromotionHistory = (promotionId: number) =>
  requestJson<PromotionHistoryApiModel[]>(`/dot-giam-gia-management/history/${promotionId}`)

// Promotion Product Detail API
export interface PromotionProductDetailApiModel {
  id: number
  idDotGiamGia: number
  idChiTietSanPham: number
  tenSanPham?: string
  maChiTietSanPham?: string
}

export interface CreatePromotionProductDetailPayload {
  idDotGiamGia: number
  idChiTietSanPham: number
  deleted?: boolean
}

export const fetchPromotionProductDetails = (promotionId: number) =>
  requestJson<PromotionProductDetailApiModel[]>(`/chi-tiet-dot-giam-gia-management/by-dot-giam-gia/${promotionId}`)

export const createPromotionProductDetailsBatch = (payload: CreatePromotionProductDetailPayload[]) =>
  requestJson<null>('/chi-tiet-dot-giam-gia-management/add-batch', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
