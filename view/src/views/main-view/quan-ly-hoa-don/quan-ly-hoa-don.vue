<template>
  <div class="invoice-page">
    <!-- Status Tabs -->
    <a-card class="tabs-card" :bordered="false">
      <a-tabs v-model:active-key="activeTab" type="line" @change="handleTabChange">
        <a-tab-pane key="all">
          <template #title>
            <span class="tab-title">
              Tất cả
              <a-badge :count="totalInvoices" :number-style="{ backgroundColor: '#165dff' }" />
            </span>
          </template>
        </a-tab-pane>
        <a-tab-pane key="paid">
          <template #title>
            <span class="tab-title">
              Đã thanh toán
              <a-badge :count="paidInvoices" :number-style="{ backgroundColor: '#00b42a' }" />
            </span>
          </template>
        </a-tab-pane>
        <a-tab-pane key="pending">
          <template #title>
            <span class="tab-title">
              Chờ thanh toán
              <a-badge :count="pendingInvoices" :number-style="{ backgroundColor: '#ff7d00' }" />
            </span>
          </template>
        </a-tab-pane>
        <a-tab-pane key="cancelled">
          <template #title>
            <span class="tab-title">
              Đã hủy
              <a-badge :count="cancelledInvoices" :number-style="{ backgroundColor: '#f53f3f' }" />
            </span>
          </template>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form layout="inline" :model="filters">
        <a-form-item label="Tìm kiếm">
          <a-input v-model="filters.search" placeholder="Mã hóa đơn, tên khách hàng..." style="width: 250px" allow-clear />
        </a-form-item>

        <a-form-item label="Trạng thái">
          <a-select v-model="filters.status" placeholder="Chọn trạng thái" style="width: 150px" allow-clear>
            <a-option value="all">Tất cả</a-option>
            <a-option value="paid">Đã thanh toán</a-option>
            <a-option value="pending">Chờ thanh toán</a-option>
            <a-option value="cancelled">Đã hủy</a-option>
          </a-select>
        </a-form-item>

        <a-form-item label="Thời gian">
          <a-space>
            <a-range-picker v-model="filters.dateRange" style="width: 280px" @change="handleDateChange" />
            <a-button @click="filterToday" type="outline" size="small">
              Hôm nay
            </a-button>
            <a-button @click="clearDateFilter" type="text" size="small" v-if="filters.dateRange && filters.dateRange.length > 0">
              Xóa
            </a-button>
          </a-space>
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" @click="searchInvoices">
              <template #icon>
                <icon-search />
              </template>
              Tìm kiếm
            </a-button>
            <a-button @click="resetFilters">
              <template #icon>
                <icon-refresh />
              </template>
              Đặt lại
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- Invoices Table -->
    <a-card title="Danh sách hóa đơn" class="table-card">
      <template #extra>
        <a-space>
          <a-button @click="testAPI" type="primary">
            <template #icon>
              <icon-refresh />
            </template>
            Test API
          </a-button>
          <a-button @click="exportExcel">
            <template #icon>
              <icon-download />
            </template>
            Xuất Excel
          </a-button>
          <a-dropdown>
            <a-button>
              <template #icon>
                <icon-more />
              </template>
              Thao tác
            </a-button>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="cancelSelected">
                  <icon-close />
                  Hủy hóa đơn đã chọn
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </a-space>
      </template>

      <a-table
        :columns="invoiceColumns"
        :data="invoices"
        :pagination="{ ...pagination, total: invoices.length }"
        :loading="loading"
        :scroll="{ x: 1200 }"
        @change="handleTableChange"
      >
        <template #stt="{ record, index }">
          {{ record.id || index + 1 }}
        </template>

        <template #maHoaDon="{ record }">
          {{ record.maHoaDon || `HD${String(record.id).padStart(6, '0')}` }}
        </template>

        <template #status="{ record }">
          <a-tag :color="getStatusColor(record.trangThai ? 'paid' : 'pending')">
            {{ getStatusText(record.trangThai ? 'paid' : 'pending') }}
          </a-tag>
        </template>

        <template #total="{ record }">
          {{ formatCurrency(record.tongTienSauGiam || record.tongTien || 0) }}
        </template>

        <template #ngayTao="{ record }">
          {{ formatDate(record.thoiGianTao || record.ngayTao) }}
        </template>

        <template #payment_method="{ record }">
          <span>{{ record.ngayThanhToan ? 'Đã thanh toán' : 'Chờ thanh toán' }}</span>
        </template>

        <template #action="{ record }">
          <a-space>
            <a-button type="text" @click="viewInvoice(record)">
              <template #icon>
                <icon-eye />
              </template>
              Xem
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- Invoice Detail Modal -->
    <a-modal v-model:open="detailModalVisible" title="Chi tiết hóa đơn" width="800px" :footer="null">
      <div v-if="selectedInvoice" class="invoice-detail">
        <!-- Invoice Header -->
        <div class="invoice-header">
          <div class="invoice-info">
            <h3>Mã hóa đơn: {{ selectedInvoice.id }}</h3>
            <p>Ngày tạo: {{ formatDate(selectedInvoice.ngayTao) }}</p>
            <p>
              Trạng thái:
              <a-tag :color="getStatusColor(selectedInvoice.trangThai ? 'paid' : 'pending')">
                {{ getStatusText(selectedInvoice.trangThai ? 'paid' : 'pending') }}
              </a-tag>
            </p>
          </div>
          <div class="customer-info">
            <h4>Thông tin khách hàng</h4>
            <p>
              <strong>Tên:</strong>
              {{ selectedInvoice.tenKhachHang }}
            </p>
            <p>
              <strong>SĐT:</strong>
              {{ selectedInvoice.soDienThoai }}
            </p>
            <p>
              <strong>Địa chỉ:</strong>
              {{ selectedInvoice.diaChi }}
            </p>
          </div>
        </div>

        <!-- Invoice Items -->
        <div class="invoice-items">
          <h4>Chi tiết sản phẩm</h4>
          <a-table :columns="itemColumns" :data="selectedInvoice.items || []" :pagination="false" size="small">
            <template #subtotal="{ record }">
              {{ formatCurrency(record.thanhTien || record.giaBan * record.soLuong) }}
            </template>
          </a-table>
        </div>

        <!-- Invoice Summary -->
        <div class="invoice-summary">
          <div class="summary-row">
            <span>Tạm tính:</span>
            <span>{{ formatCurrency(selectedInvoice.tongTien || 0) }}</span>
          </div>
          <div class="summary-row">
            <span>Giảm giá:</span>
            <span>-{{ formatCurrency((selectedInvoice.tongTien || 0) - (selectedInvoice.tongTienSauGiam || 0)) }}</span>
          </div>
          <div class="summary-row total">
            <span><strong>Tổng cộng:</strong></span>
            <span>
              <strong>{{ formatCurrency(selectedInvoice.tongTienSauGiam || selectedInvoice.tongTien || 0) }}</strong>
            </span>
          </div>
        </div>

        <!-- Payment Info -->
        <div class="payment-info">
          <h4>Thông tin thanh toán</h4>
          <p>
            <strong>Phương thức:</strong>
            {{ selectedInvoice.ngayThanhToan ? 'Đã thanh toán' : 'Chờ thanh toán' }}
          </p>
          <p>
            <strong>Người tạo:</strong>
            {{ selectedInvoice.tenNhanVien }}
          </p>
          <p v-if="selectedInvoice.ngayThanhToan">
            <strong>Thời gian thanh toán:</strong>
            {{ formatDate(selectedInvoice.ngayThanhToan) }}
          </p>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { fetchHoaDonList, type HoaDonApiModel } from '@/api/hoa-don'
