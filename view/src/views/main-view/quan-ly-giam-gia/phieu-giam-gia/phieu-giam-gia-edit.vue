<template>
  <div class="coupon-edit-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Back Button -->
    <div style="margin-bottom: 16px">
      <a-button @click="goBack">
        <template #icon>
          <icon-left />
        </template>
        Quay lại
      </a-button>
    </div>

    <!-- Main Content -->
    <a-row :gutter="[16, 16]">
      <!-- Left Column: Form -->
      <a-col :span="couponEditForm.featured ? 12 : 24">
        <a-card title="Thông tin phiếu giảm giá">
          <a-form ref="couponEditFormRef" :model="couponEditForm" :rules="couponEditRules" layout="vertical">
            <a-form-item field="code" label="Mã phiếu giảm giá">
              <a-input v-model="couponEditForm.code" placeholder="Nhập mã phiếu giảm giá" allow-clear />
            </a-form-item>

            <a-form-item field="name" label="Tên phiếu giảm giá">
              <a-input v-model="couponEditForm.name" placeholder="Nhập tên phiếu" allow-clear />
            </a-form-item>

            <a-form-item field="discountMode" label="Loại giảm giá">
              <a-select v-model="couponEditForm.discountMode" placeholder="Chọn loại giảm giá">
                <a-option value="percentage">Giảm theo phần trăm</a-option>
                <a-option value="amount">Giảm theo số tiền</a-option>
              </a-select>
            </a-form-item>

            <a-form-item field="discountValue" label="Giá trị giảm">
              <a-input
                v-model="displayDiscountValue"
                @blur="handleDiscountBlur"
                @focus="handleDiscountFocus"
                @input="handleDiscountInput"
                :placeholder="isPercentEdit ? 'Giá trị từ 0 - 100' : 'Giá trị từ 0 - 100.000.000 VND'"
                style="width: 100%"
              />
            </a-form-item>

            <a-form-item field="dateRange" label="Thời gian áp dụng">
              <a-range-picker
                v-model="couponEditForm.dateRange"
                :show-time="true"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="DD/MM/YYYY HH:mm"
                allow-clear
                :disabled-date="disablePastDates"
                style="width: 100%"
              />
            </a-form-item>

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

            <a-form-item field="quantity" label="Số lượng phiếu">
              <a-input-number
                v-model="couponEditForm.quantity"
                :min="1"
                :max="100000"
                :precision="0"
                :disabled="couponEditForm.featured"
                :placeholder="couponEditForm.featured ? 'Tự động theo số khách hàng' : 'Tối đa: 100.000 phiếu'"
                style="width: 100%"
              />
            </a-form-item>

            <a-form-item field="description" label="Mô tả">
              <a-textarea
                v-model="couponEditForm.description"
                placeholder="Nhập mô tả cho phiếu giảm giá..."
                allow-clear
                :max-length="500"
                :auto-size="{ minRows: 3, maxRows: 6 }"
              />
            </a-form-item>

            <a-form-item field="active" label="Trạng thái">
              <a-radio-group v-model="couponEditForm.active" type="button">
                <a-radio :value="true">Hoạt động</a-radio>
                <a-radio :value="false">Không hoạt động</a-radio>
              </a-radio-group>
            </a-form-item>

            <a-form-item field="featured">
              <a-checkbox v-model="couponEditForm.featured">Phiếu giảm giá riêng tư</a-checkbox>
              <div style="margin-left: 24px; margin-top: 4px; font-size: 12px; color: var(--color-text-3)">
                Phiếu giảm giá riêng tư chỉ áp dụng cho khách hàng được chọn
              </div>
            </a-form-item>

            <a-form-item field="lyDoThayDoi" label="Lý do thay đổi">
              <a-textarea
                v-model="couponEditForm.lyDoThayDoi"
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

      <!-- Right Column: Customer Selection (only if featured) -->
      <a-col :span="12" v-if="couponEditForm.featured">
        <a-card title="Chọn khách hàng">
          <div class="customer-selection-section">
            <a-input-search v-model="customerSearchQuery" placeholder="Tìm kiếm khách hàng..." allow-clear style="margin-bottom: 12px" />

            <div style="margin-bottom: 12px; display: flex; gap: 8px">
              <a-button size="small" @click="selectAllEditCustomers">
                <template #icon>
                  <icon-plus />
                </template>
                Chọn tất cả
              </a-button>
              <a-button size="small" @click="deselectAllEditCustomers">
                <template #icon>
                  <icon-delete />
                </template>
                Bỏ chọn tất cả
              </a-button>
            </div>

            <a-table
              row-key="id"
              :columns="customerColumnsWithCheckbox"
              :data="filteredCustomers"
              :pagination="customerPagination"
              :loading="customersLoading"
              :scroll="{ y: 350 }"
              size="small"
              :bordered="{ cell: true }"
            >
              <template #selectHeader>
                <a-checkbox
                  :model-value="isAllEditCustomersSelected"
                  :indeterminate="isSomeEditCustomersSelected && !isAllEditCustomersSelected"
                  @change="toggleAllEditCustomers"
                />
              </template>
              <template #select="{ record }">
                <a-checkbox
                  :model-value="couponEditForm.selectedCustomerIds.includes(record.id)"
                  @change="toggleEditCustomerSelection(record.id)"
                />
              </template>
            </a-table>

            <div style="margin-top: 8px; font-size: 12px; color: var(--color-text-3)">
              Đã chọn:
              <strong>{{ couponEditForm.selectedCustomerIds.length }}</strong>
              khách hàng
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- Edit Confirmation Modal -->
    <a-modal
      v-model:visible="couponEditConfirmVisible"
      title="Xác nhận chỉnh sửa phiếu giảm giá"
      :confirm-loading="submitting"
      ok-text="Xác nhận"
      width="500px"
      @ok="confirmCouponEdit"
      @cancel="couponEditConfirmVisible = false"
    >
      <p>
        Bạn có chắc chắn muốn chỉnh sửa phiếu giảm giá
        <strong>{{ couponEditForm.name }}</strong>
        ?
      </p>
      <p class="modal-note">
        Lý do thay đổi:
        <strong>{{ couponEditForm.lyDoThayDoi }}</strong>
      </p>
      <p class="modal-note">Hành động này sẽ cập nhật thông tin phiếu giảm giá.</p>
    </a-modal>

    <!-- Scroll to Top Button -->
    <a-button v-show="showScrollTop" class="scroll-to-top-btn" type="primary" shape="circle" size="large" @click="scrollToTop">
      <template #icon>
        <icon-up />
      </template>
    </a-button>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, reactive, watch } from 'vue'
