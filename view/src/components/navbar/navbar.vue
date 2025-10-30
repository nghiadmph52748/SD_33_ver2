<template>
  <div class="navbar">
    <div class="left-side">
      <a-space :size="12" align="center">
        <icon-menu-fold
          v-if="!topMenu && appStore.device === 'mobile'"
          style="font-size: 22px; cursor: pointer"
          @click="toggleDrawerMenu"
        />
        <Breadcrumb :items="breadcrumbItems" />
      </a-space>
    </div>
    <ul class="right-side">
      <li>
        <a-tooltip content="Trợ lý AI">
          <a-button class="nav-btn" type="outline" :shape="'circle'" @click="goAI">
            <template #icon>
              <icon-robot />
            </template>
          </a-button>
        </a-tooltip>
      </li>
      <li>
        <a-tooltip :content="$t('settings.search')">
          <a-button class="nav-btn" type="outline" :shape="'circle'" @click="openSearch">
            <template #icon>
              <icon-search />
            </template>
          </a-button>
        </a-tooltip>
      </li>
      <li>
        <a-tooltip :content="theme === 'light' ? $t('settings.navbar.theme.toDark') : $t('settings.navbar.theme.toLight')">
          <a-button class="nav-btn" type="outline" :shape="'circle'" @click="handleToggleTheme">
            <template #icon>
              <icon-moon-fill v-if="theme === 'dark'" />
              <icon-sun-fill v-else />
            </template>
          </a-button>
        </a-tooltip>
      </li>
      <li>
        <a-tooltip content="Tin nhắn">
          <div class="message-box-trigger">
            <a-badge :count="chatStore.totalUnreadCount" :dot="chatStore.totalUnreadCount > 0">
              <a-button class="nav-btn" type="outline" :shape="'circle'" @click="openChatDrawer">
                <icon-message />
              </a-button>
            </a-badge>
          </div>
        </a-tooltip>
      </li>
      <li>
        <a-tooltip :content="$t('settings.navbar.alerts')">
          <div class="message-box-trigger">
            <a-badge :count="notificationStore.totalUnread" :dot="notificationStore.totalUnread > 0">
              <a-button class="nav-btn" type="outline" :shape="'circle'" @click="setPopoverVisible">
                <icon-notification />
              </a-button>
            </a-badge>
          </div>
        </a-tooltip>
        <a-popover
          trigger="click"
          position="br"
          :arrow-style="{ display: 'none' }"
          :content-style="{ padding: 0, minWidth: '420px', maxWidth: '420px' }"
          content-class="notification-popover"
        >
          <div ref="refBtn" class="ref-btn"></div>
          <template #content>
            <message-box />
          </template>
        </a-popover>
      </li>
      <li>
        <a-tooltip :content="isFullscreen ? t('settings.navbar.screen.toExit') : t('settings.navbar.screen.toFull')">
          <a-button class="nav-btn" type="outline" :shape="'circle'" @click="toggleFullScreen">
            <template #icon>
              <icon-fullscreen-exit v-if="isFullscreen" />
              <icon-fullscreen v-else />
            </template>
          </a-button>
        </a-tooltip>
      </li>
      <li>
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
      <li>
        <a-tooltip :content="t('settings.title')">
          <a-button class="nav-btn" type="outline" :shape="'circle'" @click="setVisible">
            <template #icon>
              <icon-settings />
            </template>
          </a-button>
        </a-tooltip>
      </li>
      <li>
        <a-dropdown trigger="click">
          <a-space style="cursor: pointer">
            <a-avatar :size="32">{{ userInitials }}</a-avatar>
            <span style="color: var(--color-text-1); font-weight: 500">{{ userStore.name || 'User' }}</span>
          </a-space>
          <template #content>
            <div style="padding: 10px 12px; min-width: 220px">
              <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 8px">
                <a-avatar :size="40">{{ userInitials }}</a-avatar>
                <div>
                  <div style="font-weight: 600">{{ userStore.name }}</div>
                  <div style="font-size: 12px; color: var(--color-text-3)">{{ userStore.email }}</div>
                </div>
              </div>
              <a-divider style="margin: 8px 0" />
              <a-doption @click="goProfile">
                <a-space>
                  <icon-user />
                  <span>Hồ sơ cá nhân</span>
                </a-space>
              </a-doption>
              <a-doption @click="handleLogout">
                <a-space>
                  <icon-export />
                  <span>{{ t('messageBox.logout') }}</span>
                </a-space>
              </a-doption>
            </div>
          </template>
        </a-dropdown>
      </li>
    </ul>
  </div>

  <a-modal v-model:visible="searchVisible" :footer="false" :mask-closable="true" :width="560" title="Tìm kiếm nhanh">
    <a-input
      ref="searchInputRef"
      v-model="searchQuery"
      placeholder="Nhập từ khóa: tên menu, tính năng..."
      allow-clear
      @press-enter="selectFirstResult"
    />
    <div v-if="filteredResults.length === 0" style="padding: 12px; color: var(--color-text-3)">Không có kết quả</div>
    <a-list v-else :bordered="false" :split="true" style="max-height: 320px; overflow: auto; margin-top: 8px">
      <a-list-item v-for="item in filteredResults" :key="item.name" @click="handleSelect(item)" style="cursor: pointer">
        <a-space>
          <component :is="item.icon" v-if="item.icon" />
          <span>{{ item.title }}</span>
        </a-space>
      </a-list-item>
    </a-list>
  </a-modal>
