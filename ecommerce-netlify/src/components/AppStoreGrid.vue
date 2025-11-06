<template>
  <div class="storegrid">
    <section class="content-wrapper">
      <header class="content-header">
        <div>
          <p class="eyebrow">{{ $t('store.allSneakers') }}</p>
          <h2>{{ $t('store.browseCollection') }}</h2>
        </div>
        <p class="content-meta">
          Showing <strong>{{ totalProducts }}</strong> of <strong>{{ totalAvailable }}</strong> styles
        </p>
      </header>
      <TransitionGroup
        name="items"
        tag="div"
        class="content"
        role="list"
        aria-label="Product list"
      >
        <article
          v-for="item in filteredProducts"
          :key="item.id"
          class="item"
          role="listitem"
          :aria-labelledby="`product-title-${item.id}`"
          :aria-describedby="item.description ? `product-description-${item.id}` : undefined"
        >
          <RouterLink
            :to="`/product/${item.id}`"
            class="item__media"
            :style="{ '--media-bg-color': getMediaBackground(item.id) }"
            :aria-label="`View details for ${item.name}`"
          >
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
            <h3 class="item__title" :id="`product-title-${item.id}`">{{ item.name }}</h3>
            <p
              v-if="item.description"
              class="item__description"
              :id="`product-description-${item.id}`"
            >
              {{ item.description }}
            </p>
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
        {{ $t('store.priceFilterHelp') }}
      </p>
      <div class="filter-card__range">
        <span>Max price</span>
        <strong>{{ formatCurrency(pricerange) }}</strong>
      </div>
      <label class="sr-only" for="pricerange">Filter sneakers by maximum price</label>
      <input
        class="slider"
        id="pricerange"
        v-model.number="pricerange"
        :min="min"
        :max="max"
        type="range"
        :step="max > 0 ? Math.max(max / 100, 10000) : 10000"
        aria-label="Filter products by maximum price"
        :aria-valuemin="min"
        :aria-valuemax="max"
        :aria-valuenow="pricerange"
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

const DEFAULT_MEDIA_GLOW = "var(--glow-primary)";

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

  // Crop surrounding background so the product fills the card better
  if (!target.dataset.cropped) {
    const cropped = cropToContent(target);
    if (cropped) {
      target.src = cropped;
      target.dataset.cropped = 'true';
    }
  }

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

// Crop transparent/solid-background margins around the subject.
// Heuristic: detect the dominant edge color and trim pixels that are within
// a small distance of that color (likely background) while preserving the core.
function cropToContent(image: HTMLImageElement): string | null {
  const canvas = document.createElement('canvas');
  const ctx = canvas.getContext('2d');
  if (!ctx) return null;

  const width = image.naturalWidth;
  const height = image.naturalHeight;
  if (!width || !height) return null;

  canvas.width = width;
  canvas.height = height;

  try {
    ctx.drawImage(image, 0, 0);
  } catch {
    return null;
  }

  const data = ctx.getImageData(0, 0, width, height).data;

  // Sample edge colors (top, bottom, left, right) to estimate background color
  function sampleEdgeAverage(): [number, number, number] {
    let r = 0, g = 0, b = 0, count = 0;
    const step = Math.max(1, Math.floor(Math.min(width, height) / 200));
    for (let x = 0; x < width; x += step) {
      const ti = (0 * width + x) * 4; // top
      const bi = ((height - 1) * width + x) * 4; // bottom
      r += data[ti] + data[bi];
      g += data[ti + 1] + data[bi + 1];
      b += data[ti + 2] + data[bi + 2];
      count += 2;
    }
    for (let y = 0; y < height; y += step) {
      const li = (y * width + 0) * 4; // left
      const ri = (y * width + (width - 1)) * 4; // right
      r += data[li] + data[ri];
      g += data[li + 1] + data[ri + 1];
      b += data[li + 2] + data[ri + 2];
      count += 2;
    }
    return [Math.round(r / count), Math.round(g / count), Math.round(b / count)];
  }

  const [br, bg, bb] = sampleEdgeAverage();
  const tol = 22; // tolerance for color distance to treat as background
  const isBg = (ri: number) => {
    const r = data[ri], g = data[ri + 1], b = data[ri + 2], a = data[ri + 3];
    if (a < 10) return true;
    // near edge background color or near pure white
    const dr = r - br, dg = g - bg, db = b - bb;
    const dist = Math.sqrt(dr * dr + dg * dg + db * db);
    const nearWhite = r > 245 && g > 245 && b > 245;
    return dist <= tol || nearWhite;
  };

  let top = 0, bottom = height - 1, left = 0, right = width - 1;

  // find top
  while (top < bottom) {
    let found = false;
    for (let x = 0; x < width; x += 2) {
      const i = (top * width + x) * 4;
      if (!isBg(i)) { found = true; break; }
    }
    if (found) break;
    top++;
  }

  // bottom
  while (bottom > top) {
    let found = false;
    for (let x = 0; x < width; x += 2) {
      const i = (bottom * width + x) * 4;
      if (!isBg(i)) { found = true; break; }
    }
    if (found) break;
    bottom--;
  }

  // left
  while (left < right) {
    let found = false;
    for (let y = top; y <= bottom; y += 2) {
      const i = (y * width + left) * 4;
      if (!isBg(i)) { found = true; break; }
    }
    if (found) break;
    left++;
  }

  // right
  while (right > left) {
    let found = false;
    for (let y = top; y <= bottom; y += 2) {
      const i = (y * width + right) * 4;
      if (!isBg(i)) { found = true; break; }
    }
    if (found) break;
    right--;
  }

  const cropW = Math.max(1, right - left + 1);
  const cropH = Math.max(1, bottom - top + 1);

  // If cropping would remove less than 2% in both dimensions, skip
  if (cropW >= width * 0.98 && cropH >= height * 0.98) return null;

  const out = document.createElement('canvas');
  const octx = out.getContext('2d');
  if (!octx) return null;
  out.width = cropW;
  out.height = cropH;
  octx.drawImage(canvas, left, top, cropW, cropH, 0, 0, cropW, cropH);
  try {
    return out.toDataURL('image/png');
  } catch {
    return null;
  }
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
  font-family: var(--font-family-serif);
  font-size: var(--font-size-2xl);
  line-height: var(--line-height-tight);
  color: var(--color-text-primary);
}

