<template>
  <div class="invoice-detail-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">Hóa đơn chi tiết</h1>
        </div>
      </div>
    </div>

    <!-- Content -->
    <div class="content">
      <!-- Trạng thái hóa đơn -->
      <div class="status-panel">
        <div class="status-header">
          <div class="status-icon">
            <icon-clock-circle />
          </div>
          <span class="status-title">Trạng Thái Hóa Đơn</span>
        </div>
        
        <div class="status-content">
          <div class="status-indicator">
            <div class="success-icon">
              <icon-check-circle />
            </div>
            <div class="status-text">Hoàn thành</div>
            <div class="status-time">{{ formatDateTime(new Date()) }}</div>
          </div>
        </div>
      </div>
      
      <!-- Thông tin đơn hàng -->
      <div class="order-info-panel">
        <a-card class="info-card" :bordered="false">
          <template #title>
            <div class="card-header">
              <icon-file />
              <span>Thông tin đơn hàng</span>
            </div>
          </template>
          
          <div class="info-content">
            <div class="info-item">
              <div class="info-label">Mã đơn hàng:</div>
              <div class="info-value">{{ invoiceData?.maHoaDon || `HD${String(invoiceId).padStart(6, '0')}` }}</div>
            </div>
            
            <div class="info-item">
              <div class="info-label">Loại đơn:</div>
              <div class="info-value">{{ invoiceData?.loaiDon ? 'Online' : 'Tại quầy' }}</div>
            </div>
            
            <div class="info-item">
              <div class="info-label">Trạng thái:</div>
              <div class="info-value">
                <a-tag :color="getStatusColor(invoiceData?.trangThai)">{{ getStatusText(invoiceData?.trangThai) }}</a-tag>
              </div>
            </div>
            
            <div class="info-item">
              <div class="info-label">Phiếu giảm giá:</div>
              <div class="info-value">{{ invoiceData?.idtenPhieuGiamGia || 'Không có' }}</div>
            </div>
            
            <div class="info-item">
              <div class="info-label">Ngày đặt:</div>
              <div class="info-value">{{ formatDate(invoiceData?.ngayTao ? new Date(invoiceData.ngayTao) : new Date()) }}</div>
            </div>
          </div>
        </a-card>
      </div>

      <!-- Thông tin khách hàng -->
      <div class="customer-info-panel">
        <a-card class="info-card" :bordered="false">
          <template #title>
            <div class="card-header">
              <icon-file />
              <span>Thông tin khách hàng</span>
            </div>
          </template>
          
          <div class="info-content">
            <div class="info-item">
              <div class="info-label">Tên khách hàng:</div>
              <div class="info-value">{{ invoiceData?.idtenKhachHang || invoiceData?.tenNguoiNhan || 'Không xác định' }}</div>
            </div>
            
            <div class="info-item">
              <div class="info-label">Số điện thoại:</div>
              <div class="info-value">{{ invoiceData?.sdtNguoiNhan || 'Không có' }}</div>
            </div>
            
            <div class="info-item">
              <div class="info-label">Địa chỉ:</div>
              <div class="info-value">{{ invoiceData?.diaChiNguoiNhan || 'Không có' }}</div>
            </div>
            
            <div class="info-item">
              <div class="info-label">Email:</div>
              <div class="info-value">{{ invoiceData?.emailNguoiNhan || 'Không có' }}</div>
            </div>
            
            <div class="info-item">
              <div class="info-label">Ghi chú:</div>
              <div class="info-value">{{ invoiceData?.ghiChu || 'Không có ghi chú' }}</div>
            </div>
          </div>
        </a-card>
      </div>

      <!-- Danh sách sản phẩm -->
      <div class="product-list-panel">
        <a-card class="info-card" :bordered="false">
          <template #title>
            <div class="card-header">
              <icon-file />
              <span>Danh sách sản phẩm</span>
            </div>
          </template>
          
          <div class="product-table">
            <a-spin :loading="loading" style="width: 100%">
              <a-table 
                :columns="productColumns" 
                :data="productList"
                :pagination="false"
                :bordered="false"
              >
              <template #price="{ record }">
                <span class="price-text">{{ formatPrice(record.price) }}</span>
              </template>
              
              <template #total="{ record }">
                <span class="total-text">{{ formatPrice(record.total) }}</span>
              </template>
              </a-table>
            </a-spin>
          </div>
        </a-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { IconClockCircle, IconCheckCircle, IconFile } from '@arco-design/web-vue/es/icon'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const { breadcrumbItems } = useBreadcrumb()

const invoiceId = ref(route.params.id)

