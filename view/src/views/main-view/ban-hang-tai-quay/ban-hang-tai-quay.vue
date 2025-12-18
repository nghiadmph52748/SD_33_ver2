<template>
  <div class="pos-system">
    <!-- Main Layout -->
    <a-card class="main-pos-card">
      <!-- <template #title> -->
      <OrdersTabs
        :orders="orders as any"
        :active-key="currentOrderIndex"
        :can-add="orders.length < 8"
        @change="handleOrderChange"
        @delete="showDeleteConfirm"
        @add="createNewOrder"
      />
      <!-- </template> -->
      <a-row :gutter="16" class="pos-main">
        <!-- Left: Orders & Cart -->
        <a-col :xs="24" :lg="16" class="pos-left">
          <a-empty v-if="orders.length === 0" description="Chưa có đơn hàng nào" />
          <div v-else>
            <OrderHeaderCard
              :order-code="currentOrder?.orderCode || ''"
              :ma-hoa-don="currentOrder?.maHoaDon"
              :has-items="!!currentOrder?.items.length"
              @clear-cart="clearCart"
              @open-qr="showQRScanner = true"
              @open-product="openProductModal"
            >
              <a-card class="cart-card">
                <template #title>Giỏ Hàng</template>
                <CartTable
                  :items="paginatedCartItems as any"
                  :table-key="cartTableKey"
                  :pagination="{
                    current: (cartPagination as any).value?.current || 1,
                    pageSize: (cartPagination as any).value?.pageSize || 5,
                    total: currentOrder?.items.length || 0,
                    showTotal: true,
                    showPageSize: true,
                  }"
                  :over-stock-items="overStockItems"
                  @paginate="(page) => ((cartPagination as any).value.current = page)"
                  @update-quantity="({ id, value }) => updateQuantity(id, value)"
                  @delete="(record: any) => showDeleteProductConfirm(record)"
                />
              </a-card>
            </OrderHeaderCard>
          </div>
        </a-col>

        <!-- Right: Customer & Payment -->
        <a-col :xs="24" :lg="8" class="pos-right">
          <!-- Customer Section -->
          <CustomerCard
            :customer-id="currentOrder?.customerId || ''"
            :customers="filteredCustomers"
            :selected-customer="selectedCustomer"
            :order-type="orderType as any"
            :is-walk-in="isWalkIn"
            :walk-in-name="walkInName"
            :walk-in-email="walkInEmail"
            :walk-in-phone="walkInPhone"
            @update:customerId="updateCustomerId"
            @change:customer="handleCustomerChange"
            @add-new="showAddCustomerModal = true"
            @change:orderType="handleOrderTypeChange"
            @update:walkInName="walkInName = $event"
            @update:walkInEmail="walkInEmail = $event"
            @update:walkInPhone="walkInPhone = $event"
            @update:walkInDeliveryValid="walkInDeliveryValid = $event"
          />
          <!-- Payment Section -->
          <PaymentCard
            :order-type="orderType as any"
            :order-code="currentOrder?.orderCode || ''"
            :payment-form="paymentForm as any"
            :subtotal="subtotal"
            :discount-amount="discountAmount"
            :shipping-fee="shippingFee"
            :final-price="finalPrice"
            :can-confirm-order="canConfirmOrder"
            :confirm-loading="confirmLoading"
            :has-items="hasItems"
            :has-eligible-vouchers="hasEligibleVouchers"
            :eligible-vouchers-count="eligibleVouchersCount"
            :selected-customer="selectedCustomer as any"
            :is-walk-in="isWalkIn"
            :provinces="provinces"
            :walk-in-location="walkInLocation as any"
            :selected-coupon="selectedCoupon as any"
            :discount-display="selectedCoupon ? getDiscountDisplay(selectedCoupon as any) : ''"
            :best-voucher="bestVoucher as any"
            :calculate-voucher-discount="calculateVoucherDiscount as any"
            :qr-session="currentQrSession"
            :qr-syncing="qrSyncing"
            :qr-sync-error="qrSyncError"
            @change:orderType="handleOrderTypeChange"
            @open-voucher="showVoucherModal = true"
            @change:paymentMethod="handlePaymentMethodChange"
            @update:cash="handleCashAmountChange"
            @update:transfer="handleTransferAmountChange"
            @clear-voucher="clearVoucher"
            @confirm-order="confirmOrder"
            @select-best="bestVoucher && selectVoucher(bestVoucher as any)"
            @print="printOrder"
            @update:shippingFee="updateShippingFeeValue"
            @change:province="onWalkInProvinceChange"
            @change:district="onWalkInDistrictChange"
            @update:walkin-address="updateWalkInAddress"
            @update:walkin-ward="updateWalkInWard"
            @open-mobile="openMobileSession"
            @sync-qr="forceSyncQrSession"
            @reset-qr-session="handleResetQrSessionClick"
          />
        </a-col>
      </a-row>
    </a-card>

    <!-- Modals -->
    <!-- Product Selection Modal -->
    <ProductModal
      :visible="showProductModal"
      :search-text="productSearchText"
      :product-filters="productFilters as any"
      :product-material-options="productMaterialOptions"
      :product-sole-options="productSoleOptions"
      :product-manufacturer-options="productManufacturerOptions"
      :product-origin-options="productOriginOptions"
      :product-color-options="productColorOptions"
      :product-size-options="productSizeOptions"
      :filtered-product-variants="filteredProductVariants as any"
      :product-pagination="productPagination as any"
      @update:visible="(v) => (showProductModal = v)"
      @cancel="handleProductModalCancel"
      @update:searchText="(v) => (productSearchText = v)"
      @update-filter="({ key, value }) => ((productFilters as any)[key] = value)"
      @page-change="(page) => loadProductPage(page)"
      @select-product="(record) => showAddProductConfirm(record)"
    />

    <!-- QR Scanner Modal -->
    <QrScannerModal :visible="showQRScanner" @update:visible="(v) => (showQRScanner = v)" />

    <!-- Add Customer Modal -->
    <AddCustomerModal
      :visible="showAddCustomerModal"
      :name="newCustomerForm?.name || ''"
      :phone="newCustomerForm?.phone || ''"
      :email="newCustomerForm?.email || ''"
      @update:visible="(v) => (showAddCustomerModal = v)"
      @update:name="(v) => newCustomerForm && (newCustomerForm.name = v)"
      @update:phone="(v) => newCustomerForm && (newCustomerForm.phone = v)"
      @update:email="(v) => newCustomerForm && (newCustomerForm.email = v)"
      @ok="addNewCustomer"
    />

    <!-- Voucher Selection Modal -->
    <VoucherModal
      :visible="showVoucherModal"
      :coupons="coupons as any"
      :eligible-vouchers-count="eligibleVouchersCount"
      :show-order-summary="!!(currentOrder && currentOrder.items.length > 0)"
      :subtotal="subtotal"
      :total-quantity="currentOrder?.items.reduce((sum, item) => sum + item.quantity, 0) || 0"
      :best-voucher-id="bestVoucher ? bestVoucher.id : null"
      :is-voucher-eligible-fn="isVoucherEligible as any"
      :get-voucher-status-fn="getVoucherStatus as any"
      :get-discount-display-fn="getDiscountDisplay as any"
      @update:visible="(v) => (showVoucherModal = v)"
      @select-voucher="(c) => selectVoucher(c as any)"
    />

    <!-- Delete Product Confirm Modal -->
    <DeleteProductModal
      :visible="showDeleteProductModal"
      :product="productToDelete as any"
      @update:visible="(v) => (showDeleteProductModal = v)"
      @ok="confirmDeleteProduct"
    />

    <!-- Delete Order Confirm Modal -->
    <DeleteOrderModal :visible="showDeleteConfirmModal" @update:visible="(v) => (showDeleteConfirmModal = v)" @ok="confirmDeleteOrder" />

    <!-- Price Change Notification Modal -->
    <PriceChangeNotificationModal
      :visible="showPriceChangeModal"
      :price-changes="priceChanges"
      :confirm-loading="priceChangeConfirmLoading"
      @update:visible="(v) => (showPriceChangeModal = v)"
      @confirm="confirmPriceChangeModal"
      @cancel="() => (showPriceChangeModal = false)"
    />

    <!-- Add Product Confirm Modal -->
    <AddProductConfirmModal
      :visible="showAddProductConfirmModal"
      :product="selectedProductForAdd as any"
      :quantity="productQuantityInput"
      :is-quantity-valid="isQuantityValid"
      :confirm-loading="addProductConfirmLoading"
      @update:visible="(v) => (showAddProductConfirmModal = v)"
      @update:quantity="handleQuantityChange"
      @ok="confirmAddProduct"
    />

    <!-- Confirm Order Modal -->
    <ConfirmOrderModal
      :visible="showConfirmOrderModal"
      :order-code="currentOrder?.orderCode || ''"
      :order-type="orderType as any"
      :customer-name="selectedCustomer?.name || walkInName || 'Khách lẻ'"
      :customer-phone="selectedCustomer?.phone || walkInPhone || ''"
      :customer-address="getCustomerAddress()"
      :items="currentOrder?.items || []"
      :subtotal="subtotal"
      :discount-amount="discountAmount"
      :shipping-fee="shippingFee"
      :final-price="finalPrice"
      :payment-method="paymentForm.method"
      :cash-received="paymentForm.cashReceived"
      :transfer-received="paymentForm.transferReceived"
      :selected-coupon="selectedCoupon as any"
      :confirm-loading="confirmLoading"
      @cancel="cancelConfirmOrder"
      @confirm="handleConfirmOrderFromModal"
    />

    <!-- Confirm Order - Better Voucher Modal -->
    <ConfirmBetterVoucherModal
      :visible="showBetterVoucherModal"
      :suggested-better-vouchers="checkBetterVouchers()"
      :selected-coupon="selectedCoupon as any"
      :calculate-voucher-discount="calculateVoucherDiscount as any"
      :confirm-loading="confirmLoading"
      @cancel="
        () => {
          showBetterVoucherModal = false
          showConfirmOrderModal = true
        }
      "
      @confirm="handleConfirmFromBetterVoucher"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import {
  IconPlus,
  IconClose,
  IconDelete,
  IconQrcode,
  IconCheck,
  IconInfoCircle,
  IconExclamationCircle,
  IconRight,
  IconSwap,
  IconGift,
} from '@arco-design/web-vue/es/icon'
import { type BienTheSanPham, type ChatLieu, type DeGiay, type MauSac, type KichThuoc } from '@/api/san-pham/bien-the'
import { type CouponApiModel } from '@/api/discount-management'
import { themKhachHangNhanh, type KhachHangResponse } from '@/api/khach-hang'
import { Message, Modal } from '@arco-design/web-vue'
import { useUserStore } from '@/store'
import type { CreateQrSessionPayload } from '@/api/pos'
import {
  createInvoice,
  deleteInvoice,
  addProductToInvoice,
  updateProductQuantityInInvoice,
  deleteProductsFromInvoice,
  confirmPosOrder,
  getPosActiveCoupons,
  getPosActiveCouponsForCustomer,
  getPendingInvoices,
} from './services/posService'
import type { PendingInvoiceResponse } from './services/posService'
import { calculateShippingFee, calculateShippingFeeFromGHN } from './services/shippingFeeService'
import useVoucher from './composables/useVoucher'
import { useQrScanner } from './composables/useQrScanner'
import { useCart } from './composables/useCart'
import { useProductModal } from './composables/useProductModal'
import useCustomer from './composables/useCustomer'
import usePayment from './composables/usePayment'
import useCheckout from './composables/useCheckout'
import useCartActions from './composables/useCartActions'
import { useOrdersManager } from './composables/useOrdersManager'
import { useStock } from './composables/useStock'
import useRealTimePriceSync from './composables/useRealTimePriceSync'
import { createQrPaymentSession, updateQrPaymentSession, cancelQrPaymentSession, generateQrPayment } from './services/qrSessionService'
import CartTable from './components/CartTable.vue'
import CustomerCard from './components/CustomerCard.vue'
import PaymentCard from './components/PaymentCard.vue'
import ProductModal from './components/ProductModal.vue'
import VoucherModal from './components/VoucherModal.vue'
import QrScannerModal from './components/QrScannerModal.vue'
import AddCustomerModal from './components/AddCustomerModal.vue'
import DeleteProductModal from './components/DeleteProductModal.vue'
import DeleteOrderModal from './components/DeleteOrderModal.vue'
import ConfirmOrderModal from './components/ConfirmOrderModal.vue'
import ConfirmBetterVoucherModal from './components/ConfirmBetterVoucherModal.vue'
import AddProductConfirmModal from './components/AddProductConfirmModal.vue'
import PriceChangeNotificationModal from './components/PriceChangeNotificationModal.vue'
import OrdersTabs from './components/OrdersTabs.vue'
import OrderHeaderCard from './components/OrderHeaderCard.vue'
// ==================== TYPES ====================
interface CartItem {
  id: string
  idHoaDonChiTiets?: number[] // Server-side IDs for all invoice detail entries (array because each add creates new entry)
  productId: string
  productName: string
  price: number
  discount: number
  quantity: number
  image?: string
  images?: string[]
  // Thông tin chi tiết sản phẩm
  tenChiTietSanPham?: string
  tenMauSac?: string
  maMau?: string
  tenKichThuoc?: string
  tenDeGiay?: string
  tenChatLieu?: string
}

