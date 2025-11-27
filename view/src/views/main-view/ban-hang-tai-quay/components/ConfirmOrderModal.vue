<template>
  <a-modal
    :visible="visible"
    title="Xác Nhận Đơn Hàng"
    width="700px"
    :mask-closable="false"
    @cancel="$emit('cancel')"
    class="confirm-order-modal"
  >
    <template #title>
      <div class="modal-title">
        <icon-check-circle class="title-icon" />
        <span>Xác Nhận Đơn Hàng</span>
      </div>
    </template>

    <div class="confirm-order-content">
      <!-- Order Info Header -->
      <div class="order-header">
        <div class="order-code">
          <span class="label">Mã đơn hàng:</span>
          <a-tag color="blue" class="code-tag">{{ orderCode }}</a-tag>
        </div>
        <div class="order-type">
          <a-tag :color="orderType === 'delivery' ? 'green' : 'orange'">
            {{ orderType === 'delivery' ? 'Giao hàng' : 'Tại quầy' }}
          </a-tag>
        </div>
      </div>

      <!-- Customer Info -->
      <div class="info-section">
        <div class="section-title">
          <icon-user />
          <span>Thông tin khách hàng</span>
        </div>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">Tên khách hàng:</span>
            <span class="info-value">{{ customerName || 'Khách lẻ' }}</span>
          </div>
          <div class="info-item" v-if="customerPhone">
            <span class="info-label">Số điện thoại:</span>
            <span class="info-value">{{ customerPhone }}</span>
          </div>
          <div class="info-item" v-if="customerAddress">
            <span class="info-label">Địa chỉ:</span>
            <span class="info-value">{{ customerAddress }}</span>
          </div>
        </div>
      </div>

      <!-- Products List -->
      <div class="info-section">
        <div class="section-title">
          <icon-file />
          <span>Danh sách sản phẩm</span>
        </div>
        <div class="products-list">
          <div v-for="(item, index) in items" :key="item.id || index" class="product-item">
            <div class="product-info">
              <div class="product-name">{{ item.productName }}</div>
              <div class="product-specs" v-if="item.tenMauSac || item.tenKichThuoc">
                <a-tag v-if="item.tenMauSac" size="small">{{ item.tenMauSac }}</a-tag>
                <a-tag v-if="item.tenKichThuoc" size="small">Size: {{ item.tenKichThuoc }}</a-tag>
              </div>
            </div>
            <div class="product-quantity">
              <span class="quantity">x{{ item.quantity }}</span>
            </div>
            <div class="product-price">
              <span class="price">{{ formatCurrency(item.price * item.quantity) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Payment Summary -->
      <div class="info-section">
        <div class="section-title">
          <icon-file />
          <span>Tổng kết thanh toán</span>
        </div>
        <div class="summary-list">
          <div class="summary-item">
            <span class="summary-label">Tổng tiền hàng:</span>
            <span class="summary-value">{{ formatCurrency(subtotal) }}</span>
          </div>
          <div class="summary-item" v-if="discountAmount > 0">
            <span class="summary-label">Giảm giá:</span>
            <span class="summary-value discount">-{{ formatCurrency(discountAmount) }}</span>
          </div>
          <div class="summary-item" v-if="shippingFee > 0">
            <span class="summary-label">Phí vận chuyển:</span>
            <span class="summary-value">{{ formatCurrency(shippingFee) }}</span>
          </div>
          <div class="summary-item total">
            <span class="summary-label">Thành tiền:</span>
            <span class="summary-value total">{{ formatCurrency(finalPrice) }}</span>
          </div>
        </div>
      </div>

      <!-- Payment Method -->
      <div class="info-section">
        <div class="section-title">
          <icon-info-circle />
          <span>Phương thức thanh toán</span>
        </div>
        <div class="payment-method-info">
          <a-tag :color="getPaymentMethodColor(paymentMethod)" class="method-tag">
            {{ getPaymentMethodText(paymentMethod) }}
          </a-tag>
          <div class="payment-amounts" v-if="paymentMethod === 'both'">
            <div class="amount-item">
              <span>Tiền mặt:</span>
              <span class="amount">{{ formatCurrency(cashReceived) }}</span>
            </div>
            <div class="amount-item">
              <span>Chuyển khoản:</span>
              <span class="amount">{{ formatCurrency(transferReceived) }}</span>
            </div>
          </div>
          <div class="payment-amounts" v-else>
            <div class="amount-item">
              <span>Số tiền nhận:</span>
              <span class="amount">{{ formatCurrency(paymentMethod === 'cash' ? cashReceived : transferReceived) }}</span>
            </div>
            <div class="amount-item" v-if="change > 0">
              <span>Tiền thừa:</span>
              <span class="amount change">{{ formatCurrency(change) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Voucher Info -->
      <div class="info-section" v-if="selectedCoupon">
        <div class="section-title">
          <icon-gift />
          <span>Phiếu giảm giá</span>
        </div>
        <div class="voucher-info">
          <a-tag color="green" class="voucher-tag">
            {{ selectedCoupon.tenPhieuGiamGia }}
          </a-tag>
          <span class="voucher-discount">-{{ formatCurrency(discountAmount) }}</span>
        </div>
      </div>
    </div>

    <!-- Footer Actions -->
    <template #footer>
      <div class="modal-footer">
        <a-button size="large" @click="$emit('cancel')" class="cancel-btn">Hủy</a-button>
        <a-button type="primary" size="large" :loading="confirmLoading" @click="$emit('confirm')" class="confirm-btn">
          <template #icon>
            <icon-check />
          </template>
          Xác nhận đơn hàng
        </a-button>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { IconCheckCircle, IconUser, IconFile, IconInfoCircle, IconGift, IconCheck } from '@arco-design/web-vue/es/icon'
import type { CouponApiModel } from '@/api/discount-management'

interface CartItem {
  id: string
  productId: string
  productName: string
  price: number
  discount: number
  quantity: number
  tenMauSac?: string
  tenKichThuoc?: string
}

const props = defineProps<{
  visible: boolean
  orderCode: string
  orderType: 'counter' | 'delivery'
  customerName?: string
  customerPhone?: string
  customerAddress?: string
  items: CartItem[]
  subtotal: number
  discountAmount: number
  shippingFee: number
  finalPrice: number
  paymentMethod: 'cash' | 'transfer' | 'both'
  cashReceived: number
  transferReceived: number
  selectedCoupon?: CouponApiModel | null
  confirmLoading: boolean
}>()

defineEmits<{ (e: 'cancel'): void; (e: 'confirm'): void }>()

const change = computed(() => {
  const totalReceived = props.cashReceived + props.transferReceived
  return Math.max(0, totalReceived - props.finalPrice)
})

function formatCurrency(value: number) {
  const n = Number(value) || 0
  try {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n)
  } catch {
    return `${n} đ`
  }
}

function getPaymentMethodText(method: string) {
  const methods: { [key: string]: string } = {
    cash: 'Tiền mặt',
    transfer: 'Chuyển khoản',
    both: 'Tiền mặt + Chuyển khoản',
  }
  return methods[method] || method
}

function getPaymentMethodColor(method: string) {
  const colors: { [key: string]: string } = {
    cash: 'orange',
    transfer: 'blue',
    both: 'purple',
  }
  return colors[method] || 'gray'
}
</script>

<style scoped lang="less">
.confirm-order-modal {
  :deep(.arco-modal-header) {
    padding: 20px 24px;
    border-bottom: 1px solid #e5e6eb;
  }

  :deep(.arco-modal-body) {
    padding: 0;
  }

  :deep(.arco-modal-footer) {
    padding: 16px 24px;
    border-top: 1px solid #e5e6eb;
  }
}

.modal-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
}

.title-icon {
  font-size: 20px;
  color: #165dff;
}

.confirm-order-content {
  padding: 24px;
  max-height: 70vh;
  overflow-y: auto;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, #f0f8ff 0%, #e6f4ff 100%);
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid #b3d8ff;
}

.order-code {
  display: flex;
  align-items: center;
  gap: 8px;
}

.order-code .label {
  font-size: 14px;
  color: #4e5969;
  font-weight: 500;
}

.code-tag {
  font-size: 14px;
  font-weight: 600;
  padding: 4px 12px;
}

.info-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e5e6eb;
}

.section-title .arco-icon {
  font-size: 18px;
  color: #165dff;
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
}

.info-label {
  font-size: 14px;
  color: #4e5969;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #1d2129;
  font-weight: 400;
  text-align: right;
  max-width: 60%;
  word-break: break-word;
}

.products-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: white;
  border-radius: 6px;
  border: 1px solid #e5e6eb;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  color: #1d2129;
}

