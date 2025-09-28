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
              <a-input v-model="filters.search" placeholder="Tên sản phẩm, mã SKU..." allow-clear @input="onSearchInput" />
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

      <a-table
        :columns="columns"
        :data="variants"
        :pagination="{
          ...pagination,
          onChange: handlePageChange,
        }"
        :loading="loading"
        size="small"
      >
        <template #checkbox="{ record }">
          <a-checkbox
            :checked="selectedVariants.includes(record.id.toString())"
            @change="(checked) => onRowSelect(record.id.toString(), checked)"
          />
        </template>

        <template #product_image="{ record }">
          <div class="image-cell">
            <a-avatar
              :src="Array.isArray(record.anhSanPham) && record.anhSanPham.length > 0 ? record.anhSanPham[0] : '/default-product.png'"
              :size="48"
              shape="square"
            />
          </div>
        </template>

        <template #manufacturer="{ record }">
          <div v-if="!isEditMode || !selectedVariants.includes(record.id.toString())">
            {{ record.tenNhaSanXuat }}
          </div>
          <a-select v-else v-model="record.tenNhaSanXuat" size="small" style="width: 100%" placeholder="Chọn nhà sản xuất">
            <a-option value="Nike">Nike</a-option>
            <a-option value="Adidas">Adidas</a-option>
            <a-option value="Puma">Puma</a-option>
            <a-option value="Gucci">Gucci</a-option>
          </a-select>
        </template>

        <template #origin="{ record }">
          <div v-if="!isEditMode || !selectedVariants.includes(record.id.toString())">
            {{ record.tenXuatXu }}
          </div>
          <a-select v-else v-model="record.tenXuatXu" size="small" style="width: 100%" placeholder="Chọn xuất xứ">
            <a-option value="Việt Nam">Việt Nam</a-option>
            <a-option value="Trung Quốc">Trung Quốc</a-option>
            <a-option value="Mỹ">Mỹ</a-option>
            <a-option value="Đức">Đức</a-option>
          </a-select>
        </template>

        <template #attributes="{ record }">
          <div class="attributes-cell">
            <div class="attribute-item">
              <span class="attribute-label">Màu sắc:</span>
              <span class="attribute-value">{{ record.tenMauSac }}</span>
            </div>
            <div class="attribute-item">
              <span class="attribute-label">Kích thước:</span>
              <span class="attribute-value">{{ record.tenKichThuoc }}</span>
            </div>
            <div class="attribute-item">
              <span class="attribute-label">Chất liệu:</span>
              <span class="attribute-value">{{ record.tenChatLieu }}</span>
            </div>
            <div class="attribute-item">
              <span class="attribute-label">Đế giày:</span>
              <span class="attribute-value">{{ record.tenDeGiay }}</span>
            </div>
            <div class="attribute-item">
              <span class="attribute-label">Trọng lượng:</span>
              <span class="attribute-value">{{ record.tenTrongLuong }}</span>
            </div>
          </div>
        </template>

        <template #stock="{ record }">
          <div v-if="!isEditMode || !selectedVariants.includes(record.id.toString())">{{ record.soLuong }}</div>
          <a-input-number v-else v-model="record.soLuong" :min="0" size="small" style="width: 80px" :precision="0" />
        </template>

        <template #discount="{ record }">
          <div v-if="!isEditMode || !selectedVariants.includes(record.id.toString())">{{ record.giaTriGiamGia || 0 }}%</div>
          <a-input-number
            v-else
            v-model="record.giaTriGiamGia"
            :min="0"
            :max="100"
            size="small"
            style="width: 80px"
            :precision="1"
            suffix="%"
          />
        </template>

        <template #final_price="{ record }">
          <div class="final-price-cell">
            <div class="final-price">{{ formatCurrency(record.giaBan) }}</div>
          </div>
        </template>

        <template #status="{ record }">
          <a-tag v-if="!isEditMode || !selectedVariants.includes(record.id.toString())" :color="record.trangThai ? 'green' : 'red'">
            {{ record.trangThai ? 'Đang bán' : 'Tạm ngưng bán' }}
          </a-tag>
          <a-select v-else v-model="record.trangThai" size="small" style="width: 120px">
            <a-option :value="true">Đang bán</a-option>
            <a-option :value="false">Tạm ngưng bán</a-option>
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
import { useRoute } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import {
  getBienTheSanPhamList,
  getSanPhamOptions,
  getMauSacOptions,
  getKichThuocOptions,
  getChatLieuOptions,
  getDeGiayOptions,
  getTrongLuongOptions,
  type BienTheSanPham,
  type BienTheResponse,
  type SanPham,
  type MauSac,
  type KichThuoc,
  type ChatLieu,
  type DeGiay,
  type TrongLuong,
} from '@/api/san-pham/bien-the'
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
import { getBienTheSanPhamPage } from '../../../../api/san-pham/bien-the'

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()

