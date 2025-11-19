<template>
  <div class="chat-window">
    <template v-if="activeConversation">
      <!-- Header -->
      <div class="chat-header">
        <div class="header-user-info">
          <a-avatar :size="40">
            <icon-user />
          </a-avatar>
          <div class="user-details">
            <h4>
              {{ otherUserName }}
              <span v-if="isCustomerConversation" class="customer-badge">(KH)</span>
            </h4>
            <span v-if="isOtherUserOnline" class="status-online">
              <icon-check-circle-fill />
              {{ $t('chat.status.active') }}
            </span>
            <span v-else class="status-offline">{{ $t('chat.status.inactive') }}</span>
          </div>
        </div>
        <div class="header-actions">
          <!-- AI history is now auto-loaded, button removed for cleaner UI -->
        </div>
      </div>

      <!-- AI Chat History Section - Auto-loaded for customer conversations -->
      <div v-if="isCustomerConversation && aiHistory.length > 0" class="ai-history-section">
        <div class="ai-history-header">
          <span class="ai-history-title">
            <icon-robot :size="16" style="margin-right: 6px; vertical-align: middle;" />
            Lịch sử AI Chat trước khi nhân viên tham gia
          </span>
          <a-button type="text" size="small" @click="loadAiHistory">
            <template #icon>
              <icon-refresh />
            </template>
            Tải lại
          </a-button>
        </div>
        <div class="ai-history-content">
          <div class="ai-history-messages">
            <div
              v-for="(msg, index) in aiHistory"
              :key="index"
              :class="['ai-history-message', msg.role]"
            >
              <div class="ai-message-role">{{ msg.role === 'user' ? 'Khách hàng' : 'AI' }}</div>
              <div class="ai-message-content" v-html="formatAiMessage(msg.content)"></div>
              <div class="ai-message-time">{{ formatTime(msg.timestamp) }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Messages area -->
      <div ref="messagesContainer" class="messages-container">
        <a-spin :loading="chatStore.loadingMessages" class="messages-spin">
          <div class="messages-list">
            <MessageItem v-for="message in messages" :key="message.maTinNhan || message.id" :message="message" :show-sender-name="false" />
          </div>
        </a-spin>
      </div>

      <!-- Input area -->
      <div class="chat-input-area">
        <a-textarea
          v-model="messageInput"
          :placeholder="$t('chat.input.placeholder', { name: otherUserName })"
          :auto-size="{ minRows: 1, maxRows: 5 }"
          class="message-input"
          @keydown.enter.exact="handleSendMessage"
          @keydown.shift.enter="handleNewLine"
        />
        <a-button
          type="primary"
          shape="circle"
          :disabled="!messageInput.trim() || chatStore.sendingMessage"
          :loading="chatStore.sendingMessage"
          @click="handleSendMessage"
        >
          <template #icon>
            <icon-send />
          </template>
        </a-button>
      </div>
    </template>

    <!-- Empty state khi chưa chọn conversation -->
    <a-empty v-else class="chat-empty" description="Chọn một cuộc trò chuyện để bắt đầu nhắn tin">
      <template #image>
        <icon-message :size="80" />
      </template>
    </a-empty>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import { IconUser, IconCheckCircleFill, IconSend, IconMessage, IconRobot, IconRefresh } from '@arco-design/web-vue/es/icon'
import { Message } from '@arco-design/web-vue'
import useChatStore from '@/store/modules/chat'
import useUserStore from '@/store/modules/user'
import MessageItem from './MessageItem.vue'
import { getCustomerAiChatHistory } from '@/api/chat'
import dayjs from 'dayjs'

const chatStore = useChatStore()
const userStore = useUserStore()

const messageInput = ref('')
const messagesContainer = ref<HTMLElement | null>(null)
const loadingAiHistory = ref(false)
const aiHistory = ref<any[]>([])

/**
 * Cuộc trò chuyện đang active
 */
const activeConversation = computed(() => chatStore.activeConversation)

/**
 * Danh sách tin nhắn
 */
const messages = computed(() => chatStore.activeMessages)

/**
 * Tên người dùng kia
 */
const otherUserName = computed(() => {
  if (!activeConversation.value) return ''
  const conv = activeConversation.value
  const currentUserId = userStore.id
  
  // Handle customer-staff conversations
  if (conv.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
    if (currentUserId === conv.nhanVienId) {
      return conv.khachHangName || 'Khách hàng'
    } else if (currentUserId === conv.khachHangId) {
      return conv.nhanVienName || 'Nhân viên'
    }
  }
  
  // Handle staff-staff conversations
  if (currentUserId === conv.nhanVien1Id) {
    return conv.nhanVien2Name || ''
  }
  return conv.nhanVien1Name || ''
})

/**
 * ID người dùng kia
 */
const otherUserId = computed(() => {
  if (!activeConversation.value) return null
  const conv = activeConversation.value
  const currentUserId = userStore.id
  
  // Handle customer-staff conversations
  if (conv.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
    if (currentUserId === conv.khachHangId) {
      return conv.nhanVienId || null
    } else if (currentUserId === conv.nhanVienId) {
      return conv.khachHangId || null
    }
  }
  
  // Handle staff-staff conversations
  if (currentUserId === conv.nhanVien1Id) {
    return conv.nhanVien2Id || null
  }
  return conv.nhanVien1Id || null
})

/**
 * Check người dùng kia có online không
 */
const isOtherUserOnline = computed(() => {
  if (!otherUserId.value) return false
  return chatStore.onlineUsers.has(otherUserId.value)
})

/**
 * Check if current conversation is with a customer
 */
const isCustomerConversation = computed(() => {
  if (!activeConversation.value) return false
  return activeConversation.value.loaiCuocTraoDoi === 'CUSTOMER_STAFF' && 
         activeConversation.value.khachHangId !== null
})

/**
 * Scroll to bottom của messages container
 */
function scrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTo({
      top: messagesContainer.value.scrollHeight,
      behavior: 'smooth',
    })
  }
}

