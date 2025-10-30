import { defineStore } from 'pinia'
import { queryMessageList, setMessageStatus, MessageRecord } from '@/api/message'
import { Client, IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { getToken } from '@/utils/auth'

export interface NotificationState {
  messages: MessageRecord[]
  loading: boolean
  unreadCount: number
  wsConnected: boolean
  wsConnecting: boolean
  stompClient: Client | null
}

const useNotificationStore = defineStore('notification', {
  state: (): NotificationState => ({
    messages: [],
    loading: false,
    unreadCount: 0,
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
        // Backend returns ResponseObject with data property containing the notifications array
        const notifications = response.data?.data || []
        const normalized = notifications.map((m) => ({
          // Ensure required fields exist for UI rendering
          id: m.id,
          title: m.title || 'Thông báo',
          subTitle: m.subTitle || '',
          content: m.content || '',
          time: m.time || new Date().toISOString(),
          status: (m.status as 0 | 1) ?? 0,
          avatar: m.avatar,
          type: m.type || (m.messageType === 0 ? 'todo' : 'message'),
          messageType: m.messageType,
        }))

        // Use real data from backend
        this.messages = normalized
        this.unreadCount = this.totalUnread
      } catch (error) {
        console.error('Failed to fetch notifications:', error)
        // Keep existing messages on error
      } finally {
        this.loading = false
      }
    },

    // Mark messages as read
    async markAsRead(ids: number[]) {
      try {
        await setMessageStatus({ ids })
        // Update local state
        this.messages = this.messages.map((msg) => {
          if (ids.includes(msg.id)) {
            return { ...msg, status: 1 }
          }
          return msg
        })
        this.unreadCount = this.totalUnread
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
    clearAll() {
      this.messages = []
      this.unreadCount = 0
    },

    // Add a new notification (for real-time updates)
    addNotification(notification: MessageRecord) {
      this.messages.unshift(notification)
      if (notification.status === 0) {
        this.unreadCount += 1
      }
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

      const client = new Client({
        webSocketFactory: () => new SockJS('http://localhost:8080/ws-chat'),
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },
        debug: () => {}, // Disable debug logs in production
        reconnectDelay: 5000,
        heartbeatOutgoing: 4000,

        onConnect: () => {
          console.log('✅ Notification WebSocket connected')
          this.wsConnected = true
          this.wsConnecting = false

          // Subscribe to notification topic
          client.subscribe('/user/queue/notifications', (message: IMessage) => {
            try {
              const notification: MessageRecord = JSON.parse(message.body)
              console.log('📬 Received notification:', notification)
              this.addNotification(notification)
            } catch (err) {
              console.error('❌ Error parsing notification:', err)
            }
          })
        },

        onStompError: (frame) => {
          console.error('❌ Notification STOMP error:', frame.headers.message)
          this.wsConnected = false
          this.wsConnecting = false
        },

        onWebSocketClose: () => {
          console.log('🔌 Notification WebSocket closed')
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
        console.log('🔌 Notification WebSocket disconnected')
      }
    },
  },
})

export default useNotificationStore
