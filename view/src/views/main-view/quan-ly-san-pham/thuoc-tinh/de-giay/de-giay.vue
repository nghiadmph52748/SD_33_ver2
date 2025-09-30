<template>
  <div class="product-attribute-sole-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="filters" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="filters.search" placeholder="Tìm theo mã hoặc tên đế giày..." allow-clear @input="searchSoles" />
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
      <a-table
        :columns="columns"
        :data="soles"
        :pagination="pagination"
        :loading="loading"
        :scroll="{ x: 1000 }"
        @page-change="getDeGiayPage($event - 1)"
        @page-size-change="
          (size) => {
            pagination.pageSize = size
            getDeGiayPage(0)
          }
        "
        row-key="id"
      >
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
            <a-button type="text" danger @click="deleteSole(record)">
              <template #icon>
                <icon-delete />
              </template>
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- Add Sole Modal -->
    <a-modal
      v-model:visible="addModalVisible"
      title="Thêm đế giày"
      width="600px"
      :mask-closable="false"
      :closable="true"
      @cancel="closeAddModal"
      @ok="confirmAddSole"
    >
      <a-form :model="soleForm" :rules="formRules" layout="vertical" ref="addFormRef">
        <a-form-item label="Tên đế giày" field="tenDeGiay" required>
          <a-input v-model="soleForm.tenDeGiay" placeholder="Nhập tên đế giày" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Detail Sole Modal -->
    <a-modal
      v-model:visible="detailModalVisible"
      title="Chi tiết đế giày"
      width="600px"
      :mask-closable="false"
      :closable="true"
      @cancel="closeDetailModal"
      @ok="closeDetailModal"
      ok-text="Đóng"
      :cancel-button-props="{ style: { display: 'none' } }"
    >
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
    <a-modal
      v-model:visible="updateModalVisible"
      title="Cập nhật đế giày"
      width="600px"
      :mask-closable="false"
      :closable="true"
      @cancel="closeUpdateModal"
      @ok="confirmUpdateSole"
    >
      <a-form :model="soleForm" :rules="formRules" layout="vertical" ref="updateFormRef">
        <a-form-item label="Tên đế giày" field="tenDeGiay" required>
          <a-input v-model="soleForm.tenDeGiay" placeholder="Nhập tên đế giày" />
        </a-form-item>
        <a-form-item label="Trạng thái" field="trangThai" required>
          <a-radio-group v-model="soleForm.trangThai" type="button">
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { IconPlus, IconSearch, IconDownload, IconEdit, IconEye, IconDelete, IconRefresh } from '@arco-design/web-vue/es/icon'
import { useUserStore } from '@/store'
import { createDeGiay, getDeGiayList, updateDeGiay, deleteDeGiay } from '../../../../../api/san-pham/thuoc-tinh/de-giay'

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()
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
    align: 'center',
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

const getDeGiayPage = async (page) => {
  try {
    loading.value = true
    const res = await getDeGiayList(page)
    if (res.success) {
      soles.value = res.data.data
      pagination.value.total = res.data.totalElements
      pagination.value.pageSize = res.data.size
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
      await createDeGiay(data)
      closeAddModal()
      // Refresh data
      getDeGiayPage(0)
    } else if (confirmAction.value === 'update') {
      // TODO: Implement update API call
      const data = {
        tenDeGiay: soleForm.tenDeGiay,
        trangThai: soleForm.trangThai,
        deleted: selectedSole.value.deleted,
        createAt: selectedSole.value.createAt,
        createBy: selectedSole.value.createBy,
        updateAt: new Date().toISOString().split('T')[0],
        updateBy: userStore.id,
      }
      await updateDeGiay(selectedSole.value.id, data)
      closeUpdateModal()
      // Refresh data
      getDeGiayPage(0)
    } else if (confirmAction.value === 'delete') {
      // TODO: Implement delete API call
      await deleteDeGiay(selectedSole.value.id)
      // Refresh data
      getDeGiayPage(0)
    }
  } catch (error) {
    console.error('API call failed:', error)
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
  // TODO: Implement Excel export functionality
}

onMounted(() => {
  getDeGiayPage(0)
})
</script>

<style scoped>
.product-attribute-sole-page {
  padding: 0 20px 20px 20px;
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
</style>