/**
 * Watch conversation thay đổi để load tin nhắn
 */
watch(
  () => chatStore.activeConversationId,
  async (newId) => {
    if (newId && otherUserId.value) {
      // Load messages
      await chatStore.fetchMessages(otherUserId.value)

      // Load AI chat history if this is a customer conversation
      if (isCustomerConversation.value && activeConversation.value?.khachHangId) {
        await loadAiHistory()
      } else {
        // Clear AI history if not a customer conversation
        aiHistory.value = []
      }

      // Scroll to bottom
      await nextTick()
      scrollToBottom()

      // Mark as read
      if (chatStore.activeConversationUserInitiated) {
        await chatStore.markAsRead(otherUserId.value)
      }
    } else {
      // Clear AI history when no conversation is active
      aiHistory.value = []
    }
  },
  { immediate: true }
)

/**
 * Watch messages thay đổi để auto-scroll
 * Sử dụng deep watch để catch cả thay đổi nội dung message (không chỉ length)
 */
watch(
  messages,
  async () => {
    await nextTick()
    scrollToBottom()
  },
  { deep: true }
)

/**
 * Auto-scroll on mount nếu đã có messages
 */
onMounted(async () => {
  if (activeConversation.value && messages.value.length > 0) {
    await nextTick()
    // Use setTimeout to ensure DOM is fully rendered
    setTimeout(() => {
      scrollToBottom()
    }, 100)
  }
})

/**
 * Xử lý gửi tin nhắn
 */
async function handleSendMessage(event: KeyboardEvent | MouseEvent) {
  // Nếu là keyboard event từ Enter, prevent default
  if (event instanceof KeyboardEvent) {
    event.preventDefault()
  }

  const content = messageInput.value.trim()
  if (!content || !otherUserId.value) return

  try {
    // Gửi tin nhắn qua WebSocket (có fallback to API)
    await chatStore.sendMessageViaWebSocket({
      receiverId: otherUserId.value,
      content,
      messageType: 'TEXT',
    })

    // Clear input
    messageInput.value = ''

    // Scroll to bottom
    await nextTick()
    scrollToBottom()
  } catch (error: any) {
    Message.error(`Không thể gửi tin nhắn: ${error.message}`)
  }
}

/**
 * Xử lý xuống dòng (Shift + Enter)
 */
function handleNewLine(_event: KeyboardEvent) {
  // Allow default behavior (newline)
}

/**
 * Load AI chat history for the customer
 */
