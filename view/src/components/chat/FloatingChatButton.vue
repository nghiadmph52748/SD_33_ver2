<template>
  <div class="floating-chat">
    <!-- Chat drawer -->
    <a-drawer
      v-model:visible="drawerVisible"
      :width="400"
      :footer="false"
      placement="right"
      :mask-closable="true"
      unmount-on-close
      class="chat-drawer"
    >
      <template #title>
        <div class="drawer-title">
          <div class="title-actions">
            <a-button-group>
              <a-button size="small" :type="headerMode === 'human' ? 'primary' : 'secondary'" @click="selectHeaderMode('human')">
                Chat với nhân viên
              </a-button>
              <a-button size="small" :type="headerMode === 'ai' ? 'primary' : 'secondary'" @click="selectHeaderMode('ai')">
                Chat AI
              </a-button>
            </a-button-group>
          </div>
        </div>
      </template>

      <div class="drawer-content">
        <!-- AI Chat -->
        <div v-if="headerMode === 'ai'" class="ai-chat-container">
          <a-card class="chatbot-card" :bordered="false">
            <AIChatbot
              ref="chatbotRef"
              :suppress-connection-notice="true"
              :enable-health-check="false"
              :connection-status="aiConnected"
              @session-state="handleSessionState"
            />
          </a-card>
        </div>

        <!-- Conversation list -->
        <div v-else-if="headerMode === 'human' && !activeConversation" class="conversation-list-mini">
          <a-input-search v-model="searchKeyword" placeholder="Tìm kiếm..." class="search-box" />

          <a-spin :loading="chatStore.loadingConversations" class="conversations-spin">
            <div class="conversations">
              <div v-for="conv in filteredConversations" :key="conv.id" class="conversation-item" @click="selectConversation(conv.id)">
                <a-avatar :size="40">
                  <icon-user />
                </a-avatar>
                <div class="conv-details">
                  <div class="conv-header">
                    <span class="conv-name">{{ getOtherUserName(conv) }}</span>
                    <span class="conv-time">{{ formatTime(conv.lastMessageTime) }}</span>
                  </div>
                  <div class="conv-preview">
                    <span class="last-message">{{ conv.lastMessageContent }}</span>
                    <a-badge v-if="getUnreadCount(conv) > 0" :count="getUnreadCount(conv)" class="unread-badge" />
                  </div>
                </div>
              </div>

              <a-empty v-if="filteredConversations.length === 0" description="Chưa có cuộc trò chuyện nào" />
            </div>
          </a-spin>
        </div>

        <!-- Chat window mini -->
        <div v-else-if="headerMode === 'human' && activeConversation" class="chat-window-mini">
          <div class="mini-header">
            <a-button type="text" size="small" @click="backToList">
              <template #icon>
                <icon-left />
              </template>
            </a-button>
            <div class="mini-user-info">
              <a-avatar :size="32">
                <icon-user />
              </a-avatar>
              <span>{{ activeConversationName }}</span>
            </div>
          </div>

          <div ref="messagesContainer" class="mini-messages" @scroll="handleScroll">
            <div
              v-for="msg in activeMessages"
              :key="msg.id"
              :class="['message-bubble', msg.senderId === userStore.id ? 'sent' : 'received']"
            >
              <div class="bubble-content">{{ msg.content }}</div>
              <div class="bubble-time">
                {{ formatMessageTime(msg.sentAt) }}
                <!-- Seen indicator for sent messages -->
                <icon-check-circle-fill v-if="msg.senderId === userStore.id && msg.isRead" class="seen-icon" />
                <icon-check-circle v-else-if="msg.senderId === userStore.id" class="sent-icon" />
              </div>
            </div>
          </div>

          <div class="mini-input">
            <a-textarea
              v-model="messageInput"
              :auto-size="{ minRows: 1, maxRows: 3 }"
              placeholder="Nhập tin nhắn..."
              @keydown.enter.exact="sendMessage"
            />
            <a-button type="primary" :loading="chatStore.sendingMessage" @click="sendMessage">
              <template #icon>
                <icon-send />
              </template>
            </a-button>
          </div>
        </div>
      </div>
    </a-drawer>

    <!-- Floating chat button (moves up when scroll-to-top appears) -->
    <a-button type="primary" shape="circle" size="large" :class="['floating-btn', { 'moved-up': isScrolledDown }]" @click="toggleDrawer">
      <template #icon>
        <span class="dual-icon">
          <icon-message :size="22" />
          <icon-robot :size="14" class="ai-mini" />
        </span>
      </template>
      <!-- Connection status indicator -->
      <div
        v-if="!chatStore.wsConnected"
        class="connection-indicator"
        :class="{ connecting: chatStore.wsConnecting }"
        :title="chatStore.wsConnecting ? 'Đang kết nối...' : 'Mất kết nối'"
      />
      <a-badge v-if="chatStore.totalUnreadCount > 0" :count="chatStore.totalUnreadCount" :offset="[-8, 8]" />
    </a-button>
    <!-- Single floating button used for both Chat and AI -->
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { IconMessage, IconUser, IconSend, IconLeft, IconCheckCircle, IconCheckCircleFill, IconRobot } from '@arco-design/web-vue/es/icon'
import { Message } from '@arco-design/web-vue'
import dayjs from 'dayjs'
import useChatStore from '@/store/modules/chat'
import useUserStore from '@/store/modules/user'
import AIChatbot from '@/components/ai/AIChatbot.vue'
import { checkAIHealth } from '@/api/ai'

