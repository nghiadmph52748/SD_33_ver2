<template>
  <div class="ai-chatbot" :class="{ 'is-dark': isDark }">
    <a-card class="chatbot-card" :bordered="false" :body-style="{ padding: '0' }">
      <template #title>
        <div class="chatbot-header">
          <span class="title">
            <img 
              src="//p3-armor.byteimg.com/tos-cn-i-49unhts6dw/dfdba5317c0c20ce20e64fac803d52bc.svg~tplv-49unhts6dw-image.image" 
              alt="AI Icon" 
              style="width: 20px; height: 20px; vertical-align: middle; margin-right: 6px;"
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

        <div v-for="msg in messages" :key="msg.id" :class="['message', msg.role, { 'suggestions-only': !msg.content && msg.followUpSuggestions }]">
          <div class="message-wrapper">
            <div class="avatar">
              <a-avatar
                v-if="msg.role === 'user'"
                :size="40"
                :style="userAvatarStyle"
              >
                {{ userInitials }}
              </a-avatar>
              <img
                v-else
                src="//p3-armor.byteimg.com/tos-cn-i-49unhts6dw/dfdba5317c0c20ce20e64fac803d52bc.svg~tplv-49unhts6dw-image.image"
                alt="AI"
                style="width: 28px; height: 28px;"
              />
            </div>
            <div class="content">
              <!-- Show EITHER loading indicator OR thinking block when isThinking -->
              <template v-if="msg.role === 'assistant' && msg.isThinking">
                <!-- Loading indicator - when no thinking content yet -->
                <div v-if="!msg.thinkingContent" class="processing-indicator">
                  <a-spin :size="16" />
                  <span style="margin-left: 8px;">Đang suy nghĩ...</span>
                  <span class="thinking-dots" aria-label="thinking">
                    <span class="dot" />
                    <span class="dot" />
                    <span class="dot" />
                  </span>
                </div>
                
                <!-- Thinking Block - when thinking content exists -->
                <div v-else class="thinking-block">
                  <a-collapse :default-active-key="['1']">
                    <a-collapse-item key="1">
                      <template #header>
                        <div class="thinking-header-wrapper">
                          <span class="thinking-label">Đang suy nghĩ...</span>
                          <span class="thinking-dots" aria-label="thinking">
                            <span class="dot" />
                            <span class="dot" />
                            <span class="dot" />
                          </span>
                        </div>
                      </template>
                      <div class="thinking-content" :ref="(el) => setThinkingRef(el, msg.id)">
                        {{ msg.thinkingContent }}
                        <span class="streaming-cursor">▋</span>
                      </div>
                    </a-collapse-item>
                  </a-collapse>
                </div>
              </template>

              <!-- Thinking Block when done (not actively thinking) -->
              <div v-if="msg.role === 'assistant' && !msg.isThinking && msg.thinkingContent" class="thinking-block">
                <a-collapse :default-active-key="[]">
                  <a-collapse-item key="1">
                    <template #header>
                      <div class="thinking-header-wrapper">
                        <span class="thinking-label">Đã xong</span>
                      </div>
                    </template>
                    <div class="thinking-content">
                      {{ msg.thinkingContent }}
                    </div>
                  </a-collapse-item>
                </a-collapse>
              </div>

              <!-- Content display - ALWAYS show if has content -->
              <div v-if="msg.content" class="text" v-html="renderMarkdown(msg.content)"></div>

              <!-- Data Source (only for messages with content) -->
              <div v-if="msg.role === 'assistant' && msg.content && msg.dataSource" class="message-metadata">
                <div class="data-source">
                  <span class="metadata-icon">📊</span>
                  <span class="metadata-label">Nguồn:</span>
                  <span class="metadata-value">{{ msg.dataSource }}</span>
                </div>
              </div>

              <!-- Follow-up Suggestions (only for messages WITHOUT content AND not processing) -->
              <div v-if="msg.role === 'assistant' && msg.isSuggestionsOnly === true && msg.followUpSuggestions && msg.followUpSuggestions.length > 0 && !isProcessing" class="follow-up-suggestions">
                <a-space wrap :size="6" class="suggestions-buttons">
                  <a-button
                    v-for="(suggestion, idx) in msg.followUpSuggestions"
                    :key="idx"
                    size="mini"
                    type="text"
                    :disabled="isProcessing"
                    @click="askQuestion(suggestion)"
                    class="suggestion-btn"
                  >
                    {{ suggestion }}
                  </a-button>
                </a-space>
              </div>

              <!-- Typing indicator - fallback -->
              <div
                v-if="msg.role === 'assistant' && !msg.isThinking && !msg.processingStatus && !msg.content && !msg.followUpSuggestions"
                class="text typing-indicator"
              >
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
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
                style="width: 28px; height: 28px;"
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
          placeholder="Hỏi AI bất cứ điều gì về hệ thống..."
          :loading="loading || isProcessing"
          :disabled="!isConnected || isProcessing"
          allow-clear
          search-button
          @search="handleSearch"
          @press-enter="handleSearch"
        >
          <template #button-icon>
            <icon-send />
          </template>
          <template #button-default>{{ isProcessing ? 'Đang xử lý...' : 'Gửi' }}</template>
        </a-input-search>
      </div>
    </a-card>

    <!-- Expanded view removed -->
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { parse as markedParse } from 'marked'
import { chatWithAI, chatWithAIStream, checkAIHealth } from '@/api/ai'
import { Message } from '@arco-design/web-vue'
import { IconSend } from '@arco-design/web-vue/es/icon'
import { useUserStore } from '@/store'

