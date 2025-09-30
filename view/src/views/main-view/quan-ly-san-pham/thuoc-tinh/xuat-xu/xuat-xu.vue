<template>
  <div class="product-attribute-origin-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="filters" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="filters.search" placeholder="Tìm theo mã hoặc tên xuất xứ..." allow-clear @input="searchOrigins" />
            </a-form-item>
          </a-col>

          <a-col :span="8">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="filters.status" type="button" @change="searchOrigins">
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
          <a-button @click="exportOrigins">
            <template #icon>
              <icon-download />
            </template>
            Xuất Excel
          </a-button>
          <a-button type="primary" @click="showCreateModal">
            <template #icon>
              <icon-plus />
            </template>
            Thêm Xuất xứ
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!-- Origins Table -->
    <a-card title="Danh sách xuất xứ" class="table-card">
      <a-table :columns="columns" :data="origins" :pagination="pagination" :loading="loading" :scroll="{ x: 1000 }">
        <template #stt="{ rowIndex }">
          <div>{{ rowIndex + 1 }}</div>
        </template>

        <template #code="{ record }">
          <span>{{ record.maXuatXu }}</span>
        </template>

        <template #name="{ record }">
          <span>{{ record.tenXuatXu }}</span>
        </template>

        <template #status="{ record }">
          <a-tag :color="record.trangThai ? 'green' : 'red'">
            {{ record.trangThai ? 'Hoạt động' : 'Không hoạt động' }}
          </a-tag>
        </template>

        <template #action="{ record }">
          <a-space>
            <a-button type="text" @click="viewOrigin(record)">
              <template #icon>
                <icon-eye />
              </template>
            </a-button>
            <a-button type="text" @click="editOrigin(record)">
              <template #icon>
                <icon-edit />
              </template>
            </a-button>
            <a-button type="text" danger @click="deleteOrigin(record)">
              <template #icon>
                <icon-delete />
              </template>
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- Add Origin Modal -->
    <a-modal
      v-model:visible="addModalVisible"
      title="Thêm xuất xứ"
      width="600px"
      :mask-closable="false"
      :closable="true"
      @cancel="closeAddModal"
      @ok="confirmAddOrigin"
    >
      <a-form :model="originForm" :rules="formRules" layout="vertical" ref="addFormRef">
        <a-form-item label="Tên xuất xứ" field="tenXuatXu" required>
          <a-input v-model="originForm.tenXuatXu" placeholder="Nhập tên xuất xứ" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Detail Origin Modal -->
    <a-modal
      v-model:visible="detailModalVisible"
      title="Chi tiết xuất xứ"
      width="600px"
      :mask-closable="false"
      :closable="true"
      @cancel="closeDetailModal"
      @ok="closeDetailModal"
      ok-text="Đóng"
      :cancel-button-props="{ style: { display: 'none' } }"
    >
      <a-descriptions :column="1" size="small">
        <a-descriptions-item label="Mã xuất xứ">{{ selectedOrigin?.maXuatXu }}</a-descriptions-item>
        <a-descriptions-item label="Tên xuất xứ">{{ selectedOrigin?.tenXuatXu }}</a-descriptions-item>
        <a-descriptions-item label="Trạng thái">
          <a-tag :color="selectedOrigin?.trangThai ? 'green' : 'red'">
            {{ selectedOrigin?.trangThai ? 'Hoạt động' : 'Không hoạt động' }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- Update Origin Modal -->
    <a-modal
      v-model:visible="updateModalVisible"
      title="Cập nhật xuất xứ"
      width="600px"
      :mask-closable="false"
      :closable="true"
      @cancel="closeUpdateModal"
      @ok="confirmUpdateOrigin"
    >
      <a-form :model="originForm" :rules="formRules" layout="vertical" ref="updateFormRef">
        <a-form-item label="Tên xuất xứ" field="tenXuatXu" required>
          <a-input v-model="originForm.tenXuatXu" placeholder="Nhập tên xuất xứ" />
        </a-form-item>
        <a-form-item label="Trạng thái" field="trangThai" required>
          <a-radio-group v-model="originForm.trangThai" type="button">
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
import {
  IconPlus,
  IconStar,
  IconGift,
  IconLocation,
  IconDriveFile,
  IconSearch,
  IconDownload,
  IconEdit,
  IconEye,
  IconDelete,
  IconRefresh,
} from '@arco-design/web-vue/es/icon'
import { useUserStore } from '@/store'
import { createXuatXu, getXuatXuList, updateXuatXu, deleteXuatXu } from '../../../../../api/san-pham/thuoc-tinh/xuat-xu'

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
const origins = ref([])

// Modal states
const addModalVisible = ref(false)
const detailModalVisible = ref(false)
const updateModalVisible = ref(false)
const confirmModalVisible = ref(false)

// Form refs
const addFormRef = ref()
const updateFormRef = ref()

// Selected origin for detail/update
const selectedOrigin = ref(null)

// Origin form data
const originForm = reactive({
  tenXuatXu: '',
  trangThai: true,
})

// Form validation rules
const formRules = {
  tenXuatXu: [
    { required: true, message: 'Vui lòng nhập tên xuất xứ' },
    { min: 2, message: 'Tên xuất xứ phải có ít nhất 2 ký tự' },
  ],
  trangThai: [{ required: true, message: 'Vui lòng chọn trạng thái' }],
}

// Confirmation modal
const confirmMessage = ref('')
const confirmAction = ref(null)

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
    title: 'Mã xuất xứ',
    dataIndex: 'code',
    slotName: 'code',
    width: 100,
  },
  {
    title: 'Tên xuất xứ',
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

const searchOrigins = () => {
  // TODO: Implement search functionality
  console.log('Searching origins with filters:', filters.value)
}

const showCreateModal = () => {
  originForm.tenXuatXu = ''
  originForm.trangThai = true
  addModalVisible.value = true
}

const closeAddModal = () => {
  addModalVisible.value = false
  addFormRef.value?.resetFields()
}

const confirmAddOrigin = () => {
  addFormRef.value
    ?.validate()
    .then(() => {
      confirmMessage.value = 'Bạn có chắc chắn muốn thêm xuất xứ này?'
      confirmAction.value = 'add'
      confirmModalVisible.value = true
    })
    .catch(() => {
      // Validation failed
    })
}

const viewOrigin = (origin: any) => {
  selectedOrigin.value = origin
  detailModalVisible.value = true
}

const closeDetailModal = () => {
  detailModalVisible.value = false
  selectedOrigin.value = null
}

const editOrigin = (origin: any) => {
  selectedOrigin.value = origin
  originForm.tenXuatXu = origin.tenXuatXu
  originForm.trangThai = origin.trangThai
  updateModalVisible.value = true
}

const closeUpdateModal = () => {
  updateModalVisible.value = false
  updateFormRef.value?.resetFields()
  selectedOrigin.value = null
}

const confirmUpdateOrigin = () => {
  updateFormRef.value
    ?.validate()
    .then(() => {
      confirmMessage.value = 'Bạn có chắc chắn muốn cập nhật xuất xứ này?'
      confirmAction.value = 'update'
      confirmModalVisible.value = true
    })
    .catch(() => {
      // Validation failed
    })
}

const deleteOrigin = (origin: any) => {
  selectedOrigin.value = origin
  confirmMessage.value = 'Bạn có chắc chắn muốn xóa xuất xứ này?'
  confirmAction.value = 'delete'
  confirmModalVisible.value = true
}

const cancelConfirm = () => {
  confirmModalVisible.value = false
  confirmMessage.value = ''
  confirmAction.value = null
}

const getXuatXuPage = async (page) => {
  try {
    const res = await getXuatXuList(page)
    if (res.success) {
      origins.value = res.data.data
      console.log('Fetched origins:', origins.value)
      pagination.value.total = res.data.totalElements
      pagination.value.pageSize = res.data.pageSize
      pagination.value.current = res.data.currentPage + 1
    } else {
      console.error('Failed to fetch origins:', res.message)
      origins.value = []
      pagination.value.total = 0
      pagination.value.pageSize = 10
      pagination.value.current = 1
    }
  } catch (error) {
    console.error('Failed to fetch origins:', error)
  }
}

const executeConfirmedAction = async () => {
  try {
    if (confirmAction.value === 'add') {
      // TODO: Implement add API call
      const data = {
        tenXuatXu: originForm.tenXuatXu,
        trangThai: true,
        deleted: false,
        createAt: new Date().toISOString().split('T')[0],
        createBy: userStore.id,
      }
      console.log('Adding origin:', data)
      await createXuatXu(data)
      closeAddModal()
      // Refresh data
      getXuatXuPage(0)
    } else if (confirmAction.value === 'update') {
      // TODO: Implement update API call
      const data = {
        tenXuatXu: originForm.tenXuatXu,
        trangThai: originForm.trangThai,
        deleted: selectedOrigin.value.deleted,
        createAt: selectedOrigin.value.createAt,
        createBy: selectedOrigin.value.createBy,
        updateAt: new Date().toISOString().split('T')[0],
        updateBy: userStore.id,
      }
      console.log('Updating origin:', selectedOrigin.value.id, data)
      await updateXuatXu(selectedOrigin.value.id, data)
      closeUpdateModal()
      // Refresh data
      getXuatXuPage(0)
    } else if (confirmAction.value === 'delete') {
      // TODO: Implement delete API call
      console.log('Deleting origin:', selectedOrigin.value.id)
      await deleteXuatXu(selectedOrigin.value.id)
      // Refresh data
      getXuatXuPage(0)
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

const exportOrigins = () => {
  // TODO: Implement Excel export functionality
  console.log('Exporting origins to Excel...')
}

onMounted(() => {
  getXuatXuPage(0)
})
</script>

<style scoped>
.product-attribute-origin-page {
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
  margin: 0;
}

.product-icon {
  color: #52c41a;
}

.flag-icon {
  color: #faad14;
}

.truck-icon {
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

.flag-cell {
  display: flex;
  justify-content: center;
  font-size: 24px;
}
</style>
