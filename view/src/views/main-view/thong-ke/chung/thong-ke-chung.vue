<template>
  <div class="general-statistics">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Bộ lọc thống kê -->
    <div class="filter-section">
      <a-card class="filter-card" :bordered="false">
        <div class="filter-header">
          <div class="filter-title">
            <icon-filter class="filter-icon" />
            <span>Bộ Lọc Thống Kê</span>
          </div>
        </div>
        
        <div class="filter-content">
          <!-- Dòng 1: Labels -->
          <div class="filter-row-1">
            <div class="filter-label">Khoảng thời gian thống kê</div>
            <div class="filter-label">Loại biểu đồ</div>
          </div>
          
          <!-- Dòng 2: Controls -->
          <div class="filter-row-2">
            <a-select v-model="selectedTimeRange" class="filter-select" @change="onTimeRangeChange">
              <a-option value="today">Hôm nay</a-option>
              <a-option value="week">Tuần này</a-option>
              <a-option value="month">Tháng này</a-option>
              <a-option value="year">Năm này</a-option>
              <a-option value="custom">Tùy chọn</a-option>
            </a-select>
            
            <a-select v-model="selectedChartType" class="filter-select" @change="onChartTypeChange">
              <a-option value="line">Biểu đồ đường</a-option>
              <a-option value="bar">Biểu đồ cột</a-option>
              <a-option value="pie">Biểu đồ tròn</a-option>
              <a-option value="area">Biểu đồ vùng</a-option>
            </a-select>
          </div>
          
          <!-- Dòng 3: Summary và Actions -->
          <div class="filter-row-3">
            <div class="filter-summary">
              <span class="summary-text">Số đơn hàng: {{ totalOrdersCount }} | Tổng doanh thu: {{ formatCurrency(totalRevenueAmount) }}</span>
            </div>
            
            <div class="filter-actions">
              <a-button class="reset-btn" @click="resetFilter">
                <icon-refresh class="action-icon" />
                Đặt lại bộ lọc
              </a-button>
              <a-button type="primary" class="export-btn" @click="exportReport">
                <icon-export class="action-icon" />
                Xuất báo cáo
              </a-button>
            </div>
          </div>
        </div>
      </a-card>
    </div>

    <!-- Statistics Cards -->
    <div class="stats-grid">
      <!-- Panel Hôm nay -->
      <a-card class="stat-card today-card" :bordered="false">
        <div class="today-header">
          <div class="today-title-section">
            <div class="today-icon-wrapper">
              <icon-calendar class="today-icon" />
            </div>
            <div class="today-title">Hôm nay</div>
          </div>
          <div class="today-revenue-section">
            <div class="today-revenue-value">{{ formatCurrency(todayRevenue) }}</div>
            <div class="today-revenue-change">
              <icon-arrow-down class="change-icon" />
              <span>0%</span>
            </div>
          </div>
        </div>
        <div class="today-details">
          <span class="today-detail-text">Sản phẩm đã bán: {{ todayProductsSold }} | Đơn hàng: {{ todayOrders }}</span>
        </div>
      </a-card>

      <!-- Panel Tuần này -->
      <a-card class="stat-card today-card" :bordered="false">
        <div class="today-header">
          <div class="today-title-section">
            <div class="today-icon-wrapper week-icon-wrapper">
              <icon-calendar class="today-icon" />
            </div>
            <div class="today-title">Tuần này</div>
          </div>
          <div class="today-revenue-section">
            <div class="today-revenue-value">{{ formatCurrency(weekRevenue) }}</div>
            <div class="today-revenue-change">
              <icon-arrow-down class="change-icon" />
              <span>0%</span>
            </div>
          </div>
        </div>
        <div class="today-details">
          <span class="today-detail-text">Sản phẩm đã bán: {{ weekProductsSold }} | Đơn hàng: {{ weekOrders }}</span>
        </div>
      </a-card>

      <!-- Panel Tháng này -->
      <a-card class="stat-card today-card" :bordered="false">
        <div class="today-header">
          <div class="today-title-section">
            <div class="today-icon-wrapper month-icon-wrapper">
              <icon-calendar class="today-icon" />
            </div>
            <div class="today-title">Tháng này</div>
          </div>
          <div class="today-revenue-section">
            <div class="today-revenue-value">{{ formatCurrency(monthRevenue) }}</div>
            <div class="today-revenue-change">
              <icon-arrow-down class="change-icon" />
              <span>0%</span>
            </div>
          </div>
        </div>
        <div class="today-details">
          <span class="today-detail-text">Sản phẩm đã bán: {{ monthProductsSold }} | Đơn hàng: {{ monthOrders }}</span>
        </div>
      </a-card>

      <!-- Panel Năm này -->
      <a-card class="stat-card today-card" :bordered="false">
        <div class="today-header">
          <div class="today-title-section">
            <div class="today-icon-wrapper year-icon-wrapper">
              <icon-calendar class="today-icon" />
            </div>
            <div class="today-title">Năm này</div>
          </div>
          <div class="today-revenue-section">
            <div class="today-revenue-value">{{ formatCurrency(yearRevenue) }}</div>
            <div class="today-revenue-change">
              <icon-arrow-down class="change-icon" />
              <span>0%</span>
            </div>
          </div>
        </div>
        <div class="today-details">
          <span class="today-detail-text">Sản phẩm đã bán: {{ yearProductsSold }} | Đơn hàng: {{ yearOrders }}</span>
        </div>
      </a-card>
    </div>

    <!-- Charts Section -->
    <div class="charts-section">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-card title="Doanh thu theo tháng" class="chart-card">
            <template #extra>
              <a-select v-model="revenuePeriod" style="width: 120px">
                <a-option value="6months">6 tháng</a-option>
                <a-option value="12months">12 tháng</a-option>
              </a-select>
            </template>
            <div class="chart-container">
              <v-chart class="chart" :option="revenueChartOption" autoresize />
            </div>
          </a-card>
        </a-col>

        <a-col :span="12">
          <a-card title="Top sản phẩm bán chạy" class="chart-card">
            <template #extra>
              <a-select v-model="topProductsPeriod" style="width: 120px">
                <a-option value="week">Tuần này</a-option>
                <a-option value="month">Tháng này</a-option>
              </a-select>
            </template>
            <div class="chart-container">
              <v-chart class="chart" :option="topProductsChartOption" autoresize />
            </div>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px">
        <a-col :span="12">
          <a-card title="Phân loại khách hàng" class="chart-card">
            <div class="chart-container">
              <v-chart class="chart" :option="customerChartOption" autoresize />
            </div>
          </a-card>
        </a-col>

        <a-col :span="12">
          <a-card title="Hoạt động gần đây" class="chart-card">
            <a-timeline>
              <a-timeline-item v-for="(act, idx) in recentActivities" :key="idx">
                <template #dot>
                  <icon-check-circle v-if="act.type === 'order'" />
                  <icon-archive v-else-if="act.type === 'product'" />
                  <icon-user v-else-if="act.type === 'customer'" />
                  <icon-gift v-else />
                </template>
                <div class="activity-item">
                  <div class="activity-title">{{ act.title }}</div>
                  <div class="activity-time">{{ act.time }}</div>
                </div>
              </a-timeline-item>
            </a-timeline>
          </a-card>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { 
  IconArrowDown, 
  IconUser, 
  IconCheckCircle, 
  IconCalendar,
  IconFilter,
  IconRefresh,
  IconExport
} from '@arco-design/web-vue/es/icon'

