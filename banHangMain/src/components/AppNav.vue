<template>
  <header :class="{ scrolled: isScrolled }">
    <div class="topbar" aria-label="User navigation">
      <div class="locale-wrapper" @keydown.escape.prevent="closeLocale">
        <button class="locale-bar" :class="{ open: localeOpen }" type="button" @click="toggleLocale" :aria-expanded="localeOpen ? 'true' : 'false'" aria-haspopup="listbox">
          <span class="icon" aria-hidden="true">
            <svg width="14" height="14" viewBox="0 0 24 24"><path fill="currentColor" d="M12 2a10 10 0 100 20 10 10 0 000-20zm7.93 9H16.9a15.2 15.2 0 00-.8-4.03A8.03 8.03 0 0119.93 11zM12 4c1.2 1.37 2.08 3.33 2.46 5H9.54C9.92 7.33 10.8 5.37 12 4zM6.9 7a15.2 15.2 0 00-.8 4H4.07A8.03 8.03 0 016.9 7zM4.07 13H6.1c.12 1.4.43 2.77.8 4.03A8.03 8.03 0 014.07 13zM12 20c-1.2-1.37-2.08-3.33-2.46-5h4.92C14.08 16.67 13.2 18.63 12 20zm3-7H9c-.1-1-.1-2 0-3h6c.1 1 .1 2 0 3zm1.9 4a15.2 15.2 0 00.8-4h2.03a8.03 8.03 0 01-2.83 4z"/></svg>
          </span>
          <span class="locale-label">{{ currentLocaleLabel }}</span>
          <span class="caret" aria-hidden="true">
            <svg width="12" height="12" viewBox="0 0 24 24"><path fill="currentColor" d="M7 10l5 5 5-5z"/></svg>
          </span>
        </button>
        <transition name="locale-menu">
          <div v-if="localeOpen" class="locale-menu" role="listbox" :aria-activedescendant="currentLocale">
            <button
              v-for="opt in localeOptions"
              :key="opt.value"
              :id="`loc-${opt.value}`"
              class="locale-chip"
              :class="{ active: opt.value === currentLocale }"
              role="option"
              type="button"
              @click="selectLocale(opt.value)"
            >
              {{ opt.label }}
            </button>
          </div>
        </transition>
      </div>
      <ul>
        <li><a href="#" :aria-label="$t('nav.findStore')">{{ $t('nav.findStore') }}</a></li>
        <li><a href="#" :aria-label="$t('nav.help')">{{ $t('nav.help') }}</a></li>
        <li>
          <RouterLink to="/tra-cuu-hoa-don" class="lookup-link" :aria-label="$t('nav.orderLookup')">
            {{ $t('nav.orderLookup') }}
          </RouterLink>
        </li>
        <template v-if="!isAuthenticated">
          <li><a href="#" :aria-label="$t('nav.joinUs')">{{ $t('nav.joinUs') }}</a></li>
          <li><RouterLink to="/login" :aria-label="$t('nav.signIn')">{{ $t('nav.signIn') }}</RouterLink></li>
        </template>
        <template v-else>
          <li class="customer-name" aria-label="customer-name">{{ customerName }}</li>
          <li><a href="#" @click.prevent="openLogoutConfirm">{{ $t('nav.logout') }}</a></li>
        </template>
      </ul>
    </div>
    <div class="mainbar">
    <div class="logo-section">
      <RouterLink to="/" class="brand-link" aria-label="Home">
        <img src="@/assets/logo-datn.png" alt="GearUp Store" class="logo-brand" />
      </RouterLink>
      </div>
      <nav v-if="!isLoginPage" class="inline-nav">
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
            <div v-if="cartCount > 0" class="carttotal" :class="{ pulse: cartBadgePulse }">{{ cartCount }}</div>
          <RouterLink to="/cart">{{ $t('nav.cart') }}</RouterLink>
        </li>
        <li>
            <RouterLink to="/favorites">{{ $t('nav.favorites') }}</RouterLink>
        </li>
          
      </ul>
    </nav>
      <div class="search" role="search">
        <form class="searchbar" @submit.prevent="handleSearch" :aria-label="$t('nav.search')">
          <span class="icon" aria-hidden="true">
            <svg width="16" height="16" viewBox="0 0 24 24"><path fill="currentColor" d="M10 2a8 8 0 105.293 14.293l3.706 3.707 1.414-1.414-3.707-3.706A8 8 0 0010 2zm0 2a6 6 0 110 12 6 6 0 010-12z"/></svg>
          </span>
          <input
            v-model.trim="searchQuery"
            class="search-input"
            type="text"
            :placeholder="$t('nav.search')"
            aria-label="Search"
          />
          <button class="search-btn" type="submit">
            <span class="sr-only">{{$t('nav.search')}}</span>
            <svg class="arrow" width="16" height="16" viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M13 5l7 7-7 7v-4H4v-6h9V5z"/></svg>
          </button>
        </form>
      </div>
    </div>
  </header>
  <teleport to="body">
    <transition name="logout-fade">
      <div v-if="showLogoutConfirm" class="logout-confirm-backdrop">
        <div class="logout-confirm-dialog">
        <h3>{{ $t('nav.logoutConfirmTitle') }}</h3>
        <p>{{ $t('nav.logoutConfirmMessage') }}</p>
          <div class="dialog-actions">
            <button type="button" class="dialog-btn ghost" @click="cancelLogout">
              {{ $t('nav.logoutConfirmCancel') }}
            </button>
            <button type="button" class="dialog-btn primary" @click="confirmLogout">
              {{ $t('nav.logoutConfirmOk') }}
            </button>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onBeforeUnmount, watch } from "vue";
