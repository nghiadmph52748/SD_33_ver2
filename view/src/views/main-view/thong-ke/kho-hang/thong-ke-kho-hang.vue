<template>
  <div class="inventory-statistics">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Inventory Overview Cards -->
    <div class="stats-grid">
      <a-card class="stat-card total-stock-card" :bordered="false">
        <template #title>
          <div class="stat-header">
            <icon-archive class="stat-icon total-icon" />
            <span>Tổng sản phẩm</span>
          </div>
        </template>
        <div class="stat-content">
          <div class="stat-value">{{ totalStock }}</div>
          <div class="stat-change neutral">
            <icon-minus />
            Tổng số sản phẩm trong kho
          </div>
        </div>
      </a-card>

      <a-card class="stat-card low-stock-card" :bordered="false">
        <template #title>
          <div class="stat-header">
            <icon-exclamation-circle class="stat-icon low-icon" />
            <span>Sắp hết hàng</span>
          </div>
        </template>
        <div class="stat-content">
          <div class="stat-value">{{ lowStockCount }}</div>
          <div class="stat-change warning">
            <icon-arrow-down />
            Cần nhập thêm
          </div>
        </div>
      </a-card>

      <a-card class="stat-card out-stock-card" :bordered="false">
        <template #title>
          <div class="stat-header">
            <icon-close-circle class="stat-icon out-icon" />
            <span>Hết hàng</span>
          </div>
        </template>
        <div class="stat-content">
          <div class="stat-value">{{ outOfStockCount }}</div>
          <div class="stat-change negative">
            <icon-arrow-down />
            Cần nhập khẩn cấp
          </div>
        </div>
      </a-card>

      <a-card class="stat-card stock-value-card" :bordered="false">
        <template #title>
          <div class="stat-header">
            <icon-star class="stat-icon value-icon" />
            <span>Giá trị tồn kho</span>
          </div>
        </template>
        <div class="stat-content">
          <div class="stat-value">{{ formatCurrency(stockValue) }}</div>
          <div class="stat-change positive">
            <icon-arrow-up />
            +5.2% so với tháng trước
          </div>
        </div>
      </a-card>
    </div>

    <!-- Main Content -->
    <div class="content-section">
      <a-row :gutter="16">
        <!-- Low Stock Alert -->
        <a-col :span="12">
          <a-card title="Sản phẩm sắp hết hàng" class="alert-card">
            <template #extra>
              <a-button type="primary" size="small">
                <template #icon>
                  <icon-plus />
                </template>
                Nhập hàng
              </a-button>
            </template>

            <a-table :columns="lowStockColumns" :data="lowStockProducts" :pagination="false" size="small" :scroll="{ y: 300 }">
              <template #product_name="{ record }">
                <div class="product-cell">
                  <a-avatar :src="record.image" :alt="record.name" :size="32" />
                  <span>{{ record.name }}</span>
                </div>
              </template>

              <template #stock_status="{ record }">
                <a-tag :color="getStockStatusColor(record.stock)">{{ record.stock }} {{ record.unit }}</a-tag>
              </template>

              <template #action="{ record }">
                <a-button type="text" size="small">
                  <template #icon>
                    <icon-edit />
                  </template>
                  Cập nhật
                </a-button>
              </template>
            </a-table>
          </a-card>
        </a-col>

        <!-- Stock Analysis -->
        <a-col :span="12">
          <a-card title="Phân tích tồn kho theo danh mục" class="analysis-card">
            <div class="chart-container">
              <a-empty description="Biểu đồ phân tích sẽ hiển thị ở đây" />
            </div>

            <!-- Category Summary -->
            <div class="category-summary">
              <div class="summary-item" v-for="category in categoryStock" :key="category.name">
                <div class="category-info">
                  <span class="category-name">{{ category.name }}</span>
                  <span class="category-count">{{ category.count }} sản phẩm</span>
                </div>
                <a-progress :percent="category.percentage" :stroke-width="8" :show-text="false" :color="category.color" />
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- Stock Movement -->
      <a-card title="Lịch sử nhập xuất kho" class="movement-card" style="margin-top: 16px">
        <template #extra>
          <a-space>
            <a-select v-model="movementFilter" style="width: 120px">
              <a-option value="all">Tất cả</a-option>
              <a-option value="in">Nhập hàng</a-option>
              <a-option value="out">Xuất hàng</a-option>
            </a-select>
            <a-range-picker v-model="movementDateRange" style="width: 240px" />
          </a-space>
        </template>

        <a-table :columns="movementColumns" :data="stockMovements" :pagination="{ pageSize: 10 }" size="middle">
          <template #type="{ record }">
            <a-tag :color="record.type === 'in' ? 'green' : 'blue'">
              {{ record.type === 'in' ? 'Nhập' : 'Xuất' }}
            </a-tag>
          </template>

          <template #product="{ record }">
            <div class="product-cell">
              <span>{{ record.productName }}</span>
            </div>
          </template>

          <template #quantity="{ record }">
            <span :class="record.type === 'in' ? 'quantity-in' : 'quantity-out'">
              {{ record.type === 'in' ? '+' : '-' }}{{ record.quantity }}
            </span>
          </template>
        </a-table>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import {
  IconArchive,
  IconExclamationCircle,
  IconCloseCircle,
  IconStar,
  IconMinus,
  IconArrowDown,
  IconArrowUp,
  IconPlus,
  IconEdit,
} from '@arco-design/web-vue/es/icon'

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()

