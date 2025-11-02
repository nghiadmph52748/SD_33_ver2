<template>
  <a-modal :visible="visible" title="Xóa Sản Phẩm Khỏi Giỏ Hàng" width="500px" @ok="$emit('ok')" @update:visible="$emit('update:visible', $event)" ok-text="Xóa" cancel-text="Hủy">
    <div v-if="product">
      <div style="display: flex; gap: 12px; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid #e5e5e5">
        <img v-if="product.image" :src="product.image" style="width: 60px; height: 60px; object-fit: cover; border-radius: 4px" :alt="product.productName" />
        <div style="flex: 1">
          <div style="font-weight: 600; font-size: 14px; margin-bottom: 4px">{{ product.productName }}</div>
          <div style="font-size: 12px; color: #999; margin-bottom: 8px">Số lượng: {{ product.quantity }}</div>
          <div style="font-size: 14px; font-weight: 600; color: #f5222d">{{ formatCurrency(product.price * product.quantity) }}</div>
        </div>
      </div>
      <div style="text-align: center; padding: 16px; background: #fff7e6; border-radius: 6px; margin-bottom: 16px">
        <a-result status="warning" title="Xác Nhận Xóa Sản Phẩm?" style="margin: 0; padding: 0" />
        <p style="margin: 8px 0 0 0; color: #d48806; font-size: 12px">Sản phẩm sẽ được xóa khỏi giỏ hàng và số lượng sẽ được hoàn lại vào kho.</p>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
interface Product { image?: string; productName: string; quantity: number; price: number }

defineProps<{ visible: boolean; product: Product | null }>()

defineEmits<{ (e: 'update:visible', value: boolean): void; (e: 'ok'): void }>()

function formatCurrency(value: number) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', minimumFractionDigits: 0, maximumFractionDigits: 0 }).format(value || 0)
}
</script>
