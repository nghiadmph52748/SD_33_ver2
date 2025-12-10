<template>
  <div class="coupon-edit-page">
    <!-- Back Button -->
    <div style="margin-bottom: 16px">
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
      <a-col :span="couponEditForm.featured ? 11 : 24" class="left-column-edit">
        <a-card :title="t('discount.coupon.info')">
          <div class="template-selection">
            <div class="template-selection-header">
              <div>
                <div class="template-selection-title">Chọn mẫu phiếu</div>
                <div class="template-selection-subtitle">Áp dụng nhanh cấu hình phổ biến</div>
              </div>
              <a-button type="text" size="mini" :disabled="!selectedTemplateId" @click="clearTemplateSelection">Bỏ chọn</a-button>
            </div>
            <div class="template-grid">
              <div
                v-for="template in couponTemplateOptions"
                :key="template.id"
                class="template-card"
                :class="[{ 'template-card-selected': selectedTemplateId === template.id }, `accent-${template.accent}`]"
                @click="applyTemplate(template)"
              >
                <div class="template-card-icon">
                  <component :is="templateIconMap[template.icon]" />
                </div>
                <div class="template-card-body">
                  <div class="template-card-title">{{ template.title }}</div>
                  <div class="template-card-description">{{ template.description }}</div>
                  <div class="template-card-tags">
                    <a-tag v-for="tag in template.highlights" :key="tag" size="small" bordered>{{ tag }}</a-tag>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <a-form ref="couponEditFormRef" :model="couponEditForm" :rules="couponEditRules" layout="vertical">
            <a-form-item field="code" :label="t('discount.coupon.code')">
              <a-input v-model="couponEditForm.code" :placeholder="t('discount.coupon.codePlaceholder')" allow-clear />
            </a-form-item>

            <a-form-item field="name" :label="t('discount.coupon.name')">
              <a-input v-model="couponEditForm.name" :placeholder="t('discount.coupon.namePlaceholder')" allow-clear />
            </a-form-item>

            <a-form-item field="discountMode" :label="t('discount.coupon.discountType')">
              <a-select v-model="couponEditForm.discountMode" :placeholder="t('discount.coupon.discountTypePlaceholder')">
                <a-option value="percentage">{{ t('discount.coupon.discountType.percentage') }}</a-option>
                <a-option value="amount">{{ t('discount.coupon.discountType.amount') }}</a-option>
              </a-select>
            </a-form-item>

            <a-form-item field="discountValue" :label="t('discount.coupon.discountValue')">
              <a-input
                v-model="displayDiscountValue"
                @blur="handleDiscountBlur"
                @focus="handleDiscountFocus"
                @input="handleDiscountInput"
                :placeholder="
                  isPercentEdit ? t('discount.coupon.discountValuePercentPlaceholder') : t('discount.coupon.discountValueAmountPlaceholder')
                "
                style="width: 100%"
              />
            </a-form-item>

            <!-- Giá trị giảm tối đa (chỉ khi %)-->
            <a-form-item v-if="isPercentEdit" field="maxDiscount" :label="t('discount.coupon.maxDiscount')">
              <a-input
                v-model="displayMaxDiscount"
                @blur="handleMaxDiscountBlur"
                @focus="handleMaxDiscountFocus"
                @input="handleMaxDiscountInput"
                :placeholder="'Tối đa: 50.000.000 VND'"
                style="width: 100%"
              />
            </a-form-item>

            <a-form-item field="dateRange" :label="t('discount.coupon.dateRange')">
              <a-range-picker
                v-model="couponEditForm.dateRange"
                :show-time="true"
                value-format="YYYY-MM-DDTHH:mm:ss"
                format="DD/MM/YYYY HH:mm"
                allow-clear
                :disabled-date="disablePastDates"
                style="width: 100%"
              />
            </a-form-item>

            <a-form-item field="minOrder" :label="t('discount.coupon.minOrder')">
              <a-input
                v-model="displayMinOrder"
                @blur="handleMinOrderBlur"
                @focus="handleMinOrderFocus"
                @input="handleMinOrderInput"
                :placeholder="t('discount.coupon.minOrderPlaceholder')"
                style="width: 100%"
              />
            </a-form-item>

            <a-form-item field="quantity" :label="t('discount.coupon.quantity')">
              <a-input-number
                v-model="couponEditForm.quantity"
                :min="1"
                :max="100000"
                :precision="0"
                :disabled="couponEditForm.featured"
                :placeholder="
                  couponEditForm.featured ? t('discount.coupon.quantityAutoPlaceholder') : t('discount.coupon.quantityPlaceholder')
                "
                style="width: 100%"
              />
            </a-form-item>

            <a-form-item field="description" :label="t('discount.coupon.description')">
              <a-textarea
                v-model="couponEditForm.description"
                :placeholder="t('discount.coupon.descriptionPlaceholder')"
                allow-clear
                :max-length="500"
                :auto-size="{ minRows: 3, maxRows: 6 }"
              />
            </a-form-item>

            <a-form-item field="active" :label="t('discount.coupon.status')">
              <a-radio-group v-model="couponEditForm.active" type="button">
                <a-radio :value="true">{{ t('discount.coupon.status.active') }}</a-radio>
                <a-radio :value="false">{{ t('discount.coupon.status.inactive') }}</a-radio>
              </a-radio-group>
            </a-form-item>

            <a-form-item field="featured">
              <a-checkbox v-model="couponEditForm.featured">{{ t('discount.coupon.privateCoupon') }}</a-checkbox>
              <div style="margin-left: 24px; margin-top: 4px; font-size: 12px; color: var(--color-text-3)">
                {{ t('discount.coupon.privateCouponDescription') }}
              </div>
            </a-form-item>

            <a-form-item field="lyDoThayDoi" :label="t('discount.coupon.changeReason')">
              <a-textarea
                v-model="couponEditForm.lyDoThayDoi"
                :placeholder="t('discount.coupon.changeReasonPlaceholder')"
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

      <!-- Right Column: Customer Selection (only if featured) -->
      <a-col :span="13" v-if="couponEditForm.featured" class="right-column-edit">
        <a-card :title="t('discount.coupon.selectCustomers')">
          <div class="customer-selection-section">
            <!-- Segmentation Tabs -->
            <a-tabs v-model:active-key="activeSegmentTab" type="card" class="segment-tabs">
              <a-tab-pane key="behavior" title="Hành vi">
                <div class="segment-options">
                  <a-radio-group v-model="behaviorSegment" @change="handleSegmentChange" type="button" size="small">
                    <a-radio value="new_customers">Khách chưa mua hàng</a-radio>
                    <a-radio value="first_order">Khách đã mua 1 đơn</a-radio>
                    <a-radio value="repeat_customers">Khách mua lặp lại</a-radio>
                    <a-radio value="high_frequency">Khách mua thường xuyên (30 ngày)</a-radio>
                    <a-radio value="inactive_30">Không mua 30 ngày</a-radio>
                    <a-radio value="inactive_60">Không mua 60 ngày</a-radio>
                    <a-radio value="inactive_90">Không mua 90 ngày</a-radio>
                  </a-radio-group>
                </div>
              </a-tab-pane>
              <a-tab-pane key="rfm" title="RFM">
                <div class="segment-options">
                  <a-radio-group v-model="rfmSegment" @change="handleSegmentChange" type="button" size="small">
                    <a-radio value="loyal_high_value">Khách trung thành giá trị cao</a-radio>
                    <a-radio value="potential_loyal">Tiềm năng trung thành</a-radio>
                    <a-radio value="big_spenders">Chi tiêu nhiều</a-radio>
                    <a-radio value="at_risk">Có nguy cơ rời bỏ</a-radio>
                    <a-radio value="lost">Đã lâu không quay lại</a-radio>
                  </a-radio-group>
                </div>
              </a-tab-pane>
              <a-tab-pane key="event" title="Sự kiện">
                <div class="segment-options">
                  <a-select v-model="eventSegment" @change="handleSegmentChange" placeholder="Chọn sự kiện" style="width: 100%">
                    <a-option value="birthday_month">Sinh nhật tháng này</a-option>
                    <a-option value="birthday_upcoming">Sinh nhật trong 7 ngày tới</a-option>
                  </a-select>
                  <a-input-number
                    v-if="eventSegment === 'birthday_upcoming'"
                    v-model="birthdayDays"
                    :min="1"
                    :max="30"
                    :precision="0"
                    placeholder="Số ngày"
                    style="width: 100%; margin-top: 8px"
                    @change="handleSegmentChange"
                  />
                </div>
              </a-tab-pane>
              <a-tab-pane key="all" title="Tất cả">
                <div class="segment-options">
                  <a-input-search
                    v-model="allCustomersSearch"
                    placeholder="Tìm kiếm theo tên, SĐT, email"
                    allow-clear
                    style="margin-bottom: 12px"
                    @search="handleSegmentChange"
                  />
                  <a-row :gutter="8" style="margin-bottom: 12px">
                    <a-col :span="12">
                      <a-input-number
                        v-model="allCustomersFilters.minOrdersCount"
                        :min="0"
                        :precision="0"
                        placeholder="Số đơn tối thiểu"
                        style="width: 100%"
                      />
                    </a-col>
                    <a-col :span="12">
                      <a-input-number
                        v-model="allCustomersFilters.maxOrdersCount"
                        :min="0"
                        :precision="0"
                        placeholder="Số đơn tối đa"
                        style="width: 100%"
                      />
                    </a-col>
                  </a-row>
                  <a-row :gutter="8" style="margin-bottom: 12px">
                    <a-col :span="12">
                      <a-input-number
                        v-model="allCustomersFilters.minTotalSpent"
                        :min="0"
                        placeholder="Tổng chi tối thiểu"
                        style="width: 100%"
                      />
                    </a-col>
                    <a-col :span="12">
                      <a-input-number
                        v-model="allCustomersFilters.maxTotalSpent"
                        :min="0"
                        placeholder="Tổng chi tối đa"
                        style="width: 100%"
                      />
                    </a-col>
                  </a-row>
                  <a-range-picker
                    v-model="allCustomersFilters.lastOrderRange"
                    value-format="YYYY-MM-DD"
                    format="DD/MM/YYYY"
                    style="width: 100%; margin-bottom: 12px"
                  />
                  <a-button type="primary" @click="handleSegmentChange" style="width: 100%">Áp dụng bộ lọc</a-button>
                  <a-button @click="resetAllCustomersFilters" style="width: 100%; margin-top: 8px">Đặt lại</a-button>
                </div>
              </a-tab-pane>
            </a-tabs>

            <!-- Selection Controls -->
            <div class="selection-controls">
              <a-checkbox :model-value="isAllEditCustomersSelected" @change="toggleAllEditCustomers">Chọn tất cả trong trang</a-checkbox>
              <a-button size="small" @click="selectAllEditCustomers" type="text">Chọn tất cả kết quả</a-button>
              <a-button size="small" @click="deselectAllEditCustomers" type="text">Xóa chọn</a-button>
            </div>

            <!-- Customer Table -->
            <a-table
              row-key="id"
              :columns="customerColumnsWithCheckbox"
              :data="customers"
              :pagination="customerPagination"
              :loading="customersLoading"
              :scroll="{ y: 350 }"
              size="small"
              :bordered="{ cell: true }"
              @page-change="handlePageChange"
              @page-size-change="handlePageSizeChange"
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
              <template #birthday="{ record }">
                {{ formatCustomerBirthday(record.ngaySinh) }}
              </template>
              <template #ordersCount="{ record }">
                {{ record.ordersCount ?? 0 }}
              </template>
              <template #totalSpent="{ record }">
                {{ formatCurrency(record.totalSpent ?? 0) }}
              </template>
              <template #lastOrderAt="{ record }">
                {{ record.lastOrderAt ? formatDate(record.lastOrderAt) : '—' }}
              </template>
            </a-table>

            <!-- Footer -->
            <div class="selection-footer">
              <div class="selection-summary">
                Đã chọn:
                <strong>{{ couponEditForm.selectedCustomerIds.length }}</strong>
                khách hàng
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- Edit Confirmation Modal -->
    <a-modal
      v-model:visible="couponEditConfirmVisible"
      :title="t('discount.coupon.editConfirmTitle')"
      :confirm-loading="submitting"
      :ok-text="t('discount.common.confirm')"
      width="500px"
      @ok="confirmCouponEdit"
      @cancel="couponEditConfirmVisible = false"
    >
      <p>
        {{ t('discount.coupon.editConfirmMessage', { name: couponEditForm.name }) }}
      </p>
      <p class="modal-note">
        {{ t('discount.coupon.changeReason') }}:
        <strong>{{ couponEditForm.lyDoThayDoi }}</strong>
      </p>
      <p class="modal-note">{{ t('discount.coupon.editConfirmNote') }}</p>
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
import { ref, computed, onMounted, onUnmounted, onBeforeUnmount, reactive, watch, type Component } from 'vue'
import router from '@/router'
import { useRoute, onBeforeRouteUpdate } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import axios from 'axios'
import { useI18n } from 'vue-i18n'
import PageActions from '@/components/page-actions/page-actions.vue'
import { fetchCustomers, fetchCustomersBySegment, type CustomerApiModel, updateCoupon } from '@/api/discount-management'
import type { FormInstance, FormRules } from '@arco-design/web-vue/es/form'
import { IconPlus, IconDelete, IconLeft, IconUp, IconUser, IconGift, IconCalendar, IconStar, IconFire } from '@arco-design/web-vue/es/icon'
import dayjs from 'dayjs'
import { couponTemplateOptions, buildTemplatePayload, type CouponTemplateOption, type CouponTemplateIcon } from './coupon-template-options'

