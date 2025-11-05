<template>
  <div class="storegrid">
    <section class="content-wrapper">
      <header class="content-header">
        <div>
          <p class="eyebrow">All Sneakers</p>
          <h2>Browse our collection</h2>
        </div>
        <p class="content-meta">
          Showing <strong>{{ totalProducts }}</strong> of <strong>{{ totalAvailable }}</strong> styles
        </p>
      </header>
      <TransitionGroup name="items" tag="div" class="content">
        <article v-for="item in filteredProducts" :key="item.id" class="item">
          <RouterLink
            :to="`/product/${item.id}`"
            class="item__media"
            :style="{ '--media-bg-color': getMediaBackground(item.id) }"
          >
            <div class="item__media-bg"></div>
            <img
              :src="item.img"
              :alt="item.name"
              crossorigin="anonymous"
              @load="event => handleImageLoad(String(item.id), event)"
            />
          </RouterLink>
          <div class="item__body">
            <div class="item__meta">
              <span v-if="item.color" class="item__chip">{{ item.color }}</span>
              <div class="item__rating">
                <a-rate :model-value="item.starrating || 0" readonly :allow-half="false" :size="14" />
                <span class="item__rating-value">{{ (item.starrating || 0).toFixed(1) }}</span>
              </div>
            </div>
            <h3 class="item__title">{{ item.name }}</h3>
            <p v-if="item.description" class="item__description">{{ item.description }}</p>
            <div class="item__footer">
              <div class="item__price">
                <span>{{ formatCurrency(item.price) }}</span>
                <small v-if="item.sizes?.length">{{ item.sizes.length }} sizes</small>
              </div>
              <RouterLink :to="`/product/${item.id}`" class="item__cta">
                <span>View item</span>
                <svg width="16" height="16" viewBox="0 0 24 24" aria-hidden="true">
                  <path
                    fill="currentColor"
                    d="M13.172 12 8.222 7.05a1 1 0 1 1 1.414-1.414l6.364 6.364a1 1 0 0 1 0 1.414l-6.364 6.364a1 1 0 0 1-1.414-1.414L13.172 12Z"
                  />
                </svg>
              </RouterLink>
            </div>
          </div>
        </article>
      </TransitionGroup>
    </section>
    <aside class="filter-card">
      <div class="filter-card__badge">Special Sale</div>
      <h3>Dial in your price</h3>
      <p class="filter-card__copy">
        Adjust the slider to surface sneakers within your budget. All prices are shown in Vietnamese đồng.
      </p>
      <div class="filter-card__range">
        <span>Max price</span>
        <strong>{{ formatCurrency(pricerange) }}</strong>
      </div>
      <input
        class="slider"
        id="pricerange"
        v-model.number="pricerange"
        :min="min"
        :max="max"
        type="range"
        :step="max > 0 ? Math.max(max / 100, 10000) : 10000"
      />
      <div class="filter-card__labels">
        <span>{{ formatCurrency(min) }}</span>
        <span>{{ formatCurrency(max) }}</span>
      </div>
    </aside>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import type { Product } from "@/stores/cart";
import { formatCurrency } from "@/utils/currency";

const props = defineProps<{
  data: Product[];
}>();

const DEFAULT_MAX_PRICE = 10_000_000;
const MAX_PRICE_CAP = 50_000_000;
const min = 0;
// Calculate max from products, but cap at reasonable maximum
const max = computed(() => {
  if (props.data.length === 0) return DEFAULT_MAX_PRICE;

  const prices = props.data
    .map(product => {
      if (typeof product.price === "number" && Number.isFinite(product.price)) {
        return product.price;
      }
      if (typeof product.price === "string") {
        const parsed = Number.parseFloat(product.price);
        return Number.isNaN(parsed) ? 0 : parsed;
      }
      return 0;
    })
    .filter(price => price >= 0);

  if (prices.length === 0) return DEFAULT_MAX_PRICE;

  const positivePrices = prices.filter(price => price > 0);
  const candidateMax = positivePrices.length > 0 ? Math.max(...positivePrices) : Math.max(...prices);
  const sanitizedMax = candidateMax > 0 && Number.isFinite(candidateMax) ? candidateMax : DEFAULT_MAX_PRICE;

  // Cap at 50M VND for reasonable filter range
  return Math.min(sanitizedMax, MAX_PRICE_CAP);
});
const pricerange = ref(max.value);

