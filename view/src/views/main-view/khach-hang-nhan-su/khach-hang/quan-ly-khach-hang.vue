<template>
  <div class="customer-management-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="filters" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="filters.search" placeholder="Mã, tên, SĐT, email..." allow-clear @change="searchCustomers" />
            </a-form-item>
          </a-col>

          <a-col :span="8">
            <a-form-item label="Phân loại">
              <a-select v-model="filters.category" placeholder="Chọn phân loại" allow-clear @change="searchCustomers">
                <a-option value="">Tất cả</a-option>
                <a-option value="vip">VIP</a-option>
                <a-option value="regular">Thường xuyên</a-option>
                <a-option value="new">Mới</a-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="8">
            <a-form-item label="Giới tính">
              <a-radio-group v-model="filters.gender" type="button" @change="searchCustomers">
                <a-radio value="">Tất cả</a-radio>
                <a-radio value="Nam">Nam</a-radio>
                <a-radio value="Nữ">Nữ</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :span="24">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="filters.status" type="button" @change="searchCustomers">
                <a-radio value="">Tất cả</a-radio>
                <a-radio value="active">Hoạt động</a-radio>
                <a-radio value="inactive">Không hoạt động</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>

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
            Tạo khách hàng mới
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!-- Customers Table -->
    <a-card title="Danh sách khách hàng" class="table-card">
      <a-table
        :columns="columns"
        :data="customersWithIndex"
        :pagination="pagination"
        :loading="loading"
        :scroll="{ x: 1200 }"
        @change="handleTableChange"
      >
        <template #customer_type="{ record }">
          <a-tag :color="getCustomerTypeColor(record.customer_type)">
            {{ getCustomerTypeText(record.customer_type) }}
          </a-tag>
        </template>

        <template #total_orders="{ record }">
          <span>{{ record.total_orders }}</span>
        </template>

        <template #total_spent="{ record }">
          {{ formatCurrency(record.total_spent) }}
        </template>

        <template #status="{ record }">
          <a-tag :color="record.status === 'active' ? 'green' : 'orange'">
            {{ record.status === 'active' ? 'Hoạt động' : 'Không hoạt động' }}
          </a-tag>
        </template>

        <template #action="{ record }">
          <a-space>
            <a-button type="text" @click="viewCustomer(record)">
              <template #icon>
                <icon-eye />
              </template>
            </a-button>
            <a-button type="text" @click="editCustomer(record)">
              <template #icon>
                <icon-edit />
              </template>
            </a-button>
            <a-button type="text" danger @click="deleteCustomer(record)">
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
import { ref, reactive, computed, onMounted } from 'vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import {
  IconPlus,
  IconSearch,
  IconRefresh,
  IconDownload,
  IconEye,
  IconEdit,
  IconDelete,
  IconUserGroup,
  IconUser,
  IconGift,
  IconStar,
} from '@arco-design/web-vue/es/icon'

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()

// Filters
const filters = ref({
  search: '',
  gender: '',
  category: '',
  status: '',
})

// Table
const loading = ref(false)
const columns = [
  {
    title: 'STT',
    dataIndex: 'index',
    width: 40,
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
    width: 150,
  },
  {
    title: 'Giới tính',
    dataIndex: 'gender',
    width: 80,
    align: 'center',
  },
  {
    title: 'Ngày sinh',
    dataIndex: 'birthday',
    width: 120,
    align: 'center',
  },
  {
    title: 'Tổng đơn',
    dataIndex: 'total_orders',
    slotName: 'total_orders',
    width: 100,
    align: 'center',
  },
  {
    title: 'Tổng chi tiêu',
    dataIndex: 'total_spent',
    slotName: 'total_spent',
    width: 130,
    align: 'right',
  },
  {
    title: 'Phân loại',
    dataIndex: 'customer_type',
    slotName: 'customer_type',
    width: 120,
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
  },
]

