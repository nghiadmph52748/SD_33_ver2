<template>
  <div class="shift-handover-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <a-card title="Thêm Giao Ca" class="form-card">
      <a-form layout="vertical" :model="form" @submit.prevent="submitForm">
        <!-- Người giao & nhận -->
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="Người giao" required>
              <a-select v-model="form.nguoiGiaoId" placeholder="Chọn người giao" allow-clear :loading="loadingNhanVien">
                <a-option v-for="nv in nhanViens" :key="nv.id" :value="nv.id">{{ nv.tenNhanVien }}</a-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="12">
            <a-form-item label="Người nhận" required>
              <a-select v-model="form.nguoiNhanId" placeholder="Chọn người nhận" allow-clear :loading="loadingNhanVien">
                <a-option v-for="nv in nhanViens" :key="nv.id" :value="nv.id">{{ nv.tenNhanVien }}</a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Ca làm việc & Thời gian -->
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="Ca làm việc" required>
              <a-select v-model="form.caLamViecId" placeholder="Chọn ca làm việc" allow-clear :loading="loadingCa" not-found-content="Không có ca để chọn">
                <a-option v-for="ca in caLamViecs" :key="ca.id" :value="ca.id">{{ ca.tenCa }}</a-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="12">
            <a-form-item label="Thời gian giao ca" required>
              <a-date-picker
                v-model="form.thoiGianGiaoCa"
                show-time
                format="YYYY-MM-DD HH:mm"
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Tổng tiền ban đầu & Tổng tiền cuối ca -->
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="Tổng tiền ban đầu">
              <a-input-number v-model="form.tongTienBanDau" placeholder="Nhập tổng tiền ban đầu" :precision="0" :step="1000" style="width: 100%" />
            </a-form-item>
          </a-col>

          <a-col :span="12">
            <a-form-item label="Tổng tiền cuối ca">
              <a-input-number v-model="form.tongTienCuoiCa" placeholder="Nhập tổng tiền cuối ca" :precision="0" :step="1000" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Ghi chú -->
        <a-form-item label="Ghi chú">
          <a-textarea v-model="form.ghiChu" placeholder="Nhập ghi chú (nếu có)" :auto-size="{ minRows: 2, maxRows: 5 }" allow-clear />
        </a-form-item>

        <!-- Action buttons -->
        <div class="actions-row">
          <a-space>
            <a-button @click="resetForm">
              <template #icon><IconRefresh /></template>
              Đặt lại
            </a-button>
            <a-button type="primary" html-type="submit" :loading="loadingSubmit">
              <template #icon><IconSave /></template>
              Lưu giao ca
            </a-button>
          </a-space>
        </div>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { Message } from '@arco-design/web-vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { IconSave, IconRefresh } from '@arco-design/web-vue/es/icon'

// Breadcrumb
const { breadcrumbItems } = useBreadcrumb()

// Data interfaces
interface NhanVien {
  id: number,
  tenNhanVien: string,
}

interface CaLamViec {
  id: number,
  tenCa: string,
}

const nhanViens = ref<NhanVien[]>([])
const caLamViecs = ref<CaLamViec[]>([])

const loadingNhanVien = ref(false)
const loadingCa = ref(false)
const loadingSubmit = ref(false)

// Form
const form = ref({
  nguoiGiaoId: null as number | null,
  nguoiNhanId: null as number | null,
  caLamViecId: null as number | null,
  thoiGianGiaoCa: '' as string,
  tongTienBanDau: null as number | null,
  tongTienCuoiCa: null as number | null,
  ghiChu: '',
})

// Reset form
const resetForm = () => {
  Object.assign(form.value, {
    nguoiGiaoId: null,
    nguoiNhanId: null,
    caLamViecId: null,
    thoiGianGiaoCa: '',
    tongTienBanDau: null,
    tongTienCuoiCa: null,
    ghiChu: '',
  })
}

// Validate form
const validateForm = () => {
  const errors: string[] = []
  const f = form.value
  if (!f.nguoiGiaoId) errors.push('Vui lòng chọn người giao')
  if (!f.nguoiNhanId) errors.push('Vui lòng chọn người nhận')
  if (f.nguoiGiaoId === f.nguoiNhanId) errors.push('Người giao và người nhận không thể trùng nhau')
  if (!f.caLamViecId) errors.push('Vui lòng chọn ca làm việc')
  if (!f.thoiGianGiaoCa) errors.push('Vui lòng chọn thời gian giao ca')
  if (f.tongTienBanDau !== null && f.tongTienBanDau < 0) errors.push('Tổng tiền ban đầu không thể là số âm')
  return errors
}

