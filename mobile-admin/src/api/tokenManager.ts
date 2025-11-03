let accessToken: string | null = null
let refreshToken: string | null = null

let onTokenUpdate: ((token: string | null) => Promise<void> | void) | null = null
let onUnauthorized: (() => Promise<void> | void) | null = null

export const tokenManager = {
  getAccessToken() {
    return accessToken
  },
  getRefreshToken() {
    return refreshToken
  },
  setAccessToken(token: string | null) {
    accessToken = token
  },
  setRefreshToken(token: string | null) {
    refreshToken = token
  },
  async notifyTokenRefreshed(token: string) {
    accessToken = token
    if (onTokenUpdate) {
      await onTokenUpdate(token)
    }
  },
  async notifyUnauthorized() {
    if (onUnauthorized) {
      await onUnauthorized()
    }
  },
  registerTokenUpdateListener(listener: (token: string | null) => Promise<void> | void) {
    onTokenUpdate = listener
  },
  registerUnauthorizedListener(listener: () => Promise<void> | void) {
    onUnauthorized = listener
  },
  clear() {
    accessToken = null
    refreshToken = null
  },
}

export default tokenManager
