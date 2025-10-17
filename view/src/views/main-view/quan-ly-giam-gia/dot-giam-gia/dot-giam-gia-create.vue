<template>
  <div class="promotion-create-page">
    <Breadcrumb :items="breadcrumbItems" />

    <a-card class="promotion-card">
      <div class="page-header">
        <div>
          <h2 class="page-title">Tạo đợt giảm giá</h2>
          <p class="page-description">Điền thông tin chi tiết cho đợt giảm giá mới trước khi lưu.</p>
        </div>
        <a-space>
          <a-button @click="goBack">Quay lại</a-button>
          <a-button type="primary" :loading="confirmSaveSubmitting" @click="handleSaveClick">Lưu giảm giá</a-button>
        </a-space>
      </div>

      <a-form ref="formRef" :model="formState" :rules="rules" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="name" label="Tên đợt giảm giá">
              <a-input v-model="formState.name" placeholder="Nhập tên đợt giảm giá" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="code" label="Mã">
              <a-input v-model="formState.code" placeholder="Nhập mã đợt giảm giá" allow-clear />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="discountValue" label="Giá trị giảm (%)">
              <a-input-number
                v-model="formState.discountValue"
                :min="0.01"
                :max="100"
                :step="0.01"
                :precision="2"
                placeholder="Nhập giá trị giảm..."
                style="width: 100%"
              >
                <template #suffix>%</template>
              </a-input-number>
              <div style="margin-top: 4px; font-size: 12px; color: var(--color-text-3)">Giá trị từ 0.01-100</div>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="dateRange" label="Thời gian áp dụng">
              <a-range-picker
                v-model="formState.dateRange"
                :show-time="true"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="DD/MM/YYYY HH:mm"
                allow-clear
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Product Selection Section -->
        <div class="product-selection-section">
          <a-divider style="margin: 16px 0" />
          <div style="font-weight: 600; margin-bottom: 12px">Chọn sản phẩm áp dụng</div>

          <a-input-search v-model="productSearchQuery" placeholder="Tìm kiếm sản phẩm..." allow-clear style="margin-bottom: 12px" />

          <div style="margin-bottom: 12px; display: flex; gap: 8px">
            <a-button size="small" @click="selectAllProducts">
              <template #icon>
                <icon-plus />
              </template>
              Chọn tất cả
            </a-button>
            <a-button size="small" @click="deselectAllProducts">
              <template #icon>
                <icon-delete />
              </template>
              Bỏ chọn tất cả
            </a-button>
          </div>

          <a-table
            row-key="id"
            :columns="productColumnsWithCheckbox"
            :data="filteredProducts"
            :pagination="productPagination"
            :loading="productOptionsLoading"
            :scroll="{ y: 300 }"
            size="small"
            :bordered="{ cell: true }"
          >
            <template #selectHeader>
              <a-checkbox
                :model-value="isAllProductsSelected"
                :indeterminate="isSomeProductsSelected && !isAllProductsSelected"
                @change="toggleAllProducts"
              />
            </template>
            <template #select="{ record }">
              <a-checkbox
                :model-value="formState.selectedProducts.includes(record.id)"
                @change="() => toggleProductSelection(record.id)"
              />
            </template>
            <template #anhSanPham="{ record }">
              <div class="product-image-cell">
                <img
                  v-if="record.anhSanPham && record.anhSanPham.length > 0"
                  :src="record.anhSanPham[0]"
                  :alt="record.tenSanPham"
                  class="product-thumbnail"
                  @error="handleImageError"
                />
                <div v-else class="product-image-placeholder">
                  <icon-image :size="24" style="color: var(--color-text-4)" />
                </div>
              </div>
            </template>
            <template #tenSanPham="{ record }">
              <div style="display: flex; align-items: center; gap: 8px">
                <span>{{ record.tenSanPham }}</span>
              </div>
            </template>
            <template #giaBan="{ record }">
              <span>{{ formatCurrency(record.giaBan || 0) }}</span>
            </template>
          </a-table>

          <div style="margin-top: 8px; font-size: 12px; color: var(--color-text-3)">
            Đã chọn:
            <strong>{{ formState.selectedProducts.length }}</strong>
            sản phẩm
          </div>
        </div>
        <div class="form-footer">
          <a-space>
            <a-button @click="goBack">Hủy</a-button>
            <a-button type="primary" :loading="confirmSaveSubmitting" @click="handleSaveClick">Lưu giảm giá</a-button>
          </a-space>
        </div>
      </a-form>
    </a-card>

    <!-- Confirmation Modal -->
    <a-modal
      v-model:visible="confirmSaveVisible"
      title="Xác nhận tạo đợt giảm giá"
      :confirm-loading="confirmSaveSubmitting"
      @ok="confirmSave"
      @cancel="confirmSaveVisible = false"
      ok-text="Xác nhận"
      cancel-text="Hủy"
      width="560px"
    >
      <p style="margin-bottom: 16px; color: var(--color-text-2)">Vui lòng kiểm tra lại thông tin trước khi lưu:</p>
      <a-descriptions :column="1" bordered>
        <a-descriptions-item label="Tên đợt giảm giá">
          {{ formState.name }}
        </a-descriptions-item>
        <a-descriptions-item label="Mã">
          {{ formState.code }}
        </a-descriptions-item>
        <a-descriptions-item label="Giá trị giảm">{{ formState.discountValue }}%</a-descriptions-item>
        <a-descriptions-item label="Thời gian áp dụng">
          {{ formatDateRange(formState.dateRange) }}
        </a-descriptions-item>
        <a-descriptions-item label="Số sản phẩm áp dụng">
          {{ formState.selectedProducts.length }} sản phẩm
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, watch, computed } from 'vue'
import type { FormInstance, FormRules } from '@arco-design/web-vue/es/form'
import { Message } from '@arco-design/web-vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { useRouter } from 'vue-router'
import {
  createPromotionCampaign,
  fetchPromotionCampaigns,
  createPromotionProductDetailsBatch,
  type PromotionApiModel,
} from '@/api/discount-management'
import { getBienTheSanPhamList, type BienTheSanPham } from '@/api/san-pham'
import { IconPlus, IconDelete, IconImage } from '@arco-design/web-vue/es/icon'

