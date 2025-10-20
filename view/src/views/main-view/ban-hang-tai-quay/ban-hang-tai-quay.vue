<template>
  <div class="pos-system">
    <!-- Header -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Main Layout -->
    <a-row :gutter="16" class="pos-main">
      <!-- Left: Orders & Cart -->
      <a-col :xs="24" :lg="16" class="pos-left">
        <a-card class="orders-card" title="Danh Sách Đơn Hàng">
          <template #extra>
            <a-space size="small">
              <a-button v-if="orders.length < 8" type="primary" size="small" @click="createNewOrder">
                <template #icon>
                  <icon-plus />
                </template>
                Thêm Đơn
              </a-button>
            </a-space>
          </template>

          <a-empty v-if="orders.length === 0" description="Chưa có đơn hàng nào" />

          <a-tabs v-else v-model:active-key="currentOrderIndex" type="button" @change="handleOrderChange" class="orders-tabs">
            <a-tab-pane v-for="(order, idx) in orders" :key="idx.toString()">
              <template #title>
                <div class="tab-header">
                  <span>Đơn #{{ idx + 1 }} ({{ order.orderCode }})</span>
                  <a-badge
                    v-if="order.items.length > 0"
                    :count="order.items.reduce((sum, item) => sum + item.quantity, 0)"
                    :style="{ backgroundColor: '#f5222d' }"
                  />
                  <a-button type="text" size="mini" status="danger" class="tab-close-btn" @click.stop="showDeleteConfirm(idx)">
                    <template #icon>
                      <icon-close />
                    </template>
                  </a-button>
                </div>
              </template>

              <!-- Product Selection Toolbar -->
              <div class="toolbar" style="display: flex; justify-content: space-between; align-items: center">
                <div style="font-weight: 600; color: #333; font-size: 14px">
                  Mã Đơn:
                  <span style="color: #0960bd; font-weight: 700">{{ currentOrder?.orderCode }}</span>
                </div>
                <a-space wrap>
                  <a-button type="primary" @click="showProductModal = true">
                    <template #icon>
                      <icon-plus />
                    </template>
                    Thêm Sản Phẩm
                  </a-button>
                  <a-button @click="showQRScanner = true">
                    <template #icon>
                      <icon-qrcode />
                    </template>
                    Quét QR
                  </a-button>
                  <a-button v-if="currentOrder?.items.length > 0" type="text" status="danger" @click="clearCart">
                    <template #icon>
                      <icon-delete />
                    </template>
                    Xoá Tất Cả
                  </a-button>
                </a-space>
              </div>

              <!-- Cart Table -->
              <a-card class="cart-card">
                <template #title>🛒 Giỏ Hàng</template>
                <div class="cart-wrapper">
                  <a-table
                    v-if="currentOrder?.items.length > 0"
                    :columns="cartColumns"
                    :data="paginatedCartItems"
                    :pagination="{
                      current: cartPagination.value?.current || 1,
                      pageSize: cartPagination.value?.pageSize || 5,
                      total: currentOrder?.items.length || 0,
                      showTotal: true,
                      showPageSize: true,
                    }"
                    row-key="id"
                    size="small"
                    :scroll="{ x: '100%' }"
                    @paginate="(page) => (cartPagination.value.current = page)"
                  >
                    <template #quantity="{ record }">
                      <a-input-number
                        :model-value="record.quantity"
                        :min="1"
                        :max="999"
                        size="small"
                        @change="(val) => updateQuantity(record.id, val)"
                      />
                    </template>

                    <template #price="{ record }">
                      {{ formatCurrency(record.price) }}
                    </template>

                    <template #subtotal="{ record }">
                      <strong>{{ formatCurrency(record.price * record.quantity) }}</strong>
                    </template>

                    <template #action="{ record }">
                      <a-popconfirm title="Xoá sản phẩm này?" @ok="removeFromCart(record.id)" ok-text="Xoá" cancel-text="Hủy">
                        <a-button type="text" status="danger" size="small">
                          <template #icon>
                            <icon-delete />
                          </template>
                        </a-button>
                      </a-popconfirm>
                    </template>
                  </a-table>
                  <a-empty v-else description="Giỏ hàng trống" />
                </div>
              </a-card>
            </a-tab-pane>
          </a-tabs>
        </a-card>
      </a-col>

      <!-- Right: Customer & Payment -->
      <a-col :xs="24" :lg="8" class="pos-right">
        <!-- Customer Section -->
        <a-card title="Thông Tin Khách Hàng" class="customer-card">
          <a-form layout="vertical">
            <a-form-item label="Chọn Khách Hàng">
              <a-select
                v-model:model-value="currentOrder.customerId"
                placeholder="--- Chọn khách hàng ---"
                allow-search
                filterable
                @change="handleCustomerChange"
              >
                <a-option value="">Khách lẻ</a-option>
                <a-option v-for="customer in filteredCustomers" :key="customer.id" :value="customer.id">
                  {{ customer.name }} ({{ customer.phone }})
                </a-option>
              </a-select>
            </a-form-item>

            <a-form-item v-if="selectedCustomer">
              <a-descriptions size="small" :column="1" bordered>
                <a-descriptions-item label="Tên">{{ selectedCustomer.name }}</a-descriptions-item>
                <a-descriptions-item label="SĐT">{{ selectedCustomer.phone }}</a-descriptions-item>
                <a-descriptions-item label="Email">{{ selectedCustomer.email || 'N/A' }}</a-descriptions-item>
                <a-descriptions-item label="Địa Chỉ">{{ selectedCustomer.address || 'N/A' }}</a-descriptions-item>
              </a-descriptions>
            </a-form-item>

            <a-button v-if="!selectedCustomer" type="dashed" long @click="showAddCustomerModal = true">
              <template #icon>
                <icon-plus />
              </template>
              Thêm Khách Hàng Mới
            </a-button>
          </a-form>
        </a-card>

        <!-- Payment Section -->
        <a-card title="Thanh Toán" class="payment-card">
          <a-form layout="vertical">
            <!-- Discount Section - Button Style -->
            <a-form-item>
              <a-button
                v-if="!paymentForm.value?.discountCode"
                long
                size="large"
                type="secondary"
                style="
                  height: 56px;
                  font-size: 15px;
                  border: 1px solid #d9d9d9;
                  background: transparent;
                  color: #000;
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  padding: 0 16px;
                "
                @click="showVoucherModal = true"
              >
                <span style="font-weight: 500; text-align: left">Phiếu Giảm Giá</span>
                <span style="font-weight: 400; font-size: 12px; text-align: right; color: #999">Chọn hoặc nhập mã ></span>
              </a-button>
            </a-form-item>

            <!-- Payment Method -->
            <a-form-item label="Phương Thức Thanh Toán">
              <div style="display: flex; gap: 8px; width: 100%">
                <a-button
                  :type="paymentForm.value?.method === 'cash' ? 'primary' : 'secondary'"
                  style="flex: 1"
                  @click="paymentForm.value && (paymentForm.value.method = 'cash')"
                >
                  💵 Tiền Mặt
                </a-button>
                <a-button
                  :type="paymentForm.value?.method === 'transfer' ? 'primary' : 'secondary'"
                  style="flex: 1"
                  @click="paymentForm.value && (paymentForm.value.method = 'transfer')"
                >
                  🏦 Chuyển Khoản
                </a-button>
              </div>
            </a-form-item>

            <!-- Cash Input -->
            <a-form-item v-if="paymentForm.value?.method === 'cash'" label="Tiền Nhận">
              <a-input-number
                v-model:model-value="paymentForm.value.cashReceived"
                :min="finalPrice"
                placeholder="Nhập tiền nhận"
                style="width: 100%"
                @update:model-value="(val) => (paymentForm.value.cashReceived = val || 0)"
              />
            </a-form-item>

            <!-- Transfer Notes -->
            <a-alert v-if="paymentForm.value?.method === 'transfer'" type="info" title="Chuyển Khoản" closable>
              <p>Vui lòng chuyển khoản theo thông tin cung cấp. Mã hoá đơn: {{ currentOrder?.id }}</p>
            </a-alert>

            <!-- Price Summary -->
            <a-divider />
            <div class="payment-summary">
              <p class="summary-row">
                <span>Tổng tiền:</span>
                <strong>{{ formatCurrency(subtotal) }}</strong>
              </p>
              <p class="summary-row">
                <span>Giảm giá:</span>
                <span :class="discountAmount > 0 ? 'discount-text' : ''">
                  {{ discountAmount > 0 ? '-' : '' }}{{ formatCurrency(discountAmount) }}
                </span>
              </p>
              <p class="summary-row total">
                <span>Thành tiền:</span>
                <strong class="final-price">{{ formatCurrency(finalPrice) }}</strong>
              </p>
              <p v-if="paymentForm.value?.method === 'cash' && change > 0" class="summary-row">
                <span>Tiền thối:</span>
                <span class="change-text">{{ formatCurrency(change) }}</span>
              </p>
            </div>

            <!-- Action Buttons -->
            <a-space direction="vertical" size="large" style="width: 100%; margin-top: 16px">
              <a-button type="primary" long size="large" :disabled="!canConfirmOrder" :loading="confirmLoading" @click="confirmOrder">
                <template #icon>
                  <icon-check />
                </template>
                Xác Nhận ({{ finalPrice > 0 ? formatCurrency(finalPrice) : '0đ' }})
              </a-button>
              <a-button long @click="printOrder" :disabled="!currentOrder?.items.length">🖨️ In Hoá Đơn</a-button>
            </a-space>
          </a-form>
        </a-card>
      </a-col>
    </a-row>

    <!-- Modals -->
    <!-- Product Selection Modal -->
    <a-modal v-model:visible="showProductModal" title="Chọn Sản Phẩm" width="90%" :footer="null" @cancel="showProductModal = false">
      <a-table
        v-if="productVariants.length > 0"
        :columns="[
          { title: 'Mã', dataIndex: 'maChiTietSanPham', key: 'maChiTietSanPham', width: 120 },
          { title: 'Sản Phẩm', dataIndex: 'tenSanPham', key: 'tenSanPham', width: 200 },
          { title: 'Màu', dataIndex: 'tenMauSac', key: 'tenMauSac', width: 100 },
          { title: 'Kích Thước', dataIndex: 'tenKichThuoc', key: 'tenKichThuoc', width: 100 },
          { title: 'Giá', dataIndex: 'giaBan', key: 'giaBan', width: 100, slotName: 'price' },
          { title: 'Số Lượng', dataIndex: 'soLuong', key: 'soLuong', width: 80 },
          { title: 'Hành Động', key: 'action', width: 80, slotName: 'action' },
        ]"
        :data="productVariants"
        size="small"
        :scroll="{ x: '100%' }"
        :pagination="{
          current: productPagination.current,
          pageSize: productPagination.pageSize,
          total: productPagination.total,
          showTotal: true,
        }"
        @paginate="(page) => loadProductPage(page)"
      >
        <template #price="{ record }">
          {{ formatCurrency(record.giaBan) }}
        </template>
        <template #action="{ record }">
          <a-button
            type="primary"
            size="small"
            @click="
              () => {
                if (currentOrder) {
                  const item: CartItem = {
                    id: `${Date.now()}_${Math.random()}`,
                    productId: record.id?.toString() || '',
                    productName: record.tenSanPham || '',
                    price: record.giaBan || 0,
                    quantity: 1,
                    image: record.anhSanPham?.[0] || '',
                  }
                  currentOrder.items.push(item)
                  showProductModal = false
                  Message.success('Thêm sản phẩm thành công')
                }
              }
            "
          >
            Thêm
          </a-button>
        </template>
      </a-table>
      <a-empty v-else description="Không có sản phẩm" />
    </a-modal>

    <!-- QR Scanner Modal -->
    <a-modal v-model:visible="showQRScanner" title="Quét Mã QR Sản Phẩm" width="500px" :footer="null" @cancel="showQRScanner = false">
      <div style="text-align: center; padding: 40px">
        <a-empty description="Scanner sẽ được load ở đây" />
        <p style="color: #86909c; margin-top: 16px">(Sử dụng html5-qrcode)</p>
      </div>
    </a-modal>

    <!-- Add Customer Modal -->
    <a-modal
      v-model:visible="showAddCustomerModal"
      title="Thêm Khách Hàng Mới"
      width="500px"
      ok-text="Thêm"
      cancel-text="Hủy"
      @ok="addNewCustomer"
    >
      <a-form v-if="newCustomerForm.value" :model="newCustomerForm.value" layout="vertical">
        <a-form-item label="Tên Khách Hàng" required>
          <a-input
            :model-value="newCustomerForm.value?.name || ''"
            placeholder="Nhập tên khách hàng"
            @update:model-value="(val) => newCustomerForm.value && (newCustomerForm.value.name = val)"
          />
        </a-form-item>

        <a-form-item label="Số Điện Thoại" required>
          <a-input
            :model-value="newCustomerForm.value?.phone || ''"
            placeholder="Nhập số điện thoại"
            @update:model-value="(val) => newCustomerForm.value && (newCustomerForm.value.phone = val)"
          />
        </a-form-item>

        <a-form-item label="Email">
          <a-input
            :model-value="newCustomerForm.value?.email || ''"
            placeholder="Nhập email"
            type="email"
            @update:model-value="(val) => newCustomerForm.value && (newCustomerForm.value.email = val)"
          />
        </a-form-item>

        <a-form-item label="Địa Chỉ">
          <a-textarea
            :model-value="newCustomerForm.value?.address || ''"
            placeholder="Nhập địa chỉ"
            :auto-size="{ minRows: 2, maxRows: 4 }"
            @update:model-value="(val) => newCustomerForm.value && (newCustomerForm.value.address = val)"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Voucher Selection Modal -->
    <a-modal v-model:visible="showVoucherModal" title="Chọn Phiếu Giảm Giá" width="900px" :footer="null" @cancel="showVoucherModal = false">
      <div style="max-height: 600px; overflow-y: auto">
        <a-empty v-if="coupons.length === 0" description="Không có phiếu giảm giá" />
        <div v-else style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px">
          <div
            v-for="coupon in coupons"
            :key="coupon.id"
            style="
              border: 1px solid #e5e5e5;
              border-radius: 8px;
              padding: 16px;
              cursor: pointer;
              transition: all 0.3s;
              background: linear-gradient(135deg, #fff8e1 0%, #fff 100%);
              position: relative;
              overflow: hidden;
            "
            @click="selectVoucher(coupon)"
            @mouseenter="
              (e) => {
                e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.1)'
                e.currentTarget.style.transform = 'translateY(-4px)'
              }
            "
            @mouseleave="
              (e) => {
                e.currentTarget.style.boxShadow = 'none'
                e.currentTarget.style.transform = 'translateY(0)'
              }
            "
          >
            <!-- Dashed border decoration -->
            <div
              style="
                position: absolute;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
                border: 2px dashed #e5e5e5;
                border-radius: 8px;
                pointer-events: none;
              "
            ></div>

            <!-- Content -->
            <div style="position: relative; z-index: 1">
              <!-- Header with code -->
              <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px">
                <div style="flex: 1">
                  <div style="font-size: 14px; font-weight: 600; color: #333; margin-bottom: 4px">
                    {{ coupon.maPhieuGiamGia }}
                  </div>
                  <div style="font-size: 12px; color: #86909c; line-height: 1.4">
                    {{ coupon.tenPhieuGiamGia }}
                  </div>
                </div>
              </div>

              <!-- Discount value (main attraction) -->
              <div
                style="
                  background: linear-gradient(135deg, #f5222d 0%, #ff4d4f 100%);
                  color: white;
                  padding: 12px;
                  border-radius: 4px;
                  margin-bottom: 12px;
                  text-align: center;
                "
              >
                <div style="font-size: 20px; font-weight: 700">
                  {{ formatCurrency(Number(coupon.giaTriGiamGia) || 0) }}
                </div>
              </div>

              <!-- Min order -->
              <div v-if="coupon.hoaDonToiThieu" style="font-size: 12px; color: #86909c; margin-bottom: 8px">
                Hoá đơn tối thiểu:
                <strong>{{ formatCurrency(Number(coupon.hoaDonToiThieu)) }}</strong>
              </div>

              <!-- Quantity left -->
              <div v-if="coupon.soLuongDung" style="font-size: 12px; color: #86909c; margin-bottom: 12px">
                Còn lại:
                <strong style="color: #f5222d">{{ coupon.soLuongDung }}</strong>
              </div>

              <!-- Validity period -->
              <div
                v-if="coupon.ngayKetThuc"
                style="font-size: 11px; color: #86909c; margin-bottom: 12px; padding: 8px; background: #f5f5f5; border-radius: 4px"
              >
                Hết hạn: {{ coupon.ngayKetThuc }}
              </div>

              <!-- Select button -->
              <a-button
                type="primary"
                long
                @click.stop="selectVoucher(coupon)"
                style="background: linear-gradient(135deg, #f5222d 0%, #ff4d4f 100%); border: none"
              >
                Chọn Mã
              </a-button>
            </div>
          </div>
        </div>
      </div>
    </a-modal>

    <!-- Delete Order Confirm Modal -->
    <a-modal
      v-model:visible="showDeleteConfirmModal"
      title="Xoá Đơn Hàng"
      width="400px"
      @ok="confirmDeleteOrder"
      @cancel="showDeleteConfirmModal = false"
      ok-text="Xoá"
      cancel-text="Hủy"
    >
      <div style="text-align: center; padding: 0">
        <a-result status="warning" title="Xác Nhận Xoá Đơn Hàng?" style="margin: 0; padding: 0" />
        <p style="margin: 4px 0 0 0; color: #666; font-size: 13px">Bạn có chắc muốn xoá đơn hàng này? Hành động này không thể hoàn tác.</p>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import { IconPlus, IconClose, IconDelete, IconQrcode, IconCheck } from '@arco-design/web-vue/es/icon'
import { getBienTheSanPhamPage, type BienTheSanPham } from '@/api/san-pham/bien-the'
import { layDanhSachKhachHang, type KhachHangResponse } from '@/api/khach-hang'
import { fetchCoupons, type CouponApiModel } from '@/api/discount-management'
import { Message } from '@arco-design/web-vue'

// ==================== TYPES ====================
interface CartItem {
  id: string
  productId: string
  productName: string
  price: number
  quantity: number
  image?: string
}

interface Order {
  id: string
  orderCode: string
  items: CartItem[]
  customerId: string | null
  createdAt: Date
}

interface Customer {
  id: string
  name: string
  phone: string
  email?: string
  address?: string
}

// ==================== STATE ====================
const orders = ref<Order[]>([])
const currentOrderIndex = ref('0')
const customers = ref<Customer[]>([])
const productVariants = ref<BienTheSanPham[]>([])
const coupons = ref<CouponApiModel[]>([])

const customerSearchText = ref('')
const showProductModal = ref(false)
const showQRScanner = ref(false)
const showAddCustomerModal = ref(false)
const showVoucherModal = ref(false)
const showDeleteConfirmModal = ref(false)
const deleteConfirmOrderIndex = ref<number | null>(null)
const confirmLoading = ref(false)
const loadingData = ref(false)

const productPagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
})

const voucherPagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
})

const paymentForm = ref({
  discountCode: null as string | null,
  method: 'cash' as 'cash' | 'transfer' | 'card',
  cashReceived: 0,
})

const newCustomerForm = ref({
  name: '',
  phone: '',
  email: '',
  address: '',
})

const cartPagination = ref({
  current: 1,
  pageSize: 5,
})

const breadcrumbItems = ['menu.ban-hang-tai-quay']

// ==================== COMPUTED ====================
const currentOrder = computed(() => {
  const idx = parseInt(currentOrderIndex.value)
  return orders.value[idx] || null
})

const filteredCustomers = computed(() => {
  if (!customerSearchText.value) return customers.value
  const query = customerSearchText.value.toLowerCase()
  return customers.value.filter((c) => c.name.toLowerCase().includes(query) || c.phone.toLowerCase().includes(query))
})

const selectedCustomer = computed(() => {
  if (!currentOrder.value?.customerId) return null
  return customers.value.find((c) => c.id === currentOrder.value?.customerId)
})

const selectedCoupon = computed(() => {
  if (!paymentForm.value?.discountCode) return null
  return coupons.value.find((c) => c.maPhieuGiamGia === paymentForm.value?.discountCode)
})

const paginatedCartItems = computed(() => {
  if (!currentOrder.value) return []
  const start = (cartPagination.value.current - 1) * cartPagination.value.pageSize
  const end = start + cartPagination.value.pageSize
  return currentOrder.value.items.slice(start, end)
})

