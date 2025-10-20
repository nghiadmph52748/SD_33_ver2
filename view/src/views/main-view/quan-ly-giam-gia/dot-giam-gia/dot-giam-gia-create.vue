<template>
  <div class="promotion-create-page">
    <Breadcrumb :items="breadcrumbItems" />

    <a-card class="promotion-card">
      <a-form ref="formRef" :model="formState" :rules="rules" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="name" :label="t('discount.campaign.name')">
              <a-input v-model="formState.name" :placeholder="t('discount.campaign.namePlaceholder')" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="code" :label="t('discount.campaign.code')">
              <a-input v-model="formState.code" :placeholder="t('discount.campaign.codePlaceholder')" allow-clear />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="discountValue" :label="t('discount.campaign.discountValue')">
              <a-input-number
                v-model="formState.discountValue"
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
          </a-col>
          <a-col :span="12">
            <a-form-item field="dateRange" :label="t('discount.campaign.validityPeriod')">
              <a-range-picker
                v-model="formState.dateRange"
                :show-time="true"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="DD/MM/YYYY HH:mm"
                allow-clear
                :disabled-date="disablePastDates"
                :placeholder="[t('discount.common.selectStartDate'), t('discount.common.selectEndDate')]"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Product Selection Section -->
        <div class="product-selection-section">
          <a-divider style="margin: 16px 0" />
          <div style="font-weight: 600; margin-bottom: 12px">{{ t('discount.campaign.selectProducts') }}</div>

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
            <template #product="{ record }">
              <div class="product-info-cell">
                <div class="product-name">{{ record.tenSanPham }}</div>
                <div v-if="record.variants.length > 1" class="product-variant-meta">
                  {{ record.variants.length }} {{ t('discount.common.variants') }} •
                  <span style="color: var(--color-primary-light-4)">{{ getGroupSelectedVariantIds(record).length }} {{ t('discount.common.selected') }}</span>
                </div>
                <div v-else-if="record.variants.length === 1" class="product-variant-meta">{{ buildVariantLabel(record.variants[0]) }}</div>
              </div>
            </template>
            <template #variantSelect="{ record }">
              <div style="display: flex; align-items: center; justify-content: space-between; gap: 8px">
                <div style="display: flex; align-items: center; gap: 8px; flex: 1">
                  <span style="font-size: 13px; color: var(--color-text-2)">
                    <strong>{{ getGroupSelectedVariantIds(record).length }}</strong>
                    / {{ record.variants.length }} {{ t('discount.common.selected') }}
                  </span>
                  <a-button
                    v-if="!isAllVariantsSelected(record)"
                    size="mini"
                    type="text"
                    @click.stop="selectAllVariants(record)"
                    style="padding: 0 4px; font-size: 12px"
                  >
                    {{ t('discount.common.selectAll') }}
                  </a-button>
                  <a-button
                    v-else
                    size="mini"
                    type="text"
                    @click.stop="deselectAllVariants(record)"
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
                  :row-class="(record) => (formState.selectedProducts.includes(record.id) ? 'variant-row-selected' : '')"
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
            <strong>{{ formState.selectedProducts.length }}</strong>
            {{ t('discount.common.variants') }}
          </div>
        </div>
      </a-form>
    </a-card>

    <PageActions>
      <a-space>
        <a-button @click="goBack">{{ t('discount.common.reset') }}</a-button>
        <a-button type="primary" :loading="confirmSaveSubmitting" @click="handleSaveClick">{{ t('discount.common.update') }}</a-button>
      </a-space>
    </PageActions>

    <!-- Confirmation Modal -->
    <a-modal
      v-model:visible="confirmSaveVisible"
      :title="t('discount.campaign.confirmCreate')"
      :confirm-loading="confirmSaveSubmitting"
      @ok="confirmSave"
      @cancel="confirmSaveVisible = false"
      :ok-text="t('discount.common.confirm')"
      :cancel-text="t('discount.common.cancel')"
      width="700px"
    >
      <p style="margin-bottom: 16px; color: var(--color-text-2)">{{ t('discount.coupon.confirmSave') }}</p>
      <a-descriptions :column="1" bordered>
        <a-descriptions-item :label="t('discount.campaign.name')">
          {{ formState.name }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('discount.campaign.code')">
          {{ formState.code }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('discount.campaign.discountValue')">
          <template v-if="formState.discountValue !== null">{{ formState.discountValue }}%</template>
          <template v-else>--</template>
        </a-descriptions-item>
        <a-descriptions-item :label="t('discount.campaign.validityPeriod')">
          {{ formatDateRange(formState.dateRange) }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('discount.campaign.appliedProducts')">{{ formState.selectedProducts.length }} {{ t('discount.common.variants') }}</a-descriptions-item>
      </a-descriptions>

      <div v-if="formState.selectedProducts.length > 0" style="margin-top: 16px">
        <a-divider orientation="left" style="margin: 16px 0 12px 0">{{ t('discount.campaign.productList') }}</a-divider>
        <div style="max-height: 300px; overflow-y: auto; border: 1px solid var(--color-border-2); border-radius: 4px; padding: 8px">
          <div
            v-for="variant in selectedVariantsForModal"
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
import { reactive, ref, onMounted, watch, computed } from 'vue'
import type { FormInstance, FormRules } from '@arco-design/web-vue/es/form'
import { Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import PageActions from '@/components/page-actions/page-actions.vue'
import {
  createPromotionCampaign,
  fetchPromotionCampaigns,
  createPromotionProductDetailsBatch,
  type PromotionApiModel,
} from '@/api/discount-management'
import { getBienTheSanPhamList, type BienTheSanPham } from '@/api/san-pham'
import { IconImage, IconDown, IconUp, IconBgColors, IconExpand, IconTag, IconApps, IconNav } from '@arco-design/web-vue/es/icon'
import dayjs from 'dayjs'
import soleIcon from '@/assets/icons/soles.png'

// eslint-disable-next-line import/no-unresolved
import { useRouter } from 'vue-router'

const router = useRouter()
const { t } = useI18n()
const { breadcrumbItems } = useBreadcrumb()

const formRef = ref<FormInstance>()
const confirmSaveVisible = ref(false)
const confirmSaveSubmitting = ref(false)
type PromotionProductOption = BienTheSanPham & {
  tenSanPhamChiTiet?: string
}

const productOptions = ref<PromotionProductOption[]>([])


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
const productOptionsLoading = ref(false)
const productSearchQuery = ref('')
const expandedRowKeys = ref<(string | number)[]>([])

const formState = reactive({
  name: '',
  code: '',
  discountValue: null as number | null,
  dateRange: [] as string[],
  selectedProducts: [] as number[],
  applyToProducts: true,
})

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
  () => formState.applyToProducts,
  async (shouldApply) => {
    if (shouldApply && productOptions.value.length === 0) {
      await fetchProducts()
    }
    if (!shouldApply) {
      formState.selectedProducts = []
    }
  }
)

const disablePastDates = (current: Date | string) => {
  if (!current) return false
  return dayjs(current).isBefore(dayjs().startOf('day'))
}

// Real-time validation for discount value
watch(
  () => formState.discountValue,
  (newValue) => {
    if (newValue === undefined || newValue === null) {
      return
    }
    if (newValue < 1) {
      Message.warning(t('discount.campaign.validation.discountValueMin'))
      formState.discountValue = 1
      return
    }
    if (newValue > 100) {
      Message.warning(t('discount.campaign.validation.discountValueMax'))
      formState.discountValue = 100
    }
  }
)

const rules: FormRules = {
  code: [{ required: true, message: t('discount.campaign.validation.codeRequired') }],
  name: [{ required: true, message: t('discount.campaign.validation.nameRequired') }],
  discountValue: [
    { required: true, message: t('discount.campaign.validation.discountValueRequired') },
    {
      validator: (value, callback) => {
        if (value === undefined || value === null || value === '') {
          callback(t('discount.campaign.validation.discountValueRequired'))
          return
        }
        const numeric = Number(value)
        if (Number.isNaN(numeric) || numeric < 1) {
          callback(t('discount.campaign.validation.discountValueMin'))
          return
        }
        if (numeric > 100) {
          callback(t('discount.campaign.validation.discountRangeMax'))
          return
        }
        callback()
      },
    },
  ],
  dateRange: [
    { required: true, type: 'array', message: t('discount.campaign.validation.dateRangeRequired') },
    {
      validator: (value: string[], callback) => {
        if (!Array.isArray(value) || value.length !== 2) {
          callback(t('discount.campaign.validation.dateRangeRequired'))
          return
        }
        const [start, end] = value
        if (!start || !end) {
          callback(t('discount.campaign.validation.dateRangeRequired'))
          return
        }
        if (dayjs(start).isBefore(dayjs().startOf('day'))) {
          callback(t('discount.campaign.validation.startDateFuture'))
          return
        }
        if (new Date(start) > new Date(end)) {
          callback(t('discount.campaign.validation.endDateAfterStart'))
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

interface ProductGroup {
  key: number | string
  tenSanPham: string
  thumbnail?: string
  variants: PromotionProductOption[]
}

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
    title: t('discount.common.image'),
    dataIndex: 'thumbnail',
    slotName: 'thumbnail',
    width: 80,
    align: 'center' as const,
  },
  {
    title: t('discount.common.products'),
    dataIndex: 'tenSanPham',
    slotName: 'product',
    ellipsis: true,
    tooltip: true,
    width: 300,
  },
  {
    title: t('discount.common.variants'),
    dataIndex: 'variants',
    slotName: 'variantSelect',
    width: 240,
  },
])

const variantColumns = computed(() => [
  {
    title: t('discount.common.variants'),
    dataIndex: 'variant',
    slotName: 'variant',
    ellipsis: true,
  },
  {
    title: t('discount.common.price'),
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

const getGroupSelectedVariantIds = (group: ProductGroup) => {
  const variantIds = new Set(group.variants.map((variant) => variant.id))
  return formState.selectedProducts.filter((id) => variantIds.has(id))
}

const selectedVariantsForModal = computed(() => {
  return productOptions.value.filter((variant) => formState.selectedProducts.includes(variant.id))
})

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
  const index = formState.selectedProducts.indexOf(variantId)
  if (index > -1) {
    formState.selectedProducts.splice(index, 1)
  } else {
    formState.selectedProducts.push(variantId)
  }
}

const isAllVariantsSelected = (group: ProductGroup) => {
  const validVariantIds = group.variants
    .filter((v) => v.id && typeof v.id === 'number' && !Number.isNaN(v.id))
    .map((v) => v.id)
  return validVariantIds.length > 0 && validVariantIds.every((id) => formState.selectedProducts.includes(id))
}

const selectAllVariants = (group: ProductGroup) => {
  const variantIds = group.variants
    .filter((v) => v.id && typeof v.id === 'number' && !Number.isNaN(v.id))
    .map((v) => v.id)
  variantIds.forEach((id) => {
    if (!formState.selectedProducts.includes(id)) {
      formState.selectedProducts.push(id)
    }
  })
}

const deselectAllVariants = (group: ProductGroup) => {
  const variantIds = group.variants
    .filter((v) => v.id && typeof v.id === 'number' && !Number.isNaN(v.id))
    .map((v) => v.id)
  formState.selectedProducts = formState.selectedProducts.filter((id) => !variantIds.includes(id))
}

function formatCurrency(amount: number) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement
  img.style.display = 'none'
  // Show placeholder instead
  const placeholder = img.parentElement?.querySelector('.product-image-placeholder')
  if (placeholder) {
    ;(placeholder as HTMLElement).style.display = 'flex'
  }
}

onMounted(async () => {
  formState.code = await generateNextCode()
  // Load products since applyToProducts is true by default
  await fetchProducts()
})

const goBack = () => {
  router.push({ name: 'QuanLyDotGiamGia' })
}

function formatDateRange(dateRange: string[]) {
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
    // Filter out any invalid IDs before sending to backend
    const validProductIds = formState.selectedProducts.filter((id) => id && typeof id === 'number' && !Number.isNaN(id))

    if (validProductIds.length > 0) {
      // Fetch the newly created promotion to get its ID
      const promotions = await fetchPromotionCampaigns()
      const newPromotion = promotions.find((p: PromotionApiModel) => p.maDotGiamGia === formState.code)

      if (newPromotion) {
        const productDetailPayloads = validProductIds.map((productId) => ({
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
  } catch (err: any) {
    const message = err?.response?.data?.message || err?.message || 'Không thể tạo đợt giảm giá'
    Message.error(message)
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

.product-selection-section :deep(.arco-table-tr) {
  cursor: pointer;
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

.spec-icon-img {
  width: 12px;
  height: 12px;
  object-fit: contain;
  display: inline-block;
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
</style>
