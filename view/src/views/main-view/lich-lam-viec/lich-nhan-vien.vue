<template>
  <div class="container mt-4">
    <h4 class="mb-3 fw-bold">Lịch làm việc của tôi</h4>

    <!-- Bộ lọc -->
    <div class="filter-container d-flex flex-wrap mb-3 align-items-center">
      <input type="date" v-model="filters.startDate" class="form-control me-2" style="width: 200px" />
      <input type="date" v-model="filters.endDate" class="form-control me-2" style="width: 200px" />
      <select v-model="filters.status" class="form-select me-2" style="width: 180px">
        <option value="">Chọn trạng thái</option>
        <option value="Đã làm">Đã làm</option>
        <option value="Dự kiến">Dự kiến</option>
      </select>

      <button class="btn btn-success me-2" @click="showCalendar = true" :disabled="loading">
        <i class="bi bi-calendar-check"></i>
        Xem dạng lịch
      </button>
      <button class="btn btn-outline-success me-2" @click="resetFilter" :disabled="loading">
        <i class="bi bi-arrow-repeat"></i>
        Xóa bộ lọc
      </button>
      <button class="btn btn-outline-primary" @click="fetchData" :disabled="loading">
        <i class="bi bi-arrow-clockwise"></i>
        Làm mới
      </button>
    </div>

    <!-- Hiển thị lịch hoặc bảng -->
    <div v-if="!showCalendar">
      <!-- Bảng danh sách -->
      <div class="table-responsive">
        <table class="table table-striped align-middle">
          <thead class="table-light">
            <tr>
              <th>Nhân viên</th>
              <th>Ca làm việc</th>
              <th>Giờ bắt đầu</th>
              <th>Giờ kết thúc</th>
              <th>Ngày</th>
              <th>Trạng thái</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in filteredList" :key="item.id ?? item.tenNhanVien + index">
              <td>{{ item.tenNhanVien }}</td>
              <td>{{ item.caLamViec }}</td>
              <td>{{ item.gioBatDau }}</td>
              <td>{{ item.gioKetThuc }}</td>
              <td>{{ item.ngay }}</td>
              <td>
                <span
                  class="badge"
                  :class="{
                    'bg-success': item.trangThai === 'Đã làm',
                    'bg-info': item.trangThai === 'Dự kiến',
                  }"
                >
                  {{ item.trangThai }}
                </span>
              </td>
            </tr>
            <tr v-if="!loading && filteredList.length === 0">
              <td colspan="6" class="text-center text-muted py-3">Không có dữ liệu phù hợp</td>
            </tr>
            <tr v-if="loading">
              <td colspan="6" class="text-center py-3">Đang tải dữ liệu...</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Phân trang -->
      <div class="d-flex justify-content-between align-items-center mt-3">
        <span>Trang {{ currentPage }}</span>
        <div class="btn-group">
          <button class="btn btn-outline-secondary" @click="prevPage" :disabled="currentPage === 1">&laquo;</button>
          <button class="btn btn-outline-secondary" @click="nextPage" :disabled="endIndex >= pagedSourceLength">&raquo;</button>
        </div>
      </div>
    </div>

    <div v-else>
      <div class="calendar-container mb-3">
        <div class="calendar-header d-flex justify-content-between align-items-center mb-4">
          <div class="calendar-navigation">
            <button class="btn btn-outline-secondary me-2" @click="previousMonth" :disabled="loading">
              <i class="bi bi-chevron-left"></i>
            </button>
            <button class="btn btn-outline-secondary me-2" @click="currentMonthFn" :disabled="loading">Hôm nay</button>
            <button class="btn btn-outline-secondary" @click="nextMonth" :disabled="loading">
              <i class="bi bi-chevron-right"></i>
            </button>
          </div>

          <h3 class="calendar-title mb-0">Lịch làm việc - {{ currentMonthYear }}</h3>

          <button class="btn btn-outline-secondary quay-lai-bang-btn" @click="showCalendar = false">
            <i class="bi bi-list"></i>
            Quay lại bảng
          </button>
        </div>

        <table class="calendar-grid table table-bordered">
          <thead>
            <tr>
              <th>CN</th>
              <th>Th 2</th>
              <th>Th 3</th>
              <th>Th 4</th>
              <th>Th 5</th>
              <th>Th 6</th>
              <th>Th 7</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(week, wIndex) in calendarDays" :key="wIndex">
              <td
                v-for="day in week"
                :key="day.date.toISOString()"
                :class="{
                  'different-month': day.isCurrentMonth === false,
                  'current-day': isCurrentDay(day.date),
                }"
                style="vertical-align: top; height: 120px; width: 14%"
              >
                <div class="calendar-date">{{ day.dayOfMonth }}</div>
                <div class="calendar-events mt-2">
                  <div
                    v-for="event in getEventsForDay(day.date)"
                    :key="event.id ?? event.tenNhanVien + event.caLamViec + event.gioBatDau"
                    class="calendar-event mb-1 p-1"
                    :class="{
                      'event-done': event.trangThai === 'Đã làm',
                      'event-planned': event.trangThai === 'Dự kiến',
                    }"
                    style="border-radius: 4px; font-size: 12px"
                  >
                    <div class="event-title fw-semibold">{{ event.tenNhanVien }}</div>
                    <div class="event-time text-muted">{{ event.caLamViec }}</div>
                    <div class="event-time text-muted">{{ event.gioBatDau }} - {{ event.gioKetThuc }}</div>
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="loading" class="text-center mt-2">Đang tải lịch...</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { getLichLamViec } from '@/api/lich-lam-viec'

