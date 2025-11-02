<template>
  <div class="pos-system">
    <!-- Main Layout -->
    <a-card class="main-pos-card">
      <!-- <template #title> -->
      <div style="display: flex; width: 100%; align-items: center">
        <div
          class="tabs-container"
          style="flex: 0 0 80%; display: flex; align-items: center; overflow-x: auto; scrollbar-width: none; -ms-overflow-style: none"
        >
          <a-tabs
            v-model:active-key="currentOrderIndex"
            type="button"
            @change="handleOrderChange"
            class="orders-tabs"
            style="flex: 1; min-width: max-content"
          >
            <a-tab-pane v-for="(order, idx) in orders" :key="idx.toString()">
              <template #title>
                <div class="tab-header">
                  <span>Đơn #{{ idx + 1 }} ({{ order.orderCode }})</span>
                  <a-badge
                    v-if="order.items.length > 0"
                    :count="order.items.reduce((sum, item) => sum + item.quantity, 0)"
                    :style="{ backgroundColor: '#f5222d' }"
                  />
                  <a-button type="text" size="mini" status="danger" class="tab-close-btn" @click.stop="showDeleteConfirm(idx)">
                    <template #icon>
                      <icon-close />
                    </template>
                  </a-button>
                </div>
              </template>
            </a-tab-pane>
          </a-tabs>
        </div>
        <div style="flex: 0 0 20%; text-align: center; margin-bottom: 16px">
          <a-button v-if="orders.length < 8" type="primary" size="medium" @click="createNewOrder">
            <template #icon>
              <icon-plus />
            </template>
            Thêm Đơn
          </a-button>
        </div>
      </div>
      <!-- </template> -->
      <a-row :gutter="16" class="pos-main">
        <!-- Left: Orders & Cart -->
        <a-col :xs="24" :lg="16" class="pos-left">
          <a-empty v-if="orders.length === 0" description="Chưa có đơn hàng nào" />
          <div v-else>
            <a-card class="order-code-cart-card">
              <template #title>
                <div style="display: flex; justify-content: space-between; align-items: center">
                  <div style="font-weight: 600; color: #333; font-size: 14px">
                    Mã Đơn:
                    <span style="color: #0960bd; font-weight: 700">{{ currentOrder?.orderCode }}</span>
                  </div>
                  <a-space wrap style="margin-top: 8px">
                    <a-button
                      v-if="currentOrder?.items.length > 0"
                      type="text"
                      status="danger"
                      @click="clearCart"
                      style="border: 1px solid #d9d9d9"
                    >
                      <template #icon>
                        <icon-delete />
                      </template>
                      Xoá Tất Cả
                    </a-button>
                    <a-button @click="showQRScanner = true" style="border: 1px solid #d9d9d9">
                      <template #icon>
                        <icon-qrcode />
                      </template>
                      Quét QR
                    </a-button>
                    <a-button type="primary" @click="openProductModal">
                      <template #icon>
                        <icon-plus />
                      </template>
                      Thêm Sản Phẩm
                    </a-button>
                  </a-space>
                </div>
              </template>
              <!-- Cart Table -->
              <a-card class="cart-card">
                <template #title>🛒 Giỏ Hàng</template>
                <div class="cart-wrapper">
                  <!-- Alert for insufficient stock -->
                  <a-alert v-if="overStockItems.length > 0" type="error" closable style="margin-bottom: 16px">
                    <template #title>❌ Tồn kho không đủ</template>
                    <div style="font-size: 12px">
                      <div v-for="item in overStockItems" :key="item.id" style="margin-bottom: 8px; line-height: 1.5">
                        <strong>{{ item.productName }}</strong>
                        <br />
                        <span style="color: #666">Yêu cầu: {{ item.requiredQty }} cái | Còn lại: {{ item.currentStock }} cái | Thiếu:</span>
                        <strong style="color: #f5222d">{{ item.shortageQty }} cái</strong>
                      </div>
                    </div>
                  </a-alert>
                  <a-table
                    v-if="currentOrder?.items.length > 0"
                    :key="cartTableKey"
                    :columns="cartColumns"
                    :data="paginatedCartItems"
                    :pagination="{
                      current: cartPagination.value?.current || 1,
                      pageSize: cartPagination.value?.pageSize || 5,
                      total: currentOrder?.items.length || 0,
                      showTotal: true,
                      showPageSize: true,
                    }"
                    row-key="id"
                    size="small"
                    :scroll="{ x: '100%' }"
                    @paginate="(page) => (cartPagination.value.current = page)"
                  >
                    <template #product="{ record }">
                      <div style="display: flex; gap: 8px; align-items: center">
                        <img
                          v-if="record.image"
                          :src="record.image"
                          style="width: 70px; height: 70px; object-fit: cover; border-radius: 4px"
                          :alt="record.productName"
                        />
                        <div>
                          <div style="font-weight: 600; font-size: 13px; margin-bottom: 4px">
                            {{ getProductDisplayName(record) }}
                          </div>
                          <div style="display: flex; align-items: center; gap: 8px; font-size: 11px; color: #666">
                            <div v-if="record.tenMauSac" style="display: flex; align-items: center; gap: 4px">
                              <div
                                v-if="record.maMau"
                                style="width: 12px; height: 12px; border-radius: 2px; border: 1px solid #e5e5e5"
                                :style="{ backgroundColor: record.maMau }"
                              ></div>
                              <span>{{ record.maMau }}</span>
                            </div>
                            <div v-if="record.tenKichThuoc" style="display: flex; align-items: center; gap: 4px">
                              <span>Size:</span>
                              <strong>{{ record.tenKichThuoc }}</strong>
                            </div>
                          </div>
                        </div>
                      </div>
                    </template>
                    <template #quantity="{ record }">
                      <a-input-number
                        :model-value="record.quantity"
                        :min="1"
                        :max="999"
                        size="small"
                        @change="(val) => updateQuantity(record.id, val)"
                      />
                    </template>
                    <template #price="{ record }">
                      <div style="font-size: 12px">
                        <div v-if="record.discount && record.discount > 0">
                          <div style="text-decoration: line-through; color: #999; margin-bottom: 2px">
                            {{ formatCurrency(record.price) }}
                          </div>
                          <div style="font-weight: 600; color: #f5222d; font-size: 14px">
                            {{ formatCurrency(record.price * (1 - record.discount / 100)) }}
                          </div>
                        </div>
                        <div v-else style="font-weight: 600; color: #f5222d; font-size: 14px">
                          {{ formatCurrency(record.price) }}
                        </div>
                      </div>
                    </template>
                    <template #subtotal="{ record }">
                      <strong>
                        {{
                          formatCurrency(
                            (record.discount > 0 ? record.price * (1 - record.discount / 100) : record.price) * record.quantity
                          )
                        }}
                      </strong>
                    </template>
                    <template #action="{ record }">
                      <a-button type="text" status="danger" size="small" @click="showDeleteProductConfirm(record)">
                        <template #icon>
                          <icon-delete />
                        </template>
                      </a-button>
                    </template>
                  </a-table>
                  <a-empty v-else description="Giỏ hàng trống" />
                </div>
              </a-card>
            </a-card>
          </div>
        </a-col>

        <!-- Right: Customer & Payment -->
        <a-col :xs="24" :lg="8" class="pos-right">
          <!-- Customer Section -->
          <a-card title="Thông Tin Khách Hàng" class="customer-card">
            <a-form :model="{}" layout="vertical">
              <a-form-item label="Chọn Khách Hàng">
                <a-select
                  :model-value="currentOrder?.customerId || ''"
                  allow-search
                  filterable
                  @update:model-value="updateCustomerId"
                  @change="handleCustomerChange"
                >
                  <a-option value="">Khách lẻ</a-option>
                  <a-option v-for="customer in filteredCustomers" :key="customer.id" :value="customer.id">
                    {{ customer.name }} ({{ customer.phone }})
                  </a-option>
                </a-select>
              </a-form-item>
              <a-form-item v-if="selectedCustomer && currentOrder">
                <a-descriptions size="small" :column="1" bordered>
                  <a-descriptions-item label="Tên">{{ selectedCustomer.name }}</a-descriptions-item>
                  <a-descriptions-item label="SĐT">{{ selectedCustomer.phone }}</a-descriptions-item>
                  <a-descriptions-item label="Email">{{ selectedCustomer.email || 'N/A' }}</a-descriptions-item>
                  <a-descriptions-item label="Địa Chỉ">{{ selectedCustomer.address || 'N/A' }}</a-descriptions-item>
                </a-descriptions>
              </a-form-item>
              <a-button v-if="!selectedCustomer" type="dashed" long @click="showAddCustomerModal = true">
                <template #icon>
                  <icon-plus />
                </template>
                Thêm Khách Hàng Mới
              </a-button>
            </a-form>
          </a-card>
          <!-- Payment Section -->
          <a-card class="payment-card">
            <template #title>
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span>Thanh Toán</span>
                <a-select v-model="orderType" placeholder="Loại đơn" style="width: 120px" @change="handleOrderTypeChange">
                  <a-option value="counter">Tại quầy</a-option>
                  <a-option value="delivery">Giao hàng</a-option>
                </a-select>
              </div>
            </template>
            <a-form :model="{}" layout="vertical">
              <!-- Discount Section - Button Style -->
              <a-form-item :model="{}">
                <a-button
                  long
                  size="large"
                  type="secondary"
                  :disabled="!hasEligibleVouchers"
                  style="
                    height: 56px;
                    font-size: 15px;
                    border: 1px solid #d9d9d9;
                    background: transparent;
                    color: #000;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    padding: 0 16px;
                  "
                  @click="showVoucherModal = true"
                >
                  <span style="font-weight: 500; text-align: left">Phiếu Giảm Giá</span>
                  <span style="font-weight: 400; font-size: 12px; text-align: right; color: #999">
                    {{ hasEligibleVouchers ? `${eligibleVouchersCount} voucher có thể dùng >` : 'Không có voucher phù hợp' }}
                  </span>
                </a-button>
              </a-form-item>

              <!-- Best voucher suggestion (when no voucher selected) -->
              <div
                v-if="!selectedCoupon && bestVoucher"
                style="
                  margin-bottom: 12px;
                  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
                  border: 1px solid #91d5ff;
                  border-radius: 8px;
                  padding: 16px;
                  margin-top: 12px;
                  cursor: pointer;
                  transition: all 0.3s ease;
                  display: block;
                  width: 100%;
                  box-sizing: border-box;
                "
                @click="selectVoucher(bestVoucher)"
                @mouseenter="(e) => (e.currentTarget.style.transform = 'translateY(-2px)')"
                @mouseleave="(e) => (e.currentTarget.style.transform = 'translateY(0)')"
              >
                <div style="display: flex; align-items: center; gap: 12px">
                  <div
                    style="
                      background: linear-gradient(135deg, #1890ff 0%, #0050b3 100%);
                      width: 40px;
                      height: 40px;
                      border-radius: 50%;
                      display: flex;
                      align-items: center;
                      justify-content: center;
                      box-shadow: 0 2px 6px rgba(24, 144, 255, 0.3);
                    "
                  >
                    <icon-gift style="font-size: 20px; color: #fff" />
                  </div>
                  <div style="flex: 1">
                    <div style="font-size: 14px; font-weight: 600; color: #0050b3; margin-bottom: 4px">Gợi ý voucher tốt nhất</div>
                    <div style="display: flex; align-items: center; gap: 8px">
                      <span
                        style="
                          background: #fff;
                          color: #1890ff;
                          padding: 4px 12px;
                          border-radius: 4px;
                          font-weight: 700;
                          font-size: 13px;
                          border: 1px dashed #1890ff;
                        "
                      >
                        {{ bestVoucher.maPhieuGiamGia }}
                      </span>
                      <span style="color: #666; font-size: 12px">Tiết kiệm</span>
                      <span style="color: #52c41a; font-weight: 700; font-size: 16px">
                        {{ formatCurrency(calculateVoucherDiscount(bestVoucher)) }}
                      </span>
                    </div>
                  </div>
                  <icon-right style="color: #1890ff; font-size: 20px" />
                </div>
              </div>

              <!-- Warning: Better voucher available - Redesigned -->
              <div
                v-if="hasBetterVoucher && bestVoucher && selectedCoupon"
                style="
                  background: linear-gradient(135deg, #fff7e6 0%, #fff3e0 100%);
                  border: 2px solid #ff9800;
                  border-radius: 12px;
                  padding: 20px;
                  margin-top: 12px;
                  display: block;
                  width: 100%;
                  box-sizing: border-box;
                  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.15);
                  position: relative;
                  overflow: hidden;
                "
              >
                <!-- Animated background pattern -->
                <div
                  style="
                    position: absolute;
                    top: -50%;
                    right: -10%;
                    width: 200px;
                    height: 200px;
                    background: radial-gradient(circle, rgba(255, 152, 0, 0.1) 0%, transparent 70%);
                    border-radius: 50%;
                    animation: pulse 3s ease-in-out infinite;
                  "
                ></div>

                <div style="position: relative; z-index: 1">
                  <!-- Header with icon -->
                  <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 16px">
                    <div
                      style="
                        background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
                        width: 48px;
                        height: 48px;
                        border-radius: 50%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        box-shadow: 0 2px 8px rgba(255, 152, 0, 0.3);
                      "
                    >
                      <icon-gift style="font-size: 24px; color: #fff" />
                    </div>
                    <div style="flex: 1">
                      <div style="font-size: 16px; font-weight: 700; color: #e65100; line-height: 1.3">Có voucher tiết kiệm hơn!</div>
                      <div style="font-size: 12px; color: #f57c00; margin-top: 2px">Đổi ngay để được giảm giá cao hơn</div>
                    </div>
                  </div>

                  <!-- Voucher comparison card -->
                  <div
                    style="
                      background: #fff;
                      border-radius: 10px;
                      padding: 16px;
                      margin-bottom: 16px;
                      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
                    "
                  >
                    <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px">
                      <div style="display: flex; align-items: center; gap: 10px">
                        <span
                          style="
                            background: #fff;
                            color: #ff9800;
                            padding: 8px 16px;
                            border-radius: 8px;
                            font-weight: 800;
                            font-size: 15px;
                            letter-spacing: 0.5px;
                            border: 2px dashed #ff9800;
                            box-shadow: 0 2px 4px rgba(255, 152, 0, 0.15);
                          "
                        >
                          {{ bestVoucher.maPhieuGiamGia }}
                        </span>
                      </div>
                      <div style="text-align: right">
                        <div style="font-size: 11px; color: #666; margin-bottom: 2px">Tiết kiệm thêm</div>
                        <div
                          style="
                            color: #2e7d32;
                            font-weight: 800;
                            font-size: 18px;
                            display: flex;
                            align-items: center;
                            justify-content: flex-end;
                            gap: 4px;
                          "
                        >
                          <span style="font-size: 16px">+</span>
                          {{ formatCurrency(calculateVoucherDiscount(bestVoucher) - calculateVoucherDiscount(selectedCoupon)) }}
                        </div>
                      </div>
                    </div>

                    <!-- Benefit indicator -->
                    <div
                      style="
                        background: linear-gradient(90deg, #e8f5e9 0%, #f1f8e9 100%);
                        border-left: 3px solid #4caf50;
                        padding: 10px 12px;
                        border-radius: 6px;
                      "
                    >
                      <div style="display: flex; align-items: center; gap: 8px">
                        <span style="font-size: 16px">✓</span>
                        <span style="font-size: 12px; color: #2e7d32; font-weight: 600">
                          Giảm giá tốt hơn
                          {{ Math.round((calculateVoucherDiscount(bestVoucher) / calculateVoucherDiscount(selectedCoupon) - 1) * 100) }}% so
                          với voucher hiện tại
                        </span>
                      </div>
                    </div>
                  </div>

                  <!-- CTA Button -->
                  <a-button
                    type="primary"
                    long
                    size="large"
                    style="
                      background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
                      border: none;
                      font-weight: 700;
                      height: 48px;
                      font-size: 15px;
                      box-shadow: 0 4px 12px rgba(255, 152, 0, 0.3);
                      transition: all 0.3s ease;
                    "
                    @click="selectVoucher(bestVoucher)"
                    @mouseenter="(e) => (e.currentTarget.style.transform = 'translateY(-2px)')"
                    @mouseleave="(e) => (e.currentTarget.style.transform = 'translateY(0)')"
                  >
                    <template #icon>
                      <icon-swap />
                    </template>
                    Chuyển ngay để tiết kiệm hơn
                  </a-button>
                </div>
              </div>

              <!-- Delivery Address & Shipping Fee (only for delivery orders) -->
              <div v-if="orderType === 'delivery'" style="margin-bottom: 16px">
                <!-- Show customer address if available -->
                <a-alert v-if="selectedCustomer?.address" type="info" style="margin-bottom: 12px">
                  <template #icon>
                    <icon-info-circle />
                  </template>
                  <div style="font-size: 12px">
                    <strong>Đơn giao hàng</strong>
                    <p style="margin: 4px 0 0 0; color: #666">Địa chỉ nhận hàng: {{ selectedCustomer.address }}</p>
                  </div>
                </a-alert>

                <!-- Location form for walk-in customers -->
                <div v-if="currentOrder?.customerId === '' && !selectedCustomer">
                  <a-divider orientation="left" style="margin: 12px 0">Địa chỉ giao hàng</a-divider>
                  <a-row :gutter="[12, 12]">
                    <a-col :span="12">
                      <a-form-item label="Tỉnh/Thành phố" required>
                        <a-select
                          v-model="walkInLocation.thanhPho"
                          placeholder="-- Chọn tỉnh/thành phố --"
                          :options="provinces"
                          @change="onWalkInProvinceChange"
                          option-label-prop="label"
                          allow-search
                          allow-clear
                        />
                      </a-form-item>
                    </a-col>
                    <a-col :span="12">
                      <a-form-item label="Quận/Huyện" required>
                        <a-select
                          v-model="walkInLocation.quan"
                          placeholder="-- Chọn quận/huyện --"
                          :options="walkInLocation.districts"
                          @change="onWalkInDistrictChange"
                          option-label-prop="label"
                          allow-search
                          allow-clear
                          :disabled="!walkInLocation.thanhPho"
                        />
                      </a-form-item>
                    </a-col>
                    <a-col :span="12">
                      <a-form-item label="Phường/Xã" required>
                        <a-select
                          v-model="walkInLocation.phuong"
                          placeholder="-- Chọn phường/xã --"
                          :options="walkInLocation.wards"
                          option-label-prop="label"
                          allow-search
                          allow-clear
                          :disabled="!walkInLocation.quan"
                        />
                      </a-form-item>
                    </a-col>
                    <a-col :span="12">
                      <a-form-item label="Địa chỉ cụ thể" required>
                        <a-input v-model="walkInLocation.diaChiCuThe" placeholder="Số nhà, đường..." />
                      </a-form-item>
                    </a-col>
                  </a-row>
                </div>

                <!-- Warning for registered customers without address -->
                <a-alert v-if="selectedCustomer && !selectedCustomer.address" type="warning" style="margin-bottom: 12px">
                  <template #icon>
                    <icon-exclamation-circle />
                  </template>
                  <div style="font-size: 12px">
                    <strong>⚠️ Khách hàng chưa có địa chỉ</strong>
                    <p style="margin: 4px 0 0 0; color: #666">Vui lòng chọn khách hàng khác hoặc chọn "Khách lẻ" để nhập địa chỉ</p>
                  </div>
                </a-alert>

                <a-form-item label="Phí Vận Chuyển" required>
                  <a-input-number
                    v-model:model-value="shippingFee"
                    :min="0"
                    placeholder="Nhập phí vận chuyển"
                    style="width: 100%; height: 48px; font-size: 16px; font-weight: 500"
                    :precision="0"
                    :formatter="(value) => formatCurrency(value || 0)"
                    :parser="(value) => parseFloat(value.replace(/[^\d]/g, '')) || 0"
                  />
                </a-form-item>
              </div>

              <!-- Payment Method -->
              <a-form-item :model="{}" label="Phương Thức Thanh Toán">
                <a-radio-group v-model="paymentForm.method" @change="handlePaymentMethodChange">
                  <a-radio value="cash">Tiền Mặt</a-radio>
                  <a-radio value="transfer">Chuyển Khoản</a-radio>
                  <a-radio value="both">Cả Hai</a-radio>
                </a-radio-group>
              </a-form-item>

              <!-- Cash Input -->
              <a-form-item
                :model="{}"
                v-if="paymentForm.method === 'cash' || paymentForm.method === 'both'"
                label="Tiền Mặt"
                class="cash-input-container"
                style="transition: all 0.3s ease"
              >
                <a-input-number
                  v-model:model-value="paymentForm.cashReceived"
                  :min="0"
                  :max="paymentForm.method === 'both' ? finalPrice : undefined"
                  placeholder="Nhập số tiền mặt"
                  style="width: 100%; height: 48px; font-size: 16px; font-weight: 500"
                  :precision="0"
                  :formatter="(value) => formatCurrency(value || 0)"
                  :parser="(value) => parseFloat(value.replace(/[^\d]/g, '')) || 0"
                  @update:model-value="(val) => handleCashAmountChange(val || 0)"
                />
              </a-form-item>

              <!-- Transfer Input -->
              <a-form-item
                :model="{}"
                v-if="paymentForm.method === 'transfer' || paymentForm.method === 'both'"
                label="Chuyển Khoản"
                class="transfer-input-container"
                style="transition: all 0.3s ease"
              >
                <a-input-number
                  v-model:model-value="paymentForm.transferReceived"
                  :min="0"
                  :max="paymentForm.method === 'both' ? finalPrice : undefined"
                  placeholder="Nhập số tiền chuyển khoản"
                  style="width: 100%; height: 48px; font-size: 16px; font-weight: 500"
                  :precision="0"
                  :formatter="(value) => formatCurrency(value || 0)"
                  :parser="(value) => parseFloat(value.replace(/[^\d]/g, '')) || 0"
                  @update:model-value="(val) => handleTransferAmountChange(val || 0)"
                />
              </a-form-item>

              <!-- Transfer Notes -->
              <a-alert v-if="paymentForm.method === 'transfer' || paymentForm.method === 'both'" type="info" title="Chuyển Khoản" closable>
                <p>Vui lòng chuyển khoản theo thông tin cung cấp. Mã hoá đơn: {{ currentOrder?.orderCode }}</p>
                <p v-if="paymentForm.method === 'both'">Số tiền chuyển khoản: {{ formatCurrency(paymentForm.transferReceived || 0) }}</p>
              </a-alert>

              <!-- Selected Voucher Info -->
              <a-alert
                v-if="selectedCoupon"
                :title="`Voucher: ${selectedCoupon.tenPhieuGiamGia}`"
                type="success"
                closable
                @close="clearVoucher"
              >
                <div style="display: flex; justify-content: space-between; align-items: center">
                  <div>
                    <strong>{{ selectedCoupon.maPhieuGiamGia }}</strong>
                    <span style="margin-left: 8px; color: #52c41a">-{{ getDiscountDisplay(selectedCoupon) }}</span>
                  </div>
                  <div style="font-size: 12px; color: #666">
                    <span v-if="selectedCoupon.hoaDonToiThieu">Min: {{ formatCurrency(Number(selectedCoupon.hoaDonToiThieu)) }}</span>
                  </div>
                </div>
              </a-alert>

              <!-- Price Summary -->
              <a-divider />
              <div class="payment-summary">
                <p class="summary-row">
                  <span>Tổng tiền:</span>
                  <strong>{{ formatCurrency(subtotal) }}</strong>
                </p>
                <p class="summary-row">
                  <span>Giảm giá:</span>
                  <span :class="discountAmount > 0 ? 'discount-text' : ''">
                    {{ discountAmount > 0 ? '-' : '' }}{{ formatCurrency(discountAmount) }}
                  </span>
                </p>
                <p v-if="orderType === 'delivery'" class="summary-row">
                  <span>Phí vận chuyển:</span>
                  <strong style="color: #1890ff">{{ formatCurrency(shippingFee) }}</strong>
                </p>
                <p class="summary-row total">
                  <span>Thành tiền:</span>
                  <strong class="final-price">{{ formatCurrency(finalPrice) }}</strong>
                </p>
              </div>

              <!-- Action Buttons -->
              <a-space direction="vertical" size="large" style="width: 100%; margin-top: 16px">
                <a-button type="primary" long size="large" :disabled="!canConfirmOrder" :loading="confirmLoading" @click="confirmOrder">
                  <template #icon>
                    <icon-check />
                  </template>
                  Xác Nhận ({{ finalPrice > 0 ? formatCurrency(finalPrice) : '0đ' }})
                </a-button>
                <a-button long @click="printOrder" :disabled="!currentOrder?.items.length">In Hoá Đơn</a-button>
              </a-space>
            </a-form>
          </a-card>
        </a-col>
      </a-row>
    </a-card>

    <!-- Modals -->
    <!-- Product Selection Modal -->
    <a-modal v-model:visible="showProductModal" title="Chọn Sản Phẩm" width="90%" :footer="null" @cancel="handleProductModalCancel">
      <div style="margin-bottom: 16px">
        <a-input-search
          v-model="productSearchText"
          placeholder="Tìm kiếm sản phẩm (tên, mã, màu, kích thước...)"
          allow-clear
          style="width: 100%; margin-bottom: 12px"
        />
        <a-row :gutter="[12, 12]">
          <a-col :xs="24" :sm="12" :md="8" :lg="4">
            <a-select
              v-model="productFilters.tenChatLieu"
              placeholder="Chất liệu"
              allow-clear
              :options="productMaterialOptions"
              size="small"
            />
          </a-col>
          <a-col :xs="24" :sm="12" :md="8" :lg="4">
            <a-select v-model="productFilters.tenDeGiay" placeholder="Đế" allow-clear :options="productSoleOptions" size="small" />
          </a-col>
          <a-col :xs="24" :sm="12" :md="8" :lg="4">
            <a-select
              v-model="productFilters.tenNhaSanXuat"
              placeholder="NSX"
              allow-clear
              :options="productManufacturerOptions"
              size="small"
            />
          </a-col>
          <a-col :xs="24" :sm="12" :md="8" :lg="4">
            <a-select v-model="productFilters.tenXuatXu" placeholder="Xuất xứ" allow-clear :options="productOriginOptions" size="small" />
          </a-col>
          <a-col :xs="24" :sm="12" :md="8" :lg="4">
            <a-select v-model="productFilters.tenMauSac" placeholder="Màu Sắc" allow-clear :options="productColorOptions" size="small" />
          </a-col>
          <a-col :xs="24" :sm="12" :md="8" :lg="4">
            <a-select
              v-model="productFilters.tenKichThuoc"
              placeholder="Kích Thước"
              allow-clear
              :options="productSizeOptions"
              size="small"
            />
          </a-col>
        </a-row>
      </div>
      <a-table
        :key="`products-${productPagination.current}`"
        v-if="filteredProductVariants.length > 0"
        :columns="[
          { title: 'STT', dataIndex: 'stt', key: 'stt', width: 50 },
          { title: 'Sản Phẩm', dataIndex: 'product', key: 'product', width: 200, slotName: 'product' },
          { title: 'Thông Tin', dataIndex: 'info', key: 'info', width: 200, slotName: 'info' },
          { title: 'Màu Sắc | Kích Thước', dataIndex: 'variant', key: 'variant', width: 150, slotName: 'variant' },
          { title: 'Giá Bán', dataIndex: 'price', key: 'price', width: 150, slotName: 'price' },
          { title: 'Số Lượng', dataIndex: 'soLuong', key: 'soLuong', width: 80 },
          { title: 'Thao Tác', key: 'action', width: 80, slotName: 'action' },
        ]"
        :data="
          filteredProductVariants.map((p, idx) => ({
            ...p,
            stt: (productPagination.current - 1) * productPagination.pageSize + idx + 1,
          }))
        "
        size="small"
        :scroll="{ x: '100%' }"
        :pagination="{
          current: productPagination.current,
          pageSize: productPagination.pageSize,
          total: productPagination.total,
          showTotal: true,
        }"
        :page-position="'bottomCenter'"
        @page-change="
          (page) => {
            loadProductPage(page)
          }
        "
      >
        <template #product="{ record, rowIndex }">
          <div style="display: flex; gap: 8px; align-items: center">
            <img
              v-if="record.anhSanPham?.[0]"
              :src="record.anhSanPham[0]"
              style="width: 50px; height: 50px; object-fit: cover; border-radius: 4px"
              :alt="record.tenSanPham"
            />
            <div>
              <div style="font-weight: 600; font-size: 13px">{{ record.tenSanPham }}</div>
              <div style="font-size: 11px; color: #999">Mã: {{ record.maChiTietSanPham }}</div>
            </div>
          </div>
        </template>
        <template #info="{ record }">
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px; font-size: 11px">
            <div>
              <div v-if="record.tenChatLieu" style="margin-bottom: 4px">
                <span style="color: #999">Chất liệu:</span>
                <strong>{{ ' ' + record.tenChatLieu }}</strong>
              </div>
              <div v-if="record.tenDeGiay">
                <span style="color: #999">Đế:</span>
                <strong>{{ ' ' + record.tenDeGiay }}</strong>
              </div>
            </div>
            <div>
              <div v-if="record.tenNhaSanXuat" style="margin-bottom: 4px">
                <span style="color: #999">NSX:</span>
                <strong>{{ ' ' + record.tenNhaSanXuat }}</strong>
              </div>
              <div v-if="record.tenXuatXu">
                <span style="color: #999">Xuất xứ:</span>
                <strong>{{ ' ' + record.tenXuatXu }}</strong>
              </div>
            </div>
          </div>
        </template>
        <template #variant="{ record }">
          <div style="font-size: 12px; display: flex; align-items: center; gap: 12px">
            <!-- Màu sắc -->
            <div v-if="record.tenMauSac" style="display: flex; align-items: center; gap: 6px">
              <div
                v-if="record.maMau"
                style="width: 20px; height: 20px; border-radius: 3px; border: 1px solid #e5e5e5; background-color: #ffffff; flex-shrink: 0"
                :style="{ backgroundColor: record.maMau }"
                :title="record.maMau"
              />
              <div>
                <div style="font-weight: 600; line-height: 1">{{ record.tenMauSac }}</div>
                <div v-if="record.maMau" style="font-size: 10px; color: #999; line-height: 1">{{ record.maMau }}</div>
              </div>
            </div>
            <!-- Dấu phân cách -->
            <span v-if="record.tenMauSac && record.tenKichThuoc" style="color: #d9d9d9">|</span>
            <!-- Kích thước -->
            <div v-if="record.tenKichThuoc" style="font-weight: 600">
              {{ record.tenKichThuoc }}
            </div>
          </div>
        </template>

        <template #price="{ record }">
          <div style="font-size: 12px">
            <div v-if="record.giaTriGiamGia && record.giaTriGiamGia > 0">
              <div style="text-decoration: line-through; color: #999; margin-bottom: 2px">
                {{ formatCurrency(record.giaBan) }}
              </div>
              <div style="font-weight: 600; color: #f5222d; font-size: 14px">
                {{ formatCurrency(record.giaBan * (1 - record.giaTriGiamGia / 100)) }}
              </div>
            </div>
            <div v-else style="font-weight: 600; color: #f5222d; font-size: 14px">
              {{ formatCurrency(record.giaBan) }}
            </div>
          </div>
        </template>

        <template #action="{ record }">
          <a-button type="primary" size="small" @click="showAddProductConfirm(record)">Chọn</a-button>
        </template>
      </a-table>
      <a-empty v-else description="Không có sản phẩm" />
    </a-modal>

    <!-- QR Scanner Modal -->
    <a-modal
      v-model:visible="showQRScanner"
      title="Quét Mã QR Sản Phẩm"
      width="600px"
      :footer="null"
      @cancel="closeQRScanner"
      @open="initQRScanner"
    >
      <div style="text-align: center; padding: 20px">
        <!-- QR Scanner Container -->
        <div id="qr-reader" style="width: 100%; max-width: 550px; margin: 0 auto"></div>
      </div>
    </a-modal>

    <!-- Add Customer Modal -->
    <a-modal
      v-model:visible="showAddCustomerModal"
      title="Thêm Khách Hàng Mới"
      width="500px"
      ok-text="Thêm"
      cancel-text="Hủy"
      @ok="addNewCustomer"
    >
      <a-form v-if="newCustomerForm.value" :model="newCustomerForm.value" layout="vertical">
        <a-form-item label="Tên Khách Hàng" required>
          <a-input
            :model-value="newCustomerForm.value?.name || ''"
            placeholder="Nhập tên khách hàng"
            @update:model-value="(val) => newCustomerForm.value && (newCustomerForm.value.name = val)"
          />
        </a-form-item>

        <a-form-item label="Số Điện Thoại" required>
          <a-input
            :model-value="newCustomerForm.value?.phone || ''"
            placeholder="Nhập số điện thoại"
            @update:model-value="(val) => newCustomerForm.value && (newCustomerForm.value.phone = val)"
          />
        </a-form-item>

        <a-form-item label="Email">
          <a-input
            :model-value="newCustomerForm.value?.email || ''"
            placeholder="Nhập email"
            type="email"
            @update:model-value="(val) => newCustomerForm.value && (newCustomerForm.value.email = val)"
          />
        </a-form-item>

        <a-form-item label="Địa Chỉ">
          <a-textarea
            :model-value="newCustomerForm.value?.address || ''"
            placeholder="Nhập địa chỉ"
            :auto-size="{ minRows: 2, maxRows: 4 }"
            @update:model-value="(val) => newCustomerForm.value && (newCustomerForm.value.address = val)"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Voucher Selection Modal -->
    <a-modal v-model:visible="showVoucherModal" title="Chọn Phiếu Giảm Giá" width="800px" :footer="null" @cancel="showVoucherModal = false">
      <div style="max-height: 600px; overflow-y: auto">
        <a-empty v-if="coupons.length === 0" description="Không có phiếu giảm giá" />

        <!-- Current order summary for condition checking -->
        <div
          v-if="currentOrder && currentOrder.items.length > 0"
          style="margin-bottom: 16px; padding: 12px; background: #f0f9ff; border-radius: 6px; border: 1px solid #e5e5e5"
        >
          <div style="font-size: 12px; color: #666; margin-bottom: 8px">Đơn hàng hiện tại:</div>
          <div style="font-weight: 600; color: #1890ff">
            Tổng tiền: {{ formatCurrency(subtotal) }} | Số lượng: {{ currentOrder.items.reduce((sum, item) => sum + item.quantity, 0) }} sản
            phẩm
          </div>
        </div>

        <div v-else style="margin-bottom: 16px; padding: 12px; background: #fff7e6; border-radius: 6px; border: 1px solid #ffd591">
          <div style="font-size: 12px; color: #d48806">⚠️ Chưa có sản phẩm nào trong giỏ hàng</div>
        </div>

        <div v-if="coupons.length > 0">
          <!-- Eligible Vouchers Count -->
          <div style="margin-bottom: 12px; font-size: 14px; color: #666">
            {{ eligibleVouchersCount }}/{{ coupons.length }} voucher có thể sử dụng
          </div>

          <!-- Voucher List - 1 voucher per row -->
          <div
            v-for="coupon in coupons"
            :key="coupon.id"
            style="border: 1px solid #e5e5e5; border-radius: 8px; margin-bottom: 12px; overflow: hidden"
          >
            <div
              style="
                display: flex;
                align-items: center;
                padding: 16px;
                background: #fafafa;
                border-bottom: 1px solid #e5e5e5;
                cursor: pointer;
                transition: all 0.3s;
              "
              :class="{ 'voucher-disabled': !isVoucherEligible(coupon) }"
              @click="isVoucherEligible(coupon) ? selectVoucher(coupon) : null"
              @mouseenter="
                (e) => {
                  if (isVoucherEligible(coupon)) {
                    e.currentTarget.style.background = '#f0f9ff'
                  }
                }
              "
              @mouseleave="
                (e) => {
                  e.currentTarget.style.background = '#fafafa'
                }
              "
            >
              <!-- Left: Voucher Info -->
              <div style="flex: 1">
                <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 8px">
                  <div style="font-size: 16px; font-weight: 600; color: #333">
                    {{ coupon.maPhieuGiamGia }}
                  </div>
                  <a-tag v-if="bestVoucher && coupon.id === bestVoucher.id" color="gold" size="small">⭐ Tốt nhất</a-tag>
                  <a-tag v-if="!isVoucherEligible(coupon)" color="red" size="small">
                    {{ getVoucherStatus(coupon) }}
                  </a-tag>
                  <a-tag v-else-if="coupon.soLuongDung <= 0" color="orange" size="small">Hết lượt sử dụng</a-tag>
                  <a-tag v-else color="green" size="small">Có thể sử dụng</a-tag>
                </div>

                <div style="font-size: 12px; color: #86909c; line-height: 1.4">
                  {{ coupon.tenPhieuGiamGia }}
                </div>

                <!-- Conditions -->
                <div style="margin-top: 8px; font-size: 11px; color: #666">
                  <span v-if="!isVoucherEligible(coupon)" style="color: #ff4d4f">❌ {{ getVoucherStatus(coupon) }}</span>
                  <span v-else>
                    <span>💰 {{ getDiscountDisplay(coupon) }} giảm giá</span>
                    <span v-if="coupon.hoaDonToiThieu" style="margin-left: 12px">
                      Min: {{ formatCurrency(Number(coupon.hoaDonToiThieu)) }}
                    </span>
                    <span v-if="coupon.soLuongDung" style="margin-left: 12px">📊 Còn: {{ coupon.soLuongDung }} lượt</span>
                    <span v-if="coupon.ngayKetThuc" style="margin-left: 12px">⏰ Hết hạn: {{ coupon.ngayKetThuc }}</span>
                  </span>
                </div>
              </div>

              <!-- Right: Discount Value -->
              <div style="text-align: center; margin-left: 16px">
                <div
                  style="
                    background: linear-gradient(135deg, #f5222d 0%, #ff4d4f 100%);
                    color: white;
                    padding: 8px 16px;
                    border-radius: 6px;
                    font-size: 14px;
                    font-weight: 600;
                  "
                >
                  {{ getDiscountDisplay(coupon) }}
                </div>
              </div>

              <!-- Action Button -->
              <div style="margin-left: 16px">
                <a-button
                  type="primary"
                  size="small"
                  :disabled="!isVoucherEligible(coupon)"
                  @click.stop="selectVoucher(coupon)"
                  style="background: linear-gradient(135deg, #f5222d 0%, #ff4d4f 100%); border: none"
                >
                  {{ isVoucherEligible(coupon) ? 'Chọn' : 'Không đủ ĐK' }}
                </a-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </a-modal>

    <!-- Delete Product Confirm Modal -->
    <a-modal
      v-model:visible="showDeleteProductModal"
      title="Xóa Sản Phẩm Khỏi Giỏ Hàng"
      width="500px"
      @ok="confirmDeleteProduct"
      @cancel="showDeleteProductModal = false"
      ok-text="Xóa"
      cancel-text="Hủy"
    >
      <div v-if="productToDelete">
        <!-- Product Info -->
        <div style="display: flex; gap: 12px; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid #e5e5e5">
          <img
            v-if="productToDelete.image"
            :src="productToDelete.image"
            style="width: 60px; height: 60px; object-fit: cover; border-radius: 4px"
            :alt="productToDelete.productName"
          />
          <div style="flex: 1">
            <div style="font-weight: 600; font-size: 14px; margin-bottom: 4px">{{ getProductDisplayName(productToDelete) }}</div>
            <div style="font-size: 12px; color: #999; margin-bottom: 8px">Số lượng: {{ productToDelete.quantity }}</div>
            <div style="font-size: 14px; font-weight: 600; color: #f5222d">
              {{ formatCurrency(productToDelete.price * productToDelete.quantity) }}
            </div>
          </div>
        </div>

        <!-- Warning -->
        <div style="text-align: center; padding: 16px; background: #fff7e6; border-radius: 6px; margin-bottom: 16px">
          <a-result status="warning" title="Xác Nhận Xóa Sản Phẩm?" style="margin: 0; padding: 0" />
          <p style="margin: 8px 0 0 0; color: #d48806; font-size: 12px">
            Sản phẩm sẽ được xóa khỏi giỏ hàng và số lượng sẽ được hoàn lại vào kho.
          </p>
        </div>
      </div>
    </a-modal>

    <!-- Delete Order Confirm Modal -->
    <a-modal
      v-model:visible="showDeleteConfirmModal"
      title="Xoá Đơn Hàng"
      width="400px"
      @ok="confirmDeleteOrder"
      @cancel="showDeleteConfirmModal = false"
      ok-text="Xoá"
      cancel-text="Hủy"
    >
      <div style="text-align: center; padding: 0">
        <a-result status="warning" title="Xác Nhận Xoá Đơn Hàng?" style="margin: 0; padding: 0" />
        <p style="margin: 4px 0 0 0; color: #666; font-size: 13px">Bạn có chắc muốn xoá đơn hàng này? Hành động này không thể hoàn tác.</p>
      </div>
    </a-modal>

    <!-- Add Product Confirm Modal -->
    <a-modal
      v-model:visible="showAddProductConfirmModal"
      title="Thêm Sản Phẩm"
      width="500px"
      @ok="confirmAddProduct"
      @cancel="showAddProductConfirmModal = false"
      ok-text="Thêm"
      cancel-text="Hủy"
      :ok-button-props="{ disabled: !isQuantityValid || confirmLoading, loading: confirmLoading }"
      :cancel-button-props="{ disabled: confirmLoading }"
    >
      <div v-if="selectedProductForAdd">
        <!-- Product Info -->
        <div style="display: flex; gap: 12px; margin-bottom: 12px; padding-bottom: 8px; border-bottom: 1px solid #e5e5e5">
          <img
            v-if="selectedProductForAdd.anhSanPham?.[0]"
            :src="selectedProductForAdd.anhSanPham[0]"
            style="width: 80px; height: 80px; object-fit: cover; border-radius: 4px"
          />
          <div style="flex: 1">
            <div style="font-weight: 600; font-size: 14px; margin-bottom: 4px">{{ selectedProductForAdd.tenSanPham }}</div>
            <div style="font-size: 12px; color: #999; margin-bottom: 12px">Mã: {{ selectedProductForAdd.maChiTietSanPham }}</div>

            <!-- Thông tin chi tiết - 2 cột -->
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 12px; font-size: 11px">
              <div>
                <div v-if="selectedProductForAdd.tenChatLieu" style="margin-bottom: 6px">
                  <div style="color: #999; font-size: 10px">Chất liệu</div>
                  <div style="font-weight: 500">{{ selectedProductForAdd.tenChatLieu }}</div>
                </div>
                <div v-if="selectedProductForAdd.tenDeGiay">
                  <div style="color: #999; font-size: 10px">Đế</div>
                  <div style="font-weight: 500">{{ selectedProductForAdd.tenDeGiay }}</div>
                </div>
              </div>
              <div>
                <div v-if="selectedProductForAdd.tenNhaSanXuat" style="margin-bottom: 6px">
                  <div style="color: #999; font-size: 10px">Nhà sản xuất</div>
                  <div style="font-weight: 500">{{ selectedProductForAdd.tenNhaSanXuat }}</div>
                </div>
                <div v-if="selectedProductForAdd.tenXuatXu">
                  <div style="color: #999; font-size: 10px">Xuất xứ</div>
                  <div style="font-weight: 500">{{ selectedProductForAdd.tenXuatXu }}</div>
                </div>
              </div>
            </div>

            <!-- Màu và kích thước -->
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 12px; font-size: 11px">
              <div v-if="selectedProductForAdd.tenMauSac">
                <div style="color: #999; font-size: 10px">Màu sắc</div>
                <div style="display: flex; align-items: center; gap: 8px; margin-top: 4px">
                  <div
                    v-if="selectedProductForAdd.maMau"
                    style="width: 24px; height: 24px; border-radius: 4px; border: 1px solid #e5e5e5; background-color: #ffffff"
                    :style="{ backgroundColor: selectedProductForAdd.maMau }"
                    :title="selectedProductForAdd.maMau"
                  />
                  <div>
                    <div style="font-weight: 600">{{ selectedProductForAdd.tenMauSac }}</div>
                    <div v-if="selectedProductForAdd.maMau" style="color: #999; font-size: 10px">
                      {{ selectedProductForAdd.maMau }}
                    </div>
                  </div>
                </div>
              </div>
              <div v-if="selectedProductForAdd.tenKichThuoc">
                <div style="color: #999; font-size: 10px">Kích thước</div>
                <div style="font-weight: 600; margin-top: 4px">{{ selectedProductForAdd.tenKichThuoc }}</div>
              </div>
            </div>

            <!-- Giá bán -->
            <div style="color: #999; font-size: 10px; margin-bottom: 6px">Giá bán</div>
            <div v-if="selectedProductForAdd.giaTriGiamGia && selectedProductForAdd.giaTriGiamGia > 0" style="margin-bottom: 0">
              <div style="text-decoration: line-through; color: #999; font-size: 12px; margin-bottom: 2px">
                {{ formatCurrency(selectedProductForAdd.giaBan) }}
              </div>
              <div style="font-weight: 600; color: #f5222d; font-size: 14px">
                {{ formatCurrency(selectedProductForAdd.giaBan * (1 - selectedProductForAdd.giaTriGiamGia / 100)) }}
              </div>
            </div>
            <div v-else style="font-weight: 600; color: #f5222d; font-size: 14px">
              {{ formatCurrency(selectedProductForAdd.giaBan) }}
            </div>
          </div>
        </div>

        <!-- Quantity Input -->
        <div style="margin-bottom: 0">
          <label style="display: block; margin-bottom: 6px; font-weight: 600; font-size: 13px">
            Số Lượng (Tồn kho: {{ selectedProductForAdd.soLuong || 0 }})
          </label>
          <a-input-number
            ref="quantityInputRef"
            :model-value="productQuantityInput"
            style="width: 100%; margin-bottom: 12px"
            placeholder="Nhập số lượng"
            @update:model-value="handleQuantityChange"
          />

          <!-- Alert if quantity exceeds stock -->
          <a-alert v-if="productQuantityInput > (selectedProductForAdd.soLuong || 0)" type="error" closable>
            <template #title>❌ Tồn kho không đủ</template>
            <div style="font-size: 12px">
              Yêu cầu: {{ productQuantityInput }} cái | Tồn kho: {{ selectedProductForAdd.soLuong || 0 }} cái
            </div>
          </a-alert>
        </div>
      </div>
    </a-modal>

    <!-- Confirm Order - Better Voucher Modal -->
    <a-modal
      v-model:visible="showConfirmOrderModal"
      title="⚠️ Có Phiếu Giảm Giá Tốt Hơn"
      width="600px"
      :footer="null"
      @cancel="cancelConfirmOrder"
    >
      <div style="padding: 20px 0">
        <!-- Warning Message -->
        <div style="margin-bottom: 24px; padding: 12px 16px; background: #fff7e6; border-left: 4px solid #faad14; border-radius: 4px">
          <div style="color: #faad14; font-weight: 600; margin-bottom: 8px">💡 Gợi ý</div>
          <div style="color: #666; line-height: 1.6">
            Chúng tôi tìm thấy phiếu giảm giá với mức giảm cao hơn. Bạn có muốn quay lại để chọn phiếu tốt nhất không?
          </div>
        </div>

        <!-- Suggested Vouchers List -->
        <div v-if="suggestedBetterVouchers.length > 0" style="margin-bottom: 24px">
          <div style="font-weight: 600; margin-bottom: 12px; color: #262626">Phiếu giảm giá tốt hơn:</div>

          <div
            v-for="(voucher, idx) in suggestedBetterVouchers"
            :key="idx"
            style="
              padding: 12px;
              margin-bottom: 8px;
              border: 1px solid #e5e5e5;
              border-radius: 6px;
              background: #fafafa;
              display: flex;
              justify-content: space-between;
              align-items: center;
            "
          >
            <div>
              <div style="font-weight: 600; color: #262626">{{ voucher.tenPhieuGiamGia }}</div>
              <div style="font-size: 12px; color: #999; margin-top: 4px">
                Còn: {{ voucher.soLuongDung }} lượt | Min: {{ formatCurrency(voucher.hoaDonToiThieu) }}
              </div>
            </div>
            <div style="text-align: right">
              <div style="font-size: 16px; font-weight: 700; color: #52c41a">-{{ formatCurrency(calculateVoucherDiscount(voucher)) }}</div>
            </div>
          </div>
        </div>

        <!-- Current Selected Voucher Info -->
        <div v-if="selectedCoupon" style="margin-bottom: 24px; padding: 12px; background: #f0f0f0; border-radius: 6px">
          <div style="font-size: 12px; color: #999; margin-bottom: 4px">Phiếu đang chọn:</div>
          <div style="font-weight: 600; color: #262626">{{ selectedCoupon.tenPhieuGiamGia }}</div>
          <div style="font-size: 14px; color: #666; margin-top: 4px">-{{ formatCurrency(calculateVoucherDiscount(selectedCoupon)) }}</div>
        </div>
        <div v-else style="margin-bottom: 24px; padding: 12px; background: #f0f0f0; border-radius: 6px">
          <div style="font-size: 12px; color: #999">Không có phiếu giảm giá</div>
        </div>

        <!-- Action Buttons -->
        <div style="display: flex; gap: 12px; justify-content: flex-end">
          <a-button type="default" @click="cancelConfirmOrder">Quay lại</a-button>
          <a-button type="primary" :loading="confirmLoading" @click="doConfirmOrder">Bỏ qua và thanh toán</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import {
  IconPlus,
  IconClose,
  IconDelete,
  IconQrcode,
  IconCheck,
  IconInfoCircle,
  IconExclamationCircle,
  IconRight,
  IconSwap,
  IconGift,
} from '@arco-design/web-vue/es/icon'
import {
  getBienTheSanPhamPage,
  getChatLieuOptions,
  getDeGiayOptions,
  getMauSacOptions,
  getKichThuocOptions,
  type BienTheSanPham,
  type ChatLieu,
  type DeGiay,
  type MauSac,
  type KichThuoc,
} from '@/api/san-pham/bien-the'
import { layDanhSachKhachHang, type KhachHangResponse } from '@/api/khach-hang'
import { type CouponApiModel } from '@/api/discount-management'
import {
  createInvoice,
  deleteInvoice,
  addProductToInvoice,
  updateProductQuantityInInvoice,
  deleteProductsFromInvoice,
  updateCustomerForInvoice,
  updateShippingMethod,
  updatePaymentMethod,
  updateVoucher,
  confirmPosOrder,
  getPosActiveCoupons,
  getPosActiveCouponsForCustomer,
  type PhieuGiamGiaResponse,
  type UpdateCustomerRequest,
  type UpdatePaymentMethodRequest,
  type UpdateVoucherRequest,
  type ConfirmBanHangRequest,
} from '@/api/pos'
import { Message, Modal } from '@arco-design/web-vue'
import { Html5Qrcode, Html5QrcodeSupportedFormats, Html5QrcodeScannerState } from 'html5-qrcode'
import { useUserStore } from '@/store'
// ==================== TYPES ====================
interface CartItem {
  id: string
  idHoaDonChiTiets?: number[] // Server-side IDs for all invoice detail entries (array because each add creates new entry)
  productId: string
  productName: string
  price: number
  discount: number
  quantity: number
  image?: string
  // Thông tin chi tiết sản phẩm
  tenChiTietSanPham?: string
  tenMauSac?: string
  maMau?: string
  tenKichThuoc?: string
  tenDeGiay?: string
  tenChatLieu?: string
}

interface Order {
  id: string
  orderCode: string
  items: CartItem[]
  customerId: string | null
  createdAt: Date
}

interface Customer {
  id: string
  name: string
  phone: string
  email?: string
  address?: string
}

// ==================== STATE ====================
const userStoreInstance = useUserStore()
const orders = ref<Order[]>([])
const currentOrderIndex = ref('0')
const customers = ref<Customer[]>([])
const productVariants = ref<BienTheSanPham[]>([])
const allProductVariants = ref<BienTheSanPham[]>([])
const coupons = ref<CouponApiModel[]>([])
// Track số lượng đã bán của mỗi sản phẩm (để tính toán lại tồn kho khi reload)
const soldQuantitiesByProductId = ref<Record<string | number, number>>({})

// Cache dữ liệu để chỉ reload khi có thay đổi
const cachedProducts = ref<Map<number, number>>(new Map()) // productId -> soLuong
const cachedCoupons = ref<string>('') // JSON string of coupon data for comparison

const customerSearchText = ref('')
const productSearchText = ref('')
const productFilters = ref({
  tenChatLieu: null as string | null,
  tenDeGiay: null as string | null,
  tenNhaSanXuat: null as string | null,
  tenXuatXu: null as string | null,
  tenMauSac: null as string | null,
  tenKichThuoc: null as string | null,
})
const filterOptionsData = ref({
  chatLieu: [] as ChatLieu[],
  deGiay: [] as DeGiay[],
  mauSac: [] as MauSac[],
  kichThuoc: [] as KichThuoc[],
})
const showProductModal = ref(false)
const showQRScanner = ref(false)
const showAddCustomerModal = ref(false)
const showVoucherModal = ref(false)
const showDeleteConfirmModal = ref(false)
const showAddProductConfirmModal = ref(false)
const showDeleteProductModal = ref(false)

// Confirm order modal state
const showConfirmOrderModal = ref(false)
const suggestedBetterVouchers = ref<CouponApiModel[]>([])
const confirmOrderRequest = ref<any>(null)

// Throttle state for API calls to avoid excessive requests
let lastVoucherRefreshTime = 0
let lastStockRefreshTime = 0
const VOUCHER_THROTTLE_MS = 2000 // 2 seconds throttle between voucher refresh calls
const STOCK_THROTTLE_MS = 1000 // 1 second throttle between stock refresh calls

// QR Scanner state
const qrScannerInstance = ref<Html5Qrcode | null>(null)
const deleteConfirmOrderIndex = ref<number | null>(null)
const selectedProductForAdd = ref<BienTheSanPham | null>(null)
const productToDelete = ref<CartItem | null>(null)
const productQuantityInput = ref(1)
const quantityInputRef = ref<any>(null)
const confirmLoading = ref(false)
const loadingData = ref(false)

// Broadcast channel for real-time sync between tabs/windows
let stockBroadcastChannel: BroadcastChannel | null = null

const productPagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
})

const voucherPagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
})

const paymentForm = ref({
  discountCode: null as string | null,
  method: 'cash' as 'cash' | 'transfer' | 'both',
  cashReceived: 0,
  transferReceived: 0,
})

const orderType = ref('counter')
const shippingFee = ref(0)

const newCustomerForm = ref({
  name: '',
  phone: '',
  email: '',
  address: '',
})

// Location data for walk-in customers
const provinces = ref<{ value: string; label: string; code: number }[]>([])
const walkInLocation = ref({
  thanhPho: '',
  quan: '',
  phuong: '',
  diaChiCuThe: '',
  districts: [] as { value: string; label: string; code: number }[],
  wards: [] as { value: string; label: string }[],
})

const cartPagination = ref({
  current: 1,
  pageSize: 5,
})

// Force re-render key cho cart table khi có lỗi cập nhật quantity
const cartTableKey = ref(0)

const breadcrumbItems = []

// ==================== COMPUTED ====================
const currentOrder = computed(() => {
  const idx = parseInt(currentOrderIndex.value, 10)
  return orders.value[idx] || null
})

const filteredCustomers = computed(() => {
  if (!customerSearchText.value) return customers.value
  const query = customerSearchText.value.toLowerCase()
  return customers.value.filter((c) => c.name.toLowerCase().includes(query) || c.phone.toLowerCase().includes(query))
})