import {
  IconFile,
  IconCheckCircle,
  IconClockCircle,
  IconStar,
  IconSearch,
  IconRefresh,
  IconDownload,
  IconMore,
  IconEye,
  IconEdit,
  IconClose,
} from '@arco-design/web-vue/es/icon'

// Router setup
const router = useRouter()

// Breadcrumb setup

// Data from API
const invoicesList = ref<any[]>([
  {
    id: 1,
    maHoaDon: 'HD000001',
    tenNhanVien: 'Trần Thị Em',
    soDienThoaiKhachHang: '0123456789',
    tenKhachHang: 'Phạm Văn A',
    moTaLoaiDon: 'Bán hàng online',
    thoiGianTao: '2025-09-27 08:30:00',
    tongTienSauGiam: 2250000,
    trangThai: true,
    ngayThanhToan: '2024-01-15',
    hoaDonChiTiets: [{ soLuong: 2 }, { soLuong: 1 }, { soLuong: 3 }],
  },
  {
    id: 2,
    maHoaDon: 'HD000002',
    tenNhanVien: 'Trần Thị Em',
    soDienThoaiKhachHang: '0987654321',
    tenKhachHang: 'Hoàng Thị B',
    moTaLoaiDon: 'Bán hàng tại quầy',
    thoiGianTao: '2025-09-27 14:15:00',
    tongTienSauGiam: 3000000,
    trangThai: true,
    ngayThanhToan: '2024-01-16',
    hoaDonChiTiets: [{ soLuong: 1 }, { soLuong: 2 }],
  },
])
const totalInvoices = ref(0)
const todayInvoices = ref(0)
const paidInvoices = ref(0)
const pendingInvoices = ref(0)
const cancelledInvoices = ref(0)
const totalRevenue = ref(0)

