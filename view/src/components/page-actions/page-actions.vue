<template>
  <div class="actions-wrapper">
    <div class="actions-content">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
// Simple action bar wrapper
</script>

<style scoped>
.actions-wrapper {
  padding: 16px 0;
}

.actions-content {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  background: #ffffff;
  border: 1px solid var(--color-border-2);
  border-radius: 6px;
  padding: 12px 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}
</style>
