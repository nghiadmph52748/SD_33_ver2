import { defineStore } from "pinia";
import {
  getSneakerProducts,
  getAllProducts,
  getBestSellingProducts,
  type Product,
} from "@/api/products";
import { useUserStore } from "./user";

// Re-export Product for components that import from store
export type { Product } from "@/api/products";

type CartStatus = "idle" | "loading" | "success" | "failure";

export interface CartItem extends Product {
  quantity: number;
  size?: string;
  idBienThe?: number; // Variant ID for backend
}

export interface CartPayload {
  id: string;
  quantity: number;
}

const CART_STORAGE_PREFIX = "gearup-cart-";
const VALID_CART_STATUSES: CartStatus[] = [
  "idle",
  "loading",
  "success",
  "failure",
];

type PersistableCartState = {
  cart: CartItem[];
  cartUIStatus: CartStatus;
};

function getCartStorageKey(userId?: number | null): string {
  if (userId) {
    return `${CART_STORAGE_PREFIX}user-${userId}`;
  }
  return `${CART_STORAGE_PREFIX}guest`;
}

function loadPersistedCartState(
  userId?: number | null
): Partial<PersistableCartState> | null {
  if (typeof window === "undefined") return null;
  try {
    const key = getCartStorageKey(userId);
    const cached = window.localStorage.getItem(key);
    if (!cached) return null;
    const parsed = JSON.parse(cached);
    const status =
      typeof parsed.cartUIStatus === "string" &&
      VALID_CART_STATUSES.includes(parsed.cartUIStatus)
        ? (parsed.cartUIStatus as CartStatus)
        : "idle";
    return {
      cart: Array.isArray(parsed.cart) ? parsed.cart : [],
      cartUIStatus: status,
    };
  } catch (error) {
    console.warn("Failed to hydrate cart cache", error);
    return null;
  }
}

function persistCartStateSnapshot(
  state: PersistableCartState,
  userId?: number | null
) {
  if (typeof window === "undefined") return;
  try {
    const key = getCartStorageKey(userId);
    window.localStorage.setItem(key, JSON.stringify(state));
  } catch (error) {
    console.warn("Failed to persist cart cache", error);
  }
}

export const useCartStore = defineStore("cart", {
  state: () => {
    // Get user ID from user store if available
    const userStore = useUserStore();
    const userId = userStore.id;
    const persisted = loadPersistedCartState(userId);
    return {
      cartUIStatus: persisted?.cartUIStatus ?? "idle",
      products: [] as Product[],
      bestSellingProducts: [] as Product[],
      cart: persisted?.cart ?? [],
      loading: false,
      error: null as string | null,
      sizeMapping: {} as Record<string, string[]>,
    };
  },
  getters: {
    featuredProducts(state): Product[] {
      // Return best-selling products if available, otherwise fallback to first 3 products with images
      if (state.bestSellingProducts && state.bestSellingProducts.length > 0) {
        return state.bestSellingProducts;
      }

      const hasImage = (product: Product) =>
        typeof product.img === "string" && product.img.trim() !== "";

      // Fallback: first three products with images
      return state.products.filter(hasImage).slice(0, 3);
    },
    womenProducts(state): Product[] {
      return state.products.filter((product) => product.gender === "Female");
    },
    menProducts(state): Product[] {
      return state.products.filter((product) => product.gender === "Male");
    },
    cartCount(state): number {
      if (!state.cart.length) {
        return 0;
      }

      return state.cart.reduce((total, next) => total + next.quantity, 0);
    },
    cartTotal(state): number {
      if (!state.cart.length) {
        return 0;
      }

      // Use integer math to avoid floating-point precision issues
      // Convert to smallest unit (e.g., cents), calculate, then convert back
      const total = state.cart.reduce((total, next) => {
        // Ensure price is treated as integer (VND doesn't have decimals)
        const priceInt = Math.round(next.price || 0);
        return total + next.quantity * priceInt;
      }, 0);

      return total;
    },
    cartItems(state): CartPayload[] {
      if (!state.cart.length) {
        return [];
      }

      return state.cart.map((item) => ({
        id: item.id,
        quantity: item.quantity,
      }));
    },
  },
  actions: {
    async fetchProducts(sneakersOnly: boolean = true, forceRefresh: boolean = false) {
      // Skip fetch if products already loaded and not forcing refresh
      if (!forceRefresh && this.products.length > 0) {
        return;
      }
      
      this.loading = true;
      this.error = null;
      try {
        if (sneakersOnly) {
          this.products = await getSneakerProducts();
        } else {
          this.products = await getAllProducts();
        }
        // Fetch best-selling products for featured section
        this.bestSellingProducts = await getBestSellingProducts(3);
      } catch (error: any) {
        this.error = error.message || "Failed to fetch products";
        console.error("Error fetching products:", error);
        // Fallback to empty array on error
        this.products = [];
        this.bestSellingProducts = [];
      } finally {
        this.loading = false;
      }
    },
    async refreshProducts(sneakersOnly: boolean = true) {
      await this.fetchProducts(sneakersOnly, true);
    },
    updateCartUI(status: CartStatus) {
      this.cartUIStatus = status;
      this.persistState();
    },
    persistState() {
      const userStore = useUserStore();
      persistCartStateSnapshot(
        {
          cart: this.cart,
          cartUIStatus: this.cartUIStatus,
        },
        userStore.id
      );
    },
    loadCartForUser(userId?: number | null) {
      const persisted = loadPersistedCartState(userId);
      if (persisted) {
        this.cart = persisted.cart ?? [];
        this.cartUIStatus = persisted.cartUIStatus ?? "idle";
      } else {
        this.cart = [];
        this.cartUIStatus = "idle";
      }
      this.sizeMapping = {};
    },
    clearCart() {
      this.cart = [];
      this.cartUIStatus = "idle";
      this.sizeMapping = {};
      this.persistState();
    },
    addToCart(payload: CartItem) {
      const existing = this.cart.find(
        (item) =>
          item.id === payload.id &&
          item.size === payload.size &&
          item.color === payload.color
      );

      if (existing) {
        existing.quantity += payload.quantity;
        this.persistState();
        return;
      }

      this.cart.push(payload);
      this.persistState();
    },
    addOneToCart(payload: CartItem) {
      const existing = this.cart.find(
        (item) =>
          item.id === payload.id &&
          item.size === payload.size &&
          item.color === payload.color
      );

      if (existing) {
        existing.quantity += 1;
        this.persistState();
        return;
      }

      this.cart.push({ ...payload, quantity: 1 });
      this.persistState();
    },
    removeOneFromCart(payload: CartItem) {
      const index = this.cart.findIndex((item) => item.id === payload.id);

      if (index === -1) {
        return;
      }

      const item = this.cart[index];

      if (item.quantity > 1) {
        item.quantity -= 1;
        this.persistState();
        return;
      }

      this.cart.splice(index, 1);
      this.persistState();
    },
    removeAllFromCart(payload: CartItem) {
      this.cart = this.cart.filter((item) => item.id !== payload.id);
      this.persistState();
    },
  },
});
