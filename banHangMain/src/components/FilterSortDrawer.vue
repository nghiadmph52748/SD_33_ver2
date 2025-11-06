<template>
  <div v-if="isMounted" :class="['overlay', isClosing ? 'overlay--leave' : 'overlay--enter']" @click.self="onCancel">
    <div :class="['drawer', isClosing ? 'drawer--leave' : 'drawer--enter']" role="dialog" aria-modal="true">
      <div class="drawer__header">
        <div class="drawer-title">
          <span>Filter & Sort</span>
          <span class="count" v-if="typeof total === 'number'">[{{ total }}]</span>
        </div>
        <button class="icon-btn" aria-label="Close" @click="onCancel">×</button>
      </div>

      <div class="section">
        <div class="section__title">Sort by</div>
        <select v-model="localState.sort" class="select">
          <option value="featured">Featured</option>
          <option value="price_low_high">Price: Low to High</option>
          <option value="price_high_low">Price: High to Low</option>
          <option value="newest">Newest</option>
        </select>
      </div>

      <hr class="divider" />

      <div class="section">
        <div class="section__title">Price</div>
        <div class="slider-row">
          <input type="range" min="0" max="50000000" step="10000" v-model.number="localState.price[0]" @input="clampMin" />
          <input type="range" min="0" max="50000000" step="10000" v-model.number="localState.price[1]" @input="clampMax" />
        </div>
        <div class="price-values">
          <span class="pill">{{ localState.price[0].toLocaleString() }}đ</span>
          <span class="dash">—</span>
          <span class="pill">{{ localState.price[1].toLocaleString() }}đ</span>
        </div>
        <div class="price-row">
          <input class="number" type="number" v-model.number="localState.price[0]" min="0" :max="localState.price[1]" />
          <span>—</span>
          <input class="number" type="number" v-model.number="localState.price[1]" :min="localState.price[0]" max="50000000" />
          <span class="currency">đ</span>
        </div>
      </div>

      <hr class="divider" />

      <div class="section">
        <div class="section__title">Categories</div>
        <label class="check"><input type="checkbox" value="running" v-model="localState.categories"/> <span>Running</span></label>
        <label class="check"><input type="checkbox" value="originals" v-model="localState.categories"/> <span>Originals</span></label>
        <label class="check"><input type="checkbox" value="training" v-model="localState.categories"/> <span>Training</span></label>
        <label class="check"><input type="checkbox" value="soccer" v-model="localState.categories"/> <span>Soccer</span></label>
      </div>

      <div class="footer-actions">
        <button class="btn" @click="onReset">Reset</button>
        <button class="btn btn--primary" @click="onApply">Apply</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, toRefs, ref, watch } from 'vue'

const props = defineProps<{ visible: boolean; total?: number }>()
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'apply', payload: {
    sort: string
    price: [number, number]
    categories: string[]
  }): void
}>()

const localState = reactive({
  sort: 'featured',
  price: [0, 50000000] as [number, number],
  categories: [] as string[],
})

const isMounted = ref(false)
const isClosing = ref(false)

watch(() => props.visible, (visible) => {
  if (visible) {
    isMounted.value = true
    // wait one tick to ensure enter classes apply
    isClosing.value = false
  } else if (isMounted.value) {
    // start leave animation, then unmount
    isClosing.value = true
    window.setTimeout(() => {
      isMounted.value = false
      isClosing.value = false
    }, 250)
  }
}, { immediate: true })

function onReset() {
  localState.sort = 'featured'
  localState.price = [0, 10000]
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

function clampMin() {
  if (localState.price[0] > localState.price[1]) {
    localState.price[0] = localState.price[1]
  }
}

function clampMax() {
  if (localState.price[1] < localState.price[0]) {
    localState.price[1] = localState.price[0]
  }
}

// expose for parent if needed
defineExpose({ ...toRefs(localState) })
</script>

<style scoped>
.overlay--enter { animation: fadeIn .2s ease-out; }
.overlay--leave { animation: fadeOut .22s ease-in forwards; }
.drawer--enter { animation: slideIn .28s cubic-bezier(.2,.8,.2,1); }
.drawer--leave { animation: slideOut .24s cubic-bezier(.2,.8,.2,1) forwards; }

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
@keyframes fadeOut {
  from { opacity: 1; }
  to { opacity: 0; }
}
@keyframes slideIn {
  from { transform: translateX(12%); opacity: .6; }
  to { transform: translateX(0); opacity: 1; }
}
@keyframes slideOut {
  from { transform: translateX(0); opacity: 1; }
  to { transform: translateX(12%); opacity: .6; }
}
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.35);
  display: flex;
  justify-content: flex-end;
  z-index: 999;
}
.drawer {
  width: 360px;
  max-width: 92vw;
  height: 100%;
  background: #fff;
  padding: 16px 16px 20px 16px;
  box-shadow: -4px 0 20px rgba(0,0,0,.15);
  overflow-y: auto;
}
.drawer__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.drawer-title {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.icon-btn {
  background: transparent;
  border: none;
  font-size: 22px;
  line-height: 1;
  cursor: pointer;
  padding: 4px 6px;
  border-radius: 6px;
  transition: background-color .15s ease;
}
.icon-btn:hover { background: #f3f4f6; }
.count {
  color: #6b7280;
  font-weight: 500;
}
.section {
  margin-bottom: 12px;
}
.section__title {
  font-weight: 600;
  margin-bottom: 8px;
}
.select { width: 100%; padding: 8px 10px; border: 1px solid #d1d5db; border-radius: 6px; transition: border-color .15s ease, box-shadow .15s ease; }
.select:focus { outline: none; border-color: #111; box-shadow: 0 0 0 3px rgba(17,17,17,.08); }
.slider-row { display: flex; gap: 8px; }
.slider-row input[type="range"] { flex: 1; appearance: none; height: 4px; background: #e5e7eb; border-radius: 999px; }
.slider-row input[type="range"]::-webkit-slider-thumb { appearance: none; width: 14px; height: 14px; border-radius: 50%; background: #111; cursor: pointer; }
.slider-row input[type="range"]::-moz-range-thumb { width: 14px; height: 14px; border-radius: 50%; background: #111; border: none; cursor: pointer; }
.price-values { display: flex; align-items: center; gap: 8px; margin: 8px 0; }
.pill { padding: 4px 8px; background: #f3f4f6; border-radius: 999px; font-size: 12px; }
.dash { color: #9ca3af; }
.price-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 4px;
}
.number { width: 100px; padding: 6px 8px; border: 1px solid #d1d5db; border-radius: 6px; transition: border-color .15s ease, box-shadow .15s ease; }
.number:focus { outline: none; border-color: #111; box-shadow: 0 0 0 3px rgba(17,17,17,.08); }
.currency { color: #6b7280; }
.check { display: flex; align-items: center; gap: 8px; margin: 6px 0; }
.divider { border: 0; border-top: 1px solid #eee; margin: 12px 0; }
.footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}
.btn { padding: 9px 14px; border: 1px solid #d1d5db; background: #fff; color: #374151; border-radius: 8px; font-weight: 600; transition: border-color .15s ease, background-color .15s ease, transform .05s ease; }
.btn:hover { border-color: #9ca3af; background: #f9fafb; }
.btn:active { transform: translateY(1px); }
.btn--primary { background: #111; color: #fff; border-color: #111; }
.btn--primary:hover { background: #000; }
</style>


