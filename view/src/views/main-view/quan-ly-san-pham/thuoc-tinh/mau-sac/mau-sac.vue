<template>
  <div class="product-attribute-color-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="filters" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="filters.search" placeholder="Tìm theo mã hoặc tên màu sắc..." allow-clear @input="searchColors" />
            </a-form-item>
          </a-col>

          <a-col :span="8">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="filters.status" type="button" @change="searchColors">
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
          <a-button @click="exportColors">
            <template #icon>
              <icon-download />
            </template>
            Xuất Excel
          </a-button>
          <a-button type="primary" @click="showCreateModal">
            <template #icon>
              <icon-plus />
            </template>
            Thêm Màu sắc
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!-- Colors Table -->
    <a-card title="Danh sách màu sắc" class="table-card">
      <a-table :columns="columns" :data="colors" :pagination="pagination" :loading="loading" :scroll="{ x: 1000 }">
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

        <template #color_preview="{ record }">
          <div class="color-display">
            <div class="color-circle-small" :style="{ backgroundColor: record.hex_code }"></div>
            <span class="color-hex">{{ record.hex_code.toUpperCase() }}</span>
          </div>
        </template>

        <template #status="{ record }">
          <a-tag :color="record.is_active ? 'green' : 'red'">
            {{ record.is_active ? 'Hoạt động' : 'Không hoạt động' }}
          </a-tag>
        </template>

        <template #action="{ record }">
          <a-space>
            <a-button type="text" @click="viewColor(record)">
              <template #icon>
                <icon-eye />
              </template>
            </a-button>
            <a-button type="text" @click="editColor(record)">
              <template #icon>
                <icon-edit />
              </template>
            </a-button>
            <a-button type="text" danger @click="deleteColor(record)">
              <template #icon>
                <icon-delete />
              </template>
            </a-button>
          </a-space>
        </template>

        <template #checkbox-title>
          <a-checkbox
            :checked="selectedCount === colors.length && colors.length > 0"
            :indeterminate="selectedCount > 0 && selectedCount < colors.length"
            @change="
              (checked) => {
                if (checked) {
                  selectedRowKeys.value = [...colors.map((color) => color.id)]
                  if (!editingData.value) editingData.value = {}
                  colors.forEach((color) => {
                    editingData.value[color.id] = {
                      code: color.code,
                      name: color.name,
                      status: color.is_active,
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
      :title="isEdit ? 'Chỉnh sửa màu sắc' : 'Thêm màu sắc mới'"
      width="500px"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
        <a-form-item label="Tên màu sắc" name="name">
          <a-input v-model="formData.name" placeholder="Nhập tên màu sắc" allow-clear />
        </a-form-item>

        <a-form-item label="Mã màu HEX" name="hex_code">
          <a-input v-model="formData.hex_code" placeholder="#FF0000" allow-clear>
            <template #suffix>
              <div class="color-preview-small" :style="{ backgroundColor: formData.hex_code }"></div>
            </template>
          </a-input>
        </a-form-item>

        <a-form-item label="Chọn màu từ palette">
          <div class="color-picker">
            <div
              v-for="presetColor in presetColors"
              :key="presetColor"
              class="preset-color"
              :style="{ backgroundColor: presetColor }"
              @click="selectPresetColor(presetColor)"
            ></div>
          </div>
        </a-form-item>

        <a-form-item label="Mô tả (tùy chọn)">
          <a-textarea v-model="formData.description" placeholder="Mô tả về màu sắc này" :rows="3" allow-clear />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { IconPlus, IconPalette, IconGift, IconEye, IconDownload, IconEdit, IconDelete, IconRefresh } from '@arco-design/web-vue/es/icon'

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
    title: 'Mã màu sắc',
    dataIndex: 'code',
    width: 100,
    slotName: 'code',
  },
  {
    title: 'Tên màu sắc',
    dataIndex: 'name',
    width: 120,
    slotName: 'name',
  },
  {
    title: 'Màu sắc',
    dataIndex: 'color_preview',
    slotName: 'color_preview',
    width: 120,
    align: 'center',
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
  name: '',
  hex_code: '#000000',
  description: '',
})

// Form validation rules
const formRules = {
  name: [
    { required: true, message: 'Vui lòng nhập tên màu sắc' },
    { min: 2, max: 50, message: 'Tên màu sắc phải từ 2-50 ký tự' },
  ],
  hex_code: [
    { required: true, message: 'Vui lòng nhập mã màu HEX' },
    { pattern: /^#[0-9A-F]{6}$/i, message: 'Mã màu HEX không hợp lệ (VD: #FF0000)' },
  ],
}

// Preset colors for picker
const presetColors = ref([
  '#FF0000',
  '#00FF00',
  '#0000FF',
  '#FFFF00',
  '#FF00FF',
  '#00FFFF',
  '#FFA500',
  '#800080',
  '#FFC0CB',
  '#A52A2A',
  '#808080',
  '#000000',
  '#FFFFFF',
  '#C0C0C0',
  '#800000',
  '#808000',
  '#008000',
  '#008080',
  '#000080',
  '#800080',
])

// Mock data

const colors = ref([
  {
    id: 1,
    code: 'MS001',
    name: 'Đỏ',
    hex_code: '#FF0000',
    usage_count: 12,
    description: 'Màu đỏ tươi',
    is_active: true,
  },
  {
    id: 2,
    code: 'MS002',
    name: 'Xanh dương',
    hex_code: '#1890FF',
    usage_count: 15,
    description: 'Màu xanh dương nhạt',
    is_active: true,
  },
  {
    id: 3,
    code: 'MS003',
    name: 'Đen',
    hex_code: '#000000',
    usage_count: 8,
    description: 'Màu đen',
    is_active: true,
  },
  {
    id: 4,
    code: 'MS004',
    name: 'Trắng',
    hex_code: '#FFFFFF',
    usage_count: 20,
    description: 'Màu trắng',
    is_active: true,
  },
  {
    id: 5,
    code: 'MS005',
    name: 'Xanh lá',
    hex_code: '#00FF00',
    usage_count: 5,
    description: 'Màu xanh lá tươi',
    is_active: false,
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

const searchColors = () => {
  // TODO: Implement search functionality
  // console.log('Searching colors with filters:', filters.value)
}

const viewColor = (color: any) => {
  // TODO: Implement view color details functionality
  // console.log('View color details:', color)
}

const editColor = (color: any) => {
  isEdit.value = true
  formData.name = color.name
  formData.hex_code = color.hex_code
  formData.description = color.description || ''
  modalVisible.value = true
}

const deleteColor = (color: any) => {
  // TODO: Implement delete color functionality
  // console.log('Delete color:', color)
}

const exportColors = () => {
  // TODO: Implement Excel export functionality
  // console.log('Exporting colors to Excel...')
}

const showCreateModal = () => {
  isEdit.value = false
  formData.name = ''
  formData.hex_code = '#000000'
  formData.description = ''
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

const selectPresetColor = (color: string) => {
  formData.hex_code = color
}

onMounted(() => {
  // TODO: Load initial data
  // console.log('Colors page mounted')
})
</script>

<style scoped>
.product-attribute-color-page {
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

.color-display {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-circle-small {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 1px solid #d9d9d9;
}

.color-hex {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  color: #86909c;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.header-left {
  flex: 1;
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

.header-right {
  display: flex;
  gap: 16px;
}

.active-icon {
  color: #52c41a;
}

.colors-card {
}

.colors-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.color-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #f0f0f0;

  background: #fff;
  cursor: pointer;
}

.color-item .color-preview {
  margin-right: 16px;
}

.color-circle {
  width: 40px;
  height: 40px;

  border: 2px solid #e8e8e8;
}

.color-info {
  flex: 1;
}

.color-name {
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 4px;
}

.color-hex {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  color: #86909c;
  margin-bottom: 8px;
}

.color-usage {
  font-size: 12px;
}

.color-actions {
  margin-left: 16px;
}

.color-picker {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 8px;
  margin-top: 8px;
}

.preset-color {
  width: 32px;
  height: 32px;

  border: 2px solid #e8e8e8;
  cursor: pointer;
}

.preset-color .color-preview-small {
  width: 20px;
  height: 20px;

  border: 1px solid #d9d9d9;
}

.danger-item {
  color: #ff4d4f;
}

/* Responsive */
@media (max-width: 768px) {
  .product-attribute-color-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .colors-grid {
    grid-template-columns: 1fr;
  }

  .color-item {
    flex-direction: column;
    text-align: center;
  }

  .color-preview {
    margin-right: 0;
    margin-bottom: 12px;
  }

  .color-actions {
    margin-left: 0;
    margin-top: 12px;
  }

  .header-right {
    width: 100%;
    justify-content: center;
  }
}
</style>
