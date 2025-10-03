import axios from 'axios'
import { type PagingResponse, type PagedResponseObject } from '../ResponseObject'

// ==================== MÀU SẮC ====================
export interface MauSac {
  id: number
  maMauSac?: string
  tenMauSac: string
  maMau: string
  trangThai: boolean
  deleted: boolean
  createAt?: string
  createBy?: string
  updateAt?: string
  updateBy?: string
}

export interface MauSacPagingResponse extends PagingResponse {
  data: MauSac[]
}

export interface MauSacResponse extends PagedResponseObject {
  data: MauSacPagingResponse
}

export function getMauSacListAll() {
  return axios.get<MauSacResponse>('/api/mau-sac-management/playlist')
}

export function getMauSacList(page: number, size: number) {
  return axios.get<MauSacResponse>(`/api/mau-sac-management/paging?page=${page}&size=${size}`)
}

export function getMauSacById(id: number) {
  return axios.get<MauSac>(`/api/mau-sac-management/detail/${id}`)
}

export function createMauSac(data: Omit<MauSac, 'tenMauSac' | 'trangThai' | 'deleted' | 'createAt' | 'createBy'>) {
  return axios.post<MauSac>('/api/mau-sac-management/add', data)
}

export function updateMauSac(id: number, data: Partial<MauSac>) {
  return axios.put<MauSac>(`/api/mau-sac-management/update/${id}`, data)
}

export function deleteMauSac(id: number) {
  return axios.put(`/api/mau-sac-management/update/status/${id}`)
}
