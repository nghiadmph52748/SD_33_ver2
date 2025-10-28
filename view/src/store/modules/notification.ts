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
        const normalized = (data || []).map((m) => ({
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

        // If no data returned, provide a small demo list so UI is not empty
        this.messages = normalized.length
          ? normalized
          : [
              {
                id: 1,
                title: 'Nguyễn Văn A',
                subTitle: 'Tin nhắn riêng',
                content: 'Yêu cầu phê duyệt đã được gửi, vui lòng kiểm tra',
                time: new Date().toLocaleString('vi-VN'),
                status: 0,
                type: 'message',
                messageType: 2,
              },
              {
                id: 2,
                title: 'Hệ thống',
                subTitle: 'Cảnh báo',
                content: 'Phiên đăng nhập sẽ hết hạn sau 10 phút',
                time: new Date(Date.now() - 3600_000).toLocaleString('vi-VN'),
                status: 0,
                type: 'notice',
                messageType: 3,
              },
              {
                id: 3,
                title: 'Việc cần làm',
                subTitle: 'Đánh giá PR',
                content: 'Xem lại yêu cầu hợp nhất #128',
                time: new Date(Date.now() - 24 * 3600_000).toLocaleString('vi-VN'),
                status: 1,
                type: 'todo',
                messageType: 1,
              },
            ]
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
