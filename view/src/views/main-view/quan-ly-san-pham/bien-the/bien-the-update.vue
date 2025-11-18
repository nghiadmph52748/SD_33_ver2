<template>
  <div class="variant-update-page">
    <!-- Loading State -->
    <div v-if="initialLoading" class="loading-container">
      <a-spin :size="40" />
    </div>

    <!-- Update Form -->
    <div v-else-if="variant">
      <!-- Header Card -->
      <a-card class="header-card">
        <template #extra>
          <a-space>
            <a-button @click="goBack">
              <template #icon>
                <icon-arrow-left />
              </template>
              Quay lại
            </a-button>
            <a-button @click="viewDetail">
              <template #icon>
                <icon-eye />
              </template>
              Xem chi tiết
            </a-button>
          </a-space>
        </template>
        <div class="variant-header">
          <div class="variant-image">
            <a-avatar
              :image-url="
                Array.isArray(variant.anhSanPham) && variant.anhSanPham.length > 0 ? variant.anhSanPham[0] : '/default-product.png'
              "
              :size="80"
              shape="square"
            />
          </div>
          <div class="variant-info">
            <h2>Chỉnh sửa biến thể</h2>
            <p class="variant-code">
              Mã biến thể:
              <strong>{{ variant.maChiTietSanPham }}</strong>
            </p>
          </div>
        </div>
      </a-card>

      <!-- Update Form -->
      <a-form :model="formData" :rules="rules" ref="formRef" layout="vertical">
        <a-row :gutter="16">
          <!-- Basic Information -->
          <a-col :span="12">
            <a-card title="Thông tin cơ bản" class="form-card">
              <a-form-item>
                <template #label>
                  <span class="required-field">Giá bán</span>
                </template>
                <a-input-number
                  v-model="formData.giaBan"
                  :min="0"
                  :max="9999999999"
                  :precision="0"
                  placeholder="Nhập giá bán"
                  style="width: 100%"
                />
              </a-form-item>

              <a-form-item>
                <template #label>
                  <span class="required-field">Số lượng tồn kho</span>
                </template>
                <a-input-number
                  v-model="formData.soLuong"
                  :min="0"
                  :precision="0"
                  placeholder="Nhập số lượng tồn kho"
                  style="width: 100%"
                />
              </a-form-item>

              <a-form-item>
                <template #label>
                  <span class="required-field">Trạng thái</span>
                </template>
                <a-radio-group v-model="formData.deleted">
                  <a-radio :value="false">Đang bán</a-radio>
                  <a-radio :value="true">Tạm ngưng bán</a-radio>
                </a-radio-group>
              </a-form-item>
            </a-card>
          </a-col>

          <!-- Attributes -->
          <a-col :span="12">
            <a-card title="Thuộc tính sản phẩm" class="form-card">
              <a-form-item>
                <template #label>
                  <span class="required-field">Màu sắc</span>
                </template>
                <a-select v-model="formData.mauSac" placeholder="Chọn màu sắc" :loading="attributesLoading" allow-clear>
                  <a-option v-for="color in colorOptions" :key="color.value" :value="color.value">
                    {{ color.label }}
                  </a-option>
                </a-select>
              </a-form-item>

              <a-form-item>
                <template #label>
                  <span class="required-field">Kích thước</span>
                </template>
                <a-select v-model="formData.kichThuoc" placeholder="Chọn kích thước" :loading="attributesLoading" allow-clear>
                  <a-option v-for="size in sizeOptions" :key="size.value" :value="size.value">
                    {{ size.label }}
                  </a-option>
                </a-select>
              </a-form-item>

              <a-form-item>
                <template #label>
                  <span class="required-field">Chất liệu</span>
                </template>
                <a-select v-model="formData.chatLieu" placeholder="Chọn chất liệu" :loading="attributesLoading" allow-clear>
                  <a-option v-for="material in materialOptions" :key="material.value" :value="material.value">
                    {{ material.label }}
                  </a-option>
                </a-select>
              </a-form-item>

              <a-form-item>
                <template #label>
                  <span class="required-field">Đế giày</span>
                </template>
                <a-select v-model="formData.deGiay" placeholder="Chọn đế giày" :loading="attributesLoading" allow-clear>
                  <a-option v-for="sole in soleOptions" :key="sole.value" :value="sole.value">
                    {{ sole.label }}
                  </a-option>
                </a-select>
              </a-form-item>

              <a-form-item>
                <template #label>
                  <span class="required-field">Trọng lượng</span>
                </template>
                <a-select v-model="formData.trongLuong" placeholder="Chọn trọng lượng" :loading="attributesLoading" allow-clear>
                  <a-option v-for="weight in weightOptions" :key="weight.value" :value="weight.value">
                    {{ weight.label }}
                  </a-option>
                </a-select>
              </a-form-item>
            </a-card>
          </a-col>
        </a-row>

        <!-- Images Gallery -->
        <!-- Images Gallery -->
        <a-card title="Quản lý hình ảnh" class="images-card">
          <!-- Loading overlay during upload/delete (styled like ThemSanPham) -->
          <div v-if="uploadingImages || deletingImage" class="image-upload-loading-overlay">
            <div class="image-upload-loading-content">
              <a-spin />
              <div class="image-upload-loading-text">
                <h3>{{ uploadingImages ? 'Đang upload ảnh...' : 'Đang xoá ảnh...' }}</h3>
                <p>Vui lòng chờ trong giây lát</p>
              </div>
            </div>
          </div>

          <div
            class="images-gallery"
            :style="{
              opacity: uploadingImages || deletingImage ? 0.6 : 1,
              pointerEvents: uploadingImages || deletingImage ? 'none' : 'auto',
            }"
          >
            <!-- Existing Images -->
            <div v-for="(image, index) in managedImages" :key="`image-${managedImageIds[index]}`" class="image-item-container">
              <div
                class="image-item"
                :class="{ 'image-selected': selectedImageId === managedImageIds[index] }"
                @click="selectImage(managedImageIds[index])"
              >
                <a-image :src="image" :width="120" :height="120" fit="cover" />
                <div class="image-overlay">
                  <div class="image-actions">
                    <a-button
                      type="text"
                      size="small"
                      @click.stop="editImage(managedImageIds[index])"
                      class="icon-button"
                      :disabled="deletingImage"
                    >
                      <template #icon>
                        <icon-edit />
                      </template>
                    </a-button>
                    <a-button
                      type="text"
                      size="small"
                      @click.stop="deleteImage(managedImageIds[index])"
                      class="icon-button"
                      :disabled="deletingImage"
                    >
                      <template #icon>
                        <icon-delete />
                      </template>
                    </a-button>
                  </div>
                </div>
              </div>
              <span class="image-index">{{ index + 1 }}</span>
            </div>

            <!-- Add Image Button (only show if < 5 images) -->
            <div v-if="managedImages.length < 5" class="image-item-container">
              <div
                class="image-add-item"
                @click="uploadingImages || deletingImage ? null : triggerImageUpload()"
                :style="{ cursor: uploadingImages || deletingImage ? 'not-allowed' : 'pointer' }"
              >
                <div class="add-icon">
                  <icon-plus />
                </div>
                <span style="font-size: 12px; color: #666; margin-top: 4px">Thêm ảnh</span>
              </div>
            </div>

            <!-- Hidden file input -->
            <input
              ref="fileInput"
              type="file"
              accept="image/*"
              multiple
              style="display: none"
              @change="handleImageUpload"
              :disabled="uploadingImages || deletingImage"
            />
          </div>

          <!-- Image limit info -->
          <div style="margin-top: 12px; font-size: 12px; color: #999">
            <span v-if="managedImages.length < 5">
              {{ managedImages.length }}/5 ảnh (có thể thêm {{ 5 - managedImages.length }} ảnh nữa)
            </span>
            <span v-else style="color: #f53f3f">Đã đạt giới hạn 5 ảnh</span>
          </div>

          <!-- Edit Image Modal -->
          <a-modal
            v-model:visible="showImageEditModal"
            title="Chỉnh sửa ảnh"
            ok-text="Cập nhật"
            cancel-text="Hủy"
            @ok="confirmEditImage"
            width="600px"
            :ok-button-props="{ disabled: uploadingImages || deletingImage }"
            :cancel-button-props="{ disabled: uploadingImages || deletingImage }"
          >
            <a-spin v-if="uploadingImages" :size="40" tip="Đang xử lý ảnh..." style="margin-bottom: 16px" />

            <div v-show="!uploadingImages" style="text-align: center">
              <div v-if="editingImageData" style="margin-bottom: 16px">
                <img :src="editingImageData" :style="{ maxWidth: '100%', maxHeight: '400px' }" />
              </div>
              <div style="margin-bottom: 16px">
                <a-button type="primary" @click="triggerEditImageUpload" :disabled="uploadingImages || deletingImage">
                  <template #icon>
                    <icon-plus />
                  </template>
                  Chọn ảnh thay thế
                </a-button>
              </div>
              <input
                ref="editFileInput"
                type="file"
                accept="image/*"
                style="display: none"
                @change="handleImageEditUpload"
                :disabled="uploadingImages || deletingImage"
              />
            </div>
          </a-modal>
        </a-card>
        <!-- Action Buttons -->
        <a-card class="action-card">
          <div class="action-buttons">
            <a-button size="large" @click="handleReset">
              <template #icon>
                <icon-refresh />
              </template>
              Đặt lại
            </a-button>
            <a-button type="primary" size="large" :loading="updating" @click="showConfirmUpdate">
              <template #icon>
                <icon-save />
              </template>
              Cập nhật
            </a-button>
          </div>
        </a-card>
      </a-form>
    </div>

    <!-- Error State -->
    <div v-else class="error-container">
      <a-result status="404" title="Không tìm thấy biến thể" sub-title="Biến thể bạn đang tìm kiếm không tồn tại hoặc đã bị xóa.">
        <template #extra>
          <a-button type="primary" @click="goBack">Quay lại</a-button>
        </template>
      </a-result>
    </div>

    <!-- Update Confirm Modal -->
    <a-modal
      v-model:visible="showUpdateConfirm"
      title="Xác nhận cập nhật"
      ok-text="Cập nhật"
      cancel-text="Hủy"
      @ok="confirmUpdate"
      @cancel="cancelUpdate"
    >
      <template #default>
        <div>Bạn có chắc chắn muốn cập nhật thông tin biến thể này?</div>
        <div v-if="variant">
          <div style="margin-top: 12px">
            <strong>Mã biến thể:</strong>
            {{ variant.maChiTietSanPham }}
          </div>
          <div style="margin-top: 8px">
            <strong>Thông tin sẽ được cập nhật:</strong>
          </div>
          <ul style="margin-top: 8px; padding-left: 20px">
            <li>Giá bán: {{ formatCurrency(formData.giaBan) }}</li>
            <li>Số lượng: {{ formData.soLuong }}</li>
            <li>Trạng thái: {{ !formData.deleted ? 'Đang bán' : 'Tạm ngưng bán' }}</li>
            <li>Màu sắc: {{ formData.mauSac }}</li>
            <li>Kích thước: {{ formData.kichThuoc }}</li>
            <li>Chất liệu: {{ formData.chatLieu }}</li>
            <li>Đế giày: {{ formData.deGiay }}</li>
            <li>Trọng lượng: {{ formData.trongLuong }}</li>
          </ul>
        </div>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { useUserStore } from '@/store'
