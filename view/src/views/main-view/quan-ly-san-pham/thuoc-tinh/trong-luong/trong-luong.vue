<template>
  <div class="product-attribute-weight-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

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
      <a-table :columns="columns" :data="weights" :pagination="pagination" :loading="loading" :scroll="{ x: 1000 }">
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
            <a-button type="text" danger @click="deleteWeight(record)">
              <template #icon>
                <icon-delete />
              </template>
            </a-button>
          </a-space>
        </template>

        <template #checkbox-title>
          <a-checkbox
            :checked="selectedCount === weights.length && weights.length > 0"
            :indeterminate="selectedCount > 0 && selectedCount < weights.length"
            @change="
              (checked) => {
                if (checked) {
                  selectedRowKeys.value = [...weights.map((weight) => weight.id)]
                  if (!editingData.value) editingData.value = {}
                  weights.forEach((weight) => {
                    editingData.value[weight.id] = {
                      code: weight.code,
                      name: weight.name,
                      status: weight.is_active,
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
      :title="isEdit ? 'Chỉnh sửa trọng lượng' : 'Thêm trọng lượng mới'"
      width="500px"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
        <a-form-item label="Mã trọng lượng" name="code">
          <a-input v-model="formData.code" placeholder="Ví dụ: TL001" allow-clear />
        </a-form-item>

        <a-form-item label="Tên trọng lượng" name="name">
          <a-input v-model="formData.name" placeholder="Nhập tên trọng lượng" allow-clear />
        </a-form-item>

        <a-form-item label="Trạng thái" name="is_active">
          <a-radio-group v-model="formData.is_active">
            <a-radio :value="true">Hoạt động</a-radio>
            <a-radio :value="false">Không hoạt động</a-radio>
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
import { IconPlus, IconSearch, IconDownload, IconEdit, IconEye, IconDelete, IconRefresh } from '@arco-design/web-vue/es/icon'

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
    title: 'Mã trọng lượng',
    dataIndex: 'code',
    width: 100,
    slotName: 'code',
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
  code: [{ required: true, message: 'Vui lòng nhập mã trọng lượng' }],
  name: [{ required: true, message: 'Vui lòng nhập tên trọng lượng' }],
}

// Mock data
const weights = ref([
  {
    id: 1,
    code: 'TL001',
    name: 'Nhẹ (< 1kg)',
    min_weight: 0.1,
    max_weight: 0.9,
    products_count: 25,
    is_active: true,
    created_at: '2024-01-15',
  },
  {
    id: 2,
    code: 'TL002',
    name: 'Trung bình (1-2kg)',
    min_weight: 1.0,
    max_weight: 2.0,
    products_count: 40,
    is_active: true,
    created_at: '2024-01-16',
  },
  {
    id: 3,
    code: 'TL003',
    name: 'Nặng (> 2kg)',
    min_weight: 2.1,
    max_weight: 5.0,
    products_count: 15,
    is_active: true,
    created_at: '2024-01-17',
  },
  {
    id: 4,
    code: 'TL004',
    name: 'Rất nhẹ (< 0.5kg)',
    min_weight: 0.1,
    max_weight: 0.4,
    products_count: 10,
    is_active: true,
    created_at: '2024-01-18',
  },
  {
    id: 5,
    code: 'TL005',
    name: 'Siêu nặng (> 5kg)',
    min_weight: 5.1,
    max_weight: 10.0,
    products_count: 5,
    is_active: false,
    created_at: '2024-01-19',
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

const searchWeights = () => {
  // TODO: Implement search functionality
}

const viewWeight = (weight: any) => {
  // TODO: Implement view weight details functionality
}

const editWeight = (weight: any) => {
  isEdit.value = true
  formData.code = weight.code
  formData.name = weight.name
  formData.is_active = weight.is_active
  modalVisible.value = true
}

const deleteWeight = (weight: any) => {
  // TODO: Implement delete weight functionality
}

const exportWeights = () => {
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
.product-attribute-weight-page {
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
