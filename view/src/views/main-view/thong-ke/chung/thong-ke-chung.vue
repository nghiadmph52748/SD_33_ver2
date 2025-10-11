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
            <div class="time-range-container">
              <a-select v-model="selectedTimeRange" class="filter-select" @change="onTimeRangeChange">
                <a-option value="today">Hôm nay</a-option>
                <a-option value="week">Tuần này</a-option>
                <a-option value="month">Tháng này</a-option>
                <a-option value="year">Năm này</a-option>
                <a-option value="custom">Tùy chọn</a-option>
              </a-select>

              <!-- DatePicker cho tùy chọn -->
              <div v-if="selectedTimeRange === 'custom'" class="custom-date-picker">
                <a-range-picker
                  v-model="customDateRange"
                  @change="onCustomDateChange"
                  format="DD/MM/YYYY"
                  placeholder="['Từ ngày', 'Đến ngày']"
                  style="width: 300px"
                />
              </div>
            </div>

            <div class="chart-type-buttons">
              <button :class="['chart-btn', { active: selectedChartType === 'line' }]" @click="onChartTypeChange('line')">
                <icon-line class="chart-icon" />
                Đường
              </button>
              <button :class="['chart-btn', { active: selectedChartType === 'bar' }]" @click="onChartTypeChange('bar')">
                <icon-bar class="chart-icon" />
                Cột
              </button>
            </div>
          </div>

          <!-- Dòng 3: Summary và Actions -->
          <div class="filter-row-3">
            <span class="summary-text">Số đơn hàng: {{ totalOrdersCount }} | Tổng doanh thu: {{ formatCurrency(totalRevenueAmount) }}</span>

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
      <!-- Panel hiển thị dữ liệu theo khoảng thời gian được chọn -->
      <a-card class="stat-card today-card" :bordered="false">
        <div class="today-header">
          <div class="today-title-section">
            <div class="today-icon-wrapper" :class="getIconClass(selectedTimeRange)">
              <icon-calendar class="today-icon" />
            </div>
            <div class="today-title">{{ displayData.title }}</div>
          </div>
          <div class="today-revenue-section">
            <div class="today-revenue-value">{{ formatCurrency(displayData.revenue) }}</div>
            <div class="today-revenue-change">
              <icon-arrow-down class="change-icon" />
              <span>0%</span>
            </div>
          </div>
        </div>
        <div class="today-details">
          <span class="today-detail-text">Sản phẩm đã bán: {{ displayData.productsSold }} | Đơn hàng: {{ displayData.orders }}</span>
        </div>
      </a-card>

      <!-- Panel cố định cho so sánh -->
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

      <!-- Panel cố định cho so sánh -->
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

      <!-- Panel cố định cho so sánh -->
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
        <a-col :span="17">
          <a-card class="chart-card">
            <template #title>
              <div class="chart-title">
                <span>Biểu Đồ Doanh Thu</span>
              </div>
            </template>
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

        <a-col :span="7">
          <a-card class="chart-card">
            <template #title>
              <div class="chart-title">
                <span>Sản Phẩm Bán Chạy</span>
              </div>
            </template>
            <template #extra>
              <a-select v-model="topProductsPeriod" style="width: 120px">
                <a-option value="week">Tuần này</a-option>
                <a-option value="month">Tháng này</a-option>
              </a-select>
            </template>
            <div class="top-products-list">
              <div v-for="(product, index) in topProductsData" :key="product.name" class="product-item">
                <div class="product-rank">
                  <span class="rank-number">{{ index + 1 }}</span>
                </div>
                <div class="product-info">
                  <div class="product-name">{{ product.name }}</div>
                  <div class="product-stats">
                    <span class="quantity">Số lượng: {{ product.value }}</span>
                    <span class="revenue">Doanh thu: {{ formatCurrency(product.revenue) }}</span>
                  </div>
                </div>
                <div class="product-badge">
                  <span class="badge">Hot</span>
                </div>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px">
        <a-col :span="8">
          <a-card class="chart-card">
            <template #title>
              <div class="chart-title">
                <span>Phân Bố Trạng Thái Đơn Hàng</span>
              </div>
            </template>
            <template #extra>
              <div class="order-status-buttons">
                <button :class="['status-btn', { active: selectedOrderPeriod === 'day' }]" @click="onOrderPeriodChange('day')">Ngày</button>
                <button :class="['status-btn', { active: selectedOrderPeriod === 'month' }]" @click="onOrderPeriodChange('month')">
                  Tháng
                </button>
                <button :class="['status-btn', { active: selectedOrderPeriod === 'year' }]" @click="onOrderPeriodChange('year')">
                  Năm
                </button>
              </div>
            </template>
            <div class="chart-container">
              <v-chart class="chart" :option="orderStatusChartOption" autoresize />
            </div>
          </a-card>
        </a-col>

        <a-col :span="8">
          <a-card class="chart-card">
            <template #title>
              <div class="chart-title">
                <span>Phân Phối Đa Kênh</span>
              </div>
            </template>
            <div class="chart-container">
              <v-chart class="chart" :option="channelDistributionChartOption" autoresize />
            </div>
          </a-card>
        </a-col>

        <a-col :span="8">
          <a-card class="chart-card">
            <template #title>
              <div class="chart-title">
                <span>Danh Mục Sản Phẩm Bán Chạy</span>
              </div>
            </template>
            <div class="chart-container">
              <v-chart class="chart" :option="categoryChartOption" autoresize />
            </div>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px">
        <a-col :span="24">
          <a-card class="chart-card">
            <template #title>
              <div class="chart-title">
                <span>Bảng Thống Kê Chi Tiết</span>
              </div>
            </template>
            <div class="table-container">
              <a-table :columns="detailTableColumns" :data="detailTableData" :pagination="false" :scroll="{ x: 800 }" class="detail-table">
                <template #thờiGian="{ record }">
                  <span class="time-cell">{{ record.thoiGian }}</span>
                </template>
                <template #doanhThu="{ record }">
                  <span class="revenue-cell">{{ formatCurrency(record.doanhThu) }}</span>
                </template>
                <template #soDonHang="{ record }">
                  <span class="order-count-cell">{{ record.soDonHang }}</span>
                </template>
                <template #giaTriTB="{ record }">
                  <span class="avg-value-cell">{{ formatCurrency(record.giaTriTB) }}</span>
                </template>
                <template #tangTruong="{ record }">
                  <span :class="['growth-cell', record.tangTruong >= 0 ? 'positive' : 'negative']">
                    {{ record.tangTruong >= 0 ? '+' : '' }}{{ record.tangTruong }}%
                  </span>
                </template>
                <template #trangThai="{ record }">
                  <a-tag :color="getStatusColor(record.trangThai)">
                    {{ record.trangThai }}
                  </a-tag>
                </template>
              </a-table>
            </div>
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
import { IconArrowDown, IconCalendar, IconFilter, IconRefresh, IconExport } from '@arco-design/web-vue/es/icon'

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
const customDateRange = ref<any[]>([])
const customProductsSold = ref(0)
const customOrders = ref(0)
const customRevenue = ref(0)

