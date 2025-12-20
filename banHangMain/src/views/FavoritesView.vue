<template>
  <div class="favorites-page">
    <div class="container">
      <div v-if="favorites.length > 0">
        <AppStoreGrid :data="favorites" :title="$t('cart.favourites')" :eyebrow="''" />
      </div>
      <AppEmptyState v-else :title="$t('cart.favourites')" :description="$t('cart.viewFavourites')">
        <div class="actions">
          <RouterLink to="/all" class="btn primary">{{ $t("cart.browse") }}</RouterLink>
        </div>
      </AppEmptyState>
    </div>
  </div>
</template>

<script setup lang="ts">
import { storeToRefs } from "pinia";
import { useCartStore } from "@/stores/cart";
import AppStoreGrid from "@/components/AppStoreGrid.vue";
import AppEmptyState from "@/components/AppEmptyState.vue";

const cartStore = useCartStore();
const { favorites } = storeToRefs(cartStore);
</script>

<style scoped lang="scss">
.favorites-page {
  padding: 40px 0 80px;
  min-height: 60vh;
}

.actions {
  margin-top: 24px;
}
</style>
