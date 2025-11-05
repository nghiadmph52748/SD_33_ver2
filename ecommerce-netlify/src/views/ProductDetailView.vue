<template>
  <a-spin :loading="loading" class="product-page__spinner">
    <div v-if="product" class="product-page">
      <section class="product-hero">
        <div class="product-gallery">
          <div class="product-gallery__main">
            <img :src="activeImage" :alt="product.name" />
          </div>
          <div v-if="productImages.length > 1" class="product-gallery__thumbs">
            <button
              v-for="(image, index) in productImages"
              :key="image"
              type="button"
              :class="['product-gallery__thumb', { active: index === selectedImageIndex }]"
              @click="selectedImageIndex = index"
            >
              <img :src="image" :alt="`${product.name} #${index + 1}`" />
            </button>
          </div>
        </div>

        <a-card class="product-summary" :bordered="false" :body-style="{ padding: '28px 32px' }">
          <div class="product-summary__header">
            <a-tag v-if="product.gender" color="arcoblue" class="product-summary__tag">
              {{ product.gender }}
            </a-tag>
            <a-typography-title :heading="2" class="product-summary__title">
              {{ product.name }}
            </a-typography-title>
            <div class="product-summary__rating">
              <a-rate :model-value="product.starrating || 0" readonly :allow-half="false" />
              <span class="product-summary__rating-count">
                {{ (product.starrating || 0).toFixed(1) }}
              </span>
            </div>
          </div>

          <div class="product-summary__price-row">
            <a-typography-title :heading="3" class="product-summary__price">
              {{ formatCurrency(product.price) }}
            </a-typography-title>
            <span class="product-summary__vat">VAT included</span>
          </div>

          <p class="product-summary__description">
            {{ product.description }}
          </p>

          <div class="product-summary__meta">
            <div v-for="meta in productMeta" :key="meta.label" class="product-summary__meta-item">
              <span class="label">{{ meta.label }}</span>
              <span class="value">{{ meta.value }}</span>
            </div>
          </div>

          <a-alert
            v-if="showSizeRequiredMessage"
            type="error"
            :content="$t('product.sizeRequired')"
            class="product-summary__size-alert"
          />

          <div class="product-summary__actions">
            <div class="product-summary__controls">
              <div class="product-summary__control">
                <span>{{ $t("product.quantity") }}</span>
                <a-input-number v-model="quantity" :min="1" :max="99" />
              </div>
              <div v-if="product.sizes?.length" class="product-summary__control">
                <span>{{ $t("product.selectSize") }}</span>
                <a-select
                  v-model="size"
                  :placeholder="$t('product.selectSize')"
                  @change="showSizeRequiredMessage = false"
                >
                  <a-option
                    v-for="productSize in product.sizes"
                    :key="productSize"
                    :value="productSize"
                  >
                    {{ productSize }}
                  </a-option>
                </a-select>
              </div>
            </div>

            <div class="product-summary__buttons">
              <a-button type="primary" size="large" long @click="cartAdd">
                {{ $t("product.addToCart") }}
              </a-button>
              <a-button type="outline" size="large" long @click="$router.push('/all')">
                {{ $t("cart.browse") }}
              </a-button>
            </div>
          </div>

          <ul class="product-summary__highlights">
            <li v-for="highlight in serviceHighlights" :key="highlight.title">
              <span class="title">{{ highlight.title }}</span>
              <span class="description">{{ highlight.description }}</span>
            </li>
          </ul>
        </a-card>
      </section>

      <section class="product-details">
        <a-card class="product-details__card" :bordered="false">
          <a-tabs default-active-key="details">
            <a-tab-pane key="details" title="Product details">
              <div class="product-details__body">
                <p>{{ product.description }}</p>
                <a-descriptions :column="2" :data="productSpecs" class="product-details__specs" />
              </div>
            </a-tab-pane>
            <a-tab-pane key="reviews" :title="$t('product.reviews')">
              <div class="product-reviews">
                <div class="product-reviews__summary">
                  <a-rate :model-value="product.starrating || 0" readonly :allow-half="false" size="24" />
                  <div class="product-reviews__score">
                    <span class="score">{{ (product.starrating || 0).toFixed(1) }}</span>
                    <span class="label">{{ $t("product.reviews") }}</span>
                  </div>
                </div>
                <p class="product-reviews__text">
                  {{ product.review || $t("product.noReviews") || "No reviews yet" }}
                </p>
              </div>
            </a-tab-pane>
          </a-tabs>
        </a-card>
      </section>

      <AppFeaturedProducts />
    </div>

    <a-result
      v-else
      status="404"
      :title="$t('product.notFound')"
      :subtitle="$t('product.notFoundDesc')"
    >
      <template #extra>
        <a-button type="primary" @click="$router.push('/all')">
          {{ $t("cart.browse") }}
        </a-button>
      </template>
    </a-result>
  </a-spin>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useRoute } from "vue-router";
