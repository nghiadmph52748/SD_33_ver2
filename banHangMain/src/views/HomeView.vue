<template>
  <div>
    <AppHero />
    <AppBrandStrip />
    <div class="container" :aria-label="t('home.sections.trending')">
      <AppTrendingCarousel />
    </div>
    <div class="container" :aria-label="t('home.sections.featured')">
      <AppFeaturedProducts />
        </div>
    <div class="container narrow" :aria-label="t('home.sections.sales')">
    <AppSalesBoxes />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useI18n } from "vue-i18n";
import AppFeaturedProducts from "@/components/AppFeaturedProducts.vue";
import AppSalesBoxes from "@/components/AppSalesBoxes.vue";
import AppHero from "@/components/AppHero.vue";
import AppBrandStrip from "@/components/AppBrandStrip.vue";
import AppTrendingCarousel from "@/components/AppTrendingCarousel.vue";
import { useCartStore } from "@/stores/cart";

const cartStore = useCartStore();
const { t, locale } = useI18n();

onMounted(async () => {
  // Fetch sneaker products on home page mount
  await cartStore.fetchProducts(true);
  // Set dynamic page title using i18n
  document.title = t('home.title');
});

// hero content moved into AppHero
</script>

<style scoped lang="scss">
/* page-specific overrides can go here if needed */
</style>
