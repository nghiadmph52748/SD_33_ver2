import { UserState } from '@/store/modules/user/types'
import axios from 'axios'
import type { RouteRecordNormalized } from 'vue-router'

export interface LoginData {
  username: string
  password: string
}

export interface LoginRes {
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
export function login(data: LoginData) {
  return axios.post<LoginRes>('/api/auth/login', data)
}

export function logout() {
  return axios.post('/api/auth/logout')
}

export function getUserInfo() {
  return axios.post<UserState>('/api/user/info')
}

export function getMenuList() {
  return axios.post<RouteRecordNormalized[]>('/api/user/menu')
}