// Mock data
const totalStock = ref(2450)
const lowStockCount = ref(23)
const outOfStockCount = ref(5)
const stockValue = ref(85000000) // 85 triệu

const movementFilter = ref('all')
const movementDateRange = ref([])

// Low stock products
const lowStockColumns = [
  {
    title: 'Sản phẩm',
    dataIndex: 'name',
    slotName: 'product_name',
  },
  {
    title: 'Tồn kho',
    dataIndex: 'stock',
    slotName: 'stock_status',
    align: 'center',
  },
  {
    title: 'Giá bán',
    dataIndex: 'price',
    align: 'right',
  },
  {
    title: '',
    slotName: 'action',
    align: 'center',
    width: 100,
  },
]

const lowStockProducts = ref([
  {
    name: 'Giày sneaker Nike Air Max',
    stock: 5,
    unit: 'đôi',
    price: '2.500.000 VNĐ',
    image: 'https://via.placeholder.com/32',
  },
  {
    name: 'Giày boot Chelsea',
    stock: 3,
    unit: 'đôi',
    price: '3.200.000 VNĐ',
    image: 'https://via.placeholder.com/32',
  },
  {
    name: 'Giày thể thao Adidas',
    stock: 8,
    unit: 'đôi',
    price: '1.800.000 VNĐ',
    image: 'https://via.placeholder.com/32',
  },
  {
    name: 'Giày cao gót Jimmy Choo',
    stock: 2,
    unit: 'đôi',
    price: '5.500.000 VNĐ',
    image: 'https://via.placeholder.com/32',
  },
])

// Category stock data
const categoryStock = ref([
  { name: 'Giày sneaker', count: 450, percentage: 35, color: '#1890ff' },
  { name: 'Giày boot', count: 320, percentage: 25, color: '#52c41a' },
  { name: 'Giày cao gót', count: 280, percentage: 22, color: '#fa8c16' },
  { name: 'Giày thể thao', count: 180, percentage: 14, color: '#722ed1' },
  { name: 'Khác', count: 90, percentage: 4, color: '#13c2c2' },
])

// Stock movements
const movementColumns = [
  {
    title: 'Thời gian',
    dataIndex: 'date',
    width: 150,
  },
  {
    title: 'Loại',
    dataIndex: 'type',
    slotName: 'type',
    width: 80,
  },
  {
    title: 'Sản phẩm',
    dataIndex: 'productName',
    slotName: 'product',
  },
  {
    title: 'Số lượng',
    dataIndex: 'quantity',
    slotName: 'quantity',
    align: 'center',
    width: 100,
  },
  {
    title: 'Người thực hiện',
    dataIndex: 'user',
    width: 120,
  },
]

const stockMovements = ref([
  {
    date: '2024-01-20 14:30',
    type: 'in',
    productName: 'Giày sneaker Nike Air Max',
    quantity: 50,
    user: 'Nguyễn Văn A',
  },
  {
    date: '2024-01-20 13:15',
    type: 'out',
    productName: 'Giày boot Chelsea',
    quantity: 2,
    user: 'Trần Thị B',
  },
  {
    date: '2024-01-20 11:45',
    type: 'in',
    productName: 'Giày thể thao Adidas',
    quantity: 30,
    user: 'Lê Văn C',
  },
])

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const getStockStatusColor = (stock: number) => {
  if (stock === 0) return 'red'
  if (stock <= 10) return 'orange'
  return 'green'
}

onMounted(() => {
  // Page mounted
})
</script>

<style scoped>
.inventory-statistics {
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

.total-icon {
  color: #1890ff;
}

.low-icon {
  color: #faad14;
}

.out-icon {
  color: #ff4d4f;
}

.value-icon {
  color: #52c41a;
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

.stat-change.warning {
  color: #faad14;
}

.stat-change.negative {
  color: #ff4d4f;
}

.stat-change.neutral {
  color: #86909c;
}

.stat-change.positive {
  color: #52c41a;
}

.content-section {
  margin-top: 16px;
}

.chart-container {
  height: 250px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-summary {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.category-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.category-name {
  font-weight: 500;
  color: #1d2129;
}

.category-count {
  font-size: 12px;
  color: #86909c;
}

.quantity-in {
  color: #52c41a;
  font-weight: 500;
}

.quantity-out {
  color: #1890ff;
  font-weight: 500;
}

/* Responsive */
@media (max-width: 768px) {
  .inventory-statistics {
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

  .content-section .ant-row {
    margin-top: 16px !important;
  }

  .content-section .ant-col {
    margin-bottom: 16px;
  }
}
</style>
