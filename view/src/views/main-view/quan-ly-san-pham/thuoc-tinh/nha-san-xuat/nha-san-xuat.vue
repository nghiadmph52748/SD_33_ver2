<template>
  <div class="product-attribute-manufacturer-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="filters" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input
                v-model="filters.search"
                placeholder="Tìm theo mã hoặc tên nhà sản xuất..."
                allow-clear
                @input="searchManufacturers"
              />
            </a-form-item>
          </a-col>

          <a-col :span="8">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="filters.status" type="button" @change="searchManufacturers">
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
          <a-button @click="exportManufacturers">
            <template #icon>
              <icon-download />
            </template>
            Xuất Excel
          </a-button>
          <a-button type="primary" @click="showCreateModal">
            <template #icon>
              <icon-plus />
            </template>
            Thêm Nhà sản xuất
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!-- Manufacturers Table -->
    <a-card title="Danh sách nhà sản xuất" class="table-card">
      <a-table :columns="columns" :data="manufacturers" :pagination="pagination" :loading="loading" :scroll="{ x: 1000 }">
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
                    products_count: record.products_count,
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
            <a-input v-model="editingData.value[record.id].code" size="mini" style="width: 120px" />
          </div>
          <span v-else>{{ record.code }}</span>
        </template>

        <template #name="{ record }">
          <div v-if="editingData.value && editingData.value[record.id]">
            <a-input v-model="editingData.value[record.id].name" size="mini" style="width: 180px" />
          </div>
          <span v-else>{{ record.name }}</span>
        </template>

        <template #products_count="{ record }">
          <span>{{ record.products_count }}</span>
        </template>

        <template #status="{ record }">
          <a-tag :color="record.is_active ? 'green' : 'red'">
            {{ record.is_active ? 'Hoạt động' : 'Không hoạt động' }}
          </a-tag>
        </template>

        <template #action="{ record }">
          <a-space>
            <a-button type="text" @click="viewManufacturer(record)">
              <template #icon>
                <icon-eye />
              </template>
            </a-button>
            <a-button type="text" @click="editManufacturer(record)">
              <template #icon>
                <icon-edit />
              </template>
            </a-button>
            <a-button type="text" danger @click="deleteManufacturer(record)">
              <template #icon>
                <icon-delete />
              </template>
            </a-button>
          </a-space>
        </template>

        <template #checkbox-title>
          <a-checkbox
            :checked="selectedCount === manufacturers.length && manufacturers.length > 0"
            :indeterminate="selectedCount > 0 && selectedCount < manufacturers.length"
            @change="
              (checked) => {
                if (checked) {
                  selectedRowKeys.value = [...manufacturers.map((manufacturer) => manufacturer.id)]
                  if (!editingData.value) editingData.value = {}
                  manufacturers.forEach((manufacturer) => {
                    editingData.value[manufacturer.id] = {
                      code: manufacturer.code,
                      name: manufacturer.name,
                      products_count: manufacturer.products_count,
                      status: manufacturer.is_active,
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
  IconBuilding,
  IconGift,
  IconLocation,
  IconStar,
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
const editingData = ref<Record<number, { code: string; name: string; products_count: number; status: boolean }>>({})

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
    title: 'Mã nhà sản xuất',
    dataIndex: 'code',
    width: 100,
    slotName: 'code',
  },
  {
    title: 'Tên nhà sản xuất',
    dataIndex: 'name',
    width: 100,
    slotName: 'name',
  },
  {
    title: 'Số lượng sản phẩm',
    dataIndex: 'products_count',
    width: 100,
    align: 'center',
    slotName: 'products_count',
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

const manufacturers = ref([
  {
    id: 1,
    code: 'NSX001',
    name: 'Nike',
    logo: 'https://via.placeholder.com/40x40/1890ff/ffffff?text=Nike',
    country: 'usa',
    products_count: 45,
    is_active: true,
    created_at: '2024-01-01',
  },
  {
    id: 2,
    code: 'NSX002',
    name: 'Adidas',
    logo: 'https://via.placeholder.com/40x40/722ed1/ffffff?text=Adidas',
    country: 'usa',
    products_count: 38,
    is_active: true,
    created_at: '2024-01-02',
  },
  {
    id: 3,
    code: 'NSX003',
    name: 'Puma',
    logo: 'https://via.placeholder.com/40x40/52c41a/ffffff?text=Puma',
    country: 'usa',
    products_count: 23,
    is_active: false,
    created_at: '2024-01-03',
  },
])

// Methods
const getCountryColor = (country: string) => {
  switch (country) {
    case 'vietnam':
      return 'red'
    case 'china':
      return 'orange'
    case 'usa':
      return 'blue'
    case 'italy':
      return 'green'
    default:
      return 'gray'
  }
}

const getCountryName = (country: string) => {
  switch (country) {
    case 'vietnam':
      return 'Việt Nam'
    case 'china':
      return 'Trung Quốc'
    case 'usa':
      return 'Mỹ'
    case 'italy':
      return 'Ý'
    default:
      return country
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

const searchManufacturers = () => {
  // TODO: Implement search functionality
  console.log('Searching manufacturers with filters:', filters.value)
}

const showCreateModal = () => {
  // TODO: Implement create manufacturer modal
  console.log('Show create manufacturer modal')
}

const editManufacturer = (manufacturer: any) => {
  // TODO: Implement edit manufacturer functionality
  console.log('Edit manufacturer:', manufacturer)
}

const viewManufacturer = (manufacturer: any) => {
  // TODO: Implement view manufacturer details functionality
  console.log('View manufacturer details:', manufacturer)
}

const deleteManufacturer = (manufacturer: any) => {
  // TODO: Implement delete manufacturer functionality
  console.log('Delete manufacturer:', manufacturer)
}

const viewProducts = (manufacturer: any) => {
  // TODO: Implement view products functionality
  console.log('View products for manufacturer:', manufacturer)
}

const exportManufacturers = () => {
  // TODO: Implement Excel export functionality
  console.log('Exporting manufacturers to Excel...')
}

onMounted(() => {
  // Removed console.log
})
</script>

<style scoped>
.product-attribute-manufacturer-page {
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

.global-icon {
  color: #faad14;
}

.star-icon {
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

.logo-cell {
  display: flex;
  justify-content: center;
}
</style>
