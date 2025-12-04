import router from '@/router'
import pinia, { useUserStore } from '@/store'
import { getToken, setToken } from '@/utils/auth'
import { hasErrorBeenNotified, mapApiErrorToMessage, markErrorAsNotified } from '@/utils/api-errors'
import { Message } from '@arco-design/web-vue'
import axios from 'axios'

export interface HttpResponse<T = unknown> {
  success: boolean
  message: string
  data: T
}

// Set API base URL for backend
axios.defaults.baseURL = 'http://localhost:8080'

axios.interceptors.request.use(
  (config: any) => {
    // let each request carry token
    // this example using the JWT token
    // Authorization is a custom headers key
    // please modify it according to the actual situation
    const token = getToken()
    if (token) {
      if (!config.headers) {
        config.headers = {}
      }
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    // do something
    return Promise.reject(error)
  }
)
let isHandlingAuthError = false

const handleAuthError = async (message?: string) => {
  if (isHandlingAuthError) return
  isHandlingAuthError = true
  const userStore = useUserStore(pinia)
  await userStore.logout().catch(() => {})
  Message.warning({
    content: message || 'Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại',
    duration: 4000,
  })
  const currentPath = router.currentRoute.value?.fullPath || '/'
  await router
    .replace({
      name: 'login',
      query: { redirect: currentPath },
    })
    .catch(() => {})
  // Force full reload to reset entire state and view
  window.location.reload()
  isHandlingAuthError = false
}

// add response interceptors
axios.interceptors.response.use(
  (response: any) => {
    // Kiểm tra xem có token mới từ server không
    const newToken = response.headers['new-token']
    if (newToken) {
      // Cập nhật token mới vào localStorage
      setToken(newToken)
    }

    const res = response.data
    // Backend returns: { data: ..., message: "...", success: true/false }
    // Note: Jackson serializes boolean 'isSuccess' field as 'success' (removes 'is' prefix)

    // Check if request was successful
    if (res.success === false) {
      const pseudoError: any = {
        response: {
          status: response.status,
          data: {
            message: res.message,
          },
        },
      }
      const friendlyMessage = mapApiErrorToMessage(pseudoError, { defaultMessage: 'Yêu cầu thất bại.' })
      if (!hasErrorBeenNotified(pseudoError)) {
        Message.error({
          content: friendlyMessage,
          duration: 5 * 1000,
        })
        markErrorAsNotified(pseudoError)
      }
      return Promise.reject(new Error(friendlyMessage))
    }

    // Return the actual data for successful requests
    return res
  },
  async (error) => {
    if (error?.response?.status === 401) {
      await handleAuthError(error.response.data?.message)
      return Promise.reject(error)
    }

    const shouldShowGlobalMessage = error?.config?.skipGlobalErrorMessage !== true
    const friendlyMessage = mapApiErrorToMessage(error)

    if (shouldShowGlobalMessage && !hasErrorBeenNotified(error)) {
      Message.error({
        content: friendlyMessage,
        duration: 5 * 1000,
      })
      markErrorAsNotified(error)
    }

    return Promise.reject(error)
  }
)
