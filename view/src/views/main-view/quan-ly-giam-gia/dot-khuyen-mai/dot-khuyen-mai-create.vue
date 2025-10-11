<template>
  <div class="promotion-create-page">
    <Breadcrumb :items="breadcrumbItems" />

    <a-card class="promotion-card">
      <div class="page-header">
        <div>
          <h2 class="page-title">Tạo đợt khuyến mãi</h2>
          <p class="page-description">Điền thông tin chi tiết cho đợt khuyến mãi mới trước khi lưu.</p>
        </div>
        <a-space>
          <a-button @click="goBack">Quay lại</a-button>
          <a-button type="primary" :loading="confirmSaveSubmitting" @click="handleSaveClick">Lưu khuyến mãi</a-button>
        </a-space>
      </div>

      <a-form ref="formRef" :model="formState" :rules="rules" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="name" label="Tên đợt khuyến mãi">
              <a-input v-model="formState.name" placeholder="Nhập tên đợt khuyến mãi" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="code" label="Mã">
              <a-input v-model="formState.code" placeholder="Mã tự động" :disabled="true" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="discountValue" label="Giá trị giảm (%)">
              <a-input-number
                v-model="formState.discountValue"
                :min="1"
                :max="100"
                :step="1"
                :precision="0"
                suffix="%"
                placeholder="Nhập giá trị giảm..."
                style="width: 100%"
              />
              <div style="margin-top: 4px; font-size: 12px; color: var(--color-text-3)">Giá trị từ 1-100</div>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="dateRange" label="Thời gian áp dụng">
              <a-range-picker
                v-model="formState.dateRange"
                :show-time="true"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="DD/MM/YYYY HH:mm"
                allow-clear
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <div class="form-footer">
          <a-space>
            <a-button @click="goBack">Hủy</a-button>
            <a-button type="primary" :loading="confirmSaveSubmitting" @click="handleSaveClick">Lưu khuyến mãi</a-button>
          </a-space>
        </div>
      </a-form>
    </a-card>

    <!-- Confirmation Modal -->
    <a-modal
      v-model:visible="confirmSaveVisible"
      title="Xác nhận tạo đợt khuyến mãi"
      :confirm-loading="confirmSaveSubmitting"
      @ok="confirmSave"
      @cancel="confirmSaveVisible = false"
      ok-text="Xác nhận"
      cancel-text="Hủy"
      width="560px"
    >
      <p style="margin-bottom: 16px; color: var(--color-text-2)">Vui lòng kiểm tra lại thông tin trước khi lưu:</p>
      <a-descriptions :column="1" bordered>
        <a-descriptions-item label="Tên đợt khuyến mãi">
          {{ formState.name }}
        </a-descriptions-item>
        <a-descriptions-item label="Mã">
          {{ formState.code }}
        </a-descriptions-item>
        <a-descriptions-item label="Giá trị giảm">{{ formState.discountValue }}%</a-descriptions-item>
        <a-descriptions-item label="Thời gian áp dụng">
          {{ formatDateRange(formState.dateRange) }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, watch } from 'vue'
import type { FormInstance, FormRules } from '@arco-design/web-vue/es/form'
import { Message } from '@arco-design/web-vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { useRouter } from 'vue-router'
import { createPromotionCampaign, fetchPromotionCampaigns } from '@/api/discount-management'

const { breadcrumbItems } = useBreadcrumb()
const router = useRouter()

const formRef = ref<FormInstance>()
const confirmSaveVisible = ref(false)
const confirmSaveSubmitting = ref(false)

const formState = reactive({
  name: '',
  code: '',
  discountValue: 10,
  dateRange: [] as string[],
})

// Real-time validation for discount value
watch(
  () => formState.discountValue,
  (newValue) => {
    if (newValue !== undefined && newValue !== null && newValue > 100) {
      Message.warning('Giá trị giảm không được vượt quá 100%')
      formState.discountValue = 100
    }
  }
)

