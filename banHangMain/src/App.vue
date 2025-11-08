<template>
  <div class="app">
    <AppNav />
    <main class="app-main">
      <transition name="fade">
        <div v-if="isRouting" class="route-loading" aria-live="polite" aria-busy="true">
          <div class="spinner" />
        </div>
      </transition>
      <RouterView v-slot="{ Component, route }">
        <Transition name="page" mode="out-in">
          <component :is="Component" :key="route.path" />
        </Transition>
      </RouterView>
    </main>
    <AppFooter />
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from "vue";
import { RouterView, useRouter } from "vue-router";
import AppFooter from "@/components/AppFooter.vue";
import AppNav from "@/components/AppNav.vue";
import { useCartStore } from "@/stores/cart";

const cartStore = useCartStore();
const router = useRouter();
const isRouting = ref(false);
let removeBefore: any;
let removeAfter: any;

onMounted(async () => {
  // Fetch sneaker products on app mount
  if (cartStore.products.length === 0) {
    await cartStore.fetchProducts(true);
  }

  removeBefore = router.beforeEach((to, from, next) => {
    isRouting.value = true;
    next();
  });
  removeAfter = router.afterEach(() => {
    // small delay to avoid flicker on fast transitions
    setTimeout(() => (isRouting.value = false), 150);
  });
});

onUnmounted(() => {
  if (removeBefore) removeBefore();
  if (removeAfter) removeAfter();
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
  padding: 8px 20px 20px;
  width: 100vw;
  margin-left: 0;
}

@media (min-width: 1000px) {
  .app-main {
    padding: 0.1rem 40px 40px;
  }
}

.route-loading {
  position: fixed;
  inset: 0;
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(2px);
  display: grid;
  place-items: center;
  z-index: 1000;
}

.spinner {
  width: 44px;
  height: 44px;
  border: 3px solid rgba(17, 17, 17, 0.2);
  border-top-color: #111111;
  border-radius: 999px;
  animation: spin 0.8s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.fade-enter-active, .fade-leave-active { transition: opacity 180ms ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* Page transitions */
.page-enter-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.page-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.page-enter-to,
.page-leave-from {
  opacity: 1;
  transform: translateY(0);
}
</style>
