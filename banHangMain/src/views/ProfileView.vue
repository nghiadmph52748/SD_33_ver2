<template>
  <div class="profile-page" v-if="userStore.isAuthenticated">
    <div class="profile-container">
      <div class="card profile-card">
        <header class="card__header">
          <h1>Hồ sơ của tôi</h1>
          <p>Cập nhật tên, số điện thoại và địa chỉ giao hàng.</p>
        </header>

        <form class="form" @submit.prevent="onSave">
          <div class="form__group">
            <label for="name">Họ và tên</label>
            <input id="name" type="text" v-model.trim="form.name" autocomplete="name" required />
          </div>

          <div class="form__group">
            <label for="phone">Số điện thoại</label>
            <input id="phone" type="tel" v-model.trim="form.phone" autocomplete="tel" required />
          </div>

          <div class="form__group">
            <label>{{ $t("checkout.province") }}</label>
            <AddressSearchSelect v-model="address.province" :options="provinces" :placeholder="$t('checkout.province')"
              @change="onProvinceChange" />
          </div>

          <div class="form__group">
            <label>{{ $t("checkout.district") }}</label>
            <AddressSearchSelect v-model="address.district" :options="districts" :placeholder="$t('checkout.district')"
              :disabled="!districts.length && !address.district" @change="onDistrictChange" />
          </div>

          <div class="form__group">
            <label>{{ $t("checkout.ward") }}</label>
            <AddressSearchSelect v-model="address.ward" :options="wards" :placeholder="$t('checkout.ward')"
              :disabled="!wards.length" />
          </div>

          <div class="form__group">
            <label for="street">Địa chỉ cụ thể</label>
            <input id="street" type="text" v-model.trim="address.street" autocomplete="street-address"
              placeholder="Số nhà, đường..." />
          </div>

          <div class="form__actions">
            <button type="button" @click="openConfirm" :disabled="saving">
              {{ saving ? 'Đang lưu...' : 'Lưu thay đổi' }}
            </button>
          </div>

          <p v-if="message" class="form__message">{{ message }}</p>
        </form>
        <div v-if="showConfirm" class="modal-backdrop">
          <div class="modal-dialog" role="dialog" aria-modal="true">
            <h3>Xác nhận lưu</h3>
            <p>Bạn có chắc muốn lưu thay đổi hồ sơ?</p>
            <div class="dialog-actions">
              <button type="button" class="ghost" @click="closeConfirm">Hủy</button>
              <button type="button" class="primary" @click="onSave">Lưu</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Column: Order History -->
      <div class="card order-history">
        <header class="card__header">
          <div class="header-content">
            <h2>Lịch sử mua hàng</h2>
            <span v-if="!loadingOrders && orders.length > 0" class="order-count">{{ orders.length }} đơn hàng</span>
          </div>
        </header>
        <div v-if="loadingOrders" class="order-history__loading">
          <div class="spinner"></div>
          <p>Đang tải đơn hàng...</p>
        </div>
        <div v-else-if="orders.length === 0" class="order-history__empty">
          <svg class="empty-icon" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor"
            stroke-width="1.5">
            <path d="M9 2L3 8v13c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V8l-6-6H9z" />
            <path d="M9 2v6H3" />
            <path d="M9 13h6" />
            <path d="M9 17h6" />
          </svg>
          <p class="empty-title">Chưa có đơn hàng nào</p>
          <p class="empty-subtitle">Khi bạn đặt hàng, chúng sẽ hiển thị ở đây</p>
          <RouterLink to="/" class="shop-now-button">Mua sắm ngay</RouterLink>
        </div>
        <div v-else>
          <!-- Search box -->
          <div class="order-search">
            <div class="search-input-wrapper">
              <svg class="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                stroke-width="2">
                <circle cx="11" cy="11" r="8" />
                <path d="m21 21-4.35-4.35" />
              </svg>
              <input type="text" v-model="orderSearchQuery"
                placeholder="Tìm kiếm theo mã đơn, ngày tạo hoặc trạng thái..." class="search-input" />
              <button v-if="orderSearchQuery" @click="orderSearchQuery = ''" class="clear-search" type="button">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18" />
                  <line x1="6" y1="6" x2="18" y2="18" />
                </svg>
              </button>
            </div>
            <div v-if="orderSearchQuery && filteredOrders.length === 0" class="search-no-results">
              Không tìm thấy đơn hàng phù hợp
            </div>
          </div>

          <!-- Order list -->
          <div class="order-history__list">
            <div v-for="order in filteredOrders" :key="order.id" class="order-card">
              <div class="order-card__header">
                <div class="order-code">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M9 2L3 8v13c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V8l-6-6H9z" />
                    <path d="M9 2v6H3" />
                  </svg>
                  <span>{{ order.maHoaDon || '—' }}</span>
                </div>
                <span :class="['order-status', getStatusClass(order.trangThai)]">
                  {{ order.trangThai || 'Chưa xác định' }}
                </span>
              </div>

              <div class="order-card__body">
                <div class="order-meta">
                  <div class="meta-item">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <circle cx="12" cy="12" r="10" />
                      <path d="M12 6v6l4 2" />
                    </svg>
                    <span>{{ formatDate(order.createAt || order.ngayTao) }}</span>
                  </div>
                  <div class="meta-item" v-if="order.hoaDonChiTiets?.length || order.items?.length">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M6 2L3 6v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V6l-3-4H6z" />
                      <line x1="3" y1="6" x2="21" y2="6" />
                      <path d="M16 10c0 2.2-1.8 4-4 4s-4-1.8-4-4" />
                    </svg>
                    <span>{{ (order.hoaDonChiTiets || order.items || []).length }} sản phẩm</span>
                  </div>
                </div>

                <div class="order-total">
                  <span class="total-label">Tổng tiền:</span>
                  <span class="total-amount">{{ formatCurrency(order.tongTienSauGiam || order.tongTien || 0) }}</span>
                </div>
              </div>

              <div class="order-card__footer">
                <RouterLink :to="`/order-lookup?code=${order.maHoaDon}`" class="view-details-btn">
                  <span>Xem chi tiết</span>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M5 12h14" />
                    <path d="M12 5l7 7-7 7" />
                  </svg>
                </RouterLink>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <div v-else class="profile-page">
    <div class="card">
      <p>Bạn cần đăng nhập để xem hồ sơ.</p>
      <button type="button" @click="goLogin">Đăng nhập</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { RouterLink } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { updateProfile, getMe } from '@/api/auth'