async function loadAiHistory() {
  if (!isCustomerConversation.value || !activeConversation.value?.khachHangId) {
    aiHistory.value = []
    return
  }
  
  loadingAiHistory.value = true
  try {
    const customerId = activeConversation.value.khachHangId
    console.log(' Loading AI chat history for customer:', customerId)
    const response = await getCustomerAiChatHistory(customerId)
    aiHistory.value = response.data?.data || []
    console.log(' Loaded AI chat history:', aiHistory.value.length, 'messages')
    if (aiHistory.value.length > 0) {
      console.log(' Session ID:', aiHistory.value[0]?.sessionId)
    }
  } catch (error: any) {
    // Silently fail - don't show error if AI history doesn't exist
    console.error(' Failed to load AI chat history:', error)
    aiHistory.value = []
  } finally {
    loadingAiHistory.value = false
  }
}

/**
 * Format AI message content (support markdown)
 */
function formatAiMessage(content: string): string {
  if (!content) return ''
  // Simple markdown to HTML conversion
  let formatted = content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/\n/g, '<br>')
  return formatted
}

/**
 * Format timestamp
 */
function formatTime(timestamp: string | Date): string {
  if (!timestamp) return ''
  return dayjs(timestamp).format('DD/MM/YYYY HH:mm')
}
</script>

<style scoped lang="less">
.chat-window {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--color-bg-1);
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border-2);
  background: var(--color-bg-2);

  .header-user-info {
    display: flex;
    align-items: center;
    gap: 12px;

    .user-details {
      display: flex;
      flex-direction: column;
      gap: 4px;

      h4 {
        margin: 0;
        font-size: 16px;
        font-weight: 500;
        color: var(--color-text-1);
        display: flex;
        align-items: center;
        gap: 4px;

        .customer-badge {
          font-size: 10px;
          color: #52c41a;
          font-weight: 400;
        }
      }

      .status-online {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: rgb(var(--success-6));
        font-weight: 500;
      }

      .status-offline {
        font-size: 12px;
        color: var(--color-text-3);
        font-weight: 400;
      }
    }
  }
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 16px;
  position: relative;
  /* Hide scrollbars */
  -ms-overflow-style: none; /* IE and Edge */
  scrollbar-width: none; /* Firefox */

  &::-webkit-scrollbar {
    width: 0;
    height: 0;
  }
}

.messages-spin {
  min-height: 200px;
  width: 100%;

  :deep(.arco-spin) {
    width: 100%;
  }
}

.messages-list {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.chat-input-area {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-top: 1px solid var(--color-border-2);
  background: var(--color-bg-2);

  .message-input {
    flex: 1;
    resize: none;
    border-radius: 20px;
    padding: 8px 16px;
  }

  :deep(.arco-btn-circle) {
    width: 40px;
    height: 40px;
    flex-shrink: 0;
    align-self: flex-end;
    margin-bottom: 2px;
  }
}

.chat-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--color-text-3);

  :deep(.arco-empty-image) {
    margin-bottom: 16px;
  }
}

.ai-history-section {
  border-bottom: 1px solid var(--color-border-2);
  background: linear-gradient(to bottom, rgba(var(--primary-6), 0.05) 0%, var(--color-bg-2) 100%);
  max-height: 400px;
  display: flex;
  flex-direction: column;
  border-top: 2px solid rgb(var(--primary-6));
}

.ai-history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  border-bottom: 1px solid var(--color-border-2);
  background: rgba(var(--primary-6), 0.05);

  .ai-history-title {
    font-weight: 600;
    font-size: 13px;
    color: rgb(var(--primary-6));
    display: flex;
    align-items: center;
  }
}

.ai-history-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
  max-height: 350px;
}

.ai-history-empty {
  padding: 20px;
  text-align: center;
}

.ai-history-messages {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ai-history-message {
  padding: 12px;
  border-radius: 8px;
  background: var(--color-bg-1);
  border: 1px solid var(--color-border-2);

  &.user {
    border-left: 3px solid rgb(var(--primary-6));
  }

  &.assistant {
    border-left: 3px solid rgb(var(--success-6));
  }

  .ai-message-role {
    font-size: 12px;
    font-weight: 500;
    color: var(--color-text-2);
    margin-bottom: 6px;
  }

  .ai-message-content {
    font-size: 14px;
    color: var(--color-text-1);
    line-height: 1.6;
    margin-bottom: 6px;

    :deep(strong) {
      font-weight: 600;
    }

    :deep(em) {
      font-style: italic;
    }
  }

  .ai-message-time {
    font-size: 11px;
    color: var(--color-text-3);
  }
}
</style>
