<template>
  <div class="cart-page">
    <div class="container">
      <div class="grid">
        <section class="bag">
          <h1>{{ $t("cart.bag") }}</h1>
          <AppEmptyState
            v-if="cartCount === 0"
            :title="$t('cart.empty')"
            :description="$t('cart.emptyBag')"
          >
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
          <!-- <div class="row muted">
            <span>{{ $t('cart.estimatedDelivery') }}</span>
            <span>{{ cartCount === 0 ? '—' : formatCurrency(shippingFee) }}</span>
          </div> -->
          <hr />
          <div class="row total">
            <span>{{ $t("cart.total") }}</span>
            <span>{{
              cartCount === 0 ? "—" : formatCurrency(orderTotal)
            }}</span>
          </div>

          <div class="summary-actions">
            <RouterLink
              to="/checkout"
              class="btn btn-block"
              :class="{ disabled: cartCount === 0 }"
            >
              {{ $t("cart.checkout") }}
            </RouterLink>
          </div>
        </aside>
      </div>
    </div>

    <div class="container"><AppSalesBoxes /></div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { storeToRefs } from "pinia";
import { formatCurrency } from "@/utils/currency";
import AppCartDisplay from "@/components/AppCartDisplay.vue";
import AppCartSteps from "@/components/AppCartSteps.vue";
import AppLoader from "@/components/AppLoader.vue";
import AppSalesBoxes from "@/components/AppSalesBoxes.vue";
import { useCartStore } from "@/stores/cart";
import AppEmptyState from "@/components/AppEmptyState.vue";
import { useI18n } from "vue-i18n";
import { calculateShippingFee, ShippingLocation } from "@/api/shipping";

const cartStore = useCartStore();
const { cartUIStatus, cartCount, cartTotal } = storeToRefs(cartStore);
const shippingFee = ref(0);
const isCalculatingFee = ref(false);
const orderTotal = computed(() => cartTotal.value + shippingFee.value);
const { t } = useI18n();

/**
 * Calculate shipping fee based on delivery location
 * For demo, using a default location - in production, this should come from checkout form
 */
async function updateShippingFee() {
  if (cartCount.value === 0) {
    shippingFee.value = 0;
    return;
  }

  isCalculatingFee.value = true;
  try {
    // TODO: Get actual delivery location from checkout form/user selection
    // For now, using a default location in Hanoi
    const deliveryLocation: ShippingLocation = {
      thanhPho: "Hà Nội",
      quan: "Hoàn Kiếm",
      phuong: "Hàng Gai",
      diaChiCuThe: "Số 1 Hàng Gai",
    };

    const result = await calculateShippingFee({
      location: deliveryLocation,
      weight: 500, // gram
      length: 10, // cm
      width: 10, // cm
      height: 10, // cm
      subtotal: cartTotal.value,
    });

    shippingFee.value = result.fee;
  } catch (error) {
    console.error("Error calculating shipping fee:", error);
    // Fallback to a default fee
    shippingFee.value = 15000;
  } finally {
    isCalculatingFee.value = false;
  }
}

// Calculate shipping fee when cart total changes
watch(
  () => cartTotal.value,
  () => {
    if (cartCount.value > 0) {
      updateShippingFee();
    }
  }
);

// Calculate initial shipping fee on component mount
watch(
  () => cartCount.value,
  (newCount) => {
    if (newCount > 0) {
      updateShippingFee();
    } else {
      shippingFee.value = 0;
    }
  }
);
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
  padding: 20px;
  position: sticky;
  top: 16px;
  height: fit-content;
  min-height: 520px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.06);
}

.summary h2 {
  margin: 0 0 12px 0;
}

.summary .row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.summary .row.muted span:first-child {
  color: #4e5969;
}
.summary .row.total span:first-child {
  font-weight: 700;
}

.summary-actions {
  display: grid;
  gap: 12px;
  margin-top: 12px;
}
.summary-actions .btn.disabled {
  opacity: 0.35;
  pointer-events: none;
}

hr {
  border: none;
  border-top: 1px solid #f0f0f0;
  margin: 8px 0;
}

@media (max-width: 980px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
