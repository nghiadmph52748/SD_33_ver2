<template>
  <div>
    <section v-if="cartCount > 0" class="bag-list">
      <article v-for="item in cart" :key="item.id" class="bag-item">
        <div class="thumb">
          <img :src="resolveImage(item.img)" :alt="item.name" loading="lazy" v-img-fallback />
        </div>
        <div class="info">
          <header class="title-row">
            <h3 class="name">{{ item.name }}</h3>
            <div class="price">
              <span v-if="item.originalPrice && item.originalPrice > item.price" class="price-original">{{
                formatCurrency(item.originalPrice) }}</span>
              <span class="price-current">{{
                formatCurrency(item.price)
                }}</span>
            </div>
          </header>
          <div class="meta">
            <div class="line" v-if="item.gender">
              <span class="line__label">{{ t("cart.itemGender") }}:&nbsp;</span>
              <span>{{ formatGender(item.gender) }}</span>
            </div>
            <div class="line">
              <span class="line__label">{{ t("cart.itemColor") }}:&nbsp;</span>
              <span>{{ item.color || t("cart.valueUnknown") }}</span>
            </div>
            <div class="line" v-if="item.size">
              <span class="line__label">Size:&nbsp;</span>
              <span>{{ item.size }}</span>
            </div>
          </div>
          <div class="controls">
            <div class="pill">
              <button class="pill-btn" @click="remove(item)" :aria-label="t('cart.removeItem')">
                <svg viewBox="0 0 24 24" width="16" height="16">
                  <path fill="currentColor"
                    d="M6 19a2 2 0 002 2h8a2 2 0 002-2V7H6v12zm3-9h2v8H9V10zm4 0h2v8h-2V10zM15.5 4l-1-1h-5l-1 1H5v2h14V4h-3.5z" />
                </svg>
              </button>
              <span class="divider"></span>
              <button class="pill-btn" @click="decrement(item)" :aria-label="t('cart.decreaseQuantity')">
                −
              </button>
              <span class="qty-val">{{ item.quantity }}</span>
              <button class="pill-btn" @click="increment(item)" :aria-label="t('cart.increaseQuantity')">
                +
              </button>
            </div>
            <button class="icon wish" @click="saveForLater(item)" :aria-label="t('cart.saveForLater')">
              <svg viewBox="0 0 24 24" width="16" height="16">
                <path fill="currentColor"
                  d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 6 4 4 6.5 4 8.04 4 9.54 4.81 10.35 6.09 11.16 4.81 12.66 4 14.2 4 16.7 4 18.7 6 18.7 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" />
              </svg>
            </button>
          </div>
        </div>
      </article>

      <div class="member-returns" role="note" :aria-label="$t('cart.memberReturnsAria')">
        <div class="mr-icon">
          <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
            <path fill="currentColor"
              d="M20 8h-3V4H4a2 2 0 00-2 2v11a3 3 0 003 3h12a3 3 0 003-3V10a2 2 0 00-2-2zm-5 0H4V6h11v2zm4 9a1 1 0 01-1 1H6a1 1 0 01-1-1v-5h14v5zM7 18a2 2 0 104 0H7z" />
          </svg>
        </div>
        <div class="mr-text">
          {{ $t("cart.memberReturns") }}
          <a href="#" class="mr-link">{{ $t("cart.learnMore") }}</a>
        </div>
      </div>
    </section>

    <a-empty v-else description="Your cart is empty, fill it up!">
      <button class="btn">
        <RouterLink to="/" style="color: inherit; text-decoration: none">Back Home</RouterLink>
      </button>
    </a-empty>

    <AppConfirmModal :is-open="isConfirmOpen" :title="t('cart.removeConfirmTitle')"
      :message="t('cart.removeConfirmMessage')" :ok-text="t('cart.removeConfirmOk')"
      :cancel-text="t('cart.removeConfirmCancel')" @close="isConfirmOpen = false" @confirm="onConfirmDelete" />
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { storeToRefs } from "pinia";
import AppConfirmModal from "./AppConfirmModal.vue";
import { useCartStore, type CartItem } from "@/stores/cart";
import { createOrderFromCart } from "@/api/orders";
import { formatCurrency } from "@/utils/currency";
import { useI18n } from "vue-i18n";
import { useUserStore } from "@/stores/user";

const cartStore = useCartStore();
const { cart, cartTotal, cartCount } = storeToRefs(cartStore);
const isProcessing = ref(false);
const { t } = useI18n();
const userStore = useUserStore();

const isConfirmOpen = ref(false);
const itemToRemove = ref<CartItem | null>(null);

function resolveImage(imgPath: string | undefined | null): string {
  if (!imgPath) return "/products/1.jpg";
  const p = String(imgPath).trim();
  if (p.startsWith("http") || p.startsWith("/")) return p;
  // if it's a plain filename or relative path, assume public /products
  return `/products/${p}`;
}

