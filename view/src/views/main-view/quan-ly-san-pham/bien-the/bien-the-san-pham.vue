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
              <a-select v-model="filters.manufacturer" placeholder="Chọn nhà sản xuất" allow-clear @change="performSearch">
                <a-option value="">Tất cả</a-option>
                <a-option v-for="option in manufacturerOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="Xuất xứ">
              <a-select v-model="filters.origin" placeholder="Chọn xuất xứ" allow-clear @change="performSearch">
                <a-option value="">Tất cả</a-option>
                <a-option v-for="option in originOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <!-- Row 2: Chất liệu - Đế giày - Trọng lượng -->
          <a-col :span="8">
            <a-form-item label="Chất liệu">
              <a-select v-model="filters.material" placeholder="Chọn chất liệu" allow-clear @change="performSearch">
                <a-option value="">Tất cả</a-option>
                <a-option v-for="option in materialOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="Đế giày">
              <a-select v-model="filters.shoeSole" placeholder="Chọn đế giày" allow-clear @change="performSearch">
                <a-option value="">Tất cả</a-option>
                <a-option v-for="option in shoeSoleOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="Trọng lượng">
              <a-select v-model="filters.weight" placeholder="Chọn trọng lượng" allow-clear @change="performSearch">
                <a-option value="">Tất cả</a-option>
                <a-option v-for="option in weightOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <!-- Row 3: Trạng thái - Khoảng giá -->
          <a-col :span="8">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="filters.status" type="button" @change="performSearch">
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
          <!-- New variants highlighting notification -->
          <a-tag v-if="shouldHighlight && newVariantIds.length > 0" color="green" size="large" closable @close="clearHighlighting">
            <template #icon>
              <icon-check />
            </template>
            {{ newVariantIds.length }} biến thể mới được tạo (highlight trong 30s)
          </a-tag>
          <!-- Edit mode buttons removed -->
          <a-button @click="toggleShowAllVariants">
            <template #icon>
              <icon-eye />
            </template>
            {{ showAllVariants ? 'Hiển thị biến thể hiện tại' : 'Hiển thị toàn bộ biến thể' }}
          </a-button>
        </a-space>
      </template>
      <a-table
        :columns="columns"
        :data="sortedVariants"
        :pagination="{
          current: pagination.current,
          pageSize: pagination.pageSize,
          total: pagination.total,
          showSizeChanger: pagination.showSizeChanger,
          showQuickJumper: pagination.showQuickJumper,
          showTotal: true,
        }"
        :loading="loading"
        size="small"
        :row-class="getRowClass"
        @page-change="handlePageChange"
        @page-size-change="handlePageChange"
      >
        <template #stt="{ rowIndex }">
          <div>{{ rowIndex + 1 }}</div>
        </template>

        <template #product_image="{ record }">
          <a-carousel :autoplay="true" show-arrow="hover" :loop="true" style="width: 70px; height: 70px">
            <a-carousel-item
              v-if="record.anhSanPham && record.anhSanPham.length > 0"
              v-for="(img, index) in record.anhSanPham"
              :key="index"
            >
              <a-avatar :image-url="img" :size="70" shape="square"></a-avatar>
            </a-carousel-item>
            <a-carousel-item v-else>
              <a-avatar image-url="null" :size="70" shape="square"></a-avatar>
            </a-carousel-item>
          </a-carousel>
        </template>

        <template #manufacturer="{ record }">
          <div>
            {{ record.tenNhaSanXuat }}
          </div>
        </template>

        <template #origin="{ record }">
          <div>
            {{ record.tenXuatXu }}
          </div>
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
          <div>{{ record.soLuong }}</div>
        </template>

        <template #discount="{ record }">
          <div>{{ record.giaTriGiamGia || 0 }}%</div>
        </template>

        <template #final_price="{ record }">
          <div class="final-price-cell">
            <div v-if="record.giaTriGiamGia && record.giaTriGiamGia > 0" class="with-discount">
              <div class="original-price">{{ formatCurrency(record.giaBan) }}</div>
              <div class="final-price">{{ formatCurrency(record.giaBan * (1 - record.giaTriGiamGia / 100)) }}</div>
            </div>
            <div v-else class="final-price">{{ formatCurrency(record.giaBan) }}</div>
          </div>
        </template>

        <template #status="{ record }">
          <a-tag :color="record.trangThai ? 'green' : 'red'">
            {{ record.trangThai ? 'Đang bán' : 'Tạm ngưng bán' }}
          </a-tag>
        </template>

        <template #action="{ record }">
          <a-space>
            <!-- Status Toggle Switch -->
            <a-tooltip content="Thay đổi trạng thái bán">
              <a-switch :model-value="record.trangThai" type="round" @click="toggleStatus(record)" :loading="record.updating">
                <template #checked-icon>
                  <icon-check />
                </template>
                <template #unchecked-icon>
                  <icon-close />
                </template>
              </a-switch>
            </a-tooltip>

            <a-button type="text" @click="viewDetail(record)">
              <template #icon>
                <icon-eye />
              </template>
            </a-button>
            <a-button type="text" @click="editVariant(record)">
              <template #icon>
                <icon-edit />
              </template>
            </a-button>
            <a-tooltip content="Xóa/Khôi phục biến thể">
              <a-button type="text" danger @click="onDeleteClick(record)">
                <template #icon>
                  <icon-delete />
                </template>
              </a-button>
            </a-tooltip>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- Delete Confirm Modal -->
    <a-modal
      v-model:visible="showDeleteConfirm"
      title="Xác nhận xoá"
      ok-text="Xoá"
      cancel-text="Huỷ"
      @ok="confirmDelete"
      @cancel="cancelDelete"
    >
      <template #default>
        <div>Bạn có chắc chắn muốn xoá biến thể này?</div>
        <div v-if="variantToDelete">
          Mã biến thể:
          <strong>{{ variantToDelete.maChiTietSanPham }}</strong>
        </div>
      </template>
    </a-modal>

    <!-- Status Toggle Confirm Modal -->
    <a-modal
      v-model:visible="showStatusConfirm"
      title="Xác nhận thay đổi trạng thái"
      ok-text="Xác nhận"
      cancel-text="Huỷ"
      @ok="confirmToggleStatus"
      @cancel="cancelToggleStatus"
    >
      <template #default>
        <div v-if="variantToToggleStatus">
          <div>Bạn có chắc chắn muốn {{ variantToToggleStatus.trangThai ? 'tạm ngưng bán' : 'kích hoạt bán' }} biến thể này?</div>
          <div>
            Mã biến thể:
            <strong>{{ variantToToggleStatus.maChiTietSanPham }}</strong>
          </div>
          <div>
            Trạng thái hiện tại:
            <strong>{{ variantToToggleStatus.trangThai ? 'Đang bán' : 'Tạm ngưng bán' }}</strong>
          </div>
        </div>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message, Modal } from '@arco-design/web-vue'
