<template>
  <RouterLink :to="`/product/${product.id}`" class="product-card">
    <div class="product-card__image">
      <img
        v-if="resolvedImageUrl"
        :src="resolvedImageUrl"
        :alt="product.name"
        @error="handleImageError"
      />
      <div v-else class="product-card__image-placeholder">
        <span>📦</span>
      </div>
    </div>
    <div class="product-card__body">
      <h4 class="product-card__name">{{ product.name }}</h4>
      <div class="product-card__price">
        <span v-if="product.min_price === product.max_price" class="price-single">
          {{ formatPrice(product.min_price) }}
        </span>
        <span v-else class="price-range">
          {{ formatPrice(product.min_price) }} - {{ formatPrice(product.max_price) }}
        </span>
      </div>
      <div v-if="product.stock > 0" class="product-card__stock">
        Còn {{ product.stock }} đôi
      </div>
    </div>
  </RouterLink>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { mapProductNameToImagePath } from '@/api/products'

interface Product {
  id: number
  name: string
  min_price: number
  max_price: number
  image_url?: string
  stock: number
}

const props = defineProps<{
  product: Product
}>()

const resolvedImageUrl = computed(() =>
  mapProductNameToImagePath(props.product.name, props.product.image_url ?? null)
)

function formatPrice(price: number): string {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)
}

function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement
  img.style.display = 'none'
  const placeholder = img.parentElement?.querySelector('.product-card__image-placeholder')
  if (placeholder) {
    ;(placeholder as HTMLElement).style.display = 'flex'
  }
}
</script>

<style scoped lang="less">
.product-card {
  display: flex;
  flex-direction: column;
  background: var(--color-bg-2);
  border: 1px solid var(--color-border-2);
  border-radius: 12px;
  overflow: hidden;
  text-decoration: none;
  color: inherit;
  transition: all 0.2s ease;
  cursor: pointer;
  max-width: 200px;
  min-width: 160px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    border-color: var(--color-primary, #111111);
  }

  &__image {
    width: 100%;
    height: 160px;
    background: var(--color-fill-1);
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    position: relative;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    &-placeholder {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 100%;
      font-size: 48px;
      color: var(--color-text-3);
    }
  }

  &__body {
    padding: 12px;
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  &__name {
    font-size: 14px;
    font-weight: 600;
    color: var(--color-text-1);
    margin: 0;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__price {
    font-size: 15px;
    font-weight: 700;
    color: var(--color-primary, #111111);

    .price-single,
    .price-range {
      display: block;
    }
  }

  &__stock {
    font-size: 12px;
    color: var(--color-text-3);
  }
}

.ai-chatbot.is-dark .product-card {
  background: #151823;
  border-color: #272b36;

  &:hover {
    border-color: rgba(255, 255, 255, 0.3);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  }

  &__image {
    background: #0f1420;
  }

  &__name {
    color: #e6e9ef;
  }

  &__price {
    color: #ffffff;
  }

  &__stock {
    color: #9aa4b2;
  }
}
</style>

