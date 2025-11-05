<template>
  <div>
    <a-carousel
      :auto-play="true"
      :interval="6000"
      :animation-duration="500"
      :show-arrow="'hover'"
      indicator-type="dot"
      indicator-position="bottom"
      style="margin-bottom: 40px; width: 100%"
      class="hero-carousel"
    >
      <a-carousel-item v-for="(slide, index) in heroSlides" :key="index">
        <div class="carousel-slide">
          <AppTextlockup>
            <template #img>
              <img :src="slide.image" :alt="slide.alt" />
            </template>
            <template #new>{{ slide.new }}</template>
            <template #sale>{{ slide.sale }}</template>
            <template #collection>{{ slide.collection }}</template>
            <template #details>{{ slide.details }}</template>
          </AppTextlockup>
        </div>
      </a-carousel-item>
    </a-carousel>
    <AppSalesBoxes />
    <AppFeaturedProducts />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import AppFeaturedProducts from "@/components/AppFeaturedProducts.vue";
import AppSalesBoxes from "@/components/AppSalesBoxes.vue";
import AppTextlockup from "@/components/AppTextlockup.vue";
import { useCartStore } from "@/stores/cart";

const cartStore = useCartStore();

onMounted(async () => {
  // Fetch sneaker products on home page mount if not already loaded
  if (cartStore.products.length === 0) {
    await cartStore.fetchProducts(true);
  }
});

const heroSlides = ref([
  {
    image: "/shoe1.jpg",
    alt: "Men Shoes Collection",
    new: "New",
    sale: "Men Shoes",
    collection: "Collection",
    details: "Street Style New Fashion"
  },
  {
    image: "/bag.jpg",
    alt: "Storewide Sale",
    new: "50%",
    sale: "Storewide Sale",
    collection: "Summer",
    details: "All accessories"
  },
  {
    image: "/shoe1.jpg",
    alt: "Featured Sneakers",
    new: "Featured",
    sale: "Premium Sneakers",
    collection: "GearUp",
    details: "Elevate your style"
  }
]);
</script>

<style scoped lang="scss">
.hero-carousel {
  width: 100%;
  min-height: 400px;
  position: relative;

  :deep(.arco-carousel-item) {
    width: 100%;
    height: auto;
    min-height: 400px;
  }

  :deep(.arco-carousel-indicator) {
    bottom: 20px;
  }
}

.carousel-slide {
  width: 100%;
  height: 100%;
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
