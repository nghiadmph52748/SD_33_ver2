<template>
  <div class="filter-sidebar">
    <div class="filter-sidebar__header">
      <h2 class="filter-sidebar__title">{{ t('store.filterTitle') }}</h2>
    </div>

    <div class="filter-sidebar__footer">
      <button type="button" class="btn btn--secondary" @click="onReset">
        <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
          <path fill="currentColor" d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/>
        </svg>
        <span>{{ t('store.reset') }}</span>
      </button>
    </div>

    <div class="filter-sidebar__content">
      <div class="section">
        <label class="section__label">{{ t('store.sortBy') }}</label>
        <select v-model="localState.sort" class="select" @change="onChange">
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
              @change="onChange"
              class="checkbox-input"
            />
            <span class="checkbox-label">{{ t('store.men') }}</span>
          </label>
          <label class="checkbox-item">
            <input
              type="checkbox"
              value="Female"
              v-model="localState.categories"
              @change="onChange"
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
              @change="onChange"
              class="checkbox-input"
            />
            <span class="checkbox-label">{{ brand }}</span>
          </label>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const props = defineProps<{ 
  maxPrice?: number
  brands?: string[]
}>()

const emit = defineEmits<{
  (e: 'change', payload: {
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

watch(maxPrice, (newMax) => {
  if (newMax && newMax > 0) {
    if (localState.price[1] > newMax) {
      localState.price[1] = newMax
    }
  }
}, { immediate: true })

function updateMinPrice(event: Event) {
  const target = event.target as HTMLInputElement
  const value = Number(target.value)
  if (value <= localState.price[1]) {
    localState.price[0] = value
    onChange()
  }
}

function updateMaxPrice(event: Event) {
  const target = event.target as HTMLInputElement
  const value = Number(target.value)
  if (value >= localState.price[0]) {
    localState.price[1] = value
    onChange()
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
  onChange()
}

function onChange() {
  emit('change', {
    sort: localState.sort,
    price: localState.price,
    categories: localState.categories,
  })
}

function onReset() {
  localState.sort = 'featured'
  localState.price = [0, maxPrice.value]
  localState.categories = []
  onChange()
}

defineExpose({
  reset: onReset,
  getState: () => ({
    sort: localState.sort,
    price: localState.price,
    categories: localState.categories,
  }),
  setState: (state: { sort: string; price: [number, number]; categories: string[] }) => {
    localState.sort = state.sort
    localState.price = state.price
    localState.categories = state.categories
  },
})
</script>

<style scoped>
.filter-sidebar {
  width: 320px;
  min-width: 280px;
  max-width: 320px;
  background: transparent;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  position: sticky;
  top: 24px;
  align-self: flex-start;
  max-height: calc(100vh - 48px);
  overflow: hidden;
}

.filter-sidebar__header {
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
}

.filter-sidebar__title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #111;
  letter-spacing: 0.01em;
}

.filter-sidebar__content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 24px;
  box-sizing: border-box;
}

.filter-sidebar__content::-webkit-scrollbar {
  width: 6px;
}

.filter-sidebar__content::-webkit-scrollbar-track {
  background: transparent;
}

.filter-sidebar__content::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}

.filter-sidebar__content::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}

.section {
  margin-bottom: 32px;
  width: 100%;
  box-sizing: border-box;
  min-width: 0;
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
  width: 100%;
  box-sizing: border-box;
}

.slider-wrapper {
  position: relative;
  margin: 20px 0;
  width: 100%;
  box-sizing: border-box;
}

.slider-track {
  position: relative;
  width: 100%;
  height: 6px;
  background: #e5e7eb;
  border-radius: 3px;
  box-sizing: border-box;
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
  padding: 0;
  transform: translateY(-50%);
  background: transparent;
  outline: none;
  appearance: none;
  cursor: pointer;
  z-index: 2;
  box-sizing: border-box;
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
  width: 100%;
  box-sizing: border-box;
  min-width: 0;
}

.price-input-wrapper {
  flex: 1;
  position: relative;
  min-width: 0;
  box-sizing: border-box;
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
  font-size: 14px;
  color: #6b7280;
  pointer-events: none;
}

.price-separator {
  color: #6b7280;
  font-size: 14px;
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
  user-select: none;
}

.checkbox-input {
  width: 18px;
  height: 18px;
  border: 2px solid #d1d5db;
  border-radius: 4px;
  cursor: pointer;
  appearance: none;
  position: relative;
  transition: border-color 0.15s ease, background-color 0.15s ease;
}

.checkbox-input:checked {
  background: #111;
  border-color: #111;
}

.checkbox-input:checked::after {
  content: '';
  position: absolute;
  left: 4px;
  top: 1px;
  width: 5px;
  height: 10px;
  border: solid #fff;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.checkbox-input:hover {
  border-color: #9ca3af;
}

.checkbox-label {
  font-size: 14px;
  color: #111;
  line-height: 1.5;
}

.filter-sidebar__footer {
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.15s ease, color 0.15s ease, transform 0.08s ease;
  border: none;
}

.btn--secondary {
  width: 100%;
  justify-content: center;
  background: #f3f4f6;
  color: #111;
}

.btn--secondary:hover {
  background: #e5e7eb;
  transform: translateY(-1px);
}

.btn--secondary:active {
  transform: translateY(0);
}

@media (max-width: 1024px) {
  .filter-sidebar {
    width: 280px;
  }
}

@media (max-width: 768px) {
  .filter-sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #e5e7eb;
  }
}
</style>

