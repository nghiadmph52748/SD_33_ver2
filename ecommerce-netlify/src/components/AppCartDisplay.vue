<template>
  <div>
    <section v-if="cartCount > 0">
      <table>
        <tr>
          <th>Product</th>
          <th>Price</th>
          <th>Quantity</th>
          <th>Total</th>
          <th />
        </tr>
        <tr v-for="item in cart" :key="item.id">
          <td>
            <img :src="`/products/${item.img}`" :alt="item.name" class="product-img" />
            <h3 class="product-name">{{ item.name }}</h3>
            <h5 v-if="item.size" class="product-size">Size: {{ item.size }}</h5>
          </td>
          <td>
            <h4 class="price">{{ formatCurrency(item.price) }}</h4>
          </td>
          <td>
            <a-space>
              <a-button size="small" @click="decrement(item)">-</a-button>
              <strong>{{ item.quantity }}</strong>
              <a-button size="small" @click="increment(item)">+</a-button>
            </a-space>
          </td>
          <td>{{ formatCurrency(item.quantity * item.price) }}</td>
          <td>
            <a-button type="text" status="danger" @click="remove(item)">x</a-button>
          </td>
        </tr>
      </table>

      <section class="payment">
        <div class="payment-summary">
          <h3>Order Summary</h3>
          <p>Shipping is on us for every GearUp order.</p>
          <a-button type="primary" size="large" @click="startCheckout" :loading="isProcessing">
            {{ isProcessing ? "Processing..." : "Proceed to Checkout" }}
          </a-button>
          <small class="checkout-note"
            >Checkout experience will integrate with GearUp payments in a later phase.</small
          >
        </div>
        <div class="total">
          <div class="caption">
            <p>
              <strong>Subtotal:</strong>
            </p>
            <p>Shipping:</p>
            <p class="golden">Total:</p>
          </div>
          <div class="num">
            <p>
              <strong>{{ formatCurrency(cartTotal) }}</strong>
            </p>
            <p>Free Shipping</p>
            <p class="golden">{{ formatCurrency(cartTotal) }}</p>
          </div>
        </div>
      </section>
    </section>

    <a-empty v-else description="Your cart is empty, fill it up!">
      <a-button type="primary">
        <RouterLink to="/" style="color: inherit; text-decoration: none">Back Home</RouterLink>
      </a-button>
    </a-empty>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { storeToRefs } from "pinia";
import { Message } from "@arco-design/web-vue";
import { useCartStore, type CartItem } from "@/stores/cart";
import { createOrderFromCart } from "@/api/orders";
import { formatCurrency } from "@/utils/currency";

const cartStore = useCartStore();
const { cart, cartTotal, cartCount } = storeToRefs(cartStore);
const isProcessing = ref(false);

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
      Message.success("Order created successfully!");
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
    Message.error(error.message || "Failed to process checkout");
    cartStore.updateCartUI("failure");
    isProcessing.value = false;
  }
};
</script>

<style scoped lang="scss">
.product-img {
  float: left;
  margin-right: 15px;
  width: 100px;
}

.total {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-column-gap: 100px;
}

table {
  width: 100%;
  margin-top: 20px;
}

tr {
  text-align: center;
}

th {
  padding: 10px 0;
}

td,
th {
  border-bottom: 1px solid #ccc;
}

.golden {
  background: #f2eee2;
  font-weight: bold;
  padding: 10px;
}

.product-name,
.product-size {
  text-align: left;
}

.product-name {
  padding-top: 36px;
}

.product-size {
  font-weight: 100;
}

.num {
  text-align: right;
}

button a {
  color: white;
  transition: 0.3s all ease;
}

.delete-product,
.quantity-adjust:first-of-type,
.quantity-adjust:last-of-type {
  padding: 5px 7px;
  border: none;
}

.quantity-adjust:first-of-type {
  margin-right: 5px;
}

.quantity-adjust:last-of-type {
  margin-left: 5px;
}

.delete-product:hover {
  background-color: rgb(255, 85, 85);
  border-radius: 100%;
  border: none;
}

.delete-product:focus,
.delete-product:active {
  outline: none;
}

.payment {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-column-gap: 100px;
}

.payment-summary {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.checkout-note {
  display: block;
  color: #777;
}

@media (max-width: 700px) {
  .payment {
    width: 94%;
    margin-left: 2%;
    grid-template-columns: 1fr;
    grid-row-gap: 24px;
  }

  .total {
    width: 100%;
  }
}
</style>
