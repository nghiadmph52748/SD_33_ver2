import AsyncStorage from '@react-native-async-storage/async-storage'

const TOKEN_KEY = 'gearup::token'
const REFRESH_TOKEN_KEY = 'gearup::refreshToken'
const USER_KEY = 'gearup::user'

export interface StoredUser {
  id: number
  tenNhanVien?: string
  tenTaiKhoan?: string
  email?: string
  idQuyenHan?: number
  tenQuyenHan?: string
}

export const storage = {
  async setSession(token: string, refreshToken?: string | null, user?: StoredUser | null) {
    await Promise.all([
      storage.setToken(token),
      storage.setRefreshToken(refreshToken ?? null),
      storage.setUser(user ?? null),
    ])
  },

  async setToken(token: string | null) {
    if (!token) {
      await AsyncStorage.removeItem(TOKEN_KEY)
      return
    }
    await AsyncStorage.setItem(TOKEN_KEY, token)
  },

  async setRefreshToken(token: string | null) {
    if (!token) {
      await AsyncStorage.removeItem(REFRESH_TOKEN_KEY)
      return
    }
    await AsyncStorage.setItem(REFRESH_TOKEN_KEY, token)
  },

  async setUser(user: StoredUser | null) {
    if (!user) {
      await AsyncStorage.removeItem(USER_KEY)
      return
    }
    await AsyncStorage.setItem(USER_KEY, JSON.stringify(user))
  },

  async getToken() {
    return AsyncStorage.getItem(TOKEN_KEY)
  },

  async getRefreshToken() {
    const token = await AsyncStorage.getItem(REFRESH_TOKEN_KEY)
    return token || null
  },

  async getUser(): Promise<StoredUser | null> {
    const raw = await AsyncStorage.getItem(USER_KEY)
    if (!raw) return null
    try {
      return JSON.parse(raw) as StoredUser
    } catch (error) {
      await AsyncStorage.removeItem(USER_KEY)
      return null
    }
  },

  async clearSession() {
    await AsyncStorage.multiRemove([TOKEN_KEY, REFRESH_TOKEN_KEY, USER_KEY])
  },
}

export default storage
