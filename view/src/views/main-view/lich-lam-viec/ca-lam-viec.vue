<template>
  <div class="schedule-management-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="filterForm" layout="vertical">
        <a-row :gutter="16">
          <!-- Ô tìm kiếm -->
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="search" placeholder="Tên ca..." allow-clear />
            </a-form-item>
          </a-col>

          <!-- Trạng thái -->
          <a-col :span="8">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="filter" type="button">
                <a-radio value="all">Tất cả</a-radio>
                <a-radio value="active">Hoạt động</a-radio>
                <a-radio value="inactive">Dự Kiến</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>

          <!-- Thời gian bắt đầu -->
          <a-col :span="4">
            <a-form-item label="Thời gian bắt đầu">
              <a-time-picker v-model="filterForm.thoiGianBatDau" format="HH:mm" style="width: 100%" />
            </a-form-item>
          </a-col>

          <!-- Thời gian kết thúc -->
          <a-col :span="4">
            <a-form-item label="Thời gian kết thúc">
              <a-time-picker v-model="filterForm.thoiGianKetThuc" format="HH:mm" style="width: 100%" />
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
          <!-- Gọi openAddForm khi click để điều hướng tới form Thêm ca -->
          <a-button type="primary" @click="openAddForm">
            <template #icon><icon-plus /></template>
            Thêm ca làm việc
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!-- Table -->
    <a-card title="Danh sách ca làm việc" class="table-card">
      <a-table :columns="columns" :data="filteredList" :pagination="pagination" :loading="loading" :scroll="{ x: 1000 }">
        <!-- Cột hiển thị trạng thái -->
        <template #trangThai="{ record }">
          <a-tag :color="record.trangThai === 'Hoạt động' ? 'green' : 'blue'">
            {{ record.trangThai }}
          </a-tag>
        </template>

        <!-- Cột thao tác -->
        <template #action="{ record }">
          <a-space>
            <!-- Nút sửa -->
            <a-button type="outline" size="small" @click="handleEdit(record)">
              <template #icon><icon-edit /></template>
            </a-button>
            

            <!-- Nút tắt/bật trạng thái -->
            <a-switch
              :checked="record.trangThai === 'Hoạt động'"
              checked-color="#52c41a"
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
import { Message } from '@arco-design/web-vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { useRouter } from 'vue-router' // <-- thêm import useRouter
import { IconRefresh } from '@arco-design/web-vue/es/icon'
import { getCaLamViec, updateTrangThaiCa } from '@/api/ca-lam-viec'
import { IconEdit } from '@arco-design/web-vue/es/icon'

const { breadcrumbItems } = useBreadcrumb()
const router = useRouter() // <-- khởi tạo router

// Hàm mở form thêm ca: điều hướng đến route đã khai báo trong router
const openAddForm = () => {
  // Theo routes bạn gửi tên route là 'themcalamviec'
  router.push({ name: 'themcalamviec' })
}

const loading = ref(false)
const search = ref('')
const filter = ref('all')
const filterForm = ref({
  thoiGianBatDau: '',
  thoiGianKetThuc: '',
})

const pagination = ref({
  total: 0,
  current: 1,
  pageSize: 10,
  showTotal: true,
  showJumper: true,
  showPageSize: true,
})

interface CaLamViec {
  id: number
  tenCa: string
  thoiGianBatDau: string
  thoiGianKetThuc: string
  trangThai: string
}

// Danh sách ca làm việc
const danhSach = ref<CaLamViec[]>([])

// Cột trong bảng
const columns = [
  { title: 'STT', dataIndex: 'index', width: 80, align: 'center' },
  { title: 'Tên Ca', dataIndex: 'tenCa', width: 200 },
  { title: 'Bắt Đầu', dataIndex: 'thoiGianBatDau', width: 120 },
  { title: 'Kết Thúc', dataIndex: 'thoiGianKetThuc', width: 120 },
  { title: 'Trạng Thái', dataIndex: 'trangThai', slotName: 'trangThai', width: 140 },
  { title: 'Thao tác', slotName: 'action', width: 120, fixed: 'right' },
]