.content-header .eyebrow {
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

.content-meta {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
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
  background: var(--color-surface);
  border-radius: 24px;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-card);
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
  border-radius: 12px;
  overflow: hidden;
  padding: 0;
  background: transparent; /* remove secondary background */
}

/* Remove image-specific hover transform; keep only card hover */

.item__media-bg { display: none; }

.item__media img {
  position: relative;
  z-index: 1;
  width: 100%;
  height: auto;
  max-width: 320px;
  max-height: 240px;
  object-fit: contain;
  border-radius: 12px;
  border: 1px solid #e8e8e8; /* simple border around product image */
  box-shadow: none;
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
  background: var(--color-primary-subtle);
  color: var(--color-primary);
  font-size: var(--font-size-xs);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.item__rating {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.item__rating-value {
  font-weight: 600;
  color: var(--color-text-secondary);
}

.item__title {
  margin: 0;
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-text-primary);
}

.item__description {
  margin: 0;
  color: var(--color-text-secondary);
  line-height: var(--line-height-base);
  font-size: var(--font-size-sm);
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
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: var(--color-primary);
}

.item__price small {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.item__cta {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  color: var(--color-text-primary);
  padding: 8px 0;
  text-decoration: none;
  transition: color 0.2s ease;
}

.item__cta svg {
  transition: transform 0.2s ease;
}

.item__cta:hover {
  color: var(--color-primary);
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
  background: linear-gradient(180deg, var(--color-primary-subtle) 0%, rgba(22, 119, 255, 0.02) 100%);
  border-radius: 24px;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-soft);
  width: 100%;
  min-width: 280px;
}

.filter-card__badge {
  align-self: flex-start;
  padding: 6px 12px;
  border-radius: 999px;
  background: var(--color-primary-subtle);
  color: var(--color-primary);
  font-size: var(--font-size-xs);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.filter-card h3 {
  margin: 0;
  color: var(--color-text-primary);
  font-size: var(--font-size-lg);
  font-family: var(--font-family-serif);
}

.filter-card__copy {
  margin: 0;
  color: var(--color-text-secondary);
  line-height: var(--line-height-relaxed);
}

.filter-card__range {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.filter-card__range strong {
  font-size: var(--font-size-base);
  color: var(--color-text-primary);
}

.filter-card__labels {
  display: flex;
  justify-content: space-between;
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
}

.slider {
  width: 100%;
  appearance: none;
  height: 6px;
  border-radius: 999px;
  background: linear-gradient(90deg, var(--color-primary) 0%, rgba(22, 119, 255, 0.2) 100%);
  outline: none;
}

.slider::-webkit-slider-thumb {
  appearance: none;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--color-primary);
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
  background: var(--color-primary);
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
