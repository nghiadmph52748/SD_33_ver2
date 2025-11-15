<template>
  <div class="ai-chatbot" :class="{ 'is-dark': isDark }">
    <a-card class="chatbot-card" :bordered="false" :body-style="{ padding: '0' }">
      <template #title>
        <div class="chatbot-header">
          <span class="title">
            <img
              src="//p3-armor.byteimg.com/tos-cn-i-49unhts6dw/dfdba5317c0c20ce20e64fac803d52bc.svg~tplv-49unhts6dw-image.image"
              alt="AI Icon"
              style="width: 20px; height: 20px; vertical-align: middle; margin-right: 6px"
            />
            Trợ Lý AI
          </span>
          <a-space>
            <a-badge :status="isConnected ? 'success' : 'error'" :text="isConnected ? 'Online' : 'Offline'" />
          </a-space>
        </div>
      </template>

      <!-- Messages Container -->
      <div class="messages-container" ref="messagesContainer" :style="messagesContainerStyle">
        <!-- Empty state for new conversations -->
        <div v-if="messages.length === 0 && !loading" class="empty-messages">
          <div class="empty-content">
            <div class="empty-icon">💬</div>
            <div class="empty-title">Chào mừng đến với GearUp AI!</div>
            <div class="empty-description">Hãy bắt đầu cuộc trò chuyện bằng cách đặt câu hỏi hoặc sử dụng các gợi ý bên dưới.</div>
          </div>
        </div>

        <div
          v-for="msg in messages"
          :key="msg.id"
          :class="['message', msg.role, { 'suggestions-only': !msg.content && msg.followUpSuggestions }]"
        >
          <div class="message-wrapper">
            <div class="avatar">
              <a-avatar v-if="msg.role === 'user'" :size="40" :style="userAvatarStyle">
                {{ userInitials }}
              </a-avatar>
              <img
                v-else
                src="//p3-armor.byteimg.com/tos-cn-i-49unhts6dw/dfdba5317c0c20ce20e64fac803d52bc.svg~tplv-49unhts6dw-image.image"
                alt="AI"
                style="width: 28px; height: 28px"
              />
            </div>
            <div class="content">
              <!-- Spinning animation - show FIRST when processing but no content yet -->
              <div
                v-if="msg.role === 'assistant' && (msg.processingStatus === 'querying' || msg.processingStatus === 'analyzing') && (!msg.content || msg.content.trim().length === 0)"
                class="processing-indicator"
              >
                <a-spin :size="16" />
                <span style="margin-left: 8px">Đang trả lời...</span>
              </div>

              <!-- Content display with streaming animation -->
              <div v-if="msg.content && msg.content.trim().length > 0" class="text">
                <span v-html="renderMarkdown(msg.content)"></span><span v-if="msg.processingStatus === 'analyzing' || (isProcessing && msg.id === messages[messages.length - 1]?.id)" class="streaming-cursor">▋</span>
              </div>

              <!-- Product Cards - Only show in separate message for product suggestions -->
              <div v-if="msg.role === 'assistant' && msg.products && msg.products.length > 0 && msg.processingStatus === 'ready' && msg.isProductSuggestion === true" class="product-cards">
                <ProductCard v-for="product in msg.products" :key="product.id" :product="product" />
              </div>

              <!-- Follow-up Suggestions (only for messages WITHOUT content AND not processing) -->
              <div
                v-if="
                  msg.role === 'assistant' &&
                  msg.isSuggestionsOnly === true &&
                  msg.followUpSuggestions &&
                  msg.followUpSuggestions.length > 0 &&
                  !isProcessing
                "
                class="follow-up-suggestions"
              >
                <a-space wrap :size="6" class="suggestions-buttons">
                  <a-button
                    v-for="(suggestion, idx) in msg.followUpSuggestions"
                    :key="idx"
                    size="mini"
                    type="text"
                    :disabled="!isConnected || isProcessing"
                    @click="askQuestion(suggestion)"
                    class="suggestion-btn"
                  >
                    {{ suggestion }}
                  </a-button>
                </a-space>
              </div>

              <div v-if="msg.timestamp" class="timestamp">{{ msg.timestamp }}</div>
            </div>
          </div>
        </div>

        <!-- Loading indicator -->
        <div v-if="loading" class="message assistant">
          <div class="message-wrapper">
            <div class="avatar">
              <img
                src="//p3-armor.byteimg.com/tos-cn-i-49unhts6dw/dfdba5317c0c20ce20e64fac803d52bc.svg~tplv-49unhts6dw-image.image"
                alt="AI"
                style="width: 28px; height: 28px"
              />
            </div>
            <div class="content">
              <a-spin :size="16" />
              Đang nhập...
            </div>
          </div>
        </div>
      </div>

      <!-- Input Box -->
      <div class="input-container">
        <a-input-search
          v-model="input"
          :placeholder="staffChatMode ? 'Nhập tin nhắn cho nhân viên...' : (isConnected ? 'Hỏi AI bất cứ điều gì về sản phẩm...' : 'AI đang offline...')"
          :loading="loading || isProcessing"
          :disabled="staffChatMode ? (!chatStore.wsConnected || chatStore.sendingMessage) : (!isConnected || isProcessing)"
          allow-clear
          search-button
          @search="handleSearch"
          @press-enter="handleSearch"
        >
          <template #button-icon>
            <icon-send />
          </template>
          <template #button-default>{{ staffChatMode ? (chatStore.sendingMessage ? 'Đang gửi...' : 'Gửi') : (isProcessing ? 'Đang xử lý...' : 'Gửi') }}</template>
        </a-input-search>
      </div>
    </a-card>

    <!-- Expanded view removed -->
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { parse as markedParse } from 'marked'
import { chatWithAI, chatWithAIStream, checkAIHealth } from '@/api/ai'
import { Message } from '@arco-design/web-vue'
import { IconSend } from '@arco-design/web-vue/es/icon'
import { useUserStore } from '@/stores/user'
import useChatStore from '@/stores/chat'
import { luuLichSuAiChat } from '@/api/chat'
import ProductCard from './ProductCard.vue'

interface Product {
  id: number
  name: string
  min_price: number
  max_price: number
  image_url?: string
  stock: number
}

interface ChatMessage {
  id: number
  role: 'user' | 'assistant'
  content: string
  sources?: string
  timestamp: string
  dataSource?: string
  followUpSuggestions?: string[]
  queryType?: string
  processingStatus?: string
  isSuggestionsOnly?: boolean
  isProductSuggestion?: boolean // Flag to indicate this message is for product cards only
  products?: Product[]
}

interface SessionState {
  sessions: Record<string, ChatMessage[]>
  currentSessionId: string
  sessionNames: Record<string, string>
}

const props = defineProps<{
  suppressConnectionNotice?: boolean
  enableHealthCheck?: boolean
  connectionStatus?: boolean
  staffChatMode?: boolean
  staffConversationId?: number | null
  staffReceiverId?: number | null
}>()
const shouldNotifyConnection = props.suppressConnectionNotice !== true
const shouldHealthCheck = props.enableHealthCheck === true

const emit = defineEmits<{
  'session-state': [state: SessionState]
  'redirect-to-staff': []
  'staff-message-sent': [message: string]
}>()

const messages = ref<ChatMessage[]>([
  {
    id: 0,
    role: 'assistant',
    content:
      'Xin chào! Tôi là trợ lý AI của GearUp. Tôi có thể giúp bạn tra cứu thông tin về sản phẩm, chương trình khuyến mãi và đơn hàng. Bạn cần giúp gì không? 😊',
    timestamp: new Date().toLocaleTimeString('vi-VN'),
  },
  {
    id: -1,
    role: 'assistant',
    content: '',
    timestamp: '',
    followUpSuggestions: [
      'Sản phẩm nào đang giảm giá?',
      'Tôi muốn biết kích cỡ phù hợp',
      'Chính sách đổi trả là gì?',
      'Gợi ý cho tôi giày chạy bộ',
    ],
    isSuggestionsOnly: true,
  },
])

