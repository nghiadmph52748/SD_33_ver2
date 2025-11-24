<template>
  <div class="staff-management-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="boLoc" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="boLoc.timKiem" placeholder="Mã, tên, SĐT, email..." allow-clear @change="timKiemNhanVien" />
            </a-form-item>
          </a-col>

          <a-col :span="8">
            <a-form-item label="Chức vụ">
              <a-select v-model="boLoc.tenQuyenHan" placeholder="Chọn chức vụ" allow-clear @change="timKiemNhanVien">
                <a-option value="">Tất cả</a-option>
                <a-option value="Quản lý">Quản lý</a-option>
                <a-option value="Nhân viên">Nhân viên</a-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="8">
            <a-form-item label="Giới tính">
              <a-radio-group v-model="boLoc.gioiTinh" type="button" @change="timKiemNhanVien">
                <a-radio value="">Tất cả</a-radio>
                <a-radio value="Nam">Nam</a-radio>
                <a-radio value="Nữ">Nữ</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :span="24">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="boLoc.trangThai" type="button" @change="timKiemNhanVien">
                <a-radio value="">Tất cả</a-radio>
                <a-radio value="active">Đang làm việc</a-radio>
                <a-radio value="inactive">Nghỉ việc</a-radio>
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
        :data="danhSachNhanVienPhanTrang"
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
            <a-switch :model-value="record.trangThai" type="round" @click="onToggleStatus(record)" :loading="record.updating">
              <template #checked-icon>
                <icon-check />
              </template>
              <template #unchecked-icon>
                <icon-close />
              </template>
            </a-switch>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- Status Toggle Confirm Modal -->
    <a-modal
      v-model:visible="showStatusConfirm"
      title="Xác nhận thay đổi trạng thái"
      ok-text="Xác nhận"
      cancel-text="Huỷ"
      @ok="confirmToggleStatus"
      @cancel="cancelToggleStatus"
    >
      <template #default>
        <div v-if="nhanVienToToggleStatus">
          <div>Bạn có chắc chắn muốn {{ nhanVienToToggleStatus.trangThai ? 'nghỉ việc' : 'đang làm việc' }} nhân viên này?</div>
          <div>
            Tên nhân viên:
            <strong>{{ nhanVienToToggleStatus.tenNhanVien }}</strong>
          </div>
          <div>
            Mã nhân viên:
            <strong>{{ nhanVienToToggleStatus.maNhanVien }}</strong>
          </div>
          <div>
            Trạng thái hiện tại:
            <strong>{{ nhanVienToToggleStatus.trangThai ? 'Đang làm việc' : 'Nghỉ việc' }}</strong>
          </div>
        </div>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { Message } from '@arco-design/web-vue'
import * as XLSX from 'xlsx'
import { layDanhSachNhanVien, capNhatNhanVien } from '@/api/nhan-vien'
import { IconPlus, IconRefresh, IconDownload, IconEdit, IconCheck, IconClose } from '@arco-design/web-vue/es/icon'
import { useRouter } from 'vue-router'

// Reactive data - định nghĩa trước khi sử dụng
const loading = ref(false)
const danhSachNhanVien = ref<any[]>([])
const phanTrang = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
  showJumper: true,
  showPageSize: true,
})

// Modal confirm state
const showStatusConfirm = ref(false)
const nhanVienToToggleStatus = ref<any>(null)

// Show confirm modal for status toggle
const onToggleStatus = (record: any) => {
  nhanVienToToggleStatus.value = record
  showStatusConfirm.value = true
}