interface Order {
  id: string
  orderCode: string
  maHoaDon?: string
  items: CartItem[]
  customerId: string | null
  createdAt: Date
}

interface QrSessionState {
  sessionId: string
  status: string
  qrCodeUrl?: string
  expiresAt?: string
  lastPayloadHash: string
  updatedAt: number
}

interface Customer {
  id: string
  name: string
  phone: string
  email?: string
  address?: string
}

type PaymentMethod = 'cash' | 'transfer' | 'both'

interface OrderUiState {
  paymentForm: {
    discountCode: string | null
    method: PaymentMethod
    cashReceived: number | null
    transferReceived: number | null
  }
  orderType: 'counter' | 'delivery'
  shippingFee: number
  walkInContact: {
    name: string
    email: string
    phone: string
    deliveryValid: boolean
  }
  walkInLocation: {
    thanhPho: string
    quan: string
    phuong: string
    diaChiCuThe: string
    districts: Array<{ value: string; label: string; code?: number }>
    wards: Array<{ value: string; label: string }>
  }
  lastPaymentField: 'cash' | 'transfer'
}

// ==================== STATE ====================
const userStoreInstance = useUserStore()
const orders = ref<Order[]>([])
const currentOrderIndex = ref('0')
const allProductVariants = ref<BienTheSanPham[]>([])
const coupons = ref<CouponApiModel[]>([])
// Track số lượng đã bán của mỗi sản phẩm (để tính toán lại tồn kho khi reload)
const soldQuantitiesByProductId = ref<Record<string | number, number>>({})

// Cache dữ liệu để chỉ reload khi có thay đổi
const cachedProducts = ref<Map<number, number>>(new Map()) // productId -> soLuong
const cachedCoupons = ref<string>('') // JSON string of coupon data for comparison
const cachedCustomerCoupons = ref<string>('') // Cache for customer-specific vouchers

const showVoucherModal = ref(false)

const qrSessions = ref<Record<string, QrSessionState>>({})
const qrSyncing = ref(false)
const qrSyncError = ref<string | null>(null)
const walkInName = ref('')
const walkInEmail = ref('')
const walkInPhone = ref('')
const walkInDeliveryValid = ref(false)
// Track pending product operations (add/update) to prevent race conditions with voucher application
const pendingProductOperations = ref(0)
const QR_SYNC_DEBOUNCE_MS = 200
let qrSyncTimeout: number | null = null

// Confirm order modal state - moved to useCheckout

// Throttle state for API calls to avoid excessive requests
let lastVoucherRefreshTime = 0
let lastStockRefreshTime = 0
const VOUCHER_THROTTLE_MS = 2000 // 2 seconds throttle between voucher refresh calls
const STOCK_THROTTLE_MS = 1000 // 1 second throttle between stock refresh calls

const loadingData = ref(false)

// Broadcast channel for real-time sync between tabs/windows
let stockBroadcastChannel: BroadcastChannel | null = null

const voucherPagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
})

// Force re-render key cho cart table khi có lỗi cập nhật quantity

const breadcrumbItems = []

// ==================== COMPUTED ====================
const currentOrder = computed(() => {
  const idx = parseInt(currentOrderIndex.value, 10)
  return orders.value[idx] || null
})

const currentQrSession = computed(() => {
  const orderId = currentOrder.value?.id
  if (!orderId) return null
  return qrSessions.value[orderId] ?? null
})

// Customer state (used across payment, voucher, checkout)
const {
  customers,
  customerSearchText,
  filteredCustomers,
  selectedCustomer,
  showAddCustomerModal,
  newCustomerForm,
  updateCustomerId,
  loadCustomers,
  updateInvoiceCustomer,
} = useCustomer({ currentOrder })

// Define subtotal BEFORE using it in useVoucher
const subtotal = computed(() => {
  if (!currentOrder.value) return 0
  return currentOrder.value.items.reduce((sum, cartItem) => {
    const discountedPrice = cartItem.discount > 0 ? cartItem.price * (1 - cartItem.discount / 100) : cartItem.price
    return sum + discountedPrice * cartItem.quantity
  }, 0)
})

// Computed property to check if current customer is walk-in
const isWalkIn = computed(() => {
  const customerId = currentOrder.value?.customerId
  // Walk-in customer: no selected customer AND customerId is either null or empty string
  return !selectedCustomer.value && (customerId === '' || customerId === null || customerId === undefined)
})

// Initialize payment before voucher to avoid TDZ
const {
  paymentForm,
  orderType,
  shippingFee,
  provinces,
  walkInLocation,
  loadProvinces,
  onWalkInProvinceChange,
  onWalkInDistrictChange,
  fillWalkInLocationFromCustomer,
  handleCashAmountChange: assignCashAmount,
  handleTransferAmountChange: assignTransferAmount,
  clearVoucher,
  updateInvoicePayment,
  updateInvoiceShipping,
  updateInvoiceVoucher,
  recalculateShippingFee,
} = usePayment({ currentOrder })

const orderUiStateCache = ref<Record<string, OrderUiState>>({})
const isRestoringOrderState = ref(false)
type WalkInLocationState = typeof walkInLocation.value
const shippingFeeAutoCalcLocks = new Set<string>()
const lastAutoShippingSignature = new Map<string, string>()
let autoShippingFeeTimeout: ReturnType<typeof setTimeout> | null = null

const hasCompleteWalkInAddress = (location: WalkInLocationState) =>
  Boolean(location.thanhPho && location.quan && location.phuong && location.diaChiCuThe)

const buildLocationSignature = (location: WalkInLocationState) =>
  ['thanhPho', 'quan', 'phuong', 'diaChiCuThe']
    .map((key) => (location[key as keyof WalkInLocationState] || '').toString().trim().toLowerCase())
    .join('|')

const autoCalculateShippingFeeForCurrentOrder = async () => {
  const orderId = currentOrder.value?.id
  if (!orderId) return
  if (orderType.value !== 'delivery') return
  const locationSnapshot = { ...walkInLocation.value }
  if (!hasCompleteWalkInAddress(locationSnapshot)) return

  const cachedState = orderUiStateCache.value[orderId]
  const existingFee = normalizeNumber(shippingFee.value ?? cachedState?.shippingFee ?? 0, 0)
  if (existingFee > 0) {
    if (cachedState) cachedState.shippingFee = existingFee
    lastAutoShippingSignature.set(orderId, buildLocationSignature(locationSnapshot))
    return
  }

  if (shippingFeeAutoCalcLocks.has(orderId)) return

  const signature = buildLocationSignature(locationSnapshot)
  const previousSignature = lastAutoShippingSignature.get(orderId)
  if (previousSignature === signature && existingFee === 0) {
    return
  }
  shippingFeeAutoCalcLocks.add(orderId)
  try {
    const calculatedFee = await recalculateShippingFee()
    const normalizedFee = normalizeNumber(calculatedFee ?? shippingFee.value ?? 0, 0)
    const currentOrderId = currentOrder.value?.id
    const currentSignature = buildLocationSignature(walkInLocation.value)
    if (!currentOrderId || currentOrderId !== orderId) return
    if (currentSignature !== signature) {
      return
    }
    shippingFee.value = normalizedFee
    if (cachedState) {
      cachedState.shippingFee = normalizedFee
      cachedState.orderType = 'delivery'
    }
    lastAutoShippingSignature.set(orderId, signature)
  } catch (error) {
    console.warn('[POS] Auto shipping fee calculation failed:', error)
  } finally {
    shippingFeeAutoCalcLocks.delete(orderId)
  }
}

const scheduleAutoShippingFeeCalculation = () => {
  if (autoShippingFeeTimeout) {
    clearTimeout(autoShippingFeeTimeout)
  }
  autoShippingFeeTimeout = setTimeout(() => {
    autoShippingFeeTimeout = null
    void autoCalculateShippingFeeForCurrentOrder()
  }, 300)
}

// Cache for last confirmed order (for printing after checkout)
const lastConfirmedOrder = ref<{
  order: Order
  customer: Customer | null
  voucher: CouponApiModel | null
  orderType: 'counter' | 'delivery'
  shippingFee: number
  walkInData: {
    name: string
    email: string
    phone: string
    location: any
  }
  confirmedAt: Date
} | null>(null)

const {
  selectedCoupon,
  eligibleVouchersCount,
  hasEligibleVouchers,
  bestVoucher,
  calculateVoucherDiscount,
  getVoucherStatus,
  getDiscountDisplay,
  isVoucherEligible,
} = useVoucher({ coupons, paymentForm, currentOrder, subtotal })

const { cartPagination, cartTableKey, increaseCartTableKey, cartColumns, paginatedCartItems } = useCart({ currentOrder })

const {
  showAddProductConfirmModal,
  selectedProductForAdd,
  productQuantityInput,
  isQuantityValid,
  quantityInputRef,
  showDeleteProductModal,
  addProductConfirmLoading,
  productToDelete,
  showAddProductConfirm,
  handleQuantityChange,
  confirmAddProduct,
  showDeleteProductConfirm,
  confirmDeleteProduct,
  updateQuantity,
  removeFromCart,
  clearCart,
} = useCartActions({
  currentOrder,
  allProductVariants,
  cartTableKey,
  userId: userStoreInstance.id || 1,
  refreshProductStock,
  pendingProductOperations,
})

const { showDeleteConfirmModal, showDeleteConfirm, confirmDeleteOrder, createNewOrder, handleOrderChange } = useOrdersManager({
  orders,
  currentOrderIndex,
  allProductVariants,
  soldQuantitiesByProductId,
  cartPagination,
  userId: userStoreInstance.id || 1,
})

const { insufficientStockItems, overStockItems } = useStock({ currentOrder, allProductVariants })

const { showPriceChangeModal, priceChanges, priceChangeConfirmLoading, updateCartPricesFromServer, confirmPriceChangeModal } =
  useRealTimePriceSync({
    currentOrder,
    allProductVariants,
    increaseCartTableKey,
  })

// Watch for product price changes and update cart prices in real-time
watch(
  () => allProductVariants.value.map((p) => `${p.id}_${p.giaBan}_${p.giaTriGiamGia}`),
  () => {
    updateCartPricesFromServer()
  }
)

const discountAmount = computed(() => {
  if (!selectedCoupon.value || !currentOrder.value) return 0
  return calculateVoucherDiscount(selectedCoupon.value)
})

const totalRevenue = computed(() => {
  return orders.value.reduce((sum, order) => {
    const orderSubtotal = order.items.reduce((subtotal, item) => subtotal + item.price * item.quantity, 0)
    const discount = paymentForm.value?.discountCode === 'SUMMER10' ? orderSubtotal * 0.1 : 0
    return sum + (orderSubtotal - discount)
  }, 0)
})

const totalItemsSold = computed(() => {
  return orders.value.reduce((sum, order) => sum + order.items.reduce((subtotal, item) => subtotal + item.quantity, 0), 0)
})

// Check if we have items to print (either current order or recently confirmed order)
const hasItems = computed(() => {
  // Check if current order has items
  if (currentOrder.value?.items?.length && currentOrder.value.items.length > 0) {
    return true
  }
  // Or check if we have a recently confirmed order (within last 5 minutes)
  if (lastConfirmedOrder.value) {
    const fiveMinutesAgo = new Date(Date.now() - 5 * 60 * 1000)
    return lastConfirmedOrder.value.confirmedAt > fiveMinutesAgo
  }
  return false
})

