<template>
  <div class="product-category-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Search and Filter -->
    <a-card class="filters-card">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <!-- Row 1 -->
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="filters.search" placeholder="Tìm theo mã, tên hoặc khoảng giá..." allow-clear @input="searchCategories" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="filters.status" type="button" @change="searchCategories">
                <a-radio value="">Tất cả</a-radio>
                <a-radio value="active">Đang bán</a-radio>
                <a-radio value="inactive">Tạm ngưng bán</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="Khoảng giá">
              <div class="price-range-container">
                <a-slider
                  v-model="filters.priceRange"
                  :min="0"
                  :max="5000000"
                  :step="50000"
                  range
                  tooltip-visible
                  :tooltip-formatter="formatPrice"
                  @change="searchCategories"
                  style="width: 100%"
                />
              </div>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>

      <div class="reset-button-container">
        <div class="action-buttons">
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
            <a-button v-if="!hasSelectedRows" type="primary" @click="navigateToAddProduct">
              <template #icon>
                <icon-plus />
              </template>
              Thêm sản phẩm
            </a-button>
          </a-space>
        </div>
      </div>
    </a-card>

    <!-- Categories Table -->
    <a-card title="Danh sách danh mục" class="table-card">
      <template #extra>
        <a-button v-if="hasSelectedRows" type="primary" @click="completeBulkUpdate">
          <template #icon>
            <icon-check-circle />
          </template>
          Hoàn thành cập nhật
        </a-button>
      </template>

      <a-table
        :columns="columns"
        :data="filteredCategories"
        :pagination="pagination"
        :loading="loading"
        :scroll="{ x: 800 }"
        @change="handleTableChange"
      >
        <template #checkbox="{ record }">
          <a-checkbox
            :checked="isRowSelected(record.id)"
            @change="
              (checked) => {
                if (checked) {
                  // Add to selection and initialize editing data with current values
                  const currentKeys = Array.isArray(selectedRowKeys.value) ? selectedRowKeys.value : []
                  if (!currentKeys.includes(record.id)) {
                    selectedRowKeys.value = [...currentKeys, record.id]
                  }
                  if (!editingData.value) editingData.value = {}
                  editingData.value[record.id] = {
                    name: record.name,
                    status: record.status,
                  }
                } else {
                  // Remove from selection and editing data
                  const currentKeys = Array.isArray(selectedRowKeys.value) ? selectedRowKeys.value : []
                  selectedRowKeys.value = currentKeys.filter((id) => id !== record.id)
                  delete editingData.value[record.id]
                }
              }
            "
          />
        </template>

        <template #status="{ record }">
          <div v-if="isRowSelected(record.id)">
            <!-- Edit mode -->
            <a-select
              :model-value="editingData.value[record.id]?.status || record.status"
              @update:model-value="
                (value) => {
                  if (!editingData.value) editingData.value = {}
                  editingData.value[record.id] = { ...(editingData.value[record.id] || {}), status: value }
                }
              "
              size="mini"
              style="width: 100px"
            >
              <a-option value="active">Đang bán</a-option>
              <a-option value="inactive">Tạm ngưng bán</a-option>
            </a-select>
          </div>
          <a-tag v-else :color="record.status === 'active' ? 'green' : 'red'">
            {{ record.status === 'active' ? 'Đang bán' : 'Tạm ngưng bán' }}
          </a-tag>
        </template>

        <template #name="{ record }">
          <div v-if="isRowSelected(record.id)">
            <!-- Edit mode for name -->
            <a-input
              :model-value="editingData.value[record.id]?.name || record.name"
              @update:model-value="
                (value) => {
                  if (!editingData.value) editingData.value = {}
                  editingData.value[record.id] = { ...(editingData.value[record.id] || {}), name: value }
                }
              "
              size="mini"
              style="width: 200px"
            />
          </div>
          <span v-else class="custom-name">{{ record.name }}</span>
        </template>

        <template #price_range="{ record }">
          <span class="custom-price-range">{{ record.price_range }}</span>
        </template>

        <template #action="{ record }">
          <a-button type="text" @click="viewCategory(record)">
            <template #icon>
              <icon-eye />
            </template>
          </a-button>
        </template>

        <template #checkbox-title>
          <a-checkbox
            :checked="selectedCount === filteredCategories.length && filteredCategories.length > 0"
            :indeterminate="selectedCount > 0 && selectedCount < filteredCategories.length"
            @change="
              (checked) => {
                if (checked) {
                  // Select all filtered categories and initialize editing data
                  selectedRowKeys.value = [...filteredCategories.map((cat) => cat.id)]
                  if (!editingData.value) editingData.value = {}
                  filteredCategories.forEach((category) => {
                    editingData.value[category.id] = {
                      name: category.name,
                      status: category.status,
                    }
                  })
                } else {
                  // Deselect all
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
import { ref, reactive, computed, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import { debounce } from 'lodash'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import {
  IconPlus,
  IconSearch,
  IconRefresh,
  IconDownload,
  IconMore,
  IconEye,
  IconEdit,
  IconDelete,
  IconCheckCircle,
  IconPauseCircle,
  IconImage,
} from '@arco-design/web-vue/es/icon'

// Form and modal
// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()
const router = useRouter()

const modalVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

// Edit inline mode state
const selectedRowKeys = ref<number[]>([])
const editingData = ref<Record<number, { name: string; status: string }>>({})

// Computed properties for safe access
const selectedCount = computed(() => (Array.isArray(selectedRowKeys.value) ? selectedRowKeys.value.length : 0))
const hasSelectedRows = computed(() => selectedCount.value > 0)
const isRowSelected = (id: number) => {
  const keys = Array.isArray(selectedRowKeys.value) ? selectedRowKeys.value : []
  return keys.includes(id)
}

// Form data
const formData = reactive({
  name: '',
  description: '',
  status: 'active',
  image: '',
})

const fileList = ref([])

// Filters
const filters = reactive({
  search: '',
  status: '',
  priceRange: [0, 5000000], // min and max price range
})

// Table
const loading = ref(false)
const columns = [
  {
    title: '',
    dataIndex: 'checkbox',
    width: 50,
    align: 'center',
    slotName: 'checkbox',
    titleSlotName: 'checkbox-title',
  },
  {
    title: 'Mã sản phẩm',
    dataIndex: 'code',
    width: 110,
  },
  {
    title: 'Tên sản phẩm',
    dataIndex: 'name',
    slotName: 'name',
    width: 250,
  },
  {
    title: 'Nhà sản xuất',
    dataIndex: 'manufacturer',
    width: 110,
    align: 'center',
  },
  {
    title: 'Xuất xứ',
    dataIndex: 'origin',
    width: 100,
    align: 'center',
  },
  {
    title: 'Số lượng biến thể',
    dataIndex: 'variant_count',
    width: 150,
    align: 'center',
  },
  {
    title: 'Khoảng giá(đ)',
    dataIndex: 'price_range',
    slotName: 'price_range',
    width: 200,
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
    width: 80,
    align: 'center',
  },
]

// Mock data
const categories = ref([
  {
    id: 1,
    code: 'SP001',
    name: 'Giày sneaker',
    variant_count: 25,
    price_range: '200.000 - 500.000',
    manufacturer: 'Nike',
    origin: 'Việt Nam',
    status: 'active',
  },
  {
    id: 2,
    code: 'SP002',
    name: 'Giày boot',
    variant_count: 18,
    price_range: '800.000 - 1.800.000',
    manufacturer: 'Adidas',
    origin: 'Trung Quốc',
    status: 'active',
  },
  {
    id: 3,
    code: 'SP003',
    name: 'Giày cao gót',
    variant_count: 12,
    price_range: '150.000 - 400.000',
    manufacturer: 'Gucci',
    origin: 'Ý',
    status: 'active',
  },
  {
    id: 4,
    code: 'SP004',
    name: 'Giày thể thao',
    variant_count: 30,
    price_range: '2.500.000 - 5.000.000',
    manufacturer: 'Puma',
    origin: 'Đức',
    status: 'active',
  },
  {
    id: 5,
    code: 'SP005',
    name: 'Giày sandal',
    variant_count: 8,
    price_range: '80.000 - 150.000',
    manufacturer: "Biti's",
    origin: 'Việt Nam',
    status: 'inactive',
  },
  {
    id: 6,
    code: 'SP006',
    name: 'Giày lười',
    variant_count: 15,
    price_range: '50.000 - 100.000',
    manufacturer: 'Crocs',
    origin: 'Mỹ',
    status: 'active',
  },
  {
    id: 7,
    code: 'SP007',
    name: 'Giày da',
    variant_count: 20,
    price_range: '1.200.000 - 3.000.000',
    manufacturer: 'Louis Vuitton',
    origin: 'Pháp',
    status: 'active',
  },
])

// Filtered categories computed property
const filteredCategories = computed(() => {
  return categories.value.filter((category) => {
    // Filter by status
    if (filters.status && category.status !== filters.status) {
      return false
    }

    // Filter by price range
    const [minPrice, maxPrice] = filters.priceRange
    if (minPrice > 0 || maxPrice < 5000000) {
      const priceText = category.price_range.replace(/[^\d-]/g, '') // Remove non-numeric chars except dash
      const prices = priceText.split('-').map((p) => parseInt(p, 10))
      const categoryMinPrice = prices[0]
      const categoryMaxPrice = prices[prices.length - 1]

      // Check if category price range overlaps with filter range
      return categoryMaxPrice >= minPrice && categoryMinPrice <= maxPrice
    }

    // Filter by search (code, name, or price range)
    if (filters.search) {
      const searchTerm = filters.search.toLowerCase()
      const matchesCode = category.code.toLowerCase().includes(searchTerm)
      const matchesName = category.name.toLowerCase().includes(searchTerm)
      const matchesPrice = category.price_range.toLowerCase().includes(searchTerm)

      if (!matchesCode && !matchesName && !matchesPrice) {
        return false
      }
    }

    return true
  })
})

// Pagination
const pagination = computed(() => ({
  current: 1,
  pageSize: 10,
  total: filteredCategories.value.length,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: true,
}))

// Form validation rules
const formRules = {
  name: [
    { required: true, message: 'Vui lòng nhập tên danh mục' },
    { min: 2, max: 50, message: 'Tên danh mục phải từ 2-50 ký tự' },
  ],
  description: [{ max: 200, message: 'Mô tả không được vượt quá 200 ký tự' }],
}

// Methods
const searchCategories = debounce(() => {
  // Removed console.log
  // Implement search logic
}, 300)

const resetFilters = () => {
  filters.value = {
    search: '',
    status: '',
    priceRange: [0, 5000000],
  }
}

// Format price for display
const formatPrice = (price: number) => {
  if (price >= 1000000) {
    return `${(price / 1000000).toFixed(1)}M`
  }

  if (price >= 1000) {
    return `${(price / 1000).toFixed(0)}k`
  }

  return price.toString()
}

const navigateToAddProduct = () => {
  router.push({ name: 'ThemSanPham' })
}

const showCreateModal = () => {
  isEdit.value = false
  formData.name = ''
  formData.description = ''
  formData.status = 'active'
  formData.image = ''
  fileList.value = []
  modalVisible.value = true
}

const editCategory = (category: any) => {
  isEdit.value = true
  formData.name = category.name
  formData.description = category.description
  formData.status = category.status
  formData.image = category.image
  fileList.value = category.image ? [{ url: category.image }] : []
  modalVisible.value = true
}

const viewCategory = (category: any) => {
  // Removed console.log
  // Implement view logic
}

const deleteCategory = (category: any) => {
  // Removed console.log
  // Implement delete logic
}

const toggleStatus = (category: any) => {
  // Removed console.log
  // Implement toggle logic
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    // Removed console.log
    // Implement submit logic
    modalVisible.value = false
  } catch (error) {
    // console.error('Form validation failed:', error)
  }
}

const handleCancel = () => {
  modalVisible.value = false
}

const handleTableChange = (paginationData: any, filtersData: any, sorter: any) => {
  // Handle table change
}

const completeBulkUpdate = () => {
  // Update categories with edited data
  selectedRowKeys.value.forEach((id) => {
    const category = categories.value.find((cat) => cat.id === id)
    const editData = editingData.value[id]
    if (category && editData) {
      category.name = editData.name
      category.status = editData.status
    }
  })

  // Clear selection and editing data
  selectedRowKeys.value = []
  editingData.value = {}
}

const exportExcel = () => {
  // Removed console.log
  // Implement export logic
}

const beforeUpload = (file: File) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpgOrPng) {
    // console.error('Chỉ được upload file JPG/PNG!')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    // console.error('Ảnh phải nhỏ hơn 2MB!')
    return false
  }
  return false // Prevent auto upload
}