import { storeToRefs } from "pinia";
import { useI18n } from "vue-i18n";
import { useRoute, useRouter } from "vue-router";
import { useCartStore } from "@/stores/cart";
import { useUserStore } from "@/stores/user";
import useChatStore from "@/stores/chat"
import { LOCALE_OPTIONS } from "@/locale";

const cartStore = useCartStore();
const { cartCount } = storeToRefs(cartStore);
const userStore = useUserStore(); // Initialize user store
const chatStore = useChatStore()
const { t, locale } = useI18n();
const route = useRoute();
const router = useRouter();

// Cart badge pulse animation
const cartBadgePulse = ref(false);
let previousCartCount = cartCount.value;

watch(cartCount, (newCount) => {
  if (newCount > previousCartCount) {
    cartBadgePulse.value = true;
    setTimeout(() => {
      cartBadgePulse.value = false;
    }, 600);
  }
  previousCartCount = newCount;
});

// Check if we're on the login page
const isLoginPage = computed(() => route.path === '/login');

// Auth computed
const isAuthenticated = computed(() => userStore.isAuthenticated);
const customerName = computed(() => userStore.profile?.tenKhachHang || userStore.profile?.tenTaiKhoan || 'User');

const showLogoutConfirm = ref(false);
function openLogoutConfirm() {
  showLogoutConfirm.value = true;
}
function cancelLogout() {
  showLogoutConfirm.value = false;
}
function confirmLogout() {
  userStore.logout();
  chatStore.resetState()
  router.push('/login');
  showLogoutConfirm.value = false;
}
// removed admin link

// Theme toggle removed

// Locale selector
const currentLocale = ref(locale.value);
const localeOptions = LOCALE_OPTIONS;

const changeLocale = (value: string) => {
  locale.value = value;
  localStorage.setItem('arco-locale', value);
};

const localeOpen = ref(false)
const currentLocaleLabel = computed(() => {
  const found = localeOptions.find(o => o.value === currentLocale.value)
  return found ? found.label : currentLocale.value
})
function toggleLocale() { localeOpen.value = !localeOpen.value }
function closeLocale() { localeOpen.value = false }
function selectLocale(val: string) {
  currentLocale.value = val
  changeLocale(val)
  closeLocale()
}
 
 // Search state and handler
 const searchQuery = ref('')
 function handleSearch() {
   const q = searchQuery.value.trim()
   if (!q) return
   // Navigate to the All products page with a query param
   // The products view can read this from the route to filter
   router.push({ path: '/all', query: { search: q } })
 }

// Sticky header on scroll
const isScrolled = ref(false)
function handleScroll() {
  isScrolled.value = window.scrollY > 20
}
onMounted(() => {
  userStore.initFromStorage()
  window.addEventListener('scroll', handleScroll, { passive: true })
})
onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped lang="scss">
header {
  display: flex;
  justify-content: center;
  flex-direction: column;
  align-items: center;
  padding: 0 40px 0;
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  transition: box-shadow 0.2s ease;
}

