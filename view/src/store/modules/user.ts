import { LoginData, getUserInfo, login as userLogin, logout as userLogout } from '@/api/user'
import { clearToken, setToken } from '@/utils/auth'
import { removeRouteListener } from '@/utils/route-listener'
import { defineStore } from 'pinia'
import { Message } from '@arco-design/web-vue'
import useAppStore from './app'
import { UserState } from './user/types'

const useUserStore = defineStore('user', {
  state: (): UserState => ({
    id: undefined,
    maNhanVien: undefined,
    name: undefined,
    tenTaiKhoan: undefined,
    avatar: undefined,
    job: undefined,
    organization: undefined,
    location: undefined,
    email: undefined,
    introduction: undefined,
    personalWebsite: undefined,
    jobName: undefined,
    organizationName: undefined,
    locationName: undefined,
    phone: undefined,
    registrationDate: undefined,
    accountId: undefined,
    certification: undefined,
    role: 'admin',
    idQuyenHan: undefined,
    tenQuyenHan: undefined,
    accessToken: undefined,
    refreshToken: undefined,
  }),

  getters: {
    userInfo(state: UserState): UserState {
      return { ...state }
    },
  },

  actions: {
    switchRoles() {
      return new Promise((resolve) => {
        this.role = this.role === 'user' ? 'admin' : 'user'
        resolve(this.role)
      })
    },
    // Set user's information
    setInfo(partial: Partial<UserState>) {
      this.$patch(partial)
    },

    // Reset user's information
    resetInfo() {
      this.$reset()
    },

    // Get user's information
    async info() {
      const res = await getUserInfo()

      this.setInfo(res.data)
    },

    // Login
    async login(loginForm: LoginData) {
      try {
        const res = await userLogin(loginForm)

        // Set JWT tokens
        if (res.data.accessToken) {
          setToken(res.data.accessToken)
        }

        // Update user info
        const userInfo = {
          id: res.data.id,
          maNhanVien: res.data.maNhanVien,
          name: res.data.tenNhanVien,
          tenTaiKhoan: res.data.tenTaiKhoan,
          email: res.data.email,
          idQuyenHan: res.data.idQuyenHan,
          tenQuyenHan: res.data.tenQuyenHan,
          accessToken: res.data.accessToken,
          refreshToken: res.data.refreshToken,
          role: res.data.tenQuyenHan?.toLowerCase() === 'admin' ? 'admin' : 'user',
        }
      } catch (err) {
        clearToken()
        throw err
      }
    },
    logoutCallBack() {
      const appStore = useAppStore()
      this.resetInfo()
      clearToken()
      removeRouteListener()
      appStore.clearServerMenu()
    },
    // Logout - Call server API for synchronization
    async logout() {
      try {
        await userLogout()
        this.logoutCallBack()
      } catch (error) {
        // Even if server call fails, still logout client-side
        Message.warning('Không thể đồng bộ đăng xuất với máy chủ, đã đăng xuất trên thiết bị hiện tại')
        this.logoutCallBack()
      }
    },
  },
})

export default useUserStore