const loading = ref(false)
const selectedRowKeys = ref([])
const detailModalVisible = ref(false)
const selectedInvoice = ref(null)
const activeTab = ref('all')

// Filters
const filters = ref({
  search: '',
  status: 'all',
  dateRange: [] as any[],
  filterToday: false,
})

// Pagination
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 2, // Cập nhật total cho dữ liệu mẫu
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: true,
})

// Table columns
const invoiceColumns = [
  {
    title: 'STT',
    slotName: 'stt',
    width: 80,
    fixed: 'left',
    align: 'center',
  },
  {
    title: 'Mã hóa đơn',
    slotName: 'maHoaDon',
    width: 120,
    fixed: 'left',
  },
  {
    title: 'Tên Nhân viên',
    dataIndex: 'tenNhanVien',
    width: 150,
  },
  {
    title: 'Tên Khách Hàng',
    dataIndex: 'tenKhachHang',
    width: 150,
  },
  {
    title: 'SDT Khách hàng',
    dataIndex: 'soDienThoaiKhachHang',
    width: 130,
  },
  {
    title: 'Loại đơn hàng',
    dataIndex: 'moTaLoaiDon',
    width: 150,
  },
  {
    title: 'Ngày tạo',
    dataIndex: 'thoiGianTao',
    slotName: 'ngayTao',
    width: 150,
  },
  {
    title: 'Tổng tiền',
    dataIndex: 'tongTienSauGiam',
    slotName: 'total',
    width: 120,
    align: 'right',
    sorter: true,
  },
  {
    title: 'Trạng thái đơn hàng',
    dataIndex: 'trangThai',
    slotName: 'status',
    width: 150,
  },
  {
    title: 'Thao tác',
    slotName: 'action',
    width: 100,
    fixed: 'right',
  },
]

const itemColumns = [
  {
    title: 'Sản phẩm',
    dataIndex: 'tenSanPham',
  },
  {
    title: 'Đơn giá',
    dataIndex: 'giaBan',
    align: 'right',
  },
  {
    title: 'Số lượng',
    dataIndex: 'soLuong',
    align: 'center',
  },
  {
    title: 'Thành tiền',
    slotName: 'subtotal',
    align: 'right',
  },
]

// Computed invoices data - filter theo tab và filters
const invoices = computed(() => {
  let filtered = [...invoicesList.value]

  // Filter theo tab (trạng thái)
  if (activeTab.value !== 'all') {
    if (activeTab.value === 'paid') {
      filtered = filtered.filter((invoice) => invoice.trangThai === true || invoice.ngayThanhToan)
    } else if (activeTab.value === 'pending') {
      filtered = filtered.filter((invoice) => invoice.trangThai === false && !invoice.ngayThanhToan)
    } else if (activeTab.value === 'cancelled') {
      // Giả sử có field cancelled hoặc trangThai === false và không có ngayThanhToan sau một thời gian
      filtered = filtered.filter((invoice) => {
        // Logic hủy: có thể là invoice có flag hủy hoặc không có ngayThanhToan sau 7 ngày
        return invoice.trangThai === false && !invoice.ngayThanhToan
      })
    }
  }

  // Filter theo search
  if (filters.value.search) {
    const searchLower = filters.value.search.toLowerCase()
    filtered = filtered.filter((invoice) => {
      const maHoaDon = (invoice.maHoaDon || `HD${String(invoice.id).padStart(6, '0')}`).toLowerCase()
      const tenKhachHang = (invoice.tenKhachHang || '').toLowerCase()
      return maHoaDon.includes(searchLower) || tenKhachHang.includes(searchLower)
    })
  }

  // Filter theo date range
  if (filters.value.dateRange && filters.value.dateRange.length === 2) {
    const startDate = new Date(filters.value.dateRange[0])
    const endDate = new Date(filters.value.dateRange[1])
    endDate.setHours(23, 59, 59, 999) // Set to end of day

    filtered = filtered.filter((invoice) => {
      const invoiceDate = new Date(invoice.thoiGianTao || invoice.ngayTao)
      return invoiceDate >= startDate && invoiceDate <= endDate
    })
  }

  // Filter theo ngày hiện tại nếu được bật
  if (filters.value.filterToday) {
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    const tomorrow = new Date(today)
    tomorrow.setDate(tomorrow.getDate() + 1)

    filtered = filtered.filter((invoice) => {
      const invoiceDate = new Date(invoice.thoiGianTao || invoice.ngayTao)
      return invoiceDate >= today && invoiceDate < tomorrow
    })
  }

  return filtered
})