import { fetchGHNProvinces, fetchGHNDistrictsByProvince, fetchGHNWards } from '@/api/shipping'
import { fetchCustomerOrders, type OrderTrackingDetail } from '@/api/orders'
import { formatCurrency } from '@/utils/currency'
import AddressSearchSelect from '@/components/common/AddressSearchSelect.vue'


const userStore = useUserStore()
const router = useRouter()

const form = reactive({
  name: '',
  phone: '',
})

const address = reactive({
  province: '',
  district: '',
  ward: '',
  street: '',
})

const provinces = ref<{ value: string; label: string; code: number }[]>([])
const districts = ref<{ value: string; label: string; code: number }[]>([])
const wards = ref<{ value: string; label: string }[]>([])

const saving = ref(false)
const message = ref('')
const showConfirm = ref(false)
const orders = ref<OrderTrackingDetail[]>([])
const loadingOrders = ref(false)
const orderSearchQuery = ref('')

const filteredOrders = computed(() => {
  if (!orderSearchQuery.value) return orders.value
  const query = orderSearchQuery.value.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '')
  return orders.value.filter(order => {
    const invoiceCode = (order.maHoaDon || '').toLowerCase()
    const status = (order.trangThai || '').toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '')
    const dateStr = formatDate(order.createAt || order.ngayTao).toLowerCase()
    return invoiceCode.includes(query) || status.includes(query) || dateStr.includes(query)
  })
})