// Route params
const route = useRoute()
const productId = computed(() => (route.params.productId ? Number(route.params.productId) : undefined))
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

// Pagination
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0, // Sẽ được cập nhật từ API response
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: true,
})

// API Data State
const bienTheList = ref<BienTheResponse | null>(null)
const variants = ref<BienTheSanPham[]>([])
const totalElements = ref(0)

// Options State
const sanPhamOptions = ref<SanPham[]>([])
const mauSacOptions = ref<MauSac[]>([])
const kichThuocOptions = ref<KichThuoc[]>([])
const chatLieuOptions = ref<ChatLieu[]>([])
const deGiayOptions = ref<DeGiay[]>([])
const trongLuongOptions = ref<TrongLuong[]>([])

// Computed options for dropdowns
const manufacturerOptions = computed(() => {
  if (!Array.isArray(variants.value)) return []
  const unique = [...new Set(variants.value.map((v) => v.tenNhaSanXuat).filter(Boolean))]
  return unique.map((name) => ({ value: name, label: name }))
})

const originOptions = computed(() => {
  if (!Array.isArray(variants.value)) return []
  const unique = [...new Set(variants.value.map((v) => v.tenXuatXu).filter(Boolean))]
  return unique.map((name) => ({ value: name, label: name }))
})

const materialOptions = computed(() => {
  if (!Array.isArray(variants.value)) return []
  const unique = [...new Set(variants.value.map((v) => v.tenChatLieu).filter(Boolean))]
  return unique.map((name) => ({ value: name, label: name }))
})

const shoeSoleOptions = computed(() => {
  if (!Array.isArray(variants.value)) return []
  const unique = [...new Set(variants.value.map((v) => v.tenDeGiay).filter(Boolean))]
  return unique.map((name) => ({ value: name, label: name }))
})

// Computed title for variants table
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
    const staticManufacturerOptions = [
      { value: 'nike', label: 'Nike' },
      { value: 'adidas', label: 'Adidas' },
      { value: 'gucci', label: 'Gucci' },
      { value: 'puma', label: 'Puma' },
    ]
    const manufacturer = staticManufacturerOptions.find((m) => m.value === filters.value.manufacturer)
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
    align: 'center',
  },
  {
    title: 'Mã',
    dataIndex: 'maChiTietSanPham',
    slotName: 'sku',
    width: 70,
  },
  {
    title: 'Ảnh',
    dataIndex: 'anhSanPham',
    slotName: 'product_image',
    width: 50,
    align: 'center',
  },
  {
    title: 'Nhà sản xuất',
    dataIndex: 'tenNhaSanXuat',
    slotName: 'manufacturer',
    width: 60,
    align: 'center',
  },
  {
    title: 'Xuất xứ',
    dataIndex: 'tenXuatXu',
    slotName: 'origin',
    width: 50,
    align: 'center',
  },
  {
    title: 'Thuộc tính sản phẩm',
    slotName: 'attributes',
    width: 90,
  },
  {
    title: 'Số lượng',
    dataIndex: 'soLuong',
    slotName: 'stock',
    width: 40,
    align: 'center',
  },
  {
    title: 'Giảm giá (%)',
    dataIndex: 'giaTriGiamGia',
    slotName: 'discount',
    width: 60,
    align: 'center',
  },
  {
    title: 'Giá bán',
    dataIndex: 'giaBan',
    slotName: 'final_price',
    width: 65,
  },
  {
    title: 'Trạng thái',
    dataIndex: 'trangThai',
    slotName: 'status',
    width: 60,
    align: 'center',
  },
  {
    title: 'Thao tác',
    slotName: 'action',
    width: 60,
    align: 'center',
  },
]

