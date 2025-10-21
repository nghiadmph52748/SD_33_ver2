<template>
  <div class="promotion-edit-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Back Button -->
    <div class="header-actions">
      <a-button @click="goBack">
        <template #icon>
          <icon-left />
        </template>
        {{ t('discount.common.back') }}
      </a-button>
    </div>

    <!-- Main Content -->
    <a-row :gutter="[16, 16]">
      <!-- Left Column: Form -->
      <a-col :span="8">
        <a-card :title="t('discount.campaign.info')">
          <a-form ref="promotionEditFormRef" :model="promotionEditForm" :rules="promotionEditRules" layout="vertical">
            <a-form-item field="code" :label="t('discount.campaign.code')">
              <a-input v-model="promotionEditForm.code" :placeholder="t('discount.campaign.codePlaceholder')" allow-clear />
            </a-form-item>
            <a-form-item field="name" :label="t('discount.campaign.name')">
              <a-input v-model="promotionEditForm.name" :placeholder="t('discount.campaign.namePlaceholder')" allow-clear />
            </a-form-item>
            <a-form-item field="discountValue" :label="t('discount.campaign.discountValue')">
              <a-input-number
                v-model="promotionEditForm.discountValue"
                :min="1"
                :max="100"
                :step="0.01"
                :precision="2"
                :placeholder="t('discount.campaign.discountValuePlaceholder')"
                style="width: 100%"
              >
                <template #suffix>%</template>
              </a-input-number>
            </a-form-item>
            <a-form-item field="dateRange" :label="t('discount.campaign.validityPeriod')">
              <a-range-picker
                v-model="promotionEditForm.dateRange"
                :show-time="true"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="DD/MM/YYYY HH:mm"
                allow-clear
                :disabled-date="disablePastDates"
                style="width: 100%"
              />
            </a-form-item>
            <a-form-item field="active" :label="t('discount.campaign.status')">
              <a-radio-group v-model="promotionEditForm.active" type="button">
                <a-radio :value="true">{{ t('discount.campaign.status.active') }}</a-radio>
                <a-radio :value="false">{{ t('discount.campaign.status.inactive') }}</a-radio>
              </a-radio-group>
            </a-form-item>
            <a-form-item field="lyDoThayDoi" :label="t('discount.campaign.changeReason')">
              <a-textarea
                v-model="promotionEditForm.lyDoThayDoi"
                :placeholder="t('discount.campaign.changeReasonPlaceholder')"
                allow-clear
                :max-length="500"
                :auto-size="{ minRows: 3, maxRows: 6 }"
              />
            </a-form-item>
          </a-form>

          <!-- Action Buttons -->
          <PageActions>
            <a-space>
              <a-button @click="goBack">{{ t('discount.common.cancel') }}</a-button>
              <a-button type="primary" @click="handleSubmit" :loading="submitting">{{ t('discount.common.update') }}</a-button>
            </a-space>
          </PageActions>
        </a-card>
      </a-col>

      <!-- Right Column: Product Selection -->
      <a-col :span="16">
        <a-card :title="t('discount.campaign.selectProducts')">
          <div class="product-selection-section">
            <a-input-search v-model="productSearchQuery" :placeholder="t('discount.campaign.searchProducts')" allow-clear style="margin-bottom: 12px" />

            <a-table
              class="product-group-table"
              row-key="key"
              :columns="productGroupColumns"
              :data="filteredProductGroups"
              :pagination="productGroupPagination"
              :loading="productOptionsLoading"
              size="small"
              :bordered="{ cell: true }"
              :expandable="{
                expandedRowKeys: expandedRowKeys,
                onExpand: handleExpand,
                showExpandButtonColumn: false,
              }"
              @row-click="(record) => toggleExpanded(record.key)"
            >
              <template #thumbnail="{ record }">
                <div class="product-image-cell">
                  <img
                    v-if="record.thumbnail"
                    :src="record.thumbnail"
                    :alt="record.tenSanPham"
                    class="product-thumbnail"
                    @error="handleImageError"
                  />
                  <div v-else class="product-image-placeholder">
                    <icon-image :size="24" style="color: var(--color-text-4)" />
                  </div>
                </div>
              </template>
              <template #brand="{ record }">
                <span style="font-size: 13px; color: var(--color-text-2)">{{ record.tenNhaSanXuat || '--' }}</span>
              </template>
              <template #product="{ record }">
                <div class="product-info-cell">
                  <div class="product-name">{{ record.tenSanPham }}</div>
                  <div v-if="record.variants.length > 1" class="product-variant-meta">
                    {{ record.variants.length }} {{ t('discount.common.variants') }} •
                    <span style="color: var(--color-primary-light-4)">{{ getGroupSelectedVariantIdsEdit(record).length }} {{ t('discount.common.selected') }}</span>
                  </div>
                  <div v-else-if="record.variants.length === 1" class="product-variant-meta">
                    {{ buildVariantLabel(record.variants[0]) }}
                  </div>
                </div>
              </template>
              <template #variantSelect="{ record }">
                <div style="display: flex; align-items: center; justify-content: space-between; gap: 8px">
                  <div style="display: flex; align-items: center; gap: 8px; flex: 1">
                    <span style="font-size: 13px; color: var(--color-text-2)">
                      <strong>{{ getGroupSelectedVariantIdsEdit(record).length }}</strong>
                      / {{ record.variants.length }} {{ t('discount.common.selected') }}
                    </span>
                    <a-button
                      v-if="!isAllVariantsSelected(record)"
                      size="mini"
                      type="text"
                      @click.stop="selectAllVariantsEdit(record)"
                      style="padding: 0 4px; font-size: 12px"
                    >
                      {{ t('discount.common.selectAll') }}
                    </a-button>
                    <a-button
                      v-else
                      size="mini"
                      type="text"
                      @click.stop="deselectAllVariantsEdit(record)"
                      style="padding: 0 4px; font-size: 12px; color: var(--color-danger-light-4)"
                    >
                      {{ t('discount.common.deselectAll') }}
                    </a-button>
                  </div>
                  <icon-down v-if="!expandedRowKeys.includes(record.key)" :size="16" style="color: var(--color-text-3)" />
                  <icon-up v-else :size="16" style="color: var(--color-text-3)" />
                </div>
              </template>
              <template #expand-row="{ record }">
                <div class="variant-expansion">
                  <a-table
                    :columns="variantColumns"
                    :data="record.variants"
                    :pagination="false"
                    size="small"
                    :bordered="false"
                    row-key="id"
                    :row-class="(record) => (promotionEditForm.selectedProducts.includes(record.id) ? 'variant-row-selected' : '')"
                    @row-click="(record) => toggleVariant(record.id)"
                  >
                    <template #variant="{ record: variant }">
                      <div class="variant-info">
                        <div class="variant-name">
                          <span class="variant-label">{{ buildVariantLabel(variant) }}</span>
                          <a-tag v-if="variant.maChiTietSanPham" size="small" color="blue">{{ variant.maChiTietSanPham }}</a-tag>
                        </div>
                        <div class="variant-specs-inline">
                          <span v-if="variant.tenMauSac" class="spec-tag">
                            <icon-bg-colors :size="12" /> {{ variant.tenMauSac }}
                          </span>
                          <span v-if="variant.tenKichThuoc" class="spec-tag">
                            <icon-expand :size="12" /> {{ variant.tenKichThuoc }}
                          </span>
                          <span v-if="variant.tenChatLieu" class="spec-tag">
                            <icon-tag :size="12" /> {{ variant.tenChatLieu }}
                          </span>
                          <span v-if="variant.tenDeGiay" class="spec-tag">
                            <img :src="soleIcon" alt="sole" class="spec-icon-img" /> {{ variant.tenDeGiay }}
                          </span>
                          <span v-if="variant.tenTrongLuong" class="spec-tag">
                            <icon-nav :size="12" /> {{ variant.tenTrongLuong }}
                          </span>
                        </div>
                      </div>
                    </template>
                    <template #price="{ record: variant }">
                      <span class="variant-price">{{ formatCurrency(variant.giaBan || 0) }}</span>
                    </template>
                    <template #stock="{ record: variant }">
                      <span v-if="typeof variant.soLuong === 'number'" class="variant-stock">
                        <strong>{{ variant.soLuong }}</strong>
                      </span>
                      <span v-else class="variant-stock">--</span>
                    </template>
                  </a-table>
                </div>
              </template>
            </a-table>

            <div style="margin-top: 8px; font-size: 12px; color: var(--color-text-3)">
              {{ t('discount.common.selected') }}:
              <strong>{{ promotionEditForm.selectedProducts.length }}</strong>
              {{ t('discount.common.variants') }}
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- Confirmation Modal -->
    <a-modal
      v-model:visible="editConfirmVisible"
      :title="t('discount.campaign.editConfirmTitle')"
      :confirm-loading="submitting"
      width="700px"
      :ok-text="t('discount.common.confirm')"
      :cancel-text="t('discount.common.cancel')"
      @ok="submitPromotionEdit"
      @cancel="editConfirmVisible = false"
    >
      <p>
        {{ t('discount.campaign.editConfirmMessage', { name: promotionEditForm.name }) }}
      </p>
      <p class="modal-note">
        {{ t('discount.campaign.changeReason') }}:
        <strong>{{ promotionEditForm.lyDoThayDoi }}</strong>
      </p>
      <p class="modal-note">{{ t('discount.campaign.editConfirmNote') }}</p>

      <div v-if="promotionEditForm.selectedProducts.length > 0" style="margin-top: 16px">
        <a-divider orientation="left" style="margin: 16px 0 12px 0">{{ t('discount.campaign.productList') }} ({{ promotionEditForm.selectedProducts.length }} {{ t('discount.common.variants') }})</a-divider>
        <div style="max-height: 300px; overflow-y: auto; border: 1px solid var(--color-border-2); border-radius: 4px; padding: 8px">
          <div
            v-for="variant in selectedVariantsForModalEdit"
            :key="variant.id"
            style="
              display: flex;
              align-items: center;
              gap: 12px;
              padding: 8px;
              border-bottom: 1px solid var(--color-border-1);
            "
          >
            <img
              v-if="variant.anhSanPham?.[0]"
              :src="variant.anhSanPham[0]"
              :alt="variant.tenSanPham"
              style="width: 40px; height: 40px; object-fit: cover; border-radius: 4px"
            />
            <div v-else style="width: 40px; height: 40px; background: var(--color-fill-2); border-radius: 4px; display: flex; align-items: center; justify-content: center">
              <icon-image :size="20" style="color: var(--color-text-4)" />
            </div>
            <div style="flex: 1; min-width: 0">
              <div style="font-weight: 500; font-size: 13px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis">
                {{ variant.tenSanPham }}
              </div>
              <div style="font-size: 12px; color: var(--color-text-3); margin-top: 2px">
                {{ buildVariantLabel(variant) }}
              </div>
            </div>
            <div style="font-size: 13px; color: var(--color-text-2); white-space: nowrap">
              {{ formatCurrency(variant.giaBan || 0) }}
            </div>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive, watch } from 'vue'
import { Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
import type { FormInstance, FormRules } from '@arco-design/web-vue/es/form'
import {
  IconPlus,
  IconDelete,
  IconLeft,
  IconImage,
  IconCheck,
  IconClose,
  IconDown,
  IconUp,
  IconBgColors,
  IconExpand,
  IconTag,
  IconApps,
  IconNav,
} from '@arco-design/web-vue/es/icon'
import dayjs from 'dayjs'
import axios from 'axios'
import soleIcon from '@/assets/icons/soles.png'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import PageActions from '@/components/page-actions/page-actions.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import {
  updatePromotionCampaign,
  fetchPromotionProductDetails,
  createPromotionProductDetailsBatch,
  type PromotionProductDetailApiModel,
} from '@/api/discount-management'
import { getBienTheSanPhamList, type BienTheSanPham } from '@/api/san-pham'

// eslint-disable-next-line import/no-unresolved
import { useRouter } from 'vue-router'

// Router
const router = useRouter()
const { t } = useI18n()
const { breadcrumbItems } = useBreadcrumb()

// Get promotion ID from route params
const promotionId = computed(() => Number(router.currentRoute.value.params.id))

// Loading state
const loading = ref(false)
const submitting = ref(false)
const editConfirmVisible = ref(false)

// Product selection state
type PromotionProductOption = BienTheSanPham & {
  tenSanPhamChiTiet?: string
}

