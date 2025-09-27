<template>
  <div class="promotion-management-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="filters" layout="vertical">
        <!-- Row 1: Search - Start Date - End Date -->
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="filters.search" placeholder="Tên khuyến mãi..." allow-clear @change="searchPromotions" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="Ngày bắt đầu">
              <a-date-picker
                v-model="filters.startDate"
                placeholder="Chọn ngày bắt đầu"
                allow-clear
                @change="searchPromotions"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="Ngày kết thúc">
              <a-date-picker
                v-model="filters.endDate"
                placeholder="Chọn ngày kết thúc"
                allow-clear
                @change="searchPromotions"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Row 2: Status -->
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="filters.status" type="button" @change="searchPromotions">
                <a-radio value="">Tất cả</a-radio>
                <a-radio value="active">Đang hoạt động</a-radio>
                <a-radio value="expired">Đã kết thúc</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Actions Row -->
        <div class="actions-row">
          <a-space>
            <a-button @click="resetFilters">
              <template #icon>
                <icon-refresh />
              </template>
              Đặt lại
            </a-button>
            <a-button @click="exportExcel">
              <template #icon>
                <icon-download />
              </template>
              Xuất Excel
            </a-button>
            <a-button type="primary" @click="showCreateModal">
              <template #icon>
                <icon-plus />
              </template>
              Thêm đợt khuyến mãi
            </a-button>
          </a-space>
        </div>
      </a-form>
    </a-card>

    <!-- Promotions Table -->
    <a-card title="Danh sách khuyến mãi" class="table-card">
      <a-table :columns="columns" :data="filteredPromotions" :pagination="pagination" :loading="loading" :scroll="{ x: 1000 }">
        <template #percentage="{ record }">
          <span class="percentage-value">{{ record.percentage }}%</span>
        </template>

        <template #date_range="{ record }">
          <div class="date-range">{{ formatDate(record.start_date) }} - {{ formatDate(record.end_date) }}</div>
        </template>

        <template #status="{ record }">
          <a-tag :color="getStatusColor(record.status)">
            {{ getStatusText(record.status) }}
          </a-tag>
        </template>

        <template #action="{ record }">
          <a-space>
            <a-button type="text" @click="viewPromotion(record)">
              <template #icon>
                <icon-eye />
              </template>
            </a-button>
            <a-button type="text" @click="editPromotion(record)">
              <template #icon>
                <icon-edit />
              </template>
            </a-button>
            <a-button type="text" danger @click="deletePromotion(record)">
              <template #icon>
                <icon-delete />
              </template>
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import {
  IconPlus,
  IconSearch,
  IconRefresh,
  IconDownload,
  IconEye,
  IconEdit,
  IconMore,
  IconCopy,
  IconDelete,
  IconStar,
  IconSettings,
  IconFolder,
  IconClockCircle,
  IconArrowUp,
} from '@arco-design/web-vue/es/icon'

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()

// Filters
const filters = ref({
  search: '',
  startDate: null,
  endDate: null,
  status: '',
})

// Table
const loading = ref(false)

const columns = [
  {
    title: 'STT',
    dataIndex: 'index',
    width: 70,
    align: 'center',
  },
  {
    title: 'Mã',
    dataIndex: 'code',
    width: 100,
  },
  {
    title: 'Tên',
    dataIndex: 'name',
    width: 200,
  },
  {
    title: 'Phần trăm giảm',
    dataIndex: 'percentage',
    slotName: 'percentage',
    width: 130,
    align: 'center',
  },
  {
    title: 'Thời gian',
    dataIndex: 'start_date',
    slotName: 'date_range',
    width: 250,
    align: 'center',
  },
  {
    title: 'Trạng thái',
    dataIndex: 'status',
    slotName: 'status',
    width: 120,
    align: 'center',
  },
  {
    title: 'Thao tác',
    slotName: 'action',
    width: 150,
    fixed: 'right',
    align: 'center',
  },
]

// Pagination - moved after filteredPromotions

// Mock data

const promotions = ref([
  {
    id: 1,
    index: 1,
    code: 'KM001',
    name: 'Mừng khai trương - Giảm 20%',
    percentage: 20,
    start_date: '2024-01-01',
    end_date: '2024-01-31',
    status: 'active',
  },
  {
    id: 2,
    index: 2,
    code: 'KM002',
    name: 'Flash Sale - Giảm 50k',
    percentage: 15,
    start_date: '2024-01-15',
    end_date: '2024-01-15',
    status: 'expired',
  },
  {
    id: 3,
    index: 3,
    code: 'KM003',
    name: 'Mua 2 tặng 1',
    percentage: 25,
    start_date: '2024-01-20',
    end_date: '2024-02-20',
    status: 'active',
  },
])

