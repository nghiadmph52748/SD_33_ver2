<template>
  <div class="invoice-page">
    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form layout="inline" :model="filters">
        <a-form-item :label="t('invoice.common.search')">
          <a-input v-model="filters.search" :placeholder="t('invoice.common.searchPlaceholder')" style="width: 250px" allow-clear />
        </a-form-item>

        <a-form-item :label="t('invoice.common.status')">
          <a-select v-model="filters.status" :placeholder="t('invoice.common.selectStatus')" style="width: 150px" allow-clear>
            <a-option value="all">{{ t('invoice.common.all') }}</a-option>
            <a-option value="paid">{{ t('invoice.common.paid') }}</a-option>
            <a-option value="pending">{{ t('invoice.common.pending') }}</a-option>
            <a-option value="waiting_confirmation">{{ t('invoice.common.waitingConfirmation') }}</a-option>
            <a-option value="waiting_delivery">{{ t('invoice.common.waitingDelivery') }}</a-option>
            <a-option value="shipping">{{ t('invoice.common.shipping') }}</a-option>
            <a-option value="cancelled">{{ t('invoice.common.cancelled') }}</a-option>
          </a-select>
        </a-form-item>

        <a-form-item :label="t('invoice.common.time')">
          <a-space>
            <a-range-picker v-model="filters.dateRange" style="width: 280px" @change="handleDateChange" />
            <a-button @click="filterToday" type="outline" size="small">
              {{ t('invoice.common.today') }}
            </a-button>
            <a-button @click="clearDateFilter" type="text" size="small" v-if="filters.dateRange && filters.dateRange.length > 0">
              {{ t('invoice.common.clear') }}
            </a-button>
          </a-space>
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" @click="searchInvoices">
              <template #icon>
                <icon-search />
              </template>
              {{ t('invoice.common.search') }}
            </a-button>
            <a-button @click="resetFilters">
              <template #icon>
                <icon-refresh />
              </template>
              {{ t('invoice.common.reset') }}
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- Invoices Table -->
    <a-card title="Danh sách hóa đơn" class="table-card">
      <!-- Status Tabs -->
      <div class="tabs-container">
        <div class="tabs-wrapper">
          <a-tabs v-model:active-key="activeTab" type="line" @change="handleTabChange">
            <a-tab-pane key="all">
              <template #title>
                <span class="tab-title">
                  {{ t('invoice.status.all') }}
                  <a-badge :count="totalInvoices" :number-style="{ backgroundColor: '#165dff' }" />
                </span>
              </template>
            </a-tab-pane>
            <a-tab-pane key="waiting_confirmation">
              <template #title>
                <span class="tab-title">
                  {{ t('invoice.status.waitingConfirmation') }}
                  <a-badge :count="waitingConfirmationInvoices" :number-style="{ backgroundColor: '#ff7d00' }" />
                </span>
              </template>
            </a-tab-pane>
            <a-tab-pane key="confirmed">
              <template #title>
                <span class="tab-title">
                  {{ t('invoice.status.confirmed') }}
                  <a-badge :count="waitingDeliveryInvoices" :number-style="{ backgroundColor: '#ff7d00' }" />
                </span>
              </template>
            </a-tab-pane>
            <a-tab-pane key="shipping">
              <template #title>
                <span class="tab-title">
                  {{ t('invoice.status.shipping') }}
                  <a-badge :count="shippingInvoices" :number-style="{ backgroundColor: '#165dff' }" />
                </span>
              </template>
            </a-tab-pane>
            <a-tab-pane key="paid">
              <template #title>
                <span class="tab-title">
                  {{ t('invoice.status.paid') }}
                  <a-badge :count="paidInvoices" :number-style="{ backgroundColor: '#00b42a' }" />
                </span>
              </template>
            </a-tab-pane>
            <a-tab-pane key="cancelled">
              <template #title>
                <span class="tab-title">
                  {{ t('invoice.status.cancelled') }}
                  <a-badge :count="cancelledInvoices" :number-style="{ backgroundColor: '#f53f3f' }" />
                </span>
              </template>
            </a-tab-pane>
            <a-tab-pane key="surcharge">
              <template #title>
                <span class="tab-title">
                  Phụ phí
                  <a-badge :count="surchargeInvoices" :number-style="{ backgroundColor: '#722ed1' }" />
                </span>
              </template>
            </a-tab-pane>
            <a-tab-pane key="refund">
              <template #title>
                <span class="tab-title">
                  Hoàn phí
                  <a-badge :count="refundInvoices" :number-style="{ backgroundColor: '#13c2c2' }" />
                </span>
              </template>
            </a-tab-pane>
          </a-tabs>
          <div class="tabs-actions">
            <a-button @click="exportExcel" type="primary">
              <template #icon>
                <icon-download />
              </template>
              {{ t('invoice.common.export') }}
            </a-button>
          </div>
        </div>
      </div>

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
          <a-tag :color="getStatusColorByName(record.trangThaiDonHang)">{{ record.trangThaiDonHang }}</a-tag>
        </template>

        <template #total="{ record }">
          {{ formatCurrency(calculateFinalTotal(record)) }}
        </template>

        <template #ngayTao="{ record }">
          {{ formatDateOnly(record.thoiGianTao || record.ngayTao) }}
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
            <p>Ngày tạo: {{ formatDateOnly(selectedInvoice.ngayTao) }}</p>
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
          <div v-if="selectedInvoice.phiVanChuyen && selectedInvoice.phiVanChuyen > 0" class="summary-row">
            <span>Phí vận chuyển:</span>
            <span>{{ formatCurrency(selectedInvoice.phiVanChuyen) }}</span>
          </div>
          <div v-if="selectedInvoice.phuPhi && selectedInvoice.phuPhi > 0" class="summary-row">
            <span>Phụ phí:</span>
            <span>+{{ formatCurrency(selectedInvoice.phuPhi) }}</span>
          </div>
          <div v-if="selectedInvoice.hoanPhi && selectedInvoice.hoanPhi > 0" class="summary-row">
            <span>Hoàn phí:</span>
            <span>-{{ formatCurrency(selectedInvoice.hoanPhi) }}</span>
          </div>
          <div class="summary-row total">
            <span><strong>Tổng cộng:</strong></span>
            <span>
              <strong>{{ formatCurrency(calculateFinalTotal(selectedInvoice)) }}</strong>
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
import { useI18n } from 'vue-i18n'
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
  IconEye,
  IconEdit,
} from '@arco-design/web-vue/es/icon'