interface ProductGroup {
  key: number | string
  tenSanPham: string
  tenNhaSanXuat?: string
  thumbnail?: string
  variants: PromotionProductOption[]
}

const productOptions = ref<PromotionProductOption[]>([])
const productOptionsLoading = ref(false)


const buildVariantLabel = (product: PromotionProductOption) => {
  if (!product) return ''
  const baseName = product.tenSanPham?.trim().toLowerCase() ?? ''
  const detailName = product.tenSanPhamChiTiet?.trim()
  if (detailName && detailName.toLowerCase() !== baseName && detailName.length > 0) {
    return detailName
  }
  const attributes = [product.tenMauSac, product.tenKichThuoc, product.tenChatLieu, product.tenDeGiay, product.tenTrongLuong]
    .map((attr) => (typeof attr === 'string' ? attr.trim() : ''))
    .filter((attr) => attr && attr.length > 0)
  return attributes.join(' • ')
}
const originalProductIds = ref<number[]>([])

const promotionEditFormRef = ref<FormInstance>()
const productSearchQuery = ref('')
const expandedRowKeys = ref<(string | number)[]>([])
const promotionEditForm = reactive({
  code: '',
  name: '',
  discountValue: null as number | null,
  dateRange: [] as string[],
  active: true,
  selectedProducts: [] as number[],
  applyToProducts: true,
  lyDoThayDoi: '',
})

