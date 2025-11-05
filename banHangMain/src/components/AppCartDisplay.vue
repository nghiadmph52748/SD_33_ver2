<template>
  <div>
    <section v-if="cartCount > 0" class="bag-list">
      <article v-for="item in cart" :key="item.id" class="bag-item">
        <div class="thumb">
          <img :src="resolveImage(item.img)" :alt="item.name" />
        </div>
        <div class="info">
          <header class="title-row">
            <h3 class="name">{{ item.name }}</h3>
            <div class="price">{{ formatCurrency(item.price) }}</div>
          </header>
          <div class="meta">
            <div class="line">{{ (item.gender || 'Unisex') + "'s Shoes" }}</div>
            <div class="line color">{{ item.description }}</div>
            <div v-if="item.size" class="line size">Size {{ item.size }}</div>
          </div>
          <div class="controls">
            <div class="pill">
              <button class="pill-btn" @click="remove(item)" aria-label="Remove">
                <svg viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M6 19a2 2 0 002 2h8a2 2 0 002-2V7H6v12zm3-9h2v8H9V10zm4 0h2v8h-2V10zM15.5 4l-1-1h-5l-1 1H5v2h14V4h-3.5z"/></svg>
              </button>
              <span class="divider"></span>
              <button class="pill-btn" @click="decrement(item)" aria-label="Decrease">−</button>
              <span class="qty-val">{{ item.quantity }}</span>
              <button class="pill-btn" @click="increment(item)" aria-label="Increase">+</button>
            </div>
            <button class="icon wish" aria-label="Save for later">
              <svg viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 6 4 4 6.5 4 8.04 4 9.54 4.81 10.35 6.09 11.16 4.81 12.66 4 14.2 4 16.7 4 18.7 6 18.7 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
            </button>
          </div>
        </div>
      </article>

      <div class="member-returns" role="note" aria-label="Member returns policy">
        <div class="mr-icon">
          <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
            <path fill="currentColor" d="M20 8h-3V4H4a2 2 0 00-2 2v11a3 3 0 003 3h12a3 3 0 003-3V10a2 2 0 00-2-2zm-5 0H4V6h11v2zm4 9a1 1 0 01-1 1H6a1 1 0 01-1-1v-5h14v5zM7 18a2 2 0 104 0H7z"/>
          </svg>
        </div>
        <div class="mr-text">
          <strong>Free returns</strong> for Members within 30 days. 
          <a href="#" class="mr-link">Learn more</a>
        </div>
      </div>
    </section>

    <a-empty v-else description="Your cart is empty, fill it up!">
      <button class="btn">
        <RouterLink to="/" style="color: inherit; text-decoration: none">Back Home</RouterLink>
      </button>
    </a-empty>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { storeToRefs } from "pinia";
import { useCartStore, type CartItem } from "@/stores/cart";
import { createOrderFromCart } from "@/api/orders";
import { formatCurrency } from "@/utils/currency";

const cartStore = useCartStore();
const { cart, cartTotal, cartCount } = storeToRefs(cartStore);
const isProcessing = ref(false);

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
  cartStore.removeOneFromCart(item);
};

const remove = (item: CartItem) => {
  cartStore.removeAllFromCart(item);
};

const startCheckout = async () => {
  isProcessing.value = true;
  cartStore.updateCartUI("loading");

  try {
    // Create order using backend API
    const order = await createOrderFromCart(
      cart.value,
      undefined, // No customer ID for guest checkout
      undefined, // Payment method ID (could be passed from UI)
      undefined, // Voucher ID (could be passed from UI)
      undefined // Notes
    );

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
</script>

<style scoped lang="scss">
.bag-list { display: grid; gap: 24px; }
.bag-item { display: grid; grid-template-columns: 300px 1fr; gap: 24px; padding-bottom: 16px; border-bottom: 1px solid #f0f0f0; }
.thumb { width: 300px; aspect-ratio: 1 / 1; background: #f7f7f7; border-radius: 12px; display: grid; place-items: center; overflow: hidden; }
.thumb img { width: 100%; height: 100%; object-fit: contain; }
.title-row { display: grid; grid-template-columns: 1fr auto; align-items: baseline; gap: 12px; }
.name { margin: 0; font-size: 26px; font-weight: 700; }
.price { font-weight: 700; }
.meta { color: #4e5969; display: grid; gap: 6px; margin: 8px 0 12px; font-size: 18px; }
.size { border-bottom: 4px solid #1d1d1d; width: fit-content; padding-bottom: 4px; }
.controls { display: inline-flex; align-items: center; gap: 12px; }
.icon { width: 56px; height: 56px; border-radius: 999px; border: 1px solid #e8e8e8; background: #fff; display: inline-flex; align-items: center; justify-content: center; }
.wish { width: 64px; height: 64px; }
.pill { display: inline-flex; align-items: center; gap: 12px; border: 1px solid #e8e8e8; background: #fff; border-radius: 40px; padding: 8px 12px; }
.pill-btn { width: 36px; height: 36px; border-radius: 999px; border: 1px solid #e8e8e8; background: #fff; display: inline-flex; align-items: center; justify-content: center; }
.divider { width: 1px; height: 24px; background: #e8e8e8; }
.qty-val { min-width: 18px; text-align: center; font-weight: 600; font-size: 16px; }
.member-returns { display: grid; grid-template-columns: 36px 1fr; align-items: center; gap: 12px; color: #4e5969; border: 1px solid #e8e8e8; border-radius: 14px; padding: 12px 14px; background: #fafafa; }
.mr-icon { width: 36px; height: 36px; border-radius: 999px; background: #ffffff; display: grid; place-items: center; border: 1px solid #e8e8e8; color: #1d2129; }
.mr-text { font-size: 14px; }
.mr-link { color: #111111; text-decoration: underline; }

@media (max-width: 720px) {
  .bag-item { grid-template-columns: 1fr; }
  .thumb { width: 100%; }
}
</style>
