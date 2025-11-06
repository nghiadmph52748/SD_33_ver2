import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";

const routes: RouteRecordRaw[] = [
  {
    path: "/",
    name: "home",
    component: () => import("@/views/HomeView.vue")
  },
  {
    path: "/favorites",
    name: "favorites",
    component: () => import("@/views/FavoritesView.vue")
  },
  {
    path: "/all",
    name: "all-products",
    component: () => import("@/views/AllProductsView.vue")
  },
  {
    path: "/women",
    name: "women-products",
    component: () => import("@/views/WomenProductsView.vue")
  },
  {
    path: "/men",
    name: "men-products",
    component: () => import("@/views/MenProductsView.vue")
  },
  {
    path: "/cart",
    name: "cart",
    component: () => import("@/views/CartView.vue")
  },
  {
    path: "/product/:id",
    name: "product-detail",
    component: () => import("@/views/ProductDetailView.vue"),
    props: true
  },
  {
    path: "/login",
    name: "login",
    component: () => import("@/views/LoginView.vue")
  },
  {
    path: "/:pathMatch(.*)*",
    name: "not-found",
    component: () => import("@/views/NotFoundView.vue")
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  }
});

export default router;
