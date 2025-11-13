<template>
  <div class="floating-chat">
    <!-- Staff chat drawer -->
    <a-drawer
      v-model:visible="staffDrawerVisible"
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
            <span class="drawer-title-text">{{ t('chat.staffTab') }}</span>
          </div>
        </div>
      </template>

      <div class="drawer-content">
        <div v-if="!activeConversation" class="conversation-list-mini">
          <a-input-search v-model="searchKeyword" :placeholder="t('chat.searchPlaceholder')" class="search-box" />

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
                    <span class="last-message">{{ getLastMessagePreview(conv) }}</span>
                    <a-badge v-if="getUnreadCount(conv) > 0" :count="getUnreadCount(conv)" class="unread-badge" />
                  </div>
                </div>
              </div>

              <a-empty v-if="filteredConversations.length === 0" :description="t('chat.emptyConversations')" />
            </div>
          </a-spin>
        </div>

        <div v-else class="chat-window-mini">
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
              :key="msg.maTinNhan || msg.id"
              :class="['message-bubble', msg.senderId === userStore.id ? 'sent' : 'received']"
            >
              <div class="bubble-content">{{ msg.content }}</div>
              <div class="bubble-time">
                {{ formatMessageTime(msg.sentAt) }}
                <icon-check-circle-fill v-if="msg.senderId === userStore.id && msg.isRead" class="seen-icon" />
                <icon-check-circle v-else-if="msg.senderId === userStore.id" class="sent-icon" />
              </div>
            </div>
          </div>

          <div class="mini-input">
            <a-textarea
              v-model="messageInput"
              :auto-size="{ minRows: 1, maxRows: 3 }"
              :placeholder="t('chat.inputPlaceholder')"
              @keydown.enter.exact="onEnterKey"
              @keydown.shift.enter="handleNewLine"
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

    <!-- AI chat drawer -->
    <a-drawer
      v-model:visible="aiDrawerVisible"
      :width="360"
      :footer="false"
      placement="right"
      :mask-closable="true"
      unmount-on-close
      class="chat-drawer ai"
    >
      <template #title>
        <div class="drawer-title">
          <div class="title-actions">
            <span class="drawer-title-text">{{ t('chat.aiTab') }}</span>
          </div>
        </div>
      </template>

      <div class="drawer-content ai">
        <div class="ai-chat-container">
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
      </div>
    </a-drawer>

    <!-- Staff floating chat button -->
    <div class="floating-buttons">
      <a-button
        v-if="showStaffButton"
        type="primary"
        shape="circle"
        size="large"
        class="floating-btn staff-btn"
        @click="toggleStaffDrawer"
      >
        <template #icon>
          <span class="dual-icon">
            <icon-message :size="28" />
          </span>
        </template>
        <div
          v-if="!chatStore.wsConnected"
          class="connection-indicator"
          :class="{ connecting: chatStore.wsConnecting }"
          :title="chatStore.wsConnecting ? 'Đang kết nối...' : 'Mất kết nối'"
        />
        <a-badge v-if="totalUnreadBadge > 0" :count="totalUnreadBadge" :offset="[-6, -6]" />
      </a-button>

      <a-button
        type="primary"
        shape="circle"
        size="large"
        class="floating-btn ai-btn"
        @click="toggleAIDrawer"
      >
        <template #icon>
          <span class="dual-icon">
            <icon-robot :size="28" />
          </span>
        </template>
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { IconMessage, IconUser, IconSend, IconLeft, IconCheckCircle, IconCheckCircleFill, IconRobot } from '@arco-design/web-vue/es/icon'
import { Message } from '@arco-design/web-vue'
import dayjs from 'dayjs'
import useChatStore from '@/stores/chat'
import { useUserStore } from '@/stores/user'
import AIChatbot from '@/components/ai/AIChatbot.vue'
import { checkAIHealth } from '@/api/ai'
import { useI18n } from 'vue-i18n'

const chatStore = useChatStore()
const userStore = useUserStore()
const { t } = useI18n()

const staffDrawerVisible = ref(false)
const aiDrawerVisible = ref(false)
const searchKeyword = ref('')
const messageInput = ref('')
const messagesContainer = ref<HTMLElement | null>(null)

const aiConnected = ref(false)
const chatbotRef = ref<InstanceType<typeof AIChatbot> | null>(null)

