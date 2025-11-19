<template>
  <div class="shift-handover-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <a-card title="Giao Ca" class="form-card">
      <!-- Bỏ .prevent vì <a-form> là component (emit payload), không phải DOM Event.
           Thay vào đó dùng @submit và trong submitForm kiểm tra nếu nhận Event thì gọi preventDefault(). -->
      <a-form layout="vertical" :model="form" @submit="submitForm">
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
              <a-select
                v-model="form.caLamViecId"
                placeholder="Chọn ca làm việc"
                allow-clear
                :loading="loadingCa"
                not-found-content="Không có ca để chọn"
              >
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
              <a-input-number
                v-model="form.tongTienBanDau"
                placeholder="Nhập tổng tiền ban đầu"
                :precision="0"
                :step="1000"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>

          <a-col :span="12">
            <a-form-item label="Tổng tiền cuối ca">
              <a-input-number
                v-model="form.tongTienCuoiCa"
                placeholder="Nhập tổng tiền cuối ca"
                :precision="0"
                :step="1000"
                style="width: 100%"
              />
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

            <!-- Nút Hủy: quay về trang trước (hoặc có thể đổi sang router.push('/duong-dan-danh-sach') nếu muốn) -->
            <a-button @click="goBack">Hủy</a-button>

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
import { useRouter } from 'vue-router'
import axios from 'axios'
import { Message, Modal } from '@arco-design/web-vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { IconSave, IconRefresh } from '@arco-design/web-vue/es/icon'

// Breadcrumb
const { breadcrumbItems } = useBreadcrumb()

const router = useRouter()

// Data interfaces
interface NhanVien {
  id: number
  tenNhanVien: string
}

interface CaLamViec {
  id: string
  tenCa: string
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
  // Use string for select value to avoid type mismatch between number/string coming from API
  caLamViecId: null as string | null,
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

  if (f.tongTienCuoiCa !== null && f.tongTienCuoiCa < 0) errors.push('Tổng tiền cuối ca không thể là số âm')

  if (f.tongTienBanDau !== null && f.tongTienCuoiCa !== null && f.tongTienCuoiCa < f.tongTienBanDau) {
    errors.push('Tổng tiền cuối ca không thể nhỏ hơn tổng tiền ban đầu')
  }

  return errors
}

// Submit form
// Accept possible event or payload emitted by <a-form>.
// If an actual DOM Event is passed, call preventDefault on it.
// If <a-form> emits some payload (object), we ignore preventDefault (it won't be an Event).
const submitForm = async (maybeEventOrPayload?: any) => {
  if (maybeEventOrPayload && typeof maybeEventOrPayload.preventDefault === 'function') {
    maybeEventOrPayload.preventDefault()
  }

  // Xác nhận trước khi lưu
  const confirmed = await new Promise<boolean>((resolve) => {
    Modal.confirm({
      title: 'Xác nhận lưu giao ca',
      content: 'Bạn có chắc chắn muốn lưu thông tin giao ca này không?',
      okText: 'Lưu',
      cancelText: 'Hủy',
      onOk: () => resolve(true),
      onCancel: () => resolve(false),
    })
  })

  if (!confirmed) return

  const errors = validateForm()
  if (errors.length) {
    Message.error(errors.join('\n'))
    return
  }

  loadingSubmit.value = true
  try {
    const caLamViecIdNumber = form.value.caLamViecId ? Number(form.value.caLamViecId) : null

    const payload = {
      nguoiGiaoId: form.value.nguoiGiaoId,
      nguoiNhanId: form.value.nguoiNhanId,
      caLamViecId: caLamViecIdNumber,
      thoiGianGiaoCa: form.value.thoiGianGiaoCa,
      tongTienBanDau: form.value.tongTienBanDau,
      tongTienCuoiCa: form.value.tongTienCuoiCa,
      ghiChu: form.value.ghiChu || null,
    }

    await axios.post('http://localhost:8080/api/giao-ca', payload)
    Message.success(' Giao ca thành công!')
    resetForm()
    router.back()
  } catch (err: any) {
    Message.error(` ${err.response?.data?.message || 'Có lỗi xảy ra khi thêm giao ca'}`)
  } finally {
    loadingSubmit.value = false
  }
}

// Nút Hủy: quay về trang trước. Nếu bạn muốn chuyển tới 1 route cố định, đổi router.back() -> router.push('/duong-dan-danh-sach')
const goBack = () => {
  router.back()
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
 * Fetch ca làm việc with robust response handling
 */
const fetchCaLamViecs = async () => {
  loadingCa.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/ca-lam-viec')

    // Normalize root source:
    // - If axios returned the array directly (res is an array), use it.
    // - Otherwise prefer res.data, then fallback to res.
   let root: any;

if (Array.isArray(res)) {
  root = res;
} else if (Array.isArray(res?.data)) {
  root = res.data;
} else {
  root = res?.data ?? res;
}


    // debug logs to help see what structure we received
    // eslint-disable-next-line no-console
    console.log('API caLamViec response raw:', res)
    // eslint-disable-next-line no-console
    console.log('API caLamViec normalized root (will attempt to extract list from this):', root)

    const candidates = [
      root,
      (root as any)?.data,
      (root as any)?.items,
      (root as any)?.payload,
      (root as any)?.result,
      (root as any)?.caLamViecs,
    ]

    // Find first array candidate
    const arr = candidates.find((c) => Array.isArray(c)) as any[] | undefined

    let finalArr = arr
    if (!finalArr && root && typeof root === 'object') {
      const maybeValues = Object.values(root).filter((v) => v && (Array.isArray(v) || typeof v === 'object'))
      if (maybeValues.length) {
        const firstArray = maybeValues.find((v) => Array.isArray(v))
        finalArr = (firstArray as any[]) ?? (maybeValues[0] as any[])
      }
    }

    if (!finalArr) {
      caLamViecs.value = []
      // eslint-disable-next-line no-console
      console.warn('Dữ liệu ca làm việc không đúng định dạng hoặc rỗng:', root)
      return
    }

    caLamViecs.value = finalArr
      .map((item: any) => {
        // Normalize id to string to avoid v-model mismatch
        const idRaw = item?.id ?? item?.caId ?? item?.value ?? item?.code ?? item?.maCa ?? item?.key
        const id = idRaw !== undefined && idRaw !== null ? String(idRaw) : null

        // Resolve display name robustly
        let tenCa: any = item?.tenCa ?? item?.ten ?? item?.name ?? item?.ten_ca ?? item?.nameCa ?? item?.label ?? item?.description ?? null

        if (tenCa && typeof tenCa === 'object') {
          tenCa = tenCa.name ?? tenCa.label ?? tenCa.value ?? JSON.stringify(tenCa)
        }

        if ((typeof item === 'string' || typeof item === 'number') && !tenCa) {
          tenCa = String(item)
        }

        const finalLabel = tenCa ?? (id ? `Ca ${id}` : 'Ca không tên')

        return {
          id,
          tenCa: finalLabel,
        }
      })
      .filter((c: any) => c.id !== null && c.id !== undefined)

    if (!caLamViecs.value.length) {
      // eslint-disable-next-line no-console
      console.warn('Sau khi mapping, danh sách ca làm việc rỗng. Full normalized root:', root)
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