interface ChatMessage {
  id: number
  role: 'user' | 'assistant'
  content: string
  sources?: string
  timestamp: string
  isThinking?: boolean
  thinkingContent?: string // Nội dung reasoning bên trong <think> tags
  dataSource?: string // Nguồn dữ liệu
  followUpSuggestions?: string[] // Gợi ý câu hỏi tiếp theo
  queryType?: string // Loại truy vấn
  processingStatus?: string // Trạng thái xử lý: 'querying' | 'analyzing' | 'ready'
  isSuggestionsOnly?: boolean // Flag to render follow-up suggestions card
}

interface QuickAction {
  id: string
  label: string
  icon: string
  question: string
}

interface SessionState {
  sessions: Record<string, ChatMessage[]>
  currentSessionId: string
  sessionNames: Record<string, string>
}

// Props
const props = defineProps<{ suppressConnectionNotice?: boolean; enableHealthCheck?: boolean }>()
const shouldNotifyConnection = props.suppressConnectionNotice !== true
const shouldHealthCheck = props.enableHealthCheck === true

// Emits
const emit = defineEmits<{
  'session-state': [state: SessionState]
}>()

// State
const messages = ref<ChatMessage[]>([
  {
    id: 0,
    role: 'assistant',
    content:
      'Xin chào! Tôi là trợ lý AI của GearUp. Tôi có thể giúp bạn tra cứu thông tin về sản phẩm, doanh thu, tồn kho và nhiều thứ khác. Bạn cần giúp gì không? 😊',
    timestamp: new Date().toLocaleTimeString('vi-VN'),
  },
  {
    id: -1,
    role: 'assistant',
    content: '',
    timestamp: '',
    followUpSuggestions: [
      'Sản phẩm nào đang bán chạy nhất?',
      'So sánh top 3 sản phẩm bán chạy nhất',
      'Sản phẩm nào sắp hết hàng?',
      'Doanh thu hôm nay là bao nhiêu?',
    ],
    isSuggestionsOnly: true,
  },
])

const userStore = useUserStore()
const input = ref('')
const loading = ref(false)
const isProcessing = ref(false) // Prevent concurrent requests
const isConnected = ref(!shouldHealthCheck)
const messagesContainer = ref<HTMLElement | null>(null)
const isDark = ref(document.body.hasAttribute('arco-theme'))
const messagesContainerStyle = ref<Record<string, string>>({})
const currentSessionId = ref<string>('')
const chatSessions = ref<Record<string, ChatMessage[]>>({})
const sessionNames = ref<Record<string, string>>({})

const userInitials = computed(() => {
  const displayName = (userStore.name || userStore.tenTaiKhoan || '').trim()
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

const STORAGE_KEY = 'gearup_ai_chat_history_v1'
const CHAT_SESSIONS_KEY = 'gearup_ai_chat_sessions_v1'
const SESSION_NAMES_KEY = 'gearup_ai_session_names_v1'

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
      !msg.isThinking
    ) {
      msg.isSuggestionsOnly = true
    }
  })
}

function generateSessionName(msgs: ChatMessage[]): string {
  // Get the first user message after the welcome message
  const userMessages = msgs.filter((msg) => msg.role === 'user' && msg.id !== 0)
  if (userMessages.length === 0) return 'Cuộc trò chuyện mới'

  const firstUserMessage = userMessages[0].content.toLowerCase()

  // Common patterns for automatic naming
  const patterns = [
    { pattern: /sản phẩm.*bán chạy|top.*sản phẩm|bán nhiều nhất/, name: '📊 Top sản phẩm' },
    { pattern: /doanh thu|revenue|tháng này|tháng trước/, name: '💰 Doanh thu' },
    { pattern: /tồn kho|hết hàng|stock|inventory/, name: '⚠️ Tồn kho' },
    { pattern: /đơn hàng|order|trạng thái/, name: '📋 Đơn hàng' },
    { pattern: /khách hàng|customer|chi tiêu/, name: '👥 Khách hàng' },
    { pattern: /giảm giá|discount|promotion/, name: '🎉 Giảm giá' },
    { pattern: /nhân viên|employee|bán hàng/, name: '👨‍💼 Nhân viên' },
    { pattern: /kênh bán|online|tại quầy/, name: '🛒 Kênh bán hàng' },
    { pattern: /thống kê|statistics|báo cáo/, name: '📈 Thống kê' },
    { pattern: /tài chính|finance|tiền/, name: '💳 Tài chính' },
  ]

  // Check for patterns
  const match = patterns.find(({ pattern }) => pattern.test(firstUserMessage))
  if (match) {
    return match.name
  }

  // If no pattern matches, create a name from the first few words
  const words = firstUserMessage.split(' ').slice(0, 3)
  const truncated = words.join(' ')
  return truncated.length > 20 ? `${truncated.substring(0, 17)}...` : truncated
}

function saveHistory() {
  try {
    normalizeSuggestionMessages(messages.value)

    const messagesToSave = [...messages.value]

    // Save current session
    if (currentSessionId.value) {
      chatSessions.value[currentSessionId.value] = [...messagesToSave]
    }

    // Save all sessions
    localStorage.setItem(CHAT_SESSIONS_KEY, JSON.stringify(chatSessions.value))

    // Save session names
    localStorage.setItem(SESSION_NAMES_KEY, JSON.stringify(sessionNames.value))

    // Also save current session as active (for backward compatibility)
    localStorage.setItem(STORAGE_KEY, JSON.stringify(messagesToSave))

    // Notify parent about session state changes
    emit('session-state', {
      sessions: chatSessions.value,
      currentSessionId: currentSessionId.value,
      sessionNames: sessionNames.value,
    })
  } catch {
    // ignore storage errors
  }
}

