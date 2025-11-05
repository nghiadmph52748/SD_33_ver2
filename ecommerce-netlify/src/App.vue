<template>
  <div class="app">
    <AppNav />
    <main class="app-main">
      <RouterView />
    </main>
    <AppFooter />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { RouterView } from "vue-router";
import AppFooter from "@/components/AppFooter.vue";
import AppNav from "@/components/AppNav.vue";
import { useCartStore } from "@/stores/cart";

const cartStore = useCartStore();

onMounted(async () => {
  // Fetch sneaker products on app mount
  if (cartStore.products.length === 0) {
    await cartStore.fetchProducts(true);
  }
});
</script>

<style scoped>
.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-main {
  flex: 1;
  padding: 20px;
  width: 100vw;
  margin-left: 0;
}

@media (min-width: 1000px) {
  .app-main {
    padding: 40px;
  }
}
</style>
