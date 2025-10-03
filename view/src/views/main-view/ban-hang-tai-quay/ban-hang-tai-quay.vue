<template>
  <div class="pos-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Main Content -->
    <div class="pos-content">
      <!-- Left Panel - Products -->
      <div class="products-panel">
        <!-- Product Search & Filter -->
        <div class="product-controls">
          <a-input-search v-model="productSearch" placeholder="Tìm kiếm sản phẩm..." style="margin-bottom: 16px" allow-clear />

          <a-space wrap>
            <a-select v-model="selectedCategory" placeholder="Chọn danh mục" style="width: 150px" allow-clear>
              <a-option value="sneaker">Giày sneaker</a-option>
              <a-option value="boot">Giày boot</a-option>
              <a-option value="heel">Giày cao gót</a-option>
              <a-option value="sport">Giày thể thao</a-option>
              <a-option value="sandal">Giày sandal</a-option>
            </a-select>

            <a-select v-model="sortBy" placeholder="Sắp xếp theo" style="width: 130px">
              <a-option value="name">Tên A-Z</a-option>
              <a-option value="price-low">Giá thấp</a-option>
              <a-option value="price-high">Giá cao</a-option>
              <a-option value="newest">Mới nhất</a-option>
            </a-select>
          </a-space>
        </div>

        <!-- Products Grid -->
        <div v-if="loading" class="loading-container">
          <a-spin size="large" />
          <p>Đang tải sản phẩm...</p>
        </div>
        
        <div v-else class="products-grid">
          <a-card v-for="product in filteredProducts" :key="product.id" class="product-card" hoverable @click="addToCart(product)">
            <template #cover>
              <img :src="getProductImage(product)" :alt="product.tenSanPham" class="product-image" />
            </template>

            <a-card-meta :title="product.tenSanPham">
              <template #description>
                <div class="product-info">
                  <div class="product-price">{{ formatCurrency(getProductPrice(product)) }}</div>
                  <div class="product-stock">
                    <a-tag :color="getStockColor(product)">{{ getStockText(product) }}</a-tag>
                  </div>
                </div>
              </template>
            </a-card-meta>

            <template #actions>
              <a-button type="primary" size="small" @click.stop="addToCart(product)">
                <template #icon>
                  <icon-plus />
                </template>
                Thêm
              </a-button>
            </template>
          </a-card>
        </div>
      </div>

      <!-- Right Panel - Cart & Checkout -->
      <div class="cart-panel">
        <!-- Customer Info -->
        <a-card title="Thông tin khách hàng" class="customer-card" size="small">
          <a-form layout="vertical" :model="customerForm">
            <a-form-item label="Tên khách hàng">
              <a-input v-model="customerForm.name" placeholder="Nhập tên khách hàng" allow-clear />
            </a-form-item>
            <a-form-item label="Số điện thoại">
              <a-input v-model="customerForm.phone" placeholder="Nhập số điện thoại" allow-clear />
            </a-form-item>
          </a-form>

          <a-button type="dashed" block style="margin-top: 8px">
            <template #icon>
              <icon-user-add />
            </template>
            Chọn khách hàng
          </a-button>
        </a-card>

        <!-- Cart Items -->
        <a-card title="Giỏ hàng" class="cart-card">
          <div v-if="cartItems.length === 0" class="empty-cart">
            <icon-star style="font-size: 48px; color: #d9d9d9; margin-bottom: 16px" />
            <p style="color: #86909c">Chưa có sản phẩm nào</p>
          </div>

          <div v-else class="cart-items">
            <div v-for="(item, index) in cartItems" :key="item.id" class="cart-item">
              <div class="item-info">
                <img :src="item.image" :alt="item.name" class="item-image" />
                <div class="item-details">
                  <div class="item-name">{{ item.name }}</div>
                  <div class="item-price">{{ formatCurrency(item.price) }}</div>
                </div>
              </div>

              <div class="item-controls">
                <a-input-number
                  v-model="item.quantity"
                  :min="1"
                  :max="item.stock"
                  size="small"
                  style="width: 80px"
                  @change="updateQuantity(index, item.quantity)"
                />
                <a-button type="text" danger size="small" @click="removeFromCart(index)">
                  <template #icon>
                    <icon-delete />
                  </template>
                </a-button>
              </div>
            </div>
          </div>

          <!-- Cart Summary -->
          <div v-if="cartItems.length > 0" class="cart-summary">
            <div class="summary-row">
              <span>Tạm tính:</span>
              <span>{{ formatCurrency(subtotal) }}</span>
            </div>
            <div class="summary-row">
              <span>Giảm giá:</span>
              <span>-{{ formatCurrency(discount) }}</span>
            </div>
            <div class="summary-row total">
              <span><strong>Tổng cộng:</strong></span>
              <span>
                <strong>{{ formatCurrency(total) }}</strong>
              </span>
            </div>
          </div>
        </a-card>

        <!-- Payment -->
        <a-card title="Thanh toán" class="payment-card">
          <a-space direction="vertical" style="width: 100%">
            <a-radio-group v-model="paymentMethod" style="width: 100%">
              <a-space direction="vertical" style="width: 100%">
                <a-radio value="cash">Tiền mặt</a-radio>
                <a-radio value="card">Thẻ tín dụng</a-radio>
                <a-radio value="transfer">Chuyển khoản</a-radio>
              </a-space>
            </a-radio-group>

            <div v-if="paymentMethod === 'cash'" class="cash-payment">
              <a-form-item label="Khách đưa">
                <a-input-number
                  v-model="customerPaid"
                  :min="0"
                  :precision="0"
                  style="width: 100%"
                  placeholder="Nhập số tiền khách đưa"
                  @change="calculateChange"
                />
              </a-form-item>
              <div class="change-info">
                <div class="change-row">
                  <span>Tiền thừa:</span>
                  <span>{{ formatCurrency(change) }}</span>
                </div>
              </div>
            </div>
            <div v-else-if="paymentMethod === 'transfer'" class="transfer-payment">
              <p class="transfer-note">
                Thanh toán trực tuyến qua VNPAY (sandbox). Sau khi khởi tạo, hệ thống sẽ chuyển tới trang thanh toán.
              </p>
              <a-select v-model="selectedTransferBankCode" placeholder="Chọn kênh thanh toán (tùy chọn)" allow-clear>
                <a-option v-for="bank in vnpayBankOptions" :key="bank.value" :value="bank.value">{{ bank.label }}</a-option>
              </a-select>
            </div>
          </a-space>

          <a-button type="primary" size="large" block :disabled="isPaymentDisabled" :loading="isProcessingPayment" @click="processPayment">
            <template #icon>
              <icon-check-circle />
            </template>
            {{ paymentButtonLabel }}
          </a-button>
        </a-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { createVnpayPayment, type CreateVnpayPaymentPayload } from '@/api/payment'
