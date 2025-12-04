<template>
  <div class="invoice-detail-page">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">Chi tiết hóa đơn</h1>
      </div>
      <div class="header-right">
        <a-button @click="goBack" class="back-button">
          <template #icon>
            <icon-arrow-left />
          </template>
          Quay lại
        </a-button>
        <a-button type="primary" @click="showUpdateModal" class="update-button">
          <template #icon>
            <icon-edit />
          </template>
          Cập nhật
        </a-button>
        <a-button type="primary" @click="goToPrintPage" class="print-button">
          <template #icon>
            <icon-printer />
          </template>
          In hóa đơn
        </a-button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <a-spin />
      <p>Đang tải thông tin hóa đơn...</p>
    </div>

    <!-- Invoice Content -->
    <div v-else-if="invoice" class="invoice-content">
      <!-- Horizontal Timeline Status -->
      <a-card class="timeline-status-card" :bordered="false">
        <div class="horizontal-timeline" :key="`timeline-${timelineData.length}-${invoice?.id}`">
          <div
            v-for="(stage, index) in statusStages"
            :key="`${stage.key}-${stage.time}`"
            class="timeline-stage"
            :class="{ active: stage.active, completed: stage.completed }"
          >
            <div class="stage-connector" v-if="index > 0" :class="{ active: stage.active || stage.completed }"></div>
            <div class="stage-icon" :class="{ active: stage.active, completed: stage.completed }">
              <component :is="stage.icon" v-if="stage.icon" />
            </div>
            <div class="stage-label">{{ stage.label }}</div>
            <div class="stage-time" :key="`time-${stage.key}-${stage.time}`">{{ stage.time }}</div>
          </div>
        </div>
      </a-card>

      <!-- Order Info and Customer Info - 2 Columns -->
      <a-row :gutter="24" class="info-row">
        <!-- Thông tin đơn hàng - Left Column -->
        <a-col :span="12">
          <a-card class="order-info-card" :bordered="false">
            <template #title>
              <div class="card-header">
                <icon-file />
                <span>Thông tin đơn hàng</span>
              </div>
            </template>
            <div class="info-list-compact">
              <div class="info-item-compact">
                <span class="label">Mã đơn hàng:</span>
                <a-tag color="blue" class="value-tag">
                  {{ invoice.maHoaDon || `HD${String(invoice.id).padStart(6, '0')}` }}
                </a-tag>
              </div>
              <div class="info-item-compact">
                <span class="label">Loại đơn:</span>
                <a-tag :color="invoice.loaiDon ? 'blue' : 'green'" class="value-tag">
                  {{ invoice.loaiDon ? 'online' : 'tại quầy' }}
                </a-tag>
              </div>
              <div class="info-item-compact">
                <span class="label">Trạng thái:</span>
                <a-tag :color="getStatusColorByName(currentStageStatus)" class="value-tag">
                  {{ currentStageStatus }}
                </a-tag>
              </div>
              <div class="info-item-compact">
                <span class="label">Phiếu giảm giá:</span>
                <span class="value">{{ invoice.maPhieuGiamGia || 'Không có' }}</span>
              </div>
              <div class="info-item-compact">
                <span class="label">Ngày đặt:</span>
                <span class="value">{{ ngayDat }}</span>
              </div>
            </div>
          </a-card>
        </a-col>

        <!-- Thông tin khách hàng - Right Column -->
        <a-col :span="12">
          <a-card class="customer-info-card" :bordered="false">
            <template #title>
              <div class="card-header">
                <icon-user />
                <span>Thông tin khách hàng</span>
              </div>
            </template>
            <div class="info-list-compact">
              <div class="info-item-compact">
                <span class="label">Tên khách hàng:</span>
                <span class="value">{{ invoice.tenNguoiNhan || invoice.tenKhachHang || 'Khách lẻ' }}</span>
              </div>
              <div class="info-item-compact">
                <span class="label">Số điện thoại:</span>
                <span class="value">{{ invoice.soDienThoaiNguoiNhan || invoice.soDienThoai || 'Chưa có' }}</span>
              </div>
              <div class="info-item-compact">
                <span class="label">Địa chỉ:</span>
                <span class="value">{{ invoice.diaChiNhanHang || invoice.diaChiNguoiNhan || invoice.diaChi || 'Chưa có' }}</span>
              </div>
              <div class="info-item-compact">
                <span class="label">Email:</span>
                <span class="value">{{ invoice.emailNguoiNhan || invoice.email || 'Chưa có' }}</span>
              </div>
              <div class="info-item-compact">
                <span class="label">Ghi chú:</span>
                <span class="value">{{ invoice.ghiChu || 'Không có' }}</span>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- Danh sách sản phẩm đã bán -->
      <a-card class="product-list-card" :bordered="false">
        <template #title>
          <div class="card-header">
            <icon-file />
            <span>Danh sách sản phẩm đã bán</span>
          </div>
        </template>
        <div class="product-list">
          <ProductTableFromInvoice
            v-if="invoice && invoice.id"
            :invoice-id="invoice.id"
            :show-actions="false"
            :page-size="10"
            @view-detail="handleViewProductDetail"
          />
          <div v-else class="no-products">
            <icon-file />
            <p>Chưa có sản phẩm nào</p>
          </div>
        </div>
      </a-card>

      <!-- History and Summary - 2 Columns -->
      <a-row :gutter="24" class="history-summary-row">
        <!-- Left Column: History -->
        <a-col :span="12">
          <!-- Lịch sử thanh toán -->
          <a-card class="payment-history-card" :bordered="false">
            <template #title>
              <div class="card-header">
                <icon-file />
                <span>Lịch sử thanh toán</span>
              </div>
            </template>
            <div class="history-list">
              <div v-if="paymentHistory.length > 0">
                <div v-for="(payment, index) in paymentHistory" :key="index" class="history-item">
                  <div class="history-dot"></div>
                  <div class="history-content">
                    <div class="history-main">
                      <span class="history-title">{{ payment.method }} - {{ formatCurrency(payment.amount) }}</span>
                    </div>
                    <div class="history-meta">
                      <a-tag
                        color="green"
                        size="small"
                        :title="`Mã hình thức thanh toán: ${payment.code}\nTra cứu trong bảng hinh_thuc_thanh_toan (ma_hinh_thuc_thanh_toan)`"
                      >
                        {{ payment.code }}
                      </a-tag>
                      <span class="history-user">({{ payment.userCode }})</span>
                    </div>
                  </div>
                  <div class="history-date">{{ formatDateShort(payment.date) }}</div>
                </div>
              </div>
              <div v-else class="no-history">
                <p>Chưa có lịch sử thanh toán</p>
              </div>
            </div>
          </a-card>

          <!-- Lịch sử hóa đơn -->
          <a-card class="invoice-history-card" :bordered="false" style="margin-top: 16px">
            <template #title>
              <div class="card-header">
                <icon-file />
                <span>Lịch sử hóa đơn</span>
              </div>
            </template>
            <div class="history-list">
              <div v-if="invoiceHistory.length > 0">
                <div v-for="(item, index) in invoiceHistory" :key="index" class="history-item">
                  <div class="history-dot"></div>
                  <div class="history-content">
                    <div class="history-main">
                      <span class="history-title">{{ item.action }}</span>
                    </div>
                    <div class="history-meta">
                      <span class="history-user">{{ item.userCode }}</span>
                    </div>
                  </div>
                  <div class="history-date">{{ formatDateTime(item.date) }}</div>
                </div>
              </div>
              <div v-else class="no-history">
                <p>Chưa có lịch sử hóa đơn</p>
              </div>
            </div>
          </a-card>
        </a-col>

        <!-- Right Column: Summary -->
        <a-col :span="12">
          <a-card class="summary-card" :bordered="false">
            <template #title>
              <div class="card-header">
                <icon-file />
                <span>Tổng kết đơn hàng</span>
              </div>
            </template>
            <div class="summary-content">
              <div class="summary-section">
                <div class="summary-row">
                  <div class="summary-label-wrapper">
                    <icon-file class="summary-icon" />
                    <span class="summary-label">Tổng tiền hàng:</span>
                  </div>
                  <span class="summary-value">{{ formatCurrency(calculatedTongTien) }}</span>
                </div>
                <div class="summary-row" v-if="calculatedDiscountAmount > 0">
                  <div class="summary-label-wrapper">
                    <icon-check-circle class="summary-icon" />
                    <span class="summary-label">Giảm giá:</span>
                  </div>
                  <span class="summary-value discount">-{{ formatCurrency(calculatedDiscountAmount) }}</span>
                </div>
                <div
                  class="summary-row"
                  v-if="
                    calculatedShippingGross > 0 || calculatedShippingBase > 0 || calculatedSurchargeAmount > 0 || calculatedRefundAmount > 0
                  "
                >
                  <div class="summary-label-wrapper">
                    <icon-send class="summary-icon" />
                    <span class="summary-label">Phí giao hàng:</span>
                  </div>
                  <span class="summary-value">{{ formatCurrency(calculatedShippingBase) }}</span>
                </div>
                <div class="summary-row" v-if="calculatedSurchargeAmount > 0">
                  <div class="summary-label-wrapper">
                    <icon-send class="summary-icon" />
                    <span class="summary-label">Phụ phí phát sinh:</span>
                  </div>
                  <span class="summary-value surcharge">+{{ formatCurrency(calculatedSurchargeAmount) }}</span>
                </div>
                <div class="summary-row" v-if="calculatedRefundAmount > 0">
                  <div class="summary-label-wrapper">
                    <icon-send class="summary-icon" />
                    <span class="summary-label">Hoàn phí:</span>
                  </div>
                  <span class="summary-value refund">-{{ formatCurrency(calculatedRefundAmount) }}</span>
                </div>
                <div class="summary-row total-row">
                  <div class="summary-label-wrapper">
                    <icon-check-circle class="summary-icon" />
                    <span class="summary-label">Thành tiền:</span>
                  </div>
                  <span class="summary-value total">{{ formatCurrency(calculatedFinalTotal) }}</span>
                </div>
                <div class="summary-row">
                  <div class="summary-label-wrapper">
                    <icon-check-circle class="summary-icon" />
                    <span class="summary-label">Đã thanh toán:</span>
                  </div>
                  <span class="summary-value" :class="paymentStatusClass">{{ formatCurrency(calculatedPaidAmount) }}</span>
                </div>
                <div class="summary-row" v-if="calculatedOverpaidAmount > 0">
                  <div class="summary-label-wrapper">
                    <icon-check-circle class="summary-icon" />
                    <span class="summary-label">Thanh toán dư:</span>
                  </div>
                  <span class="summary-value overpaid">+{{ formatCurrency(calculatedOverpaidAmount) }}</span>
                </div>
                <div class="summary-row" v-if="calculatedRemainingAmount > 0">
                  <div class="summary-label-wrapper">
                    <icon-clock-circle class="summary-icon" />
                    <span class="summary-label">Số tiền còn thiếu:</span>
                  </div>
                  <span class="summary-value outstanding">{{ formatCurrency(calculatedRemainingAmount) }}</span>
                </div>
                <div class="summary-status" v-if="invoice.trangThai">
                  <icon-check-circle class="status-icon" />
                  <span>Đã xác nhận</span>
                </div>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- Error State -->
    <div v-else class="error-state">
      <a-result status="error" title="Không tìm thấy hóa đơn" sub-title="Hóa đơn bạn đang tìm kiếm không tồn tại hoặc đã bị xóa.">
        <template #extra>
          <a-button type="primary" @click="goBack">Quay lại danh sách</a-button>
        </template>
      </a-result>
    </div>

    <!-- Update Invoice Modal -->
    <a-modal v-model:visible="updateModalVisible" title="Cập Nhật Thông Tin" :width="600" :mask-closable="false" class="update-modal">
      <template #title>
        <div class="modal-title">
          <icon-edit class="title-icon" />
          <span>Cập Nhật Thông Tin</span>
        </div>
      </template>
      <template #close-icon>
        <icon-close class="close-icon" />
      </template>

      <a-tabs v-model:active-key="activeUpdateTab" type="line" class="update-tabs">
        <a-tab-pane key="order" title="Thông Tin Đơn Hàng">
          <a-form :model="updateForm" layout="vertical" class="update-form">
            <a-form-item label="Mã đơn hàng">
              <a-input v-model="updateForm.maHoaDon" disabled />
            </a-form-item>
            <a-form-item label="Loại đơn">
              <a-select v-model="updateForm.loaiDon" placeholder="Chọn loại đơn" disabled>
                <a-option :value="true">online</a-option>
                <a-option :value="false">tại quầy</a-option>
              </a-select>
            </a-form-item>
            <a-form-item label="Trạng thái">
              <a-select v-model="updateForm.trangThaiText" placeholder="Chọn trạng thái" :disabled="statusEditingLocked">
                <a-option v-for="status in statusOptions" :key="status" :value="status">
                  {{ status }}
                </a-option>
              </a-select>
            </a-form-item>
            <!-- Show payment input when completing an order that still has an outstanding balance -->
            <a-form-item v-if="showPaymentInput" label="Số tiền thu thêm">
              <a-input-number
                v-model="updateForm.soTienDaThanhToan"
                :precision="0"
                :step="1000"
                :max="calculatedRemainingAmount"
                placeholder="Nhập số tiền thu thêm"
                :min="0"
              />
              <div
                class="payment-input-hint"
                :class="{ warning: remainingAmountForCompletion > 0, success: remainingAmountForCompletion === 0 }"
              >
                <template v-if="remainingAmountForCompletion > 0">
                  Đơn hàng vẫn còn thiếu {{ formatCurrency(remainingAmountForCompletion) }} sau khi thu thêm.
                </template>
                <template v-else-if="paymentCompletionDelta > 0">
                  Số tiền thu thêm vượt quá tổng tiền sau giảm {{ formatCurrency(paymentCompletionDelta) }}.
                </template>
                <template v-else>Đã thu đủ số tiền còn thiếu.</template>
              </div>
            </a-form-item>
          </a-form>
        </a-tab-pane>
        <a-tab-pane key="customer" title="Thông Tin Khách Hàng">
          <a-form :model="updateForm" layout="vertical" class="update-form">
            <a-form-item label="Tên khách hàng">
              <a-input v-model="updateForm.tenNguoiNhan" placeholder="Nhập tên khách hàng" />
            </a-form-item>
            <a-form-item label="Số điện thoại">
              <a-input v-model="updateForm.soDienThoaiNguoiNhan" placeholder="Nhập số điện thoại" />
            </a-form-item>
            <a-form-item label="Email">
              <a-input v-model="updateForm.emailNguoiNhan" placeholder="Nhập email" type="email" />
            </a-form-item>
            <a-form-item label="Ghi chú">
              <a-textarea v-model="updateForm.ghiChu" placeholder="Nhập ghi chú (nếu có)" :auto-size="{ minRows: 2, maxRows: 4 }" />
            </a-form-item>
          </a-form>
        </a-tab-pane>
        <a-tab-pane key="address" title="Địa Chỉ Giao Hàng">
          <a-form :model="updateForm" layout="vertical" class="update-form">
            <div class="address-info-header">
              <span class="address-info-title">Thông tin địa chỉ</span>
              <a-tag v-if="isAddressLocked" color="red">{{ addressDisableReason }}</a-tag>
            </div>
            <a-row :gutter="[12, 12]">
              <a-col :span="12">
                <a-form-item label="Tỉnh/Thành phố" required>
                  <a-select
                    v-model="updateLocationForm.thanhPho"
                    placeholder="-- Chọn tỉnh/thành phố --"
                    :options="provinces"
                    @change="onProvinceChange"
                    option-label-prop="label"
                    allow-search
                    allow-clear
                    :disabled="isAddressLocked"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="Quận/Huyện" required>
                  <a-select
                    v-model="updateLocationForm.quan"
                    placeholder="-- Chọn quận/huyện --"
                    :options="districtsList"
                    @change="onDistrictChange"
                    option-label-prop="label"
                    allow-search
                    allow-clear
                    :disabled="!updateLocationForm.thanhPho || isAddressLocked"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="Phường/Xã" required>
                  <a-select
                    v-model="updateLocationForm.phuong"
                    placeholder="-- Chọn phường/xã --"
                    :options="wardsList"
                    option-label-prop="label"
                    allow-search
                    allow-clear
                    :disabled="!updateLocationForm.quan || isAddressLocked"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="Địa chỉ cụ thể" required>
                  <a-input v-model="updateLocationForm.diaChiCuThe" placeholder="Số nhà, đường..." :disabled="isAddressLocked" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-divider />
            <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 16px">
              <span style="font-weight: 500">Phí giao hàng</span>
            </div>
            <a-form-item label="Phí giao hàng (VNĐ)" v-if="calculatedShippingFee !== null">
              <a-space direction="vertical" fill>
                <div v-if="isCalculatingShippingFee" class="shipping-fee-loading">
                  <a-spin size="small" />
                  <span style="margin-left: 8px">Đang tính phí giao hàng...</span>
                </div>
                <div v-else-if="shippingFeeError" class="shipping-fee-error">
                  <span style="color: #f53f3f">{{ shippingFeeError }}</span>
                </div>
                <div v-else class="shipping-fee-info">
                  <a-input-number
                    v-model="updateForm.phiGiaoHang"
                    :min="0"
                    :step="1000"
                    :formatter="(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
                    :parser="(value) => value?.replace(/[^\d]/g, '') as any"
                    style="width: 100%"
                  />
                </div>
              </a-space>
            </a-form-item>
            <a-form-item label="Phụ phí phát sinh (VNĐ)" v-if="calculatedSurcharge !== null">
              <a-input-number
                v-model="calculatedSurcharge"
                disabled
                :formatter="(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
                :parser="(value) => value?.replace(/[^\d]/g, '') as any"
                :style="{
                  width: '100%',
                  color: calculatedSurcharge > 0 ? '#f53f3f' : '#86909c',
                  fontWeight: calculatedSurcharge > 0 ? 'bold' : 'normal',
                }"
              />
              <div
                :style="{
                  fontSize: '12px',
                  color: calculatedSurcharge > 0 ? '#f53f3f' : '#86909c',
                  marginTop: '4px',
                }"
              >
                <span v-if="calculatedSurcharge > 0">Phí phát sinh do thay đổi địa chỉ giao hàng (sẽ được cộng vào hóa đơn)</span>
                <span v-else-if="calculatedSurcharge < 0">Hoàn lại phí do thay đổi địa chỉ giao hàng (sẽ trừ khỏi hóa đơn)</span>
              </div>
            </a-form-item>
          </a-form>
        </a-tab-pane>
      </a-tabs>

      <template #footer>
        <div class="modal-footer">
          <a-button @click="closeUpdateModal" class="cancel-btn">Hủy</a-button>
          <a-button type="primary" @click="handleSaveUpdate" :loading="saving" class="save-btn">Lưu</a-button>
        </div>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { fetchTimelineByHoaDonId, type TimelineItem } from '@/api/timeline'