// Actual toggle status implementation
const performToggleStatus = async (record: any) => {
  try {
    // Bật loading
    record.updating = true
    // Tạo FormData như backend expect @RequestParam
    const formData = new FormData()
    formData.append('tenNhanVien', record.tenNhanVien || '')
    formData.append('email', record.email || '')
    formData.append('soDienThoai', record.soDienThoai || '')
    formData.append('ngaySinh', record.ngaySinh || '')
    formData.append('cccd', record.cccd || '')
    formData.append('gioiTinh', String(record.gioiTinh || false))
    formData.append('thanhPho', record.thanhPho || '')
    formData.append('quan', record.quan || '')
    formData.append('phuong', record.phuong || '')
    formData.append('diaChiCuThe', record.diaChiCuThe || '')
    formData.append('idQuyenHan', String(record.idQuyenHan || 2))
    formData.append('tenTaiKhoan', record.tenTaiKhoan || '')
    formData.append('matKhau', record.matKhau || '')
    formData.append('trangThai', String(!record.trangThai))

    // Nếu có ảnh thì thêm file
    if (record.anhNhanVien && record.anhNhanVien instanceof File) {
      formData.append('file', record.anhNhanVien)
    }
    // Gọi API PUT với id trong URL
    await capNhatNhanVien(record.id, formData)

    // Cập nhật trạng thái local (đảo trạng thái)
    record.trangThai = !record.trangThai

    const statusText = record.trangThai ? 'Đang làm việc' : 'Nghỉ việc'
    Message.success(`Đã cập nhật trạng thái thành: ${statusText}`)

    // Nếu muốn reload lại danh sách đầy đủ:
    // await loadNhanVienData();
  } catch (_) {
    // Error handled by Message.error below
    Message.error('Cập nhật trạng thái thất bại')
  } finally {
    // Tắt loading
    record.updating = false
  }
}

// Confirm status toggle
const confirmToggleStatus = async () => {
  if (nhanVienToToggleStatus.value) {
    await performToggleStatus(nhanVienToToggleStatus.value)
  }
  showStatusConfirm.value = false
  nhanVienToToggleStatus.value = null
}

// Cancel status toggle
const cancelToggleStatus = () => {
  showStatusConfirm.value = false
  nhanVienToToggleStatus.value = null
}

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()
// Modal and for
const router = useRouter()

// Phân trang - phanTrang đã được định nghĩa ở trên

// Form data
const navigateToAddStaff = () => {
  router.push({ name: 'ThemNhanVien' }) // Điều hướng tới trang thêm nhân viên
}

const goToEdit = (record: any) => {
  if (!record?.id) {
    // console.error('❌ record không có id:', record)
    return
  }
  router.push({ name: 'Updatenhanvien', params: { id: record.id } })
}

