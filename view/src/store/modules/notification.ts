import { defineStore } from 'pinia'
import { queryMessageList, setMessageStatus, clearAllNotifications, MessageRecord } from '@/api/message'
import { Client, IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { getToken } from '@/utils/auth'
import axios from 'axios'

const READ_STORAGE_KEY = 'readNotificationIds'
const LOCAL_NOTI_STORAGE_KEY = 'localNotifications'

function loadReadIds(): Set<number> {
  try {
    const raw = localStorage.getItem(READ_STORAGE_KEY)
    if (!raw) return new Set<number>()
    const parsed = JSON.parse(raw) as number[]
    return new Set<number>(Array.isArray(parsed) ? parsed : [])
  } catch {
    return new Set<number>()
  }
}

function saveReadIds(ids: Set<number>): void {
  try {
    localStorage.setItem(READ_STORAGE_KEY, JSON.stringify(Array.from(ids)))
  } catch {
    // ignore storage errors
  }
}

function loadLocalNotifications(): MessageRecord[] {
  try {
    const raw = localStorage.getItem(LOCAL_NOTI_STORAGE_KEY)
    if (!raw) return []
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? (parsed as MessageRecord[]) : []
  } catch {
    return []
  }
}

function saveLocalNotifications(notifications: MessageRecord[]): void {
  try {
    localStorage.setItem(LOCAL_NOTI_STORAGE_KEY, JSON.stringify(notifications))
  } catch {
    // ignore storage errors
  }
}

// Heuristic: treat IDs larger than 32-bit int as local-only (dev/test)
function isLocalOnlyId(id: number): boolean {
  return id > 2147483647
}

export interface NotificationState {
  messages: MessageRecord[]
  loading: boolean
  unreadCount: number
  wsConnected: boolean
  wsConnecting: boolean
  stompClient: Client | null
}

const initialMessages = loadLocalNotifications()

const useNotificationStore = defineStore('notification', {
  state: (): NotificationState => ({
    messages: initialMessages,
    loading: false,
    unreadCount: initialMessages.filter((msg) => msg.status === 0).length,
    wsConnected: false,
    wsConnecting: false,
    stompClient: null,
  }),

  getters: {
    // Get messages by type
    messagesByType: (state) => (type: string) => {
      return state.messages.filter((msg) => msg.type === type)
    },

    // Get unread messages by type
    unreadByType: (state) => (type: string) => {
      return state.messages.filter((msg) => msg.type === type && msg.status === 0)
    },

    // Get total unread count
    totalUnread: (state) => {
      return state.messages.filter((msg) => msg.status === 0).length
    },

    // Get unread count for specific type
    unreadCountByType: (state) => (type: string) => {
      return state.messages.filter((msg) => msg.type === type && msg.status === 0).length
    },
  },

  actions: {
    // Fetch all notifications from backend
    async fetchNotifications() {
      this.loading = true
      try {
        const response = await queryMessageList()
        const notifications = normalizeNotificationPayload(response)
        const readIds = loadReadIds()

        if (notifications.length > 0) {
          const normalized = notifications.map((m) => {
            const backendStatus = (m.status as 0 | 1) ?? 0
            const status: 0 | 1 = (backendStatus === 1 || readIds.has(m.id)) ? 1 : 0

            return {
              // Ensure required fields exist for UI rendering
              id: m.id,
              title: m.title || 'Thông báo',
              subTitle: m.subTitle || '',
              content: m.content || '',
              time: m.time || new Date().toISOString(),
              status,
              avatar: m.avatar,
              type: m.type || (m.messageType === 0 ? 'todo' : 'message'),
              messageType: m.messageType,
            }
          })

          // Use backend data and persist to localStorage as cache
          this.messages = normalized
          this.unreadCount = this.totalUnread
          saveLocalNotifications(this.messages)
        } else {
          // If backend returns empty (e.g. only pushing new real-time notifications),
          // keep existing/local notifications so they do not disappear when reopening popup.
          const cachedResponse = loadLocalNotifications()
          const cached = normalizeNotificationPayload(cachedResponse)
          if (!this.messages.length && cached.length) {
            this.messages = cached
            this.unreadCount = this.totalUnread
          }
        }
      } catch (error) {
        console.error('Failed to fetch notifications:', error)
        // Fallback: try to restore from localStorage cache
        const cached = normalizeNotificationPayload(loadLocalNotifications())
        if (!this.messages.length && cached.length) {
          this.messages = cached
          this.unreadCount = this.totalUnread
        }
      } finally {
        this.loading = false
      }
    },

    // Mark messages as read
    async markAsRead(ids: number[]) {
      try {
        const backendIds = ids.filter((id) => !isLocalOnlyId(id))
        const localOnlyIds = ids.filter((id) => isLocalOnlyId(id))

        if (backendIds.length > 0) {
          await setMessageStatus({ ids: backendIds })
        }
        // Update local state
        this.messages = this.messages.map((msg) => {
          if (ids.includes(msg.id)) {
            return { ...msg, status: 1 }
          }
          return msg
        })
        this.unreadCount = this.totalUnread

        // Persist read ids locally
        const readIds = loadReadIds()
        ids.forEach((id) => readIds.add(id))
        saveReadIds(readIds)

        // If any are local-only, persist updated local notifications list
        if (localOnlyIds.length > 0) {
          const localList = loadLocalNotifications()
          const updatedLocalList = localList.map((n) =>
            localOnlyIds.includes(n.id) ? { ...n, status: 1 as 0 | 1 } : n
          )
          saveLocalNotifications(updatedLocalList)
        }
      } catch (error) {
        console.error('Failed to mark messages as read:', error)
        throw error
      }
    },

    // Mark all messages as read
    async markAllAsRead() {
      const unreadIds = this.messages.filter((msg) => msg.status === 0).map((msg) => msg.id)
      if (unreadIds.length > 0) {
        await this.markAsRead(unreadIds)
      }
    },

    // Clear all notifications (for "empty list" action)
    async clearAll() {
      try {
        await clearAllNotifications()
      } catch (error) {
        console.error('Failed to clear notifications:', error)
        throw error
      }
      this.messages = []
      this.unreadCount = 0
      // Also clear persisted local data
      try {
        localStorage.removeItem(LOCAL_NOTI_STORAGE_KEY)
        localStorage.removeItem(READ_STORAGE_KEY)
      } catch {}
    },

    // Add a new notification (for real-time updates)
    addNotification(notification: MessageRecord) {
      const readIds = loadReadIds()
      const normalized: MessageRecord = {
        ...notification,
        status: (notification.status as 0 | 1) ?? (readIds.has(notification.id) ? 1 : 0),
      }

      this.messages.unshift(normalized)
      if (normalized.status === 0) {
        this.unreadCount += 1
      }

      // Persist notifications so they survive reloads (cache)
      const localList = loadLocalNotifications()
      const exists = localList.some((n) => n.id === normalized.id)
      const updated = exists ? localList.map((n) => (n.id === normalized.id ? normalized : n)) : [normalized, ...localList]
      saveLocalNotifications(updated)
    },

    // Connect to WebSocket for real-time notifications
    connectWebSocket() {
      if (this.wsConnected || this.wsConnecting) {
        return
      }

      const token = getToken()
      if (!token) {
        console.warn('No token available for WebSocket connection')
        return
      }

      this.wsConnecting = true

      const baseURL = axios.defaults.baseURL || 'http://localhost:8080'
      const wsUrl = `${baseURL}/ws-chat/sockjs`

      const client = new Client({
        webSocketFactory: () => new SockJS(wsUrl),
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },
        debug: () => {}, // Disable debug logs in production
        reconnectDelay: 5000,
        heartbeatOutgoing: 4000,

        onConnect: () => {
          console.log(' Notification WebSocket connected')
          this.wsConnected = true
          this.wsConnecting = false

          // Subscribe to notification topic
          client.subscribe('/user/queue/notifications', (message: IMessage) => {
            try {
              const notification: MessageRecord = JSON.parse(message.body)
              console.log(' Received notification:', notification)
              this.addNotification(notification)
            } catch (err) {
              console.error(' Error parsing notification:', err)
            }
          })
        },

        onStompError: (frame) => {
          console.error(' Notification STOMP error:', frame.headers.message)
          this.wsConnected = false
          this.wsConnecting = false
        },

        onWebSocketClose: () => {
          console.log(' Notification WebSocket closed')
          this.wsConnected = false
          this.wsConnecting = false
        },
      })

      client.activate()
      this.stompClient = client
    },

    // Disconnect WebSocket
    disconnectWebSocket() {
      if (this.stompClient) {
        this.stompClient.deactivate()
        this.stompClient = null
        this.wsConnected = false
        this.wsConnecting = false
        console.log(' Notification WebSocket disconnected')
      }
    },
  },
})

function normalizeNotificationPayload(payload: unknown): MessageRecord[] {
  if (Array.isArray(payload)) {
    return payload as MessageRecord[]
  }
  const dataLevel1 = (payload as { data?: unknown })?.data
  if (Array.isArray(dataLevel1)) {
    return dataLevel1 as MessageRecord[]
  }
  const dataLevel2 = (dataLevel1 as { data?: unknown })?.data
  if (Array.isArray(dataLevel2)) {
    return dataLevel2 as MessageRecord[]
  }
  return []
}

export default useNotificationStore