</template>

<script lang="ts" setup>
import useUser from '@/hooks/user'
import { useAppStore, useUserStore } from '@/store'
import useNotificationStore from '@/store/modules/notification'
import useChatStore from '@/store/modules/chat'
import { Message } from '@arco-design/web-vue'
import { useDark, useFullscreen, useToggle } from '@vueuse/core'
import { computed, inject, ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter, useRoute } from 'vue-router'
import { LOCALE_OPTIONS } from '@/locale'
import { useAppStore as useStore } from '@/store'
import MessageBox from '../message-box/message-box.vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'

const { t, locale } = useI18n()
const currentLocale = ref(locale.value)
const localeOptions = LOCALE_OPTIONS

const appStore = useAppStore()
const userStore = useUserStore()
const notificationStore = useNotificationStore()
const chatStore = useChatStore()
const { logout } = useUser()
const router = useRouter()
const route = useRoute()
const { isFullscreen, toggle: toggleFullScreen } = useFullscreen()
const avatar = computed(() => {
  return userStore.avatar
})
const theme = computed(() => {
  return appStore.theme
})
const topMenu = computed(() => appStore.topMenu && appStore.menu)
// Breadcrumb items based on current route
const { breadcrumbItems } = useBreadcrumb()
const isDark = useDark({
  selector: 'body',
  attribute: 'arco-theme',
  valueDark: 'dark',
  valueLight: 'light',
  storageKey: 'arco-theme',
  onChanged(dark: boolean) {
    // overridden default behavior
    appStore.toggleTheme(dark)
  },
})
const toggleTheme = useToggle(isDark)
const handleToggleTheme = () => {
  toggleTheme()
}
const setVisible = () => {
  appStore.updateSettings({ globalSettings: true })
}
const refBtn = ref()
const setPopoverVisible = () => {
  const event = new MouseEvent('click', {
    view: window,
    bubbles: true,
    cancelable: true,
  })
  refBtn.value.dispatchEvent(event)
}
const handleLogout = () => {
  logout()
}
const userInitials = computed(() => {
  const n = (userStore.name || '').trim()
  if (!n) return 'U'
  const parts = n.split(' ')
  const first = parts[0]?.[0] || ''
  const last = parts[parts.length - 1]?.[0] || ''
  return (first + last).toUpperCase()
})
const goProfile = () => {
  router.push('/profile')
}
const switchRoles = async () => {
  const res = await userStore.switchRoles()
  Message.success(res as string)
}
const toggleDrawerMenu = inject('toggleDrawerMenu') as () => void
const switchGit = () => {
  window.open('https://github.com/zxwk1998/vue-admin-arco')
}
const open = (val: string) => {
  window.open(`https://vuejs-core.cn/${val}`)
}
const changeLocale = (value: string) => {
  locale.value = value
  localStorage.setItem('arco-locale', value)
  Message.success(t('navbar.action.locale'))
}

