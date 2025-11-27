<template>
  <a-modal :visible="visible" title="Có Phiếu Giảm Giá Tốt Hơn" width="600px" :footer="null" @cancel="$emit('cancel')">
    <div style="padding: 20px 0">
      <div style="margin-bottom: 24px; padding: 12px 16px; background: #fff7e6; border-left: 4px solid #faad14; border-radius: 4px">
        <div style="color: #faad14; font-weight: 600; margin-bottom: 8px">Gợi ý</div>
        <div style="color: #666; line-height: 1.6">
          Chúng tôi tìm thấy phiếu giảm giá với mức giảm cao hơn. Bạn có muốn quay lại để chọn phiếu tốt nhất không?
        </div>
      </div>

      <div v-if="suggestedBetterVouchers && suggestedBetterVouchers.length > 0" style="margin-bottom: 24px">
        <div style="font-weight: 600; margin-bottom: 12px; color: #262626">Phiếu giảm giá tốt hơn:</div>
        <div
          v-for="(voucher, idx) in suggestedBetterVouchers"
          :key="idx"
          style="
            padding: 12px;
            margin-bottom: 8px;
            border: 1px solid #e5e5e5;
            border-radius: 6px;
            background: #fafafa;
            display: flex;
            justify-content: space-between;
            align-items: center;
          "
        >
          <div>
            <div style="font-weight: 600; color: #262626">{{ voucher.tenPhieuGiamGia }}</div>
            <div style="font-size: 12px; color: #999; margin-top: 4px">
              Còn: {{ voucher.soLuongDung }} lượt | Min: {{ formatCurrency(voucher.hoaDonToiThieu) }}
            </div>
          </div>
          <div style="text-align: right">
            <div style="font-size: 16px; font-weight: 700; color: #52c41a">-{{ formatCurrency(calculateVoucherDiscount(voucher)) }}</div>
          </div>
        </div>
      </div>

      <div v-if="selectedCoupon" style="margin-bottom: 24px; padding: 12px; background: #f0f0f0; border-radius: 6px">
        <div style="font-size: 12px; color: #999; margin-bottom: 4px">Phiếu đang chọn:</div>
        <div style="font-weight: 600; color: #262626">{{ selectedCoupon.tenPhieuGiamGia }}</div>
        <div style="font-size: 14px; color: #666; margin-top: 4px">-{{ formatCurrency(calculateVoucherDiscount(selectedCoupon)) }}</div>
      </div>
      <div v-else style="margin-bottom: 24px; padding: 12px; background: #f0f0f0; border-radius: 6px">
        <div style="font-size: 12px; color: #999">Không có phiếu giảm giá</div>
      </div>

      <div style="display: flex; gap: 12px; justify-content: flex-end">
        <a-button type="default" @click="$emit('cancel')">Quay lại</a-button>
        <a-button type="primary" :loading="confirmLoading" @click="$emit('confirm')">Bỏ qua và thanh toán</a-button>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import type { CouponApiModel } from '@/api/discount-management'

defineProps<{
  visible: boolean
  suggestedBetterVouchers?: CouponApiModel[]
  selectedCoupon: CouponApiModel | null
  calculateVoucherDiscount: (coupon: CouponApiModel | null | undefined) => number
  confirmLoading: boolean
}>()

defineEmits<{ (e: 'cancel'): void; (e: 'confirm'): void }>()

function formatCurrency(value: any) {
  const n = Number(value) || 0
  try {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n)
  } catch {
    return `${n} đ`
  }
}
</script>