// Data refs
const invoiceData = ref(null)
const productList = ref([])
const loading = ref(false)

// Set breadcrumb
breadcrumbItems.value = [
  {
    locale: 'menu.quan-ly-hoa-don',
    route: { name: 'QuanLyHoaDonIndex' },
  },
  {
    title: 'Hóa đơn chi tiết',
    route: { name: 'HoaDonChiTiet', params: { id: invoiceId.value } },
  },
]

// Product columns - đơn giản hóa để tránh lỗi
const productColumns = [
  {
    title: 'STT',
    dataIndex: 'stt',
    width: 80,
    align: 'center',
  },
  {
    title: 'Tên sản phẩm',
    dataIndex: 'name',
    width: 300,
  },
  {
    title: 'Số lượng',
    dataIndex: 'quantity',
    width: 100,
    align: 'center',
  },
  {
    title: 'Đơn giá',
    dataIndex: 'price',
    slotName: 'price',
    width: 150,
    align: 'right',
  },
  {
    title: 'Thành tiền',
    dataIndex: 'total',
    slotName: 'total',
    width: 150,
    align: 'right',
  },
]

// Fetch invoice detail data
const fetchInvoiceDetail = async () => {
  try {
    loading.value = true
    const response = await axios.get(`/api/hoa-don-management/${invoiceId.value}`)
    
    if (response.data && response.data.data) {
      invoiceData.value = response.data.data
      
      // Process product list from invoice details - đơn giản hóa
      if (response.data.data.items && response.data.data.items.length > 0) {
        productList.value = response.data.data.items.map((item, index) => ({
          stt: index + 1,
          name: item.tenSanPham || 'Sản phẩm không xác định',
          quantity: item.soLuong || 0,
          price: item.giaBan || 0,
          total: item.thanhTien || ((item.soLuong || 0) * (item.giaBan || 0)),
        }))
      }
    }
  } catch (error) {
    console.error('Lỗi khi tải chi tiết hóa đơn:', error)
    // Fallback data nếu API lỗi
    productList.value = [
      {
        stt: 1,
        name: 'Sản phẩm mẫu',
        quantity: 1,
        price: 1000000,
        total: 1000000,
        image: '/default-product.png',
      },
    ]
  } finally {
    loading.value = false
  }
}

// Load data on mount
onMounted(() => {
  fetchInvoiceDetail()
})

// Format functions
const formatDate = (date: Date) => {
  return date.toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

const formatCurrency = (amount: number) => {
  if (!amount) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const formatDateTime = (date: Date) => {
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  const seconds = date.getSeconds().toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const year = date.getFullYear()

  return `${hours}:${minutes}:${seconds} ${day}-${month}-${year}`
}

const formatPrice = (price: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)
}

// Status helper functions
const getStatusColor = (status: any) => {
  switch (status) {
    case 1:
    case 'Hoàn thành':
      return 'green'
    case 2:
    case 'Đang giao':
      return 'blue'
    case 3:
    case 'Chờ xác nhận':
      return 'orange'
    case 4:
    case 'Đã hủy':
      return 'red'
    default:
      return 'gray'
  }
}

const getStatusText = (status: any) => {
  switch (status) {
    case 1:
      return 'Hoàn thành'
    case 2:
      return 'Đang giao'
    case 3:
      return 'Chờ xác nhận'
    case 4:
      return 'Đã hủy'
    default:
      return 'Không xác định'
  }
}
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
  background: white;
  padding: 20px 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.status-panel {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.status-header {
  background: #f5f5f5;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #e5e5e5;
}

.status-icon {
  color: #666;
  font-size: 18px;
}

.status-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.status-content {
  padding: 40px 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

.status-indicator {
  text-align: center;
}

.success-icon {
  width: 80px;
  height: 80px;
  background: #165dff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  font-size: 40px;
  color: white;
}

.status-text {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.status-time {
  font-size: 14px;
  color: #666;
  font-family: monospace;
}

.order-info-panel {
  margin-bottom: 16px;
}

.customer-info-panel {
  margin-bottom: 16px;
}

.product-list-panel {
  margin-bottom: 16px;
}

.product-table {
  margin-top: 16px;
}

.product-image {
  display: flex;
  justify-content: center;
  align-items: center;
}

.product-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e5e5e5;
}

.price-text,
.total-text {
  font-weight: 600;
  color: #1d2129;
}

.info-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
}

.info-content {
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

.info-label {
  font-weight: 500;
  color: #4e5969;
  min-width: 120px;
}

.info-value {
  font-weight: 400;
  color: #1d2129;
  text-align: right;
  flex: 1;
}
</style>
