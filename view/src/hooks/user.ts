import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'

import { useUserStore } from '@/store'

export default function useUser() {
  const router = useRouter()
  const userStore = useUserStore()
  const logout = async (logoutTo?: string) => {
    await userStore.logout()
    const currentRoute = router.currentRoute.value
    Message.success('Đăng xuất thành công')

    // Lưu thông tin redirect vào sessionStorage thay vì URL (bảo mật)
    if (currentRoute.name && currentRoute.name !== 'login') {
      sessionStorage.setItem(
        'redirectAfterLogin',
        JSON.stringify({
          name: currentRoute.name,
          query: currentRoute.query,
          params: currentRoute.params,
        })
      )
    }

    router.push({
      name: logoutTo && typeof logoutTo === 'string' ? logoutTo : 'login',
    })
  }
  return {
    logout,
  }
}
