<template>
  <div class="product-attribute-origin-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="filters" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="filters.search" placeholder="Tìm theo mã hoặc tên xuất xứ..." allow-clear @input="searchOrigins" />
            </a-form-item>
          </a-col>

          <a-col :span="8">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="filters.status" type="button" @change="searchOrigins">
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
          <a-button @click="exportOrigins">
            <template #icon>
              <icon-download />
            </template>
            Xuất Excel
          </a-button>
          <a-button type="primary" @click="showCreateModal">
            <template #icon>
              <icon-plus />
            </template>
            Thêm Xuất xứ
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!-- Origins Table -->
    <a-card title="Danh sách xuất xứ" class="table-card">
      <a-table :columns="columns" :data="origins" :pagination="pagination" :loading="loading" :scroll="{ x: 1000 }">
        <template #checkbox="{ record }">
          <a-checkbox
            :checked="isRowSelected(record.id)"
            @change="
              (checked) => {
                if (checked) {
                  const currentKeys = Array.isArray(selectedRowKeys.value) ? selectedRowKeys.value : []
                  selectedRowKeys.value = [...currentKeys, record.id]
                  if (!editingData.value) editingData.value = {}
                  editingData.value[record.id] = {
                    code: record.code,
                    name: record.name,
                    status: record.is_active,
                  }
                } else {
                  const currentKeys = Array.isArray(selectedRowKeys.value) ? selectedRowKeys.value : []
                  selectedRowKeys.value = currentKeys.filter((key) => key !== record.id)
                  if (editingData.value && editingData.value[record.id]) {
                    delete editingData.value[record.id]
                  }
                }
              }
            "
          />
        </template>

        <template #code="{ record }">
          <div v-if="editingData.value && editingData.value[record.id]">
            <a-input v-model="editingData.value[record.id].code" size="mini" style="width: 80px" />
          </div>
          <span v-else>{{ record.code }}</span>
        </template>

        <template #name="{ record }">
          <div v-if="editingData.value && editingData.value[record.id]">
            <a-input v-model="editingData.value[record.id].name" size="mini" style="width: 100px" />
          </div>
          <span v-else>{{ record.name }}</span>
        </template>

        <template #status="{ record }">
          <a-tag :color="record.is_active ? 'green' : 'red'">
            {{ record.is_active ? 'Hoạt động' : 'Không hoạt động' }}
          </a-tag>
        </template>

        <template #action="{ record }">
          <a-space>
            <a-button type="text" @click="viewOrigin(record)">
              <template #icon>
                <icon-eye />
              </template>
            </a-button>
            <a-button type="text" @click="editOrigin(record)">
              <template #icon>
                <icon-edit />
              </template>
            </a-button>
            <a-button type="text" danger @click="deleteOrigin(record)">
              <template #icon>
                <icon-delete />
              </template>
            </a-button>
          </a-space>
        </template>

        <template #checkbox-title>
          <a-checkbox
            :checked="selectedCount === origins.length && origins.length > 0"
            :indeterminate="selectedCount > 0 && selectedCount < origins.length"
            @change="
              (checked) => {
                if (checked) {
                  selectedRowKeys.value = [...origins.map((origin) => origin.id)]
                  if (!editingData.value) editingData.value = {}
                  origins.forEach((origin) => {
                    editingData.value[origin.id] = {
                      code: origin.code,
                      name: origin.name,
                      status: origin.is_active,
                    }
                  })
                } else {
                  selectedRowKeys.value = []
                  editingData.value = {}
                }
              }
            "
          />
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
  IconStar,
  IconGift,
  IconLocation,
  IconDriveFile,
  IconSearch,
  IconDownload,
  IconEdit,
  IconEye,
  IconDelete,
  IconRefresh,
} from '@arco-design/web-vue/es/icon'

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()

// Filters
const filters = ref({
  search: '',
  status: '',
  folder: '',
  sort: 'newest',
})

