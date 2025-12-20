<template>
    <div class="search-select" ref="containerRef">
        <div class="search-select__field"
            :class="{ 'is-active': isOpen, 'is-disabled': disabled, 'is-error': status === 'error' }"
            @click="toggleDropdown">
            <input type="text" ref="inputRef" v-model="displayText" :placeholder="placeholder" :disabled="disabled"
                :readonly="!isOpen" class="search-select__input" @input="onInput"
                @keydown.down.prevent="moveHighlight(1)" @keydown.up.prevent="moveHighlight(-1)"
                @keydown.enter.prevent="selectHighlighted" @keydown.esc="closeDropdown" @focus="onFocus" />
            <div class="search-select__icon" :class="{ 'is-flipped': isOpen }">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                    stroke-linejoin="round">
                    <polyline points="6 9 12 15 18 9"></polyline>
                </svg>
            </div>
        </div>

        <transition name="dropdown">
            <div v-if="isOpen" class="search-select__dropdown">
                <div v-if="filteredOptions.length === 0" class="search-select__empty" role="status">
                    Không tìm thấy kết quả
                </div>
                <ul v-else class="search-select__list" role="listbox">
                    <li v-for="(option, index) in filteredOptions" :key="option.value" class="search-select__option"
                        :class="{ 'is-highlighted': highlightedIndex === index, 'is-selected': modelValue === option.value }"
                        role="option" @mousedown.prevent="selectOption(option)" @mouseenter="highlightedIndex = index">
                        {{ option.label }}
                    </li>
                </ul>
            </div>
        </transition>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, watch } from 'vue';
import { onClickOutside } from '@vueuse/core';

interface Option {
    value: string | number;
    label: string;
}

const props = defineProps<{
    modelValue: string | number;
    options: Option[];
    placeholder?: string;
    disabled?: boolean;
    status?: 'normal' | 'error';
}>();

const emit = defineEmits(['update:modelValue', 'change']);

const containerRef = ref<HTMLElement | null>(null);
const inputRef = ref<HTMLInputElement | null>(null);
const isOpen = ref(false);
const searchQuery = ref('');
const highlightedIndex = ref(-1);

const selectedOption = computed(() => {
    return props.options.find(o => o.value === props.modelValue);
});

const displayValue = computed(() => {
    // If we found a matching option, use its label
    if (selectedOption.value) {
        return selectedOption.value.label;
    }
    // If we have a value but no matching option (e.g., options still loading),
    // display the raw value so user sees something
    if (props.modelValue && typeof props.modelValue === 'string') {
        return props.modelValue;
    }
    return '';
});

// Display text switches between selected value (when closed) and search query (when open)
const displayText = computed({
    get() {
        return isOpen.value ? searchQuery.value : displayValue.value;
    },
    set(value: string) {
        searchQuery.value = value;
    }
});

const filteredOptions = computed(() => {
    if (!searchQuery.value) return props.options;
    const q = searchQuery.value.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
    return props.options.filter(o =>
        o.label.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '').includes(q)
    );
});

onClickOutside(containerRef, () => closeDropdown());

function toggleDropdown() {
    if (props.disabled) return;
    isOpen.value ? closeDropdown() : openDropdown();
}

function openDropdown() {
    isOpen.value = true;
    searchQuery.value = '';
    highlightedIndex.value = filteredOptions.value.findIndex(o => o.value === props.modelValue);
    nextTick(() => inputRef.value?.focus());
}

function closeDropdown() {
    isOpen.value = false;
    searchQuery.value = '';
    highlightedIndex.value = -1;
}

function onFocus() {
    if (!isOpen.value) {
        openDropdown();
    }
}

function onInput() {
    if (!isOpen.value) isOpen.value = true;
    highlightedIndex.value = 0;
}

function selectOption(option: Option) {
    emit('update:modelValue', option.value);
    emit('change', option.value);
    closeDropdown();
}

function moveHighlight(step: number) {
    if (!isOpen.value) {
        openDropdown();
        return;
    }
    const next = highlightedIndex.value + step;
    if (next >= 0 && next < filteredOptions.value.length) {
        highlightedIndex.value = next;
    }
}

function selectHighlighted() {
    if (highlightedIndex.value >= 0 && highlightedIndex.value < filteredOptions.value.length) {
        selectOption(filteredOptions.value[highlightedIndex.value]);
    }
}

// Update highlighting when list changes
watch(filteredOptions, () => {
    highlightedIndex.value = 0;
});
</script>

<style scoped>
.search-select {
    position: relative;
    width: 100%;
    font-family: inherit;
}

.search-select__field {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 0 16px;
    height: 48px;
    background: #fff;
    border: 1px solid rgba(17, 17, 17, 0.12);
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.search-select__field:hover:not(.is-disabled) {
    border-color: #111;
    background: #fafafa;
}

.search-select__field.is-active {
    border-color: #111;
    box-shadow: 0 0 0 4px rgba(17, 17, 17, 0.04);
    background: #fff;
}

.search-select__field.is-disabled {
    background: #f5f5f5;
    cursor: not-allowed;
    opacity: 0.6;
}

.search-select__field.is-error {
    border-color: #fa4539;
}

.search-select__input {
    flex: 1;
    border: none;
    background: transparent;
    padding: 0;
    font-size: 15px;
    font-weight: 500;
    color: #111;
    cursor: inherit;
    outline: none;
    width: 100%;
}

.search-select__input::placeholder {
    color: #999;
    opacity: 1;
}

.search-select__icon {
    display: flex;
    align-items: center;
    color: #999;
    transition: transform 0.2s ease;
}

.search-select__icon svg {
    width: 18px;
    height: 18px;
}

.search-select__icon.is-flipped {
    transform: rotate(180deg);
}

.search-select__dropdown {
    position: absolute;
    top: calc(100% + 8px);
    left: 0;
    right: 0;
    max-height: 280px;
    background: #fff;
    border: 1px solid rgba(17, 17, 17, 0.08);
    border-radius: 14px;
    box-shadow: 0 12px 32px rgba(17, 17, 17, 0.12);
    overflow-y: auto;
    z-index: 1000;
    backdrop-filter: blur(10px);
}

.search-select__list {
    list-style: none;
    margin: 0;
    padding: 6px;
}

.search-select__option {
    padding: 10px 14px;
    font-size: 14px;
    font-weight: 500;
    color: #444;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.15s ease;
}

.search-select__option:hover,
.search-select__option.is-highlighted {
    background: rgba(17, 17, 17, 0.05);
    color: #111;
}

.search-select__option.is-selected {
    background: #111 !important;
    color: #fff !important;
}

.search-select__empty {
    padding: 20px;
    text-align: center;
    color: #999;
    font-size: 14px;
}

/* Transitions */
.dropdown-enter-active,
.dropdown-leave-active {
    transition: opacity 0.2s ease, transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.dropdown-enter-from,
.dropdown-leave-to {
    opacity: 0;
    transform: translateY(-8px);
}

/* Scrollbar styling */
.search-select__dropdown::-webkit-scrollbar {
    width: 6px;
}

.search-select__dropdown::-webkit-scrollbar-thumb {
    background: rgba(17, 17, 17, 0.1);
    border-radius: 10px;
}

.search-select__dropdown::-webkit-scrollbar-thumb:hover {
    background: rgba(17, 17, 17, 0.2);
}
</style>
