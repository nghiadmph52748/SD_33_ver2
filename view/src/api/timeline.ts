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

export interface TimelineItem {
  id: number
  idHoaDon: number
  idNhanVien?: number
  tenNhanVien?: string
  trangThaiCu?: string
  trangThaiMoi: string
  hanhDong: string
  moTa?: string
  ghiChu?: string
  thoiGian: string
  ipAddress?: string
  userAgent?: string
}

export const fetchTimelineByHoaDonId = (hoaDonId: number) => requestJson<TimelineItem[]>(`/api/timeline-don-hang/${hoaDonId}`)

export const createTimelineItem = (data: Partial<TimelineItem>) =>
  requestJson<TimelineItem>('/api/timeline-don-hang', {
    method: 'POST',
    body: JSON.stringify(data),
  })

export const updateTimelineItem = (id: number, data: Partial<TimelineItem>) =>
  requestJson<TimelineItem>(`/api/timeline-don-hang/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  })

export const deleteTimelineItem = (id: number) =>
  requestJson<void>(`/api/timeline-don-hang/${id}`, {
    method: 'DELETE',
  })
