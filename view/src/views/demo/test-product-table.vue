<template>
  <div class="test-product-table">
    <a-card :bordered="false">
      <template #title>
        <div class="card-header">
          <icon-apps />
          <span>Test - Bảng sản phẩm với dữ liệu mẫu</span>
        </div>
      </template>

      <div class="test-content">
        <p class="test-description">Component này sẽ luôn hiển thị dữ liệu mẫu để test giao diện, không phụ thuộc vào API.</p>

        <ProductTable :show-actions="true" :selectable="false" :max-height="'500px'" />
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { IconApps } from '@arco-design/web-vue/es/icon'
import ProductTable from '@/components/product-table/product-table.vue'
</script>

<style scoped lang="less">
.test-product-table {
  padding: 20px;

  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    font-size: 18px;
  }

  .test-content {
    margin-top: 20px;

    .test-description {
      margin-bottom: 20px;
      padding: 12px;
      background: var(--color-fill-2);
      border-radius: 6px;
      color: var(--color-text-2);
      font-style: italic;
    }
  }
}
</style>
