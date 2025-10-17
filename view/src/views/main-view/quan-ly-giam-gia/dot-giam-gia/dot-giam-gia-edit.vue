<template>
  <div class="promotion-edit-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Page Header -->
    <a-card class="header-card">
      <div class="page-header">
        <div>
          <h2 class="page-title">Chỉnh sửa đợt giảm giá</h2>
          <p class="page-description">Cập nhật thông tin đợt giảm giá</p>
        </div>
        <a-button @click="goBack">
          <template #icon>
            <icon-left />
          </template>
          Quay lại
        </a-button>
      </div>
    </a-card>

    <!-- Main Content -->
    <a-row :gutter="[16, 16]">
      <!-- Left Column: Form -->
      <a-col :span="12">
        <a-card title="Thông tin đợt giảm giá">
          <a-form ref="promotionEditFormRef" :model="promotionEditForm" :rules="promotionEditRules" layout="vertical">
            <a-form-item field="code" label="Mã đợt giảm giá">
              <a-input v-model="promotionEditForm.code" placeholder="Nhập mã đợt giảm giá" allow-clear />
            </a-form-item>
            <a-form-item field="name" label="Tên đợt giảm giá">
              <a-input v-model="promotionEditForm.name" placeholder="Nhập tên đợt giảm giá" allow-clear />
            </a-form-item>
            <a-form-item field="discountValue" label="Giá trị giảm (%)">
              <a-input-number
                v-model="promotionEditForm.discountValue"
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
            <a-form-item field="dateRange" label="Thời gian áp dụng">
              <a-range-picker
                v-model="promotionEditForm.dateRange"
                :show-time="true"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="DD/MM/YYYY HH:mm"
                allow-clear
                style="width: 100%"
              />
            </a-form-item>
            <a-form-item field="active" label="Trạng thái">
              <a-switch v-model="promotionEditForm.active" checked-children="Hoạt động" un-checked-children="Không hoạt động" />
            </a-form-item>
            <a-form-item field="lyDoThayDoi" label="Lý do thay đổi">
              <a-textarea
                v-model="promotionEditForm.lyDoThayDoi"
                placeholder="Nhập lý do thay đổi..."
                allow-clear
                :max-length="500"
                :auto-size="{ minRows: 3, maxRows: 6 }"
              />
            </a-form-item>
          </a-form>

          <!-- Action Buttons -->
          <div class="action-buttons">
            <a-space>
              <a-button @click="goBack">Hủy</a-button>
              <a-button type="primary" @click="handleSubmit" :loading="submitting">Cập nhật</a-button>
            </a-space>
          </div>
        </a-card>
      </a-col>

      <!-- Right Column: Product Selection -->
      <a-col :span="12">
        <a-card title="Chọn sản phẩm áp dụng">
          <div class="product-selection-section">
            <a-input-search v-model="productSearchQuery" placeholder="Tìm kiếm sản phẩm..." allow-clear style="margin-bottom: 12px" />

            <div style="margin-bottom: 12px; display: flex; gap: 8px">
              <a-button size="small" @click="selectAllProductsEdit">
                <template #icon>
                  <icon-plus />
                </template>
                Chọn tất cả
              </a-button>
              <a-button size="small" @click="deselectAllProductsEdit">
                <template #icon>
                  <icon-delete />
                </template>
                Bỏ chọn tất cả
              </a-button>
            </div>

            <a-table
              row-key="id"
              :columns="productColumnsWithCheckbox"
              :data="filteredProductsEdit"
              :pagination="productPagination"
              :loading="productOptionsLoading"
              :scroll="{ y: 400 }"
              size="small"
              :bordered="{ cell: true }"
            >
              <template #selectHeader>
                <a-checkbox
                  :model-value="isAllProductsSelectedEdit"
                  :indeterminate="isSomeProductsSelectedEdit && !isAllProductsSelectedEdit"
                  @change="toggleAllProductsEdit"
                />
              </template>
              <template #select="{ record }">
                <a-checkbox
                  :model-value="promotionEditForm.selectedProducts.includes(record.id)"
                  @change="() => toggleProductSelectionEdit(record.id)"
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
              <strong>{{ promotionEditForm.selectedProducts.length }}</strong>
              sản phẩm
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- Confirmation Modal -->
    <a-modal
      v-model:visible="editConfirmVisible"
      title="Xác nhận cập nhật"
      :confirm-loading="submitting"
      width="480px"
      ok-text="Xác nhận"
      cancel-text="Hủy"
      @ok="submitPromotionEdit"
      @cancel="editConfirmVisible = false"
    >
      <p>
        Bạn chắc chắn muốn cập nhật đợt giảm giá
        <strong>{{ promotionEditForm.name }}</strong>
        ?
      </p>
      <p class="modal-note">
        Lý do thay đổi:
        <strong>{{ promotionEditForm.lyDoThayDoi }}</strong>
      </p>
      <p class="modal-note">Thay đổi sẽ được ghi lại vào lịch sử.</p>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import {
  updatePromotionCampaign,
  fetchPromotionProductDetails,
  createPromotionProductDetailsBatch,
  type PromotionProductDetailApiModel,
} from '@/api/discount-management'
import { getBienTheSanPhamList, type BienTheSanPham } from '@/api/san-pham'
import type { FormInstance, FormRules } from '@arco-design/web-vue/es/form'
import { IconPlus, IconDelete, IconLeft, IconImage } from '@arco-design/web-vue/es/icon'
import axios from 'axios'

