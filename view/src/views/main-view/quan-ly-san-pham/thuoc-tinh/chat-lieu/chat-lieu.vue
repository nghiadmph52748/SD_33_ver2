<template>
  <div class="product-attribute-material-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="filters" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="filters.search" placeholder="Tìm theo mã hoặc tên chất liệu..." allow-clear @input="searchMaterials" />
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
      <a-table :columns="columns" :data="materials" :pagination="pagination" :loading="loading" :scroll="{ x: 1000 }">
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
            <a-button type="text" danger @click="deleteMaterial(record)">
              <template #icon>
                <icon-delete />
              </template>
            </a-button>
          </a-space>
        </template>

        <template #checkbox-title>
          <a-checkbox
            :checked="selectedCount === materials.length && materials.length > 0"
            :indeterminate="selectedCount > 0 && selectedCount < materials.length"
            @change="
              (checked) => {
                if (checked) {
                  selectedRowKeys.value = [...materials.map((material) => material.id)]
                  if (!editingData.value) editingData.value = {}
                  materials.forEach((material) => {
                    editingData.value[material.id] = {
                      code: material.code,
                      name: material.name,
                      status: material.is_active,
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
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
    title: 'Mã chất liệu',
    dataIndex: 'code',
    width: 100,
    slotName: 'code',
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

// Mock data

const materials = ref([
  {
    id: 1,
    code: 'CL001',
    name: 'Da thật',
    type: 'natural',
    eco_friendly: true,
    products_count: 25,
    is_active: true,
    created_at: '2024-01-15',
    description: 'Chất liệu da thật cao cấp',
  },
  {
    id: 2,
    code: 'CL002',
    name: 'Da tổng hợp',
    type: 'synthetic',
    eco_friendly: false,
    products_count: 40,
    is_active: true,
    created_at: '2024-01-16',
    description: 'Chất liệu da PU bền đẹp',
  },
  {
    id: 3,
    code: 'CL003',
    name: 'Vải canvas',
    type: 'natural',
    eco_friendly: true,
    products_count: 30,
    is_active: true,
    created_at: '2024-01-17',
    description: 'Vải canvas organic thân thiện môi trường',
  },
  {
    id: 4,
    code: 'CL004',
    name: 'Nhựa tái chế',
    type: 'recycled',
    eco_friendly: true,
    products_count: 15,
    is_active: true,
    created_at: '2024-01-18',
    description: 'Nhựa tái chế từ chai nhựa',
  },
  {
    id: 5,
    code: 'CL005',
    name: 'Vải polyester',
    type: 'synthetic',
    eco_friendly: false,
    products_count: 20,
    is_active: false,
    created_at: '2024-01-19',
    description: 'Vải tổng hợp bền màu',
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

const searchMaterials = () => {
  // TODO: Implement search functionality
  console.log('Searching materials with filters:', filters.value)
}

const viewMaterial = (material: any) => {
  // TODO: Implement view material details functionality
  console.log('View material details:', material)
}

const editMaterial = (material: any) => {
  // TODO: Implement edit material functionality
  console.log('Edit material:', material)
}

const deleteMaterial = (material: any) => {
  // TODO: Implement delete material functionality
  console.log('Delete material:', material)
}

const exportMaterials = () => {
  // TODO: Implement Excel export functionality
  console.log('Exporting materials to Excel...')
}

const showCreateModal = () => {
  // TODO: Implement create material modal
  console.log('Show create material modal')
}

onMounted(() => {
  // TODO: Load initial data
  console.log('Materials page mounted')
})
</script>

<style scoped>
.product-attribute-material-page {
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
