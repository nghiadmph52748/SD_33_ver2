<template>
  <div class="customer-management-page">
    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="boLoc" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="boLoc.timKiem" placeholder="Mã, tên, SĐT, email..." allow-clear @change="timKiemKhachHang" />
            </a-form-item>
          </a-col>

          <a-col :span="8">
            <a-form-item label="Giới tính">
              <a-radio-group v-model="boLoc.gioiTinh" type="button" @change="timKiemKhachHang">
                <a-radio value="">Tất cả</a-radio>
                <a-radio value="Nam">Nam</a-radio>
                <a-radio value="Nữ">Nữ</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="boLoc.trangThai" type="button" @change="timKiemKhachHang">
                <a-radio value="">Tất cả</a-radio>
                <a-radio value="active">Hoạt động</a-radio>
                <a-radio value="inactive">Không hoạt động</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>

      <div class="actions-row">
        <a-space>
          <a-button @click="datLaiBoLoc">
            <template #icon>
              <icon-refresh />
            </template>
            Đặt lại
          </a-button>
          <a-button @click="xuatExcel">
            <template #icon>
              <icon-download />
            </template>
            Xuất Excel
          </a-button>
          <a-button type="primary" @click="chuyenTrangTaoMoi">
            <template #icon>
              <icon-plus />
            </template>
            Tạo khách hàng mới
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!-- Customers Table -->
    <a-card title="Danh sách khách hàng" class="table-card">
      <a-table
        :columns="cotBang"
        :data="danhSachKhachHang"
        :pagination="phanTrang"
        :loading="dangTai"
        :scroll="{ x: 1200 }"
        :default-sort-order="{ field: 'tongChiTieu', direction: 'descend' }"
        @change="xuLyThayDoiBang"
      >
        <template #tongChiTieu="{ record }">
          <span>{{ new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(record.tongChiTieu) }}</span>
        </template>

        <template #status="{ record }">
          <a-tag :color="record.trangThai ? 'green' : 'orange'">
            {{ record.trangThai ? 'Hoạt động' : 'Không hoạt động' }}
          </a-tag>
        </template>
        <template #diaChi="{ record }">
          {{ [record.diaChiCuThe, record.phuong, record.quan, record.thanhPho].filter(Boolean).join(', ') }}
        </template>

        <template #action="{ record }">
          <a-space>
            <!-- <a-button type="text" @click="xemChiTietKhach(record)">
              <template #icon>
                <icon-eye />
              </template>
            </a-button> -->
            <a-button type="text" @click="chinhSuaKhach(record)">
              <template #icon>
                <icon-edit />
              </template>
            </a-button>
            <!-- <a-switch
              :model-value="record.trangThai"
              @change="onToggleStatus(record)"
              checked-children="Hoạt động"
              un-checked-children="Không hoạt động"
              size="small"
            /> -->
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
        <div v-if="khachHangToToggleStatus">
          <div>Bạn có chắc chắn muốn {{ khachHangToToggleStatus.trangThai ? 'vô hiệu hóa' : 'kích hoạt' }} khách hàng này?</div>
          <div>
            Tên khách hàng:
            <strong>{{ khachHangToToggleStatus.name }}</strong>
          </div>
          <div>
            Mã khách hàng:
            <strong>{{ khachHangToToggleStatus.code }}</strong>
          </div>
          <div>
            Trạng thái hiện tại:
            <strong>{{ khachHangToToggleStatus.trangThai ? 'Hoạt động' : 'Vô hiệu hóa' }}</strong>
          </div>
        </div>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import * as XLSX from 'xlsx'
import { IconPlus, IconRefresh, IconDownload, IconEdit, IconCheck, IconClose } from '@arco-design/web-vue/es/icon'
import { Message } from '@arco-design/web-vue'
import { layDanhSachKhachHang, capNhatTrangThaiKhachHang, type KhachHangResponse } from '@/api/khach-hang'

// Interface definitions
interface KhachHang {
  id: string | number
  index: number
  code: string
  name: string
  gender: string
  birthday: string
  tongDon: number
  tongChiTieu: number
  thanhPho: string
  phuong: string
  quan: string
  diaChiCuThe: string
  tenDiaChi: string
  email: string
  soDienThoai: string
  trangThai: boolean
  updating?: boolean
}

const router = useRouter()

const chuyenTrangTaoMoi = () => {
  router.push({ name: 'ThemKhachHang' })
}
const chinhSuaKhach = (khach: any) => {
  // console.log('ID cần sửa:', khach.id)
  router.push({ name: 'UpdateKhachHang', params: { id: khach.id } })
}

const boLoc = ref({
  timKiem: '',
  gioiTinh: '',
  phanLoai: '',
  trangThai: '',
})