async function loadProvinces() {
  try {
    const ghnProvinces = await fetchGHNProvinces()
    if (ghnProvinces && ghnProvinces.length > 0) {
      provinces.value = ghnProvinces.map((p: any) => ({
        value: p.name,
        label: p.name,
        code: p.code,
      }))
    }
  } catch {
    provinces.value = []
  }
}

async function onProvinceChange(keepSelection = false) {
  const prevDistrict = address.district;
  const prevWard = address.ward;
  const prov = provinces.value.find((p) => p.value === address.province)
  districts.value = []
  wards.value = []
  if (!keepSelection) {
    address.district = ''
    address.ward = ''
  }
  if (!prov) return
  try {
    const list = await fetchGHNDistrictsByProvince(prov.code)
    districts.value = (list || []).map((d: any) => ({
      value: d.DistrictName,
      label: d.DistrictName,
      code: d.DistrictID,
    }))
    if (keepSelection && prevDistrict) {
      address.district = prevDistrict
      await onDistrictChange(true, prevWard)
    }
  } catch {
    districts.value = []
  }
}

async function onDistrictChange(keepSelection = false, wardToRestore: string | null = null) {
  const prevWard = wardToRestore || address.ward
  const dist = districts.value.find((d) => d.value === address.district)
  wards.value = []
  if (!keepSelection) {
    address.ward = ''
  }
  if (!dist) return
  try {
    const list = await fetchGHNWards(dist.code)
    wards.value = (list || []).map((w: any) => ({
      value: w.WardName,
      label: w.WardName,
    }))
    if (keepSelection && prevWard) {
      address.ward = prevWard
    }
  } catch {
    wards.value = []
  }
}

async function loadProfile() {
  try {
    const res = await getMe()
    if (res?.data) {
      userStore.profile = res.data
      localStorage.setItem('customer_profile', JSON.stringify(res.data))
    }
  } catch {
    // ignore
  }

  const profile = userStore.profile
  if (!profile) return
  form.name = profile.tenKhachHang || ''
  form.phone = profile.soDienThoai || ''
  // Prefer default address from listDiaChi, fallback to top-level fields
  const defaultAddr =
    profile.listDiaChi?.find((a) => a.macDinh) ||
    (profile.listDiaChi && profile.listDiaChi.length > 0 ? profile.listDiaChi[0] : null)
  address.street = defaultAddr?.diaChiCuThe || (profile as any).diaChiCuThe || (profile as any).diaChi || ''
  address.province = defaultAddr?.thanhPho || (profile as any).thanhPho || ''
  address.district = defaultAddr?.quan || (profile as any).quan || ''
  address.ward = defaultAddr?.phuong || (profile as any).phuong || ''

  if (!provinces.value.length) {
    await loadProvinces()
  }
  if (address.province) {
    await onProvinceChange(true)
  }
  if (address.district) {
    await onDistrictChange(true)
  }
}

