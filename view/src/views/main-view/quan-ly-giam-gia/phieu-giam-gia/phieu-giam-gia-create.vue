<template>
  <div class="coupon-create-page">
    <Breadcrumb :items="breadcrumbItems" />

    <a-card class="coupon-card" :class="{ 'featured-layout': formState.featured }">

      <div :class="formState.featured ? 'two-column-container' : ''">
        <!-- Left Column: Form -->
        <div :class="formState.featured ? 'left-column' : ''">
          <div v-if="formState.featured" class="column-title">Thông tin phiếu giảm giá</div>

          <a-form ref="formRef" :model="formState" :rules="rules" layout="vertical">
            <!-- Mã phiếu giảm giá -->
            <a-form-item field="code" label="Mã phiếu giảm giá">
              <a-input v-model="formState.code" placeholder="Nhập mã phiếu giảm giá" allow-clear />
            </a-form-item>

            <!-- Tên phiếu giảm giá -->
            <a-form-item field="name" label="Tên phiếu giảm giá">
              <a-input v-model="formState.name" placeholder="Nhập tên phiếu giảm giá..." allow-clear />
            </a-form-item>

            <!-- Loại giảm giá -->
            <a-form-item field="discountMode" label="Loại giảm giá">
              <a-select v-model="formState.discountMode" placeholder="Chọn loại giảm giá">
                <a-option value="percentage">Giảm theo phần trăm</a-option>
                <a-option value="amount">Giảm theo số tiền</a-option>
              </a-select>
            </a-form-item>

            <!-- Giá trị giảm -->
            <a-form-item field="discountValue" label="Giá trị giảm">
              <a-input
                v-model="displayDiscountValue"
                @blur="handleDiscountBlur"
                @focus="handleDiscountFocus"
                @input="handleDiscountInput"
                :placeholder="isPercent ? 'Giá trị từ 0 - 100' : 'Giá trị từ 0 - 100.000.000 VND'"
                style="width: 100%"
              />
            </a-form-item>

            <!-- Thời gian áp dụng -->
            <a-form-item field="dateRange" label="Thời gian áp dụng">
              <a-range-picker
                v-model="formState.dateRange"
                :show-time="true"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="DD/MM/YYYY HH:mm"
                allow-clear
                :disabled-date="disablePastDates"
                style="width: 100%"
              />
            </a-form-item>

            <!-- Giá trị đơn hàng tối thiểu -->
            <a-form-item field="minOrder" label="Giá trị đơn hàng tối thiểu">
              <a-input
                v-model="displayMinOrder"
                @blur="handleMinOrderBlur"
                @focus="handleMinOrderFocus"
                @input="handleMinOrderInput"
                placeholder="Giá trị từ 0 - 500.000.000 VND"
                style="width: 100%"
              />
            </a-form-item>

            <!-- Số lượng phiếu -->
            <a-form-item field="quantity" label="Số lượng phiếu">
              <a-input-number
                v-model="formState.quantity"
                :min="1"
                :max="100000"
                :precision="0"
                :disabled="formState.featured"
                :placeholder="formState.featured ? 'Tự động theo số khách hàng' : 'Tối đa: 100.000 phiếu'"
                style="width: 100%"
              />
            </a-form-item>

            <!-- Mô tả -->
            <a-form-item field="description" label="Mô tả">
              <a-textarea
                v-model="formState.description"
                placeholder="Nhập mô tả cho phiếu giảm giá..."
                allow-clear
                :max-length="500"
                :auto-size="{ minRows: 3, maxRows: 6 }"
              />
            </a-form-item>

            <!-- Phiếu giảm giá riêng tư -->
            <a-form-item field="featured">
              <a-checkbox v-model="formState.featured">Phiếu giảm giá riêng tư</a-checkbox>
              <div style="margin-left: 24px; margin-top: 4px; font-size: 12px; color: var(--color-text-3)">
                Phiếu giảm giá riêng tư chỉ áp dụng cho khách hàng được chọn
              </div>
            </a-form-item>
          </a-form>
        </div>

        <!-- Right Column: Customer Selection -->
        <div v-if="formState.featured" class="right-column">
          <div class="column-title">Chọn khách hàng</div>
          <div class="customer-selection-section">
            <div style="margin-bottom: 12px; display: flex; align-items: center; gap: 12px">
              <a-checkbox :model-value="isAllCustomersSelected" @change="toggleAllCustomers">Chọn tất cả</a-checkbox>
              <a-button size="mini" @click="formState.selectedCustomerIds = []" type="text">Xóa chọn</a-button>
            </div>
            <a-input-search v-model="customerSearchQuery" placeholder="Tìm kiếm khách hàng..." allow-clear style="margin-bottom: 12px" />
            <a-table
              row-key="id"
              :columns="customerColumns"
              :data="filteredCustomers"
              :pagination="customerPagination"
              :loading="customersLoading"
              :scroll="{ y: 450 }"
              :bordered="{ wrapper: true, cell: true }"
              size="small"
            >
              <template #checkbox="{ record }">
                <a-checkbox :model-value="isCustomerSelected(record.id)" @change="toggleCustomerSelection(record.id)" />
              </template>
              <template #empty>
                <div style="text-align: center; padding: 20px">Không có dữ liệu</div>
              </template>
            </a-table>
            <div
              v-if="!customersLoading && filteredCustomers.length === 0"
              style="text-align: center; padding: 20px; color: var(--color-text-3)"
            >
              Không tìm thấy khách hàng nào
            </div>
            <div style="margin-top: 8px; font-size: 12px; color: var(--color-text-3)">
              Đã chọn:
              <strong>{{ formState.selectedCustomerIds.length }}</strong>
              khách hàng
            </div>
          </div>
        </div>
      </div>
    </a-card>

    <div class="page-actions">
      <a-space>
        <a-button @click="goBack">Quay lại</a-button>
        <a-button type="primary" :loading="confirmSaveSubmitting" @click="handleSaveClick">Lưu phiếu giảm giá</a-button>
      </a-space>
    </div>

    <!-- Confirmation Modal -->
    <a-modal
      v-model:visible="confirmSaveVisible"
      title="Xác nhận tạo phiếu giảm giá"
      :confirm-loading="confirmSaveSubmitting"
      @ok="confirmSave"
      @cancel="confirmSaveVisible = false"
      ok-text="Xác nhận"
      cancel-text="Hủy"
      width="560px"
    >
      <p style="margin-bottom: 16px; color: var(--color-text-2)">Vui lòng kiểm tra lại thông tin trước khi lưu:</p>
      <a-descriptions :column="1" bordered>
        <a-descriptions-item label="Mã phiếu giảm giá">
          {{ formState.code }}
        </a-descriptions-item>
        <a-descriptions-item label="Tên phiếu giảm giá">
          {{ formState.name }}
        </a-descriptions-item>
        <a-descriptions-item label="Loại giảm giá">
          {{ isPercent ? 'Giảm theo phần trăm' : 'Giảm theo số tiền' }}
        </a-descriptions-item>
        <a-descriptions-item label="Giá trị giảm">
          <span v-if="isPercent">{{ formState.discountValue }}%</span>
          <span v-else>{{ formatCurrency(formState.discountValue) }}</span>
        </a-descriptions-item>
        <a-descriptions-item label="Giá trị đơn hàng tối thiểu">
          {{ formState.minOrder ? formatCurrency(formState.minOrder) : 'Không giới hạn' }}
        </a-descriptions-item>
        <a-descriptions-item label="Thời gian áp dụng">
          {{ formatDateRange(formState.dateRange[0], formState.dateRange[1]) }}
        </a-descriptions-item>
        <a-descriptions-item label="Số lượng phiếu">
          {{ formState.quantity }}
        </a-descriptions-item>
        <a-descriptions-item v-if="formState.description" label="Mô tả">
          {{ formState.description }}
        </a-descriptions-item>
        <a-descriptions-item label="Phiếu giảm giá riêng tư">
          {{ formState.featured ? 'Có' : 'Không' }}
        </a-descriptions-item>
        <a-descriptions-item v-if="formState.featured" label="Khách hàng được áp dụng">
          <div style="max-height: 150px; overflow-y: auto">
            <a-tag v-for="customerId in formState.selectedCustomerIds" :key="customerId" style="margin: 2px">
              {{ getCustomerNameById(customerId) }}
            </a-tag>
          </div>
          <div style="margin-top: 4px; font-size: 12px; color: var(--color-text-3)">
            Tổng: {{ formState.selectedCustomerIds.length }} khách hàng
          </div>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- Scroll to Top Button -->
    <a-button
      v-show="showScrollTop"
      class="scroll-to-top-btn"
      type="primary"
      shape="circle"
      size="large"
      @click="scrollToTop"
    >
      <template #icon>
        <icon-up />
      </template>
    </a-button>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch, onMounted, onUnmounted } from 'vue'
