<template>
  <div class="product-attribute-image-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="filters" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="filters.search" placeholder="Tìm theo tên ảnh..." allow-clear @input="searchImages" />
            </a-form-item>
          </a-col>

          <a-col :span="8">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="filters.status" type="button" @change="searchImages">
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
          <a-button @click="exportExcel">
            <template #icon>
              <icon-download />
            </template>
            Xuất Excel
          </a-button>
          <a-button type="primary" @click="showUploadModal">
            <template #icon>
              <icon-upload />
            </template>
            Upload ảnh
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!-- Images Grid -->
    <a-card title="Thư viện ảnh" class="images-card">
      <template #extra>
        <a-space>
          <a-button @click="toggleViewMode">
            <template #icon>
              <icon-apps v-if="viewMode === 'grid'" />
              <icon-list v-else />
            </template>
            {{ viewMode === 'grid' ? 'Danh sách' : 'Lưới' }}
          </a-button>
        </a-space>
      </template>

      <div v-if="viewMode === 'grid'" class="images-grid">
        <div v-for="image in filteredImages" :key="image.id" class="image-item" @click="previewImage(image)">
          <div class="image-container">
            <img :src="image.url" :alt="image.name" class="image-preview" />
            <div class="image-overlay">
              <a-space direction="vertical" size="small">
                <a-button type="primary" size="mini" @click.stop="editImage(image)">
                  <template #icon>
                    <icon-edit />
                  </template>
                </a-button>
                <a-button type="primary" size="mini" @click.stop="downloadImage(image)">
                  <template #icon>
                    <icon-download />
                  </template>
                </a-button>
                <a-button type="danger" size="mini" @click.stop="deleteImage(image)">
                  <template #icon>
                    <icon-delete />
                  </template>
                </a-button>
              </a-space>
            </div>
          </div>
          <div class="image-info">
            <div class="image-name">{{ image.name }}</div>
            <div class="image-status">
              <a-tag :color="image.status === 'active' ? 'green' : 'red'">
                {{ image.status === 'active' ? 'Hoạt động' : 'Không hoạt động' }}
              </a-tag>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="images-list">
        <a-table :columns="listColumns" :data="filteredImages" :pagination="pagination" :loading="loading" size="middle">
          <template #stt="{ rowIndex }">
            <div>{{ rowIndex + 1 }}</div>
          </template>

          <template #preview="{ record }">
            <img :src="record.url" :alt="record.name" class="table-image" />
          </template>

          <template #name="{ record }">
            <span>{{ record.name }}</span>
          </template>

          <template #status="{ record }">
            <a-tag :color="record.status === 'active' ? 'green' : 'red'">
              {{ record.status === 'active' ? 'Hoạt động' : 'Không hoạt động' }}
            </a-tag>
          </template>

          <template #action="{ record }">
            <a-space>
              <a-button type="text" danger @click="deleteImage(record)">
                <template #icon>
                  <icon-delete />
                </template>
              </a-button>
            </a-space>
          </template>
        </a-table>
      </div>
    </a-card>

    <!-- Image Preview Modal -->
    <a-modal v-model:open="previewVisible" :title="selectedImage?.name" width="800px" :footer="false">
      <div v-if="selectedImage" class="preview-content">
        <img :src="selectedImage.url" :alt="selectedImage.name" class="preview-image" />
        <div class="preview-info">
          <a-descriptions :column="2" size="small">
            <a-descriptions-item label="Tên file">{{ selectedImage.name }}</a-descriptions-item>
            <a-descriptions-item label="Kích thước">{{ formatFileSize(selectedImage.size) }}</a-descriptions-item>
            <a-descriptions-item label="Thư mục">{{ getFolderName(selectedImage.folder) }}</a-descriptions-item>
            <a-descriptions-item label="Ngày upload">{{ formatDate(selectedImage.uploaded_at) }}</a-descriptions-item>
            <a-descriptions-item label="Mô tả" :span="2">{{ selectedImage.description || 'Không có' }}</a-descriptions-item>
          </a-descriptions>
        </div>
      </div>
    </a-modal>

    <!-- Upload Modal -->
    <a-modal v-model:open="uploadVisible" title="Upload ảnh sản phẩm" width="600px" @ok="handleUpload" @cancel="handleCancel">
      <a-form :model="uploadForm" layout="vertical">
        <a-form-item label="Chọn thư mục">
          <a-select v-model="uploadForm.folder" placeholder="Chọn thư mục để lưu">
            <a-option value="sneakers">Giày sneaker</a-option>
            <a-option value="boots">Giày boot</a-option>
            <a-option value="heels">Giày cao gót</a-option>
            <a-option value="other">Khác</a-option>
          </a-select>
        </a-form-item>

        <a-form-item label="Upload ảnh">
          <a-upload v-model:file-list="fileList" :multiple="true" :before-upload="beforeUpload" accept=".jpg,.jpeg,.png,.gif,.webp" drag>
            <div style="padding: 20px; text-align: center; border: 2px dashed #d9d9d9; border-radius: 6px">
              <p style="font-size: 24px; margin-bottom: 8px">
                <icon-folder />
              </p>
              <p style="font-size: 16px; margin-bottom: 4px">Nhấp hoặc kéo file vào khu vực này để upload</p>
              <p style="font-size: 12px; color: #999">Hỗ trợ JPG, PNG, GIF, WebP. Tối đa 10MB mỗi file.</p>
            </div>
          </a-upload>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import {
  IconUpload,
  IconImage,
  IconFolder,
  IconStorage,
  IconClockCircle,
  IconSearch,
  IconApps,
  IconList,
  IconEye,
  IconEdit,
  IconDownload,
  IconDelete,
} from '@arco-design/web-vue/es/icon'

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()

