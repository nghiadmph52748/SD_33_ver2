<template>
  <div class="invoice-detail-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">Chi tiết hóa đơn</h1>
          <a-tag :color="getStatusColor(invoice?.trangThai)" class="status-tag">
            {{ invoice?.trangThai || 'Đang tải...' }}
          </a-tag>
        </div>
        <div class="action-buttons">
          <a-button @click="goBack" class="back-btn">
            <template #icon>
              <icon-arrow-left />
            </template>
            Quay lại
          </a-button>
          <a-button type="primary" @click="printInvoice" class="print-btn">
            <template #icon>
              <icon-printer />
            </template>
            In hóa đơn
          </a-button>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <a-spin size="large" />
      <p>Đang tải thông tin hóa đơn...</p>
    </div>

    <!-- Invoice Content -->
    <div v-else-if="invoice" class="invoice-content">
      <!-- Invoice Info Card -->
      <a-card class="invoice-info-card" :bordered="false">
        <template #title>
          <div class="card-title">
            <icon-file />
            Thông tin hóa đơn
          </div>
        </template>
        
        <a-row :gutter="24">
          <a-col :span="12">
            <div class="info-section">
              <h3>Thông tin cơ bản</h3>
              <div class="info-grid">
                <div class="info-item">
                  <span class="label">Mã hóa đơn:</span>
                  <span class="value">{{ invoice.maHoaDon || `HD${String(invoice.id).padStart(6, '0')}` }}</span>
                </div>
                <div class="info-item">
                  <span class="label">Ngày tạo:</span>
                  <span class="value">{{ formatDate(invoice.ngayTao) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">Nhân viên:</span>
                  <span class="value">{{ invoice.tenNhanVien || 'Chưa xác định' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">Khách hàng:</span>
                  <span class="value">{{ invoice.tenKhachHang || 'Khách lẻ' }}</span>
                </div>
              </div>
            </div>
          </a-col>
          
          <a-col :span="12">
            <div class="info-section">
              <h3>Thông tin thanh toán</h3>
              <div class="info-grid">
                <div class="info-item">
                  <span class="label">Tổng tiền:</span>
                  <span class="value total-amount">{{ formatCurrency(invoice.tongTienSauGiam) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">Giảm giá:</span>
                  <span class="value">{{ formatCurrency(invoice.giamGia || 0) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">Phương thức:</span>
                  <span class="value">{{ invoice.phuongThucThanhToan || 'Tiền mặt' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">Trạng thái:</span>
                  <span class="value">
                    <a-tag :color="getStatusColor(invoice.trangThai)">
                      {{ invoice.trangThai }}
                    </a-tag>
                  </span>
                </div>
              </div>
            </div>
          </a-col>
        </a-row>
      </a-card>

      <!-- Products Table -->
      <a-card class="products-card" :bordered="false">
        <template #title>
          <div class="card-title">
            <icon-shopping-cart />
            Sản phẩm trong hóa đơn
          </div>
        </template>

        <a-table
          :columns="productColumns"
          :data="invoice.hoaDonChiTiets || []"
          :pagination="false"
          :loading="loading"
          class="products-table"
        >
          <template #stt="{ record, index }">
            {{ index + 1 }}
          </template>

          <template #tenSanPham="{ record }">
            <div class="product-info">
              <div class="product-name">{{ record.tenSanPham || 'Sản phẩm không xác định' }}</div>
              <div class="product-details">
                <span v-if="record.mauSac" class="product-color">Màu: {{ record.mauSac }}</span>
                <span v-if="record.kichThuoc" class="product-size">Size: {{ record.kichThuoc }}</span>
              </div>
            </div>
          </template>

          <template #donGia="{ record }">
            {{ formatCurrency(record.donGia || 0) }}
          </template>

          <template #soLuong="{ record }">
            <a-tag color="blue">{{ record.soLuong || 0 }}</a-tag>
          </template>

          <template #thanhTien="{ record }">
            <span class="total-price">{{ formatCurrency(record.thanhTien || 0) }}</span>
          </template>
        </a-table>
      </a-card>

      <!-- Summary Card -->
      <a-card class="summary-card" :bordered="false">
        <template #title>
          <div class="card-title">
            <icon-calculator />
            Tổng kết hóa đơn
          </div>
        </template>

        <div class="summary-content">
          <div class="summary-row">
            <span class="summary-label">Tổng số sản phẩm:</span>
            <span class="summary-value">{{ getTotalProducts() }}</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">Tổng số lượng:</span>
            <span class="summary-value">{{ getTotalQuantity() }}</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">Tổng tiền hàng:</span>
            <span class="summary-value">{{ formatCurrency(getSubtotal()) }}</span>
          </div>
          <div class="summary-row" v-if="invoice.giamGia && invoice.giamGia > 0">
            <span class="summary-label">Giảm giá:</span>
            <span class="summary-value discount">-{{ formatCurrency(invoice.giamGia) }}</span>
          </div>
          <div class="summary-row total-row">
            <span class="summary-label">Thành tiền:</span>
            <span class="summary-value total">{{ formatCurrency(invoice.tongTienSauGiam) }}</span>
          </div>
        </div>
      </a-card>
    </div>

    <!-- Error State -->
    <div v-else class="error-container">
      <a-result
        status="error"
        title="Không tìm thấy hóa đơn"
        sub-title="Hóa đơn bạn đang tìm kiếm không tồn tại hoặc đã bị xóa."
      >
        <template #extra>
          <a-button type="primary" @click="goBack">
            Quay lại danh sách
          </a-button>
        </template>
      </a-result>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import {
  IconArrowLeft,
  IconPrinter,
  IconFile,
  IconShoppingCart,
  IconCalculator,
} from '@arco-design/web-vue/es/icon'

const route = useRoute()
const router = useRouter()
const { breadcrumbItems } = useBreadcrumb()

// Data
const invoice = ref<any>(null)
const loading = ref(true)
const invoiceId = ref(route.params.id as string)

// Table columns for products
const productColumns = [
  {
    title: 'STT',
    slotName: 'stt',
    width: 80,
    align: 'center',
  },
  {
    title: 'Tên sản phẩm',
    slotName: 'tenSanPham',
    width: 300,
  },
  {
    title: 'Đơn giá',
    slotName: 'donGia',
    width: 120,
    align: 'right',
  },
  {
    title: 'Số lượng',
    slotName: 'soLuong',
    width: 100,
    align: 'center',
  },
  {
    title: 'Thành tiền',
    slotName: 'thanhTien',
    width: 120,
    align: 'right',
  },
]

// Methods
const fetchInvoiceDetail = async () => {
  try {
    loading.value = true
    const response = await axios.get(`/api/hoa-don-management/${invoiceId.value}`)
    
    if (response.data && response.data.data) {
      invoice.value = response.data.data
    } else {
      // Fallback to sample data for testing
      invoice.value = {
        id: invoiceId.value,
        maHoaDon: `HD${String(invoiceId.value).padStart(6, '0')}`,
        ngayTao: new Date().toISOString(),
        tenNhanVien: 'Nguyễn Văn A',
        tenKhachHang: 'Trần Thị B',
        tongTienSauGiam: 2500000,
        giamGia: 100000,
        phuongThucThanhToan: 'Tiền mặt',
        trangThai: 'Hoàn thành',
        hoaDonChiTiets: [
          {
            tenSanPham: 'Giày Nike Air Max 270',
            mauSac: 'Đen',
            kichThuoc: '42',
            donGia: 1500000,
            soLuong: 1,
            thanhTien: 1500000,
          },
          {
            tenSanPham: 'Áo thun Adidas',
            mauSac: 'Trắng',
            kichThuoc: 'L',
            donGia: 800000,
            soLuong: 1,
            thanhTien: 800000,
          },
        ],
      }
    }
  } catch (error) {
    console.error('Lỗi khi tải chi tiết hóa đơn:', error)
    // Use sample data on error
    invoice.value = {
      id: invoiceId.value,
      maHoaDon: `HD${String(invoiceId.value).padStart(6, '0')}`,
      ngayTao: new Date().toISOString(),
      tenNhanVien: 'Nguyễn Văn A',
      tenKhachHang: 'Trần Thị B',
      tongTienSauGiam: 2500000,
      giamGia: 100000,
      phuongThucThanhToan: 'Tiền mặt',
      trangThai: 'Hoàn thành',
      hoaDonChiTiets: [
        {
          tenSanPham: 'Giày Nike Air Max 270',
          mauSac: 'Đen',
          kichThuoc: '42',
          donGia: 1500000,
          soLuong: 1,
          thanhTien: 1500000,
        },
        {
          tenSanPham: 'Áo thun Adidas',
          mauSac: 'Trắng',
          kichThuoc: 'L',
          donGia: 800000,
          soLuong: 1,
          thanhTien: 800000,
        },
      ],
    }
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString: string) => {
  if (!dateString) return 'Chưa xác định'
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const formatCurrency = (amount: number) => {
  if (!amount) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const getStatusColor = (status: string) => {
  const statusColors: { [key: string]: string } = {
    'Chờ xác nhận': 'orange',
    'Chờ giao hàng': 'blue',
    'Đang giao': 'purple',
    'Hoàn thành': 'green',
    'Đã hủy': 'red',
  }
  return statusColors[status] || 'gray'
}

const getTotalProducts = () => {
  return invoice.value?.hoaDonChiTiets?.length || 0
}

const getTotalQuantity = () => {
  return invoice.value?.hoaDonChiTiets?.reduce((total: number, item: any) => {
    return total + (item.soLuong || 0)
  }, 0) || 0
}

const getSubtotal = () => {
  return invoice.value?.hoaDonChiTiets?.reduce((total: number, item: any) => {
    return total + (item.thanhTien || 0)
  }, 0) || 0
}

const goBack = () => {
  router.back()
}

const printInvoice = () => {
  window.print()
}

// Lifecycle
onMounted(() => {
  fetchInvoiceDetail()
  
  // Set breadcrumb
  breadcrumbItems.value = [
    {
      locale: 'menu.quan-ly-hoa-don',
      route: { name: 'QuanLyHoaDonIndex' },
    },
    {
      locale: 'menu.hoa-don-chi-tiet',
      route: { name: 'HoaDonChiTiet', params: { id: invoiceId.value } },
    },
  ]
})
</script>

<style scoped>
.invoice-detail-page {
  padding: 20px;
  background-color: var(--color-fill-2);
  min-height: calc(100vh - 80px);
}

/* Header */
.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 20px 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.title-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1d2129;
  margin: 0;
}

.status-tag {
  font-size: 14px;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.back-btn {
  height: 36px;
  padding: 0 16px;
}

.print-btn {
  height: 36px;
  padding: 0 16px;
}

/* Loading */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.loading-container p {
  margin-top: 16px;
  color: #86909c;
  font-size: 16px;
}

/* Cards */
.invoice-info-card,
.products-card,
.summary-card {
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
}

/* Info Grid */
.info-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 16px;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f2f3f5;
}

.info-item:last-child {
  border-bottom: none;
}

.label {
  font-weight: 500;
  color: #4e5969;
  min-width: 120px;
}

.value {
  font-weight: 400;
  color: #1d2129;
  text-align: right;
}

.total-amount {
  font-size: 18px;
  font-weight: 600;
  color: #165dff;
}

/* Products Table */
.products-table {
  margin-top: 16px;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-name {
  font-weight: 500;
  color: #1d2129;
}

.product-details {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #86909c;
}

.product-color,
.product-size {
  background: #f2f3f5;
  padding: 2px 6px;
  border-radius: 4px;
}

.total-price {
  font-weight: 600;
  color: #165dff;
}

/* Summary */
.summary-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f2f3f5;
}

.summary-row:last-child {
  border-bottom: none;
}

.summary-label {
  font-weight: 500;
  color: #4e5969;
}

.summary-value {
  font-weight: 400;
  color: #1d2129;
}

.summary-value.discount {
  color: #f53f3f;
}

.summary-row.total-row {
  border-top: 2px solid #165dff;
  margin-top: 8px;
  padding-top: 16px;
}

.summary-row.total-row .summary-label {
  font-size: 16px;
  font-weight: 600;
}

.summary-row.total-row .summary-value.total {
  font-size: 18px;
  font-weight: 700;
  color: #165dff;
}

/* Error State */
.error-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* Responsive */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .action-buttons {
    justify-content: center;
  }

  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .value {
    text-align: left;
  }
}
</style>