import { LoginData, login as userLogin, logout as userLogout } from '@/api/user'
import { clearToken, setToken } from '@/utils/auth'
import { removeRouteListener } from '@/utils/route-listener'
import { defineStore } from 'pinia'
import useAppStore from './app'
import { UserState } from './user/types'

const useUserStore = defineStore('user', {
  state: (): UserState => ({
    id: undefined,
    maNhanVien: undefined,
    name: undefined,
    tenTaiKhoan: undefined,
    email: undefined,
    idQuyenHan: undefined,
    tenQuyenHan: undefined,
    accessToken: undefined,
    refreshToken: undefined,
    role: '',
  }),

  getters: {
    userInfo(state: UserState): UserState {
      return { ...state }
    },
  },

  actions: {
    // Set user's information
    setInfo(partial: Partial<UserState>) {
      this.$patch(partial)
    },

    // Reset user's information
    resetInfo() {
      this.$reset()
    },

    // Khôi phục thông tin user từ token khi refresh trang
    async initUserFromToken() {
      try {
        const token = localStorage.getItem('token')
        const userId = localStorage.getItem('userId')

        if (token && userId) {
          const parsedUserId = parseInt(userId, 10)

          // Khôi phục thông tin user từ localStorage (chỉ ID và token, các thông tin khác có thể load sau)
          this.setInfo({
            id: parsedUserId,
            accessToken: token,
          })
          return true
        }
        return false
      } catch (error) {
        clearToken()
        localStorage.removeItem('userId')
        this.resetInfo()
        return false
      }
    },

    // Login
    async login(loginForm: LoginData) {
      try {
        const res = await userLogin(loginForm)
        // Set JWT tokens
        if (res.data.accessToken) {
          setToken(res.data.accessToken)
        }

        // Chỉ lấy ID từ database (ID trong bảng nhân viên) - điều này quan trọng nhất
        const employeeId = res.data.id // ID thực trong bảng nhân viên từ DB

        // Lưu ID vào localStorage để khôi phục khi refresh
        localStorage.setItem('userId', employeeId.toString())

        // Lưu thông tin cần thiết vào store (bao gồm các thông tin bổ sung nhưng ID là quan trọng nhất)
        const userInfo = {
          id: employeeId, // ID chính từ bảng nhân viên - QUAN TRỌNG NHẤT
          maNhanVien: res.data.maNhanVien,
          name: res.data.tenNhanVien,
          tenTaiKhoan: res.data.tenTaiKhoan,
          email: res.data.email,
          idQuyenHan: res.data.idQuyenHan,
          tenQuyenHan: res.data.tenQuyenHan,
          accessToken: res.data.accessToken,
          refreshToken: res.data.refreshToken,
        }
        this.setInfo(userInfo)
      } catch (err) {
        clearToken()
        localStorage.removeItem('userId')
        throw err
      }
    },

    // Logout
    async logout() {
      try {
        await userLogout()
      } catch (error) {
        // Ignore server errors
      } finally {
        const appStore = useAppStore()
        this.resetInfo()
        clearToken()
        localStorage.removeItem('userId')
        removeRouteListener()
        appStore.clearServerMenu()
      }
    },
  },
})

export default useUserStore
