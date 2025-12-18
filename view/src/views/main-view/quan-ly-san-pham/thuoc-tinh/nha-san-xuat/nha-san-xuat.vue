<template>
  <div class="product-attribute-manufacturer-page">
    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="filters" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="filters.search" placeholder="Tìm theo mã hoặc tên nhà sản xuất..." allow-clear
                @input="searchManufacturers" />
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
      <a-table :columns="columns" :data="manufacturers" :pagination="pagination" :loading="loading"
        :scroll="{ x: 1000 }" @page-change="getNhaSanXuatPage($event - 1)" @page-size-change="
          (size) => {
            pagination.pageSize = size
            getNhaSanXuatPage(0)
          }
        ">
        <template #stt="{ rowIndex }">
          <div>{{ rowIndex + 1 }}</div>
        </template>

        <template #code="{ record }">
          <span>{{ record.maNhaSanXuat }}</span>
        </template>

        <template #name="{ record }">
          <span>{{ record.tenNhaSanXuat }}</span>
        </template>

        <template #status="{ record }">
          <a-tag :color="record.trangThai ? 'green' : 'red'">
            {{ record.trangThai ? 'Hoạt động' : 'Không hoạt động' }}
          </a-tag>
        </template>

        <template #action="{ record }">
          <a-space>
            <a-tooltip content="Thay đổi trạng thái">
              <a-switch :model-value="record.trangThai" type="round" @click="toggleStatus(record)"
                :loading="record.updating">
                <template #checked-icon>
                  <icon-check />
                </template>
                <template #unchecked-icon>
                  <icon-close />
                </template>
              </a-switch>
            </a-tooltip>
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
            <!-- <a-button type="text" danger @click="deleteManufacturer(record)">
              <template #icon>
                <icon-delete />
              </template>
            </a-button> -->
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- Add Manufacturer Modal -->
    <a-modal v-model:visible="addModalVisible" title="Thêm nhà sản xuất" width="600px" :mask-closable="false"
      :closable="true" @cancel="closeAddModal" @ok="confirmAddManufacturer">
      <a-form :model="manufacturerForm" :rules="formRules" layout="vertical" ref="addFormRef">
        <a-form-item>
          <template #label>
            <span class="required-field">Tên nhà sản xuất</span>
          </template>
          <a-input v-model="manufacturerForm.tenNhaSanXuat" placeholder="Nhập tên nhà sản xuất" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Detail Manufacturer Modal -->
    <a-modal v-model:visible="detailModalVisible" title="Chi tiết nhà sản xuất" width="600px" :mask-closable="false"
      :closable="true" @cancel="closeDetailModal" @ok="closeDetailModal" ok-text="Đóng"
      :cancel-button-props="{ style: { display: 'none' } }">
      <a-descriptions :column="1" size="small">
        <a-descriptions-item label="Mã nhà sản xuất">{{ selectedManufacturer?.maNhaSanXuat }}</a-descriptions-item>
        <a-descriptions-item label="Tên nhà sản xuất">{{ selectedManufacturer?.tenNhaSanXuat }}</a-descriptions-item>
        <a-descriptions-item label="Trạng thái">
          <a-tag :color="selectedManufacturer?.trangThai ? 'green' : 'red'">
            {{ selectedManufacturer?.trangThai ? 'Hoạt động' : 'Không hoạt động' }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- Update Manufacturer Modal -->
    <a-modal v-model:visible="updateModalVisible" title="Cập nhật nhà sản xuất" width="600px" :mask-closable="false"
      :closable="true" @cancel="closeUpdateModal" @ok="confirmUpdateManufacturer">
      <a-form :model="manufacturerForm" :rules="formRules" layout="vertical" ref="updateFormRef">
        <a-form-item>
          <template #label>
            <span class="required-field">Tên nhà sản xuất</span>
          </template>
          <a-input v-model="manufacturerForm.tenNhaSanXuat" placeholder="Nhập tên nhà sản xuất" />
        </a-form-item>

        <a-form-item>
          <template #label>
            <span class="required-field">Trạng thái</span>
          </template>
          <a-radio-group v-model="manufacturerForm.trangThai" type="button">
            <a-radio :value="true">Hoạt động</a-radio>
            <a-radio :value="false">Không hoạt động</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Confirmation Modal -->
    <a-modal v-model:visible="confirmModalVisible" title="Xác nhận" width="400px" :mask-closable="false"
      :closable="true" @cancel="cancelConfirm" @ok="executeConfirmedAction">
      <p>{{ confirmMessage }}</p>
    </a-modal>

    <!-- Status Toggle Confirm Modal -->
    <a-modal v-model:visible="showStatusConfirm" title="Xác nhận thay đổi trạng thái" ok-text="Xác nhận"
      cancel-text="Huỷ" @ok="confirmToggleStatus" @cancel="cancelToggleStatus">
      <template #default>
        <div v-if="manufacturerToToggleStatus">
          <div>Bạn có chắc chắn muốn {{ manufacturerToToggleStatus.trangThai ? 'tạm ngưng' : 'kích hoạt' }} nhà sản xuất
            này?</div>
          <div>
            Tên nhà sản xuất:
            <strong>{{ manufacturerToToggleStatus.tenNhaSanXuat }}</strong>
          </div>
          <div>
            Trạng thái hiện tại:
            <strong>{{ manufacturerToToggleStatus.trangThai ? 'Hoạt động' : 'Không hoạt động' }}</strong>
          </div>
        </div>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { exportToExcel, EXPORT_HEADERS } from '@/utils/export-excel'
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
  IconCheck,
  IconClose,
} from '@arco-design/web-vue/es/icon'
import { useUserStore } from '@/store'
import {
  createNhaSanXuat,
  getNhaSanXuatList,
  updateNhaSanXuat,
  deleteNhaSanXuat,
} from '../../../../../api/san-pham/thuoc-tinh/nha-san-xuat'