// ==================== METHODS ====================

const handleCustomerChange = async (customerId: string) => {
  try {
    if (!currentOrder.value) {
      Message.error('Không có đơn hàng được chọn')
      return
    }

    const invoiceId = parseInt(currentOrder.value.id)
    if (isNaN(invoiceId)) {
      Message.error('ID hóa đơn không hợp lệ')
      return
    }

    // Wait for Vue to update the DOM and computed properties after updateCustomerId was called
    await nextTick()

    // Fill walk-in location from selected customer address (await for async loading of districts/wards)
    await fillWalkInLocationFromCustomer(selectedCustomer.value)

    // Wait a moment for Vue to process the location updates
    await nextTick()

    // Call API to update customer - pass walkInLocation for walk-in customers
    await updateInvoiceCustomer(invoiceId, walkInLocation, {
      name: walkInName.value,
      phone: walkInPhone.value,
      email: walkInEmail.value,
    })

    Message.success('Khách hàng đã được cập nhật')
  } catch (error: any) {
    Message.error(error.message || 'Có lỗi xảy ra khi cập nhật khách hàng')
  }
}

const addNewCustomer = async () => {
  try {
    if (!newCustomerForm.value?.name || !newCustomerForm.value?.phone || !newCustomerForm.value?.email) {
      Message.error('Vui lòng nhập tên, số điện thoại và email khách hàng')
      return
    }

    const phoneNumber = newCustomerForm.value.phone

    // Gọi API để thêm khách hàng mới
    const response = await themKhachHangNhanh({
      tenKhachHang: newCustomerForm.value.name,
      soDienThoai: newCustomerForm.value.phone,
      email: newCustomerForm.value.email || '',
      tenTaiKhoan: newCustomerForm.value.email.split('@')[0],
      trangThai: true,
      deleted: false,
    } as any)

    // Reload danh sách khách hàng
    await loadCustomers()

    // Tìm và chọn khách hàng vừa thêm theo số điện thoại
    const newCustomer = customers.value.find((c) => c.phone === phoneNumber)
    if (newCustomer && currentOrder.value) {
      currentOrder.value.customerId = newCustomer.id
      const invoiceId = parseInt(currentOrder.value.id)
      if (!isNaN(invoiceId)) {
        await updateInvoiceCustomer(invoiceId, walkInLocation, {
          name: walkInName.value,
          phone: walkInPhone.value,
          email: walkInEmail.value,
        })
      }
    }

    showAddCustomerModal.value = false
    newCustomerForm.value = { name: '', phone: '', email: '' }
    Message.success('Thêm khách hàng thành công')
  } catch (error: any) {
    console.error('Lỗi thêm khách hàng:', error)
    Message.error(error.message || 'Có lỗi xảy ra khi thêm khách hàng')
  }
}

// checkBetterVouchers moved to useCheckout

// cancelConfirmOrder moved to useCheckout

const printOrder = () => {
  // Check if we have a current order with items OR a recently confirmed order
  const hasCurrentOrder = currentOrder.value?.items?.length && currentOrder.value.items.length > 0
  const hasConfirmedOrder = lastConfirmedOrder.value !== null

  if (!hasCurrentOrder && !hasConfirmedOrder) {
    Message.warning('Vui lòng thêm sản phẩm vào giỏ hàng trước khi in hóa đơn')
    return
  }

  try {
    // Generate invoice HTML - pass cached data if current order is empty
    const invoiceHTML = hasCurrentOrder
      ? generateInvoiceHTML()
      : generateInvoiceHTML(lastConfirmedOrder.value)

    // Open print window
    const printWindow = window.open('', '_blank', 'width=800,height=600')
    if (!printWindow) {
      Message.error('Không thể mở cửa sổ in. Vui lòng kiểm tra trình chặn popup.')
      return
    }

    // Write HTML to new window
    printWindow.document.write(invoiceHTML)
    printWindow.document.close()

    // Wait for content to load, then print
    printWindow.onload = () => {
      printWindow.focus()
      printWindow.print()
      // Close window after printing (user can cancel)
      printWindow.onafterprint = () => {
        printWindow.close()
      }
    }

    Message.success('Đang mở cửa sổ in hóa đơn...')
  } catch (error: any) {
    console.error('Lỗi khi in hóa đơn:', error)
    Message.error('Có lỗi xảy ra khi in hóa đơn')
  }
}

// Helper function to generate invoice HTML from template
const generateInvoiceHTML = (cachedData?: typeof lastConfirmedOrder.value): string => {
  // Use cached data if provided, otherwise use current state
  const order = cachedData?.order || currentOrder.value
  if (!order) return ''

  // Get customer info (from cache or current)
  const customer = cachedData?.customer || selectedCustomer.value
  const walkIn = cachedData?.walkInData || {
    name: walkInName.value,
    email: walkInEmail.value,
    phone: walkInPhone.value,
    location: walkInLocation.value,
  }
  const customerName = (customer as any)?.tenKhachHang || customer?.name || walkIn.name || 'Khách lẻ'
  const customerPhone = (customer as any)?.soDienThoai || customer?.phone || walkIn.phone || 'N/A'
  const customerEmail = selectedCustomer.value?.email || walkInEmail.value || 'N/A'

  // Get address for delivery orders (from cache or current)
  let customerAddress = 'N/A'
  const currentOrderType = cachedData?.orderType || orderType.value
  if (currentOrderType === 'delivery') {
    const loc = walkInLocation.value
    const parts = [loc.diaChiCuThe, loc.phuong, loc.quan, loc.thanhPho].filter(Boolean)
    customerAddress = parts.length > 0 ? parts.join(', ') : 'N/A'
  }

  // Format dates
  const printTime = new Date() // Always use current time when printing
  const formatDate = (date: Date) => {
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const year = date.getFullYear()
    return `${hours}:${minutes} ${day}/${month}/${year}`
  }

  // Generate invoice code
  const invoiceCode = order.id ? `HD${String(order.id).padStart(6, '0')}` : 'DRAFT'

  // Format currency
  const formatMoney = (amount: number) => {
    return new Intl.NumberFormat('vi-VN').format(amount) + ' ₫'
  }

  // Calculate totals (from cache or current)
  const currentVoucher = cachedData?.voucher || selectedCoupon.value
  const subtotalAmount = cachedData ? cachedData.order.items.reduce((sum, item) => sum + item.price * item.quantity, 0) : subtotal.value
  const discountAmount = currentVoucher ? calculateVoucherDiscount(currentVoucher) : 0
  const shippingAmount = cachedData?.shippingFee || (orderType.value === 'delivery' ? shippingFee.value : 0)
  const finalAmount = subtotalAmount - discountAmount + shippingAmount

  // Generate product rows
  const productRows = order.items
    .map((item: any, index: number) => {
      const attrs = []
      if (item.mauSac) attrs.push(`Màu: ${item.mauSac}`)
      if (item.kichThuoc) attrs.push(`Size: ${item.kichThuoc}`)
      if (item.chatLieu) attrs.push(`Chất liệu: ${item.chatLieu}`)

      const attrsHTML = attrs.length > 0 ? attrs.map((attr) => `<div>${attr}</div>`).join('') : '<div>-</div>'

      const itemTotal = item.price * item.quantity

      return `
          <tr>
            <td class="text-center">${index + 1}</td>
            <td class="product-name">${item.productName || item.tenSanPham || 'Sản phẩm'}</td>
            <td class="product-attrs">${attrsHTML}</td>
            <td class="text-center">${item.quantity}</td>
            <td class="text-right">${formatMoney(item.price)}</td>
            <td class="text-right total">${formatMoney(itemTotal)}</td>
          </tr>`
    })
    .join('')

  // Get voucher info (from cache or current)
  const voucherInfo = currentVoucher
    ? `<tr>
         <td class="label">Mã giảm giá (${currentVoucher.maPhieuGiamGia}):</td>
         <td class="value discount">-${formatMoney(discountAmount)}</td>
       </tr>`
    : `<tr>
         <td class="label">Giảm giá:</td>
         <td class="value discount">-0 ₫</td>
       </tr>`

  // Shipping fee row (only for delivery)
  const shippingRow =
    currentOrderType === 'delivery'
      ? `<tr>
         <td class="label">Phí vận chuyển:</td>
         <td class="value">${formatMoney(shippingAmount)}</td>
       </tr>`
      : ''

  // Payment status - always show as paid when printing invoice
  const paymentStatus = 'Đã thanh toán'
  const statusClass = 'status-paid'

  // Generate complete HTML
  return `<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8" />
  <title>Biên lai bán hàng - ${invoiceCode}</title>
  <style>
    @page { size: A4; margin: 10mm; }
    * { box-sizing: border-box; }
    html, body { margin: 0; padding: 0; font-family: Arial, sans-serif; font-size: 12px; line-height: 1.5; background: #fff; color: #333; }
    .invoice-page { max-width: 190mm; min-height: calc(297mm - 20mm); margin: 0 auto; display: flex; flex-direction: column; }
    .invoice-header { display: flex; justify-content: space-between; align-items: flex-start; padding-bottom: 10px; border-bottom: 2px solid #2c3e50; margin-bottom: 12px; }
    .company-name { display: block; max-width: 30mm; height: auto; margin: 0 0 4px 0; }
    .company-subtitle { margin: 0 0 6px 0; font-size: 11px; color: #555; }
    .company-details p { margin: 0; }
    .invoice-title { text-align: right; }
    .invoice-title h2 { margin: 0 0 6px 0; font-size: 16px; text-transform: uppercase; }
    .invoice-title p { margin: 0; }
    h3.section-title { margin: 0 0 6px 0; padding-bottom: 4px; border-bottom: 1px solid #3498db; font-size: 13px; text-transform: uppercase; color: #2c3e50; }
    .section { margin-bottom: 14px; }
    .info-grid { display: table; width: 100%; table-layout: fixed; }
    .info-column { display: table-cell; vertical-align: top; padding-right: 8px; }
    .info-column:last-child { padding-right: 0; padding-left: 8px; }
    .info-column h4 { margin: 0 0 4px 0; font-size: 12px; font-weight: bold; }
    .info-item { display: flex; justify-content: space-between; margin-bottom: 2px; }
    .info-item .label { color: #555; min-width: 70px; }
    .info-item .value { font-weight: 600; text-align: right; }
    .status-paid { color: #27ae60; }
    table { width: 100%; border-collapse: collapse; font-size: 12px; }
    th, td { border: 1px solid #bdc3c7; padding: 6px 5px; }
    th { background: #34495e; color: #fff; text-align: center; }
    .text-center { text-align: center; }
    .text-right { text-align: right; }
    .product-name { font-weight: 600; }
    .product-attrs { font-size: 10px; color: #555; }
    .product-attrs div { margin: 1px 0; }
    .total { font-weight: bold; color: #e74c3c; }
    .summary-table { width: 60%; margin-left: auto; margin-top: 6px; }
    .summary-table td { border: none; padding: 3px 2px; }
    .summary-table .label { text-align: left; }
    .summary-table .value { text-align: right; font-weight: 600; }
    .summary-table .discount { color: #e74c3c; }
    .summary-table .row-total td { border-top: 1px solid #2c3e50; border-bottom: 1px solid #2c3e50; padding-top: 4px; padding-bottom: 4px; font-size: 12px; }
    .footer { display: flex; justify-content: space-between; margin-top: auto; padding-top: 10px; }
    .thank-you p { margin: 0; }
    .signature { text-align: center; min-width: 120px; }
    .signature-line { width: 80px; height: 1px; background: #333; margin: 18px auto 6px; }
    .signature-name { font-weight: bold; }
    @media print { body { margin: 0; } }
  </style>
</head>
<body>
  <div class="invoice-page">
    <div class="invoice-header">
      <div>
        <img src="/src/assets/logo-datn.png" alt="GearUp" class="company-name" />
        <p class="company-subtitle">Cửa hàng giày thể thao</p>
        <div class="company-details">
          <p>Địa chỉ: FPT polytechnic, Hà Nội</p>
          <p>Hotline: 0332 050 542</p>
          <p>Email: truongtqph50260@gearup.com</p>
        </div>
      </div>
      <div class="invoice-title">
        <h2>Biên lai bán hàng</h2>
        <p><strong>Mã hóa đơn:</strong> ${invoiceCode}</p>
        <p><strong>Ngày tạo:</strong> ${formatDate(printTime)}</p>
      </div>
    </div>

    <div class="section">
      <h3 class="section-title">Thông tin đơn hàng</h3>
      <div class="info-grid">
        <div class="info-column">
          <h4>Thông tin khách hàng</h4>
          <div class="info-item">
            <span class="label">Tên khách hàng:</span>
            <span class="value">${customerName}</span>
          </div>
          <div class="info-item">
            <span class="label">Số điện thoại:</span>
            <span class="value">${customerPhone}</span>
          </div>
          <div class="info-item">
            <span class="label">Email:</span>
            <span class="value">${customerEmail}</span>
          </div>
          <div class="info-item">
            <span class="label">Địa chỉ:</span>
            <span class="value">${customerAddress}</span>
          </div>
        </div>
        <div class="info-column">
          <h4>Thông tin nhân viên</h4>
          <div class="info-item">
            <span class="label">Tên nhân viên:</span>
            <span class="value">${useUserStore().name || 'N/A'}</span>
          </div>
          <div class="info-item">
            <span class="label">Ngày thanh toán:</span>
            <span class="value">${formatDate(printTime)}</span>
          </div>
          <div class="info-item">
            <span class="label">Trạng thái:</span>
            <span class="value ${statusClass}">${paymentStatus}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="section">
      <h3 class="section-title">Danh sách sản phẩm đã bán</h3>
      <table>
        <thead>
          <tr>
            <th>STT</th>
            <th>Tên sản phẩm</th>
            <th>Thuộc tính</th>
            <th>Số lượng</th>
            <th>Đơn giá</th>
            <th>Thành tiền</th>
          </tr>
        </thead>
        <tbody>
          ${productRows}
        </tbody>
      </table>
    </div>

    <div class="section">
      <h3 class="section-title">Tổng kết đơn hàng</h3>
      <table class="summary-table">
        <tbody>
          <tr>
            <td class="label">Tổng tiền hàng:</td>
            <td class="value">${formatMoney(subtotalAmount)}</td>
          </tr>
          ${voucherInfo}
          ${shippingRow}
          <tr class="row-total">
            <td class="label"><strong>THÀNH TIỀN:</strong></td>
            <td class="value"><strong>${formatMoney(finalAmount)}</strong></td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="footer">
      <div class="thank-you">
        <p><strong>Cảm ơn quý khách đã mua hàng!</strong></p>
        <p>Hẹn gặp lại quý khách lần sau.</p>
      </div>
      <div class="signature">
        <p>Người bán</p>
        <div class="signature-line"></div>
        <p class="signature-name">${useUserStore().name || 'Nhân viên'}</p>
      </div>
    </div>
  </div>
</body>
</html>`
}

