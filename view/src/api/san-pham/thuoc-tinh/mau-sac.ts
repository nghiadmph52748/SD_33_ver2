import axios from 'axios'

// ==================== MÀU SẮC ====================
export interface MauSac {
  id: number
  maMauSac?: string
  tenMauSac: string
  maMau: string
  trangThai: boolean
  deleted: boolean
  createAt?: string
  updateAt?: string
}

export interface MauSacResponse {
  content: MauSac[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export function getMauSacListAll() {
  return axios.get<MauSacResponse>('/api/mau-sac-management/playlist')
}

export function getMauSacList(page: number) {
  return axios.get<MauSacResponse>(`/api/mau-sac-management/paging?page=${page}`)
}

export function getMauSacById(id: number) {
  return axios.get<MauSac>(`/api/mau-sac-management/detail/${id}`)
}

export function createMauSac(data: Omit<MauSac, 'id' | 'maMauSac' | 'createAt' | 'updateAt'>) {
  return axios.post<MauSac>('/api/mau-sac-management/add', data)
}

export function updateMauSac(id: number, data: Partial<MauSac>) {
  return axios.put<MauSac>(`/api/mau-sac-management/update/${id}`, data)
}

export function deleteMauSac(id: number) {
  return axios.put(`/api/mau-sac-management/update/status/${id}`)
}
