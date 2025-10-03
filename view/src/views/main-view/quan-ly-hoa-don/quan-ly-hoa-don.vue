<template>
  <div class="invoice-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Statistics Cards -->
    <div class="stats-grid">
      <a-card class="stat-card" :bordered="false">
        <template #title>
          <div class="stat-header">
            <icon-file class="stat-icon" />
            <span>Tổng hóa đơn</span>
          </div>
        </template>
        <div class="stat-content">
          <div class="stat-value">{{ totalInvoices }}</div>
          <div class="stat-change">Hôm nay: +{{ todayInvoices }}</div>
        </div>
      </a-card>

      <a-card class="stat-card" :bordered="false">
        <template #title>
          <div class="stat-header">
            <icon-check-circle class="stat-icon success-icon" />
            <span>Đã thanh toán</span>
          </div>
        </template>
        <div class="stat-content">
          <div class="stat-value">{{ paidInvoices }}</div>
          <div class="stat-change">{{ Math.round((paidInvoices / totalInvoices) * 100) }}% tổng số</div>
        </div>
      </a-card>

      <a-card class="stat-card" :bordered="false">
        <template #title>
          <div class="stat-header">
            <icon-clock-circle class="stat-icon warning-icon" />
            <span>Chờ thanh toán</span>
          </div>
        </template>
        <div class="stat-content">
          <div class="stat-value">{{ pendingInvoices }}</div>
          <div class="stat-change">Cần xử lý</div>
        </div>
      </a-card>

      <a-card class="stat-card" :bordered="false">
        <template #title>
          <div class="stat-header">
            <icon-star class="stat-icon primary-icon" />
            <span>Tổng doanh thu</span>
          </div>
        </template>
        <div class="stat-content">
          <div class="stat-value">{{ formatCurrency(totalRevenue) }}</div>
          <div class="stat-change positive">Tất cả hóa đơn đã thanh toán</div>
        </div>
      </a-card>
    </div>

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
          <a-range-picker v-model="filters.dateRange" style="width: 280px" @change="handleDateChange" />
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
                <a-menu-item @click="printSelected">
                  <icon-printer />
                  In hóa đơn đã chọn
                </a-menu-item>
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
        :pagination="pagination"
        :loading="loading"
        :row-selection="{ selectedRowKeys, onChange: onSelectChange }"
        :scroll="{ x: 1200 }"
        @change="handleTableChange"
      >
        <template #status="{ record }">
          <a-tag :color="getStatusColor(record.trangThai ? 'paid' : 'pending')">
            {{ getStatusText(record.trangThai ? 'paid' : 'pending') }}
          </a-tag>
        </template>

        <template #total="{ record }">
          {{ formatCurrency(record.tongTienSauGiam || record.tongTien || 0) }}
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
            <a-button type="text" @click="printInvoice(record)">
              <template #icon>
                <icon-printer />
              </template>
              In
            </a-button>
            <a-dropdown>
              <a-button type="text">
                <template #icon>
                  <icon-more />
                </template>
              </a-button>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="editInvoice(record)">
                    <icon-edit />
                    Chỉnh sửa
                  </a-menu-item>
                  <a-menu-item v-if="record.status === 'pending'" @click="cancelInvoice(record)" class="danger-item">
                    <icon-close />
                    Hủy đơn
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
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
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
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
  IconPrinter,
  IconEdit,
  IconClose,
} from '@arco-design/web-vue/es/icon'

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()

// Data from API
const invoicesList = ref<any[]>([])
const totalInvoices = ref(0)
const todayInvoices = ref(0)
const paidInvoices = ref(0)
const pendingInvoices = ref(0)
const totalRevenue = ref(0)

const loading = ref(false)
const selectedRowKeys = ref([])
const detailModalVisible = ref(false)
const selectedInvoice = ref(null)

// Filters
const filters = ref({
  search: '',
  status: 'all',
  dateRange: [],
})

// Pagination
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: true,
})

// Table columns
const invoiceColumns = [
  {
    title: 'Mã hóa đơn',
    dataIndex: 'id',
    width: 120,
    fixed: 'left',
  },
  {
    title: 'Khách hàng',
    dataIndex: 'tenKhachHang',
    width: 150,
  },
  {
    title: 'Ngày tạo',
    dataIndex: 'ngayTao',
    width: 120,
    sorter: true,
  },
  {
    title: 'Trạng thái',
    dataIndex: 'trangThai',
    slotName: 'status',
    width: 120,
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
    title: 'Thanh toán',
    dataIndex: 'ngayThanhToan',
    slotName: 'payment_method',
    width: 120,
  },
  {
    title: 'Người tạo',
    dataIndex: 'tenNhanVien',
    width: 120,
  },
  {
    title: '',
    slotName: 'action',
    width: 180,
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

// Computed invoices data
const invoices = computed(() => invoicesList.value)

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
  const todayStr = today.toISOString().split('T')[0]
  const currentMonth = today.getMonth() + 1
  const currentYear = today.getFullYear()

  totalInvoices.value = invoicesList.value.length

  // Đếm hóa đơn hôm nay
  todayInvoices.value = invoicesList.value.filter((invoice) => {
    const invoiceDate = new Date(invoice.ngayTao || invoice.createdAt)
    return invoiceDate.toISOString().split('T')[0] === todayStr
  }).length

  // Đếm hóa đơn đã thanh toán
  paidInvoices.value = invoicesList.value.filter((invoice) => invoice.trangThai === true || invoice.ngayThanhToan).length

  // Đếm hóa đơn chờ thanh toán
  pendingInvoices.value = invoicesList.value.filter((invoice) => invoice.trangThai === false && !invoice.ngayThanhToan).length

  // Tính tổng doanh thu từ tất cả hóa đơn đã thanh toán
  const paidInvoicesAll = invoicesList.value.filter((invoice) => {
    // Chỉ tính hóa đơn đã thanh toán (trangThai = true hoặc có ngayThanhToan)
    return invoice.trangThai === true || invoice.ngayThanhToan
  })
  
  console.log('Tổng số hóa đơn đã thanh toán:', paidInvoicesAll.length)
  console.log('Chi tiết hóa đơn đã thanh toán:', paidInvoicesAll.map(inv => ({
    id: inv.id,
    trangThai: inv.trangThai,
    ngayThanhToan: inv.ngayThanhToan,
    ngayTao: inv.ngayTao,
    tongTienSauGiam: inv.tongTienSauGiam,
    tongTien: inv.tongTien
  })))
  
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
    console.log('Đang gọi API: /api/hoa-don-management/playlist')
    const response = await axios.get('/api/hoa-don-management/playlist')
    console.log('Response từ backend:', response)
    
    if (response.success) {
      invoicesList.value = response.data || []
      console.log('Dữ liệu hóa đơn:', invoicesList.value)
      calculateStatistics()
      pagination.value.total = invoicesList.value.length
    } else {
      console.error('API trả về lỗi:', response.message)
    }
  } catch (error) {
    console.error('Lỗi khi gọi API hóa đơn:', error)
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
  }
  fetchInvoices()
}

const handleDateChange = () => {
  fetchInvoices()
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
  selectedInvoice.value = invoice
  detailModalVisible.value = true
}

const printInvoice = () => {
  // Implement print logic
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

const printSelected = () => {
  // Implement batch print logic
}

const cancelSelected = () => {
  // Implement batch cancel logic
}

onMounted(() => {
  fetchInvoices()
})
</script>

<style scoped>
.invoice-page {
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
