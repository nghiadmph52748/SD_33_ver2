import { defineStore } from 'pinia'
import { Client, IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import axios from '@/api/interceptor'
import {
  layDanhSachCuocTroChuyen,
  layDanhSachTinNhan,
  guiTinNhan,
  danhDauDaDoc,
  laySoTinNhanChuaDoc,
  type ChatMessage,
  type Conversation,
  type SendMessageRequest,
  type WebSocketSendMessage,
} from '@/api/chat'
import { getToken } from '@/utils/auth'
import { Message } from '@arco-design/web-vue'
import { useUserStore } from '@/stores/user'
import i18n from '@/locale'

interface ChatState {
  conversations: Conversation[]
  messages: Record<number, ChatMessage[]>
  activeConversationId: number | null
  activeConversationUserInitiated: boolean
  totalUnreadCount: number
  onlineUsers: Set<number>
  wsConnected: boolean
  wsConnecting: boolean
  stompClient: Client | null
  loadingConversations: boolean
  loadingMessages: boolean
  sendingMessage: boolean
  staffChatUnavailableNotified: boolean
}

const useChatStore = defineStore('chat', {
  state: (): ChatState => ({
    conversations: [],
    messages: {},
    activeConversationId: null,
    activeConversationUserInitiated: false,
    totalUnreadCount: 0,
    onlineUsers: new Set<number>(),
    wsConnected: false,
    wsConnecting: false,
    stompClient: null,
    loadingConversations: false,
    loadingMessages: false,
    sendingMessage: false,
    staffChatUnavailableNotified: false,
  }),

  getters: {
    activeConversation(state): Conversation | null {
      if (!state.activeConversationId) return null
      return state.conversations.find((c) => c.id === state.activeConversationId) || null
    },
    activeMessages(state): ChatMessage[] {
      if (!state.activeConversationId) {
        return []
      }
      return state.messages[state.activeConversationId] || []
    },
    getUnreadCount: (state) => (conversationId: number) => {
      const conversation = state.conversations.find((c) => c.id === conversationId)
      if (!conversation) return 0

      const userStore = useUserStore()
      const currentUserId = userStore.id
      if (!currentUserId) return 0

      // Handle customer-staff conversations
      if (conversation.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
        if (currentUserId === conversation.khachHangId) {
          return conversation.unreadCountNv1 || 0
        }
        if (currentUserId === conversation.nhanVienId) {
          return conversation.unreadCountNv2 || 0
        }
      } else {
        // Handle staff-staff conversations
        if (currentUserId === conversation.nhanVien1Id) {
          return conversation.unreadCountNv1 || 0
        }
        if (currentUserId === conversation.nhanVien2Id) {
          return conversation.unreadCountNv2 || 0
        }
      }
      return 0
    },
  },

  actions: {
    // Removed ensureStaffChatAvailable - customers can now use chat

    async fetchConversations() {
      const token = getToken()
      const userStore = useUserStore()
      
      // Skip fetch for guest users - they don't have permission
      if (!token || !userStore.isAuthenticated) {
        this.conversations = []
        this.updateTotalUnreadCount()
        this.loadingConversations = false
        return
      }
      
      this.loadingConversations = true
      try {
        const response = await layDanhSachCuocTroChuyen()
        const data = response.data?.data || response.data || []
        this.conversations = Array.isArray(data) ? data : []
        this.updateTotalUnreadCount()
      } catch (error: any) {
        // For 403 errors (unauthorized), silently fail and return empty array
        // This is expected for guest users or when service is unavailable
        if (error.response?.status === 403) {
          this.conversations = []
          this.updateTotalUnreadCount()
          // Don't log error for guest users - this is expected
        } else {
          console.error('❌ Fetch conversations error:', error)
          // Only show error for other types of errors
          Message.error(`Không thể tải danh sách cuộc trò chuyện: ${error.message}`)
        }
      } finally {
        this.loadingConversations = false
      }
    },

    async fetchMessages(otherUserId: number, page = 0, size = 50) {
      this.loadingMessages = true
      try {
        const response = await layDanhSachTinNhan(otherUserId, page, size)
        const conversationId = this.activeConversationId

        if (conversationId) {
          const data = response.data?.data || response.data
          const messages = data?.content || (Array.isArray(data) ? data : [])
          const messagesArray = Array.isArray(messages) ? messages : []
          const existingMessages = this.messages[conversationId] || []
          const allMessages = [...messagesArray]

          existingMessages.forEach((existingMsg) => {
            const existsInApi = allMessages.some((m) => m.id === existingMsg.id)
            if (!existsInApi) {
              allMessages.push(existingMsg)
            }
          })

          allMessages.sort((a, b) => new Date(a.sentAt).getTime() - new Date(b.sentAt).getTime())
          this.messages[conversationId] = allMessages
        }
      } catch (error: any) {
        console.error('❌ Fetch messages error:', error)
        Message.error(`Không thể tải tin nhắn: ${error.message}`)
      } finally {
        this.loadingMessages = false
      }
    },

    async sendMessageViaAPI(request: SendMessageRequest) {
      this.sendingMessage = true
      try {
        const response = await guiTinNhan(request)
        const message = response.data.data
        this.addMessageToState(message)
        return message
      } catch (error: any) {
        Message.error(`Không thể gửi tin nhắn: ${error.message}`)
        throw error
      } finally {
        this.sendingMessage = false
      }
    },

    async sendMessageViaWebSocket(message: WebSocketSendMessage) {
      if (!this.stompClient || !this.wsConnected) {
        Message.warning('WebSocket chưa kết nối. Đang gửi qua API...')
        return this.sendMessageViaAPI(message)
      }

      this.sendingMessage = true

      try {
        const userStore = useUserStore()
        // Allow guest users to send messages (guest ID will be handled by backend)
        // For guest users, senderId can be null or a temporary guest ID
        const senderId = userStore.id || null
        
        // For guest users, create a temporary optimistic message
        // Backend will assign proper ID when message is saved
        const tempSuffix = `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
        const tempId = Number(`${Date.now()}${Math.floor(Math.random() * 1000)}`)
        const optimisticMessage: ChatMessage = {
          id: Number.isFinite(tempId) ? tempId : Date.now(),
          maTinNhan: `temp-${tempSuffix}`,
          senderId: senderId || 0, // Use 0 for guest, backend will handle
          senderName: userStore.name || 'Khách',
          receiverId: message.receiverId,
          receiverName: '',
          content: message.content,
          messageType: message.messageType,
          isRead: false,
          sentAt: new Date().toISOString(),
          readAt: null,
        }

        // Add optimistic message for both authenticated and guest users
        // This ensures the message appears immediately in the UI
        this.addMessageToState(optimisticMessage)
        this.updateConversationWithNewMessage(optimisticMessage)

        this.stompClient.publish({
          destination: '/app/chat.send',
          body: JSON.stringify(message),
        })
      } catch (error: any) {
        Message.error(`Lỗi khi gửi tin nhắn qua WebSocket: ${error.message}`)
        return this.sendMessageViaAPI(message)
      } finally {
        this.sendingMessage = false
      }
    },

    async markAsRead(senderId: number, conversationId?: number) {
      try {
        await danhDauDaDoc(senderId)

        const userStore = useUserStore()
        const currentUserId = userStore.id
        if (!currentUserId) return

        let conversation = conversationId ? this.conversations.find((c) => c.id === conversationId) : null
        if (!conversation) {
          conversation = this.conversations.find((c) => {
            // Handle customer-staff conversations
            if (c.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
              return (
                (c.khachHangId === senderId && c.nhanVienId === currentUserId) ||
                (c.nhanVienId === senderId && c.khachHangId === currentUserId)
              )
            }
            // Handle staff-staff conversations
            return (
              (c.nhanVien1Id === senderId && c.nhanVien2Id === currentUserId) ||
              (c.nhanVien2Id === senderId && c.nhanVien1Id === currentUserId)
            )
          })
        }

        if (conversation) {
          const idx = this.conversations.findIndex((c) => c.id === conversation.id)
          if (idx >= 0) {
            if (conversation.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
              // Customer-staff conversation
              if (currentUserId === conversation.khachHangId) {
                this.conversations[idx].unreadCountNv1 = 0
              } else if (currentUserId === conversation.nhanVienId) {
                this.conversations[idx].unreadCountNv2 = 0
              }
            } else {
              // Staff-staff conversation
              if (currentUserId === conversation.nhanVien1Id) {
                this.conversations[idx].unreadCountNv1 = 0
              } else if (currentUserId === conversation.nhanVien2Id) {
                this.conversations[idx].unreadCountNv2 = 0
              }
            }
            conversation.unreadCountNv1 = this.conversations[idx].unreadCountNv1
            conversation.unreadCountNv2 = this.conversations[idx].unreadCountNv2
          }

          this.updateTotalUnreadCount()

          const messages = this.messages[conversation.id]
          if (messages) {
            messages.forEach((msg) => {
              if (msg.senderId === senderId && !msg.isRead) {
                msg.isRead = true
                msg.readAt = new Date().toISOString()
              }
            })
          }
        }
      } catch (error: any) {
        console.error('Lỗi khi đánh dấu đã đọc:', error)
      }
    },

    async fetchUnreadCount() {
      try {
        const response = await laySoTinNhanChuaDoc()
        const count = response.data?.data ?? response.data ?? 0
        this.totalUnreadCount = typeof count === 'number' ? count : 0
      } catch (error: any) {
        console.error('Lỗi khi lấy số tin chưa đọc:', error)
        this.totalUnreadCount = 0
      }
    },

    async fetchOnlineUsers() {
      const token = getToken()
      const userStore = useUserStore()
      
      // Skip fetch for guest users - they don't have permission and don't need online users list
      // Guest can still chat, backend will assign staff automatically
      if (!token || !userStore.isAuthenticated) {
        this.onlineUsers = new Set()
        return
      }
      
      try {
        const baseUrl = axios.defaults.baseURL || 'http://localhost:8080'
        const response = await fetch(`${baseUrl}/api/presence/online-users`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        })
        if (!response.ok) {
          // For 403 errors, silently fail and return empty set
          // This means no staff is available or service is unavailable
          if (response.status === 403) {
            this.onlineUsers = new Set()
            return
          }
          throw new Error('Fetch online users failed')
        }
        const userIds: number[] = await response.json()
        this.onlineUsers = new Set(userIds)
      } catch (error: any) {
        // For 403 errors, silently fail - this is expected for guest users
        // Check if it's a fetch error with status 403
        if (error.message?.includes('403') || error.status === 403) {
          this.onlineUsers = new Set()
          // Don't log error for guest users - this is expected
        } else {
          console.error('Lỗi khi lấy danh sách online users:', error)
          // Set empty set on any error to indicate no staff available
          this.onlineUsers = new Set()
        }
      }
    },

    updateUserPresence(userId: number, status: 'ONLINE' | 'OFFLINE') {
      if (status === 'ONLINE') {
        this.onlineUsers.add(userId)
      } else {
        this.onlineUsers.delete(userId)
      }
    },

    setActiveConversation(conversationId: number | null, options: { userInitiated?: boolean } = {}) {
      this.activeConversationId = conversationId
      this.activeConversationUserInitiated = !!options.userInitiated
    },

    connectWebSocket() {

      if (this.wsConnected || this.wsConnecting) {
        return
      }

      const token = getToken()
      const userStore = useUserStore()
      const isGuest = !token || !userStore.isAuthenticated
      
      this.wsConnecting = true

      const baseURL = axios.defaults.baseURL || 'http://localhost:8080'
      const wsUrl = `${baseURL}/ws-chat/sockjs`

      // Build connect headers - include token if available, otherwise use guest chat header
      const connectHeaders: Record<string, string> = {}
      if (token) {
        connectHeaders.Authorization = `Bearer ${token}`
      } else if (isGuest) {
        // For guest users, send special header to allow anonymous connection
        connectHeaders['x-guest-chat'] = 'true'
      }

      const client = new Client({
        webSocketFactory: () => new SockJS(wsUrl),
        connectHeaders,
        debug: (str) => {
          console.log('[STOMP Debug]', str)
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: (frame) => {
          this.wsConnected = true
          this.wsConnecting = false

          const userStore = useUserStore()
          const username = userStore.tenTaiKhoan

          const subscriptionDestination = `/user/queue/messages`
          const subscription = client.subscribe(
            subscriptionDestination,
            (message: IMessage) => {
              try {
                const chatMessage: ChatMessage = JSON.parse(message.body)
                this.handleIncomingMessage(chatMessage)
              } catch (err) {
                console.error('Error parsing message:', err)
              }
            }
          )
          ;(window as any).storefrontChatSubscription = subscription

          client.subscribe(`/user/queue/typing`, (message: IMessage) => {
            try {
              const notification = JSON.parse(message.body)
              console.log('⌨ Typing notification:', notification)
            } catch (err) {
              console.error('Error parsing typing notification:', err)
            }
          })

          client.subscribe(`/user/queue/read`, (message: IMessage) => {
            try {
              const readNotification = JSON.parse(message.body)
              this.handleReadNotification(readNotification)
            } catch (err) {
              console.error('Error parsing read notification:', err)
            }
          })

          client.subscribe('/topic/presence', (message: IMessage) => {
            try {
              const presenceUpdate = JSON.parse(message.body)
              this.updateUserPresence(presenceUpdate.userId, presenceUpdate.status)
            } catch (err) {
              console.error('Error parsing presence update:', err)
            }
          })

          this.fetchOnlineUsers()
        },
        onStompError: (frame) => {
          console.error('STOMP error:', frame.headers.message)
          this.wsConnected = false
          this.wsConnecting = false
          if (!this.staffChatUnavailableNotified) {
            Message.error(`WebSocket error: ${frame.headers.message || 'Connection failed'}`)
            this.staffChatUnavailableNotified = true
          }
        },
        onWebSocketError: (event) => {
          console.error('WebSocket error:', event)
          this.wsConnected = false
          this.wsConnecting = false
          if (!this.staffChatUnavailableNotified) {
            Message.error('WebSocket connection error')
            this.staffChatUnavailableNotified = true
          }
        },
        onWebSocketClose: () => {
          this.wsConnected = false
          this.wsConnecting = false
          setTimeout(() => {
            if (!this.wsConnected && !this.wsConnecting) {
              this.connectWebSocket()
            }
          }, 5000)
        },
        onDisconnect: () => {
          this.wsConnected = false
          this.wsConnecting = false
        },
      })

      client.activate()
      this.stompClient = client
    },

    disconnectWebSocket() {
      if (this.stompClient) {
        this.stompClient.deactivate()
        this.stompClient = null
        this.wsConnected = false
        this.wsConnecting = false
      }
    },

    async handleIncomingMessage(message: ChatMessage) {
      const userStore = useUserStore()
      const currentUserId = userStore.id
      const isGuest = !currentUserId || !userStore.isAuthenticated

      // For authenticated users, check if message is for them
      if (!isGuest) {
        if (message.receiverId !== currentUserId && message.senderId !== currentUserId) {
          return
        }
      }
      // For guest users, we accept all messages and let addMessageToState filter them
      // This allows guest users to receive messages from staff

      this.addMessageToState(message)
      this.updateConversationWithNewMessage(message)

      const conversationExists = this.conversations.some((c) => {
        // Handle customer-staff conversations
        if (c.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
          return (
            (c.khachHangId === message.senderId && c.nhanVienId === message.receiverId) ||
            (c.nhanVienId === message.senderId && c.khachHangId === message.receiverId)
          )
        }
        // Handle staff-staff conversations
        return (
          (c.nhanVien1Id === message.senderId && c.nhanVien2Id === message.receiverId) ||
          (c.nhanVien2Id === message.senderId && c.nhanVien1Id === message.receiverId)
        )
      })

      if (!conversationExists) {
        // Only fetch conversations for authenticated users
        const userStore = useUserStore()
        if (userStore.isAuthenticated) {
          this.fetchConversations()
        }
      } else {
        this.updateTotalUnreadCount()
      }

      if (this.activeConversationId && this.activeConversationUserInitiated) {
        const activeConv = this.conversations.find((c) => c.id === this.activeConversationId)
        if (activeConv) {
          let matchesSender = false
          if (activeConv.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
            matchesSender =
              (activeConv.khachHangId === message.senderId && activeConv.nhanVienId === currentUserId) ||
              (activeConv.nhanVienId === message.senderId && activeConv.khachHangId === currentUserId)
          } else {
            matchesSender =
              (activeConv.nhanVien1Id === message.senderId && activeConv.nhanVien2Id === currentUserId) ||
              (activeConv.nhanVien2Id === message.senderId && activeConv.nhanVien1Id === currentUserId)
          }

          if (matchesSender && message.receiverId === currentUserId) {
            try {
              await this.markAsRead(message.senderId, this.activeConversationId)
            } catch (error) {
              console.error('Error auto-marking message as read:', error)
            }
          }
        }
      }
    },

    addMessageToState(message: ChatMessage) {
      const userStore = useUserStore()
      const currentUserId = userStore.id
      const isGuest = !currentUserId || !userStore.isAuthenticated

      let conversation = this.conversations.find((c) => {
        // Handle customer-staff conversations
        if (c.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
          // Match conversation where message direction is exact:
          // - Guest sends to staff: senderId === khachHangId && receiverId === nhanVienId
          // - Staff sends to guest: senderId === nhanVienId && receiverId === khachHangId
          // Only match exact direction, not reversed, to avoid matching wrong conversations
          return (
            (c.khachHangId === message.senderId && c.nhanVienId === message.receiverId) ||
            (c.nhanVienId === message.senderId && c.khachHangId === message.receiverId)
          )
        }
        // Handle staff-staff conversations
        return (
          (c.nhanVien1Id === message.senderId && c.nhanVien2Id === message.receiverId) ||
          (c.nhanVien2Id === message.senderId && c.nhanVien1Id === message.receiverId)
        )
      })

      // For guest users, create a temporary conversation if needed
      // Guest users receive messages from staff, so we need to handle this case
      if (!conversation) {
        if (isGuest) {
          // For guest users, create a temporary CUSTOMER_STAFF conversation
          // When staff sends to guest: senderId = staff ID, receiverId = guest customer ID
          // When guest sends to staff: senderId = guest customer ID (or 0), receiverId = staff ID
          const isStaffToGuest = message.senderId && message.senderId !== 0 && 
                                 message.receiverId && message.receiverId !== 0 &&
                                 message.receiverId !== message.senderId
          
          if (isStaffToGuest) {
            // Message from staff to guest - receiverId is guest customer ID
            const tempId = Date.now()
            conversation = {
              id: tempId,
              maCuocTraoDoi: `temp-guest-${tempId}`,
              loaiCuocTraoDoi: 'CUSTOMER_STAFF',
              khachHangId: message.receiverId, // Guest customer ID
              khachHangName: 'Khách',
              nhanVienId: message.senderId,
              nhanVienName: message.senderName || 'Nhân viên',
              lastMessageContent: message.content,
              lastMessageTime: message.sentAt,
              lastSenderId: message.senderId,
              unreadCountNv1: 0,
              unreadCountNv2: 0,
            } as Conversation
            this.conversations.unshift(conversation)
          } else if (message.senderId && message.senderId !== 0 && message.receiverId && message.receiverId !== 0) {
            // Message from guest to staff - senderId is guest customer ID
            const tempId = Date.now()
            conversation = {
              id: tempId,
              maCuocTraoDoi: `temp-guest-${tempId}`,
              loaiCuocTraoDoi: 'CUSTOMER_STAFF',
              khachHangId: message.senderId, // Guest customer ID
              khachHangName: 'Khách',
              nhanVienId: message.receiverId,
              nhanVienName: message.receiverName || 'Nhân viên',
              lastMessageContent: message.content,
              lastMessageTime: message.sentAt,
              lastSenderId: message.senderId,
              unreadCountNv1: 0,
              unreadCountNv2: 0,
            } as Conversation
            this.conversations.unshift(conversation)
          }
        } else if (currentUserId) {
          // Authenticated user - create temporary conversation
          const tempId = Date.now()
          conversation = {
            id: tempId,
            maCuocTraoDoi: `temp-${tempId}`,
            loaiCuocTraoDoi: 'STAFF_STAFF', // Default, will be corrected by backend
            nhanVien1Id: currentUserId,
            nhanVien1Name: userStore.name || '',
            nhanVien2Id: message.senderId === currentUserId ? message.receiverId : message.senderId,
            nhanVien2Name: message.senderId === currentUserId ? message.receiverName : message.senderName,
            lastMessageContent: message.content,
            lastMessageTime: message.sentAt,
            lastSenderId: message.senderId,
            unreadCountNv1: 0,
            unreadCountNv2: 0,
          } as Conversation
          this.conversations.unshift(conversation)
        }
      }

      if (!conversation) return

      if (!this.messages[conversation.id]) {
        this.messages[conversation.id] = []
      }

      let existingIndex = this.messages[conversation.id].findIndex((m) => m.id === message.id)
      if (existingIndex < 0) {
        const serverTs = message.sentAt ? new Date(message.sentAt as any).getTime() : Number.MAX_SAFE_INTEGER
        let bestIndex = -1
        let bestDelta = Number.MAX_SAFE_INTEGER
        for (let i = 0; i < this.messages[conversation.id].length; i++) {
          const m = this.messages[conversation.id][i]
          if (
            m?.maTinNhan &&
            typeof m.maTinNhan === 'string' &&
            m.maTinNhan.startsWith('temp-') &&
            m.content === message.content &&
            m.senderId === message.senderId
          ) {
            const mTs = m.sentAt ? new Date(m.sentAt as any).getTime() : 0
            const delta = Math.abs(serverTs - mTs)
            if (delta < bestDelta) {
              bestDelta = delta
              bestIndex = i
            }
          }
        }
        existingIndex = bestIndex
      }

      this.messages[conversation.id].push(message)
      if (existingIndex >= 0) {
        this.messages[conversation.id].splice(existingIndex, 1)
      }
      this.messages[conversation.id].sort((a, b) => new Date(a.sentAt).getTime() - new Date(b.sentAt).getTime())
    },

    updateConversationWithNewMessage(message: ChatMessage) {
      const userStore = useUserStore()
      const currentUserId = userStore.id
      if (!currentUserId) return

      let conversation = this.conversations.find((c) => {
        // Handle customer-staff conversations
        if (c.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
          return (
            (c.khachHangId === message.senderId && c.nhanVienId === message.receiverId) ||
            (c.nhanVienId === message.senderId && c.khachHangId === message.receiverId)
          )
        }
        // Handle staff-staff conversations
        return (
          (c.nhanVien1Id === message.senderId && c.nhanVien2Id === message.receiverId) ||
          (c.nhanVien2Id === message.senderId && c.nhanVien1Id === message.receiverId)
        )
      })

      if (!conversation) {
        conversation = {
          id: Date.now(),
          maCuocTraoDoi: `temp-${Date.now()}`,
          loaiCuocTraoDoi: 'STAFF_STAFF', // Default, will be corrected by backend
          nhanVien1Id: currentUserId,
          nhanVien1Name: userStore.name || '',
          nhanVien2Id: message.senderId === currentUserId ? message.receiverId : message.senderId,
          nhanVien2Name: message.senderId === currentUserId ? message.receiverName : message.senderName,
          lastMessageContent: message.content,
          lastMessageTime: message.sentAt,
          lastSenderId: message.senderId,
          unreadCountNv1: 0,
          unreadCountNv2: 0,
        } as Conversation
        this.conversations.unshift(conversation)
      }

      conversation.lastMessageContent = message.content
      conversation.lastMessageTime = message.sentAt
      conversation.lastSenderId = message.senderId

      // Note: Backend handles unread count, so we don't increment here
      // This is just for temporary conversations

      this.updateTotalUnreadCount()

      if (conversation.id > 1000000000000) {
        // Only fetch conversations for authenticated users
        const userStore = useUserStore()
        if (userStore.isAuthenticated) {
          this.fetchConversations()
        }
      }
    },

    handleReadNotification(notification: { senderId: number; receiverId: number; readAt: string }) {
      const userStore = useUserStore()
      const currentUserId = userStore.id
      if (!currentUserId || currentUserId !== notification.senderId) {
        return
      }

      const conversation = this.conversations.find((c) => {
        // Handle customer-staff conversations
        if (c.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
          return (
            (c.khachHangId === notification.senderId && c.nhanVienId === notification.receiverId) ||
            (c.nhanVienId === notification.senderId && c.khachHangId === notification.receiverId)
          )
        }
        // Handle staff-staff conversations
        return (
          (c.nhanVien1Id === notification.senderId && c.nhanVien2Id === notification.receiverId) ||
          (c.nhanVien2Id === notification.senderId && c.nhanVien1Id === notification.receiverId)
        )
      })

      if (!conversation) {
        return
      }

      const messages = this.messages[conversation.id]
      if (messages) {
        messages.forEach((msg) => {
          if (msg.senderId === notification.senderId && msg.receiverId === notification.receiverId && !msg.isRead) {
            msg.isRead = true
            msg.readAt = notification.readAt
          }
        })
      }

      if (conversation.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
        if (currentUserId === conversation.khachHangId) {
          conversation.unreadCountNv2 = 0
        } else if (currentUserId === conversation.nhanVienId) {
          conversation.unreadCountNv1 = 0
        }
      } else {
        if (currentUserId === conversation.nhanVien1Id) {
          conversation.unreadCountNv2 = 0
        } else if (currentUserId === conversation.nhanVien2Id) {
          conversation.unreadCountNv1 = 0
        }
      }
    },

    updateTotalUnreadCount() {
      const userStore = useUserStore()
      const userId = userStore.id
      if (!userId || !this.conversations || this.conversations.length === 0) {
        this.totalUnreadCount = 0
        return
      }

      this.totalUnreadCount = this.conversations.reduce((total, conv) => {
        // Handle customer-staff conversations
        if (conv.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
          if (userId === conv.khachHangId) {
            return total + (conv.unreadCountNv1 || 0)
          }
          if (userId === conv.nhanVienId) {
            return total + (conv.unreadCountNv2 || 0)
          }
        } else {
          // Handle staff-staff conversations
          if (userId === conv.nhanVien1Id) {
            return total + (conv.unreadCountNv1 || 0)
          }
          if (userId === conv.nhanVien2Id) {
            return total + (conv.unreadCountNv2 || 0)
          }
        }
        return total
      }, 0)
    },

    resetState() {
      this.conversations = []
      this.messages = {}
      this.activeConversationId = null
      this.activeConversationUserInitiated = false
      this.totalUnreadCount = 0
      this.disconnectWebSocket()
      this.staffChatUnavailableNotified = false
    },
  },
})

export default useChatStore
