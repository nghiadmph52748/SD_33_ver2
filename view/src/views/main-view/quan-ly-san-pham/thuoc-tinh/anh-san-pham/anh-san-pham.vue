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
                <a-button type="primary" status="danger" size="mini" @click.stop="deleteImage(image)">
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
              <a-button type="text" @click="editImage(record)">
                <template #icon>
                  <icon-edit />
                </template>
              </a-button>
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
            <a-descriptions-item label="Màu chủ đạo">{{ selectedImage.color }}</a-descriptions-item>
            <a-descriptions-item label="Kích thước">{{ formatFileSize(selectedImage.size) }}</a-descriptions-item>
            <a-descriptions-item label="Thư mục">{{ getFolderName(selectedImage.folder) }}</a-descriptions-item>
            <a-descriptions-item label="Ngày upload">{{ formatDate(selectedImage.uploaded_at) }}</a-descriptions-item>
            <a-descriptions-item label="Mô tả" :span="2">{{ selectedImage.description || 'Không có' }}</a-descriptions-item>
          </a-descriptions>
        </div>
      </div>
    </a-modal>

    <!-- Upload Modal -->
    <div
      v-if="uploadVisible"
      style="
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.5);
        z-index: 9999;
        display: flex;
        align-items: center;
        justify-content: center;
      "
    >
      <div style="background: white; padding: 20px; border-radius: 8px; width: 600px; max-height: 80vh; overflow-y: auto">
        <h3>Upload ảnh sản phẩm</h3>
        <a-form :model="uploadForm" layout="vertical">
          <a-form-item label="Tên ảnh" required>
            <a-input v-model="uploadForm.tenAnh" placeholder="Nhập tên cho ảnh" />
          </a-form-item>
          <a-form-item label="Upload ảnh" required>
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

          <!-- Image Preview -->
          <div v-if="fileList.length > 0" class="image-preview-section">
            <h4>Ảnh đã chọn:</h4>
            <div class="preview-grid">
              <div v-for="(file, index) in fileList" :key="index" class="preview-item">
                <img :src="file.url || URL.createObjectURL(file.originFile || file)" :alt="file.name" class="preview-thumbnail" />
                <div class="preview-info">
                  <span>{{ file.name }}</span>
                  <span class="file-size">{{ formatFileSize(file.size) }}</span>
                </div>
              </div>
            </div>
          </div>
        </a-form>

        <div style="margin-top: 16px; text-align: right">
          <a-button @click="handleCancel" style="margin-right: 8px" :disabled="uploadLoading">Hủy</a-button>
          <a-button type="primary" @click="handleUpload" :loading="uploadLoading">
            <template v-if="!uploadLoading">Upload</template>
            <template v-else>Đang upload...</template>
          </a-button>
        </div>
      </div>
    </div>

    <!-- Edit Modal -->
    <div
      v-if="editVisible"
      style="
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.5);
        z-index: 9999;
        display: flex;
        align-items: center;
        justify-content: center;
      "
    >
      <div style="background: white; padding: 20px; border-radius: 8px; width: 600px; max-height: 80vh; overflow-y: auto">
        <h3>Chỉnh sửa ảnh sản phẩm</h3>
        <a-form :model="editForm" layout="vertical">
          <a-form-item label="Tên ảnh" required>
            <a-input v-model="editForm.tenAnh" placeholder="Nhập tên cho ảnh" />
          </a-form-item>

          <a-form-item label="Trạng thái">
            <a-radio-group v-model="editForm.trangThai">
              <a-radio :value="true">Hoạt động</a-radio>
              <a-radio :value="false">Không hoạt động</a-radio>
            </a-radio-group>
          </a-form-item>
        </a-form>

        <div style="margin-top: 16px; text-align: right">
          <a-button @click="handleCancel" style="margin-right: 8px" :disabled="editLoading">Hủy</a-button>
          <a-button type="primary" @click="handleEdit" :loading="editLoading">
            <template v-if="!editLoading">Cập nhật</template>
            <template v-else>Đang cập nhật...</template>
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { useUserStore } from '@/store'
import { Message } from '@arco-design/web-vue'
import { IconUpload, IconFolder, IconApps, IconList, IconEdit, IconDownload, IconDelete, IconRefresh } from '@arco-design/web-vue/es/icon'
import {
  getAnhSanPhamList,
  uploadMutipartFile,
  updateMutipartFile,
  deleteAnhSanPham,
} from '../../../../../api/san-pham/thuoc-tinh/anh-san-pham'

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()

// User store
const userStore = useUserStore()

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
const editVisible = ref(false)
const selectedImage = ref<any>(null)
const fileList = ref([])

// Mock data
const images = ref<any[]>([])

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
  tenAnh: '',
  file: '',
})

const editForm = reactive({
  id: null as number | null,
  tenAnh: '',
  trangThai: true,
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
    title: 'Tên màu',
    dataIndex: 'color',
    width: 150,
    align: 'center',
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
  pageSize: 10,
  total: filteredImages.value.length,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: true,
}))

const loading = ref(false)
const uploadLoading = ref(false)
const editLoading = ref(false)

// Methods
const resetFilters = () => {
  filters.value = {
    search: '',
    status: '',
    sort: 'newest',
  }
}

