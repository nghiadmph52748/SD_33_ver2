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
          <a-button type="primary" :loading="submitting" @click="submitPromotion">Lưu khuyến mãi</a-button>
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
              <a-range-picker
                v-model="formState.dateRange"
                value-format="YYYY-MM-DD"
                format="DD/MM/YYYY"
                allow-clear
                style="width: 100%"
              />
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
            <a-button type="primary" :loading="submitting" @click="submitPromotion">Lưu khuyến mãi</a-button>
          </a-space>
        </div>
      </a-form>
    </a-card>
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
const submitting = ref(false)

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

const submitPromotion = async () => {
  if (submitting.value) return

  const form = formRef.value
  if (!form) return

  try {
    await form.validate()
  } catch (error) {
    return
  }

  const [startDate, endDate] = formState.dateRange

  const payload = {
    tenDotGiamGia: formState.name.trim(),
    giaTriGiamGia: Number(formState.discountValue),
    ngayBatDau: startDate,
    ngayKetThuc: endDate,
    trangThai: formState.active,
    deleted: false,
  }

  submitting.value = true
  try {
    await createPromotionCampaign(payload)
    Message.success('Tạo đợt khuyến mãi thành công')
    router.push({ name: 'QuanLyDotKhuyenMai' })
  } catch (error) {
    Message.error((error as Error).message || 'Không thể tạo đợt khuyến mãi')
  } finally {
    submitting.value = false
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