import type { FormInstance, FormRules } from '@arco-design/web-vue/es/form'
import { Message } from '@arco-design/web-vue'
import { IconUp } from '@arco-design/web-vue/es/icon'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { useRouter } from 'vue-router'
import { createCoupon, fetchCoupons, fetchCustomers, type CustomerApiModel } from '@/api/discount-management'
import dayjs from 'dayjs'

const { breadcrumbItems } = useBreadcrumb()
const router = useRouter()

const formRef = ref<FormInstance>()
const confirmSaveVisible = ref(false)
const confirmSaveSubmitting = ref(false)

const formState = reactive({
  code: '',
  name: '',
  discountMode: 'percentage' as 'percentage' | 'amount',
  discountValue: null as number | null,
  minOrder: null as number | null,
  dateRange: [] as string[],
  quantity: 1,
  description: '',
  featured: false,
  selectedCustomerIds: [] as number[],
})

const disablePastDates = (current: Date | string) => {
  if (!current) return false
  return dayjs(current).isBefore(dayjs().startOf('day'))
}

const rules: FormRules = {
  code: [{ required: true, message: 'Vui lòng nhập mã phiếu giảm giá' }],
  name: [{ required: true, message: 'Vui lòng nhập tên phiếu giảm giá' }],
  discountMode: [{ required: true, message: 'Vui lòng chọn hình thức giảm giá' }],
  discountValue: [{ required: true, message: 'Vui lòng nhập giá trị giảm' }],
  quantity: [{ required: true, message: 'Vui lòng nhập số lượng áp dụng' }],
  dateRange: [
    { required: true, message: 'Vui lòng chọn khoảng thời gian áp dụng' },
    {
      validator: (value: string[], callback) => {
        if (!Array.isArray(value) || value.length !== 2) {
          callback('Vui lòng chọn khoảng thời gian áp dụng')
          return
        }
        const [start, end] = value
        if (!start || !end) {
          callback('Vui lòng chọn khoảng thời gian áp dụng')
          return
        }
        if (dayjs(start).isBefore(dayjs().startOf('day'))) {
          callback('Ngày bắt đầu phải từ hôm nay trở đi')
          return
        }
        if (dayjs(start).isAfter(dayjs(end))) {
          callback('Ngày kết thúc phải sau ngày bắt đầu')
          return
        }
        callback()
      },
    },
  ],
}