const selectedCustomer = computed(() => {
  if (!currentOrder.value?.customerId) return null
  return customers.value.find((c) => c.id === currentOrder.value?.customerId)
})

const selectedCoupon = computed(() => {
  if (!paymentForm.value?.discountCode) return null
  const coupon = coupons.value.find((c) => c.maPhieuGiamGia === paymentForm.value?.discountCode)
  return coupon
})

const eligibleVouchersCount = computed(() => {
  return coupons.value.filter((coupon) => isVoucherEligible(coupon)).length
})

const hasEligibleVouchers = computed(() => {
  return eligibleVouchersCount.value > 0
})

// Find the best voucher based on discount amount
const bestVoucher = computed(() => {
  // ONLY suggest vouchers that are TRULY USABLE right now
  const eligible = coupons.value.filter((coupon) => isVoucherEligible(coupon))

  if (eligible.length === 0) return null

  // If best voucher is already selected, skip it
  if (selectedCoupon.value && eligible.length > 1) {
    const filtered = eligible.filter((c) => c.id !== selectedCoupon.value?.id)
    if (filtered.length === 0) return null
  }

  // Calculate actual discount amount for each eligible voucher
  let bestCoupon = eligible[0]
  let maxDiscount = calculateVoucherDiscount(bestCoupon)

  for (const coupon of eligible) {
    const discount = calculateVoucherDiscount(coupon)
    if (discount > maxDiscount) {
      maxDiscount = discount
      bestCoupon = coupon
    }
  }

  // Only return if it's actually better than the selected one
  if (selectedCoupon.value) {
    const selectedDiscount = calculateVoucherDiscount(selectedCoupon.value)
    if (calculateVoucherDiscount(bestCoupon) <= selectedDiscount) {
      return null
    }
  }

  return bestCoupon
})

