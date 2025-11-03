import axios from 'axios'

import env from '../config/env'
import tokenManager from './tokenManager'

const aiClient = axios.create({
  baseURL: env.AI_BASE_URL,
  timeout: 30000,
})

aiClient.interceptors.request.use((config) => {
  const token = tokenManager.getAccessToken()
  if (token) {
    if (!config.headers) {
      config.headers = {}
    }
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export default aiClient
