<template>
  <div class="profile-page" v-if="userStore.isAuthenticated">
    <div class="card">
      <header class="card__header">
        <h1>Hồ sơ của tôi</h1>
        <p>Cập nhật tên, số điện thoại và địa chỉ giao hàng.</p>
      </header>

<form class="form" @submit.prevent="onSave">
        <div class="form__group">
          <label for="name">Họ và tên</label>
          <input
            id="name"
            type="text"
            v-model.trim="form.name"
            autocomplete="name"
            required
          />
        </div>

        <div class="form__group">
          <label for="phone">Số điện thoại</label>
          <input
            id="phone"
            type="tel"
            v-model.trim="form.phone"
            autocomplete="tel"
            required
          />
        </div>

        <div class="form__group">
          <label for="province">Tỉnh/Thành phố</label>
          <select id="province" v-model="address.province" @change="onProvinceChange">
            <option disabled value="">Chọn tỉnh/thành</option>
            <option v-for="p in provinces" :key="p.code" :value="p.value">
              {{ p.label }}
            </option>
          </select>
        </div>

        <div class="form__group">
          <label for="district">Quận/Huyện</label>
          <select
            id="district"
            v-model="address.district"
            :disabled="!districts.length"
            @change="onDistrictChange"
          >
            <option disabled value="">Chọn quận/huyện</option>
            <option v-for="d in districts" :key="d.code" :value="d.value">
              {{ d.label }}
            </option>
          </select>
        </div>

        <div class="form__group">
          <label for="ward">Phường/Xã</label>
          <select id="ward" v-model="address.ward" :disabled="!wards.length">
            <option disabled value="">Chọn phường/xã</option>
            <option v-for="w in wards" :key="w.value" :value="w.value">
              {{ w.label }}
            </option>
          </select>
        </div>

        <div class="form__group">
          <label for="street">Địa chỉ cụ thể</label>
          <input
            id="street"
            type="text"
            v-model.trim="address.street"
            autocomplete="street-address"
            placeholder="Số nhà, đường..."
          />
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

    <div class="card order-history" v-if="userStore.isAuthenticated">
      <header class="card__header">
        <h2>Lịch sử mua hàng</h2>
      </header>
      <div v-if="loadingOrders" class="order-history__loading">Đang tải...</div>
      <div v-else-if="orders.length === 0" class="order-history__empty">
        <p>Bạn chưa có đơn hàng nào.</p>
      </div>
      <div v-else class="order-history__list">
        <div v-for="order in orders" :key="order.id" class="order-item">
          <div class="order-item__header">
            <div class="order-item__info">
              <span class="order-item__code">Mã đơn: {{ order.maHoaDon || '—' }}</span>
              <span class="order-item__date">{{ formatDate(order.createAt || order.ngayTao) }}</span>
            </div>
            <span class="order-item__status">{{ order.trangThai || '—' }}</span>
          </div>
          <div class="order-item__details">
            <div class="order-item__total">
              <span>Tổng tiền:</span>
              <strong>{{ formatCurrency(order.tongTienSauGiam || order.tongTien || 0) }}</strong>
            </div>
            <div v-if="order.hoaDonChiTiets?.length || order.items?.length" class="order-item__products">
              <span class="order-item__products-label">Sản phẩm:</span>
              <span class="order-item__products-count">
                {{ (order.hoaDonChiTiets || order.items || []).length }} sản phẩm
              </span>
            </div>
          </div>
          <div class="order-item__actions">
            <RouterLink :to="`/order-lookup?code=${order.maHoaDon}`" class="order-item__link">
              Xem chi tiết
            </RouterLink>
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
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { RouterLink } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { updateProfile, getMe } from '@/api/auth'
import { fetchGHNProvinces, fetchGHNDistrictsByProvince, fetchGHNWards } from '@/api/shipping'
import { fetchCustomerOrders, type OrderTrackingDetail } from '@/api/orders'
import { formatCurrency } from '@/utils/currency'

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
      orders.value = Array.isArray(data.content) ? data.content : (Array.isArray(data) ? data : [])
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
  max-width: 720px;
  margin: 32px auto;
  padding: 0 16px;
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
  margin-top: 32px;
}

.order-history__loading,
.order-history__empty {
  padding: 24px;
  text-align: center;
  color: #6b7280;
}

.order-history__list {
  display: grid;
  gap: 16px;
  margin-top: 16px;
}

.order-item {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  background: #fff;
}

.order-item__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.order-item__info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-item__code {
  font-weight: 600;
  color: #111;
  font-size: 15px;
}

.order-item__date {
  font-size: 13px;
  color: #6b7280;
}

.order-item__status {
  font-size: 13px;
  color: #6b7280;
  padding: 4px 8px;
  background: #f3f4f6;
  border-radius: 4px;
}

.order-item__details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.order-item__total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.order-item__total strong {
  font-size: 16px;
  color: #111;
}

.order-item__products {
  display: flex;
  gap: 8px;
  font-size: 13px;
  color: #6b7280;
}

.order-item__products-label {
  font-weight: 500;
}

.order-item__actions {
  display: flex;
  justify-content: flex-end;
}

.order-item__link {
  color: #111;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  padding: 6px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  transition: all 0.15s ease;
}

.order-item__link:hover {
  background: #f9fafb;
  border-color: #111;
}
</style>