const isPercent = computed(() => formState.discountMode === 'percentage')

// Customer selection
const customers = ref<CustomerApiModel[]>([])
const customersLoading = ref(false)
const customerSearchQuery = ref('')

const filteredCustomers = computed(() => {
  if (!customerSearchQuery.value) {
    return customers.value
  }
  const query = customerSearchQuery.value.toLowerCase()
  return customers.value.filter(
    (customer) =>
      customer.tenKhachHang?.toLowerCase().includes(query) ||
      customer.soDienThoai?.toLowerCase().includes(query) ||
      customer.email?.toLowerCase().includes(query)
  )
})

const customerPagination = computed(() => ({
  pageSize: 5,
  showTotal: true,
  showPageSize: false,
}))

const isCustomerSelected = (id: number) => {
  return formState.selectedCustomerIds.includes(id)
}

const toggleCustomerSelection = (id: number) => {
  const index = formState.selectedCustomerIds.indexOf(id)
  if (index > -1) {
    formState.selectedCustomerIds.splice(index, 1)
  } else {
    formState.selectedCustomerIds.push(id)
  }
}

const toggleAllCustomers = () => {
  if (formState.selectedCustomerIds.length === filteredCustomers.value.length) {
    formState.selectedCustomerIds = []
  } else {
    formState.selectedCustomerIds = filteredCustomers.value.map((c) => c.id)
  }
}

