<template>
  <div class="update-ca-page">
    <Breadcrumb :items="breadcrumbItems" />

    <a-card class="update-ca-card" title="Cập nhật ca làm việc">
      <a-form :model="form" layout="vertical" class="form-inner">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="Tên ca" required>
              <a-input v-model="form.tenCa" placeholder="Nhập tên ca" />
            </a-form-item>
          </a-col>

          <a-col :span="6">
            <a-form-item label="Thời gian bắt đầu" required>
              <a-time-picker v-model="form.gioBatDau" format="HH:mm" style="width: 100%" />
            </a-form-item>
          </a-col>

          <a-col :span="6">
            <a-form-item label="Thời gian kết thúc" required>
              <a-time-picker v-model="form.gioKetThuc" format="HH:mm" style="width: 100%" />
            </a-form-item>
          </a-col>

          <a-col :span="24">
            <a-form-item label="Ghi chú">
              <a-textarea v-model="form.ghiChu" placeholder="Ghi chú (tùy chọn)" rows="3" />
            </a-form-item>
          </a-col>

        </a-row>

        <div class="form-actions">
          <a-space>
            <a-button @click="onCancel">Hủy</a-button>
            <a-button type="primary" @click="onSubmit">Lưu</a-button>
          </a-space>
        </div>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import { useRouter, useRoute } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { getCaLamViecById, suaCaLamViec } from '@/api/ca-lam-viec'

const { breadcrumbItems } = useBreadcrumb()
const router = useRouter()
const route = useRoute()

const form = ref({
  tenCa: '',
  gioBatDau: null as any,
  gioKetThuc: null as any,
  ghiChu: '',
})

const formatToLocalTime = (t: any) => {
  if (!t) return null
  if (typeof t === 'string') return t.length === 5 ? `${t}:00` : t
  if (t.format) return t.format('HH:mm:ss')
  return dayjs(t).format('HH:mm:ss')
}

// Load ca theo id
onMounted(async () => {
  const id = Number(route.params.id)
  if (!id) {
    Message.error('Không tìm thấy id ca làm việc')
    return
  }

  try {
    const res = await getCaLamViecById(id)
    const ca = res.data.data
    form.value.tenCa = ca.tenCa
    form.value.gioBatDau = dayjs(ca.thoiGianBatDau, 'HH:mm:ss')
    form.value.gioKetThuc = dayjs(ca.thoiGianKetThuc, 'HH:mm:ss')
    form.value.ghiChu = ca.ghiChu || ''
  } catch (err) {
    console.error(err)
    Message.error('Không thể tải thông tin ca làm việc')
  }
})


// Validate form
const validateForm = () => {
  if (!form.value.tenCa) {
    Message.warning('Vui lòng nhập tên ca')
    return false
  }
  if (!form.value.gioBatDau) {
    Message.warning('Vui lòng chọn thời gian bắt đầu')
    return false
  }
  if (!form.value.gioKetThuc) {
    Message.warning('Vui lòng chọn thời gian kết thúc')
    return false
  }
  return true
}

// Submit update
const onSubmit = async () => {
  if (!validateForm()) return
  const id = Number(route.params.id)

  try {
    const payload = {
      tenCa: form.value.tenCa,
      gioBatDau: formatToLocalTime(form.value.gioBatDau),
      gioKetThuc: formatToLocalTime(form.value.gioKetThuc),
    }
    await suaCaLamViec(id, payload)
    Message.success('Cập nhật ca thành công')
    router.push({ name: 'CaLamViec', query: { refresh: String(Date.now()) } })
  } catch (err) {
    console.error(err)
    Message.error('Cập nhật thất bại')
  }
}

const onCancel = () => {
  router.back()
}

</script>



<style scoped>
/* Page background giống trang quản lý */
.them-ca-page,
.schedule-management-page {
  background: #f5f7fa; /* nền sáng, không mờ */
  padding: 20px;
  min-height: 100%;
  color: #233043;
}

/* Card chung: giống box section trong ảnh */
.filters-card,
.table-card,
.them-ca-card,
.arco-card {
  background: #fff;
  border: 1px solid #e8eef3; /* rõ hơn để không thấy mờ */
  box-shadow: none;
  border-radius: 6px;
  margin-bottom: 16px;
}

/* Card header (tiêu đề section) */
:deep(.arco-card-header) {
  padding: 12px 16px;
  background: transparent;
  border-bottom: 1px solid #eef3f7;
  color: #273444;
  font-weight: 600;
  font-size: 14px;
}

/* Card body padding giống layout mẫu */
:deep(.arco-card-body) {
  padding: 16px;
}

/* Form label: đậm và màu rõ */
:deep(.arco-form-item-label) {
  color: #1f2d3d;
  font-weight: 600;
  margin-bottom: 8px;
}

/* Required star màu đỏ rõ */
:deep(.arco-form-item-label .arco-form-item-required) {
  color: #f5222d;
}

/* Input / TimePicker / Textarea: nền nhạt, border nhẹ, không bị mờ */
:deep(.arco-input),
:deep(.arco-time-picker),
:deep(.arco-textarea),
:deep(.arco-select-selection) {
  background: #f4f6f8;
  border: 1px solid #e6ebef;
  color: #1f2d3d;
  border-radius: 4px;
  height: 36px;
}

/* Input focus rõ hơn */
:deep(.arco-input:focus),
:deep(.arco-time-picker:focus),
:deep(.arco-textarea:focus),
:deep(.arco-select-selection:focus) {
  border-color: #1677ff;
  box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.08);
  background: #fff;
}

/* Label nhỏ phía dưới hoặc help text */
:deep(.arco-form-item-extra) {
  color: #8f9aa6;
}

/* Radio button group style giống mẫu (nhẹ nhàng) */
:deep(.arco-radio-group-button .arco-radio-button) {
  border-radius: 4px;
  border: 1px solid #e6ebef;
  background: #f8fafb;
  color: #1f2d3d;
}
:deep(.arco-radio-button-checked) {
  background: #e8f3ff;
  border-color: #1677ff;
}

/* Table / tag small adjustments */
:deep(.arco-table .arco-table-cell) {
  padding: 8px 12px;
  color: #2a3a45;
}

/* Footer action area giống ô trắng ở dưới ảnh (nút Lưu / Hủy) */
/* Nếu bạn có vùng footer riêng, gán class .form-footer */
.form-footer {
  background: #fff;
  border: 1px solid #e8eef3;
  padding: 14px 16px;
  border-radius: 6px;
  margin-top: 18px;
}

/* Nút primary nổi bật */
:deep(.arco-btn-primary) {
  background: #1677ff;
  border-color: #1677ff;
  color: #fff;
  box-shadow: none;
  border-radius: 4px;
}

/* Nút mặc định/secondary */
:deep(.arco-btn) {
  border-radius: 4px;
}

/* Fix nếu trước kia có opacity/filter làm mờ */
.them-ca-page, .them-ca-card, .filters-card, .table-card {
  opacity: 1 !important;
  filter: none !important;
}

/* Responsive: giữ spacing giống mẫu 2 cột/trên mobile xuống 1 cột */
@media (max-width: 768px) {
  :deep(.arco-col) {
    padding-bottom: 12px;
  }
  .form-footer {
    text-align: center;
  }
}
</style>