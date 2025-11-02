<template>
  <div
    style="
      margin-bottom: 12px;
      background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
      border: 1px solid #91d5ff;
      border-radius: 8px;
      padding: 16px;
      margin-top: 12px;
      cursor: pointer;
      transition: all 0.3s ease;
      display: block;
      width: 100%;
      box-sizing: border-box;
    "
    @click="$emit('select')"
    @mouseenter="(e) => ((e.currentTarget as HTMLElement).style.transform = 'translateY(-2px)')"
    @mouseleave="(e) => ((e.currentTarget as HTMLElement).style.transform = 'translateY(0)')"
  >
    <div style="display: flex; align-items: center; gap: 12px">
      <div
        style="
          background: linear-gradient(135deg, #1890ff 0%, #0050b3 100%);
          width: 40px;
          height: 40px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 2px 6px rgba(24, 144, 255, 0.3);
        "
      >
        <icon-gift style="font-size: 20px; color: #fff" />
      </div>
      <div style="flex: 1">
        <div style="font-size: 14px; font-weight: 600; color: #0050b3; margin-bottom: 4px">Gợi ý voucher tốt nhất</div>
        <div style="display: flex; align-items: center; gap: 8px">
          <span
            style="
              background: #fff;
              color: #1890ff;
              padding: 4px 12px;
              border-radius: 4px;
              font-weight: 700;
              font-size: 13px;
              border: 1px dashed #1890ff;
            "
          >
            {{ bestVoucher?.maPhieuGiamGia }}
          </span>
          <span style="color: #666; font-size: 12px">Tiết kiệm</span>
          <span style="color: #52c41a; font-weight: 700; font-size: 16px">
            {{ formatCurrency(calculateVoucherDiscount(bestVoucher as any)) }}
          </span>
        </div>
      </div>
      <icon-right style="color: #1890ff; font-size: 20px" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { IconGift, IconRight } from '@arco-design/web-vue/es/icon'
import type { CouponApiModel } from '@/api/discount-management'

defineProps<{ bestVoucher: CouponApiModel | null; calculateVoucherDiscount: (c: CouponApiModel) => number }>()

defineEmits<{ (e: 'select'): void }>()

function formatCurrency(value: number) {
  try {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0)
  } catch {
    return `${value} đ`
  }
}
</script>