/* ===================== STATE ===================== */
const showCalendar = ref(false)
const loading = ref(false)

const lichLamViec = ref([]) // mapped array for UI

const filters = ref({
  startDate: '',
  endDate: '',
  status: '',
})

const currentPage = ref(1)
const pageSize = 10

/* ===================== HELPERS ===================== */
function extractListFromResponse(res) {
  if (!res) return []
  if (Array.isArray(res)) return res
  if (Array.isArray(res.data)) return res.data
  if (res.data && Array.isArray(res.data.data)) return res.data.data
  return []
}

function formatDateToYYYYMMDD(value) {
  if (!value && value !== 0) return ''
  if (typeof value === 'string') {
    return value.split('T')[0]
  }
  if (value instanceof Date && !Number.isNaN(value.getTime())) {
    return value.toISOString().split('T')[0]
  }
  try {
    const d = new Date(value)
    if (!Number.isNaN(d.getTime())) return d.toISOString().split('T')[0]
  } catch {
     // Không làm gì, chỉ bỏ qua lỗi
  // hoặc Message.error(err.message)
  }
  return ''
}

/**
 * Map API item to UI-friendly item:
 * Output fields: id, tenNhanVien, caLamViec, gioBatDau, gioKetThuc, ngay (YYYY-MM-DD), trangThai, ghiChu
 */
function mapApiItem(item) {
  const tenNhanVien = item?.nhanVien?.tenNhanVien ?? item?.nhanVienTen ?? ''
  const caLamViec = item?.caLamViec?.tenCa ?? item?.caLamViec?.ten_ca ?? item?.caLamViecTen ?? item?.caLamViec?.ten ?? item?.caLam ?? ''
  const ngay = item?.ngayLamViec ?? item?.ngayLam ?? item?.ngay ?? formatDateToYYYYMMDD(item?.ngayLamViec) ?? ''
  const gioBatDau = item?.gioBatDau ?? item?.gio_bat_dau ?? item?.caLamViec?.thoiGianBatDau ?? item?.caLamViec?.thoi_gian_bat_dau ?? ''
  const gioKetThuc = item?.gioKetThuc ?? item?.gio_ket_thuc ?? item?.caLamViec?.thoiGianKetThuc ?? item?.caLamViec?.thoi_gian_ket_thuc ?? ''

  // Kiểm tra và chuyển đổi giá trị trạng thái từ true/false sang "Đã làm" hoặc "Dự kiến"
  let trangThai = '';
if (item?.trangThai === true) {
  trangThai = 'Đã làm';
} else if (item?.trangThai === false) {
  trangThai = 'Dự kiến';
}


  return {
    id: item?.id,
    tenNhanVien,
    caLamViec,
    ngay: formatDateToYYYYMMDD(ngay),
    gioBatDau,
    gioKetThuc,
    trangThai,  // Gán trạng thái đã chuyển đổi
    ghiChu: item?.ghiChu ?? item?.ghi_chu ?? null,
  }
}


