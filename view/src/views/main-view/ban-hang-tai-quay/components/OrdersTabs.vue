<template>
  <div style="display: flex; width: 100%; align-items: center">
    <div
      class="tabs-container"
      style="flex: 0 0 80%; display: flex; align-items: center; overflow-x: auto; scrollbar-width: none; -ms-overflow-style: none"
    >
      <a-tabs
        :active-key="activeKey"
        type="button"
        @change="$emit('change', $event)"
        class="orders-tabs"
        style="flex: 1; min-width: max-content"
      >
        <a-tab-pane v-for="(order, idx) in orders" :key="idx.toString()">
          <template #title>
            <div class="tab-header">
              <span class="order-text">Đơn #{{ idx + 1 }} ({{ order.orderCode }})</span>
              <span v-if="order.items && order.items.length > 0" class="item-count-badge">
                {{ order.items.reduce((sum, item) => sum + item.quantity, 0) }}
              </span>
              <a-button type="text" size="mini" status="danger" class="tab-close-btn" @click.stop="$emit('delete', idx)">
                <template #icon>
                  <icon-close />
                </template>
              </a-button>
            </div>
          </template>
        </a-tab-pane>
      </a-tabs>
    </div>
    <div style="flex: 0 0 20%; text-align: center; margin-bottom: 16px">
      <a-button v-if="canAdd" type="primary" size="medium" @click="$emit('add')">
        <template #icon>
          <icon-plus />
        </template>
        Thêm Đơn
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { IconPlus, IconClose } from '@arco-design/web-vue/es/icon'

interface CartItem {
  quantity: number
}
interface Order {
  orderCode: string
  items: CartItem[]
}

defineProps<{ orders: Order[]; activeKey: string; canAdd: boolean }>()

defineEmits<{ (e: 'change', key: string): void; (e: 'delete', index: number): void; (e: 'add'): void }>()
</script>

<style scoped>
.tab-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.order-text {
  display: inline-flex;
  align-items: center;
  line-height: 20px;
  vertical-align: middle;
}

.item-count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1;
  color: #fff;
  background-color: #f5222d;
  border-radius: 10px;
  box-sizing: border-box;
}

.tab-close-btn {
  margin-left: auto;
}
</style>
