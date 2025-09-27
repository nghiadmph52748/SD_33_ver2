import axios from 'axios'

// ==================== CHẤT LIỆU ====================
export interface ChatLieu {
  id: number
  maChatLieu?: string
  tenChatLieu: string
  trangThai: boolean
  deleted: boolean
  createAt?: string
  updateAt?: string
}

export interface ChatLieuResponse {
  content: ChatLieu[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export function getChatLieuListAll() {
  return axios.get<ChatLieuResponse>('/api/chat-lieu-management/playlist')
}

export function getChatLieuList(page: number) {
  return axios.get<ChatLieuResponse>(`/api/chat-lieu-management/paging?page=${page}`)
}

export function getChatLieuById(id: number) {
  return axios.get<ChatLieu>(`/api/chat-lieu-management/detail/${id}`)
}

export function createChatLieu(data: Omit<ChatLieu, 'id' | 'maChatLieu' | 'createAt' | 'updateAt'>) {
  return axios.post<ChatLieu>('/api/chat-lieu-management/add', data)
}

export function updateChatLieu(id: number, data: Partial<ChatLieu>) {
  return axios.put<ChatLieu>(`/api/chat-lieu-management/update/${id}`, data)
}

export function deleteChatLieu(id: number) {
  return axios.put(`/api/chat-lieu-management/update/status/${id}`)
}
