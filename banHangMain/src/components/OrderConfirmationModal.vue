<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h2>{{ $t("checkout.confirmOrder") || "Xác nhận đơn hàng" }}</h2>
        <button class="close-btn" @click="emit('close')">×</button>
      </div>

      <div class="modal-body">
        <!-- Customer Information -->
        <section class="info-section">
          <h3>{{ $t("checkout.customerInfo") || "Thông tin khách hàng" }}</h3>
          <div class="info-grid">
            <div class="info-item">
              <label>{{ $t("checkout.fullName") || "Họ và tên" }}</label>
              <span>{{ orderInfo.fullName }}</span>
            </div>
            <div class="info-item">
              <label>{{ $t("checkout.phone") || "Số điện thoại" }}</label>
              <span>{{ orderInfo.phone }}</span>
            </div>
            <div class="info-item">
              <label>{{ $t("checkout.email") || "Email" }}</label>
              <span>{{ orderInfo.email }}</span>
            </div>
            <div class="info-item full-width">
              <label>{{
                $t("checkout.deliveryAddress") || "Địa chỉ giao hàng"
              }}</label>
              <span>{{ orderInfo.address }}</span>
            </div>
          </div>
        </section>

        <!-- Payment Method -->
        <section class="info-section">
          <h3>{{ $t("checkout.paymentInfo") || "Thông tin thanh toán" }}</h3>
          <div class="info-grid">
            <div class="info-item full-width">
              <label>{{
                $t("checkout.paymentMethod") || "Phương thức thanh toán"
              }}</label>
              <span class="payment-badge" :class="paymentMethod">
                {{ paymentMethodLabel }}
              </span>
            </div>
          </div>
        </section>

        <!-- Order Items -->
        <section class="info-section">
          <h3>{{ $t("checkout.orderItems") || "Sản phẩm đặt hàng" }}</h3>
          <div class="items-list">
            <div
              v-for="item in orderInfo.items"
              :key="`${item.id}-${item.color}-${item.size}`"
              class="item-row"
            >
              <div class="item-info">
                <div class="item-name">{{ item.name }}</div>
                <div class="item-variant">
                  <span v-if="item.color" class="variant-badge color">{{
                    item.color
                  }}</span>
                  <span v-if="item.size" class="variant-badge size"
                    >{{ $t("checkout.size") || "Size" }}: {{ item.size }}</span
                  >
                </div>
              </div>
              <div class="item-qty">x{{ item.quantity }}</div>
              <div class="item-price">
                {{ formatCurrency(item.price * item.quantity) }}
              </div>
            </div>
          </div>
        </section>

        <!-- Order Summary -->
        <section class="info-section">
          <h3>{{ $t("checkout.orderSummary") || "Tóm tắt đơn hàng" }}</h3>
          <div class="summary-grid">
            <div class="summary-row">
              <span>{{ $t("cart.subtotal") || "Tạm tính" }}</span>
              <span>{{ formatCurrency(orderInfo.subtotal) }}</span>
            </div>
            <div class="summary-row">
              <span>{{ $t("cart.estimatedDelivery") || "Phí giao hàng" }}</span>
              <span>{{ formatCurrency(orderInfo.shippingFee) }}</span>
            </div>
            <div
              v-if="orderInfo.voucherDiscount > 0"
              class="summary-row discount"
            >
              <span>{{ $t("checkout.discount") || "Giảm giá" }}</span>
              <span>-{{ formatCurrency(orderInfo.voucherDiscount) }}</span>
            </div>
            <div class="summary-row divider">
              <span>{{ $t("cart.total") || "Tổng cộng" }}</span>
              <span class="total-amount">{{
                formatCurrency(orderInfo.total)
              }}</span>
            </div>
          </div>
        </section>

        <!-- Voucher Info -->
        <section v-if="orderInfo.voucher" class="info-section">
          <h3>
            {{ $t("checkout.voucherApplied") || "Phiếu giảm giá áp dụng" }}
          </h3>
          <div class="voucher-info">
            <div class="voucher-code">
              {{ orderInfo.voucher.maPhieuGiamGia }}
            </div>
            <div class="voucher-desc">
              <span v-if="orderInfo.voucher.loaiPhieuGiamGia">
                Giảm {{ formatCurrency(orderInfo.voucher.giaTriGiamGia) }}
              </span>
              <span v-else> Giảm {{ orderInfo.voucher.giaTriGiamGia }}% </span>
            </div>
          </div>
        </section>
      </div>

      <div class="modal-footer">
        <button class="btn btn-secondary" @click="emit('close')">
          {{ $t("checkout.cancel") || "Hủy" }}
        </button>
        <button class="btn btn-primary" @click="emit('confirm')">
          {{ confirmButtonText }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { formatCurrency } from "@/utils/currency";
import type { CartItem } from "@/stores/cart";
import type { Voucher } from "@/api/vouchers";

const { t } = useI18n();

interface OrderConfirmationInfo {
  fullName: string;
  phone: string;
  email: string;
  address: string;
  paymentMethod: "cod" | "vnpay";
  items: CartItem[];
  subtotal: number;
  shippingFee: number;
  voucherDiscount: number;
  total: number;
  voucher?: Voucher | null;
}

interface Props {
  isOpen: boolean;
  orderInfo: OrderConfirmationInfo;
  loading?: boolean;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  close: [];
  confirm: [];
}>();

const paymentMethodLabel = computed(() => {
  const method = props.orderInfo?.paymentMethod;
  if (method === "cod") {
    return t("checkout.cod") || "Thanh toán khi nhận hàng";
  } else if (method === "vnpay") {
    return "VNPAY";
  }
  return "Chưa chọn";
});

const confirmButtonText = computed(() => {
  const method = props.orderInfo?.paymentMethod;
  if (method === "cod") {
    return t("checkout.confirmAndPlaceOrder") || "Xác nhận đặt hàng";
  } else if (method === "vnpay") {
    return t("checkout.confirmAndPayment") || "Xác nhận và thanh toán";
  }
  return t("checkout.confirmOrder") || "Xác nhận";
});

const paymentMethod = computed(() => {
  return props.orderInfo?.paymentMethod || "";
});
</script>

<style scoped lang="scss">
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  animation: fadeIn 0.2s ease;

  @keyframes fadeIn {
    from {
      opacity: 0;
    }
    to {
      opacity: 1;
    }
  }
}

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  animation: slideUp 0.3s ease;

  @keyframes slideUp {
    from {
      transform: translateY(20px);
      opacity: 0;
    }
    to {
      transform: translateY(0);
      opacity: 1;
    }
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;

  h2 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #111;
  }
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s ease;

  &:hover {
    color: #333;
  }
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.info-section {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }

  h3 {
    margin: 0 0 12px 0;
    font-size: 14px;
    font-weight: 600;
    color: #111;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }
}

