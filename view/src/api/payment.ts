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

export interface CreateVnpayPaymentPayload {
  amount: number
  orderId?: string
  orderInfo?: string
  bankCode?: string
  locale?: string
}

export interface VnpayPaymentResponse {
  payUrl: string
  txnRef: string
}

export const createVnpayPayment = (payload: CreateVnpayPaymentPayload) =>
  requestJson<VnpayPaymentResponse>('/payment/vnpay/create', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
