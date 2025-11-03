import { create } from 'zustand'

import { AuthUser, getCurrentUser, login, logout, LoginRequest } from '../api/auth'
import tokenManager from '../api/tokenManager'
import storage, { StoredUser } from '../utils/storage'

interface AuthState {
  user: AuthUser | StoredUser | null
  accessToken: string | null
  refreshToken: string | null
  isRestoring: boolean
  signIn: (payload: LoginRequest) => Promise<void>
  restoreSession: () => Promise<void>
  signOut: () => Promise<void>
  signOutSilently: () => Promise<void>
  updateAccessToken: (token: string | null) => Promise<void>
}

const mapToStoredUser = (user: AuthUser): StoredUser => ({
  id: user.id,
  tenNhanVien: user.tenNhanVien,
  tenTaiKhoan: user.tenTaiKhoan,
  email: user.email,
  idQuyenHan: user.idQuyenHan,
  tenQuyenHan: user.tenQuyenHan,
})

const useAuthStore = create<AuthState>((set, get) => {
  tokenManager.registerTokenUpdateListener(async (token) => {
    await get().updateAccessToken(token)
  })

  tokenManager.registerUnauthorizedListener(async () => {
    await get().signOutSilently()
  })

  return {
    user: null,
    accessToken: null,
    refreshToken: null,
    isRestoring: true,

    async restoreSession() {
      try {
        const [token, refreshToken, storedUser] = await Promise.all([
          storage.getToken(),
          storage.getRefreshToken(),
          storage.getUser(),
        ])

        if (!token) {
          set({ user: null, accessToken: null, refreshToken: null, isRestoring: false })
          tokenManager.clear()
          return
        }

        tokenManager.setAccessToken(token)
        tokenManager.setRefreshToken(refreshToken)

        set({
          user: storedUser,
          accessToken: token,
          refreshToken,
        })

        const currentUser = await getCurrentUser()
        const normalizedUser = {
          ...currentUser,
          accessToken: currentUser.accessToken || token,
          refreshToken: currentUser.refreshToken || refreshToken,
        }

        const userProfile = mapToStoredUser(normalizedUser)

        tokenManager.setAccessToken(normalizedUser.accessToken)
        tokenManager.setRefreshToken(normalizedUser.refreshToken ?? null)

        await storage.setSession(normalizedUser.accessToken ?? token, normalizedUser.refreshToken, userProfile)

        set({
          user: normalizedUser,
          accessToken: normalizedUser.accessToken ?? null,
          refreshToken: normalizedUser.refreshToken ?? null,
        })
      } catch (error) {
        await get().signOutSilently()
      } finally {
        set({ isRestoring: false })
      }
    },

    async signIn(payload: LoginRequest) {
      const authUser = await login(payload)
      const userProfile = mapToStoredUser(authUser)

      tokenManager.setAccessToken(authUser.accessToken)
      tokenManager.setRefreshToken(authUser.refreshToken)

      await storage.setSession(authUser.accessToken, authUser.refreshToken, userProfile)

      set({
        user: authUser,
        accessToken: authUser.accessToken,
        refreshToken: authUser.refreshToken,
      })
    },

    async signOut() {
      try {
        await logout()
      } catch (error) {
        // ignore, we'll still clear local session
      } finally {
        await get().signOutSilently()
      }
    },

    async signOutSilently() {
      tokenManager.clear()
      await storage.clearSession()
      set({ user: null, accessToken: null, refreshToken: null })
    },

    async updateAccessToken(token: string | null) {
      if (token) {
        tokenManager.setAccessToken(token)
      } else {
        tokenManager.setAccessToken(null)
      }
      await storage.setToken(token ?? null)
      set({ accessToken: token })
    },
  }
})

export default useAuthStore
