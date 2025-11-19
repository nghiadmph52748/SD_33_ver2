<template>
  <a-modal :visible="visible" title="Chọn Phiếu Giảm Giá" width="800px" :footer="null" @cancel="$emit('update:visible', false)">
    <div style="max-height: 600px; overflow-y: auto">
      <a-empty v-if="coupons.length === 0" description="Không có phiếu giảm giá" />

      <div
        v-if="showOrderSummary"
        style="margin-bottom: 16px; padding: 12px; background: #f0f9ff; border-radius: 6px; border: 1px solid #e5e5e5"
      >
        <div style="font-size: 12px; color: #666; margin-bottom: 8px">Đơn hàng hiện tại:</div>
        <div style="font-weight: 600; color: #1890ff">
          Tổng tiền: {{ formatCurrency(subtotal) }} | Số lượng: {{ totalQuantity }} sản phẩm
        </div>
      </div>

      <div v-else style="margin-bottom: 16px; padding: 12px; background: #fff7e6; border-radius: 6px; border: 1px solid #ffd591">
        <div style="font-size: 12px; color: #d48806">Chưa có sản phẩm nào trong giỏ hàng</div>
      </div>

      <div v-if="coupons.length > 0">
        <div style="margin-bottom: 12px; font-size: 14px; color: #666">
          {{ eligibleVouchersCount }}/{{ coupons.length }} voucher có thể sử dụng
        </div>

        <div
          v-for="coupon in coupons"
          :key="coupon.id"
          style="border: 1px solid #e5e5e5; border-radius: 8px; margin-bottom: 12px; overflow: hidden"
        >
          <div
            :class="{ 'voucher-disabled': !isVoucherEligibleFn(coupon) }"
            style="
              display: flex;
              align-items: center;
              padding: 16px;
              background: #fafafa;
              border-bottom: 1px solid #e5e5e5;
              cursor: pointer;
              transition: all 0.3s;
            "
            @click="isVoucherEligibleFn(coupon) ? $emit('select-voucher', coupon) : null"
            @mouseenter="
              (e) => {
                if (isVoucherEligibleFn(coupon)) (e.currentTarget as HTMLElement).style.background = '#f0f9ff'
              }
            "
            @mouseleave="
              (e) => {
                ;(e.currentTarget as HTMLElement).style.background = '#fafafa'
              }
            "
          >
            <div style="flex: 1">
              <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 8px">
                <div style="font-size: 16px; font-weight: 600; color: #333">{{ coupon.maPhieuGiamGia }}</div>
                <a-tag v-if="bestVoucherId && coupon.id === bestVoucherId" color="gold" size="small">⭐ Tốt nhất</a-tag>
                <a-tag v-if="!isVoucherEligibleFn(coupon)" color="red" size="small">{{ getVoucherStatusFn(coupon) }}</a-tag>
                <a-tag v-else-if="coupon.soLuongDung <= 0" color="orange" size="small">Hết lượt sử dụng</a-tag>
                <a-tag v-else color="green" size="small">Có thể sử dụng</a-tag>
              </div>

              <div style="font-size: 12px; color: #86909c; line-height: 1.4">{{ coupon.tenPhieuGiamGia }}</div>

              <div style="margin-top: 8px; font-size: 11px; color: #666">
                <span v-if="!isVoucherEligibleFn(coupon)" style="color: #ff4d4f">{{ getVoucherStatusFn(coupon) }}</span>
                <span v-else>
                  <span> {{ getDiscountDisplayFn(coupon) }} giảm giá</span>
                  <span v-if="coupon.hoaDonToiThieu" style="margin-left: 12px">
                    Min: {{ formatCurrency(Number(coupon.hoaDonToiThieu)) }}
                  </span>
                  <span v-if="coupon.soLuongDung" style="margin-left: 12px">Còn: {{ coupon.soLuongDung }} lượt</span>
                  <span v-if="coupon.ngayKetThuc" style="margin-left: 12px">Hết hạn: {{ coupon.ngayKetThuc }}</span>
                </span>
              </div>
            </div>

            <div style="text-align: center; margin-left: 16px">
              <div
                style="
                  background: linear-gradient(135deg, #f5222d 0%, #ff4d4f 100%);
                  color: white;
                  padding: 8px 16px;
                  border-radius: 6px;
                  font-size: 14px;
                  font-weight: 600;
                "
              >
                {{ getDiscountDisplayFn(coupon) }}
              </div>
            </div>

            <div style="margin-left: 16px">
              <a-button
                type="primary"
                size="small"
                :disabled="!isVoucherEligibleFn(coupon)"
                @click.stop="$emit('select-voucher', coupon)"
                style="background: linear-gradient(135deg, #f5222d 0%, #ff4d4f 100%); border: none"
              >
                {{ isVoucherEligibleFn(coupon) ? 'Chọn' : 'Không đủ ĐK' }}
              </a-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import type { CouponApiModel } from '@/api/discount-management'

const props = defineProps<{
  visible: boolean
  coupons: CouponApiModel[]
  eligibleVouchersCount: number
  showOrderSummary: boolean
  subtotal: number
  totalQuantity: number
  bestVoucherId?: number | null
  isVoucherEligibleFn: (coupon: CouponApiModel) => boolean
  getVoucherStatusFn: (coupon: CouponApiModel) => string
  getDiscountDisplayFn: (coupon: CouponApiModel) => string
}>()

defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'select-voucher', coupon: CouponApiModel): void
}>()

function formatCurrency(value: number) {
  try {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0)
  } catch {
    return `${value} đ`
  }
}
</script>

<style scoped>
.voucher-disabled {
  opacity: 0.6;
  cursor: not-allowed !important;
  background: #f5f5f5 !important;
}
</style>