import { exportToExcel, EXPORT_HEADERS } from '@/utils/export-excel'
import {
  IconEdit,
  IconCheck,
  IconClose,
  IconRefresh,
  IconDelete,
  IconDownload,
  IconEye,
  IconImage,
  IconSettings,
} from '@arco-design/web-vue/es/icon'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import {
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
  type CreateBienTheSanPhamRequest,
} from '@/api/san-pham/bien-the'
import { deleteBienTheSanPham, getBienTheSanPhamPage, updateBienTheSanPham } from '../../../../api/san-pham/bien-the'

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()

// Router setup
const route = useRoute()
const router = useRouter()
const productId = computed(() => (route.params.productId ? Number(route.params.productId) : undefined))

// New variants highlighting
const newVariantIds = ref<number[]>([])
const shouldHighlight = ref(false)
const highlightTimeoutId = ref<number | null>(null)

// Parse new variants from query params
const parseNewVariants = () => {
  const newVariantsParam = route.query.newVariants as string
  const highlightParam = route.query.highlight as string

  if (newVariantsParam && highlightParam === 'true') {
    newVariantIds.value = newVariantsParam
      .split(',')
      .map((id) => parseInt(id.trim(), 10))
      .filter((id) => !Number.isNaN(id))
    shouldHighlight.value = true
  }
}

// API Data State - cần khai báo sớm cho pageTitle
const sanPhamOptions = ref<SanPham[]>([])

// Dynamic page title cho tab-bar phân biệt
const pageTitle = computed(() => {
  if (productId.value) {
    const product = sanPhamOptions.value.find((p) => p.id === productId.value)
    const productName = product ? product.tenSanPham : `Sản phẩm ${productId.value}`
    return `Biến thể - ${productName}`
  }
  return 'Biến thể sản phẩm'
})

