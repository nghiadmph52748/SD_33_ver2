// Detect platform and use appropriate base URL
// For iOS Simulator: localhost works (shares host network)
// For Android Emulator: 10.0.2.2 is the special IP for host machine
// For physical devices: Need actual network IP (e.g., 172.20.10.2 or your machine's IP)
import { Platform } from 'react-native'

const getDefaultBaseURL = () => {
  // Use environment variable if set
  if (process.env.API_BASE_URL) {
    return process.env.API_BASE_URL
  }

  // For iOS Simulator, use machine IP (more reliable than localhost)
  // You can also try 'http://localhost:8080' if machine IP doesn't work
  if (Platform.OS === 'ios') {
    return 'http://192.168.2.120:8080'
  }

  // For Android Emulator, use 10.0.2.2 (special IP that maps to host's localhost)
  if (Platform.OS === 'android') {
    return 'http://10.0.2.2:8080'
  }

  // Fallback: try common local network IPs
  // You can change this to your machine's actual IP address
  return 'http://172.20.10.2:8080'
}

const DEFAULT_API_BASE_URL = getDefaultBaseURL()
const DEFAULT_AI_BASE_URL = `${DEFAULT_API_BASE_URL}/api/ai`
const DEFAULT_WS_URL = DEFAULT_API_BASE_URL.replace('http://', 'ws://').replace('https://', 'wss://') + '/ws-chat'

const API_BASE_URL = process.env.API_BASE_URL ?? DEFAULT_API_BASE_URL
const AI_BASE_URL = process.env.AI_BASE_URL ?? DEFAULT_AI_BASE_URL
const WEB_SOCKET_URL = process.env.WEB_SOCKET_URL ?? DEFAULT_WS_URL

// Log connection info in development
if (process.env.NODE_ENV !== 'production') {
  console.log('API Configuration:')
  console.log('  Platform:', Platform.OS)
  console.log('  API Base URL:', API_BASE_URL)
  console.log('  AI Base URL:', AI_BASE_URL)
  console.log('  WebSocket URL:', WEB_SOCKET_URL)
}

export const env = {
  API_BASE_URL,
  AI_BASE_URL,
  WEB_SOCKET_URL,
}

export default env