function formatCurrency(amount: number) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

async function fetchProducts() {
  productOptionsLoading.value = true
  try {
    const response = await getBienTheSanPhamList(0)
    const responseData = response.data as any
    let rawData: PromotionProductOption[] = []
    if (Array.isArray(responseData)) {
      rawData = responseData
    } else if (Array.isArray(responseData?.data)) {
      rawData = responseData.data
    }
    productOptions.value = rawData.map((item) => ({
      ...item,
      tenSanPham: item.tenSanPham ?? item.tenSanPhamChiTiet ?? '',
      tenSanPhamChiTiet: item.tenSanPhamChiTiet ?? undefined,
      maChiTietSanPham: item.maChiTietSanPham ?? undefined,
    }))
  } catch {
    Message.error(t('discount.campaign.error.loadProducts'))
    productOptions.value = []
  } finally {
    productOptionsLoading.value = false
  }
}

watch(
  () => promotionEditForm.applyToProducts,
  async (shouldApply) => {
    if (shouldApply && productOptions.value.length === 0) {
      await fetchProducts()
    }
    if (!shouldApply) {
      promotionEditForm.selectedProducts = []
    }
  }
)

const disablePastDates = (current: Date | string) => {
  if (!current) return false
  return dayjs(current).isBefore(dayjs().startOf('day'))
}

watch(
  () => promotionEditForm.discountValue,
  (newValue) => {
    if (newValue === undefined || newValue === null) return
    if (newValue < 1) {
      Message.warning(t('discount.campaign.validation.discountValueMin'))
      promotionEditForm.discountValue = 1
      return
    }
    if (newValue > 100) {
      Message.warning(t('discount.campaign.validation.discountValueMax'))
      promotionEditForm.discountValue = 100
    }
  }
)

