<template>
  <div class="coupon-management-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="filters" layout="vertical">
        <!-- Row 1: Search - Start Date - End Date -->
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="filters.search" placeholder="Mã, tên, giá trị giảm..." allow-clear @change="searchCoupons" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="Ngày bắt đầu">
              <a-date-picker
                v-model="filters.startDate"
                placeholder="Chọn ngày bắt đầu"
                allow-clear
                @change="searchCoupons"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="Ngày kết thúc">
              <a-date-picker
                v-model="filters.endDate"
                placeholder="Chọn ngày kết thúc"
                allow-clear
                @change="searchCoupons"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Row 2: Discount Type - Status -->
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="Loại giảm giá">
              <a-radio-group v-model="filters.discountType" type="button" @change="searchCoupons">
                <a-radio value="">Tất cả</a-radio>
                <a-radio value="percentage">Giảm %</a-radio>
                <a-radio value="fixed">Giảm tiền</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="filters.status" type="button" @change="searchCoupons">
                <a-radio value="">Tất cả</a-radio>
                <a-radio value="active">Đang hoạt động</a-radio>
                <a-radio value="expired">Đã hết hạn</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <!-- Empty column for alignment -->
          </a-col>
        </a-row>

        <div class="slider-grid">
          <div class="slider-block">
            <div class="slider-title">Giá trị giảm (%)</div>
            <a-slider v-model="filters.discountPercentRange" range :min="0" :max="100" @change="searchCoupons" />
            <div class="slider-values">
              <span class="slider-value-badge">{{ filters.discountPercentRange[0] }}%</span>
              <span class="slider-value-separator">-</span>
              <span class="slider-value-badge">{{ filters.discountPercentRange[1] }}%</span>
            </div>
          </div>

          <div class="slider-block">
            <div class="slider-title">Giá trị giảm (VNĐ)</div>
            <a-slider
              v-model="filters.discountAmountRange"
              range
              :min="0"
              :max="DEFAULT_DISCOUNT_AMOUNT_RANGE[1]"
              :step="10000"
              @change="searchCoupons"
            />
            <div class="slider-values">
              <span class="slider-value-badge">{{ formatCurrency(filters.discountAmountRange[0]) }}</span>
              <span class="slider-value-separator">-</span>
              <span class="slider-value-badge">{{ formatCurrency(filters.discountAmountRange[1]) }}</span>
            </div>
          </div>

          <div class="slider-block">
            <div class="slider-title">Đơn hàng tối thiểu</div>
            <a-slider
              v-model="filters.minOrderRange"
              range
              :min="0"
              :max="DEFAULT_MIN_ORDER_RANGE[1]"
              :step="50000"
              @change="searchCoupons"
            />
            <div class="slider-values">
              <span class="slider-value-badge">{{ formatCurrency(filters.minOrderRange[0]) }}</span>
              <span class="slider-value-separator">-</span>
              <span class="slider-value-badge">{{ formatCurrency(filters.minOrderRange[1]) }}</span>
            </div>
          </div>

          <div class="slider-block">
            <div class="slider-title">Số lượng phiếu</div>
            <a-slider v-model="filters.quantityRange" range :min="0" :max="DEFAULT_QUANTITY_RANGE[1]" :step="10" @change="searchCoupons" />
            <div class="slider-values">
              <span class="slider-value-badge">{{ filters.quantityRange[0] }}</span>
              <span class="slider-value-separator">-</span>
              <span class="slider-value-badge">
                {{ filters.quantityRange[1] === DEFAULT_QUANTITY_RANGE[1] ? '∞' : filters.quantityRange[1] }}
              </span>
            </div>
          </div>
        </div>

        <div class="actions-row">
          <a-space>
            <a-button @click="resetFilters">
              <template #icon>
                <icon-refresh />
              </template>
              Đặt lại
            </a-button>
            <a-button type="primary" @click="openCreatePage">
              <template #icon>
                <icon-plus />
              </template>
              Thêm phiếu giảm giá
            </a-button>
            <a-button @click="exportCoupons">
              <template #icon>
                <icon-download />
              </template>
              Xuất danh sách
            </a-button>
          </a-space>
        </div>
      </a-form>
    </a-card>

    <!-- Coupons Table -->
    <a-card title="Danh sách phiếu giảm giá" class="table-card">
      <a-table :columns="columns" :data="filteredCoupons" :pagination="pagination" :loading="loading" :scroll="{ x: 1300 }">
        <template #discount_value="{ record }">
          <div class="discount-value">
            {{ record.discount_type === 'percentage' ? `${record.discount_value}%` : formatCurrency(record.discount_value) }}
          </div>
        </template>

        <template #min_order_value="{ record }">
          <div class="min-order-value">
            {{ record.min_order_value ? formatCurrency(record.min_order_value) : 'Không giới hạn' }}
          </div>
        </template>

        <template #max_discount_value="{ record }">
          <div class="max-discount-value">
            {{ record.max_discount_value ? formatCurrency(record.max_discount_value) : 'Không giới hạn' }}
          </div>
        </template>

        <template #usage_limits="{ record }">
          <div class="usage-limits">{{ record.total_used }}/{{ record.total_usage_limit || '∞' }} tổng</div>
        </template>

        <template #validity_period="{ record }">
          <div class="validity-period">
            <div>{{ formatDate(record.start_date) }}</div>
            <div class="separator">-</div>
            <div>{{ formatDate(record.end_date) }}</div>
          </div>
        </template>

        <template #quantity="{ record }">
          <div class="quantity-cell">
            {{ record.usage_limit || '∞' }}
          </div>
        </template>

        <template #status="{ record }">
          <a-tag :color="getStatusColor(record.status)">
            {{ getStatusText(record.status) }}
          </a-tag>
        </template>

        <template #action="{ record }">
          <a-space>
            <a-button type="text" @click="viewCoupon(record)">
              <template #icon>
                <icon-eye />
              </template>
            </a-button>
            <a-button type="text" @click="editCoupon(record)">
              <template #icon>
                <icon-edit />
              </template>
            </a-button>
            <a-button type="text" class="danger-item" @click="deleteCoupon(record)">
              <template #icon>
                <icon-delete />
              </template>
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:visible="couponDetailVisible" title="Chi tiết phiếu giảm giá" :footer="false" width="560px">
      <a-descriptions :column="1" layout="vertical">
        <a-descriptions-item label="Tên">{{ selectedCoupon?.name }}</a-descriptions-item>
        <a-descriptions-item label="Mã">{{ selectedCoupon?.code }}</a-descriptions-item>
        <a-descriptions-item label="Giá trị giảm">
          <span v-if="selectedCoupon?.discount_type === 'percentage'">{{ selectedCoupon?.discount_value }}%</span>
          <span v-else>{{ formatCurrency(selectedCoupon?.discount_value ?? 0) }}</span>
        </a-descriptions-item>
        <a-descriptions-item label="Đơn hàng tối thiểu">
          {{ selectedCoupon?.min_order_value ? formatCurrency(selectedCoupon.min_order_value) : 'Không giới hạn' }}
        </a-descriptions-item>
        <a-descriptions-item label="Giảm tối đa">
          {{ selectedCoupon?.max_discount_value ? formatCurrency(selectedCoupon.max_discount_value) : 'Không giới hạn' }}
        </a-descriptions-item>
        <a-descriptions-item label="Thời gian">
          {{ formatDate(selectedCoupon?.start_date ?? '') }} - {{ formatDate(selectedCoupon?.end_date ?? '') }}
        </a-descriptions-item>
        <a-descriptions-item label="Số lượng">
          {{ selectedCoupon?.usage_limit ?? '∞' }}
        </a-descriptions-item>
        <a-descriptions-item label="Trạng thái">
          {{ selectedCoupon ? getStatusText(selectedCoupon.status) : '' }}
        </a-descriptions-item>
        <a-descriptions-item label="Mô tả">
          {{ selectedCoupon?.source?.moTa || '—' }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <a-modal
      v-model:visible="couponEditVisible"
      title="Chỉnh sửa phiếu giảm giá"
      :confirm-loading="couponEditSubmitting"
      @ok="submitCouponEdit"
      @cancel="couponEditVisible = false"
      width="620px"
    >
      <a-form ref="couponEditFormRef" :model="couponEditForm" :rules="couponEditRules" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="name" label="Tên phiếu giảm giá">
              <a-input v-model="couponEditForm.name" placeholder="Nhập tên phiếu" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="discountMode" label="Hình thức giảm giá">
              <a-radio-group v-model="couponEditForm.discountMode" type="button">
                <a-radio value="percentage">Phần trăm (%)</a-radio>
                <a-radio value="amount">Số tiền (VNĐ)</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="discountValue" label="Giá trị giảm">
              <a-input-number
                v-model="couponEditForm.discountValue"
                :min="1"
                :max="isPercentEdit ? 100 : undefined"
                :precision="0"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12" v-if="isPercentEdit">
            <a-form-item field="maxDiscount" label="Giảm tối đa (VNĐ)">
              <a-input-number v-model="couponEditForm.maxDiscount" :min="1" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="minOrder" label="Đơn hàng tối thiểu (VNĐ)">
              <a-input-number v-model="couponEditForm.minOrder" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="quantity" label="Số lượng áp dụng">
              <a-input-number v-model="couponEditForm.quantity" :min="1" :precision="0" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item field="dateRange" label="Thời gian áp dụng">
          <a-range-picker
            v-model="couponEditForm.dateRange"
            value-format="YYYY-MM-DD"
            format="DD/MM/YYYY"
            allow-clear
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item field="description" label="Mô tả">
          <a-textarea v-model="couponEditForm.description" allow-clear :max-length="500" auto-size />
        </a-form-item>

        <a-form-item field="active" label="Trạng thái">
          <a-switch v-model="couponEditForm.active">
            <template #checked>Hoạt động</template>
            <template #unchecked>Tạm dừng</template>
          </a-switch>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:visible="couponDeleteVisible"
      title="Xóa phiếu giảm giá"
      :confirm-loading="couponDeleteSubmitting"
      width="440px"
      ok-text="Xóa"
      :ok-button-props="{ status: 'danger' }"
      @ok="confirmDeleteCoupon"
      @cancel="couponDeleteVisible = false"
    >
      <p>
        Bạn chắc chắn muốn xóa phiếu giảm giá
        <strong>{{ selectedCoupon?.name }}</strong>
        ?
      </p>
      <p class="modal-note">Hành động này không thể hoàn tác.</p>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import {
  IconPlus,
  IconSearch,
  IconDownload,
  IconEdit,
  IconEye,
  IconMore,
  IconCopy,
  IconDelete,
  IconStar,
  IconGift,
  IconCheckCircle,
  IconClockCircle,
  IconRefresh,
} from '@arco-design/web-vue/es/icon'
import downloadCsv from '@/utils/export-csv'
import { deleteCoupon as deleteCouponApi, fetchCoupons, type CouponApiModel, updateCoupon } from '@/api/discount-management'
import type { FormInstance, FormRules } from '@arco-design/web-vue/es/form'

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()
const router = useRouter()

// Filters
const DEFAULT_PERCENT_RANGE: [number, number] = [0, 100]
const DEFAULT_DISCOUNT_AMOUNT_RANGE: [number, number] = [0, 1_000_000]
const DEFAULT_MIN_ORDER_RANGE: [number, number] = [0, 30_000_000]
const DEFAULT_QUANTITY_RANGE: [number, number] = [0, 1_000]

interface CouponFilters {
  search: string
  discountType: string
  startDate: Date | null
  endDate: Date | null
  status: string
  discountPercentRange: [number, number]
  discountAmountRange: [number, number]
  minOrderRange: [number, number]
  quantityRange: [number, number]
}

const createDefaultFilters = (): CouponFilters => ({
  search: '',
  discountType: '',
  startDate: null,
  endDate: null,
  status: '',
  discountPercentRange: [...DEFAULT_PERCENT_RANGE],
  discountAmountRange: [...DEFAULT_DISCOUNT_AMOUNT_RANGE],
  minOrderRange: [...DEFAULT_MIN_ORDER_RANGE],
  quantityRange: [...DEFAULT_QUANTITY_RANGE],
})

const filters = ref<CouponFilters>(createDefaultFilters())

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
    title: 'Mã phiếu',
    dataIndex: 'code',
    width: 120,
  },
  {
    title: 'Tên phiếu giảm giá',
    dataIndex: 'name',
    width: 200,
  },
  {
    title: 'Giá trị giảm',
    dataIndex: 'discount_value',
    slotName: 'discount_value',
    width: 120,
    align: 'center',
  },
  {
    title: 'Giá trị tối thiểu',
    dataIndex: 'min_order_value',
    slotName: 'min_order_value',
    width: 130,
    align: 'center',
  },
  {
    title: 'Giá trị tối đa',
    dataIndex: 'max_discount_value',
    slotName: 'max_discount_value',
    width: 130,
    align: 'center',
  },
  {
    title: 'Thời gian',
    dataIndex: 'start_date',
    slotName: 'validity_period',
    width: 200,
    align: 'center',
  },
  {
    title: 'Số lượng',
    dataIndex: 'quantity',
    slotName: 'quantity',
    width: 100,
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
    title: 'Thao tác',
    slotName: 'action',
    width: 150,
    fixed: 'right',
    align: 'center',
  },
]