const revenuePeriod = ref('6months')
const topProductsPeriod = ref('month')

// Dữ liệu trạng thái đơn hàng
const selectedOrderPeriod = ref('day')
const orderStatusData = ref([
  { name: 'Chờ xác nhận', value: 15, color: '#faad14' },
  { name: 'Chờ giao hàng', value: 25, color: '#1890ff' },
  { name: 'Đang giao', value: 20, color: '#52c41a' },
  { name: 'Hoàn thành', value: 30, color: '#13c2c2' },
  { name: 'Đã hủy', value: 10, color: '#ff4d4f' },
])

// Dữ liệu phân phối đa kênh
const channelDistributionData = ref([
  { name: 'Online', value: 0, color: '#1890ff' },
  { name: 'Tại quầy', value: 0, color: '#52c41a' },
])

// Chart data
const revenueData = ref<{ month: string; revenue: number }[]>([])
const ordersList = ref<any[]>([])
const productsList = ref<any[]>([])
const customersList = ref<any[]>([])

const topProductsData = ref<{ name: string; value: number; revenue: number }[]>([])

const categoryData = ref<{ name: string; value: number; color: string }[]>([])

// Bảng thống kê chi tiết
const detailTableData = ref<
  {
    thoiGian: string
    doanhThu: number
    soDonHang: number
    giaTriTB: number
    tangTruong: number
    trangThai: string
  }[]
>([])

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