import { useUserStore } from '@/store'
import { Message } from '@arco-design/web-vue'
import { useOrderStatusNotification } from '@/composables/useOrderStatusNotification'
import { useOrderStatusAsyncNotification } from '@/composables/useOrderStatusAsyncNotification'
import {
  IconArrowLeft,
  IconPrinter,
  IconFile,
  IconUser,
  IconClockCircle,
  IconCheckCircle,
  IconSend,
  IconCheck,
  IconEdit,
  IconClose,
} from '@arco-design/web-vue/es/icon'
import { fetchProvinces, fetchDistrictsByProvinceCode, fetchWardsByDistrictCode } from '../ban-hang-tai-quay/services/locationService'
import { calculateShippingFeeFromGHN, type ShippingLocation } from '../ban-hang-tai-quay/services/shippingFeeService'
import {
  calculateShippingFeeChange,
  shouldUpdateShippingFee,
  formatShippingFeeInfo,
  createFeeUpdatePayload,
  type ShippingFeeChangeResult,
} from './services/shippingFeeService'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// Async notification setup (for status change feedback)
const asyncNotification = useOrderStatusAsyncNotification()

// Real-time notification setup
const { isConnected, notifications, lastNotification } = useOrderStatusNotification(parseInt(route.params.id as string), {
  onStatusChange: (notification) => {
    // Refresh invoice data when status changes
    if (notification.type === 'status_update') {
      fetchInvoiceDetail()
      fetchTimeline()
    }
  },
  onError: (error) => {
    console.error('Notification error:', error)
  },
  autoReload: false, // Don't auto-reload, let user see the changes first
})

// Data
const invoice = ref<any>(null)
const loading = ref(true)
const invoiceId = ref(route.params.id as string)
const timelineData = ref<TimelineItem[]>([])
const paymentMethods = ref<any[]>([])

// Update Modal
const updateModalVisible = ref(false)
const activeUpdateTab = ref('order')
const saving = ref(false)
const updateForm = ref({
  maHoaDon: '',
  loaiDon: false,
  trangThai: false,
  trangThaiText: '',
  tenNguoiNhan: '',
  soDienThoaiNguoiNhan: '',
  diaChiNhanHang: '',
  emailNguoiNhan: '',
  ghiChu: '',
  soTienDaThanhToan: 0, // Additional amount to collect when completing order
  phiGiaoHang: 0, // Shipping fee
})
const paymentAmountManuallyEdited = ref(false)
const isProgrammaticPaymentSync = ref(false)
const modalOriginalStatus = ref('Chờ xác nhận')

// Address Selection State
const updateLocationForm = ref({
  thanhPho: '',
  quan: '',
  phuong: '',
  diaChiCuThe: '',
})
const provinces = ref<any[]>([])
const districtsList = ref<any[]>([])
const wardsList = ref<any[]>([])
const isLoadingAddress = ref(false)
const isCalculatingShippingFee = ref(false)
const calculatedShippingFee = ref<number | null>(null)
const calculatedSurcharge = ref<number | null>(null) // Phụ phí phát sinh = phí mới - phí cũ
const shippingFeeError = ref<string>('')
const originalInvoiceLocation = ref<{ thanhPho: string; quan: string; phuong: string; diaChiCuThe: string }>({
  thanhPho: '',
  quan: '',
  phuong: '',
  diaChiCuThe: '',
})

const addressLockReason = ref<string | null>(null)
const isAddressLocked = computed(() => Boolean(addressLockReason.value))
const addressDisableReason = computed(() => addressLockReason.value)

// Helper functions
const formatDateTime = (dateString?: string | Date) => {
  if (!dateString) return 'N/A'

  let date: Date
  if (dateString instanceof Date) {
    date = dateString
  } else {
    date = new Date(dateString)
  }

  // Check if date is valid
  if (isNaN(date.getTime())) {
    return 'N/A'
  }

  const time = date.toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
  const dateStr = date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
  return `${time} ${dateStr}`
}

const formatDateShort = (dateString?: string | Date) => {
  if (!dateString) return 'N/A'

  let date: Date
  if (dateString instanceof Date) {
    date = dateString
  } else {
    // Handle LocalDate format from backend (YYYY-MM-DD)
    // If it's just a date string without time, add time to ensure correct parsing
    const dateStr = dateString.toString().trim()
    if (dateStr.match(/^\d{4}-\d{2}-\d{2}$/)) {
      // LocalDate format: add time to make it a valid datetime
      date = new Date(`${dateStr}T00:00:00`)
    } else {
      date = new Date(dateString)
    }
  }

  // Check if date is valid
  if (isNaN(date.getTime())) {
    return 'N/A'
  }

  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
}

const formatTimelineAction = (item: TimelineItem) => {
  const action = (item.hanhDong || '').trim()
  const newStatus = (item.trangThaiMoi || '').trim()
  const description = (item.moTa || item.ghiChu || '').trim()

  if (action && newStatus) {
    return `${action} → ${newStatus}`
  }
  if (action) {
    return action
  }
  if (newStatus) {
    return `Trạng thái: ${newStatus}`
  }
  if (description) {
    return description
  }
  return 'Cập nhật trạng thái'
}

const getHistoryUserCode = (item?: TimelineItem) => {
  if (item?.tenNhanVien) return item.tenNhanVien
  if (invoice.value?.tenNhanVien) return invoice.value.tenNhanVien
  if (invoice.value?.maNhanVien) return invoice.value.maNhanVien
  return 'Hệ thống'
}

const getStatusText = (status: any) => {
  // Xử lý cả boolean và string
  if (typeof status === 'boolean') {
    return status ? 'Hoàn thành' : 'Chờ xác nhận'
  }

  if (typeof status === 'string') {
    const statusTexts: { [key: string]: string } = {
      'Chờ xác nhận': 'Chờ xác nhận',
      'Chờ giao hàng': 'Đã xác nhận',
      'Đang giao': 'Đang giao',
      'Hoàn thành': 'Hoàn thành',
      'Đã hủy': 'Đã huỷ',
      'Đã thanh toán': 'Hoàn thành',
      'Chờ thanh toán': 'Chờ xác nhận',
      true: 'Hoàn thành',
      false: 'Chờ xác nhận',
    }
    return statusTexts[status] || status || 'Chưa xác định'
  }

  return 'Chưa xác định'
}

// Helper to get highest priority status from thongTinDonHangs
const getHighestPriorityStatusFromInvoice = (): string => {
  if (!invoice.value?.thongTinDonHangs || invoice.value.thongTinDonHangs.length === 0) {
    return 'Chờ xác nhận'
  }

  const priorityMap: Record<string, number> = {
    'Đã huỷ': 7,
    'Hoàn thành': 6,
    'Đã giao hàng': 5,
    'Đang giao hàng': 4,
    'Đang xử lý': 3,
    'Đã xác nhận': 2,
    'Chờ xác nhận': 1,
  }

  let highestStatus = 'Chờ xác nhận'
  let highestPriority = 0

  invoice.value.thongTinDonHangs.forEach((item: any) => {
    const status = normalizeStatusForFlow(item.tenTrangThaiDonHang || 'Chờ xác nhận')
    const priority = priorityMap[status] || 0
    if (priority > highestPriority) {
      highestPriority = priority
      highestStatus = status
    }
  })

  return highestStatus
}

const removeVietnameseDiacritics = (text: string) =>
  text
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim()

const normalizeStatusText = (text?: string | null) => {
  if (!text) return ''
  return removeVietnameseDiacritics(text)
}

const TIMELINE_STAGE_DEFINITIONS: Array<{
  key: string
  label: string
  keywords: string[]
  icon: any
  order: number
  variant?: 'cancelled'
}> = [
  {
    key: 'pending',
    label: 'Chờ xác nhận',
    keywords: ['cho xac nhan', 'cho thanh toan', 'tao don', 'tao hoa don', 'tao', 'dat hang'],
    icon: IconClockCircle,
    order: 1,
  },
  {
    key: 'confirmed',
    label: 'Đã xác nhận',
    keywords: [
      'da xac nhan',
      'xac nhan',
      'xac nhan don',
      'chờ giao hàng',
      'cho giao hang',
      'dang xu ly',
      'chuan bi',
      'dang chuan bi',
      'dong goi',
    ],
    icon: IconCheckCircle,
    order: 2,
  },
  {
    key: 'shipping',
    label: 'Đang giao hàng',
    keywords: ['dang giao', 'dang giao hang', 'dang van chuyen', 'giao hang', 'van chuyen', 'dang ship', 'ship hang'],
    icon: IconSend,
    order: 3,
  },
  {
    key: 'completed',
    label: 'Hoàn thành',
    keywords: [
      'hoan thanh',
      'da thanh toan',
      'thanh toan xong',
      'hoan tat',
      'da giao hang',
      'giao thanh cong',
      'giao hang thanh cong',
      'thanh toan thanh cong',
      'da thanh toan thanh cong',
    ],
    icon: IconCheck,
    order: 4,
  },
  {
    key: 'cancelled',
    label: 'Đã huỷ',
    keywords: ['da huy', 'huy don', 'don huy', 'huy hang', 'huy thanh cong'],
    icon: IconClose,
    order: 7,
    variant: 'cancelled',
  },
]

const ONLINE_CANONICAL_FLOW = TIMELINE_STAGE_DEFINITIONS.filter((def) => def.variant !== 'cancelled')
  .slice()
  .sort((a, b) => a.order - b.order)
  .map((def) => def.key)

const OFFLINE_CANONICAL_FLOW = ['pending', 'completed']

const isAddressChangeText = (text?: string | null) => {
  if (!text) return false
  const normalized = normalizeStatusText(text)
  return normalized.includes('dia chi')
}

const isAddressChangeEntry = (entry?: Partial<TimelineItem> | null) => {
  if (!entry) return false
  return isAddressChangeText(entry.trangThaiMoi) || isAddressChangeText(entry.hanhDong)
}

const parseTimelineTimestamp = (value?: unknown): number | null => {
  if (!value) return null
  try {
    if (value instanceof Date) {
      const time = value.getTime()
      return Number.isFinite(time) ? time : null
    }
    if (typeof value === 'number') {
      return Number.isFinite(value) ? value : null
    }
    if (typeof value === 'string') {
      const parsed = new Date(value)
      const time = parsed.getTime()
      return Number.isFinite(time) ? time : null
    }
    if (typeof value === 'object' && value !== null) {
      const maybeInstant = value as { epochSecond?: number; nano?: number }
      if (typeof maybeInstant.epochSecond === 'number') {
        const millis = maybeInstant.epochSecond * 1000 + Math.round((maybeInstant.nano || 0) / 1_000_000)
        return Number.isFinite(millis) ? millis : null
      }
    }
  } catch (error) {
    console.warn('Unable to parse timeline timestamp:', error)
  }
  return null
}

const resolveStageDefinitionFromStatus = (rawStatus: string) => {
  const normalized = normalizeStatusText(rawStatus)
  const definition = TIMELINE_STAGE_DEFINITIONS.find((def) => def.keywords.some((keyword) => normalized.includes(keyword)))

  if (definition) {
    const shouldUseDefinitionLabel =
      (definition.key === 'confirmed' &&
        (normalized.includes('cho giao hang') ||
          normalized.includes('dang xu ly') ||
          normalized.includes('chuan bi') ||
          normalized.includes('dong goi'))) ||
      (definition.key === 'shipping' &&
        (normalized.includes('giao hang') || normalized.includes('van chuyen') || normalized.includes('ship'))) ||
      (definition.key === 'completed' &&
        (normalized.includes('da giao hang') || normalized.includes('giao thanh cong') || normalized.includes('giao hang thanh cong'))) ||
      (definition.key === 'pending' && normalized.includes('cho thanh toan'))

    const customLabel = shouldUseDefinitionLabel || !rawStatus || rawStatus.trim().length === 0 ? definition.label : rawStatus.trim()
    return {
      key: definition.key,
      label: customLabel,
      icon: definition.icon,
      order: definition.order,
      variant: definition.variant,
    }
  }

  return {
    key: `custom-${normalized || 'unknown'}`,
    label: rawStatus && rawStatus.trim().length > 0 ? rawStatus.trim() : 'Trạng thái khác',
    icon: IconClockCircle,
    order: 98,
    variant: undefined as 'cancelled' | undefined,
  }
}

const STATUS_FLOW_ORDER: Record<string, number> = {
  'Chờ xác nhận': 1,
  'Chờ thanh toán': 1,
  'Chờ giao hàng': 2,
  'Đã xác nhận': 2,
  'Đang xử lý': 2,
  'Đang giao hàng': 3,
  'Đang giao': 3,
  'Hoàn thành': 4,
  'Đã thanh toán': 4,
  'Đã giao hàng': 4,
  'Đã huỷ': 5,
  'Đã hủy': 5,
}

const normalizeStatusForFlow = (status: string) => {
  const normalized = status?.trim()
  if (!normalized) {
    return 'Chờ xác nhận'
  }

  const mapping: Record<string, string> = {
    'Chờ thanh toán': 'Chờ xác nhận',
    'Chờ giao hàng': 'Đã xác nhận',
    'Đang xử lý': 'Đã xác nhận',
    'Đang giao': 'Đang giao hàng',
    'Đã hủy': 'Đã huỷ',
    'Đã thanh toán': 'Hoàn thành',
    'Đã giao hàng': 'Hoàn thành',
    'Giao hàng thành công': 'Hoàn thành',
    'Giao thành công': 'Hoàn thành',
  }

  return mapping[normalized] || normalized
}

const ONLINE_STATUS_FLOW = ['Chờ xác nhận', 'Đã xác nhận', 'Đang giao hàng', 'Hoàn thành']

const OFFLINE_STATUS_FLOW = ['Chờ xác nhận', 'Hoàn thành']

const CANCELLATION_STATUS = 'Đã huỷ'