// Router
const { t } = useI18n()
const route = useRoute()

// Get coupon ID from route params
const couponId = computed(() => {
  const id = route.params.id
  const numId = Number(id)
  if (Number.isNaN(numId) || numId <= 0) {
    console.error('Invalid coupon ID:', id)
    return 0
  }
  return numId
})

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
  discountValue: null as number | null,
  maxDiscount: null as number | null,
  minOrder: null as number | null,
  quantity: null as number | null,
  dateRange: [] as string[],
  description: '',
  active: true,
  featured: false,
  selectedCustomerIds: [] as number[],
  applyToProducts: false,
  selectedProductIds: [] as number[],
  lyDoThayDoi: '',
})

const templateIconMap: Record<CouponTemplateIcon, Component> = {
  gift: IconGift,
  user: IconUser,
  lightning: IconFire,
  calendar: IconCalendar,
  crown: IconStar,
}

const selectedTemplateId = ref<string | null>(null)

const applyTemplate = (template: CouponTemplateOption) => {
  const payload = buildTemplatePayload(template)
  selectedTemplateId.value = template.id
  couponEditForm.discountMode = payload.discountMode
  couponEditForm.discountValue = payload.discountValue
  couponEditForm.maxDiscount = payload.maxDiscount
  couponEditForm.minOrder = payload.minOrder
  couponEditForm.quantity = payload.quantity
  couponEditForm.description = payload.description
  couponEditForm.featured = payload.featured
  couponEditForm.dateRange = payload.dateRange
  if (!payload.featured) {
    couponEditForm.selectedCustomerIds = []
  }
}