// Định nghĩa cột cho bảng thống kê chi tiết
const detailTableColumns = [
  {
    title: 'Thời gian',
    dataIndex: 'thoiGian',
    slotName: 'thờiGian',
    width: 120,
    align: 'center' as const,
  },
  {
    title: 'Doanh thu',
    dataIndex: 'doanhThu',
    slotName: 'doanhThu',
    width: 150,
    align: 'right' as const,
  },
  {
    title: 'Số đơn hàng',
    dataIndex: 'soDonHang',
    slotName: 'soDonHang',
    width: 120,
    align: 'center' as const,
  },
  {
    title: 'Giá trị TB/đơn',
    dataIndex: 'giaTriTB',
    slotName: 'giaTriTB',
    width: 150,
    align: 'right' as const,
  },
  {
    title: 'Tăng trưởng',
    dataIndex: 'tangTruong',
    slotName: 'tangTruong',
    width: 120,
    align: 'center' as const,
  },
  {
    title: 'Trạng thái',
    dataIndex: 'trangThai',
    slotName: 'trangThai',
    width: 120,
    align: 'center' as const,
  },
]



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
      type: selectedChartType.value,
      data: revenueData.value.map((item) => item.revenue),
      smooth: selectedChartType.value === 'line',
      lineStyle:
        selectedChartType.value === 'line'
          ? {
              color: '#1890ff',
              width: 3,
            }
          : undefined,
      itemStyle:
        selectedChartType.value === 'line'
          ? {
              color: '#1890ff',
            }
          : {
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
      areaStyle:
        selectedChartType.value === 'line'
          ? {
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
            }
          : undefined,
      emphasis:
        selectedChartType.value === 'bar'
          ? {
              itemStyle: {
                color: {
                  type: 'linear',
                  x: 0,
                  y: 0,
                  x2: 0,
                  y2: 1,
                  colorStops: [
                    { offset: 0, color: '#389e0d' },
                    { offset: 1, color: '#52c41a' },
                  ],
                },
              },
            }
          : undefined,
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

// Hàm cập nhật dữ liệu trạng thái đơn hàng theo period
const updateOrderStatusData = (period: string) => {
  if (!ordersList.value || ordersList.value.length === 0) {
    return
  }

  const now = new Date()
  let filteredOrders: any[] = []

  // Lọc đơn hàng theo period
  switch (period) {
    case 'day': {
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
      filteredOrders = ordersList.value.filter((order: any) => {
        const orderDate = new Date(order.ngayTao)
        return orderDate >= today && orderDate < new Date(today.getTime() + 24 * 60 * 60 * 1000)
      })
      break
    }
    case 'month': {
      const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1)
      const endOfMonth = new Date(now.getFullYear(), now.getMonth() + 1, 1)
      filteredOrders = ordersList.value.filter((order: any) => {
        const orderDate = new Date(order.ngayTao)
        return orderDate >= startOfMonth && orderDate < endOfMonth
      })
      break
    }
    case 'year': {
      const startOfYear = new Date(now.getFullYear(), 0, 1)
      const endOfYear = new Date(now.getFullYear() + 1, 0, 1)
      filteredOrders = ordersList.value.filter((order: any) => {
        const orderDate = new Date(order.ngayTao)
        return orderDate >= startOfYear && orderDate < endOfYear
      })
      break
    }
    default:
      filteredOrders = ordersList.value
  }

  // Phân loại trạng thái đơn hàng
  const statusCounts = {
    'Chờ xác nhận': 0,
    'Chờ giao hàng': 0,
    'Đang giao': 0,
    'Hoàn thành': 0,
    'Đã hủy': 0,
  }

  filteredOrders.forEach((order: any) => {
    // Logic phân loại trạng thái dựa trên dữ liệu hóa đơn
    if (order.deleted === true) {
      statusCounts['Đã hủy'] += 1
    } else if (order.ngayThanhToan && order.trangThai === true) {
      statusCounts['Hoàn thành'] += 1
    } else if (order.trangThai === true && !order.ngayThanhToan) {
      statusCounts['Chờ giao hàng'] += 1
    } else if (order.trangThai === false) {
      statusCounts['Chờ xác nhận'] += 1
    } else {
      statusCounts['Đang giao'] += 1
    }
  })

  // Cập nhật dữ liệu biểu đồ
  orderStatusData.value = [
    { name: 'Chờ xác nhận', value: statusCounts['Chờ xác nhận'], color: '#faad14' },
    { name: 'Chờ giao hàng', value: statusCounts['Chờ giao hàng'], color: '#1890ff' },
    { name: 'Đang giao', value: statusCounts['Đang giao'], color: '#52c41a' },
    { name: 'Hoàn thành', value: statusCounts['Hoàn thành'], color: '#13c2c2' },
    { name: 'Đã hủy', value: statusCounts['Đã hủy'], color: '#ff4d4f' },
  ]
}