// Helper: Get time from timeline by status/action
// Rebuild logic để đảm bảo mỗi status lấy đúng entry của nó
const getTimeFromTimeline = (statusKey: string): string => {
  try {
    // Priority 1: Use thongTinDonHangs from invoice (most reliable)
    if (invoice.value?.thongTinDonHangs && Array.isArray(invoice.value.thongTinDonHangs)) {
      const statusMap: Record<string, string> = {
        pending: 'Chờ xác nhận',
        confirmed: 'Đã xác nhận',
        shipping: 'Đang giao hàng',
        completed: 'Hoàn thành',
      }

      const targetStatus = statusMap[statusKey]
      if (targetStatus) {
        for (const item of invoice.value.thongTinDonHangs) {
          if (item.tenTrangThaiDonHang === targetStatus && item.thoiGian) {
            return formatDateTime(item.thoiGian)
          }
        }
      }
    }

    // Priority 2: Use timelineData from API (fallback)
    if (!timelineData.value || !Array.isArray(timelineData.value) || timelineData.value.length === 0) {
      if (statusKey === 'completed') {
        return formatDateTime(invoice.value?.ngayThanhToan || invoice.value?.ngayTao)
      }
      return formatDateTime(invoice.value?.ngayTao || invoice.value?.createAt)
    }

    // Sort timeline theo thời gian tăng dần (cũ nhất trước)
    const sortedTimeline = [...(Array.isArray(timelineData.value) ? timelineData.value : [])].sort((a, b) => {
      try {
        // Xử lý thoiGian có thể là string hoặc Instant object
        const timeA = a.thoiGian ? new Date(a.thoiGian).getTime() : 0
        const timeB = b.thoiGian ? new Date(b.thoiGian).getTime() : 0
        if (isNaN(timeA) && isNaN(timeB)) {
          return (a.id || 0) - (b.id || 0)
        }
        if (isNaN(timeA)) return 1
        if (isNaN(timeB)) return -1
        return timeA - timeB // Tăng dần: cũ nhất trước
      } catch {
        return 0
      }
    })

    // Helper function để normalize text
    const normalize = (text?: string): string => removeVietnameseDiacritics((text ?? '').toString())

    // Helper function để check match
    const matches = (item: any, patterns: string[]): boolean => {
      const hanhDong = normalize(item.hanhDong)
      const trangThaiMoi = normalize(item.trangThaiMoi)
      return patterns.some((pattern) => {
        const normalizedPattern = normalize(pattern)
        if (!normalizedPattern) {
          return false
        }
        return hanhDong.includes(normalizedPattern) || trangThaiMoi.includes(normalizedPattern)
      })
    }

    // Helper function để check exclude
    const excludes = (item: any, patterns: string[]): boolean => {
      const hanhDong = normalize(item.hanhDong)
      const trangThaiMoi = normalize(item.trangThaiMoi)
      return patterns.some((pattern) => {
        const normalizedPattern = normalize(pattern)
        if (!normalizedPattern) {
          return false
        }
        return hanhDong.includes(normalizedPattern) || trangThaiMoi.includes(normalizedPattern)
      })
    }

    // ========== PENDING: Chờ xác nhận ==========
    // Ưu tiên entry "Cập nhật" với trangThaiMoi="Chờ xác nhận" (mới nhất), sau đó mới đến entry "Tạo"
    if (statusKey === 'pending') {
      // Tìm từ cuối lên để lấy entry "Cập nhật" với "Chờ xác nhận" mới nhất
      for (let i = sortedTimeline.length - 1; i >= 0; i--) {
        const item = sortedTimeline[i]
        const hanhDongNorm = normalize(item.hanhDong || '')
        const trangThaiMoiNorm = normalize(item.trangThaiMoi || '')
        const isCapNhatChoXacNhan = hanhDongNorm === 'cap nhat' && trangThaiMoiNorm.includes('cho xac nhan')

        if (isCapNhatChoXacNhan && item.thoiGian) {
          return formatDateTime(item.thoiGian)
        }
      }

      // Nếu không có "Cập nhật", tìm entry "Tạo" hoặc "Tạo đơn hàng" từ đầu timeline
      for (const item of sortedTimeline) {
        const hanhDongNorm = normalize(item.hanhDong || '')
        const trangThaiMoiNorm = normalize(item.trangThaiMoi || '')
        // Backend trả về: hanhDong = "Tạo", trangThaiMoi = "Tạo đơn hàng"
        const isTao = hanhDongNorm === 'tao' || hanhDongNorm.includes('tao')
        const isTaoDonHang = trangThaiMoiNorm.includes('tao don hang')
        const isChoXacNhan = trangThaiMoiNorm.includes('cho xac nhan')

        if ((isTao || isTaoDonHang || isChoXacNhan) && item.thoiGian) {
          return formatDateTime(item.thoiGian)
        }
      }

      // Fallback: entry đầu tiên trong timeline (entry sớm nhất)
      if (sortedTimeline.length > 0 && sortedTimeline[0].thoiGian) {
        return formatDateTime(sortedTimeline[0].thoiGian)
      }
      // Final fallback
      return formatDateTime(invoice.value?.ngayTao || invoice.value?.createAt)
    }

    // ========== CONFIRMED: Đã xác nhận ==========
    if (statusKey === 'confirmed') {
      // Ưu tiên entry "Cập nhật" / "Xác nhận" với trạng thái mới là "Đã xác nhận"
      for (let i = sortedTimeline.length - 1; i >= 0; i--) {
        const item = sortedTimeline[i]

        if (excludes(item, ['hoàn thành', 'đã thanh toán', 'đang giao', 'giao hàng'])) {
          continue
        }

        if (matches(item, ['đã xác nhận', 'xác nhận', 'da xac nhan', 'xac nhan'])) {
          if (item.thoiGian) {
            return formatDateTime(item.thoiGian)
          }
        }
      }

      // Fallback: cho phép dùng trạng thái "Chờ giao hàng" hoặc "Chuẩn bị"
      for (let i = sortedTimeline.length - 1; i >= 0; i--) {
        const item = sortedTimeline[i]
        if (excludes(item, ['hoàn thành', 'đã thanh toán', 'thanh toán thành công'])) {
          continue
        }
        if (matches(item, ['chờ giao hàng', 'cho giao hang', 'chuẩn bị', 'chuan bi'])) {
          if (item.thoiGian) {
            return formatDateTime(item.thoiGian)
          }
        }
      }

      return 'N/A'
    }

    // ========== SHIPPING: Đang giao ==========
    // Entry có "Đang giao hàng" hoặc "Giao hàng"
    if (statusKey === 'shipping') {
      // Tìm từ cuối lên để lấy entry gần nhất
      for (let i = sortedTimeline.length - 1; i >= 0; i--) {
        const item = sortedTimeline[i]

        // LOẠI TRỪ entry "Hoàn thành"
        if (excludes(item, ['hoàn thành', 'đã thanh toán'])) {
          continue
        }

        // Tìm entry "Đang giao" hoặc "Giao hàng"
        if (matches(item, ['đang giao', 'giao hàng', 'giao hang'])) {
          if (item.thoiGian) {
            return formatDateTime(item.thoiGian)
          }
        }
      }

      // Không tìm thấy -> N/A (đơn tại quầy không có bước này)
      return 'N/A'
    }

    // ========== COMPLETED: Hoàn thành ==========
    // Entry cuối cùng có "Hoàn thành" hoặc "Đã thanh toán" hoặc "Cập nhật" với trangThaiMoi="Hoàn thành"
    // Ưu tiên entry "Cập nhật" với trangThaiMoi="Hoàn thành" trước entry "Xác nhận" với trangThaiMoi="Hoàn thành"
    if (statusKey === 'completed') {
      // Bước 1: Tìm từ cuối lên để lấy entry "Cập nhật" với trangThaiMoi="Hoàn thành" (ưu tiên cao nhất)
      for (let i = sortedTimeline.length - 1; i >= 0; i--) {
        const item = sortedTimeline[i]
        const hanhDongNorm = normalize(item.hanhDong)
        const trangThaiMoiNorm = normalize(item.trangThaiMoi)
        const isCapNhatHoanThanh = hanhDongNorm === 'cap nhat' && trangThaiMoiNorm.includes('hoan thanh')

        if (isCapNhatHoanThanh && item.thoiGian) {
          return formatDateTime(item.thoiGian)
        }
      }

      // Bước 2: Nếu không tìm thấy "Cập nhật", tìm entry "Xác nhận" với trangThaiMoi="Hoàn thành" (ưu tiên cao)
      // Backend trả về: hanhDong = "Xác nhận", trangThaiMoi = "Hoàn thành"
      for (let i = sortedTimeline.length - 1; i >= 0; i--) {
        const item = sortedTimeline[i]
        const hanhDongNorm = normalize(item.hanhDong || '')
        const trangThaiMoiNorm = normalize(item.trangThaiMoi || '')

        // Tìm "Xác nhận" với "Hoàn thành" (backend format)
        const isXacNhanHoanThanh = hanhDongNorm === 'xac nhan' && trangThaiMoiNorm.includes('hoan thanh')

        if (isXacNhanHoanThanh && item.thoiGian) {
          return formatDateTime(item.thoiGian)
        }
      }

      // Bước 3: Tìm entry có trangThaiMoi="Hoàn thành" (fallback)
      for (let i = sortedTimeline.length - 1; i >= 0; i--) {
        const item = sortedTimeline[i]
        const trangThaiMoiNorm = normalize(item.trangThaiMoi || '')

        const isHoanThanh =
          trangThaiMoiNorm.includes('hoan thanh') ||
          trangThaiMoiNorm.includes('da thanh toan') ||
          trangThaiMoiNorm.includes('thanh toan thanh cong')

        if (isHoanThanh && item.thoiGian) {
          return formatDateTime(item.thoiGian)
        }
      }

      // Fallback: entry cuối cùng trong timeline (entry mới nhất) - chỉ dùng nếu có timeline
      // Đảm bảo luôn lấy từ timeline thay vì invoice để format giống nhau
      if (sortedTimeline.length > 0) {
        const lastEntry = sortedTimeline[sortedTimeline.length - 1]
        if (lastEntry && lastEntry.thoiGian) {
          return formatDateTime(lastEntry.thoiGian)
        }
      }

      // Final fallback: chỉ dùng invoice nếu không có timeline nào
      // Nhưng vẫn format giống nhau bằng formatDateTime
      return formatDateTime(invoice.value?.ngayThanhToan || invoice.value?.ngayTao)
    }

    // Default fallback
    return formatDateTime(invoice.value?.ngayTao || invoice.value?.createAt)
  } catch (error) {
    console.error('Error in getTimeFromTimeline:', error)
    // Safe fallback
    if (statusKey === 'completed') {
      return formatDateTime(invoice.value?.ngayThanhToan || invoice.value?.ngayTao)
    }
    return formatDateTime(invoice.value?.ngayTao || invoice.value?.createAt)
  }
}

// Computed: Get ngayDat (order date) with fallback from timeline
const ngayDat = computed(() => {
  // First try: use ngayTao from invoice
  if (invoice.value?.ngayTao) {
    return formatDateTime(invoice.value.ngayTao)
  }

  // Second try: get from timeline entry "Tạo" (first entry)
  if (timelineData.value && Array.isArray(timelineData.value) && timelineData.value.length > 0) {
    // Sort timeline by time to get the first entry
    const sortedTimeline = [...(Array.isArray(timelineData.value) ? timelineData.value : [])].sort((a, b) => {
      try {
        const timeA = a.thoiGian ? new Date(a.thoiGian).getTime() : 0
        const timeB = b.thoiGian ? new Date(b.thoiGian).getTime() : 0
        if (isNaN(timeA) && isNaN(timeB)) {
          return (a.id || 0) - (b.id || 0)
        }
        if (isNaN(timeA)) return 1
        if (isNaN(timeB)) return -1
        return timeA - timeB
      } catch {
        return 0
      }
    })

    // Find first entry with "Tạo" action or "Tạo đơn hàng" status
    for (const item of sortedTimeline) {
      const hanhDong = (item.hanhDong || '').toLowerCase().trim()
      const trangThaiMoi = (item.trangThaiMoi || '').toLowerCase().trim()

      if ((hanhDong === 'tạo' || trangThaiMoi.includes('tạo đơn hàng')) && item.thoiGian) {
        return formatDateTime(item.thoiGian)
      }
    }

    // Fallback: use first entry's time
    if (sortedTimeline[0]?.thoiGian) {
      return formatDateTime(sortedTimeline[0].thoiGian)
    }
  }

  // Final fallback: use createAt
  return formatDateTime(invoice.value?.createAt)
})

// Map tenTrangThaiDonHang to color
const getStatusColorByName = (statusName: string): string => {
  switch (statusName) {
    case 'Đã huỷ':
    case 'Đã hủy':
      return 'red'
    case 'Hoàn thành':
      return 'green'
    case 'Đã giao hàng':
      return 'green'
    case 'Đang giao hàng':
    case 'Đang giao':
      return 'blue'
    case 'Đang xử lý':
      return 'orange'
    case 'Đã xác nhận':
      return 'orange'
    case 'Chờ xác nhận':
      return 'orange'
    default:
      return 'blue'
  }
}

const resolvePaymentMethodLabel = (methodId?: number | null): string => {
  if (methodId === 1) return 'Tiền mặt'
  if (methodId === 2) return 'Chuyển khoản'
  if (methodId === 3) return 'Tiền mặt + Chuyển khoản'
  return 'Thanh toán'
}

// Computed: Get current stage status label (for display in order info)
const currentStageStatus = computed(() => {
  try {
    if (!invoice.value) return 'Chờ xác nhận'
    return getHighestPriorityStatusFromInvoice() || 'Chờ xác nhận'
  } catch (error) {
    console.error('Error getting current stage status:', error)
    return 'Chờ xác nhận'
  }
})