function loadHistory() {
  try {
    // Load all sessions first
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

    // Load session names
    const namesRaw = localStorage.getItem(SESSION_NAMES_KEY)
    if (namesRaw) {
      const parsed: Record<string, string> = JSON.parse(namesRaw)
      if (parsed && typeof parsed === 'object') {
        sessionNames.value = parsed
      }
    }

    // If we have existing sessions, use the most recent one
    if (Object.keys(chatSessions.value).length > 0) {
      // Get the most recent session (highest timestamp)
      const sessionIds = Object.keys(chatSessions.value)
      const sortedSessions = sessionIds.sort((a, b) => {
        const timestampA = parseInt(a.split('_')[1], 10)
        const timestampB = parseInt(b.split('_')[1], 10)
        return timestampB - timestampA // Most recent first
      })

      const [mostRecentSessionId] = sortedSessions
      currentSessionId.value = mostRecentSessionId
      messages.value = [...chatSessions.value[currentSessionId.value]]
      normalizeSuggestionMessages(messages.value)
      const hasSuggestionCard = messages.value.some(
        msg => msg.role === 'assistant' && msg.isSuggestionsOnly === true && !msg.content && Array.isArray(msg.followUpSuggestions)
      )

      // Add separate suggestions message if it's just the welcome message
      if (!hasSuggestionCard && messages.value.length === 1 && messages.value[0].id === 0 && messages.value[0].role === 'assistant') {
        messages.value.push({
          id: -1,
          role: 'assistant',
          content: '',
          timestamp: '',
          followUpSuggestions: [
            'Sản phẩm nào đang bán chạy nhất?',
            'So sánh top 3 sản phẩm bán chạy nhất',
            'Sản phẩm nào sắp hết hàng?',
            'Doanh thu hôm nay là bao nhiêu?',
          ],
          isSuggestionsOnly: true,
        })
      }
    } else {
      // Load current session (backward compatibility)
      const raw = localStorage.getItem(STORAGE_KEY)
      if (raw) {
        const parsed: ChatMessage[] = JSON.parse(raw)
        if (Array.isArray(parsed) && parsed.length > 0) {
          messages.value = parsed
          normalizeSuggestionMessages(messages.value)
          const hasSuggestionCard = messages.value.some(
            msg => msg.role === 'assistant' && msg.isSuggestionsOnly === true && !msg.content && Array.isArray(msg.followUpSuggestions)
          )
          // Add separate suggestions message if it's just the welcome message
          if (!hasSuggestionCard && messages.value.length === 1 && messages.value[0].id === 0 && messages.value[0].role === 'assistant') {
            messages.value.push({
              id: -1,
              role: 'assistant',
              content: '',
              timestamp: '',
              followUpSuggestions: [
                'Sản phẩm nào đang bán chạy nhất?',
                'So sánh top 3 sản phẩm bán chạy nhất',
                'Sản phẩm nào sắp hết hàng?',
                'Doanh thu hôm nay là bao nhiêu?',
              ],
              isSuggestionsOnly: true,
            })
          }
          // Generate a session ID for existing history
          currentSessionId.value = generateSessionId()
          chatSessions.value[currentSessionId.value] = parsed
          // Generate name for existing history
          sessionNames.value[currentSessionId.value] = generateSessionName(parsed)
        }
      }

      // If still no session, create new one
      if (!currentSessionId.value) {
        currentSessionId.value = generateSessionId()
        sessionNames.value[currentSessionId.value] = 'Cuộc trò chuyện mới'
      }
    }
  } catch {
    // ignore parse errors
  }

  // Emit current state after loading history
  emit('session-state', {
    sessions: chatSessions.value,
    currentSessionId: currentSessionId.value,
    sessionNames: sessionNames.value,
  })
}

const { t } = useI18n()

// Methods
function scrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTo({
      top: messagesContainer.value.scrollHeight,
      behavior: 'smooth',
    })
  }
}

// Auto-scroll thinking content to bottom when streaming
function setThinkingRef(el: any, msgId: number) {
  if (el) {
    nextTick(() => {
      el.scrollTop = el.scrollHeight
    })
  }
}