const isAllCustomersSelected = computed(() => {
  return filteredCustomers.value.length > 0 && formState.selectedCustomerIds.length === filteredCustomers.value.length
})

const customerColumns = [
  {
    title: '',
    slotName: 'checkbox',
    width: 50,
    align: 'center',
  },
  {
    title: 'Họ tên',
    dataIndex: 'tenKhachHang',
    ellipsis: true,
    tooltip: true,
  },
  {
    title: 'Email',
    dataIndex: 'email',
    ellipsis: true,
    tooltip: true,
  },
  {
    title: 'Số điện thoại',
    dataIndex: 'soDienThoai',
    width: 130,
  },
]

const loadCustomers = async () => {
  customersLoading.value = true
  try {
    const data = await fetchCustomers()
    if (data && Array.isArray(data)) {
      customers.value = data.filter((c) => c.trangThai !== false && c.trangThai !== 0)
      if (customers.value.length === 0) {
        Message.info('Không có khách hàng hoạt động')
      }
    } else {
      customers.value = []
      Message.warning('Dữ liệu khách hàng không hợp lệ')
    }
  } catch {
    Message.error('Không thể tải danh sách khách hàng')
    customers.value = []
  } finally {
    customersLoading.value = false
  }
}

const formatNumberWithSeparator = (value: number | string) => {
  if (value === null || value === undefined || value === '') return ''
  const numValue = typeof value === 'string' ? parseInt(value.replace(/\./g, ''), 10) : value
  if (Number.isNaN(numValue)) return ''
  return numValue.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.')
}


// Load customers when featured is checked
watch(
  () => formState.featured,
  (isFeatured) => {
    if (isFeatured && customers.value.length === 0) {
      loadCustomers()
    }
    // Clear selected customers when unchecking featured
    if (!isFeatured) {
      formState.selectedCustomerIds = []
      formState.quantity = 1 // Reset to default
    }
  }
)

// Auto-update quantity based on selected customers for private vouchers
watch(
  () => formState.selectedCustomerIds.length,
  (count) => {
    if (formState.featured) {
      formState.quantity = count > 0 ? count : 1
    }
  }
)

watch(
  () => formState.discountMode,
  (mode) => {
    if (mode === 'percentage' && formState.discountValue > 100) {
      formState.discountValue = 100
    }
  }
)

// Display value for discount input (with % or VND symbol)
const displayDiscountValue = ref('')
const isEditingDiscount = ref(false)

// Handle focus - remove % or VND for easy editing
const handleDiscountFocus = () => {
  isEditingDiscount.value = true
  displayDiscountValue.value = formState.discountValue !== null ? String(formState.discountValue) : ''
}

const handleDiscountInput = () => {
  if (!isEditingDiscount.value) return

  if (isPercent.value) {
    // For percentage, keep numbers and decimal point
    const digits = displayDiscountValue.value.replace(/[^0-9.]/g, '')
    // Ensure only one decimal point
    const parts = digits.split('.')
    if (parts.length > 2) {
      displayDiscountValue.value = `${parts[0]}.${parts.slice(1).join('')}`
    } else {
      displayDiscountValue.value = digits
    }
  } else {
    // For amount, format with thousand separators (no VND while typing)
    // Remove all dots first to get clean digits
    const cleanValue = displayDiscountValue.value.replace(/\\./g, '')
    const digits = cleanValue.replace(/[^0-9]/g, '')

    if (digits === '' || digits === '0') {
      displayDiscountValue.value = digits
      return
    }

    const formatted = formatNumberWithSeparator(parseInt(digits, 10))
    displayDiscountValue.value = formatted
  }
}

