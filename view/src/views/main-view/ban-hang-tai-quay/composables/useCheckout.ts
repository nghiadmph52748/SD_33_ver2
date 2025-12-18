import { ref, type Ref } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import type { CouponApiModel } from '@/api/discount-management'
import type { ConfirmBanHangRequest } from '@/api/pos'
import { validateInvoiceBeforeConfirm } from '@/api/pos'

interface CartItem {
  id: string
  productId: string
  productName: string
  price: number
  discount: number
  quantity: number
}

interface Order {
  id: string
  orderCode: string
  items: CartItem[]
  customerId: string | null
  createdAt: Date
}

interface Customer {
  id: string
  name: string
  phone: string
  email?: string
  address?: string
}

export default function useCheckout(params: {
  currentOrder: Ref<Order | null>
  selectedCustomer: Ref<Customer | null>
  paymentForm: Ref<{
    discountCode: string | null
    method: 'cash' | 'transfer' | 'both'
    cashReceived: number | null
    transferReceived: number | null
  }>
  orderType: Ref<'counter' | 'delivery'>
  finalPrice: Ref<number>
  subtotal: Ref<number>
  selectedCoupon: Ref<CouponApiModel | null>
  calculateVoucherDiscount: (coupon: CouponApiModel | null | undefined) => number
  coupons: Ref<CouponApiModel[]>
  userId: number
  userName?: string
  orders: Ref<Order[]>
  currentOrderIndex: Ref<string>
  shippingFee: Ref<number>
  walkInLocation: Ref<{
    thanhPho: string
    quan: string
    phuong: string
    diaChiCuThe: string
    districts: Array<{ value: string; label: string; code: number }>
    wards: Array<{ value: string; label: string }>
  }>
  walkInName: Ref<string>
  walkInEmail: Ref<string>
  walkInPhone: Ref<string>
  walkInDeliveryValid: Ref<boolean>
  confirmPosOrder: (req: ConfirmBanHangRequest) => Promise<any>
  onBeforeOrderRemoved?: (orderData: {
    order: Order
    customer: Customer | null
    voucher: CouponApiModel | null
    orderType: 'counter' | 'delivery'
    shippingFee: number
    walkInData: {
      name: string
      email: string
      phone: string
      location: any
    }
  }) => void
}) {
  const {
    currentOrder,
    selectedCustomer,
    paymentForm,
    orderType,
    finalPrice,
    subtotal,
    selectedCoupon,
    calculateVoucherDiscount,
    coupons,
    userId,
    userName,
    orders,
    currentOrderIndex,
    shippingFee,
    walkInLocation,
    walkInName,
    walkInEmail,
    walkInPhone,
    walkInDeliveryValid,
    confirmPosOrder,
    onBeforeOrderRemoved,
  } = params

  const showConfirmOrderModal = ref(false)
  const suggestedBetterVouchers = ref<CouponApiModel[]>([])
  const confirmOrderRequest = ref<ConfirmBanHangRequest | null>(null)
  const confirmLoading = ref(false)

  const checkBetterVouchers = (): CouponApiModel[] => {
    if (!coupons.value || coupons.value.length === 0) return []

    const orderSubtotal =
      currentOrder.value?.items.reduce((sum: number, item: CartItem) => {
        const discountedPrice = item.discount > 0 ? item.price * (1 - item.discount / 100) : item.price
        return sum + discountedPrice * item.quantity
      }, 0) || 0

    const getActualDiscount = (coupon: CouponApiModel): number => {
      return calculateVoucherDiscount(coupon)
    }

    return coupons.value.filter((coupon) => {
      if (selectedCoupon.value?.id === coupon.id) return false
      if (orderSubtotal < (coupon.hoaDonToiThieu || 0)) return false
      if (selectedCoupon.value) {
        const betterDiscount = getActualDiscount(coupon)
        const currentDiscount = getActualDiscount(selectedCoupon.value)
        return betterDiscount > currentDiscount
      }
      return true
    })
  }

  // eslint-disable-next-line consistent-return
  const confirmOrder = async () => {
    try {
      if (!currentOrder.value?.id) {
        throw new Error('Vui lòng tạo hóa đơn trước')
      }
      const invoiceId = parseInt(currentOrder.value.id, 10)
      const rawCustomerId = currentOrder.value.customerId
      const isWalkIn = !selectedCustomer.value && (rawCustomerId === '' || rawCustomerId === null || rawCustomerId === undefined)

      // Prepare order request data
      let walkInAddress = ''
      if (isWalkIn) {
        const addressParts = [
          walkInLocation.value.diaChiCuThe,
          walkInLocation.value.phuong,
          walkInLocation.value.quan,
          walkInLocation.value.thanhPho,
        ]
          .map((part) => (typeof part === 'string' ? part.trim() : part))
          .filter(Boolean)
        walkInAddress = addressParts.join(', ')
      }

      const walkInNameValue = walkInName.value.trim()
      const walkInPhoneValue = walkInPhone.value.trim()
      const walkInEmailValue = walkInEmail.value.trim()

      const customerId = selectedCustomer.value?.id ? parseInt(selectedCustomer.value.id, 10) : undefined
      // eslint-disable-next-line no-nested-ternary
      const paymentMethodId = paymentForm.value.method === 'cash' ? 1 : paymentForm.value.method === 'transfer' ? 2 : 3

      // Calculate payment amounts based on payment method
      let tienMat = 0
      let tienChuyenKhoan = 0
      if (paymentForm.value.method === 'cash') {
        tienMat = paymentForm.value.cashReceived > 0 ? paymentForm.value.cashReceived : finalPrice.value
      } else if (paymentForm.value.method === 'transfer') {
        tienChuyenKhoan = paymentForm.value.transferReceived > 0 ? paymentForm.value.transferReceived : finalPrice.value
      } else if (paymentForm.value.method === 'both') {
        const total = Math.max(0, finalPrice.value)
        tienMat = paymentForm.value.cashReceived > 0 ? Math.min(paymentForm.value.cashReceived, total) : 0
        tienChuyenKhoan = Math.max(0, total - tienMat)
      }

      const totalReceived = tienMat + tienChuyenKhoan
      const soTienConLai = Math.max(0, finalPrice.value - totalReceived)

      // Calculate discount amount
      const discountAmount = calculateVoucherDiscount(selectedCoupon.value)
      // tongTienSauGiam should be the subtotal minus voucher discount (without shipping/fees)
      const tongTienSauGiam = Math.max(0, subtotal.value - discountAmount)

      const req: ConfirmBanHangRequest = {
        idHoaDon: invoiceId,
        idKhachHang: customerId ?? null,
        tenKhachHang: selectedCustomer.value?.name || walkInNameValue || 'Khách lẻ',
        soDienThoai: selectedCustomer.value?.phone ?? (isWalkIn ? walkInPhoneValue || null : null),
        diaChiKhachHang: selectedCustomer.value?.address || (isWalkIn ? walkInAddress || null : null),
        emailKhachHang: selectedCustomer.value?.email ?? (isWalkIn ? walkInEmailValue || null : null),
        idPTTT: paymentMethodId,
        idPhieuGiamGia: selectedCoupon.value?.id ? parseInt(selectedCoupon.value.id, 10) : null,
        idNhanVien: userId,
        tienMat,
        tienChuyenKhoan,
        soTienConLai,
        phiVanChuyen: orderType.value === 'delivery' ? shippingFee.value : 0,
        trangThaiThanhToan: totalReceived >= finalPrice.value,
        tongTien: subtotal.value,
        tongTienSauGiam: tongTienSauGiam,
      }

      confirmOrderRequest.value = req
      // Show the new confirm order modal
      showConfirmOrderModal.value = true
    } catch (error: any) {
      console.error('Lỗi khi mở dialog xác nhận:', error)
      Message.error(error.message || 'Có lỗi xảy ra. Vui lòng thử lại.')
    }
  }

  const doConfirmOrder = async () => {
    try {
      confirmLoading.value = true
      if (!confirmOrderRequest.value) throw new Error('Thiếu dữ liệu đơn hàng để xác nhận')

      const invoiceId = confirmOrderRequest.value.idHoaDon
      const paymentMethod = paymentForm.value.method

      // Validate invoice before confirming
      try {
        const validationResult = await validateInvoiceBeforeConfirm(invoiceId)
        if (!validationResult.isValid && validationResult.inactiveVariants.length > 0) {
          const variantNames = validationResult.inactiveVariants
            .map((v) => {
              const parts = [v.tenSanPham]
              if (v.mauSac) parts.push(`Màu: ${v.mauSac}`)
              if (v.kichThuoc) parts.push(`Size: ${v.kichThuoc}`)
              return parts.join(' - ')
            })
            .join(', ')
          throw new Error(
            `Không thể xác nhận đơn hàng. Các sản phẩm sau đã bị vô hiệu hóa: ${variantNames}. Vui lòng xóa các sản phẩm này khỏi đơn hàng.`
          )
        }
      } catch (validationError: any) {
        // If validation fails, show error and stop
        if (validationError.message) {
          Message.error(validationError.message)
        } else {
          Message.error('Lỗi khi kiểm tra đơn hàng. Vui lòng thử lại.')
        }
        return
      }

      await confirmPosOrder(confirmOrderRequest.value)

      // Backend đã tự động tạo timeline khi confirm order:
      // - "Hoàn thành" với thời gian chính xác từ Instant.now()
      // Không cần tạo timeline từ frontend nữa để tránh duplicate và đảm bảo thời gian chính xác

      const orderTypeText = orderType.value === 'delivery' ? 'giao hàng' : 'tại quầy'
      let successMessage = ` Đơn ${orderTypeText} ${currentOrder.value!.orderCode} xác nhận thành công!`
      if (selectedCoupon.value) successMessage += ` (Áp dụng: ${selectedCoupon.value.tenPhieuGiamGia})`
      Message.success(successMessage)

      try {
        const orderBroadcastChannel = new BroadcastChannel('order-update-channel')
        orderBroadcastChannel.postMessage({
          type: 'ORDER_CONFIRMED',
          invoiceId: currentOrder.value!.id,
          orderCode: currentOrder.value!.orderCode,
          timestamp: new Date().toISOString(),
        })
        orderBroadcastChannel.close()
      } catch (error) {
        console.warn('BroadcastChannel broadcast failed:', error)
      }

      // Cache order data before removal for post-checkout operations (e.g., printing)
      if (onBeforeOrderRemoved && currentOrder.value) {
        onBeforeOrderRemoved({
          order: currentOrder.value,
          customer: selectedCustomer.value,
          voucher: selectedCoupon.value,
          orderType: orderType.value,
          shippingFee: shippingFee.value,
          walkInData: {
            name: walkInName.value,
            email: walkInEmail.value,
            phone: walkInPhone.value,
            location: walkInLocation.value,
          },
        })
      }

      const currentOrderIdx = parseInt(currentOrderIndex.value, 10)
      if (currentOrderIdx >= 0 && currentOrderIdx < orders.value.length) {
        orders.value.splice(currentOrderIdx, 1)
      }
      if (orders.value.length > 0) {
        // After removing an order, select the next available order:
        // - If we removed the last order, select the new last order (length - 1)
        // - Otherwise, keep the same index (which now points to the next order)
        const newIndex = Math.min(currentOrderIdx, orders.value.length - 1)
        currentOrderIndex.value = newIndex.toString()
      }

      paymentForm.value = { discountCode: null, method: 'cash', cashReceived: 0, transferReceived: 0 }
      shippingFee.value = 0
      orderType.value = 'counter'
      walkInLocation.value = { thanhPho: '', quan: '', phuong: '', diaChiCuThe: '', districts: [], wards: [] }
      walkInName.value = ''
      walkInEmail.value = ''
      walkInPhone.value = ''
      walkInDeliveryValid.value = false

      showConfirmOrderModal.value = false
    } catch (error: any) {
      console.error('Lỗi khi xác nhận đơn hàng:', error)
      Message.error(error.message || 'Có lỗi xảy ra khi xác nhận đơn hàng. Vui lòng thử lại.')
    } finally {
      confirmLoading.value = false
    }
  }

  const cancelConfirmOrder = () => {
    showConfirmOrderModal.value = false
  }

  return {
    showConfirmOrderModal,
    suggestedBetterVouchers,
    confirmOrderRequest,
    confirmLoading,
    checkBetterVouchers,
    confirmOrder,
    doConfirmOrder,
    cancelConfirmOrder,
  }
}
