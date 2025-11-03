import client from './client'

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
  nhanVien1Id: number
  nhanVien1Name: string
  nhanVien2Id: number
  nhanVien2Name: string
  lastMessageContent: string | null
  lastMessageTime: string | null
  lastSenderId: number | null
  unreadCountNv1: number
  unreadCountNv2: number
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

export interface SendMessageRequest {
  receiverId: number
  content: string
  messageType?: 'TEXT' | 'IMAGE' | 'FILE'
  fileUrl?: string
}

export const fetchConversations = () =>
  client.get<Conversation[], Conversation[]>(`/api/chat/conversations`)

export const fetchMessages = (otherUserId: number, page = 0, size = 50) =>
  client.get<PageResponse<ChatMessage>, PageResponse<ChatMessage>>(
    `/api/chat/messages/${otherUserId}?page=${page}&size=${size}`
  )

export const sendMessage = (payload: SendMessageRequest) =>
  client.post<ChatMessage, ChatMessage>('/api/chat/messages', payload)

export const markMessagesAsRead = (senderId: number) =>
  client.put<void, void>(`/api/chat/messages/read/${senderId}`)

export const getUnreadCount = () => client.get<number, number>('/api/chat/unread-count')
