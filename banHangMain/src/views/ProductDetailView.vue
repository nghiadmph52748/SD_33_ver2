<template>
  <div class="product-detail-view">
    <div class="pdp" v-if="product">
      <section class="pdp-hero">
      <!-- Left: Gallery and Accordion -->
      <div class="left-panel">
        <!-- Gallery: left vertical thumbnails, right main image -->
      <div class="gallery" :aria-label="product.name">
          <div class="thumbs" role="list">
            <button
              v-for="(imgSrc, i) in galleryImages"
              :key="i"
              class="thumb"
              :class="{ active: activeImageIndex === i }"
              type="button"
              @click="activeImageIndex = i"
              :aria-label="$t('product.viewImage', { n: i + 1 })"
            >
              <img :src="imgSrc" :alt="`${product.name} thumbnail ${i+1}`" v-img-fallback />
            </button>
          </div>
          <div class="main" @mousemove="onMainImageMove" @mouseenter="onMainImageEnter" @mouseleave="onMainImageLeave">
            <transition name="pdp-fade-image" mode="out-in">
              <img
                ref="mainImgRef"
                :key="galleryImages[activeImageIndex]"
                :src="galleryImages[activeImageIndex]"
                :alt="`${product.name} main view`"
                v-img-fallback
              />
            </transition>
              </div>
        </div>

        <!-- Accordion: moved to left panel -->
    <section class="accordion" ref="accordionRef">
      <details open class="accordion-item">
        <summary class="accordion-summary">
              <span>{{ $t('product.description') }}</span>
          <span class="icons">
            <svg class="icon icon--plus" viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M11 11V5h2v6h6v2h-6v6h-2v-6H5v-2z"/></svg>
            <svg class="icon icon--minus" viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M19 13H5v-2h14z"/></svg>
          </span>
        </summary>
        <div class="accordion-panel">
          <p class="muted">{{ product.description }}</p>
        </div>
      </details>
      <details class="accordion-item">
        <summary class="accordion-summary">
              <span>{{ $t('product.details') }}</span>
          <span class="icons">
            <svg class="icon icon--plus" viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M11 11V5h2v6h6v2h-6v6h-2v-6H5v-2z"/></svg>
            <svg class="icon icon--minus" viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M19 13H5v-2h14z"/></svg>
          </span>
        </summary>
        <div class="accordion-panel">
          <ul class="specs">
            <li v-for="spec in productSpecs" :key="spec.label">
              <span class="label">{{ spec.label }}</span>
              <span class="value">{{ spec.value }}</span>
            </li>
          </ul>
        </div>
      </details>
      <details class="accordion-item">
        <summary class="accordion-summary">
          <span>{{ $t('product.reviews') }}</span>
          <span class="icons">
            <svg class="icon icon--plus" viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M11 11V5h2v6h6v2h-6v6h-2v-6H5v-2z"/></svg>
            <svg class="icon icon--minus" viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M19 13H5v-2h14z"/></svg>
          </span>
        </summary>
        <div class="accordion-panel">
          <div class="reviews">
            <div class="score">
              <span class="value">{{ averageRating.toFixed(1) }}</span>
              <span class="outof">/ 5</span>
            </div>
            <p class="muted" v-if="allReviews.length === 0">{{ product.review || $t('product.noReviews') || 'No reviews yet' }}</p>
          </div>
          <form class="review-form" @submit.prevent="submitReview">
            <div class="form-row">
                  <label>{{ $t('product.name') }}</label>
              <input class="input" v-model.trim="reviewName" :placeholder="$t('product.reviewNamePlaceholder')" />
            </div>
            <div class="form-row">
                  <label>{{ $t('product.rating') }}</label>
                  <div class="stars" role="radiogroup" :aria-label="$t('product.rating')">
                <button v-for="n in 5" :key="n" type="button" class="star-btn" :class="{ active: reviewRating >= n }" @click="reviewRating = n" :aria-label="$t('product.rateStars', { n })">
                  <svg viewBox="0 0 24 24" width="24" height="24" aria-hidden="true"><path :fill="reviewRating >= n ? '#f59e0b' : '#d1d5db'" d="M12 17.27 18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/></svg>
                  </button>
              </div>
            </div>
            <div class="form-row">
                  <label>{{ $t('product.comment') }}</label>
              <textarea class="textarea" v-model.trim="reviewComment" rows="3" :placeholder="$t('product.reviewCommentPlaceholder')" />
            </div>
            <div class="form-actions">
                  <button class="btn-submit" :disabled="!canSubmitReview">{{ $t('product.submitReview') }}</button>
          </div>
          </form>

          <ul v-if="allReviews.length" class="review-list">
            <li v-for="(r, idx) in allReviews" :key="idx" class="review-item">
              <div class="review-head">
                <strong class="review-name">{{ r.name || $t('product.reviewAnonymous') }}</strong>
                <span class="review-date">{{ r.date }}</span>
                <span class="review-stars">{{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5 - r.rating) }}</span>
              </div>
              <p class="review-text">{{ r.comment }}</p>
            </li>
          </ul>
        </div>
      </details>
    </section>
      </div>

      <!-- Right: sticky purchase column -->
      <aside class="purchase">
        <p class="product-category" v-if="product.gender">{{ formatGender(product.gender) }} • {{ $t('product.sportswear') }}</p>
        <h1 class="product-title">{{ product.name }}</h1>

        <div class="price">
          <span v-if="product.originalPrice && product.originalPrice > product.price" class="price-original">{{ formatCurrency(product.originalPrice) }}</span>
          <span class="price-current">{{ formatCurrency(product.price) }}</span>
        </div>

        <div class="color-selector" v-if="colorSwatches.length">
          <label>{{ $t('product.color') }}:</label>
          <ul>
            <li v-for="(swatch, idx) in colorSwatches" :key="swatch.label" class="swatch-item">
              <button type="button" :class="['swatch', 'swatch-color', { active: selectedColorIndex === idx }]" @click="selectedColorIndex = idx">
                <div class="color-block" :style="{ backgroundColor: swatch.colorHex }"></div>
              </button>
              <span class="swatch-name">{{ swatch.label }}</span>
            </li>
          </ul>
        </div>

        <div class="size-selector" v-if="sizeOptions.length">
          <div class="size-row">
            <label>{{ $t('product.sizes') }}:</label>
            <button class="size-guide" type="button" @click="toggleSizeGuide(true)">{{ $t('product.sizeGuide') }}</button>
          </div>
          <div class="size-grid">
            <button
              v-for="productSize in sizeOptions"
              :key="productSize"
              type="button"
              class="size-btn"
              :disabled="isOutOfStock(productSize)"
              :class="{ active: size === productSize, disabled: isOutOfStock(productSize) }"
              @click="!isOutOfStock(productSize) && (size = productSize) && (showSizeRequiredMessage = false)"
            >
              <span class="size-label">{{ productSize }}</span>
              <span v-if="getStockForSize(productSize) !== null" class="size-stock">
                ({{ getStockForSize(productSize) }})
              </span>
            </button>
          </div>
          <p v-if="showSizeRequiredMessage" class="size-error">{{ $t('product.sizeRequired') }}</p>
        </div>

        <div class="qty">
          <label>{{ $t('product.quantity') }}</label>
                <div class="pill">
                  <button class="pill-btn" @click="quantity = Math.max(1, quantity - 1)">−</button>
                  <span class="qty-val">{{ quantity }}</span>
                  <button class="pill-btn" @click="quantity = Math.min(availableStock !== null ? availableStock : 99, quantity + 1)">+</button>
                </div>
              </div>

        <div v-if="availableStock !== null" class="stock-info">
          <span v-if="availableStock > 0" class="stock-available">
            {{ $t('product.stockAvailable', { count: availableStock }) }}
          </span>
          <span v-else class="stock-out">
            {{ $t('product.outOfStock') }}
          </span>
        </div>

        <div class="cta-block">
          <button class="add-to-bag" @click="cartAdd">{{ $t('product.addToBag') }}</button>
          <button class="wishlist" @click="$router.push('/all')">{{ $t('product.browseMore') }}</button>
        </div>

        <ul class="product-meta">
          <li v-for="highlight in serviceHighlights" :key="highlight.titleKey">{{ $t(highlight.titleKey) }}</li>
        </ul>
      </aside>
    </section>

    

    <!-- Recommendations -->
    <section class="recommendations">
      <div class="recommendations-header">
        <h2>{{ $t('product.youMayAlsoLike') }}</h2>
        <div class="recommendations-controls">
          <button 
            type="button" 
            class="nav-btn nav-btn--prev" 
            @click="scrollRecommendations(-1)"
            :aria-label="$t('product.scrollLeft')"
          >
            <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true">
              <path fill="currentColor" d="M15.41 7.41 14 6l-6 6 6 6 1.41-1.41L10.83 12z"/>
            </svg>
          </button>
          <button 
            type="button" 
            class="nav-btn nav-btn--next" 
            @click="scrollRecommendations(1)"
            :aria-label="$t('product.scrollRight')"
          >
            <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true">
              <path fill="currentColor" d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/>
            </svg>
          </button>
        </div>
      </div>
      <AppRecommendationsCarousel ref="recommendationsCarouselRef" />
    </section>
      <!-- Service cards at the very bottom -->
      <div class="container"><AppSalesBoxes /></div>
    </div>

    <div v-else class="pdp-not-found">
      <p>{{ $t('product.notFoundDesc') }}</p>
      <button class="btn" @click="$router.push('/all')">{{ $t('cart.browse') }}</button>
    </div>

    <teleport to="body">
      <transition name="modal-fade">
        <div
          v-if="showSizeGuideModal"
          class="modal-overlay"
          role="dialog"
          aria-modal="true"
          aria-labelledby="size-guide-title"
        >
          <div class="modal">
            <header class="modal-header">
              <h2 id="size-guide-title">{{ $t('product.sizeGuide') }}</h2>
              <button
                class="modal-close"
                type="button"
                @click="toggleSizeGuide(false)"
                aria-label="Close size guide"
              >
                ×
              </button>
            </header>
            <section class="modal-body">
              <table class="size-guide-table">
                <thead>
                  <tr>
                    <th>US</th>
                    <th>UK</th>
                    <th>EU</th>
                    <th>CM</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="entry in sizeGuide" :key="entry.us">
                    <td>{{ entry.us }}</td>
                    <td>{{ entry.uk }}</td>
                    <td>{{ entry.eu }}</td>
                    <td>{{ entry.cm }}</td>
                  </tr>
                </tbody>
              </table>
              <p class="modal-note">
                {{ $t('product.sizeGuide') }} {{ $t('product.description').toLowerCase() }}
              </p>
            </section>
          </div>
        </div>
      </transition>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted, nextTick } from "vue";
