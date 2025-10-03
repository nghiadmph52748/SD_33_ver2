<template>
  <div class="variant-update-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

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
              :src="Array.isArray(variant.anhSanPham) && variant.anhSanPham.length > 0 ? variant.anhSanPham[0] : '/default-product.png'"
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
                  :precision="0"
                  placeholder="Nhập giá bán"
                  style="width: 100%"
                  :formatter="(value) => formatCurrencyInput(value)"
                  :parser="(value) => parseCurrencyInput(value)"
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
                <a-radio-group v-model="formData.trangThai">
                  <a-radio :value="true">Đang bán</a-radio>
                  <a-radio :value="false">Tạm ngưng bán</a-radio>
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

        <!-- Action Buttons -->
        <a-card class="action-card">
          <div class="action-buttons">
            <a-button size="large" @click="handleReset">
              <template #icon>
                <icon-refresh />
              </template>
              Đặt lại
            </a-button>
            <a-button type="primary" size="large" :loading="updating" @click="handleUpdate">
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { useUserStore } from '@/store'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { IconArrowLeft, IconEye, IconRefresh, IconSave } from '@arco-design/web-vue/es/icon'
import {
  getBienTheById,
  updateBienThe,
  getSanPhamOptions,
  getMauSac,
  getKichThuoc,
  getChatLieu,
  getDeGiay,
  getTrongLuong,
  type BienTheSanPham,
  type SanPham,
} from '@/api/san-pham/bien-the'

// Router
const route = useRoute()
const router = useRouter()

// User store
const userStore = useUserStore()

// Breadcrumb
const { breadcrumbItems } = useBreadcrumb()

// State
const initialLoading = ref(true)
const updating = ref(false)
const attributesLoading = ref(false)
const variant = ref<BienTheSanPham | null>(null)
const formRef = ref()

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
const formatCurrencyInput = (value: number) => {
  if (!value) return ''
  return new Intl.NumberFormat('vi-VN').format(value)
}

const parseCurrencyInput = (value: string) => {
  return value.replace(/\$\s?|(,*)/g, '')
}

const goBack = () => {
  router.go(-1)
}

const viewDetail = () => {
  router.push(`/quan-ly-san-pham/bien-the/detail/${variantId}`)
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
        deleted: variant.value.deleted,
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
      // Populate form data
      formData.giaBan = variant.value.giaBan || 0
      formData.giaTriGiamGia = variant.value.giaTriGiamGia || 0
      formData.soLuong = variant.value.soLuong || 0
      formData.trangThai = variant.value.trangThai !== false

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
  padding: 0 20px 20px 20px;
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
</style>