const { breadcrumbItems } = useBreadcrumb()
const router = useRouter()

const formRef = ref<FormInstance>()
const confirmSaveVisible = ref(false)
const confirmSaveSubmitting = ref(false)
const productOptions = ref<BienTheSanPham[]>([])
const productOptionsLoading = ref(false)
const productSearchQuery = ref('')

const formState = reactive({
  name: '',
  code: '',
  discountValue: 10,
  dateRange: [] as string[],
  selectedProducts: [] as number[],
  applyToProducts: true,
})

// Real-time validation for discount value
watch(
  () => formState.discountValue,
  (newValue) => {
    if (newValue !== undefined && newValue !== null && newValue > 100) {
      Message.warning('Giá trị giảm không được vượt quá 100%')
      formState.discountValue = 100
    }
  }
)

const rules: FormRules = {
  code: [{ required: true, message: 'Vui lòng nhập mã đợt giảm giá' }],
  name: [{ required: true, message: 'Vui lòng nhập tên đợt giảm giá' }],
  discountValue: [
    { required: true, message: 'Vui lòng nhập giá trị giảm' },
    {
      validator: (value, callback) => {
        if (value === undefined || value === null || value === '') {
          callback('Vui lòng nhập giá trị giảm')
          return
        }
        const numeric = Number(value)
        if (Number.isNaN(numeric) || numeric <= 0) {
          callback('Giá trị giảm phải lớn hơn 0')
          return
        }
        if (numeric > 100) {
          callback('Giá trị giảm tối đa 100%')
          return
        }
        callback()
      },
    },
  ],
  dateRange: [
    { required: true, type: 'array', message: 'Vui lòng chọn khoảng thời gian' },
    {
      validator: (value: string[], callback) => {
        if (!Array.isArray(value) || value.length !== 2) {
          callback('Vui lòng chọn khoảng thời gian')
          return
        }
        const [start, end] = value
        if (!start || !end) {
          callback('Vui lòng chọn khoảng thời gian')
          return
        }
        if (new Date(start) > new Date(end)) {
          callback('Ngày kết thúc phải sau ngày bắt đầu')
          return
        }
        callback()
      },
    },
  ],
}