// Store original values for change detection
const originalPromotionEditForm = reactive({
  code: '',
  name: '',
  discountValue: null as number | null,
  dateRange: [] as string[],
  active: true,
  selectedProducts: [] as number[],
  applyToProducts: true,
})

const promotionEditRules = computed<FormRules>(() => ({
  code: [{ required: true, message: t('discount.campaign.validation.codeRequired') }],
  name: [{ required: true, message: t('discount.campaign.validation.nameRequired') }],
  discountValue: [
    { required: true, message: t('discount.campaign.validation.discountValueRequired') },
    {
      validator: (value: number | null, callback: (msg?: string) => void) => {
        if (value === undefined || value === null || Number.isNaN(Number(value))) {
          callback(t('discount.campaign.validation.discountValueRequired'))
          return
        }
        if (value < 1) {
          callback(t('discount.campaign.validation.discountValueMin'))
          return
        }
        if (value > 100) {
          callback(t('discount.campaign.validation.discountRangeMax'))
          return
        }
        callback()
      },
    },
  ],
  dateRange: [{ required: true, type: 'array', message: t('discount.campaign.validation.dateRangeRequired') }],
  lyDoThayDoi: [{ required: true, message: t('discount.campaign.validation.changeReasonRequired') }],
}))

const productGroups = computed<ProductGroup[]>(() => {
  const groupMap = new Map<number | string, ProductGroup>()
  productOptions.value.forEach((variant) => {
    // Skip variants with invalid ID
    if (!variant.id || typeof variant.id !== 'number' || Number.isNaN(variant.id)) {
      return
    }
    const groupKey = variant.idSanPham ?? variant.tenSanPham ?? `variant-${variant.id}`
    let group = groupMap.get(groupKey)
    if (!group) {
      group = {
        key: groupKey,
        tenSanPham: variant.tenSanPham ?? variant.tenSanPhamChiTiet ?? 'Sản phẩm',
        tenNhaSanXuat: variant.tenNhaSanXuat,
        thumbnail: variant.anhSanPham?.[0],
        variants: [],
      }
      groupMap.set(groupKey, group)
    }
    group.variants.push(variant)
  })
  return Array.from(groupMap.values())
})

const filteredProductGroups = computed(() => {
  if (!productSearchQuery.value) {
    return productGroups.value
  }
  const query = productSearchQuery.value.trim().toLowerCase()
  return productGroups.value.filter((group) => {
    if (group.tenSanPham.toLowerCase().includes(query)) {
      return true
    }
    return group.variants.some((variant) => {
      const variantLabelText = buildVariantLabel(variant).toLowerCase()
      const sku = variant.maChiTietSanPham?.toLowerCase() ?? ''
      return variantLabelText.includes(query) || sku.includes(query)
    })
  })
})

const productGroupPagination = computed(() => ({
  pageSize: 5,
  showTotal: true,
  showPageSize: false,
  total: filteredProductGroups.value.length,
}))

const productGroupColumns = computed(() => [
  {
    title: t('discount.product.image'),
    dataIndex: 'thumbnail',
    slotName: 'thumbnail',
    width: 80,
    align: 'center' as const,
  },
  {
    title: t('discount.product.name'),
    dataIndex: 'tenSanPham',
    slotName: 'product',
    ellipsis: true,
    tooltip: true,
    width: 240,
  },
  {
    title: t('discount.product.brand'),
    dataIndex: 'tenNhaSanXuat',
    slotName: 'brand',
    width: 120,
    align: 'center' as const,
  },
  {
    title: t('discount.product.variants'),
    dataIndex: 'variants',
    slotName: 'variantSelect',
    width: 240,
  },
])

const variantColumns = computed(() => [
  {
    title: t('discount.product.variant'),
    dataIndex: 'variant',
    slotName: 'variant',
    ellipsis: true,
  },
  {
    title: t('discount.product.price'),
    dataIndex: 'price',
    slotName: 'price',
    width: 140,
    align: 'right' as const,
  },
  {
    title: t('discount.campaign.stock'),
    dataIndex: 'stock',
    slotName: 'stock',
    width: 80,
    align: 'center' as const,
  },
])

