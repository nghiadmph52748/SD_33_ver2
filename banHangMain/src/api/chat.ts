import axios from '@/api/interceptor'
import type { HttpResponse } from '@/api/interceptor'

export interface ChatMessage {
  id: number
  maTinNhan: string
  senderId: number
  senderName: string
  receiverId: number
  receiverName: string
  content: string
  messageType: 'TEXT' | 'IMAGE' | 'FILE'
  isRead: boolean
  sentAt: string
  readAt: string | null
}

export interface Conversation {
  id: number
  maCuocTraoDoi: string
  loaiCuocTraoDoi?: string // STAFF_STAFF, CUSTOMER_STAFF
  // Staff-staff conversation fields
  nhanVien1Id?: number
  nhanVien1Name?: string
  nhanVien2Id?: number
  nhanVien2Name?: string
  // Customer-staff conversation fields
  khachHangId?: number
  khachHangName?: string
  nhanVienId?: number
  nhanVienName?: string
  // Common fields
  lastMessageContent: string | null
  lastMessageTime: string | null
  lastSenderId: number | null
  unreadCountNv1: number
  unreadCountNv2: number
}

export interface SendMessageRequest {
  receiverId: number
  content: string
  messageType?: 'TEXT' | 'IMAGE' | 'FILE'
  fileUrl?: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface ChatResponse<T> extends HttpResponse<T> {
  success: boolean
  message: string
  data: T
}

export const layDanhSachCuocTroChuyen = () => {
  return axios.get<ChatResponse<Conversation[]>>('/api/chat/conversations')
}

export const layDanhSachTinNhan = (otherUserId: number, page = 0, size = 50) => {
  return axios.get<ChatResponse<PageResponse<ChatMessage>>>(`/api/chat/messages/${otherUserId}`, {
    params: { page, size },
  })
}

export const guiTinNhan = (request: SendMessageRequest) => {
  return axios.post<ChatResponse<ChatMessage>>('/api/chat/messages', request)
}

export const danhDauDaDoc = (senderId: number) => {
  return axios.put<ChatResponse<null>>(`/api/chat/messages/read/${senderId}`)
}

export const laySoTinNhanChuaDoc = () => {
  return axios.get<ChatResponse<number>>('/api/chat/unread-count')
}

export const layHoacTaoCuocTroChuyen = (otherUserId: number) => {
  return axios.get<ChatResponse<Conversation>>(`/api/chat/conversation/${otherUserId}`)
}

export interface WebSocketSendMessage {
  receiverId: number
  content: string
  messageType: 'TEXT' | 'IMAGE' | 'FILE'
  fileUrl?: string
}

export interface TypingNotification {
  senderId: number
  receiverId: number
  isTyping: boolean
}

export interface SaveAiChatHistoryRequest {
  customerId: number
  sessionId: string
  role: 'user' | 'assistant'
  content: string
}

/**
 * Lưu lịch sử AI chat
 */
export const luuLichSuAiChat = (request: SaveAiChatHistoryRequest) => {
  return axios.post<ChatResponse<any>>('/api/chat/ai-history', request)
}