const increment = (item: CartItem) => {
  cartStore.addOneToCart(item);
};

const decrement = (item: CartItem) => {
  if (item.quantity > 1) {
    cartStore.removeOneFromCart(item);
  } else {
    remove(item);
  }
};

const remove = (item: CartItem) => {
  itemToRemove.value = item;
  isConfirmOpen.value = true;
};

const onConfirmDelete = () => {
  if (itemToRemove.value) {
    cartStore.removeAllFromCart(itemToRemove.value);
    itemToRemove.value = null;
  }
  isConfirmOpen.value = false;
};

const saveForLater = (item: CartItem) => {
  cartStore.addToFavorites(item);
  // cartStore.removeAllFromCart(item); // User requested to keep item in cart
};

const startCheckout = async () => {
  isProcessing.value = true;
  cartStore.updateCartUI("loading");

  try {
    // Create order using backend API
    const order = await createOrderFromCart(cart.value, {
      customerId: userStore.id || undefined,
      shippingFee: 0,
      subtotal: cartTotal.value,
      totalAmount: cartTotal.value,
      recipient: {
        fullName: userStore.profile?.tenKhachHang || "Khách lẻ",
        phone: userStore.profile?.soDienThoai || "0000000000",
        email: userStore.profile?.email || "guest@example.com",
        address: userStore.profile?.diaChi || "Chưa cập nhật",
      },
    });

    if (order) {
      cartStore.updateCartUI("success");
      setTimeout(() => {
        cartStore.clearCart();
        isProcessing.value = false;
      }, 2000);
    } else {
      throw new Error("Failed to create order");
    }
  } catch (error: any) {
    console.error("Checkout failed", error);
    cartStore.updateCartUI("failure");
    isProcessing.value = false;
  }
};

const formatGender = (gender: string | undefined | null) => {
  if (!gender) return t("cart.valueUnknown");
  const normalized = String(gender).toLowerCase();
  if (normalized.includes("male")) return t("cart.genderMenShoes");
  if (normalized.includes("female")) return t("cart.genderWomenShoes");
  return gender;
};
</script>

<style scoped lang="scss">
.bag-list {
  display: grid;
  gap: 24px;
}

.bag-item {
  display: grid;
  grid-template-columns: 240px 1fr;
  /* Increased image size */
  gap: 32px;
  padding: 32px 0;
  border-bottom: 1px solid #f0f0f0;
  align-items: start;
}

.thumb {
  width: 240px;
  aspect-ratio: 1;
  background: #f7f7f7;
  /* Very light cool gray */
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.thumb:hover img {
  transform: scale(1.06);
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 24px;
}

.name {
  margin: 0;
  font-size: 22px;
  /* Slightly larger, premium size */
  font-weight: 600;
  line-height: 1.3;
  color: #111;
  letter-spacing: -0.01em;
}

.price {
  text-align: right;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.price-current {
  font-size: 20px;
  font-weight: 600;
  color: #111;
}

.price-original {
  font-size: 15px;
  color: #999;
  text-decoration: line-through;
}

.meta {
  margin-bottom: 24px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 16px;
  color: #666;
}

.line {
  display: flex;
  align-items: baseline;
}

.line__label {
  color: #666;
}

.controls {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: auto;
}

/* Premium Pill Style for Quantity */
.pill {
  display: inline-flex;
  align-items: center;
  border: 1px solid #e0e0e0;
  border-radius: 99px;
  padding: 4px;
  height: 44px;
  background: #fff;
}

.pill-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: transparent;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #111;
  transition: background 0.2s;
}

.pill-btn:hover {
  background: #f5f5f5;
}

.qty-val {
  min-width: 44px;
  text-align: center;
  font-weight: 600;
  font-size: 16px;
}

/* Premium Icon Button */
.icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 1px solid #e0e0e0;
  background: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #111;
  transition: all 0.2s;
}

.icon:hover {
  border-color: #111;
  background: #111;
  color: #fff;
}

.wish {
  width: 44px;
  height: 44px;
}

.divider {
  display: none;
}

@media (max-width: 768px) {
  .bag-item {
    grid-template-columns: 140px 1fr;
    gap: 20px;
  }

  .thumb {
    width: 140px;
  }

  .name {
    font-size: 18px;
  }

  .price-current {
    font-size: 18px;
  }
}

.mr-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f9f9f9;
  display: grid;
  place-items: center;
  color: #111;
}

.mr-text {
  font-size: 14px;
  line-height: 1.5;
}

.mr-link {
  color: #111;
  text-decoration: underline;
  font-weight: 500;
  margin-left: 4px;
}

@media (max-width: 768px) {
  .bag-item {
    grid-template-columns: 120px 1fr;
    gap: 16px;
  }

  .thumb {
    width: 120px;
  }

  .name {
    font-size: 16px;
  }

  .price-current {
    font-size: 16px;
  }

  .controls {
    flex-wrap: wrap;
  }
}
</style>