// Pagination
// Mock data

const availableSizes = ref(['35', '36', '37', '38', '39', '40', '41', '42'])

// API Functions
const loadBienTheList = async () => {
  try {
    loading.value = true
    const response = await getBienTheSanPhamPage(pagination.value.current - 1, productId.value)
    console.log('=== DEBUG RESPONSE ===')
    console.log('Full API Response:', response)
    console.log('Response data:', response.data)
    console.log('Response success:', response.success)

    if (response && response.data) {
      // Extract the actual data array from the nested structure
      const paginationData = response.data
      console.log('Pagination data:', paginationData)
      console.log('Data array:', paginationData.data)
      console.log('Is data array?', Array.isArray(paginationData.data))

      variants.value = paginationData.data || []
      totalElements.value = paginationData.totalElements || paginationData.data?.length || 0

      // Update pagination info
      pagination.value.total = paginationData.totalElements || paginationData.data?.length || 0
      pagination.value.current = (paginationData.currentPage || 0) + 1
      pagination.value.pageSize = paginationData.pageSize || 10

      console.log('=== FINAL RESULTS ===')
      console.log('Variants array:', variants.value)
      console.log('Variants length:', variants.value.length)
      console.log('Total elements:', totalElements.value)
      console.log('Pagination:', {
        total: pagination.value.total,
        current: pagination.value.current,
        pageSize: pagination.value.pageSize,
      })
    } else {
      console.log('No data in response, setting empty arrays')
      variants.value = []
      totalElements.value = 0
    }
  } catch (error) {
    console.error('Error loading variants:', error)
    Message.error('Không thể tải danh sách biến thể sản phẩm')
    variants.value = []
    totalElements.value = 0
  } finally {
    loading.value = false
  }
}

// Load options from API
const loadAllOptions = async () => {
  try {
    const [sanPham, mauSac, kichThuoc, chatLieu, deGiay, trongLuong] = await Promise.all([
      getSanPhamOptions(),
      getMauSacOptions(),
      getKichThuocOptions(),
      getChatLieuOptions(),
      getDeGiayOptions(),
      getTrongLuongOptions(),
    ])

    sanPhamOptions.value = sanPham.data || []
    mauSacOptions.value = mauSac.data || []
    kichThuocOptions.value = kichThuoc.data || []
    chatLieuOptions.value = chatLieu.data || []
    deGiayOptions.value = deGiay.data || []
    trongLuongOptions.value = trongLuong.data || []
  } catch (error) {
    Message.error('Không thể tải danh sách lựa chọn')
  }
}

// Pagination change handler
const handlePageChange = (page: number, size: number) => {
  pagination.value.current = page
  pagination.value.pageSize = size
  loadBienTheList()
}

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
  console.log('=== COMPONENT MOUNTED ===')
  console.log('Product ID from route:', productId.value)
  console.log('Initial pagination:', pagination.value)
  // Load data from API
  loadAllOptions()
  loadBienTheList()
})

// Watch for filter changes to reload data
watch(
  () => pagination.value.current,
  () => {
    loadBienTheList()
  }
)
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
  gap: 2px;
  max-width: 280px;
}

.attribute-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
}

.attribute-label {
  font-weight: 500;
  color: #86909c;
  min-width: 70px;
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

/* Custom table styles với padding giảm */
:deep(.arco-table-td) {
  padding: 5px 8px !important;
}

:deep(.arco-table-th) {
  padding: 5px 8px !important;
}

:deep(.arco-table-cell) {
  line-height: 1.4;
}

/* Giảm padding-x cho table size small */
:deep(.arco-table-size-small .arco-table-cell) {
  padding-left: 6px !important;
  padding-right: 6px !important;
}

/* Đảm bảo table không có horizontal scroll */
:deep(.arco-table-container) {
  width: 100% !important;
}

:deep(.arco-table) {
  width: 100% !important;
  table-layout: auto !important;
}
</style>
