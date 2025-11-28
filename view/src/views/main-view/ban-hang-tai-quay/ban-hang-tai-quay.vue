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
            :has-items="!!currentOrder?.items.length"
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
} from './services/posService'
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
  handleCashAmountChange,
  handleTransferAmountChange,
  clearVoucher,
  updateInvoicePayment,
  updateInvoiceShipping,
  updateInvoiceVoucher,
} = usePayment({ currentOrder })

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
  if (!currentOrder.value?.items.length) return
  Message.info('In hoá đơn thành công')
}

// ==================== LIFECYCLE ====================

const loadInitialData = async () => {
  loadingData.value = true
  try {
    // Load customers and coupons
    const couponsResponse = await getPosActiveCoupons()
    await loadCustomers()

    // Load filter options from server
    loadFilterOptions()
    // Process coupons - Filter to only show PUBLIC coupons on initial load (for walk-in customers)
    // Personal coupons will be loaded when customer is selected
    if (couponsResponse) {
      // Filter: Only show PUBLIC vouchers (featured=false) with available quantity (soLuongDung > 0 and trangThai === true)
      // Also exclude vouchers with negative soLuongDung (already sold out)
      coupons.value = (couponsResponse as CouponApiModel[]).filter((coupon) => {
        const quantity = coupon.soLuongDung ?? 0
        // IMPORTANT: On initial load, only show public coupons (featured=false) for walk-in customers
        return !coupon.featured && quantity > 0 && coupon.trangThai === true
      })
      voucherPagination.value.total = coupons.value.length
    }
  } catch (error) {
    console.error('Error loading data:', error)
    Message.error('Không thể tải dữ liệu')
  } finally {
    loadingData.value = false
  }
}

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

    const invoiceId = parseInt(currentOrder.value.id)
    const voucherId = coupon.id

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
      paymentForm.value.cashReceived = finalPrice.value
    } else if (paymentForm.value.method === 'transfer') {
      paymentForm.value.transferReceived = finalPrice.value
    } else if (paymentForm.value.method === 'both') {
      paymentForm.value.cashReceived = finalPrice.value
      paymentForm.value.transferReceived = 0
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

const totalReceived = computed(() => (paymentForm.value?.cashReceived || 0) + (paymentForm.value?.transferReceived || 0))
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
  if (paymentForm.value.method === 'cash') return (paymentForm.value.cashReceived || 0) >= finalPrice.value
  if (paymentForm.value.method === 'transfer') return (paymentForm.value.transferReceived || 0) >= finalPrice.value
  if (paymentForm.value.method === 'both') return totalReceived.value >= finalPrice.value
  return true
})

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
      productPagination.value.current = 1
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
      paymentForm.value.cashReceived = finalPrice.value
      paymentForm.value.transferReceived = 0
    } else if (value === 'transfer') {
      paymentForm.value.transferReceived = finalPrice.value
      paymentForm.value.cashReceived = 0
    } else if (value === 'both') {
      paymentForm.value.cashReceived = finalPrice.value
      paymentForm.value.transferReceived = 0
    }
  } catch (error: any) {
    console.error('Lỗi cập nhật phương thức thanh toán:', error)
    Message.error(error.message || 'Có lỗi xảy ra khi cập nhật phương thức thanh toán')
  }
}

const handleOrderTypeChange = async (value: string) => {
  try {
    orderType.value = value as 'counter' | 'delivery'
    if (currentOrder.value?.id) {
      const invoiceId = parseInt(currentOrder.value.id)
      if (!Number.isNaN(invoiceId)) {
        await updateInvoiceShipping(invoiceId)
      }
    }
    if (value === 'delivery') {
      Message.info('Vui lòng nhập địa chỉ giao hàng')
      // Trigger shipping fee calculation for registered customer or walk-in customer
      // Use nextTick to ensure DOM is updated first
      await nextTick()
      console.log('[OrderTypeChange] Delivery mode activated, calculating shipping fee...')
      console.log('[OrderTypeChange] Walk-in:', isWalkIn.value)
      console.log('[OrderTypeChange] Location:', walkInLocation.value)

      if (walkInLocation.value.thanhPho && walkInLocation.value.quan && walkInLocation.value.phuong) {
        // Call GHN API to get real shipping fee
        try {
          const result = await calculateShippingFeeFromGHN(walkInLocation.value as any)
          console.log('[OrderTypeChange] GHN Fee:', result.fee)
          shippingFee.value = result.fee || 0
        } catch (error) {
          console.error('[OrderTypeChange] Error getting GHN fee, using fallback:', error)
          const fallbackFee = calculateShippingFee(walkInLocation.value as any, subtotal.value)
          shippingFee.value = fallbackFee || 0
        }
      } else {
        console.log('[OrderTypeChange] Address incomplete, skipping fee calculation')
      }
    } else {
      // Reset shipping fee when not in delivery mode
      shippingFee.value = 0
    }
  } catch (error: any) {
    console.error('Lỗi cập nhật loại đơn:', error)
    Message.error(error.message || 'Có lỗi xảy ra khi cập nhật loại đơn')
  }
}

const updateShippingFeeValue = (value: number) => {
  console.log('[UpdateShippingFee] Updating shipping fee to:', value)
  shippingFee.value = value
  console.log('[UpdateShippingFee] Shipping fee local state updated. Will be sent to server when confirming order.')
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
  const iframe = document.createElement('iframe')
  iframe.style.display = 'none'
  iframe.src = url
  document.body.appendChild(iframe)
  setTimeout(() => {
    document.body.removeChild(iframe)
  }, 1000)
}

const openMobileSession = async () => {
  if (finalPrice.value <= 0) {
    Message.warning('Giỏ hàng chưa có tổng tiền hợp lệ')
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
    await generateQrPayment(session.sessionId)
    const deepLink = `mobileadmin://qr?sessionId=${session.sessionId}`
    triggerDeepLink(deepLink)
    Message.success('Đã mở VietQR trên mobile')
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
})

const { showQRScanner, initQRScanner, closeQRScanner } = useQrScanner({
  allProductVariants,
  currentOrder,
  loadAllProducts,
  refreshProductStock,
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
  (newLocation) => {
    console.log('[WalkInLocationWatch] Location changed:', {
      province: newLocation.thanhPho,
      district: newLocation.quan,
      ward: newLocation.phuong,
      address: newLocation.diaChiCuThe,
    })
  },
  { deep: true }
)

// Watch for province change
watch(
  () => walkInLocation.value.thanhPho,
  async (newProvince) => {
    console.log('[AddressWatch] Walk-in Province changed to:', newProvince)
  }
)

// Watch for district change
watch(
  () => walkInLocation.value.quan,
  async (newDistrict) => {
    console.log('[AddressWatch] Walk-in District changed to:', newDistrict)
  }
)

// Watch for ward change
watch(
  () => walkInLocation.value.phuong,
  async (newWard) => {
    console.log('[AddressWatch] Walk-in Ward changed to:', newWard)
  }
)

// Watch for address detail change
watch(
  () => walkInLocation.value.diaChiCuThe,
  async (newAddress) => {
    console.log('[AddressWatch] Walk-in Address detail changed to:', newAddress)
  }
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