// Hàm cập nhật dữ liệu phân phối đa kênh
const updateChannelDistributionData = () => {
  if (!ordersList.value || ordersList.value.length === 0) {
    return
  }

  let onlineCount = 0
  let offlineCount = 0

  ordersList.value.forEach((order: any) => {
    // Phân loại dựa trên loaiDon: true = Online, false = Tại quầy
    if (order.loaiDon === true) {
      onlineCount += 1
    } else {
      offlineCount += 1
    }
  })

  // Cập nhật dữ liệu biểu đồ
  channelDistributionData.value = [
    { name: 'Online', value: onlineCount, color: '#1890ff' },
    { name: 'Tại quầy', value: offlineCount, color: '#52c41a' },
  ]
}

// Hàm phân loại sản phẩm dựa trên tên
const categorizeProduct = (productName: string): string => {
  const name = productName.toLowerCase()

  // Danh mục giày thể thao
  if (
    name.includes('sneaker') ||
    name.includes('giày thể thao') ||
    name.includes('running') ||
    name.includes('basketball') ||
    name.includes('tennis') ||
    name.includes('jogging')
  ) {
    return 'Giày Thể Thao'
  }

  // Danh mục giày chạy bộ
  if (
    name.includes('chạy') ||
    name.includes('running') ||
    name.includes('marathon') ||
    name.includes('jogging') ||
    name.includes('track')
  ) {
    return 'Giày Chạy Bộ'
  }

  // Danh mục giày bóng đá
  if (
    name.includes('bóng đá') ||
    name.includes('football') ||
    name.includes('soccer') ||
    name.includes('cleats') ||
    name.includes('futsal')
  ) {
    return 'Giày Bóng Đá'
  }

  // Danh mục giày bóng rổ
  if (name.includes('bóng rổ') || name.includes('basketball') || name.includes('hoops')) {
    return 'Giày Bóng Rổ'
  }

  // Danh mục giày tennis
  if (name.includes('tennis') || name.includes('quần vợt')) {
    return 'Giày Tennis'
  }

  // Danh mục giày lifestyle
  if (
    name.includes('lifestyle') ||
    name.includes('casual') ||
    name.includes('street') ||
    name.includes('fashion') ||
    name.includes('thời trang')
  ) {
    return 'Giày Lifestyle'
  }

  // Danh mục giày cao gót
  if (name.includes('cao gót') || name.includes('heels') || name.includes('pumps')) {
    return 'Giày Cao Gót'
  }

  // Danh mục giày boot
  if (name.includes('boot') || name.includes('bốt') || name.includes('ankle')) {
    return 'Giày Boot'
  }

  // Danh mục giày sandal
  if (name.includes('sandal') || name.includes('dép') || name.includes('flip-flop')) {
    return 'Giày Sandal'
  }

  // Danh mục khác
  return 'Danh Mục Khác'
}

// Hàm cập nhật dữ liệu danh mục sản phẩm bán chạy
const updateCategoryData = () => {
  if (!ordersList.value || ordersList.value.length === 0) {
    return
  }

  // Tạo map để đếm số lượng sản phẩm theo danh mục từ hóa đơn
  const categoryCount = new Map<string, number>()

  ordersList.value.forEach((order: any) => {
    // Kiểm tra nếu hóa đơn có chi tiết sản phẩm
    if (order.hoaDonChiTiets && Array.isArray(order.hoaDonChiTiets)) {
      order.hoaDonChiTiets.forEach((detail: any) => {
        // Lấy thông tin sản phẩm từ chi tiết hóa đơn
        if (detail.idChiTietSanPham && detail.idChiTietSanPham.idSanPham && detail.idChiTietSanPham.idSanPham.tenSanPham) {
          const productName = detail.idChiTietSanPham.idSanPham.tenSanPham
          if (productName) {
            // Phân loại sản phẩm dựa trên từ khóa trong tên
            const category = categorizeProduct(productName)
            const quantity = detail.soLuong || 0
            categoryCount.set(category, (categoryCount.get(category) || 0) + quantity)
          }
        }
      })
    }
  })

  // Chuyển đổi thành mảng và sắp xếp theo số lượng giảm dần
  const categoryArray = Array.from(categoryCount.entries())
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 6) // Lấy top 6 danh mục

  // Màu sắc cho các danh mục
  const colors = ['#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1', '#13c2c2']

  categoryData.value = categoryArray.map((category, index) => ({
    name: category.name,
    value: category.value,
    color: colors[index % colors.length],
  }))
}

