<template>
  <Teleport to="body">
    <Transition name="overlay-fade">
      <div v-if="visible" class="overlay" @click="onCancel">
        <Transition name="drawer-slide">
          <div v-if="visible" class="drawer" @click.stop>
            <div class="drawer__header">
              <h2 class="drawer__title">{{ t('store.filterTitle') }}</h2>
              <button type="button" class="drawer__close" @click="onCancel" aria-label="Close">
                <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true">
                  <path fill="currentColor" d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
                </svg>
              </button>
            </div>

            <div class="drawer__content">
              <div class="section">
                <label class="section__label">{{ t('store.sortBy') }}</label>
                <select v-model="localState.sort" class="select">
                  <option value="featured">{{ t('store.sortFeatured') }}</option>
                  <option value="price_low_high">{{ t('store.sortPriceLowHigh') }}</option>
                  <option value="price_high_low">{{ t('store.sortPriceHighLow') }}</option>
                  <option value="newest">{{ t('store.sortNewest') }}</option>
                </select>
              </div>

              <div class="section">
                <label class="section__label">{{ t('store.price') }}</label>
                <div class="price-range-container">
                  <div class="slider-wrapper">
                    <div class="slider-track">
                      <div 
                        class="slider-range" 
                        :style="{ 
                          left: `${(localState.price[0] / maxPrice) * 100}%`,
                          width: `${((localState.price[1] - localState.price[0]) / maxPrice) * 100}%`
                        }"
                      ></div>
                      <input
                        type="range"
                        :min="0"
                        :max="maxPrice"
                        :step="50000"
                        :value="localState.price[0]"
                        @input="updateMinPrice($event)"
                        class="slider slider--min"
                      />
                      <input
                        type="range"
                        :min="0"
                        :max="maxPrice"
                        :step="50000"
                        :value="localState.price[1]"
                        @input="updateMaxPrice($event)"
                        class="slider slider--max"
                      />
                    </div>
                  </div>
                  <div class="price-inputs">
                    <div class="price-input-wrapper">
                      <input
                        type="number"
                        v-model.number="localState.price[0]"
                        :min="0"
                        :max="localState.price[1]"
                        :step="50000"
                        class="price-input"
                        @input="validatePriceRange"
                      />
                      <span class="price-suffix">đ</span>
                    </div>
                    <span class="price-separator">—</span>
                    <div class="price-input-wrapper">
                      <input
                        type="number"
                        v-model.number="localState.price[1]"
                        :min="localState.price[0]"
                        :max="maxPrice"
                        :step="50000"
                        class="price-input"
                        @input="validatePriceRange"
                      />
                      <span class="price-suffix">đ</span>
                    </div>
                  </div>
                </div>
              </div>

              <div class="section">
                <label class="section__label">{{ t('store.gender') }}</label>
                <div class="checkbox-group">
                  <label class="checkbox-item">
                    <input
                      type="checkbox"
                      value="Male"
                      v-model="localState.categories"
                      class="checkbox-input"
                    />
                    <span class="checkbox-label">{{ t('store.men') }}</span>
                  </label>
                  <label class="checkbox-item">
                    <input
                      type="checkbox"
                      value="Female"
                      v-model="localState.categories"
                      class="checkbox-input"
                    />
                    <span class="checkbox-label">{{ t('store.women') }}</span>
                  </label>
                </div>
              </div>

              <div class="section" v-if="availableBrands.length > 0">
                <label class="section__label">{{ t('store.brands') }}</label>
                <div class="checkbox-group">
                  <label 
                    v-for="brand in availableBrands" 
                    :key="brand"
                    class="checkbox-item"
                  >
                    <input
                      type="checkbox"
                      :value="brand"
                      v-model="localState.categories"
                      class="checkbox-input"
                    />
                    <span class="checkbox-label">{{ brand }}</span>
                  </label>
                </div>
              </div>
            </div>

            <div class="drawer__footer">
              <button type="button" class="btn btn--secondary" @click="onReset">
                <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
                  <path fill="currentColor" d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/>
                </svg>
                <span>{{ t('store.reset') }}</span>
              </button>
              <button type="button" class="btn btn--primary" @click="onApply">
                {{ t('store.apply') }}
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { reactive, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const props = defineProps<{ 
  visible: boolean
  total?: number
  maxPrice?: number
  brands?: string[]
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'apply', payload: {
    sort: string
    price: [number, number]
    categories: string[]
  }): void
}>()

const maxPrice = computed(() => props.maxPrice || 50000000)
const availableBrands = computed(() => props.brands || [])

const localState = reactive({
  sort: 'featured',
  price: [0, 50000000] as [number, number],
  categories: [] as string[],
})

watch(() => props.visible, (visible) => {
  if (visible) {
    // Reset price range to full range when drawer opens
    localState.price = [0, maxPrice.value]
  }
})

watch(maxPrice, (newMax) => {
  if (newMax && newMax > 0) {
    // Update max price if current max exceeds new max
    if (localState.price[1] > newMax) {
      localState.price[1] = newMax
    }
    // Also update if drawer is visible to reflect new max
    if (props.visible) {
      localState.price[1] = newMax
    }
  }
}, { immediate: true })

function updateMinPrice(event: Event) {
  const target = event.target as HTMLInputElement
  const value = Number(target.value)
  if (value <= localState.price[1]) {
    localState.price[0] = value
  }
}