const openChatDrawer = () => {
  router.push('/chat')
}

const goAI = () => {
  router.push('/ai-chatbot/index')
}

// Keep outline style to avoid blue fill when active

// Quick search modal state
const searchVisible = ref(false)
const searchQuery = ref('')
const searchInputRef = ref()

const openSearch = () => {
  searchVisible.value = true
  setTimeout(() => {
    searchInputRef.value?.focus?.()
  }, 50)
}

type SearchItem = { name: string; path: string; title: string; icon?: any }
const allRoutes = router.getRoutes().filter((r) => r.meta && r.meta.locale)
const searchData: SearchItem[] = allRoutes.map((r) => ({
  name: String(r.name || r.path),
  path: r.path,
  title: t(String(r.meta?.locale || r.name || r.path)),
}))

const filteredResults = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return searchData.slice(0, 10)
  return searchData.filter((i) => i.title.toLowerCase().includes(q)).slice(0, 20)
})

const handleSelect = (item: SearchItem) => {
  searchVisible.value = false
  searchQuery.value = ''
  router.push(item.path)
}

const selectFirstResult = () => {
  const first = filteredResults.value[0]
  if (first) handleSelect(first)
}
onMounted(async () => {
  if (userStore.id) {
    // Fetch notifications and connect WebSocket
    await notificationStore.fetchNotifications()
    notificationStore.connectWebSocket()

    // Fetch unread count
    await chatStore.fetchUnreadCount()

    // Auto-connect WebSocket nếu chưa connect
    if (!chatStore.wsConnected && !chatStore.wsConnecting) {
      await chatStore.connectWebSocket()
    }

    // Fallback: Polling unread count mỗi 5s nếu WebSocket fail
    setInterval(async () => {
      if (!chatStore.wsConnected) {
        await chatStore.fetchUnreadCount()
        // Nếu đang mở trang chat, refresh conversations
        if (route.path === '/chat' && chatStore.conversations.length > 0) {
          await chatStore.fetchConversations()
        }
      }
    }, 5000)
  }
})
</script>

<style scoped lang="less">
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--color-bg-2);
  border-bottom: 1px solid var(--color-border);
  /* Let outer .layout-navbar (fixed) control offset/width */
  position: relative;
  height: 60px;
  width: 100%;
  z-index: 1;
}

.left-side {
  display: flex;
  align-items: center;
  padding-left: 20px;
  flex: 1;
  min-width: 0; /* allow children to shrink */

  :deep(.arco-breadcrumb) {
    max-width: 100%;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    display: block;
  }
}

.right-side {
  display: flex;
  padding-right: 20px;
  list-style: none;

  :deep(.locale-select) {
    border-radius: 20px;
  }

  li {
    display: flex;
    align-items: center;
    padding: 0 10px;
  }

  a {
    color: var(--color-text-1);
    text-decoration: none;
  }

  .nav-btn {
    border-color: rgb(var(--gray-2));
    color: rgb(var(--gray-8));
    font-size: 16px;
    transition:
      background-color 0.15s ease,
      box-shadow 0.15s ease,
      border-color 0.15s ease;
  }

  .nav-btn:hover,
  .nav-btn:focus {
    background-color: var(--color-fill-2);
    border-color: var(--color-border);
    color: var(--color-text-1);
    box-shadow: none;
  }

  :deep(.arco-btn-primary.nav-btn:hover),
  :deep(.arco-btn-primary.nav-btn:focus) {
    box-shadow: none;
  }

  .trigger-btn,
  .ref-btn {
    position: absolute;
    bottom: 14px;
  }

  .trigger-btn {
    margin-left: 14px;
  }
}
</style>

<style lang="less">
.notification-popover {
  .arco-popover-content {
    margin-top: 0;
    border-radius: 8px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  }
}

.arco-dropdown-list-wrapper {
  max-height: 100vh !important;
}
</style>
