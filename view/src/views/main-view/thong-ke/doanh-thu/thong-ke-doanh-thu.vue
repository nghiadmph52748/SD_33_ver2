<template>
  <div class="revenue-statistics">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Revenue Overview Cards -->
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
            +{{ revenueGrowth }}% so với kỳ trước
          </div>
        </div>
      </a-card>

      <a-card class="stat-card orders-card" :bordered="false">
        <template #title>
          <div class="stat-header">
            <icon-gift class="stat-icon orders-icon" />
            <span>Tổng đơn hàng</span>
          </div>
        </template>
        <div class="stat-content">
          <div class="stat-value">{{ totalOrders }}</div>
          <div class="stat-change positive">
            <icon-arrow-up />
            +{{ ordersGrowth }}% so với kỳ trước
          </div>
        </div>
      </a-card>

      <a-card class="stat-card avg-order-card" :bordered="false">
        <template #title>
          <div class="stat-header">
            <icon-settings class="stat-icon avg-icon" />
            <span>Giá trị TB/đơn</span>
          </div>
        </template>
        <div class="stat-content">
          <div class="stat-value">{{ formatCurrency(avgOrderValue) }}</div>
          <div class="stat-change positive">
            <icon-arrow-up />
            +{{ avgOrderGrowth }}% so với kỳ trước
          </div>
        </div>
      </a-card>

      <a-card class="stat-card profit-card" :bordered="false">
        <template #title>
          <div class="stat-header">
            <icon-star class="stat-icon profit-icon" />
            <span>Lợi nhuận</span>
          </div>
        </template>
        <div class="stat-content">
          <div class="stat-value">{{ formatCurrency(totalProfit) }}</div>
          <div class="stat-change positive">
            <icon-arrow-up />
            +{{ profitGrowth }}% so với kỳ trước
          </div>
        </div>
      </a-card>
    </div>

    <!-- Charts Section -->
    <div class="charts-section">
      <a-row :gutter="16">
        <!-- Revenue Trend Chart -->
        <a-col :span="12">
          <a-card title="Xu hướng doanh thu" class="chart-card">
            <template #extra>
              <a-radio-group v-model="chartType" size="small" type="button">
                <a-radio value="line">Đường</a-radio>
                <a-radio value="bar">Cột</a-radio>
                <a-radio value="area">Khu vực</a-radio>
              </a-radio-group>
            </template>
            <div class="chart-container">
              <a-empty description="Biểu đồ xu hướng doanh thu sẽ hiển thị ở đây" />
              <!-- Placeholder for chart -->
              <div class="chart-placeholder">
                <div class="chart-bar" v-for="item in revenueData" :key="item.date">
                  <div class="bar-fill" :style="{ height: item.height + '%' }"></div>
                  <span class="bar-label">{{ item.date }}</span>
                </div>
              </div>
            </div>
          </a-card>
        </a-col>

        <!-- Revenue by Category -->
        <a-col :span="12">
          <a-card title="Doanh thu theo danh mục" class="chart-card">
            <div class="chart-container">
              <a-empty description="Biểu đồ danh mục sẽ hiển thị ở đây" />
              <!-- Category breakdown -->
              <div class="category-breakdown">
                <div class="category-item" v-for="category in categoryRevenue" :key="category.name">
                  <div class="category-info">
                    <span class="category-name">{{ category.name }}</span>
                    <span class="category-amount">{{ formatCurrency(category.amount) }}</span>
                  </div>
                  <a-progress :percent="category.percentage" :stroke-width="8" :show-text="false" :color="category.color" />
                </div>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px">
        <!-- Top Products -->
        <a-col :span="12">
          <a-card title="Sản phẩm bán chạy nhất" class="chart-card">
            <div class="top-products">
              <div class="product-item" v-for="(product, index) in topProducts" :key="product.id">
                <div class="product-rank">{{ index + 1 }}</div>
                <img :src="product.image" :alt="product.name" class="product-image" />
                <div class="product-info">
                  <div class="product-name">{{ product.name }}</div>
                  <div class="product-stats">
                    <span class="sold-count">{{ product.sold }} đã bán</span>
                    <span class="revenue">{{ formatCurrency(product.revenue) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </a-card>
        </a-col>

        <!-- Revenue by Payment Method -->
        <a-col :span="12">
          <a-card title="Doanh thu theo phương thức thanh toán" class="chart-card">
            <div class="payment-methods">
              <div class="payment-item" v-for="method in paymentMethods" :key="method.type">
                <div class="payment-info">
                  <span class="payment-name">{{ method.name }}</span>
                  <span class="payment-amount">{{ formatCurrency(method.amount) }}</span>
                </div>
                <a-progress :percent="method.percentage" :stroke-width="10" :show-text="false" :color="method.color" />
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- Revenue Comparison -->
      <a-card title="So sánh doanh thu theo thời gian" class="comparison-card" style="margin-top: 16px">
        <template #extra>
          <a-space>
            <a-select v-model="comparisonPeriod1" style="width: 120px">
              <a-option value="this-month">Tháng này</a-option>
              <a-option value="last-month">Tháng trước</a-option>
              <a-option value="this-year">Năm nay</a-option>
              <a-option value="last-year">Năm ngoái</a-option>
            </a-select>
            <span>vs</span>
            <a-select v-model="comparisonPeriod2" style="width: 120px">
              <a-option value="last-month">Tháng trước</a-option>
              <a-option value="this-month">Tháng này</a-option>
              <a-option value="last-year">Năm ngoái</a-option>
              <a-option value="this-year">Năm nay</a-option>
            </a-select>
          </a-space>
        </template>

        <div class="comparison-grid">
          <div class="comparison-item">
            <div class="comparison-label">Doanh thu</div>
            <div class="comparison-values">
              <div class="comparison-value current">{{ formatCurrency(period1Revenue) }}</div>
              <div class="comparison-value previous">{{ formatCurrency(period2Revenue) }}</div>
              <div class="comparison-change" :class="{ positive: revenueChange > 0, negative: revenueChange < 0 }">
                <icon-arrow-up v-if="revenueChange > 0" />
                <icon-arrow-down v-else-if="revenueChange < 0" />
                {{ Math.abs(revenueChange) }}%
              </div>
            </div>
          </div>

          <div class="comparison-item">
            <div class="comparison-label">Số đơn hàng</div>
            <div class="comparison-values">
              <div class="comparison-value current">{{ period1Orders }}</div>
              <div class="comparison-value previous">{{ period2Orders }}</div>
              <div class="comparison-change" :class="{ positive: ordersChange > 0, negative: ordersChange < 0 }">
                <icon-arrow-up v-if="ordersChange > 0" />
                <icon-arrow-down v-else-if="ordersChange < 0" />
                {{ Math.abs(ordersChange) }}%
              </div>
            </div>
          </div>

          <div class="comparison-item">
            <div class="comparison-label">Giá trị TB/đơn</div>
            <div class="comparison-values">
              <div class="comparison-value current">{{ formatCurrency(period1AvgOrder) }}</div>
              <div class="comparison-value previous">{{ formatCurrency(period2AvgOrder) }}</div>
              <div class="comparison-change" :class="{ positive: avgOrderChange > 0, negative: avgOrderChange < 0 }">
                <icon-arrow-up v-if="avgOrderChange > 0" />
                <icon-arrow-down v-else-if="avgOrderChange < 0" />
                {{ Math.abs(avgOrderChange) }}%
              </div>
            </div>
          </div>
        </div>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { IconStar, IconGift, IconSettings, IconArrowUp, IconArrowDown, IconDownload } from '@arco-design/web-vue/es/icon'
import axios from 'axios'

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()

// Reactive data
const timeRange = ref('30days')
const chartType = ref('line')
const comparisonPeriod1 = ref('this-month')
const comparisonPeriod2 = ref('last-month')

// Dữ liệu hóa đơn và tổng doanh thu tính từ API
const invoices = ref<any[]>([])
const totalRevenue = computed(() => {
  if (!invoices.value || invoices.value.length === 0) return 0
  return invoices.value
    .filter((invoice: any) => invoice.trangThai === true || invoice.ngayThanhToan)
    .reduce((sum: number, invoice: any) => {
      const amount = Number(invoice.tongTienSauGiam ?? invoice.tongTien ?? 0)
      return sum + (isNaN(amount) ? 0 : amount)
    }, 0)
})
const revenueGrowth = ref(12.5)
const totalOrders = ref(1250)
const ordersGrowth = ref(8.2)
const avgOrderValue = ref(196000) // 196k
const avgOrderGrowth = ref(4.1)
const totalProfit = ref(73500000) // 73.5 triệu
const profitGrowth = ref(15.3)

// Revenue data for chart
const revenueData = ref([
  { date: '01', height: 40 },
  { date: '05', height: 60 },
  { date: '10', height: 45 },
  { date: '15', height: 80 },
  { date: '20', height: 65 },
  { date: '25', height: 90 },
  { date: '30', height: 75 },
])

// Category revenue
const categoryRevenue = ref([
  { name: 'Giày sneaker', amount: 85000000, percentage: 35, color: '#1890ff' },
  { name: 'Giày boot', amount: 61250000, percentage: 25, color: '#52c41a' },
  { name: 'Giày cao gót', amount: 48900000, percentage: 20, color: '#fa8c16' },
  { name: 'Giày thể thao', amount: 36750000, percentage: 15, color: '#722ed1' },
  { name: 'Khác', amount: 12250000, percentage: 5, color: '#13c2c2' },
])

// Top products
const topProducts = ref([
  {
    id: 1,
    name: 'Giày sneaker Nike Air Max 270',
    sold: 145,
    revenue: 43500000,
    image: 'https://via.placeholder.com/60x60/1890ff/ffffff?text=Nike',
  },
  {
    id: 2,
    name: 'Giày boot Chelsea Dr. Martens',
    sold: 98,
    revenue: 29400000,
    image: 'https://via.placeholder.com/60x60/52c41a/ffffff?text=Dr.+Martens',
  },
  {
    id: 3,
    name: 'Giày cao gót Jimmy Choo',
    sold: 76,
    revenue: 30400000,
    image: 'https://via.placeholder.com/60x60/fa8c16/ffffff?text=Jimmy+Choo',
  },
  {
    id: 4,
    name: 'Giày thể thao Adidas Ultraboost',
    sold: 89,
    revenue: 26700000,
    image: 'https://via.placeholder.com/60x60/722ed1/ffffff?text=Adidas',
  },
  {
    id: 5,
    name: 'Giày sandal Birkenstock',
    sold: 67,
    revenue: 13400000,
    image: 'https://via.placeholder.com/60x60/13c2c2/ffffff?text=Birkenstock',
  },
])

// Payment methods
const paymentMethods = ref([
  { type: 'cash', name: 'Tiền mặt', amount: 122500000, percentage: 50, color: '#52c41a' },
  { type: 'card', name: 'Thẻ tín dụng', amount: 85750000, percentage: 35, color: '#1890ff' },
  { type: 'transfer', name: 'Chuyển khoản', amount: 36750000, percentage: 15, color: '#fa8c16' },
])

// Comparison data
const period1Revenue = ref(245000000)
const period2Revenue = ref(218000000)
const period1Orders = ref(1250)
const period2Orders = ref(1150)
const period1AvgOrder = ref(196000)
const period2AvgOrder = ref(190000)

// Computed
const revenueChange = computed(() => {
  return Math.round(((period1Revenue.value - period2Revenue.value) / period2Revenue.value) * 100)
})

const ordersChange = computed(() => {
  return Math.round(((period1Orders.value - period2Orders.value) / period2Orders.value) * 100)
})

const avgOrderChange = computed(() => {
  return Math.round(((period1AvgOrder.value - period2AvgOrder.value) / period2AvgOrder.value) * 100)
})

// Methods
const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const fetchInvoices = async () => {
  try {
    const res = await axios.get('/api/hoa-don-management/playlist')
    // interceptor trả về { success, message, data }
    invoices.value = res.data ?? []
  } catch (e) {
    invoices.value = []
  }
}

const exportReport = () => {
  // Implement export logic
}

onMounted(() => {
  fetchInvoices()
})
</script>

<style scoped>
.revenue-statistics {
  padding: 0 20px 20px 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
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

.avg-icon {
  color: #fa8c16;
}

.profit-icon {
  color: #722ed1;
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

.charts-section {
  margin-top: 16px;
}

.chart-card,
.comparison-card {
  height: 100%;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  height: 200px;
  width: 100%;
}

.chart-bar {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.bar-fill {
  width: 100%;
  background: linear-gradient(180deg, #1890ff 0%, #40a9ff 100%);
}

.bar-label {
  font-size: 12px;
  color: #86909c;
}

.category-breakdown {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.category-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.category-name {
  font-weight: 500;
  color: #1d2129;
}

.category-amount {
  font-size: 12px;
  color: #1890ff;
  font-weight: 500;
}

.top-products {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid #f0f0f0;
}

.product-rank {
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  color: white;

  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 12px;
}

.product-image {
  width: 48px;
  height: 48px;
  object-fit: cover;
}

.product-info {
  flex: 1;
}

.product-name {
  font-weight: 500;
  color: #1d2129;
  margin-bottom: 4px;
}

.product-stats {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #86909c;
}

.sold-count {
  color: #faad14;
}

.revenue {
  color: #52c41a;
  font-weight: 500;
}

.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.payment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.payment-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.payment-name {
  font-weight: 500;
  color: #1d2129;
}

.payment-amount {
  font-size: 12px;
  color: #1890ff;
  font-weight: 500;
}

.comparison-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.comparison-item {
  padding: 20px;
  background: #fafafa;
}

.comparison-label {
  font-weight: 500;
  color: #1d2129;
  margin-bottom: 16px;
}

.comparison-values {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.comparison-value {
  font-size: 18px;
  font-weight: 600;
}

.comparison-value.current {
  color: #1890ff;
}

.comparison-value.previous {
  color: #86909c;
  font-size: 16px;
  font-weight: 500;
}

.comparison-change {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 500;
  margin-top: 8px;
}

.comparison-change.positive {
  color: #52c41a;
}

.comparison-change.negative {
  color: #ff4d4f;
}

/* Responsive */
@media (max-width: 768px) {
  .revenue-statistics {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
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

  .comparison-grid {
    grid-template-columns: 1fr;
  }

  .product-item {
    padding: 8px;
  }

  .product-stats {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
