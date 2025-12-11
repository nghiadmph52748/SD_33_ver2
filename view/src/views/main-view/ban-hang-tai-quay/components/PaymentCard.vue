<template>
  <a-card class="payment-card">
    <template #title>
      <div style="display: flex; justify-content: space-between; align-items: center">
        <span>Thanh Toán</span>
        <span style="font-size: 12px; color: #666">{{ orderType === 'delivery' ? 'Đơn giao hàng' : 'Đơn tại quầy' }}</span>
      </div>
    </template>

    <a-form :model="{}" layout="vertical">
      <a-form-item :model="{}">
        <a-button
          long
          size="large"
          type="secondary"
          :disabled="!hasEligibleVouchers"
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
          @click="emit('open-voucher')"
        >
          <span style="font-weight: 500; text-align: left">Phiếu Giảm Giá</span>
          <span style="font-weight: 400; font-size: 12px; text-align: right; color: #999">
            {{ hasEligibleVouchers ? `${eligibleVouchersCount} voucher có thể dùng >` : 'Không có voucher phù hợp' }}
          </span>
        </a-button>
      </a-form-item>

      <a-alert
        v-if="selectedCoupon"
        :title="`Voucher: ${selectedCoupon.tenPhieuGiamGia}`"
        type="success"
        closable
        style="margin-bottom: 12px"
        @close="emit('clear-voucher')"
      >
        <div style="display: flex; justify-content: space-between; align-items: center">
          <div>
            <strong>{{ selectedCoupon.maPhieuGiamGia }}</strong>
            <span style="margin-left: 8px; color: #52c41a">-{{ discountDisplay }}</span>
          </div>
          <div style="font-size: 12px; color: #666">
            <span v-if="selectedCoupon.hoaDonToiThieu">Min: {{ formatCurrency(Number(selectedCoupon.hoaDonToiThieu)) }}</span>
          </div>
        </div>
      </a-alert>

      <VoucherAlmostEligible v-if="almostEligibleSuggestion" :suggestion="almostEligibleSuggestion" @open-voucher="emit('open-voucher')" />

      <BestVoucherSuggestionCard
        v-if="!selectedCoupon && bestVoucher"
        :best-voucher="bestVoucher as any"
        :calculate-voucher-discount="calculateVoucherDiscount as any"
        @select="emit('select-best')"
      />

      <div v-if="orderType === 'delivery'" style="margin-bottom: 16px">
        <a-divider orientation="left" style="margin: 12px 0">Địa chỉ giao hàng</a-divider>

        <!-- Unified address form for both registered and walk-in customers -->
        <a-row :gutter="[12, 12]">
          <a-col :span="12">
            <a-form-item label="Tỉnh/Thành phố" required>
              <a-select
                :model-value="walkInLocation.thanhPho"
                placeholder="-- Chọn tỉnh/thành phố --"
                :options="provinces"
                @change="emit('change:province', $event)"
                @update:model-value="emit('change:province', $event)"
                option-label-prop="label"
                allow-search
                allow-clear
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="Quận/Huyện" required>
              <a-select
                :model-value="walkInLocation.quan"
                placeholder="-- Chọn quận/huyện --"
                :options="walkInLocation.districts"
                @change="emit('change:district', $event)"
                @update:model-value="emit('change:district', $event)"
                option-label-prop="label"
                allow-search
                allow-clear
                :disabled="!walkInLocation.thanhPho"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="Phường/Xã" required>
              <a-select
                :model-value="walkInLocation.phuong"
                placeholder="-- Chọn phường/xã --"
                :options="walkInLocation.wards"
                option-label-prop="label"
                allow-search
                allow-clear
                :disabled="!walkInLocation.quan"
                @change="emit('update:walkin-ward', $event)"
                @update:model-value="emit('update:walkin-ward', $event)"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="Địa chỉ cụ thể" required>
              <a-input
                :model-value="walkInLocation.diaChiCuThe"
                placeholder="Số nhà, đường..."
                @update:model-value="emit('update:walkin-address', $event)"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Shipping Fee Calculator Component -->
        <ShippingFeeCalculator
          :order-type="orderType"
          :selected-customer="selectedCustomer"
          :is-walk-in="isWalkIn"
          :walk-in-location="walkInLocation"
          :subtotal="subtotal"
          :shipping-fee-from-parent="shippingFee"
          @update:shipping-fee="emit('update:shippingFee', $event)"
        />
      </div>

      <a-form-item :model="{}" label="Phương Thức Thanh Toán">
        <a-radio-group :model-value="paymentForm.method" @change="emit('change:paymentMethod', $event)">
          <a-radio value="cash">Tiền Mặt</a-radio>
          <a-radio value="transfer">Chuyển Khoản</a-radio>
          <a-radio value="both">Cả Hai</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item
        v-if="paymentForm.method === 'cash' || paymentForm.method === 'both'"
        label="Tiền Mặt"
        class="cash-input-container"
        style="transition: all 0.3s ease"
      >
        <a-input-number
          class="payment-input"
          :model-value="paymentForm.cashReceived ?? undefined"
          :min="0"
          :max="paymentForm.method === 'both' ? finalPrice : undefined"
          placeholder="Nhập số tiền mặt"
          style="width: 100%; height: 48px; font-size: 16px; font-weight: 500"
          :precision="0"
          :formatter="(value: number | string | undefined) => formatCurrency(Number(value) || 0)"
          :parser="parseNumericInput"
          hide-button
          @update:model-value="handleCashInput"
        >
          <template #suffix>
            <a-button
              type="text"
              size="mini"
              class="clear-suffix-btn"
              :disabled="paymentForm.cashReceived === null || paymentForm.cashReceived === undefined"
              @click.stop="emit('update:cash', null)"
            >
              <icon-close />
            </a-button>
          </template>
        </a-input-number>
      </a-form-item>

      <a-form-item
        v-if="paymentForm.method === 'transfer' || paymentForm.method === 'both'"
        label="Chuyển Khoản"
        class="transfer-input-container"
        style="transition: all 0.3s ease"
      >
        <a-input-number
          class="payment-input"
          :model-value="paymentForm.transferReceived ?? undefined"
          :min="0"
          :max="paymentForm.method === 'both' ? finalPrice : undefined"
          placeholder="Nhập số tiền chuyển khoản"
          style="width: 100%; height: 48px; font-size: 16px; font-weight: 500"
          :precision="0"
          :formatter="(value: number | string | undefined) => formatCurrency(Number(value) || 0)"
          :parser="parseNumericInput"
          hide-button
          @update:model-value="handleTransferInput"
        >
          <template #suffix>
            <a-button
              type="text"
              size="mini"
              class="clear-suffix-btn"
              :disabled="paymentForm.transferReceived === null || paymentForm.transferReceived === undefined"
              @click.stop="emit('update:transfer', null)"
            >
              <icon-close />
            </a-button>
          </template>
        </a-input-number>
      </a-form-item>

      <a-alert v-if="paymentForm.method === 'transfer' || paymentForm.method === 'both'" type="info" title="Chuyển Khoản" closable>
        <p>Vui lòng chuyển khoản theo thông tin cung cấp. Mã hoá đơn: {{ orderCode }}</p>
        <p v-if="paymentForm.method === 'both'">Số tiền chuyển khoản: {{ formatCurrency(paymentForm.transferReceived || 0) }}</p>
      </a-alert>
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
        <p v-if="orderType === 'delivery'" class="summary-row">
          <span class="shipping-label">
            <span>Phí vận chuyển:</span>
          </span>
          <strong style="color: #1890ff">{{ formatCurrency(shippingFee) }}</strong>
        </p>
        <p class="summary-row total">
          <span>Thành tiền:</span>
          <strong class="final-price">{{ formatCurrency(finalPrice) }}</strong>
        </p>
      </div>

      <div class="qr-action">
        <a-button
          type="primary"
          status="success"
          long
          :loading="qrSyncing"
          :disabled="finalPrice <= 0 || !hasItems"
          @click="emit('open-mobile')"
        >
          <template #icon>
            <icon-qrcode />
          </template>
          Hiển thị QR
        </a-button>
        <p class="qr-hint">
          {{
            qrSession
              ? 'Sẵn sàng mở màn hình thanh toán cho khách quét.'
              : 'Hoàn tất giỏ hàng để tạo màn hình thanh toán trước khi hiển thị.'
          }}
        </p>
        <p v-if="qrSyncError" class="qr-session-error">
          <icon-info-circle style="margin-right: 4px" />
          {{ qrSyncError }}
        </p>
      </div>

      <div class="reset-session-action">
        <a-button type="outline" status="warning" long @click="emit('reset-qr-session')">Reset Màn Hình Thanh Toán</a-button>
      </div>

      <a-space direction="vertical" size="large" style="width: 100%; margin-top: 16px">
        <a-button type="primary" long size="large" :disabled="!canConfirmOrder" :loading="confirmLoading" @click="emit('confirm-order')">
          <template #icon>
            <icon-check />
          </template>
          Xác Nhận ({{ finalPrice > 0 ? formatCurrency(finalPrice) : '0đ' }})
        </a-button>
        <a-button long @click="emit('print')" :disabled="!hasItems">In Hoá Đơn</a-button>
      </a-space>
    </a-form>
  </a-card>
