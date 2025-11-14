import { computed, type Ref } from 'vue'
import type { CouponApiModel } from '@/api/discount-management'
import { Message, Modal } from '@arco-design/web-vue'
import { findAlmostEligibleVoucher, type VoucherSuggestion } from '../services/voucherSuggestionService'

interface CartItem {
  price: number
  discount: number
  quantity: number
}

interface Order {
  items: CartItem[]
}

export default function useVoucher(params: {
  coupons: Ref<CouponApiModel[]>
  paymentForm: Ref<{ discountCode: string | null } & Record<string, any>>
  currentOrder: Ref<Order | null>
  subtotal: Ref<number>
}) {
  const { coupons, paymentForm, currentOrder, subtotal } = params

  const selectedCoupon = computed(() => {
    if (!paymentForm.value?.discountCode) {
      return null
    }
    const found = coupons.value.find((c) => c.maPhieuGiamGia === paymentForm.value?.discountCode) || null
    return found
  })

  const calculateVoucherDiscount = (coupon: CouponApiModel | null | undefined): number => {
    if (!coupon) return 0
    const discountValue = Number(coupon.giaTriGiamGia) || 0
    const subtotalValue = subtotal.value
    if (!coupon.loaiPhieuGiamGia) {
      return subtotalValue * (discountValue / 100)
    }
    return Math.min(discountValue, subtotalValue)
  }

  const getVoucherStatus = (coupon: CouponApiModel) => {
    if (coupon.trangThai !== true) return 'Không hoạt động'
    if (!currentOrder.value || currentOrder.value.items.length === 0) return 'Chưa có sản phẩm'
    if (coupon.hoaDonToiThieu && subtotal.value < Number(coupon.hoaDonToiThieu)) {
      const discountText = !coupon.loaiPhieuGiamGia ? `${Number(coupon.giaTriGiamGia)}%` : formatCurrency(Number(coupon.giaTriGiamGia))
      // eslint-disable-next-line no-use-before-define
      return `Cần ${formatCurrency(Number(coupon.hoaDonToiThieu))} cho ${discountText}`
    }
    if (coupon.soLuongDung !== undefined && coupon.soLuongDung <= 0) return 'Hết lượt'
    if (coupon.ngayKetThuc) {
      const expiryDate = new Date(coupon.ngayKetThuc)
      const now = new Date()
      if (expiryDate < now) return 'Đã hết hạn'
    }
    return 'Không đủ điều kiện'
  }

  const getDiscountDisplay = (coupon: CouponApiModel) => {
    const discountValue = Number(coupon.giaTriGiamGia) || 0
    if (!coupon.loaiPhieuGiamGia) return `${discountValue}%`
    // eslint-disable-next-line no-use-before-define
    return formatCurrency(discountValue)
  }

  // eslint-disable-next-line no-use-before-define
  const eligibleVouchersCount = computed(() => coupons.value.filter((c) => isVoucherEligible(c)).length)
  const hasEligibleVouchers = computed(() => eligibleVouchersCount.value > 0)

  const bestVoucher = computed(() => {
    // eslint-disable-next-line no-use-before-define
    const eligible = coupons.value.filter((c) => isVoucherEligible(c))
    if (eligible.length === 0) return null
    if (selectedCoupon.value && eligible.length > 1) {
      const filtered = eligible.filter((c) => c.id !== selectedCoupon.value?.id)
      if (filtered.length === 0) return null
    }
    let best = eligible[0]
    let max = calculateVoucherDiscount(best)
    eligible.forEach((c) => {
      const d = calculateVoucherDiscount(c)
      if (d > max) {
        max = d
        best = c
      }
    })
    if (selectedCoupon.value) {
      const selectedDiscount = calculateVoucherDiscount(selectedCoupon.value)
      if (calculateVoucherDiscount(best) <= selectedDiscount) return null
    }
    return best
  })

  const hasBetterVoucher = computed(() => {
    if (!bestVoucher.value) return false
    if (!selectedCoupon.value) return false
    if (selectedCoupon.value.id === bestVoucher.value.id) return false
    return calculateVoucherDiscount(bestVoucher.value) > calculateVoucherDiscount(selectedCoupon.value)
  })

  const isVoucherEligible = (coupon: CouponApiModel) => {
    if (coupon.trangThai !== true) {
      return false
    }
    if (!currentOrder.value || currentOrder.value.items.length === 0) {
      return false
    }
    if (coupon.hoaDonToiThieu && subtotal.value < Number(coupon.hoaDonToiThieu)) {
      return false
    }
    if (coupon.soLuongDung !== undefined && coupon.soLuongDung <= 0) {
      return false
    }
    if (coupon.ngayKetThuc) {
      const expiryDate = new Date(coupon.ngayKetThuc)
      const now = new Date()
      if (expiryDate < now) {
        return false
      }
    }
    return true
  }

  const almostEligibleSuggestion = computed<VoucherSuggestion | null>(() => {
    try {
      return findAlmostEligibleVoucher(coupons.value, subtotal.value, calculateVoucherDiscount, selectedCoupon.value, 200000)
    } catch (e) {
      return null
    }
  })

  function formatCurrency(value: number) {
    try {
      return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
    } catch {
      return `${value} đ`
    }
  }

  return {
    selectedCoupon,
    eligibleVouchersCount,
    hasEligibleVouchers,
    bestVoucher,
    hasBetterVoucher,
    calculateVoucherDiscount,
    getVoucherStatus,
    getDiscountDisplay,
    isVoucherEligible,
    almostEligibleSuggestion,
  }
}