// Breadcrumb setup
const userStore = useUserStore()
// Filters
const filters = ref({
  search: '',
  status: '',
  folder: '',
  sort: 'newest',
})

// Data
const manufacturers = ref([])

// Filtered manufacturers computed property
const filteredManufacturers = computed(() => {
  let filtered = [...manufacturers.value]

  // Filter by search term
  if (filters.value.search) {
    const searchTerm = filters.value.search.toLowerCase()
    filtered = filtered.filter(
      (manufacturer) =>
        manufacturer.maNhaSanXuat?.toLowerCase().includes(searchTerm) || manufacturer.tenNhaSanXuat?.toLowerCase().includes(searchTerm)
    )
  }

  // Filter by status
  if (filters.value.status) {
    const statusFilter = filters.value.status === 'active'
    filtered = filtered.filter((manufacturer) => manufacturer.trangThai === statusFilter)
  }

  return filtered
})

// Modal states
const addModalVisible = ref(false)
const detailModalVisible = ref(false)
const updateModalVisible = ref(false)
const confirmModalVisible = ref(false)

// Form refs
const addFormRef = ref()
const updateFormRef = ref()

// Selected manufacturer for detail/update
const selectedManufacturer = ref(null)

// Manufacturer form data
const manufacturerForm = reactive({
  tenNhaSanXuat: '',
  trangThai: true,
})

// Form validation rules
const formRules = {
  tenNhaSanXuat: [
    { required: true, message: 'Vui lòng nhập tên nhà sản xuất' },
    { min: 2, message: 'Tên nhà sản xuất phải có ít nhất 2 ký tự' },
  ],
  trangThai: [{ required: true, message: 'Vui lòng chọn trạng thái' }],
}

// Confirmation modal
const confirmMessage = ref('')
const confirmAction = ref(null)

// Status toggle modal
const showStatusConfirm = ref(false)
const manufacturerToToggleStatus = ref(null)

// Computed properties for safe access removed

