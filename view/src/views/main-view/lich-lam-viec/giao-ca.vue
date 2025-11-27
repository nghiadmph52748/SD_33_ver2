<template>
  <div class="giao-ca-card">
    <ConfirmModal ref="confirmModal" />
    <h3>Ca Làm Việc</h3>

    <div v-if="lastShift">
      <div class="previous-shift">
        <strong>Thông tin ca trước (Bàn giao)</strong>
        <div>Người giao ca: <strong>{{ lastShift.nguoiGiao?.tenNhanVien || lastShift.nguoiGiao?.name || 'N/A' }}</strong></div>
        <div>Tiền mặt cuối ca trước: <strong>{{ formatCurrency(lastShift.tongTienCuoiCa) }}</strong></div>
        <div>Số đơn hàng chờ xử lý: <strong>{{ lastShift.soDonChoXuLy || 0 }}</strong></div>
      </div>
    </div>

    <div v-if="!activeShift">
      <p>Chưa có ca nào đang hoạt động. Nhập số tiền mặt ban đầu rồi bắt đầu ca.</p>
      <label>
        Số tiền mặt ban đầu (VND)
        <input type="number" v-model.number="initialCash" placeholder="Nhập số tiền mặt" />
      </label>
      <button class="btn-start" :disabled="loading" @click="startShift">
        {{ loading ? 'Đang tạo...' : 'Bắt Đầu Ca' }}
      </button>
    </div>

    <div v-else>
      <p>Ca đang hoạt động từ: <strong>{{ formatDate(activeShift.thoiGianGiaoCa) }}</strong></p>
      <div class="shift-info">
        <p>Người giao ca: <strong class="amount">{{ activeShift.nguoiGiao?.tenNhanVien || activeShift.nguoiGiao?.name || 'N/A' }}</strong></p>
        <p>Số tiền mặt ban đầu: <strong class="amount">{{ formatCurrency(activeShift.tongTienBanDau) }}</strong></p>
        <p v-if="lastShift && lastShift.tongTienCuoiCa">Tiền mặt cuối ca trước: <strong class="amount">{{ formatCurrency(lastShift.tongTienCuoiCa) }}</strong></p>
      </div>
      <div class="shift-sales">
        <p>Số hóa đơn trong ca: <strong>{{ invoiceCount }}</strong></p>
        <p>Tổng doanh thu (ước tính): <strong>{{ formatCurrency(invoiceTotal) }}</strong></p>
      </div>
      <div class="actions">
        <button class="btn-end" :disabled="loading" @click="endShift">
          {{ loading ? 'Đang xử lý...' : 'Kết Thúc Ca' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store'
import { themGiaoCa, suaGiaoCa, getGiaoCa } from '@/api/giao-ca'
import { fetchHoaDonList } from '@/api/hoa-don'
import ConfirmModal from '@/components/ConfirmModal.vue'
import { Message } from '@arco-design/web-vue'
import useUser from '@/hooks/user'

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

// Expose reload function to parent/navbar
async function reloadShiftData() {
  try {
    const res = await getGiaoCa()
    const list = (res.data || res) || []
    const active = list.find((s) => s.trangThai === 'Đang hoạt động' || !s.thoiGianKetThuc)
    if (active) {
      activeShift.value = active
      computeShiftSales(activeShift.value).catch((e) => console.warn(e))
    }
    const ended = list
      .filter((s) => s.thoiGianKetThuc)
      .sort((a, b) => new Date(b.thoiGianKetThuc) - new Date(a.thoiGianKetThuc))
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

async function startShift() {
  const ok = await confirmModal.value.showConfirm(
    'Bắt Đầu Ca Làm Việc',
    'Bạn có chắc muốn bắt đầu ca làm việc với số tiền mặt ban đầu là ' + formatCurrency(initialCash.value) + ' không?',
    'Xác Nhận',
    'Hủy'
  )
  if (!ok) return
  loading.value = true
  try {
    const payload = {
      nguoiGiaoId: (lastShift.value && lastShift.value.nguoiGiao && lastShift.value.nguoiGiao.id) || userStore.id,
      nguoiNhanId: userStore.id,
      caLamViecId: (lastShift.value && (lastShift.value.caLamViec?.id || lastShift.value.ca_lam_viec_id)) || 1,
      thoiGianGiaoCa: toLocalDateTimeString(new Date()),
      tongTienBanDau: Number(initialCash.value) || 0,
      trangThai: 'Đang hoạt động'
    }
    const res = await themGiaoCa(payload)
    activeShift.value = res.data || res
    Message.success('Bắt đầu ca thành công')
  } catch (err) {
    // eslint-disable-next-line no-console
    console.error('Lỗi khi bắt đầu ca', err)
    Message.error('Bắt đầu ca thất bại')
  } finally {
    loading.value = false
  }
}

// Load shifts on mount to find active or last shift
onMounted(async () => {
  try {
    const res = await getGiaoCa()
    const list = (res.data || res) || []
    // find active shift: trangThai === 'Đang hoạt động' or no thoiGianKetThuc
    const active = list.find((s) => s.trangThai === 'Đang hoạt động' || !s.thoiGianKetThuc)
    if (active) {
      activeShift.value = active
      // compute sales for active shift
      computeShiftSales(activeShift.value).catch((e) => console.warn(e))
    }
    // find last (most recent) ended shift
    const ended = list
      .filter((s) => s.thoiGianKetThuc)
      .sort((a, b) => new Date(b.thoiGianKetThuc) - new Date(a.thoiGianKetThuc))
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
  if (!activeShift.value || !activeShift.value.id) return
  const ok = await confirmModal.value.showConfirm(
    'Kết Thúc Ca Làm Việc',
    'Bạn có chắc muốn kết thúc ca hiện tại không?',
    'Xác Nhận',
    'Hủy'
  )
  if (!ok) return
  loading.value = true
  try {
    const payload = {
      thoiGianKetThuc: toLocalDateTimeString(new Date()),
      trangThai: 'Đã kết thúc',
      // backend expects nguoiGiaoId (id of the person handing over)
      nguoiGiaoId: userStore.id,
      nguoiGiao: {
        id: userStore.id,
        tenNhanVien: userStore.name || userStore.tenNhanVien || userStore.ten || ''
      },
      // include nguoiNhanId so backend validation won't fail (use activeShift's receiver if available)
      nguoiNhanId: (activeShift.value && ((activeShift.value.nguoiNhan && activeShift.value.nguoiNhan.id) || activeShift.value.nguoiNhanId)) || userStore.id,
      nguoiNhan: (activeShift.value && (activeShift.value.nguoiNhan || (activeShift.value.nguoiNhanId ? { id: activeShift.value.nguoiNhanId } : null))) || null,
      // include caLamViecId (use active shift's working shift id or fallback to 1)
      caLamViecId: (activeShift.value && ((activeShift.value.caLamViec && activeShift.value.caLamViec.id) || activeShift.value.ca_lam_viec_id || activeShift.value.caLamViecId)) || 1,
      // ensure thoiGianGiaoCa is sent (DB column is NOT NULL) - prefer the original value from activeShift
      thoiGianGiaoCa: normalizeToLocalDateTime((activeShift.value && activeShift.value.thoiGianGiaoCa) || toLocalDateTimeString(new Date()))
    }
    // dùng endpoint PUT /api/giao-ca/{id} có sẵn trên backend
    await suaGiaoCa(activeShift.value.id, payload)

    // Refresh the giao-ca list so UI reflects the ended shift (lastShift etc.)
    try {
      const res = await getGiaoCa()
      const list = (res.data || res) || []
      const ended = list
        .filter((s) => s.thoiGianKetThuc)
        .sort((a, b) => new Date(b.thoiGianKetThuc) - new Date(a.thoiGianKetThuc))
      if (ended.length) {
        lastShift.value = ended[0]
      }
    } catch (e) {
      // ignore refresh errors
      // eslint-disable-next-line no-console
      console.warn('Không tải lại danh sách giao ca sau khi kết thúc', e)
    }

    activeShift.value = null
    Message.success('Kết thúc ca thành công')
    try {
      await logout()
    } catch (e) {
      // ignore logout errors
      // eslint-disable-next-line no-console
      console.warn('Logout after endShift failed', e)
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
.giao-ca-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  max-width: 500px;
  margin: 40px auto;
}

.giao-ca-card h3 {
  text-align: center;
  color: #2c3e50;
  margin-bottom: 24px;
  font-size: 24px;
  font-weight: 600;
}

.previous-shift {
  background: #fff;
  padding: 18px;
  border-radius: 8px;
  margin-bottom: 30px;
  border-left: 5px solid #f39c12;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.previous-shift strong {
  display: block;
  color: #2c3e50;
  font-size: 15px;
  margin-bottom: 14px;
  font-weight: 600;
}

.previous-shift div {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  font-size: 14px;
  color: #555;
  border-bottom: 1px solid #eee;
}

.previous-shift div:last-child {
  border-bottom: none;
}

.previous-shift div strong {
  color: #f39c12;
  margin-bottom: 0;
  display: inline;
  font-weight: 600;
}

label {
  display: block;
  margin-bottom: 14px;
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
}

input[type="number"] {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s ease;
  box-sizing: border-box;
  margin-bottom: 24px;
}

input[type="number"]:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1);
}

input[type="number"]::placeholder {
  color: #999;
}

.shift-info {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin: 24px 0;
  border-left: 5px solid #1890ff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.shift-info p {
  margin: 14px 0;
  font-size: 14px;
  color: #555;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.shift-info p strong {
  color: #2c3e50;
  font-weight: 600;
}

.shift-sales {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin: 24px 0;
  border-left: 5px solid #2ecc71;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.shift-sales p {
  margin: 14px 0;
  font-size: 14px;
  color: #555;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.shift-sales p strong {
  color: #2c3e50;
  font-weight: 600;
}

.amount {
  color: #1890ff;
  font-size: 16px;
  font-weight: 600;
}

.btn-start,
.btn-end {
  width: 100%;
  padding: 14px 20px;
  border: none;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.btn-start {
  background: linear-gradient(135deg, #2ecc71 0%, #27ae60 100%);
  color: #fff;
  margin-bottom: 14px;
}

.btn-start:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(46, 204, 113, 0.4);
}

.btn-start:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-end {
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
  color: #fff;
}

.btn-end:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(231, 76, 60, 0.4);
}

.btn-end:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.actions {
  margin-top: 28px;
  display: flex;
  gap: 14px;
}

.actions button {
  flex: 1;
}

p {
  text-align: center;
  color: #2c3e50;
  margin-bottom: 22px;
  font-size: 14px;
  line-height: 1.6;
}

p strong {
  color: #1890ff;
  font-weight: 600;
}
</style>
