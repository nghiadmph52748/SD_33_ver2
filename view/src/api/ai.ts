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
 * Send chat message with streaming response
 * @param message - User message
 * @param onChunk - Callback for each text chunk
 * @param onComplete - Callback when stream completes
 * @param onError - Callback on error
 */
export async function chatWithAIStream(
  message: string,
  onChunk: (text: string, metadata?: any) => void,
  onComplete: () => void,
  onError: (error: string) => void
): Promise<void> {
  try {
    const token = getToken()
    const response = await fetch('http://localhost:8080/api/ai/chat-stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: token ? `Bearer ${token}` : '',
      },
      body: JSON.stringify({ message, context: '' }),
    })

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

      // Process complete lines
      const lines = buffer.split('\n')
      buffer = lines.pop() || '' // Keep incomplete line in buffer

      for (const line of lines) {
        // Handle both "data: " and "event: " lines (Spring SSE format)
        if (line.startsWith('data:')) {
          try {
            const jsonStr = line.substring(5).trim() // Remove "data:" prefix
            if (!jsonStr) continue

            const data = JSON.parse(jsonStr)

            if (data.type === 'start') {
              // Send metadata from start event
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
            // Ignore parse errors for incomplete chunks
          }
        }
        // Skip event name lines
        else if (line.startsWith('event:')) {
          continue
        }
      }
    }

    // If stream ended without explicit end event, still call onComplete
    if (!hasCompleted) {
      onComplete()
    }
  } catch (error: any) {
    console.error('[AI Stream] Error:', error.message)
    onError(error.message || 'Unknown error')
  }
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
  chatWithAIStream,
  checkAIHealth,
}
