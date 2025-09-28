<template>
  <a-layout-footer class="footer">
    GearUp
    <span style="margin-left: 3px">© {{ currentYear }}</span>
  </a-layout-footer>
</template>

<script lang="ts" setup>
import { ref } from 'vue'

const currentYear = ref(new Date().getFullYear())
</script>

<style lang="less" scoped>
.footer {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  color: var(--color-text-2);
  text-align: center;
}
</style>