// Router setup
const router = useRouter()
const { t } = useI18n()

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
const waitingConfirmationInvoices = ref(0)
const waitingDeliveryInvoices = ref(0)
const shippingInvoices = ref(0)
const surchargeInvoices = ref(0)
const refundInvoices = ref(0)
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
  hasSurcharge: false,
  hasRefund: false,
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
    align: 'left',
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
    width: 100,
    sortable: {
      sortDirections: ['ascend', 'descend'],
    },
    sorter: (a: any, b: any) => {
      const dateA = new Date(a.thoiGianTao || a.ngayTao || 0).getTime()
      const dateB = new Date(b.thoiGianTao || b.ngayTao || 0).getTime()
      if (isNaN(dateA) && isNaN(dateB)) {
        return (b.id || 0) - (a.id || 0)
      }
      if (isNaN(dateA)) return 1
      if (isNaN(dateB)) return -1
      return dateB - dateA // Giảm dần: mới nhất trước
    },
  },
  {
    title: 'Phụ phí',
    dataIndex: 'phuPhi',
    width: 100,
    align: 'right',
  },
  {
    title: 'Hoàn phí',
    dataIndex: 'hoanPhi',
    width: 100,
    align: 'right',
  },
  {
    title: 'Tổng tiền',
    dataIndex: 'tongTienSauGiam',
    slotName: 'total',
    width: 120,
    align: 'left',
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
      // Đã thanh toán: ngayThanhToan có giá trị
      filtered = filtered.filter((invoice) => invoice.ngayThanhToan)
    } else if (activeTab.value === 'waiting_confirmation') {
      // Chờ xác nhận
      filtered = filtered.filter((invoice) => invoice.trangThaiDonHang === 'Chờ xác nhận')
    } else if (activeTab.value === 'confirmed') {
      // Đã xác nhận
      filtered = filtered.filter((invoice) => invoice.trangThaiDonHang === 'Đã xác nhận')
    } else if (activeTab.value === 'shipping') {
      // Đang giao: bao gồm "Đang giao hàng"
      filtered = filtered.filter((invoice) => invoice.trangThaiDonHang === 'Đang giao hàng')
    } else if (activeTab.value === 'cancelled') {
      // Đã hủy
      filtered = filtered.filter((invoice) => invoice.trangThaiDonHang === 'Đã huỷ')
    } else if (activeTab.value === 'surcharge') {
      // Có phụ phí > 0
      filtered = filtered.filter((invoice) => invoice.phuPhi && invoice.phuPhi > 0)
    } else if (activeTab.value === 'refund') {
      // Có hoàn phí > 0
      filtered = filtered.filter((invoice) => invoice.hoanPhi && invoice.hoanPhi > 0)
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

  // Filter theo phụ phí > 0
  if (filters.value.hasSurcharge) {
    filtered = filtered.filter((invoice) => invoice.phuPhi && invoice.phuPhi > 0)
  }

  // Filter theo hoàn phí > 0
  if (filters.value.hasRefund) {
    filtered = filtered.filter((invoice) => invoice.hoanPhi && invoice.hoanPhi > 0)
  }

  // Sort theo ngày tạo giảm dần (mới nhất trước) - luôn áp dụng sort này
  filtered.sort((a, b) => {
    // Ưu tiên thoiGianTao, sau đó ngayTao, sau đó createAt
    const dateStrA = a.thoiGianTao || a.ngayTao || a.createAt || ''
    const dateStrB = b.thoiGianTao || b.ngayTao || b.createAt || ''

    // Nếu cả hai đều không có ngày, sort theo ID giảm dần (mới nhất trước)
    if (!dateStrA && !dateStrB) {
      return (b.id || 0) - (a.id || 0)
    }

    // Nếu một trong hai không có ngày, đưa xuống dưới
    if (!dateStrA) return 1
    if (!dateStrB) return -1

    // Parse dates
    const dateA = new Date(dateStrA).getTime()
    const dateB = new Date(dateStrB).getTime()

    // Nếu parse lỗi, fallback về ID
    if (isNaN(dateA) && isNaN(dateB)) {
      return (b.id || 0) - (a.id || 0)
    }
    if (isNaN(dateA)) return 1
    if (isNaN(dateB)) return -1

    // Sort giảm dần: mới nhất trước
    const diff = dateB - dateA
    if (diff !== 0) return diff

    // Nếu cùng ngày, sort theo ID giảm dần (mới nhất trước)
    return (b.id || 0) - (a.id || 0)
  })

  return filtered
})

