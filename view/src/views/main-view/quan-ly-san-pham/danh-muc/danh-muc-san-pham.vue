<template>
  <div class="product-category-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Search and Filter -->
    <a-card class="filters-card">
      <a-form :model="filters" layout="vertical">
        <a-row :gutter="16">
          <!-- Row 1 -->
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="filters.search" placeholder="Tìm theo mã, tên hoặc khoảng giá..." allow-clear @input="searchCategories" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="filters.trangThai" type="button" @change="searchCategories">
                <a-radio value="">Tất cả</a-radio>
                <a-radio :value="true">Đang bán</a-radio>
                <a-radio :value="false">Tạm ngưng bán</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="Khoảng giá">
              <div class="price-range-container">
                <a-slider
                  v-model="priceRange"
                  :min="0"
                  :max="5000000"
                  :step="50000"
                  range
                  tooltip-visible
                  :tooltip-formatter="formatPrice"
                  @change="updatePriceRangeAndSearch"
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
        :data="transformedDanhMucList || []"
        :pagination="{
          ...pagination,
          onChange: handlePageChange,
          onShowSizeChange: handlePageChange,
        }"
        :loading="loading"
        :scroll="{ x: 800 }"
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
                    tenSanPham: record.tenSanPham,
                    trangThai: record.trangThai,
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

        <template #index="{ rowIndex }">
          {{ (pagination.current - 1) * pagination.pageSize + rowIndex + 1 }}
        </template>

        <template #status="{ record }">
          <div v-if="isRowSelected(record.id)">
            <!-- Edit mode -->
            <a-select
              :model-value="editingData.value[record.id]?.trangThai ? 'active' : 'inactive'"
              @update:model-value="
                (value) => {
                  if (!editingData.value) editingData.value = {}
                  editingData.value[record.id] = { ...(editingData.value[record.id] || {}), trangThai: value === 'active' }
                }
              "
              size="mini"
              style="width: 100px"
            >
              <a-option value="active">Đang bán</a-option>
              <a-option value="inactive">Tạm ngưng bán</a-option>
            </a-select>
          </div>
          <a-tag v-else :color="record.trangThai ? 'green' : 'red'">
            {{ record.trangThai ? 'Đang bán' : 'Tạm ngưng bán' }}
          </a-tag>
        </template>

        <template #name="{ record }">
          <div v-if="isRowSelected(record.id)">
            <!-- Edit mode for name -->
            <a-input
              :model-value="editingData.value[record.id]?.tenSanPham || record.tenSanPham"
              @update:model-value="
                (value) => {
                  if (!editingData.value) editingData.value = {}
                  editingData.value[record.id] = { ...(editingData.value[record.id] || {}), tenSanPham: value }
                }
              "
              size="mini"
              style="width: 200px"
            />
          </div>
          <span v-else class="custom-name">{{ record.tenSanPham }}</span>
        </template>

        <template #price_range="{ record }">
          <span class="custom-price-range">
            {{
              record.giaNhoNhat && record.giaLonNhat
                ? `${record.giaNhoNhat.toLocaleString('vi-VN')} - ${record.giaLonNhat.toLocaleString('vi-VN')}đ`
                : 'Chưa có giá'
            }}
          </span>
        </template>

        <template #action="{ record }">
          <a-space>
            <a-tooltip content="Thay đổi trạng thái">
              <a-switch :model-value="record.trangThai" type="round" @click="toggleStatus(record)" :loading="record.updating">
                <template #checked-icon>
                  <icon-check />
                </template>
                <template #unchecked-icon>
                  <icon-close />
                </template>
              </a-switch>
            </a-tooltip>
            <a-tooltip content="Xem biến thể sản phẩm">
              <a-button type="text" @click="viewCategory(record)">
                <template #icon>
                  <icon-eye />
                </template>
              </a-button>
            </a-tooltip>
          </a-space>
        </template>

        <template #checkbox-title>
          <a-checkbox
            :checked="selectedCount === (danhMucList?.length || 0) && (danhMucList?.length || 0) > 0"
            :indeterminate="selectedCount > 0 && selectedCount < (danhMucList?.length || 0)"
            @change="
              (checked) => {
                if (checked) {
                  // Select all categories and initialize editing data
                  selectedRowKeys.value = [...(danhMucList?.map((cat) => cat.id) || [])]
                  if (!editingData.value) editingData.value = {}
                  danhMucList?.forEach((category) => {
                    editingData.value[category.id] = {
                      tenSanPham: category.tenSanPham,
                      trangThai: category.trangThai,
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
        <div v-if="categoryToToggleStatus">
          <div>Bạn có chắc chắn muốn {{ categoryToToggleStatus.trangThai ? 'tạm ngưng bán' : 'kích hoạt bán' }} sản phẩm này?</div>
          <div>
            Tên sản phẩm:
            <strong>{{ categoryToToggleStatus.tenSanPham }}</strong>
          </div>
          <div>
            Trạng thái hiện tại:
            <strong>{{ categoryToToggleStatus.trangThai ? 'Đang bán' : 'Tạm ngưng bán' }}</strong>
          </div>
        </div>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, h, watch } from 'vue'
import { useRouter } from 'vue-router'
import { debounce } from 'lodash'
import { useUserStore } from '@/store'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { getDanhMucSanPhamList, getNhaSanXuatOptions, getXuatXuOptions, updateDanhMucSanPham } from '@/api/san-pham/danh-muc'
import type { DanhMucSanPham, DanhMucParams, NhaSanXuatOption, XuatXuOption, DanhMucResponse } from '@/api/san-pham/danh-muc'
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
  IconCheck,
  IconClose,
} from '@arco-design/web-vue/es/icon'
import { Message } from '@arco-design/web-vue'
import { exportToExcel, EXPORT_HEADERS } from '@/utils/export-excel'

// Form and modal
// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()
const router = useRouter()

const modalVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

// Edit inline mode state
const selectedRowKeys = ref<number[]>([])
const editingData = ref<Record<number, { tenSanPham: string; trangThai: boolean }>>({}) // Updated to match backend

// Status toggle modal
const showStatusConfirm = ref(false)
const categoryToToggleStatus = ref(null)

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
const filters = reactive<DanhMucParams>({
  search: '',
  trangThai: '', // Sửa từ undefined thành empty string để đồng bộ với radio "Tất cả"
  idNhaSanXuat: undefined,
  idXuatXu: undefined,
  giaTu: 0,
  giaDen: 5000000,
})

// Data
const danhMucList = ref<DanhMucResponse | null>(null) // Raw data from API
const totalElements = ref(0)
const nhaSanXuatOptions = ref<NhaSanXuatOption[]>([])
const xuatXuOptions = ref<XuatXuOption[]>([])

// Pagination state
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number, range: [number, number]) => `Hiển thị ${range[0]}-${range[1]} trong tổng số ${total} sản phẩm`,
})