// Hàm lấy dữ liệu hôm nay
const getTodayData = () => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const tomorrow = new Date(today)
  tomorrow.setDate(tomorrow.getDate() + 1)

  const todayOrdersFiltered = ordersList.value.filter((order: any) => {
    if (!order.ngayTao) return false
    const orderDate = new Date(order.ngayTao)
    return orderDate >= today && orderDate < tomorrow
  })

  const revenue = todayOrdersFiltered
    .filter((order: any) => order.trangThai === true || order.ngayThanhToan)
    .reduce((sum: number, order: any) => {
      const amount = Number(order.tongTienSauGiam ?? order.tongTien ?? 0)
      return sum + (Number.isNaN(amount) ? 0 : amount)
    }, 0)

  return {
    orders: todayOrdersFiltered,
    revenue,
    orderCount: todayOrdersFiltered.length,
  }
}

// Hàm lấy dữ liệu tuần này
const getWeekData = () => {
  const now = new Date()
  const startOfWeek = new Date(now)
  startOfWeek.setDate(now.getDate() - now.getDay())
  startOfWeek.setHours(0, 0, 0, 0)
  const endOfWeek = new Date(startOfWeek)
  endOfWeek.setDate(startOfWeek.getDate() + 7)

  const weekOrdersFiltered = ordersList.value.filter((order: any) => {
    if (!order.ngayTao) return false
    const orderDate = new Date(order.ngayTao)
    return orderDate >= startOfWeek && orderDate < endOfWeek
  })

  const revenue = weekOrdersFiltered
    .filter((order: any) => order.trangThai === true || order.ngayThanhToan)
    .reduce((sum: number, order: any) => {
      const amount = Number(order.tongTienSauGiam ?? order.tongTien ?? 0)
      return sum + (Number.isNaN(amount) ? 0 : amount)
    }, 0)

  return {
    orders: weekOrdersFiltered,
    revenue,
    orderCount: weekOrdersFiltered.length,
  }
}

// Hàm lấy dữ liệu tháng này
const getMonthData = () => {
  const now = new Date()
  const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1)
  const endOfMonth = new Date(now.getFullYear(), now.getMonth() + 1, 1)

  const monthOrdersFiltered = ordersList.value.filter((order: any) => {
    if (!order.ngayTao) return false
    const orderDate = new Date(order.ngayTao)
    return orderDate >= startOfMonth && orderDate < endOfMonth
  })

  const revenue = monthOrdersFiltered
    .filter((order: any) => order.trangThai === true || order.ngayThanhToan)
    .reduce((sum: number, order: any) => {
      const amount = Number(order.tongTienSauGiam ?? order.tongTien ?? 0)
      return sum + (Number.isNaN(amount) ? 0 : amount)
    }, 0)

  return {
    orders: monthOrdersFiltered,
    revenue,
    orderCount: monthOrdersFiltered.length,
  }
}

// Hàm lấy dữ liệu năm này
const getYearData = () => {
  const now = new Date()
  const startOfYear = new Date(now.getFullYear(), 0, 1)
  const endOfYear = new Date(now.getFullYear() + 1, 0, 1)

  const yearOrdersFiltered = ordersList.value.filter((order: any) => {
    if (!order.ngayTao) return false
    const orderDate = new Date(order.ngayTao)
    return orderDate >= startOfYear && orderDate < endOfYear
  })

  const revenue = yearOrdersFiltered
    .filter((order: any) => order.trangThai === true || order.ngayThanhToan)
    .reduce((sum: number, order: any) => {
      const amount = Number(order.tongTienSauGiam ?? order.tongTien ?? 0)
      return sum + (Number.isNaN(amount) ? 0 : amount)
    }, 0)

  return {
    orders: yearOrdersFiltered,
    revenue,
    orderCount: yearOrdersFiltered.length,
  }
}