.product-specs {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.product-quantity {
  min-width: 60px;
  text-align: center;
}

.quantity {
  font-size: 14px;
  font-weight: 600;
  color: #4e5969;
}

.product-price {
  min-width: 120px;
  text-align: right;
}

.price {
  font-size: 14px;
  font-weight: 600;
  color: #165dff;
}

.summary-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.summary-item.total {
  padding: 16px;
  background: white;
  border-radius: 6px;
  border: 2px solid #165dff;
  margin-top: 8px;
}

.summary-label {
  font-size: 14px;
  color: #4e5969;
  font-weight: 500;
}

.summary-value {
  font-size: 14px;
  color: #1d2129;
  font-weight: 500;
}

.summary-value.discount {
  color: #f53f3f;
}

.summary-value.total {
  font-size: 18px;
  font-weight: 700;
  color: #165dff;
}

.payment-method-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.method-tag {
  font-size: 14px;
  font-weight: 600;
  padding: 6px 12px;
  align-self: flex-start;
}

.payment-amounts {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: white;
  border-radius: 6px;
}

.amount-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.amount-item .amount {
  font-weight: 600;
  color: #165dff;
}

.amount-item .amount.change {
  color: #00b42a;
}

.voucher-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.voucher-tag {
  font-size: 14px;
  font-weight: 600;
  padding: 6px 12px;
}

.voucher-discount {
  font-size: 16px;
  font-weight: 700;
  color: #00b42a;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
}

.cancel-btn {
  min-width: 120px;
  height: 40px;
}

.confirm-btn {
  min-width: 160px;
  height: 40px;
  font-weight: 600;
}

/* Scrollbar styling */
.confirm-order-content::-webkit-scrollbar {
  width: 6px;
}

.confirm-order-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.confirm-order-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.confirm-order-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
