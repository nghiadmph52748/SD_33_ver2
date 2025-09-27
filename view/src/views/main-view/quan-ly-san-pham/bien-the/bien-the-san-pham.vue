<template>
  <div class="product-variant-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="filters" layout="vertical">
        <!-- 3 Column x 3 Row Grid Layout -->
        <a-row :gutter="16">
          <!-- Row 1: Search - NSX - XX -->
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="filters.search" placeholder="Tên sản phẩm, mã SKU..." allow-clear @input="onSearchInput">
                <template #prefix>
                </template>
              </a-input>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="Nhà sản xuất">
              <a-select v-model="filters.manufacturer" placeholder="Chọn nhà sản xuất" allow-clear>
                <a-option value="">Tất cả</a-option>
                <a-option value="Nike">Nike</a-option>
                <a-option value="Adidas">Adidas</a-option>
                <a-option value="Puma">Puma</a-option>
                <a-option value="Gucci">Gucci</a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="Xuất xứ">
              <a-select v-model="filters.origin" placeholder="Chọn xuất xứ" allow-clear>
                <a-option value="">Tất cả</a-option>
                <a-option value="Việt Nam">Việt Nam</a-option>
                <a-option value="Trung Quốc">Trung Quốc</a-option>
                <a-option value="Mỹ">Mỹ</a-option>
                <a-option value="Đức">Đức</a-option>
                <a-option value="Nhật Bản">Nhật Bản</a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <!-- Row 2: Chất liệu - Đế giày - Trọng lượng -->
          <a-col :span="8">
            <a-form-item label="Chất liệu">
              <a-select v-model="filters.material" placeholder="Chọn chất liệu" allow-clear>
                <a-option value="">Tất cả</a-option>
                <a-option value="Da thật">Da thật</a-option>
                <a-option value="Da tổng hợp">Da tổng hợp</a-option>
                <a-option value="Vải">Vải</a-option>
                <a-option value="Nhựa">Nhựa</a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="Đế giày">
              <a-select v-model="filters.shoeSole" placeholder="Chọn đế giày" allow-clear>
                <a-option value="">Tất cả</a-option>
                <a-option value="Đế cao su">Đế cao su</a-option>
                <a-option value="Đế PU">Đế PU</a-option>
                <a-option value="Đế EVA">Đế EVA</a-option>
                <a-option value="Đế TPU">Đế TPU</a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="Trọng lượng">
              <a-select v-model="filters.weight" placeholder="Chọn trọng lượng" allow-clear>
                <a-option value="">Tất cả</a-option>
                <a-option value="0.3">Dưới 0.5kg</a-option>
                <a-option value="0.5">0.5kg</a-option>
                <a-option value="0.7">0.7kg</a-option>
                <a-option value="1.0">Trên 1.0kg</a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <!-- Row 3: Trạng thái - Khoảng giá -->
          <a-col :span="8">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="filters.status" type="button">
                <a-radio value="">Tất cả</a-radio>
                <a-radio value="active">Hoạt động</a-radio>
                <a-radio value="inactive">Không hoạt động</a-radio>
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
                  @change="performSearch"
                  style="width: 100%"
                />
              </div>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <!-- Empty column for balance -->
          </a-col>
        </a-row>

        <!-- Reset Button Outside Grid -->
        <div class="filter-actions" style="margin-top: 16px; text-align: right">
          <a-button @click="resetFilters" style="margin-right: 8px">
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
        </div>
      </a-form>
    </a-card>

    <!-- Variants Table -->
    <a-card :title="variantsTableTitle" class="table-card">
      <template #extra>
        <a-space>
          <a-button v-if="isEditMode && selectedVariants.length === 0" @click="toggleEditMode">
            <template #icon>
              <icon-close-circle />
            </template>
            Hủy
          </a-button>
          <a-button v-if="selectedVariants.length > 0 && isEditMode" type="primary" @click="completeBulkUpdate">
            <template #icon>
              <icon-check-circle />
            </template>
            Hoàn thành cập nhật
          </a-button>
          <a-button v-if="!isEditMode" @click="toggleShowAllVariants">
            <template #icon>
              <icon-eye />
            </template>
            {{ showAllVariants ? 'Hiển thị biến thể hiện tại' : 'Hiển thị toàn bộ biến thể' }}
          </a-button>
        </a-space>
      </template>

      <div v-if="selectedVariants.length > 0 && isEditMode" class="bulk-actions">
        <a-space>
          <span>Đã chọn {{ selectedVariants.length }} biến thể:</span>
          <a-button type="primary" size="small">Lưu thay đổi</a-button>
          <a-button size="small">Hủy</a-button>
        </a-space>
      </div>

      <a-table :columns="columns" :data="variants" :pagination="pagination" :loading="loading" :scroll="{ x: 1350 }">
        <template #checkbox="{ record }">
          <a-checkbox
            :checked="selectedVariants.includes(record.id.toString())"
            @change="(checked) => onRowSelect(record.id.toString(), checked)"
          />
        </template>

        <template #sku="{ record }">
          <a-typography-text copyable>{{ record.sku }}</a-typography-text>
        </template>

        <template #product_image="{ record }">
          <div class="image-cell">
            <a-avatar :src="record.product_image" :size="48" shape="square" />
          </div>
        </template>

        <template #manufacturer="{ record }">
          <div v-if="!isEditMode || !selectedVariants.includes(record.id.toString())">
            {{ record.manufacturer }}
          </div>
          <a-select v-else v-model="record.manufacturer" size="small" style="width: 100%" placeholder="Chọn nhà sản xuất">
            <a-option value="Nike">Nike</a-option>
            <a-option value="Adidas">Adidas</a-option>
            <a-option value="Puma">Puma</a-option>
            <a-option value="Gucci">Gucci</a-option>
          </a-select>
        </template>

        <template #origin="{ record }">
          <div v-if="!isEditMode || !selectedVariants.includes(record.id.toString())">
            {{ record.origin }}
          </div>
          <a-select v-else v-model="record.origin" size="small" style="width: 100%" placeholder="Chọn xuất xứ">
            <a-option value="Việt Nam">Việt Nam</a-option>
            <a-option value="Trung Quốc">Trung Quốc</a-option>
            <a-option value="Mỹ">Mỹ</a-option>
            <a-option value="Đức">Đức</a-option>
          </a-select>
        </template>

        <template #attributes="{ record }">
          <div class="attributes-cell">
            <div v-for="attr in record.attributes" :key="attr.label" class="attribute-item">
              <span class="attribute-label">{{ attr.label }}:</span>
              <span v-if="!isEditMode || !selectedVariants.includes(record.id.toString())" class="attribute-value">
                {{ attr.value }}
              </span>
              <a-input
                v-else-if="attr.label === 'Trọng lượng'"
                v-model="attr.value"
                size="small"
                style="width: 80px"
                placeholder="0.0 kg"
              />
              <a-select
                v-else-if="attr.label === 'Chất liệu'"
                v-model="attr.value"
                size="small"
                style="width: 100px"
                placeholder="Chọn chất liệu"
              >
                <a-option value="Da thật">Da thật</a-option>
                <a-option value="Da tổng hợp">Da tổng hợp</a-option>
                <a-option value="Vải">Vải</a-option>
                <a-option value="Nhựa">Nhựa</a-option>
              </a-select>
              <a-select
                v-else-if="attr.label === 'Đế giày'"
                v-model="attr.value"
                size="small"
                style="width: 100px"
                placeholder="Chọn đế giày"
              >
                <a-option value="Đế cao su">Đế cao su</a-option>
                <a-option value="Đế PU">Đế PU</a-option>
                <a-option value="Đế EVA">Đế EVA</a-option>
              </a-select>
            </div>
          </div>
        </template>

        <template #stock="{ record }">
          <div v-if="!isEditMode || !selectedVariants.includes(record.id.toString())">{{ record.stock }} {{ record.unit }}</div>
          <a-input-number v-else v-model="record.stock" :min="0" size="small" style="width: 80px" :precision="0" />
        </template>

        <template #discount="{ record }">
          <div v-if="!isEditMode || !selectedVariants.includes(record.id.toString())">{{ record.discount }}%</div>
          <a-input-number v-else v-model="record.discount" :min="0" :max="100" size="small" style="width: 80px" :precision="1" suffix="%" />
        </template>

        <template #final_price="{ record }">
          <div class="final-price-cell">
            <div class="final-price">{{ formatCurrency(record.final_price) }}</div>
            <div v-if="record.discount > 0" class="original-price">{{ formatCurrency(record.price) }}</div>
          </div>
        </template>

        <template #status="{ record }">
          <a-tag
            v-if="!isEditMode || !selectedVariants.includes(record.id.toString())"
            :color="record.status === 'active' ? 'green' : 'red'"
          >
            {{ record.status === 'active' ? 'Đang bán' : 'Tạm ngưng bán' }}
          </a-tag>
          <a-select v-else v-model="record.status" size="small" style="width: 120px">
            <a-option value="active">Đang bán</a-option>
            <a-option value="inactive">Tạm ngưng bán</a-option>
          </a-select>
        </template>

        <template #action="{ record }">
          <a-space>
            <a-button type="text" @click="editVariant(record)">
              <template #icon>
                <icon-edit />
              </template>
            </a-button>
            <a-button type="text" danger @click="deleteVariant(record)">
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
import { ref, computed, onMounted, watch } from 'vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import {
  IconPlus,
  IconSearch,
  IconGift,
  IconStorage,
  IconExclamationCircle,
  IconCloseCircle,
  IconEdit,
  IconCheck,
  IconRefresh,
  IconDelete,
  IconDownload,
  IconEye,
  IconCheckCircle,
} from '@arco-design/web-vue/es/icon'

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()

