<template>
  <a-config-provider :locale="locale">
    <router-view />
    <global-setting />
  </a-config-provider>
</template>

<script lang="ts" setup>
import viVN from '@arco-design/web-vue/es/locale/lang/vi-vn'
import GlobalSetting from '@/components/global-setting/global-setting.vue'

// Always use Vietnamese locale
const locale = viVN
</script>
