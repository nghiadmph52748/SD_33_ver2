<template>
  <div class="schedule-management-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="filterForm" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="search" placeholder="Tên nhân viên, ca làm..." allow-clear />
            </a-form-item>
          </a-col>

          <a-col :span="5">
            <a-form-item label="Ca làm việc">
              <a-select v-model="filterForm.caLam" placeholder="Chọn ca làm" allow-clear>
                <a-option value="Ca sáng">Ca sáng</a-option>
                <a-option value="Ca chiều">Ca chiều</a-option>
                <a-option value="Ca tối">Ca tối</a-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="5">
            <a-form-item label="Ngày làm việc">
              <a-date-picker v-model="filterForm.ngayLam" placeholder="Chọn ngày làm việc" format="YYYY-MM-DD" allow-clear />
            </a-form-item>
          </a-col>

          <a-col :span="8">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="filter" type="button">
                <a-radio value="all">Tất cả</a-radio>
                <a-radio value="working">Đã làm</a-radio>
                <a-radio value="off">Dự Kiến</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>

      <div class="actions-row">
        <a-space>
          <a-button @click="resetFilters">
            <template #icon><icon-refresh /></template>
            Đặt lại
          </a-button>

          <a-button type="primary" @click="themLichLamViec">
            <template #icon><icon-plus /></template>
            Thêm lịch làm việc
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!-- Schedule Table -->
    <a-card title="Danh sách lịch làm việc" class="table-card">
      <a-table :columns="columns" :data="filteredList" :pagination="pagination" :loading="loading" :scroll="{ x: 1200 }">
        <template #trangThai="{ record }">
          <a-tag
            :class="{
              'success-tag': record.trangThai === 'Đã làm',
              'info-tag': record.trangThai === 'Dự kiến',
            }"
          >
            {{ record.trangThai }}
          </a-tag>
        </template>

        <template #action="{ record }">
  <a-space>
    <!-- Nút sửa -->
    <a-button type="text" @click="editSchedule(record)">
      <template #icon><icon-edit /></template>
    </a-button>

    <!-- Nút tắt bật trạng thái -->
    <a-switch
      :checked="record.trangThai === 'Đã làm'"
      checked-color="blue"
      unchecked-color="#d9d9d9"
      @change="toggleTrangThai(record)"
    />
  </a-space>
</template>

      </a-table>
    </a-card>
  </div>
</template>
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { getLichLamViec } from '@/api/lich-lam-viec'
import axios from 'axios'
import { useRouter } from 'vue-router' 
import {
  IconPlus,
  IconRefresh,
  IconEdit,
  IconDelete
} from '@arco-design/web-vue/es/icon'
// removed unused XLSX import
const router = useRouter()

/* ===================== TYPE DEFINITIONS (nhỏ gọn, dùng cho component) ===================== */
interface LichLamViecItem {
  id: number
  nhanVien: string
  caLam: string
  ngayLam: string
  gioBatDau: string
  gioKetThuc: string
  trangThai: 'Đã làm' | 'Dự kiến'
  ghiChu?: string | null
  index?: number
}

/* ===================== STATE ===================== */
const { breadcrumbItems } = useBreadcrumb()
const loading = ref(false)
const search = ref('')
const filter = ref<'all' | 'working' | 'off'>('all')
const filterForm = ref({
  caLam: '',
  ngayLam: '' as any // có thể là string | Date | dayjs
})

const pagination = ref({
  total: 0,
  current: 1,
  pageSize: 10,
  showTotal: true,
  showJumper: true,
  showPageSize: true
})

const danhSach = ref<LichLamViecItem[]>([])

const columns = [
  { title: 'STT', dataIndex: 'index', width: 80, align: 'center' },
  { title: 'Tên nhân viên', dataIndex: 'nhanVien', width: 150 },
  { title: 'Ca làm', dataIndex: 'caLam', width: 120 },
  { title: 'Ngày làm', dataIndex: 'ngayLam', width: 120 },
  { title: 'Giờ bắt đầu', dataIndex: 'gioBatDau', width: 120 },
  { title: 'Giờ kết thúc', dataIndex: 'gioKetThuc', width: 120 },
  { title: 'Trạng thái', dataIndex: 'trangThai', slotName: 'trangThai', width: 120 },
  { title: 'Thao tác', slotName: 'action', width: 120, fixed: 'right' }
]

/* ===================== HELPERS ===================== */
function extractListFromResponse(res: any): any[] {
  if (!res) return []
  if (Array.isArray(res)) return res
  if (Array.isArray(res.data)) return res.data
  if (res.data && Array.isArray(res.data.data)) return res.data.data
  return []
}

function normalizeDateToYYYYMMDD(value: any): string | null {
  if (!value && value !== 0) return null
  if (typeof value === 'string') {
    return value.split('T')[0]
  }
  if (value instanceof Date) {
    if (Number.isNaN(value.getTime())) return null
    return value.toISOString().split('T')[0]
  }
  if (typeof value === 'object' && typeof (value as any).format === 'function') {
    try {
      return (value as any).format('YYYY-MM-DD')
    } catch (e) {
      return null
    }
  }
  try {
    const d = new Date(value)
    if (!Number.isNaN(d.getTime())) return d.toISOString().split('T')[0]
  } catch (e) {
    return null
  }
  return null
}

