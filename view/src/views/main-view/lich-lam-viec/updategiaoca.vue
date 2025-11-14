<template>
  <div class="shift-handover-page">
    <Breadcrumb :items="breadcrumbItems" />

    <a-card :title="`Cập nhật Giao Ca #${idParam ?? ''}`" class="form-card">
      <!-- dùng @submit (không .prevent) vì <a-form> emit payload, xử lý prevent nếu nhận DOM Event trong submitForm -->
      <a-form layout="vertical" :model="form" @submit="submitForm">
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
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="Tổng tiền ban đầu">
              <a-input-number v-model="form.tongTienBanDau" :precision="0" :step="1000" style="width: 100%" />
            </a-form-item>
          </a-col>

          <a-col :span="12">
            <a-form-item label="Tổng tiền cuối ca">
              <a-input-number v-model="form.tongTienCuoiCa" :precision="0" :step="1000" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="Ghi chú">
          <a-textarea v-model="form.ghiChu" :auto-size="{ minRows: 2, maxRows: 5 }" allow-clear />
        </a-form-item>

        <div class="actions-row">
          <a-space>
            <a-button @click="restoreOriginal">
              <template #icon><IconRefresh /></template>
              Đặt lại
            </a-button>

            <a-button @click="goBack">Hủy</a-button>

            <a-button type="primary" html-type="submit" :loading="loadingSubmit || loadingInitial">
              <template #icon><IconSave /></template>
              Lưu thay đổi
            </a-button>
          </a-space>
        </div>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { Message } from '@arco-design/web-vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { IconSave, IconRefresh } from '@arco-design/web-vue/es/icon'

// Breadcrumb
const { breadcrumbItems } = useBreadcrumb()

const route = useRoute()
const router = useRouter()

// route param expected: id (or giaoCaId). Use route.params.id
const idParam = route.params.id ?? route.params.giaoCaId ?? null

// Data interfaces
interface NhanVien {
  id: number
  tenNhanVien: string
}
interface CaLamViec {
  id: number
  tenCa: string
}

const nhanViens = ref<NhanVien[]>([])
const caLamViecs = ref<CaLamViec[]>([])

const loadingNhanVien = ref(false)
const loadingCa = ref(false)
const loadingSubmit = ref(false)
const loadingInitial = ref(false)

// Keep original fetched data to allow restore
const originalData = ref<any>(null)

const form = ref({
  nguoiGiaoId: null as number | null,
  nguoiNhanId: null as number | null,
  caLamViecId: null as number | null,
  thoiGianGiaoCa: '' as string,
  tongTienBanDau: null as number | null,
  tongTienCuoiCa: null as number | null,
  ghiChu: '',
})
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

// Restore to original fetched values
const restoreOriginal = () => {
  if (originalData.value) {
    Object.assign(form.value, {
      nguoiGiaoId: originalData.value.nguoiGiaoId ?? null,
      nguoiNhanId: originalData.value.nguoiNhanId ?? null,
      caLamViecId:
        originalData.value.caLamViecId !== undefined && originalData.value.caLamViecId !== null
          ? Number(originalData.value.caLamViecId)
          : null,
      thoiGianGiaoCa: originalData.value.thoiGianGiaoCa ?? '',
      tongTienBanDau: originalData.value.tongTienBanDau ?? null,
      tongTienCuoiCa: originalData.value.tongTienCuoiCa ?? null,
      ghiChu: originalData.value.ghiChu ?? '',
    })
    Message.info('Đã phục hồi giá trị ban đầu')
  } else {
    resetForm()
  }
}



const validateForm = () => {
  const errors: string[] = []
  const f = form.value
  if (!f.nguoiGiaoId) errors.push('Vui lòng chọn người giao')
  if (!f.nguoiNhanId) errors.push('Vui lòng chọn người nhận')
  if (f.nguoiGiaoId === f.nguoiNhanId) errors.push('Người giao và người nhận không thể trùng nhau')
  if (!f.caLamViecId && f.caLamViecId !== 0) errors.push('Vui lòng chọn ca làm việc')
  if (!f.thoiGianGiaoCa) errors.push('Vui lòng chọn thời gian giao ca')
  if (f.tongTienBanDau !== null && f.tongTienBanDau < 0) errors.push('Tổng tiền ban đầu không thể là số âm')
  return errors
}

