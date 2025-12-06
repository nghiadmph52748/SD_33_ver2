<template>
  <div class="floating-chat">
    <!-- AI/Staff chat popup -->
    <div v-if="aiDrawerVisible" class="chat-popup ai-popup" :class="{ minimized: isAIMinimized }">
      <div class="popup-header">
        <div v-if="!isAIMinimized" class="popup-title-wrapper">
          <span class="popup-title">{{ isStaffChatMode ? t('chat.staffTab') : t('chat.aiTab') }}</span>
          <a-badge v-if="!isStaffChatMode" :status="aiConnected ? 'success' : 'error'" :text="aiConnected ? 'Online' : 'Offline'" />
        </div>
        <div class="popup-header-actions">
          <button class="popup-btn maximize-btn" @click="toggleAIMinimize" :aria-label="isAIMinimized ? 'Maximize' : 'Minimize'">
            <svg v-if="isAIMinimized" width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
              <path d="M7 14H5v5h5v-2H7v-3zm-2-4h2V7h3V5H5v5zm12 7h-3v2h5v-5h-2v3zM14 5v2h3v3h2V5h-5z"/>
            </svg>
            <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
              <path d="M5 16h3v3h2v-5H5v2zm3-8H5v2h5V5H8v3zm6 11h2v-3h3v-2h-5v5zm2-11V5h-2v5h5V8h-3z"/>
            </svg>
          </button>
          <button class="popup-btn close-btn" @click="closeAIPopup" aria-label="Close">
            <icon-close />
          </button>
        </div>
      </div>

      <div v-if="!isAIMinimized" class="popup-content ai">
        <div class="ai-chat-container">
          <AIChatbot
            ref="chatbotRef"
            :suppress-connection-notice="true"
            :enable-health-check="false"
            :connection-status="aiConnected"
            :staff-chat-mode="isStaffChatMode"
            :staff-conversation-id="staffChatConversationId"
            :staff-receiver-id="staffChatReceiverId"
            @session-state="handleSessionState"
            @redirect-to-staff="handleRedirectToStaff"
            @staff-message-sent="handleStaffMessageSent"
          />
        </div>
      </div>

    </div>

    <!-- AI floating chat button -->
    <div class="floating-buttons">
      <button
        class="floating-btn ai-btn"
        @click="toggleAIDrawer"
        aria-label="AI Chat"
      >
        <icon-robot :size="32" />
        <a-badge v-if="isStaffChatMode && totalUnreadBadge > 0" :count="totalUnreadBadge" :offset="[-8, -8]" />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { IconMessage, IconUser, IconSend, IconLeft, IconCheckCircle, IconCheckCircleFill, IconRobot, IconClose } from '@arco-design/web-vue/es/icon'
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

const aiDrawerVisible = ref(false)
const isAIMinimized = ref(false)
const isStaffChatMode = ref(false)
const staffChatConversationId = ref<number | null>(null)
const staffChatReceiverId = ref<number | null>(null)

const aiConnected = ref(false)
const chatbotRef = ref<InstanceType<typeof AIChatbot> | null>(null)

const isCustomer = computed(() => userStore.isCustomer)
const isAuthenticated = computed(() => userStore.isAuthenticated)
const activeConversation = computed(() => chatStore.activeConversation)
const activeMessages = computed(() => chatStore.activeMessages)

const totalUnreadBadge = computed(() => {
  if (!isAuthenticated.value) return 0
  const convs = chatStore.conversations || []
  if (convs.length === 0) return 0
  return convs.reduce((sum: number, c: any) => {
    const count = Number(chatStore.getUnreadCount(c.id) || 0)
    return sum + (Number.isFinite(count) ? count : 0)
  }, 0)
})

function getOtherUserName(conv: any) {
  // Handle customer-staff conversations
  if (conv.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
    if (userStore.id === conv.khachHangId) {
      return conv.nhanVienName || ''
    } else if (userStore.id === conv.nhanVienId) {
      return conv.khachHangName || ''
    }
  }
  // Handle staff-staff conversations
  return userStore.id === conv.nhanVien1Id ? conv.nhanVien2Name : conv.nhanVien1Name
}