import { IconArrowLeft, IconEye, IconRefresh, IconSave, IconPlus, IconEdit, IconDelete } from '@arco-design/web-vue/es/icon'
import {
  getBienTheById,
  updateBienThe,
  getSanPhamOptions,
  getMauSac,
  getKichThuoc,
  getChatLieu,
  getDeGiay,
  getTrongLuong,
  uploadAnhBienThe,
  getAnhSanPhamById,
  addAnhToBienThe,
  deleteAnhBienTheLink,
  getAllAnhBienTheDetails,
  type BienTheSanPham,
  type SanPham,
} from '@/api/san-pham/bien-the'

// Router
const route = useRoute()
const router = useRouter()

// User store
const userStore = useUserStore()

// Breadcrumb

// State
const initialLoading = ref(true)
const updating = ref(false)
const uploadingImages = ref(false) // Loading state for image upload
const deletingImage = ref(false) // Loading state for image delete
const attributesLoading = ref(false)
const variant = ref<BienTheSanPham | null>(null)
const formRef = ref()

// Update confirm modal state
const showUpdateConfirm = ref(false)

// Image management state
const managedImages = ref<string[]>([])
const managedImageIds = ref<number[]>([]) // Track image IDs from server (parallel array with managedImages)
const originalImageIds = ref<number[]>([]) // Track original IDs from DB for cleanup
const selectedImageId = ref<number | null>(null) // Track selected image by ID, not index
const showImageEditModal = ref(false)
const editingImageId = ref<number | null>(null) // Track editing image by ID, not index
const editingImageData = ref<string | null>(null)
const newImageData = ref<string | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)
const editFileInput = ref<HTMLInputElement | null>(null)

