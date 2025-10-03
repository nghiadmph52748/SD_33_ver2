import axios from 'axios'
import { type PagingResponse, type PagedResponseObject } from '../ResponseObject'

// ==================== XUẤT XỨ ====================
export interface XuatXu {
  id: number
  maXuatXu?: string
  tenXuatXu: string
  trangThai: boolean
  deleted: boolean
  createAt?: string
  createBy?: number
  updateAt?: string
  updateBy?: number
}

export interface XuatXuPagingResponse extends PagingResponse {
  data: XuatXu[]
}

export interface XuatXuResponse extends PagedResponseObject {
  data: XuatXuPagingResponse
}

export function getXuatXuListAll() {
  return axios.get<XuatXuResponse>('/api/xuat-xu-management/playlist')
}

export function getXuatXuList(page: number, size: number) {
  return axios.get<XuatXuResponse>(`/api/xuat-xu-management/paging?page=${page}&size=${size}`)
}

export function getXuatXuById(id: number) {
  return axios.get<XuatXu>(`/api/xuat-xu-management/detail/${id}`)
}

export function createXuatXu(data: Omit<XuatXu, 'tenXuatXu' | 'trangThai' | 'deleted' | 'createAt' | 'createBy'>) {
  return axios.post<XuatXu>('/api/xuat-xu-management/add', data)
}

export function updateXuatXu(id: number, data: Partial<XuatXu>) {
  return axios.put<XuatXu>(`/api/xuat-xu-management/update/${id}`, data)
}

export function deleteXuatXu(id: number) {
  return axios.put(`/api/xuat-xu-management/update/status/${id}`)
}