// Filtered promotions computed
const filteredPromotions = computed(() => {
  let filtered = promotions.value

  // Filter by search term
  if (filters.value.search) {
    const searchTerm = filters.value.search.toLowerCase()
    filtered = filtered.filter(
      (promotion) => promotion.name.toLowerCase().includes(searchTerm) || promotion.code.toLowerCase().includes(searchTerm)
    )
  }

  // Filter by start date
  if (filters.value.startDate) {
    filtered = filtered.filter((promotion) => {
      const promotionStart = new Date(promotion.start_date)
      return promotionStart >= filters.value.startDate
    })
  }

  // Filter by end date
  if (filters.value.endDate) {
    filtered = filtered.filter((promotion) => {
      const promotionEnd = new Date(promotion.end_date)
      return promotionEnd <= filters.value.endDate
    })
  }

  // Filter by status
  if (filters.value.status && filters.value.status !== '') {
    filtered = filtered.filter((promotion) => promotion.status === filters.value.status)
  }

  return filtered
})

// Pagination
const pagination = computed(() => ({
  current: 1,
  pageSize: 10,
  total: filteredPromotions.value.length,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: true,
}))

// Methods

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
}

const getPromotionTypeColor = (type: string) => {
  switch (type) {
    case 'percentage':
      return 'blue'
    case 'fixed':
      return 'green'
    case 'buy_x_get_y':
      return 'orange'
    default:
      return 'default'
  }
}

const getPromotionTypeText = (type: string) => {
  switch (type) {
    case 'percentage':
      return 'Giảm %'
    case 'fixed':
      return 'Giảm tiền'
    case 'buy_x_get_y':
      return 'Mua X tặng Y'
    default:
      return type
  }
}

const getStatusColor = (status: string) => {
  switch (status) {
    case 'active':
      return 'green'
    case 'expired':
      return 'red'
    default:
      return 'default'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'active':
      return 'Đang hoạt động'
    case 'expired':
      return 'Đã kết thúc'
    default:
      return status
  }
}

const searchPromotions = () => {
  // Filtering is now handled by the computed filteredPromotions property
  // This function is called when filters change to trigger reactivity
}

const showCreateModal = () => {
  // Removed console.log
}

const viewPromotion = (promotion: any) => {
  // TODO: Implement view promotion details
}

const editPromotion = (promotion: any) => {
  // Removed console.log
}

const duplicatePromotion = (promotion: any) => {
  // Removed console.log
}

const deletePromotion = (promotion: any) => {
  // Removed console.log
}

const resetFilters = () => {
  filters.value = {
    search: '',
    startDate: null,
    endDate: null,
    status: '',
  }
  // Reset to show all promotions
  searchPromotions()
}

const exportExcel = () => {
  // TODO: Implement Excel export functionality
}

onMounted(() => {
  // Removed console.log
})
</script>

<style scoped>
.promotion-management-page {
  padding: 0 20px 20px 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #1d2129;
  margin: 0 0 8px 0;
}

.page-description {
  font-size: 16px;
  color: #86909c;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.stat-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  font-size: 20px;
  color: #1890ff;
}

.revenue-icon {
  color: #52c41a;
}

.growth-icon {
  color: #faad14;
}

.pending-icon {
  color: #722ed1;
}

.stat-content {
  margin-top: 12px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1d2129;
  margin-bottom: 4px;
}

.stat-change {
  font-size: 12px;
  color: #86909c;
}

.filters-card,
.table-card {
  margin-bottom: 16px;
}

.promotion-name-cell {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.promotion-title {
  font-weight: 600;
  color: #1d2129;
}

.promotion-type {
  display: flex;
}

.discount-value {
  text-align: center;
  font-weight: 600;
  color: #1890ff;
}

.percentage-value {
  font-weight: 600;
  color: #1890ff;
}

.date-range {
  text-align: center;
  font-size: 14px;
  color: #1d2129;
}

.danger-item {
  color: #ff4d4f;
}

.actions-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.percentage-slider {
  padding: 16px;
  border: 1px solid var(--color-border-2);
  border-radius: 6px;
  background: var(--color-bg-3);
  margin-bottom: 8px;
}

.slider-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: var(--color-text-2);
  margin-bottom: 8px;
}

.slider-min,
.slider-max {
  color: var(--color-text-2);
  font-weight: 400;
}

.slider-range {
  font-weight: 600;
  color: var(--color-text-1);
  background: var(--color-fill-3);
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 14px;
}
</style>