// ECharts setup
use([CanvasRenderer, LineChart, BarChart, PieChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()

// Mock data - trong thực tế sẽ fetch từ API
const totalRevenue = ref(0)

// Dữ liệu hôm nay
const todayProductsSold = ref(0)
const todayOrders = ref(0)
const todayRevenue = ref(0)

// Dữ liệu tuần này
const weekProductsSold = ref(0)
const weekOrders = ref(0)
const weekRevenue = ref(0)

// Dữ liệu tháng này
const monthProductsSold = ref(0)
const monthOrders = ref(0)
const monthRevenue = ref(0)

// Dữ liệu năm này
const yearProductsSold = ref(0)
const yearOrders = ref(0)
const yearRevenue = ref(0)

// Bộ lọc thống kê
const selectedTimeRange = ref('today')
const selectedChartType = ref('line')
const totalOrdersCount = ref(0)
const totalRevenueAmount = ref(0)

const revenuePeriod = ref('6months')
const topProductsPeriod = ref('month')

// Chart data
const revenueData = ref<{ month: string; revenue: number }[]>([])
const ordersList = ref<any[]>([])
const productsList = ref<any[]>([])
const customersList = ref<any[]>([])

const topProductsData = ref<{ name: string; value: number; revenue: number }[]>([])

const customerData = ref([
  { name: 'Khách hàng VIP', value: 15 },
  { name: 'Khách hàng thường', value: 45 },
  { name: 'Khách hàng mới', value: 25 },
  { name: 'Khách hàng tiềm năng', value: 15 },
])

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const recentActivities = ref<{ type: 'product' | 'order' | 'customer'; title: string; time: string; at: number }[]>([])

const toRelativeTime = (date: Date) => {
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()

  // Nếu thời gian trong tương lai hoặc quá xa, hiển thị thời gian tuyệt đối
  if (diffMs < 0) {
    return date.toLocaleString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  const seconds = Math.floor(diffMs / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (seconds < 60) return 'Vừa xong'
  if (minutes < 60) return `${minutes} phút trước`
  if (hours < 24) return `${hours} giờ trước`
  if (days < 7) return `${days} ngày trước`

  // Nếu quá 7 ngày, hiển thị ngày tháng
  return date.toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const buildRecentActivities = () => {
  const acts: { type: 'product' | 'order' | 'customer'; title: string; time: string; at: number }[] = []

  // 1. Đơn hàng thành công mới (đã thanh toán)
  ordersList.value.forEach((o: any) => {
    if (o?.ngayThanhToan && (o?.trangThai === true || o?.ngayThanhToan)) {
      const dt = new Date(o.ngayThanhToan)
      if (!Number.isNaN(dt.getTime())) {
        const amount = Number(o?.tongTienSauGiam ?? o?.tongTien ?? 0)
        acts.push({
          type: 'order',
          title: `Đơn hàng thành công ${o?.tenHoaDon ?? `#${o?.id ?? ''}`} - ${formatCurrency(Number.isNaN(amount) ? 0 : amount)}`,
          time: toRelativeTime(dt),
          at: dt.getTime(),
        })
      }
    }
  })

  // 2. Thêm sản phẩm mới
  productsList.value.forEach((p: any) => {
    const createDate = p?.createAt || p?.create_at
    if (createDate) {
      const dt = new Date(createDate)
      if (!Number.isNaN(dt.getTime())) {
        acts.push({
          type: 'product',
          title: `Thêm sản phẩm mới: ${p?.tenSanPham ?? `#${p?.id ?? ''}`}`,
          time: toRelativeTime(dt),
          at: dt.getTime(),
        })
      }
    }
  })

  // 3. Khách hàng mới
  customersList.value.forEach((c: any) => {
    const createDate = c?.createAt || c?.create_at
    if (createDate) {
      const dt = new Date(createDate)
      if (!Number.isNaN(dt.getTime())) {
        acts.push({
          type: 'customer',
          title: `Khách hàng mới: ${c?.tenKhachHang ?? `#${c?.id ?? ''}`}`,
          time: toRelativeTime(dt),
          at: dt.getTime(),
        })
      }
    }
  })

  // Sắp xếp theo thời gian giảm dần (mới nhất lên đầu)
  acts.sort((a, b) => b.at - a.at)
  recentActivities.value = acts.slice(0, 10)
}

// Build revenue by month from ordersList and revenuePeriod
const isPaidOrder = (order: any) => order?.trangThai === true || !!order?.ngayThanhToan
const orderAmount = (order: any) => {
  const amount = Number(order?.tongTienSauGiam ?? order?.tongTien ?? 0)
  return Number.isNaN(amount) ? 0 : amount
}

const buildRevenueData = () => {
  const now = new Date()
  const months: { label: string; year: number; monthIndex: number }[] = []
  const count = revenuePeriod.value === '12months' ? 12 : 6
  for (let i = count - 1; i >= 0; i -= 1) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    months.push({ label: `Tháng ${d.getMonth() + 1}`, year: d.getFullYear(), monthIndex: d.getMonth() })
  }

  const sums: number[] = new Array(months.length).fill(0)
  ordersList.value
    .filter((o: any) => isPaidOrder(o))
    .forEach((o: any) => {
      const dateStr = o?.ngayThanhToan ?? o?.ngayTao
      if (!dateStr) return
      const dt = new Date(dateStr)
      if (Number.isNaN(dt.getTime())) return
      const y = dt.getFullYear()
      const m = dt.getMonth()
      const idx = months.findIndex((mm) => mm.year === y && mm.monthIndex === m)
      if (idx >= 0) sums[idx] += orderAmount(o)
    })

  revenueData.value = months.map((m, idx) => ({ month: m.label, revenue: sums[idx] }))
}

// Chart options
const revenueChartOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    formatter: (params: any) => {
      const data = params[0]
      return `${data.axisValue}<br/>${data.seriesName}: ${formatCurrency(data.value)}`
    },
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true,
  },
  xAxis: {
    type: 'category',
    data: revenueData.value.map((item) => item.month),
    axisLabel: {
      rotate: 45,
    },
  },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: (value: number) => `${(value / 1000000).toFixed(0)}M`,
    },
  },
  series: [
    {
      name: 'Doanh thu',
      type: 'line',
      data: revenueData.value.map((item) => item.revenue),
      smooth: true,
      lineStyle: {
        color: '#1890ff',
        width: 3,
      },
      itemStyle: {
        color: '#1890ff',
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
            { offset: 1, color: 'rgba(24, 144, 255, 0.1)' },
          ],
        },
      },
    },
  ],
}))

