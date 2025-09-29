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

  if (payload.data === undefined) {
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
}

export interface CouponApiModel {
  id: number
  maPhieuGiamGia: string
  tenPhieuGiamGia: string
  loaiPhieuGiamGia: boolean
  giaTriGiamGia: number | string | null
  soTienToiDa: number | string | null
  hoaDonToiThieu: number | string | null
  soLuongDung: number | null
  ngayBatDau: string
  ngayKetThuc: string
  trangThai: boolean
  moTa: string | null
  deleted: boolean
  idKhachHang?: number[]
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
  tenPhieuGiamGia: string
  loaiPhieuGiamGia: boolean
  giaTriGiamGia: number
  soTienToiDa: number | null
  hoaDonToiThieu: number | null
  soLuongDung: number | null
  ngayBatDau: string
  ngayKetThuc: string
  trangThai: boolean
  moTa?: string | null
  deleted?: boolean
  idKhachHang?: number[]
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
