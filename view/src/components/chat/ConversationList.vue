<template>
  <div class="conversation-list">
    <!-- Header -->
    <div class="conversation-header">
      <div class="header-top">
        <h3>{{ $t('chat.header.title') }}</h3>
        <a-button type="primary" size="small" @click="showNewChatModal = true">
          <template #icon>
            <icon-plus />
          </template>
          {{ $t('chat.header.newChat') }}
        </a-button>
      </div>
      <a-input-search v-model="searchQuery" :placeholder="$t('chat.search.placeholder')" class="search-input" allow-clear />
    </div>

    <!-- Conversation list -->
    <a-spin :loading="loading" class="conversation-spin">
      <div class="conversation-items">
        <div
          v-for="conversation in filteredConversations"
          :key="conversation.id"
          class="conversation-item"
          :class="{ active: conversation.id === activeConversationId }"
          @click="handleSelectConversation(conversation)"
        >
          <!-- Avatar with Online Status -->
          <div class="avatar-wrapper">
            <a-avatar :size="48" class="conversation-avatar">
              <img v-if="getOtherUserAvatar(conversation)" :src="getOtherUserAvatar(conversation)" alt="avatar" />
              <template v-else>
                <icon-user />
              </template>
            </a-avatar>
            <!-- Online Status Indicator -->
            <span v-if="isUserOnline(conversation)" class="online-status" :class="{ 'status-online': isUserOnline(conversation) }" />
          </div>

          <!-- Content -->
          <div class="conversation-content">
            <div class="conversation-top">
              <span class="conversation-name">
                {{ getOtherUserName(conversation) }}
                <span v-if="conversation.loaiCuocTraoDoi === 'CUSTOMER_STAFF'" class="customer-badge">(KH)</span>
              </span>
              <span v-if="conversation.lastMessageTime" class="conversation-time">
                {{ formatTime(conversation.lastMessageTime) }}
              </span>
            </div>
            <div class="conversation-bottom">
              <div class="message-wrapper">
                <!-- Hiển thị "Seen" nếu là tin nhắn của mình và đã được đọc -->
                <icon-check-circle-fill v-if="isLastMessageSeenByOther(conversation)" class="seen-icon" />
                <span class="conversation-message" :class="{ unread: hasUnread(conversation) }">
                  {{ getLastMessagePreview(conversation) }}
                </span>
              </div>
              <a-badge v-if="hasUnread(conversation)" :count="getUnreadCount(conversation)" :max-count="99" class="conversation-badge" />
            </div>
          </div>
        </div>

        <!-- Empty state -->
        <a-empty v-if="filteredConversations.length === 0" description="Chưa có cuộc trò chuyện nào" />
      </div>
    </a-spin>

    <!-- New Chat Modal -->
    <NewChatModal v-model:visible="showNewChatModal" @selected="handleNewChat" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { IconUser, IconPlus, IconCheckCircleFill } from '@arco-design/web-vue/es/icon'
import { Message } from '@arco-design/web-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/vi'
import useChatStore from '@/store/modules/chat'
import useUserStore from '@/store/modules/user'
import type { Conversation } from '@/api/chat'
import NewChatModal from './NewChatModal.vue'

dayjs.extend(relativeTime)
dayjs.locale('vi')

const chatStore = useChatStore()
const userStore = useUserStore()

const searchQuery = ref('')
const showNewChatModal = ref(false)
const loading = computed(() => chatStore.loadingConversations)
const activeConversationId = computed(() => chatStore.activeConversationId)

/**
 * Lọc conversations theo search query
 */
/**
 * Lấy tên người dùng còn lại (không phải mình)
 */
function getOtherUserName(conversation: Conversation): string {
  const currentUserId = userStore.id
  
  // Handle customer-staff conversations
  if (conversation.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
    if (currentUserId === conversation.nhanVienId) {
      return conversation.khachHangName || 'Khách hàng'
    } else if (currentUserId === conversation.khachHangId) {
      return conversation.nhanVienName || 'Nhân viên'
    }
  }
  
  // Handle staff-staff conversations
  if (currentUserId === conversation.nhanVien1Id) {
    return conversation.nhanVien2Name || ''
  }
  return conversation.nhanVien1Name || ''
}

