import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import type { Router } from 'vue-router'
import { useUserStore } from '@/store'
import { isLogin } from '@/utils/auth'
import { getGiaoCa } from '@/api/giao-ca'

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

        // Shift workflow: only staff must have active shift to access other routes
        // Admin can navigate freely without shift check
        const isAdmin = userStore.idQuyenHan === 1
        const isGiaoCaRoute = to.name === 'giaoca' || to.path.includes('/lich-lam-viec/giao-ca')
        const isPublicRoute = ['login', 'forgot-password', 'reset-password'].includes(String(to.name))

        // Only enforce shift check for staff (non-admin) users
        if (!isAdmin && !isGiaoCaRoute && !isPublicRoute) {
          try {
            const res = await getGiaoCa()
            const list = res.data || res || []
            const userId = userStore.id
            const activeShift = list.find((s: any) => {
              const assignedId = (s.nguoiNhan && s.nguoiNhan.id) || s.nguoiNhanId
              const trangThaiCa = s.trangThaiCa || s.trangThai
              const isActive = trangThaiCa === 'Đang làm' || (trangThaiCa !== 'Hoàn tất' && !s.thoiGianKetThuc && s.thoiGianGiaoCa)
              return assignedId === userId && isActive
            })

            if (!activeShift) {
              // Staff has no active shift, redirect to giao-ca
              next({ name: 'giaoca' })
              return
            }
          } catch (err) {
            console.warn('Không thể kiểm tra ca làm việc, chuyển đến giao-ca', err)
            next({ name: 'giaoca' })
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