// Maps for quick lookup of names
const nhaSanXuatMap = ref<Map<number, string>>(new Map())
const xuatXuMap = ref<Map<number, string>>(new Map())

// Price range ref để tránh tạo array mới
const priceRange = ref([0, 5000000])

// Đồng bộ priceRange với filters
const syncPriceRange = () => {
  priceRange.value = [filters.giaTu || 0, filters.giaDen || 5000000]
}

// Watcher để sync khi filters thay đổi
watch([() => filters.giaTu, () => filters.giaDen], syncPriceRange, { immediate: true })

// Cập nhật filters khi priceRange thay đổi
const updatePriceRange = (value: number[]) => {
  if (Array.isArray(value) && value.length === 2) {
    ;[filters.giaTu, filters.giaDen] = value
  }
}

// Computed property to transform and filter danhMucList for table display
const transformedDanhMucList = computed(() => {
  if (!danhMucList.value || !danhMucList.value.data || !Array.isArray(danhMucList.value.data)) return []
  let filteredList = danhMucList.value.data
  // Apply search filter
  if (filters.search) {
    const lowerCaseSearch = filters.search.toLowerCase()
    filteredList = filteredList.filter(
      (item) =>
        item.maSanPham?.toLowerCase().includes(lowerCaseSearch) ||
        item.tenSanPham.toLowerCase().includes(lowerCaseSearch) ||
        (item.giaNhoNhat && item.giaLonNhat && `${item.giaNhoNhat} - ${item.giaLonNhat}`.includes(lowerCaseSearch))
    )
  }

  // Apply status filter
  if (filters.trangThai !== undefined && filters.trangThai !== '') {
    filteredList = filteredList.filter((item) => item.trangThai === filters.trangThai)
  }

  // Apply NhaSanXuat filter
  if (filters.idNhaSanXuat) {
    filteredList = filteredList.filter((item) => item.idNhaSanXuat === filters.idNhaSanXuat)
  }

  // Apply XuatXu filter
  if (filters.idXuatXu) {
    filteredList = filteredList.filter((item) => item.idXuatXu === filters.idXuatXu)
  }

  // Apply price range filter
  if (filters.giaTu !== undefined && filters.giaDen !== undefined) {
    filteredList = filteredList.filter(
      (item) =>
        (item.giaNhoNhat === null || item.giaNhoNhat >= filters.giaTu) && (item.giaLonNhat === null || item.giaLonNhat <= filters.giaDen)
    )
  }

  // Update total elements after filtering for frontend pagination
  totalElements.value = danhMucList.value.totalElements || filteredList.length

  // Nếu đang sử dụng backend pagination thì không cần frontend pagination
  const result = filteredList.map((item) => ({
    ...item,
    name: item.tenSanPham, // Map tenSanPham to name for table column
    status: item.trangThai ? 'active' : 'inactive', // Map boolean trangThai to string status
    giaThapNhat: item.giaNhoNhat, // Map giaNhoNhat to giaThapNhat
    giaCaoNhat: item.giaLonNhat, // Map giaLonNhat to giaCaoNhat
    nhaSanXuatName: nhaSanXuatMap.value.get(item.idNhaSanXuat) || '',
    xuatXuName: xuatXuMap.value.get(item.idXuatXu) || '',
  }))

  return result
})

