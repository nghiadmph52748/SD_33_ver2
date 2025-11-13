<template>
  <div class="chat-view">
    <div class="chat-container">
      <!-- Left Panel: Conversation List -->
      <div class="conversation-panel">
        <ConversationList />
      </div>

      <!-- Right Panel: Chat Window -->
      <div class="chat-panel">
        <ChatWindow v-if="chatStore.activeConversationId" />

        <!-- Empty State -->
        <a-empty v-else class="empty-state" :description="$t('chat.empty.description')">
          <template #image>
            <icon-message :size="80" />
          </template>
        </a-empty>
      </div>
    </div>

    <!-- WebSocket Connection Status -->
    <div v-if="!chatStore.wsConnected && chatStore.wsConnecting" class="connection-status">
      <a-space>
        <a-spin :size="14" />
        <span>{{ $t('chat.connection.connecting') }}</span>
      </a-space>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import useChatStore from '@/store/modules/chat'
import ConversationList from '@/components/chat/ConversationList.vue'
import ChatWindow from '@/components/chat/ChatWindow.vue'
import { IconMessage } from '@arco-design/web-vue/es/icon'

const chatStore = useChatStore()
const route = useRoute()
const router = useRouter()

onMounted(async () => {
  // Kết nối WebSocket
  if (!chatStore.wsConnected) {
    await chatStore.connectWebSocket()
  }

  // Lấy danh sách conversations
  await chatStore.fetchConversations()

  // Lấy tổng số tin chưa đọc
  await chatStore.fetchUnreadCount()

  // Nếu có conversationId trong route, set active conversation
  const conversationId = route.params.conversationId
  if (conversationId) {
    const id = Number(conversationId)
    if (!Number.isNaN(id)) {
      chatStore.setActiveConversation(id, { userInitiated: true })
    }
  } else if (route.name === 'ChatIndex' && chatStore.conversations.length > 0) {
    // Auto-select first conversation when accessing /chat/index without conversationId
    chatStore.setActiveConversation(chatStore.conversations[0].id, { userInitiated: false })
  }
})

// Watch active conversation và sync với URL
watch(
  () => chatStore.activeConversationId,
  (newId) => {
    if (newId) {
      // Update URL nếu khác với conversationId hiện tại
      const currentId = route.params.conversationId
      if (!currentId || Number(currentId) !== newId) {
        router.replace({ name: 'ChatConversation', params: { conversationId: newId } })
      }
    } else {
      // Về trang chat index nếu không có active conversation
      if (route.name !== 'ChatIndex') {
        router.replace({ name: 'ChatIndex' })
      }
    }
  }
)

onBeforeUnmount(() => {
  // Giữ WebSocket connection khi user navigate away
  // Chỉ disconnect khi logout (xử lý trong useUserStore)
})
</script>

<style scoped lang="less">
.chat-view {
  height: calc(100vh - 60px); // Trừ đi header height
  padding: 16px;
  background-color: var(--color-bg-1);
  position: relative;
}

.chat-container {
  height: 100%;
  display: flex;
  gap: 16px;
  max-width: 1400px;
  margin: 0 auto;
}

.conversation-panel {
  width: 360px;
  flex-shrink: 0;
  background-color: var(--color-bg-2);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.chat-panel {
  flex: 1;
  background-color: var(--color-bg-2);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  :deep(.arco-empty-description) {
    color: var(--color-text-2);
    font-size: 16px;
  }

  :deep(.arco-icon) {
    color: var(--color-text-3);
  }
}

.connection-status {
  position: fixed;
  bottom: 24px;
  right: 24px;
  padding: 12px 20px;
  background-color: var(--color-bg-5);
  color: var(--color-text-1);
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  font-size: 14px;
  z-index: 999;
}

/* Responsive */
@media (max-width: 1024px) {
  .conversation-panel {
    width: 300px;
  }
}

@media (max-width: 768px) {
  .chat-container {
    flex-direction: column;
  }

  .conversation-panel {
    width: 100%;
    height: 40%;
  }

  .chat-panel {
    height: 60%;
  }
}
</style>
