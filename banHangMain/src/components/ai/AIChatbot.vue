<template>
  <div class="ai-chatbot" :class="{ 'is-dark': isDark }">
    <a-card class="chatbot-card" :bordered="false" :body-style="{ padding: '0' }">
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
                src="@/assets/logo-datn.png"
                alt="AI"
                class="ai-logo"
              />
            </div>
            <div class="content">
              <!-- Spinning animation - keep visible while AI is processing, even if content starts streaming -->
              <div
                v-if="msg.role === 'assistant' && (msg.processingStatus === 'querying' || msg.processingStatus === 'analyzing')"
                class="processing-indicator"
              >
                <a-spin :size="16" />
                <span style="margin-left: 8px">Đang trả lời...</span>
              </div>

              <!-- Content display with streaming animation -->
              <div v-if="msg.content && msg.content.trim().length > 0" class="text">
                <span v-html="renderMarkdown(msg.content)"></span><span v-if="msg.processingStatus === 'analyzing' || (isProcessing && msg.id === messages[messages.length - 1]?.id)" class="streaming-cursor">▋</span>
              </div>

              <!-- Product Cards - Each product in its own bubble -->
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

              <!-- Timestamp hidden for both user and assistant messages in AI popup -->
            </div>
          </div>
        </div>

        <!-- Loading indicator -->
        <div v-if="loading" class="message assistant">
          <div class="message-wrapper">
            <div class="avatar">
              <img
                src="@/assets/logo-datn.png"
                alt="AI"
                class="ai-logo"
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

function normalizeText(input: string | undefined): string {
  if (!input) return ''
  return input
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/đ/g, 'd')
    .replace(/[^a-z0-9\s]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
}

function filterProductsByMention(content: string | undefined, products: Product[] = []): Product[] {
  if (!products.length) return products
  const normalizedContent = normalizeText(content)
  if (!normalizedContent) return products
  const matched = products.filter((product) => {
    const normalizedName = normalizeText(product.name)
    return normalizedName && normalizedContent.includes(normalizedName)
  })
  return matched.length > 0 ? matched : products
}

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
const loadedConversationIds = ref<Set<number>>(new Set())
const processedMessageIds = ref<Set<number | string>>(new Set())

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
    // Save history for both authenticated and guest users
    // Guest history is stored in localStorage but not in database
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
    // Remove all localStorage keys related to chat
    localStorage.removeItem(STORAGE_KEY)
    localStorage.removeItem(CHAT_SESSIONS_KEY)
    localStorage.removeItem(SESSION_NAMES_KEY)
    localStorage.removeItem('__ai_chat_last_save')
    
    // Clear all sessionStorage flags
    sessionStorage.removeItem('__ai_chat_window_closed')
    sessionStorage.removeItem('__ai_chat_close_time')
    sessionStorage.removeItem('__ai_chat_actual_reload')
    
    // Reset all state
    chatSessions.value = {}
    sessionNames.value = {}
    currentSessionId.value = generateSessionId()
    loadedConversationIds.value.clear()
    processedMessageIds.value.clear()
    
    // Set default messages
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
    
    // DO NOT save history after clearing - let user start fresh
    // Only create session in memory, don't persist to localStorage
    sessionNames.value[currentSessionId.value] = 'Cuộc trò chuyện mới'
    chatSessions.value[currentSessionId.value] = [...messages.value]
    
    console.log('🧹 Chat history cleared completely')
  } catch (error) {
    console.error('Error clearing chat history:', error)
  }
}

