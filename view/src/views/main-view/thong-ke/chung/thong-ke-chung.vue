<template>
  <div class="general-statistics">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Statistics Cards -->
    <div class="stats-grid">
      <a-card class="stat-card revenue-card" :bordered="false">
        <template #title>
          <div class="stat-header">
            <icon-star class="stat-icon revenue-icon" />
            <span>Tổng doanh thu</span>
          </div>
        </template>
        <div class="stat-content">
          <div class="stat-value">{{ formatCurrency(totalRevenue) }}</div>
          <div class="stat-change positive">
            <icon-arrow-up />
            +12.5% so với tháng trước
          </div>
        </div>
      </a-card>

      <a-card class="stat-card orders-card" :bordered="false">
        <template #title>
          <div class="stat-header">
            <icon-gift class="stat-icon orders-icon" />
            <span>Đơn hàng</span>
          </div>
        </template>
        <div class="stat-content">
          <div class="stat-value">{{ totalOrders }}</div>
          <div class="stat-change positive">
            <icon-arrow-up />
            +8.2% so với tháng trước
          </div>
        </div>
      </a-card>

      <a-card class="stat-card products-card" :bordered="false">
        <template #title>
          <div class="stat-header">
            <icon-archive class="stat-icon products-icon" />
            <span>Sản phẩm</span>
          </div>
        </template>
        <div class="stat-content">
          <div class="stat-value">{{ totalProducts }}</div>
          <div class="stat-change neutral">
            <icon-minus />
            Không thay đổi
          </div>
        </div>
      </a-card>

      <a-card class="stat-card customers-card" :bordered="false">
        <template #title>
          <div class="stat-header">
            <icon-user-group class="stat-icon customers-icon" />
            <span>Khách hàng</span>
          </div>
        </template>
        <div class="stat-content">
          <div class="stat-value">{{ totalCustomers }}</div>
          <div class="stat-change positive">
            <icon-arrow-up />
            +15.3% so với tháng trước
          </div>
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
              <a-timeline-item>
                <template #dot>
                  <icon-gift />
                </template>
                <div class="activity-item">
                  <div class="activity-title">Đơn hàng #12345 đã được tạo</div>
                  <div class="activity-time">2 phút trước</div>
                </div>
              </a-timeline-item>
              <a-timeline-item>
                <template #dot>
                  <icon-user />
                </template>
                <div class="activity-item">
                  <div class="activity-title">Khách hàng mới: Nguyễn Văn A</div>
                  <div class="activity-time">15 phút trước</div>
                </div>
              </a-timeline-item>
              <a-timeline-item>
                <template #dot>
                  <icon-check-circle />
                </template>
                <div class="activity-item">
                  <div class="activity-title">Thanh toán thành công 2.500.000 VNĐ</div>
                  <div class="activity-time">1 giờ trước</div>
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
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import {
  IconStar,
  IconGift,
  IconArchive,
  IconUserGroup,
  IconArrowUp,
  IconArrowDown,
  IconMinus,
  IconUser,
  IconCheckCircle,
} from '@arco-design/web-vue/es/icon'

// ECharts setup
use([CanvasRenderer, LineChart, BarChart, PieChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()

// Mock data - trong thực tế sẽ fetch từ API
const totalRevenue = ref(0)
const totalOrders = ref(0)
const totalProducts = ref(0)
const totalCustomers = ref(0)

const revenuePeriod = ref('6months')
const topProductsPeriod = ref('month')

// Chart data
const revenueData = ref([
  { month: 'Tháng 1', revenue: 45000000 },
  { month: 'Tháng 2', revenue: 52000000 },
  { month: 'Tháng 3', revenue: 48000000 },
  { month: 'Tháng 4', revenue: 61000000 },
  { month: 'Tháng 5', revenue: 58000000 },
  { month: 'Tháng 6', revenue: 72000000 },
])

const topProductsData = ref([
  { name: 'Giày sneaker Nike', value: 145, revenue: 43500000 },
  { name: 'Giày boot Chelsea', value: 98, revenue: 29400000 },
  { name: 'Giày cao gót Jimmy Choo', value: 76, revenue: 30400000 },
  { name: 'Giày thể thao Adidas', value: 89, revenue: 26700000 },
  { name: 'Giày sandal Birkenstock', value: 67, revenue: 13400000 },
])

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

const fetchProducts = async () => {
  try {
    const res = await axios.get('/api/san-pham-management/playlist')
    const products = res.data ?? []
    totalProducts.value = Array.isArray(products) ? products.length : 0
  } catch {
    totalProducts.value = 0
  }
}

const fetchOrders = async () => {
  try {
    const res = await axios.get('/api/hoa-don-management/playlist')
    const orders = res.data ?? []
    totalOrders.value = Array.isArray(orders) ? orders.length : 0
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
  } catch {
    totalOrders.value = 0
    totalRevenue.value = 0
  }
}

const fetchCustomers = async () => {
  try {
    const res = await axios.get('/api/khach-hang-management/playlist')
    const customers = res.data ?? []
    totalCustomers.value = Array.isArray(customers) ? customers.length : 0
  } catch {
    totalCustomers.value = 0
  }
}

onMounted(() => {
  fetchProducts()
  fetchOrders()
  fetchCustomers()
})
</script>

<style scoped>
.general-statistics {
  padding: 0 20px 20px 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
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

.revenue-icon {
  color: #52c41a;
}

.orders-icon {
  color: #1890ff;
}

.products-icon {
  color: #722ed1;
}

.customers-icon {
  color: #fa8c16;
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