import router from '@/router'
import { Message } from '@arco-design/web-vue'
import axios from 'axios'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { fetchCustomers, type CustomerApiModel, updateCoupon } from '@/api/discount-management'
import type { FormInstance, FormRules } from '@arco-design/web-vue/es/form'
import { IconPlus, IconDelete, IconLeft, IconUp, IconCheck, IconClose } from '@arco-design/web-vue/es/icon'
import dayjs from 'dayjs'

// Router
const { breadcrumbItems } = useBreadcrumb()

// Get coupon ID from route params
const couponId = computed(() => Number(router.currentRoute.value.params.id))

// Loading state
const loading = ref(false)
const submitting = ref(false)
const couponEditConfirmVisible = ref(false)

// Form
const couponEditFormRef = ref<FormInstance>()
const couponEditForm = reactive({
  code: '',
  name: '',
  discountMode: 'percentage' as 'percentage' | 'amount',
  discountValue: 10,
  minOrder: 0,
  quantity: 1,
  dateRange: [] as string[],
  description: '',
  active: true,
  featured: false,
  selectedCustomerIds: [] as number[],
  lyDoThayDoi: '',
})

const disablePastDates = (current: Date | string) => {
  if (!current) return false
  return dayjs(current).isBefore(dayjs().startOf('day'))
}

// Store original values for change detection
const originalCouponEditForm = reactive({
  code: '',
  name: '',
  discountMode: 'percentage' as 'percentage' | 'amount',
  discountValue: 10,
  minOrder: 0,
  quantity: 1,
  dateRange: [] as string[],
  description: '',
  active: true,
  featured: false,
  selectedCustomerIds: [] as number[],
})