const dangTai = ref(false)
const phanTrang = ref({
  current: 1,
  pageSize: 10,
  total: 0,
})

const sapXep = ref({
  field: 'tongChiTieu',
  direction: 'descend',
})

// Status toggle modal
const showStatusConfirm = ref(false)
const khachHangToToggleStatus = ref<KhachHang | null>(null)

const cotBang = [
  { title: 'STT', dataIndex: 'index', width: 40, align: 'center' },
  { title: 'Mã', dataIndex: 'code', width: 100, sortable: { sortDirections: ['ascend', 'descend'] } },
  { title: 'Tên', dataIndex: 'name', width: 150, sortable: { sortDirections: ['ascend', 'descend'] } },
  { title: 'Ngày sinh', dataIndex: 'birthday', width: 120, align: 'center', sortable: { sortDirections: ['ascend', 'descend'] } },
  { title: 'Giới tính', dataIndex: 'gender', width: 60, align: 'center', sortable: { sortDirections: ['ascend', 'descend'] } },
  { title: 'Địa chỉ', slotName: 'diaChi', width: 200 },
  { title: 'Email', dataIndex: 'email', width: 150 },
  { title: 'SDT', dataIndex: 'soDienThoai', width: 120, align: 'center' },
  { title: 'Số đơn', dataIndex: 'tongDon', width: 80, align: 'center', sortable: { sortDirections: ['ascend', 'descend'] } },
  {
    title: 'Tổng chi tiêu',
    dataIndex: 'tongChiTieu',
    slotName: 'tongChiTieu',
    width: 120,
    align: 'center',
    sortable: { sortDirections: ['ascend', 'descend'], defaultSortOrder: 'descend' },
  },
  {
    title: 'Trạng thái',
    dataIndex: 'status',
    slotName: 'status',
    width: 120,
    align: 'center',
  },
  { title: 'Thao tác', slotName: 'action', width: 100, fixed: 'right' },
]

const danhSachKhachHang = ref<KhachHang[]>([])

const timKiemKhachHang = async () => {
  try {
    dangTai.value = true
    const res = await layDanhSachKhachHang()
    if (Array.isArray(res.data)) {
      let filtered = res.data

      // Filter theo tìm kiếm
      if (boLoc.value.timKiem.trim() !== '') {
        const search = boLoc.value.timKiem.toLowerCase()
        filtered = filtered.filter(
          (item: KhachHangResponse) =>
            item.maKhachHang?.toLowerCase().includes(search) ||
            item.tenKhachHang?.toLowerCase().includes(search) ||
            item.soDienThoai?.toLowerCase().includes(search) ||
            item.email?.toLowerCase().includes(search)
        )
      }

      // Filter theo giới tính
      if (boLoc.value.gioiTinh !== '') {
        filtered = filtered.filter((item: KhachHangResponse) => (item.gioiTinh ? 'Nam' : 'Nữ') === boLoc.value.gioiTinh)
      }

      // Filter theo trạng thái
      if (boLoc.value.trangThai !== '') {
        filtered = filtered.filter((item: KhachHangResponse) => (item.trangThai ? 'active' : 'inactive') === boLoc.value.trangThai)
      }

      // Map dữ liệu
      let mappedData = filtered.map((item: KhachHangResponse, index: number) => {
        // Tìm địa chỉ mặc định, nếu không có thì lấy địa chỉ đầu tiên
        const diaChi = item.listDiaChi?.find((addr) => addr.macDinh) || item.listDiaChi?.[0] || {}
        return {
          id: item.id,
          index: index + 1,
          code: item.maKhachHang,
          name: item.tenKhachHang,
          gender: item.gioiTinh ? 'Nam' : 'Nữ',
          birthday: item.ngaySinh,
          tongDon: item.tongDon,
          tongChiTieu: item.tongChiTieu,
          thanhPho: diaChi.thanhPho || '',
          phuong: diaChi.phuong || '',
          quan: diaChi.quan || '',
          diaChiCuThe: diaChi.diaChiCuThe || '',
          tenDiaChi: diaChi.tenDiaChi || '',
          email: item.email,
          soDienThoai: item.soDienThoai,
          trangThai: item.trangThai,
          updating: false,
        }
      })

      // Áp dụng sắp xếp
      if (sapXep.value.field) {
        mappedData.sort((a: any, b: any) => {
          const fieldA = a[sapXep.value.field]
          const fieldB = b[sapXep.value.field]

          let comparison = 0
          if (fieldA > fieldB) {
            comparison = 1
          } else if (fieldA < fieldB) {
            comparison = -1
          }

          return sapXep.value.direction === 'descend' ? -comparison : comparison
        })
      }

      // Cập nhật lại index sau khi sort
      mappedData = mappedData.map((item, index) => ({
        ...item,
        index: index + 1,
      }))

      danhSachKhachHang.value = mappedData
      phanTrang.value.total = danhSachKhachHang.value.length
    } else {
      danhSachKhachHang.value = []
      phanTrang.value.total = 0
      // console.error('Dữ liệu trả về không đúng định dạng:', res.data)
    }
  } catch (error: any) {
    // console.error('Lỗi gọi API:', error.response?.data || error.message)
    danhSachKhachHang.value = []
    phanTrang.value.total = 0
  } finally {
    dangTai.value = false
  }
}