const clearTemplateSelection = () => {
  selectedTemplateId.value = null
}

const disablePastDates = (current: Date | string) => {
  if (!current) return false
  return dayjs(current).isBefore(dayjs().startOf('day'))
}

// Store original values for change detection
const originalCouponEditForm = reactive({
  code: '',
  name: '',
  discountMode: 'percentage' as 'percentage' | 'amount',
  discountValue: null as number | null,
  maxDiscount: null as number | null,
  minOrder: null as number | null,
  quantity: null as number | null,
  dateRange: [] as string[],
  description: '',
  active: true,
  featured: false,
  selectedCustomerIds: [] as number[],
  applyToProducts: false,
  selectedProductIds: [] as number[],
})

const isPercentEdit = computed(() => couponEditForm.discountMode === 'percentage')

const couponEditRules = computed<FormRules>(() => ({
  code: [{ required: true, message: t('discount.validation.codeRequired') }],
  name: [{ required: true, message: t('discount.validation.nameRequired') }],
  discountMode: [{ required: true, message: t('discount.validation.discountTypeRequired') }],
  discountValue: [
    { required: true, message: t('discount.validation.discountValueRequired') },
    {
      validator: (_: any, callback: (msg?: string) => void) => {
        if (couponEditForm.discountValue === null || couponEditForm.discountValue === undefined) {
          callback(t('discount.validation.discountValueRequired'))
          return
        }
        const v = Number(couponEditForm.discountValue)
        if (Number.isNaN(v)) {
          callback(t('discount.validation.discountValueInvalid'))
          return
        }
        if (isPercentEdit.value) {
          if (v < 1 || v > 100) {
            callback(t('discount.validation.discountValuePercentRange'))
            return
          }
        } else if (v <= 0) {
          callback(t('discount.validation.discountValuePositive'))
          return
        }
        callback()
      },
    },
  ],
  maxDiscount: [
    {
      validator: (_: any, callback: (msg?: string) => void) => {
        if (!isPercentEdit.value) {
          callback()
          return
        }
        const raw = couponEditForm.maxDiscount
        if (raw === null || raw === undefined) {
          callback(t('discount.validation.maxDiscountRequired'))
          return
        }
        const v = Number(raw)
        if (Number.isNaN(v)) {
          callback(t('discount.validation.maxDiscountInvalid'))
          return
        }
        if (v < 1000) {
          callback(t('discount.validation.maxDiscountMin'))
          return
        }
        if (v > 50000000) {
          callback(t('discount.validation.maxDiscountMax'))
          return
        }
        const minOrderValue = Number(couponEditForm.minOrder || 0)
        if (minOrderValue > 0 && v >= minOrderValue) {
          callback(t('discount.validation.maxDiscountLessThanMinOrder'))
          return
        }
        callback()
      },
    },
  ],
  quantity: [
    { required: true, message: t('discount.validation.quantityRequired') },
    {
      validator: (_: any, callback: (msg?: string) => void) => {
        const v = Number(couponEditForm.quantity)
        if (!Number.isInteger(v) || v <= 0) {
          callback(t('discount.validation.quantityPositive'))
          return
        }
        callback()
      },
    },
  ],
  dateRange: [
    { required: true, type: 'array', message: t('discount.validation.dateRangeRequired') },
    {
      validator: (value: string[], callback: (msg?: string) => void) => {
        if (!Array.isArray(value) || value.length !== 2) {
          callback(t('discount.validation.dateRangeRequired'))
          return
        }
        const [start, end] = value
        if (!start || !end) {
          callback(t('discount.validation.dateRangeRequired'))
          return
        }
        if (dayjs(start).isBefore(dayjs().startOf('day'))) {
          callback(t('discount.validation.startDateFuture'))
          return
        }
        if (dayjs(start).isAfter(dayjs(end))) {
          callback(t('discount.validation.endDateAfterStart'))
          return
        }
        callback()
      },
    },
  ],
  lyDoThayDoi: [{ required: true, message: t('discount.validation.changeReasonRequired') }],
}))

