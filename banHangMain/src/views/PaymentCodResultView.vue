<template>
  <div class="payment-result">
    <div class="container narrow">
      <Transition name="fade-quick" mode="out-in">
        <article :key="status.variant" class="status-card" :class="`status--${status.variant}`">
          <header class="status-header">
            <span class="status-icon" aria-hidden="true">{{ status.icon }}</span>
            <div>
              <p class="eyebrow">{{ $t('payment.codTitle') || 'Giao hàng tận nơi (COD)' }}</p>
              <h1>{{ $t(status.titleKey) }}</h1>
              <p class="lead">{{ $t(status.messageKey) }}</p>
            </div>
          </header>

          <div class="actions">
            <RouterLink :to="status.primaryRoute" class="btn btn-block">
              {{ $t(status.primaryLabelKey) }}
            </RouterLink>
            <RouterLink v-if="status.secondaryRoute && status.secondaryLabelKey" :to="status.secondaryRoute!"
              class="btn btn-outline btn-block">
              {{ $t(status.secondaryLabelKey!) }}
            </RouterLink>
          </div>

          <section class="details">
            <header class="details-header">
              <h2>{{ $t('payment.detailsHeading') }}</h2>
              <button v-if="orderId" type="button" class="copy-btn pill-btn" @click="copyOrderId" :disabled="copying">
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
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'

const route = useRoute()
const { t } = useI18n()

const orderId = computed(() => String(route.query.orderId ?? ''))
const statusParam = computed(() => String(route.query.status ?? ''))

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
  if (statusParam.value === 'success' && orderId.value) {
    return {
      titleKey: 'payment.codSuccessTitle',
      messageKey: 'payment.codSuccessMessage',
      primaryRoute: '/',
      primaryLabelKey: 'payment.actions.continueShopping',
      secondaryRoute: undefined,
      secondaryLabelKey: undefined,
      variant: 'success',
      icon: '✅',
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

const detailItems = computed(() => {
  const rows: { label: string; value: string }[] = []

  if (orderId.value) {
    rows.push({ label: 'payment.details.orderId', value: orderId.value })
  }
  rows.push({ label: 'payment.details.method', value: t('payment.cod') })

  return rows.filter(row => row.value && row.value !== '—')
})

const copying = ref(false)

async function copyOrderId() {
  if (copying.value || !orderId.value) return

  copying.value = true
  try {
    const text = `${t('payment.details.orderId')}: ${orderId.value}`
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
    console.warn('Failed to copy order ID', error)
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
  background: linear-gradient(180deg, rgba(17, 17, 17, 0.04), rgba(17, 17, 17, 0.02));
}

.status-card {
  position: relative;
  overflow: hidden;
  background: #ffffff;
  border-radius: 24px;
  padding: clamp(32px, 5vw, 48px);
  border: 1px solid rgba(17, 17, 17, 0.08);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.12);
  transition: border-color .2s ease, box-shadow .2s ease;
}

.status-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at top right, rgba(17, 17, 17, 0.06), transparent 60%);
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
  color: rgba(17, 17, 17, 0.55);
}

h1 {
  margin: 8px 0 12px;
  /* font-family: var(--font-family-serif); - Removed to switch to Inter */
  font-size: clamp(30px, 5vw, 38px);
  line-height: 1.1;
  color: #111111;
}

.lead {
  margin: 0;
  color: rgba(17, 17, 17, 0.7);
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
  border-top: 1px solid rgba(17, 17, 17, 0.08);
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
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
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
  color: rgba(17, 17, 17, 0.55);
}

dd {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #111111;
}

@keyframes confetti {
  0% {
    transform: translate3d(0, 0, 0) rotate(0deg);
    opacity: .6;
  }

  50% {
    transform: translate3d(12px, 6px, 0) rotate(1deg);
    opacity: .4;
  }

  100% {
    transform: translate3d(-12px, -6px, 0) rotate(-1deg);
    opacity: .6;
  }
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
