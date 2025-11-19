<template>
  <a-modal
    :visible="visible"
    title=" Thông Báo Cập Nhật Giá"
    width="700px"
    :ok-text="'Xác Nhận'"
    :cancel-button-props="{ style: { display: 'none' } }"
    :ok-button-props="{ loading: confirmLoading }"
    @ok="$emit('confirm')"
    @update:visible="$emit('update:visible', $event)"
  >
    <a-alert v-if="priceChanges.size > 0" type="info" closable style="margin-bottom: 16px">
      <template #title>Các sản phẩm trong giỏ hàng đã được cập nhật giá từ hệ thống</template>
      <div style="font-size: 12px; margin-top: 8px">
        Tổng cộng {{ priceChanges.size }} sản phẩm có thay đổi. Bạn có thể xem chi tiết bên dưới.
      </div>
    </a-alert>

    <div style="max-height: 400px; overflow-y: auto">
      <div
        v-for="(change, index) of priceChangesArray"
        :key="index"
        style="margin-bottom: 12px; padding: 12px; background: #f5f5f5; border-radius: 4px; border-left: 4px solid #1890ff"
      >
        <div style="font-weight: 600; margin-bottom: 8px; font-size: 13px">
          {{ change.productName }}
        </div>

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; font-size: 12px">
          <!-- Old Price -->
          <div style="padding: 8px; background: #fff; border-radius: 2px">
            <div style="color: #999; margin-bottom: 4px">Giá cũ</div>
            <div style="margin-bottom: 2px">
              <span style="color: #666">{{ change.oldPrice > 0 ? formatCurrency(change.oldPrice) : 'N/A' }}</span>
            </div>
            <div v-if="change.oldDiscount > 0" style="font-size: 11px; color: #999">Chiết khấu: {{ change.oldDiscount }}%</div>
          </div>

          <!-- New Price -->
          <div style="padding: 8px; background: #fff; border-radius: 2px; border: 2px solid #52c41a">
            <div style="color: #999; margin-bottom: 4px">Giá mới</div>
            <div style="margin-bottom: 2px">
              <strong style="color: #52c41a; font-size: 13px">{{ formatCurrency(change.newPrice) }}</strong>
            </div>
            <div v-if="change.newDiscount > 0" style="font-size: 11px; color: #666">Chiết khấu: {{ change.newDiscount }}%</div>
          </div>
        </div>

        <!-- Change Indicator -->
        <div style="margin-top: 8px; padding: 6px; border-radius: 2px; font-size: 11px; font-weight: 600" :style="getChangeStyle(change)">
          {{ getChangeText(change) }}
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface PriceChange {
  id: string
  productName: string
  oldPrice: number
  newPrice: number
  oldDiscount: number
  newDiscount: number
}

const props = defineProps<{
  visible: boolean
  priceChanges: Map<string, PriceChange>
  confirmLoading: boolean
}>()

defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()

function formatCurrency(value: number) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0)
}

const priceChangesArray = computed(() => Array.from(props.priceChanges.values()))

const getChangeStyle = (change: PriceChange) => {
  if (change.newPrice > change.oldPrice) {
    return {
      background: '#ffebe9',
      border: '1px solid #ffccc7',
      color: '#ff4d4f',
    }
  } else if (change.newPrice < change.oldPrice) {
    return {
      background: '#f6ffed',
      border: '1px solid #b7eb8f',
      color: '#52c41a',
    }
  }
  return {
    background: '#e6f7ff',
    border: '1px solid #91d5ff',
    color: '#0050b3',
  }
}

const getChangeText = (change: PriceChange) => {
  const priceDiff = change.newPrice - change.oldPrice
  const discountDiff = change.newDiscount - change.oldDiscount

  if (priceDiff > 0 && discountDiff === 0) {
    return ` Tăng: +${formatCurrency(priceDiff)}`
  } else if (priceDiff < 0 && discountDiff === 0) {
    return ` Giảm: ${formatCurrency(priceDiff)}`
  } else if (priceDiff === 0 && discountDiff > 0) {
    return ` Chiết khấu tăng: +${discountDiff}%`
  } else if (priceDiff === 0 && discountDiff < 0) {
    return ` Chiết khấu giảm: ${discountDiff}%`
  } else if (priceDiff !== 0 && discountDiff !== 0) {
    return ` Giá và chiết khấu cùng thay đổi`
  }

  return 'Không có thay đổi'
}
</script>