const loading = ref(false)

const loadDanhMucList = async () => {
  try {
    loading.value = true
    const response = await getDanhMucSanPhamList(pagination.value.current - 1, pagination.value.pageSize)

    if (response.data) {
      const apiData = response.data

      // Match the API response structure like bien-the
      if (apiData && apiData.data && Array.isArray(apiData.data)) {
        danhMucList.value = apiData
        pagination.value.total = apiData.totalElements || 0
        pagination.value.current = (apiData.currentPage || 0) + 1
        pagination.value.pageSize = apiData.pageSize || 10
      } else {
        // Fallback if structure is different
        danhMucList.value = response.data
        pagination.value.total = response.data.totalElements || 0
      }
    }
  } catch (error) {
    console.error('Error loading danh mục:', error)
    Message.error('Không thể tải danh sách danh mục sản phẩm')
    danhMucList.value = null
    pagination.value.total = 0
  } finally {
    loading.value = false
  }
}

// Pagination change handler
const handlePageChange = (page: number, size: number) => {
  pagination.value.current = page
  pagination.value.pageSize = size
  loadDanhMucList()
}

// Tìm kiếm với debounce
const searchCategories = debounce(() => {
  pagination.value.current = 1 // Reset về trang đầu khi tìm kiếm
  loadDanhMucList()
}, 500)

// Cập nhật price range và tìm kiếm
const updatePriceRangeAndSearch = (value: number[]) => {
  updatePriceRange(value)
  searchCategories()
}

// Table
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
    title: 'STT',
    dataIndex: 'index',
    width: 70,
    align: 'center',
    slotName: 'index',
  },
  {
    title: 'Mã sản phẩm',
    dataIndex: 'maSanPham',
    width: 110,
  },
  {
    title: 'Tên sản phẩm',
    dataIndex: 'name', // Sẽ được ánh xạ từ tenSanPham
    slotName: 'name',
    width: 200,
  },
  {
    title: 'Nhà sản xuất',
    dataIndex: 'nhaSanXuatName', // Sử dụng tên đã ánh xạ
    width: 110,
    align: 'center',
  },
  {
    title: 'Xuất xứ',
    dataIndex: 'xuatXuName', // Sử dụng tên đã ánh xạ
    width: 100,
    align: 'center',
  },
  {
    title: 'Số lượng biến thể',
    dataIndex: 'soLuongBienThe',
    width: 100,
    align: 'center',
  },
  {
    title: 'Khoảng giá(đ)',
    dataIndex: 'price_range',
    slotName: 'price_range',
    width: 170,
  },
  {
    title: 'Trạng thái',
    dataIndex: 'status', // Sử dụng status đã ánh xạ
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
// Sử dụng danhMucList từ API thay vì mock data

// API sẽ tự động filter và search phía backend

// Pagination được xử lý trực tiếp trong table component

// Form validation rules
const formRules = {
  name: [
    { required: true, message: 'Vui lòng nhập tên danh mục' },
    { min: 2, max: 50, message: 'Tên danh mục phải từ 2-50 ký tự' },
  ],
  description: [{ max: 200, message: 'Mô tả không được vượt quá 200 ký tự' }],
}

// Methods đã được định nghĩa ở phía dưới

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
  // Navigate đến trang biến thể sản phẩm với ID sản phẩm
  router.push({
    name: 'BienTheSanPhamTheoProd',
    params: {
      productId: category.id,
    },
  })
}

// Toggle status function - show confirm first
const toggleStatus = (record: any) => {
  categoryToToggleStatus.value = record
  showStatusConfirm.value = true
}