const couponEditRules: FormRules = {
  code: [{ required: true, message: 'Vui lòng nhập mã phiếu giảm giá' }],
  name: [{ required: true, message: 'Vui lòng nhập tên phiếu giảm giá' }],
  discountMode: [{ required: true, message: 'Vui lòng chọn hình thức giảm giá' }],
  discountValue: [{ required: true, message: 'Vui lòng nhập giá trị giảm' }],
  quantity: [{ required: true, message: 'Vui lòng nhập số lượng áp dụng' }],
  dateRange: [
    { required: true, type: 'array', message: 'Vui lòng chọn thời gian áp dụng' },
    {
      validator: (value: string[], callback: (msg?: string) => void) => {
        if (!Array.isArray(value) || value.length !== 2) {
          callback('Vui lòng chọn thời gian áp dụng')
          return
        }
        const [start, end] = value
        if (!start || !end) {
          callback('Vui lòng chọn thời gian áp dụng')
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
  lyDoThayDoi: [{ required: true, message: 'Vui lòng nhập lý do thay đổi' }],
}

const isPercentEdit = computed(() => couponEditForm.discountMode === 'percentage')

// Customers
const customers = ref<CustomerApiModel[]>([])
const customersLoading = ref(false)
const customerSearchQuery = ref('')

const customerColumns = [
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

const customerColumnsWithCheckbox = computed(() => [
  {
    title: '',
    dataIndex: 'select',
    slotName: 'select',
    width: 50,
    align: 'center' as const,
  },
  ...customerColumns,
])

const isAllEditCustomersSelected = computed(() => {
  if (filteredCustomers.value.length === 0) return false
  return filteredCustomers.value.every((customer) => couponEditForm.selectedCustomerIds.includes(customer.id))
})

const isSomeEditCustomersSelected = computed(() => {
  return couponEditForm.selectedCustomerIds.length > 0
})

const toggleEditCustomerSelection = (customerId: number) => {
  const index = couponEditForm.selectedCustomerIds.indexOf(customerId)
  if (index > -1) {
    couponEditForm.selectedCustomerIds.splice(index, 1)
  } else {
    couponEditForm.selectedCustomerIds.push(customerId)
  }
}

const toggleAllEditCustomers = () => {
  if (isAllEditCustomersSelected.value) {
    filteredCustomers.value.forEach((customer) => {
      const index = couponEditForm.selectedCustomerIds.indexOf(customer.id)
      if (index > -1) {
        couponEditForm.selectedCustomerIds.splice(index, 1)
      }
    })
  } else {
    filteredCustomers.value.forEach((customer) => {
      if (!couponEditForm.selectedCustomerIds.includes(customer.id)) {
        couponEditForm.selectedCustomerIds.push(customer.id)
      }
    })
  }
}

const selectAllEditCustomers = () => {
  filteredCustomers.value.forEach((customer) => {
    if (!couponEditForm.selectedCustomerIds.includes(customer.id)) {
      couponEditForm.selectedCustomerIds.push(customer.id)
    }
  })
}

const deselectAllEditCustomers = () => {
  filteredCustomers.value.forEach((customer) => {
    const index = couponEditForm.selectedCustomerIds.indexOf(customer.id)
    if (index > -1) {
      couponEditForm.selectedCustomerIds.splice(index, 1)
    }
  })
}

const loadCustomers = async () => {
  customersLoading.value = true
  try {
    const data = await fetchCustomers()

    if (data && Array.isArray(data)) {
      const activeCustomers = data.filter((c) => {
        return c.trangThai !== false && c.trangThai !== 0
      })
      customers.value = activeCustomers

      if (activeCustomers.length === 0) {
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

watch(
  () => couponEditForm.featured,
  (isFeatured) => {
    if (isFeatured && customers.value.length === 0) {
      loadCustomers()
    }
    if (!isFeatured) {
      couponEditForm.selectedCustomerIds = []
    }
  }
)

// Auto-update quantity based on selected customers for private vouchers
watch(
  () => couponEditForm.selectedCustomerIds.length,
  (count) => {
    if (couponEditForm.featured) {
      couponEditForm.quantity = count > 0 ? count : 1
    }
  }
)

// Products
interface ProductApiModel {
  id: number
  idSanPham?: {
    tenSanPham?: string
  }
  tenSanPhamChiTiet?: string
  maChiTietSanPham?: string
  anhSanPham?: string[]
  giaBan?: number
  soLuong?: number
  trangThai?: boolean
}

const products = ref<ProductApiModel[]>([])
const productsLoading = ref(false)
const productSearchQuery = ref('')

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

const filteredProducts = computed(() => {
  if (!productSearchQuery.value) {
    return products.value
  }
  const query = productSearchQuery.value.toLowerCase()
  return products.value.filter(
    (product) =>
      product.idSanPham?.tenSanPham?.toLowerCase().includes(query) ||
      product.tenSanPhamChiTiet?.toLowerCase().includes(query) ||
      product.maChiTietSanPham?.toLowerCase().includes(query)
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

const isAllEditProductsSelected = computed(() => {
  if (filteredProducts.value.length === 0) return false
  return filteredProducts.value.every((product) => couponEditForm.selectedProductIds.includes(product.id))
})

const isSomeEditProductsSelected = computed(() => {
  return couponEditForm.selectedProductIds.length > 0
})

const toggleEditProductSelection = (productId: number) => {
  const index = couponEditForm.selectedProductIds.indexOf(productId)
  if (index > -1) {
    couponEditForm.selectedProductIds.splice(index, 1)
  } else {
    couponEditForm.selectedProductIds.push(productId)
  }
}

const toggleAllEditProducts = () => {
  if (isAllEditProductsSelected.value) {
    filteredProducts.value.forEach((product) => {
      const index = couponEditForm.selectedProductIds.indexOf(product.id)
      if (index > -1) {
        couponEditForm.selectedProductIds.splice(index, 1)
      }
    })
  } else {
    filteredProducts.value.forEach((product) => {
      if (!couponEditForm.selectedProductIds.includes(product.id)) {
        couponEditForm.selectedProductIds.push(product.id)
      }
    })
  }
}

const selectAllEditProducts = () => {
  filteredProducts.value.forEach((product) => {
    if (!couponEditForm.selectedProductIds.includes(product.id)) {
      couponEditForm.selectedProductIds.push(product.id)
    }
  })
}

const deselectAllEditProducts = () => {
  filteredProducts.value.forEach((product) => {
    const index = couponEditForm.selectedProductIds.indexOf(product.id)
    if (index > -1) {
      couponEditForm.selectedProductIds.splice(index, 1)
    }
  })
}

const loadProducts = async () => {
  productsLoading.value = true
  try {
    const response = await axios.get('/api/chi-tiet-san-pham-management/playlist')
    const data = response.data.data || response.data

    if (data && Array.isArray(data)) {
      const activeProducts = data.filter((p: ProductApiModel) => {
        return p.trangThai !== false && (p.soLuong || 0) > 0
      })
      products.value = activeProducts

      if (activeProducts.length === 0) {
        Message.info('Không có sản phẩm hoạt động')
      }
    } else {
      products.value = []
      Message.warning('Dữ liệu sản phẩm không hợp lệ')
    }
  } catch {
    Message.error('Không thể tải danh sách sản phẩm')
    products.value = []
  } finally {
    productsLoading.value = false
  }
}

watch(
  () => couponEditForm.applyToProducts,
  (shouldApply) => {
    if (shouldApply && products.value.length === 0) {
      loadProducts()
    }
    if (!shouldApply) {
      couponEditForm.selectedProductIds = []
    }
  }
)

// Watchers
watch(
  () => couponEditForm.discountMode,
  (mode) => {
    if (mode === 'percentage' && couponEditForm.discountValue > 100) {
      couponEditForm.discountValue = 100
    }
  }
)

watch(
  () => couponEditForm.discountValue,
  (value) => {
    if (value === null || value === undefined) return

    if (value < 0) {
      couponEditForm.discountValue = 0
      return
    }

    if (isPercentEdit.value && value > 100) {
      couponEditForm.discountValue = 100
      Message.warning('Giá trị giảm theo phần trăm không được vượt quá 100%')
    }
  }
)

// Methods
const formatNumberWithSeparator = (value: number | string | undefined) => {
  if (value === null || value === undefined || value === '') return ''
  const numValue = typeof value === 'string' ? parseInt(value.replace(/\./g, ''), 10) : value
  if (Number.isNaN(numValue)) return ''
  return numValue.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.')
}

const parseFormattedNumber = (value: string | number | undefined) => {
  if (typeof value === 'number') return value
  if (!value || value === '') return undefined
  const cleaned = String(value).replace(/\./g, '')
  const parsed = Number(cleaned)
  return Number.isNaN(parsed) ? undefined : parsed
}

// Display value for discount input (with % or VND symbol)
const displayDiscountValue = ref('0.00%')
const isEditingDiscount = ref(false)

// Handle focus - remove % or VND for easy editing
const handleDiscountFocus = () => {
  isEditingDiscount.value = true
  displayDiscountValue.value = String(couponEditForm.discountValue)
}

const handleDiscountInput = () => {
  if (!isEditingDiscount.value) return

  if (isPercentEdit.value) {
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
    const cleanValue = displayDiscountValue.value.replace(/\./g, '')
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

  if (isPercentEdit.value) {
    // Percentage mode - parse as float to handle decimals
    const value = parseFloat(displayDiscountValue.value.replace(/[^0-9.]/g, ''))

    if (Number.isNaN(value) || value < 0) {
      couponEditForm.discountValue = 0
    } else if (value > 100) {
      couponEditForm.discountValue = 100
      Message.warning('Giá trị giảm theo phần trăm không được vượt quá 100%')
    } else {
      couponEditForm.discountValue = value
    }
    displayDiscountValue.value = `${couponEditForm.discountValue.toFixed(2)}%`
  } else {
    // Amount mode - remove thousand separators (dots) then parse
    const cleanValue = displayDiscountValue.value.replace(/\\./g, '').replace(/[^0-9]/g, '')
    const value = parseInt(cleanValue, 10)

    if (Number.isNaN(value) || value < 0) {
      couponEditForm.discountValue = 0
    } else if (value > 100000000) {
      couponEditForm.discountValue = 100000000
      Message.warning('Giá trị giảm không được vượt quá 100.000.000 VND')
    } else {
      couponEditForm.discountValue = Math.round(value)
    }
    displayDiscountValue.value = `${formatNumberWithSeparator(couponEditForm.discountValue)} VND`
  }
}

// Watch discountValue changes to update display
watch(
  () => couponEditForm.discountValue,
  (newValue) => {
    if (!isEditingDiscount.value) {
      if (isPercentEdit.value) {
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
  () => couponEditForm.discountMode,
  () => {
    if (isPercentEdit.value) {
      displayDiscountValue.value = `${couponEditForm.discountValue.toFixed(2)}%`
    } else {
      displayDiscountValue.value = `${formatNumberWithSeparator(couponEditForm.discountValue)} VND`
    }
  }
)

// Display value for minOrder field
const displayMinOrder = ref('0 VND')
const isEditingMinOrder = ref(false)

const handleMinOrderFocus = () => {
  isEditingMinOrder.value = true
  displayMinOrder.value = String(couponEditForm.minOrder || 0)
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
    couponEditForm.minOrder = 0
  } else if (value > 500000000) {
    couponEditForm.minOrder = 500000000
    Message.warning('Giá trị đơn hàng tối thiểu không được vượt quá 500.000.000 VND')
  } else {
    couponEditForm.minOrder = Math.round(value)
  }

  displayMinOrder.value = `${formatNumberWithSeparator(couponEditForm.minOrder)} VND`
}

watch(
  () => couponEditForm.minOrder,
  (newValue) => {
    if (!isEditingMinOrder.value) {
      displayMinOrder.value = `${formatNumberWithSeparator(newValue)} VND`
    }
  },
  { immediate: true }
)

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

const goBack = () => {
  router.push({ name: 'QuanLyPhieuGiamGia' })
}

// Load coupon data
const loadCouponData = async () => {
  loading.value = true
  try {
    const response = await axios.get(`/api/phieu-giam-gia-management/detail/${couponId.value}`)
    const coupon = response.data.data || response.data

    couponEditForm.code = coupon.maPhieuGiamGia ?? ''
    couponEditForm.name = coupon.tenPhieuGiamGia ?? ''

    const discountType = coupon.loaiPhieuGiamGia ? 'fixed' : 'percentage'
    couponEditForm.discountMode = discountType === 'percentage' ? 'percentage' : 'amount'
    couponEditForm.discountValue = Number(coupon.giaTriGiamGia ?? 0)
    couponEditForm.minOrder = coupon.hoaDonToiThieu ?? 0
    couponEditForm.quantity = coupon.soLuongDung ?? 1
    couponEditForm.dateRange = [coupon.ngayBatDau ?? '', coupon.ngayKetThuc ?? ''].filter(Boolean) as string[]
    couponEditForm.description = coupon.moTa ?? ''
    couponEditForm.active = Boolean(coupon.trangThai)
    couponEditForm.featured = coupon.featured ?? false
    couponEditForm.selectedCustomerIds = coupon.idKhachHang ?? []

    // Store original values
    Object.assign(originalCouponEditForm, {
      code: couponEditForm.code,
      name: couponEditForm.name,
      discountMode: couponEditForm.discountMode,
      discountValue: couponEditForm.discountValue,
      minOrder: couponEditForm.minOrder,
      quantity: couponEditForm.quantity,
      dateRange: [...couponEditForm.dateRange],
      description: couponEditForm.description,
      active: couponEditForm.active,
      featured: couponEditForm.featured,
      selectedCustomerIds: [...couponEditForm.selectedCustomerIds],
    })

    // Load customers if featured
    if (coupon.featured && customers.value.length === 0) {
      await loadCustomers()
    }

    // Load products for this coupon
    try {
      const productResponse = await axios.get(`/api/chi-tiet-phieu-giam-gia-management/by-phieu-giam-gia/${couponId.value}`)

      let productDetails: any[] = []
      if (Array.isArray(productResponse.data)) {
        productDetails = productResponse.data
      } else if (productResponse.data?.isSuccess && productResponse.data.data && Array.isArray(productResponse.data.data)) {
        productDetails = productResponse.data.data
      }

      if (productDetails.length > 0) {
        couponEditForm.applyToProducts = true
        couponEditForm.selectedProductIds.splice(0)
        const productIds = productDetails.map((item: any) => item.idChiTietSanPham)
        couponEditForm.selectedProductIds.push(...productIds)
        originalCouponEditForm.applyToProducts = true
        originalCouponEditForm.selectedProductIds = [...productIds]
      } else {
        couponEditForm.applyToProducts = false
        couponEditForm.selectedProductIds.splice(0)
        originalCouponEditForm.applyToProducts = false
        originalCouponEditForm.selectedProductIds = []
      }
    } catch {
      couponEditForm.applyToProducts = false
      couponEditForm.selectedProductIds = []
      originalCouponEditForm.applyToProducts = false
      originalCouponEditForm.selectedProductIds = []
    }

    // Load products list if not loaded
    if (products.value.length === 0) {
      await loadProducts()
    }
  } catch {
    Message.error('Không thể tải thông tin phiếu giảm giá')
    goBack()
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (submitting.value) return

  const form = couponEditFormRef.value
  if (!form) return

  try {
    await form.validate()
  } catch {
    return
  }

  // Explicitly check lyDoThayDoi
  if (!couponEditForm.lyDoThayDoi || !couponEditForm.lyDoThayDoi.trim()) {
    Message.error('Vui lòng nhập lý do thay đổi')
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

  if (!isPercentEdit.value) {
    const minOrderValue = Number(couponEditForm.minOrder || 0)
    if (minOrderValue > 0 && discountValue >= minOrderValue) {
      Message.error('Giá trị giảm phải nhỏ hơn giá trị đơn hàng tối thiểu')
      return
    }
  }

  const quantityValue = Number(couponEditForm.quantity)
  if (!Number.isInteger(quantityValue) || quantityValue <= 0) {
    Message.error('Số lượng áp dụng phải lớn hơn 0')
    return
  }

  if (couponEditForm.featured && couponEditForm.selectedCustomerIds.length === 0) {
    Message.error('Vui lòng chọn ít nhất một khách hàng cho phiếu giảm giá riêng tư')
    return
  }

  if (couponEditForm.applyToProducts && couponEditForm.selectedProductIds.length === 0) {
    Message.error('Vui lòng chọn ít nhất một sản phẩm để áp dụng giảm giá!')
    return
  }

  // Check if any changes were made
  const hasChanges =
    couponEditForm.code !== originalCouponEditForm.code ||
    couponEditForm.name !== originalCouponEditForm.name ||
    couponEditForm.discountMode !== originalCouponEditForm.discountMode ||
    couponEditForm.discountValue !== originalCouponEditForm.discountValue ||
    couponEditForm.minOrder !== originalCouponEditForm.minOrder ||
    couponEditForm.quantity !== originalCouponEditForm.quantity ||
    couponEditForm.dateRange[0] !== originalCouponEditForm.dateRange[0] ||
    couponEditForm.dateRange[1] !== originalCouponEditForm.dateRange[1] ||
    couponEditForm.description !== originalCouponEditForm.description ||
    couponEditForm.active !== originalCouponEditForm.active ||
    couponEditForm.featured !== originalCouponEditForm.featured ||
    couponEditForm.applyToProducts !== originalCouponEditForm.applyToProducts ||
    JSON.stringify([...couponEditForm.selectedCustomerIds].sort()) !==
      JSON.stringify([...originalCouponEditForm.selectedCustomerIds].sort()) ||
    JSON.stringify([...couponEditForm.selectedProductIds].sort()) !== JSON.stringify([...originalCouponEditForm.selectedProductIds].sort())

  if (!hasChanges) {
    Message.warning('Không có thay đổi nào để cập nhật')
    return
  }

  couponEditConfirmVisible.value = true
}

const confirmCouponEdit = async () => {
  // Validate lý do thay đổi before submission
  if (!couponEditForm.lyDoThayDoi || !couponEditForm.lyDoThayDoi.trim()) {
    Message.error('Vui lòng nhập lý do thay đổi')
    couponEditConfirmVisible.value = false
    return
  }

  const discountValue = Number(couponEditForm.discountValue)
  const quantityValue = Number(couponEditForm.quantity)
  const [startDate, endDate] = couponEditForm.dateRange

  const payload = {
    maPhieuGiamGia: couponEditForm.code.trim(),
    tenPhieuGiamGia: couponEditForm.name.trim(),
    loaiPhieuGiamGia: couponEditForm.discountMode === 'amount',
    giaTriGiamGia: discountValue,
    soTienToiDa: null,
    hoaDonToiThieu: couponEditForm.minOrder ? Number(couponEditForm.minOrder) : 0,
    soLuongDung: quantityValue,
    ngayBatDau: startDate,
    ngayKetThuc: endDate,
    trangThai: couponEditForm.active,
    moTa: couponEditForm.description.trim() || null,
    deleted: false,
    idKhachHang: couponEditForm.featured ? couponEditForm.selectedCustomerIds : [],
    featured: couponEditForm.featured,
    lyDoThayDoi: couponEditForm.lyDoThayDoi.trim(),
  }

  submitting.value = true
  try {
    await updateCoupon(couponId.value, payload)

    const productIds = couponEditForm.applyToProducts ? [...couponEditForm.selectedProductIds] : []
    await axios.put('/api/chi-tiet-phieu-giam-gia-management/update-by-phieu-giam-gia', productIds, {
      params: { idPhieuGiamGia: couponId.value },
    })

    Message.success('Cập nhật phiếu giảm giá thành công')
    couponEditConfirmVisible.value = false
    goBack()
  } catch (error: any) {
    const errorMessage = error.response?.data?.message || error.message || 'Không thể cập nhật phiếu giảm giá'
    Message.error(errorMessage)
  } finally {
    submitting.value = false
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

onMounted(() => {
  loadCouponData()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.coupon-edit-page {
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

.customer-selection-section {
  border: 1px solid var(--color-border-2);
  border-radius: 8px;
  padding: 16px;
  background: var(--color-bg-2);
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
</style>
