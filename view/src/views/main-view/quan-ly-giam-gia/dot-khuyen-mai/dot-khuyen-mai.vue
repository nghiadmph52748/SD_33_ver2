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
          <a-tag :color="getStatusColor(record.status)">
            {{ getStatusText(record.status) }}
          </a-tag>
        </template>

        <template #created_at="{ record }">
          <div class="timestamp-value">{{ record.created_at ? formatDateTime(record.created_at) : '-' }}</div>
        </template>

        <template #updated_at="{ record }">
          <div class="timestamp-value">{{ record.updated_at ? formatDateTime(record.updated_at) : '-' }}</div>
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

    <a-modal v-model:visible="detailVisible" title="Chi tiết đợt giảm giá" :footer="false" width="700px">
      <a-descriptions :column="1" layout="vertical">
        <a-descriptions-item label="Tên">{{ selectedPromotion?.name }}</a-descriptions-item>
        <a-descriptions-item label="Mã">{{ selectedPromotion?.code }}</a-descriptions-item>
        <a-descriptions-item label="Giá trị giảm">{{ selectedPromotion?.percentage }}%</a-descriptions-item>
        <a-descriptions-item label="Thời gian">
          {{ formatDateTime(selectedPromotion?.start_date ?? '') }} - {{ formatDateTime(selectedPromotion?.end_date ?? '') }}
        </a-descriptions-item>
        <a-descriptions-item label="Trạng thái">{{ selectedPromotion ? getStatusText(selectedPromotion.status) : '' }}</a-descriptions-item>
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
              <icon-plus-circle v-if="history.hanhDong.includes('TẠO')" style="font-size: 14px" />
              <icon-edit v-else-if="history.hanhDong.includes('CẬP NHẬT')" style="font-size: 14px" />
              <icon-delete v-else-if="history.hanhDong.includes('XÓA')" style="font-size: 14px" />
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
      :confirm-loading="editSubmitting"
      @ok="submitPromotionEdit"
      @cancel="editVisible = false"
      width="560px"
    >
      <a-form ref="promotionEditFormRef" :model="promotionEditForm" :rules="promotionEditRules" layout="vertical">
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
          <a-switch v-model="promotionEditForm.active">
            <template #checked>Hoạt động</template>
            <template #unchecked>Tạm dừng</template>
          </a-switch>
        </a-form-item>
      </a-form>
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
} from '@/api/discount-management'
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
    width: 120,
    align: 'center',
  },
  {
    title: 'Ngày tạo',
    dataIndex: 'created_at',
    slotName: 'created_at',
    width: 180,
    align: 'center',
  },
  {
    title: 'Ngày cập nhật',
    dataIndex: 'updated_at',
    slotName: 'updated_at',
    width: 180,
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
const deleteVisible = ref(false)
const editSubmitting = ref(false)
const deleteSubmitting = ref(false)

// Export confirmation modal
const exportConfirmVisible = ref(false)
const exportSubmitting = ref(false)

// History state
const historyList = ref<PromotionHistoryApiModel[]>([])
const isHistoryLoading = ref(false)

const promotionEditFormRef = ref<FormInstance>()
const promotionEditForm = reactive({
  name: '',
  discountValue: 10,
  dateRange: [] as string[],
  active: true,
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
  router.push({ name: 'QuanLyDotKhuyenMaiCreate' })
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

const editPromotion = (promotion: any) => {
  selectedPromotion.value = promotion
  promotionEditForm.name = promotion.name ?? ''
  promotionEditForm.discountValue = Number(promotion.percentage ?? 0)
  promotionEditForm.active = Boolean(promotion.source?.trangThai)
  promotionEditForm.dateRange = [promotion.start_date ?? '', promotion.end_date ?? ''].filter(Boolean) as string[]
  editVisible.value = true
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

const submitPromotionEdit = async () => {
  if (!selectedPromotion.value) return
  if (editSubmitting.value) return

  const form = promotionEditFormRef.value
  if (!form) return

  try {
    await form.validate()
  } catch {
    return
  }

  const [startDate, endDate] = promotionEditForm.dateRange
  const payload = {
    tenDotGiamGia: promotionEditForm.name.trim(),
    giaTriGiamGia: Number(promotionEditForm.discountValue),
    ngayBatDau: startDate,
    ngayKetThuc: endDate,
    trangThai: promotionEditForm.active,
    deleted: Boolean(selectedPromotion.value.source?.deleted),
  }

  editSubmitting.value = true
  try {
    await updatePromotionCampaign(selectedPromotion.value.id, payload)
    Message.success('Cập nhật đợt giảm giá thành công')
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
</style>
