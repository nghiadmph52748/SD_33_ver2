import axios from 'axios'
import { getToken } from '@/utils/auth'

// Create separate axios instance for AI service (bypass global interceptors)
const aiAxios = axios.create({
  baseURL: 'http://localhost:8080/api/ai',
  timeout: 30000,
})

// Add auth header
aiAxios.interceptors.request.use((config: any) => {
  const token = getToken()
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

const API_BASE_URL = 'http://localhost:8080/api/ai'

export interface ChatRequest {
  message: string
  context?: string
}

export interface ChatResponse {
  message: string
  sources: string
  queryType: string
}

export interface HealthResponse {
  lm_studio: string
  message: string
  model: string
  base_url: string
}

/**
 * Send chat message to AI assistant
 */
export async function chatWithAI(message: string): Promise<ChatResponse> {
  const response = await aiAxios.post<ChatResponse>('/chat', {
    message,
    context: '',
  })
  return response.data
}

/**
 * Check AI service health
 */
export async function checkAIHealth(): Promise<HealthResponse> {
  const response = await aiAxios.get<HealthResponse>('/health')
  return response.data
}

export default {
  chatWithAI,
  checkAIHealth,
}