// Expanded view removed

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
  if (!isConnected.value) {
    Message.error('AI service chưa kết nối. Vui lòng thử lại sau.')
    return
  }

  // Prevent concurrent requests
  if (isProcessing.value) {
    Message.warning('Đang xử lý câu hỏi trước, vui lòng đợi...')
    return
  }

  isProcessing.value = true

  // Remove suggestions-only messages before sending new message
  messages.value = messages.value.filter(
    msg => !(msg.role === 'assistant' && msg.isSuggestionsOnly === true && !msg.content)
  )

  // Clear followUpSuggestions from ALL previous messages to hide old suggestions
  messages.value.forEach(msg => {
    if (msg.role === 'assistant' && msg.followUpSuggestions) {
      msg.followUpSuggestions = undefined
    }
  })

  // Wait for UI to update after removing suggestions
  await nextTick()

  // Add user message
  const userMessage: ChatMessage = {
    id: Date.now(),
    role: 'user',
    content: text.trim(),
    timestamp: new Date().toLocaleTimeString('vi-VN'),
  }
  messages.value.push(userMessage)

  // Create AI message placeholder immediately with thinking mode
  const aiMessageId = Date.now() + 1
  const aiMessage: ChatMessage = {
    id: aiMessageId,
    role: 'assistant',
    content: '',
    timestamp: new Date().toLocaleTimeString('vi-VN'),
    processingStatus: 'querying',
    isThinking: true,
    thinkingContent: '',
    dataSource: '',
    queryType: '',
  }
  messages.value.push(aiMessage)

  // Clear input first
  input.value = ''

  // Force multiple UI update cycles to ensure loading indicator renders
  await nextTick()
  await nextTick()
  scrollToBottom()

  // Persist after UI updates
  saveHistory()

  // Turn off loading immediately after creating placeholder
  // The placeholder itself shows typing/thinking indicator
  loading.value = false

  try {
    let insideThinkTag = false
    let thinkingTagClosed = false
    let fullContent = ''
    let thinkingContent = ''
    let thinkingContentLive = '' // Live thinking content while streaming
    let contentAfterThinking = ''

    // Use streaming API
    await chatWithAIStream(
      text,
      // onChunk - Append text as it arrives
      (chunk: string, metadata?: any) => {
        // Accumulate full content to detect <think> tags
        fullContent += chunk

        // Check for <think> tag opening
        if (fullContent.includes('<think>') && !insideThinkTag && !thinkingTagClosed) {
          insideThinkTag = true
        }

        // Check for <think> tag closing
        if (fullContent.includes('</think>') && insideThinkTag) {
          insideThinkTag = false
          thinkingTagClosed = true

          // Extract final thinking content
          const thinkMatch = fullContent.match(/<think>([\s\S]*?)<\/think>/)
          if (thinkMatch) {
            thinkingContent = thinkMatch[1].trim()
          }

          // Remove thinking tags from content
          contentAfterThinking = fullContent.replace(/<think>[\s\S]*?<\/think>/g, '').trim()
        } else if (thinkingTagClosed) {
          // Continue accumulating content after thinking tags closed
          contentAfterThinking = fullContent.replace(/<think>[\s\S]*?<\/think>/g, '').trim()
        }

        // Extract live thinking content while inside <think> tags
        if (insideThinkTag) {
          const liveMatch = fullContent.match(/<think>([\s\S]*?)$/)
          if (liveMatch) {
            thinkingContentLive = liveMatch[1]
          }
        }

        const msgIndex = messages.value.findIndex((m) => m.id === aiMessageId)
        if (msgIndex !== -1) {
          // Update metadata if provided (from 'start' event)
          if (metadata) {
            if (metadata.data_source) messages.value[msgIndex].dataSource = metadata.data_source
            if (metadata.follow_up_suggestions) messages.value[msgIndex].followUpSuggestions = metadata.follow_up_suggestions
            if (metadata.intent) messages.value[msgIndex].queryType = metadata.intent
            messages.value[msgIndex].processingStatus = 'analyzing'
          }

          // Show thinking content while inside think tags (live update)
          if (insideThinkTag) {
            messages.value[msgIndex].isThinking = true
            messages.value[msgIndex].thinkingContent = thinkingContentLive // Update live
            messages.value[msgIndex].content = ''
            messages.value[msgIndex].processingStatus = 'analyzing'
          } else {
            // If there is no renderable content yet (e.g., metadata-only or empty chunk), keep thinking indicator
            const hasRenderableContent =
              (contentAfterThinking && contentAfterThinking.length > 0) ||
              (!fullContent.includes('<think>') && fullContent.trim().length > 0)

            if (!hasRenderableContent) {
              messages.value[msgIndex].isThinking = true
              messages.value[msgIndex].processingStatus = 'analyzing'
              messages.value[msgIndex].content = ''
            } else {
              // Show actual content when outside think tags
              messages.value[msgIndex].isThinking = false
              messages.value[msgIndex].thinkingContent = thinkingContent // Final content
              messages.value[msgIndex].content = contentAfterThinking || fullContent
              messages.value[msgIndex].processingStatus = 'ready'
            }
          }
          // Scroll while streaming
          nextTick(() => scrollToBottom())
        }
      },
      // onComplete
      () => {
        loading.value = false
        isProcessing.value = false

        // Force close thinking mode if stream ended while still thinking
        const msgIndex = messages.value.findIndex((m) => m.id === aiMessageId)
        if (msgIndex !== -1) {
          const msg = messages.value[msgIndex]

          // If still in thinking mode when stream ends, force finalize
          if (msg.isThinking || insideThinkTag) {
            msg.isThinking = false

            // Extract whatever thinking content we have
            if (fullContent.includes('<think>')) {
              const thinkMatch = fullContent.match(/<think>([\s\S]*?)(?:<\/think>|$)/)
              if (thinkMatch) {
                msg.thinkingContent = thinkMatch[1].trim()
              }
            }

            // Set content to whatever we have after removing think tags
            msg.content = fullContent.replace(/<think>[\s\S]*?(?:<\/think>|$)/g, '').trim()
            msg.processingStatus = 'ready'
          }
        }

        // Auto-generate session name after first user message
        const userMessages = messages.value.filter((msg) => msg.role === 'user' && msg.id !== 0)
        const currentName = sessionNames.value[currentSessionId.value]
        if (userMessages.length === 1 && (!currentName || currentName === 'Cuộc trò chuyện mới')) {
          const newName = generateSessionName(messages.value)
          sessionNames.value[currentSessionId.value] = newName
        }

        // Persist (before adding suggestions so they don't get saved)
        saveHistory()

        // Auto-generate suggestions card after AI response (after saving)
        const aiMsg = messages.value.find((m) => m.id === aiMessageId)
        if (aiMsg && aiMsg.followUpSuggestions && aiMsg.followUpSuggestions.length > 0) {
          // Move suggestions to a separate message and remove from the AI content bubble
          const suggestions = [...aiMsg.followUpSuggestions]
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

        // Final scroll
        nextTick(() => scrollToBottom())
      },
      // onError
      (error: string) => {
        loading.value = false
        isProcessing.value = false
        Message.error(`Lỗi khi gửi tin nhắn: ${error}`)

        // Update message with error
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

    // Update message with error
    const msgIndex = messages.value.findIndex((m) => m.id === aiMessageId)
    if (msgIndex !== -1) {
      messages.value[msgIndex].content = '❌ Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.'
    }
  }
}

function handleSearch(value?: string) {
  // Use current input if value is undefined (e.g., press-enter)
  const text = typeof value === 'string' ? value : input.value
  sendMessage(text)
}

function askQuestion(question: string) {
  sendMessage(question)
}

function switchToSession(sessionId: string) {
  if (chatSessions.value[sessionId]) {
    // Save current session before switching
    if (currentSessionId.value) {
      chatSessions.value[currentSessionId.value] = [...messages.value]
    }

    // Switch to selected session
    currentSessionId.value = sessionId
    messages.value = [...chatSessions.value[sessionId]]
    normalizeSuggestionMessages(messages.value)

    // Save and scroll
    saveHistory()
    nextTick(() => {
      scrollToBottom()
    })
  }
}

// Note: formatSessionTime removed here (unused in this component)

function createNewChat() {
  // Save current session before creating new one
  if (currentSessionId.value && messages.value.length > 1) {
    chatSessions.value[currentSessionId.value] = [...messages.value]
  }

  // Create new session
  currentSessionId.value = generateSessionId()
  messages.value = [
    {
      id: 0,
      role: 'assistant',
      content:
        'Xin chào! Tôi là trợ lý AI của GearUp. Tôi có thể giúp bạn tra cứu thông tin về sản phẩm, doanh thu, tồn kho và nhiều thứ khác. Bạn cần giúp gì không? 😊',
      timestamp: new Date().toLocaleTimeString('vi-VN'),
    },
    {
      id: -1,
      role: 'assistant',
      content: '',
      timestamp: '',
      followUpSuggestions: [
        'Sản phẩm nào đang bán chạy nhất?',
        'So sánh top 3 sản phẩm bán chạy nhất',
        'Sản phẩm nào sắp hết hàng?',
        'Doanh thu hôm nay là bao nhiêu?',
      ],
      isSuggestionsOnly: true,
    },
  ]
  normalizeSuggestionMessages(messages.value)

  // Set default name for new session
  sessionNames.value[currentSessionId.value] = 'Cuộc trò chuyện mới'

  // Save new session
  chatSessions.value[currentSessionId.value] = [...messages.value]
  saveHistory()

  // Scroll to bottom
  nextTick(() => {
    scrollToBottom()
  })

  Message.success('Đã tạo cuộc trò chuyện mới')
}

function clearMessages() {
  // Clear all sessions
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
        'Sản phẩm nào đang bán chạy nhất?',
        'So sánh top 3 sản phẩm bán chạy nhất',
        'Sản phẩm nào sắp hết hàng?',
        'Doanh thu hôm nay là bao nhiêu?',
      ],
      isSuggestionsOnly: true,
    },
  ]
  normalizeSuggestionMessages(messages.value)

  // Set default name for new session
  sessionNames.value[currentSessionId.value] = 'Cuộc trò chuyện mới'

  // Save cleared state
  chatSessions.value[currentSessionId.value] = [...messages.value]
  saveHistory()

  // Scroll to bottom
  nextTick(() => {
    scrollToBottom()
  })
}

