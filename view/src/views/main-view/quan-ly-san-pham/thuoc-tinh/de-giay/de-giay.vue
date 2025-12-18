<template>
  <div class="product-attribute-sole-page">
    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="filters" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="filters.search" placeholder="Tìm theo mã hoặc tên đế giày..." allow-clear
                @input="searchSoles" />
            </a-form-item>
          </a-col>

          <a-col :span="8">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="filters.status" type="button" @change="searchSoles">
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
          <a-button @click="exportSoles">
            <template #icon>
              <icon-download />
            </template>
            Xuất Excel
          </a-button>
          <a-button type="primary" @click="showCreateModal">
            <template #icon>
              <icon-plus />
            </template>
            Thêm Đế giày
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!-- Soles Table -->
    <a-card title="Danh sách đế giày" class="table-card">
      <a-table :columns="columns" :data="soles" :pagination="pagination" :loading="loading" :scroll="{ x: 1000 }"
        @page-change="getDeGiayPage($event - 1)" @page-size-change="
          (size) => {
            pagination.pageSize = size
            getDeGiayPage(0)
          }
        " row-key="id">
        <template #stt="{ record, rowIndex }">
          <span>{{ rowIndex + 1 }}</span>
        </template>

        <template #code="{ record }">
          <span>{{ record.maDeGiay || 'N/A' }}</span>
        </template>

        <template #name="{ record }">
          <span>{{ record.tenDeGiay }}</span>
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
            <a-button type="text" @click="viewSole(record)">
              <template #icon>
                <icon-eye />
              </template>
            </a-button>
            <a-button type="text" @click="editSole(record)">
              <template #icon>
                <icon-edit />
              </template>
            </a-button>
            <!-- <a-button type="text" danger @click="deleteSole(record)">
              <template #icon>
                <icon-delete />
              </template>
            </a-button> -->
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- Add Sole Modal -->
    <a-modal v-model:visible="addModalVisible" title="Thêm đế giày" width="600px" :mask-closable="false"
      :closable="true" @cancel="closeAddModal" @ok="confirmAddSole">
      <a-form :model="soleForm" :rules="formRules" layout="vertical" ref="addFormRef">
        <a-form-item>
          <template #label>
            <span class="required-field">Tên đế giày</span>
          </template>
          <a-input v-model="soleForm.tenDeGiay" placeholder="Nhập tên đế giày" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Detail Sole Modal -->
    <a-modal v-model:visible="detailModalVisible" title="Chi tiết đế giày" width="600px" :mask-closable="false"
      :closable="true" @cancel="closeDetailModal" @ok="closeDetailModal" ok-text="Đóng"
      :cancel-button-props="{ style: { display: 'none' } }">
      <a-descriptions :column="1" size="small">
        <a-descriptions-item label="Mã đế giày">{{ selectedSole?.maDeGiay }}</a-descriptions-item>
        <a-descriptions-item label="Tên đế giày">{{ selectedSole?.tenDeGiay }}</a-descriptions-item>
        <a-descriptions-item label="Trạng thái">
          <a-tag :color="selectedSole?.trangThai ? 'green' : 'red'">
            {{ selectedSole?.trangThai ? 'Hoạt động' : 'Không hoạt động' }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- Update Sole Modal -->
    <a-modal v-model:visible="updateModalVisible" title="Cập nhật đế giày" width="600px" :mask-closable="false"
      :closable="true" @cancel="closeUpdateModal" @ok="confirmUpdateSole">
      <a-form :model="soleForm" :rules="formRules" layout="vertical" ref="updateFormRef">
        <a-form-item>
          <template #label>
            <span class="required-field">Tên đế giày</span>
          </template>
          <a-input v-model="soleForm.tenDeGiay" placeholder="Nhập tên đế giày" />
        </a-form-item>
        <a-form-item>
          <template #label>
            <span class="required-field">Trạng thái</span>
          </template>
          <a-radio-group v-model="soleForm.trangThai" type="button">
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
        <div v-if="soleToToggleStatus">
          <div>Bạn có chắc chắn muốn {{ soleToToggleStatus.trangThai ? 'tạm ngưng' : 'kích hoạt' }} đế giày này?</div>
          <div>
            Tên đế giày:
            <strong>{{ soleToToggleStatus.tenDeGiay }}</strong>
          </div>
          <div>
            Trạng thái hiện tại:
            <strong>{{ soleToToggleStatus.trangThai ? 'Hoạt động' : 'Không hoạt động' }}</strong>
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
import { createDeGiay, getDeGiayList, updateDeGiay, deleteDeGiay } from '../../../../../api/san-pham/thuoc-tinh/de-giay'

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
const soles = ref([])

// Filtered soles computed property
const filteredSoles = computed(() => {
  let filtered = [...soles.value]

  // Filter by search term
  if (filters.value.search) {
    const searchTerm = filters.value.search.toLowerCase()
    filtered = filtered.filter(
      (sole) => sole.maDeGiay?.toLowerCase().includes(searchTerm) || sole.tenDeGiay?.toLowerCase().includes(searchTerm)
    )
  }

  // Filter by status
  if (filters.value.status) {
    const statusFilter = filters.value.status === 'active'
    filtered = filtered.filter((sole) => sole.trangThai === statusFilter)
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

// Selected sole for detail/update
const selectedSole = ref(null)

// Sole form data
const soleForm = reactive({
  tenDeGiay: '',
  trangThai: true,
})

// Form validation rules
const formRules = {
  tenDeGiay: [
    { required: true, message: 'Vui lòng nhập tên đế giày' },
    { min: 2, message: 'Tên đế giày phải có ít nhất 2 ký tự' },
  ],
  trangThai: [{ required: true, message: 'Vui lòng chọn trạng thái' }],
}

// Confirmation modal
const confirmMessage = ref('')
const confirmAction = ref(null)

// Status toggle modal
const showStatusConfirm = ref(false)
const soleToToggleStatus = ref(null)

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
    title: 'Mã đế giày',
    dataIndex: 'code',
    slotName: 'code',
    width: 100,
  },
  {
    title: 'Tên đế giày',
    dataIndex: 'name',
    width: 120,
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
    width: 120,
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
const resetFilters = () => {
  filters.value = {
    search: '',
    status: '',
    folder: '',
    sort: 'newest',
  }
}

const searchSoles = () => {
  // TODO: Implement search functionality
}

const showCreateModal = () => {
  soleForm.tenDeGiay = ''
  soleForm.trangThai = true
  addModalVisible.value = true
}

const closeAddModal = () => {
  addModalVisible.value = false
  addFormRef.value?.resetFields()
}

const confirmAddSole = () => {
  addFormRef.value
    ?.validate()
    .then(() => {
      confirmMessage.value = 'Bạn có chắc chắn muốn thêm đế giày này?'
      confirmAction.value = 'add'
      confirmModalVisible.value = true
    })
    .catch(() => {
      // Validation failed
    })
}

const viewSole = (sole: any) => {
  selectedSole.value = sole
  detailModalVisible.value = true
}

const closeDetailModal = () => {
  detailModalVisible.value = false
  selectedSole.value = null
}

const editSole = (sole: any) => {
  selectedSole.value = sole
  soleForm.tenDeGiay = sole.tenDeGiay
  soleForm.trangThai = sole.trangThai
  updateModalVisible.value = true
}

const closeUpdateModal = () => {
  updateModalVisible.value = false
  updateFormRef.value?.resetFields()
  selectedSole.value = null
}

const confirmUpdateSole = () => {
  updateFormRef.value
    ?.validate()
    .then(() => {
      confirmMessage.value = 'Bạn có chắc chắn muốn cập nhật đế giày này?'
      confirmAction.value = 'update'
      confirmModalVisible.value = true
    })
    .catch(() => {
      // Validation failed
    })
}

const deleteSole = (sole: any) => {
  selectedSole.value = sole
  confirmMessage.value = 'Bạn có chắc chắn muốn xóa đế giày này?'
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
  soleToToggleStatus.value = record
  showStatusConfirm.value = true
}

// Actual toggle status implementation
const performToggleStatus = async (record: any) => {
  try {
    // Set loading state for this specific record
    record.updating = true

    // Call API to update sole status
    const updateData = {
      tenDeGiay: record.tenDeGiay,
      trangThai: !record.trangThai, // Toggle status
      deleted: record.deleted,
      createAt: record.createAt,
      createBy: record.createBy,
      updateAt: new Date().toISOString().split('T')[0],
      updateBy: userStore.id,
    }

    const response = await updateDeGiay(record.id, updateData)

    if (response.success || response.status === 200) {
      // Update local data immediately for better UX
      record.trangThai = !record.trangThai

      // Update in soles array
      const index = soles.value.findIndex((s) => s.id === record.id)
      if (index !== -1) {
        soles.value[index].trangThai = record.trangThai
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
  await performToggleStatus(soleToToggleStatus.value)
  showStatusConfirm.value = false
  soleToToggleStatus.value = null
}

const cancelToggleStatus = () => {
  showStatusConfirm.value = false
  soleToToggleStatus.value = null
}

const getDeGiayPage = async (page) => {
  try {
    loading.value = true
    const res = await getDeGiayList(page, pagination.value.pageSize || 10)
    if (res.success) {
      soles.value = (res.data.data || []).sort((a: any, b: any) => (b.id || 0) - (a.id || 0))
      pagination.value.total = res.data.totalElements
      pagination.value.pageSize = res.data.size || 10
      pagination.value.current = res.data.number + 1
    } else {
      console.error('Failed to fetch soles:', res.message)
      soles.value = []
      pagination.value.total = 0
      pagination.value.pageSize = 10
      pagination.value.current = 1
    }
  } catch (error) {
    console.error('Failed to fetch soles:', error)
    soles.value = []
    pagination.value.total = 0
  } finally {
    loading.value = false
  }
}

const loadSolesWithUpdatedFirst = async (updatedId?: number, isNewItem: boolean = false) => {
  try {
    loading.value = true
    const res = await getDeGiayList(0, 9)
    if (res.success) {
      let solesData = res.data.data
      pagination.value.total = res.data.totalElements
      pagination.value.pageSize = res.data.size || 10
      pagination.value.current = res.data.number + 1

      // If there's an updated item and it's not a new item, move it to the front
      if (updatedId && !isNewItem) {
        const updatedIndex = solesData.findIndex((sole) => sole.id === updatedId)
        if (updatedIndex > 0) {
          const updatedItem = solesData.splice(updatedIndex, 1)[0]
          solesData = [updatedItem, ...solesData.slice(0, updatedIndex), ...solesData.slice(updatedIndex)]
        }
      }

      soles.value = solesData
    } else {
      console.error('Failed to fetch soles:', res.message)
      soles.value = []
      pagination.value.total = 0
      pagination.value.pageSize = 10
      pagination.value.current = 1
    }
  } catch (error) {
    console.error('Failed to fetch soles:', error)
    soles.value = []
    pagination.value.total = 0
  } finally {
    loading.value = false
  }
}

const executeConfirmedAction = async () => {
  try {
    if (confirmAction.value === 'add') {
      // TODO: Implement add API call
      const data = {
        tenDeGiay: soleForm.tenDeGiay,
        trangThai: true,
        deleted: false,
        createAt: new Date().toISOString().split('T')[0],
        createBy: userStore.id,
      }
      const res = await createDeGiay(data)
      closeAddModal()
      // Load data with new item first (always load from page 0 for new items)
      loadSolesWithUpdatedFirst(undefined, true)
      Message.success('Thêm đế giày thành công!')
    } else if (confirmAction.value === 'update') {
      // TODO: Implement update API call
      if (!selectedSole.value) {
        console.error('No sole selected for update')
        return
      }

      const soleId = selectedSole.value.id
      const data = {
        tenDeGiay: soleForm.tenDeGiay,
        trangThai: soleForm.trangThai,
        deleted: selectedSole.value.deleted,
        createAt: selectedSole.value.createAt,
        createBy: selectedSole.value.createBy,
        updateAt: new Date().toISOString().split('T')[0],
        updateBy: userStore.id,
      }
      await updateDeGiay(soleId, data)
      closeUpdateModal()
      // Load data with updated item first
      loadSolesWithUpdatedFirst(soleId, false)
      Message.success('Cập nhật đế giày thành công!')
    } else if (confirmAction.value === 'delete') {
      // TODO: Implement delete API call
      if (!selectedSole.value) {
        console.error('No sole selected for delete')
        return
      }

      await deleteDeGiay(selectedSole.value.id)
      // Refresh data
      loadSolesWithUpdatedFirst()
      Message.success('Xóa đế giày thành công!')
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

const exportSoles = () => {
  try {
    if (!soles.value || soles.value.length === 0) {
      Message.warning('Không có dữ liệu để xuất Excel')
      return
    }

    exportToExcel(soles.value, EXPORT_HEADERS.DE_GIAY, 'de-giay')
  } catch (error) {
    console.error('Lỗi khi xuất Excel:', error)
    Message.error('Có lỗi xảy ra khi xuất Excel')
  }
}

onMounted(() => {
  getDeGiayPage(0)
})
</script>

<style scoped>
.product-attribute-sole-page {
  padding: 16px 20px;
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

/* Custom required field styling */
.required-field::after {
  content: ' *' !important;
  color: #f53f3f !important;
  font-weight: bold !important;
}
</style>
