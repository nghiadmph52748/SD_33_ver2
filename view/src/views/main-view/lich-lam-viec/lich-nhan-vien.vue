<template>
  <div class="lich-nhan-vien-container">
    <div class="page-header">
      <h2>{{ $t('lichLamViec.mySchedule.title') }}</h2>
      <p>{{ $t('lichLamViec.mySchedule.subtitle') }}</p>
    </div>

    <!-- Filter Section -->
    <a-card class="filter-card">
      <div class="filter-content">
        <a-date-picker
          v-model="filters.startDate"
          :placeholder="$t('lichLamViec.mySchedule.fromDate')"
          style="width: 200px"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
        <a-date-picker
          v-model="filters.endDate"
          :placeholder="$t('lichLamViec.mySchedule.toDate')"
          style="width: 200px"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
        <a-select
          v-model="filters.status"
          :placeholder="$t('lichLamViec.mySchedule.selectStatus')"
          style="width: 180px"
          allow-clear
        >
          <a-option value="Đã làm">{{ $t('lichLamViec.status.done') }}</a-option>
          <a-option value="Dự kiến">{{ $t('lichLamViec.status.planned') }}</a-option>
        </a-select>

        <a-button type="primary" @click="showCalendar = true" :loading="loading">
          <template #icon>
            <icon-calendar />
          </template>
          {{ $t('lichLamViec.mySchedule.viewCalendar') }}
        </a-button>
        <a-button @click="resetFilter" :loading="loading">
          <template #icon>
            <icon-refresh />
          </template>
          {{ $t('lichLamViec.mySchedule.clearFilter') }}
        </a-button>
        <a-button @click="fetchData" :loading="loading">
          <template #icon>
            <icon-sync />
          </template>
          {{ $t('lichLamViec.mySchedule.refresh') }}
        </a-button>
      </div>
    </a-card>

    <!-- Table View -->
    <div v-if="!showCalendar">
      <a-card class="table-card">
        <a-table
          :columns="columns"
          :data="filteredList"
          :loading="loading"
          :pagination="false"
          :bordered="false"
        >
          <template #status="{ record }">
            <a-tag :color="record.trangThai === 'Đã làm' ? 'green' : 'blue'">
              {{ record.trangThai }}
            </a-tag>
          </template>
          <template #empty>
            <a-empty :description="$t('lichLamViec.mySchedule.noData')" />
          </template>
        </a-table>

        <!-- Pagination -->
        <div class="pagination-wrapper">
          <span class="page-info">{{ $t('lichLamViec.mySchedule.page') }} {{ currentPage }}</span>
          <div class="pagination-controls">
            <a-button @click="prevPage" :disabled="currentPage === 1">
              <template #icon>
                <icon-left />
              </template>
            </a-button>
            <a-button @click="nextPage" :disabled="endIndex >= pagedSourceLength">
              <template #icon>
                <icon-right />
              </template>
            </a-button>
          </div>
        </div>
      </a-card>
    </div>

    <!-- Calendar View -->
    <div v-else>
      <a-card class="calendar-card">
        <div class="calendar-header">
          <div class="calendar-navigation">
            <a-button @click="previousMonth" :disabled="loading">
              <template #icon>
                <icon-left />
              </template>
            </a-button>
            <a-button @click="currentMonthFn" :disabled="loading">{{ $t('lichLamViec.mySchedule.today') }}</a-button>
            <a-button @click="nextMonth" :disabled="loading">
              <template #icon>
                <icon-right />
              </template>
            </a-button>
          </div>

          <h3 class="calendar-title">{{ $t('lichLamViec.mySchedule.calendarTitle') }} - {{ currentMonthYear }}</h3>

          <a-button @click="showCalendar = false">
            <template #icon>
              <icon-list />
            </template>
            {{ $t('lichLamViec.mySchedule.backToTable') }}
          </a-button>
        </div>

        <div class="calendar-grid-wrapper">
          <table class="calendar-grid">
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
                  class="calendar-day"
                >
                  <div class="calendar-date">{{ day.dayOfMonth }}</div>
                  <div class="calendar-events">
                    <div
                      v-for="event in getEventsForDay(day.date)"
                      :key="event.id ?? event.tenNhanVien + event.caLamViec + event.gioBatDau"
                      class="calendar-event"
                      :class="{
                        'event-done': event.trangThai === 'Đã làm',
                        'event-planned': event.trangThai === 'Dự kiến',
                      }"
                    >
                      <div class="event-title">{{ event.tenNhanVien }}</div>
                      <div class="event-time">{{ event.caLamViec }}</div>
                      <div class="event-time">{{ event.gioBatDau }} - {{ event.gioKetThuc }}</div>
                    </div>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-if="loading" class="loading-indicator">
          <a-spin />
          <span>{{ $t('lichLamViec.mySchedule.loadingCalendar') }}</span>
        </div>
      </a-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