.info-grid {
  display: grid;
  gap: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;

  &.full-width {
    grid-column: 1 / -1;
  }

  label {
    font-size: 12px;
    color: #666;
    font-weight: 500;
    text-transform: uppercase;
    letter-spacing: 0.3px;
  }

  span {
    font-size: 14px;
    color: #111;
    word-break: break-word;
  }
}

.payment-badge {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  background: #f0f0f0;
  color: #333;
  width: fit-content;

  &.cod {
    background: #e8f5e9;
    color: #2e7d32;
  }

  &.vnpay {
    background: #e3f2fd;
    color: #1565c0;
  }
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #f9f9f9;
  padding: 12px;
  border-radius: 8px;
}

.item-row {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 12px;
  align-items: center;
  padding: 12px;
  background: white;
  border-radius: 6px;
  border: 1px solid #f0f0f0;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.item-name {
  font-size: 13px;
  font-weight: 500;
  color: #111;
  word-break: break-word;
}

.item-variant {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.variant-badge {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 11px;
  color: #666;
  background: #f0f0f0;

  &.color {
    background: #e8e8e8;
  }

  &.size {
    background: #f0f0f0;
  }
}

.item-qty {
  font-size: 13px;
  color: #666;
  text-align: center;
  white-space: nowrap;
}

.item-price {
  font-size: 13px;
  font-weight: 600;
  color: #111;
  text-align: right;
  white-space: nowrap;
}

.summary-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
  background: #f9f9f9;
  padding: 12px;
  border-radius: 8px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  padding: 6px 0;

  span:first-child {
    color: #666;
  }

  span:last-child {
    color: #111;
    font-weight: 500;
  }

  &.discount {
    span:last-child {
      color: #f77234;
      font-weight: 600;
    }
  }

  &.divider {
    border-top: 1px solid #e5e5e5;
    padding: 8px 0;
    font-weight: 600;

    span {
      color: #111;
    }
  }
}

.total-amount {
  font-size: 16px !important;
  color: #f77234 !important;
  font-weight: 700 !important;
}

.voucher-info {
  background: #fff8f2;
  border-left: 3px solid #f77234;
  padding: 12px;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.voucher-code {
  font-size: 13px;
  font-weight: 600;
  color: #f77234;
}

.voucher-desc {
  font-size: 12px;
  color: #666;
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  background: #f9f9f9;
}

.btn {
  flex: 1;
  padding: 12px 16px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.btn-primary {
  background: #f77234 !important;
  color: white !important;
  border: none !important;

  &:hover:not(:disabled) {
    background: #ff8c42 !important;
  }

  &:active:not(:disabled) {
    background: #e65820 !important;
  }
}

.btn-secondary {
  background: white;
  color: #666;
  border: 1px solid #e5e5e5;

  &:hover:not(:disabled) {
    background: #f5f5f5;
    border-color: #d0d0d0;
  }
}

@media (max-width: 600px) {
  .modal-overlay {
    padding: 12px;
  }

  .modal-content {
    max-height: 95vh;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .item-row {
    grid-template-columns: 1fr auto;
    gap: 8px;
  }

  .item-price {
    grid-column: 1 / -1;
    text-align: left;
  }
}
</style>