// Methods
const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const formatDate = (dateString: string | Date) => {
  if (!dateString) return 'N/A'
  const date = typeof dateString === 'string' ? new Date(dateString) : dateString
  return date.toLocaleString('vi-VN')
}

const getStatusColor = (status: string) => {
  switch (status) {
    case 'paid':
      return 'green'
    case 'pending':
      return 'orange'
    case 'cancelled':
      return 'red'
    default:
      return 'blue'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'paid':
      return 'Đã thanh toán'
    case 'pending':
      return 'Chờ thanh toán'
    case 'cancelled':
      return 'Đã hủy'
    default:
      return status
  }
}

// API functions
const calculateStatistics = () => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const tomorrow = new Date(today)
  tomorrow.setDate(tomorrow.getDate() + 1)
  const todayStr = today.toISOString().split('T')[0]

  totalInvoices.value = invoicesList.value.length

  // Đếm hóa đơn hôm nay
  todayInvoices.value = invoicesList.value.filter((invoice) => {
    const dateStr = invoice.thoiGianTao || invoice.ngayTao || invoice.createdAt
    if (!dateStr) return false
    const invoiceDate = new Date(dateStr)
    if (isNaN(invoiceDate.getTime())) return false
    return invoiceDate.toISOString().split('T')[0] === todayStr
  }).length

  // Đếm hóa đơn đã thanh toán
  paidInvoices.value = invoicesList.value.filter((invoice) => invoice.trangThai === true || invoice.ngayThanhToan).length

  // Đếm hóa đơn chờ thanh toán
  pendingInvoices.value = invoicesList.value.filter((invoice) => invoice.trangThai === false && !invoice.ngayThanhToan).length

  // Đếm hóa đơn đã hủy
  cancelledInvoices.value = invoicesList.value.filter((invoice) => {
    // Logic hủy: có thể là invoice có flag hủy hoặc không có ngayThanhToan sau một thời gian
    return invoice.trangThai === false && !invoice.ngayThanhToan
  }).length

  // Tính tổng doanh thu từ tất cả hóa đơn đã thanh toán
  const paidInvoicesAll = invoicesList.value.filter((invoice) => {
    // Chỉ tính hóa đơn đã thanh toán (trangThai = true hoặc có ngayThanhToan)
    return invoice.trangThai === true || invoice.ngayThanhToan
  })

  console.log('Tổng số hóa đơn đã thanh toán:', paidInvoicesAll.length)
  console.log(
    'Chi tiết hóa đơn đã thanh toán:',
    paidInvoicesAll.map((inv) => ({
      id: inv.id,
      trangThai: inv.trangThai,
      ngayThanhToan: inv.ngayThanhToan,
      ngayTao: inv.ngayTao,
      tongTienSauGiam: inv.tongTienSauGiam,
      tongTien: inv.tongTien,
    }))
  )

  totalRevenue.value = paidInvoicesAll.reduce((sum, invoice) => {
    const amount = Number(invoice.tongTienSauGiam || invoice.tongTien || 0)
    console.log(`Hóa đơn ${invoice.id}: ${amount}`)
    return sum + amount
  }, 0)

  console.log('Tổng doanh thu:', totalRevenue.value)
}