onMounted(async () => {
  loading.value = true
  try {
    const response = await getCaLamViec()
    console.log('📦 API Response:', response)

    // Nếu API trả về dạng { data: [...] } thì lấy mảng bên trong
    const list = response?.data ?? response ?? []
    console.log('🟢 list từ API:', list)

    // Chuẩn hóa dữ liệu cho Vue
    danhSach.value = list.map((item: any) => ({
      id: item.id,
      maCa: item.maca || '',
      tenCa: item.tenca || item.tenCa || '',
      thoiGianBatDau: item.thoigianbatdau || item.thoiGianBatDau || '',
      thoiGianKetThuc: item.thoigianketthuc || item.thoiGianKetThuc || '',
     trangThaiBool: item.trangThai === true,  // 🔹 đúng property
  trangThai: item.trangThai === true ? 'Hoạt động' : 'Dự Kiến',
    }))

    pagination.value.total = danhSach.value.length
    console.log('✅ Danh sách đã gán:', danhSach.value)
  } catch (error) {
    console.error('❌ Lỗi khi lấy danh sách ca làm việc:', error)
    Message.error('Không thể tải danh sách ca làm việc')
  } finally {
    loading.value = false
  }
})

const toggleTrangThai = async (record: CaLamViec) => {
  const oldStatus = record.trangThai
  const newStatus = record.trangThai === 'Hoạt động' ? 'Dự Kiến' : 'Hoạt động'
  const isActive = newStatus === 'Hoạt động'

  // Cập nhật tạm trên UI
  const target = danhSach.value.find((x) => x.id === record.id)
  if (target) target.trangThai = newStatus
  danhSach.value = [...danhSach.value]

  try {
    // Gọi API cập nhật backend
    const res = await updateTrangThaiCa(record.id, isActive)
    console.log('✅ API cập nhật trạng thái:', res)

    // Nếu backend trả dữ liệu mới, cập nhật lại theo backend
    if (res?.data?.trangthai !== undefined) {
      target.trangThai = res.data.trangthai ? 'Hoạt động' : 'Dự Kiến'
    }

    Message.success(`Đã chuyển trạng thái của ${record.tenCa} sang "${target.trangThai}"`)
  } catch (error) {
    // Rollback lại nếu lỗi
    if (target) target.trangThai = oldStatus
    danhSach.value = [...danhSach.value]
    console.error('❌ Lỗi khi cập nhật trạng thái:', error)
    Message.error('Cập nhật trạng thái thất bại')
  }
}




// ✏️ Hàm sửa ca làm việc
const handleEdit = (record: CaLamViec) => {
 router.push({ name: 'updatecalamviec', params: { id: record.id } })
}

// Tính toán danh sách sau khi lọc
const filteredList = computed(() => {
  let list = danhSach.value

  // Lọc theo trạng thái
  if (filter.value === 'active') {
    list = list.filter((x) => x.trangThai === 'Hoạt động')
  } else if (filter.value === 'inactive') {
    list = list.filter((x) => x.trangThai === 'Dự Kiến')
  }

  // Lọc theo tên
  if (search.value) {
    const keyword = search.value.toLowerCase()
    list = list.filter((x) => x.tenCa.toLowerCase().includes(keyword))
  }

  // Lọc theo thời gian
  if (filterForm.value.thoiGianBatDau) {
    list = list.filter((x) => x.thoiGianBatDau >= filterForm.value.thoiGianBatDau)
  }
  if (filterForm.value.thoiGianKetThuc) {
    list = list.filter((x) => x.thoiGianKetThuc <= filterForm.value.thoiGianKetThuc)
  }

  return list.map((item, index) => ({
    ...item,
    index: (pagination.value.current - 1) * pagination.value.pageSize + index + 1,
  }))
})

// Reset filter
const resetFilters = () => {
  search.value = ''
  filter.value = 'all'
  filterForm.value.thoiGianBatDau = ''
  filterForm.value.thoiGianKetThuc = ''
}
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

:deep(.arco-table .arco-table-cell) {
  padding: 6px 8px;
}
</style>
