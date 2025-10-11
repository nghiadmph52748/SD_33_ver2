<template>
  <div class="coupon-create-page">
    <Breadcrumb :items="breadcrumbItems" />

    <a-card class="coupon-card" :class="{ 'featured-layout': formState.featured }">
      <div class="page-header">
        <div>
          <h2 class="page-title">Tạo phiếu giảm giá</h2>
          <p class="page-description">Cấu hình mã giảm giá trước khi phân phối đến khách hàng.</p>
        </div>
        <a-space>
          <a-button @click="goBack">Quay lại</a-button>
          <a-button type="primary" :loading="confirmSaveSubmitting" @click="handleSaveClick">Lưu phiếu giảm giá</a-button>
        </a-space>
      </div>

      <div :class="formState.featured ? 'two-column-container' : ''">
        <!-- Left Column: Form -->
        <div :class="formState.featured ? 'left-column' : ''">
          <div v-if="formState.featured" class="column-title">Thông tin phiếu giảm giá</div>

          <a-form ref="formRef" :model="formState" :rules="rules" layout="vertical">
            <!-- Mã phiếu giảm giá -->
            <a-form-item field="code" label="Mã phiếu giảm giá">
              <a-input v-model="formState.code" placeholder="Mã tự động" :disabled="true" />
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
              <a-input-number
                v-model="formState.discountValue"
                :min="1"
                :max="isPercent ? 100 : 100000000"
                :precision="isPercent ? 2 : 0"
                :formatter="(value) => (isPercent ? value : formatNumberWithSeparator(value))"
                :parser="(value) => parseFormattedNumber(value)"
                :suffix="isPercent ? '%' : 'VND'"
                placeholder="Nhập giá trị giảm..."
                style="width: 100%"
              />
              <div style="margin-top: 4px; font-size: 12px; color: var(--color-text-3)">
                {{ isPercent ? 'Giá trị từ 1-100' : 'Tối đa: 100.000.000 VND' }}
              </div>
            </a-form-item>

            <!-- Giá trị tối đa (for percentage) -->
            <a-form-item v-if="isPercent" field="maxDiscount" label="Giá trị giảm tối đa">
              <a-input-number
                v-model="formState.maxDiscount"
                :min="1"
                :max="50000000"
                :precision="0"
                :formatter="formatNumberWithSeparator"
                :parser="parseFormattedNumber"
                suffix="VND"
                placeholder="Nhập giá trị tối đa..."
                style="width: 100%"
              />
              <div style="margin-top: 4px; font-size: 12px; color: var(--color-text-3)">
                Giá trị giảm tối đa không được vượt quá 50.000.000 VND
              </div>
            </a-form-item>

            <!-- Date range side by side -->
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item field="startDate" label="Ngày bắt đầu">
                  <a-date-picker
                    v-model="formState.startDate"
                    :show-time="true"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    format="DD/MM/YYYY HH:mm"
                    allow-clear
                    style="width: 100%"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item field="endDate" label="Ngày kết thúc">
                  <a-date-picker
                    v-model="formState.endDate"
                    :show-time="true"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    format="DD/MM/YYYY HH:mm"
                    allow-clear
                    style="width: 100%"
                  />
                </a-form-item>
              </a-col>
            </a-row>

            <!-- Giá trị đơn hàng tối thiểu -->
            <a-form-item field="minOrder" label="Giá trị đơn hàng tối thiểu">
              <a-input-number
                v-model="formState.minOrder"
                :min="0"
                :max="500000000"
                :precision="0"
                :formatter="formatNumberWithSeparator"
                :parser="parseFormattedNumber"
                suffix="VND"
                placeholder="Nhập giá trị đơn hàng tối thiểu..."
                style="width: 100%"
              />
              <div style="margin-top: 4px; font-size: 12px; color: var(--color-text-3)">Tối đa: 500.000.000 VND</div>
            </a-form-item>

            <!-- Số lượng phiếu -->
            <a-form-item field="quantity" label="Số lượng phiếu">
              <a-input-number
                v-model="formState.quantity"
                :min="1"
                :max="100000"
                :precision="0"
                placeholder="Nhập số lượng phiếu..."
                style="width: 100%"
              />
              <div style="margin-top: 4px; font-size: 12px; color: var(--color-text-3)">Tối đa: 100.000 phiếu</div>
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

            <!-- Phiếu giảm giá nổi bật -->
            <a-form-item field="featured">
              <a-checkbox v-model="formState.featured">Phiếu giảm giá nổi bật</a-checkbox>
              <div style="margin-left: 24px; margin-top: 4px; font-size: 12px; color: var(--color-text-3)">
                Phiếu giảm giá nổi bật sẽ chỉ áp dụng cho khách hàng được chọn
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
        <a-descriptions-item v-if="isPercent && formState.maxDiscount" label="Giá trị giảm tối đa">
          {{ formatCurrency(formState.maxDiscount) }}
        </a-descriptions-item>
        <a-descriptions-item label="Giá trị đơn hàng tối thiểu">
          {{ formState.minOrder ? formatCurrency(formState.minOrder) : 'Không giới hạn' }}
        </a-descriptions-item>
        <a-descriptions-item label="Thời gian áp dụng">
          {{ formatDateRange(formState.startDate, formState.endDate) }}
        </a-descriptions-item>
        <a-descriptions-item label="Số lượng phiếu">
          {{ formState.quantity }}
        </a-descriptions-item>
        <a-descriptions-item v-if="formState.description" label="Mô tả">
          {{ formState.description }}
        </a-descriptions-item>
        <a-descriptions-item label="Phiếu giảm giá nổi bật">
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
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch, onMounted } from 'vue'
import type { FormInstance, FormRules } from '@arco-design/web-vue/es/form'
import { Message } from '@arco-design/web-vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { useRouter } from 'vue-router'
import { createCoupon, fetchCoupons, fetchCustomers, type CustomerApiModel } from '@/api/discount-management'

