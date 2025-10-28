<template>
  <div class="ai-chatbot">
    <a-card class="chatbot-card" :bordered="false">
      <template #title>
        <div class="chatbot-header">
          <span class="title">🤖 AI Assistant</span>
          <a-space>
            <a-badge
              :status="isConnected ? 'success' : 'error'"
              :text="isConnected ? 'Online' : 'Offline'"
            />
            <a-button type="text" size="small" @click="clearMessages">
              <template #icon>
                <icon-delete />
              </template>
            </a-button>
          </a-space>
        </div>
      </template>

      <!-- Messages Container -->
      <div class="messages-container" ref="messagesContainer">
        <div
          v-for="msg in messages"
          :key="msg.id"
          :class="['message', msg.role]"
        >
          <div class="message-wrapper">
            <div class="avatar">
              {{ msg.role === 'user' ? '👤' : '🤖' }}
            </div>
            <div class="content">
              <div class="text">{{ msg.content }}</div>
              <div v-if="msg.sources" class="sources">
                <a-divider style="margin: 8px 0" />
                <div class="sources-label">📊 Nguồn dữ liệu:</div>
                <pre>{{ msg.sources }}</pre>
              </div>
              <div class="timestamp">{{ msg.timestamp }}</div>
            </div>
          </div>
        </div>

        <!-- Loading indicator -->
        <div v-if="loading" class="message assistant">
          <div class="message-wrapper">
            <div class="avatar">🤖</div>
            <div class="content">
              <a-spin :size="16" /> Đang suy nghĩ...
            </div>
          </div>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="quick-actions">
        <div class="quick-actions-label">💡 Gợi ý nhanh:</div>
        <a-space wrap :size="8">
          <a-button
            v-for="action in quickActions"
            :key="action.id"
            size="small"
            type="outline"
            @click="askQuestion(action.question)"
          >
            {{ action.icon }} {{ action.label }}
          </a-button>
        </a-space>
      </div>

      <!-- Input Box -->
      <div class="input-container">
        <a-input-search
          v-model="input"
          placeholder="Hỏi AI bất cứ điều gì về hệ thống..."
          :loading="loading"
          :disabled="!isConnected"
          allow-clear
          @search="sendMessage"
          @press-enter="sendMessage"
        >
          <template #button-icon>
            <icon-send />
          </template>
          <template #button-default>Gửi</template>
        </a-input-search>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { chatWithAI, checkAIHealth } from '@/api/ai'
import { Message } from '@arco-design/web-vue'
import { IconSend, IconDelete } from '@arco-design/web-vue/es/icon'

interface ChatMessage {
  id: number
  role: 'user' | 'assistant'
  content: string
  sources?: string
  timestamp: string
}

interface QuickAction {
  id: string
  label: string
  icon: string
  question: string
}

// State
const messages = ref<ChatMessage[]>([
  {
    id: 0,
    role: 'assistant',
    content:
      'Xin chào! Tôi là trợ lý AI của GearUp. Tôi có thể giúp bạn tra cứu thông tin về sản phẩm, doanh thu, tồn kho và nhiều thứ khác. Bạn cần giúp gì không? 😊',
    timestamp: new Date().toLocaleTimeString('vi-VN')
  }
])

const input = ref('')
const loading = ref(false)
const isConnected = ref(false)
const messagesContainer = ref<HTMLElement | null>(null)

const quickActions: QuickAction[] = [
  {
    id: 'top-products',
    label: 'Top sản phẩm',
    icon: '📊',
    question: 'Sản phẩm nào bán chạy nhất?'
  },
  {
    id: 'revenue',
    label: 'Doanh thu',
    icon: '💰',
    question: 'Doanh thu tháng này thế nào?'
  },
  {
    id: 'low-stock',
    label: 'Tồn kho thấp',
    icon: '⚠️',
    question: 'Sản phẩm nào sắp hết hàng?'
  },
  {
    id: 'customers',
    label: 'Khách hàng',
    icon: '👥',
    question: 'Có bao nhiêu khách hàng đang hoạt động?'
  }
]