const datLaiBoLoc = () => {
  boLoc.value = {
    timKiem: '',
    gioiTinh: '',
    phanLoai: '',
    trangThai: '',
  }
  timKiemKhachHang()
}

const xuLyThayDoiBang = (duLieuPhanTrang: any, duLieuSapXep: any) => {
  // Cập nhật phân trang
  phanTrang.value = {
    ...phanTrang.value,
    current: duLieuPhanTrang.current,
    pageSize: duLieuPhanTrang.pageSize,
  }

  // Cập nhật sắp xếp nếu có
  if (duLieuSapXep && duLieuSapXep.field) {
    sapXep.value = {
      field: duLieuSapXep.field,
      direction: duLieuSapXep.direction,
    }
  }

  timKiemKhachHang()
}
// Show confirm modal for status toggle
const onToggleStatus = (record: KhachHang) => {
  khachHangToToggleStatus.value = record
  showStatusConfirm.value = true
}

// Actual toggle status implementation
const performToggleStatus = async (record: KhachHang) => {
  try {
    // Bật loading
    record.updating = true

    // Gọi API PUT với id trong URL, không gửi body
    await capNhatTrangThaiKhachHang(Number(record.id))

    // Cập nhật trạng thái local (đảo trạng thái)
    record.trangThai = !record.trangThai

    const statusText = record.trangThai ? 'Hoạt động' : 'Vô hiệu hóa'
    Message.success(`Đã cập nhật trạng thái thành: ${statusText}`)

    // Nếu muốn reload lại danh sách đầy đủ:
    // await timKiemKhachHang();
  } catch (error) {
    // console.error('Lỗi cập nhật trạng thái:', error)
    Message.error('Cập nhật trạng thái thất bại')
  } finally {
    // Tắt loading
    record.updating = false
  }
}

const confirmToggleStatus = async () => {
  if (khachHangToToggleStatus.value) {
    await performToggleStatus(khachHangToToggleStatus.value)
  }
  showStatusConfirm.value = false
  khachHangToToggleStatus.value = null
}

const cancelToggleStatus = () => {
  showStatusConfirm.value = false
  khachHangToToggleStatus.value = null
}

// const moModalTaoMoi = () => {}
// const xemChiTietKhach = (khach: any) => {
//   // Điều hướng sang trang detail theo mã khách hàng
//   router.push(`/detailkhachhang/${khach.id}`)
// }
// const xoaKhach = (khach: any) => {}

const xuatExcel = () => {
  // Tạo dữ liệu để xuất
  const dataForExport = danhSachKhachHang.value.map((item: KhachHang) => ({
    'Mã khách hàng': item.code,
    Tên: item.name,
    'Ngày sinh': item.birthday,
    'Giới tính': item.gender,
    'Địa chỉ': [item.diaChiCuThe, item.phuong, item.quan, item.thanhPho].filter(Boolean).join(', '),
    Email: item.email,
    'Số điện thoại': item.soDienThoai,
    'Số đơn hàng': item.tongDon,
    'Tổng chi tiêu': new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(item.tongChiTieu),
    'Trạng thái': item.trangThai ? 'Hoạt động' : 'Không hoạt động',
  }))

  // Tạo một workbook mới
  const ws = XLSX.utils.json_to_sheet(dataForExport)
  const wb = XLSX.utils.book_new()

  // Thêm sheet vào workbook
  XLSX.utils.book_append_sheet(wb, ws, 'KhachHang')

  // Tạo file và tải về
  XLSX.writeFile(wb, 'DanhSachKhachHang.xlsx')
}

onMounted(() => {
  timKiemKhachHang()
})
</script>

<style scoped>
:deep(.arco-table .arco-table-cell) {
  padding: 6px 8px;
}
.customer-management-page {
  padding: 16px 20px;
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

.purchase-icon {
  color: #faad14;
}

.revenue-icon {
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

.stat-change.positive {
  color: #52c41a;
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
  .customer-management-page {
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