// Router
const router = useRouter()
const route = useRoute()
const { breadcrumbItems } = useBreadcrumb()

// Get promotion ID from route params
const promotionId = computed(() => Number(route.params.id))

// Loading state
const loading = ref(false)
const submitting = ref(false)
const editConfirmVisible = ref(false)

// Product selection state
const productOptions = ref<BienTheSanPham[]>([])
const productOptionsLoading = ref(false)
const originalProductIds = ref<number[]>([])

const promotionEditFormRef = ref<FormInstance>()
const productSearchQuery = ref('')
const promotionEditForm = reactive({
  code: '',
  name: '',
  discountValue: 10,
  dateRange: [] as string[],
  active: true,
  selectedProducts: [] as number[],
  applyToProducts: true,
  lyDoThayDoi: '',
})

// Store original values for change detection
const originalPromotionEditForm = reactive({
  code: '',
  name: '',
  discountValue: 10,
  dateRange: [] as string[],
  active: true,
  selectedProducts: [] as number[],
  applyToProducts: true,
})

const promotionEditRules: FormRules = {
  name: [{ required: true, message: 'Vui lòng nhập tên đợt giảm giá' }],
  discountValue: [
    { required: true, message: 'Vui lòng nhập giá trị giảm' },
    {
      validator: (value: number, callback: (msg?: string) => void) => {
        if (value === undefined || value === null || Number.isNaN(Number(value))) {
          callback('Vui lòng nhập giá trị giảm')
          return
        }
        if (value <= 0) {
          callback('Giá trị giảm phải lớn hơn 0')
          return
        }
        if (value > 100) {
          callback('Giá trị giảm tối đa 100%')
          return
        }
        callback()
      },
    },
  ],
  dateRange: [{ required: true, type: 'array', message: 'Vui lòng chọn thời gian áp dụng' }],
  lyDoThayDoi: [{ required: true, message: 'Vui lòng nhập lý do thay đổi' }],
}

// Product columns
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

const filteredProductsEdit = computed(() => {
  if (!productSearchQuery.value) {
    return productOptions.value
  }
  const query = productSearchQuery.value.toLowerCase()
  return productOptions.value.filter(
    (product) => product.tenSanPham?.toLowerCase().includes(query) || product.maChiTietSanPham?.toLowerCase().includes(query)
  )
})

const productPagination = computed(() => ({
  pageSize: 5,
  showTotal: true,
  showPageSize: false,
}))

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

const isAllProductsSelectedEdit = computed(() => {
  if (filteredProductsEdit.value.length === 0) return false
  return filteredProductsEdit.value.every((product) => promotionEditForm.selectedProducts.includes(product.id))
})

const isSomeProductsSelectedEdit = computed(() => {
  return promotionEditForm.selectedProducts.length > 0
})

const toggleProductSelectionEdit = (productId: number) => {
  const index = promotionEditForm.selectedProducts.indexOf(productId)
  if (index > -1) {
    promotionEditForm.selectedProducts.splice(index, 1)
  } else {
    promotionEditForm.selectedProducts.push(productId)
  }
}

const toggleAllProductsEdit = () => {
  if (isAllProductsSelectedEdit.value) {
    filteredProductsEdit.value.forEach((product) => {
      const index = promotionEditForm.selectedProducts.indexOf(product.id)
      if (index > -1) {
        promotionEditForm.selectedProducts.splice(index, 1)
      }
    })
  } else {
    filteredProductsEdit.value.forEach((product) => {
      if (!promotionEditForm.selectedProducts.includes(product.id)) {
        promotionEditForm.selectedProducts.push(product.id)
      }
    })
  }
}

const selectAllProductsEdit = () => {
  filteredProductsEdit.value.forEach((product) => {
    if (!promotionEditForm.selectedProducts.includes(product.id)) {
      promotionEditForm.selectedProducts.push(product.id)
    }
  })
}

const deselectAllProductsEdit = () => {
  filteredProductsEdit.value.forEach((product) => {
    const index = promotionEditForm.selectedProducts.indexOf(product.id)
    if (index > -1) {
      promotionEditForm.selectedProducts.splice(index, 1)
    }
  })
}

// Methods
const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.style.display = 'none'
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
  } catch {
    Message.error('Không thể tải danh sách sản phẩm')
    productOptions.value = []
  } finally {
    productOptionsLoading.value = false
  }
}