// Mock data
const customers = ref([
  {
    id: 1,
    code: 'KH001',
    name: 'Nguyễn Văn A',
    email: 'nguyenvana@email.com',
    phone: '0987654321',
    gender: 'Nam',
    birthday: '1990-05-15',
    avatar: 'https://via.placeholder.com/80x80/1890ff/ffffff?text=A',
    customer_type: 'vip',
    total_orders: 15,
    total_spent: 25000000,
    status: 'active',
  },
  {
    id: 2,
    code: 'KH002',
    name: 'Trần Thị C',
    email: 'tranthic@email.com',
    phone: '0978123456',
    gender: 'Nữ',
    birthday: '1988-12-03',
    avatar: 'https://via.placeholder.com/80x80/52c41a/ffffff?text=C',
    customer_type: 'regular',
    total_orders: 8,
    total_spent: 12000000,
    status: 'active',
  },
  {
    id: 3,
    code: 'KH003',
    name: 'Lê Văn D',
    email: 'levand@email.com',
    phone: '0967234567',
    gender: 'Nam',
    birthday: '1995-08-20',
    avatar: 'https://via.placeholder.com/80x80/fa8c16/ffffff?text=D',
    customer_type: 'new',
    total_orders: 2,
    total_spent: 3500000,
    status: 'active',
  },
  {
    id: 4,
    code: 'KH004',
    name: 'Phạm Thị E',
    email: 'phamthie@email.com',
    phone: '0956123456',
    gender: 'Nữ',
    birthday: '1992-03-10',
    avatar: 'https://via.placeholder.com/80x80/722ed1/ffffff?text=E',
    customer_type: 'regular',
    total_orders: 12,
    total_spent: 18000000,
    status: 'active',
  },
  {
    id: 5,
    code: 'KH005',
    name: 'Hoàng Văn F',
    email: 'hoangvanf@email.com',
    phone: '0945987654',
    gender: 'Nam',
    birthday: '1985-11-25',
    avatar: 'https://via.placeholder.com/80x80/13c2c2/ffffff?text=F',
    customer_type: 'vip',
    total_orders: 20,
    total_spent: 35000000,
    status: 'active',
  },
  {
    id: 6,
    code: 'KH006',
    name: 'Vũ Thị G',
    email: 'vuthig@email.com',
    phone: '0934876543',
    gender: 'Nữ',
    birthday: '1993-07-12',
    avatar: 'https://via.placeholder.com/80x80/eb2f96/ffffff?text=G',
    customer_type: 'regular',
    total_orders: 6,
    total_spent: 9500000,
    status: 'inactive',
  },
  {
    id: 7,
    code: 'KH007',
    name: 'Đỗ Văn H',
    email: 'dovanh@email.com',
    phone: '0923765432',
    gender: 'Nam',
    birthday: '1987-09-08',
    avatar: 'https://via.placeholder.com/80x80/f5222d/ffffff?text=H',
    customer_type: 'new',
    total_orders: 1,
    total_spent: 1200000,
    status: 'active',
  },
  {
    id: 8,
    code: 'KH008',
    name: 'Bùi Thị I',
    email: 'buithii@email.com',
    phone: '0912654321',
    gender: 'Nữ',
    birthday: '1991-04-18',
    avatar: 'https://via.placeholder.com/80x80/faad14/ffffff?text=I',
    customer_type: 'vip',
    total_orders: 18,
    total_spent: 32000000,
    status: 'active',
  },
])

// Computed customers with filtering and index for STT
const customersWithIndex = computed(() => {
  let filteredCustomers = customers.value

  // Filter by search term (code, name, phone, email)
  if (filters.value.search) {
    const searchTerm = filters.value.search.toLowerCase()
    filteredCustomers = filteredCustomers.filter(
      (customer) =>
        customer.code.toLowerCase().includes(searchTerm) ||
        customer.name.toLowerCase().includes(searchTerm) ||
        customer.phone.toLowerCase().includes(searchTerm) ||
        (customer.email && customer.email.toLowerCase().includes(searchTerm))
    )
  }

  // Filter by gender
  if (filters.value.gender) {
    filteredCustomers = filteredCustomers.filter((customer) => customer.gender === filters.value.gender)
  }

  // Filter by category
  if (filters.value.category) {
    filteredCustomers = filteredCustomers.filter((customer) => customer.customer_type === filters.value.category)
  }

  // Filter by status
  if (filters.value.status) {
    filteredCustomers = filteredCustomers.filter((customer) => customer.status === filters.value.status)
  }

  // Add index for STT
  return filteredCustomers.map((customer, index) => ({
    ...customer,
    index: index + 1,
  }))
})

// Methods
const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const getCustomerTypeColor = (type: string) => {
  switch (type) {
    case 'vip':
      return 'gold'
    case 'regular':
      return 'blue'
    case 'new':
      return 'green'
    case 'inactive':
      return 'red'
    default:
      return 'default'
  }
}

const getCustomerTypeText = (type: string) => {
  switch (type) {
    case 'vip':
      return 'VIP'
    case 'regular':
      return 'Thường xuyên'
    case 'new':
      return 'Mới'
    case 'inactive':
      return 'Không hoạt động'
    default:
      return type
  }
}

const searchCustomers = () => {
  // Filtering is handled by computed property customersWithIndex
  // This method is called when filters change (@change event)
}

const resetFilters = () => {
  filters.value = {
    search: '',
    gender: '',
    category: '',
    status: '',
  }
}

const handleTableChange = (paginationData: any, filtersData: any, sorter: any) => {
  // Removed console.log
}

const showCreateModal = () => {
  // Removed console.log
}

const viewCustomer = (customer: any) => {
  // Removed console.log
}

const editCustomer = (customer: any) => {
  // Removed console.log
}

const deleteCustomer = (customer: any) => {
  // TODO: Implement delete customer functionality
}

const exportExcel = () => {
  // Removed console.log
}

onMounted(() => {
  // Removed console.log
})
</script>

<style scoped>
.customer-management-page {
  padding: 0 20px 20px 20px;
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

.active-icon {
  color: #52c41a;
}

.purchase-icon {
  color: #faad14;
}

.revenue-icon {
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

.stat-change.positive {
  color: #52c41a;
}

.filters-card,
.table-card {
  margin-bottom: 16px;
}

.actions-row {
  display: flex;
  justify-content: flex-end;
}

/* Responsive */
@media (max-width: 768px) {
  .customer-management-page {
    padding: 16px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .header-right {
    width: 100%;
    justify-content: center;
  }
}
</style>