import { getLichLamViec } from '@/api/lich-lam-viec'
import {
  IconCalendar,
  IconRefresh,
  IconSync,
  IconLeft,
  IconRight,
  IconList,
} from '@arco-design/web-vue/es/icon'

const { t } = useI18n()

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

const columns = computed(() => [
  {
    title: t('lichLamViec.mySchedule.columns.employee'),
    dataIndex: 'tenNhanVien',
  },
  {
    title: t('lichLamViec.mySchedule.columns.shift'),
    dataIndex: 'caLamViec',
  },
  {
    title: t('lichLamViec.mySchedule.columns.startTime'),
    dataIndex: 'gioBatDau',
  },
  {
    title: t('lichLamViec.mySchedule.columns.endTime'),
    dataIndex: 'gioKetThuc',
  },
  {
    title: t('lichLamViec.mySchedule.columns.date'),
    dataIndex: 'ngay',
  },
  {
    title: t('lichLamViec.mySchedule.columns.status'),
    dataIndex: 'trangThai',
    slotName: 'status',
  },
])

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
  let trangThai = ''
  if (item?.trangThai === true) {
    trangThai = 'Đã làm'
  } else if (item?.trangThai === false) {
    trangThai = 'Dự kiến'
  }

  return {
    id: item?.id,
    tenNhanVien,
    caLamViec,
    ngay: formatDateToYYYYMMDD(ngay),
    gioBatDau,
    gioKetThuc,
    trangThai, // Gán trạng thái đã chuyển đổi
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
    Message.error(err?.message ?? t('lichLamViec.mySchedule.fetchError'))
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
.lich-nhan-vien-container {
  padding: 24px;
  background: #f8fafc;
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}

.page-header p {
  margin: 0;
  font-size: 14px;
  color: #64748b;
}

.filter-card {
  margin-bottom: 24px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.filter-content {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.table-card,
.calendar-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.pagination-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
}

.page-info {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.pagination-controls {
  display: flex;
  gap: 8px;
}

/* Calendar Styles */
.calendar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f1f5f9;
}

.calendar-navigation {
  display: flex;
  align-items: center;
  gap: 8px;
}

.calendar-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  text-transform: capitalize;
  margin: 0;
  flex: 1;
  text-align: center;
}

.calendar-grid-wrapper {
  overflow-x: auto;
}

.calendar-grid {
  width: 100%;
  border-collapse: collapse;
  background: #ffffff;
}

.calendar-grid th {
  padding: 12px 8px;
  text-align: center;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  font-weight: 600;
  font-size: 14px;
  color: #475569;
}

.calendar-day {
  vertical-align: top;
  height: 120px;
  width: 14.28%;
  padding: 8px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
}

.calendar-day.different-month {
  background: #f8fafc;
  color: #94a3b8;
}

.calendar-day.current-day {
  background: #eff6ff;
}

.calendar-day.current-day .calendar-date {
  color: #2563eb;
  font-weight: 700;
}

.calendar-date {
  font-weight: 500;
  margin-bottom: 8px;
  text-align: right;
  font-size: 14px;
  color: #1e293b;
}

.calendar-events {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.calendar-event {
  padding: 6px 8px;
  border-radius: 6px;
  font-size: 11px;
  line-height: 1.4;
  cursor: pointer;
  transition: all 0.2s;
}

.calendar-event:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.event-done {
  background: #d1fae5;
  color: #059669;
  border: 1px solid #a7f3d0;
}

.event-planned {
  background: #dbeafe;
  color: #2563eb;
  border: 1px solid #bfdbfe;
}

.event-title {
  font-weight: 600;
  margin-bottom: 2px;
  font-size: 12px;
}

.event-time {
  font-size: 10px;
  opacity: 0.8;
  margin-top: 2px;
}

.loading-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  color: #64748b;
}

:deep(.arco-card-body) {
  padding: 24px;
}

:deep(.arco-table-th) {
  background: #f8fafc;
  font-weight: 600;
}

:deep(.arco-table-tr:hover) {
  background: #f8fafc;
}

@media (max-width: 768px) {
  .lich-nhan-vien-container {
    padding: 16px;
  }

  .filter-content {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-content > * {
    width: 100%;
  }

  .calendar-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .calendar-title {
    text-align: center;
  }

  .calendar-navigation {
    justify-content: center;
  }
}
</style>
