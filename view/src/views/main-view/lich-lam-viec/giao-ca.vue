<template>
  <div class="giao-ca-container">
    <ConfirmModal ref="confirmModal" />

    <div v-if="lastShift" class="previous-shift-card">
      <div class="card-header">
        <div class="header-icon-wrapper amber">
          <icon-calendar />
        </div>
        <div class="header-title">
          <h3>Ca trước (Bàn giao)</h3>
          <span class="subtitle">Thông tin từ ca làm việc trước</span>
        </div>
      </div>
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-icon">
            <icon-user />
          </div>
          <div class="stat-content">
            <span class="stat-label">Người giao ca</span>
            <span class="stat-value">{{ lastShift.nguoiGiao?.tenNhanVien || lastShift.nguoiGiao?.name || 'N/A' }}</span>
          </div>
        </div>
        <div class="stat-item highlight">
          <div class="stat-icon">
            <icon-star />
          </div>
          <div class="stat-content">
            <span class="stat-label">Tiền mặt cuối ca</span>
            <span class="stat-value">{{ formatCurrency(lastShift.tongTienCuoiCa) }}</span>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon">
            <icon-file />
          </div>
          <div class="stat-content">
            <span class="stat-label">Đơn hàng chờ xử lý</span>
            <span class="stat-value">{{ lastShift.soDonChoXuLy || 0 }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="!activeShift" class="start-shift-card">
      <div class="card-header">
        <div class="header-icon-wrapper blue">
          <icon-play-arrow />
        </div>
        <div class="header-title">
          <h3>Bắt đầu ca mới</h3>
          <span class="subtitle">Nhập số tiền mặt ban đầu để bắt đầu ca làm việc</span>
        </div>
      </div>

      <div class="form-section">
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
            placeholder="Nhập số tiền mặt" 
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

      <a-button type="primary" status="danger" size="large" :loading="loading" @click="endShift" class="btn-end" block>
        <template #icon>
          <icon-close-circle />
        </template>
        {{ loading ? 'Đang xử lý...' : 'Kết Thúc Ca' }}
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store'
import { themGiaoCa, suaGiaoCa, getGiaoCa } from '@/api/giao-ca'
import { fetchHoaDonList } from '@/api/hoa-don'
import { getCaLamViec } from '@/api/ca-lam-viec'
import ConfirmModal from '@/components/ConfirmModal.vue'
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
const confirmModal = ref(null)
const { logout } = useUser()
const router = useRouter()

// Validation errors
const validationErrors = ref({
  initialCash: '',
})

// Expose reload function to parent/navbar
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
    const ended = list.filter((s) => {
      const trangThaiCa = s.trangThaiCa || s.trangThai
      return trangThaiCa === 'Hoàn tất' || s.thoiGianKetThuc
    }).sort((a, b) => {
      const dateA = a.thoiGianKetThuc ? new Date(a.thoiGianKetThuc) : new Date(a.thoiGianGiaoCa || 0)
      const dateB = b.thoiGianKetThuc ? new Date(b.thoiGianKetThuc) : new Date(b.thoiGianGiaoCa || 0)
      return dateB.getTime() - dateA.getTime()
    })
    if (ended.length) {
      lastShift.value = ended[0]
      if (lastShift.value.tongTienCuoiCa != null) initialCash.value = Number(lastShift.value.tongTienCuoiCa)
    }
  } catch (e) {
    console.warn('Không tải lại danh sách giao ca', e)
  }
}

defineExpose({ reloadShiftData })

function formatDate(d) {
  if (!d) return '-'
  try {
    // Accept both 'T' and space as separator between date and time
    let s = d
    if (typeof s === 'string' && s.length >= 19 && s[10] === ' ') {
      s = s.replace(' ', 'T')
    }
    return new Date(s).toLocaleString()
  } catch (e) {
    return d
  }
}