// Customers - New segmentation system
const customers = ref<CustomerApiModel[]>([])
const customersLoading = ref(false)
const activeSegmentTab = ref('behavior')
const behaviorSegment = ref('new_customers')
const rfmSegment = ref('loyal_high_value')
const eventSegment = ref<string | null>(null)
const birthdayDays = ref(7)
const allCustomersSearch = ref('')
const allCustomersFilters = reactive({
  minOrdersCount: undefined as number | undefined,
  maxOrdersCount: undefined as number | undefined,
  minTotalSpent: undefined as number | undefined,
  maxTotalSpent: undefined as number | undefined,
  lastOrderRange: [] as string[],
})

const customerPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
  showPageSize: true,
})

const formatCustomerBirthday = (value?: string | null) => {
  if (!value) return '—'
  const date = dayjs(value)
  if (!date.isValid()) return '—'
  return date.format('DD/MM')
}

const formatDate = (value: string) => {
  if (!value) return '—'
  const date = dayjs(value)
  if (!date.isValid()) return '—'
  return date.format('DD/MM/YYYY')
}

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const customerColumns = computed(() => [
  {
    title: t('discount.customer.name'),
    dataIndex: 'tenKhachHang',
    ellipsis: true,
    tooltip: true,
  },
  {
    title: t('discount.customer.email'),
    dataIndex: 'email',
    ellipsis: true,
    tooltip: true,
  },
  {
    title: t('discount.customer.phone'),
    dataIndex: 'soDienThoai',
    width: 130,
  },
  {
    title: 'Ngày sinh',
    dataIndex: 'ngaySinh',
    slotName: 'birthday',
    width: 110,
    align: 'center' as const,
  },
  {
    title: 'Số đơn',
    dataIndex: 'ordersCount',
    slotName: 'ordersCount',
    width: 80,
    align: 'center' as const,
  },
  {
    title: 'Tổng chi',
    dataIndex: 'totalSpent',
    slotName: 'totalSpent',
    width: 120,
    align: 'right' as const,
  },
  {
    title: 'Đơn cuối',
    dataIndex: 'lastOrderAt',
    slotName: 'lastOrderAt',
    width: 110,
    align: 'center' as const,
  },
])

