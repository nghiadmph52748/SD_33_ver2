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
            <h4>{{ otherUserName }}</h4>
            <span v-if="isOtherUserOnline" class="status-online">
              <icon-check-circle-fill />
              {{ $t('chat.status.active') }}
            </span>
            <span v-else class="status-offline">{{ $t('chat.status.inactive') }}</span>
          </div>
        </div>
        <div class="header-actions">
          <!-- Có thể thêm actions như call, video call, info -->
        </div>
      </div>

      <!-- Messages area -->
      <div ref="messagesContainer" class="messages-container">
        <a-spin :loading="chatStore.loadingMessages" class="messages-spin">
          <div class="messages-list">
            <MessageItem v-for="message in messages" :key="message.id" :message="message" :show-sender-name="false" />
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
import { IconUser, IconCheckCircleFill, IconSend, IconMessage } from '@arco-design/web-vue/es/icon'
import { Message } from '@arco-design/web-vue'
import useChatStore from '@/store/modules/chat'
import useUserStore from '@/store/modules/user'
import MessageItem from './MessageItem.vue'

const chatStore = useChatStore()
const userStore = useUserStore()

const messageInput = ref('')
const messagesContainer = ref<HTMLElement | null>(null)

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
  const currentUserId = userStore.id
  if (currentUserId === activeConversation.value.nhanVien1Id) {
    return activeConversation.value.nhanVien2Name
  }
  return activeConversation.value.nhanVien1Name
})

/**
 * ID người dùng kia
 */
const otherUserId = computed(() => {
  if (!activeConversation.value) return null
  const currentUserId = userStore.id
  if (currentUserId === activeConversation.value.nhanVien1Id) {
    return activeConversation.value.nhanVien2Id
  }
  return activeConversation.value.nhanVien1Id
})

/**
 * Check người dùng kia có online không
 */
const isOtherUserOnline = computed(() => {
  if (!otherUserId.value) return false
  return chatStore.onlineUsers.has(otherUserId.value)
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

      // Scroll to bottom
      await nextTick()
      scrollToBottom()

      // Mark as read
      await chatStore.markAsRead(otherUserId.value)
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

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--color-border-3);
    border-radius: 3px;
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
</style>