// Hàm cập nhật dữ liệu bảng thống kê chi tiết
const updateDetailTableData = () => {
  if (!ordersList.value || ordersList.value.length === 0) {
    detailTableData.value = []
    return
  }

  const timePeriods = [
    { key: 'today', label: 'Hôm nay', getData: () => getTodayData() },
    { key: 'week', label: 'Tuần này', getData: () => getWeekData() },
    { key: 'month', label: 'Tháng này', getData: () => getMonthData() },
    { key: 'year', label: 'Năm này', getData: () => getYearData() },
  ]

  const tableData = timePeriods.map((period) => {
    const data = period.getData()
    const avgValue = data.orderCount > 0 ? data.revenue / data.orderCount : 0

    return {
      thoiGian: period.label,
      doanhThu: data.revenue,
      soDonHang: data.orderCount,
      giaTriTB: avgValue,
      tangTruong: 0, // Sẽ tính sau
      trangThai: data.orderCount > 0 ? 'Hoạt động' : 'Không hoạt động',
    }
  })

  // Tính tăng trưởng so với khoảng thời gian trước
  for (let i = 0; i < tableData.length; i += 1) {
    if (i === 0) {
      tableData[i].tangTruong = 0
    } else {
      const prevRevenue = tableData[i - 1].doanhThu
      const currentRevenue = tableData[i].doanhThu
      if (prevRevenue > 0) {
        tableData[i].tangTruong = Math.round(((currentRevenue - prevRevenue) / prevRevenue) * 100 * 100) / 100
      } else {
        tableData[i].tangTruong = currentRevenue > 0 ? 100 : 0
      }
    }
  }

  detailTableData.value = tableData
}


// Hàm lấy màu cho trạng thái
const getStatusColor = (status: string): string => {
  switch (status) {
    case 'Hoạt động':
      return 'green'
    case 'Không hoạt động':
      return 'red'
    case 'Có đơn hàng nhưng chưa thanh toán':
      return 'orange'
    case 'Tạm dừng':
      return 'orange'
    default:
      return 'blue'
  }
}

watch([ordersList, productsList, customersList, revenuePeriod], () => {
  buildRevenueData()
  buildTopProductsData()
  updateOrderStatusData(selectedOrderPeriod.value)
  updateChannelDistributionData()
  updateCategoryData()
  updateDetailTableData()
})

// Order status chart option
const orderStatusChartOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b}: {c} ({d}%)',
  },
  legend: {
    orient: 'vertical',
    left: 'left',
    top: 'center',
  },
  series: [
    {
      name: 'Trạng thái đơn hàng',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['60%', '50%'],
      data: orderStatusData.value,
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)',
        },
      },
      label: {
        formatter: '{b}: {d}%',
        fontSize: 12,
      },
      labelLine: {
        show: true,
        length: 10,
        length2: 5,
      },
    },
  ],
}))

// Hàm xử lý thay đổi period cho trạng thái đơn hàng
const onOrderPeriodChange = (period: string) => {
  selectedOrderPeriod.value = period
  // Có thể thêm logic cập nhật dữ liệu theo period ở đây
  updateOrderStatusData(period)
}

// Channel distribution chart option
const channelDistributionChartOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b}: {c} ({d}%)',
  },
  legend: {
    orient: 'vertical',
    left: 'left',
    top: 'center',
  },
  series: [
    {
      name: 'Phân phối đa kênh',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['60%', '50%'],
      data: channelDistributionData.value,
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)',
        },
      },
      label: {
        formatter: '{b}: {d}%',
        fontSize: 12,
      },
      labelLine: {
        show: true,
        length: 10,
        length2: 5,
      },
    },
  ],
}))

// Category chart option
const categoryChartOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b}: {c} ({d}%)',
  },
  legend: {
    orient: 'vertical',
    left: 'left',
    top: 'center',
  },
  series: [
    {
      name: 'Danh mục sản phẩm bán chạy',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['60%', '50%'],
      data: categoryData.value,
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)',
        },
      },
      label: {
        formatter: '{b}: {d}%',
        fontSize: 12,
      },
      labelLine: {
        show: true,
        length: 10,
        length2: 5,
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
    case 'custom':
      totalOrdersCount.value = customOrders.value
      totalRevenueAmount.value = customRevenue.value
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
  } catch {
    totalRevenue.value = 0
  }
}

const fetchProducts = async () => {
  try {
    const res = await axios.get('/api/san-pham-management/playlist')
    const products = res.data ?? []
    productsList.value = Array.isArray(products) ? products : []
  } catch {
    productsList.value = []
  }
}

