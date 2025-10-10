<template>
  <div class="staff-management-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="filters" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="filters.timKiem" placeholder="Mã, tên, email, SĐT..." allow-clear @change="searchStaff" />
            </a-form-item>
          </a-col>

          <a-col :span="8">
            <a-form-item label="Chức vụ">
              <a-select v-model="filters.tenQuyenHan" placeholder="Chọn chức vụ" allow-clear @change="searchStaff">
                <a-option value="">Tất cả</a-option>
                <a-option value="manager">Quản lý</a-option>
                <a-option value="staff">Nhân viên</a-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="8">
            <a-form-item label="Giới tính">
              <a-radio-group v-model="filters.gioiTinh" type="button" @change="searchStaff">
                <a-radio value="">Tất cả</a-radio>
                <a-radio :value="true">Nam</a-radio>
                <a-radio :value="false">Nữ</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :span="24">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="filters.trangThai" type="button" @change="searchStaff">
                <a-radio value="">Tất cả</a-radio>
                <a-radio :value="true">Đang làm việc</a-radio>
                <a-radio :value="false">Nghỉ việc</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>

      <div class="actions-row">
        <a-space>
          <a-button @click="resetFilters">
            <template #icon>
              <icon-refresh />
            </template>
            Đặt lại
          </a-button>
          <a-button @click="exportExcel">
            <template #icon>
              <icon-download />
            </template>
            Xuất Excel
          </a-button>
          <a-button type="primary" @click="navigateToAddStaff">
            <template #icon>
              <icon-plus />
            </template>
            Thêm nhân viên
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!-- Staff Table -->
    <a-card title="Danh sách nhân viên" class="table-card">
      <a-table
        :columns="columns"
        :data="nhanVienCoSTT"
        :pagination="phanTrang"
        :loading="loading"
        :scroll="{ x: 1200 }"
        @change="handleTableChange"
      >
        <template #position="{ record }">
          <a-tag :color="getPositionColor(record.position)">
            {{ getPositionText(record.position) }}
          </a-tag>
        </template>
        <template #anhNhanVien="{ record }">
          <img
            :src="record.anhNhanVien || '/images/default-avatar.png'"
            alt="Ảnh nhân viên"
            style="width: 40px; height: 40px; border-radius: 50%; object-fit: cover"
          />
        </template>
        <template #diaChi="{ record }">
          {{ [record.diaChi, record.phuong, record.quan, record.thanhPho].filter(Boolean).join(', ') }}
        </template>
        <template #salary="{ record }">
          {{ formatCurrency(record.salary) }}
        </template>
        <template #gioiTinh="{ record }">
          <span>
            {{ record.gioiTinh === null || record.gioiTinh === undefined ? 'Chưa xác định' : record.gioiTinh ? 'Nam' : 'Nữ' }}
          </span>
        </template>

        <template #trangThai="{ record }">
          <a-tag :color="record.trangThai ? 'green' : 'red'">
            {{ record.trangThai ? 'Đang làm việc' : 'Nghỉ việc' }}
          </a-tag>
        </template>

        <template #action="{ record }">
          <a-space>
            <a-button type="text" @click="goToEdit(record)">
              <template #icon>
                <icon-edit />
              </template>
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { Modal, Message } from '@arco-design/web-vue'
import axios from 'axios'
import {
  IconPlus,
  IconSearch,
  IconRefresh,
  IconDownload,
  IconEye,
  IconEdit,
  IconDelete,
  IconLock,
  IconUserGroup,
  IconUser,
  IconStar,
} from '@arco-design/web-vue/es/icon'
import { useRouter } from 'vue-router'
// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()
// Modal and for
const router = useRouter()
// Form data
const navigateToAddStaff = () => {
  router.push('/themnhanvien') // Điều hướng tới trang thêm nhân viên
}
const viewDetail = (record: any) => {
  if (!record?.id) {
    console.error('❌ record không có id:', record)
    return
  }
  router.push(`/detail/${record.id}`)
}

const goToEdit = (record: any) => {
  if (!record?.id) {
    console.error('❌ record không có id:', record)
    return
  }
  router.push(`/updatenhanvien/${record.id}`)
}

const handleTableChange = (paginationData: any, filtersData: any, sorter: any) => {
  // Removed console.log
}