// ==================== LIFECYCLE ====================

const selectVoucher = async (coupon: CouponApiModel) => {
  try {
    // Only allow selection if voucher is eligible
    if (!isVoucherEligible(coupon)) {
      Message.warning('Voucher này không đủ điều kiện áp dụng cho đơn hàng hiện tại')
      return
    }

    if (!currentOrder.value) {
      Message.error('Không có đơn hàng được chọn')
      return
    }

    // Check if cart has products - IMPORTANT: validate before API call
    if (!currentOrder.value.items || currentOrder.value.items.length === 0) {
      Message.error('Vui lòng thêm sản phẩm vào giỏ hàng trước khi áp dụng voucher')
      return
    }

    // Additional validation: ensure cart has valid items with quantity > 0
    const validItems = currentOrder.value.items.filter((item: CartItem) => item.quantity > 0)
    if (validItems.length === 0) {
      Message.error('Giỏ hàng không có sản phẩm hợp lệ. Vui lòng thêm sản phẩm trước khi áp dụng voucher')
      return
    }

    // IMPORTANT: Wait for any pending product operations to complete before applying voucher
    // This prevents race conditions where voucher is applied before products are fully saved to database
    console.log('[selectVoucher] Pending operations:', pendingProductOperations.value)
    if (pendingProductOperations.value > 0) {
      Message.info('Đang xử lý sản phẩm, vui lòng đợi...')
      // Wait up to 3 seconds for pending operations to complete
      let waitTime = 0
      const maxWaitTime = 3000 // 3 seconds
      const checkInterval = 100 // Check every 100ms

      while (pendingProductOperations.value > 0 && waitTime < maxWaitTime) {
        await new Promise((resolve) => setTimeout(resolve, checkInterval))
        waitTime += checkInterval
        console.log('[selectVoucher] Still waiting, pending:', pendingProductOperations.value, 'waitTime:', waitTime)
      }

      if (pendingProductOperations.value > 0) {
        console.error('[selectVoucher] Timeout waiting for pending operations')
        Message.warning('Sản phẩm đang được xử lý. Vui lòng thử lại sau vài giây.')
        return
      }
    }

    // Add additional safety delay to ensure database transaction is committed
    // Even after API returns, the database transaction might not be fully committed
    console.log('[selectVoucher] Adding safety delay before voucher application')
    await new Promise((resolve) => setTimeout(resolve, 300))

    const invoiceId = parseInt(currentOrder.value.id)
    const voucherId = coupon.id

    console.log('[selectVoucher] Applying voucher. InvoiceId:', invoiceId, 'VoucherId:', voucherId)
    // Call API to update voucher
    await updateInvoiceVoucher(invoiceId, voucherId)

    // Broadcast coupon update to other tabs/pages
    try {
      const couponBroadcastChannel = new BroadcastChannel('coupon-update-channel')
      couponBroadcastChannel.postMessage({
        type: 'COUPON_UPDATED',
        voucherId,
        couponCode: coupon.maPhieuGiamGia,
        timestamp: new Date().toISOString(),
      })
      couponBroadcastChannel.close()
    } catch (error) {
      console.warn('BroadcastChannel broadcast failed:', error)
    }

    // Update local state
    paymentForm.value.discountCode = coupon.maPhieuGiamGia || coupon.id.toString()

    // Auto-set default payment amount to final price after voucher discount
    // This way user only needs to confirm payment without recalculating
    if (paymentForm.value.method === 'cash') {
      assignCashAmount(finalPrice.value)
      assignTransferAmount(0)
      lastPaymentFieldChanged.value = 'cash'
    } else if (paymentForm.value.method === 'transfer') {
      assignTransferAmount(finalPrice.value)
      assignCashAmount(0)
      lastPaymentFieldChanged.value = 'transfer'
    } else if (paymentForm.value.method === 'both') {
      // Keep current split for mixed payments; user will re-enter if needed
      syncPaymentSplit(lastPaymentFieldChanged.value)
    }

    showVoucherModal.value = false
    Message.success(`Đã áp dụng voucher: ${coupon.tenPhieuGiamGia}`)
  } catch (error: any) {
    console.error('Lỗi áp dụng voucher:', error)
    Message.error(error.message || 'Có lỗi xảy ra khi áp dụng voucher')
  }
}

const selectPaymentMethod = (method: 'cash' | 'transfer' | 'both') => {
  paymentForm.value.method = method
}

// Derived payment computeds (defined here so watchers below can safely reference them)
const finalPrice = computed(() => {
  const basePrice = subtotal.value - discountAmount.value
  const shipping = orderType.value === 'delivery' ? shippingFee.value : 0
  return basePrice + shipping
})

const lastPaymentFieldChanged = ref<'cash' | 'transfer'>(paymentForm.value.method === 'transfer' ? 'transfer' : 'cash')

const cloneOptions = <T extends Record<string, any>>(options: T[] | null | undefined) =>
  Array.isArray(options) ? options.map((option) => ({ ...option })) : []

const createDefaultOrderUiState = (): OrderUiState => ({
  paymentForm: {
    discountCode: null,
    method: 'cash',
    cashReceived: 0,
    transferReceived: 0,
  },
  orderType: 'counter',
  shippingFee: 0,
  walkInContact: {
    name: '',
    email: '',
    phone: '',
    deliveryValid: false,
  },
  walkInLocation: {
    thanhPho: '',
    quan: '',
    phuong: '',
    diaChiCuThe: '',
    districts: [],
    wards: [],
  },
  lastPaymentField: 'cash',
})

type PendingInvoiceItem = NonNullable<PendingInvoiceResponse['items']>[number]

const normalizeNumber = (value: unknown, fallback = 0) => {
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : fallback
}

const formatOrderCode = (maHoaDon: string | undefined, fallbackId: string) => {
  if (maHoaDon && maHoaDon.trim()) return maHoaDon
  if (/^\d+$/.test(fallbackId)) {
    return `HD${fallbackId.padStart(6, '0')}`
  }
  return fallbackId
}

const parseAddressComponents = (raw?: string) => {
  const defaults = { diaChiCuThe: '', phuong: '', quan: '', thanhPho: '' }
  if (!raw) return defaults
  const segments = raw
    .split(',')
    .map((segment) => segment.trim())
    .filter((segment) => segment.length > 0)
  if (segments.length === 0) return defaults
  if (segments.length === 1) {
    return { ...defaults, diaChiCuThe: segments[0].split(',')[0]?.trim() || '' }
  }
  if (segments.length === 2) {
    return { ...defaults, diaChiCuThe: segments[0].split(',')[0]?.trim() || '', thanhPho: segments[1] }
  }
  const parts = [...segments]
  const result = { ...defaults }
  result.thanhPho = parts.pop() ?? ''
  result.quan = parts.pop() ?? ''
  result.phuong = parts.length >= 1 ? (parts.pop() ?? '') : ''
  result.diaChiCuThe = parts.length > 0 ? parts.join(', ') : ''
  if (!result.diaChiCuThe && segments.length >= 3) {
    result.diaChiCuThe = parts.pop() ?? ''
  }
  const commaIndex = result.diaChiCuThe.indexOf(',')
  if (commaIndex !== -1) {
    result.diaChiCuThe = result.diaChiCuThe.slice(0, commaIndex).trim()
  }
  return result
}

const mapInvoiceItemToCartItem = (detail: PendingInvoiceItem, orderId: string, index: number): CartItem => {
  const quantity = Math.max(1, normalizeNumber(detail.soLuong, 1))
  const unitPrice = normalizeNumber(detail.giaBan ?? (detail.thanhTien && quantity ? Number(detail.thanhTien) / quantity : undefined), 0)
  const referencePrice = normalizeNumber(detail.giaBanSanPham ?? detail.giaBan, unitPrice)
  const discount = Math.max(0, referencePrice - unitPrice)
  const baseProductId = detail.sanPhamId ?? detail.maSanPhamChiTiet ?? detail.maHoaDonChiTiet ?? detail.id
  const productId = baseProductId ? String(baseProductId) : `${orderId}-item-${index}`

  return {
    id: detail.maHoaDonChiTiet || detail.id?.toString() || `${orderId}-detail-${index}`,
    idHoaDonChiTiets: detail.id ? [detail.id] : [],
    productId,
    productName: detail.tenSanPhamChiTiet || detail.tenSanPham || 'Sản phẩm',
    price: unitPrice,
    discount,
    quantity,
    image: detail.anhSanPham?.[0],
    images: detail.anhSanPham && detail.anhSanPham.length > 0 ? detail.anhSanPham : undefined,
    tenChiTietSanPham: detail.tenSanPhamChiTiet || detail.tenSanPham,
    tenMauSac: detail.tenMauSac,
    maMau: detail.maMauSac,
    tenKichThuoc: detail.tenKichThuoc,
    tenDeGiay: detail.tenDeGiay,
    tenChatLieu: detail.tenChatLieu,
  }
}