const userStore = useUserStore()
const chatStore = useChatStore()
const input = ref('')
const loading = ref(false)
const isProcessing = ref(false)
const isConnected = ref(props.connectionStatus ?? !shouldHealthCheck)
const staffChatMode = computed(() => props.staffChatMode ?? false)

watch(
  () => props.connectionStatus,
  (newStatus) => {
    if (newStatus !== undefined) {
      isConnected.value = newStatus
    }
  },
  { immediate: true }
)
const messagesContainer = ref<HTMLElement | null>(null)
const isDark = ref(document.body.hasAttribute('arco-theme'))
const messagesContainerStyle = ref<Record<string, string>>({})
const currentSessionId = ref<string>('')
const chatSessions = ref<Record<string, ChatMessage[]>>({})
const sessionNames = ref<Record<string, string>>({})

const userInitials = computed(() => {
  const displayName = (userStore.name || userStore.profile?.tenTaiKhoan || '').trim()
  if (!displayName) {
    return 'U'
  }
  const parts = displayName.split(/\s+/)
  if (parts.length === 1) {
    return parts[0].slice(0, 2).toUpperCase()
  }
  const first = parts[0][0] || ''
  const last = parts[parts.length - 1][0] || ''
  const initials = `${first}${last}`.toUpperCase()
  return initials || 'U'
})

const userAvatarStyle = computed(() => ({
  backgroundColor: 'var(--color-fill-3)',
  color: 'var(--color-text-1)',
  fontWeight: '600',
}))

const STORAGE_KEY = 'gearup_storefront_ai_chat_v1'
const CHAT_SESSIONS_KEY = 'gearup_storefront_ai_sessions_v1'
const SESSION_NAMES_KEY = 'gearup_storefront_ai_session_names_v1'