// Build Top Products from ordersList
const buildTopProductsData = () => {
  const map: Record<string, { name: string; value: number; revenue: number }> = {}
  ordersList.value
    .filter((o: any) => isPaidOrder(o))
    .forEach((o: any) => {
      const items = Array.isArray(o.items) ? o.items : []
      items.forEach((it: any) => {
        const name = it?.tenSanPham ?? 'Không rõ'
        const qty = Number(it?.soLuong ?? 0)
        const lineRevenue = Number(it?.thanhTien ?? Number(it?.giaBan ?? 0) * qty)
        if (!map[name]) {
          map[name] = { name, value: 0, revenue: 0 }
        }
        map[name].value += Number.isNaN(qty) ? 0 : qty
        map[name].revenue += Number.isNaN(lineRevenue) ? 0 : lineRevenue
      })
    })

  topProductsData.value = Object.values(map)
    .sort((a, b) => b.value - a.value || b.revenue - a.revenue)
    .slice(0, 5)
}

watch([ordersList, productsList, customersList, revenuePeriod], () => {
  buildRevenueData()
  buildTopProductsData()
  buildRecentActivities()
})

const topProductsChartOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow',
    },
    formatter: (params: any) => {
      const data = params[0]
      const product = topProductsData.value[data.dataIndex]
      return `${data.axisValue}<br/>Số lượng: ${data.value}<br/>Doanh thu: ${formatCurrency(product.revenue)}`
    },
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true,
  },
  xAxis: {
    type: 'category',
    data: topProductsData.value.map((item) => item.name),
    axisLabel: {
      rotate: 45,
      interval: 0,
    },
  },
  yAxis: {
    type: 'value',
    name: 'Số lượng bán',
  },
  series: [
    {
      name: 'Số lượng bán',
      type: 'bar',
      data: topProductsData.value.map((item) => item.value),
      itemStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: '#52c41a' },
            { offset: 1, color: '#73d13d' },
          ],
        },
      },
    },
  ],
}))

const customerChartOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b}: {c} ({d}%)',
  },
  legend: {
    orient: 'vertical',
    left: 'left',
  },
  series: [
    {
      name: 'Phân loại khách hàng',
      type: 'pie',
      radius: '50%',
      center: ['50%', '50%'],
      data: customerData.value,
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)',
        },
      },
      label: {
        formatter: '{b}: {d}%',
      },
    },
  ],
}))

// Hàm cập nhật tổng kết bộ lọc
const updateFilterSummary = () => {
  switch (selectedTimeRange.value) {
    case 'today':
      totalOrdersCount.value = todayOrders.value
      totalRevenueAmount.value = todayRevenue.value
      break
    case 'week':
      totalOrdersCount.value = weekOrders.value
      totalRevenueAmount.value = weekRevenue.value
      break
    case 'month':
      totalOrdersCount.value = monthOrders.value
      totalRevenueAmount.value = monthRevenue.value
      break
    case 'year':
      totalOrdersCount.value = yearOrders.value
      totalRevenueAmount.value = yearRevenue.value
      break
    default:
      totalOrdersCount.value = 0
      totalRevenueAmount.value = 0
  }
}

// Hàm tính dữ liệu theo khoảng thời gian
const calculateTimePeriodData = (orders: any[]) => {
  const now = new Date()

  // Hôm nay
  const todayStr = now.toISOString().split('T')[0]
  const todayOrdersList = orders.filter((order: any) => {
    if (!order.ngayTao) return false
    const orderDate = new Date(order.ngayTao)
    const orderDateStr = orderDate.toISOString().split('T')[0]
    return orderDateStr === todayStr
  })

  // Tuần này (từ thứ 2 đến chủ nhật)
  const startOfWeek = new Date(now)
  startOfWeek.setDate(now.getDate() - now.getDay() + 1) // Thứ 2
  startOfWeek.setHours(0, 0, 0, 0)
  const endOfWeek = new Date(startOfWeek)
  endOfWeek.setDate(startOfWeek.getDate() + 6) // Chủ nhật
  endOfWeek.setHours(23, 59, 59, 999)

  const weekOrdersList = orders.filter((order: any) => {
    if (!order.ngayTao) return false
    const orderDate = new Date(order.ngayTao)
    return orderDate >= startOfWeek && orderDate <= endOfWeek
  })

  // Tháng này
  const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1)
  const endOfMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0)
  endOfMonth.setHours(23, 59, 59, 999)

  const monthOrdersList = orders.filter((order: any) => {
    if (!order.ngayTao) return false
    const orderDate = new Date(order.ngayTao)
    return orderDate >= startOfMonth && orderDate <= endOfMonth
  })

  // Năm này
  const startOfYear = new Date(now.getFullYear(), 0, 1)
  const endOfYear = new Date(now.getFullYear(), 11, 31)
  endOfYear.setHours(23, 59, 59, 999)

  const yearOrdersList = orders.filter((order: any) => {
    if (!order.ngayTao) return false
    const orderDate = new Date(order.ngayTao)
    return orderDate >= startOfYear && orderDate <= endOfYear
  })

  // Hàm tính sản phẩm đã bán
  const calculateProductsSold = (orderList: any[]) => {
    return orderList.reduce((total: number, order: any) => {
      if (order.chiTietHoaDon && Array.isArray(order.chiTietHoaDon)) {
        return (
          total +
          order.chiTietHoaDon.reduce((sum: number, detail: any) => {
            return sum + (Number(detail.soLuong) || 0)
          }, 0)
        )
      }
      return total
    }, 0)
  }

  // Hàm tính doanh thu
  const calculateRevenue = (orderList: any[]) => {
    return orderList
      .filter((order: any) => order.trangThai === true || order.ngayThanhToan)
      .reduce((total: number, order: any) => {
        const amount = Number(order.tongTienSauGiam ?? order.tongTien ?? 0)
        return total + (Number.isNaN(amount) ? 0 : amount)
      }, 0)
  }

  // Tính dữ liệu hôm nay
  todayOrders.value = todayOrdersList.length
  todayProductsSold.value = calculateProductsSold(todayOrdersList)
  todayRevenue.value = calculateRevenue(todayOrdersList)

  // Tính dữ liệu tuần này
  weekOrders.value = weekOrdersList.length
  weekProductsSold.value = calculateProductsSold(weekOrdersList)
  weekRevenue.value = calculateRevenue(weekOrdersList)

  // Tính dữ liệu tháng này
  monthOrders.value = monthOrdersList.length
  monthProductsSold.value = calculateProductsSold(monthOrdersList)
  monthRevenue.value = calculateRevenue(monthOrdersList)

  // Tính dữ liệu năm này
  yearOrders.value = yearOrdersList.length
  yearProductsSold.value = calculateProductsSold(yearOrdersList)
  yearRevenue.value = calculateRevenue(yearOrdersList)

  // Cập nhật tổng kết bộ lọc
  updateFilterSummary()
}