const exportExcel = () => {
  // TODO: Implement Excel export functionality
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

const loadImages = async () => {
  try {
    loading.value = true
    const res = await getAnhSanPhamList(0)
    if (res.success) {
      // Map API data to component expected format
      images.value = res.data.data.map((item: any) => ({
        id: item.id,
        name: item.tenAnh,
        url: item.duongDanAnh,
        status: item.trangThai ? 'active' : 'inactive',
        color: item.mauAnh,
        folder: 'other', // Default folder
        size: 0, // Default size
        uploaded_at: item.createAt,
        description: `Màu chủ đạo: ${item.mauAnh}`,
        createAt: item.createAt,
        createBy: item.createBy,
        updateAt: item.updateAt,
        updateBy: item.updateBy,
        deleted: item.deleted,
      }))
      pagination.value.total = res.data.totalElements
      pagination.value.pageSize = res.data.pageSize
      pagination.value.current = res.data.currentPage + 1
    } else {
      images.value = []
      pagination.value.total = 0
      pagination.value.pageSize = 10
      pagination.value.current = 1
    }
  } catch (error) {
    Message.error('Lỗi khi tải danh sách ảnh')
  } finally {
    loading.value = false
  }
}

const searchImages = () => {
  // Search is handled by the filteredImages computed property
  // This function can be used for additional search logic if needed
}

const toggleViewMode = () => {
  viewMode.value = viewMode.value === 'grid' ? 'list' : 'grid'
}

const previewImage = (image: any) => {
  selectedImage.value = image
  previewVisible.value = true
}

const editImage = (image: any) => {
  editForm.id = image.id
  editForm.tenAnh = image.name
  editForm.trangThai = image.status === 'active'
  editVisible.value = true
}

const downloadImage = (image: any) => {
  const link = document.createElement('a')
  link.href = image.url
  link.download = image.name || 'image'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const deleteImage = async (image: any) => {
  try {
    await deleteAnhSanPham(image.id)
    // Reload images
    await loadImages()
  } catch (error) {
    Message.error('Lỗi khi xóa ảnh')
  }
}

const showUploadModal = () => {
  uploadVisible.value = true
}

const handleUpload = async () => {
  if (!uploadForm.tenAnh.trim()) {
    Message.error('Vui lòng nhập tên ảnh')
    return
  }

  if (fileList.value.length === 0) {
    Message.error('Vui lòng chọn ít nhất một file ảnh để upload')
    return
  }

  try {
    uploadLoading.value = true
    const files = fileList.value.map((fileItem: any) => fileItem.originFile || fileItem.file)
    const result = await uploadMutipartFile(files, uploadForm.tenAnh, userStore.id!)

    if (result.data && result.data.length > 0) {
      Message.success('Upload ảnh thành công')

      // Clean up preview URLs
      fileList.value.forEach((file: any) => {
        if (file.url && file.url.startsWith('blob:')) {
          URL.revokeObjectURL(file.url)
        }
      })

      // Reset form
      uploadForm.tenAnh = ''
      uploadForm.file = ''
      fileList.value = []

      // Reload images
      await loadImages()

      uploadVisible.value = false
    }
  } catch (error) {
    Message.error('Lỗi khi upload ảnh')
  } finally {
    uploadLoading.value = false
  }
}

const handleCancel = () => {
  uploadVisible.value = false
  editVisible.value = false

  // Clean up preview URLs
  fileList.value.forEach((file: any) => {
    if (file.url && file.url.startsWith('blob:')) {
      URL.revokeObjectURL(file.url)
    }
  })

  uploadForm.tenAnh = ''
  uploadForm.file = ''
  fileList.value = []
  editForm.id = null
  editForm.tenAnh = ''
  editForm.trangThai = true
}

const handleEdit = async () => {
  if (!editForm.tenAnh.trim() || !editForm.id) {
    Message.error('Vui lòng nhập tên ảnh')
    return
  }

  try {
    editLoading.value = true
    const result = await updateMutipartFile(editForm.id!, [], editForm.tenAnh, userStore.id!)

    if (result.data && result.data.length > 0) {
      Message.success('Cập nhật ảnh thành công')
      // Reload images
      await loadImages()

      editVisible.value = false
      editForm.id = null
      editForm.tenAnh = ''
      editForm.trangThai = true
    }
  } catch (error) {
    Message.error('Lỗi khi chỉnh sửa ảnh')
  } finally {
    editLoading.value = false
  }
}

const beforeUpload = (file: File) => {
  const isValidType = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  if (!isValidType) {
    Message.error('Chỉ được upload file ảnh!')
    return false
  }
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    Message.error('Ảnh phải nhỏ hơn 10MB!')
    return false
  }

  // Create preview URL for the file
  file.url = URL.createObjectURL(file)
  return false
}
onMounted(() => {
  loadImages()
})
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

.image-color {
  font-size: 12px;
  color: #165dff;
  margin-bottom: 4px;
  font-weight: 500;
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

/* Image Preview Section */
.image-preview-section {
  margin-top: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 6px;
}

.image-preview-section h4 {
  margin: 0 0 12px 0;
  color: #1d2129;
  font-size: 14px;
  font-weight: 500;
}

.preview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
}

.preview-item {
  border: 1px solid #e5e6eb;
  border-radius: 6px;
  overflow: hidden;
  background: white;
}

.preview-thumbnail {
  width: 100%;
  height: 80px;
  object-fit: cover;
  display: block;
}

.preview-info {
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.preview-info span:first-child {
  font-size: 12px;
  font-weight: 500;
  color: #1d2129;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 11px;
  color: #86909c;
}

/* Modal styles */
:deep(.arco-modal) {
  z-index: 1000 !important;
}

:deep(.arco-modal-mask) {
  z-index: 999 !important;
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
