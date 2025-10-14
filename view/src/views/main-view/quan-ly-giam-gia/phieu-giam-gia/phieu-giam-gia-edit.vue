<template>
  <div class="coupon-edit-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Page Header -->
    <a-card class="header-card">
      <div class="page-header">
        <div>
          <h2 class="page-title">Chỉnh sửa phiếu giảm giá</h2>
          <p class="page-description">Cập nhật thông tin phiếu giảm giá</p>
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
      <a-col :span="couponEditForm.featured ? 12 : 24">
        <a-card title="Thông tin phiếu giảm giá">
          <a-form ref="couponEditFormRef" :model="couponEditForm" :rules="couponEditRules" layout="vertical">
            <a-form-item field="code" label="Mã phiếu giảm giá">
              <a-input v-model="couponEditForm.code" placeholder="Mã tự động" readonly disabled />
            </a-form-item>
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
                    :max="isPercentEdit ? 100 : 100000000"
                    :step="isPercentEdit ? 1 : 1000"
                    :precision="0"
                    :formatter="isPercentEdit ? undefined : formatNumberWithSeparator"
                    :parser="isPercentEdit ? undefined : parseFormattedNumber"
                    :suffix="isPercentEdit ? '%' : 'VND'"
                    placeholder="Nhập giá trị giảm..."
                    style="width: 100%"
                  />
                  <div style="margin-top: 4px; font-size: 12px; color: var(--color-text-3)">
                    {{ isPercentEdit ? 'Giá trị từ 1-100' : 'Tối đa: 100.000.000 VND' }}
                  </div>
                </a-form-item>
              </a-col>
              <a-col :span="12" v-if="isPercentEdit">
                <a-form-item field="maxDiscount" label="Giá trị giảm tối đa">
                  <a-input-number
                    v-model="couponEditForm.maxDiscount"
                    :min="1"
                    :max="50000000"
                    :step="10000"
                    :precision="0"
                    :formatter="formatNumberWithSeparator"
                    :parser="parseFormattedNumber"
                    suffix="VND"
                    placeholder="Nhập giá trị tối đa..."
                    style="width: 100%"
                  />
                  <div style="margin-top: 4px; font-size: 12px; color: var(--color-text-3)">Tối đa: 50.000.000 VND</div>
                </a-form-item>
              </a-col>
            </a-row>

            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item field="minOrder" label="Giá trị đơn hàng tối thiểu">
                  <a-input-number
                    v-model="couponEditForm.minOrder"
                    :min="0"
                    :max="500000000"
                    :step="10000"
                    :precision="0"
                    :formatter="formatNumberWithSeparator"
                    :parser="parseFormattedNumber"
                    suffix="VND"
                    placeholder="Nhập giá trị đơn hàng tối thiểu..."
                    style="width: 100%"
                  />
                  <div style="margin-top: 4px; font-size: 12px; color: var(--color-text-3)">Tối đa: 500.000.000 VND</div>
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item field="quantity" label="Số lượng phiếu">
                  <a-input-number
                    v-model="couponEditForm.quantity"
                    :min="1"
                    :max="100000"
                    :precision="0"
                    placeholder="Nhập số lượng phiếu..."
                    style="width: 100%"
                  />
                  <div style="margin-top: 4px; font-size: 12px; color: var(--color-text-3)">Tối đa: 100.000 phiếu</div>
                </a-form-item>
              </a-col>
            </a-row>

            <a-form-item field="dateRange" label="Thời gian áp dụng">
              <a-range-picker
                v-model="couponEditForm.dateRange"
                :show-time="true"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="DD/MM/YYYY HH:mm"
                allow-clear
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
              <a-switch v-model="couponEditForm.active" checked-children="Hoạt động" un-checked-children="Không hoạt động" />
            </a-form-item>

            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item field="featured">
                  <a-checkbox v-model="couponEditForm.featured">Phiếu giảm giá nổi bật</a-checkbox>
                  <div style="margin-left: 24px; margin-top: 4px; font-size: 12px; color: var(--color-text-3)">
                    Phiếu giảm giá nổi bật sẽ được hiển thị ở đầu danh sách
                  </div>
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item field="applyToProducts">
                  <a-checkbox v-model="couponEditForm.applyToProducts">Áp dụng cho sản phẩm cụ thể</a-checkbox>
                  <div style="margin-left: 24px; margin-top: 4px; font-size: 12px; color: var(--color-text-3)">
                    Nếu bật, phiếu giảm giá chỉ áp dụng cho các sản phẩm được chọn
                  </div>
                </a-form-item>
              </a-col>
            </a-row>

            <a-form-item field="lyDoThayDoi" label="Lý do thay đổi">
              <a-textarea
                v-model="couponEditForm.lyDoThayDoi"
                placeholder="Nhập lý do thay đổi..."
                allow-clear
                :max-length="500"
                :auto-size="{ minRows: 3, maxRows: 6 }"
              />
            </a-form-item>

            <!-- Product Selection Section -->
            <div v-if="couponEditForm.applyToProducts">
              <a-divider />
              <div style="font-weight: 600; margin-bottom: 12px; font-size: 15px">Chọn sản phẩm áp dụng</div>
              <div class="product-selection-section">
                <a-input-search v-model="productSearchQuery" placeholder="Tìm kiếm sản phẩm..." allow-clear style="margin-bottom: 12px" />

                <div style="margin-bottom: 12px; display: flex; gap: 8px">
                  <a-button size="small" @click="selectAllEditProducts">
                    <template #icon>
                      <icon-plus />
                    </template>
                    Chọn tất cả
                  </a-button>
                  <a-button size="small" @click="deselectAllEditProducts">
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
                  :loading="productsLoading"
                  :scroll="{ y: 300 }"
                  size="small"
                  :bordered="{ cell: true }"
                >
                  <template #selectHeader>
                    <a-checkbox
                      :model-value="isAllEditProductsSelected"
                      :indeterminate="isSomeEditProductsSelected && !isAllEditProductsSelected"
                      @change="toggleAllEditProducts"
                    />
                  </template>
                  <template #select="{ record }">
                    <a-checkbox
                      :model-value="couponEditForm.selectedProductIds.includes(record.id)"
                      @change="() => toggleEditProductSelection(record.id)"
                    />
                  </template>
                  <template #anhSanPham="{ record }">
                    <div class="product-image-cell">
                      <img
                        v-if="record.anhSanPham && record.anhSanPham.length > 0"
                        :src="record.anhSanPham[0]"
                        :alt="record.tenSanPhamChiTiet || record.idSanPham?.tenSanPham"
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
                      <span>{{ record.tenSanPhamChiTiet || record.idSanPham?.tenSanPham || 'N/A' }}</span>
                    </div>
                  </template>
                  <template #giaBan="{ record }">
                    <span>{{ formatCurrency(record.giaBan || 0) }}</span>
                  </template>
                </a-table>

                <div style="margin-top: 8px; font-size: 12px; color: var(--color-text-3)">
                  Đã chọn:
                  <strong>{{ couponEditForm.selectedProductIds.length }}</strong>
                  sản phẩm
                </div>
              </div>
            </div>
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import axios from 'axios'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { fetchCustomers, type CustomerApiModel, updateCoupon } from '@/api/discount-management'
import type { FormInstance, FormRules } from '@arco-design/web-vue/es/form'
import { IconPlus, IconDelete, IconLeft, IconImage } from '@arco-design/web-vue/es/icon'