const mapInvoiceToOrder = (invoice: PendingInvoiceResponse, index: number): Order => {
  const orderId = invoice.id ? invoice.id.toString() : `draft-${Date.now()}-${index}`
  const rawItems = Array.isArray(invoice.items) ? invoice.items.filter((detail) => detail && detail.deleted !== true) : []
  const items = rawItems.map((detail, detailIndex) => mapInvoiceItemToCartItem(detail, orderId, detailIndex))

  const cachedState = createDefaultOrderUiState()
  const normalizedShippingFee = normalizeNumber(invoice.phiVanChuyen ?? cachedState.shippingFee)
  const hasDeliveryIndicators =
    Boolean(invoice.loaiDon) || normalizedShippingFee > 0 || Boolean(invoice.diaChiNhanHang || invoice.diaChiNguoiNhan)
  cachedState.orderType = hasDeliveryIndicators ? 'delivery' : 'counter'
  cachedState.shippingFee = normalizedShippingFee
  const parsedAddress = parseAddressComponents(invoice.diaChiNhanHang || invoice.diaChiNguoiNhan)
  cachedState.walkInContact = {
    ...cachedState.walkInContact,
    name: invoice.tenKhachHang || invoice.tenNguoiNhan || cachedState.walkInContact.name,
    email: invoice.email || invoice.emailNguoiNhan || cachedState.walkInContact.email,
    phone: invoice.soDienThoai || invoice.soDienThoaiNguoiNhan || cachedState.walkInContact.phone,
    deliveryValid: hasDeliveryIndicators ? true : cachedState.walkInContact.deliveryValid,
  }
  cachedState.walkInLocation = {
    ...cachedState.walkInLocation,
    ...parsedAddress,
    districts: [],
    wards: [],
  }

  orderUiStateCache.value[orderId] = cachedState

  return {
    id: orderId,
    orderCode: formatOrderCode(invoice.maHoaDon, orderId),
    maHoaDon: invoice.maHoaDon,
    items,
    customerId: invoice.maKhachHang || '',
    createdAt: invoice.ngayTao ? new Date(invoice.ngayTao) : new Date(),
  }
}

const resetOrderUiState = () => {
  isRestoringOrderState.value = true
  try {
    const defaults = createDefaultOrderUiState()
    paymentForm.value.discountCode = defaults.paymentForm.discountCode
    paymentForm.value.method = defaults.paymentForm.method
    paymentForm.value.cashReceived = defaults.paymentForm.cashReceived
    paymentForm.value.transferReceived = defaults.paymentForm.transferReceived
    orderType.value = defaults.orderType
    shippingFee.value = defaults.shippingFee
    walkInName.value = defaults.walkInContact.name
    walkInEmail.value = defaults.walkInContact.email
    walkInPhone.value = defaults.walkInContact.phone
    walkInDeliveryValid.value = defaults.walkInContact.deliveryValid
    walkInLocation.value = {
      thanhPho: defaults.walkInLocation.thanhPho,
      quan: defaults.walkInLocation.quan,
      phuong: defaults.walkInLocation.phuong,
      diaChiCuThe: defaults.walkInLocation.diaChiCuThe,
      districts: cloneOptions(defaults.walkInLocation.districts),
      wards: cloneOptions(defaults.walkInLocation.wards),
    }
    lastPaymentFieldChanged.value = defaults.lastPaymentField
  } finally {
    isRestoringOrderState.value = false
  }
}

const snapshotOrderUiState = (orderId: string | null | undefined) => {
  if (!orderId) return
  orderUiStateCache.value[orderId] = {
    paymentForm: {
      discountCode: paymentForm.value.discountCode,
      method: paymentForm.value.method,
      cashReceived: paymentForm.value.cashReceived,
      transferReceived: paymentForm.value.transferReceived,
    },
    orderType: orderType.value,
    shippingFee: shippingFee.value,
    walkInContact: {
      name: walkInName.value,
      email: walkInEmail.value,
      phone: walkInPhone.value,
      deliveryValid: walkInDeliveryValid.value,
    },
    walkInLocation: {
      thanhPho: walkInLocation.value.thanhPho,
      quan: walkInLocation.value.quan,
      phuong: walkInLocation.value.phuong,
      diaChiCuThe: walkInLocation.value.diaChiCuThe,
      districts: cloneOptions(walkInLocation.value.districts),
      wards: cloneOptions(walkInLocation.value.wards),
    },
    lastPaymentField: lastPaymentFieldChanged.value,
  }
}

const applyOrderUiState = (orderId: string | null | undefined) => {
  if (!orderId) {
    resetOrderUiState()
    return
  }
  const cached = orderUiStateCache.value[orderId] || createDefaultOrderUiState()
  orderUiStateCache.value[orderId] = cached

  isRestoringOrderState.value = true
  try {
    paymentForm.value.discountCode = cached.paymentForm.discountCode
    paymentForm.value.method = cached.paymentForm.method
    paymentForm.value.cashReceived = cached.paymentForm.cashReceived
    paymentForm.value.transferReceived = cached.paymentForm.transferReceived
    orderType.value = cached.orderType
    shippingFee.value = cached.shippingFee
    walkInName.value = cached.walkInContact.name
    walkInEmail.value = cached.walkInContact.email
    walkInPhone.value = cached.walkInContact.phone
    walkInDeliveryValid.value = cached.walkInContact.deliveryValid
    walkInLocation.value = {
      thanhPho: cached.walkInLocation.thanhPho,
      quan: cached.walkInLocation.quan,
      phuong: cached.walkInLocation.phuong,
      diaChiCuThe: cached.walkInLocation.diaChiCuThe,
      districts: cloneOptions(cached.walkInLocation.districts),
      wards: cloneOptions(cached.walkInLocation.wards),
    }
    lastPaymentFieldChanged.value = cached.lastPaymentField
  } finally {
    isRestoringOrderState.value = false
  }
}

watch(
  () => orders.value.map((order) => order.id),
  (newIds, oldIds) => {
    newIds.forEach((id) => {
      if (id && !orderUiStateCache.value[id]) {
        orderUiStateCache.value[id] = createDefaultOrderUiState()
      }
    })
    if (oldIds) {
      oldIds.forEach((id) => {
        if (id && !newIds.includes(id)) {
          delete orderUiStateCache.value[id]
        }
      })
    }
  },
  { immediate: true }
)

watch(
  () => currentOrder.value?.id ?? null,
  (newId, oldId) => {
    if (oldId) {
      snapshotOrderUiState(oldId)
    }
    applyOrderUiState(newId)
  },
  { immediate: true }
)

watch(
  () => shippingFee.value,
  (fee) => {
    const orderId = currentOrder.value?.id
    if (!orderId) return
    const cachedState = orderUiStateCache.value[orderId]
    if (cachedState) {
      cachedState.shippingFee = normalizeNumber(fee, 0)
    }
  }
)

watch(
  () => ({
    orderId: currentOrder.value?.id,
    orderType: orderType.value,
    thanhPho: walkInLocation.value.thanhPho,
    quan: walkInLocation.value.quan,
    phuong: walkInLocation.value.phuong,
    diaChiCuThe: walkInLocation.value.diaChiCuThe,
  }),
  () => {
    scheduleAutoShippingFeeCalculation()
  },
  { immediate: true }
)

const normalizePaymentValue = (value: number, options?: { allowOverpay?: boolean }) => {
  if (!Number.isFinite(value)) return 0
  if (value < 0) return 0
  const allowOverpay = options?.allowOverpay ?? false
  if (allowOverpay) {
    return value
  }
  const total = Math.max(0, finalPrice.value)
  return Math.min(value, total)
}

const syncPaymentSplit = (preferred: 'cash' | 'transfer' = lastPaymentFieldChanged.value) => {
  const total = Math.max(0, finalPrice.value)
  const method = paymentForm.value.method

  if (method === 'cash') {
    const cash = normalizePaymentValue(paymentForm.value.cashReceived ?? 0, { allowOverpay: true })
    paymentForm.value.cashReceived = cash
    paymentForm.value.transferReceived = 0
    return
  }

  if (method === 'transfer') {
    const rawTransfer = paymentForm.value.transferReceived ?? total
    const transfer = normalizePaymentValue(rawTransfer <= 0 ? total : rawTransfer)
    paymentForm.value.transferReceived = transfer > 0 ? transfer : total
    paymentForm.value.cashReceived = 0
    return
  }

  const cashRaw = paymentForm.value.cashReceived ?? 0
  const transferRaw = paymentForm.value.transferReceived ?? 0

  if (preferred === 'cash') {
    const cash = normalizePaymentValue(cashRaw, { allowOverpay: true })
    paymentForm.value.cashReceived = cash
    const remaining = Math.max(0, total - cash)
    // Auto-calculate transfer amount based on remaining balance
    paymentForm.value.transferReceived = remaining
    return
  }

  const transfer = normalizePaymentValue(transferRaw, { allowOverpay: true })
  paymentForm.value.transferReceived = transfer
  const remaining = Math.max(0, total - transfer)
  // Auto-calculate cash amount based on remaining balance
  paymentForm.value.cashReceived = remaining
}

const handleCashAmountChange = (value: number | null) => {
  lastPaymentFieldChanged.value = 'cash'
  const sanitized = typeof value === 'number' && Number.isFinite(value) ? value : null
  assignCashAmount(sanitized)
  // Always sync payment split when in "both" mode, even if value is null (to auto-calculate the other field)
  if (paymentForm.value.method === 'both') {
    syncPaymentSplit('cash')
  }
}

const handleTransferAmountChange = (value: number | null) => {
  lastPaymentFieldChanged.value = 'transfer'
  const sanitized = typeof value === 'number' && Number.isFinite(value) ? value : null
  assignTransferAmount(sanitized)
  // Always sync payment split when in "both" mode, even if value is null (to auto-calculate the other field)
  if (paymentForm.value.method === 'both') {
    syncPaymentSplit('transfer')
  }
}

const totalReceived = computed(() => (paymentForm.value?.cashReceived ?? 0) + (paymentForm.value?.transferReceived ?? 0))
const change = computed(() => totalReceived.value - finalPrice.value)
const paymentMethod = computed({
  get: () => paymentForm.value.method,
  set: (value: 'cash' | 'transfer' | 'both') => {
    paymentForm.value.method = value
  },
})
const canConfirmOrder = computed(() => {
  if (!currentOrder.value?.items.length || finalPrice.value <= 0) return false
  if (orderType.value === 'delivery') {
    if (selectedCustomer.value && !selectedCustomer.value.address) return false
    if (isWalkIn.value) {
      const wl = walkInLocation.value
      if (!wl.thanhPho || !wl.quan || !wl.phuong || !wl.diaChiCuThe) return false
    }
  }
  if (paymentForm.value.method === 'cash') return (paymentForm.value.cashReceived ?? 0) >= finalPrice.value
  if (paymentForm.value.method === 'transfer') return (paymentForm.value.transferReceived ?? 0) >= finalPrice.value
  if (paymentForm.value.method === 'both') return totalReceived.value >= finalPrice.value
  return true
})

watch(
  () => finalPrice.value,
  () => {
    if (isRestoringOrderState.value) return
    syncPaymentSplit()
  }
)

watch(
  () => paymentForm.value.method,
  (method) => {
    if (isRestoringOrderState.value) return
    lastPaymentFieldChanged.value = method === 'transfer' ? 'transfer' : 'cash'
    syncPaymentSplit(lastPaymentFieldChanged.value)
  }
)

// ==================== QR SCANNER METHODS ====================

// Watch for delete product modal visibility

// Watch for customer selection change - load customer-specific vouchers
watch(selectedCustomer, async (newCustomer) => {
  if (newCustomer && newCustomer.id) {
    walkInName.value = ''
    walkInEmail.value = ''
    walkInPhone.value = ''
    walkInDeliveryValid.value = false
    const idKhachHang = parseInt(newCustomer.id)

    // Add small delay to ensure state is fully updated
    await new Promise((resolve) => setTimeout(resolve, 100))

    await refreshVouchersForCustomer(idKhachHang)
  } else {
    // No customer selected, load all public vouchers

    // Add small delay to ensure state is fully updated
    await new Promise((resolve) => setTimeout(resolve, 100))

    await refreshVouchers()
  }
})

watch(
  () => orders.value.length,
  (newLength, oldLength) => {
    if (newLength > oldLength) {
      walkInName.value = ''
      walkInEmail.value = ''
      walkInPhone.value = ''
      walkInDeliveryValid.value = false
    }
  }
)

watch(
  () => ({
    orderId: currentOrder.value?.id,
    items:
      currentOrder.value?.items?.map((item) => ({
        id: item.id,
        quantity: item.quantity,
        price: item.price,
        discount: item.discount,
      })) ?? [],
    subtotal: subtotal.value,
    discountAmount: discountAmount.value,
    shippingFee: shippingFee.value,
    finalPrice: finalPrice.value,
    paymentMethod: paymentForm.value.method,
    cashReceived: paymentForm.value.cashReceived,
    transferReceived: paymentForm.value.transferReceived,
  }),
  () => {
    if (!currentOrder.value) return
    if (!currentOrder.value.items?.length) return
    if (!finalPrice.value || finalPrice.value <= 0) return
    scheduleQrSync()
  },
  { deep: true }
)