// Handle blur - format with .00% or VND
const handleDiscountBlur = () => {
  isEditingDiscount.value = false

  if (isPercent.value) {
    // Percentage mode - parse as float to handle decimals
    const value = parseFloat(displayDiscountValue.value.replace(/[^0-9.]/g, ''))

    if (Number.isNaN(value) || value < 0) {
      formState.discountValue = 0
    } else if (value > 100) {
      formState.discountValue = 100
      Message.warning('Giá trị giảm theo phần trăm không được vượt quá 100%')
    } else {
      formState.discountValue = value
    }
    displayDiscountValue.value = `${formState.discountValue.toFixed(2)}%`
  } else {
    // Amount mode - remove thousand separators (dots) then parse
    const cleanValue = displayDiscountValue.value.replace(/\./g, '').replace(/[^0-9]/g, '')
    const value = parseInt(cleanValue, 10)

    if (Number.isNaN(value) || value < 0) {
      formState.discountValue = 0
    } else if (value > 100000000) {
      formState.discountValue = 100000000
      Message.warning('Giá trị giảm không được vượt quá 100.000.000 VND')
    } else {
      formState.discountValue = Math.round(value)
    }
    displayDiscountValue.value = `${formatNumberWithSeparator(formState.discountValue)} VND`
  }
}

// Watch discountValue changes to update display
watch(
  () => formState.discountValue,
  (newValue) => {
    if (!isEditingDiscount.value) {
      if (newValue === null || newValue === undefined) {
        displayDiscountValue.value = ''
      } else if (isPercent.value) {
        displayDiscountValue.value = `${newValue.toFixed(2)}%`
      } else {
        displayDiscountValue.value = `${formatNumberWithSeparator(newValue)} VND`
      }
    }
  },
  { immediate: true }
)

// Watch mode changes
watch(
  () => formState.discountMode,
  () => {
    if (formState.discountValue === null || formState.discountValue === undefined) {
      displayDiscountValue.value = ''
    } else if (isPercent.value) {
      displayDiscountValue.value = `${formState.discountValue.toFixed(2)}%`
    } else {
      displayDiscountValue.value = `${formatNumberWithSeparator(formState.discountValue)} VND`
    }
  }
)

// Display value for minOrder field
const displayMinOrder = ref('')
const isEditingMinOrder = ref(false)

const handleMinOrderFocus = () => {
  isEditingMinOrder.value = true
  displayMinOrder.value = String(formState.minOrder || 0)
}

const handleMinOrderInput = () => {
  if (!isEditingMinOrder.value) return

  // Remove all dots first to get clean digits
  const cleanValue = displayMinOrder.value.replace(/\./g, '')
  const digits = cleanValue.replace(/[^0-9]/g, '')

  if (digits === '' || digits === '0') {
    displayMinOrder.value = digits
    return
  }

  // Format with thousand separators (no VND while typing)
  const formatted = formatNumberWithSeparator(parseInt(digits, 10))
  displayMinOrder.value = formatted
}

const handleMinOrderBlur = () => {
  isEditingMinOrder.value = false
  const cleanValue = displayMinOrder.value.replace(/\./g, '').replace(/[^0-9]/g, '')
  const value = parseInt(cleanValue, 10)

  if (Number.isNaN(value) || value < 0) {
    formState.minOrder = 0
  } else if (value > 500000000) {
    formState.minOrder = 500000000
    Message.warning('Giá trị đơn hàng tối thiểu không được vượt quá 500.000.000 VND')
  } else {
    formState.minOrder = Math.round(value)
  }

  displayMinOrder.value = `${formatNumberWithSeparator(formState.minOrder)} VND`
}

