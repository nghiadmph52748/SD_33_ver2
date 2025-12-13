<template>
<section class="brand-strip" :aria-label="$t('aria.brandNav')">
  <div class="brand-container">
    <button class="nav-btn prev" type="button" aria-label="Scroll brands left" @click="scroll(-240)">
      <svg width="16" height="16" viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M15.41 7.41 14 6l-6 6 6 6 1.41-1.41L10.83 12z"/></svg>
    </button>
    <ul class="brand-scroller" ref="scroller" role="list">
      <li class="brand-item">
        <RouterLink :to="{ path: '/all', query: { brand: 'Nike' } }" class="brand-link" aria-label="Nike">
          <img src="@/assets/icons/Nike.png" alt="Nike" class="brand-img" />
          <span>Nike</span>
        </RouterLink>
      </li>
      <li class="brand-item">
        <RouterLink :to="{ path: '/all', query: { brand: 'Jordan' } }" class="brand-link" aria-label="Jordan">
          <img src="@/assets/icons/Jordan.png" alt="Jordan" class="brand-img" />
          <span>Jordan</span>
        </RouterLink>
      </li>
      <li class="brand-item">
        <RouterLink :to="{ path: '/all', query: { brand: 'Adidas' } }" class="brand-link" aria-label="Adidas">
          <img src="@/assets/icons/adidas.png" alt="Adidas" class="brand-img" />
          <span>Adidas</span>
        </RouterLink>
      </li>
      </ul>
    <button class="nav-btn next" type="button" aria-label="Scroll brands right" @click="scroll(240)">
      <svg width="16" height="16" viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M8.59 16.59 13.17 12 8.59 7.41 10 6l6 6-6 6z"/></svg>
    </button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
const scroller = ref<HTMLUListElement | null>(null)
function scroll(delta: number) {
  const el = scroller.value
  if (!el) return
  el.scrollBy({ left: delta, behavior: 'smooth' })
}
</script>

<style scoped lang="scss">
.brand-strip { display: flex; justify-content: center; padding: 8px 0; background: #fff; border-top: 1px solid #f0f0f0; border-bottom: 1px solid #f0f0f0; width: 100vw; margin-left: calc(50% - 50vw); }
.brand-container { position: relative; width: min(1240px, 100%); padding: 0 24px; margin: 0 auto; }

/* Edge fade */
.brand-container::before, .brand-container::after { content: ""; position: absolute; top: 0; bottom: 0; width: 24px; pointer-events: none; z-index: 1; }
.brand-container::before { left: 0; background: linear-gradient(90deg, #fff 30%, rgba(255,255,255,0)); }
.brand-container::after { right: 0; background: linear-gradient(-90deg, #fff 30%, rgba(255,255,255,0)); }

.brand-scroller { display: flex; gap: 16px; justify-content: center; overflow-x: auto; scroll-snap-type: x mandatory; padding: 6px 0; margin: 0; list-style: none; scrollbar-width: none; }
.brand-scroller::-webkit-scrollbar { display: none; }

.brand-item { flex: 0 0 auto; scroll-snap-align: center; }

.brand-link { display: inline-flex; align-items: center; gap: 10px; color: #111; text-decoration: none; padding: 10px 14px; border: 1px solid #e8e8e8; border-radius: 999px; background: #fff; transition: transform .15s ease, box-shadow .15s ease, border-color .15s ease; cursor: pointer; }
.brand-link:hover { transform: translateY(-2px); box-shadow: 0 10px 20px rgba(0,0,0,.08); border-color: #ddd; }
.brand-link:active { transform: translateY(0) scale(0.98); }
.brand-link span { font-size: 14px; color: #111; font-weight: 600; letter-spacing: .02em; }
.brand-img { height: 24px; width: auto; display: block; }

.nav-btn { position: absolute; top: 50%; transform: translateY(-50%); width: 28px; height: 28px; border-radius: 999px; border: 1px solid #e8e8e8; background: #fff; display: grid; place-items: center; cursor: pointer; transition: box-shadow .15s ease, transform .08s ease, background .15s ease, border-color .15s ease; z-index: 2; }
.nav-btn:hover { box-shadow: 0 6px 18px rgba(0,0,0,.08); transform: translateY(-50%) translateY(-1px); border-color: #d1d5db; }
.nav-btn:active { transform: translateY(-50%) scale(0.95); box-shadow: 0 2px 8px rgba(0,0,0,.1); }
.nav-btn.prev { left: 4px; }
.nav-btn.next { right: 4px; }

@media (max-width: 600px) {
  .brand-container { padding: 0 12px; }
  .nav-btn { display: none; }
}
</style>

