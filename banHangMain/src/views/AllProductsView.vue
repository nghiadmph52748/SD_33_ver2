<template>
  <div class="page">
    <div class="page__content">
      <FilterSidebar ref="filterSidebarRef" :max-price="maxProductPrice" :brands="availableBrands"
        @change="applyFilters" />
      <div class="page__main">
        <div class="main__header">
          <router-link class="back" to="/">
            <!-- Back chevron SVG -->
            <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true" class="back__icon">
              <path fill="currentColor" d="M15.41 7.41 14 6l-6 6 6 6 1.41-1.41L10.83 12z" />
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
              <h1 class="title">{{ $t('store.shoesTitle') }} <span class="count" v-if="!loading">[{{ products.length
              }}]</span></h1>
              <p class="subtitle">
                {{ $t('store.shoesSubtitle') }}
              </p>
            </div>
          </div>
        </div>

        <div v-if="!error">
          <AppStoreGrid :data="products" :loading="loading" />
          <a-empty v-if="!loading && products.length === 0" :description="$t('store.noProducts')" />
        </div>
        <a-empty v-else-if="error" :description="error" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed, watch } from "vue";
import { storeToRefs } from "pinia";
import { useRoute } from "vue-router";
import AppStoreGrid from "@/components/AppStoreGrid.vue";
import { useCartStore } from "@/stores/cart";
import FilterSidebar from "@/components/FilterSidebar.vue";
import type { Product } from "@/api/products";

const route = useRoute();
const cartStore = useCartStore();
const { products: allProducts, loading, error } = storeToRefs(cartStore);
const filterSidebarRef = ref<InstanceType<typeof FilterSidebar> | null>(null);

const filterState = ref({
  sort: 'featured',
  price: [0, 50000000] as [number, number],
  categories: [] as string[],
  search: '' as string,
});

onMounted(async () => {
  // Only fetch if products haven't been loaded yet
  if (allProducts.value.length === 0) {
    await cartStore.fetchProducts(true); // Only sneakers
  }

  // Check for brand query parameter and apply filter
  const brandQuery = route.query.brand as string | undefined;
  if (brandQuery) {
    filterState.value.categories = [brandQuery];
    if (filterSidebarRef.value) {
      filterSidebarRef.value.setState({
        sort: filterState.value.sort,
        price: filterState.value.price,
        categories: filterState.value.categories,
      });
    }
  }

  // Check for search query parameter
  const searchQuery = route.query.search as string | undefined;
  if (searchQuery) {
    filterState.value.search = searchQuery;
  }
});

// Watch for route query changes (e.g., when brand link is clicked)
watch(() => route.query.brand, (brandQuery) => {
  if (brandQuery) {
    const brand = String(brandQuery);
    if (!filterState.value.categories.includes(brand)) {
      filterState.value.categories = [brand];
      if (filterSidebarRef.value) {
        filterSidebarRef.value.setState({
          sort: filterState.value.sort,
          price: filterState.value.price,
          categories: filterState.value.categories,
        });
      }
    }
  }
});

// Watch for search query changes
watch(() => route.query.search, (searchQuery) => {
  if (searchQuery) {
    filterState.value.search = String(searchQuery);
  } else {
    filterState.value.search = '';
  }
});

// Get unique brands from all products
const availableBrands = computed(() => {
  const brands = new Set<string>();
  allProducts.value.forEach(p => {
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

// Calculate max price from all products (before filtering)
const maxProductPrice = computed(() => {
  if (allProducts.value.length === 0) return 50000000;
  return Math.max(...allProducts.value.map(p => p.priceMax || p.price));
});

const products = computed(() => {
  let filtered = [...allProducts.value];

  // Filter by search query (name, brand, or description)
  if (filterState.value.search) {
    const searchLower = filterState.value.search.toLowerCase().trim();
    filtered = filtered.filter(p => {
      const nameMatch = p.name?.toLowerCase().includes(searchLower);
      const brandMatch = p.brand?.toLowerCase().includes(searchLower);
      const descMatch = p.description?.toLowerCase().includes(searchLower);
      return nameMatch || brandMatch || descMatch;
    });
  }

  // Filter by brands and gender
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
      // Assuming newer products have higher IDs (may need adjustment based on actual data)
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
  filterState.value.sort = payload.sort;
  filterState.value.price = payload.price;
  filterState.value.categories = payload.categories;
}
</script>

<style scoped>
.page {
  width: 100%;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #ffffff;
}

/* .page__header removed */

.back {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #6b7280;
  font-size: 13px;
  text-transform: uppercase;
  font-weight: 600;
  letter-spacing: 0.05em;
  transition: color 0.2s;
}

.back:hover {
  color: #111;
}

.back__icon {
  display: inline-block;
  opacity: 0.7;
}

.breadcrumbs {
  margin-top: 16px;
  color: #9ca3af;
  font-size: 13px;
  font-weight: 500;
}

.breadcrumbs__link {
  color: #6b7280;
  transition: color 0.2s;
}

.breadcrumbs__link:hover {
  color: #111;
}

.breadcrumbs__sep {
  margin: 0 8px;
  color: #d1d5db;
}

.breadcrumbs__current {
  color: #111;
}

.title-row {
  margin-top: 24px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
}

.title {
  margin: 0;
  /* font-family: var(--font-family-serif, "Times New Roman", serif); - Removed to start using Inter */
  font-size: 48px;
  line-height: 1.1;
  letter-spacing: -0.02em;
  color: #111;
}

.count {
  font-weight: 400;
  font-size: 24px;
  color: #9ca3af;
  vertical-align: super;
  font-size: 16px;
  margin-left: 4px;
}

.subtitle {
  margin: 12px 0 0 0;
  color: #4b5563;
  font-size: 16px;
  font-weight: 400;
  max-width: 600px;
  line-height: 1.6;
}

.page__content {
  display: flex;
  flex: 1;
  max-width: 1320px;
  margin: 0 auto;
  /* Increased top padding to 40px */
  padding: 40px 24px 64px 24px;
  gap: 48px;
  align-items: flex-start;
}

.page__main {
  flex: 1;
  padding: 0;
}

.main__header {
  margin-bottom: 32px;
}

.page__main {
  flex: 1;
  padding: 0;
}

@media (max-width: 768px) {
  .page__header {
    padding: 24px 16px 0 16px;
    margin-bottom: 24px;
  }

  .title {
    font-size: 32px;
  }

  .subtitle {
    font-size: 15px;
  }

  .page__content {
    padding: 0 16px 40px 16px;
    gap: 32px;
    flex-direction: column;
  }
}
</style>
