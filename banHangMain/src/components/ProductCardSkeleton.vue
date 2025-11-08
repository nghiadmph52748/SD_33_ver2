<template>
  <div class="skeleton-item" role="status" aria-label="Loading product">
    <div class="skeleton-item__media">
      <div class="skeleton-shimmer"></div>
    </div>
    <div class="skeleton-item__body">
      <div class="skeleton-shimmer skeleton-price"></div>
      <div class="skeleton-shimmer skeleton-title"></div>
      <div class="skeleton-shimmer skeleton-meta"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
// Skeleton component for product cards
</script>

<style scoped lang="scss">
.skeleton-item {
  position: relative;
  display: flex;
  flex-direction: column;
  background: #fff;
  overflow: hidden;
  aspect-ratio: 1 / 1.2;
}

.skeleton-item__media {
  position: relative;
  flex: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  overflow: hidden;
  min-height: 0;
}

.skeleton-item__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px;
  gap: 8px;
}

.skeleton-shimmer {
  background: linear-gradient(
    90deg,
    #f0f0f0 0%,
    #f8f8f8 50%,
    #f0f0f0 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
  border-radius: 4px;
}

.skeleton-price {
  height: 20px;
  width: 80px;
}

.skeleton-title {
  height: 18px;
  width: 90%;
}

.skeleton-meta {
  height: 14px;
  width: 60%;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}
</style>