async function computeShiftSales(shift) {
  try {
    const all = await fetchHoaDonList()
    const list = all || []

    // Determine shift start and end
    const start = shift.thoiGianGiaoCa ? new Date(shift.thoiGianGiaoCa) : null
    const end = shift.thoiGianKetThuc ? new Date(shift.thoiGianKetThuc) : new Date()

    // Determine employee id who made sales during shift: prefer nguoiNhan or idNhanVien
    const empId = (shift.nguoiNhan && shift.nguoiNhan.id) || shift.nguoiNhanId || shift.nguoiNhan?.id || userStore.id

    const invoices = list.filter((inv) => {
      try {
        const created = inv.ngayTao ? new Date(inv.ngayTao) : inv.createAt ? new Date(inv.createAt) : null
        if (!created) return false
        // check employee
        if (inv.createBy && empId && inv.createBy !== empId) return false
        if (inv.idNhanVien && empId && inv.idNhanVien !== empId) return false
        // check time window
        if (start && end) {
          return created >= start && created <= end
        }
        return true
      } catch (e) {
        return false
      }
    })

    invoiceIds.value = invoices.map((i) => i.id)
    invoiceCount.value = invoices.length
    // sum payments: prefer tongTienSauGiam then tongTien then soTienDaThanhToan
    invoiceTotal.value = invoices.reduce((acc, cur) => {
      const v = cur.tongTienSauGiam ?? cur.tongTien ?? cur.soTienDaThanhToan ?? 0
      return acc + Number(v || 0)
    }, 0)
  } catch (e) {
    // ignore
    // eslint-disable-next-line no-console
    console.warn('Không tính được doanh thu ca', e)
  }
}

function toLocalDateTimeString(date) {
  const pad = (n) => String(n).padStart(2, '0')
  const Y = date.getFullYear()
  const M = pad(date.getMonth() + 1)
  const D = pad(date.getDate())
  const h = pad(date.getHours())
  const m = pad(date.getMinutes())
  const s = pad(date.getSeconds())
  // Use space separator to match backend LocalDateTime parsing ("yyyy-MM-dd HH:mm:ss")
  return `${Y}-${M}-${D} ${h}:${m}:${s}`
}

