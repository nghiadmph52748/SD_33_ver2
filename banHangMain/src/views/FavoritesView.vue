<template>
  <section class="favorites">
    <h1>Yêu thích</h1>
    <p class="hint">Wishlist sắp ra mắt. Tạm thời, xem các sản phẩm nổi bật bên dưới.</p>
    <AppFeaturedProducts />
    <div class="cta">
      <RouterLink to="/all"><button class="btn">Tiếp tục mua sắm</button></RouterLink>
    </div>
  </section>
</template>

<script setup lang="ts">
import AppFeaturedProducts from "@/components/AppFeaturedProducts.vue";
</script>

<style scoped lang="scss">
.favorites {
  padding: 24px 16px;
  max-width: 1180px;
  margin: 0 auto;
  display: grid;
  gap: 16px;
}

.hint { color: #4e5969; text-align: center; }
.cta { text-align: center; }

h1 { margin: 0 0 16px 0; }
</style>

