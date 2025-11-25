<template>
  <div class="order-lookup">
    <div class="container narrow">
      <header class="lookup-hero">
        <p class="eyebrow">{{ $t('orderLookup.eyebrow') }}</p>
        <h1>{{ $t('orderLookup.title') }}</h1>
        <p class="lead">{{ $t('orderLookup.subtitle') }}</p>
      </header>

      <div class="lookup-grid">
        <form class="lookup-form" @submit.prevent="lookupOrder">
          <label class="field">
            <span>{{ $t('orderLookup.form.codeLabel') }}</span>
            <input
              v-model.trim="orderCode"
              type="text"
              :placeholder="$t('orderLookup.form.codePlaceholder')"
              autocomplete="off"
              inputmode="text"
              @blur="orderCode = orderCode.toUpperCase()"
            />
            <small v-if="formErrors.code" class="field-error">{{ formErrors.code }}</small>
          </label>

          <label class="field" v-if="isAuthenticated">
            <span>{{ $t('orderLookup.form.contactLabel') }}</span>
            <input
              v-model.trim="contactValue"
              type="text"
              :placeholder="$t('orderLookup.form.contactPlaceholder')"
              autocomplete="off"
              inputmode="text"
            />
            <small v-if="formErrors.contact" class="field-error">{{ formErrors.contact }}</small>
          </label>
          <p v-else class="form-hint">
            {{ $t('orderLookup.form.guestHint') }}
          </p>

          <button class="submit-btn" type="submit" :disabled="isLoading">
            <span class="btn-content">
              <span v-if="isLoading" class="inline-spinner" aria-hidden="true"></span>
              {{ isLoading ? $t('orderLookup.form.checking') : $t('orderLookup.form.submit') }}
            </span>
          </button>

          <p class="form-hint">{{ $t('orderLookup.form.hint') }}</p>
        </form>

        <div class="result-card" :class="resultCardClass">
          <template v-if="state === 'success' && order">
            <div class="result-head">
              <div class="status-chip" :class="statusTone">
                {{ currentStatusLabel }}
              </div>
              <p class="muted">
                {{ $t('orderLookup.status.updated') }}
                <strong>{{ statusUpdatedLabel }}</strong>
              </p>
            </div>
            <dl class="result-grid">
              <div>
                <dt>{{ $t('orderLookup.results.orderCode') }}</dt>
                <dd>{{ orderCodeDisplay }}</dd>
              </div>
              <div>
                <dt>{{ $t('orderLookup.results.placedOn') }}</dt>
                <dd>{{ placedOnLabel }}</dd>
              </div>
              <div>
                <dt>{{ $t('orderLookup.results.total') }}</dt>
                <dd>{{ totalLabel }}</dd>
              </div>
              <div>
                <dt>{{ $t('orderLookup.results.shippingTo') }}</dt>
                <dd>{{ shippingAddress || '—' }}</dd>
              </div>
              <div v-if="isAuthenticated">
                <dt>{{ $t('orderLookup.results.contact') }}</dt>
                <dd>{{ contactSummary }}</dd>
              </div>
            </dl>
          </template>

          <template v-else-if="state === 'error'">
            <h3>{{ $t('orderLookup.error.title') }}</h3>
            <p class="muted">{{ errorMessage || $t('orderLookup.error.generic') }}</p>
            <RouterLink to="/" class="text-link">{{ $t('orderLookup.help') }}</RouterLink>
          </template>

          <template v-else>
            <h3>{{ $t('orderLookup.empty.title') }}</h3>
            <p class="muted">{{ $t('orderLookup.empty.subtitle') }}</p>
            <RouterLink to="/" class="text-link">{{ $t('orderLookup.help') }}</RouterLink>
          </template>
        </div>
      </div>

      <section class="timeline-section" v-if="state === 'success'">
        <div class="section-heading">
          <h2>{{ $t('orderLookup.timeline.heading') }}</h2>
          <span class="muted">{{ timelineEvents.length }} updates</span>
        </div>

        <div v-if="timelineEvents.length" class="timeline-list">
          <article v-for="item in timelineEvents" :key="item.id" class="timeline-item">
            <div class="timeline-marker"></div>
            <div class="timeline-body">
              <div class="timeline-meta">
                <span class="time">{{ formatDateTime(item.thoiGian) }}</span>
                <span class="actor" v-if="item.tenNhanVien">{{ item.tenNhanVien }}</span>
              </div>
              <h3>{{ item.trangThaiMoi }}</h3>
              <p class="muted">{{ item.hanhDong }}</p>
              <p v-if="item.moTa" class="note">{{ item.moTa }}</p>
              <p v-if="item.ghiChu" class="note">{{ item.ghiChu }}</p>
            </div>
          </article>
        </div>
        <div v-else class="timeline-empty">
          <p class="muted">{{ $t('orderLookup.timeline.emptyTitle') }}</p>
          <p>{{ $t('orderLookup.timeline.emptySubtitle') }}</p>
        </div>
      </section>

      <section class="items-section" v-if="state === 'success'">
        <div class="section-heading">
          <h2>{{ $t('orderLookup.items.heading') }}</h2>
          <span class="muted" v-if="orderItems.length">{{ orderItems.length }} items</span>
        </div>

        <div v-if="orderItems.length" class="items-table">
          <div class="items-header">
            <span>{{ $t('orderLookup.items.product') }}</span>
            <span>{{ $t('orderLookup.items.variant') }}</span>
            <span>{{ $t('orderLookup.items.quantity') }}</span>
            <span>{{ $t('orderLookup.items.price') }}</span>
            <span>{{ $t('orderLookup.items.subtotal') }}</span>
          </div>
          <div v-for="item in orderItems" :key="item.id" class="items-row">
            <span class="product-name">{{ item.name }}</span>
            <span class="product-variant">{{ item.variant }}</span>
            <span>{{ item.quantity }}</span>
            <span>{{ formatCurrency(item.price) }}</span>
            <span>{{ formatCurrency(item.subtotal) }}</span>
          </div>
        </div>
        <div v-else class="items-empty">
          <p>{{ $t('orderLookup.items.empty') }}</p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  fetchLatestOrderStatus,
  fetchOrderById,
  fetchOrderByCode,
  fetchOrderTimeline,
  type OrderStatusSnapshot,
  type OrderTimelineEntry,
  type OrderTrackingDetail,
} from '@/api/orders'
import { formatCurrency } from '@/utils/currency'
import { useUserStore } from '@/stores/user'

