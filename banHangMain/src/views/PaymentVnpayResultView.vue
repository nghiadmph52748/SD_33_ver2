<template>
  <div class="payment-result">
    <div class="container narrow">
      <Transition name="fade-quick" mode="out-in">
        <article :key="status.variant" class="status-card" :class="`status--${status.variant}`">
          <header class="status-header">
            <span class="status-icon" aria-hidden="true">{{ status.icon }}</span>
            <div>
              <p class="eyebrow">{{ $t('payment.title') }}</p>
              <h1>{{ $t(status.titleKey) }}</h1>
              <p class="lead">{{ $t(status.messageKey) }}</p>
            </div>
          </header>

          <div class="actions">
            <RouterLink :to="status.primaryRoute" class="btn btn-block">
              {{ $t(status.primaryLabelKey) }}
            </RouterLink>
            <RouterLink
              v-if="status.secondaryRoute && status.secondaryLabelKey"
              :to="status.secondaryRoute!"
              class="btn btn-outline btn-block"
            >
              {{ $t(status.secondaryLabelKey!) }}
            </RouterLink>
          </div>

          <section class="details">
            <header class="details-header">
              <h2>{{ $t('payment.detailsHeading') }}</h2>
              <button
                v-if="txnRef.value"
                type="button"
                class="copy-btn pill-btn"
                @click="copyReference"
                :disabled="copying"
              >
                {{ copying ? $t('payment.details.copied') : $t('payment.details.copyRef') }}
              </button>
            </header>
            <dl>
              <div v-for="item in detailItems" :key="item.label" class="detail-row">
                <dt>{{ $t(item.label) }}</dt>
                <dd>{{ item.value }}</dd>
              </div>
            </dl>
          </section>
        </article>
      </Transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { formatCurrency } from '@/utils/currency'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const cartStore = useCartStore()
const { t } = useI18n()

// VNPAY specific fields
const code = computed(() => String(route.query.code ?? ''))
const txnRef = computed(() => String(route.query.txnRef ?? ''))
const transactionNo = computed(() => String(route.query.transactionNo ?? ''))
const bankTranNo = computed(() => String(route.query.bankTranNo ?? ''))
const bankCode = computed(() => String(route.query.bankCode ?? ''))
const cardType = computed(() => String(route.query.cardType ?? ''))
const orderInfo = computed(() => String(route.query.orderInfo ?? ''))
const payDateRaw = computed(() => String(route.query.payDate ?? ''))

const amountRaw = computed(() => Number(route.query.amount ?? 0) / 100)
const displayAmount = computed(() => {
  return amountRaw.value > 0 ? formatCurrency(amountRaw.value) : '—'
})

const formattedPayDate = computed(() => {
  const raw = payDateRaw.value
  if (!raw || raw.length !== 14) return raw || '—'
  const year = raw.slice(0, 4)
  const month = raw.slice(4, 6)
  const day = raw.slice(6, 8)
  const hour = raw.slice(8, 10)
  const minute = raw.slice(10, 12)
  const second = raw.slice(12, 14)
  return `${day}/${month}/${year} ${hour}:${minute}:${second}`
})

type StatusVariant = 'success' | 'failure' | 'pending'

interface StatusConfig {
  titleKey: string
  messageKey: string
  primaryRoute: string
  primaryLabelKey: string
  secondaryRoute?: string
  secondaryLabelKey?: string
  variant: StatusVariant
  icon: string
}

const status = computed<StatusConfig>(() => {
  // Handle VNPAY orders
  if (code.value === '00') {
    return {
      titleKey: 'payment.successTitle',
      messageKey: 'payment.successMessage',
      primaryRoute: '/',
      primaryLabelKey: 'payment.actions.continueShopping',
      secondaryRoute: '/cart',
      secondaryLabelKey: 'payment.actions.viewCart',
      variant: 'success',
      icon: '✅',
    }
  }

  if (code.value === '24') {
    // User cancelled on VNPAY UI
    return {
      titleKey: 'payment.cancelTitle',
      messageKey: 'payment.cancelMessage',
      primaryRoute: '/checkout',
      primaryLabelKey: 'payment.actions.retryCheckout',
      secondaryRoute: '/',
      secondaryLabelKey: 'payment.actions.continueShopping',
      variant: 'failure',
      icon: '⚠️',
    }
  }

  if (!code.value) {
    return {
      titleKey: 'payment.unknownTitle',
      messageKey: 'payment.unknownMessage',
      primaryRoute: '/',
      primaryLabelKey: 'payment.actions.continueShopping',
      secondaryRoute: '/checkout',
      secondaryLabelKey: 'payment.actions.retryCheckout',
      variant: 'pending',
      icon: '⏳',
    }
  }

  return {
    titleKey: 'payment.failureTitle',
    messageKey: 'payment.failureMessage',
    primaryRoute: '/checkout',
    primaryLabelKey: 'payment.actions.retryCheckout',
    secondaryRoute: '/',
    secondaryLabelKey: 'payment.actions.continueShopping',
    variant: 'failure',
    icon: '❌',
  }
})