import { useRoute } from "vue-router";
import AppRecommendationsCarousel from "@/components/AppRecommendationsCarousel.vue";
import AppSalesBoxes from "@/components/AppSalesBoxes.vue";
import { useCartStore, type CartItem } from "@/stores/cart";
import { formatCurrency } from "@/utils/currency";
import { getProductById, type BackendProduct } from "@/api/products";
import { fetchVariantsByProduct, type ProductVariantResponse } from "@/api/variants";
import { useI18n } from "vue-i18n";
import { getColorHex } from "@/utils/colorMapping";

const route = useRoute();
const cartStore = useCartStore();
const quantity = ref(1);
const size = ref<string | null>(null);
const showSizeRequiredMessage = ref(false);
const loading = ref(false);
const selectedColorIndex = ref(0);
const activeImageIndex = ref(0);
const mainImgRef = ref<HTMLImageElement | null>(null)
const recommendationsCarouselRef = ref<InstanceType<typeof AppRecommendationsCarousel> | null>(null)
const productDetail = ref<BackendProduct | null>(null)
const detailLoading = ref(false)
const variants = ref<ProductVariantResponse[]>([])
const showSizeGuideModal = ref(false)
const { t } = useI18n()

function onMainImageEnter() {
  const el = mainImgRef.value
  if (!el) return
  el.style.transition = 'transform .12s ease'
  el.style.transform = 'scale(1.6)'
}

