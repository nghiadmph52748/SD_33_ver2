<template>
  <header>
    <div class="topbar" aria-label="User navigation">
      <ul>
        <li><a href="#" aria-label="Find a Store">Find a Store</a></li>
        <li><a href="#" aria-label="Help">Help</a></li>
        <li><a href="#" aria-label="Join Us">Join Us</a></li>
        <li><a href="#" aria-label="Sign In">Sign In</a></li>
      </ul>
    </div>
    <div class="mainbar">
    <div class="logo-section">
      <a-space :size="12" align="center">
        <img src="@/assets/logo.svg" alt="logo" class="logo" />
        <span class="brand-name">GearUp Store</span>
      </a-space>
      </div>
      <div class="search">
        <a-input placeholder="Tìm kiếm" allow-clear>
          <template #prefix>
            <svg width="16" height="16" viewBox="0 0 24 24" aria-hidden="true">
              <path fill="currentColor" d="M10 2a8 8 0 105.293 14.293l3.706 3.707 1.414-1.414-3.707-3.706A8 8 0 0010 2zm0 2a6 6 0 110 12 6 6 0 010-12z"/>
            </svg>
          </template>
        </a-input>
      </div>
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
          <RouterLink to="/favorites">Yêu thích</RouterLink>
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
              <svg width="14" height="14" viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M12 2a10 10 0 100 20 10 10 0 000-20zm7.93 9H16.9a15.2 15.2 0 00-.8-4.03A8.03 8.03 0 0119.93 11zM12 4c1.2 1.37 2.08 3.33 2.46 5H9.54C9.92 7.33 10.8 5.37 12 4zM6.9 7a15.2 15.2 0 00-.8 4H4.07A8.03 8.03 0 016.9 7zM4.07 13H6.1c.12 1.4.43 2.77.8 4.03A8.03 8.03 0 014.07 13zM12 20c-1.2-1.37-2.08-3.33-2.46-5h4.92C14.08 16.67 13.2 18.63 12 20zm3-7H9c-.1-1-.1-2 0-3h6c.1 1 .1 2 0 3zm1.9 4a15.2 15.2 0 00.8-4h2.03a8.03 8.03 0 01-2.83 4z"/></svg>
            </template>
          </a-select>
        </li>
      </ul>
    </nav>
  </header>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { storeToRefs } from "pinia";
import { useI18n } from "vue-i18n";
import { useCartStore } from "@/stores/cart";
import { LOCALE_OPTIONS } from "@/locale";

const cartStore = useCartStore();
const { cartCount } = storeToRefs(cartStore);
const { t, locale } = useI18n();

// removed admin link

// Theme toggle removed

// Locale selector
const currentLocale = ref(locale.value);
const localeOptions = LOCALE_OPTIONS;

const changeLocale = (value: string) => {
  locale.value = value;
  localStorage.setItem('arco-locale', value);
};
</script>

<style scoped lang="scss">
header {
  display: flex;
  justify-content: center;
  flex-direction: column;
  align-items: center;
  padding: 0 40px 0;
}

.topbar {
  width: 100%;
  max-width: 1016px;
  display: flex;
  justify-content: flex-end;
  border-bottom: 1px solid #f0f0f0;
  padding: 6px 0;

  ul {
    display: flex;
    gap: 16px;
    list-style: none;
    margin: 0;
    padding: 0;

    li a {
      color: rgba(17, 17, 17, 0.8);
      text-decoration: none;
      font-size: 12px;
    }
  }
}

.mainbar {
  width: 100%;
  max-width: 1016px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0 0;
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

.brand-name {
  font-family: "Times New Roman", Georgia, "PT Serif", serif;
  font-weight: 700;
  font-size: 18px;
  letter-spacing: .4px;
}

nav {
  display: flex;
  width: 100%;
  max-width: 1016px;
  margin-top: 16px;
  justify-content: center;
  align-items: center;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
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
        color: #111111;
        text-decoration: none;

        &:hover {
          color: rgba(17, 17, 17, 1);
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

.search {
  width: 240px;
}

.search :deep(.arco-input) { border-radius: 20px; }
.search :deep(.arco-input-prefix) { color: rgba(17,17,17,.65); }

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