const customerColumnsWithCheckbox = computed(() => [
  {
    title: '',
    dataIndex: 'select',
    slotName: 'select',
    width: 50,
    align: 'center' as const,
  },
  ...customerColumns.value,
])

const isAllEditCustomersSelected = computed(() => {
  if (customers.value.length === 0) return false
  return customers.value.every((customer) => couponEditForm.selectedCustomerIds.includes(customer.id))
})

const isSomeEditCustomersSelected = computed(() => {
  return couponEditForm.selectedCustomerIds.length > 0 && !isAllEditCustomersSelected.value
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
  const currentPageIds = customers.value.map((c) => c.id)
  const allSelected = currentPageIds.every((id) => couponEditForm.selectedCustomerIds.includes(id))
  if (allSelected) {
    currentPageIds.forEach((id) => {
      const index = couponEditForm.selectedCustomerIds.indexOf(id)
      if (index > -1) {
        couponEditForm.selectedCustomerIds.splice(index, 1)
      }
    })
  } else {
    currentPageIds.forEach((id) => {
      if (!couponEditForm.selectedCustomerIds.includes(id)) {
        couponEditForm.selectedCustomerIds.push(id)
      }
    })
  }
}

const selectAllEditCustomers = () => {
  customers.value.forEach((customer) => {
    if (!couponEditForm.selectedCustomerIds.includes(customer.id)) {
      couponEditForm.selectedCustomerIds.push(customer.id)
    }
  })
  Message.info('Đã chọn tất cả khách hàng trong trang hiện tại')
}

const deselectAllEditCustomers = () => {
  couponEditForm.selectedCustomerIds = []
}

const resetAllCustomersFilters = () => {
  allCustomersSearch.value = ''
  allCustomersFilters.minOrdersCount = undefined
  allCustomersFilters.maxOrdersCount = undefined
  allCustomersFilters.minTotalSpent = undefined
  allCustomersFilters.maxTotalSpent = undefined
  allCustomersFilters.lastOrderRange = []
  handleSegmentChange()
}

const handleSegmentChange = async () => {
  customerPagination.current = 1
  await loadCustomersBySegment()
}

const handlePageChange = (page: number) => {
  customerPagination.current = page
  loadCustomersBySegment()
}

const handlePageSizeChange = (pageSize: number) => {
  customerPagination.pageSize = pageSize
  customerPagination.current = 1
  loadCustomersBySegment()
}

const loadCustomersBySegment = async () => {
  customersLoading.value = true
  try {
    const params: any = {
      page: customerPagination.current,
      pageSize: customerPagination.pageSize,
    }

    if (activeSegmentTab.value === 'behavior') {
      params.segmentType = 'behavior'
      params.segmentKey = behaviorSegment.value
    } else if (activeSegmentTab.value === 'rfm') {
      params.segmentType = 'rfm'
      params.segmentKey = rfmSegment.value
    } else if (activeSegmentTab.value === 'event') {
      params.segmentType = 'event'
      params.segmentKey = eventSegment.value
      if (eventSegment.value === 'birthday_upcoming') {
        params.birthdayDays = birthdayDays.value
      }
    } else if (activeSegmentTab.value === 'all') {
      params.segmentType = 'all'
      if (allCustomersSearch.value) {
        params.search = allCustomersSearch.value
      }
      if (allCustomersFilters.minOrdersCount !== undefined) {
        params.minOrdersCount = allCustomersFilters.minOrdersCount
      }
      if (allCustomersFilters.maxOrdersCount !== undefined) {
        params.maxOrdersCount = allCustomersFilters.maxOrdersCount
      }
      if (allCustomersFilters.minTotalSpent !== undefined) {
        params.minTotalSpent = allCustomersFilters.minTotalSpent
      }
      if (allCustomersFilters.maxTotalSpent !== undefined) {
        params.maxTotalSpent = allCustomersFilters.maxTotalSpent
      }
      if (allCustomersFilters.lastOrderRange && allCustomersFilters.lastOrderRange.length === 2) {
        params.lastOrderFrom = allCustomersFilters.lastOrderRange[0]
        params.lastOrderTo = allCustomersFilters.lastOrderRange[1]
      }
    }

    const paged = await fetchCustomersBySegment(params)
    const content = Array.isArray(paged?.content) ? paged.content : []
    customers.value = content
    customerPagination.total = paged?.totalElements ?? content.length
    customerPagination.current = paged?.page ?? customerPagination.current
    customerPagination.pageSize = paged?.size ?? customerPagination.pageSize
  } catch (error) {
    console.error('Failed to load customer segments', error)
    Message.error(t('discount.message.loadCustomersFailed'))
    customers.value = []
    customerPagination.total = 0
  } finally {
    customersLoading.value = false
  }
}

const loadCustomers = async () => {
  await loadCustomersBySegment()
}

watch(
  () => couponEditForm.featured,
  (isFeatured) => {
    if (isFeatured) {
      loadCustomersBySegment()
    }
    if (!isFeatured) {
      couponEditForm.selectedCustomerIds = []
    }
  }
)

// Reload customers when tab changes
watch(
  () => activeSegmentTab.value,
  () => {
    if (couponEditForm.featured) {
      handleSegmentChange()
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

const productColumns = computed(() => [
  {
    title: t('discount.product.image'),
    dataIndex: 'anhSanPham',
    slotName: 'anhSanPham',
    width: 80,
    align: 'center' as const,
  },
  {
    title: t('discount.product.name'),
    dataIndex: 'tenSanPham',
    slotName: 'tenSanPham',
    ellipsis: true,
    tooltip: true,
  },
  {
    title: t('discount.product.code'),
    dataIndex: 'maChiTietSanPham',
    width: 120,
  },
  {
    title: t('discount.product.price'),
    dataIndex: 'giaBan',
    slotName: 'giaBan',
    width: 150,
    align: 'right' as const,
  },
])

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

// Unused variables removed to fix ESLint warnings
// These were defined but never used in the template

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
        Message.info(t('discount.message.noActiveProducts'))
      }
    } else {
      products.value = []
      Message.warning(t('discount.message.invalidProductData'))
    }
  } catch {
    Message.error(t('discount.message.loadProductsFailed'))
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
    const currentValue = couponEditForm.discountValue ?? 0
    if (mode === 'percentage' && currentValue > 100) {
      couponEditForm.discountValue = 100
    }
  }
)

