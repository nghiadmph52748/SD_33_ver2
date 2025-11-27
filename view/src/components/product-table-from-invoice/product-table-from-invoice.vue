<template>
  <div class="product-table-from-invoice">
    <!-- Header -->
    <div class="table-header">
      <div class="header-left">
        <h3>Danh sách sản phẩm đã bán</h3>
        <p v-if="invoiceId">Hóa đơn ID: {{ invoiceId }}</p>
      </div>
      <div class="header-right">
        <a-space>
          <a-button @click="refreshData" :loading="loading">
            <template #icon>
              <icon-refresh />
            </template>
            Làm mới
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- Data Source Notice -->
    <div v-if="isUsingMockData" class="data-notice">
      <a-alert
        type="warning"
        message="Đang hiển thị dữ liệu mẫu"
        description="Không thể kết nối đến API hóa đơn. Đang sử dụng dữ liệu mẫu để demo."
        show-icon
        closable
      />
    </div>

    <!-- Product Table -->
    <div class="table-wrapper">
      <a-spin :loading="loading" style="width: 100%">
        <a-table
          :columns="tableColumns"
          :data="products"
          :pagination="paginationConfig"
          :loading="loading"
          row-key="id"
          @page-change="handlePageChange"
          @page-size-change="handlePageSizeChange"
        >
          <!-- STT Column -->
          <template #stt="{ rowIndex }">
            <span class="stt">{{ rowIndex + 1 + (paginationConfig.current - 1) * paginationConfig.pageSize }}</span>
          </template>

          <!-- Name Column -->
          <template #name="{ record }">
            <div class="product-name">
              <div class="name">{{ record.tenSanPhamChiTiet || record.tenSanPham || 'N/A' }}</div>
              <div class="code">Mã: {{ record.maSanPhamChiTiet || record.maSanPham || `SP${String(record.id).padStart(5, '0')}` }}</div>
            </div>
          </template>

          <!-- Attributes Column -->
          <template #attributes="{ record }">
            <div class="attributes">
              <div v-if="record.tenMauSac" class="attribute-item">
                <span class="label">Màu:</span>
                <a-tag :color="getColorTag(record.tenMauSac)" :style="getColorTagStyle(record.tenMauSac)">{{ record.tenMauSac }}</a-tag>
              </div>
              <div v-if="record.tenKichThuoc" class="attribute-item">
                <span class="label">Size:</span>
                <span>{{ record.tenKichThuoc }}</span>
              </div>
              <div v-if="record.tenDeGiay" class="attribute-item">
                <span class="label">Đế:</span>
                <span>{{ record.tenDeGiay }}</span>
              </div>
              <div v-if="record.tenChatLieu" class="attribute-item">
                <span class="label">Chất liệu:</span>
                <span>{{ record.tenChatLieu }}</span>
              </div>
              <div v-if="record.tenTrongLuong" class="attribute-item">
                <span class="label">Trọng lượng:</span>
                <span>{{ record.tenTrongLuong }}</span>
              </div>
            </div>
          </template>

          <!-- Quantity Column -->
          <template #quantity="{ record }">
            <div class="quantity-info">
              <span class="quantity">{{ record.soLuong || 0 }}</span>
              <span class="unit">cái</span>
            </div>
          </template>

          <!-- Price Column -->
          <template #price="{ record }">
            <div class="price-info">
              <div class="price">
                {{ formatPrice(calculateUnitPrice(record)) }}
              </div>
              <div v-if="record.thanhTien" class="total">Thành tiền: {{ formatPrice(record.thanhTien) }}</div>
            </div>
          </template>

          <!-- Status Column -->
          <template #status="{ record }">
            <a-tag color="green">Đã bán</a-tag>
          </template>

          <!-- Actions Column -->
          <template #action="{ record }" v-if="showActions">
            <a-space>
              <a-button size="small" @click="viewDetail(record)">
                <template #icon>
                  <icon-eye />
                </template>
                Xem
              </a-button>
            </a-space>
          </template>
        </a-table>
      </a-spin>
    </div>

    <!-- Empty State -->
    <div v-if="!loading && products.length === 0" class="empty-state">
      <a-empty description="Chưa có sản phẩm nào trong hóa đơn này" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { IconRefresh, IconEye } from '@arco-design/web-vue/es/icon'
