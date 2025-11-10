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

export async function createVnPayPayment(payload: CreateVnPayPayload) {
  return axios.post<CreateVnPayResponse>('/api/payment/vnpay/create', payload)
}