// scrollToBottom is defined earlier to satisfy no-use-before-define

// Lifecycle
let healthTimer: any = null

onMounted(async () => {
  // Restore history first
  loadHistory()
  await nextTick()

  // Scroll to bottom after loading history
  setTimeout(() => {
    scrollToBottom()
  }, 100)

  if (shouldHealthCheck) {
    await checkConnection()
    // Check connection every 30 seconds only when enabled and tab visible
    healthTimer = setInterval(() => {
      if (document.visibilityState === 'visible') {
        checkConnection()
      }
    }, 30000)
  }

  // Observe theme changes from Arco (class on <html>)
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
  ;(window as any).aiChatThemeObserver = observer
})
onUnmounted(() => {
  if (healthTimer) clearInterval(healthTimer)
  const obs = (window as any).aiChatThemeObserver
  if (obs && typeof obs.disconnect === 'function') obs.disconnect()
})

// Auto-persist on change (debounced by microtask via watch)
watch(
  messages,
  () => {
    // Only save if we have a valid session ID
    if (currentSessionId.value) {
      saveHistory()
    }
    
    // Auto-scroll after message updates
    nextTick(() => {
      scrollToBottom()
    })
  },
  { deep: true }
)

function renderMarkdown(md: string) {
  try {
    return markedParse(md || '') as string
  } catch {
    return md
  }
}