// Computed properties cho dữ liệu hiển thị theo khoảng thời gian được chọn
const displayData = computed(() => {
  switch (selectedTimeRange.value) {
    case 'today':
      return {
        productsSold: todayProductsSold.value,
        orders: todayOrders.value,
        revenue: todayRevenue.value,
        title: 'Hôm nay',
      }
    case 'week':
      return {
        productsSold: weekProductsSold.value,
        orders: weekOrders.value,
        revenue: weekRevenue.value,
        title: 'Tuần này',
      }
    case 'month':
      return {
        productsSold: monthProductsSold.value,
        orders: monthOrders.value,
        revenue: monthRevenue.value,
        title: 'Tháng này',
      }
    case 'year':
      return {
        productsSold: yearProductsSold.value,
        orders: yearOrders.value,
        revenue: yearRevenue.value,
        title: 'Năm này',
      }
    case 'custom':
      return {
        productsSold: customProductsSold.value,
        orders: customOrders.value,
        revenue: customRevenue.value,
        title: 'Tùy chọn',
      }
    default:
      return {
        productsSold: todayProductsSold.value,
        orders: todayOrders.value,
        revenue: todayRevenue.value,
        title: 'Hôm nay',
      }
  }
})

// Hàm lấy class icon theo khoảng thời gian
const getIconClass = (timeRange: string) => {
  switch (timeRange) {
    case 'today':
      return 'today-icon-wrapper'
    case 'week':
      return 'week-icon-wrapper'
    case 'month':
      return 'month-icon-wrapper'
    case 'year':
      return 'year-icon-wrapper'
    default:
      return 'today-icon-wrapper'
  }
}

// Hàm xử lý thay đổi khoảng thời gian
const onTimeRangeChange = (value: string) => {
  selectedTimeRange.value = value
  updateFilterSummary()
}

// Hàm tính toán dữ liệu cho custom date range
const calculateCustomData = () => {
  if (!customDateRange.value || customDateRange.value.length !== 2) {
    customProductsSold.value = 0
    customOrders.value = 0
    customRevenue.value = 0
    return
  }

  const startDate = new Date(customDateRange.value[0])
  const endDate = new Date(customDateRange.value[1])
  endDate.setHours(23, 59, 59, 999) // Đến cuối ngày

  // Lọc đơn hàng trong khoảng thời gian tùy chọn
  const customOrdersList = ordersList.value.filter((order: any) => {
    if (!order.ngayTao) return false
    const orderDate = new Date(order.ngayTao)
    return orderDate >= startDate && orderDate <= endDate
  })

  // Tính dữ liệu
  customOrders.value = customOrdersList.length
  customProductsSold.value = customOrdersList.reduce((total: number, order: any) => {
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

  customRevenue.value = customOrdersList
    .filter((order: any) => order.trangThai === true || order.ngayThanhToan)
    .reduce((total: number, order: any) => {
      const amount = Number(order.tongTienSauGiam ?? order.tongTien ?? 0)
      return total + (Number.isNaN(amount) ? 0 : amount)
    }, 0)

  updateFilterSummary()
}

// Hàm xử lý khi chọn custom date range
const onCustomDateChange = (dates: any[]) => {
  if (dates && dates.length === 2) {
    customDateRange.value = dates
    calculateCustomData()
  }
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
  // TODO: Implement export functionality
}

onMounted(() => {
  fetchOrders()
  fetchProducts()
  updateFilterSummary()
  updateOrderStatusData(selectedOrderPeriod.value)
  updateChannelDistributionData()
  updateCategoryData()
  updateDetailTableData()
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
  border: 1px solid #f0f0f0;
}

.filter-header {
  padding: 20px 24px 16px;
}

.filter-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
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
  gap: 40px;
  margin-bottom: 16px;
}

.filter-label {
  font-weight: 600;
  font-size: 14px;
  color: #333333;
  margin: 0;
  flex: 1;
}

/* Dòng 2: Controls */
.filter-row-2 {
  display: flex;
  gap: 40px;
  margin-bottom: 20px;
  align-items: flex-start;
}

.time-range-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.filter-select {
  width: 200px;
  height: 40px;
  border-radius: 8px;
  border: 1px solid #d9d9d9;
  font-size: 14px;
}

.custom-date-picker {
  margin-top: 8px;
}

.custom-date-picker .arco-picker {
  width: 300px;
  height: 40px;
  border-radius: 8px;
  border: 1px solid #d9d9d9;
}

/* Chart type buttons */
.chart-type-buttons {
  display: flex;
  gap: 12px;
  flex: 1;
  justify-content: flex-start;
}

.chart-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid #d9d9d9;
  background: white;
  color: #666;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  height: 40px;
  min-width: 80px;
  justify-content: center;
}

.chart-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
  background: #f0f8ff;
}

.chart-btn.active {
  background: #1890ff;
  border-color: #1890ff;
  color: white;
}

.chart-btn.active:hover {
  background: #40a9ff;
  border-color: #40a9ff;
}

