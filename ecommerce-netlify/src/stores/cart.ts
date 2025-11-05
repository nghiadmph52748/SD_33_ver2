import axios from "axios";
import { defineStore } from "pinia";
import { getSneakerProducts, getAllProducts, type Product } from "@/api/products";

// Re-export Product for components that import from store
export type { Product } from "@/api/products";

type CartStatus = "idle" | "loading" | "success" | "failure";

export interface CartItem extends Product {
  quantity: number;
  size?: string;
}

export interface CartPayload {
  id: string;
  quantity: number;
}

export const useCartStore = defineStore("cart", {
  state: () => ({
    cartUIStatus: "idle" as CartStatus,
    products: [] as Product[],
    cart: [] as CartItem[],
    clientSecret: "",
    loading: false,
    error: null as string | null
  }),
  getters: {
    featuredProducts(state): Product[] {
      return state.products.slice(0, 3);
    },
    womenProducts(state): Product[] {
      return state.products.filter(product => product.gender === "Female");
    },
    menProducts(state): Product[] {
      return state.products.filter(product => product.gender === "Male");
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

      return state.cart.reduce(
        (total, next) => total + next.quantity * next.price,
        0
      );
    },
    cartItems(state): CartPayload[] {
      if (!state.cart.length) {
        return [];
      }

      return state.cart.map(item => ({
        id: item.id,
        quantity: item.quantity
      }));
    },
    clientSecret(state): string {
      return state.clientSecret;
    }
  },
  actions: {
    async fetchProducts(sneakersOnly: boolean = true) {
      this.loading = true;
      this.error = null;
      try {
        if (sneakersOnly) {
          this.products = await getSneakerProducts();
        } else {
          this.products = await getAllProducts();
        }
      } catch (error: any) {
        this.error = error.message || "Failed to fetch products";
        console.error("Error fetching products:", error);
        // Fallback to empty array on error
        this.products = [];
      } finally {
        this.loading = false;
      }
    },
    updateCartUI(status: CartStatus) {
      this.cartUIStatus = status;
    },
    clearCart() {
      this.cart = [];
      this.cartUIStatus = "idle";
      this.clientSecret = "";
    },
    addToCart(payload: CartItem) {
      const existing = this.cart.find(item => item.id === payload.id);

      if (existing) {
        existing.quantity += payload.quantity;
        return;
      }

      this.cart.push(payload);
    },
    addOneToCart(payload: CartItem) {
      const existing = this.cart.find(item => item.id === payload.id);

      if (existing) {
        existing.quantity += 1;
        return;
      }

      this.cart.push({ ...payload, quantity: 1 });
    },
    removeOneFromCart(payload: CartItem) {
      const index = this.cart.findIndex(item => item.id === payload.id);

      if (index === -1) {
        return;
      }

      const item = this.cart[index];

      if (item.quantity > 1) {
        item.quantity -= 1;
        return;
      }

      this.cart.splice(index, 1);
    },
    removeAllFromCart(payload: CartItem) {
      this.cart = this.cart.filter(item => item.id !== payload.id);
    },
    setClientSecret(clientSecret: string) {
      this.clientSecret = clientSecret;
    },
    async createPaymentIntent() {
      try {
        const result = await axios.post(
          "https://ecommerce-netlify.netlify.app/.netlify/functions/create-payment-intent",
          {
            items: this.cartItems
          },
          {
            headers: {
              "Content-Type": "application/json"
            }
          }
        );

        if (result.data.clientSecret) {
          this.setClientSecret(result.data.clientSecret);
        }
      } catch (error) {
        console.error("Failed to create payment intent", error);
      }
    }
  }
});
