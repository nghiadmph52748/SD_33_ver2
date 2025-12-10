<template>
  <div class="promotion-management-page">
    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="filters" layout="vertical">
        <!-- Row 1: Search - Start Date - End Date - Percentage -->
        <a-row :gutter="16">
          <a-col :span="6">
            <a-form-item :label="t('discount.common.search')">
              <a-input
                v-model="filters.search"
                :placeholder="t('discount.campaign.searchPlaceholder')"
                allow-clear
                @change="searchPromotions"
              />
            </a-form-item>
          </a-col>
          <a-col :span="5">
            <a-form-item :label="t('discount.common.startDate')">
              <a-date-picker
                v-model="filters.startDate"
                :placeholder="t('discount.common.selectStartDate')"
                allow-clear
                @change="searchPromotions"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="5">
            <a-form-item :label="t('discount.common.endDate')">
              <a-date-picker
                v-model="filters.endDate"
                :placeholder="t('discount.common.selectEndDate')"
                allow-clear
                @change="searchPromotions"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item :label="t('discount.campaign.percentageRange')">
              <div class="percentage-filter">
                <a-slider
                  class="percentage-slider-control"
                  v-model="filters.percentageRange"
                  range
                  :min="DEFAULT_PERCENTAGE_RANGE[0]"
                  :max="DEFAULT_PERCENTAGE_RANGE[1]"
                  :step="1"
                  @change="searchPromotions"
                  style="width: 100%"
                />
                <div class="percentage-filter-values">
                  <span class="percentage-value-badge">{{ filters.percentageRange[0] }}%</span>
                  <span class="percentage-filter-separator">-</span>
                  <span class="percentage-value-badge">{{ filters.percentageRange[1] }}%</span>
                </div>
              </div>
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Row 2: Status -->
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item :label="t('discount.common.status')">
              <a-radio-group v-model="filters.status" type="button" @change="searchPromotions">
                <a-radio value="">{{ t('discount.common.status.all') }}</a-radio>
                <a-radio value="active">{{ t('discount.common.status.active') }}</a-radio>
                <a-radio value="expired">{{ t('discount.common.status.ended') }}</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Actions Row -->
        <div class="actions-row">
          <a-space>
            <a-button @click="resetFilters">
              <template #icon>
                <icon-refresh />
              </template>
              {{ t('discount.common.reset') }}
            </a-button>
            <a-button @click="openExportConfirmModal">
              <template #icon>
                <icon-download />
              </template>
              {{ t('discount.common.export') }}
            </a-button>
            <a-button type="primary" @click="openCreatePage">
              <template #icon>
                <icon-plus />
              </template>
              {{ t('discount.campaign.add') }}
            </a-button>
          </a-space>
        </div>
      </a-form>
    </a-card>

    <!-- Promotions Table -->
    <a-card :title="t('discount.campaign.list')" class="table-card">
      <template #extra>
        <a-input-search
          v-model="tableSearch"
          :placeholder="t('discount.campaign.tableSearchPlaceholder')"
          allow-clear
          style="width: 300px"
          @search="handleTableSearch"
          @clear="handleTableSearch"
        />
      </template>
      <a-table :columns="columns" :data="filteredPromotions" :pagination="pagination" :loading="loading">
        <template #code="{ record }">
          <div class="code-cell">
            {{ record.code }}
          </div>
        </template>

        <template #name="{ record }">
          <div class="name-cell">
            {{ record.name }}
          </div>
        </template>

        <template #percentage="{ record }">
          <span class="percentage-value">{{ Number(record.percentage).toFixed(2) }}%</span>
        </template>

        <template #date_range="{ record }">
          <div class="date-range">
            <div>{{ formatDateTime(record.start_date) }}</div>
            <div>{{ formatDateTime(record.end_date) }}</div>
          </div>
        </template>

        <template #status="{ record }">
          <div class="status-cell" :class="`status-${record.status}`">
            {{ getStatusText(record.status) }}
          </div>
        </template>

        <template #created_at="{ record }">
          <div>{{ record.created_at ? formatDateTime(record.created_at) : '—' }}</div>
        </template>

        <template #updated_at="{ record }">
          <div>{{ record.updated_at ? formatDateTime(record.updated_at) : '—' }}</div>
        </template>

        <template #action="{ record }">
          <a-space>
            <a-button type="text" @click="viewPromotion(record)">
              <template #icon>
                <icon-eye />
              </template>
            </a-button>
            <a-button type="text" @click="editPromotion(record)" :disabled="record.status === 'expired'">
              <template #icon>
                <icon-edit />
              </template>
            </a-button>
            <a-switch
              v-if="isAdmin"
              type="round"
              :model-value="record.enabled"
              :loading="isPromotionStatusUpdating(record.id)"
              :disabled="!canTogglePromotionStatus(record)"
              @change="(value) => handlePromotionStatusToggle(record, value)"
            >
              <template #checked-icon>
                <icon-check />
              </template>
              <template #unchecked-icon>
                <icon-close />
              </template>
            </a-switch>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:visible="detailVisible"
      :title="t('discount.campaign.detail')"
      :footer="false"
      width="900px"
      :body-style="{ maxHeight: '80vh', overflowY: 'auto' }"
    >
      <a-descriptions :column="2" bordered>
        <a-descriptions-item :label="t('discount.common.name')">{{ selectedPromotion?.name }}</a-descriptions-item>
        <a-descriptions-item :label="t('discount.common.code')">{{ selectedPromotion?.code }}</a-descriptions-item>
        <a-descriptions-item :label="t('discount.campaign.discountValue')">
          <span class="percentage-badge">{{ selectedPromotion?.percentage }}%</span>
        </a-descriptions-item>
        <a-descriptions-item :label="t('discount.common.status')">
          <a-tag :color="getStatusColor(selectedPromotion?.status ?? '')">
            {{ selectedPromotion ? getStatusText(selectedPromotion.status) : '' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item :label="t('discount.campaign.validityPeriod')" :span="2">
          {{ formatDateTime(selectedPromotion?.start_date ?? '') }} - {{ formatDateTime(selectedPromotion?.end_date ?? '') }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('discount.common.createdAt')">
          {{ selectedPromotion?.created_at ? formatDateTime(selectedPromotion.created_at) : '—' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('discount.common.updatedAt')">
          {{ selectedPromotion?.updated_at ? formatDateTime(selectedPromotion.updated_at) : '—' }}
        </a-descriptions-item>
      </a-descriptions>

      <!-- Product Details Section -->
      <a-divider style="margin: 24px 0" />
      <div class="product-details-section">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
          <h3 style="margin: 0; font-size: 16px; font-weight: 600">
            {{ t('discount.campaign.productList') }} ({{ promotionProductDetails.length }} {{ t('discount.common.variants') }})
          </h3>
          <div style="display: flex; align-items: center; gap: 8px">
            <a-spin v-if="isProductDetailsLoading" size="small" />
            <a-button v-else size="small" type="text" @click="selectedPromotion && loadPromotionProductDetails(selectedPromotion.id)">
              <template #icon>
                <icon-refresh />
              </template>
              {{ t('discount.common.reload') }}
            </a-button>
          </div>
        </div>

        <div
          v-if="!isProductDetailsLoading && promotionProductDetails.length === 0"
          style="text-align: center; padding: 32px; color: var(--color-text-3)"
        >
          <icon-empty style="font-size: 48px; margin-bottom: 8px" />
          <div>{{ t('discount.campaign.noProductsApplied') }}</div>
        </div>

        <div
          v-else-if="!isProductDetailsLoading"
          style="
            max-height: 350px;
            overflow-y: auto;
            border: 1px solid var(--color-border-2);
            border-radius: 4px;
            padding: 8px;
            scroll-behavior: smooth;
          "
        >
          <div
            v-for="variant in promotionProductDetails"
            :key="variant.id"
            style="display: flex; align-items: center; gap: 12px; padding: 8px; border-bottom: 1px solid var(--color-border-1)"
          >
            <img
              v-if="variant.anhSanPham?.[0]"
              :src="variant.anhSanPham[0]"
              :alt="variant.tenSanPham"
              style="width: 50px; height: 50px; object-fit: cover; border-radius: 4px"
            />
            <div
              v-else
              style="
                width: 50px;
                height: 50px;
                background: var(--color-fill-2);
                border-radius: 4px;
                display: flex;
                align-items: center;
                justify-content: center;
              "
            >
              <icon-image :size="24" style="color: var(--color-text-4)" />
            </div>
            <div style="flex: 1; min-width: 0">
              <div style="font-weight: 500; font-size: 14px; margin-bottom: 4px">
                {{ variant.tenSanPham }}
              </div>
              <div style="font-size: 12px; color: var(--color-text-3); margin-bottom: 2px">
                {{ buildVariantLabel(variant) }}
              </div>
              <div style="display: flex; gap: 8px; align-items: center">
                <a-tag v-if="variant.maChiTietSanPham" size="small" color="blue">{{ variant.maChiTietSanPham }}</a-tag>
                <span style="font-size: 12px; color: var(--color-text-3)">
                  {{ t('discount.campaign.stock') }}:
                  <strong>{{ variant.soLuong ?? 0 }}</strong>
                </span>
              </div>
            </div>
            <div style="text-align: right; white-space: nowrap">
              <div style="font-size: 15px; font-weight: 600; color: var(--color-text-1)">
                {{ formatCurrency(variant.giaBan || 0) }}
              </div>
              <div v-if="selectedPromotion" style="font-size: 13px; color: var(--color-success-light-4); margin-top: 2px">
                {{ formatCurrency((variant.giaBan || 0) * (1 - selectedPromotion.percentage / 100)) }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- History Section -->
      <a-divider style="margin: 24px 0" />
      <div class="history-section">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
          <h3 style="margin: 0; font-size: 16px; font-weight: 600">{{ t('discount.history.title') }}</h3>
          <div style="display: flex; align-items: center; gap: 8px">
            <a-spin v-if="isHistoryLoading" size="small" />
            <a-button v-else size="small" type="text" @click="selectedPromotion && loadHistory(selectedPromotion.id)">
              <template #icon>
                <icon-refresh />
              </template>
              {{ t('discount.common.reload') }}
            </a-button>
          </div>
        </div>

        <div v-if="!isHistoryLoading && historyList.length === 0" style="text-align: center; padding: 32px; color: var(--color-text-3)">
          <icon-empty style="font-size: 48px; margin-bottom: 8px" />
          <div>{{ t('discount.history.empty') }}</div>
        </div>

        <a-timeline v-else-if="!isHistoryLoading">
          <a-timeline-item v-for="history in historyList" :key="history.id" :dot-color="getActionColor(history.hanhDong)">
            <template #dot>
              <div class="timeline-icon-wrapper">
                <icon-plus-circle v-if="history.hanhDong.includes('TẠO')" />
                <icon-edit v-else-if="history.hanhDong.includes('CẬP NHẬT')" />
                <icon-delete v-else-if="history.hanhDong.includes('XÓA')" />
              </div>
            </template>

            <div class="history-item">
              <div class="history-header">
                <strong>{{ history.hanhDong }}</strong>
                <span class="history-time">{{ formatHistoryDate(history.ngayThayDoi) }}</span>
              </div>
              <div v-if="history.tenNhanVien" class="history-user">
                <icon-user style="font-size: 12px; margin-right: 4px" />
                {{ history.tenNhanVien }}
                <span v-if="history.maNhanVien" style="color: var(--color-text-3)">({{ history.maNhanVien }})</span>
              </div>
              <div v-if="history.moTaThayDoi" class="history-description">
                <pre style="margin: 0; font-family: inherit; white-space: pre-wrap">{{ history.moTaThayDoi }}</pre>
              </div>
            </div>
          </a-timeline-item>
        </a-timeline>
      </div>
    </a-modal>

    <a-modal
      v-model:visible="editVisible"
      title="Chỉnh sửa đợt giảm giá"
      @before-ok="handleEditOkClick"
      @cancel="editVisible = false"
      :width="editModalWidth"
      ok-text="Đồng ý"
      cancel-text="Hủy"
    >
      <div class="modal-edit-layout">
        <!-- Left Column: Form -->
        <div class="modal-left-column">
          <a-form ref="promotionEditFormRef" :model="promotionEditForm" :rules="promotionEditRules" layout="vertical">
            <a-form-item field="code" label="Mã đợt giảm giá">
              <a-input v-model="promotionEditForm.code" placeholder="Mã tự động" readonly disabled />
            </a-form-item>
            <a-form-item field="name" label="Tên đợt giảm giá">
              <a-input v-model="promotionEditForm.name" placeholder="Nhập tên đợt giảm giá" allow-clear />
            </a-form-item>
            <a-form-item field="discountValue" label="Giá trị giảm (%)">
              <a-input-number
                v-model="promotionEditForm.discountValue"
                :min="1"
                :max="100"
                :step="1"
                :precision="0"
                suffix="%"
                placeholder="Nhập giá trị giảm..."
                style="width: 100%"
              />
              <div style="margin-top: 4px; font-size: 12px; color: var(--color-text-3)">Giá trị từ 1-100</div>
            </a-form-item>
            <a-form-item field="dateRange" label="Thời gian áp dụng">
              <a-range-picker
                v-model="promotionEditForm.dateRange"
                :show-time="true"
                value-format="YYYY-MM-DDTHH:mm:ss"
                format="DD/MM/YYYY HH:mm"
                allow-clear
                :disabled-date="disablePastDates"
                style="width: 100%"
              />
            </a-form-item>
            <a-form-item field="active" label="Trạng thái">
              <a-switch v-model="promotionEditForm.active" checked-children="Hoạt động" un-checked-children="Không hoạt động" />
            </a-form-item>
            <a-form-item field="applyToProducts">
              <a-checkbox v-model="promotionEditForm.applyToProducts">Áp dụng cho sản phẩm cụ thể</a-checkbox>
              <div style="margin-left: 24px; margin-top: 4px; font-size: 12px; color: var(--color-text-3)">
                Nếu không chọn, đợt giảm giá sẽ không áp dụng cho sản phẩm cụ thể
              </div>
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
        </div>

        <!-- Right Column: Product Selection -->
        <div v-if="promotionEditForm.applyToProducts" class="modal-right-column">
          <div class="product-selection-section-inline">
            <div style="font-weight: 600; margin-bottom: 12px; font-size: 15px">Chọn sản phẩm áp dụng</div>

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
              <template #productInfo="{ record }">
                <div class="product-info-cell">
                  <div class="product-name">{{ record.tenSanPham || '—' }}</div>
                  <div v-if="buildVariantLabel(record)" class="product-variant-meta">{{ buildVariantLabel(record) }}</div>
                </div>
              </template>
              <template #giaBan="{ record }">
                <span>{{ formatCurrency(record.giaBan || 0) }}</span>
              </template>
              <template #soLuong="{ record }">
                <span>{{ record.soLuong ?? 0 }}</span>
              </template>
            </a-table>

            <div style="margin-top: 8px; font-size: 12px; color: var(--color-text-3)">
              {{ t('discount.common.selected') }}:
              <strong>{{ promotionEditForm.selectedProducts.length }}</strong>
              {{ t('discount.common.variants') }}
            </div>
          </div>
        </div>
      </div>
    </a-modal>

    <!-- Confirmation Modal for Edit -->
    <a-modal
      v-model:visible="editConfirmVisible"
      title="Xác nhận cập nhật"
      :confirm-loading="editSubmitting"
      width="480px"
      ok-text="Xác nhận"
      cancel-text="Hủy"
      @ok="submitPromotionEdit"
      @cancel="editConfirmVisible = false"
    >
      <p>
        Bạn chắc chắn muốn cập nhật đợt giảm giá
        <strong>{{ selectedPromotion?.name }}</strong>
        ?
      </p>
      <p class="modal-note">Thay đổi sẽ được ghi lại vào lịch sử.</p>
    </a-modal>

    <!-- Export Confirmation Modal -->
    <a-modal
      v-model:visible="exportConfirmVisible"
      title="Xác nhận xuất danh sách"
      :confirm-loading="exportSubmitting"
      ok-text="Xuất"
      width="480px"
      @ok="confirmExportExcel"
      @cancel="exportConfirmVisible = false"
    >
      <p>
        Bạn có chắc chắn muốn xuất danh sách
        <strong>{{ filteredPromotions.length }}</strong>
        đợt giảm giá?
      </p>
      <p class="modal-note">File CSV sẽ được tải xuống vào máy tính của bạn.</p>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive, watch } from 'vue'
import { Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/store'
import router from '@/router'
import downloadCsv from '@/utils/export-csv'
import {
  fetchPromotionCampaigns,
  fetchPromotionHistory,
  type PromotionApiModel,
  type PromotionHistoryApiModel,
  updatePromotionCampaign,
  fetchPromotionProductDetails,
  createPromotionProductDetailsBatch,
  type PromotionProductDetailApiModel,
} from '@/api/discount-management'
import { getBienTheSanPhamList, type BienTheSanPham } from '@/api/san-pham'
import type { FormInstance, FormRules } from '@arco-design/web-vue/es/form'
import dayjs from 'dayjs'
import {
  IconPlus,
  IconRefresh,
  IconDownload,
  IconEye,
  IconEdit,
  IconPlusCircle,
  IconUser,
  IconEmpty,
  IconImage,
  IconCheck,
  IconClose,
} from '@arco-design/web-vue/es/icon'

// Setup
const { t } = useI18n()
const userStore = useUserStore()

// Check if user is admin
// Check both idQuyenHan and tenQuyenHan for flexibility
const isAdmin = computed(() => {
  const roleId = userStore.idQuyenHan
  const roleName = userStore.tenQuyenHan?.toLowerCase()

  // Admin if idQuyenHan is 1 OR role name contains 'admin'
  const hasPermission = roleId === 1 || roleName?.includes('admin') || roleName?.includes('quản trị')

  return hasPermission
})

type PromotionStatus = 'active' | 'expired' | 'upcoming' | 'inactive'

type PromotionFilters = {
  search: string
  startDate: Date | null
  endDate: Date | null
  status: '' | PromotionStatus
  percentageRange: [number, number]
}

const DEFAULT_PERCENTAGE_RANGE: [number, number] = [0, 100]

const createDefaultFilters = (): PromotionFilters => ({
  search: '',
  startDate: null,
  endDate: null,
  status: '',
  percentageRange: [...DEFAULT_PERCENTAGE_RANGE] as [number, number],
})

// Filters
const filters = ref<PromotionFilters>(createDefaultFilters())
const tableSearch = ref('')

// Table
const loading = ref(false)

const columns = computed(() => [
  {
    title: 'STT',
    dataIndex: 'index',
    width: 60,
    align: 'center' as const,
  },
  {
    title: t('discount.common.code'),
    dataIndex: 'code',
    slotName: 'code',
    width: 90,
  },
  {
    title: t('discount.common.name'),
    dataIndex: 'name',
    slotName: 'name',
    width: 170,
  },
  {
    title: t('discount.campaign.percentage'),
    dataIndex: 'percentage',
    slotName: 'percentage',
    width: 100,
    align: 'center' as const,
  },
  {
    title: t('discount.campaign.validityPeriod'),
    dataIndex: 'start_date',
    slotName: 'date_range',
    width: 155,
    align: 'center' as const,
  },
  {
    title: t('discount.common.status'),
    dataIndex: 'status',
    slotName: 'status',
    width: 100,
    align: 'center' as const,
  },
  {
    title: t('discount.common.createdAt'),
    dataIndex: 'created_at',
    slotName: 'created_at',
    width: 120,
    align: 'left' as const,
  },
  {
    title: t('discount.common.updatedAt'),
    dataIndex: 'updated_at',
    slotName: 'updated_at',
    width: 120,
    align: 'left' as const,
  },
  {
    title: t('discount.common.actions'),
    slotName: 'action',
    width: 100,
    fixed: 'right' as const,
    align: 'center' as const,
  },
])

// Pagination - moved after filteredPromotions

interface PromotionRecord {
  id: number
  index: number
  code: string
  name: string
  percentage: number
  start_date: string
  end_date: string
  status: PromotionStatus
  enabled: boolean
  created_at?: string
  updated_at?: string
  source: PromotionApiModel
}

const promotions = ref<PromotionRecord[]>([])
const selectedPromotion = ref<PromotionRecord | null>(null)
const promotionStatusUpdatingIds = ref<number[]>([])

const detailVisible = ref(false)
const editVisible = ref(false)
const editConfirmVisible = ref(false)
const editSubmitting = ref(false)

// Export confirmation modal
const exportConfirmVisible = ref(false)
const exportSubmitting = ref(false)

// History state
const historyList = ref<PromotionHistoryApiModel[]>([])
const isHistoryLoading = ref(false)

// Product selection state - type defined first
type PromotionProductOption = BienTheSanPham & {
  tenSanPhamChiTiet?: string
}

const productOptions = ref<PromotionProductOption[]>([])

// Product details for view modal
const promotionProductDetails = ref<PromotionProductOption[]>([])
const isProductDetailsLoading = ref(false)

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
  applyToProducts: false,
  lyDoThayDoi: '',
})

const disablePastDates = (current: Date | string) => {
  if (!current) return false
  return dayjs(current).isBefore(dayjs().startOf('day'))
}

// Store original values for change detection
const originalPromotionEditForm = reactive({
  code: '',
  name: '',
  discountValue: 10,
  dateRange: [] as string[],
  active: true,
  selectedProducts: [] as number[],
  applyToProducts: false,
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
  dateRange: [
    { required: true, type: 'array', message: 'Vui lòng chọn khoảng thời gian' },
    {
      validator: (value: string[], callback: (msg?: string) => void) => {
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
  lyDoThayDoi: [{ required: true, message: 'Vui lòng nhập lý do thay đổi' }],
}

// Real-time validation for discount value
watch(
  () => promotionEditForm.discountValue,
  (value) => {
    if (value === null || value === undefined) return

    // Ensure value is positive
    if (value < 0) {
      promotionEditForm.discountValue = 0
      return
    }

    // Cap percentage discount at 100%
    if (value > 100) {
      promotionEditForm.discountValue = 100
      Message.warning('Giá trị giảm theo phần trăm không được vượt quá 100%')
    }
  }
)

const derivePromotionStatus = (promotion: PromotionApiModel): PromotionStatus => {
  if (promotion.deleted) {
    return 'inactive'
  }

  const now = new Date()
  const start = promotion.ngayBatDau ? new Date(promotion.ngayBatDau) : null
  const end = promotion.ngayKetThuc ? new Date(promotion.ngayKetThuc) : null

  if (start && now < start) {
    return 'upcoming'
  }

  if (end) {
    const endOfDay = new Date(end)
    endOfDay.setHours(23, 59, 59, 999)
    if (now > endOfDay) {
      return 'expired'
    }
  }

  return promotion.trangThai ? 'active' : 'inactive'
}

const resolvePromotionEnabled = (promotion: PromotionApiModel, status: PromotionStatus): boolean => {
  // Force expired promotions to be disabled
  if (status === 'expired') {
    return false
  }

  const rawStatus = (promotion as unknown as { trangThai?: unknown }).trangThai

  if (typeof rawStatus === 'boolean') {
    return rawStatus
  }

  if (typeof rawStatus === 'number') {
    return rawStatus !== 0
  }

  if (typeof rawStatus === 'string') {
    const normalized = rawStatus.trim().toLowerCase()
    if (['true', '1', 'active', 'đang hoạt động', 'dang hoat dong'].includes(normalized)) {
      return true
    }
    if (['false', '0', 'inactive', 'không hoạt động', 'khong hoat dong'].includes(normalized)) {
      return false
    }
  }

  return status === 'active' || status === 'upcoming'
}

const toPromotionRecord = (promotion: PromotionApiModel, index: number): PromotionRecord => {
  const status = derivePromotionStatus(promotion)
  return {
    id: promotion.id,
    index: index + 1,
    code: promotion.maDotGiamGia ?? '',
    name: promotion.tenDotGiamGia ?? '',
    percentage: Number(promotion.giaTriGiamGia ?? 0),
    start_date: promotion.ngayBatDau ?? '',
    end_date: promotion.ngayKetThuc ?? '',
    status,
    enabled: resolvePromotionEnabled(promotion, status),
    created_at: promotion.createdAt,
    updated_at: promotion.updatedAt,
    source: { ...promotion },
  }
}

// Filtered promotions computed
const filteredPromotions = computed(() => {
  let filtered = promotions.value

  // Filter by table search
  if (tableSearch.value) {
    const searchTerm = tableSearch.value.toLowerCase()
    filtered = filtered.filter(
      (promotion) => promotion.name.toLowerCase().includes(searchTerm) || promotion.code.toLowerCase().includes(searchTerm)
    )
  }

  // Filter by search term
  if (filters.value.search) {
    const searchTerm = filters.value.search.toLowerCase()
    filtered = filtered.filter(
      (promotion) => promotion.name.toLowerCase().includes(searchTerm) || promotion.code.toLowerCase().includes(searchTerm)
    )
  }

  // Filter by start date
  if (filters.value.startDate) {
    filtered = filtered.filter((promotion) => {
      const promotionStart = new Date(promotion.start_date)
      return promotionStart >= filters.value.startDate
    })
  }

  // Filter by end date
  if (filters.value.endDate) {
    filtered = filtered.filter((promotion) => {
      const promotionEnd = new Date(promotion.end_date)
      return promotionEnd <= filters.value.endDate
    })
  }

  // Filter by percentage range
  if (filters.value.percentageRange) {
    const [minPercentage, maxPercentage] = filters.value.percentageRange
    filtered = filtered.filter((promotion) => promotion.percentage >= minPercentage && promotion.percentage <= maxPercentage)
  }

  // Filter by status
  if (filters.value.status && filters.value.status !== '') {
    filtered = filtered.filter((promotion) => promotion.status === filters.value.status)
  }

  return filtered
})

// Pagination
const pagination = computed(() => ({
  current: 1,
  pageSize: 10,
  total: filteredPromotions.value.length,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: true,
}))

// Methods

const loadPromotions = async () => {
  loading.value = true
  try {
    const data = await fetchPromotionCampaigns()
    // Sort by createdAt descending (newest first), fallback to id if createdAt is missing
    const sorted = data.sort((a, b) => {
      if (a.createdAt && b.createdAt) {
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      }
      // Fallback to id descending if createdAt is not available
      return b.id - a.id
    })
    promotions.value = sorted.map((item, index) => toPromotionRecord(item, index))
  } catch {
    Message.error({ content: t('discount.message.loadCampaignsFailed'), duration: 5000 })
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString: string) => {
  if (!dateString) {
    return ''
  }
  const date = new Date(dateString)
  if (Number.isNaN(date.getTime())) {
    return ''
  }
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
}

const formatDateTime = (dateString: string) => {
  if (!dateString) {
    return ''
  }
  const date = new Date(dateString)
  if (Number.isNaN(date.getTime())) {
    return ''
  }
  return date.toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const getStatusColor = (status: string) => {
  switch (status) {
    case 'active':
      return 'green'
    case 'expired':
      return 'red'
    case 'upcoming':
      return 'orange'
    case 'inactive':
      return 'gray'
    default:
      return 'default'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'active':
      return t('discount.campaign.status.active')
    case 'expired':
      return t('discount.campaign.status.expired')
    case 'upcoming':
      return t('discount.campaign.status.upcoming')
    case 'inactive':
      return t('discount.campaign.status.inactive')
    default:
      return status
  }
}

const searchPromotions = () => {
  // Filtering is now handled by the computed filteredPromotions property
  // This function is called when filters change to trigger reactivity
}

const handleTableSearch = () => {
  // Table search is reactive via tableSearch ref
  // This function is called when user types or clears the search input
}

const openCreatePage = () => {
  router.push({ name: 'QuanLyDotGiamGiaCreate' })
}

// History related functions
const loadHistory = async (promotionId: number) => {
  isHistoryLoading.value = true
  try {
    const data = await fetchPromotionHistory(promotionId)
    historyList.value = data
  } catch {
    Message.error({ content: t('discount.message.loadHistoryFailed'), duration: 3000 })
    historyList.value = []
  } finally {
    isHistoryLoading.value = false
  }
}

const getActionColor = (action: string): string => {
  if (action.includes('TẠO')) return 'green'
  if (action.includes('CẬP NHẬT')) return 'blue'
  if (action.includes('XÓA')) return 'red'
  return 'gray'
}

const formatHistoryDate = (dateString: string): string => {
  if (!dateString) return ''
  const date = new Date(dateString)
  if (Number.isNaN(date.getTime())) return ''

  return date.toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const fetchProducts = async () => {
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
    Message.error(t('discount.message.loadProductsFailed'))
    productOptions.value = []
  } finally {
    productOptionsLoading.value = false
  }
}

const loadPromotionProductDetails = async (promotionId: number) => {
  isProductDetailsLoading.value = true
  try {
    const productDetailIds = await fetchPromotionProductDetails(promotionId)
    const productIds = productDetailIds.map((detail: PromotionProductDetailApiModel) => detail.idChiTietSanPham)

    // Fetch all products if not already loaded
    if (productOptions.value.length === 0) {
      await fetchProducts()
    }

    // Filter products by IDs
    promotionProductDetails.value = productOptions.value.filter((product) => productIds.includes(product.id))
  } catch {
    Message.error({ content: t('discount.message.loadProductsFailed'), duration: 3000 })
    promotionProductDetails.value = []
  } finally {
    isProductDetailsLoading.value = false
  }
}

const viewPromotion = async (promotion: any) => {
  selectedPromotion.value = promotion
  detailVisible.value = true
  // Load history and products when opening detail modal
  await Promise.all([loadHistory(promotion.id), loadPromotionProductDetails(promotion.id)])
}

// Product table configuration
const productColumns = [
  {
    title: 'Ảnh',
    dataIndex: 'anhSanPham',
    slotName: 'anhSanPham',
    width: 80,
    align: 'center' as const,
  },
  {
    title: 'Sản phẩm',
    dataIndex: 'tenSanPham',
    slotName: 'productInfo',
    width: 220,
    ellipsis: true,
    tooltip: true,
  },
  {
    title: 'Mã biến thể',
    dataIndex: 'maChiTietSanPham',
    width: 140,
  },
  {
    title: 'Giá bán',
    dataIndex: 'giaBan',
    slotName: 'giaBan',
    width: 150,
    align: 'right' as const,
  },
  {
    title: 'Tồn kho',
    dataIndex: 'soLuong',
    width: 90,
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

const filteredProductsEdit = computed(() => {
  if (!productSearchQuery.value) {
    return productOptions.value
  }
  const query = productSearchQuery.value.trim().toLowerCase()
  return productOptions.value.filter((product) => {
    const baseName = product.tenSanPham?.toLowerCase() ?? ''
    const variantName = product.tenSanPhamChiTiet?.toLowerCase() ?? ''
    const attributes = buildVariantLabel(product).toLowerCase()
    const sku = product.maChiTietSanPham?.toLowerCase() ?? ''
    return baseName.includes(query) || variantName.includes(query) || attributes.includes(query) || sku.includes(query)
  })
})

const productPagination = computed(() => ({
  pageSize: 5,
  showTotal: true,
  showPageSize: false,
}))

const isAllProductsSelectedEdit = computed(() => {
  if (filteredProductsEdit.value.length === 0) return false
  return filteredProductsEdit.value.every((product) => promotionEditForm.selectedProducts.includes(product.id))
})

const isSomeProductsSelectedEdit = computed(() => {
  return promotionEditForm.selectedProducts.length > 0
})

// Dynamic modal width based on whether products are selected
const editModalWidth = computed(() => {
  return promotionEditForm.applyToProducts ? '1200px' : '600px'
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
    // Deselect all filtered products
    filteredProductsEdit.value.forEach((product) => {
      const index = promotionEditForm.selectedProducts.indexOf(product.id)
      if (index > -1) {
        promotionEditForm.selectedProducts.splice(index, 1)
      }
    })
  } else {
    // Select all filtered products
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

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.style.display = 'none'
  // Show placeholder instead
  const placeholder = img.parentElement?.querySelector('.product-image-placeholder')
  if (placeholder) {
    ;(placeholder as HTMLElement).style.display = 'flex'
  }
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const loadProductsForPromotion = async (promotionId: number) => {
  try {
    const productDetails = await fetchPromotionProductDetails(promotionId)
    const productIds = productDetails.map((detail: PromotionProductDetailApiModel) => detail.idChiTietSanPham)
    promotionEditForm.selectedProducts = productIds
    originalProductIds.value = [...productIds]
  } catch {
    promotionEditForm.selectedProducts = []
    originalProductIds.value = []
  }
}

const editPromotion = async (promotion: any) => {
  // Navigate to edit page
  router.push({ name: 'QuanLyDotGiamGiaEdit', params: { id: promotion.id } })
}

const isPromotionStatusUpdating = (id: number) => promotionStatusUpdatingIds.value.includes(id)

const canTogglePromotionStatus = (promotion: PromotionRecord) => promotion.status !== 'expired'

const handlePromotionStatusToggle = async (promotion: PromotionRecord, nextValue: boolean) => {
  if (!promotion) return

  const previousValue = promotion.enabled
  if (nextValue === previousValue) {
    return
  }

  if (!canTogglePromotionStatus(promotion)) {
    Message.warning('Không thể thay đổi trạng thái cho đợt giảm giá đã hết hạn')
    return
  }

  if (isPromotionStatusUpdating(promotion.id)) {
    return
  }

  promotionStatusUpdatingIds.value.push(promotion.id)

  const previousStatus = promotion.status
  promotion.source.trangThai = nextValue
  promotion.enabled = nextValue
  promotion.status = derivePromotionStatus({ ...promotion.source, trangThai: nextValue })
  if (selectedPromotion.value?.id === promotion.id) {
    selectedPromotion.value = { ...promotion }
  }

  const payload = {
    maDotGiamGia: promotion.source.maDotGiamGia ?? promotion.code,
    tenDotGiamGia: promotion.source.tenDotGiamGia ?? promotion.name,
    giaTriGiamGia: Number(promotion.source.giaTriGiamGia ?? promotion.percentage),
    ngayBatDau: promotion.source.ngayBatDau ?? promotion.start_date,
    ngayKetThuc: promotion.source.ngayKetThuc ?? promotion.end_date,
    trangThai: nextValue,
    deleted: Boolean(promotion.source.deleted),
    lyDoThayDoi: nextValue ? 'Bật trạng thái đợt giảm giá' : 'Tắt trạng thái đợt giảm giá',
  }

  try {
    await updatePromotionCampaign(promotion.id, payload)
    Message.success(nextValue ? 'Đã bật đợt giảm giá' : 'Đã tắt đợt giảm giá')
    await loadPromotions()
    if (detailVisible.value && selectedPromotion.value?.id === promotion.id) {
      const refreshed = promotions.value.find((item) => item.id === promotion.id)
      if (refreshed) {
        selectedPromotion.value = refreshed
      }
    }
  } catch (error: any) {
    promotion.source.trangThai = previousValue
    promotion.enabled = previousValue
    promotion.status = previousStatus
    if (selectedPromotion.value?.id === promotion.id) {
      selectedPromotion.value = { ...promotion, status: previousStatus }
    }
    const errorMessage = error?.response?.data?.message || error?.message || 'Không thể cập nhật trạng thái đợt giảm giá'
    Message.error(errorMessage)
  } finally {
    promotionStatusUpdatingIds.value = promotionStatusUpdatingIds.value.filter((id) => id !== promotion.id)
  }
}

const resetFilters = async () => {
  filters.value = createDefaultFilters()
  // Reset to show all promotions
  searchPromotions()
  await loadPromotions()
}

const openExportConfirmModal = () => {
  if (!filteredPromotions.value.length) {
    Message.warning('Không có dữ liệu để xuất')
    return
  }
  exportConfirmVisible.value = true
}

const confirmExportExcel = async () => {
  if (exportSubmitting.value) return

  exportSubmitting.value = true
  try {
    const header = ['STT', 'Mã', 'Tên', 'Phần trăm giảm', 'Ngày bắt đầu', 'Ngày kết thúc', 'Trạng thái']
    const rows = filteredPromotions.value.map((promotion, index) => [
      promotion.index ?? index + 1,
      promotion.code ?? '',
      promotion.name ?? '',
      promotion.percentage ?? '',
      formatDate(promotion.start_date ?? ''),
      formatDate(promotion.end_date ?? ''),
      getStatusText(promotion.status ?? ''),
    ])

    downloadCsv('dot-giam-gia.csv', header, rows)
    Message.success('Xuất danh sách đợt giảm giá thành công')
    exportConfirmVisible.value = false
  } catch {
    Message.error('Không thể xuất danh sách đợt giảm giá')
  } finally {
    exportSubmitting.value = false
  }
}

const handleEditOkClick = async () => {
  if (!selectedPromotion.value) return false
  if (editSubmitting.value) return false

  const form = promotionEditFormRef.value
  if (!form) return false

  try {
    await form.validate()
  } catch {
    Message.error('Vui lòng kiểm tra lại các trường thông tin')
    return false
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
    return false
  }

  // Validation passed, show confirmation modal
  editConfirmVisible.value = true
  return false // Prevent auto-close, confirmation modal will handle actual submission
}

const submitPromotionEdit = async () => {
  if (!selectedPromotion.value) return
  if (editSubmitting.value) return

  const [startDate, endDate] = promotionEditForm.dateRange
  const payload = {
    tenDotGiamGia: promotionEditForm.name.trim(),
    giaTriGiamGia: Number(promotionEditForm.discountValue),
    ngayBatDau: startDate,
    ngayKetThuc: endDate,
    trangThai: promotionEditForm.active,
    deleted: Boolean(selectedPromotion.value.source?.deleted),
    lyDoThayDoi: promotionEditForm.lyDoThayDoi.trim(),
  }

  editSubmitting.value = true
  try {
    // Step 1: Update promotion campaign
    await updatePromotionCampaign(selectedPromotion.value.id, payload)

    // Step 2: Update product associations if changed
    const currentProducts = new Set(promotionEditForm.selectedProducts)
    const originalProducts = new Set(originalProductIds.value)

    // Find products that need to be added
    const productsToAdd = Array.from(currentProducts).filter((id) => !originalProducts.has(id))

    if (productsToAdd.length > 0) {
      const productDetailPayloads = productsToAdd.map((productId) => ({
        idDotGiamGia: selectedPromotion.value!.id,
        idChiTietSanPham: productId,
        deleted: false,
      }))
      await createPromotionProductDetailsBatch(productDetailPayloads)
    }

    // Note: Removing products requires additional backend endpoint for deletion
    // which should mark chi_tiet_dot_giam_gia.deleted = true
    // This can be implemented if needed

    Message.success('Cập nhật đợt giảm giá thành công')
    editConfirmVisible.value = false
    editVisible.value = false
    await loadPromotions()
  } catch {
    Message.error('Không thể cập nhật đợt giảm giá')
  } finally {
    editSubmitting.value = false
  }
}

onMounted(() => {
  loadPromotions()
  fetchProducts()
})
</script>

<style scoped>
.promotion-management-page {
  padding: 16px 20px;
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
  color: var(--color-text-1);
  margin: 0 0 8px 0;
}

.page-description {
  font-size: 16px;
  color: var(--color-text-3);
}

.modal-note {
  margin-top: 4px;
  color: var(--color-text-3);
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

.revenue-icon {
  color: #52c41a;
}

.growth-icon {
  color: #faad14;
}

.pending-icon {
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
.table-card {
  margin-bottom: 16px;
}

.promotion-name-cell {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.promotion-title {
  font-weight: 600;
  color: #1d2129;
}

.promotion-type {
  display: flex;
}

.discount-value {
  text-align: center;
  font-weight: 600;
  color: #1890ff;
}

.percentage-value {
  font-weight: 600;
  color: #1890ff;
}

.percentage-badge {
  display: inline-block;
  font-weight: 700;
  font-size: 16px;
  color: #1890ff;
  background: rgba(24, 144, 255, 0.1);
  padding: 4px 12px;
  border-radius: 6px;
}

.percentage-filter {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

:deep(.percentage-slider-control) {
  width: 100%;
  display: block;
}

:deep(.percentage-slider-control .arco-slider-rail),
:deep(.percentage-slider-control .arco-slider-track) {
  width: 100%;
}

.percentage-filter-values {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: var(--color-text-2);
}

.percentage-value-badge {
  padding: 2px 10px;
  border-radius: 999px;
  background: var(--color-fill-3);
  font-weight: 600;
  color: var(--color-text-1);
}

.percentage-filter-separator {
  font-weight: 500;
  color: var(--color-text-2);
}

.date-range {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 13px;
  line-height: 1.4;
}

.code-cell {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-weight: 600;
  font-size: 12px;
  word-break: break-all;
  line-height: 1.5;
}

.name-cell {
  word-wrap: break-word;
  word-break: break-word;
  white-space: normal;
  line-height: 1.5;
  font-size: 13px;
}

.status-cell {
  word-wrap: break-word;
  word-break: break-word;
  white-space: normal;
  line-height: 1.3;
  font-size: 11px;
  text-align: center;
  font-weight: 500;
  padding: 2px 6px;
  border-radius: 3px;
}

.status-active {
  color: #52c41a;
  background-color: rgba(82, 196, 26, 0.1);
}

.status-expired {
  color: #ff4d4f;
  background-color: rgba(255, 77, 79, 0.1);
}

.status-upcoming {
  color: #fa8c16;
  background-color: rgba(250, 140, 22, 0.1);
}

.status-inactive {
  color: #8c8c8c;
  background-color: rgba(140, 140, 140, 0.1);
}

/* Fix action buttons alignment */
:deep(.arco-table-td) .arco-space {
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.arco-table-td) .arco-btn-text {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

/* Status tag wrapper to ensure proper rendering */
.status-tag-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.status-tag-wrapper .arco-tag {
  border-radius: 4px !important;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: fit-content;
}

.actions-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.percentage-slider {
  padding: 16px;
  border: 1px solid var(--color-border-2);
  border-radius: 6px;
  background: var(--color-bg-3);
  margin-bottom: 8px;
}

.slider-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: var(--color-text-2);
  margin-bottom: 8px;
}

.slider-min,
.slider-max {
  color: var(--color-text-2);
  font-weight: 400;
}

.slider-range {
  font-weight: 600;
  color: var(--color-text-1);
  background: var(--color-fill-3);
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 14px;
}

/* History section styles */
.history-section {
  margin-top: 16px;
  padding-left: 8px;
  margin-left: -8px;
  overflow-x: visible;
}

/* Ensure timeline has enough space */
.history-section :deep(.arco-timeline) {
  padding-left: 4px;
}

.history-section :deep(.arco-timeline-item) {
  position: relative;
}

/* Fix timeline dot icon alignment */
.history-section :deep(.arco-timeline-item-dot) {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: visible !important;
}

.history-section :deep(.arco-timeline-item-dot-custom) {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--color-bg-2);
  overflow: visible !important;
}

.timeline-icon-wrapper {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  line-height: 1;
}

.timeline-icon-wrapper svg {
  width: 100%;
  height: 100%;
  display: block;
}

.history-item {
  margin-bottom: 8px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.history-time {
  font-size: 12px;
  color: var(--color-text-3);
}

.history-user {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: var(--color-text-2);
  margin-bottom: 4px;
}

.history-description {
  font-size: 13px;
  color: var(--color-text-2);
  padding: 8px 12px;
  background: var(--color-fill-2);
  border-radius: 4px;
  margin-top: 4px;
}

.timestamp-value {
  font-size: 13px;
  color: var(--color-text-2);
}

/* Modal Edit Layout - 2 Columns */
.modal-edit-layout {
  display: flex;
  gap: 24px;
  min-height: 300px;
}

.modal-left-column {
  flex: 0 0 380px;
  min-width: 380px;
  max-width: 100%;
}

/* When no right column exists, left column should take full width */
.modal-edit-layout:has(.modal-left-column:only-child) .modal-left-column {
  flex: 1;
  max-width: 100%;
}

.modal-right-column {
  flex: 1;
  min-width: 0;
  border-left: 1px solid var(--color-border-2);
  padding-left: 24px;
}

/* Product Selection Section Styles */
.product-selection-section {
  border: 1px solid var(--color-border-2);
  border-radius: 8px;
  padding: 16px;
  background: var(--color-bg-2);
  margin-top: 16px;
}

.product-selection-section-inline {
  height: 100%;
  display: flex;
  flex-direction: column;
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

.product-selection-section :deep(.arco-table),
.product-selection-section-inline :deep(.arco-table) {
  border-radius: 4px;
}

.product-selection-section :deep(.arco-table-container),
.product-selection-section-inline :deep(.arco-table-container) {
  border: 1px solid var(--color-border-2);
  border-radius: 4px;
}

.product-selection-section :deep(.arco-table-th),
.product-selection-section-inline :deep(.arco-table-th) {
  background-color: var(--color-fill-2);
  font-weight: 600;
}

.product-selection-section :deep(.arco-table-td),
.product-selection-section-inline :deep(.arco-table-td) {
  border-bottom: 1px solid var(--color-border-2);
}

.product-selection-section :deep(.arco-table-tr:hover),
.product-selection-section-inline :deep(.arco-table-tr:hover) {
  background-color: var(--color-fill-1);
}

.product-selection-section :deep(.arco-checkbox),
.product-selection-section-inline :deep(.arco-checkbox) {
  pointer-events: auto !important;
  cursor: pointer !important;
}

.product-selection-section :deep(.arco-checkbox-icon),
.product-selection-section-inline :deep(.arco-checkbox-icon) {
  pointer-events: auto !important;
}

.product-selection-section :deep(.arco-table-cell),
.product-selection-section-inline :deep(.arco-table-cell) {
  pointer-events: auto !important;
}

.product-selection-section :deep(.arco-table-td),
.product-selection-section-inline :deep(.arco-table-td) {
  position: relative;
  z-index: 1;
}

.product-selection-section :deep(.arco-table-th .arco-checkbox),
.product-selection-section-inline :deep(.arco-table-th .arco-checkbox) {
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

/* Modal with fixed footer */
:deep(.arco-modal) {
  display: flex;
  flex-direction: column;
  max-height: 85vh;
}

:deep(.arco-modal-header) {
  flex-shrink: 0;
  border-bottom: 1px solid var(--color-border-2);
}

:deep(.arco-modal-body) {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  min-height: 0;
  max-height: calc(85vh - 120px); /* Reserve space for header and footer */
}

:deep(.arco-modal-footer) {
  flex-shrink: 0;
  position: sticky;
  bottom: 0;
  background: var(--color-bg-1);
  border-top: 1px solid var(--color-border-2);
  margin-top: 0 !important;
  padding: 16px 20px;
  z-index: 10;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.08);
}
</style>