watch(
  () => currentOrder.value?.items?.length ?? 0,
  async (newLength, oldLength) => {
    if (oldLength && oldLength > 0 && newLength === 0 && currentOrder.value) {
      const orderId = currentOrder.value.id
      const existing = qrSessions.value[orderId]
      if (existing?.sessionId) {
        try {
          await cancelQrPaymentSession(existing.sessionId)
        } catch (error: any) {
          console.error('Không thể huỷ session khi xoá toàn bộ sản phẩm:', error)
        }
      }
      delete qrSessions.value[orderId]
    }
  }
)

// Watch for isWalkIn changes to debug reactivity
watch(
  () => [isWalkIn.value, orderType.value, currentOrder.value?.customerId],
  ([walkIn, type, custId]) => {},
  { immediate: true }
)

// Refresh vouchers periodically
const refreshVouchers = async () => {
  try {
    const couponsResponse = await getPosActiveCoupons()
    if (couponsResponse) {
      // Filter coupons: Only show PUBLIC vouchers (featured=false) with available quantity (soLuongDung > 0 and trangThai === true)
      const newCoupons = (couponsResponse as CouponApiModel[]).filter((coupon) => {
        const quantity = coupon.soLuongDung ?? 0
        return !coupon.featured && quantity > 0 && coupon.trangThai === true
      })

      // Compare with cached data - ONLY update if different
      const newCouponsJson = JSON.stringify(
        newCoupons.map((c) => ({ id: c.id, maPhieuGiamGia: c.maPhieuGiamGia, soLuongDung: c.soLuongDung, giaTriGiamGia: c.giaTriGiamGia }))
      )
      if (newCouponsJson === cachedCoupons.value) {
        return
      }
      cachedCoupons.value = newCouponsJson

      // Check if current selected coupon still exists and is valid
      if (selectedCoupon.value && !newCoupons.find((c) => c.maPhieuGiamGia === selectedCoupon.value?.maPhieuGiamGia)) {
        paymentForm.value.discountCode = null
      }

      // Find the BEST eligible voucher (highest discount)
      const eligibleVouchers = newCoupons.filter((coupon) => isVoucherEligible(coupon))
      let bestVoucher: CouponApiModel | null = null
      let maxDiscount = 0

      for (const coupon of eligibleVouchers) {
        const discount = calculateVoucherDiscount(coupon)
        if (discount > maxDiscount) {
          maxDiscount = discount
          bestVoucher = coupon
        }
      }

      // Check if selected voucher is still eligible
      const isSelectedEligible =
        selectedCoupon.value && eligibleVouchers.some((c) => c.maPhieuGiamGia === selectedCoupon.value?.maPhieuGiamGia)

      // If selected voucher is no longer eligible, clear it automatically
      if (selectedCoupon.value && !isSelectedEligible) {
        paymentForm.value.discountCode = null
      }

      // Compare current selected voucher discount with best voucher discount
      const currentDiscount = selectedCoupon.value ? calculateVoucherDiscount(selectedCoupon.value) : 0
      const bestDiscount = bestVoucher ? calculateVoucherDiscount(bestVoucher) : 0
      const isBestVoucherBetter = bestDiscount > currentDiscount

      // Auto-select the best eligible voucher if:
      // 1. No voucher selected, OR
      // 2. Current voucher is not eligible, OR
      // 3. Best voucher gives more discount than current one
      if (bestVoucher && (!selectedCoupon.value || !isSelectedEligible || isBestVoucherBetter)) {
        // Auto-select without validation checks - just update discount code
        paymentForm.value.discountCode = bestVoucher.maPhieuGiamGia || bestVoucher.id.toString()
        // Show message only when upgrading to a better voucher
        if (isBestVoucherBetter && selectedCoupon.value) {
          const savingDifference = bestDiscount - currentDiscount
          Message.success(
            `Đã áp dụng voucher: ${bestVoucher.tenPhieuGiamGia} (tiết kiệm thêm ${savingDifference.toLocaleString('vi-VN')}đ)`
          )
        }

        // If cart has items, also update payment and send API call
        if (currentOrder.value?.items?.length && currentOrder.value.items.some((item) => item.quantity > 0)) {
          const invoiceId = parseInt(currentOrder.value.id)
          await updateInvoiceVoucher(invoiceId, bestVoucher.id).catch((error) => {
            console.warn('Failed to update invoice voucher:', error)
          })
        }
      }

      coupons.value = newCoupons
      voucherPagination.value.total = newCoupons.length
    }
  } catch (error) {
    console.error('Error refreshing vouchers:', error)
  }
}

// Refresh vouchers for specific customer (load public + personal vouchers)
const refreshVouchersForCustomer = async (idKhachHang: number) => {
  try {
    const couponsResponse = await getPosActiveCouponsForCustomer(idKhachHang)
    if (couponsResponse) {
      // Filter coupons: only show active vouchers with available quantity
      let newCoupons = (couponsResponse as CouponApiModel[]).filter((coupon) => {
        const quantity = coupon.soLuongDung ?? 0
        return quantity > 0 && coupon.trangThai === true
      })

      // Deduplicate
      const seenIds = new Set<string>()
      newCoupons = newCoupons.filter((coupon) => {
        if (seenIds.has(coupon.id.toString())) {
          return false
        }
        seenIds.add(coupon.id.toString())
        return true
      })

      // Compare with cached data - ONLY update if different
      const newCouponsJson = JSON.stringify(
        newCoupons.map((c) => ({ id: c.id, maPhieuGiamGia: c.maPhieuGiamGia, soLuongDung: c.soLuongDung, giaTriGiamGia: c.giaTriGiamGia }))
      )
      if (newCouponsJson === cachedCoupons.value) {
        // No change, skip update
        return
      }
      cachedCoupons.value = newCouponsJson

      // Check if current selected coupon still exists and is valid
      if (selectedCoupon.value && !newCoupons.find((c) => c.maPhieuGiamGia === selectedCoupon.value?.maPhieuGiamGia)) {
        paymentForm.value.discountCode = null
      }

      // Find the BEST eligible voucher (highest discount)
      const eligibleVouchers = newCoupons.filter((coupon) => isVoucherEligible(coupon))
      let bestVoucher: CouponApiModel | null = null
      let maxDiscount = 0

      for (const coupon of eligibleVouchers) {
        const discount = calculateVoucherDiscount(coupon)
        if (discount > maxDiscount) {
          maxDiscount = discount
          bestVoucher = coupon
        }
      }

      // Check if selected voucher is still eligible
      const isSelectedEligible =
        selectedCoupon.value && eligibleVouchers.some((c) => c.maPhieuGiamGia === selectedCoupon.value?.maPhieuGiamGia)

      // If selected voucher is no longer eligible, clear it automatically
      if (selectedCoupon.value && !isSelectedEligible) {
        paymentForm.value.discountCode = null
      }

      // Compare current selected voucher discount with best voucher discount
      const currentDiscount = selectedCoupon.value ? calculateVoucherDiscount(selectedCoupon.value) : 0
      const bestDiscount = bestVoucher ? calculateVoucherDiscount(bestVoucher) : 0
      const isBestVoucherBetter = bestDiscount > currentDiscount

      // Auto-select the best eligible voucher if:
      // 1. No voucher selected, OR
      // 2. Current voucher is not eligible, OR
      // 3. Best voucher gives more discount than current one
      if (bestVoucher && (!selectedCoupon.value || !isSelectedEligible || isBestVoucherBetter)) {
        // Auto-select without validation checks - just update discount code
        paymentForm.value.discountCode = bestVoucher.maPhieuGiamGia || bestVoucher.id.toString()
        // Show message only when upgrading to a better voucher
        if (isBestVoucherBetter && selectedCoupon.value) {
          const savingDifference = bestDiscount - currentDiscount
          Message.success(
            `Đã nâng cấp voucher: ${bestVoucher.tenPhieuGiamGia} (tiết kiệm thêm ${savingDifference.toLocaleString('vi-VN')}đ)`
          )
        }

        // If cart has items, also update payment and send API call
        if (currentOrder.value?.items?.length && currentOrder.value.items.some((item) => item.quantity > 0)) {
          const invoiceId = parseInt(currentOrder.value.id)
          await updateInvoiceVoucher(invoiceId, bestVoucher.id).catch((error) => {
            console.warn('Failed to update invoice voucher:', error)
          })
        }
      }

      coupons.value = newCoupons
      voucherPagination.value.total = newCoupons.length
    }
  } catch (error) {
    console.error('Error refreshing vouchers for customer:', error)
  }
}

// Refresh product stock from server (for real-time sync between tabs/windows)
// Define as a function declaration to avoid TDZ when passing before its definition
async function refreshProductStock() {
  try {
    const module = await import('./services/productService')
    const availableProducts = await module.fetchAllAvailableProducts()

    let hasChanged = false

    if (availableProducts.length !== allProductVariants.value.length) {
      hasChanged = true
    } else {
      for (const product of availableProducts) {
        const cachedStock = cachedProducts.value.get(product.id)
        if (cachedStock === undefined || cachedStock !== product.soLuong) {
          hasChanged = true
          break
        }
      }
    }

    if (hasChanged) {
      cachedProducts.value.clear()
      for (const product of availableProducts) {
        cachedProducts.value.set(product.id, product.soLuong)
      }
      allProductVariants.value = availableProducts
      productPagination.value.total = availableProducts.length
      // Preserve current page instead of resetting to page 1
      // Only reset if current page exceeds total pages after update
      const maxPage = Math.ceil(availableProducts.length / (productPagination.value.pageSize || 10))
      if (productPagination.value.current > maxPage && maxPage > 0) {
        productPagination.value.current = maxPage
      }
    }
  } catch (error) {
    console.error('Error refreshing product stock:', error)
  }
}

// Auto-refresh vouchers every 30 seconds
let voucherRefreshInterval: number | null = null

// Actions that depend on finalPrice
const handlePaymentMethodChange = async (value: string) => {
  try {
    if (!currentOrder.value) {
      Message.error('Không có đơn hàng được chọn')
      return
    }
    const invoiceId = parseInt(currentOrder.value.id)
    if (Number.isNaN(invoiceId)) {
      Message.error('ID hóa đơn không hợp lệ')
      return
    }
    await updateInvoicePayment(invoiceId, value as 'cash' | 'transfer' | 'both')
    paymentForm.value.method = value as 'cash' | 'transfer' | 'both'

    if (value === 'cash') {
      assignCashAmount(finalPrice.value)
      assignTransferAmount(0)
      lastPaymentFieldChanged.value = 'cash'
    } else if (value === 'transfer') {
      assignTransferAmount(finalPrice.value)
      assignCashAmount(0)
      lastPaymentFieldChanged.value = 'transfer'
    } else if (value === 'both') {
      // Auto-populate with default split: cash = finalPrice, transfer = 0
      // User can then adjust either field and the other will auto-calculate
      assignCashAmount(finalPrice.value)
      assignTransferAmount(0)
      lastPaymentFieldChanged.value = 'cash'
    }
  } catch (error: any) {
    console.error('Lỗi cập nhật phương thức thanh toán:', error)
    Message.error(error.message || 'Có lỗi xảy ra khi cập nhật phương thức thanh toán')
  }
}

const handleOrderTypeChange = async (value: string) => {
  try {
    orderType.value = value as 'counter' | 'delivery'

    if (value === 'delivery') {
      Message.info('Vui lòng nhập địa chỉ giao hàng')
      // Trigger shipping fee calculation for registered customer or walk-in customer
      // Use nextTick to ensure DOM is updated first
      await nextTick()
      if (walkInLocation.value.thanhPho && walkInLocation.value.quan && walkInLocation.value.phuong) {
        // Call GHN API to get real shipping fee
        try {
          const result = await calculateShippingFeeFromGHN(walkInLocation.value as any)
          shippingFee.value = result.fee || 0
        } catch (error) {
          console.error('[OrderTypeChange] Error getting GHN fee, using fallback:', error)
          const fallbackFee = calculateShippingFee(walkInLocation.value as any, subtotal.value)
          shippingFee.value = fallbackFee || 0
        }
      } else {
        shippingFee.value = 0
      }
    } else {
      // Reset shipping fee when not in delivery mode
      shippingFee.value = 0
    }

    // Update backend AFTER shipping fee is calculated/reset
    if (currentOrder.value?.id) {
      const invoiceId = parseInt(currentOrder.value.id)
      if (!Number.isNaN(invoiceId)) {
        await updateInvoiceShipping(invoiceId)
      }
    }
  } catch (error: any) {
    console.error('Lỗi cập nhật loại đơn:', error)
    Message.error(error.message || 'Có lỗi xảy ra khi cập nhật loại đơn')
  }
}