</template>

<script setup lang="ts">
import { IconCheck, IconClose, IconInfoCircle, IconQrcode } from '@arco-design/web-vue/es/icon'
import type { CouponApiModel } from '@/api/discount-management'
import BestVoucherSuggestionCard from './BestVoucherSuggestionCard.vue'
import VoucherAlmostEligible from './VoucherAlmostEligible.vue'
import ShippingFeeCalculator from './ShippingFeeCalculator.vue'
import { formatCurrency } from '../utils'
import ghnLogoUrl from '@/assets/logo-ghn.png'

const props = defineProps<{
  orderType: 'counter' | 'delivery'
  orderCode: string
  paymentForm: {
    method: 'cash' | 'transfer' | 'both'
    cashReceived: number | null
    transferReceived: number | null
    discountCode: string | null
  }
  subtotal: number
  discountAmount: number
  shippingFee: number
  finalPrice: number
  canConfirmOrder: boolean
  confirmLoading: boolean
  hasItems: boolean
  hasEligibleVouchers: boolean
  eligibleVouchersCount: number
  selectedCustomer: { address?: string } | null
  isWalkIn: boolean
  bestVoucher: CouponApiModel | null
  almostEligibleSuggestion?: any
  calculateVoucherDiscount: (c: CouponApiModel | null | undefined) => number
  provinces: Array<{ value: string; label: string; code: number }>
  walkInLocation: {
    thanhPho: string
    quan: string
    phuong: string
    diaChiCuThe: string
    districts: Array<{ value: string; label: string; code: number }>
    wards: Array<{ value: string; label: string }>
  }
  selectedCoupon: CouponApiModel | null
  discountDisplay: string
  qrSession?: { sessionId: string; status?: string; expiresAt?: string } | null
  qrSyncing?: boolean
  qrSyncError?: string | null
}>()

