<template>
  <a-card class="order-code-cart-card">
    <template #title>
      <div style="display: flex; justify-content: space-between; align-items: center">
        <div style="font-weight: 600; color: #333; font-size: 14px">
          Mã Đơn:
          <span style="color: #0960bd; font-weight: 700">{{ orderCode }}</span>
        </div>
        <a-space wrap style="margin-top: 8px">
          <a-button v-if="hasItems" type="text" status="danger" @click="$emit('clear-cart')" style="border: 1px solid #d9d9d9">
            <template #icon>
              <icon-delete />
            </template>
            Xoá Tất Cả
          </a-button>
          <a-button @click="$emit('open-qr')" style="border: 1px solid #d9d9d9">
            <template #icon>
              <icon-qrcode />
            </template>
            Quét QR
          </a-button>
          <a-button type="primary" @click="$emit('open-product')">
            <template #icon>
              <icon-plus />
            </template>
            Thêm Sản Phẩm
          </a-button>
        </a-space>
      </div>
    </template>
    <slot />
  </a-card>
</template>

<script setup lang="ts">
import { IconDelete, IconQrcode, IconPlus } from '@arco-design/web-vue/es/icon'

defineProps<{ orderCode: string; hasItems: boolean }>()

defineEmits<{ (e: 'clear-cart'): void; (e: 'open-qr'): void; (e: 'open-product'): void }>()
</script>
