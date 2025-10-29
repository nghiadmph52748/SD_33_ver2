<script lang="tsx">
// @ts-nocheck
import { IconList, IconHome, IconFolder, IconSettings, IconUser, IconStar, IconMessage, IconRobot } from '@arco-design/web-vue/es/icon'
import { useAppStore } from '@/store'
import { openWindow, regexUrl } from '@/utils'
import { listenerRouteChange } from '@/utils/route-listener'
import { compile, computed, defineComponent, h, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import type { RouteMeta } from 'vue-router'
import { RouteRecordRaw, useRoute, useRouter } from 'vue-router'
import useMenuTree from './use-menu-tree'

export default defineComponent({
  emit: ['collapse'],
  setup() {
    const { t } = useI18n()
    const appStore = useAppStore()
    const router = useRouter()
    const route = useRoute()
    const { menuTree } = useMenuTree()

    const iconMap = {
      IconList,
      IconHome,
      IconFolder,
      IconSettings,
      IconUser,
      IconStar,
      IconMessage,
      IconRobot,
    }
    const collapsed = computed({
      get() {
        if (appStore.device === 'desktop') return appStore.menuCollapse
        return false
      },
      set(value: boolean) {
        appStore.updateSettings({ menuCollapse: value })
      },
    })

    const topMenu = computed(() => appStore.topMenu)
    const openKeys = ref<string[]>([])
    const selectedKey = ref<string[]>([])

    const goto = (item: RouteRecordRaw) => {
      // Open external link
      if (regexUrl.test(item.path)) {
        openWindow(item.path)
        selectedKey.value = [item.name as string]
        return
      }

      // Check if item has children and no component (parent route)
      if (item.children && item.children.length > 0) {
        // Find first navigable child route
        const firstNavigableChild = item.children.find((child) => child.component || (child.children && child.children.length > 0))
        if (firstNavigableChild) {
          // If child also has children, go to its first child
          if (firstNavigableChild.children && firstNavigableChild.children.length > 0) {
            const firstGrandChild = firstNavigableChild.children.find((grandChild) => grandChild.component)
            if (firstGrandChild) {
              router.push({
                name: firstGrandChild.name,
              })
              return
            }
          }
          // Navigate to the child route
          router.push({
            name: firstNavigableChild.name,
          })
          return
        }
      }

      // Eliminate external link side effects
      const { hideInMenu, activeMenu } = item.meta as RouteMeta
      if (route.name === item.name && !hideInMenu && !activeMenu) {
        selectedKey.value = [item.name as string]
        return
      }

      // Trigger router change
      router.push({
        name: item.name,
      })
    }
    const findMenuOpenKeys = (target: string) => {
      const result: string[] = []
      let isFind = false
      const backtrack = (item: RouteRecordRaw, keys: string[]) => {
        if (item.name === target) {
          isFind = true
          result.push(...keys)
          return
        }
        if (item.children?.length) {
          item.children.forEach((el) => {
            backtrack(el, [...keys, el.name as string])
          })
        }
      }
      menuTree.value.forEach((el: RouteRecordRaw) => {
        if (isFind) return // Performance optimization
        backtrack(el, [el.name as string])
      })
      return result
    }
    listenerRouteChange((newRoute) => {
      const { requiresAuth, activeMenu, hideInMenu } = newRoute.meta
      console.log('Route changed:', newRoute.name, { requiresAuth, activeMenu, hideInMenu })
      
      // Handle routes with activeMenu or requireAuth
      if (activeMenu || (requiresAuth && !hideInMenu)) {
        const menuOpenKeys = findMenuOpenKeys((activeMenu || newRoute.name) as string)
        console.log('Menu open keys:', menuOpenKeys)

        const keySet = new Set([...menuOpenKeys, ...openKeys.value])
        openKeys.value = [...keySet]

        // If activeMenu is specified, use it; otherwise use the first visible parent in the path
        if (activeMenu) {
          selectedKey.value = [activeMenu]
          console.log('Using activeMenu:', activeMenu)
        } else if (hideInMenu && menuOpenKeys.length > 0) {
          // For hidden routes, select the top-level parent menu item
          selectedKey.value = [menuOpenKeys[0]]
          console.log('Using first menu key:', menuOpenKeys[0])
        } else {
          selectedKey.value = [menuOpenKeys[menuOpenKeys.length - 1]]
          console.log('Using last menu key:', menuOpenKeys[menuOpenKeys.length - 1])
        }
        console.log('Final selected key:', selectedKey.value)
      }
    }, true)
    const setCollapse = (val: boolean) => {
      if (appStore.device === 'desktop') appStore.updateSettings({ menuCollapse: val })
    }

    const renderSubMenu = () => {
      function travel(_route: RouteRecordRaw[], nodes = []) {
        if (_route) {
          _route.forEach((element) => {
            // This is demo, modify nodes as needed
            const icon = element?.meta?.icon ? () => h(iconMap[element.meta.icon] || IconList) : null

            // Check if this route should be rendered as submenu or menu item
            const hasNavigableChildren =
              element?.children &&
              element?.children.length > 0 &&
              element.children.some((child) => child.component || (child.children && child.children.length > 0))

            const node = hasNavigableChildren ? (
              <a-sub-menu
                key={element?.name}
                v-slots={{
                  icon,
                  title: () => h(compile(t(element?.meta?.locale || ''))),
                }}
              >
                {travel(element?.children)}
              </a-sub-menu>
            ) : (
              <a-menu-item key={element?.name} v-slots={{ icon }} onClick={() => goto(element)}>
                {t(element?.meta?.locale || '')}
              </a-menu-item>
            )
            nodes.push(node as never)
          })
        }
        return nodes
      }
      return travel(menuTree.value)
    }

    return () => (
      <a-menu
        theme={appStore.theme === 'dark' ? 'dark' : 'light'}
        mode={topMenu.value ? 'horizontal' : 'vertical'}
        v-model:collapsed={collapsed.value}
        v-model:open-keys={openKeys.value}
        show-collapse-button={appStore.device !== 'mobile'}
        auto-open={false}
        selected-keys={selectedKey.value}
        auto-open-selected={true}
        level-indent={34}
        style='height: 100%;width:100%;'
        onCollapse={setCollapse}
      >
        {renderSubMenu()}
      </a-menu>
    )
  },
})
</script>

<style lang="less" scoped>
/* Global styles for menu hover - not scoped */
:deep(.arco-menu-inner) {
  .arco-menu-inline-header {
    display: flex;
    align-items: center;
  }
  .arco-icon {
    &:not(.arco-icon-down) {
      font-size: 18px;
    }
  }
}

/* ========================================= */
/* LIGHT MODE STYLES */
/* ========================================= */
::deep(.arco-menu-light) {
  .arco-menu-item,
  .arco-menu-inline-header {
    transition: background-color 0.15s ease, color 0.15s ease;
  }
  
  /* Hover state */
  .arco-menu-item:hover,
  .arco-menu-inline-header:hover {
    background: #e6f4ff !important;
    color: #1890ff !important;
  }
  
  /* Selected state */
  .arco-menu-selected,
  .arco-menu-selected > .arco-menu-inline-header {
    background: #bae7ff !important;
    color: #0050b3 !important;
    position: relative;
  }
  
  /* Selected item hover */
  .arco-menu-selected:hover,
  .arco-menu-selected:hover > .arco-menu-inline-header {
    background: #91d5ff !important;
    color: #003a8c !important;
  }
  
  /* Blue left border for selected items */
  .arco-menu-selected::before,
  .arco-menu-selected > .arco-menu-inline-header::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 3px;
    background: #1890ff;
    border-radius: 0 2px 2px 0;
  }
}

