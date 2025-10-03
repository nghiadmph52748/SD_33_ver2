<template>
  <div class="customer-management-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

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
            <a-form-item label="Phân loại">
              <a-select v-model="boLoc.phanLoai" placeholder="Chọn phân loại" allow-clear @change="timKiemKhachHang">
                <a-option value="">Tất cả</a-option>
                <a-option value="vip">VIP</a-option>
                <a-option value="regular">Thường xuyên</a-option>
                <a-option value="new">Mới</a-option>
              </a-select>
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
          <a-col :span="24">
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
        @change="xuLyThayDoiBang"
      >
        <template #customer_type="{ record }">
          <a-tag :color="mauPhanLoai(record.customer_type)">
            {{ tenPhanLoai(record.customer_type) }}
          </a-tag>
        </template>

        <template #total_orders="{ record }">
          <span>{{ record.total_orders }}</span>
        </template>

        <template #total_spent="{ record }">
          {{ dinhDangTien(record.total_spent) }}
        </template>

        <template #status="{ record }">
          <a-tag :color="record.status === 'active' ? 'green' : 'orange'">
            {{ record.status === 'active' ? 'Hoạt động' : 'Không hoạt động' }}
          </a-tag>
        </template>

        <template #action="{ record }">
          <a-space>
            <a-button type="text" @click="xemChiTietKhach(record)">
              <template #icon>
                <icon-eye />
              </template>
            </a-button>
            <a-button type="text" @click="chinhSuaKhach(record)">
              <template #icon>
                <icon-edit />
              </template>
            </a-button>
            <a-button type="text" danger @click="xoaKhach(record)">
              <template #icon>
                <icon-delete />
              </template>
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>
  </div>
</template>


<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import axios from 'axios'
import { useRouter } from "vue-router";
import {
  IconPlus,
  IconRefresh,
  IconDownload,
  IconEye,
  IconEdit,
  IconDelete
} from '@arco-design/web-vue/es/icon'
// ✅ Di chuyển lên đầu file <script>
const convertLoai = (text: string) => {
  switch (text?.toLowerCase()) {
    case 'vip':
      return 'vip';
    case 'thường xuyên':
      return 'regular';
    case 'mới':
      return 'new';
    case 'không hoạt động':
      return 'inactive';
    default:
      return 'inactive';
  }
};
const router = useRouter();

const chuyenTrangTaoMoi = () => {
  router.push("/themkhachhang"); // ✅ thêm chữ "h"
};

const { breadcrumbItems } = useBreadcrumb()

const boLoc = ref({
  timKiem: '',
  gioiTinh: '',
  phanLoai: '',
  trangThai: ''
})

const dangTai = ref(false)
const phanTrang = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const cotBang = [
  { title: 'STT', dataIndex: 'index', width: 40, align: 'center' },
  { title: 'Mã', dataIndex: 'code', width: 100 },
  { title: 'Tên', dataIndex: 'name', width: 150 },
  { title: 'Giới tính', dataIndex: 'gender', width: 80, align: 'center' },
  { title: 'Ngày sinh', dataIndex: 'birthday', width: 120, align: 'center' },
  { title: 'Tổng đơn', dataIndex: 'total_orders', slotName: 'total_orders', width: 100, align: 'center' },
  { title: 'Tổng chi tiêu', dataIndex: 'total_spent', slotName: 'total_spent', width: 130, align: 'right' },
  { title: 'Phân loại', dataIndex: 'customer_type', slotName: 'customer_type', width: 120, align: 'center' },
  { title: 'Trạng thái', dataIndex: 'status', slotName: 'status', width: 120, align: 'center' },
  { title: 'Thao tác', slotName: 'action', width: 150, fixed: 'right' }
]
interface KhachHang {
  index: number
  code: string
  name: string
  gender: string
  birthday: string
  total_orders: number
  total_spent: number
  customer_type: string
  status: string
}


const danhSachKhachHang = ref<KhachHang[]>([])

const dinhDangTien = (soTien: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(soTien)
}

const mauPhanLoai = (loai: string) => {
  switch (loai) {
    case 'vip': return 'gold'
    case 'regular': return 'blue'
    case 'new': return 'green'
    case 'inactive': return 'red'
    default: return 'default'
  }
}

const tenPhanLoai = (loai: string) => {
  switch (loai) {
    case 'vip': return 'VIP'
    case 'regular': return 'Thường xuyên'
    case 'new': return 'Mới'
    case 'inactive': return 'Không hoạt động'
    default: return loai
  }
}


const timKiemKhachHang = async () => {
  try {
    dangTai.value = true
    const res = await axios.get('/api/khach-hang-management/playlist')
    console.log('[DEBUG] Response data:', res.data)

    if (Array.isArray(res.data)) {
      danhSachKhachHang.value = res.data.map((item: any, index: number) => ({
        index: index + 1,
        code: item.maKhachHang,
        name: item.tenKhachHang,
        gender: item.gioiTinh ? 'Nam' : 'Nữ',
        birthday: item.ngaySinh,
        total_orders: item.tongDon,
        total_spent: item.tongChiTieu,
        customer_type: convertLoai(item.phanLoaiText),
        status: item.trangThaiText === 'Hoạt động' ? 'active' : 'inactive',
      }))
      phanTrang.value.total = danhSachKhachHang.value.length
    } else {
      danhSachKhachHang.value = []
      phanTrang.value.total = 0
      console.error('Dữ liệu trả về không đúng định dạng:', res.data)
    }
  } catch (error) {
    if (axios.isAxiosError(error)) {
      console.error('Lỗi gọi API:', error.response?.data || error.message)
    } else {
      console.error('Lỗi không xác định:', error)
    }
  } finally {
    dangTai.value = false
  }
}


const datLaiBoLoc = () => {
  boLoc.value = {
    timKiem: '',
    gioiTinh: '',
    phanLoai: '',
    trangThai: ''
  }
  timKiemKhachHang()
}

const xuLyThayDoiBang = (duLieuPhanTrang: any) => {
  phanTrang.value = {
    ...phanTrang.value,
    current: duLieuPhanTrang.current,
    pageSize: duLieuPhanTrang.pageSize
  }
  timKiemKhachHang()
}

const moModalTaoMoi = () => {}
const xemChiTietKhach = (khach: any) => {
  // Điều hướng sang trang detail theo mã khách hàng
  router.push(`/detailkhachhang/${khach.code}`)
}

const chinhSuaKhach = (khach: any) => {
  router.push(`/updatekhachhang/${khach.code}`)  
}

const xoaKhach = (khach: any) => {}
const xuatExcel = () => {}

const layDanhSachKhachHang = async () => {
  const res = await axios.get('/api/khach-hang-management/playlist')
  // Chuyển đổi field thành tên dùng trong bảng
  danhSachKhachHang.value = res.data.data.map((item: any, index: number) => ({
    id: item.id,
    index: index + 1,
    code: item.maKhachHang,
    name: item.tenKhachHang,
    gender: item.gioiTinh ? 'Nam' : 'Nữ',
    birthday: item.ngaySinh,
    total_orders: item.tongDon,
    total_spent: item.tongChiTieu,
    customer_type: convertLoai(item.phanLoaiText),
    status: item.trangThaiText === 'Hoạt động' ? 'active' : 'inactive',
  }))
}


onMounted(() => {
  timKiemKhachHang()
})

</script>


<style scoped>
.customer-management-page {
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
