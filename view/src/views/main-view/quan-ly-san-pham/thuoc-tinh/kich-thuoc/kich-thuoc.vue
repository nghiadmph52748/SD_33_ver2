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
      <a-table :columns="columns" :data="sizes" :pagination="pagination" :loading="loading" :scroll="{ x: 1000 }">
        <template #checkbox="{ record }">
          <a-checkbox
            :checked="isRowSelected(record.id)"
            @change="
              (checked) => {
                if (checked) {
                  const currentKeys = Array.isArray(selectedRowKeys.value) ? selectedRowKeys.value : []
                  selectedRowKeys.value = [...currentKeys, record.id]
                  if (!editingData.value) editingData.value = {}
                  editingData.value[record.id] = {
                    code: record.code,
                    name: record.name,
                    status: record.is_active,
                  }
                } else {
                  const currentKeys = Array.isArray(selectedRowKeys.value) ? selectedRowKeys.value : []
                  selectedRowKeys.value = currentKeys.filter((key) => key !== record.id)
                  if (editingData.value && editingData.value[record.id]) {
                    delete editingData.value[record.id]
                  }
                }
              }
            "
          />
        </template>

        <template #code="{ record }">
          <div v-if="editingData.value && editingData.value[record.id]">
            <a-input v-model="editingData.value[record.id].code" size="mini" style="width: 80px" />
          </div>
          <span v-else>{{ record.code }}</span>
        </template>

        <template #name="{ record }">
          <div v-if="editingData.value && editingData.value[record.id]">
            <a-input v-model="editingData.value[record.id].name" size="mini" style="width: 100px" />
          </div>
          <span v-else>{{ record.name }}</span>
        </template>

        <template #status="{ record }">
          <a-tag :color="record.is_active ? 'green' : 'red'">
            {{ record.is_active ? 'Hoạt động' : 'Không hoạt động' }}
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

        <template #checkbox-title>
          <a-checkbox
            :checked="selectedCount === sizes.length && sizes.length > 0"
            :indeterminate="selectedCount > 0 && selectedCount < sizes.length"
            @change="
              (checked) => {
                if (checked) {
                  selectedRowKeys.value = [...sizes.map((size) => size.id)]
                  if (!editingData.value) editingData.value = {}
                  sizes.forEach((size) => {
                    editingData.value[size.id] = {
                      code: size.code,
                      name: size.name,
                      status: size.is_active,
                    }
                  })
                } else {
                  selectedRowKeys.value = []
                  editingData.value = {}
                }
              }
            "
          />
        </template>
      </a-table>
    </a-card>

    <!-- Create/Edit Modal -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? 'Chỉnh sửa kích thước' : 'Thêm kích thước mới'"
      width="500px"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
        <a-form-item label="Giá trị kích thước" name="value">
          <a-input v-model="formData.value" placeholder="Ví dụ: 38, 39, 40..." allow-clear />
        </a-form-item>

        <a-form-item label="Đơn vị" name="unit">
          <a-select v-model="formData.unit" placeholder="Chọn đơn vị">
            <a-option value="">Không có đơn vị</a-option>
            <a-option value="EU">EU</a-option>
            <a-option value="US">US</a-option>
            <a-option value="UK">UK</a-option>
            <a-option value="cm">cm</a-option>
            <a-option value="inch">inch</a-option>
          </a-select>
        </a-form-item>

        <a-form-item label="Loại giày" name="category">
          <a-select v-model="formData.category" placeholder="Chọn loại giày áp dụng">
            <a-option value="sneakers">Giày sneaker</a-option>
            <a-option value="boots">Giày boot</a-option>
            <a-option value="heels">Giày cao gót</a-option>
            <a-option value="sandals">Giày sandal</a-option>
            <a-option value="all">Tất cả</a-option>
          </a-select>
        </a-form-item>

        <a-form-item label="Mô tả (tùy chọn)">
          <a-textarea v-model="formData.description" placeholder="Mô tả về kích thước này" :rows="2" allow-clear />
        </a-form-item>

        <a-form-item label="Trạng thái" name="is_active">
          <a-radio-group v-model="formData.is_active">
            <a-radio :value="true">Hoạt động</a-radio>
            <a-radio :value="false">Ngừng sử dụng</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
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

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()