// Filters
const filters = ref({
  search: '', // Tìm theo tên sản phẩm hoặc SKU
  manufacturer: '',
  origin: '',
  material: '',
  shoeSole: '',
  weight: '',
  priceRange: [0, 5000000], // Khoảng giá: [min, max]
  status: '', // Trạng thái: active/inactive
})

// Table
const loading = ref(false)
const selectedVariants = ref<string[]>([])
const isEditMode = ref(false)
const showAllVariants = ref(false)

// Store original variants for filtering
const originalVariants = ref([])

// Computed title for variants table
const variantsTableTitle = computed(() => {
  // Determine category name based on current filters
  let categoryName = 'Tất cả biến thể'

  // If filtering by manufacturer, use manufacturer as category name
  if (filters.value.manufacturer) {
    const manufacturerOptions = [
      { value: 'nike', label: 'Nike' },
      { value: 'adidas', label: 'Adidas' },
      { value: 'gucci', label: 'Gucci' },
      { value: 'puma', label: 'Puma' },
    ]
    const manufacturer = manufacturerOptions.find((m) => m.value === filters.value.manufacturer)
    if (manufacturer) {
      categoryName = manufacturer.label
    }
  }
  // If showing all variants explicitly
  else if (showAllVariants.value) {
    categoryName = 'Tất cả biến thể'
  }
  // If has other active filters but no specific manufacturer
  else if (
    filters.value.search ||
    filters.value.origin ||
    filters.value.material ||
    filters.value.shoeSole ||
    filters.value.weight ||
    (filters.value.status && filters.value.status !== '') ||
    (Array.isArray(filters.value.priceRange) && (filters.value.priceRange[0] > 0 || filters.value.priceRange[1] < 5000000))
  ) {
    categoryName = 'Biến thể đã lọc'
  }

  return `Danh sách biến thể - ${categoryName}`
})

