// Detect platform and use appropriate base URL
// For iOS Simulator: localhost works (shares host network)
// For Android Emulator: 10.0.2.2 is the special IP for host machine
// For physical devices: Need actual network IP (e.g., 172.20.10.2 or your machine's IP)
import { Platform, NativeModules } from 'react-native'
import Constants from 'expo-constants'

const extractHostFromExpo = (): string | null => {
  const { SourceCode } = NativeModules as { SourceCode?: { scriptURL?: string } }
  const expoConfig = Constants.expoConfig as any
  const manifest = (Constants as any).manifest
  const manifest2 = (Constants as any).manifest2
  const expoGo = manifest2?.extra?.expoGo ?? {}

  const parseHost = (value?: string | null): string | null => {
    if (!value) return null

    const normalized = value
      .replace('exp://', 'http://')
      .replace('ws://', 'http://')
      .replace('wss://', 'https://')

    const afterProtocol = normalized.includes('://') ? normalized.split('://')[1] : normalized
    if (!afterProtocol) return null

    const hostPort = afterProtocol.split('/')[0]
    if (!hostPort) return null

    const host = hostPort.split(':')[0]
    if (!host) return null

    const isLoopback =
      host === 'localhost' || host === '127.0.0.1' || host === '[::1]' || host === '::1' || host === '0.0.0.0'

    if (Constants.isDevice && isLoopback) {
      return null
    }

    if (isLoopback && Platform.OS === 'android') {
      return '10.0.2.2'
    }

    if (isLoopback) {
      return 'localhost'
    }

    return host
  }

  const candidates = [
    SourceCode?.scriptURL,
    expoConfig?.hostUri,
    expoConfig?.debuggerHost,
    expoConfig?.developmentClient?.url,
    manifest?.hostUri,
    manifest?.debuggerHost,
    expoGo?.hostUri,
    expoGo?.debuggerHost,
  ]

  for (const candidate of candidates) {
    const host = parseHost(candidate)
    if (host) return host
  }

  return null
}

const getDefaultBaseURL = () => {
  const runtimeEnvBaseUrl = process.env.API_BASE_URL ?? process.env.EXPO_PUBLIC_API_BASE_URL
  if (runtimeEnvBaseUrl) {
    return runtimeEnvBaseUrl
  }

  const manifest = (Constants as any).manifest
  const manifest2 = (Constants as any).manifest2
  const extraCandidates = [
    ((Constants.expoConfig as any)?.extra ?? {}) as Record<string, unknown>,
    ((manifest?.extra as Record<string, unknown>) ?? {}) as Record<string, unknown>,
    ((manifest2?.extra as Record<string, unknown>) ?? {}) as Record<string, unknown>,
  ]

  for (const extra of extraCandidates) {
    if (!extra) continue
    if (typeof extra.API_BASE_URL === 'string') {
      return extra.API_BASE_URL
    }
    if (typeof extra.apiBaseUrl === 'string') {
      return extra.apiBaseUrl
    }
    const nestedApi = extra.api as Record<string, unknown> | undefined
    const nestedBaseUrl = nestedApi?.baseUrl
    if (typeof nestedBaseUrl === 'string') {
      return nestedBaseUrl
    }
  }

  const expoHost = extractHostFromExpo()
  if (expoHost) {
    return `http://${expoHost}:8080`
  }

  if (Platform.OS === 'ios') {
    return 'http://localhost:8080'
  }

  if (Platform.OS === 'android') {
    return 'http://10.0.2.2:8080'
  }

  return 'http://172.20.10.2:8080'
}

const DEFAULT_API_BASE_URL = getDefaultBaseURL()

const API_BASE_URL = process.env.API_BASE_URL ?? process.env.EXPO_PUBLIC_API_BASE_URL ?? DEFAULT_API_BASE_URL

// Log connection info in development
if (process.env.NODE_ENV !== 'production') {
  console.log('API Configuration:')
  console.log('  Platform:', Platform.OS)
  console.log('  API Base URL:', API_BASE_URL)
}

export const env = {
  API_BASE_URL,
}

export default env
