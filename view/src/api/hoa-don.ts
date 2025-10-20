import { getToken } from '@/utils/auth'

const API_BASE = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'

interface ApiEnvelope<T> {
  isSuccess?: boolean
  success?: boolean
  data: T
  message?: string
}

const buildUrl = (path: string): string => {
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
}

export interface HoaDonApiModel {
  id: number
  maHoaDon: string
  tenHoaDon?: string
  loaiDon: boolean
  moTaLoaiDon?: string
  thoiGianTao?: string
  ngayTao?: string
  ngayThanhToan?: string
  tongTien: number
  tongTienSauGiam: number
  phiVanChuyen?: number
  ghiChu?: string
  trangThai: boolean
  deleted: boolean

  // Thông tin khách hàng
  tenKhachHang?: string
  soDienThoaiKhachHang?: string
  emailKhachHang?: string
  diaChiKhachHang?: string

  // Thông tin nhân viên
  tenNhanVien?: string
  maNhanVien?: string
  soDienThoaiNhanVien?: string

  // Thông tin phiếu giảm giá
  tenPhieuGiamGia?: string
  maPhieuGiamGia?: string

  // Thông tin bổ sung
  thoiGianCapNhat?: string
  ghiChuNoiBo?: string
  maVanDon?: string
  phuongThucGiaoHang?: string
  diaChiGiaoHangChiTiet?: string
  trangThaiThanhToan?: number
  soTienDaThanhToan?: number
  soTienConLai?: number
}

export const fetchHoaDonList = () => requestJson<HoaDonApiModel[]>('/api/hoa-don-management/playlist')

export const fetchHoaDonById = (id: number) => requestJson<HoaDonApiModel>(`/api/hoa-don-management/${id}`)

export const createHoaDon = (data: Partial<HoaDonApiModel>) =>
  requestJson<HoaDonApiModel>('/api/hoa-don-management', {
    method: 'POST',
    body: JSON.stringify(data),
  })

export const updateHoaDon = (id: number, data: Partial<HoaDonApiModel>) =>
  requestJson<HoaDonApiModel>(`/api/hoa-don-management/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  })

export const deleteHoaDon = (id: number) =>
  requestJson<void>(`/api/hoa-don-management/${id}`, {
    method: 'DELETE',
  })