const columns = [
  {
    title: '',
    slotName: 'checkbox',
    width: 30,
    fixed: 'left',
    align: 'center',
  },
  {
    title: 'Mã biến thể',
    dataIndex: 'sku',
    slotName: 'sku',
    width: 90,
  },
  {
    title: 'Ảnh sản phẩm',
    dataIndex: 'product_image',
    slotName: 'product_image',
    width: 98,
    align: 'center',
  },
  {
    title: 'Nhà sản xuất',
    dataIndex: 'manufacturer',
    slotName: 'manufacturer',
    width: 90,
    align: 'center',
  },
  {
    title: 'Xuất xứ',
    dataIndex: 'origin',
    slotName: 'origin',
    width: 90,
    align: 'center',
  },
  {
    title: 'Thuộc tính sản phẩm',
    dataIndex: 'attributes',
    slotName: 'attributes',
    width: 130,
  },
  {
    title: 'Số lượng',
    dataIndex: 'stock',
    slotName: 'stock',
    width: 68,
    align: 'center',
  },
  {
    title: 'Giảm giá (%)',
    dataIndex: 'discount',
    slotName: 'discount',
    width: 85,
    align: 'center',
  },
  {
    title: 'Giá sau giảm',
    dataIndex: 'final_price',
    slotName: 'final_price',
    width: 100,
  },
  {
    title: 'Trạng thái',
    dataIndex: 'status',
    slotName: 'status',
    width: 80,
    align: 'center',
  },
  {
    title: 'Thao tác',
    slotName: 'action',
    width: 80,
    fixed: 'right',
    align: 'center',
  },
]