type CouponStatus = 'active' | 'expired' | 'upcoming' | 'used_up' | 'inactive'

interface CouponRecord {
  id: number
  index: number
  code: string
  name: string
  discount_type: 'percentage' | 'fixed'
  discount_value: number
  min_order_value: number | null
  max_discount_value: number | null
  usage_limit: number | null
  total_used?: number
  total_usage_limit?: number | null
  start_date: string
  end_date: string
  status: CouponStatus
  source: CouponApiModel
}

const coupons = ref<CouponRecord[]>([])
const selectedCoupon = ref<CouponRecord | null>(null)

const couponDetailVisible = ref(false)
const couponEditVisible = ref(false)
const couponDeleteVisible = ref(false)
const couponEditSubmitting = ref(false)
const couponDeleteSubmitting = ref(false)

const couponEditFormRef = ref<FormInstance>()
const couponEditForm = reactive({
  name: '',
  discountMode: 'percentage' as 'percentage' | 'amount',
  discountValue: 10,
  maxDiscount: null as number | null,
  minOrder: 0,
  quantity: 1,
  dateRange: [] as string[],
  description: '',
  active: true,
})

const couponEditRules: FormRules = {
  name: [{ required: true, message: 'Vui lòng nhập tên phiếu giảm giá' }],
  discountMode: [{ required: true, message: 'Vui lòng chọn hình thức giảm giá' }],
  discountValue: [{ required: true, message: 'Vui lòng nhập giá trị giảm' }],
  quantity: [{ required: true, message: 'Vui lòng nhập số lượng áp dụng' }],
  dateRange: [{ required: true, type: 'array', message: 'Vui lòng chọn thời gian áp dụng' }],
}

