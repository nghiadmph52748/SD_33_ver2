<template>
  <div class="product-category-page">
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
                  :max="maxPrice"
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
        :row-class="(record) => (isRowSelected(record.id) ? 'editing-row' : '')"
      >
        <template #checkbox="{ record }">
          <a-checkbox :model-value="isRowSelected(record.id)" @change="(checked) => handleCheckboxChange(checked, record)" />
        </template>
        <template #index="{ rowIndex }">
          {{ (pagination.current - 1) * pagination.pageSize + rowIndex + 1 }}
        </template>
        <template #status="{ record }">
          <div v-if="isRowSelected(record.id)">
            <!-- Edit mode -->
            <a-select
              :model-value="editingData[record.id]?.trangThai ? 'active' : 'inactive'"
              @update:model-value="
                (value) => {
                  editingData[record.id] = { ...(editingData[record.id] || {}), trangThai: value === 'active' }
                }
              "
              size="mini"
              style="width: 120px"
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
              :model-value="editingData[record.id]?.tenSanPham || record.tenSanPham"
              @update:model-value="
                (value) => {
                  editingData[record.id] = { ...(editingData[record.id] || {}), tenSanPham: value }
                }
              "
              size="mini"
              style="width: 100%"
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
          <a-checkbox :model-value="isAllSelected" :indeterminate="isSomeSelected" @change="handleSelectAllChange" />
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { debounce } from 'lodash'
import { useUserStore } from '@/store'
import {
  getDanhMucSanPhamList,
  getAllDanhMucSanPham,
  getNhaSanXuatOptions,
  getXuatXuOptions,
  updateDanhMucSanPham,
} from '@/api/san-pham/danh-muc'
import type { DanhMucParams, NhaSanXuatOption, XuatXuOption, DanhMucResponse } from '@/api/san-pham/danh-muc'
import { IconPlus, IconRefresh, IconDownload, IconEye, IconCheckCircle, IconCheck, IconClose } from '@arco-design/web-vue/es/icon'
import { Message } from '@arco-design/web-vue'
import { exportToExcel, EXPORT_HEADERS } from '@/utils/export-excel'
// Form and modal
// Breadcrumb setup
const router = useRouter()
// Edit inline mode state
const selectedRowKeys = ref<number[]>([])
const editingData = ref<Record<number, { tenSanPham: string; trangThai: boolean }>>({}) // Updated to match backend
// Status toggle modal
const showStatusConfirm = ref(false)
const categoryToToggleStatus = ref(null)

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
// Max price from products - will be updated when data loads
const maxPrice = ref(5000000)

// Đồng bộ priceRange với filters
const syncPriceRange = () => {
  priceRange.value = [filters.giaTu || 0, filters.giaDen || maxPrice.value]
}

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

// Computed properties for safe access
const selectedCount = computed(() => (Array.isArray(selectedRowKeys.value) ? selectedRowKeys.value.length : 0))
const hasSelectedRows = computed(() => selectedCount.value > 0)
const isRowSelected = (id: number) => {
  const keys = Array.isArray(selectedRowKeys.value) ? selectedRowKeys.value : []
  return keys.includes(id)
}

// Computed for select all checkbox
const currentPageData = computed(() => transformedDanhMucList.value || [])
const isAllSelected = computed(() => {
  const pageData = currentPageData.value
  if (!pageData || pageData.length === 0) return false
  return pageData.every((item) => isRowSelected(item.id))
})
const isSomeSelected = computed(() => {
  const pageData = currentPageData.value
  if (!pageData || pageData.length === 0) return false
  const selectedInPage = pageData.filter((item) => isRowSelected(item.id)).length
  return selectedInPage > 0 && selectedInPage < pageData.length
})