async function loadOrders() {
  if (!userStore.id) return
  loadingOrders.value = true
  try {
    const profile = userStore.profile
    const res = await fetchCustomerOrders(
      userStore.id,
      profile?.email,
      profile?.soDienThoai,
      0,
      20
    )
    if (res?.data) {
      const data = res.data as any
      const orderList = Array.isArray(data.content) ? data.content : (Array.isArray(data) ? data : [])
      // Sort by latest first - try multiple date fields and use ID as final fallback
      orders.value = orderList.sort((a: any, b: any) => {
        // Try update date first (most recent activity)
        const updateA = a.updateAt || a.ngayCapNhat
        const updateB = b.updateAt || b.ngayCapNhat
        if (updateA && updateB) {
          const timeA = new Date(updateA).getTime()
          const timeB = new Date(updateB).getTime()
          if (!isNaN(timeA) && !isNaN(timeB) && timeA !== timeB) {
            return timeB - timeA
          }
        }

        // Try creation date
        const createA = a.createAt || a.ngayTao
        const createB = b.createAt || b.ngayTao
        if (createA && createB) {
          const timeA = new Date(createA).getTime()
          const timeB = new Date(createB).getTime()
          if (!isNaN(timeA) && !isNaN(timeB) && timeA !== timeB) {
            return timeB - timeA
          }
        }

        // Fallback to ID (higher ID = newer order)
        const idA = a.id || 0
        const idB = b.id || 0
        return idB - idA
      })
    }
  } catch {
    orders.value = []
  } finally {
    loadingOrders.value = false
  }
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return '—'
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return dateStr
  }
}

function getStatusClass(status?: string): string {
  if (!status) return 'status-unknown'
  const normalized = status.toLowerCase()
  if (normalized.includes('hoàn thành') || normalized.includes('đã giao')) return 'status-completed'
  if (normalized.includes('đang') || normalized.includes('xử lý')) return 'status-processing'
  if (normalized.includes('hủy')) return 'status-cancelled'
  if (normalized.includes('chờ')) return 'status-pending'
  return 'status-default'
}

onMounted(() => {
  void loadProvinces()
  void loadProfile()
  void loadOrders()
})

function goLogin() {
  router.push('/login')
}

function openConfirm() {
  showConfirm.value = true
}

function closeConfirm() {
  showConfirm.value = false
}

async function onSave() {
  if (saving.value) return
  saving.value = true
  message.value = ''
  try {
    const res = await updateProfile({
      tenKhachHang: form.name,
      soDienThoai: form.phone,
      thanhPho: address.province,
      quan: address.district,
      phuong: address.ward,
      diaChiCuThe: address.street,
    } as any)
    if (!res?.success) {
      throw new Error(res?.message || 'Cập nhật không thành công')
    }

    // refresh profile
    const me = await getMe()
    if (me?.success && me?.data) {
      userStore.profile = me.data
      localStorage.setItem('customer_profile', JSON.stringify(me.data))
    } else {
      throw new Error(me?.message || 'Không thể tải lại hồ sơ')
    }
    message.value = 'Đã lưu thông tin.'
    showConfirm.value = false
  } catch (error: any) {
    message.value = error?.message || 'Lưu không thành công.'
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.profile-page {
  max-width: 1400px;
  margin: 32px auto;
  padding: 0 16px;
}

.profile-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  align-items: start;
}

@media (max-width: 1024px) {
  .profile-container {
    grid-template-columns: 1fr;
  }
}

.profile-card {
  position: sticky;
  top: 24px;
}

.card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.04);
}

.card__header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
}

.card__header p {
  margin: 0;
  color: #6b7280;
}

.form {
  display: grid;
  gap: 16px;
  margin-top: 16px;
}

.form__group {
  display: grid;
  gap: 6px;
}

label {
  font-weight: 600;
  color: #111;
}

input,
textarea,
select {
  width: 100%;
  border: 1.5px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px 16px;
  font-size: 15px;
  font-family: inherit;
  box-sizing: border-box;
  background: #ffffff;
  transition: all 0.2s ease;
}

textarea {
  resize: vertical;
}

input:focus,
textarea:focus,
select:focus {
  outline: none;
  border-color: #111;
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.05);
}

select:disabled,
option:disabled {
  background: #f9fafb;
  color: #9ca3af;
  cursor: not-allowed;
}

.form__actions {
  display: flex;
  justify-content: flex-end;
}

button {
  background: #111;
  color: #fff;
  border: 1px solid #111;
  border-radius: 8px;
  padding: 10px 16px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.15s ease;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal-dialog {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  width: 340px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
}

.modal-dialog h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
}