/* ===================== FETCH ===================== */
async function fetchData() {
  loading.value = true
  try {
    const res = await getLichLamViec()
    const list = extractListFromResponse(res)
    // map items
    lichLamViec.value = list.map((it) => mapApiItem(it))
    currentPage.value = 1
  } catch (err) {
    // show error message
    Message.error(err?.message ?? 'Không thể lấy lịch làm việc từ server')
  } finally {
    loading.value = false
  }
}

/* ===================== FILTER & PAGINATION ===================== */
const filteredList = computed(() => {
  let list = lichLamViec.value.slice()

  if (filters.value.startDate) {
    const start = new Date(filters.value.startDate)
    list = list.filter((i) => new Date(i.ngay) >= start)
  }
  if (filters.value.endDate) {
    const end = new Date(filters.value.endDate)
    list = list.filter((i) => new Date(i.ngay) <= end)
  }
  if (filters.value.status) {
    list = list.filter((i) => i.trangThai === filters.value.status)
  }

  const startIndex = (currentPage.value - 1) * pageSize
  return list.slice(startIndex, startIndex + pageSize)
})

const pagedSourceLength = computed(() => {
  // length of filtered source (before slicing for table)
  let list = lichLamViec.value.slice()
  if (filters.value.startDate) {
    const start = new Date(filters.value.startDate)
    list = list.filter((i) => new Date(i.ngay) >= start)
  }
  if (filters.value.endDate) {
    const end = new Date(filters.value.endDate)
    list = list.filter((i) => new Date(i.ngay) <= end)
  }
  if (filters.value.status) {
    list = list.filter((i) => i.trangThai === filters.value.status)
  }
  return list.length
})

/* ===================== CALENDAR HELPERS ===================== */
const currentDate = ref(new Date())

const currentMonthYear = computed(() =>
  new Intl.DateTimeFormat('vi-VN', {
    year: 'numeric',
    month: 'long',
  }).format(currentDate.value)
)

const calendarDays = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()

  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const firstDayOfWeek = firstDay.getDay()

  const days = []

  const prevMonth = new Date(year, month, 0)
  const prevMonthDays = prevMonth.getDate()
  for (let i = firstDayOfWeek - 1; i >= 0; i -= 1) {
    days.push({
      date: new Date(year, month - 1, prevMonthDays - i),
      dayOfMonth: prevMonthDays - i,
      isCurrentMonth: false,
    })
  }

  for (let i = 1; i <= lastDay.getDate(); i += 1) {
    days.push({
      date: new Date(year, month, i),
      dayOfMonth: i,
      isCurrentMonth: true,
    })
  }

  const remainingDays = 42 - days.length
  for (let i = 1; i <= remainingDays; i += 1) {
    days.push({
      date: new Date(year, month + 1, i),
      dayOfMonth: i,
      isCurrentMonth: false,
    })
  }

  const weeks = []
  for (let i = 0; i < days.length; i += 7) {
    weeks.push(days.slice(i, i + 7))
  }
  return weeks
})

const getEventsForDay = (date) => {
  return lichLamViec.value.filter((event) => {
    const eventDate = new Date(event.ngay)
    return (
      eventDate.getDate() === date.getDate() && eventDate.getMonth() === date.getMonth() && eventDate.getFullYear() === date.getFullYear()
    )
  })
}

const isCurrentDay = (date) => {
  const today = new Date()
  return date.getDate() === today.getDate() && date.getMonth() === today.getMonth() && date.getFullYear() === today.getFullYear()
}

