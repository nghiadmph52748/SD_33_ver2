<template>
  <div class="cart-page">
    <div class="container">
      <div class="grid">
        <section class="bag">
          <!-- <h1>{{ $t("cart.bag") }}</h1> removed as requested -->
          <AppEmptyState v-if="cartCount === 0" :title="$t('cart.empty')" :description="$t('cart.emptyBag')">
            <RouterLink to="/" class="btn">{{
              $t("page.returnHome")
              }}</RouterLink>
          </AppEmptyState>
          <div v-else class="bag-items">
            <AppCartDisplay />
          </div>
        </section>

        <aside class="summary" aria-label="Order summary">
          <h2>{{ $t("cart.summary") }}</h2>
          <div class="row">
            <span>{{ $t("cart.subtotal") }}</span>
            <span>{{ cartCount === 0 ? "—" : formatCurrency(cartTotal) }}</span>
          </div>
          <hr />
          <div class="row total">
            <span>{{ $t("cart.total") }}</span>
            <span>{{ cartCount === 0 ? "—" : formatCurrency(cartTotal) }}</span>
          </div>

          <div class="summary-actions">
            <RouterLink to="/checkout" class="btn btn-block" :class="{ disabled: cartCount === 0 }">
              {{ $t("cart.checkout") }}
            </RouterLink>
          </div>
        </aside>
      </div>
    </div>

    <div class="container related-products" v-if="relatedProducts.length > 0">
      <AppStoreGrid :data="relatedProducts" :title="$t('cart.relatedProducts')" :eyebrow="''" />
    </div>

    <div class="container">
      <AppSalesBoxes />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted } from "vue";
import { storeToRefs } from "pinia";
import { formatCurrency } from "@/utils/currency";
import AppCartDisplay from "@/components/AppCartDisplay.vue";
import AppCartSteps from "@/components/AppCartSteps.vue";
import AppLoader from "@/components/AppLoader.vue";
import AppSalesBoxes from "@/components/AppSalesBoxes.vue";
import AppStoreGrid from "@/components/AppStoreGrid.vue";
import { useCartStore } from "@/stores/cart";
import AppEmptyState from "@/components/AppEmptyState.vue";
import { useI18n } from "vue-i18n";

const cartStore = useCartStore();
const { cartUIStatus, cartCount, cartTotal, products } = storeToRefs(cartStore);
const { t } = useI18n();

onMounted(() => {
  if (products.value.length === 0) {
    cartStore.fetchProducts();
  }
});

const relatedProducts = computed(() => {
  // Show 9 random products or just first 9
  return products.value.slice(0, 9);
});
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

.bag h1 {
  margin: 0 0 16px 0;
}

.bag-items table {
  width: 100%;
  border-collapse: collapse;
}

.bag-items th,
.bag-items td {
  padding: 12px 10px;
}

.bag-items tr {
  border-bottom: 1px solid #f0f0f0;
}

.bag-empty {
  padding: 18px 0 24px;
  color: #4e5969;
}

.summary {
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 16px;
  padding: 24px;
  position: sticky;
  top: 100px;
  /* min-height removed to fit content */
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
}

.summary h2 {
  margin: 0 0 16px 0;
  font-size: 20px;
}

.summary .row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  font-size: 15px;
}

.summary .row.muted span:first-child {
  color: #4e5969;
}

.summary .row.total {
  padding-top: 16px;
  font-size: 16px;
}

.summary .row.total span:first-child {
  font-weight: 700;
}

.summary-actions {
  display: grid;
  gap: 12px;
  margin-top: 16px;
}

.summary-actions .btn.disabled {
  opacity: 0.35;
  pointer-events: none;
}

hr {
  border: none;
  border-top: 1px solid #f0f0f0;
  margin: 4px 0 16px;
}

@media (max-width: 980px) {
  .grid {
    grid-template-columns: 1fr;
    gap: 0;
  }
}

.related-products {
  margin-top: 80px;
}
</style>