/* ========================================= */
/* DARK MODE STYLES */
/* ========================================= */
::deep(.arco-menu-dark) {
  .arco-menu-item,
  .arco-menu-inline-header {
    transition: background-color 0.15s ease, color 0.15s ease;
  }
  
  /* Hover state - darker blue for dark mode */
  .arco-menu-item:hover,
  .arco-menu-inline-header:hover {
    background: rgba(24, 144, 255, 0.15) !important;
    color: #69c0ff !important;
  }
  
  /* Selected state - more visible in dark mode */
  .arco-menu-selected,
  .arco-menu-selected > .arco-menu-inline-header {
    background: rgba(24, 144, 255, 0.25) !important;
    color: #91d5ff !important;
    position: relative;
  }
  
  /* Selected item hover - stronger highlight */
  .arco-menu-selected:hover,
  .arco-menu-selected:hover > .arco-menu-inline-header {
    background: rgba(24, 144, 255, 0.35) !important;
    color: #bae7ff !important;
  }
  
  /* Brighter blue border for dark mode */
  .arco-menu-selected::before,
  .arco-menu-selected > .arco-menu-inline-header::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 3px;
    background: #69c0ff;
    border-radius: 0 2px 2px 0;
  }
}

/* Alternative approach with higher specificity */
::deep(.arco-menu-light .arco-menu-item:hover) {
  background: #e6f4ff !important;
  color: #1890ff !important;
}

::deep(.arco-menu-light .arco-menu-selected) {
  background: #bae7ff !important;
  color: #0050b3 !important;
  position: relative;
}

::deep(.arco-menu-light .arco-menu-selected:hover) {
  background: #91d5ff !important;
  color: #003a8c !important;
}

::deep(.arco-menu-light .arco-menu-selected::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #1890ff;
  border-radius: 0 2px 2px 0;
}

::deep(.arco-menu-dark .arco-menu-item:hover) {
  background: rgba(24, 144, 255, 0.15) !important;
  color: #69c0ff !important;
}

::deep(.arco-menu-dark .arco-menu-selected) {
  background: rgba(24, 144, 255, 0.25) !important;
  color: #91d5ff !important;
  position: relative;
}

::deep(.arco-menu-dark .arco-menu-selected:hover) {
  background: rgba(24, 144, 255, 0.35) !important;
  color: #bae7ff !important;
}

::deep(.arco-menu-dark .arco-menu-selected::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #69c0ff;
  border-radius: 0 2px 2px 0;
}
</style>

<style lang="less">
/* ========================================= */
/* NON-SCOPED STYLES - LIGHT MODE */
/* ========================================= */
.arco-menu-light .arco-menu-item:hover {
  background: #e6f4ff !important;
  color: #1890ff !important;
}

.arco-menu-light .arco-menu-selected {
  background: #bae7ff !important;
  color: #0050b3 !important;
  position: relative;
}

.arco-menu-light .arco-menu-selected:hover {
  background: #91d5ff !important;
  color: #003a8c !important;
}

.arco-menu-light .arco-menu-selected::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #1890ff;
  border-radius: 0 2px 2px 0;
}

/* ========================================= */
/* NON-SCOPED STYLES - DARK MODE */
/* ========================================= */
.arco-menu-dark .arco-menu-item:hover {
  background: rgba(24, 144, 255, 0.15) !important;
  color: #69c0ff !important;
}

.arco-menu-dark .arco-menu-selected {
  background: rgba(24, 144, 255, 0.25) !important;
  color: #91d5ff !important;
  position: relative;
}

.arco-menu-dark .arco-menu-selected:hover {
  background: rgba(24, 144, 255, 0.35) !important;
  color: #bae7ff !important;
}

.arco-menu-dark .arco-menu-selected::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #69c0ff;
  border-radius: 0 2px 2px 0;
}
</style>