function normalizeToLocalDateTime(value) {
  if (!value) return toLocalDateTimeString(new Date())
  if (typeof value === 'string') {
    let s = value.trim()
    // remove timezone Z
    if (s.endsWith('Z')) s = s.slice(0, -1)
    // convert 'T' separator to space
    s = s.replace('T', ' ')
    // remove milliseconds if present
    const dot = s.indexOf('.')
    if (dot !== -1) s = s.substring(0, dot)
    // ensure length at least 19 -> 'YYYY-MM-DD HH:mm:ss'
    if (s.length >= 19) return s.substring(0, 19)
    // fallback: try to parse and format
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

function formatCurrency(n) {
  if (n == null) return '-'
  return new Intl.NumberFormat('vi-VN').format(n) + ' ₫'
}

// Validation functions
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

function validateShiftData(shift) {
  if (!shift) {
    return 'Không tìm thấy thông tin ca làm việc'
  }
  if (!shift.id) {
    return 'ID ca làm việc không hợp lệ'
  }
  return ''
}

function clearValidationErrors() {
  validationErrors.value = {
    initialCash: '',
  }
}

async function startShift() {
  // Clear previous validation errors
  clearValidationErrors()
  
  // Validate initial cash
  const cashError = validateInitialCash(initialCash.value)
  if (cashError) {
    validationErrors.value.initialCash = cashError
    Message.error(cashError)
    return
  }
  
  // Validate user info
  const userError = validateUserInfo()
  if (userError) {
    Message.error(userError)
    return
  }
  
  // Check if user already has an active shift
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
    // For first shift or self-started shift, nguoiGiaoId should be the same as nguoiNhanId
    const nguoiGiaoId = (lastShift.value && lastShift.value.nguoiGiao && lastShift.value.nguoiGiao.id) || userStore.id
    const nguoiNhanId = userStore.id
    
    // Validate employee IDs
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
    
    // Get caLamViecId - try from lastShift first, then fetch from API
    let caLamViecId = lastShift.value && (lastShift.value.caLamViec?.id || lastShift.value.ca_lam_viec_id)
    
    if (!caLamViecId) {
      // Fetch available ca lam viec from API
      try {
        const caLamViecRes = await getCaLamViec()
        const responseData = caLamViecRes.data?.data || caLamViecRes.data || caLamViecRes
        const caLamViecList = Array.isArray(responseData) ? responseData : []
        if (caLamViecList.length > 0) {
          caLamViecId = caLamViecList[0].id
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
    
    // Validate caLamViecId
    if (!caLamViecId || Number.isNaN(Number(caLamViecId))) {
      Message.error('Ca làm việc không hợp lệ. Vui lòng tạo ca làm việc trước.')
      loading.value = false
      return
    }
    
    // Validate initial cash value
    const cashValue = Number(initialCash.value) || 0
    const cashValidationError = validateInitialCash(cashValue)
    if (cashValidationError) {
      Message.error(cashValidationError)
      loading.value = false
      return
    }
    
    // Ensure IDs are numbers
    const payload = {
      nguoiGiaoId: Number(nguoiGiaoId),
      nguoiNhanId: Number(nguoiNhanId),
      caLamViecId: Number(caLamViecId),
      thoiGianGiaoCa: toLocalDateTimeString(now),
      tongTienBanDau: cashValue,
    }
    
    // Validate payload before sending
    if (!payload.thoiGianGiaoCa || payload.thoiGianGiaoCa.length < 19) {
      Message.error('Thời gian giao ca không hợp lệ.')
      loading.value = false
      return
    }
    
    // Debug log
    console.log('Creating shift with payload:', payload)
    const res = await themGiaoCa(payload)
    activeShift.value = res.data || res
    Message.success('Bắt đầu ca thành công')
  } catch (err) {
    // eslint-disable-next-line no-console
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

// Load shifts on mount to find active or last shift
onMounted(async () => {
  try {
    const res = await getGiaoCa()
    const list = res.data || res || []
    // find active shift: trangThaiCa === 'Đang làm' and not 'Hoàn tất'
    const active = list.find((s) => {
      const trangThaiCa = s.trangThaiCa || s.trangThai
      return trangThaiCa === 'Đang làm' || (trangThaiCa !== 'Hoàn tất' && !s.thoiGianKetThuc)
    })
    if (active) {
      activeShift.value = active
      // compute sales for active shift
      computeShiftSales(activeShift.value).catch((e) => console.warn(e))
    }
    // find last (most recent) ended shift - check trangThaiCa === 'Hoàn tất' or has thoiGianKetThuc
    const ended = list.filter((s) => {
      const trangThaiCa = s.trangThaiCa || s.trangThai
      return trangThaiCa === 'Hoàn tất' || s.thoiGianKetThuc
    }).sort((a, b) => {
      // Sort by thoiGianKetThuc if available, otherwise by thoiGianGiaoCa
      const dateA = a.thoiGianKetThuc ? new Date(a.thoiGianKetThuc) : new Date(a.thoiGianGiaoCa || 0)
      const dateB = b.thoiGianKetThuc ? new Date(b.thoiGianKetThuc) : new Date(b.thoiGianGiaoCa || 0)
      return dateB.getTime() - dateA.getTime()
    })
    if (ended.length) {
      lastShift.value = ended[0]
      // prefill initial cash from previous shift's tongTienCuoiCa if available
      if (lastShift.value.tongTienCuoiCa != null) initialCash.value = Number(lastShift.value.tongTienCuoiCa)
    }
  } catch (e) {
    // ignore errors for now
    // eslint-disable-next-line no-console
    console.warn('Không lấy được danh sách giao ca', e)
  }
})

async function endShift() {
  // Validate active shift exists
  if (!activeShift.value) {
    Message.error('Không tìm thấy ca làm việc đang hoạt động.')
    return
  }
  
  const shiftError = validateShiftData(activeShift.value)
  if (shiftError) {
    Message.error(shiftError)
    return
  }
  
  // Validate user info
  const userError = validateUserInfo()
  if (userError) {
    Message.error(userError)
    return
  }
  
  const ok = await confirmModal.value.showConfirm('Kết Thúc Ca Làm Việc', 'Bạn có chắc muốn kết thúc ca hiện tại không?', 'Xác Nhận', 'Hủy')
  if (!ok) return
  
  loading.value = true
  try {
    const payload = {
      // backend expects nguoiGiaoId (id of the person handing over)
      nguoiGiaoId: userStore.id,
      nguoiGiao: {
        id: userStore.id,
        tenNhanVien: userStore.name || userStore.tenNhanVien || userStore.ten || '',
      },
      // include nguoiNhanId so backend validation won't fail (use activeShift's receiver if available)
      nguoiNhanId:
        (activeShift.value && ((activeShift.value.nguoiNhan && activeShift.value.nguoiNhan.id) || activeShift.value.nguoiNhanId)) ||
        userStore.id,
      nguoiNhan:
        (activeShift.value &&
          (activeShift.value.nguoiNhan || (activeShift.value.nguoiNhanId ? { id: activeShift.value.nguoiNhanId } : null))) ||
        null,
      // include caLamViecId (use active shift's working shift id)
      caLamViecId:
        (activeShift.value &&
          ((activeShift.value.caLamViec && activeShift.value.caLamViec.id) ||
            activeShift.value.ca_lam_viec_id ||
            activeShift.value.caLamViecId)) ||
        null,
      // ensure thoiGianGiaoCa is sent (DB column is NOT NULL) - prefer the original value from activeShift
      thoiGianGiaoCa: normalizeToLocalDateTime(
        (activeShift.value && activeShift.value.thoiGianGiaoCa) || toLocalDateTimeString(new Date())
      ),
      // Set trangThaiCa to indicate shift is ended
      trangThaiCa: 'Hoàn tất',
    }
    
    // Validate payload before sending
    if (!payload.nguoiGiaoId || Number.isNaN(Number(payload.nguoiGiaoId))) {
      Message.error('ID người giao ca không hợp lệ.')
      loading.value = false
      return
    }
    
    if (!payload.nguoiNhanId || Number.isNaN(Number(payload.nguoiNhanId))) {
      Message.error('ID người nhận ca không hợp lệ.')
      loading.value = false
      return
    }
    
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
    
    // dùng endpoint PUT /api/giao-ca/{id} có sẵn trên backend
    await suaGiaoCa(activeShift.value.id, payload)

    // Refresh the giao-ca list so UI reflects the ended shift (lastShift etc.)
    try {
      const res = await getGiaoCa()
      const list = res.data || res || []
      const ended = list.filter((s) => {
        const trangThaiCa = s.trangThaiCa || s.trangThai
        return trangThaiCa === 'Hoàn tất' || s.thoiGianKetThuc
      }).sort((a, b) => {
        const dateA = a.thoiGianKetThuc ? new Date(a.thoiGianKetThuc) : new Date(a.thoiGianGiaoCa || 0)
        const dateB = b.thoiGianKetThuc ? new Date(b.thoiGianKetThuc) : new Date(b.thoiGianGiaoCa || 0)
        return dateB.getTime() - dateA.getTime()
      })
      if (ended.length) {
        lastShift.value = ended[0]
      }
    } catch (e) {
      // ignore refresh errors
      // eslint-disable-next-line no-console
      console.warn('Không tải lại danh sách giao ca sau khi kết thúc', e)
    }

    activeShift.value = null
    Message.success('Kết thúc ca thành công. Đang đăng xuất...')
    
    // Đợi một chút để user thấy thông báo thành công
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // Logout sau khi kết thúc ca thành công
    try {
      await logout()
    } catch (e) {
      // Nếu logout có lỗi, vẫn cố gắng redirect về login
      console.error('Logout after endShift failed', e)
      router.push({ name: 'login' })
    }
  } catch (err) {
    // eslint-disable-next-line no-console
    console.error('Lỗi khi kết thúc ca', err)
    Message.error('Kết thúc ca thất bại')
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

.header-icon-wrapper.amber {
  background: #fef3c7;
  color: #d97706;
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  transition: all 0.2s;
}

.stat-item:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.stat-item.highlight {
  background: #eff6ff;
  border-color: #bfdbfe;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #3b82f6;
  flex-shrink: 0;
}

.stat-item.highlight .stat-icon {
  background: #dbeafe;
  color: #2563eb;
}

.stat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.stat-value {
  font-size: 16px;
  color: #1e293b;
  font-weight: 600;
}

.stat-item.highlight .stat-value {
  color: #2563eb;
  font-size: 18px;
}

.form-section {
  margin-top: 8px;
}

.button-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}

.cash-input-item {
  margin-bottom: 20px;
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

.cash-input-item :deep(.arco-input-number-input) {
  font-size: 16px;
  font-weight: 600;
}

.btn-start {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  margin-top: 8px;
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

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
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

.btn-end {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  margin-top: 8px;
}

@media (max-width: 768px) {
  .giao-ca-container {
    padding: 16px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .info-cards-grid {
    grid-template-columns: 1fr;
  }

}
</style>
