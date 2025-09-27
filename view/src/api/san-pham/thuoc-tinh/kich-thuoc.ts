import axios from 'axios'

// ==================== KÍCH THƯỚC ====================
export interface KichThuoc {
  id: number
  maKichThuoc?: string
  tenKichThuoc: string
  trangThai: boolean
  deleted: boolean
  createAt?: string
  updateAt?: string
}

export interface KichThuocResponse {
  content: KichThuoc[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export function getKichThuocListAll() {
  return axios.get<KichThuocResponse>('/api/kich-thuoc-management/playlist')
}

export function getKichThuocList(page: number) {
  return axios.get<KichThuocResponse>(`/api/kich-thuoc-management/paging?page=${page}`)
}

export function getKichThuocById(id: number) {
  return axios.get<KichThuoc>(`/api/kich-thuoc-management/detail/${id}`)
}

export function createKichThuoc(data: Omit<KichThuoc, 'id' | 'maKichThuoc' | 'createAt' | 'updateAt'>) {
  return axios.post<KichThuoc>('/api/kich-thuoc-management/add', data)
}

export function updateKichThuoc(id: number, data: Partial<KichThuoc>) {
  return axios.put<KichThuoc>(`/api/kich-thuoc-management/update/${id}`, data)
}

export function deleteKichThuoc(id: number) {
  return axios.put(`/api/kich-thuoc-management/update/status/${id}`)
}