// Submit form
const submitForm = async () => {
  const errors = validateForm()
  if (errors.length) {
    Message.error(errors.join('\n'))
    return
  }

  loadingSubmit.value = true
  try {
    const payload = {
      nguoiGiaoId: form.value.nguoiGiaoId,
      nguoiNhanId: form.value.nguoiNhanId,
      caLamViecId: form.value.caLamViecId,
      thoiGianGiaoCa: form.value.thoiGianGiaoCa,
      tongTienBanDau: form.value.tongTienBanDau,
      tongTienCuoiCa: form.value.tongTienCuoiCa,
      ghiChu: form.value.ghiChu || null,
    }

    await axios.post('http://localhost:8080/api/giao-ca', payload)
    Message.success('✅ Giao ca thành công!')
    resetForm()
  } catch (err: any) {
    Message.error(`❌ ${err.response?.data?.message || 'Có lỗi xảy ra khi thêm giao ca'}`)
  } finally {
    loadingSubmit.value = false
  }
}

// Fetch API: Nhân viên
const fetchNhanViens = async () => {
  loadingNhanVien.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/nhan-vien-management/playlist')
    nhanViens.value = res.data?.data ?? res.data ?? []
  } catch (err) {
    // eslint-disable-next-line no-console
    console.error('Lỗi tải nhân viên', err)
    Message.error('Không thể tải danh sách nhân viên')
  } finally {
    loadingNhanVien.value = false
  }
}

/**
 * Fetch ca làm việc with robust response handling and prettier-friendly code
 * - Try common shapes: res.data (array), res.data.data, res.data.items, res.data.payload, res.data.result
 * - Fallback: Object.values(res.data) when backend returns keyed object
 */
const fetchCaLamViecs = async () => {
  loadingCa.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/ca-lam-viec')
    // eslint-disable-next-line no-console
    console.log('API caLamViec full response:', res)

    const candidates = [
      res.data,
      res.data?.data,
      res.data?.items,
      res.data?.payload,
      res.data?.result,
      res.data?.caLamViecs,
    ]

    // Find first array candidate (avoid for..of to satisfy lint rules)
    const arr = candidates.find((c) => Array.isArray(c)) as any[] | undefined

    let finalArr = arr
    if (!finalArr && res.data && typeof res.data === 'object') {
      const maybeValues = Object.values(res.data).filter((v) => v && typeof v === 'object')
      if (maybeValues.length && maybeValues.every((v) => typeof v === 'object')) {
        finalArr = maybeValues as any[]
      }
    }

    if (!finalArr) {
      caLamViecs.value = []
      // eslint-disable-next-line no-console
      console.warn('Dữ liệu ca làm việc không đúng định dạng hoặc rỗng:', res.data)
      return
    }

    caLamViecs.value = finalArr
      .map((item: any) => {
        const id = item?.id ?? item?.caId ?? item?.value ?? null
        const tenCa =
          item?.tenCa ?? item?.ten ?? item?.name ?? item?.ten_ca ?? item?.nameCa ?? item?.label ?? null
        return {
          id,
          tenCa: tenCa ?? (id ? String(id) : 'Ca không tên'),
        }
      })
      .filter((c: any) => c.id !== null && c.id !== undefined)

    if (!caLamViecs.value.length) {
      // eslint-disable-next-line no-console
      console.warn('Sau khi mapping, danh sách ca làm việc rỗng. Full response:', res.data)
    }
  } catch (err) {
    // eslint-disable-next-line no-console
    console.error('Lỗi tải ca làm việc', err)
    Message.error('Không thể tải danh sách ca làm việc')
  } finally {
    loadingCa.value = false
  }
}

// Mount
onMounted(() => {
  fetchNhanViens()
  fetchCaLamViecs()
})
</script>

<style scoped>
.shift-handover-page {
  padding: 0 20px 20px 20px;
}

.form-card {
  margin-top: 16px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #e5e6eb;
}

.actions-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e5e6eb;
}

:deep(.arco-form-item-label) {
  font-weight: 500;
  color: #1d2129;
  margin-bottom: 8px;
}

:deep(.arco-input),
:deep(.arco-select),
:deep(.arco-input-number),
:deep(.arco-picker),
:deep(.arco-textarea) {
  width: 100%;
  border-radius: 4px;
  border: 1px solid #e5e6eb;
  transition: all 0.1s ease-in-out;
}

:deep(.arco-input:focus),
:deep(.arco-select:focus),
:deep(.arco-input-number:focus),
:deep(.arco-picker:focus),
:deep(.arco-textarea:focus) {
  border-color: rgb(var(--primary-6));
  box-shadow: 0 0 0 2px rgba(var(--primary-6), 0.2);
}

:deep(.arco-btn) {
  border-radius: 4px;
  height: 36px;
  padding: 0 16px;
  font-weight: 500;
}

:deep(.arco-form-item-error-message) {
  color: rgb(var(--danger-6));
  font-size: 12px;
  margin-top: 4px;
}

:deep(.arco-card-header) {
  border-bottom: 1px solid #e5e6eb;
  padding: 16px 20px;
}

:deep(.arco-card-body) {
  padding: 20px;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .shift-handover-page {
    padding: 16px;
  }

  .form-card {
    margin-top: 12px;
  }
}
</style>