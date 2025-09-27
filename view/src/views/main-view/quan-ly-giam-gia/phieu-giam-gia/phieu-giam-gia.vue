<template>
  <div class="coupon-management-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="filters" layout="vertical">
        <!-- Row 1: Search - Start Date - End Date -->
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="filters.search" placeholder="Mã, tên, giá trị giảm..." allow-clear @change="searchCoupons" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="Ngày bắt đầu">
              <a-date-picker
                v-model="filters.startDate"
                placeholder="Chọn ngày bắt đầu"
                allow-clear
                @change="searchCoupons"
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
                @change="searchCoupons"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Row 2: Discount Type - Status -->
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="Loại giảm giá">
              <a-radio-group v-model="filters.discountType" type="button" @change="searchCoupons">
                <a-radio value="">Tất cả</a-radio>
                <a-radio value="percentage">Giảm %</a-radio>
                <a-radio value="fixed">Giảm tiền</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="filters.status" type="button" @change="searchCoupons">
                <a-radio value="">Tất cả</a-radio>
                <a-radio value="active">Đang hoạt động</a-radio>
                <a-radio value="expired">Đã hết hạn</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <!-- Empty column for alignment -->
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <!-- Coupons Table -->
    <a-card title="Danh sách phiếu giảm giá" class="table-card">
      <template #extra>
        <a-space>
          <a-button @click="exportCoupons">
            <template #icon>
              <icon-download />
            </template>
            Xuất danh sách
          </a-button>
        </a-space>
      </template>

      <a-table :columns="columns" :data="filteredCoupons" :pagination="pagination" :loading="loading" :scroll="{ x: 1300 }">
        <template #discount_value="{ record }">
          <div class="discount-value">
            {{ record.discount_type === 'percentage' ? `${record.discount_value}%` : formatCurrency(record.discount_value) }}
          </div>
        </template>

        <template #min_order_value="{ record }">
          <div class="min-order-value">
            {{ record.min_order_value ? formatCurrency(record.min_order_value) : 'Không giới hạn' }}
          </div>
        </template>

        <template #max_discount_value="{ record }">
          <div class="max-discount-value">
            {{ record.max_discount_value ? formatCurrency(record.max_discount_value) : 'Không giới hạn' }}
          </div>
        </template>

        <template #usage_limits="{ record }">
          <div class="usage-limits">{{ record.total_used }}/{{ record.total_usage_limit || '∞' }} tổng</div>
        </template>

        <template #validity_period="{ record }">
          <div class="validity-period">
            <div>{{ formatDate(record.start_date) }}</div>
            <div class="separator">-</div>
            <div>{{ formatDate(record.end_date) }}</div>
          </div>
        </template>

        <template #quantity="{ record }">
          <div class="quantity-cell">
            {{ record.usage_limit || '∞' }}
          </div>
        </template>

        <template #status="{ record }">
          <a-tag :color="getStatusColor(record.status)">
            {{ getStatusText(record.status) }}
          </a-tag>
        </template>

        <template #action="{ record }">
          <a-space>
            <a-button type="text" @click="viewCoupon(record)">
              <template #icon>
                <icon-eye />
              </template>
            </a-button>
            <a-button type="text" @click="editCoupon(record)">
              <template #icon>
                <icon-edit />
              </template>
            </a-button>
            <a-button type="text" class="danger-item" @click="deleteCoupon(record)">
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
  IconDownload,
  IconEdit,
  IconEye,
  IconMore,
  IconCopy,
  IconDelete,
  IconStar,
  IconGift,
  IconCheckCircle,
  IconClockCircle,
} from '@arco-design/web-vue/es/icon'

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()