watch(
  () => couponEditForm.discountValue,
  (value) => {
    if (value === null || value === undefined) return

    if (isPercentEdit.value) {
      if (value > 100) {
        couponEditForm.discountValue = 100
        Message.warning(t('discount.message.discountPercentMax'))
      } else if (value < 1) {
        couponEditForm.discountValue = 1
      }
    } else if (value <= 0) {
      couponEditForm.discountValue = 1
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

// parseFormattedNumber removed - not used

// Display value for discount input (with % or VND symbol)
const displayDiscountValue = ref('')
const isEditingDiscount = ref(false)

// Handle focus - remove % or VND for easy editing
const handleDiscountFocus = () => {
  isEditingDiscount.value = true
  displayDiscountValue.value = couponEditForm.discountValue !== null ? String(couponEditForm.discountValue) : ''
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

// Display value for maxDiscount field
const displayMaxDiscount = ref('')
const isEditingMaxDiscount = ref(false)

const handleMaxDiscountFocus = () => {
  isEditingMaxDiscount.value = true
  displayMaxDiscount.value = String(couponEditForm.maxDiscount || 0)
}

const handleMaxDiscountInput = () => {
  if (!isEditingMaxDiscount.value) return
  const cleanValue = displayMaxDiscount.value.replace(/\./g, '')
  const digits = cleanValue.replace(/[^0-9]/g, '')
  if (digits === '' || digits === '0') {
    displayMaxDiscount.value = digits
    return
  }
  const formatted = formatNumberWithSeparator(parseInt(digits, 10))
  displayMaxDiscount.value = formatted
}

const handleMaxDiscountBlur = () => {
  isEditingMaxDiscount.value = false
  const cleanValue = displayMaxDiscount.value.replace(/\./g, '').replace(/[^0-9]/g, '')
  const value = parseInt(cleanValue, 10)
  if (Number.isNaN(value) || value < 1000) {
    couponEditForm.maxDiscount = 1000
    Message.warning(t('discount.validation.maxDiscountMin'))
  } else if (value > 50000000) {
    couponEditForm.maxDiscount = 50000000
    Message.warning(t('discount.validation.maxDiscountMax'))
  } else {
    couponEditForm.maxDiscount = Math.round(value)
  }
  const minOrderValue = Number(couponEditForm.minOrder || 0)
  if (minOrderValue > 0 && couponEditForm.maxDiscount >= minOrderValue) {
    couponEditForm.maxDiscount = Math.max(1000, minOrderValue - 1)
    Message.warning(t('discount.validation.maxDiscountLessThanMinOrder'))
  }
  displayMaxDiscount.value = `${formatNumberWithSeparator(couponEditForm.maxDiscount)} VND`
}

watch(
  () => couponEditForm.maxDiscount,
  (newValue) => {
    if (!isEditingMaxDiscount.value) {
      if (newValue === null || newValue === undefined) {
        displayMaxDiscount.value = ''
      } else {
        displayMaxDiscount.value = `${formatNumberWithSeparator(newValue)} VND`
      }
    }
  },
  { immediate: true }
)

// Handle blur - format with .00% or VND
const handleDiscountBlur = () => {
  isEditingDiscount.value = false

  if (isPercentEdit.value) {
    // Percentage mode - parse as float to handle decimals
    const value = parseFloat(displayDiscountValue.value.replace(/[^0-9.]/g, ''))

    if (Number.isNaN(value) || value < 1) {
      couponEditForm.discountValue = 1
    } else if (value > 100) {
      couponEditForm.discountValue = 100
      Message.warning(t('discount.message.discountPercentMax'))
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
      Message.warning(t('discount.message.discountAmountMax'))
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
      if (newValue === null || newValue === undefined) {
        displayDiscountValue.value = ''
      } else if (isPercentEdit.value) {
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
    if (couponEditForm.discountValue === null || couponEditForm.discountValue === undefined) {
      displayDiscountValue.value = ''
      return
    }
    if (isPercentEdit.value) {
      displayDiscountValue.value = `${couponEditForm.discountValue.toFixed(2)}%`
    } else {
      displayDiscountValue.value = `${formatNumberWithSeparator(couponEditForm.discountValue)} VND`
      // reset max discount when switching to amount
      couponEditForm.maxDiscount = null
      displayMaxDiscount.value = ''
    }
  }
)

// Display value for minOrder field
const displayMinOrder = ref('')
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
    Message.warning(t('discount.message.minOrderMax'))
  } else {
    couponEditForm.minOrder = Math.round(value)
  }

  if (couponEditForm.minOrder === null || couponEditForm.minOrder === undefined) {
    displayMinOrder.value = ''
  } else {
    displayMinOrder.value = `${formatNumberWithSeparator(couponEditForm.minOrder)} VND`
  }
}

watch(
  () => couponEditForm.minOrder,
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

// formatCurrency and handleImageError removed - not used

const goBack = () => {
  router.push({ name: 'QuanLyPhieuGiamGia' })
}

// Load coupon data
const loadCouponData = async () => {
  if (!couponId.value || couponId.value <= 0) {
    Message.error('ID phiếu giảm giá không hợp lệ')
    goBack()
    return
  }
  
  loading.value = true
  try {
    const response = await axios.get(`/api/phieu-giam-gia-management/detail/${couponId.value}`)
    const coupon = response.data?.data || response.data
    
    if (!coupon || !coupon.id) {
      Message.error('Không tìm thấy phiếu giảm giá')
      goBack()
      return
    }

    couponEditForm.code = coupon.maPhieuGiamGia ?? ''
    couponEditForm.name = coupon.tenPhieuGiamGia ?? ''

    const discountType = coupon.loaiPhieuGiamGia ? 'fixed' : 'percentage'
    couponEditForm.discountMode = discountType === 'percentage' ? 'percentage' : 'amount'
    couponEditForm.discountValue = Number(coupon.giaTriGiamGia ?? 0)
    // Set maxDiscount based on discount type
    if (discountType === 'percentage') {
      couponEditForm.maxDiscount = coupon.soTienToiDa != null ? Number(coupon.soTienToiDa) : null
    } else {
      couponEditForm.maxDiscount = null
    }
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
    selectedTemplateId.value = null

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
    Message.error(t('discount.message.loadCouponFailed'))
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
    Message.error(t('discount.validation.changeReasonRequired'))
    return
  }

  const discountValue = Number(couponEditForm.discountValue)
  if (isPercentEdit.value) {
    if (Number.isNaN(discountValue) || discountValue < 1) {
      Message.error(t('discount.validation.discountValuePercentRange'))
      return
    }
    if (discountValue > 100) {
      Message.error(t('discount.message.discountPercentMax'))
      return
    }
    const capValue = Number(couponEditForm.maxDiscount)
    if (Number.isNaN(capValue)) {
      Message.error(t('discount.validation.maxDiscountInvalid'))
      return
    }
    if (capValue < 1000) {
      Message.error(t('discount.validation.maxDiscountMin'))
      return
    }
    if (capValue > 50000000) {
      Message.error(t('discount.validation.maxDiscountMax'))
      return
    }
    const minOrderValue = Number(couponEditForm.minOrder || 0)
    if (minOrderValue > 0 && capValue >= minOrderValue) {
      Message.error(t('discount.validation.maxDiscountLessThanMinOrder'))
      return
    }
  } else if (Number.isNaN(discountValue) || discountValue <= 0) {
    Message.error(t('discount.validation.discountValuePositive'))
    return
  }

  const [startDate, endDate] = couponEditForm.dateRange
  if (!startDate || !endDate) {
    Message.error(t('discount.validation.dateRangeRequired'))
    return
  }
  if (new Date(startDate) > new Date(endDate)) {
    Message.error(t('discount.validation.endDateAfterStart'))
    return
  }

  if (!isPercentEdit.value) {
    const minOrderValue = Number(couponEditForm.minOrder || 0)
    if (minOrderValue > 0 && discountValue >= minOrderValue) {
      Message.error(t('discount.validation.discountLessThanMinOrder'))
      return
    }
  }

  const quantityValue = Number(couponEditForm.quantity)
  if (!Number.isInteger(quantityValue) || quantityValue <= 0) {
    Message.error(t('discount.validation.quantityPositive'))
    return
  }

  if (couponEditForm.featured && couponEditForm.selectedCustomerIds.length === 0) {
    Message.error(t('discount.validation.selectAtLeastOneCustomer'))
    return
  }

  if (couponEditForm.applyToProducts && couponEditForm.selectedProductIds.length === 0) {
    Message.error(t('discount.validation.selectAtLeastOneProduct'))
    return
  }

  // Check if any changes were made
  const hasChanges =
    couponEditForm.code !== originalCouponEditForm.code ||
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
    Message.warning(t('discount.message.noChanges'))
    return
  }

  couponEditConfirmVisible.value = true
}

const confirmCouponEdit = async () => {
  // Validate lý do thay đổi before submission
  if (!couponEditForm.lyDoThayDoi || !couponEditForm.lyDoThayDoi.trim()) {
    Message.error(t('discount.validation.changeReasonRequired'))
    couponEditConfirmVisible.value = false
    return
  }

  const discountValue = Number(couponEditForm.discountValue)
  const quantityValue = Number(couponEditForm.quantity)
  const [startDate, endDate] = couponEditForm.dateRange

  const toBackendDateTime = (value: string): string => {
    if (!value) return value
    return value.replace(' ', 'T')
  }

  const payload = {
    maPhieuGiamGia: couponEditForm.code.trim(),
    tenPhieuGiamGia: couponEditForm.name.trim(),
    loaiPhieuGiamGia: couponEditForm.discountMode === 'amount',
    giaTriGiamGia: discountValue,
    soTienToiDa: isPercentEdit.value ? Number(couponEditForm.maxDiscount ?? 0) : null,
    hoaDonToiThieu: couponEditForm.minOrder ? Number(couponEditForm.minOrder) : 0,
    soLuongDung: quantityValue,
    ngayBatDau: toBackendDateTime(startDate),
    ngayKetThuc: toBackendDateTime(endDate),
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

    Message.success(t('discount.message.updateCouponSuccess'))
    couponEditConfirmVisible.value = false
    goBack()
  } catch (error: any) {
    const errorMessage = error.response?.data?.message || error.message || t('discount.message.updateCouponFailed')
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

onMounted(async () => {
  try {
    await loadCouponData()
  } catch (error) {
    console.error('Error loading coupon data:', error)
    Message.error('Không thể tải dữ liệu phiếu giảm giá')
  }
  window.addEventListener('scroll', handleScroll)
})

// Handle route parameter changes (when navigating between different coupon IDs)
onBeforeRouteUpdate(async (to, from, next) => {
  if (to.params.id !== from.params.id) {
    loading.value = true
    try {
      await loadCouponData()
    } catch (error) {
      console.error('Error loading coupon data on route update:', error)
      Message.error('Không thể tải dữ liệu phiếu giảm giá')
    }
  }
  next()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.coupon-edit-page {
  padding: 16px 20px;
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

.template-selection {
  border: 1px solid var(--color-border-2);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
  background: var(--color-fill-1);
}

.template-selection-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}

.template-selection-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-1);
}

.template-selection-subtitle {
  font-size: 13px;
  color: var(--color-text-3);
  margin-top: 2px;
}

.template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

.template-card {
  border: 1px solid var(--color-border-2);
  border-radius: 12px;
  padding: 16px;
  display: flex;
  gap: 12px;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease;
  background: var(--color-bg-1);
}

.template-card:hover {
  border-color: var(--color-primary-6);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transform: translateY(-2px);
}

.template-card-selected {
  border-color: var(--color-primary-6);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}

.template-card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.template-card-icon :deep(svg) {
  font-size: 24px;
}

.accent-blue .template-card-icon {
  background: rgba(64, 123, 255, 0.12);
  color: #3f76ff;
}

.accent-green .template-card-icon {
  background: rgba(0, 192, 135, 0.12);
  color: #00c087;
}

.accent-orange .template-card-icon {
  background: rgba(255, 150, 0, 0.15);
  color: #ff9600;
}

.accent-purple .template-card-icon {
  background: rgba(144, 72, 255, 0.15);
  color: #9048ff;
}

.template-card-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.template-card-title {
  font-weight: 600;
  color: var(--color-text-1);
}

.template-card-description {
  font-size: 13px;
  color: var(--color-text-3);
  line-height: 1.4;
}

.template-card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.customer-selection-section {
  border: 1px solid var(--color-border-2);
  border-radius: 8px;
  padding: 16px;
  background: var(--color-bg-2);
  min-width: 960px;
}

.right-column-edit {
  min-width: 0;
}

.right-column-edit :deep(.arco-card-body) {
  overflow-x: auto;
}

.segment-tabs {
  margin-bottom: 16px;
}

.segment-tabs :deep(.arco-tabs-content) {
  padding-top: 12px;
}

.segment-options {
  min-height: 60px;
}

.segment-options :deep(.arco-radio-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.selection-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding: 8px;
  background: var(--color-fill-1);
  border-radius: 4px;
}

.selection-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border-2);
}

.selection-summary {
  font-size: 13px;
  color: var(--color-text-2);
  text-align: center;
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

/* Hide scrollbar for left column */
.left-column-edit :deep(.arco-card-body) {
  max-height: 72vh;
  overflow-y: auto;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

.left-column-edit :deep(.arco-card-body)::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
}
</style>
