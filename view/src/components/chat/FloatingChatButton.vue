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
          <icon-message />
          <span>Tin nhắn</span>
          <a-badge v-if="chatStore.totalUnreadCount > 0" :count="chatStore.totalUnreadCount" :max-count="99" />
        </div>
      </template>

      <div class="drawer-content">
        <!-- Conversation list -->
        <div v-if="!activeConversation" class="conversation-list-mini">
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

    <!-- Floating button (ẩn khi scroll xuống) -->
    <transition name="slide-fade">
      <a-button
        v-show="!isScrolledDown"
        type="primary"
        shape="circle"
        size="large"
        class="floating-btn"
        @click="toggleDrawer"
      >
        <template #icon>
          <icon-message :size="24" />
        </template>
        <a-badge v-if="chatStore.totalUnreadCount > 0" :count="chatStore.totalUnreadCount" :offset="[-8, 8]" />
      </a-button>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { IconMessage, IconUser, IconSend, IconLeft, IconCheckCircle, IconCheckCircleFill } from '@arco-design/web-vue/es/icon'
import { Message } from '@arco-design/web-vue'
import dayjs from 'dayjs'
import useChatStore from '@/store/modules/chat'
import useUserStore from '@/store/modules/user'

const chatStore = useChatStore()
const userStore = useUserStore()

const drawerVisible = ref(false)
const searchKeyword = ref('')
const messageInput = ref('')
const messagesContainer = ref<HTMLElement | null>(null)
const isScrolledDown = ref(false)

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
  if (!searchKeyword.value) return chatStore.conversations
  return chatStore.conversations.filter((conv) => {
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
    if (unreadCount > 0) {
      await chatStore.markAsRead(otherUserId)
    }
  }
}

function backToList() {
  chatStore.setActiveConversation(null)
}

function formatTime(time: string) {
  if (!time) return ''
  const now = dayjs()
  const msgTime = dayjs(time)
  if (now.diff(msgTime, 'day') === 0) return msgTime.format('HH:mm')
  if (now.diff(msgTime, 'day') === 1) return 'Hôm qua'
  return msgTime.format('DD/MM')
}

function formatMessageTime(time: string) {
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

// Auto-scroll when messages change
watch(
  () => activeMessages.value.length,
  async () => {
    await nextTick()
    scrollToBottom()
  }
)

// Auto-scroll when conversation changes (for cached messages)
watch(
  activeConversation,
  async (newConv) => {
    if (newConv) {
      await nextTick()
      scrollToBottom()
    }
  }
)

// Connect WebSocket and scroll when drawer opens
watch(drawerVisible, async (visible) => {
  if (visible) {
    if (!chatStore.wsConnected) {
      await chatStore.connectWebSocket()
    }
    
    // Scroll to bottom if already in a conversation
    if (activeConversation.value) {
      await nextTick()
      scrollToBottom()
    }
  }
})

// Ẩn button khi scroll xuống
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

  &:hover {
    transform: scale(1.1);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
  }

  :deep(.arco-btn-icon) {
    margin: 0 !important;
  }

  :deep(.arco-icon) {
    margin: 0 !important;
  }

  :deep(.arco-badge) {
    position: absolute;
    top: -4px;
    right: -4px;
  }
}

// Slide fade animation
.slide-fade-enter-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 1, 1);
}

.slide-fade-enter-from {
  transform: translateY(20px) scale(0.8);
  opacity: 0;
}

.slide-fade-leave-to {
  transform: translateY(20px) scale(0.8);
  opacity: 0;
}

.drawer-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.drawer-content {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
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
