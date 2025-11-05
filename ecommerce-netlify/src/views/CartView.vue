<template>
  <div>
    <AppCartSteps />
    <a-divider />
    <a-typography-title :heading="2" style="text-align: center; margin: 24px 0">
      {{ $t('cart.title') }}
    </a-typography-title>

    <a-spin :loading="cartUIStatus === 'loading'" style="width: 100%">
      <section v-if="cartUIStatus === 'idle'">
        <AppCartDisplay />
      </section>
      
      <a-result
        v-else-if="cartUIStatus === 'success'"
        status="success"
        title="Success!"
        sub-title="Thank you for your purchase. You'll be receiving your items in 4 business days."
      >
        <template #extra>
          <a-button type="primary">
            <RouterLink to="/" style="color: inherit; text-decoration: none">Back to Home</RouterLink>
          </a-button>
        </template>
      </a-result>

      <a-result
        v-else-if="cartUIStatus === 'failure'"
        status="error"
        title="Oops, something went wrong"
        sub-title="Redirecting you to your cart to try again."
      />
    </a-spin>

    <AppSalesBoxes />
  </div>
</template>

<script setup lang="ts">
import { storeToRefs } from "pinia";
import AppCartDisplay from "@/components/AppCartDisplay.vue";
import AppCartSteps from "@/components/AppCartSteps.vue";
import AppLoader from "@/components/AppLoader.vue";
import AppSalesBoxes from "@/components/AppSalesBoxes.vue";
import { useCartStore } from "@/stores/cart";

const cartStore = useCartStore();
const { cartUIStatus } = storeToRefs(cartStore);
</script>

<style scoped lang="scss">
.loader {
  display: flex;
  justify-content: center;
}

.success,
.failure {
  text-align: center;
}
</style>