const isPercentEdit = computed(() => couponEditForm.discountMode === 'percentage')

watch(
  () => couponEditForm.discountMode,
  (mode) => {
    if (mode === 'amount') {
      couponEditForm.maxDiscount = null
    } else if (couponEditForm.discountValue > 100) {
      couponEditForm.discountValue = 100
    }
  }
)

const determineDiscountType = (coupon: CouponApiModel): 'percentage' | 'fixed' => {
  if (coupon.loaiPhieuGiamGia !== null && coupon.loaiPhieuGiamGia !== undefined) {
    return coupon.loaiPhieuGiamGia ? 'fixed' : 'percentage'
  }
  const numericValue = Number(coupon.giaTriGiamGia ?? 0)
  return numericValue <= 100 ? 'percentage' : 'fixed'
}

const deriveCouponStatus = (coupon: CouponApiModel): CouponStatus => {
  if (coupon.deleted) {
    return 'inactive'
  }

  const now = new Date()
  const start = coupon.ngayBatDau ? new Date(coupon.ngayBatDau) : null
  const end = coupon.ngayKetThuc ? new Date(coupon.ngayKetThuc) : null

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

  return coupon.trangThai ? 'active' : 'inactive'
}

const toCouponRecord = (coupon: CouponApiModel, index: number): CouponRecord => {
  const discountType = determineDiscountType(coupon)
  const discountValue = Number(coupon.giaTriGiamGia ?? 0)
  const minOrder = coupon.hoaDonToiThieu != null ? Number(coupon.hoaDonToiThieu) : null
  const maxDiscount = coupon.soTienToiDa != null ? Number(coupon.soTienToiDa) : null
  const usageLimit = coupon.soLuongDung ?? null

  return {
    id: coupon.id,
    index: index + 1,
    code: coupon.maPhieuGiamGia ?? '',
    name: coupon.tenPhieuGiamGia ?? '',
    discount_type: discountType,
    discount_value: discountValue,
    min_order_value: minOrder,
    max_discount_value: maxDiscount,
    usage_limit: usageLimit,
    total_used: undefined,
    total_usage_limit: usageLimit,
    start_date: coupon.ngayBatDau ?? '',
    end_date: coupon.ngayKetThuc ?? '',
    status: deriveCouponStatus(coupon),
    source: coupon,
  }
}

