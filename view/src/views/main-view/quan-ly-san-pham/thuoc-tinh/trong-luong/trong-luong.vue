<template>
  <div class="product-attribute-weight-page">

    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="filters" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="filters.search" placeholder="Tìm theo mã hoặc tên trọng lượng..." allow-clear @input="searchWeights" />
            </a-form-item>
          </a-col>

          <a-col :span="8">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="filters.status" type="button" @change="searchWeights">
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
          <a-button @click="exportWeights">
            <template #icon>
              <icon-download />
            </template>
            Xuất Excel
          </a-button>
          <a-button type="primary" @click="showCreateModal">
            <template #icon>
              <icon-plus />
            </template>
            Thêm Trọng lượng
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!-- Weights Table -->
    <a-card title="Danh sách trọng lượng" class="table-card">
      <a-table
        :columns="columns"
        :data="weights"
        :pagination="pagination"
        :loading="loading"
        :scroll="{ x: 1000 }"
        @page-change="getTrongLuongPage($event - 1)"
        @page-size-change="
          (size) => {
            pagination.pageSize = size
            getTrongLuongPage(0)
          }
        "
        row-key="id"
      >
        <template #stt="{ record, rowIndex }">
          <span>{{ rowIndex + 1 }}</span>
        </template>

        <template #code="{ record }">
          <span>{{ record.maTrongLuong || 'N/A' }}</span>
        </template>

        <template #name="{ record }">
          <span>{{ record.tenTrongLuong }}</span>
        </template>

        <template #status="{ record }">
          <a-tag :color="record.trangThai ? 'green' : 'red'">
            {{ record.trangThai ? 'Hoạt động' : 'Không hoạt động' }}
          </a-tag>
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
            <a-button type="text" @click="viewWeight(record)">
              <template #icon>
                <icon-eye />
              </template>
            </a-button>
            <a-button type="text" @click="editWeight(record)">
              <template #icon>
                <icon-edit />
              </template>
            </a-button>
            <!-- <a-button type="text" danger @click="deleteWeight(record)">
              <template #icon>
                <icon-delete />
              </template>
            </a-button> -->
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- Add Weight Modal -->
    <a-modal
      v-model:visible="addModalVisible"
      title="Thêm trọng lượng"
      width="600px"
      :mask-closable="false"
      :closable="true"
      @cancel="closeAddModal"
      @ok="confirmAddWeight"
    >
      <a-form :model="weightForm" :rules="formRules" layout="vertical" ref="addFormRef">
        <a-form-item>
          <template #label>
            <span class="required-field">Tên trọng lượng</span>
          </template>
          <a-input v-model="weightForm.tenTrongLuong" placeholder="Nhập tên trọng lượng" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Detail Weight Modal -->
    <a-modal
      v-model:visible="detailModalVisible"
      title="Chi tiết trọng lượng"
      width="600px"
      :mask-closable="false"
      :closable="true"
      @cancel="closeDetailModal"
      @ok="closeDetailModal"
      ok-text="Đóng"
      :cancel-button-props="{ style: { display: 'none' } }"
    >
      <a-descriptions :column="1" size="small">
        <a-descriptions-item label="Mã trọng lượng">{{ selectedWeight?.maTrongLuong }}</a-descriptions-item>
        <a-descriptions-item label="Tên trọng lượng">{{ selectedWeight?.tenTrongLuong }}</a-descriptions-item>
        <a-descriptions-item label="Trạng thái">
          <a-tag :color="selectedWeight?.trangThai ? 'green' : 'red'">
            {{ selectedWeight?.trangThai ? 'Hoạt động' : 'Không hoạt động' }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- Update Weight Modal -->
    <a-modal
      v-model:visible="updateModalVisible"
      title="Cập nhật trọng lượng"
      width="600px"
      :mask-closable="false"
      :closable="true"
      @cancel="closeUpdateModal"
      @ok="confirmUpdateWeight"
    >
      <a-form :model="weightForm" :rules="formRules" layout="vertical" ref="updateFormRef">
        <a-form-item>
          <template #label>
            <span class="required-field">Tên trọng lượng</span>
          </template>
          <a-input v-model="weightForm.tenTrongLuong" placeholder="Nhập tên trọng lượng" />
        </a-form-item>
        <a-form-item>
          <template #label>
            <span class="required-field">Trạng thái</span>
          </template>
          <a-radio-group v-model="weightForm.trangThai" type="button">
            <a-radio :value="true">Hoạt động</a-radio>
            <a-radio :value="false">Không hoạt động</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Confirmation Modal -->
    <a-modal
      v-model:visible="confirmModalVisible"
      title="Xác nhận"
      width="400px"
      :mask-closable="false"
      :closable="true"
      @cancel="cancelConfirm"
      @ok="executeConfirmedAction"
    >
      <p>{{ confirmMessage }}</p>
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
        <div v-if="weightToToggleStatus">
          <div>Bạn có chắc chắn muốn {{ weightToToggleStatus.trangThai ? 'tạm ngưng' : 'kích hoạt' }} trọng lượng này?</div>
          <div>
            Tên trọng lượng:
            <strong>{{ weightToToggleStatus.tenTrongLuong }}</strong>
          </div>
          <div>
            Trạng thái hiện tại:
            <strong>{{ weightToToggleStatus.trangThai ? 'Hoạt động' : 'Không hoạt động' }}</strong>
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
import { createTrongLuong, getTrongLuongList, updateTrongLuong, deleteTrongLuong } from '../../../../../api/san-pham/thuoc-tinh/trong-luong'

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
const weights = ref([])

// Modal states
const addModalVisible = ref(false)
const detailModalVisible = ref(false)
const updateModalVisible = ref(false)
const confirmModalVisible = ref(false)

// Form refs
const addFormRef = ref()
const updateFormRef = ref()

// Selected weight for detail/update
const selectedWeight = ref(null)

// Weight form data
const weightForm = reactive({
  tenTrongLuong: '',
  trangThai: true,
})

// Form validation rules
const formRules = {
  tenTrongLuong: [
    { required: true, message: 'Vui lòng nhập tên trọng lượng' },
    { min: 2, message: 'Tên trọng lượng phải có ít nhất 2 ký tự' },
  ],
  trangThai: [{ required: true, message: 'Vui lòng chọn trạng thái' }],
}

// Confirmation modal
const confirmMessage = ref('')
const confirmAction = ref(null)

// Status toggle modal
const showStatusConfirm = ref(false)
const weightToToggleStatus = ref(null)

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
    title: 'Mã trọng lượng',
    dataIndex: 'code',
    slotName: 'code',
    width: 100,
  },
  {
    title: 'Tên trọng lượng',
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

const searchWeights = () => {
  // TODO: Implement search functionality
}

const showCreateModal = () => {
  weightForm.tenTrongLuong = ''
  weightForm.trangThai = true
  addModalVisible.value = true
}

const closeAddModal = () => {
  addModalVisible.value = false
  addFormRef.value?.resetFields()
}

const confirmAddWeight = () => {
  addFormRef.value
    ?.validate()
    .then(() => {
      confirmMessage.value = 'Bạn có chắc chắn muốn thêm trọng lượng này?'
      confirmAction.value = 'add'
      confirmModalVisible.value = true
    })
    .catch(() => {
      // Validation failed
    })
}

const viewWeight = (weight: any) => {
  selectedWeight.value = weight
  detailModalVisible.value = true
}

const closeDetailModal = () => {
  detailModalVisible.value = false
  selectedWeight.value = null
}

const editWeight = (weight: any) => {
  selectedWeight.value = weight
  weightForm.tenTrongLuong = weight.tenTrongLuong
  weightForm.trangThai = weight.trangThai
  updateModalVisible.value = true
}

const closeUpdateModal = () => {
  updateModalVisible.value = false
  updateFormRef.value?.resetFields()
  selectedWeight.value = null
}

const confirmUpdateWeight = () => {
  updateFormRef.value
    ?.validate()
    .then(() => {
      confirmMessage.value = 'Bạn có chắc chắn muốn cập nhật trọng lượng này?'
      confirmAction.value = 'update'
      confirmModalVisible.value = true
    })
    .catch(() => {
      // Validation failed
    })
}

const deleteWeight = (weight: any) => {
  selectedWeight.value = weight
  confirmMessage.value = 'Bạn có chắc chắn muốn xóa trọng lượng này?'
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
  weightToToggleStatus.value = record
  showStatusConfirm.value = true
}

// Actual toggle status implementation
const performToggleStatus = async (record: any) => {
  try {
    // Set loading state for this specific record
    record.updating = true

    // Call API to update weight status
    const updateData = {
      tenTrongLuong: record.tenTrongLuong,
      trangThai: !record.trangThai, // Toggle status
      deleted: record.deleted,
      createAt: record.createAt,
      createBy: record.createBy,
      updateAt: new Date().toISOString().split('T')[0],
      updateBy: userStore.id,
    }

    const response = await updateTrongLuong(record.id, updateData)

    if (response.success || response.status === 200) {
      // Update local data immediately for better UX
      record.trangThai = !record.trangThai

      // Update in weights array
      const index = weights.value.findIndex((w) => w.id === record.id)
      if (index !== -1) {
        weights.value[index].trangThai = record.trangThai
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
  await performToggleStatus(weightToToggleStatus.value)
  showStatusConfirm.value = false
  weightToToggleStatus.value = null
}

const cancelToggleStatus = () => {
  showStatusConfirm.value = false
  weightToToggleStatus.value = null
}

const getTrongLuongPage = async (page) => {
  try {
    loading.value = true
    const res = await getTrongLuongList(page, pagination.value.pageSize)
    if (res.success) {
      weights.value = res.data.data
      pagination.value.total = res.data.totalElements
      pagination.value.pageSize = res.data.size
      pagination.value.current = res.data.number + 1
    } else {
      console.error('Failed to fetch weights:', res.message)
      weights.value = []
      pagination.value.total = 0
      pagination.value.pageSize = 10
      pagination.value.current = 1
    }
  } catch (error) {
    console.error('Failed to fetch weights:', error)
    weights.value = []
    pagination.value.total = 0
  } finally {
    loading.value = false
  }
}

const loadWeightsWithUpdatedFirst = async (updatedId?: number, isNewItem: boolean = false) => {
  try {
    const res = await getTrongLuongList(0, 9)
    if (res.success) {
      let weightsData = res.data.data
      pagination.value.total = res.data.totalElements
      pagination.value.pageSize = res.data.size
      pagination.value.current = res.data.number + 1

      // If there's an updated item and it's not a new item, move it to the front
      if (updatedId && !isNewItem) {
        const updatedIndex = weightsData.findIndex((weight) => weight.id === updatedId)
        if (updatedIndex > 0) {
          const updatedItem = weightsData.splice(updatedIndex, 1)[0]
          weightsData = [updatedItem, ...weightsData.slice(0, updatedIndex), ...weightsData.slice(updatedIndex)]
        }
      }

      weights.value = weightsData
    } else {
      console.error('Failed to fetch weights:', res.message)
      weights.value = []
      pagination.value.total = 0
      pagination.value.pageSize = 10
      pagination.value.current = 1
    }
  } catch (error) {
    console.error('Failed to fetch weights:', error)
  }
}

const executeConfirmedAction = async () => {
  try {
    if (confirmAction.value === 'add') {
      // TODO: Implement add API call
      const data = {
        tenTrongLuong: weightForm.tenTrongLuong,
        trangThai: true,
        deleted: false,
        createAt: new Date().toISOString().split('T')[0],
        createBy: userStore.id,
      }
      await createTrongLuong(data)
      closeAddModal()
      // Load data with new item first (always load from page 0 for new items)
      loadWeightsWithUpdatedFirst(undefined, true)
      Message.success('Thêm trọng lượng thành công!')
    } else if (confirmAction.value === 'update') {
      // TODO: Implement update API call
      if (!selectedWeight.value) {
        console.error('No weight selected for update')
        return
      }

      const weightId = selectedWeight.value.id
      const data = {
        tenTrongLuong: weightForm.tenTrongLuong,
        trangThai: weightForm.trangThai,
        deleted: selectedWeight.value.deleted,
        createAt: selectedWeight.value.createAt,
        createBy: selectedWeight.value.createBy,
        updateAt: new Date().toISOString().split('T')[0],
        updateBy: userStore.id,
      }
      await updateTrongLuong(weightId, data)
      closeUpdateModal()
      // Load data with updated item first
      loadWeightsWithUpdatedFirst(weightId, false)
      Message.success('Cập nhật trọng lượng thành công!')
    } else if (confirmAction.value === 'delete') {
      // TODO: Implement delete API call
      if (!selectedWeight.value) {
        console.error('No weight selected for delete')
        return
      }

      await deleteTrongLuong(selectedWeight.value.id)
      // Refresh data
      loadWeightsWithUpdatedFirst()
      Message.success('Xóa trọng lượng thành công!')
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

const exportWeights = () => {
  try {
    if (!weights.value || weights.value.length === 0) {
      Message.warning('Không có dữ liệu để xuất Excel')
      return
    }

    exportToExcel(weights.value, EXPORT_HEADERS.TRONG_LUONG, 'trong-luong')
  } catch (error) {
    console.error('Lỗi khi xuất Excel:', error)
    Message.error('Có lỗi xảy ra khi xuất Excel')
  }
}

onMounted(() => {
  getTrongLuongPage(0)
})
</script>

<style scoped>
.product-attribute-weight-page {
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
