<template>
  <div class="cart-wrapper">
    <a-alert v-if="overStockItems && overStockItems.length > 0" type="error" closable style="margin-bottom: 16px">
      <template #title>Tồn kho không đủ</template>
      <div style="font-size: 12px">
        <div v-for="item in overStockItems" :key="item.id" style="margin-bottom: 8px; line-height: 1.5">
          <strong>{{ item.productName }}</strong>
          <br />
          <span style="color: #666">Yêu cầu: {{ item.requiredQty }} cái | Còn lại: {{ item.currentStock }} cái | Thiếu:</span>
          <strong style="color: #f5222d">{{ item.shortageQty }} cái</strong>
        </div>
      </div>
    </a-alert>

    <a-table
      v-if="items && items.length > 0"
      :key="tableKey"
      :columns="columns"
      :data="items"
      :pagination="pagination"
      row-key="id"
      size="small"
      :scroll="{ x: '100%' }"
      @paginate="(page) => $emit('paginate', page)"
    >
      <template #product="{ record }">
        <div style="display: flex; gap: 8px; align-items: center">
          <div v-if="(record.images && record.images.length > 0) || record.image" style="position: relative; flex-shrink: 0; width: 70px; height: 70px;">
            <MiniCarousel 
              :images="record.images && record.images.length > 0 ? record.images : (record.image ? [record.image] : [])"
              :autoplay-interval="2500"
              class="cart-carousel"
            />
            <div
              v-if="record.images && record.images.length > 1"
              style="
                position: absolute;
                top: 2px;
                right: 2px;
                background: rgba(0, 0, 0, 0.6);
                color: white;
                padding: 2px 4px;
                border-radius: 2px;
                font-size: 9px;
                font-weight: bold;
              "
            >
              +{{ record.images.length - 1 }}
            </div>
          </div>
          <div v-else style="width: 70px; height: 70px; flex-shrink: 0; border-radius: 4px;">
            <a-image src="" :width="70" :height="70" :preview="false" />
          </div>
          <div>
            <div style="font-weight: 600; font-size: 13px; margin-bottom: 4px">
              {{ getProductDisplayName(record) }}
            </div>
            <div style="display: flex; align-items: center; gap: 8px; font-size: 11px; color: #666">
              <div v-if="record.tenMauSac" style="display: flex; align-items: center; gap: 4px">
                <div
                  v-if="record.maMau"
                  style="width: 12px; height: 12px; border-radius: 2px; border: 1px solid #e5e5e5"
                  :style="{ backgroundColor: record.maMau }"
                ></div>
                <span>{{ record.maMau }}</span>
              </div>
              <div v-if="record.tenKichThuoc" style="display: flex; align-items: center; gap: 4px">
                <span>Size:</span>
                <strong>{{ record.tenKichThuoc }}</strong>
              </div>
            </div>
          </div>
        </div>
      </template>

      <template #quantity="{ record }">
        <a-input-number
          :model-value="record.quantity"
          :min="1"
          :max="999"
          size="small"
          @change="(val) => $emit('update-quantity', { id: record.id, value: val })"
        />
      </template>

      <template #price="{ record }">
        <div style="font-size: 12px">
          <div v-if="record.discount && record.discount > 0">
            <div style="text-decoration: line-through; color: #999; margin-bottom: 2px">
              {{ formatCurrency(record.price) }}
            </div>
            <div style="font-weight: 600; color: #f5222d; font-size: 14px">
              {{ formatCurrency(record.price * (1 - record.discount / 100)) }}
            </div>
          </div>
          <div v-else style="font-weight: 600; color: #f5222d; font-size: 14px">
            {{ formatCurrency(record.price) }}
          </div>
        </div>
      </template>

      <template #subtotal="{ record }">
        <strong>
          {{ formatCurrency((record.discount > 0 ? record.price * (1 - record.discount / 100) : record.price) * record.quantity) }}
        </strong>
      </template>

      <template #action="{ record }">
        <a-button type="text" status="danger" size="small" @click="$emit('delete', record)">
          <template #icon>
            <icon-delete />
          </template>
        </a-button>
      </template>
    </a-table>

    <a-empty v-else description="Giỏ hàng trống" />
  </div>
</template>

<script setup lang="ts">
import { IconDelete } from '@arco-design/web-vue/es/icon'
import MiniCarousel from '@/components/MiniCarousel.vue'
import { formatCurrency, getProductDisplayName } from '../utils'

interface CartItem {
  id: string
  productName: string
  price: number
  discount: number
  quantity: number
  image?: string
  images?: string[]
  tenMauSac?: string
  maMau?: string
  tenKichThuoc?: string
}

const props = defineProps<{
  items: CartItem[]
  tableKey: number
  pagination: { current: number; pageSize: number; total: number; showTotal?: boolean; showPageSize?: boolean }
  overStockItems: Array<{ id: string; productName: string; requiredQty: number; currentStock: number; shortageQty: number }>
}>()

defineEmits<{
  (e: 'paginate', page: number): void
  (e: 'update-quantity', payload: { id: string; value: number }): void
  (e: 'delete', record: CartItem): void
}>()

const columns = [
  { title: 'STT', dataIndex: 'stt', key: 'stt', width: 50, align: 'center' as const },
  { title: 'Sản Phẩm', dataIndex: 'product', key: 'product', width: 300, slotName: 'product' },
  { title: 'Số Lượng', dataIndex: 'quantity', key: 'quantity', slotName: 'quantity', width: 100, align: 'center' as const },
  { title: 'Giá Bán', dataIndex: 'price', key: 'price', slotName: 'price', width: 120, align: 'right' as const },
  { title: 'Thành Tiền', dataIndex: 'subtotal', key: 'subtotal', slotName: 'subtotal', width: 130, align: 'right' as const },
  { title: 'Thao Tác', dataIndex: 'action', key: 'action', slotName: 'action', width: 80, align: 'center' as const },
]
</script>

<style scoped>
:deep(.cart-carousel) {
  width: 100% !important;
  height: 100% !important;
}

:deep(.cart-carousel .mini-carousel-container) {
  width: 100% !important;
  height: 100% !important;
  border-radius: 4px;
}

:deep(.cart-carousel .carousel-slides) {
  width: 100% !important;
  height: 100% !important;
}

:deep(.cart-carousel .carousel-slide) {
  width: 100% !important;
  height: 100% !important;
}

:deep(.cart-carousel .carousel-slide-empty) {
  width: 100% !important;
  height: 100% !important;
}
</style>
