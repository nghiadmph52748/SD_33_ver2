<template>
  <div class="product-attribute-size-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="filters" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="filters.search" placeholder="Tìm theo mã hoặc tên kích thước..." allow-clear @input="searchSizes" />
            </a-form-item>
          </a-col>

          <a-col :span="8">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="filters.status" type="button" @change="searchSizes">
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
          <a-button @click="exportSizes">
            <template #icon>
              <icon-download />
            </template>
            Xuất Excel
          </a-button>
          <a-button type="primary" @click="showCreateModal">
            <template #icon>
              <icon-plus />
            </template>
            Thêm Kích thước
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!-- Sizes Table -->
    <a-card title="Danh sách kích thước" class="table-card">
      <a-table
        :columns="columns"
        :data="sizes"
        :pagination="pagination"
        :loading="loading"
        :scroll="{ x: 1000 }"
        @page-change="getKichThuocPage($event - 1)"
        @page-size-change="
          (size) => {
            pagination.pageSize = size
            getKichThuocPage(0)
          }
        "
        row-key="id"
      >
        <template #stt="{ rowIndex }">
          <div>{{ rowIndex + 1 }}</div>
        </template>

        <template #code="{ record }">
          <span>{{ record.maKichThuoc || 'N/A' }}</span>
        </template>

        <template #name="{ record }">
          <span>{{ record.tenKichThuoc }}</span>
        </template>

        <template #status="{ record }">
          <a-tag :color="record.trangThai ? 'green' : 'red'">
            {{ record.trangThai ? 'Hoạt động' : 'Không hoạt động' }}
          </a-tag>
        </template>

        <template #action="{ record }">
          <a-space>
            <a-button type="text" @click="viewSize(record)">
              <template #icon>
                <icon-eye />
              </template>
            </a-button>
            <a-button type="text" @click="editSize(record)">
              <template #icon>
                <icon-edit />
              </template>
            </a-button>
            <a-button type="text" danger @click="deleteSize(record)">
              <template #icon>
                <icon-delete />
              </template>
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- Add Size Modal -->
    <a-modal
      v-model:visible="addModalVisible"
      title="Thêm kích thước"
      width="600px"
      :mask-closable="false"
      :closable="true"
      @cancel="closeAddModal"
      @ok="confirmAddSize"
    >
      <a-form :model="sizeForm" :rules="formRules" layout="vertical" ref="addFormRef">
        <a-form-item label="Tên kích thước" field="tenKichThuoc" required>
          <a-input v-model="sizeForm.tenKichThuoc" placeholder="Nhập tên kích thước" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Detail Size Modal -->
    <a-modal
      v-model:visible="detailModalVisible"
      title="Chi tiết kích thước"
      width="600px"
      :mask-closable="false"
      :closable="true"
      @cancel="closeDetailModal"
      @ok="closeDetailModal"
      ok-text="Đóng"
      :cancel-button-props="{ style: { display: 'none' } }"
    >
      <a-descriptions :column="1" size="small">
        <a-descriptions-item label="Mã kích thước">{{ selectedSize?.maKichThuoc }}</a-descriptions-item>
        <a-descriptions-item label="Tên kích thước">{{ selectedSize?.tenKichThuoc }}</a-descriptions-item>
        <a-descriptions-item label="Trạng thái">
          <a-tag :color="selectedSize?.trangThai ? 'green' : 'red'">
            {{ selectedSize?.trangThai ? 'Hoạt động' : 'Không hoạt động' }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- Update Size Modal -->
    <a-modal
      v-model:visible="updateModalVisible"
      title="Cập nhật kích thước"
      width="600px"
      :mask-closable="false"
      :closable="true"
      @cancel="closeUpdateModal"
      @ok="confirmUpdateSize"
    >
      <a-form :model="sizeForm" :rules="formRules" layout="vertical" ref="updateFormRef">
        <a-form-item label="Tên kích thước" field="tenKichThuoc" required>
          <a-input v-model="sizeForm.tenKichThuoc" placeholder="Nhập tên kích thước" />
        </a-form-item>
        <a-form-item label="Trạng thái" field="trangThai" required>
          <a-radio-group v-model="sizeForm.trangThai" type="button">
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
  IconRuler,
  IconGift,
  IconArrowUp,
  IconClockCircle,
  IconSearch,
  IconDownload,
  IconEdit,
  IconEye,
  IconDelete,
  IconRefresh,
} from '@arco-design/web-vue/es/icon'
import { useUserStore } from '@/store'
import { createKichThuoc, getKichThuocList, updateKichThuoc, deleteKichThuoc } from '../../../../../api/san-pham/thuoc-tinh/kich-thuoc'

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
const sizes = ref([])

