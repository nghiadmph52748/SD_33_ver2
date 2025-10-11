<template>
  <div class="product-from-invoice-demo">
    <a-card title="Demo: Danh sách sản phẩm đã bán" :bordered="false">
      <template #extra>
        <a-space>
          <a-button @click="loadInvoices" :loading="loadingInvoices">
            <template #icon>
              <icon-refresh />
            </template>
            Tải danh sách hóa đơn
          </a-button>
        </a-space>
      </template>

      <!-- Invoice Selection -->
      <div class="invoice-selection" v-if="invoices.length > 0">
        <a-alert
          type="info"
          message="Chọn hóa đơn để xem danh sách sản phẩm"
          description="Danh sách sản phẩm sẽ được lấy từ chi tiết hóa đơn đã chọn"
          show-icon
          style="margin-bottom: 20px"
        />

        <a-select v-model="selectedInvoiceId" placeholder="Chọn hóa đơn" style="width: 300px" allow-clear @change="handleInvoiceChange">
          <a-option
            v-for="invoice in invoices"
            :key="invoice.id"
            :value="invoice.id"
            :label="`${invoice.maHoaDon} - ${invoice.tenKhachHang || 'Khách lẻ'} - ${formatPrice(invoice.tongTienSauGiam || invoice.tongTien)}`"
          />
        </a-select>
      </div>

      <!-- Product Table -->
      <div v-if="selectedInvoiceId" class="product-table-container">
        <ProductTableFromInvoice :invoice-id="selectedInvoiceId" :show-actions="true" :page-size="10" @view-detail="handleViewDetail" />
      </div>

      <!-- Empty State -->
      <div v-else-if="!loadingInvoices && invoices.length === 0" class="empty-state">
        <a-empty description="Không có hóa đơn nào để hiển thị" />
      </div>

      <!-- Loading State -->
      <div v-else-if="loadingInvoices" class="loading-state">
        <a-spin />
        <p>Đang tải danh sách hóa đơn...</p>
      </div>
    </a-card>

    <!-- Product Detail Modal -->
    <a-modal v-model:visible="showDetailModal" title="Chi tiết sản phẩm" width="600px" :footer="false">
      <div v-if="selectedProduct" class="product-detail">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="Mã hóa đơn chi tiết">
            {{ selectedProduct.maHoaDonChiTiet }}
          </a-descriptions-item>
          <a-descriptions-item label="Mã sản phẩm">
            {{ selectedProduct.maSanPhamChiTiet || selectedProduct.maSanPham }}
          </a-descriptions-item>
          <a-descriptions-item label="Tên sản phẩm" :span="2">
            {{ selectedProduct.tenSanPhamChiTiet || selectedProduct.tenSanPham }}
          </a-descriptions-item>
          <a-descriptions-item label="Màu sắc">
            {{ selectedProduct.tenMauSac || 'N/A' }}
          </a-descriptions-item>
          <a-descriptions-item label="Kích thước">
            {{ selectedProduct.tenKichThuoc || 'N/A' }}
          </a-descriptions-item>
          <a-descriptions-item label="Đế giày">
            {{ selectedProduct.tenDeGiay || 'N/A' }}
          </a-descriptions-item>
          <a-descriptions-item label="Chất liệu">
            {{ selectedProduct.tenChatLieu || 'N/A' }}
          </a-descriptions-item>
          <a-descriptions-item label="Trọng lượng">
            {{ selectedProduct.tenTrongLuong || 'N/A' }}
          </a-descriptions-item>
          <a-descriptions-item label="Số lượng">{{ selectedProduct.soLuong }} cái</a-descriptions-item>
          <a-descriptions-item label="Giá bán">
            {{ formatPrice(selectedProduct.giaBan) }}
          </a-descriptions-item>
          <a-descriptions-item label="Thành tiền">
            {{ formatPrice(selectedProduct.thanhTien) }}
          </a-descriptions-item>
          <a-descriptions-item label="Trạng thái">
            <a-tag :color="selectedProduct.trangThai ? 'green' : 'red'">
              {{ selectedProduct.trangThai ? 'Đã bán' : 'Hủy' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="Ghi chú" :span="2">
            {{ selectedProduct.ghiChu || 'Không có ghi chú' }}
          </a-descriptions-item>
        </a-descriptions>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { IconRefresh } from '@arco-design/web-vue/es/icon'
import ProductTableFromInvoice from '@/components/product-table-from-invoice/product-table-from-invoice.vue'
import axios from 'axios'

// Reactive data
const loadingInvoices = ref(false)
const invoices = ref<any[]>([])
const selectedInvoiceId = ref<number | undefined>(undefined)
const showDetailModal = ref(false)
const selectedProduct = ref<any>(null)

// Methods
const loadInvoices = async () => {
  try {
    loadingInvoices.value = true
    const response = await axios.get('/api/hoa-don-management/playlist')

    if (response && response.data) {
      invoices.value = response.data || []
    } else {
      // Fallback to mock data
      invoices.value = getMockInvoices()
    }
  } catch (error) {
    // Use mock data as fallback
    invoices.value = getMockInvoices()
  } finally {
    loadingInvoices.value = false
  }
}

const getMockInvoices = () => {
  return [
    {
      id: 1,
      maHoaDon: 'HD000001',
      tenKhachHang: 'Phạm Văn A',
      tongTien: 5000000,
      tongTienSauGiam: 4500000,
      trangThai: true,
      ngayTao: '2024-01-15',
    },
    {
      id: 2,
      maHoaDon: 'HD000002',
      tenKhachHang: 'Hoàng Thị B',
      tongTien: 3200000,
      tongTienSauGiam: 3200000,
      trangThai: true,
      ngayTao: '2024-01-16',
    },
    {
      id: 3,
      maHoaDon: 'HD000003',
      tenKhachHang: 'Nguyễn Văn C',
      tongTien: 7500000,
      tongTienSauGiam: 7000000,
      trangThai: true,
      ngayTao: '2024-01-17',
    },
  ]
}

const formatPrice = (price: number) => {
  if (!price) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)
}

const handleInvoiceChange = (invoiceId: number) => {
  selectedInvoiceId.value = invoiceId
  if (invoiceId) {
    Message.success(`Đã chọn hóa đơn ID: ${invoiceId}`)
  }
}

const handleViewDetail = (product: any) => {
  selectedProduct.value = product
  showDetailModal.value = true
}

// Lifecycle
onMounted(() => {
  loadInvoices()
})
</script>

<style scoped>
.product-from-invoice-demo {
  padding: 20px;
}

.invoice-selection {
  margin-bottom: 30px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.product-table-container {
  margin-top: 20px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 300px;
  gap: 16px;
}

.loading-state p {
  margin: 0;
  color: #6b7280;
}

.product-detail {
  padding: 10px 0;
}
</style>