function updateMaxPrice(event: Event) {
  const target = event.target as HTMLInputElement
  const value = Number(target.value)
  if (value >= localState.price[0]) {
    localState.price[1] = value
  }
}

function validatePriceRange() {
  if (localState.price[0] < 0) {
    localState.price[0] = 0
  }
  if (localState.price[0] > localState.price[1]) {
    localState.price[0] = localState.price[1]
  }
  if (localState.price[1] > maxPrice.value) {
    localState.price[1] = maxPrice.value
  }
  if (localState.price[1] < localState.price[0]) {
    localState.price[1] = localState.price[0]
  }
}

function onReset() {
  localState.sort = 'featured'
  localState.price = [0, maxPrice.value]
  localState.categories = []
}

function onApply() {
  emit('apply', {
    sort: localState.sort,
    price: localState.price,
    categories: localState.categories,
  })
  emit('update:visible', false)
}

function onCancel() {
  emit('update:visible', false)
}
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 1000;
  display: flex;
  justify-content: flex-end;
}

.drawer {
  width: 420px;
  max-width: 92vw;
  height: 100%;
  background: #fff;
  display: flex;
  flex-direction: column;
  box-shadow: -4px 0 20px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.drawer__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
}

.drawer__title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #111;
  letter-spacing: 0.01em;
}

.drawer__close {
  background: transparent;
  border: none;
  padding: 6px;
  cursor: pointer;
  color: #6b7280;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.15s ease, color 0.15s ease;
}

.drawer__close:hover {
  background: #f3f4f6;
  color: #111;
}

.drawer__content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.section {
  margin-bottom: 32px;
}

.section:last-child {
  margin-bottom: 0;
}

.section__label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #111;
  margin-bottom: 12px;
  letter-spacing: 0.01em;
}

.select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: #fff;
  font-size: 14px;
  color: #111;
  cursor: pointer;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23374151' d='M6 9L1 4h10z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 36px;
}

.select:focus {
  outline: none;
  border-color: #111;
  box-shadow: 0 0 0 3px rgba(17, 17, 17, 0.1);
}

.price-range-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.slider-wrapper {
  position: relative;
  margin: 20px 0;
}

.slider-track {
  position: relative;
  height: 6px;
  background: #e5e7eb;
  border-radius: 3px;
}

.slider-range {
  position: absolute;
  height: 100%;
  background: #111;
  border-radius: 3px;
  pointer-events: none;
}

.slider {
  position: absolute;
  top: 50%;
  left: 0;
  width: 100%;
  height: 6px;
  margin: 0;
  transform: translateY(-50%);
  background: transparent;
  outline: none;
  appearance: none;
  cursor: pointer;
  z-index: 2;
}

.slider--min {
  z-index: 3;
}

.slider--max {
  z-index: 2;
}

.slider::-webkit-slider-thumb {
  appearance: none;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #111;
  cursor: pointer;
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
  position: relative;
}

.slider::-webkit-slider-thumb:hover {
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.slider::-moz-range-thumb {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #111;
  cursor: pointer;
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.slider::-moz-range-thumb:hover {
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.slider::-moz-range-track {
  background: transparent;
}

.price-inputs {
  display: flex;
  align-items: center;
  gap: 12px;
}

.price-input-wrapper {
  flex: 1;
  position: relative;
}

.price-input {
  width: 100%;
  padding: 10px 36px 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  color: #111;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.price-input:focus {
  outline: none;
  border-color: #111;
  box-shadow: 0 0 0 3px rgba(17, 17, 17, 0.1);
}

.price-suffix {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #6b7280;
  font-size: 14px;
  pointer-events: none;
}

.price-separator {
  color: #9ca3af;
  font-weight: 500;
}

.checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 8px;
  border-radius: 6px;
  transition: background-color 0.15s ease;
}

.checkbox-item:hover {
  background: #f9fafb;
}

.checkbox-input {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #111;
  flex-shrink: 0;
}

.checkbox-label {
  font-size: 14px;
  color: #374151;
  user-select: none;
}

.drawer__footer {
  padding: 20px 24px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: #fff;
  color: #374151;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: border-color 0.15s ease, background-color 0.15s ease, transform 0.08s ease, box-shadow 0.08s ease;
}

.btn:hover {
  border-color: #9ca3af;
  background: #f9fafb;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.btn:active {
  transform: translateY(0) scale(0.98);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.btn--primary {
  background: #111;
  color: #fff;
  border-color: #111;
}

.btn--primary:hover {
  background: #000;
  border-color: #000;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}

.btn--secondary {
  background: #fff;
  color: #374151;
}

.overlay-fade-enter-active,
.overlay-fade-leave-active {
  transition: opacity 0.2s ease;
}

.overlay-fade-enter-from,
.overlay-fade-leave-to {
  opacity: 0;
}

.drawer-slide-enter-active {
  transition: transform 0.3s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.drawer-slide-leave-active {
  transition: transform 0.25s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.drawer-slide-enter-from {
  transform: translateX(100%);
}

.drawer-slide-leave-to {
  transform: translateX(100%);
}

@media (max-width: 640px) {
  .drawer {
    width: 100%;
    max-width: 100%;
  }
}
</style>
