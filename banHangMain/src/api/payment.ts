import axios from '@/api/interceptor'

export interface CreateVnPayPayload {
  amount: number
  orderId?: string
  orderInfo?: string
  bankCode?: string
  locale?: 'vn' | 'en'
}

export interface CreateVnPayResponse {
  payUrl: string
  txnRef: string
}

export interface CheckPendingPaymentResponse {
  hasPending: boolean
  txnRef?: string
  minutesSinceCreation?: number
  isExpired?: boolean
  message?: string
}

export async function checkPendingVnPayPayment(orderId?: string): Promise<CheckPendingPaymentResponse> {
  if (!orderId) {
    return { hasPending: false }
  }
  const response = await axios.get<CheckPendingPaymentResponse>('/api/payment/vnpay/check-pending', {
    params: { orderId },
  })
  return response.data
}

export async function createVnPayPayment(payload: CreateVnPayPayload) {
  return axios.post<CreateVnPayResponse>('/api/payment/vnpay/create', payload)
}

export interface CreateMoMoPayload {
  amount: number
  orderId?: string
  orderInfo?: string
}

export interface CreateMoMoResponse {
  payUrl: string
  qrCodeUrl?: string
  deeplink?: string
  orderId: string
  requestId: string
}

/**
 * Create MoMo credit card payment
 * Redirects user to MoMo payment gateway for credit card processing
 */
export async function createMoMoPayment(payload: CreateMoMoPayload) {
  return axios.post<CreateMoMoResponse>('/api/payment/momo/create', payload)
}

/**
 * Create MoMo credit card payment (explicit endpoint)
 */
export async function createMoMoCreditCardPayment(payload: CreateMoMoPayload) {
  return axios.post<CreateMoMoResponse>('/api/payment/momo/create-credit-card', payload)
}

