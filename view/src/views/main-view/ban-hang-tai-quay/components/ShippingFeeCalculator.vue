<template>
  <div>
    <!-- Display shipping fee section -->
    <div v-if="orderType === 'delivery'" style="margin-bottom: 16px">
      <a-divider orientation="left" style="margin: 12px 0">Phí vận chuyển</a-divider>

      <a-spin :loading="isCalculating" style="width: 100%">
        <a-form layout="vertical" style="margin-bottom: 16px">
          <!-- Shipping info display -->
          <div
            v-if="selectedCustomer?.address && !isWalkIn"
            style="padding: 12px; background-color: #f5f5f5; border-radius: 4px; margin-bottom: 12px"
          >
            <div style="font-size: 12px; color: #666; margin-bottom: 8px">
              <strong>Địa chỉ giao hàng:</strong>
            </div>
            <div style="font-size: 13px">{{ selectedCustomer.address }}</div>
          </div>

          <!-- Walk-in location info -->
          <div
            v-if="isWalkIn && isLocationComplete"
            style="padding: 12px; background-color: #f5f5f5; border-radius: 4px; margin-bottom: 12px"
          >
            <div style="font-size: 12px; color: #666; margin-bottom: 8px">
              <strong>Địa chỉ giao hàng:</strong>
            </div>
            <div style="font-size: 13px">{{ formatLocationDisplay }}</div>
          </div>

          <!-- Shipping fee display -->
          <a-row :gutter="12">
            <a-col :span="12">
              <div style="background: #f5f5f5; padding: 12px; border-radius: 4px; text-align: center">
                <div style="font-size: 12px; color: #666; margin-bottom: 4px">Phí vận chuyển</div>
                <div style="font-size: 20px; font-weight: bold; color: #1890ff">{{ displayShippingFee }}</div>
              </div>
            </a-col>
            <a-col :span="12">
              <div v-if="shippingFee > 0" style="background: #f5f5f5; padding: 12px; border-radius: 4px; text-align: center">
                <div style="font-size: 12px; color: #666; margin-bottom: 4px">Tổng (Hàng + Phí)</div>
                <div style="font-size: 20px; font-weight: bold; color: #52c41a">{{ totalWithShipping }}</div>
              </div>
            </a-col>
          </a-row>

          <!-- Estimated delivery time -->
          <div
            v-if="shippingInfo && isLocationComplete"
            style="padding: 12px; background-color: #e6f7ff; border-radius: 4px; margin-top: 12px"
          >
            <div style="font-size: 12px; color: #0050b3">
              <strong>⏱️ Dự kiến giao hàng:</strong>
              {{ shippingInfo.estimatedDays }}
            </div>
          </div>
        </a-form>
      </a-spin>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { IconInfoCircle, IconCheckCircle } from '@arco-design/web-vue/es/icon'
import type { ShippingLocation } from '../services/shippingFeeService'
import { calculateShippingFee, getShippingInfo } from '../services/shippingFeeService'

interface Props {
  orderType: 'counter' | 'delivery'
  selectedCustomer?: any
  isWalkIn: boolean
  walkInLocation: ShippingLocation
  subtotal: number
}

interface Emits {
  (e: 'update:shippingFee', value: number): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const isCalculating = ref(false)
const shippingFee = ref(0)

const displayShippingFee = computed(() => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(shippingFee.value)
})

const totalWithShipping = computed(() => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(props.subtotal + shippingFee.value)
})

const isLocationComplete = computed(() => {
  return props.walkInLocation.thanhPho && props.walkInLocation.quan && props.walkInLocation.phuong
})

const formatLocationDisplay = computed(() => {
  // Only show specific address (số nhà, ngõ, đường), not full address
  return props.walkInLocation.diaChiCuThe || ''
})

const shippingInfo = computed(() => {
  if (!isLocationComplete.value) {
    return null
  }
  return getShippingInfo(props.walkInLocation)
})

/**
 * Calculate shipping fee based on selected location
 */
const recalculateShippingFee = async () => {
  if (!isLocationComplete.value) {
    return
  }

  isCalculating.value = true
  try {
    const fee = calculateShippingFee(props.walkInLocation, props.subtotal)
    shippingFee.value = fee || 0
    emit('update:shippingFee', shippingFee.value)
  } catch (error) {
    console.error('Error calculating shipping fee:', error)
    shippingFee.value = 0
    emit('update:shippingFee', 0)
  } finally {
    isCalculating.value = false
  }
}

/**
 * Auto-calculate shipping fee when location changes
 */
watch(
  () => [props.walkInLocation.thanhPho, props.walkInLocation.quan, props.walkInLocation.phuong],
  async () => {
    if (props.isWalkIn && isLocationComplete.value) {
      await recalculateShippingFee()
    }
  },
  { deep: true }
)

/**
 * Auto-calculate when switching to delivery mode
 */
watch(
  () => props.orderType,
  async (newType) => {
    if (newType === 'delivery') {
      if (props.isWalkIn && isLocationComplete.value) {
        await recalculateShippingFee()
      }
    }
  }
)
</script>

<style scoped>
:deep(.arco-statistic) {
  width: 100%;
}
</style>
