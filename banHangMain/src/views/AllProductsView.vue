<template>
  <a-spin :loading="loading" style="width: 100%">
    <div v-if="!error && !loading">
      <AppStoreGrid v-if="products.length > 0" :data="products" />
      <a-empty v-else description="No products available" />
    </div>
    <a-empty v-else-if="error" :description="error" />
  </a-spin>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { storeToRefs } from "pinia";
import AppStoreGrid from "@/components/AppStoreGrid.vue";
import { useCartStore } from "@/stores/cart";

const cartStore = useCartStore();
const { products, loading, error } = storeToRefs(cartStore);

onMounted(async () => {
  if (products.value.length === 0) {
    await cartStore.fetchProducts(true); // Only sneakers
  }
});
</script>
