<template>
<section class="trending">
    <div class="header">
      <h2>{{ t('home.sections.trending') }}</h2>
      <div class="controls">
        <button class="nav" aria-label="Previous" @click="scrollByAmount(-1)">
          <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="#111111" d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/></svg>
        </button>
        <button class="nav" aria-label="Next" @click="scrollByAmount(1)">
          <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="#111111" d="M8.59 16.59 13.17 12 8.59 7.41 10 6l6 6-6 6z"/></svg>
        </button>
      </div>
    </div>
    <div ref="scroller" class="scroller" role="list">
      <article v-for="p in trendingProducts" :key="p.id" class="card" role="listitem">
        <RouterLink :to="`/product/${p.id}`" class="card-link">
          <div class="img-wrap">
            <img :src="resolveImage(p.img)" :alt="p.name" loading="lazy" />
          </div>
          <div class="info">
            <h4 class="name">{{ p.name }}</h4>
            <p class="meta">{{ p.gender }}'s Shoes</p>
            <p class="price">{{ formatCurrency(p.price) }}</p>
          </div>
        </RouterLink>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { storeToRefs } from "pinia";
import { useI18n } from "vue-i18n";
import { useCartStore } from "@/stores/cart";
import { formatCurrency } from "@/utils/currency";

const cartStore = useCartStore();
const { t } = useI18n();
const { products } = storeToRefs(cartStore);

const trendingProducts = computed(() => products.value.slice(0, 10));

const scroller = ref<HTMLDivElement | null>(null);

function scrollByAmount(direction: number) {
  const el = scroller.value;
  if (!el) return;
  const card = el.querySelector<HTMLElement>(".card");
  const amount = card ? card.clientWidth * 2 : 300;
  el.scrollBy({ left: direction * amount, behavior: "smooth" });
}

function resolveImage(imgPath: string): string {
  if (!imgPath) return "/shoe1.jpg";
  if (imgPath.startsWith("http") || imgPath.startsWith("/")) return imgPath;
  if (imgPath.endsWith('.png') || imgPath.endsWith('.jpg') || imgPath.endsWith('.jpeg')) {
    return `/products/${imgPath}`;
  }
  return `/products/${imgPath}.jpg`;
}
</script>

<style scoped lang="scss">
.trending {
  margin: 40px 0 24px;
  position: relative;
}

.header {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  margin-bottom: 12px;
  position: relative;
}

h2 {
  color: #111111;
}

.controls {
  position: absolute;
  right: 8px;
  top: 8px;
  display: inline-flex;
  gap: 8px;
  z-index: 3;
}

.nav {
  width: 36px;
  height: 36px;
  border-radius: 999px;
  border: 1px solid #d9d9d9;
  background: #ffffff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #111111;
  box-shadow: 0 2px 6px rgba(0,0,0,0.08);
}

.nav svg {
  display: block;
  width: 18px;
  height: 18px;
}

.nav svg path { fill: currentColor; }

.scroller {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: 72%;
  gap: 12px;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 8px;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE/Edge */
}

.scroller::-webkit-scrollbar { /* Chrome/Safari */
  display: none;
}

@media (min-width: 700px) {
  .scroller {
    grid-auto-columns: 28%;
    gap: 16px;
  }
}

@media (min-width: 1000px) {
  .scroller {
    grid-auto-columns: 22%;
  }
}

.card {
  scroll-snap-align: start;
  border: 1px solid #f0f0f0;
  border-radius: 16px;
  background: #ffffff;
}

.card-link {
  display: block;
  color: inherit;
  text-decoration: none;
}

.img-wrap {
  width: 100%;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  border-bottom: 1px solid #f5f5f5;
}

.img-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.info {
  padding: 12px 12px 16px;
}

.name {
  font-size: 16px;
  font-weight: 500;
  color: #111111;
  margin: 0 0 4px 0;
}

.meta {
  font-size: 12px;
  color: rgba(17, 17, 17, 0.6);
  margin: 0 0 8px 0;
}

.price {
  color: #111111;
  font-weight: 500;
  margin: 0;
}
</style>