import AppFeaturedProducts from "@/components/AppFeaturedProducts.vue";
import { useCartStore, type CartItem } from "@/stores/cart";
import { formatCurrency } from "@/utils/currency";

const route = useRoute();
const cartStore = useCartStore();
const quantity = ref(1);
const size = ref<string | null>(null);
const showSizeRequiredMessage = ref(false);
const loading = ref(false);
const selectedImageIndex = ref(0);

const productId = computed(() => String(route.params.id ?? ""));

const product = computed(() => cartStore.products.find(item => item.id === productId.value));

const productImages = computed(() => {
  if (!product.value) return [];
  const images = new Set<string>();
  const primary = resolveProductImage(product.value.img);
  if (primary) {
    images.add(primary);
  }
  return images.size > 0 ? Array.from(images) : ["/products/1.jpg"];
});

const activeImage = computed(() => productImages.value[selectedImageIndex.value] ?? "/products/1.jpg");

watch(productImages, (images) => {
  if (!images.length) {
    selectedImageIndex.value = 0;
    return;
  }
  if (selectedImageIndex.value >= images.length) {
    selectedImageIndex.value = 0;
  }
}, { immediate: true });

const productSpecs = computed(() => {
  if (!product.value) return [];
  const specs: any[] = [];
  if (product.value.color) {
    specs.push({ label: 'Color', value: product.value.color });
  }
  if (product.value.sizes?.length) {
    specs.push({ label: 'Available Sizes', value: product.value.sizes.join(', ') });
  }
  return specs;
});

const productMeta = computed(() => {
  if (!product.value) return [];
  const meta: { label: string; value: string }[] = [];
  if (product.value.gender) {
    meta.push({ label: "Category", value: product.value.gender });
  }
  if (product.value.color) {
    meta.push({ label: "Colorway", value: product.value.color });
  }
  if (product.value.sizes?.length) {
    meta.push({ label: "Sizing", value: `${product.value.sizes.length} options` });
  }
  return meta;
});

const serviceHighlights = [
  {
    title: "Free nationwide shipping",
    description: "Complimentary delivery for orders over ₫2.000.000 within 2-4 working days."
  },
  {
    title: "30-day returns",
    description: "Exchange or refund unused items with the original receipt."
  },
  {
    title: "Member exclusive perks",
    description: "Earn points on every purchase and unlock early access to new drops."
  }
];

function resolveProductImage(imagePath: string | undefined | null): string | null {
  if (!imagePath) return null;
  const trimmed = imagePath.trim();

  if (/^https?:\/\//i.test(trimmed)) {
    return trimmed;
  }
  if (trimmed.startsWith("/")) {
    return trimmed;
  }
  if (trimmed.startsWith("products/")) {
    return `/${trimmed}`;
  }
  return `/products/${trimmed}`;
}

const cartAdd = () => {
  const selectedProduct = product.value;

  if (!selectedProduct) {
    return;
  }

  if (selectedProduct.sizes && selectedProduct.sizes.length && !size.value) {
    showSizeRequiredMessage.value = true;
    return;
  }

  const cartItem: CartItem = {
    ...selectedProduct,
    quantity: quantity.value,
    size: size.value ?? undefined
  };

  cartStore.addToCart(cartItem);
  cartStore.updateCartUI("idle");
  showSizeRequiredMessage.value = false;
};
</script>

<style scoped lang="scss">
.product-page__spinner {
  width: 100%;
  display: block;
}

.product-page {
  padding: clamp(32px, 6vw, 72px) clamp(16px, 6vw, 96px);
  display: flex;
  flex-direction: column;
  gap: clamp(40px, 5vw, 64px);
  background: linear-gradient(180deg, #f7f9fc 0%, #ffffff 55%);
  min-height: 100%;
  align-items: center;
}

.product-hero {
  display: grid;
  grid-template-columns: minmax(320px, 1.35fr) minmax(320px, 1fr);
  gap: clamp(32px, 6vw, 80px);
  align-items: stretch;
  max-width: 1180px;
  width: 100%;
  margin: 0 auto;
}

.product-gallery {
  background: #ffffff;
  border-radius: 24px;
  padding: clamp(20px, 3vw, 32px);
  box-shadow: 0 12px 40px rgba(15, 35, 95, 0.08);
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
}

.product-gallery__main {
  width: 100%;
  aspect-ratio: 4 / 3;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(22, 119, 255, 0.08), rgba(22, 119, 255, 0) 60%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.product-gallery__main img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform 0.3s ease;
}

.product-gallery__main:hover img {
  transform: scale(1.04);
}

.product-gallery__thumbs {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(72px, 1fr));
  gap: 12px;
}

