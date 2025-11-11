<template>
  <div class="error">
    <div class="icon" aria-hidden="true">⚠️</div>
    <h3 class="title">{{ title }}</h3>
    <p class="desc" v-if="description">{{ description }}</p>
    <div class="actions" v-if="$slots.default">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<style scoped lang="scss">
.error {
  background: #fff7f7;
  border: 1px solid #ffd6d6;
  border-radius: 16px;
  padding: 28px 20px;
  text-align: center;
}
.icon { font-size: 40px; line-height: 1; margin-bottom: 8px; }
.title { margin: 0 0 4px 0; font-size: 20px; color: #8c1d18; }
.desc { margin: 0; color: #8c1d18; }
.actions { margin-top: 12px; }
</style>