// Checkbox change handlers
const handleCheckboxChange = (checked: boolean, record: any) => {
  if (checked) {
    // Add to selection - Create NEW array to trigger reactivity
    if (!selectedRowKeys.value.includes(record.id)) {
      selectedRowKeys.value = [...selectedRowKeys.value, record.id]
    }
    // Initialize editing data
    editingData.value[record.id] = {
      tenSanPham: record.tenSanPham,
      trangThai: record.trangThai,
    }
  } else {
    // Remove from selection - Create NEW array to trigger reactivity
    selectedRowKeys.value = selectedRowKeys.value.filter((id) => id !== record.id)

    // Remove editing data
    delete editingData.value[record.id]

    // Force reactivity update
    editingData.value = { ...editingData.value }
  }
}

const handleSelectAllChange = (checked: boolean) => {
  const pageData = currentPageData.value
  if (checked) {
    // Collect all IDs to add
    const newIds = pageData.filter((item) => !selectedRowKeys.value.includes(item.id)).map((item) => item.id)

    // Create NEW array to trigger reactivity
    selectedRowKeys.value = [...selectedRowKeys.value, ...newIds]

    // Initialize editing data for all items
    pageData.forEach((item) => {
      editingData.value[item.id] = {
        tenSanPham: item.tenSanPham,
        trangThai: item.trangThai,
      }
    })
  } else {
    const pageIds = pageData.map((item) => item.id)

    // Create NEW array to trigger reactivity
    selectedRowKeys.value = selectedRowKeys.value.filter((id) => !pageIds.includes(id))

    // Delete editing data
    pageIds.forEach((id) => {
      delete editingData.value[id]
    })

    // Force reactivity update for editingData
    editingData.value = { ...editingData.value }
  }
}

const loading = ref(false)

const loadDanhMucList = async () => {
  try {
    loading.value = true
    const response = await getDanhMucSanPhamList(pagination.value.current - 1, pagination.value.pageSize)
    if (response.data) {
      const apiData = response.data

      if (apiData && apiData.data && Array.isArray(apiData.data)) {
        danhMucList.value = apiData
        pagination.value.total = apiData.totalElements || 0
        pagination.value.current = (apiData.currentPage || 0) + 1
        pagination.value.pageSize = apiData.pageSize || 10

        // Load all data to calculate max price
        try {
          const allResponse = await getAllDanhMucSanPham()
          if (allResponse.data && allResponse.data.data && Array.isArray(allResponse.data.data)) {
            let allMaxPrice = 0
            allResponse.data.data.forEach((item: any) => {
              if (item.giaLonNhat && item.giaLonNhat > allMaxPrice) {
                allMaxPrice = item.giaLonNhat
              }
            })
            if (allMaxPrice > 0) {
              maxPrice.value = allMaxPrice
            }
          }
        } catch (error) {
          console.error('Error loading all danh muc for max price:', error)
        }

        // Update price range max value
        if (priceRange.value[1] < maxPrice.value) {
          priceRange.value[1] = maxPrice.value
        }
      } else {
        danhMucList.value = response.data
        pagination.value.total = response.data?.totalElements || 0
      }
    }
  } catch (error) {
    // eslint-disable-next-line no-console
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
      // eslint-disable-next-line no-console
      // console.error('API response not successful:', response)
      Message.error('Cập nhật trạng thái thất bại')
    }
  } catch {
    // eslint-disable-next-line no-console
    // console.error('Error toggling status:', error)
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

// Load options cho dropdown
const loadOptions = async () => {
  try {
    const [nhaSanXuatRes, xuatXuRes] = await Promise.all([getNhaSanXuatOptions(), getXuatXuOptions()])
    nhaSanXuatOptions.value = nhaSanXuatRes.data
    xuatXuOptions.value = xuatXuRes.data

    // Build maps for quick lookup
    nhaSanXuatMap.value = new Map(nhaSanXuatRes.data.map((item) => [item.id, item.tenNhaSanXuat]))
    xuatXuMap.value = new Map(xuatXuRes.data.map((item) => [item.id, item.tenXuatXu]))

    // Debug: loadOptions completed
  } catch {
    // eslint-disable-next-line no-console
    // console.error('Lỗi khi tải options:', error)
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
    giaDen: maxPrice.value,
  })

  // Reset pagination to first page
  pagination.value.current = 1

  loadDanhMucList()
}

