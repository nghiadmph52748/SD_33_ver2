import aiClient from './aiClient'
import env from '../config/env'
import tokenManager from './tokenManager'

export interface AIChatResponse {
  message: string
  sources: string
  queryType: string
}

export interface AIHealthResponse {
  lm_studio: string
  message: string
  model: string
  base_url: string
}

export const chatWithAI = async (message: string): Promise<AIChatResponse> => {
  const res = await aiClient.post<AIChatResponse>('/chat', { message, context: '' })
  // Axios returns { data, status, ... }
  return (res as any).data ?? (res as any)
}

export const checkAIHealth = async (): Promise<AIHealthResponse> => {
  const res = await aiClient.get<AIHealthResponse>('/health')
  return (res as any).data ?? (res as any)
}

export async function chatWithAIStream(
  message: string,
  onChunk: (text: string, metadata?: any) => void,
  onComplete: () => void,
  onError: (error: string) => void
): Promise<void> {
  try {
    const token = tokenManager.getAccessToken()
    const response = await fetch(`${env.AI_BASE_URL}/chat-stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'text/event-stream',
        'Cache-Control': 'no-cache',
        Authorization: token ? `Bearer ${token}` : '',
      },
      body: JSON.stringify({ message, context: '' }),
    })

    if (!response.ok) {
      const errorText = await response.text().catch(() => '')
      throw new Error(`HTTP ${response.status}: ${errorText}`)
    }

    // If body is not a stream, fallback immediately
    const reader = response.body?.getReader?.()
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
          const jsonStr = line.substring(5).trim()
          if (!jsonStr) continue
          try {
            const data = JSON.parse(jsonStr)
            if (data.type === 'start') {
              onChunk('', data)
            } else if (data.type === 'content') {
              onChunk(data.content)
            } else if (data.type === 'end') {
              hasCompleted = true
              onComplete()
              return
            } else if (data.type === 'error') {
              hasCompleted = true
              onError(data.error || 'Stream error')
              return
            }
          } catch {}
        }
      }
    }
    if (!hasCompleted) onComplete()
  } catch (e: any) {
    // Fallback to non-streaming
    try {
      const res = await aiClient.post<AIChatResponse>('/chat', {
        message,
        context: '',
      })
      const body: any = (res as any).data ?? res
      if (body?.message) {
        // Simulate streaming: chunk the string into small pieces
        const text = body.message
        const step = 3
        for (let i = 0; i < text.length; i += step) {
          onChunk(text.slice(i, i + step))
          // Small delay to emulate streaming
          // eslint-disable-next-line no-await-in-loop
          await new Promise((r) => setTimeout(r, 20))
        }
      }
      onComplete()
    } catch (ee: any) {
      onError(ee?.message || e?.message || 'Unknown error')
    }
  }
}