/* ===================== NAVIGATION & CONTROLS ===================== */
const previousMonth = () => {
  currentDate.value = new Date(currentDate.value.getFullYear(), currentDate.value.getMonth() - 1, 1)
}

const nextMonth = () => {
  currentDate.value = new Date(currentDate.value.getFullYear(), currentDate.value.getMonth() + 1, 1)
}

const currentMonthFn = () => {
  currentDate.value = new Date()
}

const startIndex = computed(() => (currentPage.value - 1) * pageSize)
const endIndex = computed(() => startIndex.value + pageSize)

const applyFilter = () => {
  currentPage.value = 1
}

const resetFilter = () => {
  filters.value = { startDate: '', endDate: '', status: '' }
  currentPage.value = 1
  showCalendar.value = false
}

const prevPage = () => {
  if (currentPage.value > 1) currentPage.value -= 1
}

const nextPage = () => {
  // allow moving forward even if last page is partial
  currentPage.value += 1
}

/* ===================== LIFECYCLE ===================== */
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
/* Calendar styling */
.calendar-container {
  background: var(--color-bg-2);
  border-radius: 12px;
  padding: 16px;
}

.calendar-grid {
  width: 100%;
  border-collapse: collapse;
}

.calendar-grid th {
  padding: 8px;
  text-align: center;
  background-color: var(--color-fill-2);
  border: 1px solid var(--color-border);
  font-weight: 600;
}

.calendar-grid td {
  border: 1px solid var(--color-border);
  vertical-align: top;
  height: 120px;
  width: 14.28%;
  padding: 4px;
}

.calendar-date {
  font-weight: 500;
  margin-bottom: 4px;
  text-align: right;
}

.different-month {
  background-color: var(--color-fill-1);
  color: var(--color-text-3);
}

.current-day .calendar-date {
  color: rgb(var(--primary-6));
  font-weight: 600;
}

.calendar-events {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.calendar-event {
  padding: 4px;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.2;
}

.event-done {
  background-color: rgb(var(--success-1));
  color: rgb(var(--success-6));
  border: 1px solid rgb(var(--success-3));
}

.event-planned {
  background-color: rgb(var(--primary-1));
  color: rgb(var(--primary-6));
  border: 1px solid rgb(var(--primary-3));
}

.event-title {
  font-weight: 500;
  margin-bottom: 2px;
}

.event-time {
  font-size: 11px;
  opacity: 0.8;
}

/* === Sửa phần header để căn khoảng cách đẹp hơn === */
.calendar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 0 12px;
  flex-wrap: nowrap; /* không cho xuống dòng */
}

.calendar-title {
  font-size: 1.6rem;
  font-weight: bold;
  color: rgb(var(--primary-6));
  text-transform: capitalize;
  letter-spacing: 0.5px;
  margin: 0 430px; /* tăng margin ngang trái phải */
  white-space: nowrap;
  flex-shrink: 0;
}

/* Giữ nguyên các nút nằm 2 bên */
.calendar-navigation {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0; /* không co nhỏ */
}