// Get variant ID from route
const variantId = Number(route.params.id)

// Dynamic page title cho tab-bar
const pageTitle = computed(() => {
  if (variant.value) {
    return `Chỉnh sửa - ${variant.value.tenSanPham || 'Biến thể'}`
  }
  return 'Chỉnh sửa biến thể'
})

// Cập nhật document title
watch(
  pageTitle,
  (newTitle) => {
    if (typeof document !== 'undefined') {
      document.title = newTitle
    }
  },
  { immediate: true }
)

// Form data
const formData = reactive({
  giaBan: 0,
  giaTriGiamGia: 0,
  soLuong: 0,
  trangThai: true,
  deleted: false,
  mauSac: null,
  kichThuoc: null,
  chatLieu: null,
  deGiay: null,
  trongLuong: null,
})

// Original data for reset
const originalData = ref<any>({})

// Attribute options
const sanPhamOptions = ref<SanPham[]>([])
const colorOptions = ref([])
const sizeOptions = ref([])
const materialOptions = ref([])
const soleOptions = ref([])
const weightOptions = ref([])

// Form rules
const rules = {
  giaBan: [
    { required: true, message: 'Vui lòng nhập giá bán' },
    { type: 'number', min: 0, message: 'Giá bán phải lớn hơn hoặc bằng 0' },
  ],
  giaTriGiamGia: [{ type: 'number', min: 0, max: 100, message: 'Giá trị giảm giá phải từ 0 đến 100%' }],
  soLuong: [
    { required: true, message: 'Vui lòng nhập số lượng tồn kho' },
    { type: 'number', min: 0, message: 'Số lượng phải lớn hơn hoặc bằng 0' },
  ],
}

