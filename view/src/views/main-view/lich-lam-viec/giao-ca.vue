<template>
  <div class="giao-ca-container">
    <ConfirmModal ref="confirmModal" />

    <!-- Start shift modal / card when no active shift -->
    <div v-if="!activeShift" class="start-shift-card">
      <div class="card-header">
        <div class="header-icon-wrapper blue">
          <icon-play-arrow />
        </div>
        <div class="header-title">
          <h3>Bắt đầu ca mới</h3>
          <span class="subtitle">Nhập thông tin để bắt đầu ca làm việc</span>
        </div>
      </div>

      <div class="form-section">
        <div class="info-item" style="margin-bottom: 12px">
          <span class="item-label">
            <icon-user />
            Người bắt đầu ca
          </span>
          <span class="item-value">{{ userStore.name || userStore.tenNhanVien || 'N/A' }}</span>
        </div>

        <div class="info-item" style="margin-bottom: 12px">
          <span class="item-label">
            <icon-clock-circle />
            Thời gian bắt đầu
          </span>
          <span class="item-value">{{ formatDate(currentTime) }}</span>
        </div>

        <a-form-item label="Ca làm việc" class="shift-select-item">
          <a-select v-model="selectedCaId" placeholder="Chọn ca (chỉ ca admin phân)" size="large" :filterable="false">
            <template v-if="scheduledList.length">
              <a-select-option v-for="s in scheduledList" :key="'sch-' + s.id" :value="s.caLamViec?.id">
                {{ (s.caLamViec?.tenCa || s.caLamViec?.ten || 'Ca #' + s.caLamViec?.id) + ' — ' + (formatDate(s.ngayLamViec) || '') }}
              </a-select-option>
            </template>
            <template v-else>
              <a-select-option v-for="c in caLamViecList" :key="c && c.id" :value="c && c.id">
                {{ (c && (c.tenCa || c.ten || c.name)) || 'Ca #' + (c && c.id) }} (ID: {{ c && c.id }})
              </a-select-option>
            </template>
          </a-select>
          <div style="margin-top: 8px; color: #64748b; font-size: 12px">
            ID ca làm việc:
            <strong>{{ selectedCaId || '-' }}</strong>
          </div>
        </a-form-item>

        <a-form-item
          label="Số tiền mặt ban đầu (VND)"
          class="cash-input-item"
          :validate-status="validationErrors.initialCash ? 'error' : undefined"
          :help="validationErrors.initialCash || undefined"
        >
          <a-input-number
            v-model="initialCash"
            :min="0"
            :max="1000000000"
            :precision="0"
            placeholder="Nhập số tiền mặt (nếu có)"
            size="large"
            class="cash-input"
            @blur="validationErrors.initialCash = validateInitialCash(initialCash)"
            @change="validationErrors.initialCash = validateInitialCash(initialCash)"
          >
            <template #prefix>
              <icon-star />
            </template>
          </a-input-number>
        </a-form-item>

        <a-form-item label="Ghi chú đầu ca">
          <a-textarea v-model="ghiChu" placeholder="Ghi chú (ví dụ: bàn giao quỹ, lưu ý...)" rows="3" />
        </a-form-item>

        <div class="button-wrapper">
          <a-button type="primary" size="large" :loading="loading" @click="startShift" class="btn-start">
            <template #icon>
              <icon-check-circle />
            </template>
            {{ loading ? 'Đang tạo...' : 'Bắt Đầu Ca' }}
          </a-button>
        </div>
      </div>
    </div>

    <!-- Active shift card / end shift modal -->
    <div v-else class="active-shift-card">
      <div class="status-badge active">
        <icon-check-circle-fill />
        <span>Ca đang hoạt động</span>
      </div>

      <div class="shift-time">
        <icon-clock-circle />
        <span>
          Bắt đầu từ:
          <strong>{{ formatDate(activeShift.thoiGianGiaoCa) }}</strong>
        </span>
      </div>

      <div class="info-cards-grid">
        <div class="info-card shift-info-card">
          <div class="card-icon blue">
            <icon-info-circle />
          </div>
          <div class="card-content">
            <h4>Thông tin ca</h4>
            <div class="info-list">
              <div class="info-item">
                <span class="item-label">
                  <icon-user />
                  Người giao ca
                </span>
                <span class="item-value">{{ activeShift.nguoiGiao?.tenNhanVien || activeShift.nguoiGiao?.name || 'N/A' }}</span>
              </div>
              <div class="info-item">
                <span class="item-label">
                  <icon-star />
                  Tiền mặt ban đầu
                </span>
                <span class="item-value highlight">{{ formatCurrency(activeShift.tongTienBanDau) }}</span>
              </div>
              <div v-if="lastShift && lastShift.tongTienCuoiCa" class="info-item">
                <span class="item-label">
                  <icon-calendar />
                  Tiền mặt ca trước
                </span>
                <span class="item-value highlight">{{ formatCurrency(lastShift.tongTienCuoiCa) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="info-card sales-card">
          <div class="card-icon green">
            <icon-arrow-up />
          </div>
          <div class="card-content">
            <h4>Thống kê doanh thu</h4>
            <div class="sales-stats">
              <div class="sales-item">
                <div class="sales-label">
                  <icon-file />
                  Số hóa đơn
                </div>
                <div class="sales-value">{{ invoiceCount }}</div>
              </div>
              <div class="sales-item revenue">
                <div class="sales-label">
                  <icon-star />
                  Tổng doanh thu
                </div>
                <div class="sales-value">{{ formatCurrency(invoiceTotal) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <a-button type="primary" status="danger" size="large" :loading="loading" @click="openEndShiftModal" class="btn-end" block>
        <template #icon>
          <icon-close-circle />
        </template>
        {{ loading ? 'Đang xử lý...' : 'Kết Thúc Ca' }}
      </a-button>
    </div>

    <!-- End shift modal -->
    <ConfirmReceiveShift ref="confirmReceive" />
    <div style="margin-top: 12px">
      <a-button v-if="activeShift" size="small" @click="openConfirmReceive">Mở form xác nhận nhận ca (test)</a-button>
    </div>
    <a-modal v-model:visible="showEndShiftModal" title="Kết Thúc Ca Làm Việc" :width="800" :closable="!loading" @ok="finalizeEndShift">
      <div class="end-shift-form">
        <!-- Doanh thu summary -->
        <div class="form-section">
          <h4 class="section-title">Thống kê doanh thu</h4>
          <div class="summary-grid">
            <div class="summary-item">
              <span class="label">Tổng doanh thu</span>
              <span class="value highlight">{{ formatCurrency(invoiceTotal) }}</span>
            </div>
            <div class="summary-item">
              <span class="label">Tổng tiền mặt (ban đầu)</span>
              <span class="value">{{ formatCurrency(activeShift?.tongTienBanDau || 0) }}</span>
            </div>
            <div class="summary-item">
              <span class="label">Tổng tiền mặt dự kiến</span>
              <span class="value">{{ formatCurrency((activeShift?.tongTienBanDau || 0) + invoiceTotal) }}</span>
            </div>
          </div>
        </div>

        <!-- Số tiền thực đếm (input) -->
        <div class="form-section">
          <h4 class="section-title">Kiểm kê tiền mặt</h4>
          <a-form-item
            label="Số tiền thực đếm (VND)"
            :validate-status="validationErrors.actualCash ? 'error' : undefined"
            :help="validationErrors.actualCash"
          >
            <a-input-number
              v-model="actualCash"
              :min="0"
              :max="10000000000"
              :precision="0"
              placeholder="Nhập số tiền thực đếm"
              size="large"
            />
          </a-form-item>

          <div class="discrepancy-card" :class="{ positive: discrepancy > 0, negative: discrepancy < 0 }">
            <span class="label">Tiền lệch:</span>
            <span class="value">{{ formatCurrency(discrepancy) }}</span>
            <span class="status" v-if="discrepancy > 0" style="color: #10b981">Thừa</span>
            <span class="status" v-else-if="discrepancy < 0" style="color: #ef4444">Thiếu</span>
            <span class="status" v-else style="color: #6b7280">Cân bằng</span>
          </div>
        </div>

        <!-- Ghi chú kết ca -->
        <div class="form-section">
          <a-form-item label="Ghi chú kết ca">
            <a-textarea v-model="endNotes" placeholder="Ghi chú (ví dụ: tình hình quỹ, phát sinh...)" rows="3" />
          </a-form-item>
        </div>

        <!-- Người nhận ca tiếp theo (có thể để trống) -->
        <div class="form-section">
          <a-form-item
            label="Người nhận ca tiếp theo"
            :validate-status="validationErrors.nextReceiver ? 'error' : undefined"
            :help="validationErrors.nextReceiver"
          >
            <a-select
              v-model="nextReceiverId"
              placeholder="Chọn nhân viên nhận ca tiếp (hoặc bỏ qua)"
              size="large"
              :loading="loadingEmployees"
              allow-clear
            >
              <a-select-option :value="null">Chưa có người nhận</a-select-option>
              <a-select-option v-for="emp in employeeList" :key="emp.id" :value="emp.id">
                {{ emp.tenNhanVien || emp.name || emp.ten || 'NV #' + emp.id }} (ID: {{ emp.id }})
              </a-select-option>
            </a-select>
          </a-form-item>
        </div>

        <!-- Thời gian kết ca -->
        <div class="form-section">
          <div class="info-item">
            <span class="item-label">
              <icon-clock-circle />
              Thời gian kết ca
            </span>
            <span class="item-value">{{ formatDate(endTime) }}</span>
          </div>
        </div>

        <!-- Danh sách hóa đơn trong ca -->
        <div class="form-section">
          <h4 class="section-title">Danh sách hóa đơn trong ca ({{ invoiceList.length }})</h4>
          <a-table :columns="invoiceColumns" :data="invoiceList" :pagination="false" :bordered="false" size="small" style="font-size: 12px">
            <template #columns>
              <a-table-column title="ID" data-index="id" :width="60" />
              <a-table-column title="Số tiền" data-index="tongTien" :width="120">
                <template #cell="{ record }">
                  {{ formatCurrency(record.tongTienSauGiam ?? record.tongTien ?? 0) }}
                </template>
              </a-table-column>
              <a-table-column title="Thời gian" data-index="ngayTao" :width="150">
                <template #cell="{ record }">
                  {{ formatDate(record.ngayTao || record.createAt) }}
                </template>
              </a-table-column>
              <a-table-column title="Mô tả" data-index="mota" :ellipsis="true" />
            </template>
          </a-table>
        </div>
      </div>

      <template #footer>
        <a-button @click="showEndShiftModal = false" :disabled="loading">Hủy</a-button>
        <a-button type="primary" :loading="loading" @click="finalizeEndShift">Xác Nhận Kết Ca</a-button>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useUserStore } from '@/store'
import { themGiaoCa, suaGiaoCa, getGiaoCa } from '@/api/giao-ca'
import { fetchHoaDonList } from '@/api/hoa-don'
import { getCaLamViec } from '@/api/ca-lam-viec'
import { timKiemLichLamViec, suaLichLamViec } from '@/api/lich-lam-viec'
import { layDanhSachNhanVien } from '@/api/nhan-vien'
import ConfirmModal from '@/components/ConfirmModal.vue'
import ConfirmReceiveShift from '@/components/ConfirmReceiveShift.vue'
import { Message } from '@arco-design/web-vue'
import useUser from '@/hooks/user'
import { useRouter } from 'vue-router'
import {
  IconClockCircle,
  IconCalendar,
  IconUser,
  IconStar,
  IconFile,
  IconPlayArrow,
  IconCheckCircle,
  IconCheckCircleFill,
  IconInfoCircle,
  IconArrowUp,
  IconCloseCircle,
} from '@arco-design/web-vue/es/icon'

const loading = ref(false)
const activeShift = ref(null)
const lastShift = ref(null)
const initialCash = ref(0)
const userStore = useUserStore()
const invoiceCount = ref(0)
const invoiceTotal = ref(0)
const invoiceIds = ref([])
const invoiceList = ref([])
const confirmModal = ref(null)
const confirmReceive = ref(null)
const { logout } = useUser()
const router = useRouter()

// Start shift form refs
const caLamViecList = ref([])
const selectedCaId = ref(null)
const scheduledList = ref([]) // admin-assigned schedule entries for current user/day
const currentLichLamViecId = ref(null) // Track current schedule ID for updating on shift end
const ghiChu = ref('')
const currentTime = ref('')
const autoEndTimer = ref<number | null>(null)

// End shift modal refs
const showEndShiftModal = ref(false)
const actualCash = ref(0)
const endNotes = ref('')
const nextReceiverId = ref(null)
const endTime = ref('')
const employeeList = ref([])
const loadingEmployees = ref(false)

// Validation errors
const validationErrors = ref({
  initialCash: '',
  actualCash: '',
  nextReceiver: '',
})

// Computed
const discrepancy = computed(() => {
  const expected = (activeShift.value?.tongTienBanDau || 0) + invoiceTotal.value
  return actualCash.value - expected
})

const invoiceColumns = [
  { title: 'ID', dataIndex: 'id', width: 60 },
  { title: 'Số tiền', dataIndex: 'tongTien', width: 120 },
  { title: 'Thời gian', dataIndex: 'ngayTao', width: 150 },
  { title: 'Mô tả', dataIndex: 'mota', ellipsis: true },
]

// Helper functions
function toLocalDateTimeString(date) {
  const pad = (n) => String(n).padStart(2, '0')
  const Y = date.getFullYear()
  const M = pad(date.getMonth() + 1)
  const D = pad(date.getDate())
  const h = pad(date.getHours())
  const m = pad(date.getMinutes())
  const s = pad(date.getSeconds())
  return `${Y}-${M}-${D} ${h}:${m}:${s}`
}

function normalizeToLocalDateTime(value) {
  if (!value) return toLocalDateTimeString(new Date())
  if (typeof value === 'string') {
    let s = value.trim()
    if (s.endsWith('Z')) s = s.slice(0, -1)
    s = s.replace('T', ' ')
    const dot = s.indexOf('.')
    if (dot !== -1) s = s.substring(0, dot)
    if (s.length >= 19) return s.substring(0, 19)
    const d = new Date(s)
    if (!Number.isNaN(d.getTime())) return toLocalDateTimeString(d)
    return toLocalDateTimeString(new Date())
  }
  if (value instanceof Date) return toLocalDateTimeString(value)
  try {
    return toLocalDateTimeString(new Date(value))
  } catch (e) {
    return toLocalDateTimeString(new Date())
  }
}

function formatDate(d) {
  if (!d) return '-'
  try {
    let s = d
    if (typeof s === 'string' && s.length >= 19 && s[10] === ' ') {
      s = s.replace(' ', 'T')
    }
    return new Date(s).toLocaleString()
  } catch (e) {
    return d
  }
}

function formatCurrency(n) {
  if (n == null) return '-'
  return new Intl.NumberFormat('vi-VN').format(n) + ' ₫'
}

function validateInitialCash(value) {
  if (value === null || value === undefined) {
    return 'Vui lòng nhập số tiền mặt ban đầu'
  }
  const numValue = Number(value)
  if (Number.isNaN(numValue)) {
    return 'Số tiền mặt phải là số hợp lệ'
  }
  if (numValue < 0) {
    return 'Số tiền mặt không được âm'
  }
  if (numValue > 1000000000) {
    return 'Số tiền mặt không được vượt quá 1.000.000.000 VND'
  }
  return ''
}

function validateUserInfo() {
  if (!userStore.id) {
    return 'Thông tin người dùng không hợp lệ. Vui lòng đăng nhập lại.'
  }
  return ''
}

// Expose reload function
async function reloadShiftData() {
  try {
    const res = await getGiaoCa()
    const list = res.data || res || []
    const active = list.find((s) => {
      const trangThaiCa = s.trangThaiCa || s.trangThai
      return trangThaiCa === 'Đang làm' || (trangThaiCa !== 'Hoàn tất' && !s.thoiGianKetThuc)
    })
    if (active) {
      activeShift.value = active
      computeShiftSales(activeShift.value).catch((e) => console.warn(e))
    }
    const ended = list
      .filter((s) => {
        const trangThaiCa = s.trangThaiCa || s.trangThai
        return trangThaiCa === 'Hoàn tất' || s.thoiGianKetThuc
      })
      .sort((a, b) => {
        const dateA = a.thoiGianKetThuc ? new Date(a.thoiGianKetThuc) : new Date(a.thoiGianGiaoCa || 0)
        const dateB = b.thoiGianKetThuc ? new Date(b.thoiGianKetThuc) : new Date(b.thoiGianGiaoCa || 0)
        return dateB.getTime() - dateA.getTime()
      })
    if (ended.length) {
      lastShift.value = ended[0]
    }
  } catch (e) {
    console.warn('Không tải lại danh sách giao ca', e)
  }
}

defineExpose({ reloadShiftData })

// Helper: try to compute an end Date from a schedule entry or caLamViec definition.
function getScheduleEndDate(schedule) {
  if (!schedule) return null
  try {
    // 1) explicit full datetime fields
    const tryParse = (v) => {
      if (!v) return null
      let s = String(v).trim()
      // Normalize common ISO variants to local-like parse: remove trailing Z (UTC) and strip milliseconds
      if (s.endsWith('Z')) s = s.slice(0, -1)
      // Replace T with space so engines parse as local datetime when possible
      s = s.replace('T', ' ')
      const dot = s.indexOf('.')
      if (dot !== -1) s = s.substring(0, dot)
      // If length >= 19, crop to seconds
      if (s.length >= 19) s = s.substring(0, 19)
      const d = new Date(s)
      return Number.isNaN(d.getTime()) ? null : d
    }

    const possible = []
    if (schedule.thoiGianKetThuc) possible.push(tryParse(schedule.thoiGianKetThuc))
    if (schedule.thoi_gio_ket_thuc) possible.push(tryParse(schedule.thoi_gio_ket_thuc))
    if (schedule.gioKetThuc) possible.push(tryParse(schedule.gioKetThuc))
    if (schedule.ketThuc) possible.push(tryParse(schedule.ketThuc))
    if (schedule.caLamViec && schedule.caLamViec.gioKetThuc) possible.push(tryParse(schedule.caLamViec.gioKetThuc))
    if (schedule.caLamViec && schedule.caLamViec.gioKetThucFull) possible.push(tryParse(schedule.caLamViec.gioKetThucFull))

    // If schedule has a date (ngayLamViec) and time string (e.g., ca.gioKetThuc '17:00'), combine
    const datePart = schedule.ngayLamViec || schedule.ngay || schedule.ngay_lam_viec
    const timeCandidate =
      (schedule.caLamViec && (schedule.caLamViec.gioKetThuc || schedule.caLamViec.gioKetThucFull)) || schedule.gioKetThuc || schedule.ketThuc
    if (datePart && timeCandidate && String(timeCandidate).indexOf(':') !== -1) {
      const ds = String(datePart).trim().split(' ')[0]
      const ts = String(timeCandidate).trim()
      const combined = ds + ' ' + (ts.length === 5 ? ts + ':00' : ts)
      const parsed = tryParse(combined)
      if (parsed) possible.push(parsed)
    }

    // pick earliest non-null parsed date (prefer explicit thoiGianKetThuc)
    for (const p of possible) {
      if (p) return p
    }
  } catch (e) {
    // ignore
  }
  return null
}

// Helper: extract schedule ID from a shift object using multiple possible field names/structures
function extractScheduleIdFromShift(shift) {
  if (!shift) return null
  try {
    // common keys that might contain schedule reference
    const keys = [
      'lichLamViec',
      'lich_lam_viec',
      'lichLamViecId',
      'lich_lam_viec_id',
      'lichId',
      'lich_id',
      'lichlamviecId',
      'caLamViecId',
      'ca_lam_viec_id',
    ]

    for (const k of keys) {
      const v = shift[k]
      if (v == null) continue
      // nested object with id
      if (typeof v === 'object') {
        if (v.id != null && !Number.isNaN(Number(v.id))) return Number(v.id)
        // sometimes object may contain nested schedule under 'lich' or similar
        if (v.lich && v.lich.id != null && !Number.isNaN(Number(v.lich.id))) return Number(v.lich.id)
      }
      // primitive id-like value
      if (!Number.isNaN(Number(v))) return Number(v)
    }

    // Sometimes schedule id is stored under different names directly on shift
    const alt = shift.lichId || shift.lich_id || shift.lichLamViecId || shift.currentLichLamViecId || shift.current_lich_lam_viec_id
    if (alt != null && !Number.isNaN(Number(alt))) return Number(alt)
  } catch (e) {
    // ignore and return null
  }
  return null
}

function clearAutoEndTimer() {
  try {
    if (autoEndTimer.value) {
      clearTimeout(autoEndTimer.value as any)
      autoEndTimer.value = null
    }
  } catch (e) {
    // ignore
  }
}

async function autoFinalizeEndShift() {
  if (!activeShift.value) return
  if (loading.value) return
  loading.value = true
  try {
    const expected = (activeShift.value?.tongTienBanDau || 0) + invoiceTotal.value
    const payload = {
      nguoiGiaoId: userStore.id,
      nguoiGiao: {
        id: userStore.id,
        tenNhanVien: userStore.name || userStore.tenNhanVien || userStore.ten || '',
      },
      nguoiNhanId: null,
      caLamViecId:
        (activeShift.value && ((activeShift.value.caLamViec && activeShift.value.caLamViec.id) || activeShift.value.ca_lam_viec_id || activeShift.value.caLamViecId)) ||
        null,
      thoiGianGiaoCa: normalizeToLocalDateTime((activeShift.value && activeShift.value.thoiGianGiaoCa) || toLocalDateTimeString(new Date())),
      thoiGianKetThuc: normalizeToLocalDateTime(toLocalDateTimeString(new Date())),
      soTienThucTe: Number(expected),
      tienLech: 0,
      ghiChuNguoiGiao: 'Kết ca tự động khi hết thời gian làm việc',
      trangThaiCa: 'Hoàn tất',
    }

    await suaGiaoCa(activeShift.value.id, payload)

    if (currentLichLamViecId.value) {
      try {
        // backend expects a boolean `trangThai` (true = done), not a string
        await suaLichLamViec(currentLichLamViecId.value, { trangThai: true })
      } catch (e) {
        console.warn('Auto end: unable to update schedule status', e)
      }
    }

    // refresh minimal local state
    activeShift.value = null
    showEndShiftModal.value = false
    Message.success('Ca đã được kết tự động do hết thời gian làm việc')
    // optionally logout user after a small delay (reuse existing flow)
    await new Promise((r) => setTimeout(r, 800))
    try {
      await logout()
    } catch (e) {
      router.push({ name: 'login' })
    }
  } catch (err) {
    console.error('Auto finalize end shift failed', err)
    Message.error('Không thể tự động kết ca — vui lòng kết thúc ca thủ công')
  } finally {
    loading.value = false
    clearAutoEndTimer()
  }
}

function setupAutoEndForActiveShift() {
  clearAutoEndTimer()
  if (!activeShift.value) return
  // Try to find schedule entry linked to this shift
  let scheduleEntry = null
  try {
    const sid = currentLichLamViecId.value || extractScheduleIdFromShift(activeShift.value)
    if (sid && scheduledList.value && scheduledList.value.length > 0) {
      scheduleEntry = scheduledList.value.find((s) => Number(s.id) === Number(sid)) || null
    }
    // fallback: try to find schedule by ca id + date
    if (!scheduleEntry && scheduledList.value && scheduledList.value.length > 0) {
      const caId = activeShift.value.caLamViec?.id || activeShift.value.ca_lam_viec_id || activeShift.value.caLamViecId
      const dateStr = (activeShift.value.thoiGianGiaoCa || '').toString().split(' ')[0]
      scheduleEntry = scheduledList.value.find((s) => {
        try {
          const sCaId = s.caLamViec?.id || s.ca_lam_viec_id || s.caLamViecId
          const sDate = (s.ngayLamViec || '').toString().split(' ')[0]
          if (!sCaId || !caId) return false
          return Number(sCaId) === Number(caId) && (!sDate || !dateStr || sDate === dateStr)
        } catch {
          return false
        }
      })
    }
  } catch (e) {
    // ignore
  }

  const endDate = getScheduleEndDate(scheduleEntry) || getScheduleEndDate(activeShift.value) || null
  if (!endDate) return

  const now = new Date()
  if (now >= endDate) {
    // already past end time — finalize immediately
    autoFinalizeEndShift().catch((e) => console.warn('auto finalize failed', e))
    return
  }

  const ms = endDate.getTime() - now.getTime()
  // schedule timer
  try {
    console.log('Setup auto-end timer — endDate:', endDate, 'now:', now, 'msUntilEnd:', ms)
    autoEndTimer.value = setTimeout(() => {
      autoFinalizeEndShift().catch((e) => console.error('Auto finalize error', e))
    }, ms) as unknown as number
  } catch (e) {
    console.warn('Failed to set auto end timer', e)
  }
}

// watch activeShift and scheduledList so we can setup/clear auto-end appropriately
watch(activeShift, (n) => {
  try {
    setupAutoEndForActiveShift()
  } catch (e) {
    console.warn('watch activeShift setup auto end error', e)
  }
})

watch(scheduledList, () => {
  try {
    setupAutoEndForActiveShift()
  } catch (e) {
    console.warn('watch scheduledList setup auto end error', e)
  }
})

onUnmounted(() => {
  clearAutoEndTimer()
})

async function computeShiftSales(shift) {
  try {
    const all = await fetchHoaDonList()
    const list = all || []

    const start = shift.thoiGianGiaoCa ? new Date(shift.thoiGianGiaoCa) : null
    const end = shift.thoiGianKetThuc ? new Date(shift.thoiGianKetThuc) : new Date()

    const empId = (shift.nguoiNhan && shift.nguoiNhan.id) || shift.nguoiNhanId || shift.nguoiNhan?.id || userStore.id

    const invoices = list.filter((inv) => {
      try {
        const created = inv.ngayTao ? new Date(inv.ngayTao) : inv.createAt ? new Date(inv.createAt) : null
        if (!created) return false
        if (inv.createBy && empId && inv.createBy !== empId) return false
        if (inv.idNhanVien && empId && inv.idNhanVien !== empId) return false
        if (start && end) {
          return created >= start && created <= end
        }
        return true
      } catch (e) {
        return false
      }
    })

    invoiceIds.value = invoices.map((i) => i.id)
    invoiceList.value = invoices
    invoiceCount.value = invoices.length
    invoiceTotal.value = invoices.reduce((acc, cur) => {
      const v = cur.tongTienSauGiam ?? cur.tongTien ?? cur.soTienDaThanhToan ?? 0
      return acc + Number(v || 0)
    }, 0)
  } catch (e) {
    console.warn('Không tính được doanh thu ca', e)
  }
}

// Init on mount
onMounted(async () => {
  currentTime.value = toLocalDateTimeString(new Date())
  endTime.value = toLocalDateTimeString(new Date())

  try {
    const res = await getGiaoCa()
    const list = res.data || res || []
    const active = list.find((s) => {
      const trangThaiCa = s.trangThaiCa || s.trangThai
      return trangThaiCa === 'Đang làm' || (trangThaiCa !== 'Hoàn tất' && !s.thoiGianKetThuc)
    })
    if (active) {
      activeShift.value = active
      computeShiftSales(activeShift.value).catch((e) => console.warn(e))
    }
    const ended = list
      .filter((s) => {
        const trangThaiCa = s.trangThaiCa || s.trangThai
        return trangThaiCa === 'Hoàn tất' || s.thoiGianKetThuc
      })
      .sort((a, b) => {
        const dateA = a.thoiGianKetThuc ? new Date(a.thoiGianKetThuc) : new Date(a.thoiGianGiaoCa || 0)
        const dateB = b.thoiGianKetThuc ? new Date(b.thoiGianKetThuc) : new Date(b.thoiGianGiaoCa || 0)
        return dateB.getTime() - dateA.getTime()
      })
    if (ended.length) {
      lastShift.value = ended[0]
    }

    // Nếu có giao ca đang chờ xác nhận cho user hiện tại thì mở modal xác nhận nhận ca tự động
    try {
      const pending = list.find((s) => {
        const isPending = s.trangThaiXacNhan === 'Chưa xác nhận' || s.trangThaiXacNhan === 'Chờ xác nhận'
        const assignedId = (s.nguoiNhan && s.nguoiNhan.id) || s.nguoiNhanId
        return isPending && assignedId && Number(assignedId) === Number(userStore.id)
      })
      if (pending) {
        // Tính doanh thu + danh sách hóa đơn cho ca đó rồi mở modal xác nhận
        await computeShiftSales(pending)
        // set activeShift to the pending one so UI reflects it
        activeShift.value = pending
        // mở modal xác nhận
        setTimeout(() => {
          openConfirmReceive().catch((err) => console.warn('openConfirmReceive failed', err))
        }, 200)
      }
    } catch (errPending) {
      console.warn('Không kiểm tra được giao ca chờ xác nhận', errPending)
    }
  } catch (e) {
    console.warn('Không lấy được danh sách giao ca', e)
  }

  // Prefer admin-assigned schedule for current user/day; otherwise load all shift types
  try {
    const today = currentTime.value ? currentTime.value.split(' ')[0] : new Date().toISOString().slice(0, 10)
    const scheduleRes = await timKiemLichLamViec({ ngayLamViec: today, nhanVienId: userStore.id })
    const scheduleData = scheduleRes.data || scheduleRes || []
    const schedules = Array.isArray(scheduleData) ? scheduleData : []

    if (schedules.length > 0) {
      // keep the raw schedule entries for display (admin-assigned slots)
      const forUser = schedules.filter((s) => s && s.nhanVien && Number(s.nhanVien.id) === Number(userStore.id))
      scheduledList.value = Array.isArray(forUser) ? forUser : []
      // map to unique caLamViec objects assigned to this user (for fallback uses)
      const mapped = scheduledList.value
        .map((s) => s && s.caLamViec)
        .filter((c, idx, arr) => c && arr.findIndex((x) => x && x.id === c.id) === idx)
      caLamViecList.value = mapped.filter(Boolean)
      console.log('schedules (from API):', schedules)
      console.log('scheduledList (user):', scheduledList.value)
      console.log('mapped caLamViecList:', caLamViecList.value)
      if (lastShift.value) {
        const lastId = lastShift.value.caLamViec?.id || lastShift.value.ca_lam_viec_id || lastShift.value.caLamViecId
        if (lastId) {
          const found = caLamViecList.value.find((c) => c && c.id === lastId)
          if (found) selectedCaId.value = found.id
        }
      }
      if (!selectedCaId.value && scheduledList.value.length > 0) selectedCaId.value = scheduledList.value[0].caLamViec?.id
      console.log('selectedCaId initial (from schedule):', selectedCaId.value)
    } else {
      const caRes = await getCaLamViec()
      const responseData = caRes.data?.data || caRes.data || caRes
      const list = Array.isArray(responseData) ? responseData : []
      caLamViecList.value = list
      if (lastShift.value) {
        const lastId = lastShift.value.caLamViec?.id || lastShift.value.ca_lam_viec_id || lastShift.value.caLamViecId
        if (lastId) {
          const found = caLamViecList.value.find((c) => c && c.id === lastId)
          if (found) selectedCaId.value = found.id
        }
      }
      if (!selectedCaId.value && caLamViecList.value.length > 0) selectedCaId.value = caLamViecList.value[0].id
      console.log('caLamViecList (fallback):', caLamViecList.value)
      console.log('selectedCaId initial (fallback):', selectedCaId.value)
    }
  } catch (err) {
    console.warn('Không lấy danh sách ca làm việc hoặc lịch', err)
  }
})

async function openConfirmReceive() {
  try {
    if (confirmReceive.value && typeof confirmReceive.value.show === 'function') {
      await confirmReceive.value.show(activeShift.value, invoiceList.value, { endTime: endTime.value })
    }
  } catch (e) {
    console.error('Open confirm receive error', e)
  }
}

async function startShift() {
  validationErrors.value = { initialCash: '', actualCash: '', nextReceiver: '' }

  const cashError = validateInitialCash(initialCash.value)
  if (cashError) {
    validationErrors.value.initialCash = cashError
    Message.error(cashError)
    return
  }

  const userError = validateUserInfo()
  if (userError) {
    Message.error(userError)
    return
  }

  try {
    const res = await getGiaoCa()
    const list = Array.isArray(res.data) ? res.data : Array.isArray(res) ? res : []
    const userId = userStore.id
    const existingActive = list.find((s) => {
      const assignedId = (s.nguoiNhan && s.nguoiNhan.id) || s.nguoiNhanId
      const trangThaiCa = s.trangThaiCa || s.trangThai
      const isActive = trangThaiCa === 'Đang làm' || (trangThaiCa !== 'Hoàn tất' && !s.thoiGianKetThuc && s.thoiGianGiaoCa)
      return assignedId === userId && isActive
    })

    if (existingActive) {
      Message.warning('Bạn đã có ca làm việc đang hoạt động. Vui lòng kết thúc ca hiện tại trước khi bắt đầu ca mới.')
      activeShift.value = existingActive
      return
    }
  } catch (e) {
    console.warn('Không thể kiểm tra ca hiện tại', e)
    Message.error('Không thể kiểm tra trạng thái ca làm việc. Vui lòng thử lại.')
    return
  }

  const ok = await confirmModal.value.showConfirm(
    'Bắt Đầu Ca Làm Việc',
    'Bạn có chắc muốn bắt đầu ca làm việc với số tiền mặt ban đầu là ' + formatCurrency(initialCash.value) + ' không?',
    'Xác Nhận',
    'Hủy'
  )
  if (!ok) return

  loading.value = true
  try {
    const now = new Date()
    const nguoiGiaoId = (lastShift.value && lastShift.value.nguoiGiao && lastShift.value.nguoiGiao.id) || userStore.id
    const nguoiNhanId = userStore.id

    if (!nguoiGiaoId || !nguoiNhanId) {
      Message.error('Thông tin nhân viên không hợp lệ. Vui lòng đăng nhập lại.')
      loading.value = false
      return
    }

    if (Number.isNaN(Number(nguoiGiaoId)) || Number.isNaN(Number(nguoiNhanId))) {
      Message.error('ID nhân viên không hợp lệ.')
      loading.value = false
      return
    }

    let caLamViecId =
      (selectedCaId.value && Number(selectedCaId.value)) ||
      (lastShift.value && (lastShift.value.caLamViec?.id || lastShift.value.ca_lam_viec_id))

    if (!caLamViecId) {
      try {
        const caLamViecRes = await getCaLamViec()
        const responseData = caLamViecRes.data?.data || caLamViecRes.data || caLamViecRes
        const caLamViecListTemp = Array.isArray(responseData) ? responseData : []
        if (caLamViecListTemp.length > 0) {
          caLamViecId = caLamViecListTemp[0].id
          console.log('Using ca lam viec ID:', caLamViecId)
        } else {
          Message.error('Không tìm thấy ca làm việc nào. Vui lòng tạo ca làm việc trước.')
          loading.value = false
          return
        }
      } catch (err) {
        console.error('Lỗi khi lấy danh sách ca làm việc:', err)
        Message.error('Không thể lấy danh sách ca làm việc. Vui lòng thử lại.')
        loading.value = false
        return
      }
    }

    if (!caLamViecId || Number.isNaN(Number(caLamViecId))) {
      Message.error('Ca làm việc không hợp lệ. Vui lòng tạo ca làm việc trước.')
      loading.value = false
      return
    }

    // Find the schedule ID from scheduledList if available
    let scheduleId = null
    if (scheduledList.value.length > 0) {
      const selectedSchedule = scheduledList.value.find((s) => s.caLamViec?.id === Number(caLamViecId))
      if (selectedSchedule) {
        scheduleId = selectedSchedule.id
      }
    }
    currentLichLamViecId.value = scheduleId

    const cashValue = Number(initialCash.value) || 0
    const cashValidationError = validateInitialCash(cashValue)
    if (cashValidationError) {
      Message.error(cashValidationError)
      loading.value = false
      return
    }

    const payload = {
      nguoiGiaoId: Number(nguoiGiaoId),
      nguoiNhanId: Number(nguoiNhanId),
      caLamViecId: Number(caLamViecId),
      ghiChu: ghiChu.value || undefined,
      thoiGianGiaoCa: toLocalDateTimeString(now),
      tongTienBanDau: cashValue,
    }

    if (!payload.thoiGianGiaoCa || payload.thoiGianGiaoCa.length < 19) {
      Message.error('Thời gian giao ca không hợp lệ.')
      loading.value = false
      return
    }

    console.log('Creating shift with payload:', payload)
    const res = await themGiaoCa(payload)
    activeShift.value = res.data || res
    Message.success('Bắt đầu ca thành công')
  } catch (err) {
    console.error('Lỗi khi bắt đầu ca', err)
    let errorMsg = 'Bắt đầu ca thất bại'
    try {
      if (err && typeof err === 'object') {
        // @ts-ignore
        errorMsg = err?.response?.data?.message || err?.message || errorMsg
      }
    } catch {
      // ignore
    }
    Message.error(errorMsg)
  } finally {
    loading.value = false
  }
}

async function openEndShiftModal() {
  if (!activeShift.value) {
    Message.error('Không tìm thấy ca làm việc đang hoạt động.')
    return
  }

  // Fetch employee list for next receiver dropdown
  loadingEmployees.value = true
  try {
    const res = await layDanhSachNhanVien()
    console.log('Employee list response:', res)
    const data = res.data || res || []
    const employees = Array.isArray(data) ? data : Array.isArray(data?.data) ? data.data : []
    console.log('Processed employee list:', employees)
    employeeList.value = employees.map((emp: any) => ({
      id: emp.id,
      tenNhanVien: emp.tenNhanVien || emp.ten || emp.name || '',
      name: emp.tenNhanVien || emp.ten || emp.name || '',
      ten: emp.tenNhanVien || emp.ten || emp.name || '',
    }))
    console.log('Final employee list:', employeeList.value)
  } catch (err) {
    console.warn('Không lấy danh sách nhân viên', err)
    employeeList.value = []
  } finally {
    loadingEmployees.value = false
  }

  // Reset form
  actualCash.value = 0
  endNotes.value = ''
  nextReceiverId.value = null
  endTime.value = toLocalDateTimeString(new Date())
  validationErrors.value = { initialCash: '', actualCash: '', nextReceiver: '' }

  showEndShiftModal.value = true
}

async function finalizeEndShift() {
  validationErrors.value = { initialCash: '', actualCash: '', nextReceiver: '' }

  if (actualCash.value === null || actualCash.value === undefined) {
    validationErrors.value.actualCash = 'Vui lòng nhập số tiền thực đếm'
    Message.error('Vui lòng nhập số tiền thực đếm')
    return
  }

  // Người nhận ca tiếp có thể để trống — cho phép kết ca khi không có người nhận

  const ok = await confirmModal.value.showConfirm(
    'Xác Nhận Kết Ca',
    `Bạn có chắc muốn kết thúc ca với tiền lệch: ${formatCurrency(discrepancy.value)} không?`,
    'Xác Nhận',
    'Hủy'
  )
  if (!ok) return

  loading.value = true
  try {
    const payload = {
      nguoiGiaoId: userStore.id,
      nguoiGiao: {
        id: userStore.id,
        tenNhanVien: userStore.name || userStore.tenNhanVien || userStore.ten || '',
      },
      nguoiNhanId: nextReceiverId.value == null ? null : Number(nextReceiverId.value),
      caLamViecId:
        (activeShift.value &&
          ((activeShift.value.caLamViec && activeShift.value.caLamViec.id) ||
            activeShift.value.ca_lam_viec_id ||
            activeShift.value.caLamViecId)) ||
        null,
      thoiGianGiaoCa: normalizeToLocalDateTime(
        (activeShift.value && activeShift.value.thoiGianGiaoCa) || toLocalDateTimeString(new Date())
      ),
      thoiGianKetThuc: normalizeToLocalDateTime(endTime.value),
      soTienThucTe: Number(actualCash.value),
      tienLech: discrepancy.value,
      ghiChuNguoiGiao: endNotes.value || undefined,
      trangThaiCa: 'Hoàn tất',
    }

    if (!payload.nguoiGiaoId || Number.isNaN(Number(payload.nguoiGiaoId))) {
      Message.error('ID người giao ca không hợp lệ.')
      loading.value = false
      return
    }
    // nguoiNhanId can be null if no next receiver is assigned

    if (!payload.thoiGianGiaoCa || payload.thoiGianGiaoCa.length < 19) {
      Message.error('Thời gian giao ca không hợp lệ.')
      loading.value = false
      return
    }

    if (!payload.trangThaiCa) {
      Message.error('Trạng thái ca không hợp lệ.')
      loading.value = false
      return
    }

    console.log('End shift payload:', payload)
    await suaGiaoCa(activeShift.value.id, payload)

    // Update schedule status to "Đã làm" if schedule exists
    // Try to ensure we have a schedule ID to update. If not present, attempt to find one
    // by matching ca/date in the loaded `scheduledList` or by calling the search API.
    if (!currentLichLamViecId.value) {
      try {
        // derive date string from thoiGianGiaoCa (YYYY-MM-DD)
        const dateStr = (payload.thoiGianGiaoCa || '').toString().split(' ')[0]
        const caIdCandidate = payload.caLamViecId || (activeShift.value && ((activeShift.value.caLamViec && activeShift.value.caLamViec.id) || activeShift.value.ca_lam_viec_id || activeShift.value.caLamViecId))

        if (scheduledList.value && scheduledList.value.length > 0 && caIdCandidate) {
          const found = scheduledList.value.find((s) => {
            try {
              const sCaId = s.caLamViec?.id || s.ca_lam_viec_id || s.caLamViecId
              const sDate = (s.ngayLamViec || '').toString().split(' ')[0]
              return sCaId && Number(sCaId) === Number(caIdCandidate) && sDate === dateStr
            } catch {
              return false
            }
          })
          if (found && found.id) currentLichLamViecId.value = found.id
        }

        // fallback: call search API to find schedules for that date + user
        if (!currentLichLamViecId.value && dateStr) {
          try {
            const searchRes = await timKiemLichLamViec({ ngayLamViec: dateStr, nhanVienId: userStore.id })
            const searchData = searchRes.data || searchRes || []
            const searchList = Array.isArray(searchData) ? searchData : []
            const found2 = searchList.find((s) => {
              try {
                const sCaId = s.caLamViec?.id || s.ca_lam_viec_id || s.caLamViecId
                return sCaId && Number(sCaId) === Number(caIdCandidate)
              } catch {
                return false
              }
            })
            if (found2 && found2.id) currentLichLamViecId.value = found2.id
          } catch (e) {
            console.warn('Fallback schedule search failed', e)
          }
        }
      } catch (e) {
        console.warn('Error trying to resolve schedule id before update', e)
      }
    }

    if (currentLichLamViecId.value) {
      try {
        // backend expects a boolean `trangThai` (true = done)
        await suaLichLamViec(currentLichLamViecId.value, { trangThai: true })
        console.log('Updated schedule status to "Đã làm" for schedule ID:', currentLichLamViecId.value)

        // Refresh scheduledList for today so UI reflects change
        try {
          const today = currentTime.value ? currentTime.value.split(' ')[0] : new Date().toISOString().slice(0, 10)
          const refreshed = await timKiemLichLamViec({ ngayLamViec: today, nhanVienId: userStore.id })
          const refreshedData = refreshed.data || refreshed || []
          const schedules = Array.isArray(refreshedData) ? refreshedData : []
          const forUser = schedules.filter((s) => s && s.nhanVien && Number(s.nhanVien.id) === Number(userStore.id))
          scheduledList.value = Array.isArray(forUser) ? forUser : []
        } catch (e) {
          // ignore refresh errors
        }
      } catch (scheduleError) {
        console.warn('Không cập nhật được trạng thái lịch làm việc:', scheduleError)
        // Continue even if schedule update fails
      }
    }

    // Refresh data
    try {
      const res = await getGiaoCa()
      const list = res.data || res || []
      const ended = list
        .filter((s) => {
          const trangThaiCa = s.trangThaiCa || s.trangThai
          return trangThaiCa === 'Hoàn tất' || s.thoiGianKetThuc
        })
        .sort((a, b) => {
          const dateA = a.thoiGianKetThuc ? new Date(a.thoiGianKetThuc) : new Date(a.thoiGianGiaoCa || 0)
          const dateB = b.thoiGianKetThuc ? new Date(b.thoiGianKetThuc) : new Date(b.thoiGianGiaoCa || 0)
          return dateB.getTime() - dateA.getTime()
        })
      if (ended.length) {
        lastShift.value = ended[0]
      }
    } catch (e) {
      console.warn('Không tải lại danh sách giao ca sau khi kết thúc', e)
    }

    activeShift.value = null
    showEndShiftModal.value = false
    Message.success('Kết thúc ca thành công. Đang đăng xuất...')

    await new Promise((resolve) => setTimeout(resolve, 1000))

    try {
      await logout()
    } catch (e) {
      console.error('Logout after endShift failed', e)
      router.push({ name: 'login' })
    }
  } catch (err) {
    console.error('Lỗi khi kết thúc ca', err)
    let errorMsg = 'Kết thúc ca thất bại'
    try {
      if (err && typeof err === 'object') {
        // @ts-ignore
        errorMsg = err?.response?.data?.message || err?.message || errorMsg
      }
    } catch {
      // ignore
    }
    Message.error(errorMsg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.giao-ca-container {
  padding: 24px;
  background: #f8fafc;
  min-height: calc(100vh - 64px);
  max-width: 1200px;
  margin: 0 auto;
}

.previous-shift-card,
.start-shift-card,
.active-shift-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 28px;
  margin-bottom: 24px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f1f5f9;
}

.header-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.header-icon-wrapper.blue {
  background: #dbeafe;
  color: #2563eb;
}

.header-title h3 {
  margin: 0 0 4px 0;
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
}

.header-title .subtitle {
  font-size: 13px;
  color: #64748b;
}

.form-section {
  margin-bottom: 20px;
}

.section-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.summary-item {
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.summary-item .label {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.summary-item .value {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

.summary-item .value.highlight {
  color: #2563eb;
}

.discrepancy-card {
  padding: 16px;
  background: #f8fafc;
  border: 2px solid #cbd5e1;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 16px;
  font-weight: 600;
  margin: 12px 0;
}

.discrepancy-card.positive {
  border-color: #a7f3d0;
  background: #ecfdf5;
}

.discrepancy-card.negative {
  border-color: #fca5a5;
  background: #fef2f2;
}

.discrepancy-card .label {
  color: #64748b;
  font-size: 14px;
}

.discrepancy-card .value {
  font-size: 20px;
  color: #1e293b;
  flex: 1;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  margin-bottom: 12px;
}

.item-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.item-value {
  font-size: 14px;
  color: #1e293b;
  font-weight: 600;
}

.item-value.highlight {
  color: #2563eb;
  font-size: 15px;
}

.info-cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.info-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.2s;
}

.info-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #cbd5e1;
}

.shift-info-card {
  border-left: 4px solid #3b82f6;
}

.sales-card {
  border-left: 4px solid #10b981;
}

.card-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  margin-bottom: 16px;
}

.card-icon.blue {
  background: #dbeafe;
  color: #2563eb;
}

.card-icon.green {
  background: #d1fae5;
  color: #059669;
}

.card-content h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sales-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sales-item {
  padding: 16px;
  background: #f8fafc;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}

.sales-item.revenue {
  background: #ecfdf5;
  border-color: #a7f3d0;
}

.sales-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
  margin-bottom: 8px;
}

.sales-value {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

.sales-item.revenue .sales-value {
  color: #059669;
  font-size: 24px;
}

.shift-time {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 8px;
  margin-bottom: 24px;
  font-size: 14px;
  color: #475569;
}

.shift-time strong {
  color: #1e293b;
  font-weight: 600;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 20px;
}

.status-badge.active {
  background: #d1fae5;
  color: #059669;
}

.btn-start,
.btn-end {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
}

.button-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}

.shift-select-item :deep(.arco-select) {
  width: 100%;
}

.cash-input-item :deep(.arco-form-item-label) {
  font-weight: 600;
  color: #374151;
  font-size: 14px;
  margin-bottom: 8px;
}

.cash-input-item :deep(.arco-input-number) {
  width: 100%;
}

.end-shift-form {
  padding: 16px 0;
  max-height: 70vh;
  overflow-y: auto;
}

@media (max-width: 768px) {
  .giao-ca-container {
    padding: 16px;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .info-cards-grid {
    grid-template-columns: 1fr;
  }

  .discrepancy-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .discrepancy-card .value {
    width: 100%;
    text-align: right;
  }
}
</style>