// Update pricerange when max changes
watch(max, (newMax) => {
  if (Number.isFinite(newMax) && newMax > 0) {
    pricerange.value = newMax;
  }
}, { immediate: true });

watch(pricerange, newValue => {
  if (!Number.isFinite(newValue)) {
    pricerange.value = max.value;
    return;
  }

  if (newValue < min) {
    pricerange.value = min;
  } else if (newValue > max.value) {
    pricerange.value = max.value;
  }
});

const DEFAULT_MEDIA_GLOW = "rgba(22, 119, 255, 0.18)";

const filteredProducts = computed(() => {
  return props.data.filter(item => {
    const price = typeof item.price === "number" ? item.price : Number.parseFloat(String(item.price));
    const normalizedPrice = Number.isNaN(price) || price < 0 ? 0 : price;
    return normalizedPrice <= pricerange.value;
  });
});

const totalProducts = computed(() => filteredProducts.value.length);
const totalAvailable = computed(() => props.data.length);

const mediaBackgrounds = reactive<Record<string, string>>({});

function handleImageLoad(productId: string, event: Event) {
  const target = event.target as HTMLImageElement | null;
  if (!target) return;

  const extractedColor = extractAverageColor(target);
  if (extractedColor) {
    mediaBackgrounds[productId] = extractedColor;
  }
}

function extractAverageColor(image: HTMLImageElement): string | null {
  const canvas = document.createElement("canvas");
  const context = canvas.getContext("2d");
  if (!context) {
    return null;
  }

  const sampleSize = 36;
  canvas.width = sampleSize;
  canvas.height = sampleSize;

  try {
    context.drawImage(image, 0, 0, sampleSize, sampleSize);
  } catch (error) {
    console.warn("Failed to sample image color", error);
    return null;
  }

  let r = 0;
  let g = 0;
  let b = 0;
  let count = 0;
  const imageData = context.getImageData(0, 0, sampleSize, sampleSize).data;

  for (let i = 0; i < imageData.length; i += 4) {
    const alpha = imageData[i + 3];
    if (alpha < 200) continue;

    const red = imageData[i];
    const green = imageData[i + 1];
    const blue = imageData[i + 2];

    // Skip near-white pixels to avoid sampling background with glare
    if (red > 240 && green > 240 && blue > 240) continue;

    r += red;
    g += green;
    b += blue;
    count += 1;
  }

  if (!count) {
    return null;
  }

  r = Math.round(r / count);
  g = Math.round(g / count);
  b = Math.round(b / count);

  // Lighten the sampled color slightly for a glow effect
  const glow = (channel: number) => Math.min(255, Math.round(channel * 1.1 + 20));

  return `rgba(${glow(r)}, ${glow(g)}, ${glow(b)}, 0.24)`;
}

function getMediaBackground(productId: string): string {
  return mediaBackgrounds[productId] || DEFAULT_MEDIA_GLOW;
}
</script>

<style scoped lang="scss">
.storegrid {
  width: min(1240px, 100%);
  margin: clamp(32px, 6vw, 64px) auto;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: clamp(28px, 4vw, 48px);
  align-items: start;
  padding-inline: clamp(16px, 4vw, 32px);
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.content-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.content-header h2 {
  margin: 4px 0 0;
  font-size: clamp(28px, 4vw, 36px);
  color: #1d2129;
}

.content-header .eyebrow {
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: 12px;
  color: #86909c;
}

.content-meta {
  margin: 0;
  font-size: 14px;
  color: #4e5969;
}

.content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: clamp(24px, 3vw, 32px);
}

.item {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 18px;
  background: #ffffff;
  border-radius: 24px;
  border: 1px solid rgba(22, 119, 255, 0.12);
  box-shadow: 0 20px 40px rgba(15, 35, 95, 0.08);
  padding: 18px 20px 24px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  overflow: hidden;
}