// Helper function to find product ID by name
const findProductIdByName = (productName: string) => {
  if (!productName || !sanPhamOptions.value.length) {
    return null
  }

  const found = sanPhamOptions.value.find((product) => product.tenSanPham === productName)
  if (!found) {
    return null
  }

  return found.id
}

// Methods
const formatCurrency = (amount: number) => {
  if (!amount) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const goBack = () => {
  router.go(-1)
}

const viewDetail = () => {
  router.push(`/quan-ly-san-pham/bien-the/detail/${variantId}`)
}

// Image Management Methods
const selectImage = (imageId: number) => {
  selectedImageId.value = selectedImageId.value === imageId ? null : imageId
}

const triggerImageUpload = () => {
  if (managedImages.value.length < 5) {
    fileInput.value?.click()
  } else {
    Message.warning('Đã đạt giới hạn 5 ảnh')
  }
}

const triggerEditImageUpload = () => {
  editFileInput.value?.click()
}

const buildImageMetadata = (file?: File) => {
  const productName = variant.value?.tenSanPham?.trim() || ''
  const colorName = variant.value?.tenMauSac?.trim() || ''

  const parts = [productName, colorName].filter(Boolean)
  let tenAnh = parts.join(' - ')

  if (!tenAnh) {
    const original = file?.name?.trim()
    tenAnh = original ? original.replace(/\.[^/.]+$/, '') : 'product-image'
  }

  const mauAnh = colorName || 'product-image'
  return { tenAnh, mauAnh }
}

const handleImageUpload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const files = input.files

  if (!files || files.length === 0) return

  try {
    // Check limit
    if (managedImages.value.length >= 5) {
      Message.warning('Đã đạt giới hạn 5 ảnh')
      return
    }

    // Filter files to not exceed 5 total
    const filesToUpload = Array.from(files).slice(0, 5 - managedImages.value.length)

    if (filesToUpload.length === 0) {
      Message.warning('Đã đạt giới hạn 5 ảnh')
      return
    }

    uploadingImages.value = true
    Message.loading(`Đang upload ${filesToUpload.length} ảnh...`)

    // Call backend API to upload all images to Cloudinary
    const { tenAnh, mauAnh } = buildImageMetadata(filesToUpload[0])
    const response = await uploadAnhBienThe(filesToUpload, tenAnh, mauAnh, userStore.id || 1)

    if (response.data && Array.isArray(response.data) && response.data.length > 0) {
      // Get all uploaded image URLs using the returned IDs
      const uploadedUrls: string[] = []
      let successCount = 0

      for (const imageId of response.data) {
        try {
          const imageDetailRes = await getAnhSanPhamById(imageId)
          if (imageDetailRes.data?.duongDanAnh) {
            uploadedUrls.push(imageDetailRes.data.duongDanAnh)
            managedImageIds.value.push(imageId) // Track the ID
            successCount++
          }
        } catch (err) {
          console.error(`Failed to get image details for ID ${imageId}:`, err)
        }
      }

      if (successCount > 0) {
        managedImages.value.push(...uploadedUrls)
        Message.success(`${successCount}/${filesToUpload.length} ảnh đã được upload thành công`)
      } else {
        Message.error('Không thể lấy URL ảnh từ server')
      }
    } else {
      Message.error('Lỗi upload ảnh: Server không trả về ID')
    }

    // Reset file input
    input.value = ''
  } catch (error: any) {
    console.error('Upload error:', error)
    Message.error(error?.message || 'Lỗi khi upload ảnh')
    input.value = ''
  } finally {
    uploadingImages.value = false
  }
}

const editImage = (imageId: number) => {
  editingImageId.value = imageId
  // Find the image URL by ID
  const imageIndex = managedImageIds.value.indexOf(imageId)
  if (imageIndex !== -1) {
    editingImageData.value = managedImages.value[imageIndex]
  }
  newImageData.value = null
  showImageEditModal.value = true
}