// Submit: accept either DOM Event or component payload
const submitForm = async (maybeEventOrPayload?: any) => {
  if (maybeEventOrPayload && typeof maybeEventOrPayload.preventDefault === 'function') {
    maybeEventOrPayload.preventDefault()
  }

  const errors = validateForm()
  if (errors.length) {
    Message.error(errors.join('\n'))
    return
  }

  if (!idParam) {
    Message.error('Không có id giao ca để cập nhật')
    return
  }

  loadingSubmit.value = true
  try {
    const caLamViecIdNumber =
      form.value.caLamViecId !== null && form.value.caLamViecId !== undefined ? Number(form.value.caLamViecId) : null
    const payload = {
      nguoiGiaoId: form.value.nguoiGiaoId,
      nguoiNhanId: form.value.nguoiNhanId,
      caLamViecId: caLamViecIdNumber,
      thoiGianGiaoCa: form.value.thoiGianGiaoCa,
      tongTienBanDau: form.value.tongTienBanDau,
      tongTienCuoiCa: form.value.tongTienCuoiCa,
      ghiChu: form.value.ghiChu || null,
    }

    // PUT or PATCH depending on your backend; using PUT here
    await axios.put(`http://localhost:8080/api/giao-ca/${idParam}`, payload)
    Message.success('✅ Cập nhật giao ca thành công!')
    router.back()
  } catch (err: any) {
    Message.error(`❌ ${err.response?.data?.message || 'Có lỗi xảy ra khi cập nhật giao ca'}`)
  } finally {
    loadingSubmit.value = false
  }
}

const goBack = () => {
  router.back()
}

// Fetch employees
const fetchNhanViens = async () => {
  loadingNhanVien.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/nhan-vien-management/playlist')
    const rawList = res.data?.data ?? res.data ?? []
    // Normalize to array of objects with numeric id
    nhanViens.value = (Array.isArray(rawList) ? rawList : [])
      .map((nv: any) => ({
        id: Number(nv?.id ?? nv?.employeeId ?? nv?.value),
        tenNhanVien: nv?.tenNhanVien ?? nv?.ten ?? nv?.name ?? nv?.fullName ?? 'Không tên',
      }))
      .filter((x: any) => Number.isFinite(x.id))
    console.log('nhanViens loaded', nhanViens.value)
  } catch (err) {
    // eslint-disable-next-line no-console
    console.error('Lỗi tải nhân viên', err)
    Message.error('Không thể tải danh sách nhân viên')
  } finally {
    loadingNhanVien.value = false
  }
}

// Fetch shifts (same robust handler as add form)
const fetchCaLamViecs = async () => {
  loadingCa.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/ca-lam-viec')
    // normalize possible shapes
    const candidates = [res.data, res.data?.data, res.data?.items, res.data?.payload, res.data?.result, res.data?.caLamViecs]
    const arr = candidates.find((c) => Array.isArray(c)) as any[] | undefined

    let finalArr = arr
    if (!finalArr && res.data && typeof res.data === 'object') {
      const maybeValues = Object.values(res.data).filter((v) => v && (Array.isArray(v) || typeof v === 'object'))
      if (maybeValues.length) {
        const firstArray = maybeValues.find((v) => Array.isArray(v))
        finalArr = (firstArray as any[]) ?? (maybeValues[0] as any[])
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
        const idRaw = item?.id ?? item?.caId ?? item?.value ?? null
        const id = idRaw !== undefined && idRaw !== null ? Number(idRaw) : NaN
        const tenCa = item?.tenCa ?? item?.ten ?? item?.name ?? item?.ten_ca ?? item?.label ?? null
        return {
          id,
          tenCa: tenCa ?? (Number.isFinite(id) ? `Ca ${id}` : 'Ca không tên'),
        }
      })
      .filter((c: any) => Number.isFinite(c.id))
    console.log('caLamViecs loaded', caLamViecs.value)
  } catch (err) {
    // eslint-disable-next-line no-console
    console.error('Lỗi tải ca làm việc', err)
    Message.error('Không thể tải danh sách ca làm việc')
  } finally {
    loadingCa.value = false
  }
}

