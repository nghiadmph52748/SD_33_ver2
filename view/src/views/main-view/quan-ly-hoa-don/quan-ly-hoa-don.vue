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
            <span>Doanh thu tháng</span>
          </div>
        </template>
        <div class="stat-content">
          <div class="stat-value">{{ formatCurrency(monthlyRevenue) }}</div>
          <div class="stat-change positive">+12.5% so với tháng trước</div>
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
          <a-tag :color="getStatusColor(record.status)">
            {{ getStatusText(record.status) }}
          </a-tag>
        </template>

        <template #total="{ record }">
          {{ formatCurrency(record.total) }}
        </template>

        <template #payment_method="{ record }">
          <span>{{ getPaymentMethodText(record.payment_method) }}</span>
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
            <h3>Mã hóa đơn: {{ selectedInvoice.code }}</h3>
            <p>Ngày tạo: {{ formatDate(selectedInvoice.created_at) }}</p>
            <p>
              Trạng thái:
              <a-tag :color="getStatusColor(selectedInvoice.status)">
                {{ getStatusText(selectedInvoice.status) }}
              </a-tag>
            </p>
          </div>
          <div class="customer-info">
            <h4>Thông tin khách hàng</h4>
            <p>
              <strong>Tên:</strong>
              {{ selectedInvoice.customer_name }}
            </p>
            <p>
              <strong>SĐT:</strong>
              {{ selectedInvoice.customer_phone }}
            </p>
            <p>
              <strong>Địa chỉ:</strong>
              {{ selectedInvoice.customer_address }}
            </p>
          </div>
        </div>

        <!-- Invoice Items -->
        <div class="invoice-items">
          <h4>Chi tiết sản phẩm</h4>
          <a-table :columns="itemColumns" :data="selectedInvoice.items" :pagination="false" size="small">
            <template #subtotal="{ record }">
              {{ formatCurrency(record.price * record.quantity) }}
            </template>
          </a-table>
        </div>

        <!-- Invoice Summary -->
        <div class="invoice-summary">
          <div class="summary-row">
            <span>Tạm tính:</span>
            <span>{{ formatCurrency(selectedInvoice.subtotal) }}</span>
          </div>
          <div class="summary-row">
            <span>Giảm giá:</span>
            <span>-{{ formatCurrency(selectedInvoice.discount) }}</span>
          </div>
          <div class="summary-row total">
            <span><strong>Tổng cộng:</strong></span>
            <span>
              <strong>{{ formatCurrency(selectedInvoice.total) }}</strong>
            </span>
          </div>
        </div>

        <!-- Payment Info -->
        <div class="payment-info">
          <h4>Thông tin thanh toán</h4>
          <p>
            <strong>Phương thức:</strong>
            {{ getPaymentMethodText(selectedInvoice.payment_method) }}
          </p>
          <p>
            <strong>Người tạo:</strong>
            {{ selectedInvoice.created_by }}
          </p>
          <p v-if="selectedInvoice.paid_at">
            <strong>Thời gian thanh toán:</strong>
            {{ formatDate(selectedInvoice.paid_at) }}
          </p>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import {
  IconPlus,
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

// Mock data
const totalInvoices = ref(1247)
const todayInvoices = ref(23)
const paidInvoices = ref(1123)
const pendingInvoices = ref(89)
const monthlyRevenue = ref(245000000) // 245 triệu

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
  total: 1247,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: true,
})

// Table columns
const invoiceColumns = [
  {
    title: 'Mã hóa đơn',
    dataIndex: 'code',
    width: 120,
    fixed: 'left',
  },
  {
    title: 'Khách hàng',
    dataIndex: 'customer_name',
    width: 150,
  },
  {
    title: 'Ngày tạo',
    dataIndex: 'created_at',
    width: 120,
    sorter: true,
  },
  {
    title: 'Trạng thái',
    dataIndex: 'status',
    slotName: 'status',
    width: 120,
  },
  {
    title: 'Tổng tiền',
    dataIndex: 'total',
    slotName: 'total',
    width: 120,
    align: 'right',
    sorter: true,
  },
  {
    title: 'Thanh toán',
    dataIndex: 'payment_method',
    slotName: 'payment_method',
    width: 120,
  },
  {
    title: 'Người tạo',
    dataIndex: 'created_by',
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
    dataIndex: 'name',
  },
  {
    title: 'Đơn giá',
    dataIndex: 'price',
    align: 'right',
  },
  {
    title: 'Số lượng',
    dataIndex: 'quantity',
    align: 'center',
  },
  {
    title: 'Thành tiền',
    slotName: 'subtotal',
    align: 'right',
  },
]

