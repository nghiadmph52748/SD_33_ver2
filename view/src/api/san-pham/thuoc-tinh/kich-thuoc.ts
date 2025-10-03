import axios from 'axios'
import { type PagingResponse, type PagedResponseObject } from '../ResponseObject'

// ==================== KÍCH THƯỚC ====================
export interface KichThuoc {
  id: number
  maKichThuoc?: string
  tenKichThuoc: string
  trangThai: boolean
  deleted: boolean
  createAt?: string
  createBy?: string
  updateAt?: string
  updateBy?: string
}

export interface KichThuocPagingResponse extends PagingResponse {
  data: KichThuoc[]
}

export interface KichThuocResponse extends PagedResponseObject {
  data: KichThuocPagingResponse
}

export function getKichThuocListAll() {
  return axios.get<KichThuocResponse>('/api/kich-thuoc-management/playlist')
}

export function getKichThuocList(page: number, size: number) {
  return axios.get<KichThuocResponse>(`/api/kich-thuoc-management/paging?page=${page}&size=${size}`)
}

export function getKichThuocById(id: number) {
  return axios.get<KichThuoc>(`/api/kich-thuoc-management/detail/${id}`)
}

export function createKichThuoc(data: Omit<KichThuoc, 'tenKichThuoc' | 'trangThai' | 'deleted' | 'createAt' | 'createBy'>) {
  return axios.post<KichThuoc>('/api/kich-thuoc-management/add', data)
}

export function updateKichThuoc(id: number, data: Partial<KichThuoc>) {
  return axios.put<KichThuoc>(`/api/kich-thuoc-management/update/${id}`, data)
}

export function deleteKichThuoc(id: number) {
  return axios.put(`/api/kich-thuoc-management/update/status/${id}`)
}
