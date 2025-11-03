import axios, { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios'

import { notifyError } from '../utils/notifications'
import tokenManager from './tokenManager'
import env from '../config/env'

export interface ApiEnvelope<T> {
  data: T
  message?: string
  success?: boolean
  isSuccess?: boolean
}

const client = axios.create({
  baseURL: env.API_BASE_URL,
  timeout: 20000,
  withCredentials: true,
})

client.interceptors.request.use((config: AxiosRequestConfig) => {
  const token = tokenManager.getAccessToken()
  if (token) {
    if (!config.headers) {
      config.headers = {}
    }
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

client.interceptors.response.use(
  async <T>(response: AxiosResponse<ApiEnvelope<T> | T>) => {
    const newToken = response.headers['new-token'] as string | undefined
    if (newToken) {
      await tokenManager.notifyTokenRefreshed(newToken)
    }

    const payload = response.data

    if (payload && typeof payload === 'object' && 'data' in payload) {
      const envelope = payload as ApiEnvelope<T>
      const successFlag = envelope.success ?? envelope.isSuccess
      if (successFlag === false) {
        const message = envelope.message ?? 'Yêu cầu thất bại'
        notifyError(message)
        throw new Error(message)
      }
      return envelope.data
    }

    if (payload && typeof payload === 'object') {
      const potentialSuccess = (payload as Record<string, unknown>).success
      if (potentialSuccess === false) {
        const message = (payload as Record<string, string>).message ?? 'Yêu cầu thất bại'
        notifyError(message)
        throw new Error(message)
      }
    }

    return payload as T
  },
  async (error: AxiosError<{ message?: string }>) => {
    const status = error.response?.status
    if (status === 401 || status === 403) {
      await tokenManager.notifyUnauthorized()
    }

    const message =
      error.response?.data?.message ||
      error.message ||
      (status ? `Yêu cầu thất bại với mã lỗi ${status}` : 'Yêu cầu thất bại')

    notifyError(message)

    return Promise.reject(new Error(message))
  }
)

export default client