const handleImageEditUpload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const files = input.files

  if (!files || files.length === 0) return

  try {
    uploadingImages.value = true
    const file = files[0]
    Message.loading('Đang upload ảnh...')

    // Call backend API to upload image to Cloudinary
    const { tenAnh, mauAnh } = buildImageMetadata(file)
    const response = await uploadAnhBienThe([file], tenAnh, mauAnh, userStore.id || 1)

    if (response.data && Array.isArray(response.data) && response.data.length > 0) {
      // Get the uploaded image URL using the returned ID
      const newImageId = response.data[0]
      const imageDetailRes = await getAnhSanPhamById(newImageId)

      if (imageDetailRes.data?.duongDanAnh) {
        newImageData.value = imageDetailRes.data.duongDanAnh
        editingImageData.value = newImageData.value
        // Store the new image ID for confirmEditImage (use image ID, not index)
        if (editingImageId.value !== null) {
          if (!window.__editImageMapping) window.__editImageMapping = {}
          // Map: oldImageId -> newImageId
          window.__editImageMapping[editingImageId.value] = newImageId
        }
        Message.success('Ảnh được chọn thành công')
      } else {
        Message.error('Không thể lấy URL ảnh từ server')
      }
    } else {
      Message.error('Lỗi upload ảnh: Server không trả về ID')
    }

    // Reset file input
    input.value = ''
  } catch (error: any) {
    console.error('Upload error:', error)
    Message.error(error?.message || 'Lỗi khi upload ảnh')
    input.value = ''
  } finally {
    uploadingImages.value = false
  }
}

const confirmEditImage = async () => {
  if (editingImageId.value !== null && newImageData.value) {
    try {
      uploadingImages.value = true

      const oldImageId = editingImageId.value
      const newImageId = (window as any).__editImageMapping?.[oldImageId]
      // Find the array index by image ID
      const arrayIndex = managedImageIds.value.indexOf(oldImageId)
      if (arrayIndex === -1) {
        Message.error('Không tìm thấy ảnh được chọn')
        return
      }

      // If editing an OLD image (one that was in DB), delete the old one and add new one
      if (oldImageId && originalImageIds.value.includes(oldImageId)) {
        const relationshipId = (window as any).__imageRelationshipMap?.[oldImageId]

        // Delete the old relationship from DB
        if (relationshipId) {
          try {
            await deleteAnhBienTheLink(relationshipId)
          } catch (deleteError) {
            console.error('Error deleting old image relationship:', deleteError)
            Message.warning('Lỗi xóa ảnh cũ, nhưng vẫn tiếp tục')
          }
        }

        // Remove old image from originalImageIds so it won't be treated as "original"
        const originalIndex = originalImageIds.value.indexOf(oldImageId)
        if (originalIndex !== -1) {
          originalImageIds.value.splice(originalIndex, 1)
        }
      }

      // Update URL and Image ID in the arrays (use found index)
      managedImages.value[arrayIndex] = newImageData.value

      // Update image ID if new image uploaded
      if (newImageId) {
        managedImageIds.value[arrayIndex] = newImageId
      }

      showImageEditModal.value = false
      newImageData.value = ''

      // Clean up
      if ((window as any).__editImageMapping) {
        delete (window as any).__editImageMapping[oldImageId]
      }

      Message.success('Ảnh đã được cập nhật thành công')
    } catch (error: any) {
      console.error('Update image error:', error)
      Message.error(error?.message || 'Lỗi khi cập nhật ảnh')
    } finally {
      uploadingImages.value = false
    }
  } else if (!newImageData.value) {
    Message.warning('Vui lòng chọn ảnh mới')
  }
}

const deleteImage = async (imageId: number) => {
  // Find the array index by image ID
  const arrayIndex = managedImageIds.value.indexOf(imageId)
  if (arrayIndex === -1) {
    Message.error('Không tìm thấy ảnh được chọn')
    return
  }

  try {
    deletingImage.value = true
    // If this is an old image (from DB), delete the relationship
    if (imageId && originalImageIds.value.includes(imageId)) {
      try {
        // Get the relationship ID from the map
        const relationshipId = (window as any).__imageRelationshipMap?.[imageId]

        if (relationshipId) {
          // Gọi API đổi trạng thái relationship
          await deleteAnhBienTheLink(relationshipId)
        } else {
          console.warn('No relationship ID found for imageId:', imageId)
        }
      } catch (err) {
        console.warn('Could not delete relationship from DB:', err)
        Message.warning('Xoá khỏi database gặp lỗi, nhưng vẫn xoá khỏi danh sách')
      }
    }

    // Remove from UI and tracking arrays using found index
    managedImages.value.splice(arrayIndex, 1)
    managedImageIds.value.splice(arrayIndex, 1)

    // Remove from originalImageIds if it was there
    const origIndex = originalImageIds.value.indexOf(imageId)
    if (origIndex > -1) {
      originalImageIds.value.splice(origIndex, 1)
    }

    selectedImageId.value = null
    Message.success('Ảnh đã được xoá thành công')
  } catch (error: any) {
    console.error('Delete error:', error)
    Message.error(error?.message || 'Lỗi khi xoá ảnh')
  } finally {
    deletingImage.value = false
  }
}