// Fetch existing giao ca by id and populate form
const fetchExistingGiaoCa = async () => {
  if (!idParam) {
    Message.error('Không tìm thấy id giao ca')
    router.back()
    return
  }

  loadingInitial.value = true
  try {
    const res = await axios.get(`http://localhost:8080/api/giao-ca/${idParam}`)
    const raw = res.data?.data ?? res.data ?? res

    // Try to extract fields from raw or raw.data
    const item = raw?.data ?? raw?.giaoCa ?? raw ?? null

    const nguoiGiaoIdRaw = item?.nguoiGiaoId ?? item?.nguoi_giao_id ?? item?.nguoi_giao ?? null
    const nguoiNhanIdRaw = item?.nguoiNhanId ?? item?.nguoi_nhan_id ?? item?.nguoi_nhan ?? null
    const caLamViecIdRaw = item?.caLamViecId ?? item?.ca_lam_viec_id ?? item?.caId ?? item?.ca_id ?? null
    const thoiGianGiaoCa = item?.thoiGianGiaoCa ?? item?.thoi_gian_giao_ca ?? item?.thoiGian ?? ''
    const tongTienBanDau = item?.tongTienBanDau ?? item?.tong_tien_ban_dau ?? null
    const tongTienCuoiCa = item?.tongTienCuoiCa ?? item?.tong_tien_cuoi_ca ?? null
    const ghiChu = item?.ghiChu ?? item?.ghi_chu ?? item?.note ?? ''

    const nguoiGiaoId = nguoiGiaoIdRaw !== undefined && nguoiGiaoIdRaw !== null ? Number(nguoiGiaoIdRaw) : null
    const nguoiNhanId = nguoiNhanIdRaw !== undefined && nguoiNhanIdRaw !== null ? Number(nguoiNhanIdRaw) : null
    const caLamViecId = caLamViecIdRaw !== undefined && caLamViecIdRaw !== null ? Number(caLamViecIdRaw) : null

    originalData.value = {
      nguoiGiaoId,
      nguoiNhanId,
      caLamViecId,
      thoiGianGiaoCa,
      tongTienBanDau,
      tongTienCuoiCa,
      ghiChu,
    }

    // populate form (IDs as numbers)
    Object.assign(form.value, {
      nguoiGiaoId: nguoiGiaoId ?? null,
      nguoiNhanId: nguoiNhanId ?? null,
      caLamViecId: caLamViecId !== undefined && caLamViecId !== null && !Number.isNaN(caLamViecId) ? caLamViecId : null,
      thoiGianGiaoCa: thoiGianGiaoCa ?? '',
      tongTienBanDau: tongTienBanDau ?? null,
      tongTienCuoiCa: tongTienCuoiCa ?? null,
      ghiChu: ghiChu ?? '',
    })

    console.log('giaoCa loaded', { originalData: originalData.value, form: form.value })
  } catch (err) {
    // eslint-disable-next-line no-console
    console.error('Lỗi tải giao ca', err)
    Message.error('Không thể tải dữ liệu giao ca')
  } finally {
    loadingInitial.value = false
  }
}

onMounted(async () => {
  // Load options first to ensure Select has items when we set v-model values
  await Promise.all([fetchNhanViens(), fetchCaLamViecs()])
  // Then load the existing giao ca and assign values
  await fetchExistingGiaoCa()
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
:deep(.arco-btn) {
  border-radius: 4px;
  height: 36px;
  padding: 0 16px;
  font-weight: 500;
}
</style>