import { Message } from '@arco-design/web-vue'
import { IconPlus, IconUserAdd, IconStar, IconDelete, IconCheckCircle } from '@arco-design/web-vue/es/icon'

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()

// Data from API
const todayOrders = ref(0)
const todayRevenue = ref(0)
const productsList = ref<any[]>([])
const loading = ref(false)

// Search & Filter
const productSearch = ref('')
const selectedCategory = ref('')
const sortBy = ref('name')

// Customer info
const customerForm = ref({
  name: '',
  phone: '',
})

// Cart
const cartItems = ref<any[]>([])

// Payment
const paymentMethod = ref('cash')
const customerPaid = ref(0)
const change = ref(0)
const isProcessingPayment = ref(false)
const selectedTransferBankCode = ref('')
const vnpayBankOptions = [
  { label: 'VNPAY QR', value: 'VNPAYQR' },
  { label: 'Ngân hàng nội địa (ATM)', value: 'VNBANK' },
  { label: 'Thẻ quốc tế (Visa/Master/JCB)', value: 'INTCARD' },
]

// Products data from API
const products = computed(() => productsList.value)

// Helper functions
const getProductImage = (product: any) => {
  // Lấy ảnh đầu tiên từ danh sách ảnh sản phẩm
  if (product.anhSanPham && product.anhSanPham.length > 0) {
    return product.anhSanPham[0]
  }
  // Fallback về placeholder nếu không có ảnh
  return `https://via.placeholder.com/200x150/1890ff/ffffff?text=${encodeURIComponent(product.tenSanPham || 'Sản phẩm')}`
}

const getProductPrice = (product: any) => {
  // Sử dụng giá bán từ chi tiết sản phẩm
  return Number(product.giaBan || 0)
}

const getStockColor = (product: any) => {
  const stock = product.soLuong || 0
  if (stock > 10) return 'green'
  if (stock > 0) return 'orange'
  return 'red'
}

