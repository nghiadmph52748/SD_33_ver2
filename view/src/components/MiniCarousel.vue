<template>
  <div class="mini-carousel-wrapper">
    <div class="mini-carousel-container">
      <div v-if="images && images.length > 0" class="carousel-slides" :style="{ transform: `translateX(${-currentIndex * 100}%)` }">
        <div v-for="(img, index) in images" :key="index" class="carousel-slide">
          <img :src="img" :alt="`Image ${index}`" class="carousel-image" @error="handleImageError" />
        </div>
      </div>
      <div v-else class="carousel-slide-empty">
        <div class="placeholder-image">
          <svg viewBox="0 0 24 24" width="40" height="40" fill="#ccc">
            <path
              d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14zm-5.04-6.71l-2.75 3.54h2.63l2.96-3.83c.37-.48 1.45-2.18 2.96-3.83.35-.41.56-.99.15-1.38.44.15.85.43 1.12.82l-4.07 5.21z"
            ></path>
          </svg>
        </div>
      </div>

      <!-- Arrow buttons -->
      <div class="carousel-arrow carousel-arrow-left" @click.stop="prev" v-if="images && images.length > 1">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="white">
          <path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"></path>
        </svg>
      </div>
      <div class="carousel-arrow carousel-arrow-right" @click.stop="next" v-if="images && images.length > 1">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="white">
          <path d="M10 6L8.59 7.41 12.17 11 8.59 14.59 10 16l6-6z"></path>
        </svg>
      </div>

      <!-- Dots indicators -->
      <div class="carousel-dots" v-if="images && images.length > 1">
        <div
          v-for="(_, index) in images"
          :key="index"
          class="dot"
          :class="{ active: index === currentIndex }"
          @click.stop="currentIndex = index"
        ></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

interface Props {
  images: string[]
  autoplayInterval?: number
}

const props = withDefaults(defineProps<Props>(), {
  autoplayInterval: 2500,
})

const currentIndex = ref(0)
let autoplayTimer: number | null = null

const next = () => {
  currentIndex.value = (currentIndex.value + 1) % props.images.length
}

const prev = () => {
  currentIndex.value = (currentIndex.value - 1 + props.images.length) % props.images.length
}

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  console.warn(`Failed to load image: ${img.src}`)
  img.style.opacity = '0.5'
  img.style.backgroundImage = 'linear-gradient(45deg, #f0f0f0 25%, transparent 25%, transparent 75%, #f0f0f0 75%, #f0f0f0)'
}

const startAutoplay = () => {
  if (props.images.length <= 1) return

  autoplayTimer = window.setInterval(() => {
    next()
  }, props.autoplayInterval)
}

const stopAutoplay = () => {
  if (autoplayTimer !== null) {
    clearInterval(autoplayTimer)
    autoplayTimer = null
  }
}

onMounted(() => {
  startAutoplay()
})

onUnmounted(() => {
  stopAutoplay()
})
</script>

<style scoped>
.mini-carousel-wrapper {
  width: 100%;
  height: 100%;
}

.mini-carousel-container {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  border-radius: 4px;
  background: #f0f0f0;
}

.carousel-slides {
  display: flex;
  height: 100%;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.carousel-slide {
  flex: 0 0 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  overflow: hidden;
  background: #f0f0f0;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.carousel-slide-empty {
  flex: 0 0 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  width: 100%;
  background: #f5f5f5;
}

.placeholder-image {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #ccc;
  background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
}

/* Arrow buttons */
.carousel-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0, 0, 0, 0.5);
  border: none;
  color: white;
  cursor: pointer;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: none;
  align-items: center;
  justify-content: center;
  z-index: 5;
  transition: background 0.2s ease;
}

.carousel-arrow:hover {
  background: rgba(0, 0, 0, 0.8);
}

.mini-carousel-container:hover .carousel-arrow {
  display: flex;
}

.carousel-arrow-left {
  left: 4px;
}

.carousel-arrow-right {
  right: 4px;
}

/* Dots indicators */
.carousel-dots {
  position: absolute;
  bottom: 4px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 3px;
  z-index: 10;
}

.dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all 0.3s ease;
}

.dot:hover {
  background-color: rgba(255, 255, 255, 0.8);
}

.dot.active {
  width: 8px;
  background-color: rgba(255, 255, 255, 1);
  box-shadow: 0 0 4px rgba(0, 0, 0, 0.6);
}
</style>