const generateNextCode = async () => {
  try {
    const promotions = await fetchPromotionCampaigns()

    if (!promotions || promotions.length === 0) {
      return 'DGG000001'
    }

    // Extract all codes that match the DGG pattern
    const dggCodes = promotions
      .map((p) => p.maDotGiamGia)
      .filter((code) => code && code.startsWith('DGG'))
      .map((code) => {
        const numPart = code.substring(3)
        return parseInt(numPart, 10)
      })
      .filter((num) => !Number.isNaN(num))

    if (dggCodes.length === 0) {
      return 'DGG000001'
    }

    // Find the maximum number and increment
    const maxNum = Math.max(...dggCodes)
    const nextNum = maxNum + 1

    // Format with leading zeros (6 digits)
    return `DGG${nextNum.toString().padStart(6, '0')}`
  } catch {
    // Fallback to DGG000001 if fetch fails
    return 'DGG000001'
  }
}

// Computed properties for product filtering and selection
const filteredProducts = computed(() => {
  if (!productSearchQuery.value) {
    return productOptions.value
  }
  const query = productSearchQuery.value.toLowerCase()
  return productOptions.value.filter(
    (product) =>
      product.tenSanPham?.toLowerCase().includes(query) ||
      product.maChiTietSanPham?.toLowerCase().includes(query)
  )
})

const productPagination = computed(() => ({
  pageSize: 5,
  showTotal: true,
  showPageSize: false,
}))

const productColumns = [
  {
    title: 'Ảnh',
    dataIndex: 'anhSanPham',
    slotName: 'anhSanPham',
    width: 80,
    align: 'center' as const,
  },
  {
    title: 'Tên sản phẩm',
    dataIndex: 'tenSanPham',
    slotName: 'tenSanPham',
    ellipsis: true,
    tooltip: true,
  },
  {
    title: 'Mã SP',
    dataIndex: 'maChiTietSanPham',
    width: 120,
  },
  {
    title: 'Giá bán',
    dataIndex: 'giaBan',
    slotName: 'giaBan',
    width: 150,
    align: 'right' as const,
  },
]

const productColumnsWithCheckbox = computed(() => [
  {
    title: '',
    dataIndex: 'select',
    slotName: 'select',
    width: 50,
    align: 'center' as const,
  },
  ...productColumns,
])

const isAllProductsSelected = computed(() => {
  if (filteredProducts.value.length === 0) return false
  return filteredProducts.value.every((product) => formState.selectedProducts.includes(product.id))
})

const isSomeProductsSelected = computed(() => {
  return formState.selectedProducts.length > 0
})

const toggleProductSelection = (productId: number) => {
  const index = formState.selectedProducts.indexOf(productId)
  if (index > -1) {
    formState.selectedProducts.splice(index, 1)
  } else {
    formState.selectedProducts.push(productId)
  }
}

const toggleAllProducts = () => {
  if (isAllProductsSelected.value) {
    // Deselect all filtered products
    filteredProducts.value.forEach((product) => {
      const index = formState.selectedProducts.indexOf(product.id)
      if (index > -1) {
        formState.selectedProducts.splice(index, 1)
      }
    })
  } else {
    // Select all filtered products
    filteredProducts.value.forEach((product) => {
      if (!formState.selectedProducts.includes(product.id)) {
        formState.selectedProducts.push(product.id)
      }
    })
  }
}

const selectAllProducts = () => {
  filteredProducts.value.forEach((product) => {
    if (!formState.selectedProducts.includes(product.id)) {
      formState.selectedProducts.push(product.id)
    }
  })
}

const deselectAllProducts = () => {
  filteredProducts.value.forEach((product) => {
    const index = formState.selectedProducts.indexOf(product.id)
    if (index > -1) {
      formState.selectedProducts.splice(index, 1)
    }
  })
}

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.style.display = 'none'
  // Show placeholder instead
  const placeholder = img.parentElement?.querySelector('.product-image-placeholder')
  if (placeholder) {
    ;(placeholder as HTMLElement).style.display = 'flex'
  }
}

const fetchProducts = async () => {
  productOptionsLoading.value = true
  try {
    const response = await getBienTheSanPhamList(0)
    productOptions.value = response.data || []
  } catch (error) {
    Message.error('Không thể tải danh sách sản phẩm')
    productOptions.value = []
  } finally {
    productOptionsLoading.value = false
  }
}


onMounted(async () => {
  formState.code = await generateNextCode()
  // Load products since applyToProducts is true by default
  await fetchProducts()
})

const goBack = () => {
  router.back()
}