function loadHistory() {
  try {
    // Load history for both authenticated and guest users
    // Guest history is only cleared on page reload (in onMounted), not when closing window
    
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
  if (props.staffChatMode) {
    const messageText = text.trim()
    input.value = ''
    
    // For guest users, receiverId can be null - backend will assign a staff member
    // For authenticated users, receiverId should be set
    const receiverId = props.staffReceiverId
    
    if (!receiverId && userStore.isAuthenticated) {
      Message.error('Không xác định được nhân viên nhận tin nhắn. Vui lòng thử lại.')
      input.value = messageText // Restore input
      return
    }
    
    // Add optimistic message immediately to show in UI
    const optimisticUserMessage: ChatMessage = {
      id: Date.now(),
      role: 'user',
      content: messageText,
      timestamp: new Date().toLocaleTimeString('vi-VN'),
    }
    messages.value.push(optimisticUserMessage)
    await nextTick()
    scrollToBottom()
    saveHistory()
    
    try {
      // Send to staff via WebSocket - it will add the message to chatStore
      // The watcher will pick it up and update the message if needed
      // For guest users, backend will assign a staff member when receiverId is null
      await chatStore.sendMessageViaWebSocket({
        receiverId: receiverId || 0, // Use 0 for guest, backend will handle
        content: messageText,
        messageType: 'TEXT',
      })
      emit('staff-message-sent', messageText)
      return
    } catch (error: any) {
      Message.error('Không thể gửi tin nhắn đến nhân viên. Vui lòng thử lại.')
      // Remove optimistic message on error
      const msgIndex = messages.value.findIndex((m) => m.id === optimisticUserMessage.id)
      if (msgIndex !== -1) {
        messages.value.splice(msgIndex, 1)
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

    // Build chat history from previous messages (last 20 messages to provide more context)
    // Exclude the current user message and any messages without content
    const recentMessages = messages.value
      .filter((msg) => {
        // Include user messages and assistant messages with actual content
        // Exclude suggestion-only messages, product-only messages, and empty messages
        if (msg.role === 'user') {
          return msg.content && msg.content.trim() && msg.id !== userMessage.id
        }
        if (msg.role === 'assistant') {
          // Include assistant messages with content, but exclude:
          // - Messages that are only suggestions (isSuggestionsOnly)
          // - Messages that are only product cards (isProductSuggestion without content)
          return msg.content && 
                 msg.content.trim() && 
                 !msg.isSuggestionsOnly &&
                 !(msg.isProductSuggestion && !msg.content.trim())
        }
        return false
      })
      .slice(-20) // Last 20 messages for better context
      .map((msg) => ({
        role: msg.role as 'user' | 'assistant',
        content: msg.content || '',
      }))
    
    console.log('📝 Sending chat history to LLM:', {
      totalMessages: messages.value.length,
      filteredHistory: recentMessages.length,
      historyPreview: recentMessages.slice(-3).map(m => `${m.role}: ${m.content.substring(0, 50)}...`)
    })
    
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
          
          // If we already have streamed content, mark as ready immediately
          if (msg.content && msg.content.trim().length > 0) {
            msg.processingStatus = 'ready'
          } else {
            // No content yet: keep spinner visible while we call fallback API
            msg.processingStatus = 'analyzing'
            try {
              const fallback = await chatWithAI(text)
              if (fallback && fallback.message && fallback.message.trim().length > 0) {
                msg.content = fallback.message.trim()
                if (fallback.queryType) {
                  msg.queryType = fallback.queryType
                  receivedQueryType = receivedQueryType || fallback.queryType
                }
                if (fallback.redirect_to_staff) {
                  shouldRedirectToStaff = true
                }
                if (Array.isArray(fallback.products) && fallback.products.length > 0) {
                  msg.products = fallback.products
                  receivedProducts = [...fallback.products]
                }
              }
            } catch (fallbackError) {
              console.error('Fallback chatWithAI failed:', fallbackError)
            } finally {
              msg.processingStatus = 'ready'
            }
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
          setTimeout(() => {
            emit('redirect-to-staff')
          }, 1500)
          return
        }

        // Product cards & suggestions logic unchanged
        const userMessageLower = text.toLowerCase().trim()

        const hasExplicitSuggestionRequest = 
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
          ((userMessageLower.includes('cho tôi') || userMessageLower.includes('show me')) && (
            userMessageLower.includes('giày') ||
            userMessageLower.includes('shoe') ||
            userMessageLower.includes('sản phẩm') ||
            userMessageLower.includes('product')
          ))

        const discountKeywords = [
          'giảm giá',
          'đang giảm',
          'đang giảm giá',
          'đang khuyến mãi',
          'khuyến mãi',
          'promotion',
          'discount',
          'sale',
          'mã giảm',
          'voucher',
          'sản phẩm nào đang giảm giá',
          'sản phẩm nào giảm giá'
        ]
        const mentionsDiscount = discountKeywords.some((keyword) => userMessageLower.includes(keyword))

        const availabilityKeywords = [
          'còn hàng',
          'còn hàng không',
          'có hàng',
          'có hàng không',
          'còn không',
          'có còn không',
          'còn tồn kho',
          'tồn kho',
          'stock',
          'còn lại',
          'sản phẩm này còn hàng',
          'sản phẩm này còn hàng không',
          'sản phẩm này có hàng không'
        ]
        const mentionsAvailability = availabilityKeywords.some((keyword) => userMessageLower.includes(keyword))

        const resolvedQueryType = aiMsg?.queryType || receivedQueryType
        const isPromotionIntent = resolvedQueryType === 'promotion_inquiry'
        const isProductIntent = resolvedQueryType === 'product_inquiry'

        const aiResponseLower = (aiMsg?.content || fullContent).toLowerCase()
        const isErrorResponse = aiResponseLower.includes('lỗi') || 
                               aiResponseLower.includes('sự cố') || 
                               aiResponseLower.includes('không thể') ||
                               aiResponseLower.includes('gặp sự cố') ||
                               aiResponseLower.includes('liên hệ nhân viên') ||
                               aiResponseLower.includes('xin lỗi') && !aiResponseLower.includes('sản phẩm')
        
        const aiMentionsProducts = aiResponseLower.includes('sản phẩm') ||
                                  aiResponseLower.includes('giày') ||
                                  aiResponseLower.includes('giảm giá') ||
                                  aiResponseLower.includes('khuyến mãi') ||
                                  aiResponseLower.includes('nike') ||
                                  aiResponseLower.includes('adidas') ||
                                  aiResponseLower.includes('puma') ||
                                  aiResponseLower.includes('vans')
        
        const isProductSuggestionQuery = (
          (isPromotionIntent && (hasExplicitSuggestionRequest || mentionsDiscount)) ||
          (isProductIntent && (hasExplicitSuggestionRequest || mentionsAvailability))
        ) && !isErrorResponse && aiMentionsProducts
        
        const productsToCheck = aiMsg?.products || receivedProducts
        const filteredProducts = filterProductsByMention(
          aiMsg?.content || fullContent,
          productsToCheck || []
        )
        
        if (isProductSuggestionQuery && filteredProducts && filteredProducts.length > 0) {
          console.log('📦 Creating separate product cards messages (one per product):', {
            queryType: aiMsg?.queryType || receivedQueryType,
            productsCount: filteredProducts.length,
            userMessage: text.substring(0, 50),
            hasExplicitSuggestionRequest,
            products: filteredProducts
          })
          
          if (aiMsg) {
            aiMsg.products = undefined
          }
          
          let baseId = Date.now()
          for (const product of filteredProducts) {
            const productMessage: ChatMessage = {
              id: baseId++,
              role: 'assistant',
              content: '',
              timestamp: new Date().toLocaleTimeString('vi-VN'),
              products: [product],
              processingStatus: 'ready',
              isProductSuggestion: true,
            }
            messages.value.push(productMessage)
          }
          
          await nextTick()
          scrollToBottom()
        } else if (aiMsg && aiMsg.products && aiMsg.products.length > 0) {
          console.log('📦 Products received but not a suggestion query, removing from message', {
            queryType: aiMsg.queryType,
            hasExplicitSuggestionRequest,
            userMessage: text.substring(0, 50)
          })
          aiMsg.products = undefined
        }
        
        if (aiMsg && aiMsg.content && aiMsg.content.trim()) {
          let suggestions: string[] = []
          
          if (aiMsg.followUpSuggestions && aiMsg.followUpSuggestions.length > 0) {
            suggestions = [...aiMsg.followUpSuggestions]
          } else {
            suggestions = generateDefaultSuggestions(aiMsg.content, messages.value)
          }
          
          aiMsg.followUpSuggestions = undefined
          aiMsg.isSuggestionsOnly = false
          
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
      async (error: string) => {
        loading.value = false
        isProcessing.value = false
        Message.error(`Lỗi khi gửi tin nhắn: ${error}`)

        const msgIndex = messages.value.findIndex((m) => m.id === aiMessageId)
        if (msgIndex !== -1) {
          messages.value[msgIndex].content = '❌ Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.'
        }
      },
      recentMessages
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

// Watch for staff chat mode activation and load existing messages (only once per conversation)
watch(
  () => [props.staffChatMode, props.staffConversationId],
  async ([isStaffMode, convId]) => {
    if (!isStaffMode || !convId || typeof convId !== 'number') return
    
    // Only load once per conversation
    if (loadedConversationIds.value.has(convId)) return
    
    // Load existing messages from the conversation
    if (chatStore.activeMessages && Array.isArray(chatStore.activeMessages)) {
      // Mark this conversation as loaded
      loadedConversationIds.value.add(convId)
      
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
    } else {
      // No receiver ID or conversation ID yet
      // For guest users, find any CUSTOMER_STAFF conversation
      // For authenticated users, find conversation matching their ID
      const matchingConv = chatStore.conversations.find((c: any) => {
        if (c.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
          if (userStore.id) {
            // Authenticated user
            return c.nhanVienId === receiverId || c.khachHangId === userStore.id
          } else {
            // Guest user - accept any CUSTOMER_STAFF conversation
            // The conversation will have khachHangId set to guest customer ID by backend
            return true
          }
        }
        return false
      })
      
      if (matchingConv && allMessages && typeof allMessages === 'object' && !Array.isArray(allMessages)) {
        const convMessages = (allMessages as Record<number, any[]>)[matchingConv.id]
        if (Array.isArray(convMessages)) {
          messagesToProcess = convMessages
        }
      } else if (!convId) {
        // No conversation yet, but we can still check all messages
        // This handles the case where a message arrives before the conversation is created
        Object.values(allMessages || {}).forEach((msgs: any) => {
          if (Array.isArray(msgs)) {
            msgs.forEach((msg: any) => {
              if (userStore.id) {
                // Authenticated user - check by senderId/receiverId
                if ((msg.senderId === receiverId && msg.receiverId === userStore.id) ||
                    (msg.receiverId === receiverId && msg.senderId === userStore.id)) {
                  messagesToProcess.push(msg)
                }
              } else {
                // Guest user - accept messages where:
                // 1. Message is from staff to guest (senderId is staff, receiverId is guest customer ID)
                // 2. Message is from guest to staff (senderId is guest customer ID, receiverId is staff)
                // Since we don't know guest customer ID yet, we accept all messages in CUSTOMER_STAFF conversations
                // The addMessageToState will have created a conversation with the correct IDs
                if (msg.senderId && msg.senderId !== 0 && msg.receiverId && msg.receiverId !== 0) {
                  // This is a valid message - check if it's in a CUSTOMER_STAFF conversation
                  const msgConv = chatStore.conversations.find((c: any) => {
                    if (c.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
                      return (c.khachHangId === msg.senderId && c.nhanVienId === msg.receiverId) ||
                             (c.nhanVienId === msg.senderId && c.khachHangId === msg.receiverId) ||
                             (c.khachHangId === msg.receiverId && c.nhanVienId === msg.senderId) ||
                             (c.nhanVienId === msg.receiverId && c.khachHangId === msg.senderId)
                    }
                    return false
                  })
                  if (msgConv) {
                    messagesToProcess.push(msg)
                  }
                }
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
      let isRelevant = false
      if (convId) {
        // If we have convId, we already filtered by conversation
        isRelevant = true
      } else if (userStore.id) {
        // Authenticated user - check by receiverId
        isRelevant = receiverId !== null && (staffMsg.senderId === receiverId || staffMsg.receiverId === receiverId)
      } else {
        // Guest user - accept messages from any staff (senderId is staff, receiverId is guest customer ID)
        // or messages to any staff (senderId is guest customer ID, receiverId is staff)
        isRelevant = staffMsg.senderId && staffMsg.senderId !== 0 && 
                     staffMsg.receiverId && staffMsg.receiverId !== 0 &&
                     staffMsg.senderId !== staffMsg.receiverId
      }
      
      if (!isRelevant) return
      
      // Check if message already exists - improved duplicate detection
      const msgId = staffMsg.id || staffMsg.maTinNhan
      const msgContent = staffMsg.content || ''
      const msgSentAt = staffMsg.sentAt ? new Date(staffMsg.sentAt).getTime() : 0
      
      // Create a unique key for this message
      const messageKey = msgId ? `${msgId}` : `${staffMsg.senderId}-${msgContent}-${msgSentAt}`
      
      // First check if we've already processed this message
      if (processedMessageIds.value.has(messageKey)) {
        console.log('⚠️ Skipped already processed message:', msgContent.substring(0, 50))
        return
      }
      
      // Then check if message exists in messages array
      // First determine if this is a user message to check the correct role
      let isUserMsgForDuplicateCheck = false
      if (userStore.id) {
        isUserMsgForDuplicateCheck = staffMsg.senderId === userStore.id
      } else {
        // For guest users, determine based on message direction
        // This will be determined more accurately below, but for now use heuristic
        const senderIsLow = staffMsg.senderId && staffMsg.senderId < 10
        const receiverIsHigh = staffMsg.receiverId && staffMsg.receiverId > 10
        const senderIsHigh = staffMsg.senderId && staffMsg.senderId > 10
        const receiverIsLow = staffMsg.receiverId && staffMsg.receiverId < 10
        
        // If sender is high ID and receiver is low ID, likely guest to staff
        isUserMsgForDuplicateCheck = senderIsHigh && receiverIsLow
      }
      
      const exists = messages.value.some((msg) => {
        // Check by ID if available
        if (msgId && typeof msg.id === 'number' && msg.id === msgId) {
          return true
        }
        
        // Check by content + role + timestamp (within 5 seconds for better matching)
        if (msgContent && msg.content === msgContent) {
          const expectedRole = isUserMsgForDuplicateCheck ? 'user' : 'assistant'
          if (msg.role === expectedRole) {
            // If we have timestamp, check within 5 seconds
            if (msgSentAt > 0) {
              const msgTimestamp = msg.timestamp ? new Date(`2000-01-01 ${msg.timestamp}`).getTime() : 0
              const timeDiff = Math.abs(msgSentAt - msgTimestamp)
              if (timeDiff < 5000 || msgTimestamp === 0) {
                return true
              }
            } else {
              // No timestamp - match by content and role only (within last 10 seconds)
              return true
            }
          }
        }
        
        return false
      })
      
      if (!exists) {
        // Mark as processed
        processedMessageIds.value.add(messageKey)
        
        // Check if this is a user message (sent by current user/guest)
        // For guest users, check if senderId matches guest customer ID
        // Guest customer ID will be in senderId when guest sends, or in receiverId when staff sends to guest
        // We need to find the conversation to determine guest customer ID
        let isUserMessage = false
        if (userStore.id) {
          // Authenticated user
          isUserMessage = staffMsg.senderId === userStore.id
        } else {
          // Guest user - find conversation to get guest customer ID
          // Find conversation matching exact message direction only (not reversed)
          const guestConv = chatStore.conversations.find((c: any) => {
            if (c.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
              // Match exact direction:
              // - Guest sends to staff: senderId === khachHangId && receiverId === nhanVienId
              // - Staff sends to guest: senderId === nhanVienId && receiverId === khachHangId
              return (c.khachHangId === staffMsg.senderId && c.nhanVienId === staffMsg.receiverId) ||
                     (c.nhanVienId === staffMsg.senderId && c.khachHangId === staffMsg.receiverId)
            }
            return false
          })
          
          // Determine role based on message direction FIRST, then verify with conversation
          // This is more reliable when there are multiple conversations or conversation is created incorrectly
          
          // PRIORITY 1: Determine role based on message direction, not conversation
          // Check if senderId is staff ID in ANY conversation (more reliable)
          const senderIsStaff = chatStore.conversations.some((c: any) => 
            c.loaiCuocTraoDoi === 'CUSTOMER_STAFF' && c.nhanVienId === staffMsg.senderId
          )
          // Check if receiverId is guest customer ID in ANY conversation
          const receiverIsGuest = chatStore.conversations.some((c: any) => 
            c.loaiCuocTraoDoi === 'CUSTOMER_STAFF' && c.khachHangId === staffMsg.receiverId
          )
          
          // Find conversation that matches exact direction (for reference only)
          const correctConvForMessage = chatStore.conversations.find((c: any) => {
            if (c.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
              // Match conversation where message direction is EXACT:
              // - Staff sends to guest: senderId === nhanVienId && receiverId === khachHangId
              // - Guest sends to staff: senderId === khachHangId && receiverId === nhanVienId
              return (c.nhanVienId === staffMsg.senderId && c.khachHangId === staffMsg.receiverId) ||
                     (c.khachHangId === staffMsg.senderId && c.nhanVienId === staffMsg.receiverId)
            }
            return false
          })
          
          // Determine role based on message direction checks (more reliable than conversation)
          // PRIORITY 1: Check if sender is staff AND receiver is guest (staff sends to guest)
          // This is the most reliable check: if sender is staff ID AND receiver is guest customer ID
          const senderIsStaffAndReceiverIsGuest = senderIsStaff && receiverIsGuest
          
          // PRIORITY 2: Check if sender is guest customer ID in any conversation
          const senderIsGuest = chatStore.conversations.some((c: any) => 
            c.loaiCuocTraoDoi === 'CUSTOMER_STAFF' && c.khachHangId === staffMsg.senderId
          )
          
          // Determine role with priority order:
          // PRIORITY 0: Use ID-based heuristic FIRST (most reliable when conversation is missing/wrong)
          // This works even when conversation is not created yet or is wrong
          const senderIsLow = staffMsg.senderId && staffMsg.senderId < 10
          const receiverIsHigh = staffMsg.receiverId && staffMsg.receiverId > 10
          const senderIsHigh = staffMsg.senderId && staffMsg.senderId > 10
          const receiverIsLow = staffMsg.receiverId && staffMsg.receiverId < 10
          
          // If sender is low ID (< 10) and receiver is high ID (> 10), likely staff to guest
          if (senderIsLow && receiverIsHigh && staffMsg.senderId !== 0 && staffMsg.receiverId !== 0) {
            // Sender is low ID (likely staff), receiver is high ID (likely guest) → staff message
            isUserMessage = false
          }
          // If sender is high ID (> 10) and receiver is low ID (< 10), likely guest to staff
          else if (senderIsHigh && receiverIsLow && staffMsg.senderId !== 0 && staffMsg.receiverId !== 0) {
            // Sender is high ID (likely guest), receiver is low ID (likely staff) → user message
            isUserMessage = true
          }
          // PRIORITY 1: If receiver is guest customer ID, sender must be staff (most reliable)
          else if (receiverIsGuest && staffMsg.senderId && staffMsg.senderId !== 0) {
            // Receiver is guest customer → sender is staff → staff message
            isUserMessage = false
          }
          // PRIORITY 2: If sender is staff AND receiver is guest (double check)
          else if (senderIsStaffAndReceiverIsGuest) {
            // Sender is staff AND receiver is guest → staff message
            isUserMessage = false
          }
          // PRIORITY 3: If sender is staff (but receiver might not be identified as guest yet)
          else if (senderIsStaff) {
            // Sender is staff → staff message
            isUserMessage = false
          }
          // PRIORITY 4: If sender is guest AND receiver is NOT guest (to avoid false positive)
          // BUT: Only trust this if we also check that sender is NOT staff
          // This prevents false positive when admin ID (1) exists in both tables
          else if (senderIsGuest && !receiverIsGuest && !senderIsStaff) {
            // Sender is guest customer AND receiver is not guest AND sender is not staff → user message
            isUserMessage = true
          } 
          // PRIORITY 3: Use conversation if found and matches exact direction
          else if (correctConvForMessage) {
            // Fallback: use conversation if found
            // Case 1: Staff sends to guest
            if (staffMsg.senderId === correctConvForMessage.nhanVienId && 
                staffMsg.receiverId === correctConvForMessage.khachHangId) {
              isUserMessage = false
            }
            // Case 2: Guest sends to staff
            else if (staffMsg.senderId === correctConvForMessage.khachHangId && 
                     staffMsg.receiverId === correctConvForMessage.nhanVienId) {
              isUserMessage = true
            }
            // Fallback
            else {
              isUserMessage = staffMsg.senderId === correctConvForMessage.khachHangId
            }
          } else {
            // No conversation found - use heuristics based on message structure
            // When guest sends to staff: senderId = guest customer ID (newly created, usually > 10), receiverId = staff ID (usually 1, 2, etc.)
            // When staff sends to guest: senderId = staff ID (usually 1, 2, etc.), receiverId = guest customer ID (newly created, usually > 10)
            // Heuristic: If senderId is low (< 10) and receiverId is high (> 10), likely staff to guest
            // If senderId is high (> 10) and receiverId is low (< 10), likely guest to staff
            if (staffMsg.senderId && staffMsg.senderId !== 0 && 
                staffMsg.receiverId && staffMsg.receiverId !== 0 &&
                staffMsg.senderId !== staffMsg.receiverId) {
              // Both are valid IDs and different
              // Heuristic: staff IDs are usually low (1, 2, etc.), guest customer IDs are usually high (newly created)
              const senderIsLow = staffMsg.senderId < 10
              const receiverIsHigh = staffMsg.receiverId > 10
              const senderIsHigh = staffMsg.senderId > 10
              const receiverIsLow = staffMsg.receiverId < 10
              
              if (senderIsLow && receiverIsHigh) {
                // Sender is low ID (likely staff), receiver is high ID (likely guest) → staff message
                isUserMessage = false
              } else if (senderIsHigh && receiverIsLow) {
                // Sender is high ID (likely guest), receiver is low ID (likely staff) → user message
                isUserMessage = true
              } else {
                // Can't determine - default to staff message (safer assumption)
                isUserMessage = false
              }
            } else {
              // SenderId is 0 or null → definitely guest
              isUserMessage = staffMsg.senderId === null || staffMsg.senderId === 0
            }
          }
          
          // Debug logging
          console.log('🔍 Guest message role determination:', {
            senderId: staffMsg.senderId,
            receiverId: staffMsg.receiverId,
            receiverIsGuest,
            senderIsStaff,
            senderIsGuest,
            senderIsStaffAndReceiverIsGuest,
            hasCorrectConv: !!correctConvForMessage,
            correctConvKhachHangId: correctConvForMessage?.khachHangId,
            correctConvNhanVienId: correctConvForMessage?.nhanVienId,
            isUserMessage,
            role: isUserMessage ? 'user' : 'assistant',
            content: msgContent.substring(0, 30),
            allConversations: chatStore.conversations.filter((c: any) => c.loaiCuocTraoDoi === 'CUSTOMER_STAFF').map((c: any) => ({
              id: c.id,
              khachHangId: c.khachHangId,
              nhanVienId: c.nhanVienId
            }))
          })
        }
        
        if (isUserMessage) {
          // User message (customer/guest sent this)
          // Check if we already have this message (optimistic message)
          // Find optimistic message by content + role, prioritizing recent messages
          // Optimistic messages are added immediately, so they should be at the end of the array
          let existingMsg: ChatMessage | undefined
          for (let i = messages.value.length - 1; i >= 0; i--) {
            const m = messages.value[i]
            if (m.role === 'user' && m.content === msgContent) {
              // Check if this is likely the optimistic message
              // Optimistic messages have temporary IDs (usually high timestamp)
              // Real messages have server IDs (usually lower)
              const isOptimistic = typeof m.id === 'number' && m.id > Date.now() - 60000 // Created in last minute
              if (isOptimistic || !existingMsg) {
                existingMsg = m
                // If we found an optimistic message, break immediately
                if (isOptimistic) break
              }
            }
          }
          
          if (!existingMsg) {
            const chatMsg: ChatMessage = {
              id: msgId || Date.now(),
              role: 'user',
              content: msgContent,
              timestamp: staffMsg.sentAt ? new Date(staffMsg.sentAt).toLocaleTimeString('vi-VN') : new Date().toLocaleTimeString('vi-VN'),
            }
            messages.value.push(chatMsg)
            console.log('✅ Added user message to AI chat:', chatMsg.content.substring(0, 50))
            nextTick(() => scrollToBottom())
            saveHistory()
          } else {
            // Update existing optimistic message with real ID and timestamp
            existingMsg.id = msgId || existingMsg.id
            if (staffMsg.sentAt) {
              existingMsg.timestamp = new Date(staffMsg.sentAt).toLocaleTimeString('vi-VN')
            }
            console.log('🔄 Updated optimistic message with real ID:', msgId)
          }
        } else {
          // Staff message (staff sent this to customer/guest, displayed as assistant)
          const chatMsg: ChatMessage = {
            id: msgId || Date.now(),
            role: 'assistant',
            content: msgContent,
            timestamp: staffMsg.sentAt ? new Date(staffMsg.sentAt).toLocaleTimeString('vi-VN') : new Date().toLocaleTimeString('vi-VN'),
          }
          messages.value.push(chatMsg)
          console.log('✅ Added staff message to AI chat:', chatMsg.content.substring(0, 50))
          nextTick(() => scrollToBottom())
          saveHistory()
        }
      } else {
        // Mark as processed even if it exists to avoid future checks
        processedMessageIds.value.add(messageKey)
        console.log('⚠️ Skipped duplicate message:', msgContent.substring(0, 50))
      }
    })
  },
  { deep: true }
)

// Watch for user logout to clear chat history (frontend only - database preserved)
watch(
  () => userStore.isAuthenticated,
  (isAuthenticated) => {
    if (!isAuthenticated) {
      // User logged out - clear frontend chat history completely
      // Database messages are preserved for admin to read
      try {
        // Remove all localStorage keys
        localStorage.removeItem(STORAGE_KEY)
        localStorage.removeItem(CHAT_SESSIONS_KEY)
        localStorage.removeItem(SESSION_NAMES_KEY)
        localStorage.removeItem('__ai_chat_last_save')
        
        // Clear all sessionStorage flags
        sessionStorage.removeItem('__ai_chat_window_closed')
        sessionStorage.removeItem('__ai_chat_close_time')
        sessionStorage.removeItem('__ai_chat_actual_reload')
        
        // Reset all state
        currentSessionId.value = ''
        chatSessions.value = {}
        sessionNames.value = {}
        loadedConversationIds.value.clear()
        processedMessageIds.value.clear()
        
        messages.value = [
          {
            id: 0,
            role: 'assistant',
            content: 'Xin chào! Tôi là trợ lý AI của GearUp. Tôi có thể giúp gì cho bạn?',
            timestamp: new Date().toLocaleTimeString('vi-VN'),
          },
        ]
        
        // DO NOT save history after logout - start completely fresh
        console.log('🧹 Chat history cleared on logout')
      } catch (error) {
        console.error('Error clearing chat history on logout:', error)
      }
    }
  },
  { immediate: false }
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
  try {
    // Remove all localStorage keys related to chat
    localStorage.removeItem(STORAGE_KEY)
    localStorage.removeItem(CHAT_SESSIONS_KEY)
    localStorage.removeItem(SESSION_NAMES_KEY)
    localStorage.removeItem('__ai_chat_last_save')
    
    // Clear all sessionStorage flags
    sessionStorage.removeItem('__ai_chat_window_closed')
    sessionStorage.removeItem('__ai_chat_close_time')
    sessionStorage.removeItem('__ai_chat_actual_reload')
    
    // Reset all state
    chatSessions.value = {}
    sessionNames.value = {}
    currentSessionId.value = generateSessionId()
    loadedConversationIds.value.clear()
    processedMessageIds.value.clear()

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
    
    // Save the cleared state (empty session) to localStorage
    saveHistory()

    nextTick(() => {
      scrollToBottom()
    })
  } catch (error) {
    console.error('Error clearing messages:', error)
  }
}

let healthTimer: any = null

onMounted(async () => {
  // For authenticated users, always load history
  if (userStore.isAuthenticated) {
    loadHistory()
  } else {
    // Guest users:
    // - If page was truly reloaded (F5), clear history once
    // - If chat popup was just closed/reopened, keep history
    const wasActualReload = sessionStorage.getItem('__ai_chat_actual_reload') === 'true'
    if (wasActualReload) {
      try {
        clearChatHistory()
      } catch (e) {
        console.error('Error clearing guest chat history on reload:', e)
      } finally {
        sessionStorage.removeItem('__ai_chat_actual_reload')
      }
    } else {
      loadHistory()
    }
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
  
  // Save history before window closes (for both authenticated and guest)
  // Use both beforeunload and pagehide for better reliability
  const handleBeforeUnload = (event: BeforeUnloadEvent) => {
    // Save history synchronously before window closes
    try {
      if (messages.value.length > 0) {
        normalizeSuggestionMessages(messages.value)
        const messagesToSave = [...messages.value]
        
        if (currentSessionId.value) {
          chatSessions.value[currentSessionId.value] = [...messagesToSave]
        }
        
        // Use synchronous localStorage operations
        localStorage.setItem(CHAT_SESSIONS_KEY, JSON.stringify(chatSessions.value))
        localStorage.setItem(SESSION_NAMES_KEY, JSON.stringify(sessionNames.value))
        localStorage.setItem(STORAGE_KEY, JSON.stringify(messagesToSave))
        
        // Force synchronous write to localStorage
        localStorage.setItem('__ai_chat_last_save', Date.now().toString())
        
        // Mark that window is closing (not reloading)
        sessionStorage.setItem('__ai_chat_window_closed', 'true')
        sessionStorage.setItem('__ai_chat_close_time', Date.now().toString())
        
        console.log('💾 Saved chat history on beforeunload:', messagesToSave.length, 'messages')
      }
    } catch (e) {
      console.error('Error saving history on beforeunload:', e)
    }
  }
  
  // Detect actual page reload (F5) vs component remount
  // Only set reload flag on actual page navigation/reload, not component remount
  const handleActualReload = () => {
    // This will only fire on actual page reload, not component remount
    sessionStorage.setItem('__ai_chat_actual_reload', 'true')
  }
  
  // Listen for actual page reload (only fires on real reload, not component remount)
  window.addEventListener('beforeunload', handleActualReload)
  
  // pagehide is more reliable than beforeunload for saving data
  const handlePageHide = (event: PageTransitionEvent) => {
    // Save history when page is being hidden (window close, tab switch, etc.)
    try {
      if (messages.value.length > 0) {
        normalizeSuggestionMessages(messages.value)
        const messagesToSave = [...messages.value]
        
        if (currentSessionId.value) {
          chatSessions.value[currentSessionId.value] = [...messagesToSave]
        }
        
        // Use synchronous localStorage operations
        localStorage.setItem(CHAT_SESSIONS_KEY, JSON.stringify(chatSessions.value))
        localStorage.setItem(SESSION_NAMES_KEY, JSON.stringify(sessionNames.value))
        localStorage.setItem(STORAGE_KEY, JSON.stringify(messagesToSave))
        
        // Mark that we saved and this is NOT a reload
        // Only mark as window close if not persisted (not from cache/back-forward)
        if (!event.persisted) {
          // Not from cache - this could be window close or component unmount
          // Set a timestamp to help detect if it's a real page reload
          sessionStorage.setItem('__ai_chat_window_closed', 'true')
          sessionStorage.setItem('__ai_chat_close_time', Date.now().toString())
          console.log('💾 Saved chat history on pagehide (window close):', messagesToSave.length, 'messages')
        } else {
          console.log('💾 Saved chat history on pagehide (cached):', messagesToSave.length, 'messages')
        }
      }
    } catch (e) {
      console.error('Error saving history on pagehide:', e)
    }
  }
  
  // Handle page visibility to detect actual page reload
  const handlePageShow = (event: PageTransitionEvent) => {
    // If page was restored from cache (back/forward), don't clear history
    if (event.persisted) {
      // Page was restored from cache - load history
      if (!userStore.isAuthenticated) {
        loadHistory()
      }
    }
  }
  
  window.addEventListener('pageshow', handlePageShow)
  window.addEventListener('pagehide', handlePageHide)
  
  // Save history when tab becomes hidden (for both authenticated and guest)
  const handleVisibilityChange = () => {
    if (document.visibilityState === 'hidden') {
      // Save history for guest sessions too (don't clear on tab switch)
      saveHistory()
    }
  }
  
  // Save history periodically (every 30 seconds)
  const saveHistoryInterval = setInterval(() => {
    // Save for both authenticated and guest users
    if (messages.value.length > 0) {
      saveHistory()
    }
  }, 30000)
  
  window.addEventListener('beforeunload', handleBeforeUnload)
  window.addEventListener('beforeunload', handleActualReload)
  document.addEventListener('visibilitychange', handleVisibilityChange)
  
  // Store cleanup functions
  ;(window as any).__aiChatCleanup = () => {
    window.removeEventListener('beforeunload', handleBeforeUnload)
    window.removeEventListener('beforeunload', handleActualReload)
    window.removeEventListener('pagehide', handlePageHide)
    window.removeEventListener('pageshow', handlePageShow)
    document.removeEventListener('visibilitychange', handleVisibilityChange)
    if (saveHistoryInterval) clearInterval(saveHistoryInterval)
  }
})
onUnmounted(() => {
  // Save history before unmounting (for both authenticated and guest)
  // Use synchronous save to ensure it completes before component is destroyed
  if (messages.value.length > 0) {
    try {
      // Save immediately and synchronously
      normalizeSuggestionMessages(messages.value)
      const messagesToSave = [...messages.value]
      
      if (currentSessionId.value) {
        chatSessions.value[currentSessionId.value] = [...messagesToSave]
      }
      
      // Use synchronous localStorage operations
      localStorage.setItem(CHAT_SESSIONS_KEY, JSON.stringify(chatSessions.value))
      localStorage.setItem(SESSION_NAMES_KEY, JSON.stringify(sessionNames.value))
      localStorage.setItem(STORAGE_KEY, JSON.stringify(messagesToSave))
      
      // CRITICAL: Set flag to indicate window was closed (component unmount)
      // This flag will be checked when component mounts again to distinguish from page reload
      sessionStorage.setItem('__ai_chat_window_closed', 'true')
      sessionStorage.setItem('__ai_chat_close_time', Date.now().toString())
      
      console.log('💾 Saved chat history on unmount:', messagesToSave.length, 'messages')
      console.log('🏷️ Set window closed flag for guest session')
    } catch (e) {
      console.error('Error saving history on unmount:', e)
    }
  }
  
  if (healthTimer) clearInterval(healthTimer)
  const obs = (window as any).storefrontAiChatThemeObserver
  if (obs && typeof obs.disconnect === 'function') obs.disconnect()
  
  // Cleanup event listeners
  const cleanup = (window as any).__aiChatCleanup
  if (cleanup && typeof cleanup === 'function') {
    cleanup()
    delete (window as any).__aiChatCleanup
  }
})

watch(
  messages,
  () => {
    // Save history immediately when messages change (for both authenticated and guest)
    // This ensures history is always up-to-date, even if component unmounts suddenly
    if (messages.value.length > 0) {
      try {
        normalizeSuggestionMessages(messages.value)
        const messagesToSave = [...messages.value]
        
        if (currentSessionId.value) {
          chatSessions.value[currentSessionId.value] = [...messagesToSave]
        }
        
        // Save synchronously to localStorage
        localStorage.setItem(CHAT_SESSIONS_KEY, JSON.stringify(chatSessions.value))
        localStorage.setItem(SESSION_NAMES_KEY, JSON.stringify(sessionNames.value))
        localStorage.setItem(STORAGE_KEY, JSON.stringify(messagesToSave))
        
        emit('session-state', {
          sessions: chatSessions.value,
          currentSessionId: currentSessionId.value,
          sessionNames: sessionNames.value,
        })
      } catch (e) {
        console.error('Error saving history in watcher:', e)
      }
    }

    nextTick(() => {
      scrollToBottom()
    })
  },
  { deep: true, immediate: false }
)

// Watch for authentication status changes (duplicate watcher - kept for session-state emit)
watch(
  () => userStore.isAuthenticated,
  (isAuthenticated) => {
    if (!isAuthenticated) {
      // User logged out - history already cleared by the other watcher
      // Just emit session state update
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
  saveHistory, // Expose saveHistory so parent component can call it before unmount
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

    .ai-logo {
      max-width: 40px;
      max-height: 32px;
      width: auto;
      height: auto;
      object-fit: contain;
      display: block;
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
  margin-top: 8px;
  /* Remove border-top since each product is now in its own bubble */
}
</style>
