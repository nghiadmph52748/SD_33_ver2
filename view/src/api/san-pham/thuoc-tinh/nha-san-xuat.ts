import axios from 'axios'
import { type PagingResponse, type PagedResponseObject } from '../ResponseObject'
// ==================== NHÀ SẢN XUẤT ====================
export interface NhaSanXuat {
  id: number
  maNhaSanXuat?: string
  tenNhaSanXuat: string
  trangThai: boolean
  deleted: boolean
  createAt?: string
  updateAt?: string
}

export interface NhaSanXuatPagingResponse extends PagingResponse {
  data: NhaSanXuat[]
}

export interface NhaSanXuatResponse extends PagedResponseObject {
  data: NhaSanXuatPagingResponse
}

export function getNhaSanXuatListAll() {
  return axios.get<NhaSanXuatResponse>('/api/nha-san-xuat-management/playlist')
}

export function getNhaSanXuatList(page: number) {
  return axios.get<NhaSanXuatResponse>(`/api/nha-san-xuat-management/paging?page=${page}`)
}

export function getNhaSanXuatById(id: number) {
  return axios.get<NhaSanXuat>(`/api/nha-san-xuat-management/detail/${id}`)
}

export function createNhaSanXuat(data: Omit<NhaSanXuat, 'id' | 'maNhaSanXuat' | 'createAt' | 'updateAt'>) {
  return axios.post<NhaSanXuat>('/api/nha-san-xuat-management/add', data)
}

export function updateNhaSanXuat(id: number, data: Partial<NhaSanXuat>) {
  return axios.put<NhaSanXuat>(`/api/nha-san-xuat-management/update/${id}`, data)
}

export function deleteNhaSanXuat(id: number) {
  return axios.put(`/api/nha-san-xuat-management/update/status/${id}`)
}