function onMainImageLeave() {
  const el = mainImgRef.value
  if (!el) return
  el.style.transition = 'transform .2s ease'
  el.style.transformOrigin = 'center center'
  el.style.transform = 'scale(1)'
}

function onMainImageMove(e: MouseEvent) {
  const el = mainImgRef.value
  if (!el) return
  const rect = el.getBoundingClientRect()
  const x = ((e.clientX - rect.left) / rect.width) * 100
  const y = ((e.clientY - rect.top) / rect.height) * 100
  el.style.transformOrigin = `${x}% ${y}%`
}

function formatGender(gender: string): string {
  const normalized = gender?.trim().toLowerCase()
  if (normalized === 'male' || normalized === 'men') {
    return t('product.genderMen')
  }
  if (normalized === 'female' || normalized === 'women') {
    return t('product.genderWomen')
  }
  return gender
}

const productId = computed(() => String(route.params.id ?? ""));

const baseProduct = computed(() => cartStore.products.find(item => item.id === productId.value));

// Find variant by color and size
const selectedVariant = computed(() => {
  const color = selectedColor.value
  const sizeValue = size.value
  
  if (!color || !sizeValue || variants.value.length === 0) {
    // If no color/size selected, return first variant
    return variants.value[0] || null
  }
  
  // Find variant matching both color and size
  const matched = variants.value.find(v => {
    const variantColor = (v.tenMauSac || "").trim()
    const variantSize = (v.tenKichThuoc || "").trim()
    return variantColor === color && variantSize === sizeValue
  })
  
  // If no exact match, try to find by color only
  if (!matched) {
    const colorMatch = variants.value.find(v => {
      const variantColor = (v.tenMauSac || "").trim()
      return variantColor === color
    })
    return colorMatch || variants.value[0] || null
  }
  
  return matched
})

// Calculate product with prices from selected variant
const product = computed(() => {
  const base = baseProduct.value
  if (!base) return null
  
  // Get price from selected variant
  const variant = selectedVariant.value
  if (variant && variant.giaBan != null) {
    const originalPrice = variant.giaBan
    let discountedPrice = originalPrice
    
    // Calculate discounted price if there's a discount
    if (variant.giaTriGiamGia != null && variant.giaTriGiamGia > 0) {
      discountedPrice = originalPrice * (100 - variant.giaTriGiamGia) / 100
    }
    
    return {
      ...base,
      price: discountedPrice,
      originalPrice: discountedPrice < originalPrice ? originalPrice : undefined
    }
  }
  
  // Fallback to base product price
  return base
});

