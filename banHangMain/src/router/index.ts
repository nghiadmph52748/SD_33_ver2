import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";

const routes: RouteRecordRaw[] = [
  {
    path: "/",
    name: "home",
    component: () => import("@/views/HomeView.vue"),
  },
  {
    path: "/favorites",
    name: "favorites",
    component: () => import("@/views/FavoritesView.vue"),
  },
  {
    path: "/all",
    name: "all-products",
    component: () => import("@/views/AllProductsView.vue"),
  },
  {
    path: "/women",
    name: "women-products",
    component: () => import("@/views/WomenProductsView.vue"),
  },
  {
    path: "/men",
    name: "men-products",
    component: () => import("@/views/MenProductsView.vue"),
  },
  {
    path: "/cart",
    name: "cart",
    component: () => import("@/views/CartView.vue"),
  },
  {
    path: "/checkout",
    name: "checkout",
    component: () => import("@/views/CheckoutView.vue"),
  },
  {
    path: "/payment/cod/result",
    name: "payment-cod-result",
    component: () => import("@/views/PaymentCodResultView.vue"),
  },
  {
    path: "/payment/vnpay/result",
    name: "payment-vnpay-result",
    component: () => import("@/views/PaymentVnpayResultView.vue"),
  },
  {
    path: "/payment/momo/result",
    name: "payment-momo-result",
    component: () => import("@/views/PaymentMoMoResultView.vue"),
  },
  {
    path: "/payment/result",
    name: "payment-result",
    component: () => import("@/views/PaymentVnpayResultView.vue"),
  },
  {
    path: "/product/:id",
    name: "product-detail",
    component: () => import("@/views/ProductDetailView.vue"),
    props: true,
  },
  {
    path: "/tra-cuu-hoa-don",
    name: "order-lookup",
    alias: ["/order-lookup"],
    component: () => import("@/views/OrderLookupView.vue"),
  },
  {
    path: "/order/thank-you",
    name: "order-thank-you",
    component: () => import("@/views/OrderDeliveryThankYouView.vue"),
  },
  {
    path: "/login",
    name: "login",
    component: () => import("@/views/LoginView.vue"),
  },
  {
    path: "/profile",
    name: "profile",
    component: () => import("@/views/ProfileView.vue"),
  },
  {
    path: "/:pathMatch(.*)*",
    name: "not-found",
    component: () => import("@/views/NotFoundView.vue"),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  },
});

// Auth guard for routes with meta.requiresAuth
router.beforeEach(async (to, _from, next) => {
  if (to.meta?.requiresAuth) {
  const { useUserStore } = await import("@/stores/user");
  const userStore = useUserStore();
  await userStore.initFromStorage();
    if (!userStore.isAuthenticated) {
      return next({ path: "/login", query: { redirect: to.fullPath } });
    }
  }
  
  if (to.name === "checkout") {
    const { useCartStore } = await import("@/stores/cart");
    const cartStore = useCartStore();
    if (cartStore.cartCount === 0) {
      return next({ path: "/cart" });
    }
  }
  
  next();
});

export default router;