const getStockText = (product: any) => {
  const stock = product.soLuong || 0
  return `${stock} sản phẩm`
}

// Computed
const filteredProducts = computed(() => {
  let filtered = products.value

  // Search filter
  if (productSearch.value) {
    filtered = filtered.filter((product) => product.tenSanPham?.toLowerCase().includes(productSearch.value.toLowerCase()))
  }

  // Category filter (tạm thời bỏ qua vì API không có category)
  // if (selectedCategory.value) {
  //   filtered = filtered.filter((product) => product.category === selectedCategory.value)
  // }

  // Sort
  filtered = [...filtered].sort((a, b) => {
    switch (sortBy.value) {
      case 'price-low':
        return getProductPrice(a) - getProductPrice(b)
      case 'price-high':
        return getProductPrice(b) - getProductPrice(a)
      case 'name':
        return (a.tenSanPham || '').localeCompare(b.tenSanPham || '')
      case 'newest':
        return (b.id || 0) - (a.id || 0)
      default:
        return 0
    }
  })

  return filtered
})

const subtotal = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

const discount = computed(() => {
  // Simple discount logic - can be enhanced
  return subtotal.value > 5000000 ? subtotal.value * 0.1 : 0
})

const total = computed(() => {
  return subtotal.value - discount.value
})

// Methods
const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const paymentButtonLabel = computed(() => {
  const amountLabel = formatCurrency(Math.max(total.value, 0))
  return paymentMethod.value === 'transfer' ? `Thanh toán VNPAY (${amountLabel})` : `Thanh toán (${amountLabel})`
})

const isPaymentDisabled = computed(() => cartItems.value.length === 0 || total.value <= 0 || isProcessingPayment.value)

const addToCart = (product: any) => {
  const existingItem = cartItems.value.find((item) => item.id === product.id)

  if (existingItem) {
    // Kiểm tra số lượng sản phẩm
    if (existingItem.quantity < (product.soLuong || 0)) {
      existingItem.quantity += 1
    }
  } else {
    cartItems.value.push({
      id: product.id,
      name: product.tenSanPham,
      price: getProductPrice(product),
      image: getProductImage(product),
      stock: product.soLuong || 0,
      quantity: 1,
      // Lưu thêm thông tin gốc từ API
      originalProduct: product,
    })
  }
}

const updateQuantity = (index: number, quantity: number) => {
  cartItems.value[index].quantity = quantity
}

const removeFromCart = (index: number) => {
  cartItems.value.splice(index, 1)
}

const calculateChange = () => {
  change.value = Math.max(0, customerPaid.value - total.value)
}

const resetCartState = () => {
  cartItems.value = []
  customerForm.value = { name: '', phone: '' }
  customerPaid.value = 0
  change.value = 0
}

const completeLocalPayment = () => {
  Message.success('Thanh toán thành công')
  resetCartState()
}

const initiateVnpayPayment = async () => {
  if (isProcessingPayment.value) {
    return
  }
  if (total.value <= 0) {
    Message.warning('Số tiền thanh toán không hợp lệ')
    return
  }

  try {
    isProcessingPayment.value = true
    const payload: CreateVnpayPaymentPayload = {
      amount: Math.round(total.value),
      orderId: `POS-${Date.now()}`,
      orderInfo: `Thanh toan don hang tai quay ${customerForm.value.name || 'Khach le'}`,
      locale: 'vn',
    }

    if (selectedTransferBankCode.value) {
      payload.bankCode = selectedTransferBankCode.value
    }

    const response = await createVnpayPayment(payload)

    if (!response?.payUrl) {
      throw new Error('Thiếu đường dẫn thanh toán trong phản hồi VNPAY')
    }

    window.location.href = response.payUrl
  } catch (_error) {
    Message.error('Không thể khởi tạo thanh toán VNPAY. Vui lòng thử lại.')
  } finally {
    isProcessingPayment.value = false
  }
}

const processPayment = async () => {
  if (cartItems.value.length === 0) {
    Message.warning('Chưa có sản phẩm trong giỏ hàng')
    return
  }

  if (total.value <= 0) {
    Message.warning('Số tiền thanh toán không hợp lệ')
    return
  }

  if (paymentMethod.value === 'cash') {
    if (customerPaid.value < total.value) {
      Message.warning('Khách đưa chưa đủ số tiền cần thanh toán')
      return
    }
    calculateChange()
    completeLocalPayment()
    return
  }

  if (paymentMethod.value === 'transfer') {
    await initiateVnpayPayment()
    return
  }

  completeLocalPayment()
}

