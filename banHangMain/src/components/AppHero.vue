<template>
  <section class="hero">
    <div class="hero-spacer" aria-hidden="true"></div>
    <Transition :name="transitionName">
      <img class="hero-img" :key="index" :src="currentSlide.image" :alt="currentSlide.alt" />
    </Transition>
    <Transition :name="transitionName">
      <div class="hero-overlay" :key="`overlay-${index}`">
        <h3 class="hero-title">{{ currentSlide.title }}</h3>
        <p class="hero-sub">{{ currentSlide.sub }}</p>
        <RouterLink to="/products">
          <button class="btn btn-outline-light hero-cta">{{ t('buttons.shopNow') }}</button>
        </RouterLink>
      </div>
    </Transition>
    <div class="hero-dots" role="tablist" aria-label="Hero slides">
      <button
        v-for="(s, i) in slides"
        :key="i"
        class="dot"
        :class="{ active: i === index }"
        :aria-selected="i === index"
        role="tab"
        :aria-label="`Go to slide ${i + 1}`"
        :title="`Go to slide ${i + 1}`"
        @click="goTo(i)"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";

interface Slide {
  image: string;
  alt: string;
  title: string;
  sub: string;
}

const { t } = useI18n();
const slides = ref<Slide[]>([
  { image: "/callout.jpg", alt: t('hero.alt1'), title: t('hero.title1'), sub: t('hero.sub1') },
  { image: "/bag.jpg", alt: t('hero.alt2'), title: t('hero.title2'), sub: t('hero.sub2') },
  { image: "/shoe1.jpg", alt: t('hero.alt3'), title: t('hero.title3'), sub: t('hero.sub3') }
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
  const next = i % slides.value.length;
  direction.value = next > index.value ? 'left' : 'right';
  index.value = next;
  start();
}

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
  background: #f5f5f5;
  isolation: isolate;
}

.hero-spacer {
  width: 100%;
  height: 52vw;
  max-height: 640px;
}

.hero-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  /* Slight zoom to ensure full bleed on all aspect ratios */
  transform: scale(1.08);
}

.hero::after {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to bottom,
    rgba(0, 0, 0, 0.25),
    rgba(0, 0, 0, 0.35)
  );
  pointer-events: none;
  z-index: 0;
}

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
  font-weight: 500;
  font-size: clamp(28px, 6vw, 64px);
  line-height: 1.1;
  margin: 0 0 8px 0;
  text-shadow: 0 2px 6px rgba(0,0,0,0.35);
  color: #ffffff;
}

.hero-sub {
  font-weight: 400;
  font-size: clamp(14px, 2.4vw, 20px);
  color: rgba(255, 255, 255, 0.9);
  margin: 0 0 16px 0;
  text-shadow: 0 1px 3px rgba(0,0,0,0.35);
}

.slide-left-enter-active,
.slide-left-leave-active,
.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform 600ms ease, opacity 600ms ease;
}

.slide-left-enter-from,
.slide-right-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

.slide-left-leave-to,
.slide-right-enter-from {
  transform: translateX(-100%);
  opacity: 0;
}

.hero-cta {
  border-color: rgba(255,255,255,0.9);
  color: #ffffff;
}

.hero-cta:hover {
  border-color: #ffffff;
  color: #111111;
  background: #ffffff;
}

.hero-dots {
  position: absolute;
  right: 16px;
  bottom: 16px;
  display: inline-flex;
  gap: 8px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  border: 1px solid rgba(255,255,255,0.8);
  background: rgba(255,255,255,0.2);
  box-shadow: 0 0 0 1px rgba(0,0,0,0.08) inset;
  transition: all 200ms ease;
}

.dot.active {
  background: #ffffff;
  border-color: #ffffff;
  width: 22px; /* pill highlight */
}

.dot:hover {
  background: rgba(255,255,255,0.6);
}

.dot:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px rgba(135,206,255,0.8);
}

@media (min-width: 960px) {
  .hero-overlay {
    left: 50%;
  }
}
</style>