// Check if there's a better voucher than the currently selected one
const hasBetterVoucher = computed(() => {
  // Only show suggestion if there's a best voucher available
  if (!bestVoucher.value) return false

  // And if it's different from the currently selected one
  if (!selectedCoupon.value) return false
  if (selectedCoupon.value.id === bestVoucher.value.id) return false

  // And if it gives more discount
  const currentDiscount = calculateVoucherDiscount(selectedCoupon.value)
  const bestDiscount = calculateVoucherDiscount(bestVoucher.value)

  return bestDiscount > currentDiscount
})

// Calculate actual discount amount for a voucher
const calculateVoucherDiscount = (coupon: CouponApiModel | null | undefined): number => {
  if (!coupon) return 0

  const discountValue = Number(coupon.giaTriGiamGia) || 0
  const subtotalValue = subtotal.value

  if (!coupon.loaiPhieuGiamGia) {
    // Percentage discount
    return subtotalValue * (discountValue / 100)
  } else {
    // Fixed amount discount
    return Math.min(discountValue, subtotalValue)
  }
}

// Function to get voucher status text
const getVoucherStatus = (coupon: CouponApiModel) => {
  if (coupon.trangThai !== true) {
    return 'Không hoạt động'
  }

  if (!currentOrder.value || currentOrder.value.items.length === 0) {
    return 'Chưa có sản phẩm'
  }

  if (coupon.hoaDonToiThieu && subtotal.value < Number(coupon.hoaDonToiThieu)) {
    const discountText = !coupon.loaiPhieuGiamGia ? `${Number(coupon.giaTriGiamGia)}%` : formatCurrency(Number(coupon.giaTriGiamGia))
    return `Cần ${formatCurrency(Number(coupon.hoaDonToiThieu))} cho ${discountText}`
  }

  if (coupon.soLuongDung !== undefined && coupon.soLuongDung <= 0) {
    return 'Hết lượt'
  }

  if (coupon.ngayKetThuc) {
    const expiryDate = new Date(coupon.ngayKetThuc)
    const now = new Date()
    if (expiryDate < now) {
      return 'Đã hết hạn'
    }
  }

  return 'Không đủ điều kiện'
}

// Function to get discount display text
const getDiscountDisplay = (coupon: CouponApiModel) => {
  const discountValue = Number(coupon.giaTriGiamGia) || 0

  if (!coupon.loaiPhieuGiamGia) {
    // Percentage discount (loaiPhieuGiamGia = false)
    return `${discountValue}%`
  } else {
    // Fixed amount discount (loaiPhieuGiamGia = true)
    return formatCurrency(discountValue)
  }
}

// Function to show voucher suggestion modal when a better voucher is found
const showVoucherSuggestion = (betterVoucher: CouponApiModel) => {
  const currentDiscount = selectedCoupon.value ? calculateVoucherDiscount(selectedCoupon.value) : 0
  const newDiscount = calculateVoucherDiscount(betterVoucher)
  const savingsAmount = newDiscount - currentDiscount

  Modal.confirm({
    title: '💡 Có phiếu giảm giá tốt hơn!',
    content: `
      <div style="text-align: left; line-height: 1.8;">
        <p><strong>Phiếu hiện tại:</strong> ${selectedCoupon.value?.tenPhieuGiamGia}</p>
        <p style="color: #666; margin-bottom: 16px;">Tiết kiệm: <span style="color: #52c41a; font-weight: 600;">${formatCurrency(currentDiscount)}</span></p>

        <p><strong style="color: #0960bd;">✨ Phiếu tốt hơn:</strong> ${betterVoucher.tenPhieuGiamGia}</p>
        <p style="color: #666; margin-bottom: 16px;">Tiết kiệm: <span style="color: #52c41a; font-weight: 600;">${formatCurrency(newDiscount)}</span></p>

        <p style="background: #fafafa; padding: 8px 12px; border-radius: 4px; border-left: 3px solid #52c41a;">
          <span style="color: #52c41a; font-weight: 600;">💰 Tiết kiệm thêm: ${formatCurrency(savingsAmount)}</span>
        </p>
      </div>
    `,
    okText: 'Áp dụng phiếu tốt hơn',
    cancelText: 'Giữ phiếu cũ',
    onOk() {
      // Apply the better voucher
      paymentForm.value.discountCode = betterVoucher.maPhieuGiamGia
      Message.success(`Đã áp dụng phiếu "${betterVoucher.tenPhieuGiamGia}"`)
    },
    onCancel() {
      // User chose to keep current voucher
    },
  })
}

// Computed to check if voucher is eligible for current order
const isVoucherEligible = (coupon: CouponApiModel) => {
  // Check if voucher is active
  if (coupon.trangThai !== true) {
    return false
  }

  // Check if order has items
  if (!currentOrder.value || currentOrder.value.items.length === 0) {
    return false
  }

  // Check minimum order amount
  if (coupon.hoaDonToiThieu && subtotal.value < Number(coupon.hoaDonToiThieu)) {
    return false
  }

  // Check quantity remaining
  if (coupon.soLuongDung !== undefined && coupon.soLuongDung <= 0) {
    return false
  }

  // Check expiry date
  if (coupon.ngayKetThuc) {
    const expiryDate = new Date(coupon.ngayKetThuc)
    const now = new Date()
    if (expiryDate < now) {
      return false
    }
  }

  return true
}

const paginatedCartItems = computed(() => {
  if (!currentOrder.value) return []
  const start = (cartPagination.value.current - 1) * cartPagination.value.pageSize
  const end = start + cartPagination.value.pageSize
  const items = currentOrder.value.items.slice(start, end)

  // Thêm STT cho mỗi cart item
  return items.map((cartItem, index) => ({
    ...cartItem,
    stt: start + index + 1,
  }))
})

const subtotal = computed(() => {
  if (!currentOrder.value) return 0
  return currentOrder.value.items.reduce((sum, cartItem) => {
    const discountedPrice = cartItem.discount > 0 ? cartItem.price * (1 - cartItem.discount / 100) : cartItem.price
    return sum + discountedPrice * cartItem.quantity
  }, 0)
})

const discountAmount = computed(() => {
  if (!selectedCoupon.value || !currentOrder.value) return 0
  return calculateVoucherDiscount(selectedCoupon.value)
})

const finalPrice = computed(() => {
  const basePrice = subtotal.value - discountAmount.value
  // Add shipping fee for delivery orders
  const shipping = orderType.value === 'delivery' ? shippingFee.value : 0
  return basePrice + shipping
})

const totalReceived = computed(() => {
  return (paymentForm.value?.cashReceived || 0) + (paymentForm.value?.transferReceived || 0)
})

const change = computed(() => {
  return totalReceived.value - finalPrice.value
})

const paymentMethod = computed({
  get: () => paymentForm.value.method,
  set: (value: 'cash' | 'transfer' | 'both') => {
    paymentForm.value.method = value
  },
})

const canConfirmOrder = computed(() => {
  if (!currentOrder.value?.items.length || finalPrice.value <= 0) {
    return false
  }

  // For delivery orders, require address (from customer OR walk-in location form)
  if (orderType.value === 'delivery') {
    // If registered customer, require address
    if (selectedCustomer.value && !selectedCustomer.value.address) {
      return false
    }
    // If walk-in customer, require location form to be filled
    if (!selectedCustomer.value && currentOrder.value?.customerId === '') {
      if (
        !walkInLocation.value.thanhPho ||
        !walkInLocation.value.quan ||
        !walkInLocation.value.phuong ||
        !walkInLocation.value.diaChiCuThe
      ) {
        return false
      }
    }
  }

  // Nếu thanh toán bằng tiền mặt, cần đủ tiền
  if (paymentForm.value.method === 'cash') {
    return (paymentForm.value.cashReceived || 0) >= finalPrice.value
  }

  // Nếu thanh toán bằng chuyển khoản, cần đủ tiền
  if (paymentForm.value.method === 'transfer') {
    return (paymentForm.value.transferReceived || 0) >= finalPrice.value
  }

  // Nếu thanh toán cả hai, tổng tiền nhận cần đủ
  if (paymentForm.value.method === 'both') {
    return totalReceived.value >= finalPrice.value
  }

  return true
})

const insufficientStockItems = computed(() => {
  if (!currentOrder.value) return []
  return currentOrder.value.items
    .map((cartItem) => {
      const product = allProductVariants.value.find((p) => p.id === parseInt(cartItem.productId))
      const stock = product?.soLuong || 0
      if (stock < 0) {
        return {
          id: cartItem.id,
          productName: cartItem.productName,
          requiredQty: cartItem.quantity,
          currentStock: Math.max(0, stock), // Hiển thị 0 nếu âm
          shortageQty: Math.abs(stock), // Số lượng còn thiếu
        }
      }
      return null
    })
    .filter((stockItem) => stockItem !== null)
})

const overStockItems = computed(() => {
  if (!currentOrder.value) return []
  return currentOrder.value.items
    .map((cartItem) => {
      const product = allProductVariants.value.find((p) => p.id === parseInt(cartItem.productId))
      const stock = product?.soLuong || 0
      // Nếu item quantity > 0 và stock < 0, tức là vượt quá
      if (cartItem.quantity > 0 && stock < 0) {
        return {
          id: cartItem.id,
          productName: cartItem.productName,
          requiredQty: cartItem.quantity,
          currentStock: Math.max(0, stock),
          shortageQty: Math.abs(stock),
        }
      }
      return null
    })
    .filter((stockItem) => stockItem !== null)
})

const totalRevenue = computed(() => {
  return orders.value.reduce((sum, order) => {
    const orderSubtotal = order.items.reduce((subtotal, item) => subtotal + item.price * item.quantity, 0)
    const discount = paymentForm.value?.discountCode === 'SUMMER10' ? orderSubtotal * 0.1 : 0
    return sum + (orderSubtotal - discount)
  }, 0)
})

const totalItemsSold = computed(() => {
  return orders.value.reduce((sum, order) => sum + order.items.reduce((subtotal, item) => subtotal + item.quantity, 0), 0)
})

const filteredProductVariants = computed(() => {
  // Nếu không có allProductVariants, dùng productVariants
  const sourceData = allProductVariants.value.length > 0 ? allProductVariants.value : productVariants.value
  let result = sourceData

  // Apply search text filter
  if (productSearchText.value) {
    const query = productSearchText.value.toLowerCase()
    result = result.filter(
      (p) =>
        p.tenSanPham?.toLowerCase().includes(query) ||
        p.maChiTietSanPham?.toLowerCase().includes(query) ||
        p.tenChatLieu?.toLowerCase().includes(query) ||
        p.tenDeGiay?.toLowerCase().includes(query) ||
        p.tenNhaSanXuat?.toLowerCase().includes(query) ||
        p.tenXuatXu?.toLowerCase().includes(query) ||
        p.tenMauSac?.toLowerCase().includes(query) ||
        p.tenKichThuoc?.toLowerCase().includes(query) ||
        p.giaBan?.toString().includes(query)
    )
  }

  // Apply filter objects
  const filters = productFilters.value
  if (filters.tenChatLieu) {
    result = result.filter((p) => p.tenChatLieu === filters.tenChatLieu)
  }
  if (filters.tenDeGiay) {
    result = result.filter((p) => p.tenDeGiay === filters.tenDeGiay)
  }
  if (filters.tenNhaSanXuat) {
    result = result.filter((p) => p.tenNhaSanXuat === filters.tenNhaSanXuat)
  }
  if (filters.tenXuatXu) {
    result = result.filter((p) => p.tenXuatXu === filters.tenXuatXu)
  }
  if (filters.tenMauSac) {
    result = result.filter((p) => p.tenMauSac === filters.tenMauSac)
  }
  if (filters.tenKichThuoc) {
    result = result.filter((p) => p.tenKichThuoc === filters.tenKichThuoc)
  }

  // Update pagination total with filtered results
  productPagination.value.total = result.length

  // Get current page data from filtered results
  const startIndex = (productPagination.value.current - 1) * productPagination.value.pageSize
  const endIndex = startIndex + productPagination.value.pageSize

  return result.slice(startIndex, endIndex)
})

const productMaterialOptions = computed(() => {
  const options = filterOptionsData.value.chatLieu.map((material) => ({ label: material.tenChatLieu, value: material.tenChatLieu }))
  return [{ label: 'Tất cả', value: null }, ...options]
})

const productSoleOptions = computed(() => {
  const options = filterOptionsData.value.deGiay.map((sole) => ({ label: sole.tenDeGiay, value: sole.tenDeGiay }))
  return [{ label: 'Tất cả', value: null }, ...options]
})

const productColorOptions = computed(() => {
  const options = filterOptionsData.value.mauSac.map((color) => ({ label: color.tenMauSac, value: color.tenMauSac }))
  return [{ label: 'Tất cả', value: null }, ...options]
})

const productSizeOptions = computed(() => {
  const options = filterOptionsData.value.kichThuoc.map((size) => ({ label: size.tenKichThuoc, value: size.tenKichThuoc }))
  return [{ label: 'Tất cả', value: null }, ...options]
})