const rules: FormRules = {
  name: [{ required: true, message: 'Vui lòng nhập tên đợt khuyến mãi' }],
  discountValue: [
    { required: true, message: 'Vui lòng nhập giá trị giảm' },
    {
      validator: (value, callback) => {
        if (value === undefined || value === null || value === '') {
          callback('Vui lòng nhập giá trị giảm')
          return
        }
        const numeric = Number(value)
        if (Number.isNaN(numeric) || numeric <= 0) {
          callback('Giá trị giảm phải lớn hơn 0')
          return
        }
        if (numeric > 100) {
          callback('Giá trị giảm tối đa 100%')
          return
        }
        callback()
      },
    },
  ],
  dateRange: [
    { required: true, type: 'array', message: 'Vui lòng chọn khoảng thời gian' },
    {
      validator: (value: string[], callback) => {
        if (!Array.isArray(value) || value.length !== 2) {
          callback('Vui lòng chọn khoảng thời gian')
          return
        }
        const [start, end] = value
        if (!start || !end) {
          callback('Vui lòng chọn khoảng thời gian')
          return
        }
        if (new Date(start) > new Date(end)) {
          callback('Ngày kết thúc phải sau ngày bắt đầu')
          return
        }
        callback()
      },
    },
  ],
}

const generateNextCode = async () => {
  try {
    const promotions = await fetchPromotionCampaigns()

    if (!promotions || promotions.length === 0) {
      return 'DGG000001'
    }

    // Extract all codes that match the DGG pattern
    const dggCodes = promotions
      .map((p) => p.maDotGiamGia)
      .filter((code) => code && code.startsWith('DGG'))
      .map((code) => {
        const numPart = code.substring(3)
        return parseInt(numPart, 10)
      })
      .filter((num) => !Number.isNaN(num))

    if (dggCodes.length === 0) {
      return 'DGG000001'
    }

    // Find the maximum number and increment
    const maxNum = Math.max(...dggCodes)
    const nextNum = maxNum + 1

    // Format with leading zeros (6 digits)
    return `DGG${nextNum.toString().padStart(6, '0')}`
  } catch {
    // Fallback to DGG000001 if fetch fails
    return 'DGG000001'
  }
}

onMounted(async () => {
  formState.code = await generateNextCode()
})

const goBack = () => {
  router.back()
}

const formatDateRange = (dateRange: string[]) => {
  if (!dateRange || dateRange.length !== 2) return ''
  const [start, end] = dateRange
  if (!start || !end) return ''

  const formatDateTime = (dateStr: string) => {
    const date = new Date(dateStr)
    return date.toLocaleString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    })
  }

  return `${formatDateTime(start)} - ${formatDateTime(end)}`
}

const handleSaveClick = async () => {
  const form = formRef.value
  if (!form) return

  try {
    const errors = await form.validate()
    if (errors) {
      return
    }
  } catch {
    // Validation failed, errors are already displayed by the form
    return
  }

  // All validations passed, show confirmation modal
  confirmSaveVisible.value = true
}

const calculateStatus = (startDate: string, endDate: string): boolean => {
  const now = new Date()
  const start = new Date(startDate)
  const end = new Date(endDate)

  // Status is true (active) if current datetime is within the datetime range
  return now >= start && now <= end
}

const confirmSave = async () => {
  if (confirmSaveSubmitting.value) return

  const [startDate, endDate] = formState.dateRange

  const payload = {
    maDotGiamGia: formState.code,
    tenDotGiamGia: formState.name.trim(),
    giaTriGiamGia: Number(formState.discountValue),
    ngayBatDau: startDate,
    ngayKetThuc: endDate,
    trangThai: calculateStatus(startDate, endDate),
    deleted: false,
  }

  confirmSaveSubmitting.value = true
  try {
    await createPromotionCampaign(payload)
    Message.success('Tạo đợt khuyến mãi thành công')
    confirmSaveVisible.value = false
    router.push({ name: 'QuanLyDotKhuyenMai' })
  } catch (error) {
    Message.error((error as Error).message || 'Không thể tạo đợt khuyến mãi')
  } finally {
    confirmSaveSubmitting.value = false
  }
}
</script>

<style scoped>
.promotion-create-page {
  padding: 0 20px 20px 20px;
}

.promotion-card {
  margin-top: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  gap: 16px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-1);
}

.page-description {
  margin: 4px 0 0 0;
  color: var(--color-text-3);
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