// Form dữ liệu nhân viên
const formData = reactive({
  maNhanVien: '',
  tenNhanVien: '',
  tenTaiKhoan: '',
  matKhau: '',
  ngaySinh: null,
  cccd: '',
  email: '',
  soDienThoai: '',
  thanhPho: '',
  quan: '',
  phuong: '',
  gioiTinh: true,
  diaChiCuThe: '',
  idQuyenHan: null,
  tenQuyenHan: '',
  anhNhanVien: null,
  trangThai: true,
})

// Bộ lọc tìm kiếm
const filters = ref({
  timKiem: '', // Tìm kiếm theo tên, email, sđt...
  gioiTinh: '', // Nam / Nữ
  tenQuyenHan: '', // Nhân viên / Quản lý
  trangThai: '', // Hoạt động / Ngưng
})

// Mock data
const nhanVien = ref<any[]>([]) // staff bây giờ là mảng rỗng, chờ load từ API

// Computed staff with filtering and index for STT

// Danh sách nhân viên kèm STT và áp dụng bộ lọc
const nhanVienCoSTT = computed(() => {
  let danhSachLoc = nhanVien.value

  // Lọc theo từ khóa tìm kiếm (mã NV, tên, email, số điện thoại)
  if (filters.value.timKiem) {
    const tuKhoa = filters.value.timKiem.toLowerCase()
    danhSachLoc = danhSachLoc.filter(
      (nv) =>
        nv.maNhanVien.toLowerCase().includes(tuKhoa) ||
        nv.tenNhanVien.toLowerCase().includes(tuKhoa) ||
        nv.email.toLowerCase().includes(tuKhoa) ||
        nv.soDienThoai.toLowerCase().includes(tuKhoa)
    )
  }

  // Lọc theo quyền hạn
  if (filters.value.tenQuyenHan && filters.value.tenQuyenHan !== '') {
    danhSachLoc = danhSachLoc.filter((nv) => nv.tenQuyenHan === filters.value.tenQuyenHan)
  }

  // Lọc theo giới tính
  // Lọc theo giới tính
  if (filters.value.gioiTinh !== '') {
    // filters.gioiTinh là true/false (hoặc '' nếu Tất cả)
    danhSachLoc = danhSachLoc.filter((nv) => nv.gioiTinh === filters.value.gioiTinh)
  }

  // Lọc theo trạng thái
  if (filters.value.trangThai !== '') {
    // filters.trangThai là true/false (hoặc '' nếu Tất cả)
    danhSachLoc = danhSachLoc.filter((nv) => nv.trangThai === filters.value.trangThai)
  }

  // Thêm chỉ số STT
  return danhSachLoc.map((nv, index) => ({
    ...nv,
    stt: index + 1,
  }))
})

// Table
const loading = ref(false)
const columns = [
  { title: 'STT', dataIndex: 'stt', width: 50, align: 'center' },
  { title: 'Ảnh', dataIndex: 'anhNhanVien', width: 80, align: 'center', slotName: 'anhNhanVien' },
  { title: 'Mã nhân viên', dataIndex: 'maNhanVien', width: 120 },
  { title: 'Tên nhân viên', dataIndex: 'tenNhanVien', width: 180 },
  { title: 'Email', dataIndex: 'email', width: 200 },
  { title: 'Số điện thoại', dataIndex: 'soDienThoai', width: 150 },
  { title: 'Địa chỉ', slotName: 'diaChi', width: 250 },
  { title: 'Ngày sinh', dataIndex: 'ngaySinh', width: 120, align: 'center' },
  { title: 'Giới tính', dataIndex: 'gioiTinh', slotName: 'gioiTinh', width: 100, align: 'center' },
  { title: 'Chức vụ', dataIndex: 'tenQuyenHan', slotName: 'tenQuyenHan', width: 120, align: 'center' },
  { title: 'Trạng thái', dataIndex: 'trangThai', slotName: 'trangThai', width: 120, align: 'center' },
  { title: 'Thao tác', slotName: 'action', width: 150, fixed: 'right' },
]