const fetchOrders = async () => {
  try {
    const res = await axios.get('/api/hoa-don-management/playlist')
    const orders = res.data ?? []
    ordersList.value = Array.isArray(orders) ? orders : []
    // Tính tổng doanh thu từ các hóa đơn đã thanh toán
    if (Array.isArray(orders)) {
      totalRevenue.value = orders
        .filter((order: any) => order.trangThai === true || order.ngayThanhToan)
        .reduce((sum: number, order: any) => {
          const amount = Number(order.tongTienSauGiam ?? order.tongTien ?? 0)
          return sum + (Number.isNaN(amount) ? 0 : amount)
        }, 0)
    } else {
      totalRevenue.value = 0
    }

    // Tính dữ liệu theo khoảng thời gian
    calculateTimePeriodData(orders)

    buildRevenueData()
    buildTopProductsData()
    buildRecentActivities()
  } catch {
    totalRevenue.value = 0
  }
}

// Hàm xử lý thay đổi khoảng thời gian
const onTimeRangeChange = (value: string) => {
  selectedTimeRange.value = value
  updateFilterSummary()
}

// Hàm xử lý thay đổi loại biểu đồ
const onChartTypeChange = (value: string) => {
  selectedChartType.value = value
  // Có thể thêm logic cập nhật biểu đồ ở đây
}

