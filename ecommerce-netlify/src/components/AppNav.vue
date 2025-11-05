<template>
  <header>
    <div class="logo-section">
      <a-space :size="12" align="center">
        <img src="@/assets/logo.svg" alt="logo" class="logo" />
        <a-typography-title :heading="4" style="margin: 0">GearUp Store</a-typography-title>
      </a-space>
    </div>
    <nav>
      <ul>
        <li>
          <RouterLink to="/">{{ $t('nav.home') }}</RouterLink>
        </li>
        <li>
          <RouterLink to="/all">{{ $t('nav.all') }}</RouterLink>
        </li>
        <li>
          <RouterLink to="/women">{{ $t('nav.women') }}</RouterLink>
        </li>
        <li>
          <RouterLink to="/men">{{ $t('nav.men') }}</RouterLink>
        </li>
        <li>
          <div v-if="cartCount > 0" class="carttotal">{{ cartCount }}</div>
          <RouterLink to="/cart">{{ $t('nav.cart') }}</RouterLink>
        </li>
        <li>
          <a :href="adminUrl" target="_blank" rel="noopener">{{ $t('nav.admin') }}</a>
        </li>
        <li class="nav-controls">
          <a-tooltip :content="theme === 'light' ? $t('settings.navbar.theme.toDark') : $t('settings.navbar.theme.toLight')">
            <a-button class="nav-btn" type="outline" :shape="'circle'" @click="handleToggleTheme">
              <template #icon>
                <icon-moon-fill v-if="theme === 'dark'" />
                <icon-sun-fill v-else />
              </template>
            </a-button>
          </a-tooltip>
        </li>
        <li class="nav-controls">
          <a-select
            v-model="currentLocale"
            :trigger-props="{ autoFitPopupMinWidth: true }"
            :options="localeOptions"
            @change="changeLocale"
            class="locale-select"
          >
            <template #prefix>
              <icon-language />
            </template>
          </a-select>
        </li>
      </ul>
    </nav>
  </header>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { storeToRefs } from "pinia";
import { useI18n } from "vue-i18n";
import { useDark, useToggle } from "@vueuse/core";
import { Message } from "@arco-design/web-vue";
import { useCartStore } from "@/stores/cart";
import { LOCALE_OPTIONS } from "@/locale";

const cartStore = useCartStore();
const { cartCount } = storeToRefs(cartStore);
const { t, locale } = useI18n();

const adminUrl = computed(() => import.meta.env.VITE_ADMIN_URL ?? "/");

// Theme toggle
const isDark = useDark({
  attribute: 'arco-theme',
  valueDark: 'dark',
  valueLight: 'light',
  storageKey: 'arco-theme',
  onChanged(dark: boolean) {
    if (dark) {
      document.body.setAttribute('arco-theme', 'dark');
    } else {
      document.body.removeAttribute('arco-theme');
    }
  }
});

const toggleTheme = useToggle(isDark);
const theme = computed(() => isDark.value ? 'dark' : 'light');

const handleToggleTheme = () => {
  toggleTheme();
};

// Locale selector
const currentLocale = ref(locale.value);
const localeOptions = LOCALE_OPTIONS;

const changeLocale = (value: string) => {
  locale.value = value;
  localStorage.setItem('arco-locale', value);
  Message.success(t('navbar.action.locale'));
};
</script>

<style scoped lang="scss">
header {
  display: flex;
  justify-content: center;
  flex-direction: column;
  align-items: center;
  padding: 40px 40px 0;
}

.logo-section {
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo {
  width: 33px;
  height: 33px;
}

nav {
  display: flex;
  width: 80vw;
  margin-top: 30px;
  justify-content: center;
  align-items: center;
  border-top: 1px solid #ccc;
  border-bottom: 1px solid #ccc;
  padding: 8px 0;

  ul {
    padding-left: 0;
    display: flex;
    align-items: center;
    gap: 0;

    li {
      display: inline-flex;
      align-items: center;
      text-transform: uppercase;
      letter-spacing: 0.1em;
      font-size: 13px;
      padding: 0 20px;
      border-left: 1px solid #ddd;
      border-right: 1px solid #ddd;
      position: relative;

      a {
        color: black;
        text-decoration: none;

        &:hover {
          color: var(--color-primary-6, #1677ff);
        }
      }

      &.nav-controls {
        border-left: none;
        border-right: none;
        padding: 0 10px;
      }
    }
  }
}

.nav-btn {
  border: none;
  background: transparent;
}

.locale-select {
  width: 120px;
}

.carttotal {
  position: absolute;
  border-radius: 1000px;
  background: black;
  color: white;
  padding: 6px 10px;
  top: -18px;
  right: -5px;
  width: 25px;
  text-align: center;
  height: 25px;
  font-size: 10px;
  font-weight: bold;
}

@media (max-width: 850px) {
  nav ul li {
    padding: 0 5px !important;
    border-left: none !important;
    border-right: none !important;
  }

  .locale-select {
    width: 100px;
  }
}
</style>