const filteredConversations = computed(() => {
  const conversations = chatStore.conversations || []
  if (!searchQuery.value) {
    return conversations
  }
  const query = searchQuery.value.toLowerCase()
  return conversations.filter((conv) => {
    const otherUserName = getOtherUserName(conv).toLowerCase()
    return otherUserName.includes(query)
  })
})

/**
 * Lấy avatar người dùng còn lại
 */
function getOtherUserAvatar(_conversation: Conversation): string | undefined {
  // TODO: Implement avatar from user data
  return undefined
}

/**
 * Lấy preview tin nhắn cuối
 */
function getLastMessagePreview(conversation: Conversation): string {
  if (!conversation.lastMessageContent) {
    return 'Chưa có tin nhắn'
  }
  const maxLength = 40
  const currentUserId = userStore.id
  // Prefer backend flag; fall back to locally cached last message sender
  // Handle null/undefined lastSenderId (can happen when conversation is newly created)
  let isMine = conversation.lastSenderId === currentUserId
  if (!isMine && conversation.lastSenderId === null) {
    // If lastSenderId is null, check cached messages to determine if last message is ours
    const cached = chatStore.messages?.[conversation.id]
    if (Array.isArray(cached) && cached.length > 0) {
      const last = cached[cached.length - 1]
      if (last && last.senderId === currentUserId) {
        isMine = true
      }
    }
  } else if (!isMine) {
    const cached = chatStore.messages?.[conversation.id]
    if (Array.isArray(cached) && cached.length > 0) {
      const last = cached[cached.length - 1]
      if (last && last.senderId === currentUserId) {
        isMine = true
      }
    }
  }
  const preview = conversation.lastMessageContent || ''
  const prefix = isMine ? '' : ''
  const content = preview.length > maxLength ? `${preview.substring(0, maxLength)}...` : preview
  if (isMine) {
    return `Bạn: ${content}`
  }
  return `${prefix}${content}`
}

/**
 * Format time hiển thị
 */
function formatTime(timestamp: string | null): string {
  if (!timestamp) return ''
  const time = dayjs(timestamp)
  const now = dayjs()
  const diffInHours = now.diff(time, 'hour')

  if (diffInHours < 24) {
    return time.format('HH:mm')
  }
  if (diffInHours < 168) {
    // < 7 days
    return time.format('ddd')
  }
  return time.format('DD/MM')
}

/**
 * Kiểm tra có tin nhắn chưa đọc không
 */
function hasUnread(conversation: Conversation): boolean {
  return chatStore.getUnreadCount(conversation.id) > 0
}

/**
 * Lấy số tin nhắn chưa đọc
 */
function getUnreadCount(conversation: Conversation): number {
  return chatStore.getUnreadCount(conversation.id)
}

/**
 * Kiểm tra tin nhắn cuối cùng của mình đã được đọc chưa
 */
function isLastMessageSeenByOther(conversation: Conversation): boolean {
  const currentUserId = userStore.id

  // Chỉ hiển thị "seen" nếu tin nhắn cuối là của mình
  // Handle null/undefined lastSenderId (can happen when conversation is newly created)
  if (!conversation.lastSenderId || conversation.lastSenderId !== currentUserId) {
    return false
  }

  // Handle customer-staff conversations
  if (conversation.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
    if (currentUserId === conversation.khachHangId) {
      return conversation.unreadCountNv2 === 0
    } else if (currentUserId === conversation.nhanVienId) {
      return conversation.unreadCountNv1 === 0
    }
  }
  
  // Handle staff-staff conversations
  if (currentUserId === conversation.nhanVien1Id) {
    return conversation.unreadCountNv2 === 0
  }
  if (currentUserId === conversation.nhanVien2Id) {
    return conversation.unreadCountNv1 === 0
  }

  return false
}

/**
 * Kiểm tra user có đang online không
 * Logic: Check từ WebSocket presence tracking (real-time)
 */