// Hàm đặt lại bộ lọc
const resetFilter = () => {
  selectedTimeRange.value = 'today'
  selectedChartType.value = 'line'
  updateFilterSummary()
}

// Hàm xuất báo cáo
const exportReport = () => {
  // Logic xuất báo cáo
  console.log('Exporting report...')
}

onMounted(() => {
  fetchOrders()
  buildRecentActivities()
  updateFilterSummary()
})
</script>

<style scoped>
.general-statistics {
  padding: 0 20px 20px 20px;
}

/* CSS cho bộ lọc thống kê */
.filter-section {
  margin-bottom: 24px;
}

.filter-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #e8e8e8;
}

.filter-header {
  padding: 20px 24px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.filter-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
}

.filter-icon {
  font-size: 20px;
  color: #1890ff;
}

.filter-content {
  padding: 24px;
}

/* Dòng 1: Labels */
.filter-row-1 {
  display: flex;
  gap: 32px;
  margin-bottom: 12px;
}

.filter-label {
  font-weight: 600;
  font-size: 14px;
  color: #1d2129;
  margin: 0;
  flex: 1;
}

/* Dòng 2: Controls */
.filter-row-2 {
  display: flex;
  gap: 32px;
  margin-bottom: 20px;
  align-items: center;
}

.filter-select {
  width: 200px;
  flex: 1;
}

