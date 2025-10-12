<template>
  <div class="print-layout">
    <router-view />
  </div>
</template>

<script setup lang="ts">
// Layout đơn giản chỉ có nội dung, không có menu và navbar
</script>

<style scoped>
.print-layout {
  width: 100%;
  height: 100vh;
  background: white;
  overflow: auto;
  margin: 0;
  padding: 0;
}

/* Ẩn hoàn toàn tất cả menu và navbar */
:deep(.arco-layout-header),
:deep(.arco-layout-sider),
:deep(.arco-layout-aside),
:deep(.arco-menu),
:deep(.arco-menu-item),
:deep(.arco-menu-group),
:deep(.arco-menu-submenu),
:deep(.layout-navbar),
:deep(.layout-sider),
:deep(.menu-wrapper),
:deep(.left-side),
:deep(.arco-layout-content) {
  display: none !important;
}
</style>