// Pagination
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 50,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: true,
})

// Mock data

const availableSizes = ref(['35', '36', '37', '38', '39', '40', '41', '42'])

const variants = ref([
  {
    id: 1,
    product_name: 'Giày sneaker Nike Air Max',
    product_image: 'https://via.placeholder.com/80x80/1890ff/ffffff?text=Nike',
    sku: 'NK-AM270-38-BLUE',
    manufacturer: 'Nike',
    origin: 'Việt Nam',
    material: 'Da tổng hợp',
    shoeSole: 'Đế PU',
    weight: '0.5',
    attributes: [
      { label: 'Màu sắc', value: 'Xanh dương' },
      { label: 'Kích thước', value: '38' },
      { label: 'Chất liệu', value: 'Da tổng hợp' },
      { label: 'Đế giày', value: 'Đế PU' },
      { label: 'Trọng lượng', value: '0.5 kg' },
    ],
    stock: 25,
    price: 3200000,
    discount: 10,
    final_price: 2880000,
    status: 'active',
    unit: 'đôi',
  },
  {
    id: 2,
    product_name: 'Giày boot Chelsea',
    product_image: 'https://via.placeholder.com/80x80/52c41a/ffffff?text=Boot',
    sku: 'BT-CH-37-BLACK',
    manufacturer: 'Adidas',
    origin: 'Trung Quốc',
    material: 'Da thật',
    shoeSole: 'Đế cao su',
    weight: '0.7',
    attributes: [
      { label: 'Màu sắc', value: 'Đen' },
      { label: 'Kích thước', value: '37' },
      { label: 'Chất liệu', value: 'Da thật' },
      { label: 'Đế giày', value: 'Đế cao su' },
      { label: 'Trọng lượng', value: '0.7 kg' },
    ],
    stock: 8,
    price: 4500000,
    discount: 0,
    final_price: 4500000,
    status: 'active',
    unit: 'đôi',
  },
  {
    id: 3,
    product_name: 'Giày thể thao Puma',
    product_image: 'https://via.placeholder.com/80x80/f5222d/ffffff?text=Puma',
    sku: 'PM-SPRT-39-WHITE',
    manufacturer: 'Puma',
    origin: 'Mỹ',
    material: 'Vải',
    shoeSole: 'Đế EVA',
    weight: '0.4',
    attributes: [
      { label: 'Màu sắc', value: 'Trắng' },
      { label: 'Kích thước', value: '39' },
      { label: 'Chất liệu', value: 'Vải' },
      { label: 'Đế giày', value: 'Đế EVA' },
      { label: 'Trọng lượng', value: '0.4 kg' },
    ],
    stock: 15,
    price: 2800000,
    discount: 5,
    final_price: 2660000,
    status: 'inactive',
    unit: 'đôi',
  },
  {
    id: 4,
    product_name: 'Giày chạy bộ Adidas Ultraboost',
    product_image: 'https://via.placeholder.com/80x80/722ed1/ffffff?text=Adidas',
    sku: 'AD-UB-40-GRAY',
    manufacturer: 'Adidas',
    origin: 'Đức',
    material: 'Vải',
    shoeSole: 'Đế EVA',
    weight: '0.3',
    attributes: [
      { label: 'Màu sắc', value: 'Xám' },
      { label: 'Kích thước', value: '40' },
      { label: 'Chất liệu', value: 'Vải' },
      { label: 'Đế giày', value: 'Đế EVA' },
      { label: 'Trọng lượng', value: '0.3 kg' },
    ],
    stock: 12,
    price: 5200000,
    discount: 15,
    final_price: 4420000,
    status: 'active',
    unit: 'đôi',
  },
  {
    id: 5,
    product_name: 'Giày Gucci Marmont',
    product_image: 'https://via.placeholder.com/80x80/13c2c2/ffffff?text=Gucci',
    sku: 'GC-MM-36-BEIGE',
    manufacturer: 'Gucci',
    origin: 'Ý',
    material: 'Da thật',
    shoeSole: 'Đế TPU',
    weight: '0.6',
    attributes: [
      { label: 'Màu sắc', value: 'Beige' },
      { label: 'Kích thước', value: '36' },
      { label: 'Chất liệu', value: 'Da thật' },
      { label: 'Đế giày', value: 'Đế TPU' },
      { label: 'Trọng lượng', value: '0.6 kg' },
    ],
    stock: 5,
    price: 15000000,
    discount: 0,
    final_price: 15000000,
    status: 'active',
    unit: 'đôi',
  },
])