/* Dòng 3: Summary và Actions */
.filter-row-3 {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.filter-summary {
  flex: 1;
}

.summary-text {
  font-size: 14px;
  color: #1d2129;
  font-weight: 500;
}

.filter-actions {
  display: flex;
  gap: 12px;
}

.reset-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #f5f5f5;
  border-color: #d9d9d9;
  color: #666;
}

.export-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #52c41a;
  border-color: #52c41a;
}

.action-icon {
  font-size: 14px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
}

.stat-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  font-size: 24px;
}

.today-icon {
  color: #1890ff;
}


.stat-content {
  margin-top: 16px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1d2129;
  margin-bottom: 8px;
}

.stat-change {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 500;
}

.stat-change.positive {
  color: #52c41a;
}

.stat-change.negative {
  color: #ff4d4f;
}

.stat-change.neutral {
  color: #86909c;
}

.today-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.today-title-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.today-icon-wrapper {
  width: 40px;
  height: 40px;
  background: #1890ff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.week-icon-wrapper {
  background: #52c41a;
}

.month-icon-wrapper {
  background: #fa8c16;
}

.year-icon-wrapper {
  background: #722ed1;
}

.today-icon {
  font-size: 20px;
  color: white;
}

.today-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
}

.today-revenue-section {
  text-align: right;
}

.today-revenue-value {
  font-size: 24px;
  font-weight: 700;
  color: #1d2129;
  margin-bottom: 4px;
}

.today-revenue-change {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  font-size: 14px;
  color: #86909c;
}

.change-icon {
  font-size: 12px;
}

.today-details {
  margin-top: 8px;
}

.today-detail-text {
  font-size: 14px;
  color: #86909c;
  font-weight: 500;
}

/* Animation cho các panel */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}


/* Animation cho từng panel */
.today-card:nth-child(1) {
  animation: fadeInUp 0.6s ease-out;
}

.today-card:nth-child(2) {
  animation: fadeInLeft 0.6s ease-out 0.1s both;
}

.today-card:nth-child(3) {
  animation: fadeInRight 0.6s ease-out 0.2s both;
}

.today-card:nth-child(4) {
  animation: scaleIn 0.6s ease-out 0.3s both;
}


/* Hiệu ứng cascade cho các phần tử bên trong */
.today-header {
  animation: fadeInUp 0.8s ease-out 0.4s both;
}

.today-details {
  animation: fadeInUp 0.8s ease-out 0.6s both;
}

/* Hiệu ứng cho icon */
.today-icon {
  animation: scaleIn 0.5s ease-out 0.5s both;
}

/* Hiệu ứng cho doanh thu */
.today-revenue-value {
  animation: fadeInRight 0.6s ease-out 0.7s both;
}

.today-revenue-change {
  animation: fadeInRight 0.6s ease-out 0.8s both;
}

/* Hiệu ứng cho title */
.today-title {
  animation: fadeInLeft 0.6s ease-out 0.5s both;
}

/* Hiệu ứng cho detail text */
.today-detail-text {
  animation: fadeInUp 0.6s ease-out 0.9s both;
}

.charts-section {
  margin-top: 16px;
}

.chart-card {
  height: 100%;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart {
  width: 100%;
  height: 100%;
}

.activity-item {
  margin-left: 8px;
}

.activity-title {
  font-weight: 500;
  color: #1d2129;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #86909c;
}

/* Responsive */
@media (max-width: 768px) {
  .general-statistics {
    padding: 16px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .page-title {
    font-size: 24px;
  }

  .stat-value {
    font-size: 28px;
  }

  .charts-section .ant-row {
    margin-top: 16px !important;
  }

  .charts-section .ant-col {
    margin-bottom: 16px;
  }
}
</style>