type LookupState = 'idle' | 'loading' | 'success' | 'error'

const orderCode = ref('')
const contactValue = ref('')
const state = ref<LookupState>('idle')
const errorMessage = ref('')
const order = ref<OrderTrackingDetail | null>(null)
const timeline = ref<OrderTimelineEntry[]>([])
const snapshot = ref<OrderStatusSnapshot | null>(null)
const formErrors = reactive<{ code?: string; contact?: string }>({})
const { t } = useI18n()
const userStore = useUserStore()
const isAuthenticated = computed(() => userStore.isAuthenticated)

function hydrateContactFromProfile() {
  if (!isAuthenticated.value) return
  if (contactValue.value) return
  const profile = userStore.profile
  contactValue.value = profile?.email || profile?.soDienThoai || ''
}

hydrateContactFromProfile()

watch(isAuthenticated, (loggedIn) => {
  if (loggedIn) {
    hydrateContactFromProfile()
  } else {
    contactValue.value = ''
    formErrors.contact = undefined
  }
})

const isLoading = computed(() => state.value === 'loading')

const orderCodeDisplay = computed(() => {
  if (!order.value) return '—'
  if (order.value.maHoaDon) return order.value.maHoaDon
  if (order.value.id) return `HD${String(order.value.id).padStart(6, '0')}`
  return '—'
})

const placedOnLabel = computed(() => formatDateTime(order.value?.ngayTao || order.value?.createAt))

const totalLabel = computed(() => {
  const amount = order.value?.tongTienSauGiam ?? order.value?.tongTien ?? 0
  return formatCurrency(amount)
})

const shippingAddress = computed(() => {
  return (
    order.value?.diaChiNhanHang ||
    order.value?.diaChiNguoiNhan ||
    order.value?.diaChi ||
    ''
  )
})

const contactSummary = computed(() => {
  const email = order.value?.emailNguoiNhan || order.value?.email || order.value?.emailKhachHang
  const phone =
    order.value?.soDienThoaiNguoiNhan ||
    order.value?.soDienThoai ||
    order.value?.soDienThoaiKhachHang
  if (email && phone) return `${email} · ${phone}`
  return email || phone || '—'
})