.modal-dialog p {
  margin: 0 0 16px 0;
  color: #4b5563;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-actions .ghost {
  background: #f5f5f5;
  color: #111;
  border: 1px solid #e5e7eb;
}

.dialog-actions .primary {
  background: #111;
  color: #fff;
  border: 1px solid #111;
}

.form__message {
  margin: 0;
  color: #111;
  font-size: 14px;
}

.order-history {
  /* No margin-top needed since we're in a grid */
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-count {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
  padding: 4px 12px;
  background: #f3f4f6;
  border-radius: 20px;
}

.order-history__loading {
  padding: 48px 24px;
  text-align: center;
  color: #6b7280;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f4f6;
  border-top-color: #111;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.order-history__empty {
  padding: 64px 24px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.empty-icon {
  color: #d1d5db;
  margin-bottom: 8px;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #111;
  margin: 0;
}

.empty-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.shop-now-button {
  margin-top: 8px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #111;
  color: #fff;
  text-decoration: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.2s ease;
}

.shop-now-button:hover {
  background: #000;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* Order Search */
.order-search {
  margin-bottom: 20px;
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 16px;
  color: #9ca3af;
  pointer-events: none;
}

.search-input {
  width: 100%;
  padding: 14px 48px 14px 48px;
  border: 1.5px solid #e5e7eb;
  border-radius: 12px;
  font-size: 15px;
  transition: all 0.2s ease;
  background: #fff;
}

.search-input:focus {
  outline: none;
  border-color: #111;
  box-shadow: 0 0 0 4px rgba(17, 17, 17, 0.04);
}

.search-input::placeholder {
  color: #9ca3af;
}

.clear-search {
  position: absolute;
  right: 12px;
  padding: 6px;
  background: transparent;
  border: none;
  cursor: pointer;
  color: #9ca3af;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: all 0.15s ease;
}

.clear-search:hover {
  background: rgba(17, 17, 17, 0.05);
  color: #111;
}

.search-no-results {
  padding: 16px;
  text-align: center;
  color: #6b7280;
  font-size: 14px;
  margin-top: 12px;
  background: #f9fafb;
  border-radius: 8px;
}

.order-history__list {
  display: grid;
  gap: 16px;
  margin-top: 20px;
}

.order-card {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  background: #fff;
  transition: all 0.2s ease;
}

.order-card:hover {
  border-color: #d1d5db;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transform: translateY(-2px);
}

.order-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f3f4f6;
}

.order-code {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #111;
  font-size: 15px;
}

.order-code svg {
  color: #6b7280;
  flex-shrink: 0;
}

.order-status {
  font-size: 12px;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 20px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-completed {
  background: #dcfce7;
  color: #166534;
}

.status-processing {
  background: #dbeafe;
  color: #1e40af;
}

.status-pending {
  background: #fef3c7;
  color: #92400e;
}

.status-cancelled {
  background: #fee2e2;
  color: #991b1b;
}

.status-default,
.status-unknown {
  background: #f3f4f6;
  color: #4b5563;
}

.order-card__body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
}

.order-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
}

.meta-item svg {
  flex-shrink: 0;
  color: #9ca3af;
}

.order-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
}

.total-label {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

.total-amount {
  font-size: 18px;
  font-weight: 700;
  color: #111;
}

.order-card__footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
}

.view-details-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #fff;
  color: #111;
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
  border: 1.5px solid #e5e7eb;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.view-details-btn:hover {
  background: #111;
  color: #fff;
  border-color: #111;
}

.view-details-btn svg {
  transition: transform 0.2s ease;
}

.view-details-btn:hover svg {
  transform: translateX(3px);
}

@media (max-width: 640px) {
  .order-card {
    padding: 16px;
  }

  .order-code {
    font-size: 14px;
  }

  .order-meta {
    flex-direction: column;
    gap: 8px;
  }

  .total-amount {
    font-size: 16px;
  }
}
</style>
