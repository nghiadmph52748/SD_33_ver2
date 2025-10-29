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
  onChunk: (text: string) => void,
  onComplete: () => void,
  onError: (error: string) => void
): Promise<void> {
  try {
    console.log('[AI Stream] Starting request for:', message)
    const token = getToken()
    const response = await fetch('http://localhost:8080/api/ai/chat-stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
      },
      body: JSON.stringify({ message, context: '' }),
    })

    console.log('[AI Stream] Response status:', response.status)
    
    if (!response.ok) {
      const errorText = await response.text()
      console.error('[AI Stream] Error response:', errorText)
      throw new Error(`HTTP error! status: ${response.status}, body: ${errorText}`)
    }

    const reader = response.body?.getReader()
    const decoder = new TextDecoder()

    if (!reader) {
      throw new Error('No response body')
    }

    console.log('[AI Stream] Starting to read stream...')
    let buffer = ''
    let eventCount = 0

    let hasCompleted = false
    
    while (true) {
      const { done, value } = await reader.read()
      if (done) {
        console.log('[AI Stream] 🏁 Stream ended, total events:', eventCount)
        break
      }

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
            eventCount++
            
            if (eventCount % 20 === 0) {
              console.log('[AI Stream] Received', eventCount, 'events')
            }
            
            if (data.type === 'content') {
              onChunk(data.content)
            } else if (data.type === 'end') {
              console.log('[AI Stream] ✅ Received END event, total:', eventCount)
              hasCompleted = true
              onComplete()
              return
            } else if (data.type === 'error') {
              console.error('[AI Stream] ❌ Error:', data.error)
              hasCompleted = true
              onError(data.error)
              return
            } else if (data.type === 'start') {
              console.log('[AI Stream] ▶️ Started, intent:', data.intent)
            } else {
              console.warn('[AI Stream] ⚠️ Unknown event type:', data.type, data)
            }
          } catch (e) {
            console.warn('[AI Stream] Parse error for line:', line, e)
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
      console.log('[AI Stream] ⚠️ Stream ended without END event, completing anyway')
      onComplete()
    }
  } catch (error: any) {
    console.error('[AI Stream] Exception:', error)
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
