import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import type { Router } from 'vue-router'
import { useUserStore } from '@/store'
import { isLogin } from '@/utils/auth'

export default function setupUserLoginInfoGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    NProgress.start()
    const userStore = useUserStore()

    try {
      if (isLogin()) {
        //  Khôi phục userStore nếu chưa có
        if (!userStore.id) {
          const restored = await userStore.initUserFromToken()
          console.log(' Kết quả khôi phục user:', restored, '→ roles (Proxy):', userStore.roles)
        }

        //  Dùng getter normalizedRoles
        const userRoles = userStore.normalizedRoles
        const routeRoles: string[] = (to.meta?.roles || []).map((r) => r.toLowerCase())

        console.log(' Roles hiện tại (user):', userRoles)
        console.log(' Roles được yêu cầu (route):', routeRoles)

        //  Nếu không có roles -> logout
        if (!userRoles || userRoles.length === 0) {
          await userStore.logout()
          next({ name: 'login' })
          return
        }

        //  Kiểm tra quyền truy cập
        if (routeRoles.length > 0) {
          const hasAccess = routeRoles.includes('*') || routeRoles.some((r) => userRoles.includes(r))
          if (!hasAccess) {
            console.warn(' Truy cập bị chặn — không đủ quyền!')
            next({ name: '403' })
            return
          }
        }

        next()
        return
      }

      // Nếu chưa login
      const publicPages = ['login', 'forgot-password', 'reset-password']
      if (publicPages.includes(String(to.name))) {
        next()
        return
      }

      // Lưu redirect
      sessionStorage.setItem(
        'redirectAfterLogin',
        JSON.stringify({
          name: to.name,
          query: to.query,
          params: to.params,
        })
      )
      next({ name: 'login' })
    } finally {
      NProgress.done()
    }
  })
}