const updateShippingFeeValue = (value: number) => {
  shippingFee.value = value
}

const updateWalkInAddress = (value: string) => {
  walkInLocation.value.diaChiCuThe = value
}

const updateWalkInWard = (value: string) => {
  walkInLocation.value.phuong = value
}

const buildQrPayload = (order: Order): CreateQrSessionPayload | null => {
  if (!order || !order.items?.length) return null
  const invoiceId = parseInt(order.id, 10)
  if (Number.isNaN(invoiceId)) return null
  // Ưu tiên dùng mã hoá đơn (HD000...) để đồng bộ với UI POS và mobile
  const displayOrderCode = order.maHoaDon || order.orderCode
  if (!displayOrderCode) return null
  if (!finalPrice.value || finalPrice.value <= 0) return null

  return {
    orderCode: displayOrderCode,
    invoiceId,
    items: order.items.map((item) => ({
      productId: item.productId,
      productName: item.productName,
      price: item.price,
      discount: item.discount,
      quantity: item.quantity,
      image: item.image,
      tenMauSac: item.tenMauSac,
      maMau: item.maMau,
      tenKichThuoc: item.tenKichThuoc,
      tenDeGiay: item.tenDeGiay,
      tenChatLieu: item.tenChatLieu,
    })),
    subtotal: subtotal.value,
    discountAmount: discountAmount.value,
    shippingFee: shippingFee.value,
    finalPrice: finalPrice.value,
    customerId: order.customerId || undefined,
  }
}

const syncQrSession = async (force = false) => {
  const order = currentOrder.value
  if (!order) return
  const payload = buildQrPayload(order)
  if (!payload) {
    const existing = qrSessions.value[order.id]
    if (existing?.sessionId) {
      try {
        await cancelQrPaymentSession(existing.sessionId)
      } catch (error: any) {
        console.error('Không thể huỷ QR session khi giỏ hàng trống:', error)
      } finally {
        delete qrSessions.value[order.id]
      }
    }
    return
  }

  const payloadHash = JSON.stringify(payload)
  const existing = qrSessions.value[order.id]
  if (!force && existing?.lastPayloadHash === payloadHash) return

  qrSyncing.value = true
  qrSyncError.value = null
  try {
    const response = existing?.sessionId ? await updateQrPaymentSession(existing.sessionId, payload) : await createQrPaymentSession(payload)

    qrSessions.value[order.id] = {
      sessionId: response.qrSessionId,
      status: response.status,
      qrCodeUrl: response.qrCodeUrl,
      expiresAt: response.expiresAt,
      lastPayloadHash: payloadHash,
      updatedAt: Date.now(),
    }
  } catch (error: any) {
    console.error('Không thể đồng bộ QR session:', error)
    qrSyncError.value = error.message || 'Không thể đồng bộ VietQR'
  } finally {
    qrSyncing.value = false
  }
}

const scheduleQrSync = () => {
  if (typeof window === 'undefined') return
  if (qrSyncTimeout) {
    window.clearTimeout(qrSyncTimeout)
  }
  qrSyncTimeout = window.setTimeout(() => {
    syncQrSession()
  }, QR_SYNC_DEBOUNCE_MS)
}

const forceSyncQrSession = () => {
  if (typeof window !== 'undefined' && qrSyncTimeout) {
    window.clearTimeout(qrSyncTimeout)
    qrSyncTimeout = null
  }
  syncQrSession(true)
}

const triggerDeepLink = (url: string) => {
  if (typeof document === 'undefined') return

  // Method 1: Try iframe (works for most cases)
  try {
    const iframe = document.createElement('iframe')
    iframe.style.display = 'none'
    iframe.style.width = '0'
    iframe.style.height = '0'
    iframe.src = url
    document.body.appendChild(iframe)
    setTimeout(() => {
      if (iframe.parentNode) {
        document.body.removeChild(iframe)
      }
    }, 2000)
  } catch (e) {
    // Silent fail
  }

  // Method 2: Try window.location (fallback)
  try {
    const link = document.createElement('a')
    link.href = url
    link.style.display = 'none'
    document.body.appendChild(link)
    link.click()
    setTimeout(() => {
      if (link.parentNode) {
        document.body.removeChild(link)
      }
    }, 100)
  } catch (e) {
    // Silent fail
  }

  // Method 3: Try direct window.location.href (last resort)
  try {
    window.location.href = url
  } catch (e) {
    // Silent fail
  }
}

const openMobileSession = async () => {
  if (finalPrice.value <= 0) {
    Message.warning('Giỏ hàng chưa có tổng tiền hợp lệ')
    return
  }

  if (paymentForm.value.method === 'cash' && paymentForm.value.cashReceived >= finalPrice.value) {
    Message.warning('Không cần tạo VietQR khi khách đã thanh toán đủ bằng tiền mặt')
    return
  }

  let session = currentQrSession.value
  if (!session || session.status !== 'PENDING') {
    await syncQrSession(true)
    session = currentQrSession.value
  }

  if (!session?.sessionId || session.status !== 'PENDING') {
    Message.warning('Chưa có VietQR để mở trên mobile')
    return
  }

  try {
    // Gọi generateQrPayment để backend tạo/update mã QR cho session hiện tại.
    // Mobile app (QRPaymentScreen) sẽ tự động nhận session mới qua WebSocket/polling
    // và hiển thị màn hình QR mà không cần deeplink từ trình duyệt.
    await generateQrPayment(session.sessionId)
    Message.success('Đã sẵn sàng màn hình QR trên mobile')
  } catch (error: any) {
    console.error('Không thể tạo mã QR:', error)
    Message.error(error.message || 'Không thể tạo mã QR thanh toán')
  }
}

const cancelCurrentQrSession = async () => {
  try {
    const session = currentQrSession.value
    if (!session || session.status !== 'PENDING') return
    await cancelQrPaymentSession(session.sessionId)
  } catch (error: any) {
    console.error('Không thể huỷ QR session hiện tại:', error)
  }
}

const resetQrSession = async () => {
  try {
    // Use the same method as when clearing cart: cancel QR session
    // This will trigger WebSocket message with status 'EXPIRED'
    // Mobile app will automatically return to welcome screen (QRPaymentScreen with "Vui lòng chờ...")
    const session = currentQrSession.value
    const orderId = currentOrder.value?.id

    // Try to find sessionId from current session or from qrSessions
    let sessionId: string | null = null
    let foundOrderId: string | null = null

    if (session?.sessionId) {
      sessionId = session.sessionId
      foundOrderId = orderId || null
    } else if (orderId && qrSessions.value[orderId]?.sessionId) {
      sessionId = qrSessions.value[orderId].sessionId
      foundOrderId = orderId
    } else {
      // If no orderId, try to find sessionId from all qrSessions (get the most recent one)
      const allSessions = Object.entries(qrSessions.value)
      if (allSessions.length > 0) {
        const sortedSessions = allSessions.sort((a, b) => {
          const aUpdated = a[1]?.updatedAt || 0
          const bUpdated = b[1]?.updatedAt || 0
          return bUpdated - aUpdated
        })
        const [latestOrderId, latestSession] = sortedSessions[0]
        if (latestSession?.sessionId) {
          sessionId = latestSession.sessionId
          foundOrderId = latestOrderId
        }
      }
    }

    // Không có session nào đang hiển thị trên mobile
    if (!sessionId) {
      Message.warning('Không có màn hình thanh toán nào để reset')
      return
    }

    // Cancel session để mobile tự quay về màn hình chờ (giống clear cart)
    // Cancel session to trigger WebSocket message (same as when clearing cart)
    if (sessionId) {
      try {
        await cancelQrPaymentSession(sessionId)

        // Remove from local state
        const orderIdToDelete = foundOrderId || orderId
        if (orderIdToDelete && qrSessions.value[orderIdToDelete]) {
          delete qrSessions.value[orderIdToDelete]
        }
      } catch (error: any) {
        console.error('Không thể reset màn hình mobile:', error)
        Message.error('Không thể reset màn hình mobile: ' + (error.message || 'Lỗi không xác định'))
        return
      }
    } else {
      Message.warning('Không có màn hình thanh toán nào để reset')
      return
    }

    Message.success('Đã reset màn hình thanh toán trên mobile về màn hình chính')
  } catch (error: any) {
    console.error('Lỗi khi reset màn hình mobile:', error)
    Message.error('Không thể reset màn hình mobile')
  }
}

const handleResetQrSessionClick = () => {
  Modal.confirm({
    title: 'Reset màn hình thanh toán?',
    content: 'Màn hình thanh toán trên mobile sẽ quay về trạng thái chờ. Bạn có chắc chắn muốn thực hiện?',
    okText: 'Đồng ý',
    cancelText: 'Huỷ',
    onOk: () => resetQrSession(),
  })
}

watch(
  () => ({
    orderId: currentOrder.value?.id,
    items:
      currentOrder.value?.items?.map((item) => ({
        id: item.id,
        quantity: item.quantity,
        price: item.price,
        discount: item.discount,
      })) ?? [],
    subtotal: subtotal.value,
    discountAmount: discountAmount.value,
    shippingFee: shippingFee.value,
    finalPrice: finalPrice.value,
  }),
  () => {
    if (!currentOrder.value) return
    if (!currentOrder.value.items?.length) return
    if (!finalPrice.value || finalPrice.value <= 0) return
    scheduleQrSync()
  },
  { deep: true }
)

const {
  showProductModal,
  productSearchText,
  productFilters,
  filterOptionsData,
  productPagination,
  productMaterialOptions,
  productSoleOptions,
  productManufacturerOptions,
  productOriginOptions,
  productColorOptions,
  productSizeOptions,
  filteredProductVariants,
  loadFilterOptions,
  loadProductPage,
  loadAllProducts,
  resetProductFilters,
  openProductModal,
  handleProductModalCancel,
} = useProductModal({ allProductVariants })

const loadInitialData = async () => {
  loadingData.value = true
  try {
    await Promise.all([loadCustomers(), loadAllProducts()])
    loadFilterOptions()

    const [pendingInvoices, couponsResponse] = await Promise.all([getPendingInvoices(), getPosActiveCoupons()])

    if (couponsResponse) {
      coupons.value = (couponsResponse as CouponApiModel[]).filter((coupon) => {
        const quantity = coupon.soLuongDung ?? 0
        return !coupon.featured && quantity > 0 && coupon.trangThai === true
      })
      voucherPagination.value.total = coupons.value.length
    }

    const waitingInvoices = Array.isArray(pendingInvoices)
      ? pendingInvoices.filter((invoice) => {
          if (!invoice) return false
          const isWaiting = invoice.trangThai === false || invoice.trangThai === undefined || invoice.trangThai === null
          const isNotDeleted = invoice.deleted === false || invoice.deleted === undefined || invoice.deleted === null
          return isWaiting && isNotDeleted
        })
      : []

    orderUiStateCache.value = {}

    if (waitingInvoices.length > 0) {
      const customerCodeLookup = new Map<string, string>()
      customers.value.forEach((customer) => {
        const code = customer.code?.trim()
        if (code) customerCodeLookup.set(code, customer.id)
      })

      const mappedOrders = waitingInvoices.map((invoice, idx) => {
        const order = mapInvoiceToOrder(invoice, idx)
        const normalizedCode = typeof order.customerId === 'string' ? order.customerId.trim() : ''
        if (normalizedCode) {
          const matchedCustomerId = customerCodeLookup.get(normalizedCode)
          order.customerId = matchedCustomerId ?? ''
        } else {
          order.customerId = ''
        }
        return order
      })
      orders.value = mappedOrders
      currentOrderIndex.value = '0'

      const soldMap: Record<string | number, number> = {}
      mappedOrders.forEach((order) => {
        order.items.forEach((item) => {
          const numericId = Number(item.productId)
          if (!Number.isNaN(numericId)) {
            soldMap[numericId] = (soldMap[numericId] || 0) + item.quantity
          }
        })
      })
      soldQuantitiesByProductId.value = soldMap

      Message.success(`Đã tải ${mappedOrders.length} hóa đơn chờ`)
    } else {
      orders.value = []
      currentOrderIndex.value = '0'
      soldQuantitiesByProductId.value = {}
    }
  } catch (error: any) {
    console.error('Error loading data:', error)
    Message.error(error?.message || 'Không thể tải dữ liệu')
  } finally {
    loadingData.value = false
  }
}

