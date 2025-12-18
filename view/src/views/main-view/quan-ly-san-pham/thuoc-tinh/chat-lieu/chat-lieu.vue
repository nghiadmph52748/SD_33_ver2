<template>
  <div class="product-attribute-material-page">
    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="filters" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="filters.search" placeholder="Tìm theo mã hoặc tên chất liệu..." allow-clear
                @input="searchMaterials" />
            </a-form-item>
          </a-col>

          <a-col :span="8">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="filters.status" type="button" @change="searchMaterials">
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
          <a-button @click="exportMaterials">
            <template #icon>
              <icon-download />
            </template>
            Xuất Excel
          </a-button>
          <a-button type="primary" @click="showCreateModal">
            <template #icon>
              <icon-plus />
            </template>
            Thêm Chất liệu
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!-- Materials Table -->
    <a-card title="Danh sách chất liệu" class="table-card">
      <a-table :columns="columns" :data="materials" :pagination="pagination" :loading="loading" :scroll="{ x: 1000 }"
        @page-change="getChatLieuPage($event - 1)" @page-size-change="
          (size) => {
            pagination.pageSize = size
            getChatLieuPage(0)
          }
        " row-key="id">
        <template #stt="{ record, rowIndex }">
          <span>{{ rowIndex + 1 }}</span>
        </template>

        <template #code="{ record }">
          <span>{{ record.maChatLieu || 'N/A' }}</span>
        </template>

        <template #name="{ record }">
          <span>{{ record.tenChatLieu }}</span>
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
            <a-button type="text" @click="viewMaterial(record)">
              <template #icon>
                <icon-eye />
              </template>
            </a-button>
            <a-button type="text" @click="editMaterial(record)">
              <template #icon>
                <icon-edit />
              </template>
            </a-button>
            <!-- <a-button type="text" danger @click="deleteMaterial(record)">
              <template #icon>
                <icon-delete />
              </template>
            </a-button> -->
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- Add Material Modal -->
    <a-modal v-model:visible="addModalVisible" title="Thêm chất liệu" width="600px" :mask-closable="false"
      :closable="true" @cancel="closeAddModal" @ok="confirmAddMaterial">
      <a-form :model="materialForm" :rules="formRules" layout="vertical" ref="addFormRef">
        <a-form-item>
          <template #label>
            <span class="required-field">Tên chất liệu</span>
          </template>
          <a-input v-model="materialForm.tenChatLieu" placeholder="Nhập tên chất liệu" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Detail Material Modal -->
    <a-modal v-model:visible="detailModalVisible" title="Chi tiết chất liệu" width="600px" :mask-closable="false"
      :closable="true" @cancel="closeDetailModal" @ok="closeDetailModal" ok-text="Đóng"
      :cancel-button-props="{ style: { display: 'none' } }">
      <a-descriptions :column="1" size="small">
        <a-descriptions-item label="Mã chất liệu">{{ selectedMaterial?.maChatLieu }}</a-descriptions-item>
        <a-descriptions-item label="Tên chất liệu">{{ selectedMaterial?.tenChatLieu }}</a-descriptions-item>
        <a-descriptions-item label="Trạng thái">
          <a-tag :color="selectedMaterial?.trangThai ? 'green' : 'red'">
            {{ selectedMaterial?.trangThai ? 'Hoạt động' : 'Không hoạt động' }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- Update Material Modal -->
    <a-modal v-model:visible="updateModalVisible" title="Cập nhật chất liệu" width="600px" :mask-closable="false"
      :closable="true" @cancel="closeUpdateModal" @ok="confirmUpdateMaterial">
      <a-form :model="materialForm" :rules="formRules" layout="vertical" ref="updateFormRef">
        <a-form-item>
          <template #label>
            <span class="required-field">Tên chất liệu</span>
          </template>
          <a-input v-model="materialForm.tenChatLieu" placeholder="Nhập tên chất liệu" />
        </a-form-item>
        <a-form-item>
          <template #label>
            <span class="required-field">Trạng thái</span>
          </template>
          <a-radio-group v-model="materialForm.trangThai" type="button">
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
        <div v-if="materialToToggleStatus">
          <div>Bạn có chắc chắn muốn {{ materialToToggleStatus.trangThai ? 'tạm ngưng' : 'kích hoạt' }} chất liệu này?
          </div>
          <div>
            Tên chất liệu:
            <strong>{{ materialToToggleStatus.tenChatLieu }}</strong>
          </div>
          <div>
            Trạng thái hiện tại:
            <strong>{{ materialToToggleStatus.trangThai ? 'Hoạt động' : 'Không hoạt động' }}</strong>
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
  IconTool,
  IconGift,
  IconCheckCircle,
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
  createChatLieu,
  getChatLieuList,
  updateChatLieu,
  deleteChatLieu,
  getChatLieuListAll,
} from '../../../../../api/san-pham/thuoc-tinh/chat-lieu'

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
const materials = ref([])

