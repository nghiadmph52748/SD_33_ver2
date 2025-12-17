<template>
  <a-card class="customer-card">
    <template #title>
      <div class="cust-header">
        <span>Thông Tin Khách Hàng</span>
        <a-select :model-value="orderType" placeholder="Loại đơn" style="width: 140px" @change="$emit('change:orderType', $event)">
          <a-option value="counter">Tại quầy</a-option>
          <a-option value="delivery">Giao hàng</a-option>
        </a-select>
      </div>
    </template>

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

      <!-- Walk-in: contact info -->
      <template v-if="isWalkIn">
        <hr />
        <a-row :gutter="[12, 12]">
          <a-col :span="12">
            <a-form-item label="Tên" :validate-status="nameError ? 'error' : undefined" :help="nameError || ''" :required="orderType === 'delivery'">
              <a-input :model-value="walkInName" placeholder="Nhập tên" @update:model-value="$emit('update:walkInName', $event)" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="Số điện thoại" :validate-status="phoneError ? 'error' : undefined" :help="phoneError || ''" :required="orderType === 'delivery'">
              <a-input
                :model-value="walkInPhone"
                placeholder="Nhập số điện thoại"
                @update:model-value="$emit('update:walkInPhone', $event)"
              />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="Email" :validate-status="emailError ? 'error' : undefined" :help="emailError || ''" :required="orderType === 'delivery'">
              <a-input :model-value="walkInEmail" placeholder="Nhập email" @update:model-value="$emit('update:walkInEmail', $event)" />
            </a-form-item>
          </a-col>
        </a-row>
      </template>
    </a-form>
  </a-card>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { IconPlus } from '@arco-design/web-vue/es/icon'

interface Customer {
  id: string
  name: string
  phone: string
  email?: string
  address?: string
}

const props = defineProps<{
  customerId: string
  customers: Customer[]
  selectedCustomer: Customer | null
  orderType: 'counter' | 'delivery'
  isWalkIn: boolean
  walkInName?: string
  walkInEmail?: string
  walkInPhone?: string
}>()

const emit = defineEmits<{
  (e: 'update:customerId', value: string): void
  (e: 'change:customer', value: string): void
  (e: 'add-new'): void
  (e: 'change:orderType', value: 'counter' | 'delivery'): void
  (e: 'update:walkInName', value: string): void
  (e: 'update:walkInEmail', value: string): void
  (e: 'update:walkInPhone', value: string): void
  (e: 'update:walkInDeliveryValid', value: boolean): void
}>()

const nameError = computed(() => {
  if (props.orderType !== 'delivery') return ''
  if (!props.isWalkIn) return ''
  const v = (props.walkInName || '').trim()
  if (!v) return ''
  if (v.length < 2) return 'Tên tối thiểu 2 ký tự'
  return ''
})

const emailError = computed(() => {
  if (props.orderType !== 'delivery') return ''
  if (!props.isWalkIn) return ''
  const v = (props.walkInEmail || '').trim()
  if (!v) return ''
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!re.test(v)) return 'Email không hợp lệ'
  return ''
})

const phoneError = computed(() => {
  if (props.orderType !== 'delivery') return ''
  if (!props.isWalkIn) return ''
  const v = (props.walkInPhone || '').trim()
  if (!v) return ''
  const re = /^\d{9,11}$/
  if (!re.test(v)) return 'SĐT phải gồm 9-11 chữ số'
  return ''
})

const isValid = computed(() => !nameError.value && !emailError.value && !phoneError.value)

watch(
  () => ({
    ot: props.orderType,
    iw: props.isWalkIn,
    n: props.walkInName,
    e: props.walkInEmail,
    p: props.walkInPhone,
  }),
  () => emit('update:walkInDeliveryValid', isValid.value),
  { deep: true, immediate: true }
)
</script>

<style scoped>
.cust-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
