import client from './client'

export interface CartItem {
  productId: string
  productName: string
  price: number
  discount: number
  quantity: number
  image?: string
  tenMauSac?: string
  tenKichThuoc?: string
  tenDeGiay?: string
  tenChatLieu?: string
}

export interface QRSession {
  qrSessionId: string
  qrCodeUrl: string
  orderCode: string
  items: CartItem[]
  subtotal: number
  discountAmount: number
  shippingFee: number
  finalPrice: number
  status: 'PENDING' | 'PAID' | 'EXPIRED' | 'FAILED' | 'CANCELLED'
  expiresAt: string
  createdAt: string
  customerName?: string
  customerPhone?: string
  customerEmail?: string
}

export const getQRSession = (sessionId: string) =>
  client.get<QRSession, QRSession>(`/api/pos/qr-session/${sessionId}`)

export const getLatestQRSession = () =>
  client.get<QRSession | null, QRSession | null>('/api/pos/qr-session/latest')

export const createQRSession = (data: {
  orderCode: string
  invoiceId?: number
  items: CartItem[]
  subtotal: number
  discountAmount: number
  shippingFee: number
  finalPrice: number
  customerId?: string
}) =>
  client.post<QRSession, QRSession>('/api/pos/qr-session', data)