// Expose state and methods for parent sidebar controls
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
      /* make card body transparent so container background is visible */
      background: transparent !important;
    }

    /* also ensure the card root is transparent */
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

    :deep(.arco-switch) {
      margin-right: 8px;
    }
  }

  .messages-container {
    flex: 1;
    overflow-y: auto;
    padding: 12px 12px 16px; /* Reduce right padding */
    margin-bottom: 8px;
    min-height: 0; /* Allow flex shrinking */
    /* Use Arco theme variables so it adapts to light/dark automatically */
    background: var(--color-bg-1) !important;
    border-radius: 12px;

    /* Custom scrollbar */
    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-track {
      background: #f5f5f5;
      border-radius: 3px;
    }

    &::-webkit-scrollbar-thumb {
      background: #d9d9d9;
      border-radius: 3px;

      &:hover {
        background: #bfbfbf;
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
          background: #165dff;
          color: #fff;
          margin-right: 8px;
          margin-left: 0;
          box-shadow: 0 4px 10px rgba(22, 93, 255, 0.25);
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
      white-space: normal;

      h1,
      h2,
      h3 {
        margin: 0 0 8px 0;
        font-weight: 600;
      }

      table {
        width: 100%;
        border-collapse: collapse;
        margin: 8px 0 4px 0;
        border: 1px solid var(--color-border-2);
      }

      th,
      td {
        border: 1px solid var(--color-border-2);
        padding: 8px 10px;
        text-align: left;
      }

      th {
        background: var(--color-fill-2);
        font-weight: 600;
      }
      code,
      pre code {
        font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
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
        background: #fafbfc;
        padding: 8px;
        border-radius: 6px;
        border: 1px solid #eef0f2;
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

  .chat-history {
    margin-bottom: 12px;
    padding: 12px;
    background: var(--color-bg-2);
    border-radius: 8px;

    .chat-history-label {
      font-size: 12px;
      font-weight: 600;
      margin-bottom: 8px;
      color: var(--color-text-2);
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
    flex-shrink: 0; /* Prevent input from shrinking */
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
      }
    }
  }
}

/* Component-level dark mode using .is-dark to avoid global scoping issues */
.ai-chatbot.is-dark {
  .input-container {
    background: rgba(18, 20, 26, 0.95);
    border-top-color: #272b36;
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
    box-shadow: 0 6px 16px rgba(22, 93, 255, 0.35);
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
  .chat-history {
    background: #131722;
  }
  .chat-history .chat-history-label {
    color: #9aa4b2;
  }
}

/* Dark theme overrides for Arco (.arco-theme-dark on body/html)
   Use :deep to pierce scoped styles and raise specificity with !important */
:deep(html.arco-theme-dark) .ai-chatbot .messages-container,
:deep(body.arco-theme-dark) .ai-chatbot .messages-container,
:deep(.arco-theme-dark) .ai-chatbot .messages-container {
  background: linear-gradient(180deg, #0f1115 0%, #12141a 100%) !important;
}

:deep(html.arco-theme-dark) .ai-chatbot .messages-container::-webkit-scrollbar-track,
:deep(body.arco-theme-dark) .ai-chatbot .messages-container::-webkit-scrollbar-track,
:deep(.arco-theme-dark) .ai-chatbot .messages-container::-webkit-scrollbar-track {
  background: #161922 !important;
}
:deep(html.arco-theme-dark) .ai-chatbot .messages-container::-webkit-scrollbar-thumb,
:deep(body.arco-theme-dark) .ai-chatbot .messages-container::-webkit-scrollbar-thumb,
:deep(.arco-theme-dark) .ai-chatbot .messages-container::-webkit-scrollbar-thumb {
  background: #2a2f3a !important;
}
:deep(html.arco-theme-dark) .ai-chatbot .messages-container::-webkit-scrollbar-thumb:hover,
:deep(body.arco-theme-dark) .ai-chatbot .messages-container::-webkit-scrollbar-thumb:hover,
:deep(.arco-theme-dark) .ai-chatbot .messages-container::-webkit-scrollbar-thumb:hover {
  background: #3a4150 !important;
}

:deep(html.arco-theme-dark) .ai-chatbot .message.assistant .content,
:deep(body.arco-theme-dark) .ai-chatbot .message.assistant .content,
:deep(.arco-theme-dark) .ai-chatbot .message.assistant .content {
  background: #151823 !important;
  color: #e6e9ef !important;
  border: 1px solid #272b36 !important;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.35) !important;
}

:deep(html.arco-theme-dark) .ai-chatbot .message.user .message-wrapper .content,
:deep(body.arco-theme-dark) .ai-chatbot .message.user .message-wrapper .content,
:deep(.arco-theme-dark) .ai-chatbot .message.user .message-wrapper .content {
  box-shadow: 0 6px 16px rgba(22, 93, 255, 0.35) !important;
}

:deep(html.arco-theme-dark) .ai-chatbot .content .text table,
:deep(body.arco-theme-dark) .ai-chatbot .content .text table,
:deep(.arco-theme-dark) .ai-chatbot .content .text table {
  border-color: #2b3040 !important;
}
:deep(html.arco-theme-dark) .ai-chatbot .content .text th,
:deep(body.arco-theme-dark) .ai-chatbot .content .text th,
:deep(.arco-theme-dark) .ai-chatbot .content .text th,
:deep(html.arco-theme-dark) .ai-chatbot .content .text td,
:deep(body.arco-theme-dark) .ai-chatbot .content .text td,
:deep(.arco-theme-dark) .ai-chatbot .content .text td {
  border-color: #2b3040 !important;
}
:deep(html.arco-theme-dark) .ai-chatbot .content .text th,
:deep(body.arco-theme-dark) .ai-chatbot .content .text th,
:deep(.arco-theme-dark) .ai-chatbot .content .text th {
  background: #1b2030 !important;
  color: #e6e9ef !important;
}
:deep(html.arco-theme-dark) .ai-chatbot .content .text code,
:deep(body.arco-theme-dark) .ai-chatbot .content .text code,
:deep(.arco-theme-dark) .ai-chatbot .content .text code,
:deep(html.arco-theme-dark) .ai-chatbot .content .text pre code,
:deep(body.arco-theme-dark) .ai-chatbot .content .text pre code,
:deep(.arco-theme-dark) .ai-chatbot .content .text pre code {
  background: #0f1420 !important;
  color: #e6e9ef !important;
}

:deep(html.arco-theme-dark) .ai-chatbot .content .sources .sources-content,
:deep(body.arco-theme-dark) .ai-chatbot .content .sources .sources-content,
:deep(.arco-theme-dark) .ai-chatbot .content .sources .sources-content {
  background: #141926 !important;
  border-color: #2b3040 !important;
  color: #cfd6e4 !important;
}

:deep(html.arco-theme-dark) .ai-chatbot .content .timestamp,
:deep(body.arco-theme-dark) .ai-chatbot .content .timestamp,
:deep(.arco-theme-dark) .ai-chatbot .content .timestamp {
  opacity: 0.7 !important;
}

:deep(html.arco-theme-dark) .ai-chatbot .chat-history,
:deep(body.arco-theme-dark) .ai-chatbot .chat-history,
:deep(.arco-theme-dark) .ai-chatbot .chat-history {
  background: #131722 !important;
}
:deep(html.arco-theme-dark) .ai-chatbot .chat-history .chat-history-label,
:deep(body.arco-theme-dark) .ai-chatbot .chat-history .chat-history-label,
:deep(.arco-theme-dark) .ai-chatbot .chat-history .chat-history-label {
  color: #9aa4b2 !important;
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

/* Empty state styles */
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
  color: #1d2129;
  margin-bottom: 8px;
}

.empty-description {
  font-size: 14px;
  color: #86909c;
  line-height: 1.5;
}

/* Dark theme empty state */
.is-dark .empty-title {
  color: #e6e9ef;
}

.is-dark .empty-description {
  color: #9aa4b2;
}

:deep(.arco-theme-dark) .ai-chatbot .empty-title {
  color: #e6e9ef !important;
}

:deep(.arco-theme-dark) .ai-chatbot .empty-description {
  color: #9aa4b2 !important;
}

/* Typing indicator */
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

/* Thinking mode indicator */
.thinking-mode {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  background: linear-gradient(135deg, rgba(22, 93, 255, 0.1), rgba(123, 97, 255, 0.1));
  border-radius: 8px;
  font-weight: 500;
  color: var(--color-text-1);
}

.thinking-mode .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: rgb(var(--primary-6));
  animation: typingDot 1.4s infinite;
  margin-left: 2px;
}

.typing-indicator .dot:nth-child(1) {
  animation-delay: 0s;
}

.typing-indicator .dot:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator .dot:nth-child(3) {
  animation-delay: 0.4s;
}

.thinking-mode .dot:nth-child(1) {
  animation-delay: 0s;
}

.thinking-mode .dot:nth-child(2) {
  animation-delay: 0.2s;
}

.thinking-mode .dot:nth-child(3) {
  animation-delay: 0.4s;
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

/* Thinking Block Styles - Collapsible Clean Design */
.thinking-block {
  margin-bottom: 12px;

  :deep(.arco-collapse) {
    background: transparent;
    border: none;
  }

  :deep(.arco-collapse-item) {
    background: linear-gradient(135deg, rgba(123, 97, 255, 0.06), rgba(22, 93, 255, 0.06));
    border: 1px solid rgba(123, 97, 255, 0.2);
    border-radius: 8px;
  }

  :deep(.arco-collapse-item-header) {
    padding: 10px 14px !important;
    background: transparent;
    display: flex !important;
    align-items: center !important;

    &:hover {
      background: rgba(123, 97, 255, 0.05);
    }
  }

  // Header wrapper - clean flex layout
  .thinking-header-wrapper {
    display: flex !important;
    align-items: center;
    gap: 12px;
    flex: 1;
    min-width: 0;
    margin-left: 20px; // Space from dropdown icon

    .thinking-label {
      font-weight: 600;
      color: var(--color-text-1);
      font-size: 14px;
      flex: 1;
      min-width: 0;
    }

    .thinking-status {
      font-size: 11px;
      color: rgb(var(--primary-6));
      font-weight: 600;
      animation: pulse 1.5s ease-in-out infinite;
      white-space: nowrap;
      flex-shrink: 0;
      padding: 2px 8px;
      background: rgba(var(--primary-6), 0.1);
      border-radius: 12px;
    }
  }

  :deep(.arco-collapse-item-content) {
    padding: 0;
    background: var(--color-bg-2);
    border-top: 1px solid rgba(123, 97, 255, 0.15);
  }

  .thinking-content {
    padding: 12px 14px;
    font-size: 13px;
    line-height: 1.5;
    color: var(--color-text-2);
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'SF Pro Text', system-ui, sans-serif;
    white-space: pre-wrap;
    max-height: 300px;
    overflow-y: auto;

    // Tight spacing for all elements
    p,
    div,
    span,
    ul,
    ol,
    li {
      margin: 0;
      padding: 0;
      line-height: 1.5;
    }

    // Small gap between paragraphs only
    p + p,
    div + div {
      margin-top: 8px;
    }

    // Lists spacing
    ul,
    ol {
      padding-left: 20px;
      margin: 4px 0;
    }

    li {
      margin: 2px 0;
    }

    /* Custom scrollbar */
    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-track {
      background: var(--color-fill-2);
      border-radius: 2px;
    }

    &::-webkit-scrollbar-thumb {
      background: var(--color-fill-4);
      border-radius: 2px;

      &:hover {
        background: var(--color-fill-3);
      }
    }
  }

  // Loading state for thinking block
  .thinking-loading {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;

    .loading-dots {
      display: flex;
      gap: 6px;

      .dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: rgb(var(--primary-6));
        opacity: 0.3;
        animation: dotPulse 1.4s ease-in-out infinite;

        &:nth-child(1) {
          animation-delay: 0s;
        }

        &:nth-child(2) {
          animation-delay: 0.2s;
        }

        &:nth-child(3) {
          animation-delay: 0.4s;
        }
      }
    }
  }

  @keyframes dotPulse {
    0%,
    60%,
    100% {
      opacity: 0.3;
      transform: scale(1);
    }
    30% {
      opacity: 1;
      transform: scale(1.2);
    }
  }

  /* Reusable thinking dots */
  .thinking-dots {
    display: inline-flex;
    gap: 4px;
    margin-left: 6px;
    vertical-align: middle;

    .dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background-color: rgb(var(--primary-6));
      animation: typingDot 1.4s infinite;
    }

    .dot:nth-child(1) { animation-delay: 0s; }
    .dot:nth-child(2) { animation-delay: 0.2s; }
    .dot:nth-child(3) { animation-delay: 0.4s; }
  }

  .streaming-cursor {
    display: inline-block;
    font-family: monospace;
    color: rgb(var(--primary-6));
    font-size: 14px;
    font-weight: bold;
    animation: blink 0.8s step-end infinite;
    margin-left: 2px;
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

.ai-chatbot.is-dark .thinking-block {
  :deep(.arco-collapse-item) {
    background: linear-gradient(135deg, rgba(123, 97, 255, 0.1), rgba(22, 93, 255, 0.1));
    border-color: rgba(123, 97, 255, 0.3);
  }

  :deep(.arco-collapse-item-header:hover) {
    background: rgba(123, 97, 255, 0.08);
  }

  :deep(.arco-collapse-item-content) {
    background: #1a1f2e;
    border-top-color: rgba(123, 97, 255, 0.2);
  }

  .thinking-content {
    color: #9aa4b2;
  }
}

/* Processing Status Styles */
.processing-status {
  padding: 12px 16px;
  background: linear-gradient(135deg, rgba(22, 93, 255, 0.05), rgba(123, 97, 255, 0.05));
  border-radius: 8px;
  border: 1px solid rgba(22, 93, 255, 0.15);

  .status-text {
    margin-top: 8px;
    font-size: 13px;
    font-weight: 500;
    color: var(--color-text-2);
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.ai-chatbot.is-dark .processing-status {
  background: linear-gradient(135deg, rgba(22, 93, 255, 0.1), rgba(123, 97, 255, 0.1));
  border-color: rgba(22, 93, 255, 0.25);
}

/* Message Metadata Styles */
.message-metadata {
  margin-top: 12px;
  padding-top: 8px;
  border-top: 1px solid var(--color-border-2);
}

.data-source {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: var(--color-text-3);
  margin-bottom: 8px;
  padding: 4px 8px;
  background: var(--color-fill-1);
  border-radius: 6px;

  .metadata-icon {
    font-size: 12px;
  }

  .metadata-label {
    font-weight: 600;
  }

  .metadata-value {
    font-style: italic;
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
    border: 1px solid var(--color-border-2);
    background: var(--color-bg-1);
    border-radius: 12px;
    transition: all 0.2s ease;

    &:hover {
      background: rgb(var(--primary-1));
      border-color: rgb(var(--primary-6));
      color: rgb(var(--primary-6));
      transform: translateY(-1px);
      box-shadow: 0 2px 6px rgba(var(--primary-6), 0.15);
    }
  }
}

/* Dark mode metadata styles */
.ai-chatbot.is-dark {
  .data-source {
    background: #1a1f2e;
    border: 1px solid #272b36;
  }

  .suggestion-btn {
    background: #151823;
    border-color: #272b36;
    color: #9aa4b2;

    &:hover {
      background: rgba(22, 93, 255, 0.15);
      border-color: rgb(var(--primary-5));
      color: rgb(var(--primary-5));
    }
  }
}

:deep(.arco-theme-dark) .ai-chatbot .data-source {
  background: #1a1f2e !important;
  border: 1px solid #272b36 !important;
  color: #9aa4b2 !important;
}

:deep(.arco-theme-dark) .ai-chatbot .suggestion-btn {
  background: #151823 !important;
  border-color: #272b36 !important;
  color: #9aa4b2 !important;
}

:deep(.arco-theme-dark) .ai-chatbot .suggestion-btn:hover {
  background: rgba(22, 93, 255, 0.15) !important;
  border-color: rgb(var(--primary-5)) !important;
  color: rgb(var(--primary-5)) !important;
}

/* Expanded Modal Styles */
</style>