const isCustomer = computed(() => userStore.isCustomer)
const isAuthenticated = computed(() => userStore.isAuthenticated)
const activeConversation = computed(() => chatStore.activeConversation)
const activeMessages = computed(() => chatStore.activeMessages)

const totalUnreadBadge = computed(() => {
  if (!isAuthenticated.value || isCustomer.value) return 0
  const convs = chatStore.conversations || []
  if (convs.length === 0) return 0
  return convs.reduce((sum: number, c: any) => {
    const count = Number(chatStore.getUnreadCount(c.id) || 0)
    return sum + (Number.isFinite(count) ? count : 0)
  }, 0)
})

const showStaffButton = computed(() => true)

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
  const conversations = chatStore.conversations
  if (!searchKeyword.value) return conversations
  return conversations.filter((conv) => {
    const name = getOtherUserName(conv).toLowerCase()
    return name.includes(searchKeyword.value.toLowerCase())
  })
})

function getLastMessagePreview(conv: any): string {
  const text: string = conv.lastMessageContent || ''
  if (!text) return t('chat.noMessages')
  const currentUserId = userStore.id
  let isMine = conv.lastSenderId === currentUserId
  if (!isMine) {
    const cached = chatStore.messages?.[conv.id]
    if (Array.isArray(cached) && cached.length > 0) {
      const last = cached[cached.length - 1]
      if (last && last.senderId === currentUserId) isMine = true
    }
  }
  const maxLength = 40
  const content = text.length > maxLength ? `${text.substring(0, maxLength)}...` : text
  return isMine ? t('chat.previewYou', { text: content }) : content
}

function toggleStaffDrawer() {
  if (!isAuthenticated.value) {
    Message.info(t('chat.loginRequired'))
    return
  }

  if (isCustomer.value) {
    Message.info(t('chat.staffLoginRequired'))
    return
  }

  staffDrawerVisible.value = !staffDrawerVisible.value

  if (staffDrawerVisible.value && chatStore.conversations.length === 0) {
    chatStore.fetchConversations()
    chatStore.fetchUnreadCount()
  }
}

function toggleAIDrawer() {
  aiDrawerVisible.value = !aiDrawerVisible.value
  if (aiDrawerVisible.value && !aiConnected.value) {
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
  if (!isAuthenticated.value || isCustomer.value) return
  chatStore.setActiveConversation(id, { userInitiated: true })
  const conv = chatStore.conversations.find((c) => c.id === id)
  if (conv) {
    const otherUserId = userStore.id === conv.nhanVien1Id ? conv.nhanVien2Id : conv.nhanVien1Id
    await chatStore.fetchMessages(otherUserId)

    await nextTick()
    scrollToBottom()

    const unreadCount = getUnreadCount(conv)
    if (unreadCount > 0) {
      await chatStore.markAsRead(otherUserId, id)
    }
  }
}

function backToList() {
  chatStore.setActiveConversation(null, { userInitiated: false })
}

function formatTime(time: string | null | undefined) {
  if (!time) return ''
  const now = dayjs()
  const msgTime = dayjs(time)
  if (now.diff(msgTime, 'day') === 0) return msgTime.format('HH:mm')
  if (now.diff(msgTime, 'day') === 1) return t('chat.yesterday')
  return msgTime.format('DD/MM')
}

function formatMessageTime(time: string | null | undefined) {
  if (!time) return ''
  return dayjs(time).format('HH:mm')
}

async function sendMessage() {
  if (!messageInput.value.trim() || !activeConversation.value || !isAuthenticated.value || isCustomer.value) return

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
    Message.error(t('chat.sendError', { message: error.message }))
  }
}

function handleScroll() {
  // Placeholder for future infinite scroll/pagination
}

function onEnterKey(event: KeyboardEvent) {
  event.preventDefault()
  if (messageInput.value.trim()) {
    sendMessage()
  }
}

function handleNewLine(_event: KeyboardEvent) {
  // Allow default newline on Shift+Enter
}

function handleSessionState(state: any) {
  // eslint-disable-next-line no-console
  console.log('Session state updated:', state)
}

watch(
  () => activeMessages.value.length,
  async () => {
    if (isCustomer.value || !staffDrawerVisible.value) return
    await nextTick()
    scrollToBottom()
  }
)