// Router
const router = useRouter()
const route = useRoute()
const { breadcrumbItems } = useBreadcrumb()

// Get coupon ID from route params
const couponId = computed(() => Number(route.params.id))

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
  maxDiscount: null as number | null,
  minOrder: 0,
  quantity: 1,
  dateRange: [] as string[],
  description: '',
  active: true,
  featured: false,
  applyToProducts: false,
  selectedCustomerIds: [] as number[],
  selectedProductIds: [] as number[],
  lyDoThayDoi: '',
})

// Store original values for change detection
const originalCouponEditForm = reactive({
  code: '',
  name: '',
  discountMode: 'percentage' as 'percentage' | 'amount',
  discountValue: 10,
  maxDiscount: null as number | null,
  minOrder: 0,
  quantity: 1,
  dateRange: [] as string[],
  description: '',
  active: true,
  featured: false,
  applyToProducts: false,
  selectedCustomerIds: [] as number[],
  selectedProductIds: [] as number[],
})

const couponEditRules: FormRules = {
  name: [{ required: true, message: 'Vui lòng nhập tên phiếu giảm giá' }],
  discountMode: [{ required: true, message: 'Vui lòng chọn hình thức giảm giá' }],
  discountValue: [{ required: true, message: 'Vui lòng nhập giá trị giảm' }],
  quantity: [{ required: true, message: 'Vui lòng nhập số lượng áp dụng' }],
  dateRange: [{ required: true, type: 'array', message: 'Vui lòng chọn thời gian áp dụng' }],
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
    if (mode === 'amount') {
      couponEditForm.maxDiscount = null
    } else if (couponEditForm.discountValue > 100) {
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
  const numValue = typeof value === 'string' ? parseFloat(value.replace(/\./g, '')) : value
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
    couponEditForm.maxDiscount = discountType === 'percentage' ? (coupon.soTienToiDa ?? null) : null
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
      maxDiscount: couponEditForm.maxDiscount,
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
    if (capValue > 50000000) {
      Message.error('Giá trị giảm tối đa không được vượt quá 50.000.000 VND')
      return
    }

    const minOrderValue = Number(couponEditForm.minOrder || 0)
    if (minOrderValue > 0 && capValue >= minOrderValue) {
      Message.error('Giá trị giảm tối đa phải nhỏ hơn giá trị đơn hàng tối thiểu')
      return
    }
  } else {
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
    Message.error('Vui lòng chọn ít nhất một khách hàng cho phiếu giảm giá nổi bật')
    return
  }

  if (couponEditForm.applyToProducts && couponEditForm.selectedProductIds.length === 0) {
    Message.error('Vui lòng chọn ít nhất một sản phẩm để áp dụng giảm giá!')
    return
  }

  // Check if any changes were made
  const hasChanges =
    couponEditForm.name !== originalCouponEditForm.name ||
    couponEditForm.discountMode !== originalCouponEditForm.discountMode ||
    couponEditForm.discountValue !== originalCouponEditForm.discountValue ||
    couponEditForm.maxDiscount !== originalCouponEditForm.maxDiscount ||
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
  const discountValue = Number(couponEditForm.discountValue)
  const quantityValue = Number(couponEditForm.quantity)
  const [startDate, endDate] = couponEditForm.dateRange

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

onMounted(() => {
  loadCouponData()
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
</style>