// Filters
const filters = ref({
  search: '',
  discountType: '',
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
    title: 'Mã phiếu',
    dataIndex: 'code',
    width: 120,
  },
  {
    title: 'Tên phiếu giảm giá',
    dataIndex: 'name',
    width: 200,
  },
  {
    title: 'Giá trị giảm',
    dataIndex: 'discount_value',
    slotName: 'discount_value',
    width: 120,
    align: 'center',
  },
  {
    title: 'Giá trị tối thiểu',
    dataIndex: 'min_order_value',
    slotName: 'min_order_value',
    width: 130,
    align: 'center',
  },
  {
    title: 'Giá trị tối đa',
    dataIndex: 'max_discount_value',
    slotName: 'max_discount_value',
    width: 130,
    align: 'center',
  },
  {
    title: 'Thời gian',
    dataIndex: 'start_date',
    slotName: 'validity_period',
    width: 200,
    align: 'center',
  },
  {
    title: 'Số lượng',
    dataIndex: 'quantity',
    slotName: 'quantity',
    width: 100,
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

// Mock data

const coupons = ref([
  {
    id: 1,
    index: 1,
    code: 'WELCOME2024',
    name: 'Phiếu chào mừng',
    discount_type: 'percentage',
    discount_value: 15,
    min_order_value: 200000,
    max_discount_value: null,
    usage_limit: 500,
    start_date: '2024-01-01',
    end_date: '2024-12-31',
    status: 'active',
  },
  {
    id: 2,
    index: 2,
    code: 'FLASH50K',
    name: 'Flash Sale 50k',
    discount_type: 'fixed',
    discount_value: 50000,
    min_order_value: null,
    max_discount_value: null,
    usage_limit: 100,
    start_date: '2024-01-15',
    end_date: '2024-01-15',
    status: 'expired',
  },
  {
    id: 3,
    index: 3,
    code: 'VIP20',
    name: 'Ưu đãi VIP',
    discount_type: 'percentage',
    discount_value: 20,
    min_order_value: 500000,
    max_discount_value: 100000,
    usage_limit: null,
    start_date: '2024-01-01',
    end_date: '2024-12-31',
    status: 'active',
  },
])

// Methods
const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

// Filtered coupons computed
const filteredCoupons = computed(() => {
  let filtered = coupons.value

  // Filter by search term (code, name, discount value)
  if (filters.value.search) {
    const searchTerm = filters.value.search.toLowerCase()
    filtered = filtered.filter((coupon) => {
      const discountValueStr = coupon.discount_type === 'percentage' ? `${coupon.discount_value}%` : formatCurrency(coupon.discount_value)
      return (
        coupon.code.toLowerCase().includes(searchTerm) ||
        coupon.name.toLowerCase().includes(searchTerm) ||
        discountValueStr.toLowerCase().includes(searchTerm)
      )
    })
  }

  // Filter by discount type
  if (filters.value.discountType) {
    filtered = filtered.filter((coupon) => coupon.discount_type === filters.value.discountType)
  }

  // Filter by start date
  if (filters.value.startDate) {
    filtered = filtered.filter((coupon) => {
      const couponStart = new Date(coupon.start_date)
      return couponStart >= filters.value.startDate
    })
  }

  // Filter by end date
  if (filters.value.endDate) {
    filtered = filtered.filter((coupon) => {
      const couponEnd = new Date(coupon.end_date)
      return couponEnd <= filters.value.endDate
    })
  }

  // Filter by status
  if (filters.value.status) {
    filtered = filtered.filter((coupon) => coupon.status === filters.value.status)
  }

  return filtered
})

// Pagination
const pagination = computed(() => ({
  current: 1,
  pageSize: 10,
  total: filteredCoupons.value.length,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: true,
}))

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
}

const getStatusColor = (status: string) => {
  switch (status) {
    case 'active':
      return 'green'
    case 'expired':
      return 'red'
    case 'used_up':
      return 'orange'
    default:
      return 'default'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'active':
      return 'Đang hoạt động'
    case 'expired':
      return 'Đã hết hạn'
    case 'used_up':
      return 'Hết lượt sử dụng'
    default:
      return status
  }
}

const searchCoupons = () => {
  // Filtering is now handled by the computed filteredCoupons property
  // This function is called when filters change to trigger reactivity
}

const showCreateModal = () => {
  // Removed console.log
}

const viewCoupon = (coupon: any) => {
  // TODO: Implement view coupon details
  // Removed console.log
}

const editCoupon = (coupon: any) => {
  // Removed console.log
}

const duplicateCoupon = (coupon: any) => {
  // Removed console.log
}

const deleteCoupon = (coupon: any) => {
  // Removed console.log
}

const exportCoupons = () => {
  // Removed console.log
}

onMounted(() => {
  // Removed console.log
})
</script>

<style scoped>
.coupon-management-page {
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
  margin: 0;
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

.used-icon {
  color: #52c41a;
}

.revenue-icon {
  color: #faad14;
}

.expiring-icon {
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

.coupon-code-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.coupon-code {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-weight: 600;
  color: #1890ff;
  font-size: 16px;
}

.coupon-name {
  font-size: 12px;
  color: #86909c;
}

.discount-value {
  text-align: center;
  font-weight: 600;
  color: #1890ff;
}

.usage-limits {
  text-align: center;
  font-size: 12px;
  color: #1d2129;
}

.validity-period {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.separator {
  color: #86909c;
}

.danger-item {
  color: #ff4d4f;
}
</style>