// Modal states
const addModalVisible = ref(false)
const detailModalVisible = ref(false)
const updateModalVisible = ref(false)
const confirmModalVisible = ref(false)

// Form refs
const addFormRef = ref()
const updateFormRef = ref()

// Selected material for detail/update
const selectedMaterial = ref(null)

// Material form data
const materialForm = reactive({
  tenChatLieu: '',
  trangThai: true,
})

// Form validation rules
const formRules = {
  tenChatLieu: [
    { required: true, message: 'Vui lòng nhập tên chất liệu' },
    { min: 2, message: 'Tên chất liệu phải có ít nhất 2 ký tự' },
  ],
  trangThai: [{ required: true, message: 'Vui lòng chọn trạng thái' }],
}

// Confirmation modal
const confirmMessage = ref('')
const confirmAction = ref(null)

// Status toggle modal
const showStatusConfirm = ref(false)
const materialToToggleStatus = ref(null)

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
    title: 'Mã chất liệu',
    dataIndex: 'code',
    slotName: 'code',
    width: 100,
  },
  {
    title: 'Tên chất liệu',
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

// Filtered materials computed property
const filteredMaterials = computed(() => {
  return materials.value.filter((material) => {
    // Filter by status
    if (filters.value.status && material.trangThai !== (filters.value.status === 'active')) {
      return false
    }
    // Filter by search (name or code)
    if (filters.value.search) {
      const searchTerm = filters.value.search.toLowerCase()
      const matchesName = material.tenChatLieu.toLowerCase().includes(searchTerm)
      const matchesCode = material.maChatLieu?.toLowerCase().includes(searchTerm)
      if (!matchesName && !matchesCode) {
        return false
      }
    }
    return true
  })
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

const searchMaterials = () => {
  // Search is handled by the filteredMaterials computed property
  // This function can be used for additional search logic if needed
}

const showCreateModal = () => {
  materialForm.tenChatLieu = ''
  materialForm.trangThai = true
  addModalVisible.value = true
}

const closeAddModal = () => {
  addModalVisible.value = false
  addFormRef.value?.resetFields()
}

const confirmAddMaterial = () => {
  addFormRef.value
    ?.validate()
    .then(() => {
      confirmMessage.value = 'Bạn có chắc chắn muốn thêm chất liệu này?'
      confirmAction.value = 'add'
      confirmModalVisible.value = true
    })
    .catch(() => {
      // Validation failed
    })
}

const viewMaterial = (material: any) => {
  selectedMaterial.value = material
  detailModalVisible.value = true
}

const closeDetailModal = () => {
  detailModalVisible.value = false
  selectedMaterial.value = null
}

const editMaterial = (material: any) => {
  selectedMaterial.value = material
  materialForm.tenChatLieu = material.tenChatLieu
  materialForm.trangThai = material.trangThai
  updateModalVisible.value = true
}

const closeUpdateModal = () => {
  updateModalVisible.value = false
  updateFormRef.value?.resetFields()
  selectedMaterial.value = null
}

const confirmUpdateMaterial = () => {
  updateFormRef.value
    ?.validate()
    .then(() => {
      confirmMessage.value = 'Bạn có chắc chắn muốn cập nhật chất liệu này?'
      confirmAction.value = 'update'
      confirmModalVisible.value = true
    })
    .catch(() => {
      // Validation failed
    })
}

const deleteMaterial = (material: any) => {
  selectedMaterial.value = material
  confirmMessage.value = 'Bạn có chắc chắn muốn xóa chất liệu này?'
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
  materialToToggleStatus.value = record
  showStatusConfirm.value = true
}

// Actual toggle status implementation
const performToggleStatus = async (record: any) => {
  try {
    // Set loading state for this specific record
    record.updating = true

    // Call API to update material status
    const updateData = {
      tenChatLieu: record.tenChatLieu,
      trangThai: !record.trangThai, // Toggle status
      deleted: record.deleted,
      createAt: record.createAt,
      createBy: record.createBy,
      updateAt: new Date().toISOString().split('T')[0],
      updateBy: userStore.id,
    }

    const response = await updateChatLieu(record.id, updateData)

    if (response.success || response.status === 200) {
      // Update local data immediately for better UX
      record.trangThai = !record.trangThai

      // Update in materials array
      const index = materials.value.findIndex((m) => m.id === record.id)
      if (index !== -1) {
        materials.value[index].trangThai = record.trangThai
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
  await performToggleStatus(materialToToggleStatus.value)
  showStatusConfirm.value = false
  materialToToggleStatus.value = null
}

const cancelToggleStatus = () => {
  showStatusConfirm.value = false
  materialToToggleStatus.value = null
}

const getChatLieuPage = async (page) => {
  try {
    loading.value = true
    const res = await getChatLieuList(page, pagination.value.pageSize || 10)
    if (res.success) {
      materials.value = (res.data.data || []).sort((a: any, b: any) => (b.id || 0) - (a.id || 0))
      pagination.value.total = res.data.totalElements
      pagination.value.pageSize = res.data.size
      pagination.value.current = res.data.number + 1
    } else {
      console.error('Failed to fetch materials:', res.message)
      materials.value = []
      pagination.value.total = 0
      pagination.value.pageSize = 10
      pagination.value.current = 1
    }
  } catch (error) {
    console.error('Failed to fetch materials:', error)
    materials.value = []
    pagination.value.total = 0
  } finally {
    loading.value = false
  }
}

const loadMaterialsWithUpdatedFirst = async (updatedId?: number, isNewItem: boolean = false) => {
  try {
    loading.value = true
    const res = await getChatLieuList(0, 9)
    if (res.success) {
      let materialsData = (res.data.data || []).sort((a: any, b: any) => (b.id || 0) - (a.id || 0))
      pagination.value.total = res.data.totalElements
      pagination.value.pageSize = res.data.size
      pagination.value.current = res.data.number + 1

      // If there's an updated item and it's not a new item, move it to the front
      if (updatedId && !isNewItem) {
        const updatedIndex = materialsData.findIndex((material) => material.id === updatedId)
        if (updatedIndex > 0) {
          const updatedItem = materialsData.splice(updatedIndex, 1)[0]
          materialsData = [updatedItem, ...materialsData]
        }
      }

      materials.value = materialsData
    } else {
      console.error('Failed to fetch materials:', res.message)
      materials.value = []
      pagination.value.total = 0
      pagination.value.pageSize = 10
      pagination.value.current = 1
    }
  } catch (error) {
    console.error('Failed to fetch materials:', error)
    materials.value = []
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
        tenChatLieu: materialForm.tenChatLieu,
        trangThai: true,
        deleted: false,
        createAt: new Date().toISOString().split('T')[0],
        createBy: userStore.id,
      }
      const result = await createChatLieu(data)
      closeAddModal()
      // Load data with new item first (always load from page 0 for new items)
      loadMaterialsWithUpdatedFirst(undefined, true)
      Message.success('Thêm chất liệu thành công!')
    } else if (confirmAction.value === 'update') {
      // TODO: Implement update API call
      if (!selectedMaterial.value) {
        console.error('No material selected for update')
        return
      }

      const materialId = selectedMaterial.value.id
      const data = {
        tenChatLieu: materialForm.tenChatLieu,
        trangThai: materialForm.trangThai,
        deleted: selectedMaterial.value.deleted,
        createAt: selectedMaterial.value.createAt,
        createBy: selectedMaterial.value.createBy,
        updateAt: new Date().toISOString().split('T')[0],
        updateBy: userStore.id,
      }
      await updateChatLieu(materialId, data)
      closeUpdateModal()
      // Load data with updated item first
      loadMaterialsWithUpdatedFirst(materialId, false)
      Message.success('Cập nhật chất liệu thành công!')
    } else if (confirmAction.value === 'delete') {
      // TODO: Implement delete API call
      if (!selectedMaterial.value) {
        console.error('No material selected for delete')
        return
      }

      await deleteChatLieu(selectedMaterial.value.id)
      // Refresh data
      loadMaterialsWithUpdatedFirst()
      Message.success('Xóa chất liệu thành công!')
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

const exportMaterials = () => {
  try {
    if (!materials.value || materials.value.length === 0) {
      Message.warning('Không có dữ liệu để xuất Excel')
      return
    }

    exportToExcel(materials.value, EXPORT_HEADERS.CHAT_LIEU, 'chat-lieu')
  } catch (error) {
    console.error('Lỗi khi xuất Excel:', error)
    Message.error('Có lỗi xảy ra khi xuất Excel')
  }
}

onMounted(() => {
  getChatLieuPage(0)
})
</script>

<style scoped>
.product-attribute-material-page {
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