// Computed: Status stages for horizontal timeline
const statusStages = computed(() => {
  const ensureLegacyStages = () => {
    const defaultStages = [
      {
        key: 'pending',
        label: 'Chờ xác nhận',
        icon: IconClockCircle,
        active: true,
        completed: true,
        time: invoice.value?.ngayTao ? formatDateTime(invoice.value?.ngayTao) : 'N/A',
      },
      {
        key: 'confirmed',
        label: 'Đã xác nhận',
        icon: IconCheckCircle,
        active: false,
        completed: false,
        time: 'N/A',
      },
      {
        key: 'shipping',
        label: 'Đang giao hàng',
        icon: IconSend,
        active: false,
        completed: false,
        time: 'N/A',
      },
      {
        key: 'completed',
        label: 'Hoàn thành',
        icon: IconCheck,
        active: false,
        completed: false,
        time: 'N/A',
      },
    ]

    if (!invoice.value) {
      return defaultStages
    }

    try {
      const currentStatus = getHighestPriorityStatusFromInvoice()
      const normalizedStatus = normalizeStatusForFlow(currentStatus)
      const isTaiQuay = invoice.value.loaiDon === false

      if (isTaiQuay) {
        const stages = [
          {
            key: 'pending',
            label: 'Chờ xác nhận',
            icon: IconClockCircle,
            active: false,
            completed: false,
            time: getTimeFromTimeline('pending'),
          },
          {
            key: 'completed',
            label: 'Hoàn thành',
            icon: IconCheck,
            active: false,
            completed: false,
            time: getTimeFromTimeline('completed'),
          },
        ]

        if (normalizedStatus === 'Chờ xác nhận') {
          stages[0].active = true
          stages[0].completed = true
          stages[1].time = 'N/A'
        } else if (normalizedStatus === 'Hoàn thành') {
          stages[0].completed = true
          stages[1].active = true
          stages[1].completed = true
        } else if (normalizedStatus === CANCELLATION_STATUS) {
          stages[0].completed = true
          stages[1].time = 'N/A'
        } else {
          stages[0].completed = true
          stages[0].active = true
          stages[1].time = 'N/A'
        }

        return stages
      }

      const stages = [
        {
          key: 'pending',
          label: 'Chờ xác nhận',
          icon: IconClockCircle,
          active: false,
          completed: false,
          time: getTimeFromTimeline('pending'),
        },
        {
          key: 'confirmed',
          label: 'Đã xác nhận',
          icon: IconCheckCircle,
          active: false,
          completed: false,
          time: getTimeFromTimeline('confirmed'),
        },
        {
          key: 'shipping',
          label: 'Đang giao hàng',
          icon: IconSend,
          active: false,
          completed: false,
          time: getTimeFromTimeline('shipping'),
        },
        {
          key: 'completed',
          label: 'Hoàn thành',
          icon: IconCheck,
          active: false,
          completed: false,
          time: getTimeFromTimeline('completed'),
        },
      ]

      if (normalizedStatus === 'Chờ xác nhận') {
        stages[0].active = true
        stages[0].completed = true
        stages[1].time = 'N/A'
        stages[2].time = 'N/A'
        stages[3].time = 'N/A'
      } else if (normalizedStatus === 'Đã xác nhận') {
        stages[0].completed = true
        stages[1].active = true
        stages[1].completed = true
        stages[2].time = 'N/A'
        stages[3].time = 'N/A'
      } else if (normalizedStatus === 'Đang giao hàng') {
        stages[0].completed = true
        stages[1].completed = true
        stages[2].active = true
        stages[2].completed = true
        stages[3].time = 'N/A'
      } else if (normalizedStatus === 'Hoàn thành') {
        stages.forEach((stage) => {
          stage.completed = true
        })
        stages[3].active = true
      } else if (normalizedStatus === CANCELLATION_STATUS) {
        stages[0].completed = true
        stages[1].completed = true
        stages[2].completed = false
        stages[1].time = getTimeFromTimeline('confirmed')
        stages[2].time = 'N/A'
        stages[3].time = 'N/A'
      } else {
        stages[0].completed = true
        stages[0].active = true
        stages[1].time = 'N/A'
        stages[2].time = 'N/A'
        stages[3].time = 'N/A'
      }

      return stages
    } catch (error) {
      console.error('Error computing status stages:', error)
      const isTaiQuay = invoice.value?.loaiDon === false

      if (isTaiQuay) {
        return [
          {
            key: 'pending',
            label: 'Chờ xác nhận',
            icon: IconClockCircle,
            active: true,
            completed: true,
            time: formatDateTime(invoice.value?.ngayTao),
          },
          {
            key: 'completed',
            label: 'Hoàn thành',
            icon: IconCheck,
            active: false,
            completed: false,
            time: formatDateTime(invoice.value?.ngayThanhToan || invoice.value?.ngayTao),
          },
        ]
      }

      return defaultStages
    }
  }

  try {
    const stageEntries: Array<{
      key: string
      label: string
      icon: any
      time: string
      timestamp: number | null
      order: number
      variant?: 'cancelled'
      isReal: boolean
    }> = []

    const seenKeys = new Set<string>()

    const registerStage = (rawStatus?: string | null, rawTime?: unknown) => {
      if (!rawStatus) {
        return
      }

      if (isAddressChangeText(rawStatus)) {
        return
      }

      const definition = resolveStageDefinitionFromStatus(rawStatus)

      if (seenKeys.has(definition.key)) {
        const existing = stageEntries.find((stage) => stage.key === definition.key)
        if (existing && (!existing.timestamp || existing.timestamp === null)) {
          const parsedTime = parseTimelineTimestamp(rawTime)
          if (parsedTime !== null) {
            existing.timestamp = parsedTime
            existing.time = formatDateTime(new Date(parsedTime))
            existing.isReal = true
          }
        }
        return
      }

      const parsedTime = parseTimelineTimestamp(rawTime)
      stageEntries.push({
        key: definition.key,
        label: definition.label,
        icon: definition.icon || IconClockCircle,
        time: parsedTime !== null ? formatDateTime(new Date(parsedTime)) : 'N/A',
        timestamp: parsedTime,
        order: definition.order ?? 98,
        variant: definition.variant,
        isReal: true,
      })
      seenKeys.add(definition.key)
    }

    if (invoice.value?.thongTinDonHangs && Array.isArray(invoice.value.thongTinDonHangs)) {
      const sortedStatuses = [...invoice.value.thongTinDonHangs].sort((a: any, b: any) => {
        const timeA = parseTimelineTimestamp(a.thoiGian || a.ngayCapNhat || a.ngayTao)
        const timeB = parseTimelineTimestamp(b.thoiGian || b.ngayCapNhat || b.ngayTao)
        if (timeA !== null && timeB !== null) {
          return timeA - timeB
        }
        if (timeA !== null) return -1
        if (timeB !== null) return 1
        return (STATUS_FLOW_ORDER[a.tenTrangThaiDonHang] || 99) - (STATUS_FLOW_ORDER[b.tenTrangThaiDonHang] || 99)
      })

      sortedStatuses.forEach((item: any) => {
        const statusText = item.tenTrangThaiDonHang || item.trangThaiMoi || ''
        if (!statusText || isAddressChangeText(statusText)) {
          return
        }
        const timeSource = item.thoiGian || item.ngayCapNhat || item.ngayTao
        registerStage(statusText, timeSource)
      })
    }

    if (timelineData.value && Array.isArray(timelineData.value) && timelineData.value.length > 0) {
      const sortedTimeline = [...timelineData.value]
        .filter((entry) => !isAddressChangeEntry(entry))
        .sort((a, b) => {
          const timeA = parseTimelineTimestamp(a.thoiGian)
          const timeB = parseTimelineTimestamp(b.thoiGian)
          if (timeA !== null && timeB !== null) {
            return timeA - timeB
          }
          if (timeA !== null) return -1
          if (timeB !== null) return 1
          return 0
        })

      sortedTimeline.forEach((entry) => {
        const statusText = entry.trangThaiMoi || entry.hanhDong || ''
        if (!statusText) return
        registerStage(statusText, entry.thoiGian)
      })
    }

    if (stageEntries.length === 0) {
      return ensureLegacyStages()
    }

    const sortStageEntries = () => {
      stageEntries.sort((a, b) => {
        if (a.timestamp !== null && b.timestamp !== null && a.timestamp !== b.timestamp) {
          return a.timestamp - b.timestamp
        }
        if (a.timestamp !== null && b.timestamp === null) {
          return -1
        }
        if (a.timestamp === null && b.timestamp !== null) {
          return 1
        }
        if (a.order !== b.order) {
          return a.order - b.order
        }
        return a.label.localeCompare(b.label)
      })
    }

    sortStageEntries()

    const isTaiQuay = invoice.value?.loaiDon === false
    const canonicalFlow = isTaiQuay ? OFFLINE_CANONICAL_FLOW : ONLINE_CANONICAL_FLOW
    const hasCancellationStage = stageEntries.some((stage) => stage.variant === 'cancelled' || stage.key === 'cancelled')
    const lastRealIndex = stageEntries.reduce((acc, stage, index) => (stage.isReal ? index : acc), -1)
    const lastRealStage = lastRealIndex >= 0 ? stageEntries[lastRealIndex] : null

    if (!hasCancellationStage && lastRealStage && lastRealStage.key !== 'completed') {
      let lastCanonicalIndex = -1
      stageEntries.forEach((stage) => {
        if (!stage.isReal) return
        const idx = canonicalFlow.indexOf(stage.key)
        if (idx > lastCanonicalIndex) {
          lastCanonicalIndex = idx
        }
      })

      if (lastCanonicalIndex >= 0) {
        for (let i = lastCanonicalIndex + 1; i < canonicalFlow.length; i++) {
          const key = canonicalFlow[i]
          if (seenKeys.has(key)) continue
          const definition = TIMELINE_STAGE_DEFINITIONS.find((def) => def.key === key)
          if (!definition) continue
          stageEntries.push({
            key: definition.key,
            label: definition.label,
            icon: definition.icon || IconClockCircle,
            time: 'N/A',
            timestamp: null,
            order: definition.order ?? 98,
            variant: definition.variant,
            isReal: false,
          })
          seenKeys.add(definition.key)
        }

        sortStageEntries()
      }
    }

    const cancellationIndex = stageEntries.findIndex((stage) => {
      if (stage.variant === 'cancelled' || stage.key === 'cancelled') {
        return true
      }
      const normalizedLabel = normalizeStatusText(stage.label)
      return normalizedLabel.includes('huy')
    })

    if (cancellationIndex >= 0 && cancellationIndex < stageEntries.length - 1) {
      stageEntries.splice(cancellationIndex + 1)
    }

    const resolveActiveIndex = () => {
      let bestIndex = -1
      let bestPriority = -1

      stageEntries.forEach((stage, index) => {
        if (!stage.isReal) {
          return
        }

        if (stage.variant === 'cancelled' || stage.key === 'cancelled') {
          bestIndex = index
          bestPriority = Number.POSITIVE_INFINITY
          return
        }

        const canonicalIndex = canonicalFlow.indexOf(stage.key)
        if (canonicalIndex >= 0 && canonicalIndex > bestPriority) {
          bestPriority = canonicalIndex
          bestIndex = index
          return
        }

        if (canonicalIndex === -1 && stage.order > bestPriority) {
          bestPriority = stage.order
          bestIndex = index
        }
      })

      return bestIndex
    }

    const resolvedActiveIndex = resolveActiveIndex()
    const statusToStageKey: Record<string, string> = {
      'Chờ xác nhận': 'pending',
      'Đã xác nhận': 'confirmed',
      'Đang giao hàng': 'shipping',
      'Hoàn thành': 'completed',
      [CANCELLATION_STATUS]: 'cancelled',
    }

    let activeIndex = resolvedActiveIndex >= 0 ? resolvedActiveIndex : stageEntries.length - 1

    const normalizedInvoiceStatus = normalizeStatusForFlow(currentStageStatus.value)
    const desiredStageKey = statusToStageKey[normalizedInvoiceStatus]
    if (desiredStageKey) {
      const invoiceStageIndex = stageEntries.findIndex((stage) => stage.key === desiredStageKey)
      if (invoiceStageIndex >= 0) {
        activeIndex = Math.max(activeIndex, invoiceStageIndex)
      }
    }

    return stageEntries.map((stage, index) => ({
      key: stage.key,
      label: stage.label,
      icon: stage.icon,
      time: stage.time,
      active: index === activeIndex,
      completed: index < activeIndex,
      variant: stage.variant,
    }))
  } catch (error) {
    console.error('Error building status stages from timeline:', error)
    return ensureLegacyStages()
  }
})

// Payment history
const paymentHistory = computed(() => {
  if (!invoice.value) return []

  // Check if invoice has been paid at all
  const soTienDaThanhToan = Number(invoice.value.soTienDaThanhToan) || 0

  if (soTienDaThanhToan <= 0) {
    return []
  }

  const history: Array<{
    method: string
    amount: number
    code: string
    userCode: string
    date: unknown
    methodId?: number
  }> = []
  let hasValidPaymentRecord = false

  // Nếu có paymentMethods từ API, sử dụng thông tin đó
  if (paymentMethods.value && paymentMethods.value.length > 0) {
    paymentMethods.value.forEach((payment: any) => {
      // Skip deleted or inactive payment records
      if (payment.deleted === true || payment.trangThai === false) {
        return
      }

      hasValidPaymentRecord = true
      const idPTTT = payment.idPhuongThucThanhToan
      // Convert BigDecimal to number
      const tienMat = Number(payment.tienMat) || 0
      const tienChuyenKhoan = Number(payment.tienChuyenKhoan) || 0

      // Get date from payment record, fallback to invoice, then timeline
      let paymentDate = payment.ngayTao || invoice.value.ngayThanhToan || invoice.value.ngayTao
      // If still no date, try to get from timeline (completed entry)
      if (!paymentDate && timelineData.value && timelineData.value.length > 0) {
        const completedEntry = timelineData.value.find((t: any) => {
          const trangThaiMoi = (t.trangThaiMoi || '').toLowerCase()
          return trangThaiMoi.includes('hoàn thành') || trangThaiMoi.includes('đã thanh toán')
        })
        if (completedEntry?.thoiGian) {
          paymentDate = completedEntry.thoiGian
        }
      }

      // Get user code from payment record, fallback to invoice
      const userCode = payment.tenNhanVienXacNhan || invoice.value.maNhanVien || invoice.value.tenNhanVien || 'NV000001'

      // Get payment code - prefer maHinhThucThanhToan from backend, fallback to generated code
      const paymentCode = payment.maHinhThucThanhToan || `PTT${String(payment.id || invoice.value.id || 0).padStart(5, '0')}`

      // idPTTT: 1 = Tiền mặt, 2 = Chuyển khoản, 3 = Cả hai
      if (idPTTT === 1 && tienMat > 0) {
        // Tiền mặt
        history.push({
          method: 'Tiền mặt',
          amount: tienMat,
          code: paymentCode,
          userCode,
          date: paymentDate,
          methodId: idPTTT,
        })
      } else if (idPTTT === 2 && tienChuyenKhoan > 0) {
        // Chuyển khoản
        history.push({
          method: 'Chuyển khoản',
          amount: tienChuyenKhoan,
          code: paymentCode,
          userCode,
          date: paymentDate,
          methodId: idPTTT,
        })
      } else if (idPTTT === 3) {
        // Cả hai - tạo mã riêng cho từng phương thức
        const paymentCodeTM = payment.maHinhThucThanhToan
          ? `${payment.maHinhThucThanhToan}-TM`
          : `PTT${String(payment.id || invoice.value.id || 0).padStart(5, '0')}-TM`
        const paymentCodeCK = payment.maHinhThucThanhToan
          ? `${payment.maHinhThucThanhToan}-CK`
          : `PTT${String(payment.id || invoice.value.id || 0).padStart(5, '0')}-CK`

        if (tienMat > 0) {
          history.push({
            method: 'Tiền mặt',
            amount: tienMat,
            code: paymentCodeTM,
            userCode,
            date: paymentDate,
            methodId: 1,
          })
        }
        if (tienChuyenKhoan > 0) {
          history.push({
            method: 'Chuyển khoản',
            amount: tienChuyenKhoan,
            code: paymentCodeCK,
            userCode,
            date: paymentDate,
            methodId: 2,
          })
        }
      }
    })
  }

  // Fallback: nếu không có paymentMethods hoặc tất cả đều bị skip, sử dụng thông tin từ invoice
  if (!hasValidPaymentRecord && soTienDaThanhToan > 0) {
    // Fallback: nếu không có paymentMethods, sử dụng thông tin từ invoice
    const idPTTT = invoice.value.idPhuongThucThanhToan || invoice.value.idPTTT
    let method = resolvePaymentMethodLabel(idPTTT)

    history.push({
      method,
      amount: soTienDaThanhToan,
      code: `PTT${String(invoice.value.id || 0).padStart(5, '0')}`,
      userCode: invoice.value.maNhanVien || invoice.value.tenNhanVien || 'NV000001',
      date: invoice.value.ngayThanhToan,
      methodId: idPTTT || undefined,
    })
  }

  if (history.length === 0) {
    return history
  }

  const normalizeAmount = (value: unknown) => {
    const numeric = Number(value)
    return Number.isFinite(numeric) ? Math.round(numeric) : 0
  }

  const parseHistoryTimestamp = (value: unknown): number => {
    const parsed = parseTimelineTimestamp(value)
    if (parsed !== null) {
      return parsed
    }
    if (value instanceof Date) {
      const time = value.getTime()
      return Number.isFinite(time) ? time : 0
    }
    if (typeof value === 'number') {
      return Number.isFinite(value) ? value : 0
    }
    if (typeof value === 'string') {
      const date = new Date(value)
      const time = date.getTime()
      return Number.isFinite(time) ? time : 0
    }
    return 0
  }

  const expectedTotal = normalizeAmount(soTienDaThanhToan)
  const recordedTotal = history.reduce((sum, item) => sum + normalizeAmount(item.amount), 0)
  const tolerance = 0

  let normalizedHistory = history as typeof history

  if (expectedTotal > 0 && Math.abs(recordedTotal - expectedTotal) > tolerance) {
    const map = new Map<number | string, { payment: (typeof history)[number]; timestamp: number }>()
    history.forEach((item) => {
      const key = item.methodId ?? item.method ?? item.code
      const timestamp = parseHistoryTimestamp(item.date)
      const existing = map.get(key)
      if (!existing || timestamp >= existing.timestamp) {
        map.set(key, { payment: item, timestamp })
      }
    })

    const deduped = Array.from(map.values())
      .map((entry) => entry.payment)
      .sort((a, b) => parseHistoryTimestamp(a.date) - parseHistoryTimestamp(b.date))

    const dedupedTotal = deduped.reduce((sum, item) => sum + normalizeAmount(item.amount), 0)

    if (deduped.length > 0 && Math.abs(dedupedTotal - expectedTotal) <= tolerance) {
      normalizedHistory = deduped
    } else {
      const sortedByDateDesc = [...history].sort((a, b) => parseHistoryTimestamp(b.date) - parseHistoryTimestamp(a.date))
      const latest = sortedByDateDesc[0]
      const fallbackMethodId = latest?.methodId ?? invoice.value?.idPhuongThucThanhToan ?? invoice.value?.idPTTT ?? null
      normalizedHistory = [
        {
          method: latest?.method || resolvePaymentMethodLabel(fallbackMethodId),
          amount: expectedTotal,
          code: latest?.code || `PTT${String(invoice.value?.id || 0).padStart(5, '0')}`,
          userCode: latest?.userCode || invoice.value?.maNhanVien || invoice.value?.tenNhanVien || 'NV000001',
          date: latest?.date || invoice.value?.ngayThanhToan || invoice.value?.ngayTao,
          methodId: fallbackMethodId ?? undefined,
        },
      ]
    }
  }

  return [...normalizedHistory].sort((a, b) => parseHistoryTimestamp(b.date) - parseHistoryTimestamp(a.date))
})

const primaryPaymentMethodId = computed(() => {
  const extractId = (raw: unknown): number | null => {
    if (raw === null || raw === undefined) return null
    if (typeof raw === 'number' && Number.isFinite(raw) && raw > 0) return raw
    if (typeof raw === 'string') {
      const parsed = Number(raw)
      return Number.isFinite(parsed) && parsed > 0 ? parsed : null
    }
    if (typeof raw === 'object') {
      const candidate = raw as Record<string, unknown>
      if (candidate && typeof candidate.id === 'number' && candidate.id > 0) {
        return candidate.id
      }
      if (candidate && typeof candidate.id === 'string') {
        const parsed = Number(candidate.id)
        if (Number.isFinite(parsed) && parsed > 0) {
          return parsed
        }
      }
    }
    return null
  }

  if (paymentMethods.value && paymentMethods.value.length > 0) {
    const active = paymentMethods.value.find((method: any) => method?.deleted !== true && method?.trangThai !== false)
    const fallback = active || paymentMethods.value[0]
    const resolved = extractId(fallback?.idPhuongThucThanhToan)
    if (resolved) return resolved
  }

  const invoiceMethod = extractId(invoice.value?.idPhuongThucThanhToan || invoice.value?.idPTTT)
  if (invoiceMethod) return invoiceMethod

  return null
})