// Reactive data
const filters = ref({
  search: '',
  status: '',
  folder: '',
  sort: 'newest',
})

const viewMode = ref('grid')
const previewVisible = ref(false)
const uploadVisible = ref(false)
const selectedImage = ref(null)
const fileList = ref([])

// Mock data
const images = ref([
  {
    id: 1,
    name: 'Nike Air Max 270 - Trắng',
    url: 'https://via.placeholder.com/200x200/f5f5f5/666666?text=Nike+Air+Max',
    size: 2450000,
    folder: 'sneakers',
    status: 'active',
    uploaded_at: '2024-01-20',
    description: 'Ảnh chính giày Nike Air Max 270 màu trắng',
  },
  {
    id: 2,
    name: 'Dr. Martens Boot - Đen',
    url: 'https://via.placeholder.com/200x200/333333/ffffff?text=Dr.+Martens',
    size: 1890000,
    folder: 'boots',
    status: 'active',
    uploaded_at: '2024-01-19',
    description: 'Ảnh chi tiết giày boot Dr. Martens màu đen',
  },
  {
    id: 3,
    name: 'Jimmy Choo Heel - Đỏ',
    url: 'https://via.placeholder.com/200x200/ff4d4f/ffffff?text=Jimmy+Choo',
    size: 3200000,
    folder: 'heels',
    status: 'inactive',
    uploaded_at: '2024-01-18',
    description: 'Ảnh sản phẩm giày cao gót Jimmy Choo màu đỏ',
  },
])

// Filtered images computed property
const filteredImages = computed(() => {
  return images.value.filter((image) => {
    // Filter by status
    if (filters.value.status && image.status !== filters.value.status) {
      return false
    }

    // Filter by search (name)
    if (filters.value.search) {
      const searchTerm = filters.value.search.toLowerCase()
      const matchesName = image.name.toLowerCase().includes(searchTerm)
      const matchesDescription = image.description?.toLowerCase().includes(searchTerm)

      if (!matchesName && !matchesDescription) {
        return false
      }
    }

    return true
  })
})

const uploadForm = reactive({
  folder: '',
})

