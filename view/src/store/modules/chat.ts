import { defineStore } from 'pinia'
import { Client, IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
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
import useUserStore from './user'

/**
 * State của chat store
 */
interface ChatState {
  // Danh sách cuộc trò chuyện
  conversations: Conversation[]

  // Tin nhắn của cuộc trò chuyện hiện tại (map: conversationId -> messages[])
  messages: Record<number, ChatMessage[]>

  // Cuộc trò chuyện đang active
  activeConversationId: number | null

  // Tổng số tin nhắn chưa đọc
  totalUnreadCount: number

  // WebSocket connection state
  wsConnected: boolean
  wsConnecting: boolean

  // STOMP client instance
  stompClient: Client | null

  // Loading states
  loadingConversations: boolean
  loadingMessages: boolean
  sendingMessage: boolean
}

const useChatStore = defineStore('chat', {
  state: (): ChatState => ({
    conversations: [],
    messages: {},
    activeConversationId: null,
    totalUnreadCount: 0,
    wsConnected: false,
    wsConnecting: false,
    stompClient: null,
    loadingConversations: false,
    loadingMessages: false,
    sendingMessage: false,
  }),

  getters: {
    /**
     * Lấy cuộc trò chuyện hiện tại
     */
    activeConversation(state): Conversation | null {
      if (!state.activeConversationId) return null
      return state.conversations.find((c) => c.id === state.activeConversationId) || null
    },

    /**
     * Lấy tin nhắn của cuộc trò chuyện hiện tại
     */
    activeMessages(state): ChatMessage[] {
      if (!state.activeConversationId) {
        return []
      }
      return state.messages[state.activeConversationId] || []
    },

    /**
     * Lấy số tin nhắn chưa đọc của 1 conversation
     */
    getUnreadCount: (state) => (conversationId: number) => {
      const conversation = state.conversations.find((c) => c.id === conversationId)
      if (!conversation) return 0

      const userStore = useUserStore()
      const currentUserId = userStore.id

      // Xác định unread count dựa trên user nào đang đăng nhập
      if (currentUserId === conversation.nhanVien1Id) {
        return conversation.unreadCountNv1
      }
      if (currentUserId === conversation.nhanVien2Id) {
        return conversation.unreadCountNv2
      }
      return 0
    },
  },

  actions: {
    /**
     * Lấy danh sách cuộc trò chuyện
     */
    async fetchConversations() {
      this.loadingConversations = true
      try {
        const response = await layDanhSachCuocTroChuyen()
        
        // Handle different response structures
        const data = response.data?.data || response.data || []
        this.conversations = Array.isArray(data) ? data : []
        

        // Cập nhật total unread count
        this.updateTotalUnreadCount()
      } catch (error: any) {
        console.error('❌ Fetch conversations error:', error)
        Message.error(`Không thể tải danh sách cuộc trò chuyện: ${error.message}`)
      } finally {
        this.loadingConversations = false
      }
    },

    /**
     * Lấy tin nhắn của 1 cuộc trò chuyện
     */
    async fetchMessages(otherUserId: number, page = 0, size = 50) {
      this.loadingMessages = true
      try {
        const response = await layDanhSachTinNhan(otherUserId, page, size)
        const conversationId = this.activeConversationId

        if (conversationId) {
          // Handle different response structures
          
          // Try different paths:
          // 1. response.data.data.content (paginated in wrapper)
          // 2. response.data.content (paginated direct)
          // 3. response.data.data (array in wrapper)
          // 4. response.data (direct array)
          const data = response.data?.data || response.data
          
          const messages = data?.content || (Array.isArray(data) ? data : [])
          
          // Lưu tin nhắn vào state
          const messagesArray = Array.isArray(messages) ? messages : []
          
          // Merge với messages đã có trong cache (từ WebSocket)
          const existingMessages = this.messages[conversationId] || []
          const allMessages = [...messagesArray]
          
          // Thêm các message chỉ có trong cache nhưng không có trong API response
          existingMessages.forEach((existingMsg) => {
            const existsInApi = allMessages.some((m) => m.id === existingMsg.id)
            if (!existsInApi) {
              allMessages.push(existingMsg)
            }
          })
          
          // Backend trả về DESC (newest first), cần reverse để hiển thị oldest first
          allMessages.sort((a, b) => new Date(a.sentAt).getTime() - new Date(b.sentAt).getTime())
          
          this.messages[conversationId] = allMessages
          
        } else {
        }
      } catch (error: any) {
        console.error('❌ Fetch messages error:', error)
        Message.error(`Không thể tải tin nhắn: ${error.message}`)
      } finally {
        this.loadingMessages = false
      }
    },

    /**
     * Gửi tin nhắn qua REST API
     */
    async sendMessageViaAPI(request: SendMessageRequest) {
      this.sendingMessage = true
      try {
        const response = await guiTinNhan(request)
        const message = response.data.data

        // Thêm tin nhắn vào state
        this.addMessageToState(message)

        return message
      } catch (error: any) {
        Message.error(`Không thể gửi tin nhắn: ${error.message}`)
        throw error
      } finally {
        this.sendingMessage = false
      }
    },

    /**
     * Gửi tin nhắn qua WebSocket
     */
    async sendMessageViaWebSocket(message: WebSocketSendMessage) {
      if (!this.stompClient || !this.wsConnected) {
        Message.warning('WebSocket chưa kết nối. Đang gửi qua API...')
        // Fallback to API
        return this.sendMessageViaAPI(message)
      }

      this.sendingMessage = true
      
      try {
        // Tạo optimistic message để hiển thị ngay trên UI
        const userStore = useUserStore()
        const optimisticMessage: ChatMessage = {
          id: Date.now(), // Temporary ID
          maTinNhan: `temp-${Date.now()}`,
          senderId: userStore.id!,
          senderName: userStore.name || '',
          receiverId: message.receiverId,
          receiverName: '',
          content: message.content,
          messageType: message.messageType,
          isRead: false,
          sentAt: new Date().toISOString(),
          readAt: null,
        }
        
        // Thêm optimistic message vào UI ngay lập tức
        this.addMessageToState(optimisticMessage)
        
        // Gửi qua WebSocket
        this.stompClient.publish({
          destination: '/app/chat.send',
          body: JSON.stringify(message),
        })
        
      } catch (error: any) {
        Message.error(`Lỗi khi gửi tin nhắn qua WebSocket: ${error.message}`)
        // Fallback to API
        return this.sendMessageViaAPI(message)
      } finally {
        this.sendingMessage = false
      }
    },

    /**
     * Đánh dấu tin nhắn đã đọc
     */
    async markAsRead(senderId: number) {
      try {
        await danhDauDaDoc(senderId)

        // Cập nhật state local
        const userStore = useUserStore()
        const conversation = this.conversations.find(
          (c) =>
            (c.nhanVien1Id === senderId && c.nhanVien2Id === userStore.id) || (c.nhanVien2Id === senderId && c.nhanVien1Id === userStore.id)
        )

        if (conversation) {
          // Reset unread count
          if (userStore.id === conversation.nhanVien1Id) {
            conversation.unreadCountNv1 = 0
          } else if (userStore.id === conversation.nhanVien2Id) {
            conversation.unreadCountNv2 = 0
          }
          this.updateTotalUnreadCount()
          
          // Cập nhật isRead = true cho tất cả messages từ sender trong conversation
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

    /**
     * Lấy số tin nhắn chưa đọc
     */
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

    /**
     * Set active conversation
     */
    setActiveConversation(conversationId: number | null) {
      this.activeConversationId = conversationId
    },

    /**
     * Kết nối WebSocket
     */
    connectWebSocket() {
      if (this.wsConnected || this.wsConnecting) {
        return
      }

      const token = getToken()
      if (!token) {
        return
      }

      this.wsConnecting = true

      // Tạo STOMP client với SockJS
      const client = new Client({
        webSocketFactory: () => new SockJS('http://localhost:8080/ws-chat'),

        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },

        // Debug disabled in production
        debug: () => {},

        reconnectDelay: 5000, // Auto-reconnect sau 5s
        heartbeatOutgoing: 4000,

        onConnect: () => {
          this.wsConnected = true
          this.wsConnecting = false

          const userStore = useUserStore()
          const userId = userStore.id
          const username = userStore.tenTaiKhoan
          

          // Subscribe để nhận tin nhắn
          // Spring WebSocket tự động resolve /user/queue/messages đến session của user hiện tại
          const subscriptionDestination = `/user/queue/messages`
          
          const subscription = client.subscribe(subscriptionDestination, (message: IMessage) => {
            try {
              const chatMessage: ChatMessage = JSON.parse(message.body)
              this.handleIncomingMessage(chatMessage)
            } catch (err) {
              console.error('❌ Error parsing message:', err)
            }
          })
          

          // Subscribe typing notifications
          client.subscribe(`/user/queue/typing`, (message: IMessage) => {
            const notification = JSON.parse(message.body)
            console.log('Typing notification:', notification)
            // TODO: Handle typing indicator in UI
          })
          
          // Subscribe read notifications (khi người nhận đã đọc tin nhắn)
          const readSubscription = client.subscribe(`/user/queue/read`, (message: IMessage) => {
            try {
              const readNotification = JSON.parse(message.body)
              this.handleReadNotification(readNotification)
            } catch (err) {
              console.error('❌ Error parsing read notification:', err)
            }
          })
          
        },

        onStompError: (frame) => {
          console.error('❌ STOMP error:', frame.headers.message)
          console.error('Details:', frame.body)
          this.wsConnected = false
          this.wsConnecting = false
        },

        onWebSocketClose: () => {
          this.wsConnected = false
          this.wsConnecting = false
        },
      })

      client.activate()
      this.stompClient = client
    },

    /**
     * Ngắt kết nối WebSocket
     */
    disconnectWebSocket() {
      if (this.stompClient) {
        this.stompClient.deactivate()
        this.stompClient = null
        this.wsConnected = false
        this.wsConnecting = false
      }
    },

    /**
     * Xử lý tin nhắn đến từ WebSocket
     */
    handleIncomingMessage(message: ChatMessage) {
      console.log('Sender ID:', message.senderId, 'Receiver ID:', message.receiverId)
      console.log('Current user ID:', useUserStore().id)

      // Thêm tin nhắn vào state (nếu đang mở conversation)
      this.addMessageToState(message)

      // Cập nhật conversation list
      this.updateConversationWithNewMessage(message)

      // Refresh unread count
      this.fetchUnreadCount()
      
      // Refresh conversations nếu chưa có
      if (this.conversations.length === 0) {
        this.fetchConversations()
      }
    },

    /**
     * Thêm tin nhắn vào state
     */
    addMessageToState(message: ChatMessage) {
      
      // Tìm conversation chứa tin nhắn này
      const userStore = useUserStore()
      const conversation = this.conversations.find(
        (c) =>
          (c.nhanVien1Id === message.senderId && c.nhanVien2Id === message.receiverId) ||
          (c.nhanVien2Id === message.senderId && c.nhanVien1Id === message.receiverId) ||
          (c.nhanVien1Id === message.receiverId && c.nhanVien2Id === message.senderId) ||
          (c.nhanVien2Id === message.receiverId && c.nhanVien1Id === message.senderId)
      )
      
      if (!conversation) {
        return
      }
      
      
      // Thêm tin nhắn vào conversation đó
      if (!this.messages[conversation.id]) {
        this.messages[conversation.id] = []
      }

      // Kiểm tra duplicate - thay thế optimistic message (temp ID) bằng message thật từ server
      const existingIndex = this.messages[conversation.id].findIndex((m) => 
        m.id === message.id || // Exact ID match
        (m.maTinNhan && m.maTinNhan.startsWith('temp-') && m.content === message.content && m.senderId === message.senderId) // Optimistic message match
      )
      
      if (existingIndex >= 0) {
        // Replace optimistic message with real one from server
        this.messages[conversation.id][existingIndex] = message
      } else {
        // Add new message
        this.messages[conversation.id].push(message)
      }
    },

    /**
     * Cập nhật conversation khi có tin nhắn mới
     */
    updateConversationWithNewMessage(message: ChatMessage) {
      const userStore = useUserStore()
      const conversation = this.conversations.find(
        (c) =>
          (c.nhanVien1Id === message.senderId && c.nhanVien2Id === message.receiverId) ||
          (c.nhanVien2Id === message.senderId && c.nhanVien1Id === message.receiverId)
      )

      if (conversation) {
        conversation.lastMessageContent = message.content
        conversation.lastMessageTime = message.sentAt
        conversation.lastSenderId = message.senderId

        // Tăng unread count nếu không phải tin nhắn của mình
        if (message.senderId !== userStore.id) {
          if (userStore.id === conversation.nhanVien1Id) {
            conversation.unreadCountNv1++
          } else if (userStore.id === conversation.nhanVien2Id) {
            conversation.unreadCountNv2++
          }
        }

        this.updateTotalUnreadCount()
      }
    },

    /**
     * Xử lý thông báo đã đọc từ WebSocket
     */
    handleReadNotification(notification: { senderId: number; receiverId: number; readAt: string }) {
      
      const userStore = useUserStore()
      const currentUserId = userStore.id
      
      // Chỉ xử lý nếu current user là sender (người gửi tin nhắn)
      if (currentUserId !== notification.senderId) {
        return
      }
      
      // Tìm conversation với receiver
      const conversation = this.conversations.find(
        (c) =>
          (c.nhanVien1Id === notification.senderId && c.nhanVien2Id === notification.receiverId) ||
          (c.nhanVien2Id === notification.senderId && c.nhanVien1Id === notification.receiverId)
      )
      
      if (!conversation) {
        return
      }
      
      
      // Cập nhật isRead cho tất cả messages từ sender trong conversation
      const messages = this.messages[conversation.id]
      if (messages) {
        let updatedCount = 0
        messages.forEach((msg) => {
          if (msg.senderId === notification.senderId && msg.receiverId === notification.receiverId && !msg.isRead) {
            msg.isRead = true
            msg.readAt = notification.readAt
            updatedCount++
          }
        })
      }
      
      // Cập nhật unread count trong conversation (cho người kia)
      if (currentUserId === conversation.nhanVien1Id) {
        conversation.unreadCountNv2 = 0
      } else if (currentUserId === conversation.nhanVien2Id) {
        conversation.unreadCountNv1 = 0
      }
    },

    /**
     * Cập nhật tổng số tin nhắn chưa đọc
     */
    updateTotalUnreadCount() {
      const userStore = useUserStore()
      const userId = userStore.id

      if (!this.conversations || this.conversations.length === 0) {
        this.totalUnreadCount = 0
        return
      }

      this.totalUnreadCount = this.conversations.reduce((total, conv) => {
        if (userId === conv.nhanVien1Id) {
          return total + (conv.unreadCountNv1 || 0)
        }
        if (userId === conv.nhanVien2Id) {
          return total + (conv.unreadCountNv2 || 0)
        }
        return total
      }, 0)
    },

    /**
     * Reset toàn bộ state
     */
    resetState() {
      this.conversations = []
      this.messages = {}
      this.activeConversationId = null
      this.totalUnreadCount = 0
      this.disconnectWebSocket()
    },
  },
})

export default useChatStore
