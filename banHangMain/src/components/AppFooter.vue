<template>
  <footer>
    <div class="footer-links">
    <AppFooterLinks />
    </div>
    <div class="footer-meta">
      <div class="meta-inner">
        <span>{{ t('footer.copyright', { year: new Date().getFullYear() }) }}</span>
        <nav :aria-label="t('footer.legal.aria')">
          <a href="#">{{ t('footer.legal.guides') }}</a>
          <a href="#">{{ t('footer.legal.termsOfSale') }}</a>
          <a href="#">{{ t('footer.legal.termsOfUse') }}</a>
          <a href="#">{{ t('footer.legal.privacyPolicy') }}</a>
        </nav>
        <div class="locale">
          <svg width="16" height="16" viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M12 2a10 10 0 100 20 10 10 0 000-20zm7.93 9H16.9a15.2 15.2 0 00-.8-4.03A8.03 8.03 0 0119.93 11zM12 4c1.2 1.37 2.08 3.33 2.46 5H9.54C9.92 7.33 10.8 5.37 12 4zM6.9 7a15.2 15.2 0 00-.8 4H4.07A8.03 8.03 0 016.9 7zM4.07 13H6.1c.12 1.4.43 2.77.8 4.03A8.03 8.03 0 014.07 13zM12 20c-1.2-1.37-2.08-3.33-2.46-5h4.92C14.08 16.67 13.2 18.63 12 20zm3-7H9c-.1-1-.1-2 0-3h6c.1 1 .1 2 0 3zm1.9 4a15.2 15.2 0 00.8-4h2.03a8.03 8.03 0 01-2.83 4z"/></svg>
          {{ t('footer.locale.vietnam') }}
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import AppFooterLinks from "@/components/AppFooterLinks.vue";

const adminUrl = computed(() => import.meta.env.VITE_ADMIN_URL ?? "/");
const { t } = useI18n();
</script>

<style scoped lang="scss">
.footer-links {
  background: #ffffff;
  color: #1d2129;
  padding: 24px 30px 0;
}

.footer-meta {
  background: #ffffff;
  width: 100%;
  padding: 16px 30px 32px;
  color: #4e5969;
  border-top: 1px solid #f0f0f0;
}

.meta-inner { width: min(1180px, 100%); margin: 0 auto; display: grid; gap: 12px; align-items: center; grid-template-columns: 1fr auto auto; }
.meta-inner nav { display: inline-flex; gap: 16px; }
.meta-inner a { color: #4e5969; text-decoration: none; }
.meta-inner a:hover { text-decoration: underline; }
.locale { display: inline-flex; gap: 8px; align-items: center; color: #1d2129; }
</style>
