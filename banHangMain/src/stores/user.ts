import { defineStore } from 'pinia'
import { setToken, clearToken, getToken } from '@/utils/auth'
import { loginCustomer, getMe, refreshToken, type CustomerProfile, type ApiResponse, type LoginResponseData } from '@/api/auth'
import { showMessageError } from '@/utils/message'

type UserState = {
  isAuthenticated: boolean
  profile: CustomerProfile | null
  accessToken: string | null
  refreshToken: string | null
  loading: boolean
}

const REFRESH_KEY = 'customer_refresh_token'
const PROFILE_KEY = 'customer_profile'

function normalizeProfile(
  profile?: CustomerProfile | (CustomerProfile & { hoTen?: string }) | null
): CustomerProfile | null {
  if (!profile) return null
  const name = (profile as any).tenKhachHang || (profile as any).hoTen
  return {
    ...profile,
    tenKhachHang: name,
  }
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    isAuthenticated: !!getToken(),
    profile: null,
    accessToken: getToken(),
    refreshToken: localStorage.getItem(REFRESH_KEY),
    loading: false,
  }),
  getters: {
    isCustomer: (state): boolean => {
      const profile = state.profile as Record<string, any> | null
      return !!profile?.maKhachHang
    },
    id: (state): number | undefined => {
      return state.profile?.id
    },
    name: (state): string => {
      return state.profile?.tenKhachHang || ''
    },
  },
  actions: {
    async initFromStorage() {
      const token = getToken()
      const profileRaw = localStorage.getItem(PROFILE_KEY)
      this.accessToken = token
      this.isAuthenticated = !!token
      this.refreshToken = localStorage.getItem(REFRESH_KEY)
      if (profileRaw) {
        try {
          const parsed = JSON.parse(profileRaw)
          this.profile = normalizeProfile(parsed)
        } catch {
          this.profile = null
        }
      } else if (token) {
        try {
          const me = await getMe()
          if (!me.success) {
            throw new Error(me.message || 'Không thể lấy thông tin khách hàng')
          }
          this.profile = normalizeProfile(me.data)
          localStorage.setItem(PROFILE_KEY, JSON.stringify(this.profile))
        } catch (error: any) {
          // token invalid
          showMessageError(error.message || 'Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.')
          this.logout()
        }
      }
    },
    async login(email: string, password: string) {
      this.loading = true
      try {
        const res: ApiResponse<LoginResponseData> = await loginCustomer(email, password)
        if (!res.success) {
          throw new Error(res.message || 'Đăng nhập thất bại')
        }
        const { accessToken, refreshToken: rt, khachHang } = res.data
        setToken(accessToken)
        if (rt) localStorage.setItem(REFRESH_KEY, rt)
        const normalizedProfile = normalizeProfile(khachHang)
        localStorage.setItem(PROFILE_KEY, JSON.stringify(normalizedProfile))
        this.isAuthenticated = true
        this.accessToken = accessToken
        this.refreshToken = rt || null
        this.profile = normalizedProfile
        return true
      } finally {
        this.loading = false
      }
    },
    logout() {
      clearToken()
      localStorage.removeItem(REFRESH_KEY)
      localStorage.removeItem(PROFILE_KEY)
      
      // Clear AI chat sessions from localStorage (preserve in database)
      localStorage.removeItem('gearup_storefront_ai_sessions_v1')
      localStorage.removeItem('gearup_storefront_ai_session_names_v1')
      localStorage.removeItem('gearup_storefront_ai_chat_v1')
      
      this.isAuthenticated = false
      this.profile = null
      this.accessToken = null
      this.refreshToken = null
    },
    async refreshIfNeeded() {
      if (!this.refreshToken) return false
      try {
        const res = await refreshToken(this.refreshToken)
        if (!res.success) {
          throw new Error(res.message || 'Không thể làm mới token')
        }
        const newToken = res.data.accessToken
        setToken(newToken)
        this.accessToken = newToken
        return true
      } catch (error: any) {
        showMessageError(error.message || 'Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.')
        this.logout()
        return false
      }
    },
  },
})


