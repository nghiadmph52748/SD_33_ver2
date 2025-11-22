import client from './client'

export interface VnpayCreatePaymentRequest {
  amount: number
  orderId?: string
  orderInfo?: string
  bankCode?: string
  locale?: string
}

export interface VnpayCreatePaymentResponse {
  payUrl: string
  txnRef: string
}

export const createVnpayPayment = (data: VnpayCreatePaymentRequest) =>
  client.post<VnpayCreatePaymentResponse, VnpayCreatePaymentResponse>('/api/payment/vnpay/create', data)