watch(
  () => activeMessages.value,
  async (messages) => {
    if (!isAuthenticated.value || isCustomer.value) return
    if (!staffDrawerVisible.value || !activeConversation.value) return

    if (Array.isArray(messages) && messages.length > 0) {
      const otherUserId =
        userStore.id === activeConversation.value.nhanVien1Id ? activeConversation.value.nhanVien2Id : activeConversation.value.nhanVien1Id

      const newUnreadMessages = messages.filter((msg: any) => msg.senderId === otherUserId && !msg.isRead)

      if (newUnreadMessages.length > 0 && chatStore.activeConversationUserInitiated) {
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

watch(activeConversation, async (newConv) => {
  if (isCustomer.value || !staffDrawerVisible.value) return
  if (newConv) {
    await nextTick()
    scrollToBottom()
  }
})

watch(staffDrawerVisible, async (visible) => {
  if (visible) {
    if (!chatStore.wsConnected && !chatStore.wsConnecting) {
      await chatStore.connectWebSocket()
    }

    if (chatStore.conversations.length === 0) {
      await chatStore.fetchConversations()
    }
    await chatStore.fetchUnreadCount()

    if (activeConversation.value) {
      await nextTick()
      scrollToBottom()
    }
  } else {
    if (chatStore.activeConversationId !== null) {
      chatStore.setActiveConversation(null, { userInitiated: false })
    }
    messageInput.value = ''
  }
})

watch(aiDrawerVisible, async (visible) => {
  if (visible && !aiConnected.value) {
    await checkAIConnection()
  }
})

watch(
  () => chatStore.totalUnreadCount,
  (newCount, oldCount) => {
    if (isCustomer.value) return
    if (newCount > oldCount && newCount > 0) {
      console.log(`📬 New message! Unread count: ${newCount}`)
    }
  }
)

watch(
  () => [staffDrawerVisible.value, activeConversation.value, activeMessages.value],
  async ([isDrawerOpen, currentConv, messages]) => {
    if (!isDrawerOpen || isCustomer.value) return
    if (!currentConv || !Array.isArray(messages) || messages.length === 0) return

    const otherUserId = userStore.id === currentConv.nhanVien1Id ? currentConv.nhanVien2Id : currentConv.nhanVien1Id
    const hasUnreadMessages = messages.some((msg: any) => msg.senderId === otherUserId && !msg.isRead)

    if (hasUnreadMessages && chatStore.activeConversationUserInitiated) {
      try {
        await chatStore.markAsRead(otherUserId, currentConv.id)
      } catch (error) {
        console.error('Error auto-marking messages as read:', error)
      }
    }
  },
  { deep: true, immediate: false }
)

watch(isCustomer, (value) => {
  if (value) {
    staffDrawerVisible.value = false
    chatStore.resetState()
  }
})

watch(
  () => isAuthenticated.value,
  async (loggedIn) => {
    if (!loggedIn) {
      staffDrawerVisible.value = false
      aiDrawerVisible.value = false
      chatStore.resetState()
      return
    }

    if (isCustomer.value) {
      await checkAIConnection()
      return
    }

    await chatStore.fetchConversations()
    await chatStore.fetchUnreadCount()
    if (!chatStore.wsConnected && !chatStore.wsConnecting) {
      await chatStore.connectWebSocket()
    }
  },
  { immediate: false }
)

onMounted(async () => {
  if (isAuthenticated.value && !isCustomer.value) {
    await chatStore.fetchConversations()
    await chatStore.fetchUnreadCount()
    if (!chatStore.wsConnected && !chatStore.wsConnecting) {
      await chatStore.connectWebSocket()
    }
  } else {
    chatStore.resetState()
  }

  await checkAIConnection()
})
</script>

<style scoped lang="less">
.floating-chat {
  position: relative;
}

.floating-buttons {
  position: fixed;
  bottom: 24px;
  right: 24px;
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 999;
}

.dual-icon {
  position: relative;
  width: 36px;
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;

  :deep(.arco-icon) {
    display: block;
  }
}

.floating-btn {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: 50% !important;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.24);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 0 !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  line-height: 1 !important;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 18px 36px rgba(0, 0, 0, 0.28);
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
    top: 8px;
    left: 8px;
    width: 10px;
    height: 10px;
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

.drawer-title {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
}

.drawer-title-text {
  font-weight: 600;
  font-size: 16px;
}

.title-actions {
  display: flex;
  align-items: center;
  justify-content: center;
}

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

.drawer-content.ai {
  height: calc(100vh - 120px);
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
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.mini-messages::-webkit-scrollbar {
  width: 0;
  height: 0;
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