// Pagination
// Phân trang
const phanTrang = computed(() => ({
  current: 1, // Trang hiện tại
  pageSize: 10, // Số bản ghi mỗi trang
  total: nhanVien.value.length, // Tổng số nhân viên
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `Tổng cộng ${total} nhân viên`, // ✅ ép kiểu number
}))

// Methods
const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const getPositionColor = (position: string) => {
  switch (position) {
    case 'manager':
      return 'gold'
    case 'staff':
      return 'blue'
    case 'intern':
      return 'green'
    default:
      return 'default'
  }
}

const getPositionText = (position: string) => {
  switch (position) {
    case 'manager':
      return 'Quản lý'
    case 'staff':
      return 'Nhân viên'
    case 'intern':
      return 'Thực tập'
    default:
      return position
  }
}

const searchStaff = () => {
  // Filtering is handled by computed property staffWithIndex
  // This method is called when filters change (@change event)
}
const resetFilters = () => {
  filters.value = {
    timKiem: '', // tìm kiếm theo mã, tên, email, sđt
    tenQuyenHan: '', // lọc theo quyền hạn
    gioiTinh: '', // lọc theo giới tính
    trangThai: '', // lọc theo trạng thái
  }

  Object.assign(formData, {
    maNhanVien: '',
    tenNhanVien: '',
    email: '',
    soDienThoai: '',
    ngaySinh: null,
    gioiTinh: null, // null = chưa chọn, true = Nam, false = Nữ
    tenQuyenHan: '',
    trangThai: true, // true = Hoạt động, false = Ngưng
  })
}
const deleteStaff = (staff: any) => {
  Modal.warning({
    title: 'Xác nhận xoá nhân viên',
    content: `Bạn chắc chắn muốn xoá "${staff.tenNhanVien}"?`,
    okText: 'Xoá',
    cancelText: 'Hủy',
    onOk: () =>
      axios
        .delete(`/api/nhan-vien-management/nhan-vien/${staff.id}`)
        .then(() => {
          Message.success('Đã xoá nhân viên thành công.')
          nhanVien.value = nhanVien.value.filter((nv) => nv.id !== staff.id)
        })
        .catch((error) => {
          console.error('❌ Lỗi khi xoá nhân viên:', error)
          Message.error('Lỗi khi xoá nhân viên.')
        }),
  })
}

const exportExcel = () => {
  // Removed console.log
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/nhan-vien-management/playlist')
    console.log('📌 Response từ backend:', res.data)

    // Nếu backend trả về { data: [...] }
    const list = res.data.data || res.data || [] // 👈 fallback an toàn

    nhanVien.value = list.map((nv: any, index: number) => ({
      id: nv.id,
      stt: index + 1,
      maNhanVien: nv.maNhanVien,
      tenNhanVien: nv.tenNhanVien,
      tenTaiKhoan: nv.tenTaiKhoan,
      email: nv.email,
      soDienThoai: nv.soDienThoai,
      ngaySinh: nv.ngaySinh,
      gioiTinh: nv.gioiTinh === null || nv.gioiTinh === undefined ? null : Boolean(nv.gioiTinh),
      thanhPho: nv.thanhPho,
      quan: nv.quan,
      phuong: nv.phuong,
      diaChi: nv.diaChiCuThe,
      tenQuyenHan: nv.tenQuyenHan,
      trangThai: Boolean(nv.trangThai),
      anhNhanVien: nv.anhNhanVien ? `/uploads/${nv.anhNhanVien}` : null,
    }))
  } catch (error) {
    console.error('❌ Lỗi load nhân viên:', error)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.staff-management-page {
  padding: 0 20px 20px 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.stat-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  font-size: 20px;
  color: #1890ff;
}

.active-icon {
  color: #52c41a;
}

.manager-icon {
  color: #faad14;
}

.salary-icon {
  color: #722ed1;
}

.stat-content {
  margin-top: 12px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1d2129;
  margin-bottom: 4px;
}

.stat-change {
  font-size: 12px;
  color: #86909c;
}

.filters-card,
.table-card {
  margin-bottom: 16px;
}

.actions-row {
  display: flex;
  justify-content: flex-end;
}

/* Responsive */
@media (max-width: 768px) {
  .staff-management-page {
    padding: 16px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .header-right {
    width: 100%;
    justify-content: center;
  }
}
</style>
