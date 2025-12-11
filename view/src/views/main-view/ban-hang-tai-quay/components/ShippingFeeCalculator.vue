<template>
  <div>
    <!-- Display shipping fee section -->
    <div v-if="orderType === 'delivery'" style="margin-bottom: 16px">
      <a-divider orientation="left" style="margin: 12px 0">Phí vận chuyển</a-divider>

      <a-spin :loading="isCalculating" style="width: 100%">
        <a-form layout="vertical" style="margin-bottom: 16px">
          <!-- Shipping address display -->
          <div style="padding: 12px; background-color: #f5f5f5; border-radius: 4px; margin-bottom: 12px">
            <div style="font-size: 12px; color: #666; margin-bottom: 8px">
              <strong>Địa chỉ giao hàng:</strong>
            </div>
            <div style="font-size: 13px">
              {{ formatLocationDisplay }}
            </div>
          </div>

          <!-- Shipping fee input -->
          <a-form-item :colon="false">
            <template #label>
              <span class="shipping-fee-label">
                <span class="shipping-fee-label-text">Phí vận chuyển (VNĐ)</span>
                <img :src="ghnLogo" alt="GHN" class="shipping-fee-logo" />
              </span>
            </template>
            <a-input-number
              v-model="shippingFee"
              :min="0"
              :max="10000000"
              :step="1000"
              placeholder="Nhập hoặc điều chỉnh phí vận chuyển"
              :formatter="(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
              :parser="(value) => value?.replace(/\$\s?|(,*)/g, '') as any"
              @change="handleShippingFeeChange"
              style="width: 100%"
            />
          </a-form-item>

          <!-- Estimated delivery time -->
          <div
            v-if="shippingInfo && isLocationComplete"
            style="padding: 12px; background-color: #e6f7ff; border-radius: 4px; margin-top: 12px"
          >
            <div style="font-size: 12px; color: #0050b3">
              <strong>⏱ Dự kiến giao hàng:</strong>
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
import type { ShippingLocation } from '../services/shippingFeeService'
import { calculateShippingFee, getShippingInfo, calculateShippingFeeFromGHN } from '../services/shippingFeeService'
import ghnLogoUrl from '@/assets/logo-ghn.png'

interface Props {
  orderType: 'counter' | 'delivery'
  selectedCustomer?: any
  isWalkIn: boolean
  walkInLocation: ShippingLocation
  subtotal: number
  shippingFeeFromParent?: number
}

interface Emits {
  (e: 'update:shippingFee', value: number): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const isCalculating = ref(false)
const shippingFee = ref(0)
const apiCalculatedFee = ref(0)
const ghnLogo = ghnLogoUrl

const totalWithShipping = computed(() => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(props.subtotal + shippingFee.value)
})

const isLocationComplete = computed(() => {
  return props.walkInLocation.thanhPho && props.walkInLocation.quan && props.walkInLocation.phuong
})

const formatLocationDisplay = computed(() => {
  if (props.isWalkIn) {
    // For walk-in: show full address with all components
    const parts = [
      props.walkInLocation.diaChiCuThe,
      props.walkInLocation.phuong,
      props.walkInLocation.quan,
      props.walkInLocation.thanhPho,
    ].filter((part) => part && part.trim())
    return parts.join(', ') || 'Chưa chọn địa chỉ'
  } else {
    // For registered customers: show full address from customer + location details
    const addressParts = []
    if (props.selectedCustomer?.address) {
      addressParts.push(props.selectedCustomer.address)
    }
    if (props.walkInLocation.phuong && props.walkInLocation.quan && props.walkInLocation.thanhPho) {
      addressParts.push(props.walkInLocation.phuong)
      addressParts.push(props.walkInLocation.quan)
      addressParts.push(props.walkInLocation.thanhPho)
    }
    return addressParts.filter((part) => part).join(', ') || 'Chưa chọn địa chỉ'
  }
})

const shippingInfo = computed(() => {
  if (!isLocationComplete.value) {
    return null
  }
  return getShippingInfo(props.walkInLocation)
})

const formatCurrency = (value: number) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

/**
 * Tính phí vận chuyển từ GHN API và set giá mặc định
 */
const fetchShippingFeeFromGHN = async () => {
  if (!isLocationComplete.value) {
    return
  }

  isCalculating.value = true
  try {
    const result = await calculateShippingFeeFromGHN(props.walkInLocation)
    apiCalculatedFee.value = result.fee

    // Set giá GHN làm giá mặc định nếu chưa có giá custom
    if (shippingFee.value === 0 || shippingFee.value === apiCalculatedFee.value) {
      shippingFee.value = result.fee
      emit('update:shippingFee', result.fee)
    }
  } catch (error) {
    console.error('Error fetching shipping fee from GHN:', error)
    apiCalculatedFee.value = 0
  } finally {
    isCalculating.value = false
  }
}

/**
 * Xử lý thay đổi phí vận chuyển khi nhân viên nhập hoặc chỉnh sửa
 */
const handleShippingFeeChange = (value: number | null) => {
  if (value !== null && value !== undefined) {
    shippingFee.value = value
    emit('update:shippingFee', value)
  }
}

/**
 * Auto-fetch GHN fee khi location thay đổi
 */
watch(
  () => props.walkInLocation,
  async () => {
    if (isLocationComplete.value) {
      await fetchShippingFeeFromGHN()
    }
  },
  { deep: true }
)

/**
 * Auto-fetch khi chuyển sang delivery mode
 */
watch(
  () => props.orderType,
  async (newType) => {
    if (newType === 'delivery') {
      setTimeout(async () => {
        if (isLocationComplete.value) {
          await fetchShippingFeeFromGHN()
        }
      }, 0)
    } else {
      // Reset khi chuyển sang mode khác
      shippingFee.value = 0
      apiCalculatedFee.value = 0
      emit('update:shippingFee', 0)
    }
  }
)

/**
 * Auto-fetch khi selectedCustomer address thay đổi
 */
watch(
  () => props.selectedCustomer?.address,
  async () => {
    if (props.orderType === 'delivery' && isLocationComplete.value) {
      await fetchShippingFeeFromGHN()
    }
  }
)

/**
 * Sync shipping fee từ parent khi cập nhật từ bên ngoài
 */
watch(
  () => props.shippingFeeFromParent,
  (newFee) => {
    if (newFee !== undefined && newFee !== null) {
      shippingFee.value = newFee
    }
  }
)

/**
 * Export method để parent có thể trigger recalculation
 */
const triggerRecalculation = async () => {
  if (isLocationComplete.value) {
    await fetchShippingFeeFromGHN()
  }
}

defineExpose({ triggerRecalculation })
</script>

<style scoped>
:deep(.arco-statistic) {
  width: 100%;
}

.shipping-fee-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.shipping-fee-label-text {
  margin-right: 6px;
}

.shipping-fee-logo {
  height: 16px;
  width: auto;
  object-fit: contain;
  margin-left: auto;
}
</style>