// Filters
const filters = ref({
  search: '',
  status: '',
  folder: '',
  sort: 'newest',
})

// Edit inline mode state
const selectedRowKeys = ref<number[]>([])
const editingData = ref<Record<number, { code: string; name: string; status: boolean }>>({})

// Computed properties for safe access
const selectedCount = computed(() => (Array.isArray(selectedRowKeys.value) ? selectedRowKeys.value.length : 0))
const isRowSelected = (id: number) => {
  const keys = Array.isArray(selectedRowKeys.value) ? selectedRowKeys.value : []
  return keys.includes(id)
}

// Table
const loading = ref(false)
const columns = [
  {
    title: '',
    dataIndex: 'checkbox',
    slotName: 'checkbox',
    width: 50,
    align: 'center',
    titleSlotName: 'checkbox-title',
  },
  {
    title: 'Mã kích thước',
    dataIndex: 'code',
    width: 100,
    slotName: 'code',
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
  total: 5,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: true,
})

// Modal and form
const modalVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

// Form data
const formData = reactive({
  code: '',
  name: '',
  is_active: true,
})

// Form validation rules
const formRules = {
  code: [{ required: true, message: 'Vui lòng nhập mã kích thước' }],
  name: [{ required: true, message: 'Vui lòng nhập tên kích thước' }],
}

// Mock data

const sizes = ref([
  {
    id: 1,
    code: 'KT001',
    name: 'Size 35',
    value: '35',
    unit: 'EU',
    category: 'heels',
    usage_count: 15,
    is_active: true,
    created_at: '2024-01-15',
    description: 'Size nhỏ cho giày cao gót',
  },
  {
    id: 2,
    code: 'KT002',
    name: 'Size 36',
    value: '36',
    unit: 'EU',
    category: 'heels',
    usage_count: 20,
    is_active: true,
    created_at: '2024-01-16',
    description: 'Size phổ biến cho giày cao gót',
  },
  {
    id: 3,
    code: 'KT003',
    name: 'Size 37',
    value: '37',
    unit: 'EU',
    category: 'sneakers',
    usage_count: 25,
    is_active: true,
    created_at: '2024-01-17',
    description: 'Size phổ biến cho giày sneaker',
  },
  {
    id: 4,
    code: 'KT004',
    name: 'Size 38',
    value: '38',
    unit: 'EU',
    category: 'sneakers',
    usage_count: 30,
    is_active: true,
    created_at: '2024-01-18',
    description: 'Size lớn cho giày sneaker',
  },
  {
    id: 5,
    code: 'KT005',
    name: 'Size 39',
    value: '39',
    unit: 'EU',
    category: 'boots',
    usage_count: 10,
    is_active: false,
    created_at: '2024-01-19',
    description: 'Size cho giày boot',
  },
])

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

const viewSize = (size: any) => {
  // TODO: Implement view size details functionality
}

const editSize = (size: any) => {
  isEdit.value = true
  formData.code = size.code
  formData.name = size.name
  formData.is_active = size.is_active
  modalVisible.value = true
}

const deleteSize = (size: any) => {
  // TODO: Implement delete size functionality
}

const exportSizes = () => {
  // TODO: Implement Excel export functionality
}

const showCreateModal = () => {
  isEdit.value = false
  formData.code = ''
  formData.name = ''
  formData.is_active = true
  modalVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    modalVisible.value = false
  } catch (error) {
    // console.error('Form validation failed:', error)
  }
}

const handleCancel = () => {
  modalVisible.value = false
}

onMounted(() => {
  // TODO: Load initial data
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