// Methods
const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

// Filtered coupons computed
const filteredCoupons = computed(() => {
  let filtered = coupons.value

  // Filter by search term (code, name, discount value)
  if (filters.value.search) {
    const searchTerm = filters.value.search.toLowerCase()
    filtered = filtered.filter((coupon) => {
      const discountValueStr = coupon.discount_type === 'percentage' ? `${coupon.discount_value}%` : formatCurrency(coupon.discount_value)
      return (
        coupon.code.toLowerCase().includes(searchTerm) ||
        coupon.name.toLowerCase().includes(searchTerm) ||
        discountValueStr.toLowerCase().includes(searchTerm)
      )
    })
  }

  // Filter by discount type
  if (filters.value.discountType) {
    filtered = filtered.filter((coupon) => coupon.discount_type === filters.value.discountType)
  }

  // Filter by start date
  if (filters.value.startDate) {
    filtered = filtered.filter((coupon) => {
      const couponStart = new Date(coupon.start_date)
      return couponStart >= filters.value.startDate
    })
  }

  // Filter by end date
  if (filters.value.endDate) {
    filtered = filtered.filter((coupon) => {
      const couponEnd = new Date(coupon.end_date)
      return couponEnd <= filters.value.endDate
    })
  }

  // Filter by status
  if (filters.value.status) {
    filtered = filtered.filter((coupon) => coupon.status === filters.value.status)
  }

  // Discount percent slider (applies to percentage coupons)
  const [percentMin, percentMax] = filters.value.discountPercentRange
  if (percentMin !== DEFAULT_PERCENT_RANGE[0] || percentMax !== DEFAULT_PERCENT_RANGE[1]) {
    filtered = filtered.filter((coupon) => {
      if (coupon.discount_type !== 'percentage') return true
      return coupon.discount_value >= percentMin && coupon.discount_value <= percentMax
    })
  }

  // Discount amount slider (applies to fixed amount coupons)
  const [amountMin, amountMax] = filters.value.discountAmountRange
  if (amountMin !== DEFAULT_DISCOUNT_AMOUNT_RANGE[0] || amountMax !== DEFAULT_DISCOUNT_AMOUNT_RANGE[1]) {
    filtered = filtered.filter((coupon) => {
      if (coupon.discount_type !== 'fixed') return true
      return coupon.discount_value >= amountMin && coupon.discount_value <= amountMax
    })
  }

  // Minimum order slider (null treated as 0)
  const [minOrderMin, minOrderMax] = filters.value.minOrderRange
  if (minOrderMin !== DEFAULT_MIN_ORDER_RANGE[0] || minOrderMax !== DEFAULT_MIN_ORDER_RANGE[1]) {
    filtered = filtered.filter((coupon) => {
      const orderValue = coupon.min_order_value ?? 0
      return orderValue >= minOrderMin && orderValue <= minOrderMax
    })
  }

  // Quantity slider (null treated as Infinity)
  const [quantityMin, quantityMax] = filters.value.quantityRange
  if (quantityMin !== DEFAULT_QUANTITY_RANGE[0] || quantityMax !== DEFAULT_QUANTITY_RANGE[1]) {
    filtered = filtered.filter((coupon) => {
      const limit = coupon.usage_limit ?? Number.POSITIVE_INFINITY
      const appliedMax = quantityMax === DEFAULT_QUANTITY_RANGE[1] ? Number.POSITIVE_INFINITY : quantityMax
      return limit >= quantityMin && limit <= appliedMax
    })
  }

  return filtered
})

