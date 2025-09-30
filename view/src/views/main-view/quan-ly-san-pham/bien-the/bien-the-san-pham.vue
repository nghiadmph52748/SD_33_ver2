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
        :data="variants"
        :pagination="{
          ...pagination,
          onChange: handlePageChange,
        }"
        :loading="loading"
        size="small"
      >
        <template #stt="{ rowIndex }">
          <div>{{ rowIndex + 1 }}</div>
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
            <a-button type="text" danger @click="onDeleteClick(record)">
              <template #icon>
                <icon-delete />
              </template>
            </a-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message, Modal } from '@arco-design/web-vue'
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
} from '@/api/san-pham/bien-the'
import { getBienTheSanPhamPage } from '../../../../api/san-pham/bien-the'

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()

// Router setup
const route = useRoute()
const router = useRouter()
const productId = computed(() => (route.params.productId ? Number(route.params.productId) : undefined))

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
const bienTheList = ref<BienTheResponse | null>(null)
const variants = ref<BienTheSanPham[]>([])
const totalElements = ref(0)

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
    width: 40,
    align: 'center',
  },
  {
    title: 'Thao tác',
    slotName: 'action',
    width: 40,
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
    console.log('Response:', response)
    if (response.success) {
      // Extract the actual data array from the nested structure
      const paginationData = response.data.data
      console.log('Pagination Data:', paginationData)
      variants.value = paginationData
      totalElements.value = paginationData.totalElements || paginationData.data?.length || 0

      // Update originalVariants for search functionality
      originalVariants.value = { data: paginationData }

      // Update pagination info
      pagination.value.total = paginationData.totalElements || paginationData.data?.length || 0
      pagination.value.current = (paginationData.currentPage || 0) + 1
      pagination.value.pageSize = paginationData.pageSize || 10
    } else {
      // API response structure unexpected
      variants.value = []
      totalElements.value = 0
      originalVariants.value = { data: [], totalElements: 0, currentPage: 0, pageSize: 10, totalPages: 0 }
    }
  } catch (error) {
    // Error loading biến thể
    Message.error('Không thể tải danh sách biến thể sản phẩm')
    variants.value = []
    totalElements.value = 0
    originalVariants.value = { data: [], totalElements: 0, currentPage: 0, pageSize: 10, totalPages: 0 }
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
    const response = await getBienTheSanPhamPage(pagination.value.current - 1)
    if (response.success) {
      const paginationData = response.data.data
      variants.value = paginationData
      totalElements.value = paginationData.totalElements || paginationData.data?.length || 0

      // Update originalVariants for search functionality
      originalVariants.value = { data: paginationData }

      // Update pagination info
      pagination.value.total = paginationData.totalElements || paginationData.data?.length || 0
      pagination.value.current = (paginationData.currentPage || 0) + 1
      pagination.value.pageSize = paginationData.pageSize || 10
    } else {
      // All variants API response structure unexpected
      variants.value = []
      totalElements.value = 0
      originalVariants.value = { data: [], totalElements: 0, currentPage: 0, pageSize: 10, totalPages: 0 }
    }
  } catch (error) {
    Message.error('Không thể tải danh sách biến thể sản phẩm')
    variants.value = []
    totalElements.value = 0
    originalVariants.value = { data: [], totalElements: 0, currentPage: 0, pageSize: 10, totalPages: 0 }
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
  loading.value = true

  // Frontend search implementation
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
  const sourceData = originalVariants.value?.data || []
  variants.value = [...sourceData]
}

const exportExcel = () => {
  // Implement Excel export logic
}

const completeBulkUpdate = () => {
  // Implement bulk update logic
  // This would save all changes made to selected variants

  // Reset edit mode and selections
  selectedVariants.value = []
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

const editVariant = (variant: any) => {
  router.push(`/quan-ly-san-pham/bien-the/update/${variant.id}`)
}

const deleteVariant = async (variant: any) => {
  // Implement delete logic
}

// Delete confirm modal state
const showDeleteConfirm = ref(false)
const variantToDelete = ref(null)

const onDeleteClick = (variant) => {
  variantToDelete.value = variant
  showDeleteConfirm.value = true
}

const confirmDelete = async () => {
  if (variantToDelete.value) {
    await deleteVariant(variantToDelete.value)
    showDeleteConfirm.value = false
    variantToDelete.value = null
  }
}

const cancelDelete = () => {
  showDeleteConfirm.value = false
  variantToDelete.value = null
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
  // Load data from API
  loadAllOptions()
  loadBienTheList()
})

// Watch for productId changes (khi nhấn xem biến thể từ danh mục sản phẩm)
watch(
  () => productId.value,
  (newProductId, oldProductId) => {
    if (newProductId !== oldProductId) {
      // Reset pagination khi chuyển sản phẩm
      pagination.value.current = 1
      showAllVariants.value = false // Đảm bảo hiển thị biến thể của sản phẩm cụ thể
      loadBienTheList()
    }
  },
  { immediate: false }
)

// Watch for filter changes to reload data
watch(
  () => pagination.value.current,
  () => {
    if (showAllVariants.value) {
      loadAllVariants()
    } else {
      loadBienTheList()
    }
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

.price-range-container:hover {
  border-color: #d9d9d9;
}

:deep(.arco-table-td) {
  word-break: normal;
}
</style>
