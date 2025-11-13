import { defineStore } from 'pinia'
import { Client, IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import axios from 'axios'
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
  activeConversationUserInitiated: boolean

  // Tổng số tin nhắn chưa đọc
  totalUnreadCount: number

  // Online users tracking
  onlineUsers: Set<number>

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
    activeConversationUserInitiated: false,
    totalUnreadCount: 0,
    onlineUsers: new Set<number>(),
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
        const tempSuffix = `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
        const tempId = Number(`${Date.now()}${Math.floor(Math.random() * 1000)}`)
        const optimisticMessage: ChatMessage = {
          id: Number.isFinite(tempId) ? tempId : Date.now(),
          maTinNhan: `temp-${tempSuffix}`,
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
        // Cập nhật conversation list ngay (preview, time, last sender)
        this.updateConversationWithNewMessage(optimisticMessage)

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
    async markAsRead(senderId: number, conversationId?: number) {
      try {
        await danhDauDaDoc(senderId)

        // Cập nhật state local
        const userStore = useUserStore()
        // Try to find conversation by ID first, then fallback to sender lookup
        let conversation = conversationId ? this.conversations.find((c) => c.id === conversationId) : null

        if (!conversation) {
          conversation = this.conversations.find(
            (c) =>
              (c.nhanVien1Id === senderId && c.nhanVien2Id === userStore.id) ||
              (c.nhanVien2Id === senderId && c.nhanVien1Id === userStore.id)
          )
        }

        if (conversation) {
          const conversationId = conversation.id
          console.log('Marking conversation as read:', conversationId, 'senderId:', senderId)

          // Reset unread count - directly mutate to ensure reactivity
          const conversationIndex = this.conversations.findIndex((c) => c.id === conversationId)
          if (conversationIndex >= 0) {
            if (userStore.id === conversation.nhanVien1Id) {
              this.conversations[conversationIndex].unreadCountNv1 = 0
              console.log('Reset unreadCountNv1 to 0')
            } else if (userStore.id === conversation.nhanVien2Id) {
              this.conversations[conversationIndex].unreadCountNv2 = 0
              console.log('Reset unreadCountNv2 to 0')
            }

            // Also update the conversation reference we have
            conversation.unreadCountNv1 = this.conversations[conversationIndex].unreadCountNv1
            conversation.unreadCountNv2 = this.conversations[conversationIndex].unreadCountNv2
          }

          // Update total unread count immediately
          this.updateTotalUnreadCount()
          console.log('Updated total unread count:', this.totalUnreadCount)

          // Cập nhật isRead = true cho tất cả messages từ sender trong conversation
          const messages = this.messages[conversation.id]
          if (messages) {
            let updatedCount = 0
            messages.forEach((msg) => {
              if (msg.senderId === senderId && !msg.isRead) {
                msg.isRead = true
                msg.readAt = new Date().toISOString()
                updatedCount++
              }
            })
            console.log(`Marked ${updatedCount} messages as read`)
          }
        } else {
          console.warn('Conversation not found for markAsRead, senderId:', senderId, 'conversationId:', conversationId)
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
     * Lấy danh sách users đang online
     */
    async fetchOnlineUsers() {
      try {
        const token = getToken()
        const response = await fetch('http://localhost:8080/api/presence/online-users', {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        })
        const userIds: number[] = await response.json()
        this.onlineUsers = new Set(userIds)
        console.log('📊 Online users:', Array.from(this.onlineUsers))
      } catch (error: any) {
        console.error('Lỗi khi lấy danh sách online users:', error)
      }
    },

    /**
     * Update user presence status
     */
    updateUserPresence(userId: number, status: 'ONLINE' | 'OFFLINE') {
      if (status === 'ONLINE') {
        this.onlineUsers.add(userId)
        console.log(`🟢 User ${userId} is now ONLINE`)
      } else {
        this.onlineUsers.delete(userId)
        console.log(`🔴 User ${userId} is now OFFLINE`)
      }
    },

    /**
     * Set active conversation
     */
    setActiveConversation(conversationId: number | null, options: { userInitiated?: boolean } = {}) {
      this.activeConversationId = conversationId
      this.activeConversationUserInitiated = !!options.userInitiated
    },

    /**
     * Kết nối WebSocket
     */
    connectWebSocket() {
      if (this.wsConnected || this.wsConnecting) {
        console.log('WebSocket already connected or connecting, skipping...')
        return
      }

      const token = getToken()
      if (!token) {
        console.warn('No token available, cannot connect WebSocket')
        return
      }

      this.wsConnecting = true
      console.log('Connecting to WebSocket...')

      // Get base URL from axios defaults or use localhost
      const baseURL = axios.defaults.baseURL || 'http://localhost:8080'
      const wsUrl = `${baseURL}/ws-chat`
      console.log('WebSocket URL:', wsUrl)

      // Tạo STOMP client với SockJS
      const client = new Client({
        webSocketFactory: () => new SockJS(wsUrl),

        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },

        // Enable debug logging to see what's happening
        debug: (str) => {
          console.log('[STOMP Debug]', str)
        },

        reconnectDelay: 5000, // Auto-reconnect sau 5s
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,

        onConnect: (frame) => {
          console.log('WebSocket connected successfully!', frame)
          this.wsConnected = true
          this.wsConnecting = false

          const userStore = useUserStore()
          const userId = userStore.id
          const username = userStore.tenTaiKhoan

          console.log(`Connected as user ${userId} (${username})`)

          // Subscribe để nhận tin nhắn
          // Spring WebSocket tự động resolve /user/queue/messages đến session của user hiện tại
          // Backend sends to: /user/{username}/queue/messages
          // Frontend subscribes to: /user/queue/messages (Spring auto-resolves to current user)
          const subscriptionDestination = `/user/queue/messages`
          console.log(`Subscribing to: ${subscriptionDestination} for user: ${username} (ID: ${userId})`)

          const subscription = client.subscribe(
            subscriptionDestination,
            (message: IMessage) => {
              console.log('========== RAW WEBSOCKET MESSAGE RECEIVED ==========')
              console.log('Message headers:', message.headers)
              console.log('Message body:', message.body)
              console.log('===================================================')

              try {
                const chatMessage: ChatMessage = JSON.parse(message.body)
                console.log('📬 Parsed chat message:', chatMessage)
                console.log('Message details:', {
                  id: chatMessage.id,
                  senderId: chatMessage.senderId,
                  receiverId: chatMessage.receiverId,
                  content: chatMessage.content?.substring(0, 50),
                })

                this.handleIncomingMessage(chatMessage)
                console.log('Message processed successfully')
              } catch (err) {
                console.error('Error parsing message:', err)
                console.error('Raw message body:', message.body)
              }
            },
            {
              // Add headers if needed
            }
          )

          console.log('Subscribed to messages:', subscription.id)
          console.log('Subscription destination:', subscriptionDestination)

          // Store subscription for debugging
          ;(window as any).chatSubscription = subscription

          // Subscribe typing notifications
          client.subscribe(`/user/queue/typing`, (message: IMessage) => {
            const notification = JSON.parse(message.body)
            console.log('⌨Typing notification:', notification)
            // TODO: Handle typing indicator in UI
          })

          // Subscribe read notifications (khi người nhận đã đọc tin nhắn)
          const readSubscription = client.subscribe(`/user/queue/read`, (message: IMessage) => {
            try {
              const readNotification = JSON.parse(message.body)
              console.log('Read notification:', readNotification)
              this.handleReadNotification(readNotification)
            } catch (err) {
              console.error('Error parsing read notification:', err)
            }
          })

          // Subscribe to presence updates (online/offline status)
          client.subscribe('/topic/presence', (message: IMessage) => {
            try {
              const presenceUpdate = JSON.parse(message.body)
              console.log('Presence update:', presenceUpdate)
              this.updateUserPresence(presenceUpdate.userId, presenceUpdate.status)
            } catch (err) {
              console.error('Error parsing presence update:', err)
            }
          })

          // Fetch initial online users
          this.fetchOnlineUsers()
        },

        onStompError: (frame) => {
          console.error('STOMP error:', frame.headers.message)
          console.error('Error details:', frame.body)
          console.error('Error command:', frame.command)
          this.wsConnected = false
          this.wsConnecting = false
          Message.error(`WebSocket error: ${frame.headers.message || 'Connection failed'}`)
        },

        onWebSocketError: (event) => {
          console.error('WebSocket error:', event)
          this.wsConnected = false
          this.wsConnecting = false
          Message.error('WebSocket connection error')
        },

        onWebSocketClose: (event) => {
          console.log('WebSocket closed:', event.code, event.reason)
          this.wsConnected = false
          this.wsConnecting = false
          // Try to reconnect after a delay
          setTimeout(() => {
            if (!this.wsConnected && !this.wsConnecting) {
              console.log('Attempting to reconnect WebSocket...')
              this.connectWebSocket()
            }
          }, 5000)
        },

        onDisconnect: () => {
          console.log('🔌 WebSocket disconnected')
          this.wsConnected = false
          this.wsConnecting = false
        },
      })

      client.activate()
      this.stompClient = client
      console.log('🚀 WebSocket client activated')
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
    async handleIncomingMessage(message: ChatMessage) {
      console.log('========== HANDLE INCOMING MESSAGE ==========')
      console.log('Full message:', message)
      const userStore = useUserStore()
      console.log('Sender ID:', message.senderId, 'Receiver ID:', message.receiverId)
      console.log('Current user ID:', userStore.id)
      console.log('Current user username:', userStore.tenTaiKhoan)

      // Chỉ xử lý nếu liên quan đến mình (mình là người nhận hoặc người gửi)
      if (message.receiverId !== userStore.id && message.senderId !== userStore.id) {
        console.log('Message not related to current user, ignoring')
        console.log(`Current user: ${userStore.id}, sender: ${message.senderId}, receiver: ${message.receiverId}`)
        return
      }

      console.log('Message is for current user, processing...')

      // Thêm tin nhắn vào state
      console.log('Step 1: Adding message to state...')
      this.addMessageToState(message)

      // Cập nhật conversation list (sẽ tự động cập nhật unread count)
      console.log('Step 2: Updating conversation with new message...')
      this.updateConversationWithNewMessage(message)

      // Nếu conversation chưa tồn tại, fetch lại danh sách
      const conversationExists = this.conversations.some(
        (c) =>
          (c.nhanVien1Id === message.senderId && c.nhanVien2Id === message.receiverId) ||
          (c.nhanVien2Id === message.senderId && c.nhanVien1Id === message.receiverId)
      )

      if (!conversationExists) {
        console.log('🔄 Conversation not found, fetching conversations...')
        this.fetchConversations()
      } else {
        // Update total unread count immediately (không cần gọi API)
        console.log('Step 3: Updating total unread count...')
        this.updateTotalUnreadCount()
        console.log(`📊 New total unread count: ${this.totalUnreadCount}`)
      }

      // Auto-mark as read if user is currently viewing this conversation (chỉ khi mình là người nhận)
      // Check if there's an active conversation with the sender
      if (this.activeConversationId && this.activeConversationUserInitiated) {
        const activeConv = this.conversations.find((c) => c.id === this.activeConversationId)
        if (activeConv) {
          const matchesSender =
            (activeConv.nhanVien1Id === message.senderId && activeConv.nhanVien2Id === userStore.id) ||
            (activeConv.nhanVien2Id === message.senderId && activeConv.nhanVien1Id === userStore.id)

          if (matchesSender && message.receiverId === userStore.id) {
            console.log('📖 Auto-marking message as read (user is viewing this conversation)')
            // Mark as read immediately without waiting for API
            try {
              await this.markAsRead(message.senderId, this.activeConversationId)
            } catch (error) {
              console.error('Error auto-marking message as read:', error)
            }
          }
        }
      }

      console.log('✅ ========== MESSAGE HANDLING COMPLETE ==========')
    },

    /**
     * Thêm tin nhắn vào state
     */
    addMessageToState(message: ChatMessage) {
      // Tìm conversation chứa tin nhắn này
      const userStore = useUserStore()
      let conversation = this.conversations.find(
        (c) =>
          (c.nhanVien1Id === message.senderId && c.nhanVien2Id === message.receiverId) ||
          (c.nhanVien2Id === message.senderId && c.nhanVien1Id === message.receiverId) ||
          (c.nhanVien1Id === message.receiverId && c.nhanVien2Id === message.senderId) ||
          (c.nhanVien2Id === message.receiverId && c.nhanVien1Id === message.senderId)
      )

      // Nếu không tìm thấy conversation, tạo tạm thời (sẽ được update sau khi fetch)
      if (!conversation) {
        console.log('Conversation not found for message, creating temporary one')
        // Tạo conversation tạm với ID timestamp
        const tempId = Date.now()
        conversation = {
          id: tempId,
          maCuocTraoDoi: `temp-${tempId}`,
          nhanVien1Id: userStore.id!,
          nhanVien1Name: userStore.name || '',
          nhanVien2Id: message.senderId === userStore.id ? message.receiverId : message.senderId,
          nhanVien2Name: message.senderId === userStore.id ? message.receiverName : message.senderName,
          lastMessageContent: message.content,
          lastMessageTime: message.sentAt,
          lastSenderId: message.senderId,
          unreadCountNv1: 0,
          unreadCountNv2: 0,
        }
        this.conversations.unshift(conversation)
      }

      // Thêm tin nhắn vào conversation đó
      if (!this.messages[conversation.id]) {
        this.messages[conversation.id] = []
      }

      // Kiểm tra duplicate - thay thế optimistic message (temp ID) bằng message thật từ server.
      // Quan trọng: nếu có nhiều optimistic messages có cùng content, chỉ thay thế cái MỚI NHẤT.
      let existingIndex = this.messages[conversation.id].findIndex((m) => m.id === message.id)
      if (existingIndex < 0) {
        // Tìm optimistic có cùng content/sender gần nhất theo thời gian gửi (gần với server sentAt)
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

      // Always append the real message, then remove exactly one matched optimistic (if any).
      this.messages[conversation.id].push(message)
      if (existingIndex >= 0) {
        this.messages[conversation.id].splice(existingIndex, 1)
      }
      // Sort messages by sentAt to maintain order
      this.messages[conversation.id].sort(
        (a, b) => new Date(a.sentAt).getTime() - new Date(b.sentAt).getTime()
      )
    },

    /**
     * Cập nhật conversation khi có tin nhắn mới
     */
    updateConversationWithNewMessage(message: ChatMessage) {
      const userStore = useUserStore()
      let conversation = this.conversations.find(
        (c) =>
          (c.nhanVien1Id === message.senderId && c.nhanVien2Id === message.receiverId) ||
          (c.nhanVien2Id === message.senderId && c.nhanVien1Id === message.receiverId)
      )

      // Nếu conversation chưa tồn tại, tạo một conversation tạm thời
      if (!conversation) {
        console.log('🆕 Creating temporary conversation for new message')
        conversation = {
          id: Date.now(), // Temporary ID
          maCuocTraoDoi: `temp-${Date.now()}`,
          nhanVien1Id: userStore.id!,
          nhanVien1Name: userStore.name || '',
          nhanVien2Id: message.senderId === userStore.id ? message.receiverId : message.senderId,
          nhanVien2Name: message.senderId === userStore.id ? message.receiverName : message.senderName,
          lastMessageContent: message.content,
          lastMessageTime: message.sentAt,
          lastSenderId: message.senderId,
          unreadCountNv1: 0,
          unreadCountNv2: 0,
        }
        this.conversations.unshift(conversation) // Add to beginning
      }

      // Cập nhật conversation
      conversation.lastMessageContent = message.content
      conversation.lastMessageTime = message.sentAt
      conversation.lastSenderId = message.senderId

      // Tăng unread count nếu không phải tin nhắn của mình
      if (message.senderId !== userStore.id) {
        if (userStore.id === conversation.nhanVien1Id) {
          conversation.unreadCountNv1 = (conversation.unreadCountNv1 || 0) + 1
        } else if (userStore.id === conversation.nhanVien2Id) {
          conversation.unreadCountNv2 = (conversation.unreadCountNv2 || 0) + 1
        }
      }

      // Update total unread count immediately
      this.updateTotalUnreadCount()

      // Nếu là conversation tạm, fetch lại để lấy ID thật
      if (conversation.id > 1000000000000) {
        // Temporary ID (timestamp)
        console.log('🔄 Fetching conversations to get real conversation ID...')
        this.fetchConversations()
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
      this.activeConversationUserInitiated = false
      this.totalUnreadCount = 0
      this.disconnectWebSocket()
    },
  },
})

export default useChatStore