// Lấy Nhà Sản Xuất và Xuất xứ từ allProductVariants
const productManufacturerOptions = computed(() => {
  const manufacturers = [...new Set(allProductVariants.value.map((product) => product.tenNhaSanXuat).filter(Boolean))]
  const options = manufacturers.map((manufacturer) => ({ label: manufacturer, value: manufacturer }))
  return [{ label: 'Tất cả', value: null }, ...options]
})

const productOriginOptions = computed(() => {
  const origins = [...new Set(allProductVariants.value.map((product) => product.tenXuatXu).filter(Boolean))]
  const options = origins.map((origin) => ({ label: origin, value: origin }))
  return [{ label: 'Tất cả', value: null }, ...options]
})

// ==================== COLUMNS ====================
const cartColumns = [
  {
    title: 'STT',
    dataIndex: 'stt',
    key: 'stt',
    width: 50,
    align: 'center' as const,
  },
  {
    title: 'Sản Phẩm',
    dataIndex: 'product',
    key: 'product',
    width: 300,
    slotName: 'product',
  },
  {
    title: 'Số Lượng',
    dataIndex: 'quantity',
    key: 'quantity',
    slotName: 'quantity',
    width: 100,
    align: 'center' as const,
  },
  {
    title: 'Giá Bán',
    dataIndex: 'price',
    key: 'price',
    slotName: 'price',
    width: 120,
    align: 'right' as const,
  },
  {
    title: 'Thành Tiền',
    dataIndex: 'subtotal',
    key: 'subtotal',
    slotName: 'subtotal',
    width: 130,
    align: 'right' as const,
  },
  {
    title: 'Thao Tác',
    dataIndex: 'action',
    key: 'action',
    slotName: 'action',
    width: 80,
    align: 'center' as const,
  },
]

// ==================== METHODS ====================
const generateOrderCode = (): string => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
  let code = ''
  for (let i = 0; i < 7; i += 1) {
    code += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return code
}

const createNewOrder = async () => {
  try {
    // Create invoice on server first (returns invoiceId)
    const invoiceId = await createNewInvoice()

    // Create local order object with the returned invoiceId
    const newOrder: Order = {
      id: invoiceId.toString(),
      orderCode: generateOrderCode(),
      items: [],
      customerId: null,
      createdAt: new Date(),
    }

    // Add to orders list
    orders.value.push(newOrder)
    currentOrderIndex.value = (orders.value.length - 1).toString()
    Message.success('Đơn hàng mới đã được tạo')
  } catch (error) {
    console.error('Lỗi tạo đơn hàng:', error)
    Message.error(error.message || 'Không thể tạo đơn hàng mới')
  }
}

const deleteOrderByIndex = (index: number) => {
  orders.value.splice(index, 1)
  if (orders.value.length > 0) {
    currentOrderIndex.value = '0'
  }
}

const showDeleteConfirm = (index: number) => {
  deleteConfirmOrderIndex.value = index
  showDeleteConfirmModal.value = true
}

const showDeleteProductConfirm = (product: CartItem) => {
  productToDelete.value = product
  showDeleteProductModal.value = true
}

const confirmDeleteProduct = async () => {
  if (productToDelete.value) {
    try {
      const itemId = productToDelete.value.id
      await removeFromCart(itemId)
      showDeleteProductModal.value = false
      productToDelete.value = null
    } catch (error) {
      console.error('❌ Lỗi khi xóa sản phẩm:', error)
      Message.error('Có lỗi xảy ra khi xóa sản phẩm')
    }
  }
}

const confirmDeleteOrder = async () => {
  if (deleteConfirmOrderIndex.value !== null) {
    try {
      const orderIndex = deleteConfirmOrderIndex.value
      const orderToDelete = orders.value[orderIndex]

      if (orderToDelete?.id) {
        const invoiceId = parseInt(orderToDelete.id)
        if (!isNaN(invoiceId)) {
          // Call API to delete invoice
          await deleteInvoice(invoiceId, userStoreInstance.id)

          // Broadcast order deletion to other tabs/pages
          try {
            const orderBroadcastChannel = new BroadcastChannel('order-update-channel')
            orderBroadcastChannel.postMessage({
              type: 'ORDER_DELETED',
              invoiceId: invoiceId,
              orderCode: orderToDelete.orderCode,
              timestamp: new Date().toISOString(),
            })
            orderBroadcastChannel.close()
          } catch (error) {
            console.warn('BroadcastChannel broadcast failed:', error)
          }
        }
      }

      if (orderToDelete && orderToDelete.items.length > 0) {
        // Hoàn lại tất cả số lượng vào kho trước khi xóa đơn hàng
        orderToDelete.items.forEach((item) => {
          try {
            const productId = parseInt(item.productId)
            if (isNaN(productId)) {
              return
            }

            const productInVariants = allProductVariants.value.find((p) => p.id === productId)
            if (productInVariants) {
              productInVariants.soLuong = (productInVariants.soLuong || 0) + item.quantity
            }

            // Cập nhật số lượng đã bán
            soldQuantitiesByProductId.value[productId] = (soldQuantitiesByProductId.value[productId] || 0) - item.quantity
          } catch (itemError) {
            console.warn(`Lỗi khi hoàn stock cho sản phẩm ${item.productName}:`, itemError)
          }
        })
      }

      // Xóa đơn hàng
      deleteOrderByIndex(orderIndex)
      showDeleteConfirmModal.value = false
      deleteConfirmOrderIndex.value = null
      Message.success('Đơn hàng đã được xoá')
    } catch (error) {
      console.error('Lỗi xóa đơn hàng:', error)
      Message.error(error.message || 'Có lỗi xảy ra khi xóa đơn hàng')
    }
  }
}

const showAddProductConfirm = (product: BienTheSanPham) => {
  selectedProductForAdd.value = product
  productQuantityInput.value = 1
  showAddProductConfirmModal.value = true
  nextTick(() => {
    quantityInputRef.value?.focus?.()
  })
}

const isQuantityValid = computed(() => {
  const quantity = productQuantityInput.value
  const stock = selectedProductForAdd.value?.soLuong || 0
  return quantity > 0 && quantity <= stock
})

const handleQuantityChange = (val: number) => {
  productQuantityInput.value = val
  const stock = selectedProductForAdd.value?.soLuong || 0

  // Show warning if quantity is out of range
  if (val && val > stock) {
    Message.warning(`⚠️ Số lượng vượt quá tồn kho! Tồn kho: ${stock}`)
  } else if (val && val < 1) {
    Message.warning('⚠️ Số lượng phải lớn hơn 0')
  }
}

const confirmAddProduct = async () => {
  try {
    confirmLoading.value = true

    if (!selectedProductForAdd.value || !currentOrder.value) {
      throw new Error('Dữ liệu sản phẩm hoặc đơn hàng không hợp lệ')
    }

    let invoiceId = parseInt(currentOrder.value.id)
    const quantity = productQuantityInput.value
    const productId = selectedProductForAdd.value.id

    if (!quantity || quantity < 1) {
      Message.error('Số lượng phải lớn hơn 0')
      return
    }

    if (!productId || isNaN(productId)) {
      Message.error('ID sản phẩm không hợp lệ')
      return
    }

    // Create invoice on server if not yet created
    if (isNaN(invoiceId)) {
      invoiceId = await createNewInvoice()

      if (!invoiceId) {
        throw new Error('Không thể tạo hóa đơn')
      }
      // Update currentOrder with the new invoiceId
      currentOrder.value.id = invoiceId.toString()
    }

    // Call API to add product to invoice
    const idHoaDonChiTiet = await addProductToInvoice(invoiceId, productId, quantity, userStoreInstance.id)

    // Update local cart
    const existingItem = currentOrder.value.items.find((item) => item.productId === productId.toString())
    if (existingItem) {
      existingItem.quantity += quantity
      if (idHoaDonChiTiet) {
        // Push new id to array (each add creates new HoaDonChiTiet in backend)
        if (!existingItem.idHoaDonChiTiets) {
          existingItem.idHoaDonChiTiets = []
        }
        existingItem.idHoaDonChiTiets.push(idHoaDonChiTiet)
      }
      Message.success(`Cập nhật số lượng sản phẩm. Tổng cộng: ${existingItem.quantity}`)
    } else {
      const item: CartItem = {
        id: `${Date.now()}_${Math.random()}`,
        idHoaDonChiTiets: idHoaDonChiTiet ? [idHoaDonChiTiet] : [],
        productId: productId.toString(),
        productName: selectedProductForAdd.value.tenSanPham || '',
        price: selectedProductForAdd.value.giaBan || 0,
        discount: selectedProductForAdd.value.giaTriGiamGia || 0,
        quantity: quantity,
        image: selectedProductForAdd.value.anhSanPham?.[0] || '',
        tenChiTietSanPham: selectedProductForAdd.value.tenChiTietSanPham || '',
        tenMauSac: selectedProductForAdd.value.tenMauSac || '',
        maMau: selectedProductForAdd.value.maMau || '',
        tenKichThuoc: selectedProductForAdd.value.tenKichThuoc || '',
        tenDeGiay: selectedProductForAdd.value.tenDeGiay || '',
        tenChatLieu: selectedProductForAdd.value.tenChatLieu || '',
      }
      currentOrder.value.items.push(item)
      Message.success('Thêm sản phẩm thành công')
    }

    // Reload all products to get accurate stock from server
    await loadAllProducts()

    // Broadcast stock change to other tabs/windows (trigger refresh)
    if (stockBroadcastChannel) {
      stockBroadcastChannel.postMessage({
        type: 'STOCK_CHANGE',
        productId,
        needsRefresh: true, // Signal other tabs to refresh stock
      })
    }

    // Close modals
    showAddProductConfirmModal.value = false
    showProductModal.value = false
    selectedProductForAdd.value = null
    productQuantityInput.value = 1
  } catch (error) {
    console.error('Lỗi thêm sản phẩm:', error)
    Message.error(error.message || 'Có lỗi xảy ra khi thêm sản phẩm')
  } finally {
    confirmLoading.value = false
  }
}

const handleOrderChange = (key: string) => {
  currentOrderIndex.value = key
  cartPagination.value.current = 1

  // Reset pagination to show from first item
  cartPagination.value.current = 1
}

const handleOrderTypeChange = async (value: string) => {
  try {
    orderType.value = value as 'counter' | 'delivery'

    // Call API to update shipping method if we have an active invoice
    if (currentOrder.value?.id) {
      const invoiceId = parseInt(currentOrder.value.id)
      if (!isNaN(invoiceId)) {
        await updateInvoiceShipping(invoiceId)
      }
    }

    if (value === 'delivery') {
      Message.info('Vui lòng nhập địa chỉ giao hàng')
    }
  } catch (error) {
    console.error('Lỗi cập nhật loại đơn:', error)
    Message.error(error.message || 'Có lỗi xảy ra khi cập nhật loại đơn')
  }
}

const updateQuantity = async (itemId: string, quantity: number) => {
  let item: CartItem | undefined
  let oldQuantity = 1

  try {
    if (!currentOrder.value) {
      throw new Error('Không tìm thấy đơn hàng hiện tại')
    }

    item = currentOrder.value.items.find((i) => i.id === itemId)
    if (!item) {
      throw new Error('Không tìm thấy sản phẩm trong giỏ hàng')
    }

    oldQuantity = item.quantity
    const newQuantity = Math.max(1, quantity || 1)
    const diff = newQuantity - oldQuantity

    if (diff === 0) {
      return
    }

    // Kiểm tra xem tổng số lượng trong giỏ có vượt quá tồn kho không
    const productId = parseInt(item.productId)

    if (isNaN(productId)) {
      throw new Error('ID sản phẩm không hợp lệ')
    }

    const productInVariants = allProductVariants.value.find((p) => p.id === productId)

    if (!productInVariants) {
      throw new Error('Không tìm thấy thông tin sản phẩm trong kho')
    }

    // Kiểm tra tồn kho
    try {
      // Tính số lượng khả dụng trong kho: số hiện tại trong kho + số có trong giỏ (có thể lấy lại được)
      const currentStockInWarehouse = productInVariants.soLuong || 0

      if (diff > 0) {
        // Nếu tăng: kiểm tra xem tồn kho có đủ để tăng không
        if (currentStockInWarehouse < diff) {
          throw new Error(`Tồn kho không đủ! Yêu cầu tăng: ${diff} cái | Còn lại trong kho: ${currentStockInWarehouse} cái`)
        }
      }
    } catch (stockError) {
      Message.error(`❌ ${stockError.message}`)
      // Reset quantity và force re-render table
      item.quantity = oldQuantity
      // Force re-render table để đồng bộ UI
      cartTableKey.value++
      return
    }

    // Call API to update quantity on server
    // Update all invoice detail IDs for this product (may have multiple from repeated adds)
    if (item.idHoaDonChiTiets && item.idHoaDonChiTiets.length > 0) {
      // Calculate new quantity per invoice detail (distributed equally)
      const quantityPerDetail = Math.max(1, Math.floor(newQuantity / item.idHoaDonChiTiets.length))
      const remainingQuantity = newQuantity % item.idHoaDonChiTiets.length

      // Update each invoice detail with distributed quantity
      for (let idx = 0; idx < item.idHoaDonChiTiets.length; idx++) {
        const detailId = item.idHoaDonChiTiets[idx]
        // Give extra quantity to the last item if there's a remainder
        const detailQuantity = idx === item.idHoaDonChiTiets.length - 1 ? quantityPerDetail + remainingQuantity : quantityPerDetail
        await updateProductQuantityInInvoice(detailId, detailQuantity, userStoreInstance.id)
      }
    }

    // DO NOT update stock locally - backend already handles it
    // Just update the quantity in cart
    item.quantity = newQuantity

    // Immediately refresh product stock to get accurate data for next operations
    await refreshProductStock()

    // Broadcast stock change to other tabs/windows (trigger refresh)
    if (stockBroadcastChannel) {
      stockBroadcastChannel.postMessage({
        type: 'STOCK_CHANGE',
        productId,
        needsRefresh: true, // Signal other tabs to refresh stock
      })
    }
  } catch (error) {
    console.error('Lỗi cập nhật số lượng:', error)
    Message.error(error.message || 'Có lỗi xảy ra khi cập nhật số lượng')
    // Reset lại giá trị input về số lượng cũ khi có lỗi hệ thống
    if (item) {
      item.quantity = oldQuantity
      // Force re-render table để đồng bộ UI
      cartTableKey.value++
    }
  }
}

const resetQuantity = (itemId: string, previousQuantity: number) => {
  if (!currentOrder.value) return
  const item = currentOrder.value.items.find((i) => i.id === itemId)
  if (item) {
    item.quantity = previousQuantity
  }
}

const removeFromCart = async (itemId: string) => {
  try {
    if (!currentOrder.value) {
      Message.error('Không tìm thấy đơn hàng hiện tại')
      throw new Error('Không tìm thấy đơn hàng hiện tại')
    }

    const index = currentOrder.value.items.findIndex((i) => i.id === itemId)

    if (index === -1) {
      Message.error('Không tìm thấy sản phẩm trong giỏ hàng')
      throw new Error('Không tìm thấy sản phẩm trong giỏ hàng')
    }

    const item = currentOrder.value.items[index]
    const productId = parseInt(item.productId)

    if (isNaN(productId)) {
      Message.error('ID sản phẩm không hợp lệ')
      throw new Error('ID sản phẩm không hợp lệ')
    }

    const invoiceId = parseInt(currentOrder.value.id)
    if (!isNaN(invoiceId) && item.idHoaDonChiTiets && item.idHoaDonChiTiets.length > 0) {
      // Call API to delete ALL invoice detail IDs for this product (may have multiple from repeated adds)
      await deleteProductsFromInvoice(item.idHoaDonChiTiets, userStoreInstance.id)
    }

    currentOrder.value.items.splice(index, 1)

    // Reload all products to get accurate stock from server
    await loadAllProducts()

    Message.success('Sản phẩm đã được xóa khỏi giỏ hàng')
  } catch (error) {
    console.error('Lỗi xóa sản phẩm:', error)
    Message.error(error.message || 'Có lỗi xảy ra khi xóa sản phẩm')
  }
}

const clearCart = async () => {
  try {
    if (!currentOrder.value) {
      Message.error('Không tìm thấy đơn hàng hiện tại')
      throw new Error('Không tìm thấy đơn hàng hiện tại')
    }

    const invoiceDetailIds: number[] = []
    const affectedProductIds: number[] = []

    // Hoàn lại tất cả số lượng vào kho và cập nhật số lượng đã bán
    currentOrder.value.items.forEach((item) => {
      try {
        const productId = parseInt(item.productId)
        if (isNaN(productId)) {
          return
        }

        affectedProductIds.push(productId)

        // Collect ALL invoice detail IDs for API call (may have multiple from repeated adds)
        if (item.idHoaDonChiTiets && item.idHoaDonChiTiets.length > 0) {
          invoiceDetailIds.push(...item.idHoaDonChiTiets)
        }

        const productInVariants = allProductVariants.value.find((p) => p.id === productId)
        if (productInVariants) {
          productInVariants.soLuong = (productInVariants.soLuong || 0) + item.quantity
        }

        // Cập nhật số lượng đã bán
        soldQuantitiesByProductId.value[productId] = (soldQuantitiesByProductId.value[productId] || 0) - item.quantity
      } catch (itemError) {
        console.warn(`Lỗi khi xử lý sản phẩm ${item.productName}:`, itemError)
      }
    })

    // Call API to delete all products from invoice
    if (invoiceDetailIds.length > 0) {
      await deleteProductsFromInvoice(invoiceDetailIds, userStoreInstance.id)
    }

    currentOrder.value.items = []

    // Broadcast stock changes to other tabs/windows for each affected product
    try {
      const stockBroadcastChannelForClear = new BroadcastChannel('stock-update-channel')
      for (const productId of affectedProductIds) {
        stockBroadcastChannelForClear.postMessage({
          type: 'STOCK_CHANGE',
          productId,
          needsRefresh: true, // Signal other tabs to refresh stock
        })
      }
      stockBroadcastChannelForClear.close()
    } catch (error) {
      console.warn('BroadcastChannel broadcast failed:', error)
    }

    // Trigger immediate stock refresh
    await refreshProductStock()

    Message.success('Đã xóa tất cả sản phẩm khỏi giỏ hàng')
  } catch (error) {
    console.error('Lỗi xóa giỏ hàng:', error)
    Message.error(error.message || 'Có lỗi xảy ra khi xóa giỏ hàng')
  }
}

