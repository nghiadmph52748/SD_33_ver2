<template>
  <div
    v-if="hasBetter && bestVoucher && selectedCoupon"
    style="
      background: linear-gradient(135deg, #fff7e6 0%, #fff3e0 100%);
      border: 2px solid #ff9800;
      border-radius: 12px;
      padding: 20px;
      margin-top: 12px;
      display: block;
      width: 100%;
      box-sizing: border-box;
      box-shadow: 0 4px 12px rgba(255, 152, 0, 0.15);
      position: relative;
      overflow: hidden;
    "
  >
    <div
      style="
        position: absolute;
        top: -50%;
        right: -10%;
        width: 200px;
        height: 200px;
        background: radial-gradient(circle, rgba(255, 152, 0, 0.1) 0%, transparent 70%);
        border-radius: 50%;
        animation: pulse 3s ease-in-out infinite;
      "
    ></div>

    <div style="position: relative; z-index: 1">
      <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 16px">
        <div
          style="
            background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
            width: 48px;
            height: 48px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            box-shadow: 0 2px 8px rgba(255, 152, 0, 0.3);
          "
        >
          <icon-gift style="font-size: 24px; color: #fff" />
        </div>
        <div style="flex: 1">
          <div style="font-size: 16px; font-weight: 700; color: #e65100; line-height: 1.3">Có voucher tiết kiệm hơn!</div>
          <div style="font-size: 12px; color: #f57c00; margin-top: 2px">Đổi ngay để được giảm giá cao hơn</div>
        </div>
      </div>

      <div style="background: #fff; border-radius: 10px; padding: 16px; margin-bottom: 16px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06)">
        <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px">
          <div style="display: flex; align-items: center; gap: 10px">
            <span
              style="
                background: #fff;
                color: #ff9800;
                padding: 8px 16px;
                border-radius: 8px;
                font-weight: 800;
                font-size: 15px;
                letter-spacing: 0.5px;
                border: 2px dashed #ff9800;
                box-shadow: 0 2px 4px rgba(255, 152, 0, 0.15);
              "
            >
              {{ bestVoucher?.maPhieuGiamGia }}
            </span>
          </div>
          <div style="text-align: right">
            <div style="font-size: 11px; color: #666; margin-bottom: 2px">Tiết kiệm thêm</div>
            <div style="color: #2e7d32; font-weight: 800; font-size: 18px; display: flex; align-items: center; justify-content: flex-end; gap: 4px">
              <span style="font-size: 16px">+</span>
              {{ formatCurrency(calculateVoucherDiscount(bestVoucher as any) - calculateVoucherDiscount(selectedCoupon as any)) }}
            </div>
          </div>
        </div>
      </div>

      <a-button type="primary" long size="large" style="background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%); border: none; font-weight: 700; height: 48px; font-size: 15px; box-shadow: 0 4px 12px rgba(255, 152, 0, 0.3); transition: all 0.3s ease;" @click="$emit('select')">
        <template #icon>
          <icon-swap />
        </template>
        Chuyển ngay để tiết kiệm hơn
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { IconGift, IconSwap } from '@arco-design/web-vue/es/icon'
import type { CouponApiModel } from '@/api/discount-management'

defineProps<{
  hasBetter: boolean
  bestVoucher: CouponApiModel | null
  selectedCoupon: CouponApiModel | null
  calculateVoucherDiscount: (c: CouponApiModel | null | undefined) => number
}>()

defineEmits<{ (e: 'select'): void }>()

function formatCurrency(value: number) {
  try {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0)
  } catch {
    return `${value} đ`
  }
}
</script>

<style scoped>
@keyframes pulse {
  0%,
  100% { opacity: 1; }
  50% { opacity: 0.95; }
}
</style>
