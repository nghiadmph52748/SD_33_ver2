import { getToken, setToken, clearToken } from '@/utils/auth'
import axios from 'axios'
import { showMessageError } from '@/utils/message'

export interface HttpResponse<T = unknown> {
  success: boolean
  message: string
  data: T
}

// Set API base URL for backend from environment variable
const apiBaseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
axios.defaults.baseURL = apiBaseURL

// List of public endpoints that don't require authentication
const PUBLIC_ENDPOINTS = [
  '/api/san-pham-management/playlist',
  '/api/san-pham-management/paging',
  '/api/san-pham-management/detail',
  '/api/anh-san-pham-management', // allow product images without token
  '/api/public',
  '/api/payment/vnpay/create',
  '/api/payment/vnpay/return',
  '/api/payment/vnpay/ipn',
]

const isPublicEndpoint = (url: string): boolean => {
  return PUBLIC_ENDPOINTS.some(endpoint => url.includes(endpoint))
}

axios.interceptors.request.use(
  (config: any) => {
    // Only add token for non-public endpoints
    if (!isPublicEndpoint(config.url || '')) {
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
  (error) => {
    // Handle network errors or other axios errors
    if (error.response) {
      const status = error.response.status
      const url = error.config?.url || ''
      
      // If it's a 401 on a public endpoint, clear any invalid token and retry
      if (status === 401 && isPublicEndpoint(url)) {
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