// Methods
const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const formatDate = (dateString: string | Date | null | undefined) => {
  if (!dateString) return 'N/A'
  const date = typeof dateString === 'string' ? new Date(dateString) : dateString
  if (isNaN(date.getTime())) return 'N/A' // Invalid date
  return date.toLocaleString('vi-VN')
}

const formatDateOnly = (dateString: string | Date | null | undefined) => {
  if (!dateString) return 'N/A'
  const date = typeof dateString === 'string' ? new Date(dateString) : dateString
  if (isNaN(date.getTime())) return 'N/A' // Invalid date
  return date.toLocaleDateString('vi-VN')
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

// Map tenTrangThaiDonHang to color
const getStatusColorByName = (statusName: string): string => {
  switch (statusName) {
    case 'Đã huỷ':
      return 'red'
    case 'Hoàn thành':
      return 'green'
    case 'Đã giao hàng':
      return 'green'
    case 'Đang giao hàng':
      return 'blue'
    case 'Đang xử lý':
      return 'orange'
    case 'Đã xác nhận':
      return 'orange'
    case 'Chờ xác nhận':
      return 'orange'
    default:
      return 'blue'
  }
}

// API functions
const calculateStatistics = () => {
  totalInvoices.value = invoicesList.value.length

  // Đếm hóa đơn đã thanh toán
  paidInvoices.value = invoicesList.value.filter((invoice) => invoice.ngayThanhToan).length

  // Đếm hóa đơn chờ xác nhận
  waitingConfirmationInvoices.value = invoicesList.value.filter((invoice) => invoice.trangThaiDonHang === 'Chờ xác nhận').length

  // Đếm hóa đơn đã xác nhận
  waitingDeliveryInvoices.value = invoicesList.value.filter((invoice) => invoice.trangThaiDonHang === 'Đã xác nhận').length

  // Đếm hóa đơn đang giao
  shippingInvoices.value = invoicesList.value.filter((invoice) => invoice.trangThaiDonHang === 'Đang giao hàng').length

  // Đếm hóa đơn đã hủy
  cancelledInvoices.value = invoicesList.value.filter((invoice) => invoice.trangThaiDonHang === 'Đã huỷ').length

  // Đếm hóa đơn có phụ phí > 0
  surchargeInvoices.value = invoicesList.value.filter((invoice) => invoice.phuPhi && invoice.phuPhi > 0).length

  // Đếm hóa đơn có hoàn phí > 0
  refundInvoices.value = invoicesList.value.filter((invoice) => invoice.hoanPhi && invoice.hoanPhi > 0).length

  // Tính tổng doanh thu từ tất cả hóa đơn đã thanh toán
  totalRevenue.value = invoicesList.value
    .filter((invoice) => invoice.ngayThanhToan)
    .reduce((sum, invoice) => {
      const amount = Number(invoice.tongTienSauGiam || invoice.tongTien || 0)
      return sum + amount
    }, 0)
}

// Helper function to get highest priority status from thongTinDonHangs
const getHighestPriorityStatus = (thongTinDonHangs: any[]): string => {
  if (!thongTinDonHangs || thongTinDonHangs.length === 0) {
    return 'Chờ xác nhận'
  }

  // Priority order: Đã huỷ > Hoàn thành > Đã giao hàng > Đang giao hàng > Đang xử lý > Đã xác nhận > Chờ xác nhận
  const priorityMap: Record<string, number> = {
    'Đã huỷ': 7,
    'Hoàn thành': 6,
    'Đã giao hàng': 5,
    'Đang giao hàng': 4,
    'Đang xử lý': 3,
    'Đã xác nhận': 2,
    'Chờ xác nhận': 1,
  }

  let highestStatus = 'Chờ xác nhận'
  let highestPriority = 0

  thongTinDonHangs.forEach((item: any) => {
    const status = item.tenTrangThaiDonHang || 'Chờ xác nhận'
    const priority = priorityMap[status] || 0
    if (priority > highestPriority) {
      highestPriority = priority
      highestStatus = status
    }
  })

  return highestStatus
}

const fetchInvoices = async () => {
  try {
    loading.value = true

    // Sử dụng API mới
    const apiData = await fetchHoaDonList()

    // Chỉ hiển thị những hóa đơn đã hoàn tất (không còn trong trạng thái tạo nháp ở POS)
    const confirmedInvoices = apiData.filter((invoice: HoaDonApiModel) => invoice.ghiChu !== 'Tạo hóa đơn bán hàng tại quầy')

    // Map dữ liệu để đảm bảo có đầy đủ các trường cần thiết
    invoicesList.value = confirmedInvoices.map((invoice: HoaDonApiModel) => {
      // Calculate tongTien from items if not provided
      let calculatedTongTien = invoice.tongTienSauGiam || invoice.tongTien || 0
      if (calculatedTongTien === 0 && invoice.items && invoice.items.length > 0) {
        calculatedTongTien = invoice.items.reduce((sum: number, item: any) => {
          return sum + (item.thanhTien || (item.giaBan || 0) * (item.soLuong || 0))
        }, 0)
      }
      if (calculatedTongTien === 0 && invoice.chiTietSanPham && invoice.chiTietSanPham.length > 0) {
        calculatedTongTien = invoice.chiTietSanPham.reduce((sum: number, item: any) => {
          return sum + (item.thanhTien || (item.giaBan || 0) * (item.soLuong || 0))
        }, 0)
      }

      // Format ngayTao - use createAt if ngayTao is not available
      // Don't fallback to current time - use null if not available
      let ngayTaoValue: string | null = null
      if (invoice.ngayTao) {
        ngayTaoValue = invoice.ngayTao
      } else if (invoice.createAt) {
        ngayTaoValue = invoice.createAt
      }

      // Don't modify the date format - use it as-is from server
      // Server returns the correct format with time included

      return {
        id: invoice.id,
        maHoaDon: invoice.maHoaDon || `HD${String(invoice.id || 0).padStart(6, '0')}`,
        tenNhanVien: invoice.tenNhanVien || 'Chưa xác định',
        soDienThoaiKhachHang: invoice.soDienThoaiKhachHang || invoice.soDienThoai || invoice.soDienThoaiNguoiNhan || 'Chưa có',
        tenKhachHang: invoice.tenKhachHang || invoice.tenNguoiNhan || 'Khách lẻ',
        moTaLoaiDon: invoice.moTaLoaiDon || (invoice.loaiDon ? 'Bán hàng online' : 'Bán hàng tại quầy'),
        thoiGianTao: ngayTaoValue,
        ngayTao: ngayTaoValue,
        // Use tongTienSauGiam from API if available, otherwise use calculated value
        tongTienSauGiam: invoice.tongTienSauGiam && invoice.tongTienSauGiam > 0 ? invoice.tongTienSauGiam : calculatedTongTien,
        tongTien: invoice.tongTien && invoice.tongTien > 0 ? invoice.tongTien : calculatedTongTien,
        phiVanChuyen: invoice.phiVanChuyen || 0,
        phuPhi: invoice.phuPhi || 0,
        hoanPhi: invoice.hoanPhi || 0,
        trangThai: invoice.trangThai,
        ngayThanhToan: invoice.ngayThanhToan,
        hoaDonChiTiets: invoice.items || invoice.chiTietSanPham || [],
        trangThaiGiaoHang: invoice.trangThaiGiaoHang, // For filtering
        // Get highest priority status from thongTinDonHangs
        trangThaiDonHang: getHighestPriorityStatus(invoice.thongTinDonHangs),
      }
    })
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
    hasSurcharge: false,
    hasRefund: false,
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
  } else if (key === 'waiting_confirmation') {
    filters.value.status = 'waiting_confirmation'
  } else if (key === 'confirmed') {
    filters.value.status = 'confirmed'
  } else if (key === 'shipping') {
    filters.value.status = 'shipping'
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
  // Chuyển đến trang chi tiết hóa đơn mới
  router.push({ name: 'HoaDonChiTiet', params: { id: invoice.id } })
}

const editInvoice = () => {
  // Implement edit logic
}

const cancelInvoice = () => {
  // Implement cancel logic
}

const exportExcel = () => {
  // Implement export logic
}

const calculateFinalTotal = (invoice: any) => {
  if (!invoice) return 0

  // Tổng tiền = tongTienSauGiam + phiVanChuyen + phuPhi - hoanPhi
  let total = invoice.tongTienSauGiam || invoice.tongTien || 0

  // Add shipping fee if available
  if (invoice.phiVanChuyen && invoice.phiVanChuyen > 0) {
    total += invoice.phiVanChuyen
  }

  // Add surcharge if available
  if (invoice.phuPhi && invoice.phuPhi > 0) {
    total += invoice.phuPhi
  }

  // Subtract refund if available
  if (invoice.hoanPhi && invoice.hoanPhi > 0) {
    total -= invoice.hoanPhi
  }

  console.log('calculateFinalTotal:', {
    id: invoice.id,
    tongTienSauGiam: invoice.tongTienSauGiam,
    phiVanChuyen: invoice.phiVanChuyen,
    phuPhi: invoice.phuPhi,
    hoanPhi: invoice.hoanPhi,
    total,
  })

  return Math.max(0, total)
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

.tabs-container {
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.tabs-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.tabs-wrapper :deep(.arco-tabs) {
  flex: 1;
}

.tabs-actions {
  display: flex;
  align-items: center;
  gap: 8px;
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