const {
  showConfirmOrderModal,
  confirmOrderRequest,
  confirmLoading,
  checkBetterVouchers,
  confirmOrder,
  doConfirmOrder,
  cancelConfirmOrder,
} = useCheckout({
  currentOrder,
  selectedCustomer,
  paymentForm,
  orderType,
  finalPrice,
  subtotal,
  selectedCoupon,
  calculateVoucherDiscount,
  coupons,
  userId: userStoreInstance.id || 1,
  userName: userStoreInstance.name,
  orders,
  currentOrderIndex,
  shippingFee,
  walkInLocation,
  walkInName,
  walkInEmail,
  walkInPhone,
  walkInDeliveryValid,
  confirmPosOrder,
  onBeforeOrderRemoved: (orderData) => {
    // Cache confirmed order data for post-checkout operations (e.g., printing)
    lastConfirmedOrder.value = {
      ...orderData,
      confirmedAt: new Date(),
    }
  },
})

const { showQRScanner, initQRScanner, closeQRScanner } = useQrScanner({
  allProductVariants,
  currentOrder,
  loadAllProducts,
  refreshProductStock,
  userId: userStoreInstance.id || 1,
  addProductToInvoiceApi: addProductToInvoice,
  pendingProductOperations,
})

// Better voucher modal state (separate from main confirm modal)
const showBetterVoucherModal = ref(false)

// Helper function to get customer address
const getCustomerAddress = (): string | undefined => {
  if (selectedCustomer.value?.address) {
    return selectedCustomer.value.address
  }
  if (isWalkIn.value) {
    const addressParts = [
      walkInLocation.value.diaChiCuThe,
      walkInLocation.value.phuong,
      walkInLocation.value.quan,
      walkInLocation.value.thanhPho,
    ].filter(Boolean)
    return addressParts.length > 0 ? addressParts.join(', ') : undefined
  }
  return undefined
}

// Handle confirm order from modal - check for better vouchers first
const handleConfirmOrderFromModal = async () => {
  const betterVouchers = checkBetterVouchers()
  if (betterVouchers.length > 0) {
    // Close main confirm modal and show better voucher modal
    showConfirmOrderModal.value = false
    showBetterVoucherModal.value = true
  } else {
    // No better vouchers, proceed with confirmation
    await doConfirmOrder()
    // Ensure both modals are closed after confirmation
    showConfirmOrderModal.value = false
    showBetterVoucherModal.value = false
  }
}

// Handle confirm from better voucher modal
const handleConfirmFromBetterVoucher = async () => {
  await doConfirmOrder()
  // Close both modals after confirmation
  showConfirmOrderModal.value = false
  showBetterVoucherModal.value = false
}

// Watch for walk-in location changes (province, district, ward, address)
watch(
  () => walkInLocation.value,
  (newLocation) => {},
  { deep: true }
)

// Watch for province change
watch(
  () => walkInLocation.value.thanhPho,
  async (newProvince) => {}
)

// Watch for district change
watch(
  () => walkInLocation.value.quan,
  async (newDistrict) => {}
)

// Watch for ward change
watch(
  () => walkInLocation.value.phuong,
  async (newWard) => {}
)

// Watch for address detail change
watch(
  () => walkInLocation.value.diaChiCuThe,
  async (newAddress) => {}
)

onMounted(() => {
  // Do NOT initialize with an empty order - let user create orders manually by clicking "Thêm Đơn"
  // orders.value will be empty until user explicitly creates the first order

  // Load data from API
  loadInitialData()
  // Load provinces for location picker
  loadProvinces()

  // Setup BroadcastChannel for real-time sync between tabs/windows
  try {
    stockBroadcastChannel = new BroadcastChannel('stock-update-channel')
    stockBroadcastChannel.onmessage = (event) => {
      if (event.data.type === 'STOCK_CHANGE') {
        const { productId, newStock, needsRefresh } = event.data

        if (needsRefresh) {
          // Trigger immediate refresh instead of waiting for next interval
          refreshProductStock()
        } else if (newStock !== undefined) {
          // Direct stock update (for add-to-cart from other pages)
          const product = allProductVariants.value.find((p) => p.id === productId)
          if (product) {
            product.soLuong = newStock
          }
        }
      }
    }

    // Setup BroadcastChannel for coupon updates
    const couponBroadcastChannel = new BroadcastChannel('coupon-update-channel')
    couponBroadcastChannel.onmessage = (event) => {
      if (event.data.type === 'COUPON_CHANGE') {
        // Refresh vouchers for current customer or public vouchers
        if (selectedCustomer.value && selectedCustomer.value.id) {
          const idKhachHang = parseInt(selectedCustomer.value.id)
          refreshVouchersForCustomer(idKhachHang)
        } else {
          refreshVouchers()
        }
      }
    }
    // Store for cleanup
    // @ts-ignore
    window.__couponBroadcastChannel = couponBroadcastChannel
  } catch (error) {
    console.warn('BroadcastChannel not supported, falling back to polling', error)
  }

  // Set up auto-refresh for vouchers (every 4 seconds for near real-time updates)
  voucherRefreshInterval = window.setInterval(() => {
    // Throttle: only refresh if enough time has passed since last refresh
    const now = Date.now()
    if (now - lastVoucherRefreshTime < VOUCHER_THROTTLE_MS) {
      return // Skip this refresh cycle
    }
    lastVoucherRefreshTime = now

    // If customer is selected, refresh customer-specific vouchers
    // Otherwise, refresh public vouchers only
    if (selectedCustomer.value && selectedCustomer.value.id) {
      const idKhachHang = parseInt(selectedCustomer.value.id)
      refreshVouchersForCustomer(idKhachHang)
    } else {
      refreshVouchers()
    }
  }, 4000) // 4 seconds (faster near real-time sync)

  // Set up auto-refresh for product stock (every 2.5 seconds for near real-time sync between tabs)
  const stockRefreshInterval = window.setInterval(() => {
    // Throttle: only refresh if enough time has passed since last refresh
    const now = Date.now()
    if (now - lastStockRefreshTime < STOCK_THROTTLE_MS) {
      return // Skip this refresh cycle
    }
    lastStockRefreshTime = now

    refreshProductStock()
  }, 2500) // 2.5 seconds (faster for real-time stock updates)

  // Set up auto-refresh for product modal data (realtime even when modal is closed)
  const productModalRefreshInterval = window.setInterval(() => {
    loadAllProducts()
  }, 2500) // Same interval as stock refresh for consistency

  // Watch for cart items changes to refresh voucher eligibility
  watch(
    () => currentOrder.value?.items?.length,
    async (newLength, oldLength) => {
      if (newLength !== oldLength) {
        // Clear cache to force re-evaluation of voucher eligibility
        cachedCoupons.value = ''
        // Small delay to ensure cart state is fully updated
        await new Promise((resolve) => setTimeout(resolve, 100))
        if (selectedCustomer.value && selectedCustomer.value.id) {
          const idKhachHang = parseInt(selectedCustomer.value.id)
          cachedCustomerCoupons.value = ''
          refreshVouchersForCustomer(idKhachHang)
        } else {
          refreshVouchers()
        }
      }
    }
  )

  // Store interval IDs for cleanup
  ;(window as any).__stockRefreshInterval = stockRefreshInterval
  ;(window as any).__productModalRefreshInterval = productModalRefreshInterval

  // Huỷ QR session hiện tại khi reload/đóng tab
  window.addEventListener('beforeunload', cancelCurrentQrSession)
})

// Cleanup intervals on unmount
onBeforeUnmount(() => {
  if (voucherRefreshInterval !== null) {
    clearInterval(voucherRefreshInterval)
  }
  const globalWindow = window as any
  if (globalWindow.__stockRefreshInterval) {
    clearInterval(globalWindow.__stockRefreshInterval)
  }
  if (globalWindow.__productModalRefreshInterval) {
    clearInterval(globalWindow.__productModalRefreshInterval)
  }

  // Close BroadcastChannels
  if (stockBroadcastChannel) {
    stockBroadcastChannel.close()
  }
  if (globalWindow.__couponBroadcastChannel) {
    globalWindow.__couponBroadcastChannel.close()
  }

  if (qrSyncTimeout !== null) {
    clearTimeout(qrSyncTimeout)
  }

  window.removeEventListener('beforeunload', cancelCurrentQrSession)

  // Huỷ QR session hiện tại khi rời khỏi trang POS
  cancelCurrentQrSession()
})
</script>

<style scoped lang="less">
.pos-system {
  padding: 16px 20px;
}

.main-pos-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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

:deep(.arco-modal-body) {
  padding: 20px 20px;
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

  .item-count-badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 20px;
    height: 20px;
    padding: 0 6px;
    font-size: 12px;
    font-weight: 500;
    line-height: 1;
    color: #fff;
    background-color: #f5222d;
    border-radius: 10px;
    box-sizing: border-box;
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

.order-code-cart-card {
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

/* Payment Method Button Styling */
.payment-method-btn {
  position: relative;
  overflow: hidden;

  &:not(.payment-method-active) {
    border: 2px solid #f0f0f0 !important;
    background: #fafafa !important;
    color: #666 !important;

    &:hover {
      border-color: #40a9ff !important;
      background: #e6f7ff !important;
      color: #1890ff !important;
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
    }
  }

  &.payment-method-active {
    border: 2px solid #1890ff !important;
    box-shadow: 0 4px 16px rgba(24, 144, 255, 0.25);
    transform: translateY(-2px);

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 3px;
      background: linear-gradient(90deg, #1890ff, #40a9ff);
      animation: paymentActiveGlow 2s ease-in-out infinite alternate;
    }

    &::after {
      content: '';
      position: absolute;
      top: 8px;
      right: 12px;
      font-size: 12px;
      color: #1890ff;
      font-weight: bold;
      animation: paymentCheckPulse 1.5s ease-in-out infinite;
    }
  }
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
    box-shadow: 0 0 0 0 rgba(250, 140, 22, 0.4);
  }

  50% {
    opacity: 0.95;
    box-shadow: 0 0 0 8px rgba(250, 140, 22, 0);
  }
}

@keyframes paymentActiveGlow {
  0% {
    opacity: 0.8;
    transform: scaleX(1);
  }

  50% {
    opacity: 1;
    transform: scaleX(1.02);
  }
}

@keyframes paymentCheckPulse {
  0%,
  100% {
    opacity: 0.7;
    transform: scale(1);
  }

  50% {
    opacity: 1;
    transform: scale(1.1);
  }
}

.cash-feedback {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 14px;
  animation: cashFeedbackFadeIn 0.3s ease-out;
}

@keyframes cashFeedbackFadeIn {
  0% {
    opacity: 0;
    transform: scale(0.95);
  }

  100% {
    opacity: 1;
    transform: scale(1);
  }
}

.cash-change-positive {
  background: linear-gradient(135deg, #f6ffed, #b7eb8f);
  border: 1px solid #b7eb8f;
  color: #52c41a;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  animation: cashPositivePulse 1s ease-in-out infinite alternate;
}

@keyframes cashPositivePulse {
  0% {
    box-shadow: 0 0 0 0 rgba(82, 196, 26, 0.4);
  }

  100% {
    box-shadow: 0 2px 8px 0 rgba(82, 196, 26, 0.4);
  }
}

.cash-change-negative {
  background: linear-gradient(135deg, #fff2f0, #ffccc7);
  border: 1px solid #ffccc7;
  color: #ff4d4f;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  animation: cashNegativeShake 0.5s ease-in-out;
}

@keyframes cashNegativeShake {
  0%,
  100% {
    transform: translateX(0);
  }

  25% {
    transform: translateX(-2px);
  }

  75% {
    transform: translateX(2px);
  }
}

.cash-icon {
  font-size: 16px;
}

.cash-text {
  flex: 1;

  strong {
    font-size: 16px;
    font-weight: 700;
  }
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

/* Voucher disabled styling */
.voucher-disabled {
  opacity: 0.6;
  cursor: not-allowed !important;
  background: #f5f5f5 !important;
}

.voucher-disabled:hover {
  background: #f5f5f5 !important;
}

/* Hide scrollbar for tabs container */
.tabs-container::-webkit-scrollbar {
  display: none;
}
</style>