async function loadProductDetail() {
  if (!productId.value) return
  detailLoading.value = true
  try {
    const res = await getProductById(productId.value)
    if (res?.data) {
      productDetail.value = res.data
    }
    const numericId = Number(productId.value)
    if (!Number.isNaN(numericId)) {
      variants.value = await fetchVariantsByProduct(numericId)
    } else {
      variants.value = []
    }
  } catch (error) {
    console.error("Failed to load product detail", error)
    productDetail.value = null
    variants.value = []
  } finally {
    detailLoading.value = false
  }
}

watch(productId, () => {
  void loadProductDetail()
}, { immediate: true })

const variantsByColor = computed(() => {
  const map: Record<string, { sizes: Set<string>; image?: string | null }> = {}
  const variantList = variants.value || []

  variantList.forEach((variant) => {
    const colorName = (variant.tenMauSac || "").trim() || product.value?.color || "Default"
    const sizeValue = variant.tenKichThuoc?.trim() || ""
    if (!map[colorName]) {
      map[colorName] = { sizes: new Set(), image: undefined }
    }
    if (sizeValue) {
      map[colorName].sizes.add(sizeValue)
    }
    if (!map[colorName].image && Array.isArray(variant.anhSanPham) && variant.anhSanPham.length > 0) {
      const firstImage = variant.anhSanPham[0]
      const url = typeof firstImage === 'string' ? firstImage : firstImage?.duongDanAnh
      map[colorName].image = resolveProductImage(url) || undefined
    }
  })
  return map
})

const colorOptions = computed(() => Object.keys(variantsByColor.value))

watch(colorOptions, (colors) => {
  if (selectedColorIndex.value >= colors.length) {
    selectedColorIndex.value = 0
  }
})

const selectedColor = computed(() => colorOptions.value[selectedColorIndex.value] || null)

const sizeOptions = computed(() => {
  const color = selectedColor.value
  const sizesForColor = color ? variantsByColor.value[color]?.sizes : undefined
  if (sizesForColor && sizesForColor.size > 0) {
    const sizes = Array.from(sizesForColor)
    cartStore.sizeMapping[color!] = sizes
    return sizes
  }

  const aggregated = new Set<string>()
  Object.values(variantsByColor.value).forEach((entry) => {
    entry.sizes.forEach((sizeVal) => aggregated.add(sizeVal))
  })
  if (aggregated.size > 0) {
    const sizes = Array.from(aggregated)
    cartStore.sizeMapping.ALL = sizes
    return sizes
  }
  if (product.value?.sizes?.length) {
    cartStore.sizeMapping.ALL = product.value.sizes
    return product.value.sizes
  }
  return []
})

watch(sizeOptions, (sizes) => {
  if (sizes.length === 0) {
    size.value = null
    showSizeRequiredMessage.value = false
    return
  }
  if (size.value && !sizes.includes(size.value)) {
    size.value = null
  }
})

const detailImages = computed(() => {
  const collected = new Set<string>()
  variants.value.forEach((variant) => {
    if (Array.isArray(variant.anhSanPham)) {
      variant.anhSanPham.forEach((item) => {
        const url = typeof item === 'string' ? item : item?.duongDanAnh
        const resolved = resolveProductImage(url)
        if (resolved) collected.add(resolved)
      })
    }
  })
  const imgs = Array.from(collected)
  if (imgs && imgs.length > 0) {
    return imgs
  }
  return null
})

const galleryImages = computed(() => {
  const baseFallback = resolveProductImage(product.value?.img) || "/products/1.jpg"
  const images = detailImages.value ? [...detailImages.value] : [baseFallback]
  if (selectedColor.value) {
    const colorKey = selectedColor.value
    const candidateName = productDetail.value?.tenSanPham
      ? `${productDetail.value.tenSanPham} - ${colorKey}`
      : colorKey
    // Attempt to prioritize color-specific image if it exists in detailImages
    const match = images.find((src) => src?.toLowerCase().includes(colorKey.toLowerCase()))
    if (match) {
      images.unshift(match)
    } else if (candidateName && images.length === 1) {
      images[0] = images[0]
    }
  }
  const unique = Array.from(new Set(images))
  if (unique.length === 1) {
    return Array(4).fill(unique[0])
  }
  return unique
})

watch(galleryImages, () => {
  activeImageIndex.value = 0
})

watch(selectedColor, () => {
  size.value = null
  showSizeRequiredMessage.value = false
  const color = selectedColor.value
  if (!color) return

  const normalizedColor = color.toLowerCase()

  const matchIndex = galleryImages.value.findIndex((src) =>
    src?.toLowerCase().includes(normalizedColor)
  )
  if (matchIndex >= 0) {
    activeImageIndex.value = matchIndex
  } else if (variantsByColor.value[color]?.image) {
    const target = resolveProductImage(variantsByColor.value[color]?.image)
    const index = galleryImages.value.findIndex((src) => src === target)
    if (index >= 0) {
      activeImageIndex.value = index
    }
  } else {
    activeImageIndex.value = 0
  }
})