function isUserOnline(conversation: Conversation): boolean {
  const currentUserId = userStore.id

  // Xác định user còn lại (không phải mình)
  let otherUserId: number | undefined
  
  // Handle customer-staff conversations
  if (conversation.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
    if (currentUserId === conversation.khachHangId) {
      otherUserId = conversation.nhanVienId
    } else if (currentUserId === conversation.nhanVienId) {
      otherUserId = conversation.khachHangId
    }
  } else {
    // Handle staff-staff conversations
    otherUserId = currentUserId === conversation.nhanVien1Id ? conversation.nhanVien2Id : conversation.nhanVien1Id
  }

  // Check từ online users set (WebSocket presence tracking)
  return otherUserId !== undefined && chatStore.onlineUsers.has(otherUserId)
}

/**
 * Xử lý khi click vào conversation
 */
async function handleSelectConversation(conversation: Conversation) {
  chatStore.setActiveConversation(conversation.id, { userInitiated: true })

  // Nếu có tin nhắn chưa đọc, tự động mark as read
  if (hasUnread(conversation)) {
    const currentUserId = userStore.id
    let otherUserId: number | null = null

    // Handle customer-staff conversations
    if (conversation.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
      if (currentUserId === conversation.khachHangId) {
        otherUserId = conversation.nhanVienId || null
      } else if (currentUserId === conversation.nhanVienId) {
        otherUserId = conversation.khachHangId || null
      }
    } else {
      // Handle staff-staff conversations
      if (currentUserId === conversation.nhanVien1Id) {
        otherUserId = conversation.nhanVien2Id || null
      } else if (currentUserId === conversation.nhanVien2Id) {
        otherUserId = conversation.nhanVien1Id || null
      }
    }

    if (otherUserId) {
      // Gọi mark as read ngay lập tức
      await chatStore.markAsRead(otherUserId, conversation.id)
    }
  }
}

/**
 * Xử lý tạo chat mới
 */
async function handleNewChat(userId: number) {
  console.log(' Starting new chat with user:', userId)
  try {
    // Kiểm tra xem đã có conversation chưa (check both staff-staff and customer-staff)
    const existingConv = (chatStore.conversations || []).find((c) => {
      // Check staff-staff conversations
      if (c.loaiCuocTraoDoi === 'STAFF_STAFF' || !c.loaiCuocTraoDoi) {
        return (
          (c.nhanVien1Id === userStore.id && c.nhanVien2Id === userId) ||
          (c.nhanVien2Id === userStore.id && c.nhanVien1Id === userId)
        )
      }
      // Check customer-staff conversations
      if (c.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
        return (
          (c.nhanVienId === userStore.id && c.khachHangId === userId) ||
          (c.khachHangId === userStore.id && c.nhanVienId === userId)
        )
      }
      return false
    })

    if (existingConv) {
      // Nếu đã có, chỉ mở conversation
      console.log('Existing conversation found:', existingConv.id)
      chatStore.setActiveConversation(existingConv.id, { userInitiated: true })
      // Fetch messages to ensure we have the latest
      await chatStore.fetchMessages(userId)
    } else {
      // Nếu chưa có, tạo mới bằng cách gọi tin nhắn đầu tiên
      console.log('Sending first message to create conversation...')
      await chatStore.sendMessageViaAPI({
        receiverId: userId,
        content: 'Xin chào! ',
        messageType: 'TEXT',
      })

      // Wait a bit for the backend to process the message and create the conversation
      await new Promise((resolve) => setTimeout(resolve, 300))

      console.log('Fetching conversation...')
      // Lấy conversation vừa tạo qua API
      await chatStore.fetchConversations()

      // Mở conversation (check both staff-staff and customer-staff)
      const newConv = (chatStore.conversations || []).find((c) => {
        // Check staff-staff conversations
        if (c.loaiCuocTraoDoi === 'STAFF_STAFF' || !c.loaiCuocTraoDoi) {
          return (
            (c.nhanVien1Id === userStore.id && c.nhanVien2Id === userId) ||
            (c.nhanVien2Id === userStore.id && c.nhanVien1Id === userId)
          )
        }
        // Check customer-staff conversations
        if (c.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
          return (
            (c.nhanVienId === userStore.id && c.khachHangId === userId) ||
            (c.khachHangId === userStore.id && c.nhanVienId === userId)
          )
        }
        return false
      })

      if (newConv) {
        console.log('Conversation found, opening:', newConv)
        chatStore.setActiveConversation(newConv.id, { userInitiated: true })
        // Lấy tin nhắn của conversation
        await chatStore.fetchMessages(userId)
      } else {
        console.warn('Could not find conversation after sending message')
        // Try one more time after a longer delay
        await new Promise((resolve) => setTimeout(resolve, 500))
        await chatStore.fetchConversations()
        
        const retryConv = (chatStore.conversations || []).find((c) => {
          if (c.loaiCuocTraoDoi === 'STAFF_STAFF' || !c.loaiCuocTraoDoi) {
            return (
              (c.nhanVien1Id === userStore.id && c.nhanVien2Id === userId) ||
              (c.nhanVien2Id === userStore.id && c.nhanVien1Id === userId)
            )
          }
          if (c.loaiCuocTraoDoi === 'CUSTOMER_STAFF') {
            return (
              (c.nhanVienId === userStore.id && c.khachHangId === userId) ||
              (c.khachHangId === userStore.id && c.nhanVienId === userId)
            )
          }
          return false
        })
        
        if (retryConv) {
          chatStore.setActiveConversation(retryConv.id, { userInitiated: true })
          await chatStore.fetchMessages(userId)
        } else {
          Message.warning('Vui lòng reload trang để xem cuộc trò chuyện mới')
        }
      }
    }
  } catch (error: any) {
    console.error('Error creating new chat:', error)
    Message.error(`Không thể tạo cuộc trò chuyện: ${error.message || error}`)
  }
}
</script>

