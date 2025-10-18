<template>
  <a-config-provider :locale="locale">
    <router-view />
    <global-setting />
    <scroll-to-top />
  </a-config-provider>
</template>

<script lang="ts" setup>
import viVN from '@arco-design/web-vue/es/locale/lang/vi-vn'
import GlobalSetting from '@/components/global-setting/global-setting.vue'
import ScrollToTop from '@/components/scroll-to-top/ScrollToTop.vue'

// Always use Vietnamese locale
const locale = viVN
</script>
