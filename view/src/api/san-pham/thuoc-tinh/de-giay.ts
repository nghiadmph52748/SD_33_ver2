import axios from 'axios'
import { type PagingResponse, type PagedResponseObject } from '../ResponseObject'

// ==================== ĐẾ GIÀY ====================
export interface DeGiay {
  id: number
  maDeGiay?: string
  tenDeGiay: string
  trangThai: boolean
  deleted: boolean
  createAt?: string
  updateAt?: string
}

export interface DeGiayPagingResponse extends PagingResponse {
  data: DeGiay[]
}

export interface DeGiayResponse extends PagedResponseObject {
  data: DeGiayPagingResponse
}

export function getDeGiayListAll() {
  return axios.get<DeGiayResponse>('/api/de-giay-management/playlist')
}

export function getDeGiayList(page: number) {
  return axios.get<DeGiayResponse>(`/api/de-giay-management/paging?page=${page}`)
}

export function getDeGiayById(id: number) {
  return axios.get<DeGiay>(`/api/de-giay-management/detail/${id}`)
}

export function createDeGiay(data: Omit<DeGiay, 'id' | 'maDeGiay' | 'createAt' | 'updateAt'>) {
  return axios.post<DeGiay>('/api/de-giay-management/add', data)
}

export function updateDeGiay(id: number, data: Partial<DeGiay>) {
  return axios.put<DeGiay>(`/api/de-giay-management/update/${id}`, data)
}

export function deleteDeGiay(id: number) {
  return axios.put(`/api/de-giay-management/update/status/${id}`)
}
