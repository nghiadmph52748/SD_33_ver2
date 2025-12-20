<template>
  <div class="filter-sidebar">
    <div class="filter-sidebar__header">
      <h2 class="filter-sidebar__title">{{ t('store.filterTitle') }}</h2>
    </div>

    <div class="filter-sidebar__footer">
      <button type="button" class="btn btn--secondary" @click="onReset">
        <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
          <path fill="currentColor"
            d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z" />
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
              <div class="slider-range" :style="{
                left: `${(localState.price[0] / maxPrice) * 100}%`,
                width: `${((localState.price[1] - localState.price[0]) / maxPrice) * 100}%`
              }"></div>
              <input type="range" :min="0" :max="maxPrice" :step="50000" :value="localState.price[0]"
                @input="updateMinPrice($event)" class="slider slider--min" />
              <input type="range" :min="0" :max="maxPrice" :step="50000" :value="localState.price[1]"
                @input="updateMaxPrice($event)" class="slider slider--max" />
            </div>
          </div>
          <div class="price-inputs">
            <div class="price-input-wrapper">
              <input type="number" v-model.number="localState.price[0]" :min="0" :max="localState.price[1]"
                :step="50000" class="price-input" @input="validatePriceRange" />
              <span class="price-suffix">đ</span>
            </div>
            <span class="price-separator">—</span>
            <div class="price-input-wrapper">
              <input type="number" v-model.number="localState.price[1]" :min="localState.price[0]" :max="maxPrice"
                :step="50000" class="price-input" @input="validatePriceRange" />
              <span class="price-suffix">đ</span>
            </div>
          </div>
        </div>
      </div>

      <div class="section">
        <label class="section__label">{{ t('store.gender') }}</label>
        <div class="checkbox-group">
          <label class="checkbox-item">
            <input type="checkbox" value="Male" v-model="localState.categories" @change="onChange"
              class="checkbox-input" />
            <span class="checkbox-label">{{ t('store.men') }}</span>
          </label>
          <label class="checkbox-item">
            <input type="checkbox" value="Female" v-model="localState.categories" @change="onChange"
              class="checkbox-input" />
            <span class="checkbox-label">{{ t('store.women') }}</span>
          </label>
        </div>
      </div>

      <div class="section" v-if="availableBrands.length > 0">
        <label class="section__label">{{ t('store.brands') }}</label>
        <div class="checkbox-group">
          <label v-for="brand in availableBrands" :key="brand" class="checkbox-item">
            <input type="checkbox" :value="brand" v-model="localState.categories" @change="onChange"
              class="checkbox-input" />
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
  width: 280px;
  /* Slightly narrower */
  min-width: 260px;
  max-width: 280px;
  background: transparent;
  /* Removed border-right for a cleaner floating look, or keep it subtle */
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  position: sticky;
  top: 110px;
  /* Offset to clear the global sticky header */
  align-self: flex-start;
  max-height: calc(100vh - 130px);
  overflow: hidden;
  padding-right: 24px;
  /* Space between sidebar and grid */
}

/* Custom Scrollbar */
.filter-sidebar__content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 4px 4px 24px 4px;
  /* Inner padding */
  box-sizing: border-box;
}

.filter-sidebar__content::-webkit-scrollbar {
  width: 4px;
}

.filter-sidebar__content::-webkit-scrollbar-track {
  background: transparent;
}

.filter-sidebar__content::-webkit-scrollbar-thumb {
  background: #e5e7eb;
  border-radius: 4px;
}

.filter-sidebar__content::-webkit-scrollbar-thumb:hover {
  background: #d1d5db;
}

.filter-sidebar__header {
  padding: 0 0 24px 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.filter-sidebar__title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #111;
  letter-spacing: -0.02em;
}

.filter-sidebar__footer {
  /* Moved reset button to header maybe? Or keep at bottom? */
  padding: 24px 0;
  border-top: 1px solid #f3f4f6;
  margin-top: auto;
}

