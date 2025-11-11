<template>
  <div class="checkout-page">
    <div class="container">
      <h1>{{ $t('cart.checkout') }}</h1>
      <div class="grid">
        <section class="details">
          <form class="form" @submit.prevent>
            <div class="row two">
              <div>
                <label for="fullName">{{ $t('checkout.fullName') }}</label>
                <input id="fullName" v-model.trim="contact.fullName" :class="{ invalid: errs.fullName }" type="text" placeholder="Nguyen Van A" />
                <small v-if="errs.fullName" class="error-text">{{ errs.fullName }}</small>
              </div>
              <div>
                <label for="phone">{{ $t('checkout.phone') }}</label>
                <input id="phone" v-model.trim="contact.phone" :class="{ invalid: errs.phone }" type="tel" placeholder="090..." />
                <small v-if="errs.phone" class="error-text">{{ errs.phone }}</small>
              </div>
            </div>
            <div class="row">
              <label for="email">{{ $t('checkout.email') }}</label>
              <input id="email" v-model.trim="contact.email" :class="{ invalid: errs.email }" type="email" placeholder="you@example.com" />
              <small v-if="errs.email" class="error-text">{{ errs.email }}</small>
            </div>
            <div class="row two">
              <div>
                <label for="province">{{ $t('checkout.province') }}</label>
                <select id="province" v-model="address.province" @change="onProvinceChange" :class="{ invalid: errs.province }">
                  <option value="">{{ '—' }}</option>
                  <option v-for="p in provinces" :key="p.code" :value="p.value">{{ p.label }}</option>
                </select>
                <small v-if="errs.province" class="error-text">{{ errs.province }}</small>
              </div>
              <div>
                <label for="district">{{ $t('checkout.district') }}</label>
                <select id="district" v-model="address.district" @change="onDistrictChange" :disabled="!districts.length" :class="{ invalid: errs.district }">
                  <option value="">{{ '—' }}</option>
                  <option v-for="d in districts" :key="d.code" :value="d.value">{{ d.label }}</option>
                </select>
                <small v-if="errs.district" class="error-text">{{ errs.district }}</small>
              </div>
            </div>
            <div class="row two">
              <div>
                <label for="ward">{{ $t('checkout.ward') }}</label>
                <select id="ward" v-model="address.ward" :disabled="!wards.length" :class="{ invalid: errs.ward }">
                  <option value="">{{ '—' }}</option>
                  <option v-for="w in wards" :key="w.value" :value="w.value">{{ w.label }}</option>
                </select>
                <small v-if="errs.ward" class="error-text">{{ errs.ward }}</small>
              </div>
              <div>
                <label for="street">{{ $t('checkout.street') }}</label>
                <input id="street" v-model.trim="address.street" :class="{ invalid: errs.street }" type="text" placeholder="123 Street" />
                <small v-if="errs.street" class="error-text">{{ errs.street }}</small>
              </div>
            </div>
            <div class="row">
              <label for="bank">{{ $t('checkout.bankOptional') }}</label>
              <select id="bank" v-model="bankCode">
                <option value="">{{ '—' }}</option>
                <option value="NCB">NCB</option>
                <option value="VCB">VCB</option>
                <option value="ABB">ABBANK</option>
              </select>
            </div>
          </form>
        </section>
        <section class="summary">
          <h2>{{ $t('cart.summary') }}</h2>
          <div class="row">
            <span>{{ $t('cart.subtotal') }}</span>
            <span>{{ cartCount === 0 ? '—' : formatCurrency(cartTotal) }}</span>
          </div>
          <div class="row muted">
            <span>{{ $t('cart.estimatedDelivery') }}</span>
            <span>{{ cartCount === 0 ? '—' : formatCurrency(shippingFee) }}</span>
          </div>
          <div class="row muted">
            <span>VAT (10%)</span>
            <span>{{ cartCount === 0 ? '—' : formatCurrency(vat) }}</span>
          </div>
          <hr />
          <div class="row total">
            <span>{{ $t('cart.total') }}</span>
            <span>{{ cartCount === 0 ? '—' : formatCurrency(orderTotal) }}</span>
          </div>
          <div class="actions">
            <button class="btn btn-block" :disabled="cartCount === 0 || paying" @click="payWithVnpay">
              <span v-if="!paying">{{ $t('cart.checkout') }} — VNPAY</span>
              <span v-else>{{ $t('cart.processing') }}</span>
            </button>
          </div>
          <p v-if="error" class="error">{{ error }}</p>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useCartStore } from '@/stores/cart'
import { formatCurrency } from '@/utils/currency'
import { createVnPayPayment } from '@/api/payment'

const cartStore = useCartStore()
const { cartCount, cartTotal } = storeToRefs(cartStore)

const shippingFee = 250000
const vat = computed(() => Math.round((cartTotal.value + shippingFee) * 0.1))
const orderTotal = computed(() => cartTotal.value + shippingFee + vat.value)