// Modal states
const addModalVisible = ref(false)
const detailModalVisible = ref(false)
const updateModalVisible = ref(false)
const confirmModalVisible = ref(false)

// Form refs
const addFormRef = ref()
const updateFormRef = ref()

// Selected size for detail/update
const selectedSize = ref(null)

// Size form data
const sizeForm = reactive({
  tenKichThuoc: '',
  trangThai: true,
})

// Form validation rules
const formRules = {
  tenKichThuoc: [
    { required: true, message: 'Vui lòng nhập tên kích thước' },
    { min: 2, message: 'Tên kích thước phải có ít nhất 2 ký tự' },
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
    title: 'Mã kích thước',
    dataIndex: 'code',
    slotName: 'code',
    width: 100,
  },
  {
    title: 'Tên kích thước',
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

const searchSizes = () => {
  // TODO: Implement search functionality
}

const showCreateModal = () => {
  sizeForm.tenKichThuoc = ''
  sizeForm.trangThai = true
  addModalVisible.value = true
}

const closeAddModal = () => {
  addModalVisible.value = false
  addFormRef.value?.resetFields()
}

const confirmAddSize = () => {
  addFormRef.value
    ?.validate()
    .then(() => {
      confirmMessage.value = 'Bạn có chắc chắn muốn thêm kích thước này?'
      confirmAction.value = 'add'
      confirmModalVisible.value = true
    })
    .catch(() => {
      // Validation failed
    })
}

const viewSize = (size: any) => {
  selectedSize.value = size
  detailModalVisible.value = true
}

const closeDetailModal = () => {
  detailModalVisible.value = false
  selectedSize.value = null
}

const editSize = (size: any) => {
  selectedSize.value = size
  sizeForm.tenKichThuoc = size.tenKichThuoc
  sizeForm.trangThai = size.trangThai
  updateModalVisible.value = true
}

const closeUpdateModal = () => {
  updateModalVisible.value = false
  updateFormRef.value?.resetFields()
  selectedSize.value = null
}

const confirmUpdateSize = () => {
  updateFormRef.value
    ?.validate()
    .then(() => {
      confirmMessage.value = 'Bạn có chắc chắn muốn cập nhật kích thước này?'
      confirmAction.value = 'update'
      confirmModalVisible.value = true
    })
    .catch(() => {
      // Validation failed
    })
}

const deleteSize = (size: any) => {
  selectedSize.value = size
  confirmMessage.value = 'Bạn có chắc chắn muốn xóa kích thước này?'
  confirmAction.value = 'delete'
  confirmModalVisible.value = true
}

const cancelConfirm = () => {
  confirmModalVisible.value = false
  confirmMessage.value = ''
  confirmAction.value = null
}

const getKichThuocPage = async (page) => {
  try {
    loading.value = true
    const res = await getKichThuocList(page)
    if (res.success) {
      sizes.value = res.data.data
      pagination.value.total = res.data.totalElements
      pagination.value.pageSize = res.data.size
      pagination.value.current = res.data.number + 1
    } else {
      console.error('Failed to fetch sizes:', res.message)
      sizes.value = []
      pagination.value.total = 0
      pagination.value.pageSize = 10
      pagination.value.current = 1
    }
  } catch (error) {
    console.error('Failed to fetch sizes:', error)
    sizes.value = []
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
        tenKichThuoc: sizeForm.tenKichThuoc,
        trangThai: true,
        deleted: false,
        createAt: new Date().toISOString().split('T')[0],
        createBy: userStore.id,
      }
      await createKichThuoc(data)
      closeAddModal()
      // Refresh data
      getKichThuocPage(0)
    } else if (confirmAction.value === 'update') {
      // TODO: Implement update API call
      const data = {
        tenKichThuoc: sizeForm.tenKichThuoc,
        trangThai: sizeForm.trangThai,
        deleted: selectedSize.value.deleted,
        createAt: selectedSize.value.createAt,
        createBy: selectedSize.value.createBy,
        updateAt: new Date().toISOString().split('T')[0],
        updateBy: userStore.id,
      }
      await updateKichThuoc(selectedSize.value.id, data)
      closeUpdateModal()
      // Refresh data
      getKichThuocPage(0)
    } else if (confirmAction.value === 'delete') {
      // TODO: Implement delete API call
      await deleteKichThuoc(selectedSize.value.id)
      // Refresh data
      getKichThuocPage(0)
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

const exportSizes = () => {
  // TODO: Implement Excel export functionality
}

onMounted(() => {
  getKichThuocPage(0)
})
</script>

<style scoped>
.product-attribute-size-page {
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
