<template>
  <div class="checkout-page">
    <div class="container">
      <h1>{{ $t('cart.checkout') }}</h1>
      <div class="grid">
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
const orderTotal = computed(() => cartTotal.value + shippingFee)

const paying = ref(false)
const error = ref('')

async function payWithVnpay() {
  if (cartCount.value === 0 || paying.value) return
  paying.value = true
  error.value = ''
  try {
    const res = await createVnPayPayment({
      amount: Math.round(orderTotal.value),
      orderInfo: 'Thanh toan don hang',
      locale: 'vn'
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
  grid-template-columns: 1fr;
  gap: 24px;
}
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
</style>


