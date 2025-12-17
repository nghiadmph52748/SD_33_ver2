<template>
  <section class="hero">
    <div class="hero-spacer" aria-hidden="true"></div>
    <Transition :name="transitionName">
      <img class="hero-img" :key="index" :src="currentSlide.image" :alt="currentSlide.alt" />
    </Transition>
    <Transition :name="transitionName" mode="out-in">
      <div class="hero-overlay" :key="`overlay-${index}`">
        <h3 class="hero-title">{{ currentSlide.title }}</h3>
        <p class="hero-sub">{{ currentSlide.sub }}</p>
        <RouterLink to="/all">
          <button class="btn btn-outline-light hero-cta">{{ t('buttons.shopNow') }}</button>
        </RouterLink>
      </div>
    </Transition>
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import { RouterLink } from "vue-router";
import heroNike from '@/assets/hero/Nike.png'
import heroNikeAlt from '@/assets/hero/Nike1.png'
import heroAdidas from '@/assets/hero/adidas.png'

interface Slide {
  image: string;
  alt: string;
  title: string;
  sub: string;
}

const { t } = useI18n();
const slides = ref<Slide[]>([
  { image: heroNike, alt: t('hero.alt1'), title: t('hero.title1'), sub: t('hero.sub1') },
  { image: heroNikeAlt, alt: t('hero.alt2'), title: t('hero.title2'), sub: t('hero.sub2') },
  { image: heroAdidas, alt: t('hero.alt3'), title: t('hero.title3'), sub: t('hero.sub3') }
]);

const index = ref(0);
const direction = ref<'left' | 'right'>('left');
const transitionName = computed(() => direction.value === 'left' ? 'slide-left' : 'slide-right');
const currentSlide = computed(() => slides.value[index.value]);

let timer: number | undefined;

function start() {
  stop();
  timer = window.setInterval(() => {
    direction.value = 'left';
    index.value = (index.value + 1) % slides.value.length;
  }, 5000);
}

function stop() {
  if (timer) {
    clearInterval(timer);
    timer = undefined;
  }
}

function goTo(i: number) {
  const next = (i + slides.value.length) % slides.value.length;
  direction.value = next > index.value ? 'left' : 'right';
  index.value = next;
  start();
}

function next() { goTo(index.value + 1); }
function prev() { goTo(index.value - 1); }

onMounted(start);
onBeforeUnmount(stop);
</script>

<style scoped lang="scss">
.hero {
  position: relative;
  width: 100vw; /* full-bleed horizontally */
  left: 50%;
  right: 50%;
  margin-left: -50vw;
  margin-right: -50vw;
  overflow: hidden;
  background: transparent;
  isolation: isolate;
}

.hero-spacer { width: 100%; height: 52vw; max-height: 640px; }

/* Image fits inside container without stretching */
.hero-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: contain; /* fit image inside without stretching or cropping */
  object-position: center;
  display: block;
}

.hero::after { content: none; }

.hero-overlay {
  position: absolute;
  left: 50%;
  bottom: 14%;
  transform: translateX(-50%);
  width: 90%;
  max-width: 1016px;
  color: #ffffff;
  z-index: 1;
}

.hero-title {
  font-family: var(--font-family-sans);
  font-weight: 600;
  font-size: clamp(28px, 6vw, 64px);
  line-height: 1.1;
  letter-spacing: -0.02em;
  margin: 0 0 8px 0;
  text-shadow: 0 2px 6px rgba(0,0,0,0.35);
  color: #ffffff;
  text-rendering: optimizeLegibility;
}
.hero-sub { font-weight: 400; font-size: clamp(14px, 2.4vw, 20px); color: rgba(255, 255, 255, 0.9); margin: 0 0 16px 0; text-shadow: 0 1px 3px rgba(0,0,0,0.35); }

.slide-left-enter-active,
.slide-left-leave-active,
.slide-right-enter-active,
.slide-right-leave-active { transition: transform 600ms ease, opacity 600ms ease; }

.slide-left-enter-from,
.slide-right-leave-to { transform: translateX(100%); opacity: 0; }

.slide-left-leave-to,
.slide-right-enter-from { transform: translateX(-100%); opacity: 0; }

.hero-cta { border-color: rgba(255,255,255,0.9); color: #ffffff; }
.hero-cta:hover { border-color: #ffffff; color: #111111; background: #ffffff; }

/* Navigation arrows */
.hero-nav {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100vw;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  pointer-events: none; /* allow clicks only on buttons */
  box-sizing: border-box;
  max-width: 100vw;
  overflow: visible;
}

.nav-btn {
  pointer-events: auto;
  width: 42px;
  height: 42px;
  border-radius: 999px;
  border: 1px solid rgba(255,255,255,0.8);
  background: rgba(0,0,0,0.35);
  color: #ffffff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(8px);
  box-shadow: 0 4px 14px rgba(0,0,0,0.25);
  transition: transform .12s ease, background-color .12s ease, border-color .12s ease;
  flex-shrink: 0;
  position: relative;
  z-index: 2;
}

.nav-btn:hover { 
  background: rgba(0,0,0,0.55); 
  border-color: #ffffff; 
  transform: translateY(-1px); 
}

.nav-btn:active { 
  transform: translateY(0) scale(0.96); 
}

@media (min-width: 960px) { .hero-overlay { left: 50%; } }
</style>

