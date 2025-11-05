<template>
  <a-steps
    :current="currentStep"
    style="width: 80%; margin: 20px auto"
  >
    <a-step title="Shopping Cart" />
    <a-step title="Check out" />
    <a-step title="Order Complete" />
  </a-steps>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { storeToRefs } from "pinia";
import { useCartStore } from "@/stores/cart";

const cartStore = useCartStore();
const { cartUIStatus, cartCount } = storeToRefs(cartStore);

const currentStep = computed(() => {
  if (cartUIStatus.value === 'success') return 2;
  if (cartCount.value > 0) return 1;
  return 0;
});
</script>