// Methods
const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const formatPrice = (price: number) => {
  if (price >= 1000000) {
    return `${(price / 1000000).toFixed(1)}M`
  }

  if (price >= 1000) {
    return `${(price / 1000).toFixed(0)}k`
  }

  return price.toString()
}

// Debounce timer for search
let searchTimer: number | null = null

const performSearch = () => {
  loading.value = true

  // Frontend search implementation
  setTimeout(() => {
    let filteredVariants = [...originalVariants.value]

    // Search by product name or SKU
    if (filters.value.search) {
      const searchTerm = filters.value.search.toLowerCase()
      filteredVariants = filteredVariants.filter(
        (variant) => variant.product_name.toLowerCase().includes(searchTerm) || variant.sku.toLowerCase().includes(searchTerm)
      )
    }

    // Filter by manufacturer
    if (filters.value.manufacturer) {
      filteredVariants = filteredVariants.filter((variant) => variant.manufacturer === filters.value.manufacturer)
    }

    // Filter by origin
    if (filters.value.origin) {
      filteredVariants = filteredVariants.filter((variant) => variant.origin === filters.value.origin)
    }

    // Filter by material
    if (filters.value.material) {
      filteredVariants = filteredVariants.filter((variant) => variant.material === filters.value.material)
    }

    // Filter by shoe sole
    if (filters.value.shoeSole) {
      filteredVariants = filteredVariants.filter((variant) => variant.shoeSole === filters.value.shoeSole)
    }

    // Filter by weight
    if (filters.value.weight) {
      filteredVariants = filteredVariants.filter((variant) => {
        const weight = parseFloat(variant.weight)
        if (filters.value.weight === '0.3') return weight < 0.5
        if (filters.value.weight === '0.5') return weight >= 0.5 && weight < 0.7
        if (filters.value.weight === '0.7') return weight >= 0.7 && weight < 1.0
        if (filters.value.weight === '1.0') return weight >= 1.0
        return true
      })
    }

    // Filter by price range (slider: [min, max])
    if (filters.value.priceRange && Array.isArray(filters.value.priceRange)) {
      const [minPrice, maxPrice] = filters.value.priceRange
      filteredVariants = filteredVariants.filter((variant) => {
        const { price } = variant
        return price >= minPrice && price <= maxPrice
      })
    }

    // Filter by status (only filter if not "Tất cả")
    if (filters.value.status && filters.value.status !== '') {
      filteredVariants = filteredVariants.filter((variant) => variant.status === filters.value.status)
    }

    // Update displayed variants
    variants.value = filteredVariants

    console.log('Filtered variants:', filteredVariants.length, 'from', originalVariants.value.length, 'total') // eslint-disable-line no-console

    loading.value = false
  }, 200)
}

