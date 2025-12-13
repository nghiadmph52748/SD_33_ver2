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
          <h1 class="title">{{ $t('store.womenTitle') }} <span class="count" v-if="!loading">[{{ products.length }}]</span></h1>
          <p class="subtitle">
            {{ $t('store.womenSubtitle') }}
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
      <AppStoreGrid :data="products" :loading="loading" source-page="women" />
      <a-empty v-if="!loading && products.length === 0" :description="$t('store.noProducts')" />
    </div>
    <a-empty v-else-if="error" :description="error" />
    <FilterSortDrawer 
      v-model:visible="filterVisible" 
      :total="products.length" 
      :max-price="maxProductPrice"
      :brands="availableBrands"
      @apply="applyFilters" 
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from "vue";
import { storeToRefs } from "pinia";
import { useI18n } from "vue-i18n";
import AppStoreGrid from "@/components/AppStoreGrid.vue";
import { useCartStore } from "@/stores/cart";
import FilterSortDrawer from "@/components/FilterSortDrawer.vue";
import type { Product } from "@/api/products";

const { t } = useI18n();
const cartStore = useCartStore();
const { products: allProducts, loading, error } = storeToRefs(cartStore);
const filterVisible = ref(false);

const filterState = ref({
  sort: 'featured',
  price: [0, 50000000] as [number, number],
  categories: [] as string[],
});

onMounted(async () => {
  await cartStore.fetchProducts(true); // Only sneakers
});

// Get unique brands from women products
const availableBrands = computed(() => {
  if (!allProducts.value || !Array.isArray(allProducts.value)) {
    return [];
  }
  const brands = new Set<string>();
  allProducts.value
    .filter(p => p.gender === "Female")
    .forEach(p => {
      if (p.brand) {
        brands.add(p.brand);
      }
    });
  return Array.from(brands).sort();
});

function matchesFilter(product: Product, filters: string[]): boolean {
  if (filters.length === 0) return true;
  
  // Separate gender filters and brand filters
  const genderFilters = filters.filter(f => f === 'Male' || f === 'Female');
  const brandFilters = filters.filter(f => f !== 'Male' && f !== 'Female');
  
  // Check gender match (if gender filters are selected)
  // Note: We're already filtering for Female, but this allows for multiple gender selection
  if (genderFilters.length > 0) {
    const genderMatches = genderFilters.some(gender => product.gender === gender);
    if (!genderMatches) return false;
  }
  
  // Check brand match (if brand filters are selected)
  if (brandFilters.length > 0) {
    const brandMatches = brandFilters.some(brand => 
      product.brand && product.brand.toLowerCase() === brand.toLowerCase()
    );
    if (!brandMatches) return false;
  }
  
  return true;
}

// Calculate max price from women products (before filtering)
const maxProductPrice = computed(() => {
  if (!allProducts.value || !Array.isArray(allProducts.value)) {
    return 50000000;
  }
  const womenProducts = allProducts.value.filter(p => p.gender === "Female");
  if (womenProducts.length === 0) return 50000000;
  return Math.max(...womenProducts.map(p => p.priceMax || p.price));
});

const products = computed(() => {
  // Safety check: ensure allProducts is an array
  if (!allProducts.value || !Array.isArray(allProducts.value)) {
    return [];
  }

  // Start with all products filtered to Female gender
  let filtered = allProducts.value.filter(p => p.gender === "Female");
  
  // If no Female products, show some products as fallback (maybe products without gender set or all products)
  if (filtered.length === 0) {
    // Fallback: show first few products if no Female products found
    filtered = allProducts.value.slice(0, 10);
  }

  // Filter by brands (categories now contains brands)
  if (filterState.value.categories.length > 0) {
    filtered = filtered.filter(p => matchesFilter(p, filterState.value.categories));
  }

  // Filter by price range
  filtered = filtered.filter(p => {
    const productPrice = p.priceMax || p.price;
    return productPrice >= filterState.value.price[0] && productPrice <= filterState.value.price[1];
  });

  // Sort
  switch (filterState.value.sort) {
    case 'price_low_high':
      filtered.sort((a, b) => (a.priceMax || a.price) - (b.priceMax || b.price));
      break;
    case 'price_high_low':
      filtered.sort((a, b) => (b.priceMax || b.price) - (a.priceMax || a.price));
      break;
    case 'newest':
      filtered.sort((a, b) => Number(b.id) - Number(a.id));
      break;
    case 'featured':
    default:
      // Keep original order (featured products first)
      break;
  }

  return filtered;
});

function applyFilters(payload: { sort: string; price: [number, number]; categories: string[] }) {
  filterState.value = {
    sort: payload.sort,
    price: payload.price,
    categories: payload.categories,
  };
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