import axios from 'axios'

// Props
interface Props {
  invoiceId?: number
  showActions?: boolean
  pageSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  showActions: true,
  pageSize: 10,
})

// Emits
const emit = defineEmits<{
  viewDetail: [product: any]
}>()

// Reactive data
const loading = ref(false)
const products = ref<any[]>([])
const isUsingMockData = ref(false)

// Pagination
const paginationConfig = ref({
  current: 1,
  pageSize: props.pageSize,
  total: 0,
  showTotal: true,
  showPageSize: true,
  pageSizeOptions: ['10', '20', '50', '100'],
})

// Table columns
const tableColumns = computed(() => {
  const baseColumns = [
    {
      title: 'STT',
      dataIndex: 'stt',
      key: 'stt',
      width: 60,
      align: 'center' as const,
      slotName: 'stt',
    },
    {
      title: 'Tên sản phẩm',
      dataIndex: 'tenSanPhamChiTiet',
      key: 'name',
      width: 300,
      slotName: 'name',
    },
    {
      title: 'Thuộc tính',
      key: 'attributes',
      width: 250,
      slotName: 'attributes',
    },
    {
      title: 'Số lượng',
      dataIndex: 'soLuong',
      key: 'quantity',
      width: 100,
      align: 'center' as const,
      slotName: 'quantity',
    },
    {
      title: 'Giá',
      dataIndex: 'giaBan',
      key: 'price',
      width: 150,
      align: 'right' as const,
      slotName: 'price',
    },
    {
      title: 'Trạng thái',
      key: 'status',
      width: 100,
      align: 'center' as const,
      slotName: 'status',
    },
  ]
  if (props.showActions) {
    baseColumns.push({
      title: 'Thao tác',
      key: 'action',
      width: 120,
      align: 'center' as const,
      slotName: 'action',
    })
  }
  return baseColumns
})

// Methods
const formatPrice = (price: number) => {
  if (!price) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)
}

// Calculate unit price: prefer thanhTien / soLuong if available, otherwise use giaBan
const calculateUnitPrice = (record: any): number => {
  // If we have thanhTien and soLuong, calculate unit price from them (most accurate)
  if (record.thanhTien && record.soLuong && record.soLuong > 0) {
    const calculatedPrice = Number(record.thanhTien) / Number(record.soLuong)
    // Round to nearest integer to avoid decimal issues
    return Math.round(calculatedPrice)
  }

  // Fallback to giaBan if available
  if (record.giaBan) {
    return Number(record.giaBan)
  }

  return 0
}

const getColorTag = (color: string) => {
  const colorMap: { [key: string]: string } = {
    Đen: 'black',
    Trắng: 'white',
    Đỏ: 'red',
    Xanh: 'blue',
    Vàng: 'yellow',
    Nâu: 'brown',
    Xám: 'gray',
    Hồng: 'pink',
  }
  return colorMap[color] || 'default'
}

const getColorTagStyle = (color: string) => {
  const background = getColorTag(color)
  if (background === 'white' || background === 'yellow') {
    return {
      color: '#1d2129',
      border: '1px solid #d9d9d9',
    }
  }
  return {}
}