// Cập nhật document title để phân biệt tab
watch(
  pageTitle,
  (newTitle) => {
    if (typeof document !== 'undefined') {
      document.title = newTitle
    }
  },
  { immediate: true }
)

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
const variants = ref<BienTheSanPham[]>([])

// Computed for sorted variants with new variants on top
const sortedVariants = computed(() => {
  if (!shouldHighlight.value || newVariantIds.value.length === 0) {
    return variants.value
  }

  // Convert all IDs to numbers for consistent comparison
  const newIds = newVariantIds.value.map((id) => Number(id))

  // Separate new and existing variants
  const newVariants = variants.value.filter((v) => {
    const variantId = Number(v.id)
    const isNew = newIds.includes(variantId)
    return isNew
  })
  const existingVariants = variants.value.filter((v) => {
    const variantId = Number(v.id)
    return !newIds.includes(variantId)
  })

  // Return new variants first, then existing variants
  return [...newVariants, ...existingVariants]
})

// Options State (tiếp tục từ sanPhamOptions đã khai báo ở trên)
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

const weightOptions = computed(() => {
  if (!Array.isArray(variants.value)) return []
  const unique = [...new Set(variants.value.map((v) => v.tenTrongLuong).filter(Boolean))]
  return unique.map((name) => ({ value: name, label: name }))
})

// Computed title for variants table
// Table
const loading = ref(false)
const selectedVariants = ref<string[]>([])
// Edit mode removed
const showAllVariants = ref(false)

// Store original variants for filtering
const originalVariants = ref([])

// Computed title for variants table
const variantsTableTitle = computed(() => {
  if (productId.value && showAllVariants.value === false) {
    const product = sanPhamOptions.value.find((p) => p.id === productId.value)
    const categoryName = product ? product.tenSanPham : `Sản phẩm ${productId.value}`
    return `Danh sách biến thể - ${categoryName}`
  }

  return `Danh sách biến thể - Tất cả sản phẩm`
})

const columns = [
  {
    title: 'STT',
    dataIndex: 'stt',
    slotName: 'stt',
    width: 50,
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
    width: 60,
    align: 'center',
  },
  {
    title: 'Thuộc tính sản phẩm',
    slotName: 'attributes',
    width: 120,
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
    width: 30,
    align: 'center',
  },
  {
    title: 'Giá bán',
    dataIndex: 'giaBan',
    slotName: 'final_price',
    width: 40,
  },
  {
    title: 'Trạng thái',
    dataIndex: 'trangThai',
    slotName: 'status',
    width: 60,
    // align: 'center',
  },
  {
    title: 'Thao tác',
    slotName: 'action',
    width: 40,
    align: 'center',
  },
]

// API Functions
const loadBienTheList = async () => {
  try {
    loading.value = true
    const response = await getBienTheSanPhamPage(pagination.value.current - 1, productId.value, pagination.value.pageSize)
    if (response.success && response.data) {
      const apiData = response.data
      // Match the exact API response structure
      if (apiData && apiData.data && Array.isArray(apiData.data)) {
        // Add updating property to each record for loading state
        variants.value = apiData.data.map((item) => ({ ...item, updating: false }))
        pagination.value.total = apiData.totalElements || 0
        pagination.value.current = (apiData.currentPage || 0) + 1 // Convert from 0-based to 1-based
        pagination.value.pageSize = apiData.pageSize || 10
      } else {
        // Fallback if structure is different
        variants.value = []
        pagination.value.total = 0
      }

      // Update originalVariants for search functionality
      originalVariants.value = { data: variants.value }
    } else {
      // API response structure unexpected
      variants.value = []
      pagination.value.total = 0
      originalVariants.value = { data: [] }
    }
  } catch (error) {
    console.error('Error loading biến thể:', error)
    // Error loading biến thể
    Message.error('Không thể tải danh sách biến thể sản phẩm')
    variants.value = []
    pagination.value.total = 0
    originalVariants.value = { data: [] }
  } finally {
    loading.value = false
  }
}