const statusUpdatedLabel = computed(() => {
  const source =
    snapshot.value?.thoiGian ||
    timeline.value.at(-1)?.thoiGian ||
    order.value?.ngayTao ||
    null
  return formatDateTime(source)
})

const currentStatusLabel = computed(() => {
  return (
    snapshot.value?.tenTrangThaiDonHang ||
    timeline.value.at(-1)?.trangThaiMoi ||
    t('orderLookup.status.unknown')
  )
})

const statusTone = computed(() => {
  const key = (currentStatusLabel.value || '').toLowerCase()
  if (key.includes('hủy') || key.includes('cancel')) return 'tone-danger'
  if (key.includes('giao') || key.includes('delivered')) return 'tone-success'
  if (key.includes('chuẩn') || key.includes('processing')) return 'tone-warn'
  return 'tone-neutral'
})

const orderItems = computed(() => {
  if (!order.value?.hoaDonChiTiets?.length) return []
  return order.value.hoaDonChiTiets.map((item, index) => {
    const productName =
      item.tenSanPhamChiTiet ||
      item.idChiTietSanPham?.idSanPham?.tenSanPham ||
      t('orderLookup.items.product')
    const color =
      item.idChiTietSanPham?.idMauSac?.tenMauSac ||
      item.idChiTietSanPham?.idMauSac?.tenMau
    const size =
      item.idChiTietSanPham?.idKichThuoc?.tenKichThuoc ||
      item.idChiTietSanPham?.idKichThuoc?.kichThuoc
    const variantParts = [color, size].filter(Boolean)
    const variant = variantParts.length ? variantParts.join(' · ') : t('orderLookup.items.variant')
    const quantity = item.soLuong ?? 0
    const price = item.giaBan ?? 0
    const subtotal = item.thanhTien ?? quantity * price
    return {
      id: item.id ?? index,
      name: productName,
      variant,
      quantity,
      price,
      subtotal,
    }
  })
})

const timelineEvents = computed(() => {
  return [...timeline.value].sort((a, b) => {
    const timeA = Date.parse(a.thoiGian || '')
    const timeB = Date.parse(b.thoiGian || '')
    return timeA - timeB
  })
})

const resultCardClass = computed(() => {
  if (state.value === 'success') return 'result-card--success'
  if (state.value === 'error') return 'result-card--error'
  return 'result-card--idle'
})

async function lookupOrder() {
  resetState()
  if (!validateForm()) {
    state.value = 'idle'
    return
  }

  const code = orderCode.value.trim().toUpperCase()
  if (!code) {
    formErrors.code = t('orderLookup.error.invalidCode')
    state.value = 'idle'
    return
  }

  const hasLetters = /[A-Za-z]/.test(code)
  const extractedId = extractOrderId(code)
  if (!hasLetters && !extractedId) {
    formErrors.code = t('orderLookup.error.invalidCode')
    state.value = 'idle'
    return
  }

  state.value = 'loading'
  try {
    const orderRequest = hasLetters
      ? fetchOrderByCode(code)
      : fetchOrderById(extractedId as number)
    const orderRes = await orderRequest
    const orderData = orderRes.data
    if (!orderData) {
      throw new Error(t('orderLookup.error.notFound'))
    }
    if (isAuthenticated.value && !matchesContact(orderData, contactValue.value)) {
      throw new Error(t('orderLookup.error.contactMismatch'))
    }

    order.value = orderData

    const orderIdForTimeline = orderData.id ?? extractedId ?? null
    if (orderIdForTimeline) {
      const [timelineRes, statusRes] = await Promise.allSettled([
        fetchOrderTimeline(orderIdForTimeline),
        fetchLatestOrderStatus(orderIdForTimeline),
      ])
      if (timelineRes.status === 'fulfilled' && Array.isArray(timelineRes.value.data)) {
        timeline.value = timelineRes.value.data
      } else {
        timeline.value = []
      }
      if (statusRes.status === 'fulfilled') {
        snapshot.value = statusRes.value.data
      } else {
        snapshot.value = null
      }
    } else {
      timeline.value = []
      snapshot.value = null
    }

    errorMessage.value = ''
    state.value = 'success'
  } catch (error: any) {
    console.warn('Order lookup failed', error)
    errorMessage.value =
      error?.message ?? t('orderLookup.error.generic')
    order.value = null
    timeline.value = []
    snapshot.value = null
    state.value = 'error'
  }
}