const fetchInvoices = async () => {
  try {
    loading.value = true
    console.log('Đang gọi API hóa đơn...')

    // Sử dụng API mới
    const apiData = await fetchHoaDonList()
    console.log('Dữ liệu từ API:', apiData)

    // Map dữ liệu để đảm bảo có đầy đủ các trường cần thiết
    invoicesList.value = apiData.map((invoice: HoaDonApiModel) => ({
      id: invoice.id,
      maHoaDon: invoice.maHoaDon || `HD${String(invoice.id).padStart(6, '0')}`,
      tenNhanVien: invoice.tenNhanVien || 'Chưa xác định',
      soDienThoaiKhachHang: invoice.soDienThoaiKhachHang || 'Chưa có',
      tenKhachHang: invoice.tenKhachHang || 'Khách lẻ',
      moTaLoaiDon: invoice.moTaLoaiDon || (invoice.loaiDon ? 'Bán hàng online' : 'Bán hàng tại quầy'),
      thoiGianTao: invoice.thoiGianTao || invoice.ngayTao || new Date().toISOString(),
      tongTienSauGiam: invoice.tongTienSauGiam || invoice.tongTien || 0,
      trangThai: invoice.trangThai,
      ngayThanhToan: invoice.ngayThanhToan,
      hoaDonChiTiets: [], // Sẽ được load riêng nếu cần
    }))

    console.log('Dữ liệu hóa đơn đã xử lý:', invoicesList.value)
    calculateStatistics()
    // Pagination total sẽ được tính từ computed invoices
    return
  } catch (error) {
    console.error('Lỗi khi gọi API hóa đơn:', error)

    // Thêm dữ liệu mẫu khi API lỗi
    invoicesList.value = [
      {
        id: 1,
        maHoaDon: 'HD000001',
        tenNhanVien: 'Trần Thị Em',
        soDienThoaiKhachHang: '0123456789',
        tenKhachHang: 'Phạm Văn A',
        moTaLoaiDon: 'Bán hàng online',
        thoiGianTao: '2025-09-27 08:30:00',
        tongTienSauGiam: 2250000,
        trangThai: true,
        ngayThanhToan: '2024-01-15',
        hoaDonChiTiets: [{ soLuong: 2 }, { soLuong: 1 }, { soLuong: 3 }],
      },
      {
        id: 2,
        maHoaDon: 'HD000002',
        tenNhanVien: 'Trần Thị Em',
        soDienThoaiKhachHang: '0987654321',
        tenKhachHang: 'Hoàng Thị B',
        moTaLoaiDon: 'Bán hàng tại quầy',
        thoiGianTao: '2025-09-27 14:15:00',
        tongTienSauGiam: 3000000,
        trangThai: true,
        ngayThanhToan: '2024-01-16',
        hoaDonChiTiets: [{ soLuong: 1 }, { soLuong: 2 }],
      },
    ]
    calculateStatistics()
    // Pagination total sẽ được tính từ computed invoices
  } finally {
    loading.value = false
  }
}

const searchInvoices = () => {
  fetchInvoices()
}

const resetFilters = () => {
  filters.value = {
    search: '',
    status: 'all',
    dateRange: [],
    filterToday: false,
  }
  activeTab.value = 'all'
  fetchInvoices()
}

const handleDateChange = () => {
  // Khi thay đổi date range, tắt filter today
  if (filters.value.dateRange && filters.value.dateRange.length > 0) {
    filters.value.filterToday = false
  }
  // Không cần fetch lại vì computed sẽ tự động filter
}

const filterToday = () => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const tomorrow = new Date(today)
  tomorrow.setDate(tomorrow.getDate() + 1)

  filters.value.dateRange = [today, tomorrow]
  filters.value.filterToday = true
}

const clearDateFilter = () => {
  filters.value.dateRange = []
  filters.value.filterToday = false
}

const handleTabChange = (key: string) => {
  activeTab.value = key
  // Cập nhật filter status để đồng bộ với tab
  if (key === 'all') {
    filters.value.status = 'all'
  } else if (key === 'paid') {
    filters.value.status = 'paid'
  } else if (key === 'pending') {
    filters.value.status = 'pending'
  } else if (key === 'cancelled') {
    filters.value.status = 'cancelled'
  }
  // Không cần fetch lại vì computed sẽ tự động filter
}

const handleTableChange = (paginationData: any) => {
  pagination.value.current = paginationData.current
  pagination.value.pageSize = paginationData.pageSize
  fetchInvoices()
}

const onSelectChange = (selectedKeys: any) => {
  selectedRowKeys.value = selectedKeys
}

const viewInvoice = (invoice: any) => {
  console.log('Đang chuyển đến trang chi tiết hóa đơn:', invoice.id)
  // Chuyển đến trang chi tiết hóa đơn mới
  router.push({ name: 'HoaDonChiTiet', params: { id: invoice.id } })
}

