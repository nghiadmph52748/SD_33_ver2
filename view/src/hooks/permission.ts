import { RouteLocationNormalized, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store'

export default function usePermission() {
  const userStore = useUserStore()

  // Hàm đệ quy kiểm tra quyền cho route cha và con
  // `userRoles` có thể là một chuỗi hoặc mảng chuỗi
  function hasPermission(route: RouteLocationNormalized | RouteRecordRaw, userRoles: string | string[]): boolean {
    const allowedRoles = route.meta?.roles
    if (!allowedRoles || allowedRoles.includes('*')) return true

    const users = Array.isArray(userRoles) ? userRoles : [userRoles]
    // Nếu bất kỳ role người dùng nào có trong allowedRoles thì cho phép
    if (users.some((r) => allowedRoles.includes(r))) return true

    // Kiểm tra children nếu có
    if ('children' in route && route.children?.length) {
      return route.children.some((child) => hasPermission(child, users))
    }

    return false
  }


  return {
    accessRouter(route: RouteLocationNormalized | RouteRecordRaw) {
      // ⚡ Nếu route KHÔNG yêu cầu đăng nhập thì cho phép luôn
      if (route.meta?.requiresAuth === false) {
        return true
      }

      // Lấy roles hiện tại từ store; dùng getter `normalizedRoles` trả về mảng
      const currentRoles = (userStore as any).normalizedRoles || (userStore as any).roles || []

      // ⚡ Nếu route có yêu cầu đăng nhập thì kiểm tra quyền, bao gồm cả children
      return hasPermission(route, currentRoles)
    },

    findFirstPermissionRoute(_routers: any, role = 'admin') {
      const cloneRouters = [..._routers]
      while (cloneRouters.length) {
        const firstElement = cloneRouters.shift()
        if (
          firstElement?.meta?.roles?.includes('*') ||
          firstElement?.meta?.roles?.includes(role)
        ) {
          return { name: firstElement.name }
        }
        if (firstElement?.children) {
          cloneRouters.push(...firstElement.children)
        }
      }
      return null
    },
  }
}
