import axios from 'axios'
import { type PagingResponse, type PagedResponseObject } from '../ResponseObject'

// ==================== CHẤT LIỆU ====================
export interface ChatLieu {
  id: number
  maChatLieu?: string
  tenChatLieu: string
  trangThai: boolean
  deleted: boolean
  createAt?: string
  createBy?: number
  updateAt?: string
  updateBy?: number
}

export interface ChatLieuPagingResponse extends PagingResponse {
  data: ChatLieu[]
}

export interface ChatLieuResponse extends PagedResponseObject {
  data: ChatLieuPagingResponse
}

export function getChatLieuListAll() {
  return axios.get<ChatLieuResponse>('/api/chat-lieu-management/playlist')
}

export function getChatLieuList(page: number, size: number) {
  return axios.get<ChatLieuResponse>(`/api/chat-lieu-management/paging?page=${page}&size=${size}`)
}

export function getChatLieuById(id: number) {
  return axios.get<ChatLieu>(`/api/chat-lieu-management/detail/${id}`)
}

export function createChatLieu(data: Omit<ChatLieu, 'tenChatLieu' | 'trangThai' | 'deleted' | 'createAt' | 'createBy'>) {
  return axios.post<ChatLieu>('/api/chat-lieu-management/add', data)
}

export function updateChatLieu(id: number, data: Partial<ChatLieu>) {
  return axios.put<ChatLieu>(`/api/chat-lieu-management/update/${id}`, data)
}

export function deleteChatLieu(id: number) {
  return axios.put(`/api/chat-lieu-management/update/status/${id}`)
}
