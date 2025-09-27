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
              <a-empty description="Biểu đồ doanh thu sẽ hiển thị ở đây" />
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
              <a-empty description="Top sản phẩm sẽ hiển thị ở đây" />
            </div>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px">
        <a-col :span="12">
          <a-card title="Phân loại khách hàng" class="chart-card">
            <div class="chart-container">
              <a-empty description="Phân loại khách hàng sẽ hiển thị ở đây" />
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

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()

// Mock data - trong thực tế sẽ fetch từ API
const totalRevenue = ref(125000000) // 125 triệu
const totalOrders = ref(1250)
const totalProducts = ref(450)
const totalCustomers = ref(3200)

const revenuePeriod = ref('6months')
const topProductsPeriod = ref('month')

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

onMounted(() => {
  // Trong thực tế sẽ gọi API để fetch data
  // Load data here
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