header.scrolled {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.topbar {
  width: 100%;
  max-width: 1016px;
  display: flex;
  justify-content: space-between;
  border-bottom: 1px solid #f0f0f0;
  padding: 6px 0;

  ul {
    display: flex;
    gap: 16px;
    list-style: none;
    margin: 0;
    padding: 0;
    align-items: center;

    li {
      display: flex;
      align-items: center;
    }

    li a {
      color: rgba(17, 17, 17, 0.8);
      text-decoration: none;
      font-size: 12px;
      line-height: 1;
    }
  }
}

.lookup-link {
  border: 1px solid rgba(17, 17, 17, 0.3);
  border-radius: 999px;
  padding: 6px 14px;
  font-weight: 600;
  transition: border-color 0.2s ease, background-color 0.2s ease, color 0.2s ease;
}

.lookup-link:hover,
.lookup-link:focus-visible {
  border-color: #111;
  background: #111;
  color: #fff;
  outline: none;
}

.customer-name {
  color: rgba(17, 17, 17, 0.8);
  font-size: 12px;
  font-weight: normal;
  line-height: 1;
  display: inline-block;
  vertical-align: middle;
}

.mainbar {
  width: 100%;
  max-width: 1016px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 12px 0 0;
  gap: 12px;
  flex-wrap: nowrap;
}

.logo-section {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
}

.brand-link { display: inline-flex; align-items: center; padding: 6px 10px; border-radius: 10px; text-decoration: none; color: inherit; border: 1px solid transparent; transition: background-color .15s ease, border-color .15s ease; }
.brand-link:hover { background: #f9fafb; border-color: #f0f0f0; }
.brand-link:focus-visible { outline: none; box-shadow: 0 0 0 2px #111 inset; }

.logo-brand {
  height: 40px;
  width: auto;
  object-fit: contain;
}

.inline-nav { display: flex; align-items: center; justify-content: center; flex: 1 1 auto; min-width: 0; }
.inline-nav ul { padding-left: 0; display: flex; align-items: center; gap: 0; list-style: none; margin: 0; }
.inline-nav ul li { display: inline-flex; align-items: center; text-transform: uppercase; letter-spacing: .1em; font-size: 13px; padding: 0 16px; border-left: 1px solid #ddd; border-right: 1px solid #ddd; position: relative; }
.inline-nav ul li:first-child { border-left: none; padding-left: 0; }
.inline-nav ul li:last-child { border-right: none; padding-right: 0; }
.inline-nav ul li a { 
        color: #111111;
        text-decoration: none;
  position: relative;
  transition: color 0.2s ease;
  padding: 4px 0;
  display: inline-block;
        }

.inline-nav ul li a::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: #111;
  transition: width 0.3s ease;
}

.inline-nav ul li a:hover { 
  color: rgba(17,17,17,0.8); 
      }

.inline-nav ul li a:hover::after {
  width: 100%;
}
/* locale moved to topbar */

.nav-btn {
  border: none;
  background: transparent;
}

.search { width: 220px; margin-left: auto; flex: 0 0 auto; }

/* Custom searchbar (no Arco) */
.searchbar { position: relative; display: flex; align-items: center; gap: 4px; border: 1px solid #e5e7eb; background: rgba(255,255,255,.85); backdrop-filter: blur(6px); border-radius: 16px; padding: 4px 6px; box-shadow: 0 2px 10px rgba(0,0,0,.04); transition: border-color .2s ease, box-shadow .2s ease, background-color .2s ease; height: 34px; overflow: hidden; }
.searchbar:focus-within { border-color: #111; box-shadow: 0 6px 20px rgba(0,0,0,.08); background: rgba(255,255,255,.95); }
.searchbar .icon { color: rgba(17,17,17,.55); display: inline-flex; }
.search-input { border: none; outline: none; background: transparent; flex: 1; font: inherit; font-size: 13px; color: #111; padding: 4px 2px; min-width: 0; }
.search-input::placeholder { color: rgba(17,17,17,.55); }
.search-btn { display: inline-flex; align-items: center; justify-content: center; border: 1px solid #111; background: linear-gradient(135deg,#111,#000); color: #fff; border-radius: 12px; width: 28px; height: 26px; cursor: pointer; transition: background-color .15s ease, border-color .15s ease, transform .08s ease, box-shadow .08s ease; flex-shrink: 0; }
.search-btn .arrow { transition: transform .2s ease; }
.search-btn:hover { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(0,0,0,.2); }
.search-btn:hover .arrow { transform: translateX(2px); }
.search-btn:active { transform: translateY(0) scale(0.95); box-shadow: 0 1px 4px rgba(0,0,0,.15); }
.sr-only { position: absolute; width: 1px; height: 1px; padding: 0; margin: -1px; overflow: hidden; clip: rect(0,0,0,0); white-space: nowrap; border: 0; }

.locale-select {
  width: 120px;
}

/* Locale bar styled like searchbar */
.locale-wrapper { position: relative; }
.locale-bar { display: flex; align-items: center; gap: 6px; border: 1px solid #e5e7eb; background: rgba(255,255,255,.85); backdrop-filter: blur(6px); border-radius: 20px; padding: 6px 12px; height: 36px; box-shadow: 0 2px 10px rgba(0,0,0,.04); transition: border-color .2s ease, box-shadow .2s ease, background-color .2s ease; cursor: pointer; }
.locale-bar:hover { box-shadow: 0 6px 20px rgba(0,0,0,.06); }
.locale-bar:focus-within { border-color: #111; box-shadow: 0 6px 20px rgba(0,0,0,.08); background: rgba(255,255,255,.95); }
.locale-bar .icon { color: rgba(17,17,17,.55); display: inline-flex; }
.locale-label { font-size: 13px; color: #111; }
.caret { display: inline-flex; color: rgba(17,17,17,.55); transition: transform .2s ease; }
.locale-bar.open .caret { transform: rotate(180deg); }

.locale-menu { position: absolute; left: 0; top: calc(100% + 8px); background: #fff; border: 1px solid #e5e7eb; border-radius: 16px; box-shadow: 0 12px 30px rgba(0,0,0,.12); padding: 8px; z-index: 10; display: flex; gap: 8px; }
.locale-chip { padding: 10px 14px; border-radius: 14px; font-size: 13px; color: #111; background: #f5f5f5; border: 1px solid #e8e8e8; cursor: pointer; transition: background-color .15s ease, transform .08s ease, border-color .15s ease, box-shadow .08s ease; }
.locale-chip:hover { background: #efefef; transform: translateY(-1px); box-shadow: 0 2px 8px rgba(0,0,0,.08); }
.locale-chip:active { transform: translateY(0) scale(0.96); }
.locale-chip.active { background: #111; color: #fff; border-color: #111; }

/* Menu open/close animation */
.locale-menu-enter-active, .locale-menu-leave-active { transition: opacity .18s ease, transform .18s ease; transform-origin: top right; }
.locale-menu-enter-from, .locale-menu-leave-to { opacity: 0; transform: scale(.96) translateY(-4px); }
.locale-menu-enter-to, .locale-menu-leave-from { opacity: 1; transform: scale(1) translateY(0); }

.logout-fade-enter-active,
.logout-fade-leave-active {
  transition: opacity 0.2s ease;
}

.logout-fade-enter-from,
.logout-fade-leave-to {
  opacity: 0;
}

.logout-fade-enter-active .logout-confirm-dialog,
.logout-fade-leave-active .logout-confirm-dialog {
  transition: transform 0.22s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.22s ease;
}

.logout-fade-enter-from .logout-confirm-dialog,
.logout-fade-leave-to .logout-confirm-dialog {
  transform: translateY(16px) scale(0.94);
  opacity: 0;
}

.logout-confirm-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1200;
  padding: 16px;
}

.logout-confirm-dialog {
  background: #fff;
  border-radius: 16px;
  max-width: 360px;
  width: 100%;
  padding: 24px;
  box-shadow: 0 16px 45px rgba(0, 0, 0, 0.18);
  text-align: center;

  h3 {
    margin: 0 0 8px 0;
    font-size: 20px;
    font-weight: 600;
    color: #111;
  }

  p {
    margin: 0 0 24px 0;
    color: #4e5969;
    font-size: 14px;
    line-height: 1.6;
  }
}

.dialog-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.dialog-btn {
  min-width: 120px;
  padding: 10px 18px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
  border: 1px solid transparent;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  }

  &.ghost {
    background: #fff;
    border-color: #d0d0d0;
    color: #111;
  }

  &.primary {
    background: #111;
    color: #fff;
  }
}

.carttotal {
  position: absolute;
  top: -12px;
  right: 7px; /* nudge towards the end of the word HÀNG */
  width: 18px;
  height: 18px;
  border-radius: 999px;
  background: #111;
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 10px;
  font-weight: 700;
  line-height: 1;
  box-shadow: 0 2px 6px rgba(0,0,0,.18);
  pointer-events: none;
  z-index: 1;
  transition: transform 0.3s ease;
}

.carttotal.pulse {
  animation: cartPulse 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes cartPulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.3);
  }
  100% {
    transform: scale(1);
  }
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
