import axios from 'axios'

// ==================== XUẤT XỨ ====================
export interface XuatXu {
  id: number
  maXuatXu?: string
  tenXuatXu: string
  trangThai: boolean
  deleted: boolean
  createAt?: string
  updateAt?: string
}

export interface XuatXuResponse {
  content: XuatXu[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export function getXuatXuListAll() {
  return axios.get<XuatXuResponse>('/api/xuat-xu-management/playlist')
}

export function getXuatXuList(page: number) {
  return axios.get<XuatXuResponse>(`/api/xuat-xu-management/paging?page=${page}`)
}

export function getXuatXuById(id: number) {
  return axios.get<XuatXu>(`/api/xuat-xu-management/detail/${id}`)
}

export function createXuatXu(data: Omit<XuatXu, 'id' | 'maXuatXu' | 'createAt' | 'updateAt'>) {
  return axios.post<XuatXu>('/api/xuat-xu-management/add', data)
}

export function updateXuatXu(id: number, data: Partial<XuatXu>) {
  return axios.put<XuatXu>(`/api/xuat-xu-management/update/${id}`, data)
}

export function deleteXuatXu(id: number) {
  return axios.put(`/api/xuat-xu-management/update/status/${id}`)
}
