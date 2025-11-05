<template>
  <footer>
    <section class="callout">
      <h2>"Elevate your sneaker game, every season."</h2>
      <p>GearUp</p>
    </section>
    <AppFooterLinks />
    <div class="footer-meta">
      <p>
        {{ $t('footer.poweredBy') }}. Admin dashboard available at
        <a :href="adminUrl" target="_blank" rel="noopener">{{ $t('footer.adminLink') }}</a>.
      </p>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { computed } from "vue";
import AppFooterLinks from "@/components/AppFooterLinks.vue";

const adminUrl = computed(() => import.meta.env.VITE_ADMIN_URL ?? "/");
</script>

<style scoped lang="scss">
.callout {
  width: 100%;
  height: 280px;
  background: url("/callout.jpg") center center no-repeat;
  background-size: cover;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  color: white;

  h2 {
    padding: 0 30px;
    text-align: center;
  }
}

.footer-meta {
  background: black;
  width: 100%;
  padding: 10px 30px;
  color: white;
  text-align: center;
}

a,
a:visited,
a:active {
  color: #91caff;
  font-weight: bold;
}
</style>