const colorSwatches = computed(() => {
  if (colorOptions.value.length === 0) {
    return [{
      label: product.value?.color || 'Default',
      image: galleryImages.value[0],
      colorHex: getColorHex(product.value?.color),
    }]
  }
  return colorOptions.value.map((color) => {
    const preferred = variantsByColor.value[color]?.image
    const match = preferred || galleryImages.value.find((src) => src?.toLowerCase().includes(color.toLowerCase()))
    return {
      label: color,
      image: match || galleryImages.value[0],
      colorHex: getColorHex(color),
    }
  })
})

const productSpecs = computed(() => {
  if (!product.value) return [];
  const specs: any[] = [];
  if (product.value.color) {
    specs.push({ label: t('product.specColor'), value: product.value.color });
  }
  if (product.value.sizes?.length) {
    specs.push({ label: t('product.specSizes'), value: product.value.sizes.join(', ') });
  }
  return specs;
});

const productMeta = computed(() => {
  if (!product.value) return [];
  const meta: { label: string; value: string }[] = [];
  if (product.value.gender) {
    meta.push({ label: t('product.metaCategory'), value: formatGender(product.value.gender) });
  }
  if (selectedColor.value) {
    meta.push({ label: t('product.metaColorway'), value: selectedColor.value });
  } else if (product.value.color) {
    meta.push({ label: t('product.metaColorway'), value: product.value.color });
  }
  const sizes = sizeOptions.value
  if (sizes?.length) {
    meta.push({ label: t('product.metaSizing'), value: t('product.metaSizingOptions', { count: sizes.length }) });
  }
  return meta;
});

const serviceHighlights = [
  { titleKey: 'product.highlightShipping' },
  { titleKey: 'product.highlightReturns' },
  { titleKey: 'product.highlightPerks' },
];

const sizeGuide = [
  { us: '7', uk: '6', eu: '40', cm: '25' },
  { us: '7.5', uk: '6.5', eu: '40.5', cm: '25.5' },
  { us: '8', uk: '7', eu: '41', cm: '26' },
  { us: '8.5', uk: '7.5', eu: '42', cm: '26.5' },
  { us: '9', uk: '8', eu: '42.5', cm: '27' },
  { us: '9.5', uk: '8.5', eu: '43', cm: '27.5' },
  { us: '10', uk: '9', eu: '44', cm: '28' }
]

function toggleSizeGuide(visible: boolean) {
  showSizeGuideModal.value = visible
  if (visible) {
    document.body.classList.add('no-scroll')
  } else {
    document.body.classList.remove('no-scroll')
  }
}

watch(showSizeGuideModal, (visible) => {
  if (!visible) {
    document.body.classList.remove('no-scroll')
  }
})

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

  const requiresSizeSelection = sizeOptions.value.length > 0

  if (requiresSizeSelection && !size.value) {
    showSizeRequiredMessage.value = true;
    return;
  }

  // Find variant ID from selected variant
  const variantId = selectedVariant.value?.id
  
  // Check stock availability
  const stock = availableStock.value
  if (stock !== null && stock < quantity.value) {
    showSizeRequiredMessage.value = false
    // You can add a toast/notification here if needed
    return
  }

  const cartItem: CartItem = {
    ...selectedProduct,
    color: selectedColor.value || selectedProduct.color,
    img: galleryImages.value[activeImageIndex.value] || selectedProduct.img,
    quantity: quantity.value,
    size: size.value ?? undefined,
    // Store variant ID for order creation
    ...(variantId ? { idBienThe: variantId } : {})
  } as CartItem & { idBienThe?: number };

  cartStore.addToCart(cartItem);
  cartStore.updateCartUI("idle");
  showSizeRequiredMessage.value = false;
};

// Get stock for a specific size (considering selected color)
function getStockForSize(sizeVal: string | number): number | null {
  const color = selectedColor.value
  const sizeStr = String(sizeVal).trim()
  
  // Find variant matching color and size
  const variant = variants.value.find(v => {
    const variantColor = (v.tenMauSac || "").trim()
    const variantSize = (v.tenKichThuoc || "").trim()
    const colorMatch = color ? variantColor === color : true
    return colorMatch && variantSize === sizeStr
  })
  
  if (variant && variant.soLuong !== undefined) {
    return variant.soLuong
  }
  
  // If no exact match, try to find by size only (aggregate all colors)
  if (!color) {
    const sizeVariants = variants.value.filter(v => {
      const variantSize = (v.tenKichThuoc || "").trim()
      return variantSize === sizeStr
    })
    if (sizeVariants.length > 0) {
      const totalStock = sizeVariants.reduce((sum, v) => sum + (v.soLuong || 0), 0)
      return totalStock > 0 ? totalStock : 0
    }
  }
  
  return null
}