const handleReset = () => {
  Object.assign(formData, originalData.value)
  formRef.value?.resetFields()
}

const handleUpdate = async () => {
  try {
    const valid = await formRef.value?.validate()
    if (!valid) {
      updating.value = true

      // Validate required attributes
      const missingAttributes = []
      if (!formData.mauSac) missingAttributes.push('Màu sắc')
      if (!formData.kichThuoc) missingAttributes.push('Kích thước')
      if (!formData.chatLieu) missingAttributes.push('Chất liệu')
      if (!formData.deGiay) missingAttributes.push('Đế giày')
      if (!formData.trongLuong) missingAttributes.push('Trọng lượng')

      if (missingAttributes.length > 0) {
        Message.error(`Thiếu thông tin thuộc tính: ${missingAttributes.join(', ')}`)
        updating.value = false
        return
      }

      // Find product ID by name
      const productId = findProductIdByName(variant.value.tenSanPham)
      if (!productId) {
        Message.error('Không tìm thấy ID sản phẩm, vui lòng thử lại')
        updating.value = false
        return
      }

      // Map form data to match UpdateBienTheRequest structure
      const updateData = {
        id: variantId,
        idSanPham: productId, // Sử dụng ID thực tìm được từ tên
        giaBan: formData.giaBan,
        giaTriGiamGia: formData.giaTriGiamGia,
        soLuong: formData.soLuong,
        trangThai: formData.trangThai,
        mauSac: formData.mauSac,
        kichThuoc: formData.kichThuoc,
        chatLieu: formData.chatLieu,
        deGiay: formData.deGiay,
        trongLuong: formData.trongLuong,
        ghiChu: variant.value.ghiChu,
        deleted: formData.deleted,
        createAt: variant.value.createdAt,
        createBy: variant.value.createdBy,
        updateAt: new Date().toISOString().split('T')[0], // LocalDate format: YYYY-MM-DD
        updateBy: userStore.id,
      }

      const response = await updateBienThe(updateData)
      if (response.success) {
        Message.success('Cập nhật biến thể thành công')
        router.push(`/quan-ly-san-pham/bien-the/${productId}`)
      } else {
        Message.error(response.message || 'Không thể cập nhật biến thể')
      }
    }
  } catch (error) {
    Message.error('Có lỗi xảy ra khi cập nhật biến thể')
  } finally {
    updating.value = false
  }
}

// Show update confirm modal
const showConfirmUpdate = async () => {
  try {
    const valid = await formRef.value?.validate()
    if (!valid) {
      // Validate required attributes
      const missingAttributes = []
      if (!formData.mauSac) missingAttributes.push('Màu sắc')
      if (!formData.kichThuoc) missingAttributes.push('Kích thước')
      if (!formData.chatLieu) missingAttributes.push('Chất liệu')
      if (!formData.deGiay) missingAttributes.push('Đế giày')
      if (!formData.trongLuong) missingAttributes.push('Trọng lượng')

      if (missingAttributes.length > 0) {
        Message.error(`Thiếu thông tin thuộc tính: ${missingAttributes.join(', ')}`)
        return
      }

      // Show confirm modal
      showUpdateConfirm.value = true
    }
  } catch (error) {
    Message.error('Vui lòng kiểm tra lại thông tin nhập vào')
  }
}