const updateCustomerId = (customerId: string) => {
  if (currentOrder.value) {
    // Keep empty string for "Khách lẻ", convert undefined to null
    currentOrder.value.customerId = customerId === '' ? '' : customerId || null
  }
}

const handleCustomerChange = async (customerId: string) => {
  try {
    if (!currentOrder.value) {
      Message.error('Không có đơn hàng được chọn')
      return
    }

    const invoiceId = parseInt(currentOrder.value.id)
    if (isNaN(invoiceId)) {
      Message.error('ID hóa đơn không hợp lệ')
      return
    }

    // Call API to update customer
    const parsedCustomerId = customerId === '' ? null : customerId ? parseInt(customerId) : null
    await updateInvoiceCustomer(invoiceId, parsedCustomerId)

    // Update local state
    currentOrder.value.customerId = customerId === '' ? '' : customerId || null
    Message.success('Khách hàng đã được cập nhật')
  } catch (error) {
    console.error('Lỗi cập nhật khách hàng:', error)
    Message.error(error.message || 'Có lỗi xảy ra khi cập nhật khách hàng')
  }
}

const handleCustomerSearch = () => {
  // Placeholder for search implementation
}

const addNewCustomer = async () => {
  try {
    if (!newCustomerForm.value?.name || !newCustomerForm.value?.phone) {
      Message.error('Vui lòng nhập tên và số điện thoại khách hàng')
      return
    }

    const customer: Customer = {
      id: `CUSTOMER_${Date.now()}`,
      ...newCustomerForm.value,
    }
    customers.value.push(customer)

    // If current order exists, update customer in invoice via API
    if (currentOrder.value) {
      const invoiceId = parseInt(currentOrder.value.id)
      if (!isNaN(invoiceId)) {
        await updateInvoiceCustomer(invoiceId)
      }
      currentOrder.value.customerId = customer.id
    }

    showAddCustomerModal.value = false
    newCustomerForm.value = { name: '', phone: '', email: '', address: '' }
    Message.success('Thêm khách hàng thành công')
  } catch (error) {
    console.error('Lỗi thêm khách hàng:', error)
    Message.error(error.message || 'Có lỗi xảy ra khi thêm khách hàng')
  }
}

const createNewInvoice = async () => {
  try {
    // Create invoice on server (no items needed at this point)
    const invoiceId = await createInvoice(userStoreInstance.id)
    if (!invoiceId) {
      throw new Error('Không thể tạo hóa đơn')
    }
    // Update currentOrder with the created invoiceId
    if (currentOrder.value) {
      currentOrder.value.id = invoiceId.toString()
    }
    Message.success(`Hóa đơn #${invoiceId} đã được tạo`)
    return invoiceId
  } catch (error) {
    console.error('Lỗi tạo hóa đơn:', error)
    Message.error(error.message || 'Có lỗi xảy ra khi tạo hóa đơn')
    throw error
  }
}

const addProductsToInvoice = async (invoiceId: number) => {
  try {
    if (!currentOrder.value?.items.length) {
      throw new Error('Giỏ hàng trống')
    }
    for (const item of currentOrder.value.items) {
      const productId = parseInt(item.productId)
      if (isNaN(productId)) continue
      const idHoaDonChiTiet = await addProductToInvoice(invoiceId, productId, item.quantity, userStoreInstance.id)
      if (idHoaDonChiTiet) {
        item.idHoaDonChiTiet = idHoaDonChiTiet
      }
    }
    Message.success(`${currentOrder.value.items.length} sản phẩm đã được thêm vào hóa đơn`)
  } catch (error) {
    console.error('Lỗi thêm sản phẩm:', error)
    Message.error(error.message || 'Có lỗi xảy ra khi thêm sản phẩm')
    throw error
  }
}

const updateInvoiceCustomer = async (invoiceId: number) => {
  try {
    let walkInAddress = ''
    if (!selectedCustomer.value && currentOrder.value?.customerId === '') {
      const addressParts = [
        walkInLocation.value.diaChiCuThe,
        walkInLocation.value.phuong,
        walkInLocation.value.quan,
        walkInLocation.value.thanhPho,
      ].filter(Boolean)
      walkInAddress = addressParts.join(', ')
    }

    const customerId = selectedCustomer.value?.id ? parseInt(selectedCustomer.value.id) : undefined
    const updateCustomerRequest: UpdateCustomerRequest = {
      idHoaDon: invoiceId,
      idKhachHang: customerId,
      tenKhachHang: selectedCustomer.value?.name || 'Khách lẻ',
      soDienThoai: selectedCustomer.value?.phone,
      diaChiKhachHang: selectedCustomer.value?.address || walkInAddress,
      emailKhachHang: selectedCustomer.value?.email,
      idNhanVien: userStoreInstance.id,
    }
    await updateCustomerForInvoice(updateCustomerRequest)

    // Only show success message if switching TO a customer (not when clearing to walk-in)
    if (selectedCustomer.value) {
      Message.success('Thông tin khách hàng đã được cập nhật')
    }
  } catch (error) {
    console.error('Lỗi cập nhật khách hàng:', error)
    Message.error(error.message || 'Có lỗi xảy ra khi cập nhật khách hàng')
    throw error
  }
}

const updateInvoiceShipping = async (invoiceId: number) => {
  try {
    if (orderType.value !== 'delivery') {
      Message.info('Đơn hàng tại quầy, không cần cập nhật giao hàng')
      return
    }
    await updateShippingMethod(invoiceId, userStoreInstance.id)
    Message.success('Hình thức giao hàng đã được cập nhật')
  } catch (error) {
    console.error('Lỗi cập nhật giao hàng:', error)
    Message.error(error.message || 'Có lỗi xảy ra khi cập nhật giao hàng')
    throw error
  }
}

const updateInvoicePayment = async (invoiceId: number, paymentMethod: 'cash' | 'transfer' | 'both') => {
  try {
    const paymentMethodId = paymentMethod === 'cash' ? 1 : paymentMethod === 'transfer' ? 2 : 3
    const updatePaymentRequest: UpdatePaymentMethodRequest = {
      idHoaDon: invoiceId,
      idPTTT: paymentMethodId,
      idNhanVien: userStoreInstance.id,
    }
    await updatePaymentMethod(updatePaymentRequest)
    const methodName = paymentMethod === 'cash' ? 'Tiền mặt' : paymentMethod === 'transfer' ? 'Chuyển khoản' : 'Cả hai'
    Message.success(`Hình thức thanh toán (${methodName}) đã được cập nhật`)
  } catch (error) {
    console.error('Lỗi cập nhật thanh toán:', error)
    Message.error(error.message || 'Có lỗi xảy ra khi cập nhật hình thức thanh toán')
    throw error
  }
}

const updateInvoiceVoucher = async (invoiceId: number, voucherId: number) => {
  try {
    const updateVoucherRequest: UpdateVoucherRequest = {
      idHoaDon: invoiceId,
      idPhieuGiamGia: voucherId,
      idNhanVien: userStoreInstance.id,
    }

    await updateVoucher(updateVoucherRequest)
    Message.success('Voucher đã được áp dụng')
  } catch (error) {
    console.error('Lỗi cập nhật voucher:', error)

    // Check if error is due to empty cart
    if (error.message && error.message.includes('chưa có sản phẩm')) {
      Message.error('Giỏ hàng trống hoặc dữ liệu chưa được tải. Vui lòng thử lại sau vài giây.')
    } else {
      Message.error(error.message || 'Có lỗi xảy ra khi cập nhật voucher')
    }
    throw error
  }
}

// Check if there are better vouchers available than currently selected one
const checkBetterVouchers = (): CouponApiModel[] => {
  if (!coupons.value || coupons.value.length === 0) return []

  const orderSubtotal =
    currentOrder.value?.items.reduce((sum: number, item: CartItem) => {
      const discountedPrice = item.discount > 0 ? item.price * (1 - item.discount / 100) : item.price
      return sum + discountedPrice * item.quantity
    }, 0) || 0

  // Helper: Calculate actual discount amount for comparison
  const getActualDiscount = (coupon: CouponApiModel): number => {
    const discountValue = Number(coupon.giaTriGiamGia) || 0

    if (!coupon.loaiPhieuGiamGia) {
      // Percentage discount: calculate actual amount
      return orderSubtotal * (discountValue / 100)
    } else {
      // Fixed amount: cap at order subtotal
      return Math.min(discountValue, orderSubtotal)
    }
  }

  // Find available vouchers with higher actual discount value
  const betterVouchers = coupons.value.filter((coupon) => {
    // Skip if already selected
    if (selectedCoupon.value?.id === coupon.id) return false

    // Check if voucher is applicable (min order amount)
    if (orderSubtotal < (coupon.hoaDonToiThieu || 0)) return false

    // Check if currently selected voucher exists and compare actual discount amounts
    if (selectedCoupon.value) {
      const betterDiscount = getActualDiscount(coupon)
      const currentDiscount = getActualDiscount(selectedCoupon.value as CouponApiModel)
      return betterDiscount > currentDiscount
    }

    return true
  })

  return betterVouchers
}

const confirmOrder = async () => {
  try {
    if (!currentOrder.value?.id) {
      throw new Error('Vui lòng tạo hóa đơn trước')
    }

    const invoiceId = parseInt(currentOrder.value.id)

    // Show confirmation dialog first
    return new Promise<void>((resolve) => {
      Modal.confirm({
        title: '🔔 Xác Nhận Đơn Hàng',
        content: `Bạn có chắc chắn muốn xác nhận đơn hàng ${currentOrder.value?.orderCode}?`,
        okText: 'Xác nhận',
        cancelText: 'Huỷ',
        onOk: async () => {
          try {
            // Build the confirm request with all current data
            let walkInAddress = ''
            if (!selectedCustomer.value && currentOrder.value.customerId === '') {
              const addressParts = [
                walkInLocation.value.diaChiCuThe,
                walkInLocation.value.phuong,
                walkInLocation.value.quan,
                walkInLocation.value.thanhPho,
              ].filter(Boolean)
              walkInAddress = addressParts.join(', ')
            }

            const customerId = selectedCustomer.value?.id ? parseInt(selectedCustomer.value.id) : undefined
            const paymentMethodId = paymentForm.value.method === 'cash' ? 1 : paymentForm.value.method === 'transfer' ? 2 : 3

            const confirmRequest: ConfirmBanHangRequest = {
              idHoaDon: invoiceId,
              idKhachHang: customerId || null,
              tenKhachHang: selectedCustomer.value?.name || 'Khách lẻ',
              soDienThoai: selectedCustomer.value?.phone || null,
              diaChiKhachHang: selectedCustomer.value?.address || walkInAddress || null,
              emailKhachHang: selectedCustomer.value?.email || null,
              idPTTT: paymentMethodId,
              idPhieuGiamGia: selectedCoupon.value?.id ? parseInt(selectedCoupon.value.id) : null,
              idNhanVien: userStoreInstance.id,
            }

            // Check for better vouchers
            suggestedBetterVouchers.value = checkBetterVouchers()
            confirmOrderRequest.value = confirmRequest

            // If there are better vouchers available, show warning modal to suggest them
            // Otherwise proceed with confirmation directly
            if (suggestedBetterVouchers.value.length > 0) {
              // Show confirm modal with suggestion of better vouchers
              showConfirmOrderModal.value = true
            } else {
              // No better vouchers found - proceed with confirmation directly
              await doConfirmOrder()
            }

            resolve()
          } catch (error) {
            console.error('Lỗi khi xác nhận đơn hàng:', error)
            Message.error(error.message || 'Có lỗi xảy ra khi xác nhận đơn hàng. Vui lòng thử lại.')
            resolve()
          }
        },
        onCancel: () => {
          resolve()
        },
      })
    })
  } catch (error) {
    console.error('Lỗi khi mở dialog xác nhận:', error)
    Message.error(error.message || 'Có lỗi xảy ra. Vui lòng thử lại.')
  }
}

// Execute actual order confirmation after modal approval
const doConfirmOrder = async () => {
  try {
    confirmLoading.value = true
    await confirmPosOrder(confirmOrderRequest.value)

    const orderTypeText = orderType.value === 'delivery' ? 'giao hàng' : 'tại quầy'
    const customerText = selectedCustomer.value?.name || 'Khách lẻ'
    const finalPriceText = formatCurrency(finalPrice.value)

    // Build detailed success message
    let successMessage = `✅ Đơn ${orderTypeText} ${currentOrder.value.orderCode} xác nhận thành công!`
    if (selectedCoupon.value) {
      successMessage += ` (Áp dụng: ${selectedCoupon.value.tenPhieuGiamGia})`
    }

    Message.success(successMessage)

    // Broadcast order confirmation to other tabs/pages
    try {
      const orderBroadcastChannel = new BroadcastChannel('order-update-channel')
      orderBroadcastChannel.postMessage({
        type: 'ORDER_CONFIRMED',
        invoiceId: currentOrder.value.id,
        orderCode: currentOrder.value.orderCode,
        timestamp: new Date().toISOString(),
      })
      orderBroadcastChannel.close()
    } catch (error) {
      console.warn('BroadcastChannel broadcast failed:', error)
    }

    // Remove confirmed order from list (close it)
    const currentOrderIdx = parseInt(currentOrderIndex.value)
    if (currentOrderIdx >= 0 && currentOrderIdx < orders.value.length) {
      orders.value.splice(currentOrderIdx, 1)
    }

    // Do NOT auto-create a new order - let user manually create the next one
    // Just switch to previous order if available
    if (orders.value.length > 0) {
      // Switch to previous order or first order if we deleted the last one
      currentOrderIndex.value = Math.max(0, currentOrderIdx - 1).toString()
    }
    // If no more orders, leave it empty - user must click "Thêm Đơn" to create a new one

    // Reset payment form
    paymentForm.value = {
      discountCode: null,
      method: 'cash',
      cashReceived: 0,
      transferReceived: 0,
    }
    shippingFee.value = 0
    orderType.value = 'counter'
    walkInLocation.value = {
      thanhPho: '',
      quan: '',
      phuong: '',
      diaChiCuThe: '',
      districts: [],
      wards: [],
    }

    // Close modal
    showConfirmOrderModal.value = false
  } catch (error) {
    console.error('Lỗi khi xác nhận đơn hàng:', error)
    Message.error(error.message || 'Có lỗi xảy ra khi xác nhận đơn hàng. Vui lòng thử lại.')
  } finally {
    confirmLoading.value = false
  }
}

const cancelConfirmOrder = () => {
  showConfirmOrderModal.value = false
}

const printOrder = () => {
  if (!currentOrder.value?.items.length) return
  Message.info('In hoá đơn thành công')
}

const getProductDisplayName = (record: CartItem): string => {
  const parts = [record.productName]

  if (record.tenMauSac) {
    parts.push(record.tenMauSac)
  }

  if (record.tenKichThuoc) {
    parts.push(record.tenKichThuoc)
  }

  return parts.join(' - ')
}

const formatCurrency = (value: number): string => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(value)
}

// ==================== LIFECYCLE ====================
const loadFilterOptions = async () => {
  try {
    const [chatLieuRes, deGiayRes, mauSacRes, kichThuocRes] = await Promise.all([
      getChatLieuOptions(),
      getDeGiayOptions(),
      getMauSacOptions(),
      getKichThuocOptions(),
    ])

    if (chatLieuRes?.data) filterOptionsData.value.chatLieu = chatLieuRes.data
    if (deGiayRes?.data) filterOptionsData.value.deGiay = deGiayRes.data
    if (mauSacRes?.data) filterOptionsData.value.mauSac = mauSacRes.data
    if (kichThuocRes?.data) filterOptionsData.value.kichThuoc = kichThuocRes.data
  } catch (error) {
    console.error('Error loading filter options:', error)
  }
}

const loadInitialData = async () => {
  loadingData.value = true
  try {
    // Parallel load: customers, filter options, and coupons simultaneously
    const [customersResponse, couponsResponse] = await Promise.all([layDanhSachKhachHang(), getPosActiveCoupons()])

    // Process customers in parallel
    if (customersResponse?.data) {
      customers.value = customersResponse.data.map((c: KhachHangResponse) => ({
        id: c.id.toString(),
        name: c.tenKhachHang,
        phone: c.soDienThoai,
        email: c.email,
        address: c.listDiaChi?.[0]?.diaChiCuThe || '',
      }))
    }

    // Load filter options from server
    loadFilterOptions()
    // Process coupons - Filter to only show PUBLIC coupons on initial load (for walk-in customers)
    // Personal coupons will be loaded when customer is selected
    if (couponsResponse) {
      // Filter: Only show PUBLIC vouchers (featured=false) with available quantity (soLuongDung > 0 and trangThai === true)
      // Also exclude vouchers with negative soLuongDung (already sold out)
      coupons.value = (couponsResponse as CouponApiModel[]).filter((coupon) => {
        const quantity = coupon.soLuongDung ?? 0
        // IMPORTANT: On initial load, only show public coupons (featured=false) for walk-in customers
        return !coupon.featured && quantity > 0 && coupon.trangThai === true
      })
      voucherPagination.value.total = coupons.value.length
    }
  } catch (error) {
    console.error('Error loading data:', error)
    Message.error('Không thể tải dữ liệu')
  } finally {
    loadingData.value = false
  }
}

