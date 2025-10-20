<template>
  <a-config-provider :locale="locale">
    <router-view />
    <global-setting />
    <scroll-to-top />
  </a-config-provider>
</template>

<script lang="ts" setup>
import enUS from '@arco-design/web-vue/es/locale/lang/en-us'
import viVN from '@arco-design/web-vue/es/locale/lang/vi-vn'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import GlobalSetting from '@/components/global-setting/global-setting.vue'
import ScrollToTop from '@/components/scroll-to-top/ScrollToTop.vue'

const { locale: i18nLocale } = useI18n()

// Sync Arco Design locale with vue-i18n locale
const locale = computed(() => {
  return i18nLocale.value === 'en-US' ? enUS : viVN
})
</script>
