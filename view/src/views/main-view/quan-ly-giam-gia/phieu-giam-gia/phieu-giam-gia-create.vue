<template>
  <div class="coupon-create-page">
    <a-card class="coupon-card" :class="{ 'featured-layout': formState.featured }">
      <div :class="formState.featured ? 'two-column-container' : ''">
        <!-- Left Column: Form -->
        <div :class="formState.featured ? 'left-column' : ''">
          <div v-if="formState.featured" class="column-title">{{ t('discount.coupon.info') }}</div>

          <div class="template-selection">
            <div class="template-selection-header">
              <div>
                <div class="template-selection-title">Chọn mẫu phiếu</div>
                <div class="template-selection-subtitle">Áp dụng nhanh cấu hình phổ biến cho chiến dịch</div>
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

          <a-form ref="formRef" :model="formState" :rules="rules" layout="vertical">
            <!-- Mã phiếu giảm giá -->
            <a-form-item field="code" :label="t('discount.coupon.code')">
              <a-input v-model="formState.code" :placeholder="t('discount.coupon.codePlaceholder')" allow-clear />
            </a-form-item>

            <!-- Tên phiếu giảm giá -->
            <a-form-item field="name" :label="t('discount.coupon.name')">
              <a-input v-model="formState.name" :placeholder="t('discount.coupon.namePlaceholder')" allow-clear />
            </a-form-item>

            <!-- Loại giảm giá -->
            <a-form-item field="discountMode" :label="t('discount.coupon.discountType')">
              <a-select v-model="formState.discountMode" :placeholder="t('discount.coupon.discountTypePlaceholder')">
                <a-option value="percentage">{{ t('discount.coupon.discountType.percentage') }}</a-option>
                <a-option value="amount">{{ t('discount.coupon.discountType.amount') }}</a-option>
              </a-select>
            </a-form-item>

            <!-- Giá trị giảm -->
            <a-form-item field="discountValue" :label="t('discount.coupon.discountValue')">
              <a-input
                v-model="displayDiscountValue"
                @blur="handleDiscountBlur"
                @focus="handleDiscountFocus"
                @input="handleDiscountInput"
                :placeholder="
                  isPercent
                    ? t('discount.coupon.discountValuePlaceholder.percentage')
                    : t('discount.coupon.discountValuePlaceholder.amount')
                "
                style="width: 100%"
              />
            </a-form-item>

            <!-- Giá trị giảm tối đa (chỉ khi %)-->
            <a-form-item v-if="isPercent" field="maxDiscount" :label="t('discount.coupon.maxDiscount')">
              <a-input
                v-model="displayMaxDiscount"
                @blur="handleMaxDiscountBlur"
                @focus="handleMaxDiscountFocus"
                @input="handleMaxDiscountInput"
                :placeholder="'Tối đa: 50.000.000 VND'"
                style="width: 100%"
              />
            </a-form-item>

            <!-- Thời gian áp dụng -->
            <a-form-item field="dateRange" :label="t('discount.coupon.validityPeriod')">
              <a-range-picker
                v-model="formState.dateRange"
                :show-time="true"
                value-format="YYYY-MM-DDTHH:mm:ss"
                format="DD/MM/YYYY HH:mm"
                allow-clear
                :disabled-date="disablePastDates"
                :placeholder="[t('discount.common.selectStartDate'), t('discount.common.selectEndDate')]"
                style="width: 100%"
              />
            </a-form-item>

            <!-- Giá trị đơn hàng tối thiểu -->
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

            <!-- Số lượng phiếu -->
            <a-form-item field="quantity" :label="t('discount.coupon.quantity')">
              <a-input-number
                v-model="formState.quantity"
                :min="1"
                :max="100000"
                :precision="0"
                :disabled="formState.featured"
                :placeholder="formState.featured ? t('discount.coupon.quantityAuto') : t('discount.coupon.quantityPlaceholder')"
                style="width: 100%"
              />
            </a-form-item>

            <!-- Mô tả -->
            <a-form-item field="description" :label="t('discount.common.description')">
              <a-textarea
                v-model="formState.description"
                :placeholder="t('discount.coupon.descriptionPlaceholder')"
                allow-clear
                :max-length="500"
                :auto-size="{ minRows: 3, maxRows: 6 }"
              />
            </a-form-item>

            <!-- Phiếu giảm giá riêng tư -->
            <a-form-item field="featured">
              <a-checkbox v-model="formState.featured">{{ t('discount.coupon.private') }}</a-checkbox>
              <div style="margin-left: 24px; margin-top: 4px; font-size: 12px; color: var(--color-text-3)">
                {{ t('discount.coupon.privateDescription') }}
              </div>
            </a-form-item>
          </a-form>
        </div>

        <!-- Right Column: Customer Selection -->
        <div v-if="formState.featured" class="right-column">
          <div class="column-title">Chọn khách hàng</div>
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
              <a-checkbox :model-value="isAllCustomersSelected" @change="toggleAllCustomers">Chọn tất cả trong trang</a-checkbox>
              <a-button size="small" @click="selectAllResults" type="text">Chọn tất cả kết quả</a-button>
              <a-button size="small" @click="formState.selectedCustomerIds = []" type="text">Xóa chọn</a-button>
            </div>

            <!-- Customer Table -->
            <a-table
              row-key="id"
              :columns="customerColumns"
              :data="customers"
              :pagination="customerPagination"
              :loading="customersLoading"
              :scroll="{ y: 400 }"
              :bordered="{ wrapper: true, cell: true }"
              size="small"
              @page-change="handlePageChange"
              @page-size-change="handlePageSizeChange"
            >
              <template #checkbox="{ record }">
                <a-checkbox :model-value="isCustomerSelected(record.id)" @change="toggleCustomerSelection(record.id)" />
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
              <template #empty>
                <div style="text-align: center; padding: 20px">{{ t('discount.common.noData') }}</div>
              </template>
            </a-table>

            <!-- Footer -->
            <div class="selection-footer">
              <div class="selection-summary">
                Đã chọn:
                <strong>{{ formState.selectedCustomerIds.length }}</strong>
                khách hàng
              </div>
            </div>
          </div>
        </div>
      </div>
    </a-card>

    <PageActions>
      <a-space>
        <a-button @click="goBack">{{ t('discount.common.reset') }}</a-button>
        <a-button type="primary" :loading="confirmSaveSubmitting" @click="handleSaveClick">{{ t('discount.common.create') }}</a-button>
      </a-space>
    </PageActions>

    <!-- Confirmation Modal -->
    <a-modal
      v-model:visible="confirmSaveVisible"
      :title="t('discount.coupon.confirmCreate')"
      :confirm-loading="confirmSaveSubmitting"
      @ok="confirmSave"
      @cancel="confirmSaveVisible = false"
      :ok-text="t('discount.common.confirm')"
      :cancel-text="t('discount.common.cancel')"
      width="560px"
    >
      <p style="margin-bottom: 16px; color: var(--color-text-2)">{{ t('discount.coupon.confirmSave') }}</p>
      <a-descriptions :column="1" bordered>
        <a-descriptions-item :label="t('discount.coupon.code')">
          {{ formState.code }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('discount.coupon.name')">
          {{ formState.name }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('discount.coupon.discountType')">
          {{ isPercent ? t('discount.coupon.discountType.percentage') : t('discount.coupon.discountType.amount') }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('discount.coupon.discountValue')">
          <span v-if="isPercent">{{ formState.discountValue }}%</span>
          <span v-else>{{ formState.discountValue ? formatCurrency(formState.discountValue) : t('discount.common.unlimited') }}</span>
        </a-descriptions-item>
        <a-descriptions-item v-if="isPercent" :label="t('discount.coupon.maxDiscount')">
          <span>{{ formState.maxDiscount ? formatCurrency(formState.maxDiscount) : t('discount.common.unlimited') }}</span>
        </a-descriptions-item>
        <a-descriptions-item :label="t('discount.coupon.minOrder')">
          {{ formState.minOrder ? formatCurrency(formState.minOrder) : t('discount.common.unlimited') }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('discount.coupon.validityPeriod')">
          {{ formatDateRange(formState.dateRange[0], formState.dateRange[1]) }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('discount.coupon.quantity')">
          {{ formState.quantity }}
        </a-descriptions-item>
        <a-descriptions-item v-if="formState.description" :label="t('discount.common.description')">
          {{ formState.description }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('discount.coupon.private')">
          {{ formState.featured ? t('discount.coupon.featured.yes') : t('discount.coupon.featured.no') }}
        </a-descriptions-item>
        <a-descriptions-item v-if="formState.featured" :label="t('discount.coupon.appliedCustomers')">
          <div style="max-height: 150px; overflow-y: auto">
            <a-tag v-for="customerId in formState.selectedCustomerIds" :key="customerId" style="margin: 2px">
              {{ getCustomerNameById(customerId) }}
            </a-tag>
          </div>
          <div style="margin-top: 4px; font-size: 12px; color: var(--color-text-3)">
            {{ t('discount.common.total') }}: {{ formState.selectedCustomerIds.length }} {{ t('discount.common.customers') }}
          </div>
        </a-descriptions-item>
      </a-descriptions>
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
import { computed, reactive, ref, watch, onMounted, onUnmounted, type Component } from 'vue'
import type { FormInstance, FormRules } from '@arco-design/web-vue/es/form'
import { Message } from '@arco-design/web-vue'
import { IconUp, IconUser, IconGift, IconCalendar, IconStar, IconFire } from '@arco-design/web-vue/es/icon'
import PageActions from '@/components/page-actions/page-actions.vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { createCoupon, fetchCoupons, fetchCustomers, fetchCustomersBySegment, type CustomerApiModel } from '@/api/discount-management'
import dayjs from 'dayjs'
import { couponTemplateOptions, buildTemplatePayload, type CouponTemplateOption, type CouponTemplateIcon } from './coupon-template-options'

const { t } = useI18n()
const router = useRouter()

const formRef = ref<FormInstance>()
const confirmSaveVisible = ref(false)
const confirmSaveSubmitting = ref(false)

const formState = reactive({
  code: '',
  name: '',
  discountMode: 'percentage' as 'percentage' | 'amount',
  discountValue: null as number | null,
  maxDiscount: null as number | null,
  minOrder: null as number | null,
  dateRange: [] as string[],
  quantity: null as number | null,
  description: '',
  featured: false,
  selectedCustomerIds: [] as number[],
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
  formState.discountMode = payload.discountMode
  formState.discountValue = payload.discountValue
  formState.maxDiscount = payload.maxDiscount
  formState.minOrder = payload.minOrder
  formState.quantity = payload.quantity
  formState.description = payload.description
  formState.featured = payload.featured
  formState.dateRange = payload.dateRange
  if (!payload.featured) {
    formState.selectedCustomerIds = []
  }
}

const clearTemplateSelection = () => {
  selectedTemplateId.value = null
}

const disablePastDates = (current: Date | string) => {
  if (!current) return false
  return dayjs(current).isBefore(dayjs().startOf('day'))
}

const rules: FormRules = {
  code: [{ required: true, message: t('discount.coupon.validation.codeRequired') }],
  name: [{ required: true, message: t('discount.coupon.validation.nameRequired') }],
  discountMode: [{ required: true, message: t('discount.coupon.validation.discountTypeRequired') }],
  discountValue: [
    { required: true, message: t('discount.coupon.validation.discountValueRequired') },
    {
      validator: (_: any, callback: (msg?: string) => void) => {
        if (formState.discountValue === null || formState.discountValue === undefined) {
          callback(t('discount.coupon.validation.discountValueRequired'))
          return
        }
        const v = Number(formState.discountValue)
        if (Number.isNaN(v)) {
          callback(t('discount.coupon.validation.discountValueInvalid'))
          return
        }
        if (isPercent.value) {
          if (v < 1 || v > 100) {
            callback(t('discount.coupon.validation.discountPercentRange'))
            return
          }
        } else if (v <= 0) {
          callback(t('discount.coupon.validation.discountAmountPositive'))
          return
        }
        callback()
      },
    },
  ],
  maxDiscount: [
    {
      validator: (_: any, callback: (msg?: string) => void) => {
        if (!isPercent.value) return callback()
        const raw = formState.maxDiscount
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
        const minOrderValue = Number(formState.minOrder || 0)
        if (minOrderValue > 0 && v >= minOrderValue) {
          callback(t('discount.validation.maxDiscountLessThanMinOrder'))
          return
        }
        callback()
      },
    },
  ],
  quantity: [
    { required: true, message: t('discount.coupon.validation.quantityRequired') },
    {
      validator: (_: any, callback: (msg?: string) => void) => {
        const v = Number(formState.quantity)
        if (!Number.isInteger(v) || v <= 0) {
          callback(t('discount.coupon.validation.quantityPositive'))
          return
        }
        callback()
      },
    },
  ],
  dateRange: [
    { required: true, message: 'Vui lòng chọn khoảng thời gian áp dụng' },
    {
      validator: (value: string[], callback: (msg?: string) => void) => {
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

// Customer selection - New segmentation system
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
  const currentPageIds = customers.value.map((c) => c.id)
  const allSelected = currentPageIds.every((id) => formState.selectedCustomerIds.includes(id))
  if (allSelected) {
    currentPageIds.forEach((id) => {
      const index = formState.selectedCustomerIds.indexOf(id)
      if (index > -1) {
        formState.selectedCustomerIds.splice(index, 1)
      }
    })
  } else {
    currentPageIds.forEach((id) => {
      if (!formState.selectedCustomerIds.includes(id)) {
        formState.selectedCustomerIds.push(id)
      }
    })
  }
}

const selectAllResults = async () => {
  // This would require fetching all customer IDs for the current segment
  // For now, we'll select all visible customers
  customers.value.forEach((customer) => {
    if (!formState.selectedCustomerIds.includes(customer.id)) {
      formState.selectedCustomerIds.push(customer.id)
    }
  })
  Message.info('Đã chọn tất cả khách hàng trong trang hiện tại')
}

const isAllCustomersSelected = computed(() => {
  if (customers.value.length === 0) return false
  return customers.value.every((c) => formState.selectedCustomerIds.includes(c.id))
})

const customerColumns = [
  {
    title: '',
    slotName: 'checkbox',
    width: 50,
    align: 'center',
  },
  {
    title: 'Số điện thoại',
    dataIndex: 'soDienThoai',
    width: 130,
  },
  {
    title: 'Họ tên',
    dataIndex: 'tenKhachHang',
    ellipsis: true,
    tooltip: true,
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
]

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
    Message.error('Không thể tải danh sách khách hàng')
    customers.value = []
    customerPagination.total = 0
  } finally {
    customersLoading.value = false
  }
}

const loadCustomers = async () => {
  await loadCustomersBySegment()
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
    if (isFeatured) {
      loadCustomersBySegment()
    }
    // Clear selected customers when unchecking featured
    if (!isFeatured) {
      formState.selectedCustomerIds = []
      formState.quantity = 1 // Reset to default
    }
  }
)

// Reload customers when tab changes
watch(
  () => activeSegmentTab.value,
  () => {
    if (formState.featured) {
      handleSegmentChange()
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
    const currentValue = formState.discountValue ?? 0
    if (mode === 'percentage' && currentValue > 100) {
      formState.discountValue = 100
    }
    if (mode === 'amount') {
      formState.maxDiscount = null
      displayMaxDiscount.value = ''
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

    if (Number.isNaN(value) || value < 1) {
      formState.discountValue = 1
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

// Display value for maxDiscount field
const displayMaxDiscount = ref('')
const isEditingMaxDiscount = ref(false)

const handleMaxDiscountFocus = () => {
  isEditingMaxDiscount.value = true
  displayMaxDiscount.value = String(formState.maxDiscount || 0)
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
    formState.maxDiscount = 1000
    Message.warning(t('discount.validation.maxDiscountMin'))
  } else if (value > 50000000) {
    formState.maxDiscount = 50000000
    Message.warning(t('discount.validation.maxDiscountMax'))
  } else {
    formState.maxDiscount = Math.round(value)
  }
  // Validate relation with minOrder
  const minOrderValue = Number(formState.minOrder || 0)
  if (minOrderValue > 0 && formState.maxDiscount >= minOrderValue) {
    formState.maxDiscount = Math.max(1000, minOrderValue - 1)
    Message.warning(t('discount.validation.maxDiscountLessThanMinOrder'))
  }
  displayMaxDiscount.value = `${formatNumberWithSeparator(formState.maxDiscount)} VND`
}

watch(
  () => formState.maxDiscount,
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
    if (isPercent.value && newValue !== undefined && newValue !== null) {
      if (newValue > 100) {
        Message.warning('Giá trị giảm theo phần trăm không được vượt quá 100%')
        formState.discountValue = 100
      } else if (newValue < 1) {
        formState.discountValue = 1
      }
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
  try {
    const code = await generateNextCode()
    if (code) {
      formState.code = code
    } else {
      formState.code = 'PGG000001'
    }
  } catch (error) {
    console.error('Error generating coupon code:', error)
    formState.code = 'PGG000001'
  }
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
  if (isPercent.value) {
    if (Number.isNaN(discountValue) || discountValue < 1) {
      Message.error('Giá trị giảm theo % phải từ 1 - 100')
      return
    }
    if (discountValue > 100) {
      Message.error('Giá trị giảm theo phần trăm tối đa 100%')
      return
    }
  } else {
    if (Number.isNaN(discountValue) || discountValue <= 0) {
      Message.error('Giá trị giảm phải lớn hơn 0')
      return
    }
  }

  if (!formState.dateRange || formState.dateRange.length !== 2 || !formState.dateRange[0] || !formState.dateRange[1]) {
    Message.error('Vui lòng chọn thời gian áp dụng')
    return
  }
  if (new Date(formState.dateRange[0]) > new Date(formState.dateRange[1])) {
    Message.error('Ngày kết thúc phải sau ngày bắt đầu')
    return
  }

  if (isPercent.value) {
    const capValue = Number(formState.maxDiscount)
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
    const minOrderValue = Number(formState.minOrder || 0)
    if (minOrderValue > 0 && capValue >= minOrderValue) {
      Message.error(t('discount.validation.maxDiscountLessThanMinOrder'))
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

const toBackendDateTime = (value: string): string => {
  if (!value) return value
  // Backend expects ISO_LOCAL_DATE_TIME (yyyy-MM-dd'T'HH:mm:ss)
  return value.replace(' ', 'T')
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
    ngayBatDau: toBackendDateTime(formState.dateRange[0]),
    ngayKetThuc: toBackendDateTime(formState.dateRange[1]),
    trangThai: calculateStatus(toBackendDateTime(formState.dateRange[0]), toBackendDateTime(formState.dateRange[1])),
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
  padding: 16px 20px;
}

.coupon-card {
  margin-top: 16px;
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
  flex: 0 0 42%;
  max-width: 42%;
  padding: 20px 18px 20px 20px;
  border-right: 1px solid var(--color-border-2);
  overflow-y: auto;
  max-height: 72vh;
  /* Hide scrollbar */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

.left-column::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
}

.right-column {
  flex: 1;
  min-width: 0;
  padding: 20px;
  background: var(--color-fill-1);
  overflow-x: auto;
}

.column-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-1);
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--color-primary-6);
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