.item:hover {
  transform: translateY(-6px);
  box-shadow: 0 28px 50px rgba(15, 35, 95, 0.12);
}

.item__media {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20px;
  overflow: hidden;
  padding: 32px 28px;
  background: linear-gradient(135deg, rgba(22, 119, 255, 0.12), rgba(22, 119, 255, 0));
  min-height: 260px;
}

.item__media:hover img {
  transform: scale(1.06) rotate(-1.5deg);
}

.item__media-bg {
  position: absolute;
  inset: 10% 12%;
  background: radial-gradient(circle at top, var(--media-bg-color, rgba(22, 119, 255, 0.18)) 0%, rgba(255, 255, 255, 0) 70%);
  filter: blur(40px);
  z-index: 0;
}

.item__media img {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100%;
  max-width: 320px;
  max-height: 220px;
  object-fit: contain;
  border-radius: 16px;
  box-shadow: 0 18px 35px rgba(15, 35, 95, 0.15);
  transition: transform 0.3s ease;
}

.item__body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.item__chip {
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(22, 119, 255, 0.12);
  color: #1677ff;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.item__rating {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #4e5969;
  font-size: 13px;
}

.item__rating-value {
  font-weight: 600;
}

.item__title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
}

.item__description {
  margin: 0;
  color: #4e5969;
  line-height: 1.5;
  font-size: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item__footer {
  margin-top: auto;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
}

.item__price {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item__price span {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-primary-6, #1677ff);
}

.item__price small {
  font-size: 12px;
  color: #86909c;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.item__cta {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  color: #1d2129;
  padding: 8px 0;
  text-decoration: none;
  transition: color 0.2s ease;
}

.item__cta svg {
  transition: transform 0.2s ease;
}

.item__cta:hover {
  color: var(--color-primary-6, #1677ff);
}

.item__cta:hover svg {
  transform: translateX(3px);
}

.filter-card {
  position: sticky;
  top: 120px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: clamp(24px, 3vw, 32px);
  background: linear-gradient(180deg, rgba(22, 119, 255, 0.08) 0%, rgba(22, 119, 255, 0.02) 100%);
  border-radius: 24px;
  border: 1px solid rgba(22, 119, 255, 0.12);
  box-shadow: 0 18px 40px rgba(15, 35, 95, 0.08);
  width: 100%;
  min-width: 280px;
}

.filter-card__badge {
  align-self: flex-start;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(22, 119, 255, 0.12);
  color: #1677ff;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.filter-card h3 {
  margin: 0;
  color: #1d2129;
  font-size: 22px;
}

.filter-card__copy {
  margin: 0;
  color: #4e5969;
  line-height: 1.6;
}

.filter-card__range {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 14px;
  color: #4e5969;
}

.filter-card__range strong {
  font-size: 16px;
  color: #1d2129;
}

.filter-card__labels {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #86909c;
}

.slider {
  width: 100%;
  appearance: none;
  height: 6px;
  border-radius: 999px;
  background: linear-gradient(90deg, #1677ff 0%, rgba(22, 119, 255, 0.2) 100%);
  outline: none;
}

.slider::-webkit-slider-thumb {
  appearance: none;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #1677ff;
  border: 3px solid #ffffff;
  box-shadow: 0 8px 16px rgba(22, 119, 255, 0.3);
  cursor: pointer;
  transition: transform 0.2s ease;
}

.slider::-webkit-slider-thumb:hover {
  transform: scale(1.05);
}

.slider::-moz-range-thumb {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #1677ff;
  border: 3px solid #ffffff;
  box-shadow: 0 8px 16px rgba(22, 119, 255, 0.3);
  cursor: pointer;
  transition: transform 0.2s ease;
}

.slider::-moz-range-thumb:hover {
  transform: scale(1.05);
}

@media (max-width: 1024px) {
  .storegrid {
    grid-template-columns: 1fr;
  }

  .filter-card {
    position: static;
    order: -1;
  }

  .content {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 680px) {
  .content {
    grid-template-columns: 1fr;
  }
}
</style>