const chatStore = useChatStore()
const userStore = useUserStore()

const drawerVisible = ref(false)
const searchKeyword = ref('')
const messageInput = ref('')
const messagesContainer = ref<HTMLElement | null>(null)
const isScrolledDown = ref(false)

// AI assistant state
const aiConnected = ref(false)
const chatbotRef = ref<InstanceType<typeof AIChatbot> | null>(null)

// Header mode toggle
const headerMode = ref<'human' | 'ai'>('human')

const activeConversation = computed(() => chatStore.activeConversation)
const activeMessages = computed(() => chatStore.activeMessages)

function getOtherUserName(conv: any) {
  return userStore.id === conv.nhanVien1Id ? conv.nhanVien2Name : conv.nhanVien1Name
}

function getUnreadCount(conv: any) {
  return userStore.id === conv.nhanVien1Id ? conv.unreadCountNv1 : conv.unreadCountNv2
}

function scrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const activeConversationName = computed(() => {
  if (!activeConversation.value) return ''
  return getOtherUserName(activeConversation.value)
})

const filteredConversations = computed(() => {
  // Force reactivity by accessing the conversations array
  const conversations = chatStore.conversations
  if (!searchKeyword.value) return conversations
  return conversations.filter((conv) => {
    const name = getOtherUserName(conv).toLowerCase()
    return name.includes(searchKeyword.value.toLowerCase())
  })
})

function toggleDrawer() {
  drawerVisible.value = !drawerVisible.value

  if (drawerVisible.value && chatStore.conversations.length === 0) {
    chatStore.fetchConversations()
    chatStore.fetchUnreadCount()
  }
}

function toggleAIModal() {
  headerMode.value = 'ai'
  drawerVisible.value = true
  if (!aiConnected.value) {
    checkAIConnection()
  }
}

async function checkAIConnection() {
  try {
    await checkAIHealth()
    aiConnected.value = true
  } catch (error) {
    aiConnected.value = false
  }
}

async function selectConversation(id: number) {
  chatStore.setActiveConversation(id)
  const conv = chatStore.conversations.find((c) => c.id === id)
  if (conv) {
    const otherUserId = userStore.id === conv.nhanVien1Id ? conv.nhanVien2Id : conv.nhanVien1Id
    await chatStore.fetchMessages(otherUserId)

    // Scroll to bottom to show latest messages
    await nextTick()
    scrollToBottom()

    // Auto mark as read if has unread messages
    const unreadCount = getUnreadCount(conv)
    console.log('Selecting conversation:', id, 'unread count:', unreadCount)
    if (unreadCount > 0) {
      // Pass conversation ID to ensure correct conversation is updated
      await chatStore.markAsRead(otherUserId, id)
      console.log('Marked conversation as read')
    }
  }
}

function backToList() {
  chatStore.setActiveConversation(null)
}

function formatTime(time: string | null | undefined) {
  if (!time) return ''
  const now = dayjs()
  const msgTime = dayjs(time)
  if (now.diff(msgTime, 'day') === 0) return msgTime.format('HH:mm')
  if (now.diff(msgTime, 'day') === 1) return 'Hôm qua'
  return msgTime.format('DD/MM')
}

function formatMessageTime(time: string | null | undefined) {
  if (!time) return ''
  return dayjs(time).format('HH:mm')
}

async function sendMessage() {
  if (!messageInput.value.trim() || !activeConversation.value) return

  const conv = activeConversation.value
  const otherUserId = userStore.id === conv.nhanVien1Id ? conv.nhanVien2Id : conv.nhanVien1Id

  try {
    await chatStore.sendMessageViaWebSocket({
      receiverId: otherUserId,
      content: messageInput.value.trim(),
      messageType: 'TEXT',
    })
    messageInput.value = ''
    await nextTick()
    scrollToBottom()
  } catch (error: any) {
    Message.error(`Không thể gửi: ${error.message}`)
  }
}

function handleScroll() {
  // Reserved for future features
}

function handleSessionState(state: any) {
  // Forward-looking hook for session state if needed
  // eslint-disable-next-line no-console
  console.log('Session state updated:', state)
}

