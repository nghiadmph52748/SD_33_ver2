<template>
  <div class="promotion-management-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="filters" layout="vertical">
        <!-- Row 1: Search - Start Date - End Date - Percentage -->
        <a-row :gutter="16">
          <a-col :span="6">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="filters.search" placeholder="Tên giảm giá..." allow-clear @change="searchPromotions" />
            </a-form-item>
          </a-col>
          <a-col :span="5">
            <a-form-item label="Ngày bắt đầu">
              <a-date-picker
                v-model="filters.startDate"
                placeholder="Chọn ngày bắt đầu"
                allow-clear
                @change="searchPromotions"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="5">
            <a-form-item label="Ngày kết thúc">
              <a-date-picker
                v-model="filters.endDate"
                placeholder="Chọn ngày kết thúc"
                allow-clear
                @change="searchPromotions"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="Phần trăm giảm">
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
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="filters.status" type="button" @change="searchPromotions">
                <a-radio value="">Tất cả</a-radio>
                <a-radio value="active">Đang hoạt động</a-radio>
                <a-radio value="expired">Đã kết thúc</a-radio>
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
              Đặt lại
            </a-button>
            <a-button @click="openExportConfirmModal">
              <template #icon>
                <icon-download />
              </template>
              Xuất Excel
            </a-button>
            <a-button type="primary" @click="openCreatePage">
              <template #icon>
                <icon-plus />
              </template>
              Thêm đợt giảm giá
            </a-button>
          </a-space>
        </div>
      </a-form>
    </a-card>

    <!-- Promotions Table -->
    <a-card title="Danh sách giảm giá" class="table-card">
      <a-table :columns="columns" :data="filteredPromotions" :pagination="pagination" :loading="loading" :scroll="{ x: 1000 }">
        <template #percentage="{ record }">
          <span class="percentage-value">{{ record.percentage }}%</span>
        </template>

        <template #date_range="{ record }">
          <div class="date-range">{{ formatDateTime(record.start_date) }} - {{ formatDateTime(record.end_date) }}</div>
        </template>

        <template #status="{ record }">
          <div class="status-tag-wrapper">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </div>
        </template>

        <template #action="{ record }">
          <a-space>
            <a-button type="text" @click="viewPromotion(record)">
              <template #icon>
                <icon-eye />
              </template>
            </a-button>
            <a-button type="text" @click="editPromotion(record)">
              <template #icon>
                <icon-edit />
              </template>
            </a-button>
            <a-button type="text" status="danger" @click="deletePromotion(record)">
              <template #icon>
                <icon-delete />
              </template>
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:visible="detailVisible" title="Chi tiết đợt giảm giá" :footer="false" width="800px">
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="Tên">{{ selectedPromotion?.name }}</a-descriptions-item>
        <a-descriptions-item label="Mã">{{ selectedPromotion?.code }}</a-descriptions-item>
        <a-descriptions-item label="Giá trị giảm">
          <span class="percentage-badge">{{ selectedPromotion?.percentage }}%</span>
        </a-descriptions-item>
        <a-descriptions-item label="Trạng thái">
          <a-tag :color="getStatusColor(selectedPromotion?.status ?? '')">{{ selectedPromotion ? getStatusText(selectedPromotion.status) : '' }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Thời gian" :span="2">
          {{ formatDateTime(selectedPromotion?.start_date ?? '') }} - {{ formatDateTime(selectedPromotion?.end_date ?? '') }}
        </a-descriptions-item>
        <a-descriptions-item label="Ngày tạo">
          {{ selectedPromotion?.created_at ? formatDateTime(selectedPromotion.created_at) : '—' }}
        </a-descriptions-item>
        <a-descriptions-item label="Ngày cập nhật">
          {{ selectedPromotion?.updated_at ? formatDateTime(selectedPromotion.updated_at) : '—' }}
        </a-descriptions-item>
      </a-descriptions>

      <!-- History Section -->
      <a-divider style="margin: 24px 0" />
      <div class="history-section">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
          <h3 style="margin: 0; font-size: 16px; font-weight: 600">Lịch sử thay đổi</h3>
          <div style="display: flex; align-items: center; gap: 8px">
            <a-spin v-if="isHistoryLoading" size="small" />
            <a-button v-else size="small" type="text" @click="selectedPromotion && loadHistory(selectedPromotion.id)">
              <template #icon>
                <icon-refresh />
              </template>
              Tải lại
            </a-button>
          </div>
        </div>

        <div v-if="!isHistoryLoading && historyList.length === 0" style="text-align: center; padding: 32px; color: var(--color-text-3)">
          <icon-empty style="font-size: 48px; margin-bottom: 8px" />
          <div>Chưa có lịch sử thay đổi</div>
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
                value-format="YYYY-MM-DD HH:mm:ss"
                format="DD/MM/YYYY HH:mm"
                allow-clear
                style="width: 100%"
              />
            </a-form-item>
            <a-form-item field="active" label="Trạng thái">
              <a-switch
                v-model="promotionEditForm.active"
                checked-children="Hoạt động"
                un-checked-children="Không hoạt động"
              />
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

    <a-modal
      v-model:visible="deleteVisible"
      title="Xóa đợt giảm giá"
      :confirm-loading="deleteSubmitting"
      width="420px"
      ok-text="Xóa"
      :ok-button-props="{ status: 'danger' }"
      @ok="confirmDeletePromotion"
      @cancel="deleteVisible = false"
    >
      <p>
        Bạn chắc chắn muốn xóa đợt giảm giá
        <strong>{{ selectedPromotion?.name }}</strong>
        ?
      </p>
      <p class="modal-note">Hành động này không thể hoàn tác.</p>
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
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import downloadCsv from '@/utils/export-csv'
import {
  deletePromotionCampaign,
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
import {
  IconPlus,
  IconRefresh,
  IconDownload,
  IconEye,
  IconEdit,
  IconDelete,
  IconPlusCircle,
  IconUser,
  IconEmpty,
  IconImage,
} from '@arco-design/web-vue/es/icon'

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()
const router = useRouter()

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

// Table
const loading = ref(false)

const columns = [
  {
    title: 'STT',
    dataIndex: 'index',
    width: 70,
    align: 'center',
  },
  {
    title: 'Mã',
    dataIndex: 'code',
    width: 100,
  },
  {
    title: 'Tên',
    dataIndex: 'name',
    width: 200,
  },
  {
    title: 'Phần trăm giảm',
    dataIndex: 'percentage',
    slotName: 'percentage',
    width: 130,
    align: 'center',
  },
  {
    title: 'Thời gian',
    dataIndex: 'start_date',
    slotName: 'date_range',
    width: 250,
    align: 'center',
  },
  {
    title: 'Trạng thái',
    dataIndex: 'status',
    slotName: 'status',
    width: 110,
    align: 'center',
  },
  {
    title: 'Thao tác',
    slotName: 'action',
    width: 150,
    fixed: 'right',
    align: 'center',
  },
]

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
  created_at?: string
  updated_at?: string
  source: PromotionApiModel
}

const promotions = ref<PromotionRecord[]>([])
const selectedPromotion = ref<PromotionRecord | null>(null)

const detailVisible = ref(false)
const editVisible = ref(false)
const editConfirmVisible = ref(false)
const deleteVisible = ref(false)
const editSubmitting = ref(false)
const deleteSubmitting = ref(false)

// Export confirmation modal
const exportConfirmVisible = ref(false)
const exportSubmitting = ref(false)

// History state
const historyList = ref<PromotionHistoryApiModel[]>([])
const isHistoryLoading = ref(false)

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
  applyToProducts: false,
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

const toPromotionRecord = (promotion: PromotionApiModel, index: number): PromotionRecord => ({
  id: promotion.id,
  index: index + 1,
  code: promotion.maDotGiamGia ?? '',
  name: promotion.tenDotGiamGia ?? '',
  percentage: Number(promotion.giaTriGiamGia ?? 0),
  start_date: promotion.ngayBatDau ?? '',
  end_date: promotion.ngayKetThuc ?? '',
  status: derivePromotionStatus(promotion),
  created_at: promotion.createdAt,
  updated_at: promotion.updatedAt,
  source: promotion,
})

// Filtered promotions computed
const filteredPromotions = computed(() => {
  let filtered = promotions.value

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
    Message.error({ content: 'Không thể tải danh sách đợt giảm giá', duration: 5000 })
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
      return 'Đang hoạt động'
    case 'expired':
      return 'Đã kết thúc'
    case 'upcoming':
      return 'Sắp diễn ra'
    case 'inactive':
      return 'Tạm dừng'
    default:
      return status
  }
}

const searchPromotions = () => {
  // Filtering is now handled by the computed filteredPromotions property
  // This function is called when filters change to trigger reactivity
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
    Message.error({ content: 'Không thể tải lịch sử thay đổi', duration: 3000 })
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

const viewPromotion = async (promotion: any) => {
  selectedPromotion.value = promotion
  detailVisible.value = true
  // Load history when opening detail modal
  await loadHistory(promotion.id)
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

const filteredProductsEdit = computed(() => {
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

const loadProductsForPromotion = async (promotionId: number) => {
  try {
    const productDetails = await fetchPromotionProductDetails(promotionId)
    const productIds = productDetails.map((detail: PromotionProductDetailApiModel) => detail.idChiTietSanPham)
    promotionEditForm.selectedProducts = productIds
    originalProductIds.value = [...productIds]
  } catch (error) {
    console.error('Failed to load products for promotion:', error)
    promotionEditForm.selectedProducts = []
    originalProductIds.value = []
  }
}

const editPromotion = async (promotion: any) => {
  // Navigate to edit page
  router.push({ name: 'QuanLyDotGiamGiaEdit', params: { id: promotion.id } })
}

const deletePromotion = (promotion: any) => {
  selectedPromotion.value = promotion
  deleteVisible.value = true
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
  } catch (error) {
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
  const hasChanges = (
    promotionEditForm.name !== originalPromotionEditForm.name ||
    promotionEditForm.discountValue !== originalPromotionEditForm.discountValue ||
    promotionEditForm.active !== originalPromotionEditForm.active ||
    promotionEditForm.dateRange[0] !== originalPromotionEditForm.dateRange[0] ||
    promotionEditForm.dateRange[1] !== originalPromotionEditForm.dateRange[1] ||
    promotionEditForm.applyToProducts !== originalPromotionEditForm.applyToProducts ||
    JSON.stringify([...promotionEditForm.selectedProducts].sort()) !== JSON.stringify([...originalPromotionEditForm.selectedProducts].sort())
  )

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
    const productsToAdd = Array.from(currentProducts).filter(id => !originalProducts.has(id))
    
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

const confirmDeletePromotion = async () => {
  if (!selectedPromotion.value) return
  if (deleteSubmitting.value) return

  deleteSubmitting.value = true
  try {
    await deletePromotionCampaign(selectedPromotion.value.id)
    Message.success('Đã xóa đợt giảm giá')
    deleteVisible.value = false
    await loadPromotions()
  } catch {
    Message.error('Không thể xóa đợt giảm giá')
  } finally {
    deleteSubmitting.value = false
  }
}

onMounted(() => {
  loadPromotions()
  fetchProducts()
})
</script>

<style scoped>
.promotion-management-page {
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
  text-align: center;
  font-size: 14px;
  color: var(--color-text-1);
}

.danger-item {
  color: #ff4d4f;
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
