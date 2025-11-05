<template>
  <section class="reco">
    <div class="reco-header">
      <h2>You Might Also Like</h2>
      <div class="controls">
        <button class="nav" aria-label="Prev" @click="scrollBy(-1)">‹</button>
        <button class="nav" aria-label="Next" @click="scrollBy(1)">›</button>
      </div>
    </div>
    <div ref="scroller" class="scroller" role="list">
      <article v-for="p in items" :key="p.id" class="card" role="listitem">
        <RouterLink :to="`/product/${p.id}`" class="card-link">
          <div class="img"><img :src="resolveImage(p.img)" :alt="p.name" loading="lazy" /></div>
          <div class="body">
            <h4 class="name">{{ p.name }}</h4>
            <div class="cat">{{ p.gender }}'s Shoes</div>
            <div class="price">{{ formatCurrency(p.price) }}</div>
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

function resolveImage(imgPath: string | undefined | null): string {
  if (!imgPath) return '/products/1.jpg'
  const p = String(imgPath).trim()
  if (p.startsWith('http') || p.startsWith('/')) return p
  return `/products/${p}`
}
</script>

<style scoped lang="scss">
.reco { margin-top: 40px; }
.reco-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.controls { display: inline-flex; gap: 8px; }
.nav { width: 36px; height: 36px; border-radius: 999px; border: 1px solid #e8e8e8; background: #fff; }

.scroller { display: grid; grid-auto-flow: column; grid-auto-columns: 80%; gap: 16px; overflow-x: auto; scroll-snap-type: x mandatory; -webkit-overflow-scrolling: touch; padding-bottom: 8px; scrollbar-width: none; -ms-overflow-style: none; }
.scroller::-webkit-scrollbar { display: none; }

@media (min-width: 700px) { .scroller { grid-auto-columns: calc(33% - 10px); } }
@media (min-width: 1100px) { .scroller { grid-auto-columns: calc(25% - 12px); } }

.card { scroll-snap-align: start; border: 1px solid #f0f0f0; border-radius: 16px; background: #fff; overflow: hidden; }
.card-link { color: inherit; text-decoration: none; display: block; }
.img { width: 100%; aspect-ratio: 4/3; background: #f7f7f7; display: grid; place-items: center; }
.img img { width: 100%; height: 100%; object-fit: contain; }
.body { padding: 12px; display: grid; gap: 6px; }
.name { margin: 0; font-size: 16px; }
.cat { color: #4e5969; font-size: 13px; }
.price { font-weight: 700; }
</style>

