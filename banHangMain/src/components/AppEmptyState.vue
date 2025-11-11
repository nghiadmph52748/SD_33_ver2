<template>
  <div class="empty">
    <div class="icon" aria-hidden="true">👜</div>
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
.empty {
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 16px;
  padding: 28px 20px;
  text-align: center;
}
.icon { font-size: 40px; line-height: 1; margin-bottom: 8px; }
.title { margin: 0 0 4px 0; font-size: 20px; color: #111; }
.desc { margin: 0; color: rgba(17,17,17,.7); }
.actions { margin-top: 12px; }
</style>


