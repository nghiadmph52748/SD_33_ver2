<template>
  <a-spin :loading="loading" style="width: 100%">
    <div v-if="!error">
      <AppStoreGrid :data="menProducts" />
    </div>
    <a-empty v-else :description="error" />
  </a-spin>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { storeToRefs } from "pinia";
import AppStoreGrid from "@/components/AppStoreGrid.vue";
import { useCartStore } from "@/stores/cart";

const cartStore = useCartStore();
const { menProducts, loading, error } = storeToRefs(cartStore);

onMounted(async () => {
  if (menProducts.value.length === 0) {
    await cartStore.fetchProducts(true); // Only sneakers
  }
});
</script>