// Load ALL variants for highlighting mode
const loadAllVariantsForHighlight = async () => {
  try {
    loading.value = true
    // Load with very high pageSize to get all variants
    const response = await getBienTheSanPhamPage(0, productId.value, 1000)
    if (response.success && response.data) {
      const apiData = response.data
      if (apiData && apiData.data && Array.isArray(apiData.data)) {
        // Add updating property to each record for loading state
        variants.value = apiData.data.map((item) => ({ ...item, updating: false }))

        // Keep original pagination structure but show all data
        pagination.value.total = apiData.totalElements || 0
        pagination.value.current = 1
        pagination.value.pageSize = apiData.data.length // Show all loaded data
        // Check if new variants are present
        const newIds = newVariantIds.value.map((id) => Number(id))
        // foundNewVariants removed as it was unused
      } else {
        variants.value = []
        pagination.value.total = 0
      }

      originalVariants.value = { data: variants.value }
    } else {
      variants.value = []
      pagination.value.total = 0
      originalVariants.value = { data: [] }
    }
  } catch (error) {
    console.error('Error loading ALL variants for highlighting:', error)
    Message.error('Không thể tải danh sách biến thể sản phẩm')
    variants.value = []
    pagination.value.total = 0
    originalVariants.value = { data: [] }
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

// Function để load tất cả variants
const loadAllVariants = async () => {
  try {
    loading.value = true
    const response = await getBienTheSanPhamPage(pagination.value.current - 1, undefined, pagination.value.pageSize)
    if (response.success && response.data) {
      const apiData = response.data

      // Match the exact API response structure
      if (apiData && apiData.data && Array.isArray(apiData.data)) {
        // Add updating property to each record for loading state
        variants.value = apiData.data.map((item) => ({ ...item, updating: false }))
        pagination.value.total = apiData.totalElements || 0
        pagination.value.current = (apiData.currentPage || 0) + 1 // Convert from 0-based to 1-based
        pagination.value.pageSize = apiData.pageSize || 10
      } else {
        // Fallback if structure is different
        variants.value = []
        pagination.value.total = 0
      }

      // Update originalVariants for search functionality
      originalVariants.value = { data: variants.value }
    } else {
      // All variants API response structure unexpected
      variants.value = []
      pagination.value.total = 0
      originalVariants.value = { data: [] }
    }
  } catch (error) {
    console.error('Error loading all variants:', error)
    Message.error('Không thể tải danh sách biến thể sản phẩm')
    variants.value = []
    pagination.value.total = 0
    originalVariants.value = { data: [] }
  } finally {
    loading.value = false
  }
}

// Pagination change handler
const handlePageChange = (page: number, size: number) => {
  pagination.value.current = page
  pagination.value.pageSize = size

  // Gọi đúng function dựa trên trạng thái hiện tại
  if (showAllVariants.value) {
    loadAllVariants()
  } else {
    loadBienTheList()
  }
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
  // Check if any filters are active
  const hasActiveFilters =
    filters.value.search ||
    filters.value.manufacturer ||
    filters.value.origin ||
    filters.value.material ||
    filters.value.shoeSole ||
    filters.value.weight ||
    filters.value.status ||
    (filters.value.priceRange && (filters.value.priceRange[0] > 0 || filters.value.priceRange[1] < 5000000))

  if (!hasActiveFilters) {
    // No filters active, reload data from API with current pagination
    if (showAllVariants.value) {
      loadAllVariants()
    } else {
      loadBienTheList()
    }
    return
  }

  // Filters are active, perform frontend filtering
  loading.value = true

  setTimeout(() => {
    // Ensure originalVariants.value.data exists and is an array
    const sourceData = originalVariants.value?.data || []
    let filteredVariants = [...sourceData]

    // Search by product name or SKU
    if (filters.value.search) {
      const searchTerm = filters.value.search.toLowerCase()
      filteredVariants = filteredVariants.filter(
        (variant) =>
          (variant.tenSanPham && variant.tenSanPham.toLowerCase().includes(searchTerm)) ||
          (variant.maChiTietSanPham && variant.maChiTietSanPham.toLowerCase().includes(searchTerm))
      )
    }

    // Filter by manufacturer
    if (filters.value.manufacturer) {
      filteredVariants = filteredVariants.filter((variant) => variant.tenNhaSanXuat === filters.value.manufacturer)
    }

    // Filter by origin
    if (filters.value.origin) {
      filteredVariants = filteredVariants.filter((variant) => variant.tenXuatXu === filters.value.origin)
    }

    // Filter by material
    if (filters.value.material) {
      filteredVariants = filteredVariants.filter((variant) => variant.tenChatLieu === filters.value.material)
    }

    // Filter by shoe sole
    if (filters.value.shoeSole) {
      filteredVariants = filteredVariants.filter((variant) => variant.tenDeGiay === filters.value.shoeSole)
    }

    // Filter by weight
    if (filters.value.weight) {
      filteredVariants = filteredVariants.filter((variant) => variant.tenTrongLuong === filters.value.weight)
    }

    // Filter by price range (slider: [min, max])
    if (filters.value.priceRange && Array.isArray(filters.value.priceRange)) {
      const [minPrice, maxPrice] = filters.value.priceRange
      filteredVariants = filteredVariants.filter((variant) => {
        const price = variant.giaBan || 0
        return price >= minPrice && price <= maxPrice
      })
    }

    // Filter by status (only filter if not "Tất cả")
    if (filters.value.status && filters.value.status !== '') {
      filteredVariants = filteredVariants.filter((variant) => {
        if (filters.value.status === 'active') {
          return variant.trangThai === true
        }
        if (filters.value.status === 'inactive') {
          return variant.trangThai === false
        }
        return true
      })
    }

    // Update displayed variants
    variants.value = filteredVariants

    // Check if filtering results in empty current page and adjust if needed
    const totalFiltered = filteredVariants.length
    const maxPage = Math.ceil(totalFiltered / pagination.value.pageSize) || 1

    if (pagination.value.current > maxPage) {
      pagination.value.current = maxPage
    }

    // Update pagination total for filtered results (for display purposes)
    pagination.value.total = totalFiltered

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

  // Reset pagination to first page
  pagination.value.current = 1

  // Reload data from API
  if (showAllVariants.value) {
    loadAllVariants()
  } else {
    loadBienTheList()
  }
}

const exportExcel = () => {
  try {
    if (!variants.value || variants.value.length === 0) {
      Message.warning('Không có dữ liệu để xuất Excel')
      return
    }

    exportToExcel(variants.value, EXPORT_HEADERS.BIEN_THE, 'bien-the-san-pham')
  } catch (error) {
    console.error('Lỗi khi xuất Excel:', error)
    Message.error('Có lỗi xảy ra khi xuất Excel')
  }
}

// Clear highlighting function
const clearHighlighting = () => {
  shouldHighlight.value = false
  newVariantIds.value = []

  // Clear timeout if exists
  if (highlightTimeoutId.value) {
    clearTimeout(highlightTimeoutId.value)
    highlightTimeoutId.value = null
  }

  // Clean up URL query params
  router.replace({
    name: route.name,
    params: route.params,
  })
  Message.success('Đã tắt highlighting biến thể mới')
}

// Helper function to check if we need to go back a page after deletion
const checkAndAdjustPagination = () => {
  // For filtered results, we need to check against the actual filtered count
  const totalItems = variants.value.length
  const maxPage = Math.ceil(totalItems / pagination.value.pageSize) || 1
  const currentPage = pagination.value.current

  if (currentPage > maxPage) {
    pagination.value.current = maxPage
    return true
  }

  return false
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

// Edit mode removed

const toggleShowAllVariants = async () => {
  showAllVariants.value = !showAllVariants.value

  // Reset về trang đầu khi toggle
  pagination.value.current = 1

  if (showAllVariants.value) {
    // Lấy tất cả biến thể
    await loadAllVariants()
  } else {
    // Lấy biến thể của sản phẩm cụ thể
    await loadBienTheList()
  }
}

const viewDetail = (variant: any) => {
  router.push(`/quan-ly-san-pham/bien-the/detail/${variant.id}`)
}

// Confirm modal states
const showStatusConfirm = ref(false)
const variantToToggleStatus = ref(null)

const editVariant = (variant: any) => {
  router.push(`/quan-ly-san-pham/bien-the/update/${variant.id}`)
}

// Toggle status function - show confirm first
const toggleStatus = (record: any) => {
  variantToToggleStatus.value = record
  showStatusConfirm.value = true
}

// Actual toggle status implementation
const performToggleStatus = async (record: any) => {
  try {
    // Set loading state for this specific record
    record.updating = true

    // Helper function to find ID by name from options
    const findIdByName = (options: any[], nameField: string, name: string) => {
      const found = options.find((option) => option[nameField] === name)
      return found ? found.id : null
    }

    // Create update request with all required fields
    const updateData = {
      idSanPham: record.idSanPham || productId.value || findIdByName(sanPhamOptions.value, 'tenSanPham', record.tenSanPham),
      idMauSac: findIdByName(mauSacOptions.value, 'tenMauSac', record.tenMauSac),
      idKichThuoc: findIdByName(kichThuocOptions.value, 'tenKichThuoc', record.tenKichThuoc),
      idDeGiay: findIdByName(deGiayOptions.value, 'tenDeGiay', record.tenDeGiay),
      idChatLieu: findIdByName(chatLieuOptions.value, 'tenChatLieu', record.tenChatLieu),
      idTrongLuong: findIdByName(trongLuongOptions.value, 'tenTrongLuong', record.tenTrongLuong),
      soLuong: record.soLuong,
      giaBan: record.giaBan,
      trangThai: !record.trangThai, // Toggle status
      ghiChu: record.ghiChu || '',
      deleted: record.deleted || false,
    }

    // Call API to update variant
    const response = await updateBienTheSanPham(record.id, updateData)

    if (response.success || response.status === 200 || response.data) {
      // Update local data immediately for better UX
      record.trangThai = !record.trangThai

      // Update in variants array
      const index = variants.value.findIndex((v) => v.id === record.id)
      if (index !== -1) {
        variants.value[index].trangThai = record.trangThai
      }

      // Update in originalVariants if exists
      if (originalVariants.value?.data) {
        const origIndex = originalVariants.value.data.findIndex((v) => v.id === record.id)
        if (origIndex !== -1) {
          originalVariants.value.data[origIndex].trangThai = record.trangThai
        }
      }

      const statusText = record.trangThai ? 'Đang bán' : 'Tạm ngưng bán'
      Message.success(`Đã cập nhật trạng thái thành: ${statusText}`)
    } else {
      // Revert the local change if API fails
      console.error('API response not successful:', response)
      Message.error('Cập nhật trạng thái thất bại')
    }
  } catch (error) {
    console.error('Error toggling status:', error)
    Message.error('Có lỗi xảy ra khi cập nhật trạng thái')
  } finally {
    // Remove loading state
    record.updating = false
  }
}

const deleteVariant = async (variant: any) => {
  try {
    const res = await deleteBienTheSanPham(variant.id)
    if (res.success) {
      Message.success('Xoá biến thể thành công')
      // Check if filters are active
      const hasActiveFilters =
        filters.value.search ||
        filters.value.manufacturer ||
        filters.value.origin ||
        filters.value.material ||
        filters.value.shoeSole ||
        filters.value.weight ||
        filters.value.status ||
        (filters.value.priceRange && (filters.value.priceRange[0] > 0 || filters.value.priceRange[1] < 5000000))

      if (hasActiveFilters) {
        // With filters: Remove item from local arrays first, then check pagination
        variants.value = variants.value.filter((v) => v.id !== variant.id)
        if (originalVariants.value?.data) {
          originalVariants.value.data = originalVariants.value.data.filter((v) => v.id !== variant.id)
        }
        // Check if page adjustment needed and perform search to refresh
        const wasAdjusted = checkAndAdjustPagination()
        performSearch()
      } else {
        const currentPageItems = sortedVariants.value.length
        const isLastItemOnPage = currentPageItems === 1
        const isNotFirstPage = pagination.value.current > 1

        if (isLastItemOnPage && isNotFirstPage) {
          pagination.value.current -= 1
        }

        // Reload data after deletion
        if (showAllVariants.value) {
          await loadAllVariants()
        } else {
          await loadBienTheList()
        }
      }
    } else {
      Message.error('Xoá biến thể thất bại')
    }
  } catch (error) {
    Message.error('Có lỗi xảy ra khi xóa biến thể')
  }
}

// Delete confirm modal state
const showDeleteConfirm = ref(false)
const variantToDelete = ref(null)

const onDeleteClick = (variant) => {
  variantToDelete.value = variant
  showDeleteConfirm.value = true
}

const confirmDelete = async () => {
  deleteVariant(variantToDelete.value)
  showDeleteConfirm.value = false
}

// Row class for highlighting new variants
const getRowClass = (record: any) => {
  if (shouldHighlight.value && newVariantIds.value.length > 0) {
    const newIds = newVariantIds.value.map((id) => Number(id))
    const recordId = Number(record.id)
    const isNew = newIds.includes(recordId)
    return isNew ? 'new-variant-row' : ''
  }
  return ''
}

const cancelDelete = () => {
  showDeleteConfirm.value = false
  variantToDelete.value = null
}

const confirmToggleStatus = async () => {
  await performToggleStatus(variantToToggleStatus.value)
  showStatusConfirm.value = false
  variantToToggleStatus.value = null
}

const cancelToggleStatus = () => {
  showStatusConfirm.value = false
  variantToToggleStatus.value = null
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
  // Parse new variants from query params first
  parseNewVariants()

  // Check if returning from update page
  if (route.query.updated === 'true') {
    Message.success('Cập nhật biến thể thành công!')
    // Remove the query parameter from URL
    router.replace({ query: { ...route.query, updated: undefined } })
  }

  // If we have new variants to highlight, ensure we're on page 1
  if (shouldHighlight.value && newVariantIds.value.length > 0) {
    pagination.value.current = 1
  }

  // Load data from API
  loadAllOptions()

  // Load all variants if highlighting, otherwise load paginated
  if (shouldHighlight.value && newVariantIds.value.length > 0) {
    loadAllVariantsForHighlight()
  } else {
    loadBienTheList()
  }

  // Auto-remove highlighting after 30 seconds (increased from 10)
  if (shouldHighlight.value) {
    // Clear any existing timeout
    if (highlightTimeoutId.value) {
      clearTimeout(highlightTimeoutId.value)
    }

    highlightTimeoutId.value = setTimeout(() => {
      shouldHighlight.value = false
      newVariantIds.value = []
      highlightTimeoutId.value = null
      // Clean up URL query params without reloading data
      router.replace({
        name: route.name,
        params: route.params,
      })
      Message.info('Highlighting biến thể mới đã hết hiệu lực')
    }, 30000)
  }
})

// Watch for variants data changes to ensure proper sorting
watch(
  () => variants.value,
  (newVariants) => {
    if (shouldHighlight.value && newVariantIds.value.length > 0 && newVariants.length > 0) {
      const newIds = newVariantIds.value.map((id) => Number(id))
      // Check if new variants are present but don't store in unused variable
      newVariants.filter((v) => newIds.includes(Number(v.id)))
    }
  },
  { deep: true }
)

// Watch for productId changes (khi nhấn xem biến thể từ danh mục sản phẩm)
watch(
  () => productId.value,
  (newProductId, oldProductId) => {
    // Only reload if productId actually changed AND it's not the initial load
    if (newProductId !== oldProductId && oldProductId !== undefined) {
      // Clear highlighting when switching products
      if (shouldHighlight.value) {
        clearHighlighting()
      }

      // Reset pagination khi chuyển sản phẩm
      pagination.value.current = 1
      showAllVariants.value = false // Đảm bảo hiển thị biến thể của sản phẩm cụ thể
      loadBienTheList()
    }
  },
  { immediate: false }
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

:deep(.arco-table-td-content) {
  width: auto;
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

.with-discount .original-price {
  font-size: 12px;
  color: #86909c;
  text-decoration: line-through;
  font-weight: 400;
}

.final-price {
  font-weight: 500;
  color: #1d2129;
}

.with-discount .final-price {
  color: #f53f3f;
  font-weight: 600;
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

/* Highlight for new variants */
:deep(.new-variant-row) {
  background-color: #f6ffed !important;
  border-left: 4px solid #52c41a !important;
  animation: highlightFade 10s ease-out;
}

:deep(.new-variant-row:hover) {
  background-color: #f0f9ff !important;
}

@keyframes highlightFade {
  0% {
    background-color: #d9f7be;
    box-shadow: 0 0 10px rgba(82, 196, 26, 0.3);
  }
  50% {
    background-color: #f6ffed;
  }
  100% {
    background-color: #f6ffed;
  }
}

.price-range-container:hover {
  border-color: #d9d9d9;
}

:deep(.arco-table-td) {
  word-break: normal;
}

/* Status Switch Styling */
:deep(.arco-switch) {
  margin-right: 8px;
}

:deep(.arco-switch-loading) {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>