// Watch for payment method changes
watch(paymentMethod, (method) => {
  if (method !== 'cash') {
    customerPaid.value = 0
    change.value = 0
  }
  if (method !== 'transfer') {
    selectedTransferBankCode.value = ''
  }
})

watch(total, () => {
  if (paymentMethod.value === 'cash') {
    calculateChange()
  }
})

// API functions
const fetchProducts = async () => {
  try {
    loading.value = true
    // Sử dụng API chi tiết sản phẩm để lấy ảnh
    const response = await axios.get('/api/chi-tiet-san-pham-management/playlist')
    
    if (response.success) {
      productsList.value = response.data || []
    }
  } catch {
    // Handle error silently
  } finally {
    loading.value = false
  }
}

const fetchTodayStats = async () => {
  try {
    // Lấy thống kê hôm nay từ API hóa đơn
    const response = await axios.get('/api/hoa-don-management/playlist')
    if (response.success) {
      const invoices = response.data || []
      const today = new Date()
      const todayStr = today.toISOString().split('T')[0]
      
      // Đếm hóa đơn hôm nay
      todayOrders.value = invoices.filter((invoice: any) => {
        const invoiceDate = new Date(invoice.ngayTao || invoice.createdAt)
        return invoiceDate.toISOString().split('T')[0] === todayStr
      }).length
      
      // Tính doanh thu hôm nay
      todayRevenue.value = invoices
        .filter((invoice: any) => {
          const invoiceDate = new Date(invoice.ngayTao || invoice.createdAt)
          return invoiceDate.toISOString().split('T')[0] === todayStr && (invoice.trangThai === true || invoice.ngayThanhToan)
        })
        .reduce((sum: number, invoice: any) => {
          return sum + Number(invoice.tongTienSauGiam || invoice.tongTien || 0)
        }, 0)
    }
  } catch {
    // Handle error silently
  }
}

onMounted(() => {
  fetchProducts()
  fetchTodayStats()
})
</script>

<style scoped>
.pos-page {
  padding: 0 20px 20px 20px;
}

.pos-content {
  display: flex;
  gap: 16px;
  height: calc(100vh - 140px);
}

.products-panel {
  flex: 2;
  display: flex;
  flex-direction: column;
}

.product-controls {
  margin-bottom: 16px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: #86909c;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  overflow-y: auto;
  padding-right: 8px;
}

.product-card {
  overflow: hidden;
}

.product-card .product-image {
  height: 150px;
  object-fit: cover;
}

.product-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.product-price {
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
}

.product-stock {
  font-size: 12px;
}

.cart-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.customer-card,
.cart-card,
.payment-card {
}

.cart-items {
  max-height: 300px;
  overflow-y: auto;
}

.cart-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.cart-item:last-child {
  border-bottom: none;
}

.item-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.item-image {
  width: 40px;
  height: 40px;
  object-fit: cover;
}

.item-details {
  flex: 1;
}

.item-name {
  font-weight: 500;
  color: #1d2129;
  margin-bottom: 4px;
  font-size: 14px;
  line-height: 1.2;
}

.item-price {
  font-size: 12px;
  color: #1890ff;
  font-weight: 500;
}

.item-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.empty-cart {
  text-align: center;
  padding: 40px 20px;
}

.cart-summary {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
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
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #e8e8e8;
}

.cash-payment {
  margin: 16px 0;
}

.change-info {
  background: #f6ffed;
  border: 1px solid #b7eb8f;

  padding: 8px 12px;
  margin-top: 8px;
}

.change-row {
  display: flex;
  justify-content: space-between;
  font-weight: 500;
  color: #52c41a;
}

.transfer-payment {
  margin: 16px 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.transfer-note {
  margin: 0;
  font-size: 13px;
  color: #64748b;
  line-height: 1.4;
}

/* Responsive */
@media (max-width: 1200px) {
  .pos-content {
    flex-direction: column;
    height: auto;
  }

  .products-panel,
  .cart-panel {
    flex: none;
  }

  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  }
}

@media (max-width: 768px) {
  .pos-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .header-right {
    width: 100%;
    justify-content: space-around;
  }

  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  }

  .product-card {
    margin: 0;
  }
}
</style>