const onSearchInput = () => {
  // Clear existing timer
  if (searchTimer) {
    clearTimeout(searchTimer)
  }

  // Set new timer for debounced search
  searchTimer = setTimeout(() => {
    performSearch()
  }, 500) // 500ms debounce
}

const resetFilters = () => {
  // Reset all filters
  filters.value = {
    search: '',
    manufacturer: '',
    origin: '',
    material: '',
    shoeSole: '',
    weight: '',
    priceRange: [0, 5000000],
    status: '',
  }

  // Reset to show all variants
  variants.value = [...originalVariants.value]
}

const exportExcel = () => {
  // Removed console.log
  // Implement Excel export logic
}

const completeBulkUpdate = () => {
  // Implement bulk update logic
  // This would save all changes made to selected variants
  console.log('Completing bulk update for selected variants:', selectedVariants.value) // eslint-disable-line no-console

  // Reset edit mode and selections
  isEditMode.value = false
  selectedVariants.value = []
}

const showCreateModal = () => {
  // Removed console.log
}

// Selection methods
const onSelectChange = (selectedRowKeys: string[]) => {
  selectedVariants.value = selectedRowKeys
}

const onRowSelect = (id: string, checked: boolean) => {
  if (checked) {
    if (!selectedVariants.value.includes(id)) {
      selectedVariants.value.push(id)
    }
  } else {
    const index = selectedVariants.value.indexOf(id)
    if (index > -1) {
      selectedVariants.value.splice(index, 1)
    }
  }
}

// Edit mode methods
const toggleEditMode = () => {
  isEditMode.value = !isEditMode.value
  if (!isEditMode.value) {
    selectedVariants.value = []
  }
}

const toggleShowAllVariants = () => {
  showAllVariants.value = !showAllVariants.value
  if (showAllVariants.value) {
    // Hiển thị tất cả variants
    variants.value = [...originalVariants.value]
  } else {
    // Hiển thị variants đã filter
    performSearch()
  }
}

const editVariant = (variant: any) => {
  // Removed console.log
}

const deleteVariant = (variant: any) => {
  // Removed console.log
  // Implement delete logic
}

// Watch for filter changes (excluding search which is handled by onSearchInput)
watch(
  () => [
    filters.value.manufacturer,
    filters.value.origin,
    filters.value.material,
    filters.value.shoeSole,
    filters.value.weight,
    filters.value.priceRange,
    filters.value.status,
  ],
  () => {
    performSearch()
  }
)