function resetState() {
  formErrors.code = undefined
  formErrors.contact = undefined
  errorMessage.value = ''
  if (state.value !== 'loading') {
    order.value = null
    timeline.value = []
    snapshot.value = null
  }
}

function validateForm() {
  const code = orderCode.value.trim()
  const contact = contactValue.value.trim()
  if (!code) {
    formErrors.code = t('orderLookup.form.codeError')
  }
  if (isAuthenticated.value) {
    if (!contact) {
      formErrors.contact = t('orderLookup.form.contactError')
    } else if (!isEmail(contact) && !isPhone(contact)) {
      formErrors.contact = t('orderLookup.form.contactFormatError')
    }
  }
  return !formErrors.code && (!isAuthenticated.value || !formErrors.contact)
}

function extractOrderId(code: string) {
  const digits = code.replace(/\D/g, '')
  if (!digits) return null
  const parsed = Number(digits)
  if (!Number.isFinite(parsed) || parsed <= 0) return null
  return parsed
}

function matchesContact(orderData: OrderTrackingDetail, input: string) {
  if (!isAuthenticated.value) return true
  const trimmed = input.trim()
  if (!trimmed) return false
  if (isEmail(trimmed)) {
    const normalizedInput = trimmed.toLowerCase()
    const candidate =
      orderData.emailNguoiNhan || orderData.email || orderData.emailKhachHang || ''
    return candidate.toLowerCase() === normalizedInput
  }

  const normalizedInput = normalizePhone(trimmed)
  if (!normalizedInput) return false
  const candidates = [
    orderData.soDienThoaiNguoiNhan,
    orderData.soDienThoai,
    orderData.soDienThoaiKhachHang,
  ]
  return candidates.some((value) => {
    const phone = normalizePhone(value || '')
    return phone && (phone === normalizedInput || phone.endsWith(normalizedInput))
  })
}

function normalizePhone(value: string) {
  const digits = value.replace(/\D/g, '')
  return digits.startsWith('0') ? digits.slice(1) : digits
}

function isEmail(value: string) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.toLowerCase())
}

function isPhone(value: string) {
  return /\d{6,}/.test(value.replace(/\D/g, ''))
}

function formatDateTime(value?: string | null) {
  if (!value) return '—'
  const normalized = value.includes('T') ? value : value.replace(' ', 'T')
  const date = new Date(normalized)
  if (Number.isNaN(date.getTime())) return '—'
  return new Intl.DateTimeFormat(undefined, {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}
</script>

<style scoped lang="scss">
.order-lookup {
  padding: clamp(32px, 6vw, 96px) 0 clamp(48px, 8vw, 120px);
  background: linear-gradient(180deg, rgba(17, 17, 17, 0.02), rgba(17, 17, 17, 0));
}

.lookup-hero {
  text-align: center;
  margin-bottom: clamp(32px, 5vw, 56px);

  .eyebrow {
    margin: 0;
    text-transform: uppercase;
    letter-spacing: 0.12em;
    font-size: 12px;
    color: rgba(17, 17, 17, 0.55);
  }

  h1 {
    margin: 12px 0 12px;
    font-size: clamp(32px, 6vw, 44px);
    font-family: var(--font-family-serif);
    color: #111;
  }

  .lead {
    margin: 0 auto;
    max-width: 560px;
    color: rgba(17, 17, 17, 0.7);
    line-height: 1.6;
  }
}

.lookup-grid {
  display: grid;
  gap: 24px;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  margin-bottom: clamp(32px, 5vw, 64px);
}

.lookup-form,
.result-card {
  background: #fff;
  border: 1px solid rgba(17, 17, 17, 0.08);
  border-radius: 24px;
  padding: clamp(20px, 4vw, 32px);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.06);
}

.lookup-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 14px;
  color: #111;

  input {
    border: 1px solid rgba(17, 17, 17, 0.15);
    border-radius: 14px;
    padding: 12px 14px;
    font-size: 15px;
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
  }

  input:focus {
    outline: none;
    border-color: #111;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  }
}

.field-error {
  color: #d64545;
  font-size: 12px;
}

.submit-btn {
  margin-top: 8px;
  border: none;
  border-radius: 18px;
  padding: 12px 18px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  color: #fff;
  background: #111;
  transition: transform 0.1s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.2);
}