// Methods
async function checkConnection() {
  try {
    await checkAIHealth()
    isConnected.value = true
  } catch (error) {
    isConnected.value = false
    console.error('AI service connection failed:', error)
  }
}

async function sendMessage(text: string = input.value) {
  if (!text.trim()) return
  if (!isConnected.value) {
    Message.error('AI service chưa kết nối. Vui lòng thử lại sau.')
    return
  }

  // Add user message
  const userMessage: ChatMessage = {
    id: Date.now(),
    role: 'user',
    content: text.trim(),
    timestamp: new Date().toLocaleTimeString('vi-VN')
  }
  messages.value.push(userMessage)

  // Clear input
  input.value = ''
  loading.value = true

  // Scroll to bottom
  await nextTick()
  scrollToBottom()

  try {
    const response = await chatWithAI(text)

    // Add AI response
    const aiMessage: ChatMessage = {
      id: Date.now() + 1,
      role: 'assistant',
      content: response.message,
      sources: response.sources || undefined,
      timestamp: new Date().toLocaleTimeString('vi-VN')
    }
    messages.value.push(aiMessage)

    // Scroll to bottom
    await nextTick()
    scrollToBottom()
  } catch (error: any) {
    Message.error('Lỗi khi gửi tin nhắn: ' + error.message)

    // Add error message
    messages.value.push({
      id: Date.now() + 1,
      role: 'assistant',
      content: '❌ Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.',
      timestamp: new Date().toLocaleTimeString('vi-VN')
    })
  } finally {
    loading.value = false
  }
}

function askQuestion(question: string) {
  sendMessage(question)
}

function clearMessages() {
  messages.value = [
    {
      id: 0,
      role: 'assistant',
      content: 'Đã xóa lịch sử chat. Tôi có thể giúp gì cho bạn? 🤖',
      timestamp: new Date().toLocaleTimeString('vi-VN')
    }
  ]
}

function scrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// Lifecycle
onMounted(async () => {
  await checkConnection()

  // Check connection every 30 seconds
  setInterval(checkConnection, 30000)
})
</script>

<style scoped lang="less">
.ai-chatbot {
  .chatbot-card {
    height: 600px;
    display: flex;
    flex-direction: column;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    :deep(.arco-card-body) {
      flex: 1;
      display: flex;
      flex-direction: column;
      padding: 16px;
      overflow: hidden;
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
    padding: 16px 0;
    margin-bottom: 16px;

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

        .content {
          background: #165dff;
          color: white;
          margin-right: 8px;
          margin-left: 0;
        }
      }
    }

    &.assistant {
      .content {
        background: #f5f5f5;
        color: #333;
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
  }

  .content {
    padding: 10px 14px;
    border-radius: 12px;
    max-width: 70%;
    word-wrap: break-word;
    margin-left: 8px;

    .text {
      line-height: 1.6;
      white-space: pre-wrap;
    }

    .sources {
      margin-top: 8px;
      font-size: 12px;
      opacity: 0.8;

      .sources-label {
        font-weight: 600;
        margin-bottom: 4px;
      }

      pre {
        background: rgba(0, 0, 0, 0.05);
        padding: 8px;
        border-radius: 4px;
        white-space: pre-wrap;
        font-size: 11px;
        line-height: 1.4;
        margin: 4px 0 0 0;
      }
    }

    .timestamp {
      margin-top: 4px;
      font-size: 11px;
      opacity: 0.6;
    }
  }

  .quick-actions {
    margin-bottom: 12px;
    padding: 12px;
    background: #f7f8fa;
    border-radius: 8px;

    .quick-actions-label {
      font-size: 12px;
      font-weight: 600;
      margin-bottom: 8px;
      color: #666;
    }
  }

  .input-container {
    :deep(.arco-input-search) {
      .arco-input-wrapper {
        border-radius: 20px;
      }

      .arco-input-append {
        border-radius: 0 20px 20px 0;
      }
    }
  }
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
</style>