function getUnreadCount(conv: any) {
  // Handle customer-staff conversations
  if (conv.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
    if (userStore.id === conv.khachHangId) {
      return conv.unreadCountNv1 || 0
    } else if (userStore.id === conv.nhanVienId) {
      return conv.unreadCountNv2 || 0
    }
  }
  // Handle staff-staff conversations
  return userStore.id === conv.nhanVien1Id ? conv.unreadCountNv1 : conv.unreadCountNv2
}

// Helper functions for staff chat (used in findAvailableStaff)

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

function toggleAIDrawer() {
  aiDrawerVisible.value = !aiDrawerVisible.value
  if (aiDrawerVisible.value && !aiConnected.value && !isStaffChatMode.value) {
    checkAIConnection()
  }
  if (aiDrawerVisible.value && isStaffChatMode.value) {
    // Ensure WebSocket is connected for staff chat
    if (!chatStore.wsConnected && !chatStore.wsConnecting) {
      chatStore.connectWebSocket()
    }
    if (chatStore.conversations.length === 0 && isAuthenticated.value) {
      chatStore.fetchConversations()
      chatStore.fetchUnreadCount()
    }
  }
}

function toggleAIMinimize() {
  isAIMinimized.value = !isAIMinimized.value
}

function closeAIPopup() {
  // Save chat history before closing (component will unmount when aiDrawerVisible becomes false)
  // Access the chatbot component's saveHistory method if available
  if (chatbotRef.value && typeof (chatbotRef.value as any).saveHistory === 'function') {
    try {
      (chatbotRef.value as any).saveHistory()
      // Also set flag to indicate window is closing (for guest sessions)
      if (!userStore.isAuthenticated) {
        sessionStorage.setItem('__ai_chat_window_closed', 'true')
        sessionStorage.setItem('__ai_chat_close_time', Date.now().toString())
        console.log('🏷️ Set window closed flag before closing AI popup')
      }
    } catch (e) {
      console.error('Error saving history before close:', e)
    }
  }
  
  // Small delay to ensure save completes before unmount
  setTimeout(() => {
    aiDrawerVisible.value = false
    isAIMinimized.value = false
    isStaffChatMode.value = false
    staffChatConversationId.value = null
    staffChatReceiverId.value = null
    // Reset active conversation when closing
    if (chatStore.activeConversationId !== null) {
      chatStore.setActiveConversation(null, { userInitiated: false })
    }
  }, 50)
}

async function checkAIConnection() {
  try {
    await checkAIHealth()
    aiConnected.value = true
  } catch (error) {
    aiConnected.value = false
  }
}

// Helper functions for staff chat mode

function handleSessionState(state: any) {
  // eslint-disable-next-line no-console
  console.log('Session state updated:', state)
}

async function handleRedirectToStaff() {
  // Switch to staff chat mode in the same window
  isStaffChatMode.value = true
  
  // Ensure WebSocket is connected
  if (!chatStore.wsConnected && !chatStore.wsConnecting) {
    await chatStore.connectWebSocket()
  }
  
  // Fetch conversations and online users (may fail silently for 403 errors)
  // For guest users, skip fetch as they don't have permission
  try {
    await chatStore.fetchConversations()
    // Only fetch online users for authenticated users
    if (isAuthenticated.value) {
      await chatStore.fetchOnlineUsers()
    } else {
      // Guest users don't need online users list
      chatStore.onlineUsers = new Set()
    }
  } catch (error) {
    // Errors are already handled in store methods
    // Continue to check for available staff
  }
  
  // Find an available online staff member
  const availableStaff = findAvailableStaff()
  
  if (availableStaff) {
    await nextTick()
    
    // Set staff chat receiver ID
    if (availableStaff.id) {
      // Existing conversation
      staffChatConversationId.value = availableStaff.id
      staffChatReceiverId.value = availableStaff.nhanVienId || null
      
      // Set as active conversation and load messages
      chatStore.setActiveConversation(availableStaff.id, { userInitiated: true })
      if (availableStaff.nhanVienId) {
        await chatStore.fetchMessages(availableStaff.nhanVienId)
      }
    } else if (availableStaff.nhanVienId) {
      // New conversation - will be created when first message is sent
      staffChatReceiverId.value = availableStaff.nhanVienId
      staffChatConversationId.value = null
    } else if (availableStaff.allowGuestChat) {
      // Guest chat - backend will assign staff when first message is sent
      staffChatReceiverId.value = null
      staffChatConversationId.value = null
    }
  } else {
    // Show a friendly message that no staff is available
    Message.warning({
      content: 'Xin lỗi, hiện tại không có nhân viên nào đang hoạt động. Vui lòng thử lại sau hoặc để lại tin nhắn, chúng tôi sẽ phản hồi sớm nhất có thể.',
      duration: 5000,
    })
    isStaffChatMode.value = false
  }
}