const listColumns = [
  {
    title: 'STT',
    dataIndex: 'stt',
    slotName: 'stt',
    width: 50,
    align: 'center',
  },
  {
    title: 'Ảnh',
    dataIndex: 'preview',
    slotName: 'preview',
    width: 80,
    align: 'center',
  },
  {
    title: 'Tên ảnh',
    dataIndex: 'name',
    slotName: 'name',
    width: 250,
  },
  {
    title: 'Trạng thái',
    dataIndex: 'status',
    slotName: 'status',
    width: 100,
    align: 'center',
  },
  {
    title: 'Thao tác',
    slotName: 'action',
    width: 200,
    fixed: 'right',
    align: 'center',
  },
]

// Pagination
const pagination = computed(() => ({
  current: 1,
  pageSize: 12,
  total: filteredImages.value.length,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: true,
}))

const loading = ref(false)

// Methods
const resetFilters = () => {
  filters.value = {
    search: '',
    status: '',
    folder: '',
    sort: 'newest',
  }
}

const exportExcel = () => {
  // TODO: Implement Excel export functionality
  console.log('Exporting to Excel...')
  // You can implement the actual Excel export logic here
  // For example, using libraries like xlsx or similar
}

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${parseFloat((bytes / k ** i).toFixed(2))} ${sizes[i]}`
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN')
}

const getFolderColor = (folder: string) => {
  switch (folder) {
    case 'sneakers':
      return 'blue'
    case 'boots':
      return 'green'
    case 'heels':
      return 'red'
    case 'other':
      return 'gray'
    default:
      return 'default'
  }
}

const getFolderName = (folder: string) => {
  switch (folder) {
    case 'sneakers':
      return 'Giày sneaker'
    case 'boots':
      return 'Giày boot'
    case 'heels':
      return 'Giày cao gót'
    case 'other':
      return 'Khác'
    default:
      return folder
  }
}

const searchImages = () => {
  // Removed console.log
}

const toggleViewMode = () => {
  viewMode.value = viewMode.value === 'grid' ? 'list' : 'grid'
}

const previewImage = (image: any) => {
  selectedImage.value = image
  previewVisible.value = true
}

const editImage = (image: any) => {
  // Removed console.log
}

const downloadImage = (image: any) => {
  // Removed console.log
}

const deleteImage = (image: any) => {
  // Removed console.log
}

const showUploadModal = () => {
  uploadVisible.value = true
}

const handleUpload = () => {
  // Removed console.log
  uploadVisible.value = false
}

const handleCancel = () => {
  uploadVisible.value = false
}

const beforeUpload = (file: File) => {
  const isValidType = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  if (!isValidType) {
    // console.error('Chỉ được upload file ảnh!')
    return false
  }
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    // console.error('Ảnh phải nhỏ hơn 10MB!')
    return false
  }
  return false
}
</script>

<style scoped>
.product-attribute-image-page {
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

.header-right {
  display: flex;
  gap: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.stat-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  font-size: 20px;
  color: #1890ff;
}

.folder-icon {
  color: #52c41a;
}

.storage-icon {
  color: #faad14;
}

.recent-icon {
  color: #722ed1;
}

.stat-content {
  margin-top: 12px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1d2129;
  margin-bottom: 4px;
}

.stat-change {
  font-size: 12px;
  color: #86909c;
}

.filters-card,
.images-card {
  margin-bottom: 16px;
}

.actions-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.image-item {
  border: 1px solid #f0f0f0;
  overflow: hidden;
  background: #fff;
  cursor: pointer;
  min-height: 280px;
}

.image-item .image-container {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.image-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-item .image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
}

.image-item .image-info {
  padding: 12px;
}

.image-name {
  font-weight: 500;
  color: #1d2129;
  margin-bottom: 4px;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.image-meta {
  font-size: 12px;
  color: #86909c;
}

.image-status {
  margin-top: 4px;
}

.images-list {
  margin-top: 16px;
}

.table-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
}

.preview-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.preview-image {
  max-width: 100%;
  max-height: 400px;
  object-fit: contain;
}

.preview-info {
  padding: 16px;
  background: #f8f9fa;
}

/* Responsive */
@media (max-width: 768px) {
  .product-attribute-image-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .images-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }

  .header-right {
    width: 100%;
    justify-content: center;
  }
}
</style>