const completeBulkUpdate = async () => {
  // Update selected items via API
  try {
    const userStore = useUserStore()
    const updatePromises = []

    // Loop through each selected row and update
    selectedRowKeys.value.forEach((recordId) => {
      // Get edited data for this record
      const editData = editingData.value[recordId]
      if (!editData) return

      // Find the original record to compare changes
      const originalRecord = danhMucList.value?.data?.find((item) => item.id === recordId)
      if (!originalRecord) return

      // Check if there are any changes
      const hasChanges = editData.tenSanPham !== originalRecord.tenSanPham || editData.trangThai !== originalRecord.trangThai

      if (hasChanges) {
        // Prepare update data - PHẢI GỬI ĐẦY ĐỦ TẤT CẢ FIELD
        const updateData = {
          id: recordId,
          tenSanPham: editData.tenSanPham,
          trangThai: editData.trangThai,
          idNhaSanXuat: originalRecord.idNhaSanXuat, // Giữ nguyên
          idXuatXu: originalRecord.idXuatXu, // Giữ nguyên
          deleted: originalRecord.deleted || false, // Giữ nguyên
          createAt: originalRecord.createAt, // Giữ nguyên
          createBy: originalRecord.createBy, // Giữ nguyên
          updateAt: new Date().toISOString().split('T')[0],
          updateBy: userStore.id,
        }
        // Add to update promises
        updatePromises.push(updateDanhMucSanPham(recordId, updateData))
      }
    })

    // Execute all updates in parallel
    if (updatePromises.length > 0) {
      await Promise.all(updatePromises)
      Message.success(`Đã cập nhật thành công ${updatePromises.length} sản phẩm`)
    } else {
      Message.info('Không có thay đổi nào để cập nhật')
    }

    // Reload the list to reflect changes
    await loadDanhMucList()

    // Clear selection and editing data
    selectedRowKeys.value = []
    editingData.value = {}
  } catch (error) {
    // eslint-disable-next-line no-console
    console.error('❌ LỖI khi cập nhật hàng loạt:', error)
    // eslint-disable-next-line no-console
    console.error('❌ Error details:', {
      message: error.message,
      stack: error.stack,
      response: error.response,
    })
    Message.error('Không thể cập nhật hàng loạt. Vui lòng thử lại.')

    // Clear selection even on error
    selectedRowKeys.value = []
    editingData.value = {}
  }
}

const exportExcel = () => {
  try {
    if (!transformedDanhMucList.value || transformedDanhMucList.value.length === 0) {
      Message.warning('Không có dữ liệu để xuất Excel')
      return
    }

    exportToExcel(transformedDanhMucList.value, EXPORT_HEADERS.DANH_MUC, 'danh-muc-san-pham')
  } catch {
    // eslint-disable-next-line no-console
    // console.error('Lỗi khi xuất Excel:', error)
    Message.error('Có lỗi xảy ra khi xuất Excel')
  }
}

onMounted(async () => {
  try {
    // Ensure reactive state is properly initialized
    selectedRowKeys.value = Array.isArray(selectedRowKeys.value) ? selectedRowKeys.value : []
    editingData.value = editingData.value && typeof editingData.value === 'object' ? editingData.value : {}

    // Load dữ liệu ban đầu
    await Promise.all([loadOptions(), loadDanhMucList()])
  } catch {
    // eslint-disable-next-line no-console
    // console.error('Lỗi khi khởi tạo component:', error)
    Message.error('Không thể tải dữ liệu ban đầu')
  }
})
</script>

<style scoped>
.product-category-page {
  padding: 16px 20px;
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

/* Edit inline mode styles */
:deep(.editing-row) {
  background-color: #f0f8ff !important;
  border-left: 3px solid #1890ff;
}

:deep(.editing-row:hover) {
  background-color: #e6f4ff !important;
}

:deep(.editing-row td) {
  background-color: transparent !important;
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