const subtotal = computed(() => {
  if (!currentOrder.value) return 0
  return currentOrder.value.items.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

const discountAmount = computed(() => {
  const code = paymentForm.value?.discountCode
  if (!code) return 0
  if (code === 'SUMMER10') return subtotal.value * 0.1
  if (code === 'SUMMER20') return subtotal.value * 0.2
  if (code === 'VIP50') return 50000
  return 0
})

const tax = computed(() => {
  return (subtotal.value - discountAmount.value) * 0.1
})

const finalPrice = computed(() => {
  return subtotal.value - discountAmount.value + tax.value
})

const change = computed(() => {
  return Math.max(0, paymentForm.value?.cashReceived - finalPrice.value)
})

const canConfirmOrder = computed(() => {
  return currentOrder.value?.items.length > 0 && finalPrice.value > 0
})

const totalRevenue = computed(() => {
  return orders.value.reduce((sum, order) => {
    const orderSubtotal = order.items.reduce((s, item) => s + item.price * item.quantity, 0)
    const discount = paymentForm.value?.discountCode === 'SUMMER10' ? orderSubtotal * 0.1 : 0
    const tax = (orderSubtotal - discount) * 0.1
    return sum + (orderSubtotal - discount + tax)
  }, 0)
})

const totalItemsSold = computed(() => {
  return orders.value.reduce((sum, order) => sum + order.items.reduce((s, item) => s + item.quantity, 0), 0)
})

// ==================== COLUMNS ====================
const cartColumns = [
  {
    title: 'Sản Phẩm',
    dataIndex: 'productName',
    key: 'productName',
    width: 150,
  },
  {
    title: 'Giá',
    dataIndex: 'price',
    key: 'price',
    slotName: 'price',
    width: 100,
    align: 'right' as const,
  },
  {
    title: 'Số Lượng',
    dataIndex: 'quantity',
    key: 'quantity',
    slotName: 'quantity',
    width: 100,
    align: 'center' as const,
  },
  {
    title: 'Thành Tiền',
    dataIndex: 'subtotal',
    key: 'subtotal',
    slotName: 'subtotal',
    width: 120,
    align: 'right' as const,
  },
  {
    title: 'Hành Động',
    dataIndex: 'action',
    key: 'action',
    slotName: 'action',
    width: 80,
    align: 'center' as const,
  },
]

// ==================== METHODS ====================
const generateOrderCode = (): string => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
  let code = ''
  for (let i = 0; i < 7; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return code
}

const createNewOrder = () => {
  const newOrder: Order = {
    id: `ORDER_${Date.now()}`,
    orderCode: generateOrderCode(),
    items: [],
    customerId: null,
    createdAt: new Date(),
  }
  orders.value.push(newOrder)
  currentOrderIndex.value = (orders.value.length - 1).toString()
}

const deleteOrderByIndex = (index: number) => {
  orders.value.splice(index, 1)
  if (orders.value.length > 0) {
    currentOrderIndex.value = '0'
  }
}

const showDeleteConfirm = (index: number) => {
  deleteConfirmOrderIndex.value = index
  showDeleteConfirmModal.value = true
}

const confirmDeleteOrder = () => {
  if (deleteConfirmOrderIndex.value !== null) {
    deleteOrderByIndex(deleteConfirmOrderIndex.value)
    showDeleteConfirmModal.value = false
    deleteConfirmOrderIndex.value = null
    Message.success('Đơn hàng đã được xoá')
  }
}

const handleOrderChange = (key: string) => {
  currentOrderIndex.value = key
  cartPagination.value.current = 1
}

const updateQuantity = (itemId: string, quantity: number) => {
  if (!currentOrder.value) return
  const item = currentOrder.value.items.find((i) => i.id === itemId)
  if (item) {
    item.quantity = Math.max(1, quantity || 1)
  }
}

const removeFromCart = (itemId: string) => {
  if (!currentOrder.value) return
  const index = currentOrder.value.items.findIndex((i) => i.id === itemId)
  if (index > -1) {
    currentOrder.value.items.splice(index, 1)
  }
}

const clearCart = () => {
  if (currentOrder.value) {
    currentOrder.value.items = []
  }
}

const handleCustomerChange = (customerId: string) => {
  if (currentOrder.value) {
    currentOrder.value.customerId = customerId || null
  }
}

const handleCustomerSearch = () => {
  // Placeholder for search implementation
}

const addNewCustomer = () => {
  if (!newCustomerForm.value?.name || !newCustomerForm.value?.phone) {
    // Show error
    return
  }
  const customer: Customer = {
    id: `CUSTOMER_${Date.now()}`,
    ...newCustomerForm.value,
  }
  customers.value.push(customer)
  if (currentOrder.value) {
    currentOrder.value.customerId = customer.id
  }
  showAddCustomerModal.value = false
  newCustomerForm.value = { name: '', phone: '', email: '', address: '' }
}

const confirmOrder = () => {
  if (!canConfirmOrder.value) return
  confirmLoading.value = true
  setTimeout(() => {
    // API call would go here
    console.log('Order confirmed:', currentOrder.value)
    Message.success('Đơn hàng đã được xác nhận')
    confirmLoading.value = false
  }, 500)
}

const printOrder = () => {
  if (!currentOrder.value?.items.length) return
  // Implement print functionality
  console.log('Printing order:', currentOrder.value)
  Message.info('In hoá đơn thành công')
}

const formatCurrency = (value: number): string => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(value)
}

// ==================== LIFECYCLE ====================
const loadInitialData = async () => {
  loadingData.value = true
  try {
    // Load customers
    const customersResponse = await layDanhSachKhachHang()
    if (customersResponse?.data) {
      customers.value = customersResponse.data.map((c: KhachHangResponse) => ({
        id: c.id.toString(),
        name: c.tenKhachHang,
        phone: c.soDienThoai,
        email: c.email,
        address: c.listDiaChi?.[0]?.diaChiCuThe || '',
      }))
    }

    // Load product variants with pagination
    const productsResponse = await getBienTheSanPhamPage(1, undefined, 10)
    if (productsResponse?.data?.data) {
      productVariants.value = productsResponse.data.data
      productPagination.value.total = productsResponse.data.totalElements || 0
      productPagination.value.current = 1
    }

    // Load coupons
    const couponsResponse = await fetchCoupons()
    if (couponsResponse) {
      coupons.value = couponsResponse
      voucherPagination.value.total = couponsResponse.length
    }

    Message.success('Tải dữ liệu thành công')
  } catch (error) {
    console.error('Error loading data:', error)
    Message.error('Không thể tải dữ liệu')
  } finally {
    loadingData.value = false
  }
}

const loadProductPage = async (page: number) => {
  try {
    const productsResponse = await getBienTheSanPhamPage(page, undefined, 10)
    if (productsResponse?.data?.data) {
      productVariants.value = productsResponse.data.data
      productPagination.value.current = page
      productPagination.value.total = productsResponse.data.totalElements || 0
    }
  } catch (error) {
    console.error('Error loading products:', error)
    Message.error('Không thể tải sản phẩm')
  }
}

const selectVoucher = (coupon: CouponApiModel) => {
  paymentForm.value.discountCode = coupon.maPhieuGiamGia || coupon.id.toString()
  showVoucherModal.value = false
}

onMounted(() => {
  // Initialize with one empty order
  createNewOrder()
  // Load data from API
  loadInitialData()
})
</script>

<style scoped lang="less">
.pos-system {
  padding: 16px;
}

.pos-main {
  margin-bottom: 24px;
}

:deep(.orders-tabs .arco-tabs-tab) {
  gap: 4px;
  margin: 0 1px;
  border: 1px solid black;
  border-bottom: none;
}

.pos-left,
.pos-right {
  :deep(.arco-card) {
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  }
}

.orders-card {
  margin-bottom: 16px;
}

.orders-tabs {
  :deep(.arco-tabs-tab) {
    padding: 8px 16px;
  }
}

.tab-header {
  display: flex;
  align-items: center;
  gap: 8px;

  .tab-close-btn {
    margin-left: 8px;
  }
}

.toolbar {
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 4px;
}

.cart-card {
  margin-bottom: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.cart-wrapper {
  margin-bottom: 16px;
  min-height: 200px;

  :deep(.arco-table) {
    font-size: 13px;
  }
}

.cart-summary {
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;

  p {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
    font-size: 13px;

    strong {
      font-weight: 600;
    }
  }
}

.customer-card {
  margin-bottom: 16px;

  :deep(.arco-form-item) {
    margin-bottom: 12px;
  }
}

.payment-card {
  :deep(.arco-form-item) {
    margin-bottom: 12px;
  }

  :deep(.arco-form-item-label-col) {
    line-height: 28px;
  }
}

.payment-summary {
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;

  .summary-row {
    display: flex;
    justify-content: space-between;
    font-size: 13px;
    margin-bottom: 8px;

    &:last-child {
      margin-bottom: 0;
    }

    &.total {
      border-top: 1px solid #e5e5e5;
      padding-top: 8px;
      font-size: 14px;
      font-weight: 600;
    }
  }
}

.discount-text {
  color: #f5222d;
  font-weight: 600;
}

.final-price {
  color: #52c41a;
  font-size: 16px;
  font-weight: 700;
}

.change-text {
  color: #faad14;
  font-weight: 600;
}

.text-right {
  text-align: right;
}

@media (max-width: 768px) {
  .pos-system {
    padding: 8px;
  }

  .pos-left,
  .pos-right {
    margin-bottom: 16px;
  }

  .orders-tabs {
    :deep(.arco-tabs-tab) {
      padding: 6px 12px;
      font-size: 12px;
    }
  }
}
</style>