// Invoice history
const invoiceHistory = computed(() => {
  const history: Array<{ action: string; userCode: string; date: string }> = []

  // Priority 1: Use thongTinDonHangs from invoice (most reliable)
  if (invoice.value?.thongTinDonHangs && Array.isArray(invoice.value.thongTinDonHangs) && invoice.value.thongTinDonHangs.length > 0) {
    const sorted = [...invoice.value.thongTinDonHangs].sort((a, b) => {
      const timeA = a.thoiGian ? new Date(a.thoiGian).getTime() : 0
      const timeB = b.thoiGian ? new Date(b.thoiGian).getTime() : 0
      return timeB - timeA
    })

    sorted.forEach((item) => {
      if (item.tenTrangThaiDonHang && item.thoiGian) {
        history.push({
          action: item.tenTrangThaiDonHang,
          userCode: item.ghiChu || invoice.value?.tenNhanVien || invoice.value?.maNhanVien || 'Hệ thống',
          date: item.thoiGian,
        })
      }
    })

    // De-duplicate by action + userCode + timestamp to second
    {
      const seen = new Set<string>()
      const deduped: Array<{ action: string; userCode: string; date: string }> = []
      for (const h of history) {
        const dt = h.date ? new Date(h.date) : null
        const secondKey = dt && !isNaN(dt.getTime()) ? dt.toISOString().slice(0, 19) : String(h.date)
        const key = `${h.action}|${h.userCode}|${secondKey}`
        if (!seen.has(key)) {
          seen.add(key)
          deduped.push(h)
        }
      }
      return deduped
    }
  }

  // Priority 2: Use timelineData from API (fallback)
  const timeline = timelineData.value || []
  if (timeline.length > 0) {
    const sorted = [...timeline].sort((a, b) => {
      const timeA = a.thoiGian ? new Date(a.thoiGian).getTime() : 0
      const timeB = b.thoiGian ? new Date(b.thoiGian).getTime() : 0
      return timeB - timeA
    })

    sorted.forEach((item) => {
      history.push({
        action: formatTimelineAction(item),
        userCode: getHistoryUserCode(item),
        date: item.thoiGian,
      })
    })

    // De-duplicate as a safeguard (same key as above)
    {
      const seen = new Set<string>()
      const deduped: Array<{ action: string; userCode: string; date: string }> = []
      for (const h of history) {
        const dt = h.date ? new Date(h.date) : null
        const secondKey = dt && !isNaN(dt.getTime()) ? dt.toISOString().slice(0, 19) : String(h.date)
        const key = `${h.action}|${h.userCode}|${secondKey}`
        if (!seen.has(key)) {
          seen.add(key)
          deduped.push(h)
        }
      }
      return deduped
    }
  }

  // Fallback: Use ngayTao and ngayThanhToan
  if (invoice.value?.ngayTao) {
    history.push({
      action: 'Tạo hóa đơn',
      userCode: invoice.value.tenNhanVien || invoice.value.maNhanVien || 'Hệ thống',
      date: invoice.value.ngayTao,
    })
  }

  if (invoice.value?.ngayThanhToan) {
    history.push({
      action: 'Thanh toán hóa đơn',
      userCode: invoice.value.tenNhanVien || invoice.value.maNhanVien || 'Hệ thống',
      date: invoice.value.ngayThanhToan,
    })
  }

  return history
})

// Calculate total from hoaDonChiTiets if tongTien is missing
const calculatedTongTien = computed(() => {
  if (!invoice.value) return 0

  // Priority 1: Use tongTien from HoaDon (backend subtotal before discounts/shipping)
  if (invoice.value.tongTien && Number(invoice.value.tongTien) > 0) {
    return Number(invoice.value.tongTien)
  }

  // Priority 2: Use tongTienHang if available (from ThongTinDonHang)
  if (invoice.value.tongTienHang && Number(invoice.value.tongTienHang) > 0) {
    return Number(invoice.value.tongTienHang)
  }

  // Priority 3: Calculate from hoaDonChiTiets
  if (invoice.value.hoaDonChiTiets && invoice.value.hoaDonChiTiets.length > 0) {
    const total = invoice.value.hoaDonChiTiets.reduce((sum: number, item: any) => {
      // Use thanhTien if available, otherwise calculate from giaBan * soLuong
      const thanhTien = item.thanhTien ? Number(item.thanhTien) : (Number(item.giaBan) || 0) * (Number(item.soLuong) || 0)
      return sum + (thanhTien || 0)
    }, 0)
    if (total > 0) {
      return total
    }
  }

  // Priority 4: Fallback to items array
  if (invoice.value.items && invoice.value.items.length > 0) {
    const total = invoice.value.items.reduce((sum: number, item: any) => {
      const thanhTien = item.thanhTien ? Number(item.thanhTien) : (Number(item.giaBan) || 0) * (Number(item.soLuong) || 0)
      return sum + (thanhTien || 0)
    }, 0)
    if (total > 0) {
      return total
    }
  }

  return 0
})

const clampCurrency = (value: number) => {
  if (!Number.isFinite(value)) return 0
  return value > 0 ? value : 0
}

const resolveAmount = (value: unknown) => {
  const numeric = Number(value)
  return Number.isFinite(numeric) ? numeric : 0
}

const hasSubtotalAfterDiscount = computed(() => {
  if (!invoice.value) return false
  return Object.prototype.hasOwnProperty.call(invoice.value, 'tongTienSauGiam')
})

const resolvedSubtotalAfterDiscount = computed(() => {
  if (!invoice.value) return 0

  if (hasSubtotalAfterDiscount.value) {
    const rawSubtotal = invoice.value.tongTienSauGiam
    if (rawSubtotal !== null && rawSubtotal !== undefined) {
      return clampCurrency(resolveAmount(rawSubtotal))
    }
  }

  const productTotal = calculatedTongTien.value
  if (productTotal <= 0) {
    return 0
  }

  const rawDiscount = invoice.value.giamGia ?? invoice.value.soTienGiamGia ?? invoice.value.giaTriGiamGia
  if (rawDiscount !== undefined && rawDiscount !== null) {
    const discountAmount = clampCurrency(resolveAmount(rawDiscount))
    if (discountAmount >= productTotal) {
      return 0
    }
    if (discountAmount > 0) {
      return clampCurrency(productTotal - discountAmount)
    }
  }

  return clampCurrency(productTotal)
})

const calculatedDiscountAmount = computed(() => {
  const productTotal = calculatedTongTien.value
  if (productTotal <= 0) {
    return 0
  }

  if (hasSubtotalAfterDiscount.value) {
    const subtotalAfterDiscount = resolvedSubtotalAfterDiscount.value
    const discount = productTotal - subtotalAfterDiscount
    return discount > 0 ? clampCurrency(discount) : 0
  }

  const derivedSubtotal = resolvedSubtotalAfterDiscount.value
  if (derivedSubtotal >= 0 && derivedSubtotal <= productTotal) {
    return clampCurrency(productTotal - derivedSubtotal)
  }

  return 0
})

const calculatedSurchargeAmount = computed(() => {
  if (!invoice.value) return 0
  return clampCurrency(resolveAmount(invoice.value.phuPhi))
})

const calculatedRefundAmount = computed(() => {
  if (!invoice.value) return 0
  const rawRefund = resolveAmount(invoice.value.hoanPhi)
  const absoluteRefund = Math.abs(rawRefund)
  return clampCurrency(absoluteRefund)
})

const calculatedShippingGross = computed(() => {
  if (!invoice.value) return 0
  return clampCurrency(resolveAmount(invoice.value.phiVanChuyen))
})

const calculatedShippingBase = computed(() => {
  // Normalize displayed shipping so surcharges/refunds remain on their own rows.
  const shippingBase = calculatedShippingGross.value - calculatedSurchargeAmount.value + calculatedRefundAmount.value
  return shippingBase > 0 ? shippingBase : 0
})

const calculatedFinalTotalRaw = computed(() => {
  // Thành tiền = tổng tiền sau giảm + toàn bộ phí giao hàng theo API.
  const subtotalAfterDiscount = resolvedSubtotalAfterDiscount.value
  let baseSubtotal = subtotalAfterDiscount

  if (!hasSubtotalAfterDiscount.value && baseSubtotal <= 0) {
    const derivedSubtotal = calculatedTongTien.value - calculatedDiscountAmount.value
    baseSubtotal = derivedSubtotal > 0 ? derivedSubtotal : calculatedTongTien.value
  }

  if (baseSubtotal < 0) {
    baseSubtotal = 0
  }

  const finalTotal = baseSubtotal + calculatedShippingGross.value
  return finalTotal > 0 ? finalTotal : 0
})

const calculatedFinalTotal = computed(() => Math.max(Math.round(calculatedFinalTotalRaw.value), 0))

const calculatedPaidAmountRaw = computed(() => {
  if (!invoice.value) return 0

  let paid = clampCurrency(resolveAmount(invoice.value.soTienDaThanhToan))

  if (!paid && paymentHistory.value && paymentHistory.value.length > 0) {
    paid = clampCurrency(paymentHistory.value.reduce((sum: number, record: any) => sum + clampCurrency(resolveAmount(record.amount)), 0))
  }

  return paid
})

const calculatedPaidAmount = computed(() => Math.max(Math.round(calculatedPaidAmountRaw.value), 0))

const calculatedRemainingAmountRaw = computed(() => {
  const remaining = calculatedFinalTotalRaw.value - calculatedPaidAmountRaw.value
  return remaining > 0 ? remaining : 0
})

const calculatedRemainingAmount = computed(() => Math.max(Math.round(calculatedRemainingAmountRaw.value), 0))

const calculatedOverpaidAmount = computed(() => {
  const excess = calculatedPaidAmount.value - calculatedFinalTotal.value
  return excess > 0 ? excess : 0
})

const paymentStatusClass = computed(() => {
  if (calculatedOverpaidAmount.value > 0) {
    return 'overpaid'
  }
  if (calculatedRemainingAmount.value > 0) {
    return 'outstanding'
  }
  return 'paid-ok'
})

const additionalPaymentAmount = computed(() => {
  if (updateForm.value?.trangThaiText !== 'Hoàn thành') {
    return 0
  }
  if (modalOriginalStatus.value !== 'Đang giao hàng') {
    return 0
  }
  const inputAmount = updateForm.value?.soTienDaThanhToan
  if (inputAmount === undefined || inputAmount === null) {
    return 0
  }
  const numeric = Number(inputAmount)
  if (!Number.isFinite(numeric)) {
    return 0
  }
  const rounded = Math.round(numeric)
  return rounded > 0 ? rounded : 0
})

const paymentAmountForCompletion = computed(() => {
  // Projected total paid after collecting the additional amount.
  const projectedTotal = calculatedPaidAmount.value + additionalPaymentAmount.value
  return projectedTotal > 0 ? Math.round(projectedTotal) : 0
})

const remainingAmountForCompletion = computed(() => {
  const outstanding = calculatedFinalTotal.value - paymentAmountForCompletion.value
  return outstanding > 0 ? outstanding : 0
})

const paymentCompletionDelta = computed(() => paymentAmountForCompletion.value - calculatedFinalTotal.value)

const showPaymentInput = computed(
  () =>
    updateForm.value.trangThaiText === 'Hoàn thành' && modalOriginalStatus.value === 'Đang giao hàng' && calculatedRemainingAmount.value > 0
)
const setPaymentAmountProgrammatically = (amount: number) => {
  isProgrammaticPaymentSync.value = true
  updateForm.value.soTienDaThanhToan = amount
  nextTick(() => {
    isProgrammaticPaymentSync.value = false
  })
}

watch(
  () => updateForm.value.trangThaiText,
  (newStatus) => {
    if (newStatus === 'Hoàn thành') {
      if (!paymentAmountManuallyEdited.value) {
        setPaymentAmountProgrammatically(calculatedRemainingAmount.value)
      }
    } else {
      paymentAmountManuallyEdited.value = false
    }
  }
)

watch(
  () => calculatedRemainingAmount.value,
  (next) => {
    if (showPaymentInput.value && !paymentAmountManuallyEdited.value) {
      setPaymentAmountProgrammatically(next)
    }
  }
)

watch(
  () => updateForm.value.soTienDaThanhToan,
  (newValue, oldValue) => {
    if (!showPaymentInput.value) {
      paymentAmountManuallyEdited.value = false
      return
    }
    if (isProgrammaticPaymentSync.value) {
      return
    }
    if (newValue !== oldValue) {
      paymentAmountManuallyEdited.value = true
    }
  }
)

const statusOptions = computed(() => {
  if (modalOriginalStatus.value === 'Hoàn thành' || modalOriginalStatus.value === 'Đã huỷ') {
    return [modalOriginalStatus.value]
  }
  const isOffline = invoice.value?.loaiDon === false || updateForm.value.loaiDon === false
  const flow = isOffline ? OFFLINE_STATUS_FLOW : ONLINE_STATUS_FLOW

  const currentStatus = modalOriginalStatus.value || 'Chờ xác nhận'

  const normalizedCurrent = normalizeStatusForFlow(currentStatus)
  const currentPriority = STATUS_FLOW_ORDER[normalizedCurrent] ?? 0

  if (normalizedCurrent === CANCELLATION_STATUS) {
    return [CANCELLATION_STATUS]
  }

  const options: string[] = []
  const pushIfMissing = (status: string) => {
    if (!status) return
    if (!options.includes(status)) {
      options.push(status)
    }
  }

  pushIfMissing(normalizedCurrent)

  const eligibleFlow = flow.filter((status) => {
    const statusPriority = STATUS_FLOW_ORDER[status] ?? 0
    return statusPriority >= currentPriority
  })

  if (eligibleFlow.length > 0) {
    eligibleFlow.forEach(pushIfMissing)
  } else {
    flow.forEach(pushIfMissing)
  }

  pushIfMissing(CANCELLATION_STATUS)

  return options
})

const statusEditingLocked = computed(() => modalOriginalStatus.value === 'Hoàn thành' || modalOriginalStatus.value === 'Đã huỷ')

// Methods

const fetchTimeline = async () => {
  try {
    const timelineResponse = await fetchTimelineByHoaDonId(Number(invoiceId.value))
    timelineData.value = timelineResponse || []
  } catch (timelineError) {
    console.warn('Failed to load timeline:', timelineError)
    timelineData.value = []
  }
}