// Clear cart only when payment is confirmed success (code === '00')
onMounted(() => {
  if (code.value === '00') {
    cartStore.clearCart()
  }
})

const detailItems = computed(() => {
  const rows: { label: string; value: string }[] = []
  
  // Show VNPAY details
  rows.push(
    { label: 'payment.details.code', value: code.value || '—' },
    { label: 'payment.details.txnRef', value: txnRef.value || '—' },
    { label: 'payment.details.transaction', value: transactionNo.value || '—' },
    { label: 'payment.details.amount', value: displayAmount.value },
    { label: 'payment.details.bank', value: bankCode.value || '—' },
    { label: 'payment.details.bankTranNo', value: bankTranNo.value || '—' },
    { label: 'payment.details.cardType', value: cardType.value || '—' },
    { label: 'payment.details.orderInfo', value: orderInfo.value || '—' },
    { label: 'payment.details.payDate', value: formattedPayDate.value || '—' }
  )

  return rows.filter(row => row.value && row.value !== '—')
})

const copying = ref(false)

async function copyReference() {
  if (copying.value || !txnRef.value) return
  
  copying.value = true
  try {
    const text = `${t('payment.details.txnRef')}: ${txnRef.value}`
    if (navigator?.clipboard?.writeText) {
      await navigator.clipboard.writeText(text)
    } else {
      const textarea = document.createElement('textarea')
      textarea.value = text
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.focus()
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
    }
  } catch (error) {
    console.warn('Failed to copy reference', error)
  } finally {
    setTimeout(() => {
      copying.value = false
    }, 1600)
  }
}
</script>

<style scoped lang="scss">
.payment-result {
  padding: clamp(32px, 6vw, 72px) 0 clamp(48px, 8vw, 120px);
  background: linear-gradient(180deg, rgba(17,17,17,0.04), rgba(17,17,17,0.02));
}

.status-card {
  position: relative;
  overflow: hidden;
  background: #ffffff;
  border-radius: 24px;
  padding: clamp(32px, 5vw, 48px);
  border: 1px solid rgba(17,17,17,0.08);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.12);
  transition: border-color .2s ease, box-shadow .2s ease;
}

.status-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at top right, rgba(17,17,17,0.06), transparent 60%);
  pointer-events: none;
}

.status-card.status--success::before {
  content: '';
  position: absolute;
  inset: -40px;
  background-image:
    linear-gradient(120deg, rgba(64, 170, 120, 0.2) 0%, rgba(64, 170, 120, 0) 60%),
    linear-gradient(300deg, rgba(17, 138, 178, 0.2) 0%, rgba(17, 138, 178, 0) 60%);
  opacity: .6;
  animation: confetti 9s linear infinite;
  pointer-events: none;
}

.status-card.status--failure {
  border-color: rgba(214, 69, 69, 0.4);
}

.status-card.status--pending {
  border-color: rgba(241, 196, 15, 0.35);
}

.status-header {
  display: flex;
  gap: clamp(20px, 3vw, 28px);
  align-items: flex-start;
}

.status-icon {
  font-size: clamp(40px, 6vw, 56px);
  line-height: 1;
}

.eyebrow {
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: 12px;
  color: rgba(17,17,17,0.55);
}

h1 {
  margin: 8px 0 12px;
  font-family: var(--font-family-serif);
  font-size: clamp(30px, 5vw, 38px);
  line-height: 1.1;
  color: #111111;
}

.lead {
  margin: 0;
  color: rgba(17,17,17,0.7);
  font-size: 16px;
  line-height: 1.6;
}

.actions {
  display: grid;
  gap: 12px;
  margin-top: clamp(24px, 4vw, 32px);
}

.details {
  margin-top: clamp(28px, 4vw, 40px);
  padding-top: 24px;
  border-top: 1px solid rgba(17,17,17,0.08);
}

.details-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.details-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #111111;
}

.copy-btn {
  border: none;
  background: #f5f5f5;
  padding: 6px 14px;
  font-size: 13px;
  font-weight: 600;
  border-radius: 999px;
  cursor: pointer;
  transition: background .2s ease, transform .06s ease, box-shadow .2s ease;
}

.copy-btn:hover {
  background: #ebebeb;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.copy-btn:disabled {
  cursor: default;
  opacity: .65;
  box-shadow: none;
}

dl {
  margin: 0;
  display: grid;
  gap: 16px;
}

.detail-row {
  display: grid;
  gap: 4px;
}

dt {
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: rgba(17,17,17,0.55);
}

dd {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #111111;
}

@keyframes confetti {
  0% { transform: translate3d(0, 0, 0) rotate(0deg); opacity: .6; }
  50% { transform: translate3d(12px, 6px, 0) rotate(1deg); opacity: .4; }
  100% { transform: translate3d(-12px, -6px, 0) rotate(-1deg); opacity: .6; }
}

@media (prefers-reduced-motion: reduce) {
  .status-card.status--success::before {
    animation: none;
  }
}

@media (max-width: 640px) {
  .status-card {
    padding: 28px 20px;
  }

  .status-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .actions {
    grid-template-columns: 1fr;
  }

  .details-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .copy-btn {
    width: auto;
  }
}
</style>


