import { defineStore } from 'pinia'
import { queryMessageList, setMessageStatus, MessageRecord } from '@/api/message'

export interface NotificationState {
  messages: MessageRecord[]
  loading: boolean
  unreadCount: number
}

const useNotificationStore = defineStore('notification', {
  state: (): NotificationState => ({
    messages: [],
    loading: false,
    unreadCount: 0,
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
        const { data } = await queryMessageList()
        this.messages = data
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
  },
})

export default useNotificationStore