.calendar-navigation .btn-outline-secondary {
  min-width: 40px;
  height: 40px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.calendar-navigation .btn-outline-secondary:hover {
  background-color: rgb(var(--primary-1));
  color: rgb(var(--primary-6));
  border-color: rgb(var(--primary-3));
  transform: scale(1.05);
}

.calendar-navigation .btn-outline-secondary:nth-child(2) {
  min-width: 100px;
  border-radius: 20px;
  background-color: rgb(var(--primary-1));
  color: rgb(var(--primary-6));
  font-weight: 500;
}

.calendar-navigation .btn-outline-secondary:nth-child(2):hover {
  background-color: rgb(var(--primary-2));
  border-color: rgb(var(--primary-4));
}

/* Nút Quay lại bảng */
.quay-lai-bang-btn {
  margin-left: auto;
  border-radius: 20px;
  padding: 8px 18px;
  background-color: rgb(var(--success-1));
  color: rgb(var(--success-6));
  border-color: rgb(var(--success-3));
  font-weight: 500;
  transition: all 0.2s ease;
}

.quay-lai-bang-btn:hover {
  background-color: rgb(var(--success-2));
  border-color: rgb(var(--success-4));
  transform: scale(1.03);
}

/* Calendar-header .btn-outline-secondary ở ngoài (nút Quay lại bảng) */
.calendar-header .btn-outline-secondary {
  border-radius: 20px;
  padding: 6px 16px;
  background-color: rgb(var(--success-1));
  color: rgb(var(--success-6));
  border-color: rgb(var(--success-3));
  font-weight: 500;
  transition: all 0.2s ease;
}

.calendar-header .btn-outline-secondary:hover {
  background-color: rgb(var(--success-2));
  border-color: rgb(var(--success-4));
  transform: scale(1.02);
}

.container {
  padding: 0 20px 20px 20px;
}

.filter-container {
  gap: 16px;
}

/* Card styling */
.table-responsive {
  background: var(--color-bg-2);
  border-radius: 12px;
  margin-bottom: 16px;
  padding: 16px;
}

/* Table styling */
.table {
  width: 100%;
  background: var(--color-bg-2);
  border-collapse: collapse;
}

.table th {
  font-weight: 600;
  background: var(--color-fill-2);
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border);
  color: var(--color-text-1);
  text-align: left;
  white-space: nowrap;
}

.table td {
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border);
  color: var(--color-text-1);
  text-align: left;
  vertical-align: middle;
}

.table-striped > tbody > tr:nth-of-type(odd) {
  background-color: var(--color-fill-1);
}

/* Badge styling to match Arco tags */
.badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.5715;
}

.bg-success {
  color: rgb(var(--success-6));
  background: rgb(var(--success-1));
}

.bg-info {
  color: rgb(var(--primary-6));
  background: rgb(var(--primary-1));
}

/* Filter controls */
.form-control,
.form-select {
  padding: 4px 12px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  background: var(--color-bg-2);
  color: var(--color-text-1);
  transition: all 0.1s cubic-bezier(0, 0, 1, 1);
  margin-right: 8px;
}

.form-control:focus,
.form-select:focus {
  border-color: rgb(var(--primary-6));
  box-shadow: 0 0 0 2px rgb(var(--primary-2));
}

/* Button styling to match Arco */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 16px;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.5715;
  transition: all 0.1s cubic-bezier(0, 0, 1, 1);
  gap: 8px;
}

.btn-success {
  color: #fff;
  background: rgb(var(--success-6));
  border: 1px solid rgb(var(--success-6));
}

.btn-success:hover {
  background: rgb(var(--success-5));
  border-color: rgb(var(--success-5));
}

.btn-outline-success {
  color: rgb(var(--success-6));
  border: 1px solid rgb(var(--success-6));
  background: transparent;
}

.btn-outline-success:hover {
  color: #fff;
  background: rgb(var(--success-6));
}

.btn-outline-secondary {
  color: var(--color-text-1);
  border: 1px solid var(--color-border);
  background: transparent;
}

.btn-outline-secondary:hover:not(:disabled) {
  color: rgb(var(--primary-6));
  border-color: rgb(var(--primary-6));
}

.btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

/* Pagination section */
.btn-group {
  display: inline-flex;
}

.btn-group .btn {
  border-radius: 0;
}

.btn-group .btn:first-child {
  border-top-left-radius: 4px;
  border-bottom-left-radius: 4px;
}

.btn-group .btn:last-child {
  border-top-right-radius: 4px;
  border-bottom-right-radius: 4px;
}

/* Calendar title styling */

/* Căn hàng ngang cho phần tiêu đề và các nút */
/* Đã chuyển sang kiểu position absolute cho tiêu đề nên không cần nữa */
/* .calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;  giúp responsive khi nhỏ màn hình 
} */

/* Tiêu đề nằm giữa, căn giữa đẹp */
/* Đã cập nhật bên trên */
</style>