// Table
const loading = ref(false)
const columns = [
  {
    title: 'STT',
    dataIndex: 'stt',
    slotName: 'stt',
    width: 50,
    align: 'center',
  },
  {
    title: 'Mã nhà sản xuất',
    dataIndex: 'code',
    slotName: 'code',
    width: 100,
  },
  {
    title: 'Tên nhà sản xuất',
    dataIndex: 'name',
    width: 100,
    slotName: 'name',
  },
  {
    title: 'Trạng thái',
    dataIndex: 'is_active',
    slotName: 'status',
    width: 100,
    // align: 'center',
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
}

const showCreateModal = () => {
  manufacturerForm.tenNhaSanXuat = ''
  manufacturerForm.trangThai = true
  addModalVisible.value = true
}

const closeAddModal = () => {
  addModalVisible.value = false
  addFormRef.value?.resetFields()
}

const confirmAddManufacturer = () => {
  addFormRef.value
    ?.validate()
    .then(() => {
      confirmMessage.value = 'Bạn có chắc chắn muốn thêm nhà sản xuất này?'
      confirmAction.value = 'add'
      confirmModalVisible.value = true
    })
    .catch(() => {
      // Validation failed
    })
}

const viewManufacturer = (manufacturer: any) => {
  selectedManufacturer.value = manufacturer
  detailModalVisible.value = true
}

const closeDetailModal = () => {
  detailModalVisible.value = false
  selectedManufacturer.value = null
}

const editManufacturer = (manufacturer: any) => {
  selectedManufacturer.value = manufacturer
  manufacturerForm.tenNhaSanXuat = manufacturer.tenNhaSanXuat
  manufacturerForm.trangThai = manufacturer.trangThai
  updateModalVisible.value = true
}

const closeUpdateModal = () => {
  updateModalVisible.value = false
  updateFormRef.value?.resetFields()
  selectedManufacturer.value = null
}

const confirmUpdateManufacturer = () => {
  updateFormRef.value
    ?.validate()
    .then(() => {
      confirmMessage.value = 'Bạn có chắc chắn muốn cập nhật nhà sản xuất này?'
      confirmAction.value = 'update'
      confirmModalVisible.value = true
    })
    .catch(() => {
      // Validation failed
    })
}

const deleteManufacturer = (manufacturer: any) => {
  selectedManufacturer.value = manufacturer
  confirmMessage.value = 'Bạn có chắc chắn muốn xóa nhà sản xuất này?'
  confirmAction.value = 'delete'
  confirmModalVisible.value = true
}

const cancelConfirm = () => {
  confirmModalVisible.value = false
  confirmMessage.value = ''
  confirmAction.value = null
}

// Toggle status function - show confirm first
const toggleStatus = (record: any) => {
  manufacturerToToggleStatus.value = record
  showStatusConfirm.value = true
}

// Actual toggle status implementation
const performToggleStatus = async (record: any) => {
  try {
    // Set loading state for this specific record
    record.updating = true

    // Call API to update manufacturer status
    const updateData = {
      tenNhaSanXuat: record.tenNhaSanXuat,
      trangThai: !record.trangThai, // Toggle status
      deleted: record.deleted,
      createAt: record.createAt,
      createBy: record.createBy,
      updateAt: new Date().toISOString().split('T')[0],
      updateBy: userStore.id,
    }

    const response = await updateNhaSanXuat(record.id, updateData)

    if (response.success || response.status === 200) {
      // Update local data immediately for better UX
      record.trangThai = !record.trangThai

      // Update in manufacturers array
      const index = manufacturers.value.findIndex((m) => m.id === record.id)
      if (index !== -1) {
        manufacturers.value[index].trangThai = record.trangThai
      }

      const statusText = record.trangThai ? 'Hoạt động' : 'Không hoạt động'
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
  await performToggleStatus(manufacturerToToggleStatus.value)
  showStatusConfirm.value = false
  manufacturerToToggleStatus.value = null
}

const cancelToggleStatus = () => {
  showStatusConfirm.value = false
  manufacturerToToggleStatus.value = null
}

const getNhaSanXuatPage = async (page) => {
  try {
    const res = await getNhaSanXuatList(page, pagination.value.pageSize)
    if (res.success) {
      manufacturers.value = (res.data.data || []).sort((a: any, b: any) => (b.id || 0) - (a.id || 0))
      pagination.value.total = res.data.totalElements
      pagination.value.pageSize = res.data.pageSize
      pagination.value.current = res.data.currentPage + 1
    } else {
      console.error('Failed to fetch manufacturers:', res.message)
      manufacturers.value = []
      pagination.value.total = 0
      pagination.value.pageSize = 10
      pagination.value.current = 1
    }
  } catch (error) {
    console.error('Failed to fetch manufacturers:', error)
  }
}

const loadManufacturersWithUpdatedFirst = async (updatedId?: number, isNewItem: boolean = false) => {
  try {
    const res = await getNhaSanXuatList(0, 9)
    if (res.success) {
      let manufacturersData = (res.data.data || []).sort((a: any, b: any) => (b.id || 0) - (a.id || 0))
      pagination.value.total = res.data.totalElements
      pagination.value.pageSize = res.data.size
      pagination.value.current = res.data.number + 1

      // If there's an updated item and it's not a new item, move it to the front
      if (updatedId && !isNewItem) {
        const updatedIndex = manufacturersData.findIndex((manufacturer) => manufacturer.id === updatedId)
        if (updatedIndex > 0) {
          const updatedItem = manufacturersData.splice(updatedIndex, 1)[0]
          manufacturersData = [updatedItem, ...manufacturersData]
        }
      }

      manufacturers.value = manufacturersData
    } else {
      console.error('Failed to fetch manufacturers:', res.message)
      manufacturers.value = []
      pagination.value.total = 0
      pagination.value.pageSize = 10
      pagination.value.current = 1
    }
  } catch (error) {
    console.error('Failed to fetch manufacturers:', error)
  }
}

const executeConfirmedAction = async () => {
  try {
    if (confirmAction.value === 'add') {
      // TODO: Implement add API call
      const data = {
        tenNhaSanXuat: manufacturerForm.tenNhaSanXuat,
        trangThai: true,
        deleted: false,
        createAt: new Date().toISOString().split('T')[0],
        createBy: userStore.id,
      }
      const res = await createNhaSanXuat(data)
      closeAddModal()
      // Load data with new item first (always load from page 0 for new items)
      loadManufacturersWithUpdatedFirst(undefined, true)
      Message.success('Thêm nhà sản xuất thành công!')
    } else if (confirmAction.value === 'update') {
      // TODO: Implement update API call
      if (!selectedManufacturer.value) {
        console.error('No manufacturer selected for update')
        return
      }

      const manufacturerId = selectedManufacturer.value.id
      const data = {
        tenNhaSanXuat: manufacturerForm.tenNhaSanXuat,
        trangThai: manufacturerForm.trangThai,
        deleted: selectedManufacturer.value.deleted,
        createAt: selectedManufacturer.value.createAt,
        createBy: selectedManufacturer.value.createBy,
        updateAt: new Date().toISOString().split('T')[0],
        updateBy: userStore.id,
      }
      await updateNhaSanXuat(manufacturerId, data)
      closeUpdateModal()
      // Load data with updated item first
      loadManufacturersWithUpdatedFirst(manufacturerId, false)
      Message.success('Cập nhật nhà sản xuất thành công!')
    } else if (confirmAction.value === 'delete') {
      // TODO: Implement delete API call
      if (!selectedManufacturer.value) {
        console.error('No manufacturer selected for delete')
        return
      }

      await deleteNhaSanXuat(selectedManufacturer.value.id)
      // Refresh data
      loadManufacturersWithUpdatedFirst()
      Message.success('Xóa nhà sản xuất thành công!')
    }
  } catch (error) {
    console.error('API call failed:', error)
    Message.error('Có lỗi xảy ra. Vui lòng thử lại!')
  } finally {
    confirmModalVisible.value = false
    confirmMessage.value = ''
    confirmAction.value = null
  }
}

const formatDate = (dateString: string) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('vi-VN')
}

const viewProducts = (manufacturer: any) => {
  // TODO: Implement view products functionality
}

const exportManufacturers = () => {
  try {
    if (!manufacturers.value || manufacturers.value.length === 0) {
      Message.warning('Không có dữ liệu để xuất Excel')
      return
    }

    exportToExcel(manufacturers.value, EXPORT_HEADERS.NHA_SAN_XUAT, 'nha-san-xuat')
  } catch (error) {
    console.error('Lỗi khi xuất Excel:', error)
    Message.error('Có lỗi xảy ra khi xuất Excel')
  }
}

onMounted(() => {
  getNhaSanXuatPage(0)
})
</script>

<style scoped>
.product-attribute-manufacturer-page {
  padding: 16px 20px;
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

/* Custom required field styling */
.required-field::after {
  content: ' *' !important;
  color: #f53f3f !important;
  font-weight: bold !important;
}
</style>
