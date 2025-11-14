import axios from 'axios'
import type { HttpResponse } from './interceptor'

// ==================== TypeScript Interfaces ====================

/**
 * Tin nhắn chat
 */
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
  sentAt: string // ISO datetime string
  readAt: string | null
}

/**
 * Cuộc trò chuyện
 */
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

/**
 * Request gửi tin nhắn mới
 */
export interface SendMessageRequest {
  receiverId: number
  content: string
  messageType?: 'TEXT' | 'IMAGE' | 'FILE'
  fileUrl?: string
}

/**
 * Paginated response cho tin nhắn
 */
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

/**
 * Response wrapper từ backend
 */
export interface ChatResponse<T> extends HttpResponse<T> {
  success: boolean
  message: string
  data: T
}

// ==================== API Functions ====================

/**
 * Lấy danh sách cuộc trò chuyện của người dùng hiện tại
 */
export const layDanhSachCuocTroChuyen = () => {
  return axios.get<ChatResponse<Conversation[]>>('/api/chat/conversations')
}

/**
 * Lấy danh sách tin nhắn giữa 2 người dùng (có phân trang)
 * @param otherUserId - ID người dùng kia
 * @param page - Số trang (default: 0)
 * @param size - Số lượng tin nhắn mỗi trang (default: 50)
 */
export const layDanhSachTinNhan = (otherUserId: number, page = 0, size = 50) => {
  return axios.get<ChatResponse<PageResponse<ChatMessage>>>(`/api/chat/messages/${otherUserId}`, {
    params: { page, size },
  })
}

/**
 * Gửi tin nhắn mới
 * @param request - Thông tin tin nhắn cần gửi
 */
export const guiTinNhan = (request: SendMessageRequest) => {
  return axios.post<ChatResponse<ChatMessage>>('/api/chat/messages', request)
}

/**
 * Đánh dấu tin nhắn từ người gửi là đã đọc
 * @param senderId - ID người gửi tin nhắn
 */
export const danhDauDaDoc = (senderId: number) => {
  return axios.put<ChatResponse<null>>(`/api/chat/messages/read/${senderId}`)
}

/**
 * Lấy tổng số tin nhắn chưa đọc của người dùng hiện tại
 */
export const laySoTinNhanChuaDoc = () => {
  return axios.get<ChatResponse<number>>('/api/chat/unread-count')
}

/**
 * Lấy hoặc tạo cuộc trò chuyện với người dùng khác
 * @param otherUserId - ID người dùng muốn chat
 */
export const layHoacTaoCuocTroChuyen = (otherUserId: number) => {
  return axios.get<ChatResponse<Conversation>>(`/api/chat/conversation/${otherUserId}`)
}

/**
 * AI Chat History interface
 */
export interface AiChatHistory {
  id: number
  sessionId: string
  role: 'user' | 'assistant'
  content: string
  timestamp: string
}

/**
 * Lấy lịch sử AI chat của khách hàng (chỉ staff có quyền)
 * @param customerId - ID khách hàng
 */
export const getCustomerAiChatHistory = (customerId: number) => {
  return axios.get<ChatResponse<AiChatHistory[]>>(`/api/chat/ai-history/${customerId}`)
}

// ==================== WebSocket Message Types ====================

/**
 * Message để gửi qua WebSocket
 */
export interface WebSocketSendMessage {
  receiverId: number
  content: string
  messageType: 'TEXT' | 'IMAGE' | 'FILE'
  fileUrl?: string
}

/**
 * Thông báo typing qua WebSocket
 */
export interface TypingNotification {
  senderId: number
  receiverId: number
  isTyping: boolean
}