// Pagination
const pagination = computed(() => ({
  current: 1,
  pageSize: 10,
  total: filteredCoupons.value.length,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: true,
}))

const loadCoupons = async () => {
  loading.value = true
  try {
    const data = await fetchCoupons()
    coupons.value = data.map((item, index) => toCouponRecord(item, index))
  } catch (error) {
    Message.error({ content: 'Không thể tải danh sách phiếu giảm giá', duration: 5000 })
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

const getStatusColor = (status: string) => {
  switch (status) {
    case 'active':
      return 'green'
    case 'expired':
      return 'red'
    case 'used_up':
      return 'orange'
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
      return 'Đã hết hạn'
    case 'used_up':
      return 'Hết lượt sử dụng'
    case 'upcoming':
      return 'Sắp diễn ra'
    case 'inactive':
      return 'Tạm dừng'
    default:
      return status
  }
}

const searchCoupons = () => {
  // Filtering is now handled by the computed filteredCoupons property
  // This function is called when filters change to trigger reactivity
}

const openCreatePage = () => {
  router.push({ name: 'QuanLyPhieuGiamGiaCreate' })
}

const viewCoupon = (coupon: CouponRecord) => {
  selectedCoupon.value = coupon
  couponDetailVisible.value = true
}

const editCoupon = (coupon: CouponRecord) => {
  selectedCoupon.value = coupon
  couponEditForm.name = coupon.name ?? ''
  couponEditForm.discountMode = coupon.discount_type === 'percentage' ? 'percentage' : 'amount'
  couponEditForm.discountValue = Number(coupon.discount_value ?? 0)
  couponEditForm.maxDiscount = coupon.discount_type === 'percentage' ? (coupon.max_discount_value ?? null) : null
  couponEditForm.minOrder = coupon.min_order_value ?? 0
  couponEditForm.quantity = coupon.usage_limit ?? 1
  couponEditForm.dateRange = [coupon.start_date ?? '', coupon.end_date ?? ''].filter(Boolean) as string[]
  couponEditForm.description = coupon.source?.moTa ?? ''
  couponEditForm.active = Boolean(coupon.source?.trangThai)
  couponEditVisible.value = true
}

const duplicateCoupon = (coupon: CouponRecord) => {
  Message.info(`Tính năng nhân bản đang được phát triển cho "${coupon.name}"`)
}

const deleteCoupon = (coupon: CouponRecord) => {
  selectedCoupon.value = coupon
  couponDeleteVisible.value = true
}

const submitCouponEdit = async () => {
  if (!selectedCoupon.value) return
  if (couponEditSubmitting.value) return

  const form = couponEditFormRef.value
  if (!form) return

  try {
    await form.validate()
  } catch (error) {
    return
  }

  const discountValue = Number(couponEditForm.discountValue)
  if (Number.isNaN(discountValue) || discountValue <= 0) {
    Message.error('Giá trị giảm phải lớn hơn 0')
    return
  }

  if (isPercentEdit.value && discountValue > 100) {
    Message.error('Giá trị giảm theo phần trăm tối đa 100%')
    return
  }

  const [startDate, endDate] = couponEditForm.dateRange
  if (!startDate || !endDate) {
    Message.error('Vui lòng chọn thời gian áp dụng')
    return
  }
  if (new Date(startDate) > new Date(endDate)) {
    Message.error('Ngày kết thúc phải sau ngày bắt đầu')
    return
  }

  if (isPercentEdit.value) {
    const capValue = Number(couponEditForm.maxDiscount)
    if (!capValue || Number.isNaN(capValue) || capValue <= 0) {
      Message.error('Vui lòng nhập mức giảm tối đa hợp lệ')
      return
    }
  }

  const quantityValue = Number(couponEditForm.quantity)
  if (!Number.isInteger(quantityValue) || quantityValue <= 0) {
    Message.error('Số lượng áp dụng phải lớn hơn 0')
    return
  }

  const payload = {
    tenPhieuGiamGia: couponEditForm.name.trim(),
    loaiPhieuGiamGia: couponEditForm.discountMode === 'amount',
    giaTriGiamGia: discountValue,
    soTienToiDa: isPercentEdit.value ? Number(couponEditForm.maxDiscount ?? 0) : null,
    hoaDonToiThieu: couponEditForm.minOrder ? Number(couponEditForm.minOrder) : 0,
    soLuongDung: quantityValue,
    ngayBatDau: startDate,
    ngayKetThuc: endDate,
    trangThai: couponEditForm.active,
    moTa: couponEditForm.description.trim() || null,
    deleted: Boolean(selectedCoupon.value.source?.deleted),
    idKhachHang: selectedCoupon.value.source?.idKhachHang ?? [],
  }

  couponEditSubmitting.value = true
  try {
    await updateCoupon(selectedCoupon.value.id, payload)
    Message.success('Cập nhật phiếu giảm giá thành công')
    couponEditVisible.value = false
    await loadCoupons()
  } catch (error) {
    Message.error((error as Error).message || 'Không thể cập nhật phiếu giảm giá')
  } finally {
    couponEditSubmitting.value = false
  }
}

const confirmDeleteCoupon = async () => {
  if (!selectedCoupon.value) return
  if (couponDeleteSubmitting.value) return

  couponDeleteSubmitting.value = true
  try {
    await deleteCouponApi(selectedCoupon.value.id)
    Message.success('Đã xóa phiếu giảm giá')
    couponDeleteVisible.value = false
    await loadCoupons()
  } catch (error) {
    Message.error((error as Error).message || 'Không thể xóa phiếu giảm giá')
  } finally {
    couponDeleteSubmitting.value = false
  }
}

const exportCoupons = () => {
  if (!filteredCoupons.value.length) {
    Message.warning('Không có dữ liệu để xuất')
    return
  }

  const header = [
    'STT',
    'Mã phiếu',
    'Tên phiếu giảm giá',
    'Giá trị giảm',
    'Giá trị tối thiểu',
    'Giá trị tối đa',
    'Ngày bắt đầu',
    'Ngày kết thúc',
    'Số lượng',
    'Trạng thái',
  ]

  const rows = filteredCoupons.value.map((coupon, index) => {
    const discountDisplay = coupon.discount_type === 'percentage' ? `${coupon.discount_value}%` : formatCurrency(coupon.discount_value)
    const minOrderDisplay = coupon.min_order_value != null ? formatCurrency(coupon.min_order_value) : 'Không giới hạn'
    const maxDiscountDisplay = coupon.max_discount_value != null ? formatCurrency(coupon.max_discount_value) : 'Không giới hạn'
    const quantityDisplay = coupon.usage_limit != null ? coupon.usage_limit : '∞'

    return [
      coupon.index ?? index + 1,
      coupon.code ?? '',
      coupon.name ?? '',
      discountDisplay,
      minOrderDisplay,
      maxDiscountDisplay,
      formatDate(coupon.start_date ?? ''),
      formatDate(coupon.end_date ?? ''),
      quantityDisplay,
      getStatusText(coupon.status ?? ''),
    ]
  })

  downloadCsv('phieu-giam-gia.csv', header, rows)
  Message.success('Đã xuất danh sách phiếu giảm giá')
}

const resetFilters = async () => {
  filters.value = createDefaultFilters()
  searchCoupons()
  await loadCoupons()
}

onMounted(() => {
  loadCoupons()
})
</script>

<style scoped>
.coupon-management-page {
  padding: 0 20px 20px 20px;
}

.slider-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
  margin: 16px 0 8px 0;
}

.slider-block {
  background: var(--color-bg-3);
  border: 1px solid var(--color-border-2);
  border-radius: 8px;
  padding: 16px;
}

.slider-title {
  font-weight: 600;
  color: var(--color-text-1);
  margin-bottom: 12px;
}

.slider-values {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  color: var(--color-primary-6);
  font-weight: 600;
}

.slider-value-badge {
  background: rgba(49, 130, 255, 0.12);
  color: var(--color-primary-6);
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
}

.slider-value-separator {
  color: var(--color-text-3);
  font-size: 12px;
}

:deep(.slider-block .arco-slider-thumb) {
  border-color: var(--color-primary-6);
}

:deep(.slider-block .arco-slider-bar) {
  background-color: var(--color-primary-6);
}

:deep(.slider-block .arco-slider-track) {
  background-color: transparent;
}

.actions-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
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
  margin: 0;
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

.used-icon {
  color: #52c41a;
}

.revenue-icon {
  color: #faad14;
}

.expiring-icon {
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

.coupon-code-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.coupon-code {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-weight: 600;
  color: #1890ff;
  font-size: 16px;
}

.coupon-name {
  font-size: 12px;
  color: #86909c;
}

.discount-value {
  text-align: center;
  font-weight: 600;
  color: #1890ff;
}

.usage-limits {
  text-align: center;
  font-size: 12px;
  color: #1d2129;
}

.validity-period {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.separator {
  color: #86909c;
}

.danger-item {
  color: #ff4d4f;
}
</style>
