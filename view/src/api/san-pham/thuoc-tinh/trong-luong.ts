import axios from 'axios'
import { type PagingResponse, type PagedResponseObject } from '../ResponseObject'

// ==================== TRỌNG LƯỢNG ====================
export interface TrongLuong {
  id: number
  maTrongLuong?: string
  tenTrongLuong: string
  trangThai: boolean
  deleted: boolean
  createAt?: string
  updateAt?: string
}

export interface TrongLuongPagingResponse extends PagingResponse {
  data: TrongLuong[]
}

export interface TrongLuongResponse extends PagedResponseObject {
  data: TrongLuongPagingResponse
}

export function getTrongLuongListAll() {
  return axios.get<TrongLuongResponse>('/api/trong-luong-management/playlist')
}

export function getTrongLuongList(page: number) {
  return axios.get<TrongLuongResponse>(`/api/trong-luong-management/paging?page=${page}`)
}

export function getTrongLuongById(id: number) {
  return axios.get<TrongLuong>(`/api/trong-luong-management/detail/${id}`)
}

export function createTrongLuong(data: Omit<TrongLuong, 'id' | 'maTrongLuong' | 'createAt' | 'updateAt'>) {
  return axios.post<TrongLuong>('/api/trong-luong-management/add', data)
}

export function updateTrongLuong(id: number, data: Partial<TrongLuong>) {
  return axios.put<TrongLuong>(`/api/trong-luong-management/update/${id}`, data)
}

export function deleteTrongLuong(id: number) {
  return axios.put(`/api/trong-luong-management/update/status/${id}`)
}