const { breadcrumbItems } = useBreadcrumb()
const router = useRouter()

const formRef = ref<FormInstance>()
const confirmSaveVisible = ref(false)
const confirmSaveSubmitting = ref(false)

const formState = reactive({
  code: '',
  name: '',
  discountMode: 'percentage' as 'percentage' | 'amount',
  discountValue: 10,
  maxDiscount: null as number | null,
  minOrder: 0,
  startDate: '',
  endDate: '',
  quantity: 1,
  description: '',
  featured: false,
  selectedCustomerIds: [] as number[],
})

const rules: FormRules = {
  name: [{ required: true, message: 'Vui lòng nhập tên phiếu giảm giá' }],
  discountMode: [{ required: true, message: 'Vui lòng chọn hình thức giảm giá' }],
  discountValue: [{ required: true, message: 'Vui lòng nhập giá trị giảm' }],
  quantity: [{ required: true, message: 'Vui lòng nhập số lượng áp dụng' }],
  startDate: [{ required: true, message: 'Vui lòng chọn ngày bắt đầu' }],
  endDate: [{ required: true, message: 'Vui lòng chọn ngày kết thúc' }],
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
  } catch (error) {
    Message.error('Không thể tải danh sách khách hàng')
    customers.value = []
  } finally {
    customersLoading.value = false
  }
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
    }
  }
)

watch(
  () => formState.discountMode,
  (mode) => {
    if (mode === 'amount') {
      formState.maxDiscount = null
    } else if (formState.discountValue > 100) {
      formState.discountValue = 100
    }
  }
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

// Real-time validation for max discount value
watch(
  () => formState.maxDiscount,
  (newValue) => {
    if (newValue !== undefined && newValue !== null && newValue > 50000000) {
      Message.warning('Giá trị giảm tối đa không được vượt quá 50.000.000 VND')
      formState.maxDiscount = 50000000
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

onMounted(async () => {
  formState.code = await generateNextCode()
})

const goBack = () => {
  router.back()
}

const getCustomerNameById = (customerId: number): string => {
  const customer = customers.value.find((c) => c.id === customerId)
  return customer ? `${customer.tenKhachHang} - ${customer.soDienThoai}` : `KH#${customerId}`
}

const formatNumberWithSeparator = (value: number | string) => {
  if (value === null || value === undefined || value === '') return ''
  const stringValue = String(value)
  const parts = stringValue.split('.')
  parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, '.')
  return parts.join('.')
}

const parseFormattedNumber = (value: string | number) => {
  if (typeof value === 'number') return value
  if (!value) return 0
  return Number(value.replace(/\./g, ''))
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

  if (!formState.startDate || !formState.endDate) {
    Message.error('Vui lòng chọn thời gian áp dụng')
    return
  }
  if (new Date(formState.startDate) > new Date(formState.endDate)) {
    Message.error('Ngày kết thúc phải sau ngày bắt đầu')
    return
  }

  if (isPercent.value) {
    const capValue = Number(formState.maxDiscount)
    if (!capValue || Number.isNaN(capValue) || capValue <= 0) {
      Message.error('Vui lòng nhập mức giảm tối đa hợp lệ')
      return
    }
    if (capValue > 50000000) {
      Message.error('Giá trị giảm tối đa không được vượt quá 50.000.000 VND')
      return
    }

    // Validate: Max discount should not exceed min order value
    const minOrderValue = Number(formState.minOrder || 0)
    if (minOrderValue > 0 && capValue >= minOrderValue) {
      Message.error('Giá trị giảm tối đa phải nhỏ hơn giá trị đơn hàng tối thiểu')
      return
    }
  } else {
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

  // Validate customer selection for featured vouchers
  if (formState.featured && formState.selectedCustomerIds.length === 0) {
    Message.error('Vui lòng chọn ít nhất một khách hàng cho phiếu giảm giá nổi bật')
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
    soTienToiDa: isPercent.value ? Number(formState.maxDiscount ?? 0) : null,
    hoaDonToiThieu: formState.minOrder ? Number(formState.minOrder) : 0,
    soLuongDung: quantityValue,
    ngayBatDau: formState.startDate,
    ngayKetThuc: formState.endDate,
    trangThai: calculateStatus(formState.startDate, formState.endDate),
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

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  gap: 16px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-1);
}

.page-description {
  margin: 4px 0 0 0;
  color: var(--color-text-3);
}

.form-footer {
  display: flex;
  justify-content: flex-end;
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

.featured-layout .page-header {
  padding: 20px;
  border-bottom: 1px solid var(--color-border-2);
  margin-bottom: 0;
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
</style>
