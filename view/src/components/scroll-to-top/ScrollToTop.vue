<template>
  <transition name="slide-fade-up">
    <a-button v-show="showScrollTop" class="scroll-to-top-btn" type="primary" shape="circle" size="large" @click="scrollToTop">
      <template #icon>
        <icon-up />
      </template>
    </a-button>
  </transition>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { IconUp } from '@arco-design/web-vue/es/icon'

const showScrollTop = ref(false)

const handleScroll = () => {
  showScrollTop.value = window.scrollY > 300
}

const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth',
  })
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.scroll-to-top-btn {
  position: fixed;
  bottom: 32px;
  right: 32px;
  z-index: 999;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  background-color: #165dff !important;
  border-color: #165dff !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.scroll-to-top-btn:hover {
  background-color: #4080ff !important;
  border-color: #4080ff !important;
  transform: translateY(-4px) scale(1.05);
  box-shadow: 0 6px 20px rgba(22, 93, 255, 0.3);
}

.scroll-to-top-btn:active {
  transform: translateY(-2px) scale(1.02);
}

/* Slide fade up animation */
.slide-fade-up-enter-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-fade-up-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 1, 1);
}

.slide-fade-up-enter-from {
  transform: translateY(30px) scale(0.7);
  opacity: 0;
}

.slide-fade-up-leave-to {
  transform: translateY(30px) scale(0.7);
  opacity: 0;
}
</style>