const handleTableChange = (paginationData: any) => {
  // Cập nhật phân trang
  phanTrang.value = {
    ...phanTrang.value,
    current: paginationData.current,
    pageSize: paginationData.pageSize,
  }
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
const boLoc = ref({
  timKiem: '', // Tìm kiếm theo tên, email, sđt...
  gioiTinh: '', // Nam / Nữ
  tenQuyenHan: '', // Nhân viên / Quản lý
  trangThai: '', // Hoạt động / Ngưng
})

// Mock data - nhanVien đã được định nghĩa ở trên

const timKiemNhanVien = async () => {
  try {
    loading.value = true
    const res = await layDanhSachNhanVien()
    if (Array.isArray(res.data)) {
      let filtered = res.data

      // Filter theo tìm kiếm
      if (boLoc.value.timKiem.trim() !== '') {
        const search = boLoc.value.timKiem.toLowerCase()
        filtered = filtered.filter(
          (item: any) =>
            item.maNhanVien?.toLowerCase().includes(search) ||
            item.tenNhanVien?.toLowerCase().includes(search) ||
            item.soDienThoai?.toLowerCase().includes(search) ||
            item.email?.toLowerCase().includes(search)
        )
      }

      // Filter theo giới tính
      if (boLoc.value.gioiTinh !== '') {
        filtered = filtered.filter((item: any) => {
          let gioiTinhText = null
          if (item.gioiTinh === null || item.gioiTinh === undefined) {
            gioiTinhText = null
          } else if (item.gioiTinh) {
            gioiTinhText = 'Nam'
          } else {
            gioiTinhText = 'Nữ'
          }
          return gioiTinhText === boLoc.value.gioiTinh
        })
      }
      // Filter theo quyền hạn
      if (boLoc.value.tenQuyenHan !== '') {
        filtered = filtered.filter((item: any) => item.tenQuyenHan === boLoc.value.tenQuyenHan)
      }

      // Filter theo trạng thái
      if (boLoc.value.trangThai !== '') {
        filtered = filtered.filter((item: any) => (item.trangThai ? 'active' : 'inactive') === boLoc.value.trangThai)
      }

      // Map dữ liệu
      const mappedData = filtered.map((item: any, index: number) => {
        const tenQuyenHanMap = item.idQuyenHan === 1 ? 'Quản lý' : 'Nhân viên'
        return {
          id: item.id,
          stt: index + 1,
          maNhanVien: item.maNhanVien,
          tenNhanVien: item.tenNhanVien,
          tenTaiKhoan: item.tenTaiKhoan,
          email: item.email,
          soDienThoai: item.soDienThoai,
          ngaySinh: item.ngaySinh,
          gioiTinh: item.gioiTinh === null || item.gioiTinh === undefined ? null : Boolean(item.gioiTinh),
          thanhPho: item.thanhPho,
          quan: item.quan,
          phuong: item.phuong,
          diaChi: item.diaChiCuThe,
          tenQuyenHan: tenQuyenHanMap,
          idQuyenHan: item.idQuyenHan,
          trangThai: Boolean(item.trangThai),
          anhNhanVien: item.anhNhanVien ? `/uploads/${item.anhNhanVien}` : null,
          updating: false,
        }
      })
      mappedData.sort((a, b) => b.id - a.id)

      // ✅ Đánh lại STT sau khi sắp xếp
      mappedData.forEach((item, index) => {
        item.stt = index + 1
      })
      danhSachNhanVien.value = mappedData
      phanTrang.total = mappedData.length
    }
    // } catch (_) {
    //   Message.error('Không thể tải dữ liệu nhân viên')
  } finally {
    loading.value = false
  }
}

// Computed danh sách nhân viên với phân trang
const danhSachNhanVienPhanTrang = computed(() => {
  const startIndex = (phanTrang.current - 1) * phanTrang.pageSize
  const endIndex = startIndex + phanTrang.pageSize
  return danhSachNhanVien.value.slice(startIndex, endIndex)
})

// Table - loading đã được định nghĩa ở trên
const columns = [
  { title: 'STT', dataIndex: 'stt', width: 50, align: 'center' },
  { title: 'Ảnh', dataIndex: 'anhNhanVien', width: 80, align: 'center', slotName: 'anhNhanVien' },
  { title: 'Mã nhân viên', dataIndex: 'maNhanVien', width: 120 },
  { title: 'Tên nhân viên', dataIndex: 'tenNhanVien', width: 140 },
  { title: 'Email', dataIndex: 'email', width: 150 },
  { title: 'Số điện thoại', dataIndex: 'soDienThoai', width: 100 },
  { title: 'Địa chỉ', slotName: 'diaChi', width: 200 },
  { title: 'Ngày sinh', dataIndex: 'ngaySinh', width: 100, align: 'center' },
  { title: 'Giới tính', dataIndex: 'gioiTinh', slotName: 'gioiTinh', width: 60, align: 'center' },
  { title: 'Chức vụ', dataIndex: 'tenQuyenHan', slotName: 'tenQuyenHan', width: 85, align: 'center' },
  { title: 'Trạng thái', dataIndex: 'trangThai', slotName: 'trangThai', width: 120, align: 'center' },
  { title: 'Thao tác', slotName: 'action', width: 100, fixed: 'right' },
]

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

const resetFilters = () => {
  boLoc.value = {
    timKiem: '', // tìm kiếm theo mã, tên, email, sđt
    gioiTinh: '', // lọc theo giới tính
    tenQuyenHan: '', // lọc theo quyền hạn
    trangThai: '', // lọc theo trạng thái
  }
  timKiemNhanVien()

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

const exportExcel = () => {
  const ws = XLSX.utils.json_to_sheet(danhSachNhanVien.value)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, 'Danh sách nhân viên')
  XLSX.writeFile(wb, 'danhsachnhanvien.xlsx')
}
onMounted(async () => {
  await timKiemNhanVien()
})
</script>

<style scoped>
:deep(.arco-table .arco-table-cell) {
  padding: 6px 8px;
}
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
