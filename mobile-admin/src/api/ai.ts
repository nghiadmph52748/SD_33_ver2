import aiClient from './aiClient'

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

export const chatWithAI = (message: string) =>
  aiClient.post<AIChatResponse, AIChatResponse>('/chat', { message, context: '' })

export const checkAIHealth = () =>
  aiClient.get<AIHealthResponse, AIHealthResponse>('/health')