function selectHeaderMode(mode: 'human' | 'ai') {
  headerMode.value = mode
  if (mode === 'ai' && !aiConnected.value) {
    checkAIConnection()
  }
}

// Auto-scroll when messages change
watch(
  () => activeMessages.value.length,
  async () => {
    await nextTick()
    scrollToBottom()
  }
)

// Watch for new messages in the active conversation and auto-mark as read
watch(
  () => activeMessages.value,
  async (messages, oldMessages) => {
    // Only process if drawer is open and we have an active conversation
    if (!drawerVisible.value || !activeConversation.value) return

    // Check if there's a new unread message
    if (Array.isArray(messages) && messages.length > 0) {
      const otherUserId =
        userStore.id === activeConversation.value.nhanVien1Id ? activeConversation.value.nhanVien2Id : activeConversation.value.nhanVien1Id

      // Find new unread messages from the other user
      const newUnreadMessages = messages.filter((msg: any) => msg.senderId === otherUserId && !msg.isRead)

      if (newUnreadMessages.length > 0) {
        console.log(`📖 Auto-marking ${newUnreadMessages.length} new message(s) as read`)
        try {
          await chatStore.markAsRead(otherUserId, activeConversation.value.id)
        } catch (error) {
          console.error('Error auto-marking new messages as read:', error)
        }
      }
    }
  },
  { deep: true, immediate: false }
)

// Auto-scroll when conversation changes (for cached messages)
watch(activeConversation, async (newConv) => {
  if (newConv) {
    await nextTick()
    scrollToBottom()
  }
})

// Connect WebSocket when component mounts to receive real-time messages
onMounted(async () => {
  // Fetch initial data
  await chatStore.fetchConversations()
  await chatStore.fetchUnreadCount()

  // Connect WebSocket for real-time updates
  if (!chatStore.wsConnected && !chatStore.wsConnecting) {
    await chatStore.connectWebSocket()
  }

  // Pre-check AI connection
  await checkAIConnection()
})

// Cleanup on unmount
onUnmounted(() => {
  // Don't disconnect WebSocket here - keep it alive for real-time updates
  // It will be disconnected when user logs out
})

// Watch header mode changes
watch(headerMode, async (mode) => {
  if (mode === 'ai' && !aiConnected.value) {
    await checkAIConnection()
  }
})

// Connect WebSocket and scroll when drawer opens
watch(drawerVisible, async (visible) => {
  if (visible) {
    // If AI mode, check AI connection
    if (headerMode.value === 'ai' && !aiConnected.value) {
      await checkAIConnection()
    }

    // Ensure WebSocket is connected for human chat
    if (headerMode.value === 'human') {
      if (!chatStore.wsConnected && !chatStore.wsConnecting) {
        await chatStore.connectWebSocket()
      }

      // Refresh conversations when drawer opens
      if (chatStore.conversations.length === 0) {
        await chatStore.fetchConversations()
      }
      await chatStore.fetchUnreadCount()

      // Scroll to bottom if already in a conversation
      if (activeConversation.value) {
        await nextTick()
        scrollToBottom()
      }
    }
  }
})

// Watch for real-time unread count updates
watch(
  () => chatStore.totalUnreadCount,
  (newCount, oldCount) => {
    // Badge will automatically update via reactivity
    if (newCount > oldCount && newCount > 0) {
      // Optional: Show notification when new message arrives
      console.log(`📬 New message! Unread count: ${newCount}`)
    }
  }
)

// Auto-mark messages as read when drawer is open and user is viewing the conversation
watch(
  () => [drawerVisible.value, activeConversation.value, activeMessages.value],
  async (values) => {
    const [isDrawerOpen, currentConv, messages] = values
    // Only auto-mark if:
    // 1. Drawer is open
    // 2. User is viewing a conversation (type check)
    // 3. There are messages in the conversation
    if (
      isDrawerOpen &&
      currentConv &&
      currentConv !== null &&
      typeof currentConv === 'object' &&
      'nhanVien1Id' in currentConv &&
      Array.isArray(messages) &&
      messages.length > 0
    ) {
      const otherUserId = userStore.id === currentConv.nhanVien1Id ? currentConv.nhanVien2Id : currentConv.nhanVien1Id

      // Check if there are unread messages from the other user
      const hasUnreadMessages = messages.some((msg: any) => msg.senderId === otherUserId && !msg.isRead)

      if (hasUnreadMessages) {
        console.log('📖 Auto-marking messages as read (drawer open, viewing conversation)')
        try {
          await chatStore.markAsRead(otherUserId, currentConv.id)
        } catch (error) {
          console.error('Error auto-marking messages as read:', error)
        }
      }
    }
  },
  { deep: true, immediate: false }
)

// Track scroll to move chat button when scroll-to-top appears
const checkScroll = () => {
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  isScrolledDown.value = scrollTop > 300
}