.product-gallery__thumb {
  border: 1px solid transparent;
  border-radius: 16px;
  padding: 8px;
  background: #f5f7fb;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-gallery__thumb img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.product-gallery__thumb:hover,
.product-gallery__thumb.active {
  border-color: var(--color-primary-6, #1677ff);
  background: rgba(22, 119, 255, 0.08);
}

.product-summary {
  border-radius: 28px;
  box-shadow: 0 16px 50px rgba(15, 35, 95, 0.08);
  display: flex;
  flex-direction: column;
  gap: 28px;
  background: #ffffff;
  width: 100%;
}

.product-summary__header {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.product-summary__header > *:not(:last-child) {
  margin-bottom: 4px;
}

.product-summary__tag {
  width: fit-content;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.product-summary__title {
  margin: 0;
  line-height: 1.15;
}

.product-summary__rating {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-summary__rating-count {
  font-weight: 600;
  color: #1d2129;
  font-size: 16px;
}

.product-summary__price-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.product-summary__price {
  margin: 0;
  color: var(--color-primary-6, #1677ff);
  font-size: clamp(28px, 3vw, 36px);
}

.product-summary__vat {
  font-size: 14px;
  color: #86909c;
}

.product-summary__description {
  margin: 0;
  color: #4e5969;
  line-height: 1.6;
}

.product-summary__meta {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 16px;
  margin-bottom: 8px;
}

.product-summary__meta-item {
  padding: 12px 16px;
  border-radius: 16px;
  background: #f5f8ff;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-summary__meta-item .label {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #86909c;
}

.product-summary__meta-item .value {
  font-weight: 600;
  color: #1d2129;
}

.product-summary__size-alert {
  width: 100%;
}

.product-summary__actions {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.product-summary__controls {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.product-summary__control {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 16px;
  background: #f7f9fc;
  color: #1d2129;
  min-width: 220px;
}

.product-summary__buttons {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
}

.product-summary__highlights {
  margin: 8px 0 0;
  padding: 0;
  list-style: none;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: clamp(16px, 3vw, 24px);
}

.product-summary__highlights li {
  background: #f7f9fc;
  border-radius: 18px;
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: #1d2129;
}

.product-summary__highlights .title {
  font-weight: 600;
}

.product-summary__highlights .description {
  font-size: 13px;
  color: #86909c;
  line-height: 1.5;
}

.product-details__card {
  border-radius: 24px;
  box-shadow: 0 16px 50px rgba(15, 35, 95, 0.08);
  max-width: 1180px;
  margin: 0 auto;
  width: 100%;
  background: #ffffff;
}

.product-details {
  width: 100%;
  display: flex;
  justify-content: center;
}

.product-details__card :deep(.arco-card-body) {
  padding: clamp(24px, 4vw, 36px);
}

.product-details__body {
  display: flex;
  flex-direction: column;
  gap: 24px;
  color: #4e5969;
  line-height: 1.7;
}

.product-details__specs {
  background: #f7f9fc;
  padding: 24px;
  border-radius: 18px;
}

.product-reviews {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.product-reviews__summary {
  display: flex;
  align-items: center;
  gap: 18px;
}

.product-reviews__score .score {
  font-size: 32px;
  font-weight: 700;
  color: var(--color-primary-6, #1677ff);
  line-height: 1;
}

.product-reviews__score .label {
  display: block;
  font-size: 13px;
  color: #86909c;
}

.product-reviews__text {
  margin: 0;
  color: #4e5969;
  line-height: 1.6;
}

@media (max-width: 1024px) {
  .product-hero {
    grid-template-columns: 1fr;
    max-width: 100%;
  }

  .product-summary {
    order: -1;
  }
}

@media (max-width: 640px) {
  .product-page {
    padding: 32px 16px;
    gap: 32px;
  }

  .product-summary__controls {
    flex-direction: column;
    align-items: flex-start;
  }

  .product-summary__control {
    width: 100%;
    justify-content: space-between;
  }

  .product-gallery {
    padding: 16px;
  }

  .product-gallery__main {
    border-radius: 18px;
  }
}
</style>
