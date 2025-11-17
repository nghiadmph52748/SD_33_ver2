<template>
  <a-card class="payment-card">
    <template #title>
      <div style="display: flex; justify-content: space-between; align-items: center">
        <span>Thanh Toán</span>
        <a-select :model-value="orderType" placeholder="Loại đơn" style="width: 120px" @change="$emit('change:orderType', $event)">
          <a-option value="counter">Tại quầy</a-option>
          <a-option value="delivery">Giao hàng</a-option>
        </a-select>
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
          @click="$emit('open-voucher')"
        >
          <span style="font-weight: 500; text-align: left">Phiếu Giảm Giá</span>
          <span style="font-weight: 400; font-size: 12px; text-align: right; color: #999">
            {{ hasEligibleVouchers ? `${eligibleVouchersCount} voucher có thể dùng >` : 'Không có voucher phù hợp' }}
          </span>
        </a-button>
      </a-form-item>

      <VoucherAlmostEligible v-if="almostEligibleSuggestion" :suggestion="almostEligibleSuggestion" @open-voucher="$emit('open-voucher')" />

      <BestVoucherSuggestionCard
        v-if="!selectedCoupon && bestVoucher"
        :best-voucher="bestVoucher as any"
        :calculate-voucher-discount="calculateVoucherDiscount as any"
        @select="$emit('select-best')"
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
                @change="$emit('change:province', $event)"
                @update:model-value="$emit('change:province', $event)"
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
                @change="$emit('change:district', $event)"
                @update:model-value="$emit('change:district', $event)"
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
                @change="$emit('update:walkin-ward', $event)"
                @update:model-value="$emit('update:walkin-ward', $event)"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="Địa chỉ cụ thể" required>
              <a-input
                :model-value="walkInLocation.diaChiCuThe"
                placeholder="Số nhà, đường..."
                @update:model-value="$emit('update:walkin-address', $event)"
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
          @update:shipping-fee="$emit('update:shippingFee', $event)"
        />
      </div>

      <a-form-item :model="{}" label="Phương Thức Thanh Toán">
        <a-radio-group :model-value="paymentForm.method" @change="$emit('change:paymentMethod', $event)">
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
          :model-value="paymentForm.cashReceived"
          :min="0"
          :max="paymentForm.method === 'both' ? finalPrice : undefined"
          placeholder="Nhập số tiền mặt"
          style="width: 100%; height: 48px; font-size: 16px; font-weight: 500"
          :precision="0"
          :formatter="(value: number | string | undefined) => formatCurrency(Number(value) || 0)"
          :parser="(value: string) => parseFloat(value.replace(/[^\d]/g, '')) || 0"
          @update:model-value="$emit('update:cash', $event || 0)"
        />
      </a-form-item>

      <a-form-item
        v-if="paymentForm.method === 'transfer' || paymentForm.method === 'both'"
        label="Chuyển Khoản"
        class="transfer-input-container"
        style="transition: all 0.3s ease"
      >
        <a-input-number
          :model-value="paymentForm.transferReceived"
          :min="0"
          :max="paymentForm.method === 'both' ? finalPrice : undefined"
          placeholder="Nhập số tiền chuyển khoản"
          style="width: 100%; height: 48px; font-size: 16px; font-weight: 500"
          :precision="0"
          :formatter="(value: number | string | undefined) => formatCurrency(Number(value) || 0)"
          :parser="(value: string) => parseFloat(value.replace(/[^\d]/g, '')) || 0"
          @update:model-value="$emit('update:transfer', $event || 0)"
        />
      </a-form-item>

      <a-alert v-if="paymentForm.method === 'transfer' || paymentForm.method === 'both'" type="info" title="Chuyển Khoản" closable>
        <p>Vui lòng chuyển khoản theo thông tin cung cấp. Mã hoá đơn: {{ orderCode }}</p>
        <p v-if="paymentForm.method === 'both'">Số tiền chuyển khoản: {{ formatCurrency(paymentForm.transferReceived || 0) }}</p>
      </a-alert>

      <a-alert
        v-if="selectedCoupon"
        :title="`Voucher: ${selectedCoupon.tenPhieuGiamGia}`"
        type="success"
        closable
        @close="$emit('clear-voucher')"
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
          <span>Phí vận chuyển:</span>
          <strong style="color: #1890ff">{{ formatCurrency(shippingFee) }}</strong>
        </p>
        <p class="summary-row total">
          <span>Thành tiền:</span>
          <strong class="final-price">{{ formatCurrency(finalPrice) }}</strong>
        </p>
      </div>

      <div class="qr-session-panel" :class="{ 'qr-session-panel--active': !!qrSession }">
        <div class="qr-session-head">
          <div>
            <div class="qr-session-title">VietQR tại quầy</div>
            <div class="qr-session-desc">
              {{
                qrSession
                  ? 'Khách sẽ quét mã trên thiết bị mobile để hoàn tất thanh toán.'
                  : 'Chưa có mã VietQR. Hoàn tất giỏ hàng và nhấn “Mở trên mobile” để tạo mã.'
              }}
            </div>
          </div>
          <a-tag v-if="qrSession" :color="qrStatusTagColor" class="qr-status-tag">
            {{ qrStatusLabel }}
          </a-tag>
          <a-tag v-else color="default" class="qr-status-tag">CHƯA HIỂN THỊ</a-tag>
        </div>

        <div v-if="qrSyncing" class="qr-session-syncing">
          <icon-refresh class="spin" />
          <span>Đang đồng bộ giỏ hàng…</span>
        </div>

        <div v-else-if="qrSession" class="qr-session-body">
          <div class="qr-session-info">
            <div class="info-block">
              <p class="info-label">Mã phiên</p>
              <p class="info-value code">{{ qrSession.sessionId }}</p>
            </div>
            <div class="info-block">
              <p class="info-label">Thành tiền</p>
              <p class="info-value">{{ formatCurrency(finalPrice) }}</p>
            </div>
          </div>
          <p class="qr-session-expired" v-if="qrSession.expiresAt">Hết hạn sau: {{ formatQrExpires(qrSession.expiresAt) }}</p>
        </div>

        <div v-else class="qr-session-empty">
          <icon-qrcode class="qr-session-empty-icon" />
          <div>
            <p class="empty-title">Chưa tạo VietQR</p>
            <p class="empty-desc">Nhân viên hãy kiểm tra giỏ hàng, sau đó bấm “Mở trên mobile” để hiển thị mã.</p>
          </div>
        </div>

        <p v-if="qrSyncError" class="qr-session-error">
          <icon-info-circle style="margin-right: 4px" /> {{ qrSyncError }}
        </p>

        <a-space direction="vertical" size="small" style="width: 100%; margin-top: 12px">
          <a-button
            long
            type="primary"
            status="success"
            :ghost="true"
            :loading="qrSyncing"
            :disabled="!qrSession"
            @click="$emit('open-mobile')"
          >
            <template #icon>
              <icon-qrcode />
            </template>
            Mở trên mobile
          </a-button>
          <a-button long type="outline" :disabled="qrSyncing" @click="$emit('sync-qr')">
            <template #icon>
              <icon-refresh />
            </template>
            Đồng bộ giỏ hàng
          </a-button>
        </a-space>
      </div>

      <a-space direction="vertical" size="large" style="width: 100%; margin-top: 16px">
        <a-button type="primary" long size="large" :disabled="!canConfirmOrder" :loading="confirmLoading" @click="$emit('confirm-order')">
          <template #icon>
            <icon-check />
          </template>
          Xác Nhận ({{ finalPrice > 0 ? formatCurrency(finalPrice) : '0đ' }})
        </a-button>
        <a-button long @click="$emit('print')" :disabled="!hasItems">In Hoá Đơn</a-button>
      </a-space>
    </a-form>
  </a-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { IconCheck, IconInfoCircle, IconQrcode, IconRefresh } from '@arco-design/web-vue/es/icon'
