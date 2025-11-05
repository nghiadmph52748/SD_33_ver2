<template>
  <section>
    <h2>
      <span>{{ t('home.sections.featured') }}</span>
    </h2>
    <div class="featureditems">
      <div class="item" v-for="product in featuredProducts" :key="product.id">
        <img :src="product.img" :alt="product.name" />
        <h3>{{ product.name }}</h3>
        <h4>{{ formatCurrency(product.price) }}</h4>
        <RouterLink :to="`/product/${product.id}`">
          <button class="btn btn-outline">{{ t('buttons.viewItem') }} ></button>
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
    overflow: hidden;
  }
}

h4 {
  color: #111111;
  margin: 10px 0;
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

.featureditems .item:hover img {
  transform: scale(1.06);
}
</style>
