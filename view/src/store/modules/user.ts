import { LoginData, login as userLogin, logout as userLogout } from '@/api/user'
import { clearToken, setToken } from '@/utils/auth'
import { removeRouteListener } from '@/utils/route-listener'
import { defineStore } from 'pinia'
import useAppStore from './app'
import { UserState, RoleType } from './user/types'

const USER_INFO_STORAGE_KEY = 'userInfo'
const USER_ID_STORAGE_KEY = 'userId'
const ROLES_STORAGE_KEY = 'roles'
const TOKEN_STORAGE_KEY = 'token'

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
    roles: [], // 🔹 dùng array cho nhiều quyền
  }),

  getters: {
  userInfo(state: UserState): UserState {
    return { ...state }
  },
  primaryRole(state: UserState): string {
    return state.roles[0] || ''
  },
  normalizedRoles(state): string[] {
    // Luôn trả về array thuần, lowercase
    return (state.roles || []).map(r => r.toLowerCase())
  },
}
,

  actions: {
    setInfo(partial: Partial<UserState>) {
      this.$patch(partial)
    },

    resetInfo() {
      this.$reset()
    },

    // Khôi phục thông tin user từ token khi refresh trang
    async initUserFromToken() {
      try {
        const token = localStorage.getItem(TOKEN_STORAGE_KEY)
        const userId = localStorage.getItem(USER_ID_STORAGE_KEY)
        const roles = JSON.parse(localStorage.getItem(ROLES_STORAGE_KEY) || '[]') as RoleType[]
        const storedInfoRaw = localStorage.getItem(USER_INFO_STORAGE_KEY)
        const storedInfo = storedInfoRaw ? (JSON.parse(storedInfoRaw) as Partial<UserState>) : {}

        if (token && userId) {
          const parsedUserId = Number.parseInt(userId, 10)
          const mergedInfo: Partial<UserState> = {
            id: parsedUserId,
            accessToken: token,
            roles,
            ...storedInfo,
          }
          this.setInfo(mergedInfo)
          return true
        }

        return false
      } catch (err) {
        clearToken()
        localStorage.removeItem(USER_ID_STORAGE_KEY)
        localStorage.removeItem(ROLES_STORAGE_KEY)
        localStorage.removeItem(USER_INFO_STORAGE_KEY)
        this.resetInfo()
        return false
      }
    },

    // Login
    // Trong phần actions của useUserStore

async login(loginForm: LoginData) {
  try {
    const res = await userLogin(loginForm)

    if (res.data.accessToken) {
      setToken(res.data.accessToken)
    }

    const employeeId = res.data.id
    localStorage.setItem(USER_ID_STORAGE_KEY, employeeId.toString())

    // 🔹 Mapping idQuyenHan thành RoleType chính xác
    const roleMap: Record<number, RoleType> = {
      1: 'admin', // idQuyenHan = 1 là admin
      2: 'user',  // idQuyenHan = 2 là nhân viên
    }

    const userRoles: RoleType[] = [roleMap[res.data.idQuyenHan] || 'user']
    localStorage.setItem(ROLES_STORAGE_KEY, JSON.stringify(userRoles))

    const userInfo = {
      id: employeeId,
      maNhanVien: res.data.maNhanVien,
      name: res.data.tenNhanVien,
      tenTaiKhoan: res.data.tenTaiKhoan,
      email: res.data.email,
      idQuyenHan: res.data.idQuyenHan,
      tenQuyenHan: res.data.tenQuyenHan,
      accessToken: res.data.accessToken,
      refreshToken: res.data.refreshToken,
      roles: userRoles,
    }

    const storedProfile = {
      id: employeeId,
      maNhanVien: res.data.maNhanVien,
      name: res.data.tenNhanVien,
      tenTaiKhoan: res.data.tenTaiKhoan,
      email: res.data.email,
      idQuyenHan: res.data.idQuyenHan,
      tenQuyenHan: res.data.tenQuyenHan,
    }

    localStorage.setItem(USER_INFO_STORAGE_KEY, JSON.stringify(storedProfile))

    this.setInfo(userInfo)
  } catch (err) {
    clearToken()
    localStorage.removeItem(USER_ID_STORAGE_KEY)
    localStorage.removeItem(ROLES_STORAGE_KEY)
    localStorage.removeItem(USER_INFO_STORAGE_KEY)
    throw err
  }
}
,

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
        localStorage.removeItem(USER_ID_STORAGE_KEY)
        localStorage.removeItem(ROLES_STORAGE_KEY)
        localStorage.removeItem(USER_INFO_STORAGE_KEY)
        removeRouteListener()
        appStore.clearServerMenu()
      }
    },
  },
})

export default useUserStore