import type { CouponApiModel } from '@/api/discount-management'
import BestVoucherSuggestionCard from './BestVoucherSuggestionCard.vue'
import VoucherAlmostEligible from './VoucherAlmostEligible.vue'
import ShippingFeeCalculator from './ShippingFeeCalculator.vue'
import { formatCurrency } from '../utils'

const props = defineProps<{
  orderType: 'counter' | 'delivery'
  orderCode: string
  paymentForm: { method: 'cash' | 'transfer' | 'both'; cashReceived: number; transferReceived: number; discountCode: string | null }
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

defineEmits<{
  (e: 'change:orderType', value: string): void
  (e: 'open-voucher'): void
  (e: 'change:paymentMethod', value: string): void
  (e: 'update:cash', value: number): void
  (e: 'update:transfer', value: number): void
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
  (e: 'sync-qr'): void
}>()

const qrStatusLabel = computed(() => {
  const status = props.qrSession?.status
  if (status === 'PAID') return 'ĐÃ THANH TOÁN'
  if (status === 'EXPIRED') return 'ĐÃ ĐÓNG'
  return 'ĐANG CHỜ'
})

const qrStatusTagColor = computed(() => {
  const status = props.qrSession?.status
  if (status === 'PAID') return 'green'
  if (status === 'EXPIRED') return 'orange'
  return 'blue'
})

const formatQrExpires = (value?: string) => {
  if (!value) return '—'
  try {
    const date = new Date(value)
    return date.toLocaleString('vi-VN', { hour12: false, hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit' })
  } catch {
    return value
  }
}
</script>

<style scoped lang="less">
.qr-session-panel {
  border: 1px dashed #e2e8f0;
  padding: 16px;
  border-radius: 12px;
  background: linear-gradient(135deg, #f8fafc, #fefefe);
  margin-top: 16px;
  transition: border-color 0.3s ease, background 0.3s ease;

  &--active {
    border-color: #c7d2fe;
    background: linear-gradient(135deg, rgba(199, 210, 254, 0.2), #fff);
  }
}

.qr-session-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.qr-session-title {
  font-weight: 700;
  font-size: 16px;
  color: #111;
}

.qr-session-desc {
  font-size: 13px;
  color: #64748b;
}

.qr-status-tag {
  font-weight: 600;
}

.qr-session-syncing {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #2563eb;
  margin-bottom: 10px;

  .spin {
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.qr-session-body {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 14px;
}

.qr-session-info {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.info-block {
  flex: 1;
  min-width: 140px;
}

.info-label {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #94a3b8;
  margin-bottom: 4px;
}

.info-value {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;

  &.code {
    font-family: 'JetBrains Mono', monospace;
    word-break: break-all;
  }
}

.qr-session-expired {
  margin-top: 8px;
  font-size: 12px;
  color: #94a3b8;
}

.qr-session-empty {
  display: flex;
  gap: 12px;
  align-items: center;
  border: 1px dashed #d7e3fc;
  border-radius: 10px;
  padding: 14px;
}

.qr-session-empty-icon {
  font-size: 28px;
  color: #94a3b8;
}

.empty-title {
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 2px;
}

.empty-desc {
  font-size: 12px;
  color: #6b7280;
}

.qr-session-error {
  font-size: 12px;
  color: #d32f2f;
  margin-top: 4px;
  display: flex;
  align-items: center;
}
</style>