if (typeof window !== 'undefined') {
  window.addEventListener('scroll', checkScroll)
}
</script>

<style scoped lang="less">
.floating-chat {
  position: relative;
}

.floating-btn {
  position: fixed;
  bottom: 24px;
  right: 24px;
  width: 56px;
  height: 56px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  z-index: 999;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 0 !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  line-height: 1 !important;

  &.moved-up {
    right: 96px; // Equal 16px gap from scroll-to-top button
  }

  &:hover {
    transform: scale(1.1);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
  }

  :deep(.arco-btn-icon) {
    margin: 0 !important;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  :deep(.arco-icon) {
    margin: 0 !important;
    display: block;
  }

  :deep(.arco-badge) {
    position: absolute;
    top: -4px;
    right: -4px;
  }

  .connection-indicator {
    position: absolute;
    top: 4px;
    left: 4px;
    width: 8px;
    height: 8px;
    background-color: #f5222d;
    border-radius: 50%;
    border: 1px solid #fff;
    z-index: 1;

    &.connecting {
      background-color: #faad14;
      animation: pulse 1.5s ease-in-out infinite;
    }
  }
}

.dual-icon {
  position: relative;
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;

  :deep(.arco-icon) {
    display: block;
  }

  .ai-mini {
    position: absolute;
    right: -2px;
    bottom: -2px;
    background: var(--color-bg-2);
    border-radius: 50%;
    padding: 1px;
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

.drawer-title {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
}

.title-actions {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Center header content and keep close button pinned right */
.chat-drawer :deep(.arco-drawer-header) {
  position: relative;
  display: block;
  text-align: center;
}

.chat-drawer :deep(.arco-drawer-header-title) {
  display: inline-block;
}

.chat-drawer :deep(.arco-drawer-close-btn) {
  position: absolute;
  right: 16px;
}

.drawer-content {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.ai-chat-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .chatbot-card {
    height: 100%;
    display: flex;
    flex-direction: column;
    box-shadow: none;
    border: none;

    :deep(.arco-card-body) {
      flex: 1;
      display: flex !important;
      flex-direction: column !important;
      padding: 0 !important;
      overflow: hidden !important;
      min-height: 0 !important;
    }
  }

  :deep(.ai-chatbot) {
    height: 100%;
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;

    .chatbot-card {
      flex: 1;
      display: flex;
      flex-direction: column;
      box-shadow: none;
      border: none;
      min-height: 0;

      :deep(.arco-card-header) {
        flex-shrink: 0;
      }

      :deep(.arco-card-body) {
        flex: 1;
        display: flex !important;
        flex-direction: column !important;
        padding: 0 !important;
        overflow: hidden !important;
        min-height: 0 !important;
      }
    }

    .messages-container {
      overflow-y: auto;
      padding: 12px;
      flex: 1 1 auto !important;
      min-height: 0 !important;
    }

    .input-container {
      padding: 12px;
      border-top: 1px solid var(--color-border-2);
      background: var(--color-bg-2);
      flex-shrink: 0 !important;
    }
  }
}

.conversation-list-mini {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.search-box {
  margin-bottom: 12px;
}

.conversations-spin {
  flex: 1;
  overflow: hidden;
}

.conversations {
  height: 100%;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.2s;

  &:hover {
    background: var(--color-fill-2);
  }
}

.conv-details {
  flex: 1;
  min-width: 0;
}

.conv-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.conv-name {
  font-weight: 500;
  font-size: 14px;
}

.conv-time {
  font-size: 12px;
  color: var(--color-text-3);
}

.conv-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.last-message {
  flex: 1;
  font-size: 13px;
  color: var(--color-text-2);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-window-mini {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.mini-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border-2);
}

.mini-user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.mini-messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.message-bubble {
  display: flex;
  flex-direction: column;
  max-width: 80%;

  &.sent {
    align-self: flex-end;

    .bubble-content {
      background: rgb(var(--primary-6));
      color: white;
      border-radius: 18px 18px 4px 18px;
    }
  }

  &.received {
    align-self: flex-start;

    .bubble-content {
      background: var(--color-fill-2);
      color: var(--color-text-1);
      border-radius: 18px 18px 18px 4px;
    }
  }
}

.bubble-content {
  padding: 8px 14px;
  font-size: 14px;
  word-wrap: break-word;
}

.bubble-time {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--color-text-3);
  margin-top: 2px;
  padding: 0 4px;
  align-self: flex-end;

  .seen-icon,
  .sent-icon {
    font-size: 12px;
    flex-shrink: 0;
  }

  .seen-icon {
    color: var(--color-success-6);
  }

  .sent-icon {
    color: var(--color-text-3);
  }
}

.mini-input {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border-2);

  :deep(.arco-textarea-wrapper) {
    flex: 1;
  }
}
</style>