const emit = defineEmits<{
  (e: 'change:orderType', value: string): void
  (e: 'open-voucher'): void
  (e: 'change:paymentMethod', value: string): void
  (e: 'update:cash', value: number | null): void
  (e: 'update:transfer', value: number | null): void
  (e: 'clear-voucher'): void
  (e: 'confirm-order'): void
  (e: 'select-best'): void
  (e: 'print'): void
  (e: 'update:shippingFee', value: number): void
  (e: 'change:province', value: string): void
  (e: 'change:district', value: string): void
  (e: 'update:walkin-address', value: string): void
  (e: 'update:walkin-ward', value: string): void
  (e: 'open-mobile'): void
  (e: 'reset-qr-session'): void
}>()

const ghnLogo = ghnLogoUrl

const parseNumericInput = (value: string) => {
  if (!value) return undefined
  const digits = value.replace(/[^\d]/g, '')
  return digits ? Number(digits) : undefined
}

const handleCashInput = (value: number | undefined) => {
  emit('update:cash', typeof value === 'number' && Number.isFinite(value) ? value : null)
}

const handleTransferInput = (value: number | undefined) => {
  emit('update:transfer', typeof value === 'number' && Number.isFinite(value) ? value : null)
}
</script>

<style scoped lang="less">
.payment-input {
  :deep(.arco-input-number-suffix) {
    display: flex;
    align-items: center;
  }
}

.clear-suffix-btn {
  padding: 0;
  min-width: auto;
  color: #9ca3af;
}

.clear-suffix-btn:hover {
  color: #ef4444;
}

.clear-suffix-btn[disabled] {
  color: #d1d5db;
}

.qr-action {
  margin-top: 16px;
  padding: 16px;
  border: 1px dashed #e2e8f0;
  border-radius: 12px;
  background: #f8fafc;
  text-align: center;
}

.qr-hint {
  margin-top: 12px;
  font-size: 13px;
  color: #6b7280;
}

.qr-session-error {
  font-size: 12px;
  color: #d32f2f;
  margin-top: 4px;
  display: flex;
  align-items: center;
}

.reset-session-action {
  margin-top: 12px;
}

.shipping-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.shipping-logo {
  height: 18px;
  width: auto;
  object-fit: contain;
}
</style>