// Perform actual update (extracted from handleUpdate)
const performUpdate = async () => {
  try {
    updating.value = true

    // Find product ID by name
    const productId = findProductIdByName(variant.value.tenSanPham)
    if (!productId) {
      Message.error('Không tìm thấy ID sản phẩm, vui lòng thử lại')
      updating.value = false
      return
    }

    // Handle image relationships:
    // Only add NEW images, don't delete old ones
    // User can only delete images by clicking delete button
    // That will handle the DB cleanup

    // Add new image relationships (only new ones not already in originalImageIds)
    const idsToAdd = managedImageIds.value.filter((id: number) => !originalImageIds.value.includes(id))
    if (idsToAdd.length > 0) {
      try {
        await addAnhToBienThe(variantId, idsToAdd)
        Message.success(`${idsToAdd.length} ảnh mới đã được liên kết`)
      } catch (imgError) {
        console.error('Error linking images:', imgError)
        Message.warning('Cập nhật hình ảnh gặp lỗi, nhưng vẫn tiếp tục cập nhật biến thể')
      }
    }

    // Map form data to match UpdateBienTheRequest structure
    const updateData = {
      id: variantId,
      idSanPham: productId,
      giaBan: formData.giaBan,
      giaTriGiamGia: formData.giaTriGiamGia,
      soLuong: formData.soLuong,
      trangThai: formData.trangThai,
      mauSac: formData.mauSac,
      kichThuoc: formData.kichThuoc,
      chatLieu: formData.chatLieu,
      deGiay: formData.deGiay,
      trongLuong: formData.trongLuong,
      ghiChu: variant.value.ghiChu,
      deleted: formData.deleted,
      createAt: variant.value.createdAt,
      createBy: variant.value.createdBy,
      updateAt: new Date().toISOString().split('T')[0],
      updateBy: userStore.id,
    }

    const response = await updateBienThe(updateData)
    if (response.success) {
      Message.success('Cập nhật biến thể thành công!')

      // Navigate back with success parameter
      const returnTo = route.query.returnTo as string
      if (returnTo) {
        router.push({ path: returnTo, query: { updated: 'true' } })
      } else {
        router.push({
          path: `/quan-ly-san-pham/bien-the/${productId}`,
          query: { updated: 'true' },
        })
      }
    } else {
      Message.error(response.message || 'Không thể cập nhật biến thể')
    }
  } catch (error) {
    Message.error('Có lỗi xảy ra khi cập nhật biến thể')
  } finally {
    updating.value = false
  }
}

// Confirm update action
const confirmUpdate = async () => {
  showUpdateConfirm.value = false
  await performUpdate()
}

// Cancel update action
const cancelUpdate = () => {
  showUpdateConfirm.value = false
}

// Helper function to find attribute ID by name
const findAttributeIdByName = (options: any[], name: string, attributeType: string) => {
  if (!name || !options.length) {
    return null
  }
  const found = options.find((option) => option.label === name)
  if (!found) {
    Message.error(`Không tìm thấy ${attributeType}: ${name}`)
    return null
  }
  return found.value
}

// Load variant data
const loadVariantData = async () => {
  try {
    initialLoading.value = true
    const response = await getBienTheById(variantId)
    if (response.data && response.success) {
      variant.value = response.data

      // Try to load image relationship details from DB for tracking
      try {
        const allDetailsRes = await getAllAnhBienTheDetails()
        if (allDetailsRes.data && Array.isArray(allDetailsRes.data)) {
          // Find relationships for this variant
          const variantImageDetails = allDetailsRes.data.filter((detail: any) => detail.idChiTietSanPham === variantId)

          // CRITICAL: Get image URLs in the SAME ORDER as the relationship details
          // This ensures managedImages[i] and managedImageIds[i] are always synchronized
          const imageUrls: string[] = []
          const imageIds: number[] = []

          for (const detail of variantImageDetails) {
            try {
              const urlRes = await getAnhSanPhamById(detail.idAnhSanPham)
              if (urlRes.data?.duongDanAnh) {
                imageUrls.push(urlRes.data.duongDanAnh)
                imageIds.push(detail.idAnhSanPham)
              }
            } catch (err) {
              console.warn('Could not load image URL for ID:', detail.idAnhSanPham, err)
            }
          }

          // Set both arrays in synchronized order
          managedImages.value = imageUrls
          managedImageIds.value = imageIds

          // Store ORIGINAL IMAGE IDs for comparison
          originalImageIds.value = [...managedImageIds.value]

          // Also store relationship mapping for deletion: { imageId: relationshipId }
          if (!window.__imageRelationshipMap) {
            window.__imageRelationshipMap = {}
          }
          variantImageDetails.forEach((detail: any) => {
            ;(window as any).__imageRelationshipMap[detail.idAnhSanPham] = detail.id
          })
        }
      } catch (err) {
        console.warn('Could not load image detail relationships:', err)
        // Fallback: use images from variant as-is
        managedImages.value = Array.isArray(variant.value.anhSanPham) ? [...variant.value.anhSanPham] : []
      }

      // Populate form data
      formData.giaBan = variant.value.giaBan || 0
      formData.giaTriGiamGia = variant.value.giaTriGiamGia || 0
      formData.soLuong = variant.value.soLuong || 0
      formData.trangThai = variant.value.trangThai !== false
      formData.deleted = variant.value.deleted || false

      // Find attribute IDs by matching names with loaded options
      formData.mauSac = findAttributeIdByName(colorOptions.value, variant.value.tenMauSac, 'màu sắc')
      formData.kichThuoc = findAttributeIdByName(sizeOptions.value, variant.value.tenKichThuoc, 'kích thước')
      formData.chatLieu = findAttributeIdByName(materialOptions.value, variant.value.tenChatLieu, 'chất liệu')
      formData.deGiay = findAttributeIdByName(soleOptions.value, variant.value.tenDeGiay, 'đế giày')
      formData.trongLuong = findAttributeIdByName(weightOptions.value, variant.value.tenTrongLuong, 'trọng lượng')

      // Save original data for reset
      originalData.value = { ...formData }
    } else {
      Message.error('Không thể tải thông tin biến thể')
    }
  } catch (error) {
    Message.error('Có lỗi xảy ra khi tải thông tin biến thể')
  } finally {
    initialLoading.value = false
  }
}

