import { getToken, setToken, clearToken } from '@/utils/auth'
import axios from 'axios'
import { showMessageError, showMessageWarning } from '@/utils/message'

export interface HttpResponse<T = unknown> {
  success: boolean
  message: string
  data: T
}

// Set API base URL for backend from environment variable
const apiBaseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
axios.defaults.baseURL = apiBaseURL

// List of public endpoints that don't require authentication
type PublicEndpoint = {
  path: string
  methods?: string[]
}

const PUBLIC_ENDPOINTS: PublicEndpoint[] = [
  { path: '/api/san-pham-management/playlist' },
  { path: '/api/san-pham-management/paging' },
  { path: '/api/san-pham-management/detail' },
  { path: '/api/anh-san-pham-management' }, // allow product images without token
  { path: '/api/public' },
  { path: '/api/payment/vnpay/create' },
  { path: '/api/payment/vnpay/return' },
  { path: '/api/payment/vnpay/ipn' },
  // Guest order lookup endpoints (GET only)
  { path: '/api/hoa-don-management', methods: ['GET'] },
  { path: '/api/timeline-don-hang', methods: ['GET'] },
  { path: '/api/thong-tin-hoa-don-management', methods: ['GET'] },
]

const isPublicEndpoint = (url = '', method = 'GET'): boolean => {
  const upperMethod = method.toUpperCase()
  return PUBLIC_ENDPOINTS.some(endpoint => {
    if (!url.includes(endpoint.path)) return false
    if (!endpoint.methods?.length) return true
    return endpoint.methods.includes(upperMethod)
  })
}

axios.interceptors.request.use(
  (config: any) => {
    const method = (config.method || 'GET').toUpperCase()
    // Only add token for non-public endpoints
    if (!isPublicEndpoint(config.url || '', method)) {
      const token = getToken()
      if (token) {
        if (!config.headers) {
          config.headers = {}
        }
        config.headers.Authorization = `Bearer ${token}`
      }
    }
    // For public endpoints, don't send any token (even if one exists)
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Add response interceptors
axios.interceptors.response.use(
  (response: any) => {
    // Check for new token from server
    const newToken = response.headers['new-token']
    if (newToken) {
      setToken(newToken)
    }

    const res = response.data
    // Backend returns: { success: true/false, data: ..., message: "..." }
    // Note: Jackson serializes boolean 'isSuccess' field as 'success' (removes 'is' prefix)

    // Check if request was successful
    if (res.success === false) {
      showMessageError(res.message || 'Request failed')
      return Promise.reject(new Error(res.message || 'Request failed'))
    }

    // Return the actual data for successful requests
    // This matches the view project's interceptor behavior
    return res
  },
  async (error) => {
    // Handle network errors or other axios errors
    if (error.response) {
      const status = error.response.status
      const url = error.config?.url || ''
      const method = (error.config?.method || 'GET').toUpperCase()
      
      // Handle 401 Unauthorized (token expired or invalid)
      if (status === 401) {
        // If it's a public endpoint, clear any invalid token and retry
        if (isPublicEndpoint(url, method)) {
          // Clear invalid token that might be causing the issue
          clearToken()
          
          // Don't show error for public endpoints with 401 - just log it
          console.warn('401 on public endpoint, cleared token. Retrying without token...')
          
          // Retry the request without token
          return axios.request({
            ...error.config,
            headers: {
              ...error.config.headers,
              Authorization: undefined
            }
          })
        } else {
          // For protected endpoints, token has expired - force logout
          const token = getToken()
          if (token) {
            // Only logout if we had a token (avoid infinite loops)
            showMessageWarning('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.')
            
            // Clear token immediately
            clearToken()
            
            // Import and call logout from user store
            try {
              const { useUserStore } = await import('@/stores/user')
              const userStore = useUserStore()
              userStore.logout()
            } catch (err) {
              console.error('Error during logout:', err)
            }
            
            // Redirect to login page, preserving the intended destination
            try {
              const routerModule = await import('@/router')
              const router = routerModule.default
              const currentPath = router.currentRoute.value.fullPath
              if (currentPath !== '/login') {
                router.push({
                  path: '/login',
                  query: { redirect: currentPath }
                }).catch(() => {
                  // Ignore navigation errors (e.g., if already on login page)
                })
              }
            } catch (routerError) {
              // Fallback: use window.location if router import fails
              console.error('Error accessing router:', routerError)
              if (window.location.pathname !== '/login') {
                window.location.href = `/login?redirect=${encodeURIComponent(window.location.pathname)}`
              }
            }
          }
          
          // Reject with a clear error message
          return Promise.reject(new Error('Phiên đăng nhập đã hết hạn'))
        }
      }
      
      // Server responded with error status
      const errorMessage = error.response.data?.message || `Request failed with status ${status}`
      showMessageError(errorMessage)
    } else if (error.request) {
      // Network error
      showMessageError('Network error - unable to connect to server')
    } else {
      // Other error
      showMessageError(error.message || 'Request Error')
    }
    return Promise.reject(error)
  }
)

export default axios

