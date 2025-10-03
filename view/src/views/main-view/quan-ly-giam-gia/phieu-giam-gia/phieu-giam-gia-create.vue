<template>
  <div class="coupon-create-page">
    <Breadcrumb :items="breadcrumbItems" />

    <a-card class="coupon-card">
      <div class="page-header">
        <div>
          <h2 class="page-title">Tạo phiếu giảm giá</h2>
          <p class="page-description">Cấu hình mã giảm giá trước khi phân phối đến khách hàng.</p>
        </div>
        <a-space>
          <a-button @click="goBack">Quay lại</a-button>
          <a-button type="primary" :loading="confirmSaveSubmitting" @click="handleSaveClick">Lưu phiếu giảm giá</a-button>
        </a-space>
      </div>

      <a-form ref="formRef" :model="formState" :rules="rules" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="name" label="Tên phiếu giảm giá">
              <a-input v-model="formState.name" placeholder="Nhập tên phiếu" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="discountMode" label="Hình thức giảm giá">
              <a-radio-group v-model="formState.discountMode" type="button">
                <a-radio value="percentage">Phần trăm (%)</a-radio>
                <a-radio value="amount">Số tiền (VNĐ)</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="discountValue" label="Giá trị giảm">
              <a-input-number
                v-model="formState.discountValue"
                :min="1"
                :max="isPercent ? 100 : undefined"
                :precision="0"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12" v-if="isPercent">
            <a-form-item field="maxDiscount" label="Giảm tối đa (VNĐ)">
              <a-input-number v-model="formState.maxDiscount" :min="1" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="minOrder" label="Đơn hàng tối thiểu (VNĐ)">
              <a-input-number v-model="formState.minOrder" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="quantity" label="Số lượng áp dụng">
              <a-input-number v-model="formState.quantity" :min="1" :precision="0" style="width: 100%" />
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

        <a-form-item field="description" label="Mô tả (tuỳ chọn)">
          <a-textarea v-model="formState.description" allow-clear :max-length="500" auto-size />
        </a-form-item>

        <div class="form-footer">
          <a-space>
            <a-button @click="goBack">Hủy</a-button>
            <a-button type="primary" :loading="confirmSaveSubmitting" @click="handleSaveClick">Lưu phiếu giảm giá</a-button>
          </a-space>
        </div>
      </a-form>
    </a-card>

    <!-- Confirmation Modal -->
    <a-modal
      v-model:visible="confirmSaveVisible"
      title="Xác nhận tạo phiếu giảm giá"
      :confirm-loading="confirmSaveSubmitting"
      @ok="confirmSave"
      @cancel="confirmSaveVisible = false"
      ok-text="Xác nhận"
      cancel-text="Hủy"
      width="560px"
    >
      <p style="margin-bottom: 16px; color: var(--color-text-2);">Vui lòng kiểm tra lại thông tin trước khi lưu:</p>
      <a-descriptions :column="1" bordered>
        <a-descriptions-item label="Tên phiếu giảm giá">
          {{ formState.name }}
        </a-descriptions-item>
        <a-descriptions-item label="Hình thức giảm giá">
          {{ isPercent ? 'Phần trăm (%)' : 'Số tiền (VNĐ)' }}
        </a-descriptions-item>
        <a-descriptions-item label="Giá trị giảm">
          <span v-if="isPercent">{{ formState.discountValue }}%</span>
          <span v-else>{{ formatCurrency(formState.discountValue) }}</span>
        </a-descriptions-item>
        <a-descriptions-item v-if="isPercent && formState.maxDiscount" label="Giảm tối đa">
          {{ formatCurrency(formState.maxDiscount) }}
        </a-descriptions-item>
        <a-descriptions-item label="Đơn hàng tối thiểu">
          {{ formState.minOrder ? formatCurrency(formState.minOrder) : 'Không giới hạn' }}
        </a-descriptions-item>
        <a-descriptions-item label="Số lượng áp dụng">
          {{ formState.quantity }}
        </a-descriptions-item>
        <a-descriptions-item label="Thời gian áp dụng">
          {{ formatDateRange(formState.dateRange) }}
        </a-descriptions-item>
        <a-descriptions-item label="Trạng thái">
          <a-tag :color="formState.active ? 'green' : 'gray'">
            {{ formState.active ? 'Bật' : 'Tắt' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item v-if="formState.description" label="Mô tả">
          {{ formState.description }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from '@arco-design/web-vue/es/form'
import { Message } from '@arco-design/web-vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { useRouter } from 'vue-router'
import { createCoupon } from '@/api/discount-management'

const { breadcrumbItems } = useBreadcrumb()
const router = useRouter()

const formRef = ref<FormInstance>()
const confirmSaveVisible = ref(false)
const confirmSaveSubmitting = ref(false)

const formState = reactive({
  name: '',
  discountMode: 'percentage' as 'percentage' | 'amount',
  discountValue: 10,
  maxDiscount: null as number | null,
  minOrder: 0,
  quantity: 1,
  dateRange: [] as string[],
  description: '',
  active: true,
})

const rules: FormRules = {
  name: [{ required: true, message: 'Vui lòng nhập tên phiếu giảm giá' }],
  discountMode: [{ required: true, message: 'Vui lòng chọn hình thức giảm giá' }],
  discountValue: [{ required: true, message: 'Vui lòng nhập giá trị giảm' }],
  quantity: [{ required: true, message: 'Vui lòng nhập số lượng áp dụng' }],
  dateRange: [{ required: true, type: 'array', message: 'Vui lòng chọn thời gian áp dụng' }],
}

const isPercent = computed(() => formState.discountMode === 'percentage')

watch(
  () => formState.discountMode,
  (mode) => {
    if (mode === 'amount') {
      formState.maxDiscount = null
    } else if (formState.discountValue > 100) {
      formState.discountValue = 100
    }
  }
)

const goBack = () => {
  router.back()
}

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
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

  const discountValue = Number(formState.discountValue)
  if (Number.isNaN(discountValue) || discountValue <= 0) {
    Message.error('Giá trị giảm phải lớn hơn 0')
    return
  }

  if (isPercent.value && discountValue > 100) {
    Message.error('Giá trị giảm theo phần trăm tối đa 100%')
    return
  }

  const [startDate, endDate] = formState.dateRange
  if (!startDate || !endDate) {
    Message.error('Vui lòng chọn thời gian áp dụng')
    return
  }
  if (new Date(startDate) > new Date(endDate)) {
    Message.error('Ngày kết thúc phải sau ngày bắt đầu')
    return
  }

  if (isPercent.value) {
    const capValue = Number(formState.maxDiscount)
    if (!capValue || Number.isNaN(capValue) || capValue <= 0) {
      Message.error('Vui lòng nhập mức giảm tối đa hợp lệ')
      return
    }
  }

  const quantityValue = Number(formState.quantity)
  if (!Number.isInteger(quantityValue) || quantityValue <= 0) {
    Message.error('Số lượng áp dụng phải lớn hơn 0')
    return
  }

  // All validations passed, show confirmation modal
  confirmSaveVisible.value = true
}

const confirmSave = async () => {
  if (confirmSaveSubmitting.value) return

  const discountValue = Number(formState.discountValue)
  const [startDate, endDate] = formState.dateRange
  const quantityValue = Number(formState.quantity)

  const payload = {
    tenPhieuGiamGia: formState.name.trim(),
    loaiPhieuGiamGia: !isPercent.value,
    giaTriGiamGia: discountValue,
    soTienToiDa: isPercent.value ? Number(formState.maxDiscount ?? 0) : null,
    hoaDonToiThieu: formState.minOrder ? Number(formState.minOrder) : 0,
    soLuongDung: quantityValue,
    ngayBatDau: startDate,
    ngayKetThuc: endDate,
    trangThai: formState.active,
    moTa: formState.description.trim() || null,
    deleted: false,
    idKhachHang: [],
  }

  confirmSaveSubmitting.value = true
  try {
    await createCoupon(payload)
    Message.success('Tạo phiếu giảm giá thành công')
    confirmSaveVisible.value = false
    router.push({ name: 'QuanLyPhieuGiamGia' })
  } catch (error) {
    Message.error((error as Error).message || 'Không thể tạo phiếu giảm giá')
  } finally {
    confirmSaveSubmitting.value = false
  }
}
</script>

<style scoped>
.coupon-create-page {
  padding: 0 20px 20px 20px;
}

.coupon-card {
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