.btn-content {
  display: inline-flex;
  gap: 8px;
  align-items: center;
}

.inline-spinner {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  animation: spin 0.8s linear infinite;
}

.form-hint {
  margin: 0;
  font-size: 13px;
  color: rgba(17, 17, 17, 0.55);
}

.result-card {
  display: flex;
  flex-direction: column;
  gap: 16px;

  h3 {
    margin: 0;
    font-size: 20px;
  }
}

.result-card--success {
  border-color: rgba(53, 180, 126, 0.4);
}

.result-card--error {
  border-color: rgba(214, 69, 69, 0.4);
}

.result-head {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.status-chip {
  align-self: flex-start;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
}

.tone-danger {
  background: rgba(214, 69, 69, 0.12);
  color: #a02121;
}

.tone-success {
  background: rgba(53, 180, 126, 0.15);
  color: #1c7c4a;
}

.tone-warn {
  background: rgba(241, 196, 15, 0.15);
  color: #805a00;
}

.tone-neutral {
  background: rgba(17, 17, 17, 0.05);
  color: #111;
}

.result-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  margin: 0;

  dt {
    text-transform: uppercase;
    letter-spacing: 0.08em;
    font-size: 11px;
    color: rgba(17, 17, 17, 0.55);
    margin-bottom: 4px;
  }

  dd {
    margin: 0;
    font-size: 15px;
    font-weight: 600;
    color: #111;
  }
}

.muted {
  color: rgba(17, 17, 17, 0.6);
  margin: 0;
  font-size: 14px;
}

.text-link {
  font-weight: 600;
  color: #111;
  text-decoration: underline;
}

.section-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h2 {
    margin: 0;
    font-size: 24px;
  }
}

.timeline-section,
.items-section {
  background: #fff;
  border-radius: 24px;
  border: 1px solid rgba(17, 17, 17, 0.08);
  padding: clamp(20px, 4vw, 32px);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.06);
  margin-bottom: clamp(24px, 4vw, 40px);
}

.timeline-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.timeline-item {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 16px;
}

.timeline-marker {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #111;
  margin-top: 6px;
  position: relative;
}

.timeline-marker::after {
  content: '';
  position: absolute;
  width: 2px;
  height: calc(100% + 20px);
  background: rgba(17, 17, 17, 0.12);
  left: 50%;
  top: 12px;
  transform: translateX(-50%);
}

.timeline-item:last-child .timeline-marker::after {
  display: none;
}

.timeline-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 13px;
  color: rgba(17, 17, 17, 0.6);
  margin-bottom: 4px;
}

.timeline-body h3 {
  margin: 0;
  font-size: 18px;
}

.timeline-body .note {
  margin: 6px 0 0;
  color: rgba(17, 17, 17, 0.7);
}

.timeline-empty {
  text-align: center;
  padding: 24px;
  border-radius: 18px;
  background: rgba(17, 17, 17, 0.03);
}

.items-table {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.items-header,
.items-row {
  display: grid;
  grid-template-columns: 1.8fr 1.2fr 0.6fr 0.8fr 0.8fr;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid rgba(17, 17, 17, 0.08);
}

.items-header {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: rgba(17, 17, 17, 0.6);
}

.items-row {
  font-size: 14px;
  align-items: center;
}

.product-name {
  font-weight: 600;
}

.product-variant {
  color: rgba(17, 17, 17, 0.6);
}

.items-empty {
  padding: 24px;
  text-align: center;
  color: rgba(17, 17, 17, 0.6);
}

@keyframes spin {
  from {
    transform: rotate(0);
  }
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 768px) {
  .lookup-grid {
    grid-template-columns: 1fr;
  }

  .items-header,
  .items-row {
    grid-template-columns: 1fr 1fr;
    grid-template-areas:
      'name price'
      'variant qty'
      'subtotal subtotal';
  }

  .items-row span:nth-child(1) {
    grid-area: name;
  }

  .items-row span:nth-child(2) {
    grid-area: variant;
  }

  .items-row span:nth-child(3) {
    grid-area: qty;
  }

  .items-row span:nth-child(4) {
    grid-area: price;
  }

  .items-row span:nth-child(5) {
    grid-area: subtotal;
    font-weight: 600;
  }
}
</style>

