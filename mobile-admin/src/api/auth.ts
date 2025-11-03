import client from './client'

export interface LoginRequest {
  username: string
  password: string
}

export interface AuthUser {
  id: number
  maNhanVien: string
  tenNhanVien: string
  tenTaiKhoan: string
  email: string
  idQuyenHan: number
  tenQuyenHan: string
  accessToken: string
  refreshToken: string
}

export const login = (credentials: LoginRequest) =>
  client.post<AuthUser, AuthUser>('/api/auth/login', credentials)

export const logout = () => client.post<void, void>('/api/auth/logout')

export const getCurrentUser = () => client.post<AuthUser, AuthUser>('/api/auth/me')
