import { LoginData, login as userLogin, logout as userLogout, getUserInfo } from '@/api/user'
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

        if (!token) {
          return false
        }

        // Fetch full user info from backend using the token
        const res = await getUserInfo()
        
        if (res.data) {
          // Set full user info including role information
          const userInfo = {
            id: res.data.id,
            maNhanVien: res.data.maNhanVien,
            name: res.data.tenNhanVien,
            tenTaiKhoan: res.data.tenTaiKhoan,
            email: res.data.email,
            idQuyenHan: res.data.idQuyenHan,
            tenQuyenHan: res.data.tenQuyenHan,
            accessToken: token,
            refreshToken: res.data.refreshToken,
          }
          this.setInfo(userInfo)
          
          // Update userId in localStorage in case it changed
          localStorage.setItem('userId', res.data.id.toString())
          
          return true
        }
        
        return false
      } catch (error) {
        // Token is invalid or expired, clear everything
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

        // Check if we have valid response with data
        if (!res || !res.data || !res.data.id) {
          throw new Error('Đăng nhập thất bại - không nhận được thông tin người dùng')
        }

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
      } catch {
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