const loadProductPage = async (page: number) => {
  try {
    productPagination.value.current = page
  } catch (error) {
    console.error('Error changing page:', error)
    Message.error('Không thể chuyển trang')
  }
}

const loadAllProducts = async () => {
  try {
    // First, fetch the first page to determine total pages needed
    const firstPageResponse = await getBienTheSanPhamPage(0, undefined, 100)
    if (!firstPageResponse?.data?.data) {
      throw new Error('Failed to load products')
    }

    let allProducts: BienTheSanPham[] = [...(firstPageResponse.data.data || [])]

    // Calculate how many pages we need to load
    const pageSize = 100
    const totalPages = Math.ceil((firstPageResponse.data.total || 0) / pageSize)

    // If there are more pages, load them in parallel (instead of sequential)
    if (totalPages > 1) {
      const pagePromises = []
      for (let pageIndex = 1; pageIndex < totalPages; pageIndex++) {
        pagePromises.push(getBienTheSanPhamPage(pageIndex, undefined, pageSize))
      }

      // Wait for all pages in parallel
      const results = await Promise.all(pagePromises)
      for (const result of results) {
        if (result?.data?.data && Array.isArray(result.data.data)) {
          allProducts = allProducts.concat(result.data.data)
        }
      }
    }

    // Filter out products with soLuong = 0
    const availableProducts = allProducts.filter((product) => (product.soLuong ?? 0) > 0)

    allProductVariants.value = availableProducts
    productPagination.value.total = availableProducts.length
    productPagination.value.current = 1
  } catch (error) {
    console.error('Error loading all products:', error)
    Message.error('Không thể tải sản phẩm')
  }
}

const resetProductFilters = () => {
  productSearchText.value = ''
  productFilters.value = {
    tenChatLieu: null,
    tenDeGiay: null,
    tenNhaSanXuat: null,
    tenXuatXu: null,
    tenMauSac: null,
    tenKichThuoc: null,
  }
}

const openProductModal = async () => {
  await loadAllProducts()
  showProductModal.value = true
}

const handleProductModalCancel = () => {
  showProductModal.value = false
  resetProductFilters()
}

const selectVoucher = async (coupon: CouponApiModel) => {
  try {
    // Only allow selection if voucher is eligible
    if (!isVoucherEligible(coupon)) {
      Message.warning('Voucher này không đủ điều kiện áp dụng cho đơn hàng hiện tại')
      return
    }

    if (!currentOrder.value) {
      Message.error('Không có đơn hàng được chọn')
      return
    }

    // Check if cart has products - IMPORTANT: validate before API call
    if (!currentOrder.value.items || currentOrder.value.items.length === 0) {
      Message.error('Vui lòng thêm sản phẩm vào giỏ hàng trước khi áp dụng voucher')
      return
    }

    // Additional validation: ensure cart has valid items with quantity > 0
    const validItems = currentOrder.value.items.filter((item: CartItem) => item.quantity > 0)
    if (validItems.length === 0) {
      Message.error('Giỏ hàng không có sản phẩm hợp lệ. Vui lòng thêm sản phẩm trước khi áp dụng voucher')
      return
    }

    const invoiceId = parseInt(currentOrder.value.id)
    const voucherId = coupon.id

    // Call API to update voucher
    await updateInvoiceVoucher(invoiceId, voucherId)

    // Broadcast coupon update to other tabs/pages
    try {
      const couponBroadcastChannel = new BroadcastChannel('coupon-update-channel')
      couponBroadcastChannel.postMessage({
        type: 'COUPON_UPDATED',
        voucherId: voucherId,
        couponCode: coupon.maPhieuGiamGia,
        timestamp: new Date().toISOString(),
      })
      couponBroadcastChannel.close()
    } catch (error) {
      console.warn('BroadcastChannel broadcast failed:', error)
    }

    // Update local state
    paymentForm.value.discountCode = coupon.maPhieuGiamGia || coupon.id.toString()

    // Auto-set default payment amount to final price after voucher discount
    // This way user only needs to confirm payment without recalculating
    if (paymentForm.value.method === 'cash') {
      paymentForm.value.cashReceived = finalPrice.value
    } else if (paymentForm.value.method === 'transfer') {
      paymentForm.value.transferReceived = finalPrice.value
    } else if (paymentForm.value.method === 'both') {
      paymentForm.value.cashReceived = finalPrice.value
      paymentForm.value.transferReceived = 0
    }

    showVoucherModal.value = false
    Message.success(`Đã áp dụng voucher: ${coupon.tenPhieuGiamGia}`)
  } catch (error) {
    console.error('Lỗi áp dụng voucher:', error)
    Message.error(error.message || 'Có lỗi xảy ra khi áp dụng voucher')
  }
}

const selectPaymentMethod = (method: 'cash' | 'transfer' | 'both') => {
  paymentForm.value.method = method
}

const handlePaymentMethodChange = async (value: string) => {
  try {
    if (!currentOrder.value) {
      Message.error('Không có đơn hàng được chọn')
      return
    }

    const invoiceId = parseInt(currentOrder.value.id)
    if (isNaN(invoiceId)) {
      Message.error('ID hóa đơn không hợp lệ')
      return
    }

    // Call API to update payment method
    await updateInvoicePayment(invoiceId, value as 'cash' | 'transfer' | 'both')

    // Update local state
    paymentForm.value.method = value as 'cash' | 'transfer' | 'both'

    // Set default amount to finalPrice when method changes
    if (value === 'cash') {
      paymentForm.value.cashReceived = finalPrice.value
      paymentForm.value.transferReceived = 0
    } else if (value === 'transfer') {
      paymentForm.value.transferReceived = finalPrice.value
      paymentForm.value.cashReceived = 0
    } else if (value === 'both') {
      // For 'both', split equally or set cash to full amount initially
      paymentForm.value.cashReceived = finalPrice.value
      paymentForm.value.transferReceived = 0
    }
  } catch (error) {
    console.error('Lỗi cập nhật phương thức thanh toán:', error)
    Message.error(error.message || 'Có lỗi xảy ra khi cập nhật phương thức thanh toán')
  }
}

const handleCashAmountChange = (value: number) => {
  paymentForm.value.cashReceived = value || 0
}

const handleTransferAmountChange = (value: number) => {
  paymentForm.value.transferReceived = value || 0
}

const clearVoucher = () => {
  paymentForm.value.discountCode = null
  Message.info('Đã xóa voucher')
}

// ==================== QR SCANNER METHODS ====================

const initQRScanner = async () => {
  try {
    // Clean up previous instance if exists
    if (qrScannerInstance.value) {
      try {
        await qrScannerInstance.value.stop()
        await qrScannerInstance.value.clear()
      } catch (cleanupError) {
        console.warn('Cleanup error:', cleanupError)
      }
      qrScannerInstance.value = null
    }

    // Request camera permissions explicitly before initializing scanner
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: false })
      // Close the stream after getting permission
      stream.getTracks().forEach(track => track.stop())
    } catch (permissionError) {
      console.error('❌ Camera permission error:', permissionError)
      if (permissionError.name === 'NotAllowedError') {
        Message.error('Quyền truy cập camera bị từ chối. Vui lòng cho phép truy cập camera trong trình duyệt.')
      } else if (permissionError.name === 'NotFoundError') {
        Message.error('Không tìm thấy camera trên thiết bị. Vui lòng kết nối camera và thử lại.')
      } else {
        Message.error('Không thể truy cập camera. Vui lòng kiểm tra quyền truy cập.')
      }
      showQRScanner.value = false
      return
    }

    // Check if DOM element exists
    const qrReaderElement = document.getElementById('qr-reader')
    if (!qrReaderElement) {
      console.error('❌ [DEBUG] QR reader element not found in DOM')
      throw new Error('QR reader element not found')
    }

    // Initialize scanner
    const html5QrCode = new Html5Qrcode('qr-reader')
    qrScannerInstance.value = html5QrCode

    // Try environment camera first (back camera) with enhanced detection
    try {
      await html5QrCode.start(
        { facingMode: 'environment' }, // Try back camera first
        {
          fps: 30, // Increased FPS for faster scanning
          qrbox: { width: 400, height: 400 }, // Large scan area for better detection
          aspectRatio: 1,
          formats: Html5QrcodeSupportedFormats.QR_CODE, // Focus on QR codes only
          supportedScanTypes: ['qr_code'], // Only QR codes for better performance
          showTorchButtonIfSupported: true,
          showZoomSliderIfSupported: true,
          defaultZoomValueIfSupported: 1.5, // Better zoom for QR detection
          experimentalFeatures: {
            useBarCodeDetectorIfSupported: true, // Use native barcode detector if available
          },
          // Enhanced detection settings
          showScanHighlighting: true,
          highlightScanRegion: true,
          highlightCodeOutline: true,
        },
        (decodedText: string) => {
          // When QR code is successfully decoded
          handleQRScanSuccess(decodedText)
        },
        (errorMessage: string) => {
          // Optional: Handle scan errors silently
          console.debug('QR Scan error:', errorMessage)
        }
      )
    } catch (envError) {
      console.warn('⚠️ [DEBUG] Environment camera failed, trying any camera:', envError)

      // Fallback to any available camera with enhanced detection
      await html5QrCode.start(
        { facingMode: 'user' }, // Front camera or any available camera
        {
          fps: 30, // Increased FPS for faster scanning
          qrbox: { width: 400, height: 400 }, // Large scan area for better detection
          aspectRatio: 1,
          formats: Html5QrcodeSupportedFormats.QR_CODE, // Focus on QR codes only
          supportedScanTypes: ['qr_code'], // Only QR codes for better performance
          showTorchButtonIfSupported: true,
          showZoomSliderIfSupported: true,
          defaultZoomValueIfSupported: 1.5, // Better zoom for QR detection
          experimentalFeatures: {
            useBarCodeDetectorIfSupported: true, // Use native barcode detector if available
          },
          // Enhanced detection settings
          showScanHighlighting: true,
          highlightScanRegion: true,
          highlightCodeOutline: true,
        },
        (decodedText: string) => {
          // When QR code is successfully decoded
          handleQRScanSuccess(decodedText)
        },
        (errorMessage: string) => {
          // Optional: Handle scan errors silently
          console.debug('QR Scan error:', errorMessage)
        }
      )
    }
  } catch (error) {
    console.error('❌ [DEBUG] Error initializing QR scanner:', error)
    console.error('❌ [DEBUG] Error details:', {
      name: error.name,
      message: error.message,
      stack: error.stack,
    })

    // More specific error handling
    if (error.name === 'NotAllowedError') {
      Message.error('Quyền truy cập camera bị từ chối. Vui lòng cho phép truy cập camera trong trình duyệt.')
    } else if (error.name === 'NotFoundError') {
      Message.error('Không tìm thấy camera trên thiết bị. Vui lòng kết nối camera và thử lại.')
    } else if (error.name === 'NotReadableError') {
      Message.error('Camera đang được sử dụng bởi ứng dụng khác. Vui lòng đóng ứng dụng khác và thử lại.')
    } else if (error.name === 'OverconstrainedError') {
      Message.error('Camera không hỗ trợ cấu hình yêu cầu. Vui lòng thử lại.')
    } else {
      Message.error('Không thể khởi tạo camera. Vui lòng kiểm tra quyền truy cập camera và thử lại.')
    }

    showQRScanner.value = false
  }
}

const handleQRScanSuccess = async (decodedText: string) => {
  try {
    // Stop scanner immediately after successful scan
    if (qrScannerInstance.value) {
      try {
        await qrScannerInstance.value.stop()
      } catch (stopError) {}
    }
    // Find product in allProductVariants that has matching qrcode
    const matchedProduct = allProductVariants.value.find((product) => {
      if (product.qrcode) {
        // Multiple matching strategies:
        // 1. Exact match with QR code URL
        // 2. QR code contains decoded text or vice versa
        // 3. Match with product ID
        // 4. Match with product SKU
        const qrMatch =
          decodedText === product.qrcode ||
          product.qrcode === decodedText ||
          decodedText.includes(product.qrcode) ||
          product.qrcode.includes(decodedText) ||
          decodedText === product.id?.toString() ||
          decodedText === product.maChiTietSanPham
        return qrMatch
      }
      return false
    })

    if (!matchedProduct) {
      console.error('❌ [DEBUG] No product found with QR code:', decodedText)
      console.error(
        '❌ [DEBUG] Available QR codes in products:',
        allProductVariants.value.filter((p) => p.qrcode).map((p) => ({ id: p.id, qrcode: p.qrcode, maChiTietSanPham: p.maChiTietSanPham }))
      )

      // Fallback: try to parse as ID
      const fallbackId = parseInt(decodedText.trim(), 10)
      if (!isNaN(fallbackId) && fallbackId > 0) {
        const fallbackProduct = allProductVariants.value.find((p) => p.id === fallbackId)
        if (fallbackProduct) {
          await addProductToCart(fallbackProduct, 1)
          await closeQRScanner()
          Message.success(`Đã thêm sản phẩm "${fallbackProduct.tenSanPham}" vào giỏ hàng`)
          return
        }
      }

      Message.error(`Không tìm thấy sản phẩm với mã QR: "${decodedText}". Vui lòng thử lại.`)
      return
    }

    // Check stock
    if (matchedProduct.soLuong <= 0) {
      Message.error(`Sản phẩm "${matchedProduct.tenSanPham}" đã hết hàng`)
      return
    }

    // Add to cart with quantity 1
    await addProductToCart(matchedProduct, 1)

    // Close QR scanner after success
    await closeQRScanner()
    Message.success(`Đã thêm sản phẩm "${matchedProduct.tenSanPham}" vào giỏ hàng`)
  } catch (error) {
    console.error('❌ Error processing QR scan:', error)
    console.error('❌ Error details:', {
      name: error.name,
      message: error.message,
      stack: error.stack,
    })
    Message.error('Có lỗi xảy ra khi xử lý mã QR. Vui lòng thử lại.')
  }
}

const addProductToCart = async (product: BienTheSanPham, quantity: number) => {
  try {
    if (!currentOrder.value) {
      throw new Error('Không tìm thấy đơn hàng hiện tại')
    }

    // Check if already in cart
    const existingItem = currentOrder.value.items.find((item) => item.productId === product.id?.toString())

    if (existingItem) {
      // Update quantity if already exists
      const newQuantity = existingItem.quantity + quantity
      if (newQuantity > product.soLuong) {
        throw new Error(`Tổng số lượng (${newQuantity}) vượt quá tồn kho (${product.soLuong})`)
      }
      existingItem.quantity = newQuantity
      Message.success(`Cập nhật số lượng sản phẩm. Tổng cộng: ${newQuantity}`)
    } else {
      // Add new item
      const item: CartItem = {
        id: `${Date.now()}_${Math.random()}`,
        productId: product.id?.toString() || '',
        productName: product.tenSanPham || '',
        price: product.giaBan || 0,
        discount: product.giaTriGiamGia || 0,
        quantity: quantity,
        image: product.anhSanPham?.[0] || '',
        tenChiTietSanPham: product.tenChiTietSanPham || '',
        tenMauSac: product.tenMauSac || '',
        maMau: product.maMau || '',
        tenKichThuoc: product.tenKichThuoc || '',
        tenDeGiay: product.tenDeGiay || '',
        tenChatLieu: product.tenChatLieu || '',
      }
      currentOrder.value.items.push(item)
      Message.success('Thêm sản phẩm vào giỏ hàng thành công')
    }

    // Update stock
    const productInVariants = allProductVariants.value.find((p) => p.id === product.id)
    if (productInVariants) {
      productInVariants.soLuong = Math.max(0, productInVariants.soLuong - quantity)
    }

    // Broadcast stock change to other tabs/windows (trigger refresh)
    try {
      const qrStockBroadcastChannel = new BroadcastChannel('stock-update-channel')
      qrStockBroadcastChannel.postMessage({
        type: 'STOCK_CHANGE',
        productId: product.id,
        needsRefresh: true, // Signal other tabs to refresh stock
      })
      qrStockBroadcastChannel.close()
    } catch (error) {
      console.warn('BroadcastChannel broadcast failed:', error)
    }

    // Trigger immediate stock refresh
    await refreshProductStock()

    // Stock tracking is handled by backend, no need to track locally
  } catch (error) {
    console.error('Error adding product to cart:', error)
    throw error
  }
}

const closeQRScanner = async () => {
  // Clean up scanner instance
  if (qrScannerInstance.value) {
    try {
      await qrScannerInstance.value.stop()
      await qrScannerInstance.value.clear()
    } catch (cleanupError) {
      console.warn('Cleanup error:', cleanupError)
    }
    qrScannerInstance.value = null
  }

  showQRScanner.value = false
}

// Watch for modal visibility
watch([showQRScanner, showDeleteProductModal], async ([qrOpen, deleteProductOpen]) => {
  if (qrOpen) {
    // QR Modal opened, ensure products are loaded first
    if (allProductVariants.value.length === 0) {
      await loadAllProducts()
    }

    // Then start camera
    setTimeout(() => {
      initQRScanner()
    }, 100)
  } else if (!qrOpen) {
    // QR Modal closed, cleanup scanner
    await closeQRScanner()
  }

  if (!deleteProductOpen) {
    // Reset delete product state when modal closes
    productToDelete.value = null
  }
})

