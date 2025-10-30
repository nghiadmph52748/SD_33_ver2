import { setActivePinia, createPinia } from 'pinia'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import useNotificationStore from '../notification'
import * as messageApi from '@/api/message'
import type { MessageRecord } from '@/api/message'

// Mock the API
vi.mock('@/api/message')
vi.mock('@/utils/auth', () => ({
  getToken: vi.fn(() => 'mock-token')
}))

// Mock WebSocket
vi.mock('@stomp/stompjs', () => ({
  Client: vi.fn(() => ({
    activate: vi.fn(),
    deactivate: vi.fn(),
    subscribe: vi.fn(),
    publish: vi.fn()
  }))
}))

vi.mock('sockjs-client', () => ({
  default: vi.fn()
}))

describe('Notification Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('State', () => {
    it('should have initial state', () => {
      const store = useNotificationStore()
      
      expect(store.messages).toEqual([])
      expect(store.loading).toBe(false)
      expect(store.unreadCount).toBe(0)
      expect(store.wsConnected).toBe(false)
      expect(store.wsConnecting).toBe(false)
      expect(store.stompClient).toBeNull()
    })
  })

  describe('Getters', () => {
    it('should filter messages by type', () => {
      const store = useNotificationStore()
      
      store.messages = [
        { id: 1, type: 'message', status: 0 } as MessageRecord,
        { id: 2, type: 'notice', status: 0 } as MessageRecord,
        { id: 3, type: 'message', status: 0 } as MessageRecord,
      ]

      const messageType = store.messagesByType('message')
      expect(messageType).toHaveLength(2)
      expect(messageType[0].id).toBe(1)
    })

    it('should filter unread messages by type', () => {
      const store = useNotificationStore()
      
      store.messages = [
        { id: 1, type: 'message', status: 0 } as MessageRecord,
        { id: 2, type: 'message', status: 1 } as MessageRecord,
        { id: 3, type: 'notice', status: 0 } as MessageRecord,
      ]

      const unread = store.unreadByType('message')
      expect(unread).toHaveLength(1)
      expect(unread[0].id).toBe(1)
    })

    it('should calculate total unread count', () => {
      const store = useNotificationStore()
      
      store.messages = [
        { id: 1, type: 'message', status: 0 } as MessageRecord,
        { id: 2, type: 'notice', status: 0 } as MessageRecord,
        { id: 3, type: 'todo', status: 1 } as MessageRecord,
      ]

      expect(store.totalUnread).toBe(2)
    })

    it('should calculate unread count by type', () => {
      const store = useNotificationStore()
      
      store.messages = [
        { id: 1, type: 'message', status: 0 } as MessageRecord,
        { id: 2, type: 'message', status: 0 } as MessageRecord,
        { id: 3, type: 'message', status: 1 } as MessageRecord,
      ]

      expect(store.unreadCountByType('message')).toBe(2)
      expect(store.unreadCountByType('notice')).toBe(0)
    })
  })

  describe('Actions', () => {
    describe('fetchNotifications', () => {
      it('should fetch notifications successfully', async () => {
        const mockData: MessageRecord[] = [
          {
            id: 1,
            type: 'notice',
            title: 'Test Notification',
            subTitle: 'Subtitle',
            content: 'Content',
            time: new Date().toISOString(),
            status: 0,
            messageType: 3,
          },
        ]

        vi.mocked(messageApi.queryMessageList).mockResolvedValue({
          data: mockData,
        } as any)

        const store = useNotificationStore()
        await store.fetchNotifications()

        expect(store.messages).toHaveLength(1)
        expect(store.messages[0].title).toBe('Test Notification')
        expect(store.loading).toBe(false)
      })

      it('should handle fetch error gracefully', async () => {
        vi.mocked(messageApi.queryMessageList).mockRejectedValue(
          new Error('Network error')
        )

        const store = useNotificationStore()
        const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

        await store.fetchNotifications()

        expect(store.loading).toBe(false)
        expect(consoleSpy).toHaveBeenCalled()
        
        consoleSpy.mockRestore()
      })

      it('should normalize data with defaults', async () => {
        const mockData = [
          {
            id: 1,
            // Missing some fields
          },
        ]

        vi.mocked(messageApi.queryMessageList).mockResolvedValue({
          data: mockData,
        } as any)

        const store = useNotificationStore()
        await store.fetchNotifications()

        expect(store.messages[0].title).toBe('Thông báo')
        expect(store.messages[0].subTitle).toBe('')
        expect(store.messages[0].content).toBe('')
      })
    })

    describe('markAsRead', () => {
      it('should mark notifications as read', async () => {
        vi.mocked(messageApi.setMessageStatus).mockResolvedValue({} as any)

        const store = useNotificationStore()
        store.messages = [
          { id: 1, status: 0 } as MessageRecord,
          { id: 2, status: 0 } as MessageRecord,
          { id: 3, status: 0 } as MessageRecord,
        ]

        await store.markAsRead([1, 2])

        expect(store.messages[0].status).toBe(1)
        expect(store.messages[1].status).toBe(1)
        expect(store.messages[2].status).toBe(0)
      })

      it('should handle mark as read error', async () => {
        vi.mocked(messageApi.setMessageStatus).mockRejectedValue(
          new Error('API error')
        )

        const store = useNotificationStore()
        const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

        await expect(store.markAsRead([1])).rejects.toThrow()
        
        consoleSpy.mockRestore()
      })
    })

    describe('markAllAsRead', () => {
      it('should mark all unread notifications as read', async () => {
        vi.mocked(messageApi.setMessageStatus).mockResolvedValue({} as any)

        const store = useNotificationStore()
        store.messages = [
          { id: 1, status: 0 } as MessageRecord,
          { id: 2, status: 0 } as MessageRecord,
          { id: 3, status: 1 } as MessageRecord,
        ]

        await store.markAllAsRead()

        expect(store.messages[0].status).toBe(1)
        expect(store.messages[1].status).toBe(1)
        expect(store.messages[2].status).toBe(1)
      })

      it('should do nothing if no unread notifications', async () => {
        const store = useNotificationStore()
        store.messages = [
          { id: 1, status: 1 } as MessageRecord,
        ]

        await store.markAllAsRead()

        expect(messageApi.setMessageStatus).not.toHaveBeenCalled()
      })
    })

    describe('clearAll', () => {
      it('should clear all notifications', () => {
        const store = useNotificationStore()
        store.messages = [
          { id: 1, status: 0 } as MessageRecord,
          { id: 2, status: 1 } as MessageRecord,
        ]
        store.unreadCount = 1

        store.clearAll()

        expect(store.messages).toEqual([])
        expect(store.unreadCount).toBe(0)
      })
    })

    describe('addNotification', () => {
      it('should add new notification to beginning', () => {
        const store = useNotificationStore()
        store.messages = [
          { id: 1, status: 0 } as MessageRecord,
        ]

        const newNotification: MessageRecord = {
          id: 2,
          type: 'notice',
          title: 'New',
          subTitle: '',
          content: '',
          time: new Date().toISOString(),
          status: 0,
        }

        store.addNotification(newNotification)

        expect(store.messages).toHaveLength(2)
        expect(store.messages[0].id).toBe(2)
      })

      it('should increment unread count for unread notification', () => {
        const store = useNotificationStore()
        store.unreadCount = 2

        store.addNotification({
          id: 1,
          status: 0,
        } as MessageRecord)

        expect(store.unreadCount).toBe(3)
      })

      it('should not increment unread count for read notification', () => {
        const store = useNotificationStore()
        store.unreadCount = 2

        store.addNotification({
          id: 1,
          status: 1,
        } as MessageRecord)

        expect(store.unreadCount).toBe(2)
      })
    })
  })

  describe('WebSocket', () => {
    it('should not connect if already connected', () => {
      const store = useNotificationStore()
      store.wsConnected = true

      store.connectWebSocket()

      // Should not create new client
      expect(store.stompClient).toBeNull()
    })

    it('should not connect if already connecting', () => {
      const store = useNotificationStore()
      store.wsConnecting = true

      store.connectWebSocket()

      expect(store.stompClient).toBeNull()
    })

    it('should disconnect properly', () => {
      const store = useNotificationStore()
      const mockClient = {
        deactivate: vi.fn(),
      }
      store.stompClient = mockClient as any
      store.wsConnected = true

      store.disconnectWebSocket()

      expect(mockClient.deactivate).toHaveBeenCalled()
      expect(store.stompClient).toBeNull()
      expect(store.wsConnected).toBe(false)
      expect(store.wsConnecting).toBe(false)
    })
  })
})