const fetchInvoiceDetail = async () => {
  try {
    loading.value = true

    // Fetch timeline data in parallel
    await fetchTimeline()

    // Fetch payment methods
    try {
      const paymentResponse = await axios.get(`/api/hinh-thuc-thanh-toan-management/by-hoa-don/${invoiceId.value}`)
      // ResponseObject structure: { data: [...], message: "...", isSuccess: true }
      if (paymentResponse.data) {
        // Check if data is directly in response.data or in response.data.data
        if (Array.isArray(paymentResponse.data)) {
          paymentMethods.value = paymentResponse.data
        } else if (paymentResponse.data.data && Array.isArray(paymentResponse.data.data)) {
          paymentMethods.value = paymentResponse.data.data
        } else {
          paymentMethods.value = []
        }
      } else {
        paymentMethods.value = []
      }
    } catch (paymentError) {
      console.warn(' Failed to load payment methods:', paymentError)
      paymentMethods.value = []
    }

    // Ưu tiên lấy từ API thông tin đơn hàng mới nhất (có đầy đủ hoaDonChiTiets)
    try {
      const orderInfoResponse = await axios.get(`/api/thong-tin-hoa-don-management/latest-by-hoa-don/${invoiceId.value}`)

      if (orderInfoResponse.data && orderInfoResponse.data.data) {
        const orderInfo = orderInfoResponse.data.data
        invoice.value = orderInfo.idHoaDon
        console.log('[InvoiceData] Loaded from orderInfo:', invoice.value)
        loading.value = false
        return // Thành công, không cần fallback
      }
    } catch (orderInfoError) {
      // Silent error handling
    }

    // Fallback: thử API hóa đơn trực tiếp
    const invoiceResponse = await axios.get(`/api/invoice-management/${invoiceId.value}`)

    if (invoiceResponse.data && invoiceResponse.data.data) {
      invoice.value = invoiceResponse.data.data
      console.log('[InvoiceData] Loaded from hoa-don API:', invoice.value)

      // Thử lấy thông tin đơn hàng từ API thông tin đơn hàng mới (không bắt buộc)
      try {
        const orderInfoResponse = await axios.get(`/api/thong-tin-hoa-don-management/latest-by-hoa-don/${invoiceId.value}`)

        if (orderInfoResponse.data && orderInfoResponse.data.data) {
          const orderInfo = orderInfoResponse.data.data
          // Cập nhật thông tin từ API thông tin đơn hàng
          invoice.value.ngayTao = orderInfo.ngayTao || invoice.value.ngayTao
          invoice.value.ngayThanhToan = orderInfo.ngayThanhToan || invoice.value.ngayThanhToan
          invoice.value.tenNhanVien = orderInfo.tenNhanVien || invoice.value.tenNhanVien
          invoice.value.maNhanVien = orderInfo.maNhanVien || invoice.value.maNhanVien
          invoice.value.tongTienHang = orderInfo.tongTienHang || invoice.value.tongTien

          // Cập nhật thông tin khách hàng từ API thông tin đơn hàng
          invoice.value.tenKhachHang = orderInfo.tenKhachHang || invoice.value.tenKhachHang
          invoice.value.maKhachHang = orderInfo.maKhachHang || invoice.value.maKhachHang
          invoice.value.emailKhachHang = orderInfo.emailKhachHang || invoice.value.email
          invoice.value.soDienThoaiKhachHang = orderInfo.soDienThoaiKhachHang || invoice.value.soDienThoai
          invoice.value.diaChiKhachHang = orderInfo.diaChiKhachHang || invoice.value.diaChiKhachHang
        }
      } catch {
        // Silent error handling
      }
    } else if (invoiceResponse.data) {
      // Nếu response trực tiếp là dữ liệu (không có wrapper data)
      invoice.value = invoiceResponse.data

      // Thử lấy thông tin đơn hàng từ API thông tin đơn hàng mới (không bắt buộc)
      try {
        const orderInfoResponse = await axios.get(`/api/thong-tin-hoa-don-management/latest-by-hoa-don/${invoiceId.value}`)

        if (orderInfoResponse.data && orderInfoResponse.data.data) {
          const orderInfo = orderInfoResponse.data.data
          // Cập nhật thông tin từ API thông tin đơn hàng
          invoice.value.ngayTao = orderInfo.ngayTao || invoice.value.ngayTao
          invoice.value.ngayThanhToan = orderInfo.ngayThanhToan || invoice.value.ngayThanhToan
          invoice.value.tenNhanVien = orderInfo.tenNhanVien || invoice.value.tenNhanVien
          invoice.value.maNhanVien = orderInfo.maNhanVien || invoice.value.maNhanVien
          invoice.value.tongTienHang = orderInfo.tongTienHang || invoice.value.tongTien

          // Cập nhật thông tin khách hàng từ API thông tin đơn hàng
          invoice.value.tenKhachHang = orderInfo.tenKhachHang || invoice.value.tenKhachHang
          invoice.value.maKhachHang = orderInfo.maKhachHang || invoice.value.maKhachHang
          invoice.value.emailKhachHang = orderInfo.emailKhachHang || invoice.value.email
          invoice.value.soDienThoaiKhachHang = orderInfo.soDienThoaiKhachHang || invoice.value.soDienThoai
          invoice.value.diaChiKhachHang = orderInfo.diaChiKhachHang || invoice.value.diaChiKhachHang
        }
      } catch {
        // Silent error handling
      }
    } else {
      // Fallback to sample data for testing
      invoice.value = {
        id: invoiceId.value,
        maHoaDon: `HD${String(invoiceId.value).padStart(6, '0')}`,
        ngayTao: new Date().toISOString(),
        tenNhanVien: 'Nguyễn Văn A',
        tenKhachHang: 'Trần Thị B',
        tongTienSauGiam: 2500000,
        giamGia: 100000,
        phuongThucThanhToan: 'Tiền mặt',
        trangThai: true, // true = Hoàn thành, false = Chờ xác nhận
        hoaDonChiTiets: [
          {
            tenSanPham: 'Giày Nike Air Max 270',
            mauSac: 'Đen',
            kichThuoc: '42',
            donGia: 1500000,
            soLuong: 1,
            thanhTien: 1500000,
          },
          {
            tenSanPham: 'Áo thun Adidas',
            mauSac: 'Trắng',
            kichThuoc: 'L',
            donGia: 800000,
            soLuong: 1,
            thanhTien: 800000,
          },
        ],
      }
    }

    // Don't need to fetch thongTinDonHangs separately - we use timelineData instead
    // The timeline already contains status change information including address changes (status id = 8)

    // Check if address has already been changed when loading the invoice
    checkAddressChangeStatus()
  } catch (error: any) {
    // Use sample data on error
    invoice.value = {
      id: invoiceId.value,
      maHoaDon: `HD${String(invoiceId.value).padStart(6, '0')}`,
      ngayTao: new Date().toISOString(),
      tenNhanVien: 'Nguyễn Văn A',
      tenKhachHang: 'Trần Thị B',
      tongTienSauGiam: 2500000,
      giamGia: 100000,
      phuongThucThanhToan: 'Tiền mặt',
      trangThai: true, // true = Hoàn thành, false = Chờ xác nhận
      hoaDonChiTiets: [
        {
          tenSanPham: 'Giày Nike Air Max 270',
          mauSac: 'Đen',
          kichThuoc: '42',
          donGia: 1500000,
          soLuong: 1,
          thanhTien: 1500000,
        },
        {
          tenSanPham: 'Áo thun Adidas',
          mauSac: 'Trắng',
          kichThuoc: 'L',
          donGia: 800000,
          soLuong: 1,
          thanhTien: 800000,
        },
      ],
    }

    // Check if address has already been changed
    checkAddressChangeStatus()
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString: string) => {
  if (!dateString) return 'Chưa xác định'
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

/**
 * Determine whether the delivery address can still be edited
 * Populates addressLockReason when the address is locked either by status or previous change
 */
const checkAddressChangeStatus = () => {
  addressLockReason.value = null

  const currentStatusText = getHighestPriorityStatusFromInvoice() || 'Chờ xác nhận'
  if (currentStatusText !== 'Chờ xác nhận') {
    addressLockReason.value = 'Chỉ có thể thay đổi địa chỉ khi đơn hàng đang ở trạng thái "Chờ xác nhận"'
    return
  }

  if (invoice.value?.thongTinDonHangs && Array.isArray(invoice.value.thongTinDonHangs)) {
    for (let i = 0; i < invoice.value.thongTinDonHangs.length; i++) {
      const item = invoice.value.thongTinDonHangs[i]
      const statusId = item.idTrangThaiDonHang?.id
      const statusName = (item.tenTrangThaiDonHang || '').toLowerCase()
      const note = `${item.ghiChu || ''} ${item.moTa || ''}`.toLowerCase()
      const noteContainsAddressChange = note.includes('thay đổi địa chỉ')

      if (statusId === 8 || statusName.includes('thay đổi địa chỉ') || noteContainsAddressChange) {
        addressLockReason.value = 'Địa chỉ giao hàng đã được thay đổi trước đó'
        return
      }
    }
  }
}

const formatCurrency = (amount: number) => {
  if (!amount) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const getStatusColor = (status: any) => {
  // Xử lý cả boolean và string
  if (typeof status === 'boolean') {
    return status ? 'green' : 'orange'
  }

  if (typeof status === 'string') {
    const statusColors: { [key: string]: string } = {
      'Chờ xác nhận': 'orange',
      'Chờ giao hàng': 'orange',
      'Đã xác nhận': 'orange',
      'Đang giao': 'blue',
      'Đang giao hàng': 'blue',
      'Hoàn thành': 'green',
      'Đã huỷ': 'red',
      'Đã hủy': 'red',
      'Đã thanh toán': 'green',
      'Chờ thanh toán': 'orange',
      true: 'green',
      false: 'orange',
    }
    return statusColors[status] || 'gray'
  }

  return 'gray'
}

const goBack = () => {
  router.back()
}

const printInvoice = () => {
  window.print()
}

const goToPrintPage = () => {
  if (invoice.value && invoice.value.id) {
    router.push({ name: 'InHoaDon', params: { id: invoice.value.id } })
  } else {
    console.error('No invoice ID available')
  }
}

const handleViewProductDetail = (product: any) => {
  // Có thể mở modal hoặc chuyển trang chi tiết sản phẩm
}

/**
 * Load provinces from GHN API
 */
const loadProvinces = async () => {
  try {
    isLoadingAddress.value = true
    const response = await fetchProvinces()
    provinces.value = response || []
  } catch (error) {
    console.error('Error loading provinces:', error)
    Message.error('Lỗi khi tải danh sách tỉnh/thành phố')
  } finally {
    isLoadingAddress.value = false
  }
}

/**
 * Handle province change and load districts
 */
const onProvinceChange = async () => {
  updateLocationForm.value.quan = ''
  updateLocationForm.value.phuong = ''
  districtsList.value = []
  wardsList.value = []

  if (!updateLocationForm.value.thanhPho) return

  try {
    isLoadingAddress.value = true
    // Find province code
    const province = provinces.value.find((p) => p.value === updateLocationForm.value.thanhPho)
    if (province) {
      const response = await fetchDistrictsByProvinceCode(province.code)
      districtsList.value = response || []
    }
  } catch (error) {
    console.error('Error loading districts:', error)
    Message.error('Lỗi khi tải danh sách quận/huyện')
  } finally {
    isLoadingAddress.value = false
  }
}

/**
 * Handle district change and load wards, then recalculate shipping fee
 */
const onDistrictChange = async () => {
  updateLocationForm.value.phuong = ''
  wardsList.value = []

  if (!updateLocationForm.value.quan) return

  try {
    isLoadingAddress.value = true
    // Find district code
    const district = districtsList.value.find((d) => d.value === updateLocationForm.value.quan)
    if (district) {
      const response = await fetchWardsByDistrictCode(district.code)
      wardsList.value = response || []

      // Auto-recalculate shipping fee
      await recalculateShippingFee()
    }
  } catch (error) {
    console.error('Error loading wards:', error)
    Message.error('Lỗi khi tải danh sách phường/xã')
  } finally {
    isLoadingAddress.value = false
  }
}

/**
 * Recalculate shipping fee when location changes
 */
const recalculateShippingFee = async () => {
  if (!updateLocationForm.value.thanhPho || !updateLocationForm.value.quan || !updateLocationForm.value.phuong) {
    calculatedShippingFee.value = null
    shippingFeeError.value = ''
    return
  }

  // Don't recalculate if address hasn't changed significantly
  if (
    updateLocationForm.value.thanhPho === originalInvoiceLocation.value.thanhPho &&
    updateLocationForm.value.quan === originalInvoiceLocation.value.quan &&
    updateLocationForm.value.phuong === originalInvoiceLocation.value.phuong &&
    updateLocationForm.value.diaChiCuThe === originalInvoiceLocation.value.diaChiCuThe
  ) {
    calculatedShippingFee.value = null
    calculatedSurcharge.value = null
    shippingFeeError.value = ''
    return
  }

  try {
    isCalculatingShippingFee.value = true
    shippingFeeError.value = ''

    const newLocation: ShippingLocation = {
      thanhPho: updateLocationForm.value.thanhPho,
      quan: updateLocationForm.value.quan,
      phuong: updateLocationForm.value.phuong,
      diaChiCuThe: updateLocationForm.value.diaChiCuThe,
    }

    // Call GHN API to calculate shipping fee from current location to new location
    const result = await calculateShippingFeeFromGHN(newLocation)

    if (result && result.fee) {
      const newFee = result.fee
      const currentFee = invoice.value.phiVanChuyen || 0
      const difference = newFee - currentFee // Có thể là dương (phụ phí) hoặc âm (hoàn phí)

      calculatedShippingFee.value = newFee
      calculatedSurcharge.value = difference // Lưu giá trị thực tế (có thể âm)
      updateForm.value.phiGiaoHang = newFee
    } else {
      throw new Error('Invalid shipping fee response')
    }
  } catch (error: any) {
    console.error('Error calculating shipping fee:', error)
    shippingFeeError.value = error?.message || 'Không thể tính phí giao hàng. Vui lòng thử lại.'
    calculatedShippingFee.value = null
    calculatedSurcharge.value = null
    updateForm.value.phiGiaoHang = 0
  } finally {
    isCalculatingShippingFee.value = false
  }
}

const showUpdateModal = async () => {
  if (!invoice.value) return

  // Check if address has already been changed
  checkAddressChangeStatus()

  // Get current status text from trangThaiDonHang (from getHighestPriorityStatusFromInvoice)
  const currentStatusText = getHighestPriorityStatusFromInvoice() || 'Chờ xác nhận'
  const suggestedCollectionAmount = calculatedRemainingAmount.value

  modalOriginalStatus.value = currentStatusText

  // Populate form with current invoice data
  isProgrammaticPaymentSync.value = true
  updateForm.value = {
    maHoaDon: invoice.value.maHoaDon || `HD${String(invoice.value.id).padStart(6, '0')}`,
    loaiDon: invoice.value.loaiDon || false,
    trangThai: invoice.value.trangThai || false,
    trangThaiText: currentStatusText,
    tenNguoiNhan: invoice.value.tenNguoiNhan || invoice.value.tenKhachHang || '',
    soDienThoaiNguoiNhan: invoice.value.soDienThoaiNguoiNhan || invoice.value.soDienThoai || '',
    diaChiNhanHang: invoice.value.diaChiNhanHang || invoice.value.diaChiNguoiNhan || invoice.value.diaChi || '',
    emailNguoiNhan: invoice.value.emailNguoiNhan || invoice.value.email || '',
    ghiChu: invoice.value.ghiChu || '',
    soTienDaThanhToan: 0,
    phiGiaoHang: invoice.value.phiVanChuyen || 0, // Current shipping fee
  }
  paymentAmountManuallyEdited.value = false
  nextTick(() => {
    isProgrammaticPaymentSync.value = false
  })

  // Reset location form
  updateLocationForm.value = {
    thanhPho: '',
    quan: '',
    phuong: '',
    diaChiCuThe: '',
  }

  activeUpdateTab.value = 'order'
  updateModalVisible.value = true

  // Load provinces when modal opens
  await loadProvinces()

  // Try to parse and fill current address
  const currentAddress = updateForm.value.diaChiNhanHang
  if (currentAddress) {
    await parseAndFillAddress(currentAddress)
    // After parsing address, display current shipping fee
    calculatedShippingFee.value = invoice.value.phiVanChuyen || 0
  }

  // Reset surcharge when modal opens
  calculatedSurcharge.value = null

  // Store the original location details for comparison
  // This is used to detect when user changes the address
  originalInvoiceLocation.value = {
    thanhPho: updateLocationForm.value.thanhPho,
    quan: updateLocationForm.value.quan,
    phuong: updateLocationForm.value.phuong,
    diaChiCuThe: updateLocationForm.value.diaChiCuThe,
  }

  console.log('[Modal] Original location set:', originalInvoiceLocation.value)
}

/**
 * Parse current address string and fill location form
 * Address format: "Địa chỉ cụ thể, Phường, Quận, Tỉnh"
 */
const parseAndFillAddress = async (addressString: string) => {
  if (!addressString) return

  // Split address by comma
  const parts = addressString
    .split(',')
    .map((p) => p.trim())
    .filter((p) => p)

  if (parts.length === 0) return

  // Try to identify components
  // Usually format is: "số nhà đường, phường, quận, tỉnh"
  // So we work backwards: last is tỉnh, second-to-last is quận, etc.

  if (parts.length >= 1) {
    // Last part should be tỉnh
    const thanhPhoName = parts[parts.length - 1]
    const province = provinces.value.find(
      (p) => p.value.toLowerCase().includes(thanhPhoName.toLowerCase()) || thanhPhoName.toLowerCase().includes(p.value.toLowerCase())
    )
    if (province) {
      updateLocationForm.value.thanhPho = province.value

      // Load districts for this province
      try {
        const districts = await fetchDistrictsByProvinceCode(province.code)
        districtsList.value = districts || []

        // Try to find quận
        if (parts.length >= 2) {
          const quanName = parts[parts.length - 2]
          const district = districtsList.value.find(
            (d) => d.value.toLowerCase().includes(quanName.toLowerCase()) || quanName.toLowerCase().includes(d.value.toLowerCase())
          )
          if (district) {
            updateLocationForm.value.quan = district.value

            // Load wards for this district
            try {
              const wards = await fetchWardsByDistrictCode(district.code)
              wardsList.value = wards || []

              // Try to find phường
              if (parts.length >= 3) {
                const phuongName = parts[parts.length - 3]
                const ward = wardsList.value.find(
                  (w) =>
                    w.value.toLowerCase().includes(phuongName.toLowerCase()) || phuongName.toLowerCase().includes(w.value.toLowerCase())
                )
                if (ward) {
                  updateLocationForm.value.phuong = ward.value
                }
              }
            } catch (error) {
              console.error('Error loading wards:', error)
            }
          }
        }
      } catch (error) {
        console.error('Error loading districts:', error)
      }

      // Set địa chỉ cụ thể (first part or first few parts)
      if (parts.length >= 3) {
        // Join all parts except last 3 (tỉnh, quận, phường)
        const diaChiCuThe = parts.slice(0, parts.length - 3).join(', ')
        updateLocationForm.value.diaChiCuThe = diaChiCuThe
      } else if (parts.length > 1) {
        // If we have less parts, use the first part as địa chỉ cụ thể
        updateLocationForm.value.diaChiCuThe = parts[0]
      }
    }
  }
}

const closeUpdateModal = () => {
  updateModalVisible.value = false
  // Reset form
  updateForm.value = {
    maHoaDon: '',
    loaiDon: false,
    trangThai: false,
    trangThaiText: '',
    tenNguoiNhan: '',
    soDienThoaiNguoiNhan: '',
    diaChiNhanHang: '',
    emailNguoiNhan: '',
    ghiChu: '',
    soTienDaThanhToan: 0,
  }
  paymentAmountManuallyEdited.value = false
  isProgrammaticPaymentSync.value = false
  modalOriginalStatus.value = 'Chờ xác nhận'
  updateLocationForm.value = {
    thanhPho: '',
    quan: '',
    phuong: '',
    diaChiCuThe: '',
  }
  originalInvoiceLocation.value = {
    thanhPho: '',
    quan: '',
    phuong: '',
    diaChiCuThe: '',
  }
}

const handleSaveUpdate = async () => {
  if (!invoice.value) return

  saving.value = true
  try {
    // Map status text to priority ID
    // Priority map: 'Đã huỷ': 6, 'Hoàn thành': 7, 'Đã giao hàng': 5, 'Đang giao hàng': 4,
    // 'Đang xử lý': 3, 'Đã xác nhận': 2, 'Chờ xác nhận': 1
    let trangThai = false
    let idTrangThaiDonHang: number = 1 // Default to Chờ xác nhận

    const statusPriorityMap: { [key: string]: number } = {
      'Đã huỷ': 6,
      'Hoàn thành': 7,
      'Đã giao hàng': 5,
      'Đang giao hàng': 4,
      'Đang xử lý': 3,
      'Đã xác nhận': 2,
      'Chờ xác nhận': 1,
    }

    const statusName = updateForm.value.trangThaiText
    idTrangThaiDonHang = statusPriorityMap[statusName] || 1 // Default to 1 if not found

    const currentStatusText = getHighestPriorityStatusFromInvoice() || 'Chờ xác nhận'
    const currentStatusOrder = STATUS_FLOW_ORDER[currentStatusText] || 0
    const newStatusOrder = STATUS_FLOW_ORDER[statusName] || 0

    if (currentStatusOrder > 0 && newStatusOrder > 0 && newStatusOrder < currentStatusOrder) {
      Message.warning('Không thể quay lại trạng thái trước đó.')
      saving.value = false
      return
    }

    const additionalAmount = Math.max(additionalPaymentAmount.value, 0)
    const finalTotalRounded = Math.max(Math.round(calculatedFinalTotalRaw.value), 0)
    const paidSoFarRounded = Math.max(Math.round(calculatedPaidAmountRaw.value), 0)
    const expectedCollection = Math.max(finalTotalRounded - paidSoFarRounded, 0)
    const projectedPaidTotal = paidSoFarRounded + additionalAmount

    if (statusName === 'Hoàn thành') {
      const collectionDelta = expectedCollection - additionalAmount

      if (collectionDelta > 0) {
        Message.error(
          `Đơn hàng vẫn còn thiếu ${formatCurrency(collectionDelta)} sau khi thu thêm. Thu đủ trước khi chuyển trạng thái Hoàn thành.`
        )
        saving.value = false
        return
      }

      if (collectionDelta < 0) {
        Message.error(`Số tiền thu thêm vượt quá số tiền cần thu ${formatCurrency(Math.abs(collectionDelta))}. Vui lòng kiểm tra lại.`)
        saving.value = false
        return
      }

      const totalDelta = projectedPaidTotal - finalTotalRounded
      if (totalDelta !== 0) {
        if (totalDelta > 0) {
          Message.error(
            `Số tiền thu thêm khiến tổng thanh toán vượt tổng tiền sau giảm ${formatCurrency(totalDelta)}. Vui lòng kiểm tra lại.`
          )
        } else {
          Message.error(
            `Đơn hàng vẫn còn thiếu ${formatCurrency(Math.abs(totalDelta))} sau khi thu thêm. Thu đủ trước khi chuyển trạng thái Hoàn thành.`
          )
        }
        saving.value = false
        return
      }
    }

    // Set trangThai boolean based on status
    if (statusName === 'Hoàn thành' || statusName === 'Đã giao hàng' || statusName === 'Đã huỷ') {
      trangThai = true
    } else {
      trangThai = false
    }

    // Get current values to check if they changed
    const currentTrangThai = invoice.value.trangThai
    const currentLoaiDon = invoice.value.loaiDon

    // Get idNhanVien - prioritize from invoice, then userStore, then createBy
    let idNhanVienValue: number | null = null
    if (invoice.value.idNhanVien) {
      idNhanVienValue = typeof invoice.value.idNhanVien === 'number' ? invoice.value.idNhanVien : invoice.value.idNhanVien?.id || null
    }
    if (!idNhanVienValue && userStore.id) {
      idNhanVienValue = userStore.id
    }
    if (!idNhanVienValue && invoice.value.createBy) {
      idNhanVienValue = invoice.value.createBy
    }

    // Detect if address has changed
    const normalizeLocationValue = (value?: string) => (value || '').trim()

    const originalAddress = { ...originalInvoiceLocation.value }
    const newAddress = {
      thanhPho: updateLocationForm.value.thanhPho,
      quan: updateLocationForm.value.quan,
      phuong: updateLocationForm.value.phuong,
      diaChiCuThe: updateLocationForm.value.diaChiCuThe,
    }

    const addressChanged =
      normalizeLocationValue(originalAddress.thanhPho) !== normalizeLocationValue(newAddress.thanhPho) ||
      normalizeLocationValue(originalAddress.quan) !== normalizeLocationValue(newAddress.quan) ||
      normalizeLocationValue(originalAddress.phuong) !== normalizeLocationValue(newAddress.phuong) ||
      normalizeLocationValue(originalAddress.diaChiCuThe) !== normalizeLocationValue(newAddress.diaChiCuThe)

    // Validation: Check if address change is allowed based on order status
    if (addressChanged) {
      const currentStatusText = getHighestPriorityStatusFromInvoice() || 'Chờ xác nhận'
      if (currentStatusText === 'Đã huỷ' || currentStatusText === 'Hoàn thành') {
        Message.error(
          `Không thể thay đổi địa chỉ khi đơn hàng ở trạng thái "${currentStatusText}". Chỉ có thể thay đổi địa chỉ trước khi đơn hàng bị huỷ hoặc hoàn thành.`
        )
        saving.value = false
        return
      }
    }

    // If address changed, calculate new shipping fee and surcharge/refund
    let feeChangeResult: ShippingFeeChangeResult | null = null
    let feePayload: ReturnType<typeof createFeeUpdatePayload> | null = null
    if (addressChanged) {
      try {
        const shippingLocation: ShippingLocation = {
          thanhPho: newAddress.thanhPho,
          quan: newAddress.quan,
          phuong: newAddress.phuong,
          diaChiCuThe: updateLocationForm.value.diaChiCuThe,
        }

        const newFeeResponse = await calculateShippingFeeFromGHN(shippingLocation)
        const newShippingFee = newFeeResponse.fee
        const currentShippingFee = invoice.value.phiVanChuyen || 0

        // Sử dụng service để tính toán phụ phí/hoàn phí
        feeChangeResult = calculateShippingFeeChange(currentShippingFee, newShippingFee)

        console.log('[AddressChange] Shipping fee change:', feeChangeResult)

        // Address changed: do NOT force status id = 8 here.
        // Backend endpoint `/send-address-change-notification` will append a single
        // "Thay đổi địa chỉ giao hàng" record to history. Keeping the current
        // `idTrangThaiDonHang` avoids creating duplicate history entries.
      } catch (error) {
        console.error('[AddressChange] Error calculating new shipping fee:', error)
        Message.error('Lỗi khi tính phí vận chuyển mới')
        saving.value = false
        return
      }
    }

    const statusChanged = statusName !== currentStatusText

    const updateData: any = {
      // Always include loaiDon and trangThai to ensure they are updated
      loaiDon: updateForm.value.loaiDon,
      trangThai,
    }

    // Only include idTrangThaiDonHang when the status actually changed to avoid duplicate history rows
    if (statusChanged) {
      updateData.idTrangThaiDonHang = idTrangThaiDonHang
    }

    // Only include idNhanVien if we have a valid value
    if (idNhanVienValue) {
      updateData.idNhanVien = idNhanVienValue
    }

    // Only include fields that have values to avoid overwriting with empty strings
    if (updateForm.value.tenNguoiNhan) {
      updateData.tenNguoiNhan = updateForm.value.tenNguoiNhan
    }

    // Build address from structured form
    const addressParts = [
      updateLocationForm.value.diaChiCuThe,
      updateLocationForm.value.phuong,
      updateLocationForm.value.quan,
      updateLocationForm.value.thanhPho,
    ].filter((part) => part && part.trim())

    if (addressParts.length > 0) {
      updateData.diaChiNhanHang = addressParts.join(', ')
    }

    if (updateForm.value.soDienThoaiNguoiNhan) {
      updateData.soDienThoaiNguoiNhan = updateForm.value.soDienThoaiNguoiNhan
    }
    if (updateForm.value.emailNguoiNhan) {
      updateData.emailNguoiNhan = updateForm.value.emailNguoiNhan
    }
    if (updateForm.value.ghiChu !== undefined && updateForm.value.ghiChu !== null) {
      updateData.ghiChu = updateForm.value.ghiChu
    }

    // Add surcharge/refund info if address changed
    if (addressChanged && feeChangeResult) {
      if (shouldUpdateShippingFee(feeChangeResult)) {
        feePayload = createFeeUpdatePayload(feeChangeResult)
        console.log('[FeeUpdate] Fee payload:', feePayload)
      }
    }

    // Include payment amount when completing order for delivery (online)
    let amountToPersist = additionalAmount
    let totalPaidToPersist = projectedPaidTotal

    if (statusName === 'Hoàn thành') {
      totalPaidToPersist = finalTotalRounded
      amountToPersist = Math.max(finalTotalRounded - paidSoFarRounded, 0)
    }

    if (statusName === 'Hoàn thành' || amountToPersist > 0) {
      updateData.soTienDaThanhToan = totalPaidToPersist
      if (amountToPersist > 0) {
        updateData.soTienThuThem = amountToPersist
      }
      const methodId = primaryPaymentMethodId.value
      if (methodId) {
        updateData.idPhuongThucThanhToan = methodId
      }
    }

    console.log('[SaveUpdate] Final update data:', updateData)

    // Call API to update invoice
    let updateSucceeded = false
    try {
      const response = await axios.put(`/api/invoice-management/update/${invoice.value.id}`, updateData)

      // ResponseObject has isSuccess field (not success)
      // Backend returns: { isSuccess: true, data: null, message: "..." }
      // Check response.data structure
      if (response?.data && response.data.isSuccess === false) {
        // Explicitly failed
        const errorMessage = response.data.message || 'Cập nhật thất bại'
        console.error('Update failed:', response.data)
        throw new Error(errorMessage)
      }

      // Success case - if we got here, assume success
      // Even if response.status is undefined, backend may have updated successfully
      updateSucceeded = true
    } catch (axiosError: any) {
      // Axios throws error for non-2xx responses or network errors
      console.error('Axios error:', axiosError)

      // If axios got a response from server (even if error status)
      if (axiosError?.response) {
        const responseData = axiosError.response.data
        const { status } = axiosError.response

        // Check if it's actually a success response wrapped in error
        // Backend may return success but axios throws error due to interceptor or other reasons
        if (responseData?.isSuccess === true || responseData?.success === true) {
          // Success response
          updateSucceeded = true
        } else if (status >= 200 && status < 300) {
          // Status is OK - assume success (backend updated successfully)
          console.warn('Status OK but no isSuccess field, assuming success')
          updateSucceeded = true
        } else {
          // It's a real error response
          let errorMessage = 'Cập nhật thất bại'
          if (responseData?.message) {
            errorMessage = responseData.message
          } else if (responseData?.error) {
            errorMessage = responseData.error
          } else if (axiosError.message) {
            errorMessage = axiosError.message
          }
          throw new Error(errorMessage)
        }
      } else {
        // Network error or no response - but backend may have updated successfully
        // Since reload shows the update, we'll assume success and refresh
        console.warn('No response from server, but assuming update succeeded (will refresh)')
        updateSucceeded = true
      }
    }

    // If update succeeded (either from try or catch), refresh data
    if (updateSucceeded) {
      let addressChangeNotificationSent = false

      if (addressChanged) {
        const oldAddressPayload = {
          thanhPho: normalizeLocationValue(originalAddress.thanhPho),
          quan: normalizeLocationValue(originalAddress.quan),
          phuong: normalizeLocationValue(originalAddress.phuong),
          diaChiCuThe: normalizeLocationValue(originalAddress.diaChiCuThe),
        }

        const newAddressPayload = {
          thanhPho: normalizeLocationValue(newAddress.thanhPho),
          quan: normalizeLocationValue(newAddress.quan),
          phuong: normalizeLocationValue(newAddress.phuong),
          diaChiCuThe: normalizeLocationValue(newAddress.diaChiCuThe),
        }

        const shippingChangePayload = feeChangeResult
          ? {
              currentFee: Number(feeChangeResult.currentFee ?? invoice.value?.phiVanChuyen ?? 0),
              newFee: Number(feeChangeResult.newFee ?? updateForm.value.phiGiaoHang ?? 0),
              difference: Number(feeChangeResult.difference ?? 0),
              isExtra: feeChangeResult.isExtra ?? (feeChangeResult.difference ?? 0) > 0,
              description: feeChangeResult.description || 'Thay đổi địa chỉ giao hàng',
            }
          : {
              currentFee: Number(invoice.value?.phiVanChuyen || 0),
              newFee: Number(updateForm.value.phiGiaoHang || invoice.value?.phiVanChuyen || 0),
              difference:
                Number(updateForm.value.phiGiaoHang || invoice.value?.phiVanChuyen || 0) - Number(invoice.value?.phiVanChuyen || 0),
              isExtra:
                Number(updateForm.value.phiGiaoHang || invoice.value?.phiVanChuyen || 0) - Number(invoice.value?.phiVanChuyen || 0) > 0,
              description: 'Thay đổi địa chỉ giao hàng',
            }

        const addressChangePayload = {
          oldAddress: oldAddressPayload,
          newAddress: newAddressPayload,
          surcharge: feePayload?.phuPhi ?? (shippingChangePayload.isExtra ? shippingChangePayload.difference : 0),
          shippingFeeChange: shippingChangePayload,
        }
        console.log('[AddressChange] Notification payload to send:', addressChangePayload)
        try {
          await axios.post(`/api/invoice-management/send-address-change-notification/${invoice.value.id}`, addressChangePayload)
          addressChangeNotificationSent = true
          console.log('[AddressChange] Notification payload sent:', addressChangePayload)
        } catch (notificationError: any) {
          console.error('[AddressChange] Failed to send backend notification:', notificationError)
          Message.error(
            notificationError?.response?.data?.message || 'Không thể gửi thông báo thay đổi địa chỉ. Vui lòng thử lại hoặc kiểm tra log.'
          )
        }
      }

      // Show loading notification
      const loadingMessage = asyncNotification.showLoadingNotification('Đang cập nhật trạng thái đơn hàng...')

      await fetchInvoiceDetail()
      // Force refresh timeline to get latest updates
      // Add small delay to ensure backend has committed the transaction
      await new Promise((resolve) => setTimeout(resolve, 300))
      await fetchTimeline()

      // Check if address change status was actually created in backend (after refresh)
      let addressChangeStatusCreated = false
      if (addressChanged && invoice.value?.thongTinDonHangs) {
        for (const item of invoice.value.thongTinDonHangs) {
          const statusName = item.tenTrangThaiDonHang || ''
          if (statusName === 'Thay đổi địa chỉ giao hàng' || statusName.includes('Thay đổi địa chỉ')) {
            addressChangeStatusCreated = true
            break
          }
        }
      }

      // Email notification + voucher/refund logic handled by backend via the API call above

      closeUpdateModal()

      // Hide loader and show success notification
      if (loadingMessage?.close) {
        loadingMessage.close()
      }

      // Show appropriate success message based on whether address changed
      let successMessage = `Cập nhật trạng thái thành công: ${updateForm.value.trangThaiText}`
      if (addressChangeStatusCreated && addressChangeNotificationSent && feeChangeResult) {
        const feeInfo = formatShippingFeeInfo(feeChangeResult)
        successMessage = `Cập nhật thành công! ${feeInfo.icon} ${feeInfo.description}. Khách hàng sẽ nhận được thông báo qua email.`
      }

      asyncNotification.showSuccessNotification(
        {
          orderId: invoice.value.id,
          orderCode: invoice.value.maHoaDon,
          oldStatus: getStatusText(currentTrangThai),
          newStatus: updateForm.value.trangThaiText,
          message: successMessage,
        },
        5
      )
    }
  } catch (error: any) {
    console.error('Error updating invoice:', error)
    console.error('Error message:', error?.message)
    console.error('Error stack:', error?.stack)

    // Handle different error types
    let errorMessage = 'Có lỗi xảy ra khi cập nhật'
    if (error?.response?.data?.code) {
      errorMessage = error.response.data.code
    } else if (error?.message) {
      errorMessage = error.message
    }

    // Check if this is an address change error
    if (errorMessage.includes('Chỉ được phép thay đổi địa chỉ 1 lần')) {
      // Refresh the page to ensure the address lock state is set correctly
      await fetchInvoiceDetail()
      checkAddressChangeStatus()
      // Close the modal so user sees the updated state
      closeUpdateModal()
      asyncNotification.showErrorNotification(
        {
          orderId: invoice.value?.id,
          orderCode: invoice.value?.maHoaDon,
          message: 'Đơn hàng này đã được thay đổi địa chỉ trước đó. Không thể thay đổi lần nữa!',
        },
        5
      )
    } else if (errorMessage.includes('Số lượng sản phẩm')) {
      asyncNotification.showInventoryShortageNotification(errorMessage.replace('Số lượng sản phẩm yêu cầu không đủ: ', ''), 8)
    } else {
      asyncNotification.showErrorNotification(
        {
          orderId: invoice.value?.id,
          orderCode: invoice.value?.maHoaDon,
          message: errorMessage,
        },
        5
      )
    }
  } finally {
    saving.value = false
  }
}

// Lấy text trạng thái đơn
const getTrangThaiDonText = (trangThai: boolean) => {
  if (invoice.value?.ngayThanhToan && trangThai) {
    return 'Đã thanh toán'
  }
  if (trangThai) {
    return 'Đã xác nhận'
  }
  return 'Chờ xử lý'
}

// Lấy màu trạng thái đơn
const getTrangThaiDonColor = (trangThai: boolean) => {
  if (invoice.value?.ngayThanhToan && trangThai) {
    return 'green'
  }
  if (trangThai) {
    return 'blue'
  }
  return 'orange'
}

// Watch for province change
watch(
  () => updateLocationForm.value.thanhPho,
  async (newProvince) => {
    if (isAddressLocked.value) {
      return
    }
    console.log('[AddressWatch] Province changed to:', newProvince)
    if (newProvince) {
      await onProvinceChange()
    }
  }
)

// Watch for district change
watch(
  () => updateLocationForm.value.quan,
  async (newDistrict) => {
    if (isAddressLocked.value) {
      return
    }
    console.log('[AddressWatch] District changed to:', newDistrict)
    if (newDistrict) {
      await onDistrictChange()
    }
  }
)

// Watch for ward change to recalculate shipping fee
watch(
  () => updateLocationForm.value.phuong,
  async (newWard) => {
    if (isAddressLocked.value) {
      return
    }
    console.log('[AddressWatch] Ward changed to:', newWard, {
      quan: updateLocationForm.value.quan,
      thanhPho: updateLocationForm.value.thanhPho,
      diaChiCuThe: updateLocationForm.value.diaChiCuThe,
    })
    if (newWard && updateLocationForm.value.quan && updateLocationForm.value.thanhPho) {
      console.log('[AddressWatch] Calling recalculateShippingFee from Ward watch')
      await recalculateShippingFee()
    } else {
      console.warn('[AddressWatch] Ward watch conditions not met for recalculation')
    }
  }
)

// Watch for address detail change
watch(
  () => updateLocationForm.value.diaChiCuThe,
  (newDetail) => {
    if (isAddressLocked.value) {
      return
    }
    console.log('[AddressWatch] Address detail changed to:', newDetail)
  }
)

// Watch for address detail change to recalculate shipping fee
watch(
  () => updateLocationForm.value.diaChiCuThe,
  async (newAddress) => {
    if (isAddressLocked.value) {
      return
    }
    console.log('[AddressWatch] Address detail changed to:', newAddress, {
      phuong: updateLocationForm.value.phuong,
      quan: updateLocationForm.value.quan,
      thanhPho: updateLocationForm.value.thanhPho,
    })
    if (newAddress && updateLocationForm.value.phuong && updateLocationForm.value.quan && updateLocationForm.value.thanhPho) {
      console.log('[AddressWatch] Calling recalculateShippingFee from address detail watch')
      await recalculateShippingFee()
    } else {
      console.warn('[AddressWatch] Address detail watch conditions not met for recalculation')
    }
  }
)

// Lifecycle
onMounted(() => {
  fetchInvoiceDetail()
})
</script>

<style scoped>
.invoice-detail-page {
  padding: 20px;
  background-color: var(--color-fill-2);
  min-height: calc(100vh - 80px);
}

/* Simple Breadcrumb */
.simple-breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  padding: 8px 0;
}

.breadcrumb-separator {
  color: #86909c;
  font-size: 14px;
}

.breadcrumb-current {
  color: #1d2129;
  font-weight: 500;
  font-size: 14px;
}

/* Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: white;
  padding: 20px 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1d2129;
  margin: 0;
}

.status-badge {
  font-size: 14px;
  font-weight: 500;
}

.connection-status-badge {
  font-size: 13px;
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 4px;
  animation: pulse 2s infinite;
}

.connection-status-badge[color='green'] {
  background-color: #f0fdf4;
  border-color: #22c55e;
  color: #16a34a;
}

.connection-status-badge[color='red'] {
  background-color: #fef2f2;
  border-color: #ef4444;
  color: #dc2626;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

.header-right {
  display: flex;
  gap: 12px;
}

.back-button,
.update-button,
.print-button {
  height: 36px;
  padding: 0 16px;
}

.update-button {
  background-color: #165dff;
  border-color: #165dff;
  color: white;
}

.update-button:hover {
  background-color: #0e42d2;
  border-color: #0e42d2;
}

/* Loading */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.loading-state p {
  margin-top: 16px;
  color: #86909c;
  font-size: 16px;
}

/* Debug Panel */
.debug-panel {
  margin-bottom: 20px;
  background: #f0f8ff;
  border: 1px solid #1890ff;
}

.debug-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.debug-content p {
  margin: 0;
  font-size: 14px;
}

/* Horizontal Timeline */
.timeline-status-card {
  margin-bottom: 24px;
}

.horizontal-timeline {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 24px 0;
  position: relative;
}

.timeline-stage {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  min-width: 0;
}

.stage-connector {
  position: absolute;
  left: calc(-50% + 20px);
  right: calc(50% - 20px);
  top: 20px;
  height: 2px;
  background-color: #e5e6eb;
  z-index: 0;
}

.stage-connector.active {
  background-color: #165dff;
}

.stage-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e5e6eb;
  color: #86909c;
  font-size: 20px;
  position: relative;
  z-index: 1;
  margin-bottom: 8px;
  transition: all 0.3s ease;
}

.stage-icon.active {
  background-color: #165dff;
  color: white;
}

.stage-icon.completed {
  background-color: #165dff;
  color: white;
}

.stage-label {
  font-size: 14px;
  font-weight: 500;
  color: #1d2129;
  margin-bottom: 4px;
  text-align: center;
}

.stage-time {
  font-size: 12px;
  color: #86909c;
  text-align: center;
}

/* Info Row */
.info-row {
  margin-bottom: 24px;
}

.info-list-compact {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item-compact {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.info-item-compact .label {
  font-weight: 500;
  color: #4e5969;
  font-size: 14px;
}

.info-item-compact .value {
  font-weight: 400;
  color: #1d2129;
  font-size: 14px;
  text-align: right;
}

.value-tag {
  font-size: 12px;
}

/* History List */
.history-summary-row {
  margin-bottom: 24px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.history-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 8px 0;
  position: relative;
  padding-left: 8px;
}

.history-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #00b42a;
  margin-top: 6px;
  flex-shrink: 0;
}

.history-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.history-main {
  display: flex;
  align-items: center;
  gap: 8px;
}

.history-title {
  font-size: 14px;
  font-weight: 500;
  color: #1d2129;
}

.history-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.history-user {
  font-size: 12px;
  color: #86909c;
}

.history-date {
  font-size: 12px;
  color: #86909c;
  white-space: nowrap;
}

.no-history {
  text-align: center;
  padding: 20px;
  color: #86909c;
}

/* Summary Section */
.summary-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.summary-label-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.summary-icon {
  font-size: 16px;
  color: #4e5969;
}

.summary-status {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f2f3f5;
  color: #00b42a;
  font-weight: 500;
}

.status-icon {
  color: #00b42a;
}

:deep(.arco-form-item-content-flex) {
  display: block;
}

/* Update Modal */
.update-modal :deep(.arco-modal-header) {
  padding: 20px 24px;
  border-bottom: 1px solid #e5e6eb;
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

.close-icon {
  font-size: 18px;
  color: #f53f3f;
  cursor: pointer;
  transition: color 0.2s;
}

.close-icon:hover {
  color: #cb2634;
}

.update-tabs {
  margin-top: 0;
}

.update-tabs :deep(.arco-tabs-nav) {
  padding: 0 24px;
  border-bottom: 1px solid #e5e6eb;
}

.update-tabs :deep(.arco-tabs-tab) {
  padding: 12px 16px;
  font-size: 14px;
  font-weight: 500;
  color: #4e5969;
}

.update-tabs :deep(.arco-tabs-tab-active) {
  color: #165dff;
}

.update-tabs :deep(.arco-tabs-tab-active .arco-tabs-tab-title) {
  color: #165dff;
  font-weight: 600;
}

.update-tabs :deep(.arco-tabs-content) {
  padding: 24px;
}

.address-info-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  margin-bottom: 16px;
}

.address-info-title {
  font-weight: 500;
}

.update-form {
  margin-top: 0;
}

.update-form :deep(.arco-form-item) {
  margin-bottom: 20px;
}

.update-form :deep(.arco-form-item-label) {
  font-size: 14px;
  font-weight: 500;
  color: #1d2129;
  margin-bottom: 8px;
}

.update-form :deep(.arco-input),
.update-form :deep(.arco-select),
.update-form :deep(.arco-textarea) {
  border-radius: 6px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #e5e6eb;
  background-color: #ffffff;
}

.cancel-btn {
  min-width: 100px;
  background-color: #ffffff;
  color: #1d2129;
  border-color: #d9d9d9;
}

.cancel-btn:hover {
  background-color: #f7f8fa;
  border-color: #165dff;
  color: #165dff;
}

.save-btn {
  min-width: 100px;
  background-color: #165dff;
  border-color: #165dff;
  color: #ffffff;
}

.save-btn:hover {
  background-color: #0e42d2;
  border-color: #0e42d2;
}

/* Cards */
.info-card,
.products-card,
.summary-card {
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
}

/* Info Block */
.info-block {
  margin-bottom: 16px;
}

.block-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 16px;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f2f3f5;
}

.info-item:last-child {
  border-bottom: none;
}

.label {
  font-weight: 500;
  color: #4e5969;
  min-width: 120px;
}

.value {
  font-weight: 400;
  color: #1d2129;
  text-align: right;
}

.value.total {
  font-size: 18px;
  font-weight: 600;
  color: #165dff;
}

/* Products Table */
.products-table {
  margin-top: 16px;
}

.stt-number {
  font-weight: 500;
  color: #1d2129;
}

.product-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-name {
  font-weight: 500;
  color: #1d2129;
}

.product-specs {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #86909c;
}

.spec-item {
  background: #f2f3f5;
  padding: 2px 6px;
  border-radius: 4px;
  color: #1d2129;
}

.price-text,
.total-text {
  font-weight: 500;
  color: #1d2129;
}

.quantity-tag {
  font-weight: 500;
}

/* Summary */
.summary-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f2f3f5;
}