const loadProductsForPromotion = async (campaignId: number) => {
  try {
    const productDetails = await fetchPromotionProductDetails(campaignId)
    const productIds = productDetails.map((detail: PromotionProductDetailApiModel) => detail.idChiTietSanPham)
    promotionEditForm.selectedProducts = productIds
    originalProductIds.value = [...productIds]
  } catch {
    promotionEditForm.selectedProducts = []
    originalProductIds.value = []
  }
}

const goBack = () => {
  router.push({ name: 'QuanLyDotGiamGia' })
}

// Load promotion data
const loadPromotionData = async () => {
  loading.value = true
  try {
    const response = await axios.get(`/api/dot-giam-gia-management/detail/${promotionId.value}`)
    const promotion = response.data.data || response.data

    promotionEditForm.code = promotion.maDotGiamGia ?? ''
    promotionEditForm.name = promotion.tenDotGiamGia ?? ''
    promotionEditForm.discountValue = Number(promotion.giaTriGiamGia ?? 0)
    promotionEditForm.active = Boolean(promotion.trangThai)
    promotionEditForm.dateRange = [promotion.ngayBatDau ?? '', promotion.ngayKetThuc ?? ''].filter(Boolean) as string[]

    // Load products for this promotion
    await loadProductsForPromotion(promotionId.value)

    // Set applyToProducts based on whether there are products
    promotionEditForm.applyToProducts = promotionEditForm.selectedProducts.length > 0

    // Store original values for change detection
    Object.assign(originalPromotionEditForm, {
      code: promotionEditForm.code,
      name: promotionEditForm.name,
      discountValue: promotionEditForm.discountValue,
      active: promotionEditForm.active,
      dateRange: [...promotionEditForm.dateRange],
      selectedProducts: [...promotionEditForm.selectedProducts],
      applyToProducts: promotionEditForm.applyToProducts,
    })
  } catch {
    Message.error('Không thể tải thông tin đợt giảm giá')
    goBack()
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (submitting.value) return

  const form = promotionEditFormRef.value
  if (!form) return

  try {
    await form.validate()
  } catch {
    Message.error('Vui lòng kiểm tra lại các trường thông tin')
    return
  }

  // Check if any changes were made
  const hasChanges =
    promotionEditForm.name !== originalPromotionEditForm.name ||
    promotionEditForm.discountValue !== originalPromotionEditForm.discountValue ||
    promotionEditForm.active !== originalPromotionEditForm.active ||
    promotionEditForm.dateRange[0] !== originalPromotionEditForm.dateRange[0] ||
    promotionEditForm.dateRange[1] !== originalPromotionEditForm.dateRange[1] ||
    promotionEditForm.applyToProducts !== originalPromotionEditForm.applyToProducts ||
    JSON.stringify([...promotionEditForm.selectedProducts].sort()) !==
      JSON.stringify([...originalPromotionEditForm.selectedProducts].sort())

  if (!hasChanges) {
    Message.warning('Không có thay đổi nào để cập nhật')
    return
  }

  // Validation passed, show confirmation modal
  editConfirmVisible.value = true
}

const submitPromotionEdit = async () => {
  const [startDate, endDate] = promotionEditForm.dateRange
  const payload = {
    tenDotGiamGia: promotionEditForm.name.trim(),
    giaTriGiamGia: Number(promotionEditForm.discountValue),
    ngayBatDau: startDate,
    ngayKetThuc: endDate,
    trangThai: promotionEditForm.active,
    deleted: false,
    lyDoThayDoi: promotionEditForm.lyDoThayDoi.trim(),
  }

  submitting.value = true
  try {
    // Step 1: Update promotion campaign
    await updatePromotionCampaign(promotionId.value, payload)

    // Step 2: Update product associations if changed
    const currentProducts = new Set(promotionEditForm.selectedProducts)
    const originalProducts = new Set(originalProductIds.value)

    // Find products that need to be added
    const productsToAdd = Array.from(currentProducts).filter((id) => !originalProducts.has(id))

    if (productsToAdd.length > 0) {
      const productDetailPayloads = productsToAdd.map((productId) => ({
        idDotGiamGia: promotionId.value,
        idChiTietSanPham: productId,
        deleted: false,
      }))
      await createPromotionProductDetailsBatch(productDetailPayloads)
    }

    Message.success('Cập nhật đợt giảm giá thành công')
    editConfirmVisible.value = false
    goBack()
  } catch {
    Message.error('Không thể cập nhật đợt giảm giá')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await fetchProducts()
  await loadPromotionData()
})
</script>

<style scoped>
.promotion-edit-page {
  padding: 0 20px 20px 20px;
}

.header-card {
  margin-bottom: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-1);
  margin: 0 0 4px 0;
}

.page-description {
  font-size: 14px;
  color: var(--color-text-3);
  margin: 0;
}

.action-buttons {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--color-border-2);
  display: flex;
  justify-content: flex-end;
}

.product-selection-section {
  border: 1px solid var(--color-border-2);
  border-radius: 8px;
  padding: 16px;
  background: var(--color-bg-2);
}

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

.modal-note {
  margin-top: 4px;
  color: var(--color-text-3);
}
</style>
