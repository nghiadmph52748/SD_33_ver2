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
            <a-form-item :label="t('discount.common.search')">
              <a-input v-model="filters.search" :placeholder="t('discount.coupon.searchPlaceholder')" allow-clear @change="searchCoupons" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item :label="t('discount.common.startDate')">
              <a-date-picker
                v-model="filters.startDate"
                :placeholder="t('discount.common.selectStartDate')"
                allow-clear
                @change="searchCoupons"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item :label="t('discount.common.endDate')">
              <a-date-picker
                v-model="filters.endDate"
                :placeholder="t('discount.common.selectEndDate')"
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
            <a-form-item :label="t('discount.coupon.discountType')">
              <a-radio-group v-model="filters.discountType" type="button" @change="searchCoupons">
                <a-radio value="">{{ t('discount.coupon.discountType.all') }}</a-radio>
                <a-radio value="percentage">{{ t('discount.coupon.discountType.percentageShort') }}</a-radio>
                <a-radio value="fixed">{{ t('discount.coupon.discountType.amountShort') }}</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item :label="t('discount.common.status')">
              <a-radio-group v-model="filters.status" type="button" @change="searchCoupons">
                <a-radio value="">{{ t('discount.common.status.all') }}</a-radio>
                <a-radio value="active">{{ t('discount.common.status.active') }}</a-radio>
                <a-radio value="expired">{{ t('discount.common.status.expired') }}</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <!-- Empty column for alignment -->
          </a-col>
        </a-row>

        <div class="slider-grid">
          <div class="slider-block">
            <div class="slider-title">{{ t('discount.coupon.discountValueRange.percent') }}</div>
            <a-slider v-model="filters.discountPercentRange" range :min="0" :max="100" @change="searchCoupons" />
            <div class="slider-values">
              <span class="slider-value-badge">{{ filters.discountPercentRange[0] }}%</span>
              <span class="slider-value-separator">-</span>
              <span class="slider-value-badge">{{ filters.discountPercentRange[1] }}%</span>
            </div>
          </div>

          <div class="slider-block">
            <div class="slider-title">{{ t('discount.coupon.discountValueRange.amount') }}</div>
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
            <div class="slider-title">{{ t('discount.coupon.minOrderRange') }}</div>
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
            <div class="slider-title">{{ t('discount.coupon.quantityRange') }}</div>
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
              {{ t('discount.common.reset') }}
            </a-button>
            <a-button type="primary" @click="openCreatePage">
              <template #icon>
                <icon-plus />
              </template>
              {{ t('discount.coupon.add') }}
            </a-button>
            <a-button @click="exportCoupons">
              <template #icon>
                <icon-download />
              </template>
              {{ t('discount.common.export') }}
            </a-button>
          </a-space>
        </div>
      </a-form>
    </a-card>

    <!-- Coupons Table -->
    <a-card :title="t('discount.coupon.list')" class="table-card">
      <template #extra>
        <a-input-search
          v-model="tableSearch"
          :placeholder="t('discount.coupon.tableSearchPlaceholder')"
          allow-clear
          style="width: 300px"
          @search="handleTableSearch"
          @clear="handleTableSearch"
        />
      </template>
      <a-table :columns="columns" :data="filteredCoupons" :pagination="pagination" :loading="loading">
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

        <template #featured="{ record }">
          <div class="privacy-badge" :class="{ 'privacy-badge-private': record.featured }">
            <icon-lock v-if="record.featured" class="privacy-icon" />
            <icon-public v-else class="privacy-icon" />
            <span>{{ record.featured ? t('discount.coupon.privacy.private') : t('discount.coupon.privacy.public') }}</span>
          </div>
        </template>

        <template #discount_type="{ record }">
          <a-tag :color="record.discount_type === 'percentage' ? 'blue' : 'green'">
            {{
              record.discount_type === 'percentage'
                ? t('discount.coupon.discountType.percentageShort')
                : t('discount.coupon.discountType.amountShort')
            }}
          </a-tag>
        </template>

        <template #discount_value="{ record }">
          <div class="numeric-cell numeric-cell--discount">
            <span class="numeric-cell__main">{{ discountMain(record) }}</span>
            <span v-if="discountUnit(record)" class="numeric-cell__suffix">{{ discountUnit(record) }}</span>
          </div>
        </template>

        <template #min_order_value="{ record }">
          <div class="numeric-cell">
            <span class="numeric-cell__main">{{ minOrderMain(record) }}</span>
            <span v-if="minOrderUnit(record)" class="numeric-cell__suffix">{{ minOrderUnit(record) }}</span>
          </div>
        </template>

        <template #usage_limits="{ record }">
          <div class="usage-limits">{{ record.total_used }}/{{ record.total_usage_limit || '∞' }} tổng</div>
        </template>

        <template #validity_period="{ record }">
          <div class="validity-period">
            <div>{{ formatDateTime(record.start_date) }}</div>
            <div>{{ formatDateTime(record.end_date) }}</div>
          </div>
        </template>

        <template #quantity="{ record }">
          <div class="quantity-cell">
            <template v-if="record.usage_limit !== null">
              <div>{{ record.usage_limit - (record.total_used || 0) }}/{{ record.usage_limit }}</div>
            </template>
            <template v-else>∞</template>
          </div>
        </template>

        <template #status="{ record }">
          <div class="status-cell" :class="`status-${record.status}`">
            {{ getStatusText(record.status) }}
          </div>
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
            <a-switch
              v-if="isAdmin"
              type="round"
              :model-value="record.enabled"
              :loading="isCouponStatusUpdating(record.id)"
              :disabled="!canToggleCouponStatus(record)"
              @change="(value) => handleCouponStatusToggle(record, value)"
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
      v-model:visible="couponDetailVisible"
      :title="t('discount.coupon.detail')"
      :footer="false"
      width="900px"
      :body-style="{ maxHeight: '80vh', overflowY: 'auto' }"
    >
      <a-descriptions :column="2" bordered>
        <a-descriptions-item :label="t('discount.common.name')">{{ selectedCoupon?.name }}</a-descriptions-item>
        <a-descriptions-item :label="t('discount.common.code')">{{ selectedCoupon?.code }}</a-descriptions-item>
        <a-descriptions-item :label="t('discount.coupon.discountValue')">
          <span v-if="selectedCoupon?.discount_type === 'percentage'">{{ selectedCoupon?.discount_value }}%</span>
          <span v-else>{{ formatCurrency(selectedCoupon?.discount_value ?? 0) }}</span>
        </a-descriptions-item>
        <a-descriptions-item v-if="selectedCoupon?.discount_type === 'percentage'" :label="t('discount.coupon.maxDiscount')">
          <span>
            {{
              selectedCoupon?.max_discount_value != null
                ? formatCurrency(selectedCoupon.max_discount_value)
                : t('discount.common.unlimited')
            }}
          </span>
        </a-descriptions-item>
        <a-descriptions-item :label="t('discount.coupon.minOrder')">
          {{ selectedCoupon?.min_order_value ? formatCurrency(selectedCoupon.min_order_value) : t('discount.common.unlimited') }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('discount.coupon.usageLimit')">
          {{ selectedCoupon?.usage_limit ?? '∞' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('discount.coupon.validityPeriod')" :span="2">
          {{ formatDateTime(selectedCoupon?.start_date ?? '') }} - {{ formatDateTime(selectedCoupon?.end_date ?? '') }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('discount.common.status')">
          <a-tag :color="getStatusColor(selectedCoupon?.status ?? '')">
            {{ selectedCoupon ? getStatusText(selectedCoupon.status) : '' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item :label="t('discount.coupon.featured')">
          <a-tag v-if="selectedCoupon?.featured" color="orange">
            <template #icon>
              <icon-star />
            </template>
            {{ t('discount.coupon.featured.yes') }}
          </a-tag>
          <span v-else style="color: var(--color-text-3)">{{ t('discount.coupon.featured.no') }}</span>
        </a-descriptions-item>
        <a-descriptions-item :label="t('discount.common.description')" :span="2">
          {{ selectedCoupon?.source?.moTa || '—' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('discount.common.createdAt')">
          {{ selectedCoupon?.created_at ? formatDateTime(selectedCoupon.created_at) : '—' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('discount.common.updatedAt')">
          {{ selectedCoupon?.updated_at ? formatDateTime(selectedCoupon.updated_at) : '—' }}
        </a-descriptions-item>
        <a-descriptions-item v-if="selectedCoupon?.featured" :label="t('discount.coupon.appliedCustomers')" :span="2">
          <div style="display: flex; align-items: center; gap: 12px">
            <span>
              <strong>{{ selectedCoupon?.source?.idKhachHang?.length || 0 }}</strong>
              {{ t('discount.common.customers') }}
            </span>
            <a-button
              v-if="selectedCoupon?.source?.idKhachHang && selectedCoupon.source.idKhachHang.length > 0"
              size="small"
              @click="viewAppliedCustomers"
            >
              {{ t('discount.common.viewList') }}
            </a-button>
          </div>
        </a-descriptions-item>
      </a-descriptions>

      <!-- History Section -->
      <a-divider style="margin: 24px 0" />
      <div class="history-section">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
          <h3 style="margin: 0; font-size: 16px; font-weight: 600">{{ t('discount.history.title') }}</h3>
          <div style="display: flex; align-items: center; gap: 8px">
            <a-spin v-if="isHistoryLoading" size="small" />
            <a-button v-else size="small" type="text" @click="selectedCoupon && loadHistory(selectedCoupon.id)">
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
      v-model:visible="couponEditVisible"
      title="Chỉnh sửa phiếu giảm giá"
      :confirm-loading="couponEditSubmitting"
      @before-ok="submitCouponEdit"
      @cancel="couponEditVisible = false"
      :width="modalWidth"
      :class="{ 'edit-modal-multi-column': hasMultipleColumns }"
    >
      <div :class="modalLayoutClass">
        <!-- Left Column: Form -->
        <div :class="formColumnClass">
          <div v-if="hasMultipleColumns" class="modal-column-title">Thông tin phiếu giảm giá</div>

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

            <a-form-item field="featured">
              <a-checkbox v-model="couponEditForm.featured">Phiếu giảm giá nổi bật</a-checkbox>
              <div style="margin-left: 24px; margin-top: 4px; font-size: 12px; color: var(--color-text-3)">
                Phiếu giảm giá nổi bật sẽ được hiển thị ở đầu danh sách
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
        </div>

        <!-- Middle/Right Column: Customer Selection -->
        <div v-if="couponEditForm.featured" :class="customerColumnClass">
          <div class="modal-column-title">Chọn khách hàng</div>
          <div class="customer-selection-section">
            <a-input-search v-model="customerSearchQuery" placeholder="Tìm kiếm khách hàng..." allow-clear style="margin-bottom: 12px" />

            <!-- Select All / Deselect All buttons -->
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

            <div
              v-if="!customersLoading && filteredCustomers.length === 0"
              style="text-align: center; padding: 20px; color: var(--color-text-3)"
            >
              Không tìm thấy khách hàng nào
            </div>
            <div style="margin-top: 8px; font-size: 12px; color: var(--color-text-3)">
              Đã chọn:
              <strong>{{ couponEditForm.selectedCustomerIds.length }}</strong>
              khách hàng
            </div>
          </div>
        </div>
      </div>
    </a-modal>

    <!-- Applied Customers Modal -->
    <a-modal
      v-model:visible="appliedCustomersVisible"
      :title="t('discount.coupon.appliedCustomers')"
      :footer="false"
      width="700px"
      :body-style="{ maxHeight: '70vh', overflowY: 'auto' }"
    >
      <div v-if="appliedCustomersList.length > 0">
        <a-list :data="appliedCustomersList" :bordered="false">
          <template #item="{ item, index }">
            <a-list-item>
              <div style="display: flex; align-items: center; gap: 12px; width: 100%">
                <div style="font-weight: 600; color: var(--color-text-3); min-width: 30px">
                  {{ index + 1 }}
                </div>
                <div style="flex: 1">
                  <div style="font-weight: 500; color: var(--color-text-1)">
                    {{ item.tenKhachHang }}
                  </div>
                  <div style="font-size: 12px; color: var(--color-text-3); margin-top: 2px">
                    {{ item.email || '—' }} | {{ item.soDienThoai || '—' }}
                  </div>
                </div>
              </div>
            </a-list-item>
          </template>
        </a-list>
      </div>
      <div v-else style="text-align: center; padding: 40px; color: var(--color-text-3)">{{ t('discount.coupon.noCustomersFound') }}</div>
    </a-modal>

    <!-- Edit Confirmation Modal -->
    <a-modal
      v-model:visible="couponEditConfirmVisible"
      title="Xác nhận chỉnh sửa phiếu giảm giá"
      :confirm-loading="couponEditSubmitting"
      ok-text="Xác nhận"
      width="500px"
      @ok="confirmCouponEdit"
      @cancel="couponEditConfirmVisible = false"
    >
      <p>
        Bạn có chắc chắn muốn chỉnh sửa phiếu giảm giá
        <strong>{{ selectedCoupon?.name }}</strong>
        ?
      </p>
      <p class="modal-note">
        Lý do thay đổi:
        <strong>{{ couponEditForm.lyDoThayDoi }}</strong>
      </p>
      <p class="modal-note">Hành động này sẽ cập nhật thông tin phiếu giảm giá.</p>
    </a-modal>

    <!-- Export Confirmation Modal -->
    <a-modal
      v-model:visible="exportConfirmVisible"
      title="Xác nhận xuất danh sách"
      :confirm-loading="exportSubmitting"
      ok-text="Xuất"
      width="480px"
      @ok="confirmExportCoupons"
      @cancel="exportConfirmVisible = false"
    >
      <p>
        Bạn có chắc chắn muốn xuất danh sách
        <strong>{{ filteredCoupons.length }}</strong>
        phiếu giảm giá?
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
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import router from '@/router'
import {
  IconPlus,
  IconDownload,
  IconEdit,
  IconEye,
  IconDelete,
  IconStar,
  IconRefresh,
  IconPlusCircle,
  IconUser,
  IconEmpty,
  IconCheck,
  IconClose,
  IconLock,
  IconPublic,
} from '@arco-design/web-vue/es/icon'
import downloadCsv from '@/utils/export-csv'
import {
  fetchCoupons,
  fetchCustomers,
  fetchCouponHistory,
  type CouponApiModel,
  type CustomerApiModel,
  type CouponHistoryApiModel,
  updateCoupon,
} from '@/api/discount-management'
import type { FormInstance, FormRules } from '@arco-design/web-vue/es/form'

// Breadcrumb setup
const { t } = useI18n()
const { breadcrumbItems } = useBreadcrumb()
const userStore = useUserStore()

const isAdmin = computed(() => {
  const roleId = userStore.idQuyenHan
  const roleName = userStore.tenQuyenHan?.toLowerCase()

  return roleId === 1 || roleName?.includes('admin') || roleName?.includes('quản trị')
})

// Check if user is admin
// Check both idQuyenHan and tenQuyenHan for flexibility

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
const tableSearch = ref('')

// Table
const loading = ref(false)
const columns = computed(() => [
  {
    title: 'STT',
    dataIndex: 'index',
    width: 56,
    align: 'center' as const,
  },
  {
    title: t('discount.common.code'),
    dataIndex: 'code',
    slotName: 'code',
    width: 85,
  },
  {
    title: t('discount.coupon.name'),
    dataIndex: 'name',
    slotName: 'name',
  },
  {
    title: t('discount.coupon.private'),
    dataIndex: 'featured',
    slotName: 'featured',
    width: 100,
    align: 'center' as const,
  },
  {
    title: t('discount.coupon.discountType'),
    dataIndex: 'discount_type',
    slotName: 'discount_type',
    width: 80,
    align: 'center' as const,
  },
  {
    title: t('discount.coupon.discountValue'),
    dataIndex: 'discount_value',
    slotName: 'discount_value',
    width: 85,
    align: 'center' as const,
  },
  {
    title: t('discount.coupon.minOrder'),
    dataIndex: 'min_order_value',
    slotName: 'min_order_value',
    width: 105,
    align: 'right' as const,
  },
  {
    title: t('discount.coupon.validityPeriod'),
    dataIndex: 'start_date',
    slotName: 'validity_period',
    width: 155,
    align: 'center' as const,
  },
  {
    title: t('discount.coupon.quantity'),
    dataIndex: 'quantity',
    slotName: 'quantity',
    width: 55,
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
    title: t('discount.common.actions'),
    slotName: 'action',
    width: 105,
    fixed: 'right' as const,
    align: 'center' as const,
  },
])

type CouponStatus = 'active' | 'expired' | 'upcoming' | 'used_up' | 'inactive'

interface CouponRecord {
  id: number
  index: number
  code: string
  name: string
  discount_type: 'percentage' | 'fixed'
  discount_value: number
  max_discount_value?: number | null
  min_order_value: number | null
  usage_limit: number | null
  total_used?: number
  total_usage_limit?: number | null
  start_date: string
  end_date: string
  status: CouponStatus
  featured: boolean
  enabled: boolean
  created_at?: string
  updated_at?: string
  source: CouponApiModel
}

const coupons = ref<CouponRecord[]>([])
const selectedCoupon = ref<CouponRecord | null>(null)

const couponDetailVisible = ref(false)
const couponEditVisible = ref(false)
const couponEditSubmitting = ref(false)
const couponEditConfirmVisible = ref(false)
const couponStatusUpdatingIds = ref<number[]>([])

// Applied customers modal
const appliedCustomersVisible = ref(false)
const appliedCustomersList = ref<CustomerApiModel[]>([])

// History
const historyList = ref<CouponHistoryApiModel[]>([])
const isHistoryLoading = ref(false)

// Export confirmation modal
const exportConfirmVisible = ref(false)
const exportSubmitting = ref(false)

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
  name: [{ required: true, message: 'Vui lòng nhập tên phiếu giảm giá' }],
  discountMode: [{ required: true, message: 'Vui lòng chọn hình thức giảm giá' }],
  discountValue: [{ required: true, message: 'Vui lòng nhập giá trị giảm' }],
  quantity: [{ required: true, message: 'Vui lòng nhập số lượng áp dụng' }],
  dateRange: [{ required: true, type: 'array', message: 'Vui lòng chọn thời gian áp dụng' }],
  lyDoThayDoi: [{ required: true, message: 'Vui lòng nhập lý do thay đổi' }],
}

const isPercentEdit = computed(() => couponEditForm.discountMode === 'percentage')

// Modal layout computeds - only featured creates new column, products stay in form
const hasMultipleColumns = computed(() => couponEditForm.featured)

const modalWidth = computed(() => {
  if (couponEditForm.featured) return 1200 // 2 columns
  return 700 // 1 column - comfortable width for 2-column checkbox layout
})

const modalLayoutClass = computed(() => {
  if (couponEditForm.featured) return 'modal-two-columns'
  return ''
})

const formColumnClass = computed(() => {
  if (couponEditForm.featured) return 'modal-left-column'
  return ''
})

const customerColumnClass = computed(() => {
  return 'modal-right-column'
})

watch(
  () => couponEditForm.discountMode,
  (mode) => {
    if (mode === 'percentage' && couponEditForm.discountValue > 100) {
      couponEditForm.discountValue = 100
    }
  }
)

// Real-time validation for discount value
watch(
  () => couponEditForm.discountValue,
  (value) => {
    if (value === null || value === undefined) return

    // Ensure value is positive
    if (value < 0) {
      couponEditForm.discountValue = 0
      return
    }

    // Cap percentage discount at 100%
    if (isPercentEdit.value && value > 100) {
      couponEditForm.discountValue = 100
      Message.warning('Giá trị giảm theo phần trăm không được vượt quá 100%')
    }
  }
)

// Customer selection for edit form
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

// Custom checkbox column for edit modal
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
    // Deselect all filtered customers
    filteredCustomers.value.forEach((customer) => {
      const index = couponEditForm.selectedCustomerIds.indexOf(customer.id)
      if (index > -1) {
        couponEditForm.selectedCustomerIds.splice(index, 1)
      }
    })
  } else {
    // Select all filtered customers
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
      // Filter active customers
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

// Load customers when featured is checked in edit form
watch(
  () => couponEditForm.featured,
  (isFeatured) => {
    if (isFeatured && customers.value.length === 0) {
      loadCustomers()
    }
    // Clear selected customers when unchecking featured
    if (!isFeatured) {
      couponEditForm.selectedCustomerIds = []
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

const resolveCouponEnabled = (coupon: CouponApiModel, status: CouponStatus): boolean => {
  const raw = (coupon as unknown as { trangThai?: unknown }).trangThai

  if (typeof raw === 'boolean') {
    return raw
  }

  if (typeof raw === 'number') {
    return raw !== 0
  }

  if (typeof raw === 'string') {
    const normalized = raw.trim().toLowerCase()
    if (['true', '1', 'active', 'đang hoạt động', 'dang hoat dong'].includes(normalized)) {
      return true
    }
    if (['false', '0', 'inactive', 'không hoạt động', 'khong hoat dong'].includes(normalized)) {
      return false
    }
  }

  return status === 'active' || status === 'upcoming'
}

const toCouponRecord = (coupon: CouponApiModel, index: number): CouponRecord => {
  const discountType = determineDiscountType(coupon)
  const discountValue = Number(coupon.giaTriGiamGia ?? 0)
  const minOrder = coupon.hoaDonToiThieu != null ? Number(coupon.hoaDonToiThieu) : null
  const usageLimit = coupon.soLuongDung ?? null
  const usedCount = coupon.soLuongDaDung ?? 0
  const status = deriveCouponStatus(coupon)

  return {
    id: coupon.id,
    index: index + 1,
    code: coupon.maPhieuGiamGia ?? '',
    name: coupon.tenPhieuGiamGia ?? '',
    discount_type: discountType,
    discount_value: discountValue,
    min_order_value: minOrder,
    max_discount_value: discountType === 'percentage' ? (coupon.soTienToiDa != null ? Number(coupon.soTienToiDa) : null) : null,
    usage_limit: usageLimit,
    total_used: usedCount,
    total_usage_limit: usageLimit,
    start_date: coupon.ngayBatDau ?? '',
    end_date: coupon.ngayKetThuc ?? '',
    status,
    featured: coupon.featured ?? false,
    enabled: resolveCouponEnabled(coupon, status),
    created_at: coupon.createdAt,
    updated_at: coupon.updatedAt,
    source: { ...coupon },
  }
}

// Methods
const formatNumberWithSeparator = (value: number | string | undefined) => {
  if (value === null || value === undefined || value === '') return ''
  // Convert to number first to handle any string input
  const numValue = typeof value === 'string' ? parseFloat(value.replace(/\./g, '')) : value
  if (Number.isNaN(numValue)) return ''
  // Format with thousand separators
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

const extractCurrencyParts = (amount: number | null | undefined) => {
  if (amount === null || amount === undefined) {
    return null
  }
  const formatted = formatCurrency(Number(amount))
  const normalized = formatted.replace(/\u00a0/g, ' ').trim()
  const parts = normalized.split(' ')
  if (parts.length === 1) {
    return { numeric: parts[0], unit: '' }
  }
  return {
    numeric: parts.slice(0, -1).join(' '),
    unit: parts.at(-1) ?? '',
  }
}

const discountMain = (record: CouponRecord) => {
  if (record.discount_type === 'percentage') {
    return `${record.discount_value}%`
  }
  return extractCurrencyParts(record.discount_value)?.numeric ?? formatCurrency(record.discount_value)
}

const discountUnit = (record: CouponRecord) => {
  if (record.discount_type === 'percentage') {
    return ''
  }
  return extractCurrencyParts(record.discount_value)?.unit ?? ''
}

const minOrderMain = (record: CouponRecord) => {
  if (record.min_order_value === null) {
    return 'Không giới hạn'
  }
  return extractCurrencyParts(record.min_order_value)?.numeric ?? formatCurrency(record.min_order_value)
}

const minOrderUnit = (record: CouponRecord) => {
  if (record.min_order_value === null) {
    return ''
  }
  return extractCurrencyParts(record.min_order_value)?.unit ?? ''
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

  // Table search filter
  if (tableSearch.value) {
    const searchTerm = tableSearch.value.toLowerCase()
    filtered = filtered.filter((coupon) => {
      const discountValueStr = coupon.discount_type === 'percentage' ? `${coupon.discount_value}%` : formatCurrency(coupon.discount_value)
      const minOrderStr = coupon.min_order_value ? formatCurrency(coupon.min_order_value) : 'không giới hạn'
      return (
        coupon.code.toLowerCase().includes(searchTerm) ||
        coupon.name.toLowerCase().includes(searchTerm) ||
        discountValueStr.toLowerCase().includes(searchTerm) ||
        minOrderStr.toLowerCase().includes(searchTerm)
      )
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

    // Sort by featured status first (featured vouchers at top), then by createdAt (newest first)
    const sorted = data.sort((a, b) => {
      // Featured vouchers always come first
      if (a.featured && !b.featured) return -1
      if (!a.featured && b.featured) return 1
      // If both have same featured status, sort by createdAt descending (newest first)
      if (a.createdAt && b.createdAt) {
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      }
      // Fallback to id descending if createdAt is not available
      return b.id - a.id
    })
    coupons.value = sorted.map((item, index) => toCouponRecord(item, index))
    // eslint-disable-next-line no-console
    console.log(
      '[DEBUG] Rendered coupons with featured:',
      coupons.value.filter((c) => c.featured)
    )
  } catch {
    Message.error({ content: t('discount.message.loadCouponsFailed'), duration: 5000 })
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
      return t('discount.coupon.status.active')
    case 'expired':
      return t('discount.coupon.status.expired')
    case 'used_up':
      return t('discount.coupon.status.usedUp')
    case 'upcoming':
      return t('discount.coupon.status.upcoming')
    case 'inactive':
      return t('discount.coupon.status.inactive')
    default:
      return status
  }
}

const searchCoupons = () => {
  // Filtering is now handled by the computed filteredCoupons property
  // This function is called when filters change to trigger reactivity
}

const handleTableSearch = () => {
  // Filtering is handled reactively by filteredCoupons
  // This function is called when the table search input changes
}

const isCouponStatusUpdating = (id: number) => couponStatusUpdatingIds.value.includes(id)

const canToggleCouponStatus = (coupon: CouponRecord) => {
  // Allow toggling for active/inactive/upcoming vouchers; lock expired or exhausted vouchers
  return coupon.status !== 'expired' && coupon.status !== 'used_up'
}

const handleCouponStatusToggle = async (coupon: CouponRecord, nextValue: boolean) => {
  if (!coupon) return

  const previousValue = coupon.enabled
  if (nextValue === previousValue) {
    return
  }

  if (!canToggleCouponStatus(coupon)) {
    Message.warning(t('discount.message.cannotToggleCouponStatus'))
    return
  }

  if (isCouponStatusUpdating(coupon.id)) {
    return
  }

  couponStatusUpdatingIds.value.push(coupon.id)

  const previousStatus = coupon.status
  coupon.source.trangThai = nextValue
  coupon.enabled = nextValue
  coupon.status = deriveCouponStatus({ ...coupon.source, trangThai: nextValue })
  if (selectedCoupon.value?.id === coupon.id) {
    selectedCoupon.value = { ...coupon }
  }

  const payload = {
    maPhieuGiamGia: coupon.source.maPhieuGiamGia ?? coupon.code,
    tenPhieuGiamGia: coupon.source.tenPhieuGiamGia ?? coupon.name,
    loaiPhieuGiamGia: Boolean(coupon.source.loaiPhieuGiamGia),
    giaTriGiamGia: Number(coupon.source.giaTriGiamGia ?? coupon.discount_value),
    hoaDonToiThieu:
      coupon.source.hoaDonToiThieu !== null && coupon.source.hoaDonToiThieu !== undefined ? Number(coupon.source.hoaDonToiThieu) : null,
    soLuongDung: coupon.source.soLuongDung ?? null,
    ngayBatDau: coupon.source.ngayBatDau ?? coupon.start_date,
    ngayKetThuc: coupon.source.ngayKetThuc ?? coupon.end_date,
    trangThai: nextValue,
    moTa: coupon.source.moTa ?? null,
    deleted: Boolean(coupon.source.deleted),
    idKhachHang: coupon.source.idKhachHang ?? [],
    featured: Boolean(coupon.source.featured),
    lyDoThayDoi: t('discount.message.updateCouponStatus'),
  }

  try {
    await updateCoupon(coupon.id, payload)
    Message.success(nextValue ? t('discount.message.couponEnabled') : t('discount.message.couponDisabled'))
  } catch (error: any) {
    coupon.source.trangThai = previousValue
    coupon.enabled = previousValue
    coupon.status = previousStatus
    if (selectedCoupon.value?.id === coupon.id) {
      selectedCoupon.value = { ...coupon, status: previousStatus }
    }
    const errorMessage = error?.response?.data?.message || error?.message || t('discount.message.updateCouponStatusFailed')
    Message.error(errorMessage)
  } finally {
    couponStatusUpdatingIds.value = couponStatusUpdatingIds.value.filter((id) => id !== coupon.id)
  }
}

// Load coupon history
const loadHistory = async (couponId: number) => {
  isHistoryLoading.value = true
  try {
    historyList.value = await fetchCouponHistory(couponId)
  } catch {
    Message.error(t('discount.message.loadHistoryFailed'))
    historyList.value = []
  } finally {
    isHistoryLoading.value = false
  }
}

// Format datetime for history
const formatHistoryDate = (dateString: string) => {
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

// Get action color for timeline
const getActionColor = (action: string) => {
  if (action.includes('TẠO') || action.includes('CREATE')) return 'blue'
  if (action.includes('CẬP NHẬT') || action.includes('UPDATE')) return 'orange'
  if (action.includes('XÓA') || action.includes('DELETE')) return 'red'
  return 'gray'
}

const openCreatePage = () => {
  router.push({ name: 'QuanLyPhieuGiamGiaCreate' })
}

const viewCoupon = async (coupon: CouponRecord) => {
  selectedCoupon.value = coupon
  couponDetailVisible.value = true

  // Load history when viewing coupon
  await loadHistory(coupon.id)
}

const viewAppliedCustomers = async () => {
  if (!selectedCoupon.value?.source?.idKhachHang || selectedCoupon.value.source.idKhachHang.length === 0) {
    Message.warning('Không có khách hàng nào')
    return
  }

  // Load all customers if not loaded yet
  if (customers.value.length === 0) {
    await loadCustomers()
  }

  // Filter customers that are applied to this voucher
  const customerIds = selectedCoupon.value.source.idKhachHang
  appliedCustomersList.value = customers.value.filter((customer) => customerIds.includes(customer.id))

  appliedCustomersVisible.value = true
}

const editCoupon = async (coupon: CouponRecord) => {
  // Navigate to edit page
  router.push({ name: 'QuanLyPhieuGiamGiaEdit', params: { id: coupon.id } })
}

const submitCouponEdit = async () => {
  if (!selectedCoupon.value) {
    return false
  }
  if (couponEditSubmitting.value) {
    return false
  }

  const form = couponEditFormRef.value
  if (!form) {
    return false
  }

  try {
    await form.validate()
  } catch {
    return false
  }

  const discountValue = Number(couponEditForm.discountValue)
  if (Number.isNaN(discountValue) || discountValue <= 0) {
    Message.error('Giá trị giảm phải lớn hơn 0')
    return false
  }

  if (isPercentEdit.value && discountValue > 100) {
    Message.error('Giá trị giảm theo phần trăm tối đa 100%')
    return false
  }

  const [startDate, endDate] = couponEditForm.dateRange
  if (!startDate || !endDate) {
    Message.error('Vui lòng chọn thời gian áp dụng')
    return false
  }
  if (new Date(startDate) > new Date(endDate)) {
    Message.error('Ngày kết thúc phải sau ngày bắt đầu')
    return false
  }

  if (isPercentEdit.value) {
    const capValue = Number(couponEditForm.maxDiscount)
    if (!capValue || Number.isNaN(capValue) || capValue <= 0) {
      Message.error('Vui lòng nhập mức giảm tối đa hợp lệ')
      return false
    }
    if (capValue > 50000000) {
      Message.error('Giá trị giảm tối đa không được vượt quá 50.000.000 VND')
      return false
    }

    // Validate: Max discount should not exceed min order value
    const minOrderValue = Number(couponEditForm.minOrder || 0)
    if (minOrderValue > 0 && capValue >= minOrderValue) {
      Message.error('Giá trị giảm tối đa phải nhỏ hơn giá trị đơn hàng tối thiểu')
      return false
    }
  } else {
    // For fixed amount discount
    const minOrderValue = Number(couponEditForm.minOrder || 0)
    if (minOrderValue > 0 && discountValue >= minOrderValue) {
      Message.error('Giá trị giảm phải nhỏ hơn giá trị đơn hàng tối thiểu')
      return false
    }
  }

  const quantityValue = Number(couponEditForm.quantity)
  if (!Number.isInteger(quantityValue) || quantityValue <= 0) {
    Message.error('Số lượng áp dụng phải lớn hơn 0')
    return false
  }

  // Validate customer selection for featured vouchers
  if (couponEditForm.featured && couponEditForm.selectedCustomerIds.length === 0) {
    Message.error('Vui lòng chọn ít nhất một khách hàng cho phiếu giảm giá nổi bật')
    return false
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
    JSON.stringify([...couponEditForm.selectedCustomerIds].sort()) !==
      JSON.stringify([...originalCouponEditForm.selectedCustomerIds].sort())

  if (!hasChanges) {
    Message.warning('Không có thay đổi nào để cập nhật')
    return false
  }

  // All validations passed - show confirmation modal
  couponEditConfirmVisible.value = true
  return false // Prevent auto-close, confirmation modal will handle actual submission
}

const confirmCouponEdit = async () => {
  if (!selectedCoupon.value) {
    return
  }

  const discountValue = Number(couponEditForm.discountValue)
  const quantityValue = Number(couponEditForm.quantity)
  const [startDate, endDate] = couponEditForm.dateRange

  // eslint-disable-next-line no-console
  console.log('[DEBUG] Edit form data:', {
    name: couponEditForm.name,
    quantity: couponEditForm.quantity,
    quantityParsed: quantityValue,
  })

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
    idKhachHang: couponEditForm.featured ? couponEditForm.selectedCustomerIds : [],
    featured: couponEditForm.featured,
    lyDoThayDoi: couponEditForm.lyDoThayDoi.trim(),
  }

  couponEditSubmitting.value = true
  try {
    await updateCoupon(selectedCoupon.value.id, payload)
    Message.success('Cập nhật phiếu giảm giá thành công')

    couponEditConfirmVisible.value = false
    couponEditVisible.value = false

    await loadCoupons()

    // Reload history if detail modal is still open
    if (couponDetailVisible.value && selectedCoupon.value) {
      await loadHistory(selectedCoupon.value.id)
    }
  } catch (error: any) {
    const errorMessage = error.response?.data?.message || error.message || 'Không thể cập nhật phiếu giảm giá'
    Message.error(errorMessage)
  } finally {
    couponEditSubmitting.value = false
  }
}

const exportCoupons = () => {
  if (!filteredCoupons.value.length) {
    Message.warning('Không có dữ liệu để xuất')
    return
  }

  exportConfirmVisible.value = true
}

const confirmExportCoupons = () => {
  exportSubmitting.value = true

  try {
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
    exportConfirmVisible.value = false
  } catch {
    Message.error('Không thể xuất danh sách')
  } finally {
    exportSubmitting.value = false
  }
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

.numeric-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  line-height: 1.15;
}

.numeric-cell__main {
  font-weight: 600;
  text-align: center;
  white-space: nowrap;
}

.numeric-cell__suffix {
  font-size: 11px;
  color: var(--color-text-3);
  white-space: nowrap;
}

.numeric-cell--discount .numeric-cell__main {
  color: #1890ff;
}

.usage-limits {
  text-align: center;
  font-size: 12px;
  color: #1d2129;
}

.validity-period {
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

.quantity-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  font-size: 13px;
  line-height: 1.4;
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

.status-used_up {
  color: #fa8c16;
  background-color: rgba(250, 140, 22, 0.1);
}

.status-upcoming {
  color: #fa8c16;
  background-color: rgba(250, 140, 22, 0.1);
}

.status-inactive {
  color: #8c8c8c;
  background-color: rgba(140, 140, 140, 0.1);
}

.danger-item {
  color: #ff4d4f;
}

.privacy-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 12px;
  color: var(--color-text-1);
  white-space: nowrap;
}

.privacy-badge-private {
  font-weight: 600;
}

.privacy-badge-private .privacy-icon {
  color: #165dff;
}

.privacy-icon {
  font-size: 16px;
  color: var(--color-text-3);
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
  white-space: nowrap;
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

.customer-selection-section :deep(.arco-table-th) {
  background-color: var(--color-fill-2);
  font-weight: 600;
}

/* Modal multi-column layout */
.edit-modal-multi-column :deep(.arco-modal-body) {
  padding: 0;
  max-height: 70vh;
  overflow: hidden;
}

/* Two-column layout */
.modal-two-columns {
  display: flex;
  gap: 0;
  min-height: 500px;
}

/* Three-column layout */
.modal-three-columns {
  display: flex;
  gap: 0;
  min-height: 500px;
}

/* Form column in 2-column layout */
.modal-left-column {
  flex: 0 0 50%;
  padding: 20px;
  border-right: 1px solid var(--color-border-2);
  overflow-y: auto;
  max-height: 70vh;
  min-width: 0;
}

/* Form column in 3-column layout */
.modal-left-column-three {
  flex: 0 0 33.33%;
  padding: 20px;
  border-right: 1px solid var(--color-border-2);
  overflow-y: auto;
  max-height: 70vh;
}

/* Middle column (customers) in 3-column layout */
.modal-middle-column {
  flex: 0 0 33.33%;
  padding: 20px;
  border-right: 1px solid var(--color-border-2);
  background: var(--color-fill-1);
  overflow-y: auto;
  max-height: 70vh;
}

/* Right column (customers in 2-col or products in 2/3-col) */
.modal-right-column {
  flex: 0 0 50%;
  padding: 20px;
  background: var(--color-fill-1);
  overflow-y: auto;
  max-height: 70vh;
  min-width: 0;
}

.modal-column-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-1);
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 2px solid var(--color-primary-6);
}

.edit-modal-multi-column .customer-selection-section {
  border: none;
  padding: 0;
  background: transparent;
}

.edit-modal-multi-column .customer-selection-section :deep(.arco-table-container) {
  border: 1px solid var(--color-border-2);
  border-radius: 4px;
}

/* Product selection section */
.product-selection-section {
  border: none;
  padding: 0;
  background: transparent;
}

.edit-modal-multi-column .product-selection-section :deep(.arco-table-container) {
  border: 1px solid var(--color-border-2);
  border-radius: 4px;
}

.product-selection-section :deep(.arco-table) {
  border-radius: 4px;
}

.product-selection-section :deep(.arco-table-th) {
  background-color: var(--color-fill-2);
  font-weight: 600;
}

.product-selection-section :deep(.arco-table-td) {
  border-bottom: 1px solid var(--color-border-2);
}

.product-selection-section :deep(.arco-table-tr:hover) {
  background-color: var(--color-fill-1);
}

.product-selection-section :deep(.arco-checkbox) {
  pointer-events: auto !important;
  cursor: pointer !important;
}

.product-selection-section :deep(.arco-checkbox-icon) {
  pointer-events: auto !important;
}

.product-selection-section :deep(.arco-table-cell) {
  pointer-events: auto !important;
}

.product-selection-section :deep(.arco-table-td) {
  position: relative;
  z-index: 1;
}

.product-selection-section :deep(.arco-table-th .arco-checkbox) {
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

/* History section styles */
.history-section {
  max-height: 400px;
  overflow-y: auto;
  overflow-x: visible;
  padding-left: 8px;
  margin-left: -8px;
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
  padding-bottom: 8px;
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
  font-size: 13px;
  color: var(--color-text-2);
  margin-bottom: 4px;
}

.history-description {
  margin-top: 8px;
  padding: 8px 12px;
  background: var(--color-fill-2);
  border-radius: 4px;
  font-size: 13px;
  color: var(--color-text-2);
  line-height: 1.6;
}

/* Modal with fixed footer - applies to all modals */
:deep(.arco-modal-wrapper .arco-modal) {
  display: flex;
  flex-direction: column;
  max-height: 90vh;
}

:deep(.arco-modal-wrapper .arco-modal-body) {
  max-height: calc(90vh - 120px);
  overflow-y: auto;
  overflow-x: hidden;
}

:deep(.arco-modal-wrapper .arco-modal-footer) {
  position: sticky;
  bottom: 0;
  background: var(--color-bg-1);
  border-top: 1px solid var(--color-border-2);
  margin-top: 0;
  padding: 16px 20px;
  z-index: 10;
  flex-shrink: 0;
}

/* Edit modal specific adjustments */
.edit-modal-multi-column :deep(.arco-modal-body) {
  padding: 0;
}

/* Fix modal columns overflow */
.modal-two-column-container,
.modal-three-column-container {
  max-height: calc(90vh - 140px);
  overflow: hidden;
}

.modal-left-column,
.modal-middle-column,
.modal-right-column {
  max-height: calc(90vh - 140px);
  overflow-y: auto;
}
</style>
