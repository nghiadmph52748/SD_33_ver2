import {
  createQrSession as apiCreateQrSession,
  updateQrSession as apiUpdateQrSession,
  cancelQrSession as apiCancelQrSession,
  generateQrForSession as apiGenerateQrForSession,
  type CreateQrSessionPayload,
  type QRSessionResponse,
} from '@/api/pos'

export const createQrPaymentSession = (payload: CreateQrSessionPayload): Promise<QRSessionResponse> => apiCreateQrSession(payload)

export const updateQrPaymentSession = (sessionId: string, payload: CreateQrSessionPayload): Promise<QRSessionResponse> =>
  apiUpdateQrSession(sessionId, payload)

export const cancelQrPaymentSession = (sessionId: string): Promise<void> => apiCancelQrSession(sessionId)

export const generateQrPayment = (sessionId: string): Promise<QRSessionResponse> => apiGenerateQrForSession(sessionId)