const paying = ref(false)
const error = ref('')
const bankCode = ref<string>('')
const contact = ref({ fullName: '', email: '', phone: '', address: '' })

// Address picker (provinces.open-api.vn)
const provinces = ref<{ value: string; label: string; code: number }[]>([])
const districts = ref<{ value: string; label: string; code: number }[]>([])
const wards = ref<{ value: string; label: string }[]>([])
const address = ref({ province: '', district: '', ward: '', street: '' })

// Validation errors
const errs = ref<Record<string, string>>({
  fullName: '',
  email: '',
  phone: '',
  province: '',
  district: '',
  ward: '',
  street: '',
})

function validateDetails(): boolean {
  errs.value = { fullName: '', email: '', phone: '', province: '', district: '', ward: '', street: '' }
  let ok = true
  if (!contact.value.fullName.trim()) {
    errs.value.fullName = $t('checkout.err.fullName') as string
    ok = false
  }
  const email = contact.value.email.trim()
  if (!email || !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email)) {
    errs.value.email = $t('checkout.err.email') as string
    ok = false
  }
  const phone = contact.value.phone.trim()
  if (!phone || !/^[0-9+\-\s]{8,15}$/.test(phone)) {
    errs.value.phone = $t('checkout.err.phone') as string
    ok = false
  }
  if (!address.value.province) { errs.value.province = $t('checkout.err.province') as string; ok = false }
  if (!address.value.district) { errs.value.district = $t('checkout.err.district') as string; ok = false }
  if (!address.value.ward) { errs.value.ward = $t('checkout.err.ward') as string; ok = false }
  if (!address.value.street.trim()) { errs.value.street = $t('checkout.err.street') as string; ok = false }
  return ok
}

async function loadProvinces() {
  try {
    const res = await fetch('https://provinces.open-api.vn/api/p/')
    const data = await res.json()
    provinces.value = data.map((p: any) => ({ value: p.name, label: p.name, code: p.code }))
  } catch {
    // ignore
  }
}
loadProvinces()

async function onProvinceChange() {
  districts.value = []
  wards.value = []
  address.value.district = ''
  address.value.ward = ''
  const p = provinces.value.find((x) => x.value === address.value.province)
  if (!p) return
  try {
    const res = await fetch(`https://provinces.open-api.vn/api/p/${p.code}?depth=2`)
    const data = await res.json()
    districts.value = (data.districts || []).map((d: any) => ({ value: d.name, label: d.name, code: d.code }))
  } catch {
    // ignore
  }
}

async function onDistrictChange() {
  wards.value = []
  address.value.ward = ''
  const d = districts.value.find((x) => x.value === address.value.district)
  if (!d) return
  try {
    const res = await fetch(`https://provinces.open-api.vn/api/d/${d.code}?depth=2`)
    const data = await res.json()
    wards.value = (data.wards || []).map((w: any) => ({ value: w.name, label: w.name }))
  } catch {
    // ignore
  }
}

async function payWithVnpay() {
  if (cartCount.value === 0 || paying.value) return
  if (!validateDetails()) {
    const first = document.querySelector('.details .invalid') as HTMLElement | null
    if (first?.focus) first.focus()
    return
  }
  paying.value = true
  error.value = ''
  try {
    const res = await createVnPayPayment({
      amount: Math.round(orderTotal.value),
      orderInfo: 'Thanh toan don hang',
      locale: 'vn',
      bankCode: bankCode.value || undefined
    })
    const url = res.data?.payUrl
    if (!url) {
      throw new Error('Missing payUrl')
    }
    window.location.href = url
  } catch (e: any) {
    error.value = e?.message || 'Payment init failed'
    paying.value = false
  }
}
</script>

<style scoped lang="scss">
.checkout-page { padding: 24px 0 48px; }
.grid {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 24px;
}
.details {
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 16px;
  padding: 20px;
}
.form { display: grid; gap: 12px; }
.row { display: grid; gap: 6px; }
.row.two { grid-template-columns: 1fr 1fr; gap: 12px; }
.row.two > div { min-width: 0; }
.row label { font-size: 13px; color: #4e5969; }
.row input, .row select { width: 100%; border: 1px solid #e5e5e5; border-radius: 8px; padding: 10px 12px; font: inherit; }
.row .invalid { border-color: #dc2626; outline: 0; box-shadow: 0 0 0 3px rgba(220,38,38,.12); }
.error-text { color: #dc2626; font-size: 12px; }
.summary {
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.06);
}
.summary h2 { margin: 0 0 12px 0; }
.row { display: flex; justify-content: space-between; padding: 10px 0; }
.row.muted span:first-child { color: #4e5969; }
.row.total span:first-child { font-weight: 700; }
.actions { margin-top: 12px; }
.error { color: #c53030; margin: 12px 0 0; }

@media (max-width: 980px) {
  .grid { grid-template-columns: 1fr; }
}
</style>