function generateSessionId(): string {
  return `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

function normalizeSuggestionMessages(msgs: ChatMessage[]): void {
  msgs.forEach((msg) => {
    if (msg.content && msg.isSuggestionsOnly) {
      msg.isSuggestionsOnly = false
      return
    }

    if (
      msg.role === 'assistant' &&
      !msg.content &&
      Array.isArray(msg.followUpSuggestions) &&
      msg.followUpSuggestions.length > 0 &&
      !msg.processingStatus &&
      true
    ) {
      msg.isSuggestionsOnly = true
    }
  })
}

function generateSessionName(msgs: ChatMessage[]): string {
  const userMessages = msgs.filter((msg) => msg.role === 'user' && msg.id !== 0)
  if (userMessages.length === 0) return 'Cuộc trò chuyện mới'

  const firstUserMessage = userMessages[0].content.toLowerCase()
  const patterns = [
    { pattern: /sản phẩm/i, name: '🛍️ Gợi ý sản phẩm' },
    { pattern: /đổi trả|hoàn hàng/i, name: '📦 Chính sách đổi trả' },
    { pattern: /khuyến mãi|giảm giá/i, name: '🎉 Khuyến mãi' },
    { pattern: /vận chuyển|giao hàng/i, name: '🚚 Vận chuyển' },
    { pattern: /kích cỡ|size|fit/i, name: '📏 Tư vấn kích cỡ' },
  ]

  const match = patterns.find(({ pattern }) => pattern.test(firstUserMessage))
  if (match) {
    return match.name
  }

  const words = firstUserMessage.split(' ').slice(0, 3)
  const truncated = words.join(' ')
  return truncated.length > 20 ? `${truncated.substring(0, 17)}...` : truncated
}

/**
 * Generate default follow-up suggestions based on AI response and conversation context
 */
function generateDefaultSuggestions(aiResponse: string, allMessages: ChatMessage[]): string[] {
  const responseLower = aiResponse.toLowerCase()
  const lastUserMessage = allMessages.filter((m) => m.role === 'user').pop()?.content.toLowerCase() || ''
  
  // Default suggestions that are always relevant
  const defaultSuggestions = [
    'Sản phẩm nào đang giảm giá?',
    'Tôi muốn biết kích cỡ phù hợp',
    'Chính sách đổi trả là gì?',
    'Gợi ý cho tôi giày chạy bộ',
  ]
  
  // Context-aware suggestions based on AI response
  const contextSuggestions: string[] = []
  
  // If AI mentioned products, suggest related questions
  if (responseLower.includes('sản phẩm') || responseLower.includes('giày') || responseLower.includes('nike') || responseLower.includes('adidas')) {
    contextSuggestions.push('Sản phẩm này có size nào?')
    contextSuggestions.push('Giá của sản phẩm này là bao nhiêu?')
    contextSuggestions.push('Sản phẩm này còn hàng không?')
  }
  
  // If AI mentioned promotions/discounts
  if (responseLower.includes('giảm giá') || responseLower.includes('khuyến mãi') || responseLower.includes('voucher')) {
    contextSuggestions.push('Có mã giảm giá nào khác không?')
    contextSuggestions.push('Chương trình khuyến mãi kéo dài đến khi nào?')
    contextSuggestions.push('Làm sao để sử dụng voucher?')
  }
  
  // If AI mentioned orders
  if (responseLower.includes('đơn hàng') || responseLower.includes('order') || lastUserMessage.includes('đơn')) {
    contextSuggestions.push('Tôi muốn kiểm tra trạng thái đơn hàng')
    contextSuggestions.push('Thời gian giao hàng là bao lâu?')
    contextSuggestions.push('Có thể hủy đơn hàng không?')
  }
  
  // If AI mentioned shipping/delivery
  if (responseLower.includes('vận chuyển') || responseLower.includes('giao hàng') || responseLower.includes('shipping')) {
    contextSuggestions.push('Phí vận chuyển là bao nhiêu?')
    contextSuggestions.push('Có giao hàng nhanh không?')
    contextSuggestions.push('Tôi có thể đổi địa chỉ giao hàng không?')
  }
  
  // If AI mentioned size/fit
  if (responseLower.includes('size') || responseLower.includes('kích cỡ') || responseLower.includes('fit')) {
    contextSuggestions.push('Làm sao để chọn size đúng?')
    contextSuggestions.push('Size này có phù hợp với chân tôi không?')
    contextSuggestions.push('Có bảng size không?')
  }
  
  // If AI mentioned return/refund
  if (responseLower.includes('đổi trả') || responseLower.includes('hoàn hàng') || responseLower.includes('refund')) {
    contextSuggestions.push('Thời gian đổi trả là bao lâu?')
    contextSuggestions.push('Có thể đổi sang sản phẩm khác không?')
    contextSuggestions.push('Phí đổi trả là bao nhiêu?')
  }
  
  // Combine context suggestions with defaults, remove duplicates, limit to 4
  const allSuggestions = [...contextSuggestions, ...defaultSuggestions]
  const uniqueSuggestions = Array.from(new Set(allSuggestions))
  
  // Return top 4 suggestions (prioritize context-aware ones)
  return uniqueSuggestions.slice(0, 4)
}

function saveHistory() {
  try {
    // Don't save history if user is not authenticated
    if (!userStore.isAuthenticated) {
      return
    }
    
    normalizeSuggestionMessages(messages.value)

    const messagesToSave = [...messages.value]

    if (currentSessionId.value) {
      chatSessions.value[currentSessionId.value] = [...messagesToSave]
    }

    localStorage.setItem(CHAT_SESSIONS_KEY, JSON.stringify(chatSessions.value))
    localStorage.setItem(SESSION_NAMES_KEY, JSON.stringify(sessionNames.value))
    localStorage.setItem(STORAGE_KEY, JSON.stringify(messagesToSave))

    emit('session-state', {
      sessions: chatSessions.value,
      currentSessionId: currentSessionId.value,
      sessionNames: sessionNames.value,
    })
  } catch {
    // ignore storage errors
  }
}

/**
 * Save AI chat message to database
 * Only saves if not in staff chat mode and user is authenticated
 */
async function saveAiChatToDatabase(role: 'user' | 'assistant', content: string) {
  // Don't save if in staff chat mode
  if (props.staffChatMode) return
  
  // Don't save if user is not authenticated or not a customer
  if (!userStore.isAuthenticated || !userStore.id) return
  
  // Don't save empty content
  if (!content || !content.trim()) return
  
  // Ensure we have a session ID
  if (!currentSessionId.value) {
    currentSessionId.value = generateSessionId()
  }
  
  try {
    await luuLichSuAiChat({
      customerId: userStore.id,
      sessionId: currentSessionId.value,
      role,
      content: content.trim(),
    })
  } catch (error: any) {
    // Silently fail - don't interrupt user experience
    console.error('Failed to save AI chat history:', error)
  }
}

function clearChatHistory() {
  try {
    localStorage.removeItem(STORAGE_KEY)
    localStorage.removeItem(CHAT_SESSIONS_KEY)
    localStorage.removeItem(SESSION_NAMES_KEY)
    chatSessions.value = {}
    sessionNames.value = {}
    currentSessionId.value = generateSessionId()
    messages.value = [
      {
        id: 0,
        role: 'assistant',
        content:
          'Xin chào! Tôi là trợ lý AI của GearUp. Tôi có thể giúp bạn tra cứu thông tin về sản phẩm, chương trình khuyến mãi và đơn hàng. Bạn cần giúp gì không? 😊',
        timestamp: new Date().toLocaleTimeString('vi-VN'),
      },
      {
        id: -1,
        role: 'assistant',
        content: '',
        timestamp: '',
        followUpSuggestions: [
          'Sản phẩm nào đang giảm giá?',
          'Tôi muốn biết kích cỡ phù hợp',
          'Chính sách đổi trả là gì?',
          'Gợi ý cho tôi giày chạy bộ',
        ],
        isSuggestionsOnly: true,
      },
    ]
    normalizeSuggestionMessages(messages.value)
    sessionNames.value[currentSessionId.value] = 'Cuộc trò chuyện mới'
    chatSessions.value[currentSessionId.value] = [...messages.value]
    console.log('🧹 Chat history cleared for unauthenticated user')
  } catch (error) {
    console.error('Error clearing chat history:', error)
  }
}

function loadHistory() {
  try {
    // If user is not authenticated, clear chat history
    if (!userStore.isAuthenticated) {
      clearChatHistory()
      emit('session-state', {
        sessions: chatSessions.value,
        currentSessionId: currentSessionId.value,
        sessionNames: sessionNames.value,
      })
      return
    }
    
    const sessionsRaw = localStorage.getItem(CHAT_SESSIONS_KEY)
    if (sessionsRaw) {
      const parsed: Record<string, ChatMessage[]> = JSON.parse(sessionsRaw)
      if (parsed && typeof parsed === 'object') {
        chatSessions.value = parsed
        Object.values(chatSessions.value).forEach((sessionMessages) => {
          normalizeSuggestionMessages(sessionMessages)
        })
      }
    }

    const namesRaw = localStorage.getItem(SESSION_NAMES_KEY)
    if (namesRaw) {
      const parsed: Record<string, string> = JSON.parse(namesRaw)
      if (parsed && typeof parsed === 'object') {
        sessionNames.value = parsed
      }
    }

    if (Object.keys(chatSessions.value).length > 0) {
      const sessionIds = Object.keys(chatSessions.value)
      const sortedSessions = sessionIds.sort((a, b) => {
        const timestampA = parseInt(a.split('_')[1], 10)
        const timestampB = parseInt(b.split('_')[1], 10)
        return timestampB - timestampA
      })

      const [mostRecentSessionId] = sortedSessions
      currentSessionId.value = mostRecentSessionId
      messages.value = [...chatSessions.value[currentSessionId.value]]
      normalizeSuggestionMessages(messages.value)
      const hasSuggestionCard = messages.value.some(
        (msg) => msg.role === 'assistant' && msg.isSuggestionsOnly === true && !msg.content && Array.isArray(msg.followUpSuggestions)
      )

      if (!hasSuggestionCard && messages.value.length === 1 && messages.value[0].id === 0 && messages.value[0].role === 'assistant') {
        messages.value.push({
          id: -1,
          role: 'assistant',
          content: '',
          timestamp: '',
          followUpSuggestions: [
            'Sản phẩm nào đang giảm giá?',
            'Tôi muốn biết kích cỡ phù hợp',
            'Chính sách đổi trả là gì?',
            'Gợi ý cho tôi giày chạy bộ',
          ],
          isSuggestionsOnly: true,
        })
      }
    } else {
      const raw = localStorage.getItem(STORAGE_KEY)
      if (raw) {
        const parsed: ChatMessage[] = JSON.parse(raw)
        if (Array.isArray(parsed) && parsed.length > 0) {
          messages.value = parsed
          normalizeSuggestionMessages(messages.value)
          const hasSuggestionCard = messages.value.some(
            (msg) => msg.role === 'assistant' && msg.isSuggestionsOnly === true && !msg.content && Array.isArray(msg.followUpSuggestions)
          )
          if (!hasSuggestionCard && messages.value.length === 1 && messages.value[0].id === 0 && messages.value[0].role === 'assistant') {
            messages.value.push({
              id: -1,
              role: 'assistant',
              content: '',
              timestamp: '',
              followUpSuggestions: [
                'Sản phẩm nào đang giảm giá?',
                'Tôi muốn biết kích cỡ phù hợp',
                'Chính sách đổi trả là gì?',
                'Gợi ý cho tôi giày chạy bộ',
              ],
              isSuggestionsOnly: true,
            })
          }
          currentSessionId.value = generateSessionId()
          chatSessions.value[currentSessionId.value] = parsed
          sessionNames.value[currentSessionId.value] = generateSessionName(parsed)
        }
      }

      if (!currentSessionId.value) {
        currentSessionId.value = generateSessionId()
        sessionNames.value[currentSessionId.value] = 'Cuộc trò chuyện mới'
      }
    }
  } catch {
    // ignore parse errors
  }

  emit('session-state', {
    sessions: chatSessions.value,
    currentSessionId: currentSessionId.value,
    sessionNames: sessionNames.value,
  })
}

function scrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTo({
      top: messagesContainer.value.scrollHeight,
      behavior: 'smooth',
    })
  }
}


async function checkConnection() {
  try {
    await checkAIHealth()
    isConnected.value = true
  } catch (error) {
    isConnected.value = false
    if (shouldNotifyConnection) {
      Message.warning('AI service connection failed')
    }
  }
}

async function sendMessage(text: string = input.value) {
  if (!text.trim()) return

  // If in staff chat mode, send to staff instead of AI
  if (props.staffChatMode && props.staffReceiverId) {
    // Add user message to chat immediately
    const userMessage: ChatMessage = {
      id: Date.now(),
      role: 'user',
      content: text.trim(),
      timestamp: new Date().toLocaleTimeString('vi-VN'),
    }
    messages.value.push(userMessage)
    const messageText = text.trim()
    input.value = ''
    await nextTick()
    scrollToBottom()
    saveHistory()
    
    try {
      // Send to staff via WebSocket
      await chatStore.sendMessageViaWebSocket({
        receiverId: props.staffReceiverId,
        content: messageText,
        messageType: 'TEXT',
      })
      emit('staff-message-sent', messageText)
      return
    } catch (error: any) {
      Message.error('Không thể gửi tin nhắn đến nhân viên. Vui lòng thử lại.')
      // Remove the message if sending failed
      const index = messages.value.findIndex((m) => m.id === userMessage.id)
      if (index >= 0) {
        messages.value.splice(index, 1)
      }
      input.value = messageText // Restore input
      return
    }
  }

  // Normal AI chat flow
  if (!isConnected.value) {
    Message.error('AI service chưa kết nối. Vui lòng thử lại sau.')
    return
  }

  if (isProcessing.value) {
    Message.warning('Đang xử lý câu hỏi trước, vui lòng đợi...')
    return
  }

  isProcessing.value = true

  messages.value = messages.value.filter((msg) => !(msg.role === 'assistant' && msg.isSuggestionsOnly === true && !msg.content))
  messages.value.forEach((msg) => {
    if (msg.role === 'assistant' && msg.followUpSuggestions) {
      msg.followUpSuggestions = undefined
    }
  })

  await nextTick()

  const userMessage: ChatMessage = {
    id: Date.now(),
    role: 'user',
    content: text.trim(),
    timestamp: new Date().toLocaleTimeString('vi-VN'),
  }
  messages.value.push(userMessage)

  // Save user message to database
  await saveAiChatToDatabase('user', text.trim())

  // Create AI message placeholder for streaming - MUST have processingStatus to show spinner
  const aiMessageId = Date.now() + 1
  const aiMessage: ChatMessage = {
    id: aiMessageId,
    role: 'assistant',
    content: '', // Empty content to show spinner
    timestamp: new Date().toLocaleTimeString('vi-VN'),
    processingStatus: 'querying', // This triggers spinner display
    dataSource: '',
    queryType: '',
  }
  
  // Push message immediately - Vue will reactively update
  messages.value.push(aiMessage)
  input.value = ''
  
  // Force immediate DOM update to show spinner
  await nextTick()
  scrollToBottom()
  
  // Additional tick to ensure spinner renders
  await nextTick()
  
  saveHistory()

  try {
    let fullContent = ''
    let shouldRedirectToStaff = false
    let receivedProducts: Product[] = [] // Store products from metadata
    let receivedQueryType: string | undefined = undefined

    await chatWithAIStream(
      text,
      (chunk: string, metadata?: any) => {
        fullContent += chunk

        // Check for redirect_to_staff flag in metadata
        if (metadata && metadata.redirect_to_staff === true) {
          shouldRedirectToStaff = true
        }

        const msgIndex = messages.value.findIndex((m) => m.id === aiMessageId)
        if (msgIndex !== -1) {
          if (metadata) {
            if (metadata.data_source) messages.value[msgIndex].dataSource = metadata.data_source
            if (metadata.follow_up_suggestions) messages.value[msgIndex].followUpSuggestions = metadata.follow_up_suggestions
            if (metadata.intent) {
              messages.value[msgIndex].queryType = metadata.intent
              receivedQueryType = metadata.intent
            }
            if (metadata.products && Array.isArray(metadata.products) && metadata.products.length > 0) {
              // Store products in the message AND in local variable
              messages.value[msgIndex].products = metadata.products
              receivedProducts = [...metadata.products]
              console.log('📦 Products received from backend:', metadata.products.length, 'products', metadata.products)
            }
            messages.value[msgIndex].processingStatus = 'analyzing'
          }

          // Update content as it streams - only set if there's actual content
          if (fullContent.trim().length > 0) {
            messages.value[msgIndex].content = fullContent
            messages.value[msgIndex].processingStatus = 'analyzing'
          } else {
            // Keep content empty to show spinner until real content arrives
            messages.value[msgIndex].content = ''
          }
          
          nextTick(() => scrollToBottom())
        }
      },
      async () => {
        loading.value = false
        isProcessing.value = false

        const msgIndex = messages.value.findIndex((m) => m.id === aiMessageId)
        if (msgIndex !== -1) {
          const msg = messages.value[msgIndex]
          msg.content = fullContent.trim()
          msg.processingStatus = 'ready'
          
          // Ensure products and queryType are set from received data
          if (receivedProducts.length > 0 && !msg.products) {
            msg.products = [...receivedProducts]
          }
          if (receivedQueryType && !msg.queryType) {
            msg.queryType = receivedQueryType
          }
        }

        const userMessages = messages.value.filter((msg) => msg.role === 'user' && msg.id !== 0)
        const currentName = sessionNames.value[currentSessionId.value]
        if (userMessages.length === 1 && (!currentName || currentName === 'Cuộc trò chuyện mới')) {
          const newName = generateSessionName(messages.value)
          sessionNames.value[currentSessionId.value] = newName
        }

        saveHistory()

        // Save AI response to database
        const aiMsg = messages.value.find((m) => m.id === aiMessageId)
        if (aiMsg && aiMsg.content && aiMsg.content.trim()) {
          await saveAiChatToDatabase('assistant', aiMsg.content)
        }

        // Check if we should redirect to staff chat
        if (shouldRedirectToStaff) {
          // Wait a bit for the message to be displayed, then redirect
          setTimeout(() => {
            emit('redirect-to-staff')
          }, 1500)
          return
        }
        
        // Check if we should show product cards in a separate message
        // Only show product cards when customer explicitly asks for product suggestions
        // Pattern: "gợi ý [sản phẩm/giày/product/shoe]" or "suggest [product/shoe]"
        const userMessageLower = text.toLowerCase().trim()
        
        // Check for explicit suggestion request with product keywords
        const hasExplicitSuggestionRequest = 
          // Vietnamese patterns: "gợi ý" + product keywords
          (userMessageLower.includes('gợi ý') && (
            userMessageLower.includes('giày') ||
            userMessageLower.includes('shoe') ||
            userMessageLower.includes('sản phẩm') ||
            userMessageLower.includes('product') ||
            userMessageLower.includes('nike') ||
            userMessageLower.includes('adidas') ||
            userMessageLower.includes('puma') ||
            userMessageLower.includes('vans') ||
            userMessageLower.includes('converse') ||
            userMessageLower.includes('chạy bộ') ||
            userMessageLower.includes('running') ||
            userMessageLower.includes('thể thao') ||
            userMessageLower.includes('sport')
          )) ||
          // English patterns: "suggest" + product keywords
          (userMessageLower.includes('suggest') && (
            userMessageLower.includes('shoe') ||
            userMessageLower.includes('product') ||
            userMessageLower.includes('nike') ||
            userMessageLower.includes('adidas') ||
            userMessageLower.includes('puma') ||
            userMessageLower.includes('vans') ||
            userMessageLower.includes('converse') ||
            userMessageLower.includes('running')
          )) ||
          // Alternative patterns: "cho tôi xem" + product keywords
          ((userMessageLower.includes('cho tôi') || userMessageLower.includes('show me')) && (
            userMessageLower.includes('giày') ||
            userMessageLower.includes('shoe') ||
            userMessageLower.includes('sản phẩm') ||
            userMessageLower.includes('product')
          ))
        
        // Only show product cards if:
        // 1. Query type is product_inquiry (from backend), AND
        // 2. User explicitly asked for product suggestions (hasExplicitSuggestionRequest), AND
        // 3. We have products from backend
        const isProductSuggestionQuery = 
          (aiMsg?.queryType === 'product_inquiry' || aiMsg?.queryType === 'promotion_inquiry') &&
          hasExplicitSuggestionRequest
        
        // Use products from aiMsg or receivedProducts
        const productsToCheck = aiMsg?.products || receivedProducts
        
        // If this is a product suggestion query AND we have products, create a separate message for product cards
        if (isProductSuggestionQuery && productsToCheck && productsToCheck.length > 0) {
          console.log('📦 Creating separate product cards message:', {
            queryType: aiMsg?.queryType || receivedQueryType,
            productsCount: productsToCheck.length,
            userMessage: text.substring(0, 50),
            hasExplicitSuggestionRequest,
            products: productsToCheck
          })
          
          // Remove products from the main message
          if (aiMsg) {
            aiMsg.products = undefined
          }
          
          // Create a separate message for product cards
          const productMessage: ChatMessage = {
            id: Date.now() + 1,
            role: 'assistant',
            content: '',
            timestamp: new Date().toLocaleTimeString('vi-VN'),
            products: [...productsToCheck],
            processingStatus: 'ready',
            isProductSuggestion: true,
          }
          messages.value.push(productMessage)
          
          await nextTick()
          scrollToBottom()
        } else if (aiMsg && aiMsg.products && aiMsg.products.length > 0) {
          // If we have products but it's not a suggestion query, remove them from the message
          console.log('📦 Products received but not a suggestion query, removing from message', {
            queryType: aiMsg.queryType,
            hasExplicitSuggestionRequest,
            userMessage: text.substring(0, 50)
          })
          aiMsg.products = undefined
        }
        
        // Always show suggestions card after AI response (unless redirecting to staff)
        if (aiMsg && aiMsg.content && aiMsg.content.trim()) {
          // Get suggestions from metadata or use default suggestions
          let suggestions: string[] = []
          
          if (aiMsg.followUpSuggestions && aiMsg.followUpSuggestions.length > 0) {
            // Use suggestions from backend
            suggestions = [...aiMsg.followUpSuggestions]
          } else {
            // Generate default suggestions based on conversation context
            suggestions = generateDefaultSuggestions(aiMsg.content, messages.value)
          }
          
          // Remove suggestions from the AI message
          aiMsg.followUpSuggestions = undefined
          aiMsg.isSuggestionsOnly = false
          
          // Create a separate suggestions card message
          const suggestionsMessage: ChatMessage = {
            id: Date.now() + 2,
            role: 'assistant',
            content: '',
            timestamp: '',
            followUpSuggestions: suggestions,
            isSuggestionsOnly: true,
          }
          messages.value.push(suggestionsMessage)
        }

        nextTick(() => scrollToBottom())
      },
      (error: string) => {
        loading.value = false
        isProcessing.value = false
        Message.error(`Lỗi khi gửi tin nhắn: ${error}`)

        const msgIndex = messages.value.findIndex((m) => m.id === aiMessageId)
        if (msgIndex !== -1) {
          messages.value[msgIndex].content = '❌ Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.'
        }
      }
    )
  } catch (error: any) {
    loading.value = false
    isProcessing.value = false
    Message.error(`Lỗi khi gửi tin nhắn: ${error.message}`)

    const msgIndex = messages.value.findIndex((m) => m.id === aiMessageId)
    if (msgIndex !== -1) {
      messages.value[msgIndex].content = '❌ Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.'
    }
  }
}

function handleSearch(value?: string) {
  const text = typeof value === 'string' ? value : input.value
  sendMessage(text)
}

function askQuestion(question: string) {
  sendMessage(question)
}

// Watch for staff chat mode activation and load existing messages
watch(
  () => [props.staffChatMode, props.staffConversationId],
  async ([isStaffMode, convId]) => {
    if (!isStaffMode || !convId) return
    
    // Load existing messages from the conversation
    if (chatStore.activeMessages && Array.isArray(chatStore.activeMessages)) {
      // Keep the initial AI greeting and AI conversation history
      // Just add staff messages after the redirect message
      
      // Add all messages from the conversation
      chatStore.activeMessages.forEach((staffMsg: any) => {
        // Check if message already exists
        const msgId = staffMsg.id || staffMsg.maTinNhan
        const exists = messages.value.some((msg) => {
          if (msgId && typeof msg.id === 'number') {
            return msg.id === msgId
          }
          return false
        })
        
        if (!exists) {
          if (staffMsg.senderId === userStore.id) {
            // User message
            const chatMsg: ChatMessage = {
              id: msgId || Date.now(),
              role: 'user',
              content: staffMsg.content || '',
              timestamp: staffMsg.sentAt ? new Date(staffMsg.sentAt).toLocaleTimeString('vi-VN') : new Date().toLocaleTimeString('vi-VN'),
            }
            messages.value.push(chatMsg)
          } else {
            // Staff message (displayed as assistant)
            const chatMsg: ChatMessage = {
              id: msgId || Date.now(),
              role: 'assistant',
              content: staffMsg.content || '',
              timestamp: staffMsg.sentAt ? new Date(staffMsg.sentAt).toLocaleTimeString('vi-VN') : new Date().toLocaleTimeString('vi-VN'),
            }
            messages.value.push(chatMsg)
          }
        }
      })
      
      await nextTick()
      scrollToBottom()
      saveHistory()
    }
  },
  { immediate: true }
)

// Watch for new incoming staff messages
watch(
  () => [chatStore.activeMessages, chatStore.messages, props.staffChatMode, props.staffConversationId, props.staffReceiverId, chatStore.activeConversationId],
  ([activeMessages, allMessages, isStaffMode, convId, receiverId, activeConvId]) => {
    if (!isStaffMode) return
    
    // Get messages from active conversation, or find messages for the staff receiver if no active conversation yet
    let messagesToProcess: any[] = []
    
    if (convId && activeConvId === convId && Array.isArray(activeMessages)) {
      // We have an active conversation matching the staff conversation
      messagesToProcess = activeMessages
    } else if (receiverId !== null) {
      // No active conversation yet, but we have a receiver ID
      // Find the conversation that matches this receiver
      const matchingConv = chatStore.conversations.find((c: any) => {
        if (c.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
          return c.nhanVienId === receiverId || c.khachHangId === userStore.id
        }
        return false
      })
      
      if (matchingConv && allMessages && typeof allMessages === 'object' && !Array.isArray(allMessages)) {
        const convMessages = (allMessages as Record<number, any[]>)[matchingConv.id]
        if (Array.isArray(convMessages)) {
          messagesToProcess = convMessages
        }
      } else if (!convId) {
        // No conversation yet, but we can still check all messages for ones from/to this receiver
        // This handles the case where a message arrives before the conversation is created
        Object.values(allMessages || {}).forEach((msgs: any) => {
          if (Array.isArray(msgs)) {
            msgs.forEach((msg: any) => {
              if ((msg.senderId === receiverId && msg.receiverId === userStore.id) ||
                  (msg.receiverId === receiverId && msg.senderId === userStore.id)) {
                messagesToProcess.push(msg)
              }
            })
          }
        })
      }
    }
    
    if (messagesToProcess.length === 0) return
    
    console.log('📬 Processing staff messages in AI chat:', {
      isStaffMode,
      convId,
      receiverId,
      activeConvId,
      messagesToProcessCount: messagesToProcess.length,
    })
    
    messagesToProcess.forEach((staffMsg: any) => {
      // Check if this message is relevant to the current staff chat
      const isRelevant = convId
        ? true // If we have convId, we already filtered by conversation
        : receiverId !== null && (staffMsg.senderId === receiverId || staffMsg.receiverId === receiverId)
      
      if (!isRelevant) return
      
      // Check if message already exists
      const msgId = staffMsg.id || staffMsg.maTinNhan
      const exists = messages.value.some((msg) => {
        if (msgId && typeof msg.id === 'number') {
          return msg.id === msgId
        }
        // Also check by content and timestamp for duplicate detection
        if (staffMsg.content && msg.content === staffMsg.content) {
          const msgTime = staffMsg.sentAt ? new Date(staffMsg.sentAt).getTime() : 0
          const existingTime = msg.timestamp ? new Date(msg.timestamp).getTime() : 0
          if (Math.abs(msgTime - existingTime) < 5000) { // Within 5 seconds
            return true
          }
        }
        return false
      })
      
      if (!exists) {
        if (staffMsg.senderId === userStore.id) {
          // User message (customer sent this)
          const chatMsg: ChatMessage = {
            id: msgId || Date.now(),
            role: 'user',
            content: staffMsg.content || '',
            timestamp: staffMsg.sentAt ? new Date(staffMsg.sentAt).toLocaleTimeString('vi-VN') : new Date().toLocaleTimeString('vi-VN'),
          }
          messages.value.push(chatMsg)
          console.log('✅ Added user message to AI chat:', chatMsg.content.substring(0, 50))
        } else if (staffMsg.receiverId === userStore.id) {
          // Staff message (staff sent this to customer, displayed as assistant)
          const chatMsg: ChatMessage = {
            id: msgId || Date.now(),
            role: 'assistant',
            content: staffMsg.content || '',
            timestamp: staffMsg.sentAt ? new Date(staffMsg.sentAt).toLocaleTimeString('vi-VN') : new Date().toLocaleTimeString('vi-VN'),
          }
          messages.value.push(chatMsg)
          console.log('✅ Added staff message to AI chat:', chatMsg.content.substring(0, 50))
        }
        nextTick(() => scrollToBottom())
        saveHistory()
      }
    })
  },
  { deep: true }
)

function switchToSession(sessionId: string) {
  if (chatSessions.value[sessionId]) {
    if (currentSessionId.value) {
      chatSessions.value[currentSessionId.value] = [...messages.value]
    }

    currentSessionId.value = sessionId
    messages.value = [...chatSessions.value[sessionId]]
    normalizeSuggestionMessages(messages.value)

    saveHistory()
    nextTick(() => {
      scrollToBottom()
    })
  }
}

function createNewChat() {
  if (currentSessionId.value && messages.value.length > 1) {
    chatSessions.value[currentSessionId.value] = [...messages.value]
  }

  currentSessionId.value = generateSessionId()
  messages.value = [
    {
      id: 0,
      role: 'assistant',
      content:
        'Xin chào! Tôi là trợ lý AI của GearUp. Tôi có thể giúp bạn tra cứu thông tin về sản phẩm, chương trình khuyến mãi và đơn hàng. Bạn cần giúp gì không? 😊',
      timestamp: new Date().toLocaleTimeString('vi-VN'),
    },
    {
      id: -1,
      role: 'assistant',
      content: '',
      timestamp: '',
      followUpSuggestions: [
        'Sản phẩm nào đang giảm giá?',
        'Tôi muốn biết kích cỡ phù hợp',
        'Chính sách đổi trả là gì?',
        'Gợi ý cho tôi giày chạy bộ',
      ],
      isSuggestionsOnly: true,
    },
  ]
  normalizeSuggestionMessages(messages.value)

  sessionNames.value[currentSessionId.value] = 'Cuộc trò chuyện mới'

  chatSessions.value[currentSessionId.value] = [...messages.value]
  saveHistory()

  nextTick(() => {
    scrollToBottom()
  })

  Message.success('Đã tạo cuộc trò chuyện mới')
}

function clearMessages() {
  chatSessions.value = {}
  sessionNames.value = {}
  currentSessionId.value = generateSessionId()

  messages.value = [
    {
      id: 0,
      role: 'assistant',
      content: 'Đã xóa lịch sử chat. Tôi có thể giúp gì cho bạn?',
      timestamp: new Date().toLocaleTimeString('vi-VN'),
    },
    {
      id: -1,
      role: 'assistant',
      content: '',
      timestamp: '',
      followUpSuggestions: [
        'Sản phẩm nào đang giảm giá?',
        'Tôi muốn biết kích cỡ phù hợp',
        'Chính sách đổi trả là gì?',
        'Gợi ý cho tôi giày chạy bộ',
      ],
      isSuggestionsOnly: true,
    },
  ]
  normalizeSuggestionMessages(messages.value)

  sessionNames.value[currentSessionId.value] = 'Cuộc trò chuyện mới'

  chatSessions.value[currentSessionId.value] = [...messages.value]
  saveHistory()

  nextTick(() => {
    scrollToBottom()
  })
}

let healthTimer: any = null

onMounted(async () => {
  // Clear chat history if user is not authenticated
  if (!userStore.isAuthenticated) {
    clearChatHistory()
  } else {
    loadHistory()
  }
  
  await nextTick()

  setTimeout(() => {
    scrollToBottom()
  }, 100)

  if (shouldHealthCheck) {
    await checkConnection()
    healthTimer = setInterval(() => {
      if (document.visibilityState === 'visible') {
        checkConnection()
      }
    }, 30000)
  }

  const root = document.documentElement
  const updateTheme = () => {
    isDark.value = root.classList.contains('arco-theme-dark')
    if (isDark.value) {
      messagesContainerStyle.value = {
        background: 'linear-gradient(180deg, #0f1115 0%, #12141a 100%)',
      }
    } else {
      messagesContainerStyle.value = {}
    }
  }
  updateTheme()
  const observer = new MutationObserver(updateTheme)
  observer.observe(root, { attributes: true, attributeFilter: ['class'] })
  ;(window as any).storefrontAiChatThemeObserver = observer
})
onUnmounted(() => {
  if (healthTimer) clearInterval(healthTimer)
  const obs = (window as any).storefrontAiChatThemeObserver
  if (obs && typeof obs.disconnect === 'function') obs.disconnect()
})

watch(
  messages,
  () => {
    if (currentSessionId.value) {
      saveHistory()
    }

    nextTick(() => {
      scrollToBottom()
    })
  },
  { deep: true }
)

// Watch for authentication status changes
watch(
  () => userStore.isAuthenticated,
  (isAuthenticated) => {
    if (!isAuthenticated) {
      // User logged out, clear chat history
      clearChatHistory()
      emit('session-state', {
        sessions: chatSessions.value,
        currentSessionId: currentSessionId.value,
        sessionNames: sessionNames.value,
      })
    }
  }
)

function renderMarkdown(md: string) {
  try {
    if (!md) return ''
    // Configure marked to preserve line breaks
    const html = markedParse(md, {
      breaks: true, // Convert line breaks to <br>
      gfm: true, // GitHub Flavored Markdown
    }) as string
    return html
  } catch {
    return md
  }
}

defineExpose({
  createNewChat,
  clearMessages,
  switchToSession,
  chatSessions,
  currentSessionId,
  sessionNames,
})
</script>

<style scoped lang="less">
.ai-chatbot {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;

  .chatbot-card {
    width: 100%;
    height: 100%;
    min-height: 0;
    display: flex;
    flex-direction: column;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    :deep(.arco-card-body) {
      flex: 1;
      display: flex;
      flex-direction: column;
      padding: 0 !important;
      overflow: hidden;
      background: transparent !important;
    }

    :deep(.arco-card) {
      background: transparent !important;
    }
  }

  .chatbot-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;

    .title {
      font-size: 16px;
      font-weight: 600;
    }
  }

  .messages-container {
    flex: 1;
    overflow-y: auto;
    padding: 12px 12px 16px;
    margin-bottom: 8px;
    min-height: 0;
    background: var(--color-bg-1) !important;
    border-radius: 12px;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-track {
      background: var(--color-fill-2);
      border-radius: 3px;
    }

    &::-webkit-scrollbar-thumb {
      background: var(--color-fill-4);
      border-radius: 3px;

      &:hover {
        background: var(--color-fill-3);
      }
    }
  }

  .message {
    margin-bottom: 16px;
    animation: fadeIn 0.3s ease-in;

    &.user {
      .message-wrapper {
        flex-direction: row-reverse;

        .avatar {
          margin-top: 0;
        }

        .content {
          background: var(--color-primary, #111111);
          color: #fff;
          margin-right: 8px;
          margin-left: 0;
          box-shadow: 0 4px 10px rgba(17, 17, 17, 0.25);

          /* Ensure text inside user message is white */
          .text,
          :deep(.text),
          :deep(p),
          :deep(span),
          :deep(div) {
            color: #fff !important;
          }
        }
      }
    }

    &.assistant {
      .content {
        background: var(--color-bg-2);
        color: var(--color-text-1);
        border: 1px solid var(--color-border-2);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
      }
    }
  }

  .message-wrapper {
    display: flex;
    gap: 8px;
    align-items: flex-start;
  }

  .avatar {
    font-size: 24px;
    flex-shrink: 0;
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-top: 2px;

    :deep(.arco-avatar) {
      width: 40px;
      height: 40px;
      font-size: 16px;
      font-weight: 600;
    }
  }

  .content {
    padding: 10px 12px;
    border-radius: 14px;
    max-width: 72%;
    word-wrap: break-word;

    :deep(table) {
      width: 100%;
      border-collapse: collapse;
      margin-top: 10px;
      border: 1px solid var(--color-border-2);
      border-radius: 8px;
      overflow: hidden;
      background: var(--color-bg-1);
    }

    :deep(th),
    :deep(td) {
      padding: 8px 12px;
      border: 1px solid var(--color-border-2);
      text-align: left;
      font-size: 13px;
    }

    :deep(th) {
      background: var(--color-fill-2);
      font-weight: 600;
    }

    :deep(tr:nth-child(even) td) {
      background: var(--color-fill-1);
    }

    :deep(ul),
    :deep(ol) {
      padding-left: 20px;
      margin: 8px 0;
    }

    :deep(li) {
      margin-bottom: 4px;
    }

    .processing-indicator {
      display: flex;
      align-items: center;
      color: var(--color-text-2);
      font-size: 14px;
    }

    .text {
      line-height: 1.7;
      white-space: normal; /* Let markdown handle line breaks */
      word-wrap: break-word;
      font-size: 14px;
      color: var(--color-text-1);
      display: block; /* Ensure block-level display */

      /* Ensure streaming cursor is inline with content */
      > span:first-child {
        display: inline;
      }

      /* Paragraphs - better spacing */
      :deep(p) {
        margin: 0 0 12px 0;
        line-height: 1.7;
        color: var(--color-text-1);
        display: block; /* Ensure paragraphs are block-level */
      }

      :deep(p:first-child) {
        margin-top: 0;
      }

      :deep(p:last-child) {
        margin-bottom: 0;
      }
      
      /* Ensure last paragraph's content allows inline cursor */
      :deep(p:last-child) {
        display: block;
      }
      
      /* Make sure inline elements in last paragraph allow cursor on same line */
      :deep(p:last-child > *:last-child) {
        display: inline;
      }

      /* Handle line breaks within paragraphs */
      :deep(br) {
        line-height: 1.7;
      }

      /* Spacing between paragraphs */
      :deep(p + p) {
        margin-top: 12px;
      }

      /* Headings - highlighted with colors */
      :deep(h1),
      :deep(h2),
      :deep(h3),
      :deep(h4),
      :deep(h5),
      :deep(h6) {
        margin: 16px 0 12px 0;
        font-weight: 700;
        line-height: 1.4;
        color: var(--color-text-1);
      }

      :deep(h1) {
        font-size: 20px;
        border-bottom: 2px solid var(--color-border-2);
        padding-bottom: 8px;
      }

      :deep(h2) {
        font-size: 18px;
        color: var(--color-primary, #111111);
      }

      :deep(h3) {
        font-size: 16px;
        color: var(--color-primary, #111111);
      }

      :deep(h4),
      :deep(h5),
      :deep(h6) {
        font-size: 14px;
        font-weight: 600;
      }

      /* Lists - better styling */
      :deep(ul),
      :deep(ol) {
        margin: 12px 0;
        padding-left: 24px;
        line-height: 1.8;
        display: block;
      }

      :deep(ul) {
        list-style-type: disc;
      }

      :deep(ol) {
        list-style-type: decimal;
      }

      :deep(li) {
        margin: 8px 0;
        padding-left: 4px;
        color: var(--color-text-1);
        display: list-item;
        line-height: 1.7;
      }

      :deep(li::marker) {
        color: var(--color-primary, #111111);
      }

      /* Ensure list items have proper spacing */
      :deep(li + li) {
        margin-top: 8px;
      }

      /* Spacing between list and other elements */
      :deep(p + ul),
      :deep(p + ol),
      :deep(ul + p),
      :deep(ol + p) {
        margin-top: 12px;
      }

      /* Nested lists */
      :deep(ul ul),
      :deep(ol ol),
      :deep(ul ol),
      :deep(ol ul) {
        margin: 6px 0;
        padding-left: 20px;
      }

      /* Strong/Bold text - highlighted */
      :deep(strong),
      :deep(b) {
        font-weight: 700;
        color: var(--color-text-1);
        background: linear-gradient(120deg, rgba(17, 17, 17, 0.1) 0%, rgba(17, 17, 17, 0.05) 100%);
        padding: 2px 4px;
        border-radius: 3px;
      }

      /* Emphasis/Italic */
      :deep(em),
      :deep(i) {
        font-style: italic;
        color: var(--color-text-2);
      }

      /* Tables - improved styling */
      :deep(table) {
        width: 100%;
        border-collapse: collapse;
        margin: 16px 0;
        border: 1px solid var(--color-border-2);
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
      }

      :deep(th),
      :deep(td) {
        border: 1px solid var(--color-border-2);
        padding: 12px 14px;
        text-align: left;
        font-size: 13px;
      }

      :deep(th) {
        background: linear-gradient(135deg, rgba(17, 17, 17, 0.1), rgba(17, 17, 17, 0.05));
        font-weight: 700;
        color: var(--color-primary, #111111);
        text-transform: uppercase;
        font-size: 12px;
        letter-spacing: 0.5px;
      }

      :deep(tr:nth-child(even) td) {
        background: var(--color-fill-1);
      }

      :deep(tr:hover td) {
        background: var(--color-fill-2);
      }

      /* Code blocks */
      :deep(code) {
        font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
        background: var(--color-fill-2);
        padding: 2px 6px;
        border-radius: 4px;
        font-size: 13px;
        color: var(--color-primary, #111111);
        border: 1px solid var(--color-border-2);
      }

      :deep(pre) {
        background: var(--color-fill-2);
        padding: 12px;
        border-radius: 8px;
        overflow-x: auto;
        margin: 12px 0;
        border: 1px solid var(--color-border-2);
      }

      :deep(pre code) {
        background: transparent;
        padding: 0;
        border: none;
        color: var(--color-text-1);
        font-size: 13px;
      }

      /* Blockquotes */
      :deep(blockquote) {
        border-left: 4px solid var(--color-primary, #111111);
        padding-left: 16px;
        margin: 12px 0;
        color: var(--color-text-2);
        font-style: italic;
        background: var(--color-fill-1);
        padding: 12px 16px;
        border-radius: 4px;
      }

      /* Horizontal rules */
      :deep(hr) {
        border: none;
        border-top: 2px solid var(--color-border-2);
        margin: 16px 0;
      }

      /* Links */
      :deep(a) {
        color: var(--color-primary, #111111);
        text-decoration: none;
        border-bottom: 1px solid transparent;
        transition: all 0.2s;
      }

      :deep(a:hover) {
        border-bottom-color: var(--color-primary, #111111);
      }
    }

    .sources {
      margin-top: 8px;
      font-size: 12px;
      opacity: 0.8;

      .sources-label {
        font-weight: 600;
        margin-bottom: 4px;
      }

      .sources-content {
        background: var(--color-fill-1);
        padding: 8px;
        border-radius: 6px;
        border: 1px solid var(--color-border-2);
        font-size: 12px;
        line-height: 1.5;
      }
    }

    .timestamp {
      margin-top: 6px;
      font-size: 11px;
      opacity: 0.55;
    }
  }

  .input-container {
    margin-top: auto;
    position: static;
    background: var(--color-bg-2);
    padding-top: 8px;
    padding-bottom: 8px;
    border-top: 1px solid var(--color-border-2);
    z-index: 1;
    backdrop-filter: blur(8px);
    flex-shrink: 0;
    :deep(.arco-input-search) {
      .arco-input {
        border-radius: 20px 0 0 20px;
      }

      .arco-input-append {
        border-radius: 0 20px 20px 0;
      }

      .arco-btn {
        border-radius: 0 20px 20px 0;
        height: 100%;
        background: var(--color-primary, #111111) !important;
        border-color: var(--color-primary, #111111) !important;
        color: #ffffff !important;

        &:hover {
          background: #000000 !important;
          border-color: #000000 !important;
        }

        &:active {
          background: #1a1a1a !important;
          border-color: #1a1a1a !important;
        }
      }
    }
  }
}

.ai-chatbot.is-dark {
  .input-container {
    background: rgba(18, 20, 26, 0.95);
    border-top-color: #272b36;

    :deep(.arco-input-search) {
      .arco-btn {
        background: #1a1a1a !important;
        border-color: #1a1a1a !important;
        color: #ffffff !important;

        &:hover {
          background: #2a2a2a !important;
          border-color: #2a2a2a !important;
        }

        &:active {
          background: #0f0f0f !important;
          border-color: #0f0f0f !important;
        }
      }
    }
  }

  .messages-container {
    background: linear-gradient(180deg, #0f1115 0%, #12141a 100%);
    &::-webkit-scrollbar-track {
      background: #161922;
    }
    &::-webkit-scrollbar-thumb {
      background: #2a2f3a;
    }
    &::-webkit-scrollbar-thumb:hover {
      background: #3a4150;
    }
  }
  .message.assistant .content {
    background: #151823;
    color: #e6e9ef;
    border: 1px solid #272b36;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.35);
  }
  .message.user .message-wrapper .content {
    box-shadow: 0 6px 16px rgba(17, 17, 17, 0.35);
  }
  .content .text table {
    border-color: #2b3040;
  }
  .content .text th,
  .content .text td {
    border-color: #2b3040;
  }
  .content .text th {
    background: #1b2030;
    color: #e6e9ef;
  }
  .content .text code,
  .content .text pre code {
    background: #0f1420;
    color: #e6e9ef;
  }
  .content .sources .sources-content {
    background: #141926;
    border-color: #2b3040;
    color: #cfd6e4;
  }
  .content .timestamp {
    opacity: 0.7;
  }
}

::deep(html.arco-theme-dark) .ai-chatbot .messages-container,
::deep(body.arco-theme-dark) .ai-chatbot .messages-container,
::deep(.arco-theme-dark) .ai-chatbot .messages-container {
  background: linear-gradient(180deg, #0f1115 0%, #12141a 100%) !important;
}

::deep(html.arco-theme-dark) .ai-chatbot .message.assistant .content,
::deep(body.arco-theme-dark) .ai-chatbot .message.assistant .content,
::deep(.arco-theme-dark) .ai-chatbot .message.assistant .content {
  background: #151823 !important;
  color: #e6e9ef !important;
  border: 1px solid #272b36 !important;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.35) !important;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.empty-messages {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  padding: 40px 20px;
}

.empty-content {
  text-align: center;
  max-width: 300px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-1);
  margin-bottom: 8px;
}

.empty-description {
  font-size: 14px;
  color: var(--color-text-3);
  line-height: 1.5;
}

.is-dark .empty-title {
  color: var(--color-text-1);
}

.is-dark .empty-description {
  color: var(--color-text-3);
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 0;
}

.typing-indicator .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: var(--color-text-3);
  animation: typingDot 1.4s infinite;
}

/* Streaming cursor animation */
.streaming-cursor {
  display: inline-block;
  font-family: monospace;
  color: var(--color-primary, #111111);
  font-size: 14px;
  font-weight: bold;
  animation: blink 0.8s step-end infinite;
  margin-left: 2px;
  vertical-align: baseline;
  line-height: 1.7;
}

@keyframes typingDot {
  0%,
  60%,
  100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  30% {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

@keyframes blink {
  0%,
  50% {
    opacity: 1;
  }
  51%,
  100% {
    opacity: 0;
  }
}

.follow-up-suggestions {
  margin-top: 8px;

  .suggestions-buttons {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }

  .suggestion-btn {
    font-size: 11px;
    padding: 4px 10px;
    height: auto;
    min-height: 24px;
    border: 1px solid var(--color-border-2) !important;
    background: var(--color-bg-1) !important;
    color: var(--color-text-1) !important;
    border-radius: 12px;
    transition: all 0.2s ease;

    &:hover {
      background: rgba(17, 17, 17, 0.05) !important;
      border-color: var(--color-primary, #111111) !important;
      color: var(--color-primary, #111111) !important;
      transform: translateY(-1px);
      box-shadow: 0 2px 6px rgba(17, 17, 17, 0.15);
    }

    &:active {
      background: rgba(17, 17, 17, 0.1) !important;
    }

    &:focus {
      border-color: var(--color-primary, #111111) !important;
      color: var(--color-primary, #111111) !important;
    }
  }

  :deep(.suggestion-btn) {
    border-color: var(--color-border-2) !important;
    background: var(--color-bg-1) !important;
    color: var(--color-text-1) !important;

    &:hover {
      background: rgba(17, 17, 17, 0.05) !important;
      border-color: var(--color-primary, #111111) !important;
      color: var(--color-primary, #111111) !important;
    }
  }

  :deep(.suggestions-buttons .arco-btn) {
    border-color: var(--color-border-2) !important;
    background: var(--color-bg-1) !important;
    color: var(--color-text-1) !important;

    &:hover {
      background: rgba(17, 17, 17, 0.05) !important;
      border-color: var(--color-primary, #111111) !important;
      color: var(--color-primary, #111111) !important;
    }

    &:active {
      background: rgba(17, 17, 17, 0.1) !important;
    }
  }
}

.ai-chatbot.is-dark {
  .suggestion-btn {
    background: #151823 !important;
    border-color: #272b36 !important;
    color: #9aa4b2 !important;

    &:hover {
      background: rgba(255, 255, 255, 0.1) !important;
      border-color: rgba(255, 255, 255, 0.3) !important;
      color: #ffffff !important;
    }
  }

  :deep(.suggestions-buttons .arco-btn) {
    background: #151823 !important;
    border-color: #272b36 !important;
    color: #9aa4b2 !important;

    &:hover {
      background: rgba(255, 255, 255, 0.1) !important;
      border-color: rgba(255, 255, 255, 0.3) !important;
      color: #ffffff !important;
    }

    &:active {
      background: rgba(255, 255, 255, 0.15) !important;
    }
  }
}

.product-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border-2);
}
</style>