/* ===================== FETCH API ===================== */
async function fetchLichLamViec() {
  loading.value = true
  try {
    const res = await getLichLamViec()
    // removed console.log for ESLint

    const list = extractListFromResponse(res)

    danhSach.value = list.map((item: any) => {
  const nhanVienName = item?.nhanVien?.tenNhanVien ?? ''
  const caLamName = item?.caLamViec?.tenCa ?? item?.caLam ?? ''
  const ngayLamVal = item?.ngayLamViec ?? ''
  const gioBatDau = item?.caLamViec?.thoiGianBatDau ?? ''
  const gioKetThuc = item?.caLamViec?.thoiGianKetThuc ?? ''

  // Chuyển trạng thái Boolean thành string để hiển thị
  const trangThaiStr = item?.trangThai === true ? 'Đã làm' : 'Dự kiến'

  return {
    id: item?.id,
    nhanVien: nhanVienName,
    caLam: caLamName,
    ngayLam: ngayLamVal,
    gioBatDau,
    gioKetThuc,
    trangThai: trangThaiStr,
    ghiChu: item?.ghiChu ?? null
  } as LichLamViecItem
})


    pagination.value.total = danhSach.value.length
  } catch (error: any) {
    Message.error(error?.message ?? 'Không thể tải lịch làm việc')
  } finally {
    loading.value = false
  }
}
async function toggleTrangThai(record: LichLamViecItem) {
  const newStatus = record.trangThai === 'Đã làm' ? 'Dự kiến' : 'Đã làm'

  // Cập nhật local
  const target = danhSach.value.find((x) => x.id === record.id)
  if (target) target.trangThai = newStatus

  Message.success(`Đã chuyển trạng thái của ${record.nhanVien} sang "${newStatus}"`)

  // Gọi API cập nhật backend
  try {
    await axios.patch(`http://localhost:8080/api/lich-lam-viec/${record.id}/trang-thai`, {
      trangThai: newStatus === 'Đã làm'
    })
  } catch (error) {
    Message.error('Cập nhật trạng thái thất bại')
  }
}


/* ===================== FILTER & PAGINATION ===================== */
const filteredList = computed(() => {
  let list = [...danhSach.value]

 if (filter.value === 'working') {
  list = list.filter((x) => x.trangThai === 'Đã làm')
} else if (filter.value === 'off') {
  list = list.filter((x) => x.trangThai === 'Dự kiến')
}


  if (search.value) {
    const keyword = search.value.toLowerCase()
    list = list.filter(
      (x) =>
        (x.nhanVien || '').toLowerCase().includes(keyword) ||
        (x.caLam || '').toLowerCase().includes(keyword)
    )
  }

  if (filterForm.value.caLam) {
    list = list.filter((x) => x.caLam === filterForm.value.caLam)
  }

  if (filterForm.value.ngayLam) {
    const selectedDate = normalizeDateToYYYYMMDD(filterForm.value.ngayLam)
    if (selectedDate) {
      list = list.filter((x) => {
        const itemDate = normalizeDateToYYYYMMDD(x.ngayLam)
        return itemDate === selectedDate
      })
    }
  }

  pagination.value.total = list.length

  const current = pagination.value.current || 1
  const pageSize = pagination.value.pageSize || 10
  return list.map((item, i) => ({ ...item, index: (current - 1) * pageSize + i + 1 }))
})

const resetFilters = () => {
  search.value = ''
  filter.value = 'all'
  filterForm.value = { caLam: '', ngayLam: '' }
}

/* ===================== ACTIONS ===================== */
function themLichLamViec() {
  // Thay vì Message.info, chuyển hướng sang trang thêm lịch
  router.push({ name: 'themlichlamviec' })
}

function editSchedule(record: LichLamViecItem) {
   router.push({
    name: 'updatelichlamviec',
    params: { id: record.id },
  })
}



/* ===================== LIFECYCLE ===================== */
onMounted(() => {
  fetchLichLamViec()
})
</script>
<style scoped>
.schedule-management-page {
  padding: 0 20px 20px 20px;
}

.filters-card,
.table-card {
  margin-bottom: 16px;
  border-radius: 12px;
}

.actions-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* Table cell padding */
:deep(.arco-table .arco-table-cell) {
  padding: 6px 8px;
}

/* Tag styling để hiển thị trạng thái */
:deep(.success-tag) {
  color: rgb(var(--success-6)) !important;
  background: rgb(var(--success-1)) !important;
  border-color: rgb(var(--success-1)) !important;
}

:deep(.info-tag) {
  color: rgb(var(--primary-6)) !important;
  background: rgb(var(--primary-1)) !important;
  border-color: rgb(var(--primary-1)) !important;
}

/* Button nhỏ gọn trong table */
:deep(.arco-table .arco-btn) {
  padding: 2px 6px;
}

/* Optional: căn chỉnh input, select filter */
:deep(.filters-card .arco-form-item) {
  margin-bottom: 12px;
}
</style>

