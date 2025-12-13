<template>
  <section>
    <h2>
      <span>{{ t("home.sections.featured") }}</span>
    </h2>
    <div class="featureditems">
      <div class="item" v-for="product in featuredProducts" :key="product.id">
        <RouterLink
          class="image-link"
          :to="`/product/${product.id}`"
          :aria-label="t('buttons.viewItem')"
        >
          <img
            :src="product.img"
            :alt="product.name"
            loading="lazy"
            v-img-fallback
          />
        </RouterLink>
        <h3>{{ product.name }}</h3>
        <h4>
          <span
            v-if="
              product.originalPrice && product.originalPrice > product.price
            "
            class="price-original"
            >{{ formatCurrency(product.originalPrice) }}</span
          >
          <span class="price-current">
            {{ formatCurrency(product.price)
            }}<span
              v-if="product.priceMax && product.priceMax !== product.price"
            >
              - {{ formatCurrency(product.priceMax) }}</span
            >
          </span>
        </h4>
        <RouterLink :to="`/product/${product.id}`">
          <button class="btn btn-outline">{{ t("buttons.viewItem") }} ></button>
        </RouterLink>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { storeToRefs } from "pinia";
import { useI18n } from "vue-i18n";
import { useCartStore } from "@/stores/cart";
import { formatCurrency } from "@/utils/currency";

const cartStore = useCartStore();
const { featuredProducts } = storeToRefs(cartStore);
const { t } = useI18n();
</script>

<style scoped lang="scss">
section {
  margin-top: 60px;
}

.featureditems {
  width: 100%;
  margin: 20px 0 70px;

  .item {
    border: 1px solid #f0f0f0;
    border-radius: 16px;
    padding: 10px 20px 30px;
    min-height: 150px;
    justify-self: center;
    align-self: center;
    text-align: center;
    overflow: visible;
    transition: transform 0.2s ease, box-shadow 0.2s ease,
      border-color 0.2s ease;
    display: flex;
    flex-direction: column;
  }

  .item:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12), 0 0 0 1px rgba(0, 0, 0, 0.08);
    border-color: #d0d0d0;
  }
}

h4 {
  color: #111111;
  margin: 10px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.price-original {
  font-size: 14px;
  font-weight: 400;
  color: #9ca3af;
  text-decoration: line-through;
}

.price-current {
  font-size: 18px;
  font-weight: 700;
  color: #111;
}

h2 {
  color: #111111;
  text-align: center;
  overflow: hidden;
}

h2 span {
  display: inline-block;
  position: relative;
}

h2 span::after,
h2 span::before {
  content: " ";
  display: block;
  height: 1px;
  width: 1000px;
  background: #f0f0f0;
  position: absolute;
  top: 50%;
}

h2 span::before {
  left: -1010px;
}

h2 span::after {
  right: -1010px;
}

@media (max-width: 699px) {
  .featureditems {
    width: 83vw;
    margin-left: 5vw;

    div {
      padding: 10px 20px;
      margin-bottom: 10px;
    }
  }

  img {
    width: initial;
  }
}

@media (min-width: 700px) {
  .featureditems {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    grid-template-rows: 1fr;
    grid-column-gap: 20px;

    div {
      padding: 40px 50px;
    }
  }

  img {
    width: 100%;
    transition: transform 0.3s ease;
    transform-origin: center center;
  }
}

.featureditems .item:hover .image-link img {
  transform: scale(1.08);
}

.featureditems .item .btn {
  transition: transform 0.08s ease, box-shadow 0.15s ease;
}

.featureditems .item .btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.featureditems .item .btn:active {
  transform: translateY(0) scale(0.98);
}

.image-link {
  display: block;
  overflow: hidden;
  margin-bottom: 12px;
  flex-shrink: 0;
}
.image-link:hover {
  text-decoration: none;
}
.image-link img {
  cursor: pointer;
  display: block;
  width: 100%;
}

.featureditems .item img.image-placeholder {
  background: linear-gradient(90deg, #f0f0f0 0%, #f8f8f8 50%, #f0f0f0 100%);
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
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