// Mock data fallback
const getMockProducts = () => {
  return [
    {
      id: 1,
      maHoaDonChiTiet: 'HDCT00001',
      tenSanPhamChiTiet: 'Giày Nike Air Max 270 - Đen - Size 39',
      maSanPhamChiTiet: 'CTSP00001',
      tenSanPham: 'Giày Nike Air Max 270',
      tenMauSac: 'Đen',
      tenKichThuoc: '39',
      tenDeGiay: 'Đế cao su',
      tenChatLieu: 'Da tổng hợp',
      tenTrongLuong: '250g',
      soLuong: 2,
      giaBan: 2500000,
      thanhTien: 5000000,
      trangThai: true,
      anhSanPham: 'https://via.placeholder.com/200x150/1890ff/ffffff?text=Nike+Air+Max',
    },
    {
      id: 2,
      maHoaDonChiTiet: 'HDCT00002',
      tenSanPhamChiTiet: 'Giày Adidas Ultraboost 22 - Trắng - Size 40',
      maSanPhamChiTiet: 'CTSP00002',
      tenSanPham: 'Giày Adidas Ultraboost 22',
      tenMauSac: 'Trắng',
      tenKichThuoc: '40',
      tenDeGiay: 'Đế EVA',
      tenChatLieu: 'Vải canvas',
      tenTrongLuong: '300g',
      soLuong: 1,
      giaBan: 3200000,
      thanhTien: 3200000,
      trangThai: true,
      anhSanPham: 'https://via.placeholder.com/200x150/52c41a/ffffff?text=Adidas+Ultraboost',
    },
  ]
}

const loadProducts = async () => {
  if (!props.invoiceId) {
    products.value = []
    paginationConfig.value.total = 0
    return
  }

  try {
    loading.value = true
    const response = await axios.get(`/api/thong-tin-hoa-don-management/san-pham-da-ban/${props.invoiceId}`)

    // Response đã được interceptor xử lý, nên response chính là data từ backend
    if (response && response.data && Array.isArray(response.data)) {
      products.value = response.data || []
      paginationConfig.value.total = products.value.length
      isUsingMockData.value = false
    } else {
      // Fallback to mock data if API fails
      products.value = getMockProducts()
      paginationConfig.value.total = products.value.length
      isUsingMockData.value = true
    }
  } catch (error) {
    console.error('Error loading products:', error)
    // Use mock data as fallback
    products.value = getMockProducts()
    paginationConfig.value.total = products.value.length
    isUsingMockData.value = true
  } finally {
    loading.value = false
  }
}

const refreshData = () => {
  loadProducts()
}

const handlePageChange = (page: number) => {
  paginationConfig.value.current = page
  loadProducts()
}

const handlePageSizeChange = (pageSize: number) => {
  paginationConfig.value.pageSize = pageSize
  paginationConfig.value.current = 1
  loadProducts()
}

const viewDetail = (product: any) => {
  emit('viewDetail', product)
}

// Watch for invoiceId changes
watch(
  () => props.invoiceId,
  () => {
    if (props.invoiceId) {
      loadProducts()
    }
  },
  { immediate: true }
)

// Lifecycle
onMounted(() => {
  if (props.invoiceId) {
    loadProducts()
  }
})
</script>

<style scoped>
.product-table-from-invoice {
  padding: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e5e7eb;
}

.header-left h3 {
  margin: 0 0 5px 0;
  color: #1f2937;
  font-size: 18px;
  font-weight: 600;
}

.header-left p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.data-notice {
  margin-bottom: 20px;
}

.table-wrapper {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.stt {
  font-weight: 500;
  color: #6b7280;
}

.product-name {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-name .name {
  font-weight: 500;
  color: #1f2937;
  line-height: 1.4;
}

.product-name .code {
  font-size: 12px;
  color: #6b7280;
}

.attributes {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.attribute-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}

.attribute-item .label {
  font-weight: 500;
  color: #6b7280;
  min-width: 50px;
}

.quantity-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.quantity {
  font-weight: 600;
  color: #1f2937;
  font-size: 16px;
}

.unit {
  font-size: 12px;
  color: #6b7280;
}

.price-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.price {
  font-weight: 600;
  color: #dc2626;
  font-size: 14px;
}

.total {
  font-size: 12px;
  color: #059669;
  font-weight: 500;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}
</style>
