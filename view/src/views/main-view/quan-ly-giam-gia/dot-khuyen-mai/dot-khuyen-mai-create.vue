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
            <a-form-item field="discountValue" label="Giá trị giảm (%)">
              <a-input-number v-model="formState.discountValue" :min="1" :max="100" :precision="0" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="dateRange" label="Thời gian áp dụng">
              <a-range-picker v-model="formState.dateRange" value-format="YYYY-MM-DD" format="DD/MM/YYYY" allow-clear style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="active" label="Trạng thái">
              <a-switch v-model="formState.active" checked-text="Bật" unchecked-text="Tắt" />
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
      <p style="margin-bottom: 16px; color: var(--color-text-2);">Vui lòng kiểm tra lại thông tin trước khi lưu:</p>
      <a-descriptions :column="1" bordered>
        <a-descriptions-item label="Tên đợt khuyến mãi">
          {{ formState.name }}
        </a-descriptions-item>
        <a-descriptions-item label="Giá trị giảm">
          {{ formState.discountValue }}%
        </a-descriptions-item>
        <a-descriptions-item label="Thời gian áp dụng">
          {{ formatDateRange(formState.dateRange) }}
        </a-descriptions-item>
        <a-descriptions-item label="Trạng thái">
          <a-tag :color="formState.active ? 'green' : 'gray'">
            {{ formState.active ? 'Bật' : 'Tắt' }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { FormInstance, FormRules } from '@arco-design/web-vue/es/form'
import { Message } from '@arco-design/web-vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { useRouter } from 'vue-router'
import { createPromotionCampaign } from '@/api/discount-management'

const { breadcrumbItems } = useBreadcrumb()
const router = useRouter()

const formRef = ref<FormInstance>()
const confirmSaveVisible = ref(false)
const confirmSaveSubmitting = ref(false)

const formState = reactive({
  name: '',
  discountValue: 10,
  dateRange: [] as string[],
  active: true,
})

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

const goBack = () => {
  router.back()
}

const formatDateRange = (dateRange: string[]) => {
  if (!dateRange || dateRange.length !== 2) return ''
  const [start, end] = dateRange
  if (!start || !end) return ''
  
  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr)
    return date.toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    })
  }
  
  return `${formatDate(start)} - ${formatDate(end)}`
}

const handleSaveClick = async () => {
  const form = formRef.value
  if (!form) return

  try {
    const errors = await form.validate()
    if (errors) {
      return
    }
  } catch (error) {
    // Validation failed, errors are already displayed by the form
    return
  }

  // All validations passed, show confirmation modal
  confirmSaveVisible.value = true
}

const confirmSave = async () => {
  if (confirmSaveSubmitting.value) return

  const [startDate, endDate] = formState.dateRange

  const payload = {
    tenDotGiamGia: formState.name.trim(),
    giaTriGiamGia: Number(formState.discountValue),
    ngayBatDau: startDate,
    ngayKetThuc: endDate,
    trangThai: formState.active,
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
