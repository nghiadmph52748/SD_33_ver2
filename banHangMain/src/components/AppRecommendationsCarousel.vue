<template>
  <section class="reco">
    <div ref="scroller" class="scroller" role="list">
      <article v-for="p in items" :key="p.id" class="card" role="listitem">
        <RouterLink :to="`/product/${p.id}`" class="card-link">
          <div class="img-wrap">
            <img 
              :src="resolveImage(p.img)" 
              :alt="p.name" 
              loading="lazy"
              @error="handleImageError"
            />
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
import { computed, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useCartStore } from '@/stores/cart'
import { formatCurrency } from '@/utils/currency'
import { handleImageError } from '@/utils/imageFallback'

const cart = useCartStore()
const { products } = storeToRefs(cart)

const items = computed(() => products.value.slice(0, 12))

const scroller = ref<HTMLDivElement | null>(null)
const cardWidthFactor = 2.2

function scrollBy(dir: number) {
  const el = scroller.value
  if (!el) return
  const card = el.querySelector<HTMLElement>('.card')
  const amount = card ? card.clientWidth * cardWidthFactor : 360
  el.scrollBy({ left: dir * amount, behavior: 'smooth' })
}

// Expose scrollBy for parent component
defineExpose({ scrollBy })

function resolveImage(imgPath: string | undefined | null): string {
  if (!imgPath) return '/products/1.jpg'
  const p = String(imgPath).trim()
  if (p.startsWith('http') || p.startsWith('/')) return p
  return `/products/${p}`
}
</script>

<style scoped lang="scss">
.reco { margin-top: 0; position: relative; z-index: 2; }

.scroller { display: grid; grid-auto-flow: column; grid-auto-columns: 70%; gap: 20px; overflow-x: auto; scroll-snap-type: x mandatory; -webkit-overflow-scrolling: touch; padding-bottom: 8px; scrollbar-width: none; -ms-overflow-style: none; position: relative; }
.scroller::-webkit-scrollbar { display: none; }

@media (min-width: 700px) { .scroller { grid-auto-columns: calc(50% - 10px); } }
@media (min-width: 1100px) { .scroller { grid-auto-columns: calc(33.333% - 14px); } }
@media (min-width: 1400px) { .scroller { grid-auto-columns: calc(25% - 15px); } }

.card {
  scroll-snap-align: start;
  border: 1px solid #f0f0f0;
  border-radius: 16px;
  background: #ffffff;
  transition: transform .2s ease, box-shadow .2s ease, border-color .2s ease;
  overflow: hidden;
  position: relative;
  z-index: 0;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,.12), 0 0 0 1px rgba(0,0,0,.08);
  border-color: #d0d0d0;
  z-index: 10;
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
  position: relative;
}

.img-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform .3s ease;
}

.img-wrap img.image-placeholder {
  background: linear-gradient(
    90deg,
    #f0f0f0 0%,
    #f8f8f8 50%,
    #f0f0f0 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
}

.card:hover .img-wrap img {
  transform: scale(1.05);
}

.info {
  padding: 12px 12px 16px;
}

.name {
  font-size: 16px;
  font-weight: 500;
  color: #111111;
  margin: 0 0 4px 0;
  transition: color .2s ease;
}

.card:hover .name {
  color: #000;
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

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}
</style>