watch(
  () => formState.minOrder,
  (newValue) => {
    if (!isEditingMinOrder.value) {
      if (newValue === null || newValue === undefined) {
        displayMinOrder.value = ''
      } else {
        displayMinOrder.value = `${formatNumberWithSeparator(newValue)} VND`
      }
    }
  },
  { immediate: true }
)

// Real-time validation for discount value (percentage mode)
watch(
  () => formState.discountValue,
  (newValue) => {
    if (isPercent.value && newValue !== undefined && newValue !== null && newValue > 100) {
      Message.warning('Giá trị giảm theo phần trăm không được vượt quá 100%')
      formState.discountValue = 100
    }
  }
)

const generateNextCode = async () => {
  try {
    const coupons = await fetchCoupons()

    if (!coupons || coupons.length === 0) {
      return 'PGG000001'
    }

    // Extract all codes that match the PGG pattern
    const pggCodes = coupons
      .map((p) => p.maPhieuGiamGia)
      .filter((code) => code && code.startsWith('PGG'))
      .map((code) => {
        const numPart = code.substring(3)
        return parseInt(numPart, 10)
      })
      .filter((num) => !Number.isNaN(num))

    if (pggCodes.length === 0) {
      return 'PGG000001'
    }

    // Find the maximum number and increment
    const maxNum = Math.max(...pggCodes)
    const nextNum = maxNum + 1

    // Format with leading zeros (6 digits)
    return `PGG${nextNum.toString().padStart(6, '0')}`
  } catch {
    // Fallback to PGG000001 if fetch fails
    return 'PGG000001'
  }
}

// Scroll to top functionality
const showScrollTop = ref(false)

const handleScroll = () => {
  showScrollTop.value = window.scrollY > 300
}

const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth',
  })
}

