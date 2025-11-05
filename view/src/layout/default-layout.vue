<template>
  <a-layout class="layout" :class="{ mobile: appStore.hideMenu }">
    <div v-if="navbar" class="layout-navbar" :style="navbarStyle">
      <NavBar />
    </div>
    <a-layout>
      <a-layout>
        <a-layout-sider
          v-if="renderMenu"
          v-show="!hideMenu"
          class="layout-sider"
          :breakpoint="'lg'"
          :collapsible="true"
          :width="menuWidth"
          :style="{ paddingTop: navbar ? '60px' : '' }"
          :hide-trigger="true"
          @collapse="setCollapsed"
        >
          <div class="menu-wrapper">
            <div class="left-side">
              <a-space>
                <img
                  alt="logo"
                  src="//p3-armor.byteimg.com/tos-cn-i-49unhts6dw/dfdba5317c0c20ce20e64fac803d52bc.svg~tplv-49unhts6dw-image.image"
                />
                <a-typography-title>GearUp</a-typography-title>
              </a-space>
            </div>
            <Menu />
          </div>
        </a-layout-sider>
        <a-drawer
          v-if="hideMenu"
          :visible="drawerVisible"
          placement="left"
          :footer="false"
          mask-closable
          :closable="false"
          @cancel="drawerCancel"
        >
          <Menu />
        </a-drawer>
        <a-layout class="layout-content" :style="paddingStyle">
          <!-- <TabBar v-if="appStore.tabBar" /> -->
          <a-layout-content>
            <PageLayout />
          </a-layout-content>
          <Footer v-if="footer" />

          <!-- Floating Chat Button (ẩn khi đang ở trang /chat) -->
          <transition name="fade">
            <FloatingChatButton v-if="userStore.id && !isOnChatPage && !isOnAIChatbotPage" />
          </transition>
        </a-layout>
      </a-layout>
    </a-layout>
  </a-layout>
</template>

<script lang="ts" setup>
import Footer from '@/components/footer/footer.vue'
import Menu from '@/components/menu/menu.vue'
import NavBar from '@/components/navbar/navbar.vue'
import FloatingChatButton from '@/components/chat/FloatingChatButton.vue'
import usePermission from '@/hooks/permission'
import useResponsive from '@/hooks/responsive'
import { useAppStore, useUserStore } from '@/store'
import { computed, onMounted, provide, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageLayout from './page-layout.vue'

const isInit = ref(false)
const appStore = useAppStore()
const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

// Ẩn floating chat button khi đang ở bất kỳ trang chat nào (/chat, /chat/index, /chat/:id)
const isOnChatPage = computed(() => route.path.startsWith('/chat'))
// Ẩn các floating button khi đang ở trang trợ lý AI toàn màn hình
const isOnAIChatbotPage = computed(() => route.path.startsWith('/ai-chatbot'))
const permission = usePermission()
useResponsive(true)
const navbarHeight = `60px`
const navbar = computed(() => appStore.navbar)
const renderMenu = computed(() => appStore.menu && !appStore.topMenu)
const hideMenu = computed(() => appStore.hideMenu)
const footer = computed(() => appStore.footer)
const menuWidth = computed(() => {
  return appStore.menuCollapse ? 48 : appStore.menuWidth
})
const paddingStyle = computed(() => {
  // Offset content relative to fixed sider/navbar using margins (no inner padding)
  const offsetLeft = renderMenu.value && !hideMenu.value ? { marginLeft: `${menuWidth.value}px` } : {}
  const offsetTop = navbar.value ? { marginTop: navbarHeight } : {}
  return { ...offsetLeft, ...offsetTop }
})
// Navbar dynamic offset to avoid overlapping the sider
const navbarStyle = computed(() => {
  const left = renderMenu.value && !hideMenu.value ? menuWidth.value : 0
  return {
    left: `${left}px`,
    right: '0', // stretch to the right to avoid rounding gaps
  }
})
const setCollapsed = (val: boolean) => {
  if (!isInit.value) return // for page initialization menu state problem
  appStore.updateSettings({ menuCollapse: val })
}
watch(
  () => userStore.role,
  (roleValue) => {
    if (roleValue && !permission.accessRouter(route)) router.push({ name: 'notFound' })
  }
)
const drawerVisible = ref(false)
const drawerCancel = () => {
  drawerVisible.value = false
}
provide('toggleDrawerMenu', () => {
  drawerVisible.value = !drawerVisible.value
})
onMounted(() => {
  isInit.value = true
})
</script>

<style scoped lang="less">
@nav-size-height: 60px;
@layout-max-width: 1100px;

.layout {
  width: 100%;
  height: 100%;

  .layout-sider {
    background: var(--color-menu-dark-bg);
    position: fixed;
    top: 0;
    left: 0;
    z-index: 99;
    height: 100%;
    padding-top: 0 !important;

    .left-side {
      display: flex;
      align-items: center;
      padding-left: 8px;
      background: var(--color-menu-dark-bg);
      height: 60px;
      transition: background-color 0.15s ease;

      .arco-typography {
        color: #fff;
        font-size: 18px;
        width: 200px;
      }

      &:hover {
        background: fade(#fff, 6%);
      }
    }

    &::after {
      position: absolute;
      top: 0;
      right: -1px;
      display: block;
      width: 1px;
      height: 100%;
      background-color: var(--color-border);
      content: '';
    }

    > :deep(.arco-layout-sider-children) {
      overflow-y: hidden;
      top: 0;
    }

    .menu-wrapper {
      height: 100%;
      overflow: auto;
      overflow-x: hidden;

      :deep(.arco-menu) {
        height: calc(100% - 60px) !important;

        ::-webkit-scrollbar {
          width: 12px;
          height: 4px;
        }

        ::-webkit-scrollbar-thumb {
          border: 4px solid transparent;
          background-clip: padding-box;
          border-radius: 7px;
          background-color: var(--color-text-4);
        }

        ::-webkit-scrollbar-thumb:hover {
          background-color: var(--color-text-3);
        }
      }
    }
  }

  .layout-content {
    min-width: 0;
    min-height: 100vh;
    overflow-y: hidden;
    background-color: var(--color-fill-2);
    transition: all 0.2s cubic-bezier(0.34, 0.69, 0.1, 1);
  }

  .layout-navbar {
    transition: all 0.2s cubic-bezier(0.34, 0.69, 0.1, 1);
    position: fixed;
    top: 0;
    z-index: 100;
    min-width: 0;
    height: @nav-size-height;
    /* left and width are controlled via inline navbarStyle */
  }

  .arco-layout-sider-collapsed {
    .left-side {
      width: 50px;

      .arco-typography {
        color: transparent;
      }
    }
  }
}

/* Light theme adjustments for menu colors */
body:not([arco-theme='dark']) {
  .layout {
    .layout-sider {
      background: var(--color-bg-2);

      .left-side {
        background: var(--color-bg-2);

        .arco-typography {
          color: var(--color-text-1);
        }

        &:hover {
          background: var(--color-fill-2);
        }
      }
    }
  }
}

/* Responsive tweaks */
@media (max-width: 1200px) {
  .layout {
    .layout-content {
      padding-left: 0 !important;
    }
  }
}

@media (max-width: 992px) {
  .layout {
    .layout-sider {
      width: 220px !important;
    }
  }
}

@media (max-width: 768px) {
  .layout {
    .layout-sider {
      display: none;
    }
  }
}

:deep(.arco-menu-vertical .arco-menu-inner, .arco-menu-inner) {
  scrollbar-width: none !important;
}

/* Floating chat button fade animation */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