const editInvoice = () => {
  // Implement edit logic
}

const cancelInvoice = () => {
  // Implement cancel logic
}

const testAPI = async () => {
  try {
    console.log('Testing API connection...')

    // Test health check
    const healthResponse = await fetch('http://localhost:8080/api/public/health')
    const healthData = await healthResponse.json()
    console.log('Health check:', healthData)

    // Test database connection
    const dbResponse = await fetch('http://localhost:8080/api/test/database')
    const dbData = await dbResponse.json()
    console.log('Database test:', dbData)

    // Test invoice API
    const invoiceResponse = await fetch('http://localhost:8080/api/test/invoices')
    const invoiceData = await invoiceResponse.json()
    console.log('Invoice API:', invoiceData)

    // Test product API
    const productResponse = await fetch('http://localhost:8080/api/test/products')
    const productData = await productResponse.json()
    console.log('Product API:', productData)

    // Test customer API
    const customerResponse = await fetch('http://localhost:8080/api/test/customers')
    const customerData = await customerResponse.json()
    console.log('Customer API:', customerData)

    // Refresh data
    await fetchInvoices()

    alert('API test completed! Check console for details.')
  } catch (error) {
    console.error('API Test Error:', error)
    alert('API test failed! Check console for details.')
  }
}

const exportExcel = () => {
  // Implement export logic
}

const cancelSelected = () => {
  // Implement batch cancel logic
}

// BroadcastChannel for real-time sync with other pages
let orderBroadcastChannel: BroadcastChannel | null = null

// Auto-refresh invoice list interval
let invoiceRefreshInterval: number | null = null

onMounted(() => {
  fetchInvoices()

  // Setup BroadcastChannel for real-time sync when orders are confirmed/deleted
  try {
    orderBroadcastChannel = new BroadcastChannel('order-update-channel')
    orderBroadcastChannel.onmessage = (event) => {
      if (event.data.type === 'ORDER_CONFIRMED' || event.data.type === 'ORDER_DELETED' || event.data.type === 'ORDER_UPDATED') {
        console.log(`[BroadcastChannel] ${event.data.type} received, refreshing invoice list`)
        // Immediately refresh invoice list when order is confirmed/deleted from POS
        fetchInvoices()
      }
    }
  } catch (error) {
    console.warn('BroadcastChannel not supported, falling back to polling', error)
  }

  // Set up auto-refresh for invoice list (every 10 seconds for fallback)
  invoiceRefreshInterval = window.setInterval(() => {
    fetchInvoices()
  }, 10000) // 10 seconds
})

onBeforeUnmount(() => {
  if (invoiceRefreshInterval !== null) {
    clearInterval(invoiceRefreshInterval)
  }

  // Close BroadcastChannel
  if (orderBroadcastChannel) {
    orderBroadcastChannel.close()
    console.log('[BroadcastChannel] Closed')
  }
})
</script>

<style scoped>
.invoice-page {
  padding: 16px;
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

.success-icon {
  color: #52c41a;
}

.warning-icon {
  color: #faad14;
}

.primary-icon {
  color: #1890ff;
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

.tabs-card {
  margin-bottom: 16px;
}

.tab-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filters-card,
.table-card {
  margin-bottom: 16px;
}

.invoice-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.invoice-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.invoice-info h3 {
  margin: 0 0 8px 0;
  color: #1d2129;
}

.invoice-info p {
  margin: 4px 0;
  color: #86909c;
}

.customer-info h4 {
  margin: 0 0 12px 0;
  color: #1d2129;
}

.customer-info p {
  margin: 6px 0;
  color: #4e5969;
}

.invoice-items,
.payment-info {
  margin-bottom: 16px;
}

.invoice-items h4,
.payment-info h4 {
  margin: 0 0 16px 0;
  color: #1d2129;
  font-size: 16px;
}

.invoice-summary {
  background: #fafafa;
  padding: 16px;

  margin-bottom: 16px;
}

:deep(.arco-table .arco-table-cell) {
  padding: 9px 8px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}

.summary-row.total {
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e8e8e8;
}

.danger-item {
  color: #ff4d4f;
}

/* Responsive */
@media (max-width: 768px) {
  .invoice-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .invoice-header {
    flex-direction: column;
    gap: 16px;
  }

  .header-right {
    width: 100%;
    justify-content: center;
  }
}
</style>