onMounted(async () => {
  formState.code = await generateNextCode()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

const goBack = () => {
  router.back()
}

const getCustomerNameById = (customerId: number): string => {
  const customer = customers.value.find((c) => c.id === customerId)
  return customer ? `${customer.tenKhachHang} - ${customer.soDienThoai}` : `KH#${customerId}`
}

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const formatDateRange = (startDate: string, endDate: string) => {
  if (!startDate || !endDate) return ''

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

  return `${formatDateTime(startDate)} - ${formatDateTime(endDate)}`
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

  const discountValue = Number(formState.discountValue)
  if (Number.isNaN(discountValue) || discountValue <= 0) {
    Message.error('Giá trị giảm phải lớn hơn 0')
    return
  }

  if (isPercent.value && discountValue > 100) {
    Message.error('Giá trị giảm theo phần trăm tối đa 100%')
    return
  }

  if (!formState.dateRange || formState.dateRange.length !== 2 || !formState.dateRange[0] || !formState.dateRange[1]) {
    Message.error('Vui lòng chọn thời gian áp dụng')
    return
  }
  if (new Date(formState.dateRange[0]) > new Date(formState.dateRange[1])) {
    Message.error('Ngày kết thúc phải sau ngày bắt đầu')
    return
  }

  if (!isPercent.value) {
    // For fixed amount discount
    const minOrderValue = Number(formState.minOrder || 0)
    if (minOrderValue > 0 && discountValue >= minOrderValue) {
      Message.error('Giá trị giảm phải nhỏ hơn giá trị đơn hàng tối thiểu')
      return
    }
  }

  const quantityValue = Number(formState.quantity)
  if (!Number.isInteger(quantityValue) || quantityValue <= 0) {
    Message.error('Số lượng áp dụng phải lớn hơn 0')
    return
  }

  // Validate customer selection for private vouchers
  if (formState.featured && formState.selectedCustomerIds.length === 0) {
    Message.error('Vui lòng chọn ít nhất một khách hàng cho phiếu giảm giá riêng tư')
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

  const discountValue = Number(formState.discountValue)
  const quantityValue = Number(formState.quantity)

  const payload = {
    maPhieuGiamGia: formState.code,
    tenPhieuGiamGia: formState.name.trim(),
    loaiPhieuGiamGia: !isPercent.value,
    giaTriGiamGia: discountValue,
    soTienToiDa: null,
    hoaDonToiThieu: formState.minOrder ? Number(formState.minOrder) : 0,
    soLuongDung: quantityValue,
    ngayBatDau: formState.dateRange[0],
    ngayKetThuc: formState.dateRange[1],
    trangThai: calculateStatus(formState.dateRange[0], formState.dateRange[1]),
    moTa: formState.description.trim() || null,
    deleted: false,
    idKhachHang: formState.featured ? formState.selectedCustomerIds : [],
    featured: formState.featured,
  }

  confirmSaveSubmitting.value = true
  try {
    await createCoupon(payload)
    Message.success('Tạo phiếu giảm giá thành công')
    confirmSaveVisible.value = false
    router.push({ name: 'QuanLyPhieuGiamGia' })
  } catch (error) {
    Message.error((error as Error).message || 'Không thể tạo phiếu giảm giá')
  } finally {
    confirmSaveSubmitting.value = false
  }
}
</script>

<style scoped>
.coupon-create-page {
  padding: 0 20px 20px 20px;
}

.coupon-card {
  margin-top: 16px;
}

.customer-selection-section {
  border: 1px solid var(--color-border-2);
  border-radius: 8px;
  padding: 16px;
  background: var(--color-bg-2);
}

.customer-selection-section :deep(.arco-table) {
  border-radius: 4px;
}

.customer-selection-section :deep(.arco-table-container) {
  border: 1px solid var(--color-border-2);
  border-radius: 4px;
}

.customer-selection-section :deep(.arco-table-th) {
  background-color: var(--color-fill-2);
  font-weight: 600;
}

.customer-selection-section :deep(.arco-table-td) {
  border-bottom: 1px solid var(--color-border-2);
}

.customer-selection-section :deep(.arco-table-tr:hover) {
  background-color: var(--color-fill-1);
}

/* Two-column layout */
.featured-layout :deep(.arco-card-body) {
  padding: 0;
}

.two-column-container {
  display: flex;
  gap: 0;
  min-height: 600px;
}

.left-column {
  flex: 0 0 48%;
  padding: 20px;
  border-right: 1px solid var(--color-border-2);
  overflow-y: auto;
  max-height: 80vh;
}

.right-column {
  flex: 0 0 52%;
  padding: 20px;
  background: var(--color-fill-1);
}

.column-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-1);
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--color-primary-6);
}

.page-actions {
  display: flex;
  justify-content: flex-end;
  padding: 24px 20px;
}

.featured-layout .customer-selection-section {
  border: none;
  padding: 0;
  background: transparent;
}

.featured-layout .customer-selection-section :deep(.arco-table-container) {
  border: 1px solid var(--color-border-2);
  border-radius: 4px;
}

/* Fix checkbox click issues */
.customer-selection-section :deep(.arco-checkbox) {
  pointer-events: auto !important;
  cursor: pointer !important;
}

.customer-selection-section :deep(.arco-checkbox-icon) {
  pointer-events: auto !important;
}

.customer-selection-section :deep(.arco-table-cell) {
  pointer-events: auto !important;
}

.customer-selection-section :deep(.arco-table-td) {
  position: relative;
  z-index: 1;
}

.customer-selection-section :deep(.arco-table-th .arco-checkbox) {
  pointer-events: auto !important;
}

/* Scroll to Top Button */
.scroll-to-top-btn {
  position: fixed;
  bottom: 32px;
  right: 32px;
  z-index: 999;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  background-color: #4ade80 !important;
  border-color: #4ade80 !important;
  transition: all 0.3s ease;
}

.scroll-to-top-btn:hover {
  background-color: #22c55e !important;
  border-color: #22c55e !important;
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.scroll-to-top-btn:active {
  transform: translateY(-2px);
}
</style>