<style scoped lang="less">
.conversation-list {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--color-bg-1);
  border-right: 1px solid var(--color-border-2);
}

.conversation-header {
  padding: 16px;
  border-bottom: 1px solid var(--color-border-2);

  .header-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  h3 {
    margin: 0;
    font-size: 24px;
    font-weight: 600;
    color: var(--color-text-1);
  }

  .search-input {
    width: 100%;
  }
}

.conversation-spin {
  flex: 1;
  overflow: hidden;
}

.conversation-items {
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--color-border-3);
    border-radius: 3px;
  }
}

.conversation-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
  gap: 12px;

  &:hover {
    background: var(--color-fill-2);
  }

  &.active {
    background: var(--color-fill-3);
  }

  // Avatar wrapper with online status
  .avatar-wrapper {
    position: relative;
    flex-shrink: 0;

    .conversation-avatar {
      flex-shrink: 0;
    }

    // Online status dot
    .online-status {
      position: absolute;
      bottom: 2px;
      right: 2px;
      width: 12px;
      height: 12px;
      border-radius: 50%;
      border: 2px solid var(--color-bg-1);
      background-color: var(--color-text-4);
      transition: background-color 0.3s ease;

      &.status-online {
        background-color: rgb(var(--success-6));
        box-shadow: 0 0 0 2px rgba(var(--success-6), 0.2);
        animation: pulse-online 2s ease-in-out infinite;
      }
    }
  }

  .conversation-content {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4px;

    .conversation-top {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 8px;

      .conversation-name {
        font-weight: 500;
        font-size: 15px;
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        color: var(--color-text-1);
        display: flex;
        align-items: center;
        gap: 4px;

        .customer-badge {
          font-size: 10px;
          color: #52c41a;
          font-weight: 400;
          flex-shrink: 0;
        }
      }

      .conversation-time {
        font-size: 12px;
        color: var(--color-text-3);
        flex-shrink: 0;
      }
    }

    .conversation-bottom {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 8px;

      .message-wrapper {
        display: flex;
        align-items: center;
        gap: 4px;
        flex: 1;
        min-width: 0;

        .seen-icon {
          font-size: 12px;
          color: rgb(var(--success-6));
          flex-shrink: 0;
        }
      }

      .conversation-message {
        font-size: 13px;
        color: var(--color-text-3);
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;

        &.unread {
          color: var(--color-text-1);
          font-weight: 500;
        }
      }

      .conversation-badge {
        flex-shrink: 0;
      }
    }
  }
}

// Pulse animation for online status
@keyframes pulse-online {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.05);
  }
}
</style>
