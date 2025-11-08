<template>
  <div class="page">
    <div class="page__header">
      <router-link class="back" to="/">
        <!-- Back chevron SVG -->
        <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true" class="back__icon">
          <path fill="currentColor" d="M15.41 7.41 14 6l-6 6 6 6 1.41-1.41L10.83 12z"/>
        </svg>
        <span>{{ $t('store.back') }}</span>
      </router-link>
      <div class="breadcrumbs">
        <router-link to="/" class="breadcrumbs__link">{{ $t('nav.home') }}</router-link>
        <span class="breadcrumbs__sep">/</span>
        <span class="breadcrumbs__current">{{ $t('store.shoes') }}</span>
      </div>
      <div class="title-row">
        <div>
          <h1 class="title">{{ $t('store.menTitle') }} <span class="count" v-if="!loading">[{{ menProducts.length }}]</span></h1>
          <p class="subtitle">
            {{ $t('store.menSubtitle') }}
          </p>
        </div>
        <button type="button" class="filter-btn" @click="filterVisible = true">
          <!-- Filter & sort inline SVG icon -->
          <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
            <path fill="currentColor" d="M10 18h4v-2h-4v2ZM3 6v2h18V6H3Zm3 7h12v-2H6v2Z"/>
          </svg>
          <span>{{ $t('store.filterSort') }}</span>
        </button>
      </div>
    </div>

    <div v-if="!error">
      <AppStoreGrid :data="menProducts" :loading="loading" />
      <a-empty v-if="!loading && menProducts.length === 0" :description="$t('store.noProducts')" />
    </div>
    <a-empty v-else-if="error" :description="error" />
    <FilterSortDrawer v-model:visible="filterVisible" :total="menProducts.length" @apply="applyFilters" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { storeToRefs } from "pinia";
import { useI18n } from "vue-i18n";
import AppStoreGrid from "@/components/AppStoreGrid.vue";
import { useCartStore } from "@/stores/cart";
import FilterSortDrawer from "@/components/FilterSortDrawer.vue";

const { t } = useI18n();

const cartStore = useCartStore();
const { menProducts, loading, error } = storeToRefs(cartStore);
const filterVisible = ref(false);

onMounted(async () => {
  if (menProducts.value.length === 0) {
    await cartStore.fetchProducts(true); // Only sneakers
  }
});

function applyFilters(payload: { sort: string; price: [number, number]; categories: string[] }) {
  // Hook for future filter logic; currently just closes drawer via v-model.
  // Sorting and filtering can be implemented in store or locally.
}
</script>

<style scoped>
.page {
  width: 100%;
}

.page__header {
  max-width: 1200px;
  margin: 0 auto 24px auto;
  padding: 16px 12px 0 12px;
}

.back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #111;
  text-transform: uppercase;
  font-weight: 600;
  letter-spacing: .02em;
}

.back__icon {
  display: inline-block;
}

.breadcrumbs {
  margin-top: 12px;
  color: #6b7280;
  font-size: 14px;
}

.breadcrumbs__link {
  color: #6b7280;
}

.breadcrumbs__sep {
  margin: 0 6px;
}

.title-row {
  margin-top: 16px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
}

.title {
  margin: 0;
  font-size: 40px;
  line-height: 1.1;
  letter-spacing: .01em;
}

.count {
  font-weight: 400;
  font-size: 20px;
  color: #6b7280;
}

.subtitle {
  margin: 12px 0 0 0;
  color: #374151;
}

.filter-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 1px solid #d1d5db;
  padding: 10px 14px;
  border-radius: 8px;
  background: #fff;
  font-weight: 600;
  cursor: pointer;
  transition: border-color .15s ease, background-color .15s ease, transform .08s ease, box-shadow .08s ease;
}

.filter-btn:hover { border-color: #9ca3af; background: #f9fafb; transform: translateY(-1px); box-shadow: 0 2px 8px rgba(0,0,0,.08); }
.filter-btn:active { transform: translateY(0) scale(0.98); box-shadow: 0 1px 4px rgba(0,0,0,.1); }

@media (max-width: 768px) {
  .title { font-size: 30px; }
  .count { font-size: 16px; }
}
</style>