const getGroupSelectedVariantIdsEdit = (group: ProductGroup) => {
  const variantIds = new Set(group.variants.map((variant) => variant.id))
  return promotionEditForm.selectedProducts.filter((id) => variantIds.has(id))
}

const selectedVariantsForModalEdit = computed(() => {
  return productOptions.value.filter((variant) => promotionEditForm.selectedProducts.includes(variant.id))
})

const selectAllFilteredVariantsEdit = () => {
  const idsToAdd = filteredProductGroups.value.flatMap((group) => group.variants.map((variant) => variant.id))
  const merged = new Set([...promotionEditForm.selectedProducts, ...idsToAdd])
  promotionEditForm.selectedProducts = Array.from(merged)
}

const deselectAllFilteredVariantsEdit = () => {
  const idsToRemove = new Set(filteredProductGroups.value.flatMap((group) => group.variants.map((variant) => variant.id)))
  promotionEditForm.selectedProducts = promotionEditForm.selectedProducts.filter((id) => !idsToRemove.has(id))
}

const selectAllVariantsEdit = (group: ProductGroup) => {
  const variantIds = group.variants
    .filter((v) => v.id && typeof v.id === 'number' && !Number.isNaN(v.id))
    .map((v) => v.id)
  variantIds.forEach((id) => {
    if (!promotionEditForm.selectedProducts.includes(id)) {
      promotionEditForm.selectedProducts.push(id)
    }
  })
}

const deselectAllVariantsEdit = (group: ProductGroup) => {
  const variantIds = group.variants
    .filter((v) => v.id && typeof v.id === 'number' && !Number.isNaN(v.id))
    .map((v) => v.id)
  promotionEditForm.selectedProducts = promotionEditForm.selectedProducts.filter((id) => !variantIds.includes(id))
}

const toggleExpanded = (key: string | number) => {
  const index = expandedRowKeys.value.indexOf(key)
  if (index > -1) {
    expandedRowKeys.value.splice(index, 1)
  } else {
    expandedRowKeys.value.push(key)
  }
}

const handleExpand = (rowKey: string | number, expanded: boolean) => {
  if (expanded) {
    if (!expandedRowKeys.value.includes(rowKey)) {
      expandedRowKeys.value.push(rowKey)
    }
  } else {
    const index = expandedRowKeys.value.indexOf(rowKey)
    if (index > -1) {
      expandedRowKeys.value.splice(index, 1)
    }
  }
}

const toggleVariant = (variantId: number) => {
  // Validate ID
  if (!variantId || typeof variantId !== 'number' || Number.isNaN(variantId)) {
    return
  }
  const index = promotionEditForm.selectedProducts.indexOf(variantId)
  if (index > -1) {
    promotionEditForm.selectedProducts.splice(index, 1)
  } else {
    promotionEditForm.selectedProducts.push(variantId)
  }
}

const isAllVariantsSelected = (group: ProductGroup) => {
  const groupVariantIds = group.variants.map((v) => v.id)
  return groupVariantIds.length > 0 && groupVariantIds.every((id) => promotionEditForm.selectedProducts.includes(id))
}

const isSomeVariantsSelected = (group: ProductGroup) => {
  const groupVariantIds = group.variants.map((v) => v.id)
  const selectedCount = groupVariantIds.filter((id) => promotionEditForm.selectedProducts.includes(id)).length
  return selectedCount > 0 && selectedCount < groupVariantIds.length
}

const toggleAllVariants = (group: ProductGroup, checked: boolean) => {
  const groupVariantIds = group.variants.map((v) => v.id)
  if (checked) {
    // Add all variants from this group
    const merged = new Set([...promotionEditForm.selectedProducts, ...groupVariantIds])
    promotionEditForm.selectedProducts = Array.from(merged)
  } else {
    // Remove all variants from this group
    promotionEditForm.selectedProducts = promotionEditForm.selectedProducts.filter((id) => !groupVariantIds.includes(id))
  }
}

