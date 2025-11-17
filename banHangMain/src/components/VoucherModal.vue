<template>
  <div v-if="isOpen" class="voucher-modal-overlay" @click.self="closeModal">
    <div class="voucher-modal">
      <div class="modal-header">
        <h2>Chọn phiếu giảm giá</h2>
        <button class="close-btn" @click="closeModal">✕</button>
      </div>

      <div class="modal-body">
        <div v-if="loading" class="loading">Đang tải phiếu giảm giá...</div>

        <div v-else-if="filteredVouchers.length === 0" class="no-vouchers">
          <p>Không có phiếu giảm giá phù hợp</p>
        </div>

        <div v-else class="voucher-list">
          <div
            v-for="voucher in filteredVouchers"
            :key="voucher.id"
            class="voucher-item"
            :class="{ selected: selectedVoucherId === voucher.id }"
            @click="selectVoucher(voucher)"
          >
            <div class="voucher-header">
              <div class="voucher-code">{{ voucher.maPhieuGiamGia }}</div>
              <div class="voucher-discount">
                {{
                  voucher.loaiPhieuGiamGia
                    ? `Giảm ${formatCurrency(voucher.giaTriGiamGia)}`
                    : `Giảm ${voucher.giaTriGiamGia}%`
                }}
              </div>
            </div>

            <div class="voucher-info">
              <p class="voucher-name">{{ voucher.tenPhieuGiamGia }}</p>
              <p v-if="voucher.moTa" class="voucher-desc">{{ voucher.moTa }}</p>

              <!-- Type badge -->
              <div class="voucher-type">
                <span
                  class="type-badge"
                  :class="{ percentage: !voucher.loaiPhieuGiamGia }"
                >
                  {{ voucher.loaiPhieuGiamGia ? "Giảm VND" : "Giảm %" }}
                </span>
              </div>

              <!-- Conditions -->
              <div class="voucher-conditions">
                <div
                  v-if="voucher.hoaDonToiThieu && voucher.hoaDonToiThieu > 0"
                  class="condition"
                >
                  <span class="icon">📦</span>
                  <span
                    >Tối thiểu:
                    {{ formatCurrency(voucher.hoaDonToiThieu) }}</span
                  >
                </div>
                <div v-if="voucher.soLuongDung" class="condition">
                  <span class="icon">📋</span>
                  <span>Còn: {{ voucher.soLuongDung }}</span>
                </div>
              </div>

              <!-- Date range -->
              <div
                v-if="voucher.ngayBatDau && voucher.ngayKetThuc"
                class="voucher-date"
              >
                <span class="icon">📅</span>
                <small
                  >{{ formatDate(voucher.ngayBatDau) }} -
                  {{ formatDate(voucher.ngayKetThuc) }}</small
                >
              </div>

              <!-- Other conditions -->
              <div v-if="voucher.personalUsed" class="voucher-note">
                <span class="icon">✓</span>
                <small>Bạn đã sử dụng phiếu này rồi</small>
              </div>
            </div>

            <div v-if="selectedVoucherId === voucher.id" class="checkmark">
              ✓
            </div>
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn btn-cancel" @click="closeModal">Hủy</button>
        <button
          class="btn btn-confirm"
          :disabled="!selectedVoucherId || loading"
          @click="confirmSelection"
        >
          Áp dụng
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from "vue";
import type { Voucher } from "@/api/vouchers";
import {
  getActiveVouchersForCustomer,
  validateVoucherUsage,
  getVoucherDetailedInfo,
} from "@/api/vouchers";

interface Props {
  isOpen: boolean;
  customerId?: number;
  subtotal?: number;
  currentVoucherId?: number;
}

interface Emits {
  (e: "close"): void;
  (e: "select", voucher: Voucher): void;
}

const props = withDefaults(defineProps<Props>(), {
  subtotal: 0,
});

const emit = defineEmits<Emits>();

const loading = ref(false);
const vouchers = ref<Voucher[]>([]);
const selectedVoucherId = ref<number | null>(props.currentVoucherId || null);

// Filter vouchers by:
// 1. Status must be active
// 2. Must have remaining quantity
// 3. Must meet minimum order value
// 4. Must be within date range (if dates exist)
const filteredVouchers = computed(() => {
  const now = new Date();
  const filtered = vouchers.value.filter((v) => {
    // Check if active (trangThai can be true/false/null, treat null/undefined as active)
    const isActive = v.trangThai !== false && !v.deleted;

    // Check quantity (soLuongDung from API)
    const hasQuantity = (v.soLuongDung ?? 0) > 0;

    // Check minimum order value (hoaDonToiThieu from API)
    const meetsMinimum = props.subtotal >= (v.hoaDonToiThieu ?? 0);

    // Check date range (if dates exist)
    let isDateValid = true;
    if (v.ngayBatDau && v.ngayKetThuc) {
      isDateValid =
        new Date(v.ngayBatDau) <= now && now <= new Date(v.ngayKetThuc);
    }
    return isActive && hasQuantity && meetsMinimum && isDateValid;
  });
  return filtered;
});

