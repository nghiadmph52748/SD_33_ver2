<template>
  <div class="storegrid">
    <section class="content-wrapper">
      <header class="content-header">
        <div>
          <p class="eyebrow">{{ $t('store.allSneakers') }}</p>
          <h2>{{ $t('store.browseCollection') }}</h2>
        </div>
        <p class="content-meta">
          {{ contentMeta }}
        </p>
      </header>
      <TransitionGroup
        name="items"
        tag="div"
        class="content"
        role="list"
        :aria-label="t('store.productListAria')"
      >
        <template v-if="loading">
          <ProductCardSkeleton
            v-for="n in skeletonCount"
            :key="`skeleton-${n}`"
          />
        </template>
        <RouterLink
          v-else
          v-for="item in filteredProducts"
          :key="item.id"
          :to="`/product/${item.id}`"
          class="item"
          role="listitem"
          :aria-labelledby="`product-title-${item.id}`"
        >
          <div class="item__media">
            <div v-if="!imageLoaded[item.id]" class="image-skeleton">
              <div class="skeleton-shimmer"></div>
            </div>
            <img
              :src="item.img"
              :alt="item.name"
              loading="lazy"
              crossorigin="anonymous"
              :class="{ loaded: imageLoaded[item.id] }"
              @load="event => handleImageLoad(String(item.id), event)"
              v-img-fallback
            />
          </div>
          <div class="item__body">
            <div class="item__price">
              <span v-if="item.originalPrice && item.originalPrice > item.price" class="item__price-original">{{ formatCurrency(item.originalPrice) }}</span>
              <span class="item__price-current">{{ formatCurrency(item.price) }}</span>
            </div>
            <h3 class="item__title" :id="`product-title-${item.id}`">{{ item.name }}</h3>
            <div class="item__meta">
              <span v-if="item.gender" class="item__category">{{ formatCategory(item) }}</span>
              <span class="item__colours">{{ t('store.coloursCount', { count: getColourCount(item) }) }}</span>
            </div>
            <div v-if="getSpecialTag(item)" class="item__tag">{{ getSpecialTag(item) }}</div>
          </div>
        </RouterLink>
      </TransitionGroup>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import type { Product } from "@/stores/cart";
import { formatCurrency } from "@/utils/currency";
import { FALLBACK_IMAGE, handleImageError } from "@/utils/imageFallback";
import ProductCardSkeleton from "./ProductCardSkeleton.vue";
import { useI18n } from "vue-i18n";

const props = defineProps<{
  data: Product[];
  loading?: boolean;
}>();

const skeletonCount = 8; // Number of skeleton cards to show

const DEFAULT_MAX_PRICE = 10_000_000;
const MAX_PRICE_CAP = 50_000_000;
const min = 0;

const { t } = useI18n();

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

const contentMeta = computed(() =>
  t('store.showingStyles', {
    count: totalProducts.value,
    total: totalAvailable.value,
  })
);

const mediaBackgrounds = reactive<Record<string, string>>({});
const imageLoaded = reactive<Record<string, boolean>>({});

function handleImageLoad(productId: string, event: Event) {
  const target = event.target as HTMLImageElement | null;
  if (!target) return;

  // Mark image as loaded
  imageLoaded[productId] = true;

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

function formatCategory(item: Product): string {
  // Format gender to match design: "Male" -> "Men", "Female" -> "Women"
  // Could be enhanced to add "Running", "Originals", etc. based on product type
  if (item.gender === 'Male') {
    return t('store.genderMen');
  }
  if (item.gender === 'Female') {
    return t('store.genderWomen');
  }
  return item.gender || '';
}

function getColourCount(item: Product): number {
  // Return a default count - this could be enhanced if color variants are available
  // For now, use a reasonable default based on product type
  return item.sizes?.length ? Math.min(item.sizes.length, 10) : 3;
}

function getSpecialTag(item: Product): string | null {
  // Show "Trending" for highly rated products
  if (item.starrating && item.starrating >= 4.5) {
    return t('store.tagTrending');
  }
  // Show "New" for products with no reviews yet (starrating = 0)
  if (item.starrating === 0 || !item.starrating) {
    return t('store.tagNew');
  }
  return null;
}
</script>

<style scoped lang="scss">
.storegrid {
  width: min(1240px, 100%);
  margin: clamp(32px, 6vw, 64px) auto;
  display: grid;
  grid-template-columns: minmax(0, 1fr);
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
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: clamp(24px, 3vw, 32px);
}

.item {
  position: relative;
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 16px;
  text-decoration: none;
  color: inherit;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease, outline 0.2s ease;
  overflow: hidden;
  aspect-ratio: 1 / 1.2;
}

.item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,.12);
  border-color: #b0b0b0;
  outline: 1px solid rgba(0,0,0,.12);
  outline-offset: -1px;
  z-index: 1;
}

.item__media {
  position: relative;
  flex: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  overflow: hidden;
  min-height: 0;
}

.image-skeleton {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.image-skeleton .skeleton-shimmer {
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    #f0f0f0 0%,
    #f8f8f8 50%,
    #f0f0f0 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
}

.item__media.image-loading img.image-placeholder,
.item__media img.image-placeholder {
  background: linear-gradient(
    90deg,
    #f0f0f0 0%,
    #f8f8f8 50%,
    #f0f0f0 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
}

.item__media img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  object-position: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.item__media img.loaded {
  opacity: 1;
}

.item__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px;
  gap: 6px;
}

.item__price {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: #111;
  line-height: 1.2;
  margin: 0;
}

.item__price-original {
  font-size: 14px;
  font-weight: 400;
  color: #9ca3af;
  text-decoration: line-through;
}

.item__price-current {
  font-size: 18px;
  font-weight: 700;
  color: #111;
}

.item__title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #111;
  line-height: 1.3;
}

.item__meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-top: 2px;
}

.item__category,
.item__colours {
  font-size: 13px;
  font-weight: 400;
  color: #6b7280;
  line-height: 1.4;
}

.item__tag {
  font-size: 13px;
  font-weight: 400;
  color: #6b7280;
  line-height: 1.4;
  margin-top: 2px;
}

/* removed aside filter-card and slider styling */

@media (max-width: 1024px) {
  .storegrid {
    grid-template-columns: 1fr;
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

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}
</style>
