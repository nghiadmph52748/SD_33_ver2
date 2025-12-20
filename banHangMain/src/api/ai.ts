import axios from 'axios'
import { getToken } from '@/utils/auth'

const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const aiAxios = axios.create({
  baseURL: `${apiBase}/api/ai`,
  timeout: 30000,
})

aiAxios.interceptors.request.use((config: any) => {
  const token = getToken()
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export interface ChatRequest {
  message: string
  context?: string
  chat_history?: Array<{
    role: 'user' | 'assistant'
    content: string
    products?: Product[]  // Include for conversation context
  }>
}

export interface Product {
  id: number
  name: string
  min_price: number
  max_price: number
  image_url?: string
  stock: number
}

export interface ChatResponse {
  message: string
  sources: string
  queryType: string
  redirect_to_staff?: boolean
  products?: Product[]
}

export interface HealthResponse {
  lm_studio: string
  message: string
  model: string
  base_url: string
}

export async function chatWithAI(message: string): Promise<ChatResponse> {
  const response = await aiAxios.post<ChatResponse>('/chatbot/customer/chat', {
    message,
    context: '',
  })
  return response.data
}

export async function chatWithAIStream(
  message: string,
  onChunk: (text: string, metadata?: any) => void,
  onComplete: () => void,
  onError: (error: string) => void,
  chatHistory?: Array<{ role: 'user' | 'assistant'; content: string }>
): Promise<void> {
  try {
    const token = getToken()
    const response = await fetch(`${apiBase}/api/ai/chatbot/customer/chat-stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: token ? `Bearer ${token}` : '',
      },
      body: JSON.stringify({
        message,
        context: '',
        chat_history: chatHistory || []
      }),
    })

    console.log('[DEBUG API] chatWithAIStream called with history length:', chatHistory?.length)
    if (chatHistory && chatHistory.length > 0) {
      console.log('[DEBUG API] First item:', chatHistory[0])
    }

    if (!response.ok) {
      const errorText = await response.text()
      console.error('[AI Stream] HTTP error:', response.status, errorText)
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body?.getReader()
    const decoder = new TextDecoder()

    if (!reader) {
      throw new Error('No response body')
    }

    let buffer = ''
    let hasCompleted = false

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const chunk = decoder.decode(value, { stream: true })
      buffer += chunk

      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (line.startsWith('data:')) {
          try {
            const jsonStr = line.substring(5).trim()
            if (!jsonStr) continue
            const data = JSON.parse(jsonStr)

            if (data.type === 'start') {
              // Initial metadata (intent, data_source, etc.)
              onChunk('', data)
            } else if (data.type === 'suggestions') {
              // Follow-up suggestions / extra metadata
              onChunk('', data)
            } else if (data.type === 'content') {
              onChunk(data.content)
            } else if (data.type === 'end') {
              hasCompleted = true
              onComplete()
              return
            } else if (data.type === 'error') {
              console.error('[AI Stream] Error:', data.error)
              hasCompleted = true
              onError(data.error)
              return
            }
          } catch (e) {
            // ignore
          }
        }
      }
    }

    if (!hasCompleted) {
      onComplete()
    }
  } catch (error: any) {
    console.error('[AI Stream] Error:', error.message)
    onError(error.message || 'Unknown error')
  }
}

export async function checkAIHealth(): Promise<HealthResponse> {
  const response = await aiAxios.get<HealthResponse>('/chatbot/customer/health')
  return response.data
}

export default {
  chatWithAI,
  chatWithAIStream,
  checkAIHealth,
}