// Load attribute options
const loadAttributeOptions = async () => {
  try {
    attributesLoading.value = true

    const [sanPhamRes, colorsRes, sizesRes, materialsRes, solesRes, weightsRes] = await Promise.all([
      getSanPhamOptions(),
      getMauSac(),
      getKichThuoc(),
      getChatLieu(),
      getDeGiay(),
      getTrongLuong(),
    ])

    // Load san pham options
    if (sanPhamRes.success) {
      sanPhamOptions.value = sanPhamRes.data || []
    }

    if (colorsRes.success) {
      colorOptions.value = colorsRes.data.map((item: any) => ({
        value: item.id,
        label: item.ten || item.tenMauSac,
      }))
    }

    if (sizesRes.success) {
      sizeOptions.value = sizesRes.data.map((item: any) => ({
        value: item.id,
        label: item.ten || item.tenKichThuoc,
      }))
    }

    if (materialsRes.success) {
      materialOptions.value = materialsRes.data.map((item: any) => ({
        value: item.id,
        label: item.ten || item.tenChatLieu,
      }))
    }

    if (solesRes.success) {
      soleOptions.value = solesRes.data.map((item: any) => ({
        value: item.id,
        label: item.ten || item.tenDeGiay,
      }))
    }

    if (weightsRes.success) {
      weightOptions.value = weightsRes.data.map((item: any) => ({
        value: item.id,
        label: item.ten || item.tenTrongLuong,
      }))
    }
  } catch (error) {
    // Error loading attribute options
  } finally {
    attributesLoading.value = false
  }
}

// Lifecycle
onMounted(async () => {
  if (variantId) {
    // Load attributes first, then variant data
    await loadAttributeOptions()
    await loadVariantData()
  } else {
    Message.error('ID biến thể không hợp lệ')
    goBack()
  }
})
</script>

<style scoped>
.variant-update-page {
  padding: 16px 20px;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.error-container {
  margin-top: 40px;
}

.header-card {
  margin-bottom: 16px;
}

.variant-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.variant-image {
  flex-shrink: 0;
}

.variant-info h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
}

.variant-code {
  margin: 0;
  color: #666;
}

.form-card {
  margin-bottom: 16px;
}

.action-card {
  margin-bottom: 16px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
}

:deep(.arco-form-item-label-col > .arco-form-item-label) {
  font-weight: 600;
}

:deep(.arco-input-number) {
  width: 100%;
}

/* Custom required field styling */
.required-field::after {
  content: ' *' !important;
  color: #f53f3f !important;
  font-weight: bold !important;
}
.images-card {
  margin-bottom: 16px;
}

.images-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
  margin-top: 12px;
}

/* Image upload loading overlay - styled like ThemSanPham */
.image-upload-loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  backdrop-filter: blur(4px);
}

.image-upload-loading-content {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  text-align: center;
  max-width: 400px;
  width: 90%;
}

.image-upload-loading-text {
  margin-top: 20px;
}

.image-upload-loading-text h3 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.image-upload-loading-text p {
  margin: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.image-item-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.image-item {
  position: relative;
  border-radius: 6px;
  overflow: hidden;
  border: 2px solid #e5e5e5;
  cursor: pointer;
  transition: all 0.3s ease;
  width: 120px;
  height: 120px;
}

.image-item:hover {
  border-color: #1890ff;
}

.image-item.image-selected {
  border-color: #1890ff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.2);
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-add-item {
  position: relative;
  border-radius: 6px;
  overflow: hidden;
  border: 2px dashed #d9d9d9;
  cursor: pointer;
  transition: all 0.3s ease;
  width: 120px;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
}

.image-add-item:hover {
  border-color: #1890ff;
  background-color: #f5f5f5;
}

.add-icon {
  font-size: 32px;
  color: #1890ff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-item:hover .image-overlay {
  opacity: 1;
}

.image-actions {
  display: flex;
  gap: 8px;
}

.icon-button {
  color: #ffffff;
  background: transparent;
  border: none;
  padding: 4px 8px !important;
  font-size: 18px;
  transition: all 0.3s ease;
}

.icon-button:hover {
  color: #1890ff;
  transform: scale(1.15);
}

.image-index {
  font-size: 12px;
  color: #666;
  text-align: center;
}
</style>