.chart-icon {
  font-size: 18px;
  font-weight: 600;
}

/* Dòng 3: Summary và Actions */
.filter-row-3 {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
}

.summary-text {
  font-size: 14px;
  color: #666666;
  font-weight: 500;
  line-height: 1.4;
}

.filter-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

.reset-btn {
  background: #8c8c8c;
  border-color: #8c8c8c;
  color: white;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  height: 40px;
  min-width: 140px;
  justify-content: center;
}

.reset-btn:hover {
  background: #737373;
  border-color: #737373;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.export-btn {
  background: #1890ff;
  border-color: #1890ff;
  color: white;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  height: 40px;
  min-width: 140px;
  justify-content: center;
}

.export-btn:hover {
  background: #40a9ff;
  border-color: #40a9ff;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(24, 144, 255, 0.3);
}

.action-icon {
  font-size: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
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

/* CSS cho bảng thống kê chi tiết */
.table-container {
  margin-top: 16px;
}

.detail-table {
  border-radius: 8px;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.detail-table :deep(.arco-table-thead) {
  background-color: #f5f5f5;
}

.detail-table :deep(.arco-table-thead .arco-table-th) {
  background-color: #f5f5f5;
  font-weight: 600;
  font-size: 14px;
  color: #1d2129;
  border-bottom: 2px solid #e8e8e8;
  padding: 12px 16px;
}

.detail-table :deep(.arco-table-tbody .arco-table-tr) {
  transition: background-color 0.2s ease;
}

.detail-table :deep(.arco-table-tbody .arco-table-tr:hover) {
  background-color: #f8f9fa;
}

.detail-table :deep(.arco-table-td) {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
  color: #1d2129;
}

.time-cell {
  font-weight: 500;
  font-size: 14px;
  color: #000000;
}

.revenue-cell {
  font-weight: 600;
  font-size: 14px;
  color: #000000;
}

.order-count-cell {
  font-weight: 500;
  font-size: 14px;
  color: #000000;
}

.avg-value-cell {
  font-weight: 500;
  font-size: 14px;
  color: #000000;
}

.growth-cell {
  font-weight: 600;
  font-size: 14px;
  padding: 4px 8px;
  border-radius: 4px;
  display: inline-block;
  min-width: 60px;
  text-align: center;
  color: #000000;
}

.growth-cell.positive {
  background-color: #f6ffed;
  color: #000000;
  border: 1px solid #b7eb8f;
}

.growth-cell.negative {
  background-color: #fff2f0;
  color: #000000;
  border: 1px solid #ffccc7;
}
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

.chart-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
}

.chart-title-icon {
  font-size: 20px;
  color: #1890ff;
}

.chart-title-icon:first-child {
  color: #1890ff;
}

.chart-title-icon:last-child {
  color: #52c41a;
  margin-left: 4px;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* CSS cho danh sách sản phẩm bán chạy */
.top-products-list {
  max-height: 300px;
  overflow-y: auto;
  padding: 8px 0;
}

.product-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 8px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

.product-item:hover {
  background: #f0f8ff;
  border-color: #1890ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
}

.product-item:last-child {
  margin-bottom: 0;
}

.product-rank {
  margin-right: 16px;
  flex-shrink: 0;
}

.rank-number {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: #1890ff;
  color: white;
  border-radius: 50%;
  font-weight: 700;
  font-size: 14px;
}

.product-item:nth-child(1) .rank-number {
  background: #ff4d4f;
}

.product-item:nth-child(2) .rank-number {
  background: #fa8c16;
}

.product-item:nth-child(3) .rank-number {
  background: #52c41a;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #666;
}

.quantity {
  color: #52c41a;
  font-weight: 500;
}

.revenue {
  color: #1890ff;
  font-weight: 500;
}

.product-badge {
  margin-left: 12px;
  flex-shrink: 0;
}

.badge {
  display: inline-block;
  padding: 4px 8px;
  background: #ff4d4f;
  color: white;
  border-radius: 12px;
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* CSS cho buttons trạng thái đơn hàng */
.order-status-buttons {
  display: flex;
  gap: 8px;
}

.status-btn {
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  background: white;
  color: #666;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 50px;
  text-align: center;
}

.status-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
  background: #f0f8ff;
}

.status-btn.active {
  background: #1890ff;
  border-color: #1890ff;
  color: white;
}

.status-btn.active:hover {
  background: #40a9ff;
  border-color: #40a9ff;
}

.chart {
  width: 100%;
  height: 100%;
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
