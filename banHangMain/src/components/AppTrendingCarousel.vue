<template>
    <section>
        <div class="heading-row">
            <h2>
                <span>{{ t("home.sections.trending") }}</span>
            </h2>
            <div v-if="trendingProducts.length > 0" class="nav-group" :class="{ 'nav-hidden': loading }">
                <button @click="scrollByAmount(-1)" :aria-label="t('buttons.previous')" :disabled="loading">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="m15 18-6-6 6-6" />
                    </svg>
                </button>
                <button @click="scrollByAmount(1)" :aria-label="t('buttons.next')" :disabled="loading">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="m9 18 6-6-6-6" />
                    </svg>
                </button>
            </div>
        </div>
        <!-- Loading state: show only when actively loading -->
        <div v-if="loading" class="loading-container">
            <div class="loading-spinner"></div>
        </div>
        <!-- Content: show when not loading -->
        <div v-else ref="scroller" class="scroller" role="list" tabindex="0" aria-label="Trending products"
            @keydown="onKeydown">
            <article v-for="p in trendingProducts" :key="p.id" class="card" role="listitem">
                <RouterLink :to="`/product/${p.id}`" class="card-link">
                    <div class="img-wrap">
                        <img :src="resolveImage(p.img)" :alt="p.name" loading="lazy" v-img-fallback />
                    </div>
                    <div class="info">
                        <h3>{{ p.name }}</h3>
                        <div class="price-group">
                            <p class="price-current">{{ formatCurrency(p.price) }}</p>
                            <p v-if="p.originalPrice && p.originalPrice > p.price" class="price-original">
                                {{ formatCurrency(p.originalPrice) }}
                            </p>
                        </div>
                    </div>
                </RouterLink>
            </article>
        </div>
    </section>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { RouterLink } from "vue-router";
import { useI18n } from "vue-i18n";
import { useCartStore } from "@/stores/cart";
import { storeToRefs } from "pinia";
import { formatCurrency } from "@/utils/currency";

const cartStore = useCartStore();
const { t } = useI18n();
// Use trendingProducts getter from store
const { trendingProducts, loading, products } = storeToRefs(cartStore);

const scroller = ref<HTMLDivElement | null>(null);

function scrollByAmount(direction: number) {
    const el = scroller.value;
    if (!el) return;
    const cardWidth = 280;
    const gap = 20;
    const scrollAmount = (cardWidth + gap) * direction;
    el.scrollBy({ left: scrollAmount, behavior: "smooth" });
}

function onKeydown(e: KeyboardEvent) {
    if (e.key === "ArrowLeft") {
        e.preventDefault();
        scrollByAmount(-1);
    } else if (e.key === "ArrowRight") {
        e.preventDefault();
        scrollByAmount(1);
    }
}
function resolveImage(imgPath: string): string {
    if (!imgPath) return "/shoe1.jpg";
    if (imgPath.startsWith("http") || imgPath.startsWith("/")) return imgPath;
    if (
        imgPath.endsWith(".png") ||
        imgPath.endsWith(".jpg") ||
        imgPath.endsWith(".jpeg")
    ) {
        return `/products/${imgPath}`;
    }
    return imgPath;
}
</script>

<style scoped lang="scss">
section {
    padding: 48px 0;
}

.heading-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}

h2 {
    margin: 0;
    font-size: 28px;
    font-weight: 700;
    color: #111;
    position: relative;
    display: inline-block;

    span {
        position: relative;
        z-index: 1;
    }
}

.nav-group {
    display: flex;
    gap: 8px;
    transition: opacity 0.2s ease;

    &.nav-hidden {
        opacity: 0.3;
        pointer-events: none;
    }

    button {
        background: #fff;
        border: 1px solid #e5e5e5;
        border-radius: 50%;
        width: 40px;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.2s ease;

        &:hover:not(:disabled) {
            background: #111;
            border-color: #111;
            color: #fff;
        }

        &:disabled {
            opacity: 0.4;
            cursor: not-allowed;
        }

        svg {
            width: 20px;
            height: 20px;
        }
    }
}

.loading-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 320px;
}

.loading-spinner {
    width: 40px;
    height: 40px;
    border: 3px solid #f3f4f6;
    border-top-color: #111;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
}

@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}

.scroller {
    display: flex;
    gap: 20px;
    overflow-x: auto;
    scroll-behavior: smooth;
    padding-bottom: 16px;
    scrollbar-width: none;
    /* Firefox */
    -ms-overflow-style: none;
    /* IE and Edge */

    &::-webkit-scrollbar {
        display: none;
        /* Chrome, Safari, Opera */
    }
}

.card {
    flex: 0 0 auto;
    width: 280px;
    background: #fff;
    border: 1px solid #e5e5e5;
    border-radius: 12px;
    overflow: hidden;
    transition: all 0.3s ease;

    &:hover {
        transform: translateY(-4px);
        box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
        border-color: #111;
    }
}

.card-link {
    text-decoration: none;
    color: inherit;
    display: block;
}

.img-wrap {
    position: relative;
    width: 100%;
    padding-top: 100%;
    overflow: hidden;
    background: #f9f9f9;

    img {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.3s ease;
    }
}

.card:hover .img-wrap img {
    transform: scale(1.05);
}

.info {
    padding: 16px;
}

h3 {
    margin: 0 0 8px 0;
    font-size: 16px;
    font-weight: 600;
    color: #111;
    line-height: 1.4;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
}


.price-group {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
}

.price-current {
    margin: 0;
    font-size: 18px;
    font-weight: 700;
    color: #111;
}

.price-original {
    margin: 0;
    font-size: 14px;
    font-weight: 500;
    color: #999;
    text-decoration: line-through;
}


@media (max-width: 768px) {
    .card {
        width: 240px;
    }

    h2 {
        font-size: 24px;
    }
}
</style>