const handleUploadChange = (info: any) => {
  // Removed console.log
  // Handle upload logic
}

onMounted(() => {
  // Ensure reactive state is properly initialized
  selectedRowKeys.value = Array.isArray(selectedRowKeys.value) ? selectedRowKeys.value : []
  editingData.value = editingData.value && typeof editingData.value === 'object' ? editingData.value : {}
})
</script>

<style scoped>
.product-category-page {
  padding: 0 20px 20px 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.header-left {
  flex: 1;
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

.header-right {
  display: flex;
  gap: 16px;
}

.filters-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  background: var(--color-bg-2);
  margin-bottom: 16px;
}

.table-card {
  margin-bottom: 16px;
}

/* Form layout adjustments for vertical layout */
.filters-card .arco-form-item {
  margin-bottom: 0;
}

.filters-card .arco-form-item-label {
  font-weight: 500;
  color: var(--color-text-1);
  margin-bottom: 8px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.action-buttons .arco-space {
  flex-direction: column !important;
}

.action-buttons .arco-btn {
  width: 100%;
  justify-content: flex-start;
}

.price-range-container {
  padding: 9px 16px;
  background-color: #fafafa;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
  width: 100%;
}

.price-range-container:hover {
  border-color: #d9d9d9;
}

/* Prevent dropdown menu wrapping */
.arco-dropdown-menu {
  white-space: nowrap;
  min-width: 150px;
  z-index: 1050;
}

.arco-dropdown-menu-item {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Ensure dropdown popup is not constrained */
.arco-dropdown-popup {
  z-index: 1050;
  overflow: visible;
}

.arco-dropdown-popup .arco-dropdown-menu {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.reset-button-container {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 8px 0;
}

.action-buttons {
  display: flex;
}
.action-buttons .arco-space {
  flex-direction: row !important;
}

.action-buttons .arco-btn {
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.action-buttons .arco-btn:hover {
  transform: translateY(-1px);
}

.danger-item {
  color: #ff4d4f;
}

.custom-name {
  white-space: nowrap;
}

.custom-price-range {
  white-space: nowrap;
}

/* Responsive */
@media (max-width: 768px) {
  .product-category-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .header-right {
    width: 100%;
    justify-content: center;
  }

  /* Responsive form layout */
  .filters-card .arco-row .arco-col {
    margin-bottom: 16px;
  }

  .filters-card .arco-row .arco-col:last-child {
    margin-bottom: 0;
  }

  .reset-button-container {
    justify-content: center;
  }

  .action-buttons {
    justify-content: center;
  }

  .action-buttons .arco-space {
    flex-direction: column !important;
    width: 100%;
  }

  .price-range-container {
    padding: 6px 0;
  }
}
</style>