function handleStaffMessageSent(message: string) {
  // Message was sent, refresh conversations to get the new one if needed
  // Only for authenticated users - guest users don't need conversations list
  if (staffChatConversationId.value === null && staffChatReceiverId.value && isAuthenticated.value) {
    setTimeout(async () => {
      await chatStore.fetchConversations()
      // Find the new conversation
      const newConv = chatStore.conversations.find((conv) => {
        if (conv.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
          return conv.nhanVienId === staffChatReceiverId.value
        }
        return false
      })
      if (newConv) {
        staffChatConversationId.value = newConv.id
      }
    }, 500)
  }
}

function findAvailableStaff(): any {
  // For guest users, always allow chat (backend will assign staff)
  if (!isAuthenticated.value) {
    // Guest can chat - backend will handle staff assignment when first message is sent
    return { id: null, nhanVienId: null, allowGuestChat: true }
  }
  
  // For authenticated users, try to find existing conversation or online staff
  const onlineStaffIds = Array.from(chatStore.onlineUsers)
  
  // Find conversations with online staff (customer-staff conversations)
  const staffConversations = chatStore.conversations.filter((conv) => {
    if (conv.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
      return onlineStaffIds.length === 0 || onlineStaffIds.includes(conv.nhanVienId || 0)
    }
    return false
  })
  
  // If we have an existing conversation, use it
  if (staffConversations.length > 0) {
    // Prefer online staff, then staff with fewer unread messages
    staffConversations.sort((a, b) => {
      const aIsOnline = onlineStaffIds.includes(a.nhanVienId || 0)
      const bIsOnline = onlineStaffIds.includes(b.nhanVienId || 0)
      if (aIsOnline && !bIsOnline) return -1
      if (!aIsOnline && bIsOnline) return 1
      const aUnread = getUnreadCount(a)
      const bUnread = getUnreadCount(b)
      return aUnread - bUnread
    })
    return staffConversations[0]
  }
  
  // No existing conversation, but we have online staff
  if (onlineStaffIds.length > 0) {
    // Return first online staff ID - conversation will be created when first message is sent
    return { id: null, nhanVienId: onlineStaffIds[0] }
  }
  
  // Authenticated user but no online staff
  return null
}

// Watch functions removed - AI chatbot handles its own message display

watch(aiDrawerVisible, async (visible) => {
  if (visible) {
    if (isStaffChatMode.value) {
      // Staff chat mode
      if (!chatStore.wsConnected && !chatStore.wsConnecting) {
        await chatStore.connectWebSocket()
      }
      // Only fetch conversations for authenticated users
      if (chatStore.conversations.length === 0 && isAuthenticated.value) {
        await chatStore.fetchConversations()
      }
      if (isAuthenticated.value) {
        await chatStore.fetchUnreadCount()
      }
    } else {
      // AI chat mode
      if (!aiConnected.value) {
        await checkAIConnection()
      }
    }
  } else {
    // When closing, reset if in staff mode
    if (isStaffChatMode.value && chatStore.activeConversationId !== null) {
      chatStore.setActiveConversation(null, { userInitiated: false })
    }
  }
})

watch(
  () => chatStore.totalUnreadCount,
  (newCount, oldCount) => {
    if (newCount > oldCount && newCount > 0) {
      console.log(`📬 New message! Unread count: ${newCount}`)
    }
  }
)