// Watch for customer selection change - load customer-specific vouchers
watch(selectedCustomer, async (newCustomer) => {
  if (newCustomer && newCustomer.id) {
    const idKhachHang = parseInt(newCustomer.id)

    // Add small delay to ensure state is fully updated
    await new Promise((resolve) => setTimeout(resolve, 100))

    await refreshVouchersForCustomer(idKhachHang)
  } else {
    // No customer selected, load all public vouchers

    // Add small delay to ensure state is fully updated
    await new Promise((resolve) => setTimeout(resolve, 100))

    await refreshVouchers()
  }
})

// Location API functions
const loadProvinces = async () => {
  try {
    const res = await fetch('https://provinces.open-api.vn/api/p/')
    const data = await res.json()
    provinces.value = data.map((p: any) => ({
      value: p.name,
      label: p.name,
      code: p.code,
    }))
  } catch (error) {
    console.error('Error loading provinces:', error)
  }
}

const onWalkInProvinceChange = async (value: string) => {
  walkInLocation.value.districts = []
  walkInLocation.value.wards = []
  walkInLocation.value.quan = ''
  walkInLocation.value.phuong = ''

  const province = provinces.value.find((p) => p.value === value)
  if (province) {
    try {
      const res = await fetch(`https://provinces.open-api.vn/api/p/${province.code}?depth=2`)
      const data = await res.json()
      walkInLocation.value.districts = data.districts.map((d: any) => ({
        value: d.name,
        label: d.name,
        code: d.code,
      }))
    } catch (error) {
      console.error('Error loading districts:', error)
    }
  }
}

const onWalkInDistrictChange = async (value: string) => {
  walkInLocation.value.wards = []
  walkInLocation.value.phuong = ''

  const district = walkInLocation.value.districts.find((d) => d.value === value)
  if (district) {
    try {
      const res = await fetch(`https://provinces.open-api.vn/api/d/${district.code}?depth=2`)
      const data = await res.json()
      walkInLocation.value.wards = data.wards.map((w: any) => ({
        value: w.name,
        label: w.name,
      }))
    } catch (error) {
      console.error('Error loading wards:', error)
    }
  }
}

// Refresh vouchers periodically
const refreshVouchers = async () => {
  try {
    const couponsResponse = await getPosActiveCoupons()
    if (couponsResponse) {
      // Filter coupons: Only show PUBLIC vouchers (featured=false) with available quantity (soLuongDung > 0 and trangThai === true)
      const newCoupons = (couponsResponse as CouponApiModel[]).filter((coupon) => {
        const quantity = coupon.soLuongDung ?? 0
        return !coupon.featured && quantity > 0 && coupon.trangThai === true
      })

      // Compare with cached data - ONLY update if different
      const newCouponsJson = JSON.stringify(newCoupons.map((c) => ({ id: c.id, maPhieuGiamGia: c.maPhieuGiamGia, soLuongDung: c.soLuongDung, giaTriGiamGia: c.giaTriGiamGia })))
      if (newCouponsJson === cachedCoupons.value) {
        // No change, skip update
        return
      }
      cachedCoupons.value = newCouponsJson

      // Check if current selected coupon still exists and is valid
      if (selectedCoupon.value && !newCoupons.find((c) => c.maPhieuGiamGia === selectedCoupon.value?.maPhieuGiamGia)) {
        paymentForm.value.discountCode = null
      }

      // Find the BEST eligible voucher (highest discount)
      const eligibleVouchers = newCoupons.filter((coupon) => isVoucherEligible(coupon))
      let bestVoucher: CouponApiModel | null = null
      let maxDiscount = 0

      for (const coupon of eligibleVouchers) {
        const discount = calculateVoucherDiscount(coupon)
        if (discount > maxDiscount) {
          maxDiscount = discount
          bestVoucher = coupon
        }
      }

      // Check if selected voucher is still eligible
      const isSelectedEligible =
        selectedCoupon.value && eligibleVouchers.some((c) => c.maPhieuGiamGia === selectedCoupon.value?.maPhieuGiamGia)

      // If selected voucher is no longer eligible, clear it automatically
      if (selectedCoupon.value && !isSelectedEligible) {
        paymentForm.value.discountCode = null
      }

      const bestDiscount = calculateVoucherDiscount(bestVoucher)
      const selectedDiscount = calculateVoucherDiscount(selectedCoupon.value)
      const discountDifference = bestDiscount - selectedDiscount

      if (
        bestVoucher &&
        selectedCoupon.value &&
        isSelectedEligible &&
        bestVoucher.maPhieuGiamGia !== selectedCoupon.value.maPhieuGiamGia &&
        discountDifference > 1000
      ) {
        showVoucherSuggestion(bestVoucher)
      }

      coupons.value = newCoupons
      voucherPagination.value.total = newCoupons.length
    }
  } catch (error) {
    console.error('Error refreshing vouchers:', error)
  }
}

// Refresh vouchers for specific customer (load public + personal vouchers)
const refreshVouchersForCustomer = async (idKhachHang: number) => {
  try {
    const couponsResponse = await getPosActiveCouponsForCustomer(idKhachHang)
    if (couponsResponse) {
      // Filter coupons: only show active vouchers with available quantity
      let newCoupons = (couponsResponse as CouponApiModel[]).filter((coupon) => {
        const quantity = coupon.soLuongDung ?? 0
        return quantity > 0 && coupon.trangThai === true
      })

      // Deduplicate
      const seenIds = new Set<string>()
      newCoupons = newCoupons.filter((coupon) => {
        if (seenIds.has(coupon.id.toString())) {
          return false
        }
        seenIds.add(coupon.id.toString())
        return true
      })

      // Compare with cached data - ONLY update if different
      const newCouponsJson = JSON.stringify(newCoupons.map((c) => ({ id: c.id, maPhieuGiamGia: c.maPhieuGiamGia, soLuongDung: c.soLuongDung, giaTriGiamGia: c.giaTriGiamGia })))
      if (newCouponsJson === cachedCoupons.value) {
        // No change, skip update
        return
      }
      cachedCoupons.value = newCouponsJson

      // Check if current selected coupon still exists and is valid
      if (selectedCoupon.value && !newCoupons.find((c) => c.maPhieuGiamGia === selectedCoupon.value?.maPhieuGiamGia)) {
        paymentForm.value.discountCode = null
      }

      coupons.value = newCoupons
      voucherPagination.value.total = newCoupons.length
    }
  } catch (error) {
    console.error('Error refreshing vouchers for customer:', error)
  }
}

// Refresh product stock from server (for real-time sync between tabs/windows)
// ONLY updates if data actually changed to avoid unnecessary re-renders
const refreshProductStock = async () => {
  try {
    const firstPageResponse = await getBienTheSanPhamPage(0, undefined, 100)
    if (!firstPageResponse?.data?.data) {
      throw new Error('Failed to load products')
    }

    let allProducts: BienTheSanPham[] = [...(firstPageResponse.data.data || [])]

    // Calculate how many pages we need to load
    const pageSize = 100
    const totalPages = Math.ceil((firstPageResponse.data.total || 0) / pageSize)

    // If there are more pages, load them in parallel
    if (totalPages > 1) {
      const pagePromises = []
      for (let pageIndex = 1; pageIndex < totalPages; pageIndex++) {
        pagePromises.push(getBienTheSanPhamPage(pageIndex, undefined, pageSize))
      }
      const results = await Promise.all(pagePromises)
      for (const result of results) {
        if (result?.data?.data && Array.isArray(result.data.data)) {
          allProducts = allProducts.concat(result.data.data)
        }
      }
    }

    // Filter out products with soLuong = 0
    const availableProducts = allProducts.filter((product) => (product.soLuong ?? 0) > 0)

    // Check if data actually changed by comparing stock quantities
    let hasChanged = false
    
    // Check if count changed
    if (availableProducts.length !== allProductVariants.value.length) {
      hasChanged = true
    } else {
      // Check if any stock quantities changed
      for (const product of availableProducts) {
        const cachedStock = cachedProducts.value.get(product.id)
        if (cachedStock === undefined || cachedStock !== product.soLuong) {
          hasChanged = true
          break
        }
      }
    }

    // ONLY update if there's actual change
    if (hasChanged) {
      // Update cache
      cachedProducts.value.clear()
      for (const product of availableProducts) {
        cachedProducts.value.set(product.id, product.soLuong)
      }

      // Update state
      allProductVariants.value = availableProducts
      productPagination.value.total = availableProducts.length
      productPagination.value.current = 1
    }
  } catch (error) {
    console.error('Error refreshing product stock:', error)
  }
}

// Auto-refresh vouchers every 30 seconds
let voucherRefreshInterval: number | null = null

onMounted(() => {
  // Do NOT initialize with an empty order - let user create orders manually by clicking "Thêm Đơn"
  // orders.value will be empty until user explicitly creates the first order

  // Load data from API
  loadInitialData()
  // Load provinces for location picker
  loadProvinces()

  // Setup BroadcastChannel for real-time sync between tabs/windows
  try {
    stockBroadcastChannel = new BroadcastChannel('stock-update-channel')
    stockBroadcastChannel.onmessage = (event) => {
      if (event.data.type === 'STOCK_CHANGE') {
        const { productId, newStock, needsRefresh } = event.data

        if (needsRefresh) {
          // Trigger immediate refresh instead of waiting for next interval
          refreshProductStock()
        } else if (newStock !== undefined) {
          // Direct stock update (for add-to-cart from other pages)
          const product = allProductVariants.value.find((p) => p.id === productId)
          if (product) {
            product.soLuong = newStock
          }
        }
      }
    }

    // Setup BroadcastChannel for coupon updates
    const couponBroadcastChannel = new BroadcastChannel('coupon-update-channel')
    couponBroadcastChannel.onmessage = (event) => {
      if (event.data.type === 'COUPON_CHANGE') {
        // Refresh vouchers for current customer or public vouchers
        if (selectedCustomer.value && selectedCustomer.value.id) {
          const idKhachHang = parseInt(selectedCustomer.value.id)
          refreshVouchersForCustomer(idKhachHang)
        } else {
          refreshVouchers()
        }
      }
    }
    // Store for cleanup
    // @ts-ignore
    window.__couponBroadcastChannel = couponBroadcastChannel
  } catch (error) {
    console.warn('BroadcastChannel not supported, falling back to polling', error)
  }

  // Set up auto-refresh for vouchers (every 4 seconds for near real-time updates)
  voucherRefreshInterval = window.setInterval(() => {
    // Throttle: only refresh if enough time has passed since last refresh
    const now = Date.now()
    if (now - lastVoucherRefreshTime < VOUCHER_THROTTLE_MS) {
      return // Skip this refresh cycle
    }
    lastVoucherRefreshTime = now

    // If customer is selected, refresh customer-specific vouchers
    // Otherwise, refresh public vouchers only
    if (selectedCustomer.value && selectedCustomer.value.id) {
      const idKhachHang = parseInt(selectedCustomer.value.id)
      refreshVouchersForCustomer(idKhachHang)
    } else {
      refreshVouchers()
    }
  }, 4000) // 4 seconds (faster near real-time sync)

  // Set up auto-refresh for product stock (every 2.5 seconds for near real-time sync between tabs)
  const stockRefreshInterval = window.setInterval(() => {
    // Throttle: only refresh if enough time has passed since last refresh
    const now = Date.now()
    if (now - lastStockRefreshTime < STOCK_THROTTLE_MS) {
      return // Skip this refresh cycle
    }
    lastStockRefreshTime = now

    refreshProductStock()
  }, 2500) // 2.5 seconds (faster for real-time stock updates)

  // Store interval ID for cleanup
  // @ts-ignore
  window.__stockRefreshInterval = stockRefreshInterval
})

// Cleanup intervals on unmount
onBeforeUnmount(() => {
  if (voucherRefreshInterval !== null) {
    clearInterval(voucherRefreshInterval)
  }
  // @ts-ignore
  if (window.__stockRefreshInterval) {
    clearInterval(window.__stockRefreshInterval)
  }

  // Close BroadcastChannels
  if (stockBroadcastChannel) {
    stockBroadcastChannel.close()
  }
  // @ts-ignore
  if (window.__couponBroadcastChannel) {
    window.__couponBroadcastChannel.close()
  }
})
</script>

<style scoped lang="less">
.pos-system {
  padding: 16px 20px;
}

.main-pos-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.pos-main {
  margin-bottom: 24px;
}

:deep(.orders-tabs .arco-tabs-tab) {
  gap: 4px;
  margin: 0 1px;
  border: 1px solid black;
  border-bottom: none;
}

:deep(.arco-modal-body) {
  padding: 20px 20px;
}

.pos-left,
.pos-right {
  :deep(.arco-card) {
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  }
}

.orders-card {
  margin-bottom: 16px;
}

.orders-tabs {
  :deep(.arco-tabs-tab) {
    padding: 8px 16px;
  }
}

.tab-header {
  display: flex;
  align-items: center;
  gap: 8px;

  .tab-close-btn {
    margin-left: 8px;
  }

  :deep(.arco-badge) {
    line-height: 1;
  }

  :deep(.arco-badge-number) {
    min-width: 20px;
    height: 20px;
    line-height: 20px;
    padding: 0 6px;
    font-size: 12px;
    border-radius: 10px;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  :deep(.arco-badge-custom-dot) {
    border-radius: 50%;
  }
}

.toolbar {
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 4px;
}

.cart-card {
  margin-bottom: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.order-code-cart-card {
  margin-bottom: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.cart-wrapper {
  margin-bottom: 16px;
  min-height: 200px;

  :deep(.arco-table) {
    font-size: 13px;
  }
}

.cart-summary {
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;

  p {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
    font-size: 13px;

    strong {
      font-weight: 600;
    }
  }
}

.customer-card {
  margin-bottom: 16px;

  :deep(.arco-form-item) {
    margin-bottom: 12px;
  }
}

.payment-card {
  :deep(.arco-form-item) {
    margin-bottom: 12px;
  }

  :deep(.arco-form-item-label-col) {
    line-height: 28px;
  }
}

.payment-summary {
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;

  .summary-row {
    display: flex;
    justify-content: space-between;
    font-size: 13px;
    margin-bottom: 8px;

    &:last-child {
      margin-bottom: 0;
    }

    &.total {
      border-top: 1px solid #e5e5e5;
      padding-top: 8px;
      font-size: 14px;
      font-weight: 600;
    }
  }
}

.discount-text {
  color: #f5222d;
  font-weight: 600;
}

.final-price {
  color: #52c41a;
  font-size: 16px;
  font-weight: 700;
}

.change-text {
  color: #faad14;
  font-weight: 600;
}

.text-right {
  text-align: right;
}

/* Payment Method Button Styling */
.payment-method-btn {
  position: relative;
  overflow: hidden;

  &:not(.payment-method-active) {
    border: 2px solid #f0f0f0 !important;
    background: #fafafa !important;
    color: #666 !important;

    &:hover {
      border-color: #40a9ff !important;
      background: #e6f7ff !important;
      color: #1890ff !important;
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
    }
  }

  &.payment-method-active {
    border: 2px solid #1890ff !important;
    box-shadow: 0 4px 16px rgba(24, 144, 255, 0.25);
    transform: translateY(-2px);

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 3px;
      background: linear-gradient(90deg, #1890ff, #40a9ff);
      animation: paymentActiveGlow 2s ease-in-out infinite alternate;
    }

    &::after {
      content: '✓';
      position: absolute;
      top: 8px;
      right: 12px;
      font-size: 12px;
      color: #1890ff;
      font-weight: bold;
      animation: paymentCheckPulse 1.5s ease-in-out infinite;
    }
  }
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
    box-shadow: 0 0 0 0 rgba(250, 140, 22, 0.4);
  }
  50% {
    opacity: 0.95;
    box-shadow: 0 0 0 8px rgba(250, 140, 22, 0);
  }
}

@keyframes paymentActiveGlow {
  0% {
    opacity: 0.8;
    transform: scaleX(1);
  }
  50% {
    opacity: 1;
    transform: scaleX(1.02);
  }
}

@keyframes paymentCheckPulse {
  0%,
  100% {
    opacity: 0.7;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.1);
  }
}

.cash-feedback {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 14px;
  animation: cashFeedbackFadeIn 0.3s ease-out;
}

@keyframes cashFeedbackFadeIn {
  0% {
    opacity: 0;
    transform: scale(0.95);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

.cash-change-positive {
  background: linear-gradient(135deg, #f6ffed, #b7eb8f);
  border: 1px solid #b7eb8f;
  color: #52c41a;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  animation: cashPositivePulse 1s ease-in-out infinite alternate;
}

@keyframes cashPositivePulse {
  0% {
    box-shadow: 0 0 0 0 rgba(82, 196, 26, 0.4);
  }
  100% {
    box-shadow: 0 2px 8px 0 rgba(82, 196, 26, 0.4);
  }
}

.cash-change-negative {
  background: linear-gradient(135deg, #fff2f0, #ffccc7);
  border: 1px solid #ffccc7;
  color: #ff4d4f;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  animation: cashNegativeShake 0.5s ease-in-out;
}

@keyframes cashNegativeShake {
  0%,
  100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-2px);
  }
  75% {
    transform: translateX(2px);
  }
}

.cash-icon {
  font-size: 16px;
}

.cash-text {
  flex: 1;

  strong {
    font-size: 16px;
    font-weight: 700;
  }
}

@media (max-width: 768px) {
  .pos-system {
    padding: 8px;
  }

  .pos-left,
  .pos-right {
    margin-bottom: 16px;
  }

  .orders-tabs {
    :deep(.arco-tabs-tab) {
      padding: 6px 12px;
      font-size: 12px;
    }
  }
}

/* Voucher disabled styling */
.voucher-disabled {
  opacity: 0.6;
  cursor: not-allowed !important;
  background: #f5f5f5 !important;
}

.voucher-disabled:hover {
  background: #f5f5f5 !important;
}

/* Hide scrollbar for tabs container */
.tabs-container::-webkit-scrollbar {
  display: none;
}
</style>
