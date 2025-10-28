<template>
  <div class="ai-chatbot">
    <a-card class="chatbot-card" :bordered="false">
      <template #title>
        <div class="chatbot-header">
          <span class="title">🤖 Trợ Lý AI</span>
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
              <div class="text" v-html="renderMarkdown(msg.content)"></div>
              <div v-if="msg.sources" class="sources">
                <a-divider style="margin: 8px 0" />
                <div class="sources-label">📊 Nguồn dữ liệu:</div>
                <div class="sources-content" v-html="renderMarkdown(msg.sources)"></div>
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
          search-button
          @search="handleSearch"
          @press-enter="handleSearch"
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
import { ref, onMounted, nextTick, watch } from 'vue'
import { parse as markedParse } from 'marked'
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

const STORAGE_KEY = 'gearup_ai_chat_history_v1'

function saveHistory() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(messages.value))
  } catch {
    // ignore storage errors
  }
}

function loadHistory() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      const parsed: ChatMessage[] = JSON.parse(raw)
      if (Array.isArray(parsed) && parsed.length > 0) {
        messages.value = parsed
      }
    }
  } catch {
    // ignore parse errors
  }
}

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
    id: 'order-status',
    label: 'Trạng thái đơn',
    icon: '📋',
    question: 'Trạng thái đơn hàng thế nào?'
  },
  {
    id: 'top-customers',
    label: 'Top khách hàng',
    icon: '👥',
    question: 'Khách hàng nào chi tiêu nhiều nhất?'
  },
  {
    id: 'discounts',
    label: 'Đợt giảm giá',
    icon: '🎉',
    question: 'Đợt giảm giá nào đang hoạt động?'
  },
  {
    id: 'employees',
    label: 'Nhân viên',
    icon: '👨‍💼',
    question: 'Nhân viên nào bán hàng tốt nhất?'
  },
  {
    id: 'channel',
    label: 'Kênh bán hàng',
    icon: '🛒',
    question: 'Bán online hay tại quầy nhiều hơn?'
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

function handleSearch(value?: string) {
  // Use current input if value is undefined (e.g., press-enter)
  const text = typeof value === 'string' ? value : input.value
  sendMessage(text)
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

  // Persist
  saveHistory()

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

    // Persist
    saveHistory()
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
  saveHistory()
}

function scrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// Lifecycle
onMounted(async () => {
  // Restore history first
  loadHistory()
  await checkConnection()

  // Check connection every 30 seconds
  setInterval(checkConnection, 30000)
})

// Auto-persist on change (debounced by microtask via watch)
watch(
  messages,
  () => {
    saveHistory()
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
    padding: 16px;
    margin-bottom: 16px;
    background: linear-gradient(180deg, #f8fafc 0%, #ffffff 100%);
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
        background: #ffffff;
        color: #1d2129;
        border: 1px solid #e5e6eb;
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
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .content {
    padding: 10px 12px;
    border-radius: 14px;
    max-width: 72%;
    word-wrap: break-word;
    margin-left: 8px;

    .text {
      line-height: 1.7;
      white-space: normal;

      h1, h2, h3 {
        margin: 0 0 8px 0;
        font-weight: 600;
      }

      table {
        width: 100%;
        border-collapse: collapse;
        margin: 6px 0 2px 0;
      }

      th, td {
        border: 1px solid #e5e6eb;
        padding: 6px 8px;
        text-align: left;
      }

      th {
        background: #f2f3f5;
        font-weight: 600;
      }
      code, pre code {
        font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
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
