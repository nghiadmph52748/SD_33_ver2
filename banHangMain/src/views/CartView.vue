<template>
  <div class="cart-page">
    <div class="container">
      <div class="grid">
        <section class="bag">
          <h1>Bag</h1>
          <div v-if="cartCount === 0" class="bag-empty">
            <p>There are no items in your bag.</p>
          </div>
          <div v-else class="bag-items">
        <AppCartDisplay />
          </div>

          <section class="favourites" aria-label="Favourites">
            <h2>Favourites</h2>
            <p>
              Want to view your favourites?
              <a href="#">Join us</a>
              or
              <a href="#">Sign in</a>
            </p>
          </section>
      </section>
      
        <aside class="summary" aria-label="Order summary">
          <h2>Summary</h2>
          <div class="row">
            <span>{{ $t('cart.subtotal') }}</span>
            <span>{{ cartCount === 0 ? '—' : formatCurrency(cartTotal) }}</span>
          </div>
          <div class="row muted">
            <span>Estimated Delivery &amp; Handling</span>
            <span>{{ cartCount === 0 ? '—' : formatCurrency(shippingFee) }}</span>
          </div>
          <hr />
          <div class="row total">
            <span>Total</span>
            <span>{{ cartCount === 0 ? '—' : formatCurrency(orderTotal) }}</span>
          </div>

          <div class="summary-actions">
            <button class="btn btn-block" :disabled="cartCount === 0">{{ $t('cart.checkout') }}</button>
          </div>
        </aside>
      </div>
    </div>

    <div class="container"><AppRecommendationsCarousel /></div>
    <div class="container"><AppSalesBoxes /></div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { storeToRefs } from "pinia";
import { formatCurrency } from "@/utils/currency";
import AppCartDisplay from "@/components/AppCartDisplay.vue";
import AppCartSteps from "@/components/AppCartSteps.vue";
import AppLoader from "@/components/AppLoader.vue";
import AppSalesBoxes from "@/components/AppSalesBoxes.vue";
import AppRecommendationsCarousel from "@/components/AppRecommendationsCarousel.vue";
import { useCartStore } from "@/stores/cart";

const cartStore = useCartStore();
const { cartUIStatus, cartCount, cartTotal } = storeToRefs(cartStore);
const shippingFee = 250000;
const orderTotal = computed(() => cartTotal.value + shippingFee);
</script>

<style scoped lang="scss">
.cart-page {
  padding: 24px 0 48px;
}

.grid {
  display: grid;
  grid-template-columns: minmax(640px, 1.6fr) 1fr;
  gap: 32px;
  align-items: start;
}

.bag h1 { margin: 0 0 16px 0; }

.bag-items table { width: 100%; border-collapse: collapse; }
.bag-items th, .bag-items td { padding: 12px 10px; }
.bag-items tr { border-bottom: 1px solid #f0f0f0; }

.bag-empty {
  padding: 18px 0 24px;
  color: #4e5969;
}

.favourites { margin-top: 32px; }
.favourites h2 { font-size: 20px; margin: 0 0 8px 0; }
.favourites p { color: #4e5969; margin: 0; }
.favourites a { color: #111111; text-decoration: underline; }

.summary {
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 16px;
  padding: 20px;
  position: sticky;
  top: 16px;
  height: fit-content;
  min-height: 520px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.06);
}

.summary h2 { margin: 0 0 12px 0; }

.summary .row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.summary .row.muted span:first-child { color: #4e5969; }
.summary .row.total span:first-child { font-weight: 700; }

.summary-actions { display: grid; gap: 12px; margin-top: 12px; }
.summary-actions .btn[disabled] { opacity: .35; }

hr { border: none; border-top: 1px solid #f0f0f0; margin: 8px 0; }

@media (max-width: 980px) {
  .grid { grid-template-columns: 1fr; }
}
</style>