// Edit inline mode state
const selectedRowKeys = ref<number[]>([])
const editingData = ref<Record<number, { code: string; name: string; status: boolean }>>({})

// Computed properties for safe access
const selectedCount = computed(() => (Array.isArray(selectedRowKeys.value) ? selectedRowKeys.value.length : 0))
const isRowSelected = (id: number) => {
  const keys = Array.isArray(selectedRowKeys.value) ? selectedRowKeys.value : []
  return keys.includes(id)
}

// Table
const loading = ref(false)
const columns = [
  {
    title: '',
    dataIndex: 'checkbox',
    slotName: 'checkbox',
    width: 50,
    align: 'center',
    titleSlotName: 'checkbox-title',
  },
  {
    title: 'Mã xuất xứ',
    dataIndex: 'code',
    width: 100,
    slotName: 'code',
  },
  {
    title: 'Tên xuất xứ',
    dataIndex: 'name',
    width: 120,
    slotName: 'name',
  },
  {
    title: 'Trạng thái',
    dataIndex: 'is_active',
    slotName: 'status',
    width: 400,
    align: 'center',
  },
  {
    title: 'Thao tác',
    slotName: 'action',
    width: 100,
    fixed: 'right',
    align: 'center',
  },
]

// Pagination
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 3,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: true,
})

// Mock data

const origins = ref([
  {
    id: 1,
    code: 'XX001',
    name: 'Trung Quốc',
    flag: '🇨🇳',
    continent: 'asia',
    products_count: 65,
    shipping_cost: 180000,
    currency: 'CNY',
    is_active: true,
  },
  {
    id: 2,
    code: 'XX002',
    name: 'Việt Nam',
    flag: '🇻🇳',
    continent: 'asia',
    products_count: 45,
    shipping_cost: 50000,
    currency: 'VND',
    is_active: true,
  },
  {
    id: 3,
    code: 'XX003',
    name: 'Ý',
    flag: '🇮🇹',
    continent: 'europe',
    products_count: 35,
    shipping_cost: 450000,
    currency: 'EUR',
    is_active: false,
  },
])

// Methods
const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const getContinentColor = (continent: string) => {
  switch (continent) {
    case 'asia':
      return 'orange'
    case 'europe':
      return 'blue'
    case 'america':
      return 'green'
    default:
      return 'gray'
  }
}

const getContinentName = (continent: string) => {
  switch (continent) {
    case 'asia':
      return 'Châu Á'
    case 'europe':
      return 'Châu Âu'
    case 'america':
      return 'Châu Mỹ'
    default:
      return continent
  }
}

const resetFilters = () => {
  filters.value = {
    search: '',
    status: '',
    folder: '',
    sort: 'newest',
  }
}

const searchOrigins = () => {
  // TODO: Implement search functionality
  console.log('Searching origins with filters:', filters.value)
}

const showCreateModal = () => {
  // TODO: Implement create origin modal
  console.log('Show create origin modal')
}

const editOrigin = (origin: any) => {
  // TODO: Implement edit origin functionality
  console.log('Edit origin:', origin)
}

const viewOrigin = (origin: any) => {
  // TODO: Implement view origin details functionality
  console.log('View origin details:', origin)
}

const deleteOrigin = (origin: any) => {
  // TODO: Implement delete origin functionality
  console.log('Delete origin:', origin)
}

const viewProducts = (origin: any) => {
  // TODO: Implement view products functionality
  console.log('View products for origin:', origin)
}

const exportOrigins = () => {
  // TODO: Implement Excel export functionality
  console.log('Exporting origins to Excel...')
}

onMounted(() => {
  // Removed console.log
})
</script>

<style scoped>
.product-attribute-origin-page {
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

.product-icon {
  color: #52c41a;
}

.flag-icon {
  color: #faad14;
}

.truck-icon {
  color: #722ed1;
}

.filters-card,
.table-card {
  margin-bottom: 16px;
}

.actions-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.flag-cell {
  display: flex;
  justify-content: center;
  font-size: 24px;
}
</style>
