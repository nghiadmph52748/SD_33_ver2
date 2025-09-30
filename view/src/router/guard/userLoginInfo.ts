import NProgress from 'nprogress' // progress bar
import type { LocationQueryRaw, Router } from 'vue-router'

import { useUserStore } from '@/store'
import { isLogin } from '@/utils/auth'

export default function setupUserLoginInfoGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    NProgress.start()
    const userStore = useUserStore()
    if (isLogin()) {
      if (userStore.id) {
        next()
      } else {
        // Thử khôi phục thông tin user từ token
        const restored = await userStore.initUserFromToken()
        if (restored) {
          next()
        } else {
          // Nếu không khôi phục được, logout
          await userStore.logout()
          // Lưu thông tin redirect vào sessionStorage thay vì URL
          if (to.name !== 'login') {
            sessionStorage.setItem(
              'redirectAfterLogin',
              JSON.stringify({
                name: to.name,
                query: to.query,
                params: to.params,
              })
            )
          }
          next({ name: 'login' })
        }
      }
    } else {
      if (to.name === 'login') {
        next()
        return
      }
      // Lưu thông tin redirect vào sessionStorage thay vì URL
      sessionStorage.setItem(
        'redirectAfterLogin',
        JSON.stringify({
          name: to.name,
          query: to.query,
          params: to.params,
        })
      )
      next({ name: 'login' })
    }
  })
}