// Actual toggle status implementation
const performToggleStatus = async (record: any) => {
  try {
    // Set loading state for this specific record
    record.updating = true

    // Get current userStore for user info
    const userStore = useUserStore()

    // Prepare full update data with all required fields
    const updateData = {
      tenSanPham: record.tenSanPham,
      idNhaSanXuat: record.idNhaSanXuat,
      idXuatXu: record.idXuatXu,
      trangThai: !record.trangThai, // Toggle status
      deleted: record.deleted || false,
      createAt: record.createAt,
      createBy: record.createBy,
      updateAt: new Date().toISOString(),
      updateBy: userStore.id || 1, // Use current user ID
    }

    // Call API to update category status
    const response = await updateDanhMucSanPham(record.id, updateData)

    if (response.status === 200 || response.data) {
      // Update local data immediately for better UX
      record.trangThai = !record.trangThai

      // Update in danhMucList array
      if (danhMucList.value?.data) {
        const index = danhMucList.value.data.findIndex((c) => c.id === record.id)
        if (index !== -1) {
          danhMucList.value.data[index].trangThai = record.trangThai
        }
      }

      const statusText = record.trangThai ? 'Đang bán' : 'Tạm ngưng bán'
      Message.success(`Đã cập nhật trạng thái thành: ${statusText}`)
    } else {
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

const confirmToggleStatus = async () => {
  await performToggleStatus(categoryToToggleStatus.value)
  showStatusConfirm.value = false
  categoryToToggleStatus.value = null
}

const cancelToggleStatus = () => {
  showStatusConfirm.value = false
  categoryToToggleStatus.value = null
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    modalVisible.value = false
  } catch (error) {
    Message.error('Có lỗi xảy ra khi lưu danh mục sản phẩm')
  }
}

const handleCancel = () => {
  modalVisible.value = false
}

// Load options cho dropdown
const loadOptions = async () => {
  try {
    // Debug: Starting loadOptions
    const [nhaSanXuatRes, xuatXuRes] = await Promise.all([getNhaSanXuatOptions(), getXuatXuOptions()])
    // Debug: Options loaded
    nhaSanXuatOptions.value = nhaSanXuatRes.data
    xuatXuOptions.value = xuatXuRes.data

    // Build maps for quick lookup
    nhaSanXuatMap.value = new Map(nhaSanXuatRes.data.map((item) => [item.id, item.tenNhaSanXuat]))
    xuatXuMap.value = new Map(xuatXuRes.data.map((item) => [item.id, item.tenXuatXu]))

    // Debug: loadOptions completed
  } catch (error) {
    // Error: Lỗi khi tải options
    // Error details available in development
    Message.error('Không thể tải dữ liệu dropdown')
  }
}

// Reset filters
const resetFilters = () => {
  Object.assign(filters, {
    search: '',
    trangThai: '', // Sửa từ undefined thành empty string để đồng bộ với radio "Tất cả"
    idNhaSanXuat: undefined,
    idXuatXu: undefined,
    giaTu: 0,
    giaDen: 5000000,
  })

  // Reset pagination to first page
  pagination.value.current = 1

  loadDanhMucList()
}

const handleTableChange = (paginationData: any, filtersData: any, sorter: any) => {
  // Handle table change
}

const completeBulkUpdate = async () => {
  // Update selected items via API
  try {
    // Debug: Starting completeBulkUpdate
    // TODO: Implement bulk update API call
    // Debug: Bulk update API not implemented yet, reloading list
    // For now, just reload the list
    await loadDanhMucList()
    // Debug: completeBulkUpdate completed
  } catch (error) {
    // Error: Lỗi khi cập nhật hàng loạt
    // Error details available in development
    Message.error('Không thể cập nhật hàng loạt')
  }

  // Clear selection and editing data
  selectedRowKeys.value = []
  editingData.value = {}
}

const exportExcel = () => {
  try {
    if (!transformedDanhMucList.value || transformedDanhMucList.value.length === 0) {
      Message.warning('Không có dữ liệu để xuất Excel')
      return
    }

    exportToExcel(transformedDanhMucList.value, EXPORT_HEADERS.DANH_MUC, 'danh-muc-san-pham')
  } catch (error) {
    console.error('Lỗi khi xuất Excel:', error)
    Message.error('Có lỗi xảy ra khi xuất Excel')
  }
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

const handleUploadChange = (info: any) => {}

onMounted(async () => {
  try {
    // Debug: Component mounted, initializing...

    // Ensure reactive state is properly initialized
    selectedRowKeys.value = Array.isArray(selectedRowKeys.value) ? selectedRowKeys.value : []
    editingData.value = editingData.value && typeof editingData.value === 'object' ? editingData.value : {}
    // Debug: Reactive state initialized

    // Load dữ liệu ban đầu
    // Debug: Loading initial data...
    await Promise.all([loadOptions(), loadDanhMucList()])
    // Debug: Initial data loaded successfully
  } catch (error) {
    // Error: Lỗi khi khởi tạo component
    // Error details available in development
    Message.error('Không thể tải dữ liệu ban đầu')
  }
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