onMounted(() => {
  // Initialize with mock data
  const mockVariants = [
    {
      id: '1',
      sku: 'NK-AIR-RED-36',
      product_name: 'Nike Air Max 270',
      manufacturer: 'Nike',
      origin: 'Việt Nam',
      color: 'red',
      size: '36',
      material: 'Vải tổng hợp',
      shoeSole: 'Đế cao su',
      weight: '0.8',
      price: 2500000,
      discount: 10,
      final_price: 2250000,
      stock: 25,
      status: 'active',
      image: 'https://via.placeholder.com/80',
      attributes: [
        { label: 'Màu sắc', value: 'Đỏ' },
        { label: 'Kích thước', value: '36' },
        { label: 'Chất liệu', value: 'Vải tổng hợp' },
        { label: 'Đế giày', value: 'Đế cao su' },
      ],
    },
    {
      id: '2',
      sku: 'NK-AIR-BLUE-37',
      product_name: 'Nike Air Max 270',
      manufacturer: 'Nike',
      origin: 'Việt Nam',
      color: 'blue',
      size: '37',
      material: 'Da tổng hợp',
      shoeSole: 'Đế EVA',
      weight: '0.9',
      price: 2600000,
      discount: 5,
      final_price: 2470000,
      stock: 15,
      status: 'active',
      image: 'https://via.placeholder.com/80',
      attributes: [
        { label: 'Màu sắc', value: 'Xanh dương' },
        { label: 'Kích thước', value: '37' },
        { label: 'Chất liệu', value: 'Da tổng hợp' },
        { label: 'Đế giày', value: 'Đế EVA' },
      ],
    },
    {
      id: '3',
      sku: 'AD-ULTRA-BLK-38',
      product_name: 'Adidas Ultraboost 22',
      manufacturer: 'Adidas',
      origin: 'Trung Quốc',
      color: 'black',
      size: '38',
      material: 'Vải lưới',
      shoeSole: 'Đế Boost',
      weight: '0.7',
      price: 3200000,
      discount: 15,
      final_price: 2720000,
      stock: 8,
      status: 'active',
      image: 'https://via.placeholder.com/80',
      attributes: [
        { label: 'Màu sắc', value: 'Đen' },
        { label: 'Kích thước', value: '38' },
        { label: 'Chất liệu', value: 'Vải lưới' },
        { label: 'Đế giày', value: 'Đế Boost' },
      ],
    },
    {
      id: '4',
      sku: 'PU-GAZ-WHT-39',
      product_name: 'Puma Gazelle',
      manufacturer: 'Puma',
      origin: 'Việt Nam',
      color: 'white',
      size: '39',
      material: 'Da thật',
      shoeSole: 'Đế cao su',
      weight: '0.6',
      price: 1800000,
      discount: 0,
      final_price: 1800000,
      stock: 0,
      status: 'inactive',
      image: 'https://via.placeholder.com/80',
      attributes: [
        { label: 'Màu sắc', value: 'Trắng' },
        { label: 'Kích thước', value: '39' },
        { label: 'Chất liệu', value: 'Da thật' },
        { label: 'Đế giày', value: 'Đế cao su' },
      ],
    },
    {
      id: '5',
      sku: 'NK-JORDAN-GRN-40',
      product_name: 'Nike Air Jordan 1',
      manufacturer: 'Nike',
      origin: 'Mỹ',
      color: 'green',
      size: '40',
      material: 'Da tổng hợp',
      shoeSole: 'Đế cao su',
      weight: '1.0',
      price: 4500000,
      discount: 20,
      final_price: 3600000,
      stock: 12,
      status: 'active',
      image: 'https://via.placeholder.com/80',
      attributes: [
        { label: 'Màu sắc', value: 'Xanh lá' },
        { label: 'Kích thước', value: '40' },
        { label: 'Chất liệu', value: 'Da tổng hợp' },
        { label: 'Đế giày', value: 'Đế cao su' },
      ],
    },
  ]

  // Store original data and display data
  originalVariants.value = mockVariants
  variants.value = mockVariants
})
</script>

<style scoped>
.product-variant-page {
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

.active-icon {
  color: #52c41a;
}

.warning-icon {
  color: #faad14;
}

.danger-icon {
  color: #ff4d4f;
}

.filters-card {
  margin-bottom: 16px;
}

.filters-card .arco-form-item-label {
  font-weight: 500;
  color: #1d2129;
}

.table-card {
  margin-bottom: 16px;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-details {
  flex: 1;
}

.product-name {
  font-weight: 500;
  color: #1d2129;
  margin-bottom: 4px;
}

.product-sku {
  font-size: 12px;
  color: #86909c;
}

.variant-attributes {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.stock-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stock-price {
  font-size: 12px;
  color: #1890ff;
  font-weight: 500;
}

.bulk-actions {
  padding: 12px 16px;
  background-color: var(--color-primary-1);
  border-radius: 6px;
  margin-bottom: 16px;
  border: 1px solid var(--color-primary-3);
}

.image-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.attributes-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 250px;
}

.attribute-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}

.attribute-label {
  font-weight: 500;
  color: #86909c;
  min-width: 60px;
}

.attribute-value {
  color: #1d2129;
}

.final-price-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.final-price {
  font-weight: 500;
  color: #1d2129;
}

.original-price {
  font-size: 12px;
  color: #86909c;
  text-decoration: line-through;
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
</style>
