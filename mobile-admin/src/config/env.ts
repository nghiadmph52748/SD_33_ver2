const DEFAULT_API_BASE_URL = 'http://172.20.10.2:8080'
const DEFAULT_AI_BASE_URL = `${DEFAULT_API_BASE_URL}/api/ai`
const DEFAULT_WS_URL = 'ws://172.20.10.2:8080/ws-chat'

const API_BASE_URL = process.env.API_BASE_URL ?? DEFAULT_API_BASE_URL
const AI_BASE_URL = process.env.AI_BASE_URL ?? DEFAULT_AI_BASE_URL
const WEB_SOCKET_URL = process.env.WEB_SOCKET_URL ?? DEFAULT_WS_URL

export const env = {
  API_BASE_URL,
  AI_BASE_URL,
  WEB_SOCKET_URL,
}

export default env
