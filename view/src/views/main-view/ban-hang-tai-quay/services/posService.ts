import {
  createInvoice as apiCreateInvoice,
  deleteInvoice as apiDeleteInvoice,
  addProductToInvoice as apiAddProductToInvoice,
  updateProductQuantityInInvoice as apiUpdateProductQuantityInInvoice,
  deleteProductsFromInvoice as apiDeleteProductsFromInvoice,
  updateCustomerForInvoice as apiUpdateCustomerForInvoice,
  updateShippingMethod as apiUpdateShippingMethod,
  updatePaymentMethod as apiUpdatePaymentMethod,
  updateVoucher as apiUpdateVoucher,
  confirmPosOrder as apiConfirmPosOrder,
  getPosActiveCoupons as apiGetPosActiveCoupons,
  getPosActiveCouponsForCustomer as apiGetPosActiveCouponsForCustomer,
  getInvoiceTimeline as apiGetInvoiceTimeline,
  type UpdatePaymentMethodRequest,
  type UpdateVoucherRequest,
  type UpdateCustomerRequest,
  type ConfirmBanHangRequest,
  type TimelineEntry,
} from '@/api/pos'

// Thin wrappers + convenience helpers for POS-related server operations
export async function createInvoice(userId: number) {
  return apiCreateInvoice(userId)
}

export async function deleteInvoice(invoiceId: number, userId: number) {
  return apiDeleteInvoice(invoiceId, userId)
}

export async function addProductToInvoice(invoiceId: number, productId: number, quantity: number, userId: number) {
  return apiAddProductToInvoice(invoiceId, productId, quantity, userId)
}

export async function updateProductQuantityInInvoice(detailId: number, quantity: number, userId: number) {
  return apiUpdateProductQuantityInInvoice(detailId, quantity, userId)
}

export async function deleteProductsFromInvoice(detailIds: number[], userId: number) {
  return apiDeleteProductsFromInvoice(detailIds, userId)
}

export async function updateCustomerForInvoice(req: UpdateCustomerRequest) {
  return apiUpdateCustomerForInvoice(req)
}

export async function updateShippingMethod(invoiceId: number, userId: number) {
  return apiUpdateShippingMethod(invoiceId, userId)
}

export async function updatePaymentMethod(invoiceId: number, method: 'cash' | 'transfer' | 'both', userId: number) {
  // eslint-disable-next-line no-nested-ternary
  const idPTTT = method === 'cash' ? 1 : method === 'transfer' ? 2 : 3
  const payload: UpdatePaymentMethodRequest = { idHoaDon: invoiceId, idPTTT, idNhanVien: userId }
  return apiUpdatePaymentMethod(payload)
}

export async function updateVoucher(invoiceId: number, voucherId: number, userId: number) {
  const payload: UpdateVoucherRequest = { idHoaDon: invoiceId, idPhieuGiamGia: voucherId, idNhanVien: userId }
  return apiUpdateVoucher(payload)
}

export async function confirmPosOrder(req: ConfirmBanHangRequest) {
  return apiConfirmPosOrder(req)
}

export async function getPosActiveCoupons() {
  return apiGetPosActiveCoupons()
}

export async function getPosActiveCouponsForCustomer(customerId: number) {
  return apiGetPosActiveCouponsForCustomer(customerId)
}

export async function getInvoiceTimeline(invoiceId: number) {
  return apiGetInvoiceTimeline(invoiceId)
}