.summary-item:last-child {
  border-bottom: none;
}

.summary-label {
  font-weight: 500;
  color: #4e5969;
}

.summary-value {
  font-weight: 400;
  color: #1d2129;
}

.summary-value.discount {
  color: #f53f3f;
}

.summary-value.surcharge {
  color: #f53f3f;
  font-weight: 500;
}

.summary-value.refund {
  color: #13c2c2;
  font-weight: 500;
}

.summary-value.paid-ok {
  color: #00b578;
  font-weight: 600;
}

.summary-value.overpaid {
  color: #165dff;
  font-weight: 600;
}

.summary-value.outstanding {
  color: #f77234;
  font-weight: 600;
}

.summary-item.final-total {
  border-top: 2px solid #165dff;
  margin-top: 8px;
  padding-top: 16px;
}

.summary-item.final-total .summary-label {
  font-size: 16px;
  font-weight: 600;
}

.summary-item.final-total .summary-value.total {
  font-size: 18px;
  font-weight: 700;
  color: #165dff;
}

/* Error State */
.error-state {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* Status Card */
.status-card {
  background: #f7f8fa;
  border: 1px solid #e5e6eb;
}

.status-content {
  padding: 16px 0;
}

.status-main {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-tag-large {
  font-size: 16px;
  padding: 8px 16px;
  border-radius: 4px;
  font-weight: 500;
}

.status-info p {
  margin: 6px 0;
  font-size: 14px;
  color: #4e5969;
}

.status-info .arco-tag {
  margin-left: 8px;
}

/* Order Info Card */
.order-info-card .info-block {
  margin-bottom: 16px;
}

.order-info-card .block-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 12px;
  color: #1d2129;
  border-bottom: 1px solid #e5e6eb;
  padding-bottom: 6px;
}

.order-info-card .info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f2f3f5;
}

