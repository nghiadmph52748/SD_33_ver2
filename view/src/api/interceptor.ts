import { useUserStore } from '@/store'
import { getToken, setToken } from '@/utils/auth'
import { Message, Modal } from '@arco-design/web-vue'
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

    // Check if request was successful
    if (!res.success) {
      Message.error({
        content: res.message || 'Request failed',
        duration: 5 * 1000,
      })
      return Promise.reject(new Error(res.message || 'Request failed'))
    }

    // Return the actual data for successful requests
    return res
  },
  (error) => {
    // Handle network errors or other axios errors
    if (error.response) {
      // Server responded with error status
      const errorMessage = error.response.data?.message || `Request failed with status ${error.response.status}`
      Message.error({
        content: errorMessage,
        duration: 5 * 1000,
      })
    } else if (error.request) {
      // Network error
      Message.error({
        content: 'Network error - unable to connect to server',
        duration: 5 * 1000,
      })
    } else {
      // Other error
      Message.error({
        content: error.message || 'Request Error',
        duration: 5 * 1000,
      })
    }
    return Promise.reject(error)
  }
)
