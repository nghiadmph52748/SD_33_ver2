<template>
  <div class="voucher-almost-eligible" v-if="suggestion">
    <a-alert type="info" show-icon>
      <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px">
        <div>
          <div style="font-size: 13px; color: #333">
            Mua thêm
            <strong style="color: #1890ff">{{ formatCurrency(suggestion?.neededAmount || 0) }}</strong>
            để được giảm thêm
            <strong style="color: #52c41a">{{ formatCurrency(suggestion?.extraSavings || 0) }}</strong>
          </div>
          <div style="font-size: 12px; color: #666">
            Phiếu:
            <strong>{{ suggestion?.coupon?.tenPhieuGiamGia || '' }}</strong>
          </div>
        </div>
        <div>
          <a-button size="small" type="secondary" @click="$emit('open-voucher')">Xem phiếu</a-button>
        </div>
      </div>
    </a-alert>
  </div>
</template>

<script setup lang="ts">
import type { VoucherSuggestion } from '../services/voucherSuggestionService'

const { suggestion } = defineProps<{ suggestion: VoucherSuggestion | null }>()
defineEmits<{
  (e: 'open-voucher'): void
}>()

function formatCurrency(value: number) {
  try {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
  } catch {
    return `${value} đ`
  }
}
</script>

<style scoped>
.voucher-almost-eligible {
  margin: 12px 0;
}
</style>