watch(
  () => props.isOpen,
  async (newVal) => {
    if (newVal) {
      await loadVouchers();
    }
  }
);

async function loadVouchers() {
  loading.value = true;

  try {
    // Nếu không có customerId, lấy tất cả phiếu giảm giá công khai (cho khách lạ)
    // Nếu có customerId, lấy phiếu riêng cho khách hàng đó
    let data: Voucher[] = [];

    if (props.customerId === 0) {
      // For guests, get all public vouchers
      data = await getActiveVouchersForCustomer(props.customerId);
    } else {
      data = await getActiveVouchersForCustomer(props.customerId);
    }

    vouchers.value = data;
  } catch (error) {
    console.error(`[VoucherModal Error] Failed to load vouchers:`, error);
    vouchers.value = [];
  } finally {
    loading.value = false;
  }
}

function selectVoucher(voucher: Voucher) {
  selectedVoucherId.value = voucher.id;
}

function confirmSelection() {
  const selected = vouchers.value.find((v) => v.id === selectedVoucherId.value);
  if (selected) {
    emit("select", selected);
    closeModal();
  }
}

function closeModal() {
  emit("close");
  // Reset selection on close
  selectedVoucherId.value = props.currentVoucherId || null;
}

function formatCurrency(amount: number): string {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(amount);
}

function formatDate(dateString: string): string {
  try {
    const date = new Date(dateString);
    return date.toLocaleDateString("vi-VN");
  } catch {
    return dateString;
  }
}
</script>

<style scoped lang="scss">
.voucher-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.voucher-modal {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
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
  }

  .close-btn {
    background: none;
    border: none;
    font-size: 24px;
    cursor: pointer;
    color: #999;
    padding: 0;
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;

    &:hover {
      color: #333;
    }
  }
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;

  .loading,
  .no-vouchers {
    text-align: center;
    padding: 40px 20px;
    color: #999;
  }

  .voucher-list {
    display: grid;
    gap: 12px;
  }
}

.voucher-item {
  border: 2px solid #f0f0f0;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;

  &:hover {
    border-color: #f77234;
    background: #fef7f3;
  }

  &.selected {
    border-color: #f77234;
    background: #fff8f2;
  }

  .voucher-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;

    .voucher-code {
      font-weight: 600;
      font-size: 14px;
      color: #f77234;
      font-family: monospace;
    }

    .voucher-discount {
      font-weight: 600;
      color: #f77234;
      font-size: 14px;
    }
  }

  .voucher-info {
    .voucher-name {
      margin: 0 0 4px 0;
      font-weight: 500;
      color: #1d2129;
      font-size: 14px;
    }

    .voucher-desc {
      margin: 0 0 8px 0;
      font-size: 13px;
      color: #666;
    }

    // Type badge
    .voucher-type {
      margin-bottom: 8px;

      .type-badge {
        display: inline-block;
        padding: 4px 8px;
        border-radius: 4px;
        font-size: 12px;
        font-weight: 500;
        background: #f0f0f0;
        color: #666;

        &.percentage {
          background: #ffe8d6;
          color: #f77234;
        }
      }
    }

    // Conditions list
    .voucher-conditions {
      display: flex;
      flex-direction: column;
      gap: 6px;
      margin-bottom: 8px;

      .condition {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 12px;
        color: #666;

        .icon {
          font-size: 11px;
        }
      }
    }

    .voucher-date {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      color: #999;

      .icon {
        font-size: 11px;
      }
    }

    .voucher-note {
      display: flex;
      align-items: flex-start;
      gap: 6px;
      font-size: 12px;
      color: #f77234;
      background: #fff8f2;
      padding: 6px 8px;
      border-radius: 4px;
      margin-top: 8px;

      .icon {
        flex-shrink: 0;
        font-size: 11px;
      }
    }
  }

  .checkmark {
    position: absolute;
    top: 12px;
    right: 12px;
    width: 24px;
    height: 24px;
    background: #f77234;
    color: white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    font-size: 14px;
  }
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 20px;
  border-top: 1px solid #f0f0f0;
  justify-content: flex-end;

  .btn {
    padding: 10px 24px;
    border: 1px solid #ccc;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s ease;

    &.btn-cancel {
      background: white;
      color: #666;

      &:hover {
        border-color: #999;
      }
    }

    &.btn-confirm {
      background: #f77234;
      color: white;
      border-color: #f77234;

      &:hover:not(:disabled) {
        background: #e65f1f;
      }

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }
  }
}
</style>