function isOutOfStock(sizeVal: string | number): boolean {
  const stock = getStockForSize(sizeVal)
  return stock !== null && stock <= 0
}

// Get available stock for selected variant
const availableStock = computed(() => {
  // If size is selected, get stock for that specific size
  if (size.value) {
    const stock = getStockForSize(size.value)
    return stock
  }
  
  // If only color is selected, sum stock for all sizes of that color
  if (selectedColor.value) {
    const colorVariants = variants.value.filter(v => {
      const variantColor = (v.tenMauSac || "").trim()
      return variantColor === selectedColor.value
    })
    if (colorVariants.length > 0) {
      const totalStock = colorVariants.reduce((sum, v) => sum + (v.soLuong || 0), 0)
      return totalStock > 0 ? totalStock : 0
    }
  }
  
  // If no selection, calculate total stock from all variants
  if (variants.value.length > 0) {
    const totalStock = variants.value.reduce((sum, v) => {
      return sum + (v.soLuong || 0)
    }, 0)
    return totalStock > 0 ? totalStock : null
  }
  return null
})

// Reviews state (local only)
type LocalReview = { name: string; rating: number; comment: string; date: string }
const localReviews = ref<LocalReview[]>([])
const reviewName = ref('')
const reviewRating = ref(0)
const reviewComment = ref('')

const canSubmitReview = computed(() => reviewRating.value > 0 && reviewComment.value.length > 0)

function submitReview() {
  if (!canSubmitReview.value) return
  const now = new Date()
  localReviews.value.unshift({
    name: reviewName.value.trim(),
    rating: reviewRating.value,
    comment: reviewComment.value.trim(),
    date: now.toLocaleDateString()
  })
  reviewName.value = ''
  reviewRating.value = 0
  reviewComment.value = ''
}

const allReviews = computed(() => localReviews.value)
const averageRating = computed(() => {
  if (allReviews.value.length === 0) return Number(product.value?.starrating || 0)
  const sum = allReviews.value.reduce((acc, r) => acc + r.rating, 0)
  return sum / allReviews.value.length
})

// Smooth open/close animation for accordion <details>
const accordionRef = ref<HTMLElement | null>(null)

async function initAccordion() {
  await nextTick()
  const root = accordionRef.value
  if (!root) return
  const items = Array.from(root.querySelectorAll<HTMLDetailsElement>('.accordion-item'))
  items.forEach((el) => setupAccordion(el))
}

onMounted(() => {
  void initAccordion()
})

watch(product, (val) => {
  if (val) {
    void initAccordion()
  }
})

function scrollRecommendations(direction: number) {
  if (recommendationsCarouselRef.value) {
    recommendationsCarouselRef.value.scrollBy(direction)
  }
}

function setupAccordion(el: HTMLDetailsElement) {
  const panel = el.querySelector<HTMLElement>('.accordion-panel')
  if (!panel) return
  panel.style.overflow = 'hidden'
  panel.style.transition = 'height 0.3s cubic-bezier(0.4, 0, 0.2, 1), padding 0.3s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.3s ease'
  
  // Set initial height
  if (el.open) {
    panel.style.height = panel.scrollHeight + 'px'
    panel.style.opacity = '1'
  } else {
    panel.style.height = '0px'
    panel.style.opacity = '0'
  }
  
  el.addEventListener('toggle', () => {
    if (el.open) {
      // expanding
      const targetHeight = panel.scrollHeight
      panel.style.height = targetHeight + 'px'
      panel.style.opacity = '1'
      
      const after = () => {
        panel.style.height = 'auto'
        panel.removeEventListener('transitionend', after)
      }
      panel.addEventListener('transitionend', after, { once: true })
    } else {
      // collapsing from auto height back to 0
      const currentHeight = panel.scrollHeight
      panel.style.height = currentHeight + 'px'
      panel.style.opacity = '0'
      
      // Force reflow to ensure height is set before transition
      void panel.offsetHeight
      
      requestAnimationFrame(() => {
      panel.style.height = '0px'
      })
    }
  })
}
</script>

<style scoped>
.pdp {
  padding: 32px 24px;
  max-width: 1240px;
  margin: 0 auto;
}
.pdp-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(0, 1fr);
  gap: 32px;
  align-items: start;
}

