<template>
  <a-card title="Thông Tin Khách Hàng" class="customer-card">
    <a-form :model="{}" layout="vertical">
      <a-form-item label="Chọn Khách Hàng">
        <a-select
          :model-value="customerId"
          allow-search
          filterable
          @update:model-value="$emit('update:customerId', $event)"
          @change="$emit('change:customer', $event)"
        >
          <a-option value="">Khách lẻ</a-option>
          <a-option v-for="customer in customers" :key="customer.id" :value="customer.id">
            {{ customer.name }} ({{ customer.phone }})
          </a-option>
        </a-select>
      </a-form-item>

      <a-form-item v-if="selectedCustomer">
        <a-descriptions size="small" :column="1" bordered>
          <a-descriptions-item label="Tên">{{ selectedCustomer.name }}</a-descriptions-item>
          <a-descriptions-item label="SĐT">{{ selectedCustomer.phone }}</a-descriptions-item>
          <a-descriptions-item label="Email">{{ selectedCustomer.email || 'N/A' }}</a-descriptions-item>
          <a-descriptions-item label="Địa Chỉ">{{ selectedCustomer.address || 'N/A' }}</a-descriptions-item>
        </a-descriptions>
      </a-form-item>

      <a-button v-if="!selectedCustomer" type="dashed" long @click="$emit('add-new')">
        <template #icon>
          <icon-plus />
        </template>
        Thêm Khách Hàng Mới
      </a-button>
    </a-form>
  </a-card>
</template>

<script setup lang="ts">
import { IconPlus } from '@arco-design/web-vue/es/icon'

interface Customer {
  id: string
  name: string
  phone: string
  email?: string
  address?: string
}

defineProps<{
  customerId: string
  customers: Customer[]
  selectedCustomer: Customer | null
}>()

defineEmits<{
  (e: 'update:customerId', value: string): void
  (e: 'change:customer', value: string): void
  (e: 'add-new'): void
}>()
</script>