.order-info-card .info-item:last-child {
  border-bottom: none;
}

.order-info-card .total-item {
  background: #f7f8fa;
  padding: 12px;
  border-radius: 4px;
  margin-top: 8px;
  border: 1px solid #e5e6eb;
}

.order-info-card .total-item .label,
.order-info-card .total-item .value {
  font-weight: 500;
  font-size: 14px;
}

.order-info-card .discount {
  color: #f53f3f;
}

/* Customer Info Card */
.customer-info-card .info-block {
  margin-bottom: 16px;
}

.customer-info-card .block-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 12px;
  color: #1d2129;
  border-bottom: 1px solid #e5e6eb;
  padding-bottom: 6px;
}

.customer-info-card .info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f2f3f5;
}

.customer-info-card .info-item:last-child {
  border-bottom: none;
}

/* Payment History Card */
.payment-history {
  padding: 12px 0;
}

.payment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 4px;
  border: 1px solid #e5e6eb;
}

.payment-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.payment-method {
  font-size: 14px;
  color: #1d2129;
}

.payment-amount {
  font-size: 16px;
  font-weight: 500;
  color: #00b42a;
}

.payment-date {
  font-size: 12px;
  color: #86909c;
}

.no-payment {
  text-align: center;
  padding: 32px;
  color: #86909c;
}

/* Summary Card */
.summary-content {
  padding: 16px 0;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f2f3f5;
}

.summary-row:last-child {
  border-bottom: none;
}

.summary-row.total-row {
  background: #f7f8fa;
  padding: 16px;
  border-radius: 4px;
  margin-top: 12px;
  border: 1px solid #e5e6eb;
}

.summary-row.total-row .summary-label,
.summary-row.total-row .summary-value {
  font-weight: 500;
  font-size: 16px;
}

.summary-label {
  font-size: 14px;
  color: #4e5969;
}

.summary-value {
  font-size: 14px;
  font-weight: 500;
  color: #1d2129;
}

.summary-value.discount {
  color: #f53f3f;
}

.summary-value.total {
  color: #00b42a;
  font-size: 16px;
}

.summary-value.paid-ok {
  color: #00b42a;
}

.summary-value.overpaid {
  color: #165dff;
}

.summary-value.outstanding {
  color: #f77234;
  font-weight: 600;
}

.payment-input-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #4e5969;
}

.payment-input-hint.warning {
  color: #f77234;
}

.payment-input-hint.success {
  color: #00b42a;
}

/* Product List */
.product-list-card {
  margin-bottom: 24px;
}

.product-list {
  padding: 16px 0;
}

.product-table-container {
  overflow-x: auto;
}

.product-table {
  width: 100%;
}

.product-name {
  font-weight: 600;
  color: var(--color-text-1);
  margin-bottom: 4px;
}

.product-details {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.detail-item {
  font-size: 12px;
  color: var(--color-text-3);
  background: var(--color-fill-2);
  padding: 2px 6px;
  border-radius: 4px;
}

.product-image {
  display: flex;
  justify-content: center;
  align-items: center;
}

.product-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid var(--color-border-2);
}

.no-image {
  width: 60px;
  height: 60px;
  background: var(--color-fill-2);
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: var(--color-text-3);
}

.no-image .arco-icon {
  font-size: 24px;
}

.quantity {
  font-weight: 600;
  color: var(--color-text-1);
}

.price {
  font-weight: 500;
  color: var(--color-text-2);
}

.total-price {
  font-weight: 600;
  color: var(--color-text-1);
}

.no-products {
  text-align: center;
  padding: 40px 0;
  color: var(--color-text-3);
}

.no-products .arco-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

/* Responsive */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .header-right {
    justify-content: center;
  }

  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .value {
    text-align: left;
  }
}

/* Print Styles */
@media print {
  /* Ẩn tất cả menu, header, sidebar khi in */
  .arco-layout-header,
  .arco-layout-sider,
  .arco-layout-aside,
  .arco-menu,
  .arco-menu-item,
  .arco-menu-group,
  .arco-menu-submenu,
  .arco-layout-content,
  .simple-breadcrumb,
  .page-header,
  .header-right,
  .back-button,
  .print-button {
    display: none !important;
  }

  /* Ẩn các phần không cần thiết khi in */
  .status-card,
  .payment-history-card {
    display: none !important;
  }

  /* Chỉ hiển thị nội dung chính */
  .invoice-detail-page {
    padding: 0;
    background: white;
    min-height: auto;
    margin: 0;
    width: 100%;
  }

  /* Hiển thị lại nội dung hóa đơn */
  .invoice-content {
    display: block !important;
    margin: 0;
    padding: 20px;
  }

  /* Tiêu đề biên lai */
  .invoice-content::before {
    content: 'BIÊN LAI BÁN HÀNG';
    display: block;
    text-align: center;
    font-size: 24px;
    font-weight: bold;
    margin-bottom: 30px;
    padding-bottom: 10px;
    border-bottom: 2px solid #000;
  }

  /* Thông tin đơn hàng */
  .info-card {
    margin-bottom: 20px;
    border: 1px solid #000;
    page-break-inside: avoid;
  }

  .info-card .arco-card-header {
    background: #f5f5f5;
    border-bottom: 1px solid #000;
  }

  /* Danh sách sản phẩm */
  .product-list-card {
    margin-bottom: 20px;
    border: 1px solid #000;
    page-break-inside: avoid;
  }

  .product-list-card .arco-card-header {
    background: #f5f5f5;
    border-bottom: 1px solid #000;
  }

  /* Tổng kết đơn hàng */
  .summary-card {
    margin-bottom: 0;
    border: 1px solid #000;
    page-break-inside: avoid;
  }

  .summary-card .arco-card-header {
    background: #f5f5f5;
    border-bottom: 1px solid #000;
  }

  /* Đảm bảo table hiển thị đầy đủ */
  .arco-table {
    font-size: 12px;
  }

  .arco-table th,
  .arco-table td {
    border: 1px solid #000;
    padding: 8px;
  }

  /* Ẩn pagination khi in */
  .arco-pagination {
    display: none !important;
  }

  /* Đảm bảo màu sắc hiển thị đúng */
  * {
    color: #000 !important;
    background: white !important;
  }

  .arco-tag {
    border: 1px solid #000;
  }
}
</style>