.left-panel {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.gallery { display: grid; grid-template-columns: 120px 1fr; gap: 12px; align-items: stretch; }
.gallery .main { position: relative; overflow: hidden; min-height: 500px; }
.gallery .main img { display: block; width: 100%; height: 100%; object-fit: contain; border-radius: 10px; background: #f5f5f5; cursor: zoom-in; will-change: transform, transform-origin; }
.thumbs { display: flex; flex-direction: column; gap: 8px; }
.thumb { padding: 0; border: 1px solid #e5e7eb; border-radius: 8px; background: #fff; overflow: hidden; cursor: pointer; }
.thumb.active { border-color: #111; box-shadow: 0 0 0 2px #111 inset; }
.thumb img { display: block; width: 100%; aspect-ratio: 1 / 1; object-fit: cover; background: #f5f5f5; }

.purchase { 
  position: sticky; 
  top: 80px; 
  align-self: start; 
  display: flex; 
  flex-direction: column; 
  gap: 16px; 
  max-height: calc(100vh - 100px);
  overflow-y: auto;
}
.purchase::-webkit-scrollbar {
  width: 6px;
}
.purchase::-webkit-scrollbar-track {
  background: transparent;
}
.purchase::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}
.purchase::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}
.product-category { margin: 0; text-transform: uppercase; letter-spacing: .08em; color: #6b7280; font-size: 12px; }
.product-title { margin: 0; font-size: 32px; line-height: 1.2; }
.price {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 700;
  font-size: 24px;
}

.price-original {
  font-size: 18px;
  font-weight: 400;
  color: #9ca3af;
  text-decoration: line-through;
}

.price-current {
  font-size: 24px;
  font-weight: 700;
  color: #111;
}

.color-selector label { display: block; margin-bottom: 8px; color: #374151; }
.color-selector ul { list-style: none; padding: 0; margin: 0; display: flex; gap: 12px; flex-wrap: wrap; }
.swatch-item { display: flex; flex-direction: column; align-items: center; gap: 4px; min-width: 60px; }
.swatch { width: 44px; height: 44px; border-radius: 8px; border: 1px solid #e5e7eb; padding: 2px; background: #fff; }
.swatch.active { border-color: #111; box-shadow: 0 0 0 2px #111 inset; }
.swatch img { width: 100%; height: 100%; object-fit: cover; border-radius: 6px; }
.swatch-color .color-block { width: 100%; height: 100%; border-radius: 6px; border: 1px solid rgba(0, 0, 0, 0.1); }
.swatch-name { font-size: 12px; color: #4b5563; text-transform: capitalize; text-align: center; }

.no-scroll {
  overflow: hidden;
}
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1300;
  padding: 24px;
}
.modal {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.2);
  max-width: 640px;
  width: 100%;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 24px;
  border-bottom: 1px solid #f1f5f9;
}
.modal-header h2 {
  margin: 0;
  font-size: 20px;
  letter-spacing: 0.05em;
}
.modal-close {
  border: none;
  background: transparent;
  font-size: 24px;
  cursor: pointer;
  line-height: 1;
}
.modal-body {
  padding: 24px;
  overflow-y: auto;
}
.size-guide-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 16px;
}
.size-guide-table th,
.size-guide-table td {
  padding: 10px 12px;
  text-align: center;
  border: 1px solid #e2e8f0;
  font-size: 14px;
}
.size-guide-table th {
  background: #f8fafc;
  font-weight: 700;
  letter-spacing: .04em;
}
.modal-note {
  color: #475569;
  font-size: 13px;
  margin: 0;
}
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.2s ease;
}
.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.size-row { display: flex; align-items: baseline; justify-content: space-between; }
.size-guide {
  color: #111;
  text-decoration: underline;
  font-size: 12px;
  background: transparent;
  border: none;
  padding: 0;
  cursor: pointer;
  font-weight: 600;
}
.size-guide:hover {
  color: #000;
}
.size-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 8px; }
.size-btn { 
  padding: 8px 0; 
  border: 1px solid #e5e7eb; 
  border-radius: 8px; 
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}
.size-btn.active { border-color: #111; box-shadow: 0 0 0 2px #111 inset; }
.size-btn.disabled { color: #9ca3af; text-decoration: line-through; background: #fafafa; }
.size-label { font-weight: 600; font-size: 14px; }
.size-stock { font-size: 10px; color: #6b7280; font-weight: 400; line-height: 1; }
.size-btn.disabled .size-stock { color: #9ca3af; }
.size-error { color: #dc2626; margin: 0; font-size: 13px; }

.qty { display: flex; align-items: center; gap: 12px; }
.pill { display: inline-flex; align-items: center; gap: 12px; border: 1px solid #e8e8e8; background: #fff; border-radius: 40px; padding: 8px 12px; }
.pill-btn { width: 36px; height: 36px; border-radius: 999px; border: 1px solid #e8e8e8; background: #fff; display: inline-flex; align-items: center; justify-content: center; }
.qty-val { min-width: 18px; text-align: center; font-weight: 600; font-size: 16px; }

.cta-block { display: grid; grid-template-columns: 1fr; gap: 10px; }
.add-to-bag { width: 100%; padding: 14px 18px; background: #111; color: #fff; border: 1px solid #111; border-radius: 8px; font-weight: 700; transition: background-color .15s ease, color .15s ease, border-color .15s ease, transform .15s ease; }
.add-to-bag:hover { background: #000; border-color: #000; transform: translateY(-1px); }
.wishlist { width: 100%; padding: 14px 18px; background: #fff; color: #111; border: 1px solid #111; border-radius: 8px; font-weight: 700; transition: background-color .15s ease, color .15s ease, border-color .15s ease, transform .15s ease; }
.wishlist:hover { background: #111; color: #fff; border-color: #111; transform: translateY(-1px); }

.product-meta { list-style: none; padding: 0; margin: 8px 0 0; display: grid; gap: 8px; }
.product-meta li { font-size: 12px; letter-spacing: .06em; color: #374151; }

.accordion { display: grid; gap: 12px; }
.accordion-item { border: 1px solid #e5e7eb; border-radius: 12px; background: #fff; overflow: hidden; }
.accordion-summary { list-style: none; display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 16px 18px; font-weight: 700; cursor: pointer; transition: background-color .2s ease; }
.accordion-summary:hover { background: #f9fafb; }
.accordion-item summary::-webkit-details-marker { display: none; }
.icons { display: inline-flex; gap: 8px; }
.accordion-item[open] .icon--plus { display: none; }
.accordion-item:not([open]) .icon--minus { display: none; }
.accordion-panel { padding: 0 18px; color: #4b5563; height: 0; overflow: hidden; transition: height .28s cubic-bezier(.2,.8,.2,1), opacity .28s ease; opacity: 0; }
.accordion-item[open] .accordion-panel { padding: 4px 18px 18px 18px; opacity: 1; }
.muted { color: #4b5563; line-height: 1.7; margin: 0; }
.specs { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px 16px; padding: 0; margin: 0; list-style: none; }
.specs .label { display: block; font-size: 12px; color: #6b7280; text-transform: uppercase; letter-spacing: .06em; }
.specs .value { font-weight: 600; color: #111; }
.reviews { display: flex; align-items: center; gap: 16px; }
.reviews .score { display: flex; align-items: baseline; gap: 6px; }
.reviews .value { font-size: 32px; font-weight: 800; }
.reviews .outof { color: #9ca3af; }

.review-form { display: grid; gap: 12px; margin-top: 8px; }
.form-row { display: grid; gap: 6px; }
.input, .textarea { width: 100%; border: 1px solid #e5e7eb; border-radius: 8px; padding: 10px 12px; font: inherit; }
.textarea { resize: vertical; }
.stars { display: inline-flex; gap: 4px; }
.star-btn { background: transparent; border: none; padding: 0; cursor: pointer; }
.star-btn.active svg path { fill: #f59e0b; }
.form-actions { display: flex; justify-content: flex-end; }
.btn-submit { background: #111; color: #fff; border: 1px solid #111; border-radius: 8px; padding: 10px 14px; font-weight: 700; }
.btn-submit:disabled { opacity: .5; cursor: not-allowed; }

.review-list { list-style: none; padding: 0; margin: 16px 0 0; display: grid; gap: 12px; }
.review-item { border-top: 1px solid #eee; padding-top: 12px; }
.review-head { display: flex; align-items: baseline; gap: 8px; justify-content: space-between; }
.review-name { font-weight: 700; }
.review-date { color: #9ca3af; font-size: 12px; }
.review-stars { color: #f59e0b; font-size: 14px; }
.review-text { margin: 6px 0 0; color: #374151; }

 

.recommendations { max-width: 1240px; margin: 48px auto; padding: 0 16px; position: relative; }
.recommendations-header { 
  display: flex; 
  align-items: center; 
  justify-content: space-between; 
  margin-bottom: 16px; 
  gap: 16px; 
  position: relative; 
  z-index: 0;
  pointer-events: none;
}
.recommendations-header h2 {
  pointer-events: auto;
}
.recommendations h2 { margin: 0; font-size: 18px; letter-spacing: .08em; }
.recommendations-controls { 
  display: flex; 
  gap: 8px; 
  pointer-events: auto;
  position: relative;
  z-index: 1;
}
.nav-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid #e5e7eb;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #111;
}
.nav-btn:hover {
  border-color: #111;
  background: #111;
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
.nav-btn:active {
  transform: translateY(0) scale(0.95);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

@media (max-width: 900px) {
  .pdp-hero { grid-template-columns: 1fr; align-items: start; }
  .purchase { position: static; max-height: none; overflow-y: visible; }
  .gallery { grid-template-columns: 1fr; }
  .thumbs { flex-direction: row; overflow-x: auto; padding-bottom: 6px; }
  .thumb { min-width: 80px; }
  .gallery .main { min-height: auto; }
  .gallery .main img { border-radius: 0; height: auto; aspect-ratio: 4 / 3; }
}

/* Image crossfade */
.pdp-fade-image-enter-active, .pdp-fade-image-leave-active { transition: opacity .25s ease, transform .25s ease; }
.pdp-fade-image-enter-from { opacity: 0; transform: scale(.985); }
.pdp-fade-image-enter-to { opacity: 1; transform: scale(1); }
.pdp-fade-image-leave-from { opacity: 1; transform: scale(1); position: absolute; inset: 0; }
.pdp-fade-image-leave-to { opacity: 0; transform: scale(1.015); }
</style>