// Watch for new messages when in staff chat mode to ensure active conversation is set
watch(
  () => [chatStore.messages, isStaffChatMode.value, staffChatReceiverId.value, staffChatConversationId.value],
  ([, isStaffMode, receiverId, convId]) => {
    if (!isStaffMode || !receiverId || typeof receiverId !== 'number') return
    
    // If we have a conversation ID but it's not active, set it as active
    if (convId && typeof convId === 'number' && chatStore.activeConversationId !== convId) {
      chatStore.setActiveConversation(convId, { userInitiated: true })
      chatStore.fetchMessages(receiverId)
    } else if (!convId && receiverId) {
      // No conversation ID yet, but we have a receiver ID
      // Check if a conversation exists for this receiver
      const matchingConv = chatStore.conversations.find((c: any) => {
        if (c.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
          return c.nhanVienId === receiverId && c.khachHangId === userStore.id
        }
        return false
      })
      
      if (matchingConv && matchingConv.id) {
        // Found the conversation, update our state
        staffChatConversationId.value = matchingConv.id
        chatStore.setActiveConversation(matchingConv.id, { userInitiated: true })
        chatStore.fetchMessages(receiverId)
      }
    }
  },
  { deep: true }
)

watch(
  () => [isStaffChatMode.value, aiDrawerVisible.value, activeConversation.value, activeMessages.value] as const,
  async ([isStaffMode, isAIVisible, currentConv, messages]) => {
    if (!isStaffMode || !isAIVisible) return
    if (!currentConv || !Array.isArray(messages) || messages.length === 0) return
    // Type guard: ensure currentConv is a Conversation
    if (typeof currentConv !== 'object' || !('id' in currentConv)) return

    let otherUserId: number | undefined
    if (currentConv.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
      otherUserId = userStore.id === currentConv.khachHangId ? currentConv.nhanVienId : currentConv.khachHangId
    } else {
      otherUserId = userStore.id === currentConv.nhanVien1Id ? currentConv.nhanVien2Id : currentConv.nhanVien1Id
    }
    if (!otherUserId) return
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

// Removed customer watch - customers can now use chat

watch(
  () => isAuthenticated.value,
  async (loggedIn) => {
    if (!loggedIn) {
      aiDrawerVisible.value = false
      isStaffChatMode.value = false
      chatStore.resetState()
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
  if (isAuthenticated.value) {
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

.chat-popup {
  position: fixed;
  bottom: 100px;
  right: 24px;
  width: 380px;
  max-height: 600px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  z-index: 1000;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &.minimized {
    max-height: 60px;
    width: 200px; /* Reduced width when minimized */
    overflow: hidden;
  }

  &.staff-popup {
    width: 500px;
    
    &.minimized {
      width: 200px; /* Reduced width when minimized */
    }
  }

  &.ai-popup {
    width: 500px;
    
    &.minimized {
      width: 200px; /* Reduced width when minimized */
    }
  }
}

.popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  background: #fff;
  flex-shrink: 0;
  
  .chat-popup.minimized & {
    padding: 12px 20px;
    justify-content: flex-end; /* Only show actions when minimized */
  }
}

.popup-header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.popup-title-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.popup-title {
  font-weight: 600;
  font-size: 16px;
  color: #111;
}

.popup-title-minimized {
  font-weight: 600;
  font-size: 14px;
  color: #111;
}

.popup-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  transition: color 0.2s ease;
  border-radius: 4px;

  &:hover {
    color: #111;
    background: #f5f5f5;
  }

  svg {
    display: block;
  }
}

.popup-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  min-height: 0;
  background: #fafafa;
  padding: 16px;
  display: flex;
  flex-direction: column;

  &.ai {
    padding: 0;
    background: #fff;
  }
}

.popup-btn.close-btn {
  :deep(.arco-icon) {
    font-size: 16px;
  }
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


.floating-btn {
  position: relative;
  width: 66px;
  height: 66px;
  border-radius: 50%;
  background: #111;
  border: none;
  color: #fff;
  cursor: pointer;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.24);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 18px 36px rgba(0, 0, 0, 0.28);
    background: #1a1a1a;
  }

  &:active {
    transform: translateY(0);
  }

  :deep(.arco-icon) {
    color: #fff;
    margin: 0;
    display: block;
  }

  :deep(.arco-badge) {
    position: absolute;
    top: -4px;
    right: -4px;
  }

  .connection-indicator {
    position: absolute;
    top: 10px;
    left: 10px;
    width: 12px;
    height: 12px;
    background-color: #f5222d;
    border-radius: 50%;
    border: 2px solid #111;
    z-index: 1;

    &.connecting {
      background-color: #faad14;
      animation: pulse 1.5s ease-in-out infinite;
    }
  }
}


.ai-chat-container {
  height: 100%;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;

  :deep(.ai-chatbot) {
    height: 100%;
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;

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


