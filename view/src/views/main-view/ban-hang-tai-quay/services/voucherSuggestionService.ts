import type { CouponApiModel } from '@/api/discount-management'

export interface VoucherSuggestion {
  coupon: CouponApiModel
  neededAmount: number
  extraSavings: number
}

/**
 * Find a voucher that is "almost eligible" — subtotal is below its minimum by up to `threshold`.
 * - coupons: array of coupons
 * - subtotal: current order subtotal
 * - calculateVoucherDiscount: function to compute discount for a coupon
 * - selectedCoupon: currently selected coupon (optional) — used to compute extraSavings relative to selected
 */
export function findAlmostEligibleVoucher(
  coupons: CouponApiModel[],
  subtotal: number,
  calculateVoucherDiscount: (c: CouponApiModel | null | undefined) => number,
  selectedCoupon: CouponApiModel | null | undefined,
  threshold = 200000
): VoucherSuggestion | null {
  if (!coupons || coupons.length === 0) return null

  const now = new Date()
  let best: VoucherSuggestion | null = null

  for (const c of coupons) {
    if (!c) continue
    if (c.trangThai !== true) continue
    if (c.soLuongDung !== undefined && c.soLuongDung <= 0) continue
    if (c.ngayKetThuc) {
      const expiry = new Date(c.ngayKetThuc)
      if (expiry < now) continue
    }

    const minRequired = Number(c.hoaDonToiThieu) || 0
    if (!minRequired || subtotal >= minRequired) continue

    const needed = minRequired - subtotal
    if (needed > threshold) continue

    const currentDiscount = selectedCoupon ? calculateVoucherDiscount(selectedCoupon) : 0
    const newDiscount = calculateVoucherDiscount(c)
    const extra = newDiscount - currentDiscount

    // prefer the suggestion that requires the least extra money, or gives the biggest extra saving if tied
    if (!best || needed < best.neededAmount || (needed === best.neededAmount && extra > best.extraSavings)) {
      best = { coupon: c, neededAmount: needed, extraSavings: extra }
    }
  }

  return best
}