.section {
  margin-bottom: 40px;
  width: 100%;
}

.section__label {
  display: block;
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #111;
  margin-bottom: 16px;
}

/* Custom Checkboxes */
.checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  user-select: none;
  transition: opacity 0.2s;
}

.checkbox-item:hover {
  opacity: 0.8;
}

.checkbox-input {
  appearance: none;
  -webkit-appearance: none;
  width: 20px;
  height: 20px;
  border: 1.5px solid #d1d5db;
  border-radius: 4px;
  /* Soft square */
  background: #fff;
  cursor: pointer;
  position: relative;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.checkbox-input:checked {
  background: #111;
  border-color: #111;
}

.checkbox-input:checked::after {
  content: '';
  position: absolute;
  top: 45%;
  left: 50%;
  width: 5px;
  height: 9px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: translate(-50%, -60%) rotate(45deg);
}

.checkbox-label {
  font-size: 15px;
  color: #374151;
  line-height: 1.4;
  font-weight: 400;
}

.checkbox-input:checked+.checkbox-label {
  color: #111;
  font-weight: 500;
}

/* Price Slider */
.price-range-container {
  padding: 0 4px;
}

.slider-wrapper {
  height: 30px;
  position: relative;
  margin-bottom: 16px;
}

.slider-track {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 100%;
  height: 4px;
  background: #e5e7eb;
  border-radius: 2px;
}

.slider-range {
  position: absolute;
  top: 0;
  height: 100%;
  background: #111;
  border-radius: 2px;
}

.slider {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 100%;
  height: 4px;
  background: transparent;
  appearance: none;
  pointer-events: none;
  /* Allow click through to track */
  z-index: 10;
}

.slider--min {
  z-index: 12;
  /* Min slider on top */
}

.slider--max {
  z-index: 11;
}

/* Thumb Styling */
.slider::-webkit-slider-thumb {
  pointer-events: auto;
  /* Re-enable pointer on thumb */
  appearance: none;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #fff;
  border: 1.5px solid #111;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.15);
  cursor: grab;
  transition: transform 0.1s;
}

.slider::-webkit-slider-thumb:active {
  transform: scale(0.95);
  cursor: grabbing;
}

.slider::-moz-range-thumb {
  pointer-events: auto;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #fff;
  border: 1.5px solid #111;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.15);
  cursor: grab;
  border: none;
}

.price-inputs {
  display: flex;
  align-items: center;
  gap: 12px;
}

.price-input-wrapper {
  position: relative;
  flex: 1;
}

.price-input {
  width: 100%;
  padding: 8px 0;
  border: none;
  border-bottom: 1px solid #e5e7eb;
  border-radius: 0;
  font-size: 14px;
  font-weight: 500;
  color: #111;
  background: transparent;
  padding-right: 20px;
  transition: border-color 0.2s;
}

.price-input:focus {
  outline: none;
  border-bottom-color: #111;
}

.price-suffix {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  font-size: 14px;
  color: #9ca3af;
}

.select {
  width: 100%;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  color: #111;
  background-color: #fff;
  cursor: pointer;
  transition: all 0.2s;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg width='10' height='6' viewBox='0 0 10 6' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M1 1L5 5L9 1' stroke='%23111111' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 16px center;
}

.select:hover {
  border-color: #d1d5db;
}

.select:focus {
  outline: none;
  border-color: #111;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  font-family: inherit;
}

.btn--secondary {
  width: 100%;
  background: #fff;
  border: 1px solid #e5e7eb;
  color: #111;
  padding: 12px;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  font-weight: 600;
}

.btn--secondary:hover {
  border-color: #111;
  background: #111;
  color: #fff;
}

@media (max-width: 1024px) {
  .filter-sidebar {
    display: none;
    /* Hide styled sidebar on tablet, assumes Drawer is used */
  }
}
</style>