const formatDateRange = (dateRange: string[]) => {
  if (!dateRange || dateRange.length !== 2) return ''
  const [start, end] = dateRange
  if (!start || !end) return ''

  const formatDateTime = (dateStr: string) => {
    const date = new Date(dateStr)
    return date.toLocaleString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    })
  }

  return `${formatDateTime(start)} - ${formatDateTime(end)}`
}

const handleSaveClick = async () => {
  const form = formRef.value
  if (!form) return

  try {
    const errors = await form.validate()
    if (errors) {
      return
    }
  } catch {
    // Validation failed, errors are already displayed by the form
    return
  }

  // Validate product selection
  if (formState.selectedProducts.length === 0) {
    Message.error('Vui lòng chọn ít nhất một sản phẩm')
    return
  }

  // All validations passed, show confirmation modal
  confirmSaveVisible.value = true
}

const calculateStatus = (startDate: string, endDate: string): boolean => {
  const now = new Date()
  const start = new Date(startDate)
  const end = new Date(endDate)

  // Status is true (active) if current datetime is within the datetime range
  return now >= start && now <= end
}

const confirmSave = async () => {
  if (confirmSaveSubmitting.value) return

  const [startDate, endDate] = formState.dateRange

  const payload = {
    maDotGiamGia: formState.code,
    tenDotGiamGia: formState.name.trim(),
    giaTriGiamGia: Number(formState.discountValue),
    ngayBatDau: startDate,
    ngayKetThuc: endDate,
    trangThai: calculateStatus(startDate, endDate),
    deleted: false,
  }

  confirmSaveSubmitting.value = true
  try {
    // Step 1: Create promotion campaign
    await createPromotionCampaign(payload)

    // Step 2: If products are selected, link them to the promotion
    if (formState.selectedProducts.length > 0) {
      // Fetch the newly created promotion to get its ID
      const promotions = await fetchPromotionCampaigns()
      const newPromotion = promotions.find((p: PromotionApiModel) => p.maDotGiamGia === formState.code)

      if (newPromotion) {
        const productDetailPayloads = formState.selectedProducts.map((productId) => ({
          idDotGiamGia: newPromotion.id,
          idChiTietSanPham: productId,
          deleted: false,
        }))

        await createPromotionProductDetailsBatch(productDetailPayloads)
      }
    }

    Message.success('Tạo đợt giảm giá thành công')
    confirmSaveVisible.value = false
    router.push({ name: 'QuanLyDotGiamGia' })
  } catch (error) {
    Message.error((error as Error).message || 'Không thể tạo đợt giảm giá')
  } finally {
    confirmSaveSubmitting.value = false
  }
}
</script>

<style scoped>
.promotion-create-page {
  padding: 0 20px 20px 20px;
}

.promotion-card {
  margin-top: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  gap: 16px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-1);
}

.page-description {
  margin: 4px 0 0 0;
  color: var(--color-text-3);
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* Product Selection Section Styles */
.product-selection-section {
  border: 1px solid var(--color-border-2);
  border-radius: 8px;
  padding: 16px;
  background: var(--color-bg-2);
  margin-top: 16px;
}

.product-selection-section :deep(.arco-table) {
  border-radius: 4px;
}

.product-selection-section :deep(.arco-table-container) {
  border: 1px solid var(--color-border-2);
  border-radius: 4px;
}

.product-selection-section :deep(.arco-table-th) {
  background-color: var(--color-fill-2);
  font-weight: 600;
}

.product-selection-section :deep(.arco-table-td) {
  border-bottom: 1px solid var(--color-border-2);
}

.product-selection-section :deep(.arco-table-tr:hover) {
  background-color: var(--color-fill-1);
}

.product-selection-section :deep(.arco-checkbox) {
  pointer-events: auto !important;
  cursor: pointer !important;
}

.product-selection-section :deep(.arco-checkbox-icon) {
  pointer-events: auto !important;
}

.product-selection-section :deep(.arco-table-cell) {
  pointer-events: auto !important;
}

.product-selection-section :deep(.arco-table-td) {
  position: relative;
  z-index: 1;
}

.product-selection-section :deep(.arco-table-th .arco-checkbox) {
  pointer-events: auto !important;
}

/* Product Image Styles */
.product-image-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.product-thumbnail {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid var(--color-border-2);
}

.product-image-placeholder {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-fill-2);
  border-radius: 4px;
  border: 1px dashed var(--color-border-3);
}
</style>