// Mock invoices data
const invoices = ref([
  {
    id: 1,
    code: 'HD001247',
    customer_name: 'Nguyễn Văn A',
    customer_phone: '0987654321',
    customer_address: '123 Đường ABC, Quận 1, TP.HCM',
    created_at: '2024-01-20 14:30:00',
    status: 'paid',
    total: 2500000,
    subtotal: 2500000,
    discount: 0,
    payment_method: 'cash',
    created_by: 'Nguyễn Thị B',
    paid_at: '2024-01-20 14:35:00',
    items: [
      {
        name: 'Giày sneaker Nike Air Max',
        price: 2500000,
        quantity: 1,
      },
    ],
  },
  {
    id: 2,
    code: 'HD001246',
    customer_name: 'Trần Thị C',
    customer_phone: '0978123456',
    customer_address: '456 Đường XYZ, Quận 2, TP.HCM',
    created_at: '2024-01-20 13:15:00',
    status: 'paid',
    total: 3200000,
    subtotal: 3200000,
    discount: 0,
    payment_method: 'card',
    created_by: 'Lê Văn D',
    paid_at: '2024-01-20 13:20:00',
    items: [
      {
        name: 'Giày boot Chelsea',
        price: 3200000,
        quantity: 1,
      },
    ],
  },
  {
    id: 3,
    code: 'HD001245',
    customer_name: 'Phạm Văn E',
    customer_phone: '0967234567',
    customer_address: '789 Đường DEF, Quận 3, TP.HCM',
    created_at: '2024-01-20 11:45:00',
    status: 'pending',
    total: 1800000,
    subtotal: 1800000,
    discount: 0,
    payment_method: 'transfer',
    created_by: 'Hoàng Thị F',
    paid_at: null,
    items: [
      {
        name: 'Giày thể thao Adidas',
        price: 1800000,
        quantity: 1,
      },
    ],
  },
])

// Methods
const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('vi-VN')
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

const getPaymentMethodText = (method: string) => {
  switch (method) {
    case 'cash':
      return 'Tiền mặt'
    case 'card':
      return 'Thẻ tín dụng'
    case 'transfer':
      return 'Chuyển khoản'
    default:
      return method
  }
}

const searchInvoices = () => {
  // Removed console.log
  // Implement search logic
}

const resetFilters = () => {
  filters.value = {
    search: '',
    status: 'all',
    dateRange: [],
  }
}

const handleDateChange = (dates: any) => {
  // Removed console.log
}

const handleTableChange = (paginationData: any, filtersData: any, sorter: any) => {
  // Removed console.log
}

const onSelectChange = (selectedKeys: any) => {
  // Removed console.log
}

const viewInvoice = (invoice: any) => {
  selectedInvoice.value = invoice
  detailModalVisible.value = true
}

const printInvoice = (invoice: any) => {
  // Removed console.log
  // Implement print logic
}

const editInvoice = (invoice: any) => {
  // Removed console.log
  // Implement edit logic
}

const cancelInvoice = (invoice: any) => {
  // Removed console.log
  // Implement cancel logic
}

const showCreateModal = () => {
  // Removed console.log
  // Implement create logic
}

const exportExcel = () => {
  // Removed console.log
  // Implement export logic
}

const printSelected = () => {
  // Removed console.log
  // Implement batch print logic
}

const cancelSelected = () => {
  // Removed console.log
  // Implement batch cancel logic
}

onMounted(() => {
  // Page mounted
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