// Methods
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.style.display = 'none'
  const placeholder = img.parentElement?.querySelector('.product-image-placeholder')
  if (placeholder) {
    ;(placeholder as HTMLElement).style.display = 'flex'
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

  // Explicitly check lyDoThayDoi
  if (!promotionEditForm.lyDoThayDoi || !promotionEditForm.lyDoThayDoi.trim()) {
    Message.error('Vui lòng nhập lý do thay đổi')
    return
  }

  // Check if any changes were made
  const hasChanges =
    promotionEditForm.code !== originalPromotionEditForm.code ||
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
    maDotGiamGia: promotionEditForm.code.trim(),
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
    // Filter out invalid IDs
    const validCurrentProducts = promotionEditForm.selectedProducts.filter((id) => id && typeof id === 'number' && !Number.isNaN(id))
    const validOriginalProducts = originalProductIds.value.filter((id) => id && typeof id === 'number' && !Number.isNaN(id))

    const currentProducts = new Set(validCurrentProducts)
    const originalProducts = new Set(validOriginalProducts)

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

.header-actions {
  margin-top: 16px;
  margin-bottom: 16px;
}


.product-selection-section {
  border: 1px solid var(--color-border-2);
  border-radius: 8px;
  padding: 16px;
  background: var(--color-bg-2);
}

.product-selection-section :deep(.arco-table-tr) {
  cursor: pointer;
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

.product-info-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.product-info-cell .product-name {
  font-weight: 600;
  color: var(--color-text-1);
}

.product-variant-meta {
  font-size: 12px;
  color: var(--color-text-3);
  line-height: 1.2;
}

.modal-note {
  margin-top: 4px;
  color: var(--color-text-3);
}

.status-switch-wrapper {
  display: inline-flex;
  justify-content: center;
  width: 72px;
}

.status-switch-wrapper .arco-switch {
  width: 100%;
  max-width: 72px;
  justify-content: center;
}

.status-switch-wrapper .arco-switch::before {
  inset-inline-start: 4px;
}

/* Variant Expansion Styles */
.variant-expansion {
  padding: 12px;
  background: var(--color-fill-1);
  border-radius: 4px;
}

.variant-expansion :deep(.arco-table) {
  background: transparent;
}

.variant-expansion :deep(.arco-table-th) {
  background-color: var(--color-fill-2);
  font-weight: 600;
  font-size: 12px;
}

.variant-expansion :deep(.arco-table-td) {
  background: white;
}

.variant-expansion :deep(.arco-table-tr) {
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.variant-expansion :deep(.arco-table-tr:hover .arco-table-td) {
  background: var(--color-primary-light-1);
}

.variant-expansion :deep(.variant-row-selected .arco-table-td) {
  background-color: var(--color-primary-light-2) !important;
  border-left: 3px solid var(--color-primary-6);
}

.variant-expansion :deep(.variant-row-selected:hover .arco-table-td) {
  background-color: var(--color-primary-light-3) !important;
}


.variant-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.variant-name {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.variant-label {
  font-weight: 500;
  color: var(--color-text-1);
  font-size: 13px;
}

.variant-specs-inline {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.spec-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--color-text-3);
  background: var(--color-fill-2);
  padding: 2px 8px;
  border-radius: 4px;
  white-space: nowrap;
}

.spec-tag svg {
  color: var(--color-primary-6);
  flex-shrink: 0;
}

.variant-price {
  font-weight: 600;
  color: var(--color-success-6);
  font-size: 14px;
}

.variant-stock {
  color: var(--color-text-3);
  font-size: 12px;
}

.variant-stock strong {
  color: var(--color-text-2);
}

.spec-icon-img {
  width: 12px;
  height: 12px;
  object-fit: contain;
  display: inline-block;
}
</style>
