<template>
  <div class="ai-chatbot-page">
    <Breadcrumb :items="breadcrumbItems" />

    <a-row :gutter="16">
      <a-col :span="16">
        <div class="chatbot-wrap">
          <AIChatbot ref="chatbotRef" @session-state="onSessionState" :enable-health-check="true" />
        </div>
      </a-col>

      <a-col :span="8">
        <!-- Context Settings Card -->
        <a-card title="⚙️ Cài đặt ngữ cảnh" :bordered="false" style="margin-bottom: 16px">
          <a-form :model="contextSettings" layout="vertical" size="small">
            <a-form-item label="Phạm vi thời gian" field="timeRange">
              <a-select v-model="contextSettings.timeRange" placeholder="Chọn phạm vi">
                <a-option value="today">Hôm nay</a-option>
                <a-option value="7_days">7 ngày qua</a-option>
                <a-option value="30_days">30 ngày qua</a-option>
                <a-option value="90_days">90 ngày qua</a-option>
                <a-option value="custom">Tùy chỉnh</a-option>
              </a-select>
            </a-form-item>

            <a-form-item label="Kênh bán hàng" field="channel">
              <a-select v-model="contextSettings.channel" placeholder="Chọn kênh">
                <a-option value="all">Tất cả</a-option>
                <a-option value="online">Online</a-option>
                <a-option value="pos">Tại quầy</a-option>
              </a-select>
            </a-form-item>

            <a-space>
              <a-button type="primary" size="small" @click="applyContextSettings">
                <template #icon>
                  <icon-check />
                </template>
                Áp dụng
              </a-button>
              <a-button size="small" @click="resetContextSettings">
                <template #icon>
                  <icon-refresh />
                </template>
                Đặt lại
              </a-button>
            </a-space>
          </a-form>
        </a-card>

        <!-- Chat History Card -->
        <a-card title="Lịch sử chat" :bordered="false" style="margin-bottom: 16px">
          <!-- Search box -->
          <div style="margin-bottom: 12px">
            <a-input-search v-model="searchQuery" placeholder="Tìm kiếm trong lịch sử..." allow-clear size="small" />
          </div>
          <div v-if="filteredSessions.length === 0" class="empty-history">
            <a-empty description="Chưa có lịch sử chat" :image="false">
              <template #description>
                <span style="color: #86909c; font-size: 14px">Tạo cuộc trò chuyện mới để bắt đầu</span>
              </template>
            </a-empty>
          </div>

          <div v-else class="chat-history">
            <a-list :bordered="false" :split="false" size="small" :data="filteredSessions">
              <template #item="{ item }">
                <a-list-item :class="['session-item', { active: item.id === currentSessionId }]" @click="onSwitchSession(item.id)">
                  <a-list-item-meta>
                    <template #title>
                      <div class="session-title">
                        <span class="session-name">{{ item.name }}</span>
                        <a-tag v-if="item.id === currentSessionId" size="small" color="blue">Đang chọn</a-tag>
                      </div>
                    </template>
                    <template #description>
                      <div class="session-info">
                        <span class="session-time">{{ formatSessionTime(item.id) }}</span>
                        <span class="message-count">{{ item.messageCount }} tin nhắn</span>
                      </div>
                    </template>
                  </a-list-item-meta>
                </a-list-item>
              </template>
            </a-list>
          </div>
        </a-card>

        <!-- Quick Actions Card -->
        <a-card title="Hành động nhanh" :bordered="false" style="margin-bottom: 16px">
          <a-space direction="vertical" :size="12" style="width: 100%">
            <a-button type="primary" long @click="createNewChat">
              <template #icon>
                <icon-plus />
              </template>
              Tạo cuộc trò chuyện mới
            </a-button>
            <a-button type="outline" long @click="clearChatHistory">
              <template #icon>
                <icon-delete />
              </template>
              Xóa lịch sử chat
            </a-button>
          </a-space>
        </a-card>

        <!-- Info Card -->
        <a-card title="Giới thiệu" :bordered="false" style="margin-bottom: 16px">
          <p>
            <strong>GearUp AI</strong>
            là trợ lý thông minh giúp bạn tra cứu thông tin nhanh chóng trong hệ thống GearUp.
          </p>
          <a-divider />
          <div class="feature-list">
            <div class="feature-item">
              <span class="icon">📊</span>
              <span>Tra cứu sản phẩm bán chạy</span>
            </div>
            <div class="feature-item">
              <span class="icon">💰</span>
              <span>Thống kê doanh thu</span>
            </div>
            <div class="feature-item">
              <span class="icon">⚠️</span>
              <span>Cảnh báo tồn kho thấp</span>
            </div>
            <div class="feature-item">
              <span class="icon">📋</span>
              <span>Trạng thái đơn hàng</span>
            </div>
            <div class="feature-item">
              <span class="icon">👥</span>
              <span>Top khách hàng chi tiêu</span>
            </div>
            <div class="feature-item">
              <span class="icon">🎉</span>
              <span>Đợt giảm giá hoạt động</span>
            </div>
            <div class="feature-item">
              <span class="icon">👨‍💼</span>
              <span>Hiệu suất nhân viên</span>
            </div>
            <div class="feature-item">
              <span class="icon">🛒</span>
              <span>Phân bố kênh bán hàng</span>
            </div>
          </div>
        </a-card>

        <!-- Tips Card -->
        <a-card title="💡 Mẹo sử dụng" :bordered="false">
          <a-list :bordered="false" size="small">
            <a-list-item>
              <a-list-item-meta>
                <template #description>Sử dụng các nút "Gợi ý nhanh" để truy vấn thông tin phổ biến</template>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta>
                <template #description>Hỏi bằng ngôn ngữ tự nhiên, ví dụ: "Giày nào bán nhiều nhất?"</template>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta>
                <template #description>AI sẽ truy vấn dữ liệu thực tế từ hệ thống và trả lời chính xác</template>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta>
                <template #description>Dữ liệu được cache để tối ưu tốc độ phản hồi</template>
              </a-list-item-meta>
            </a-list-item>
          </a-list>

          <a-divider />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watchEffect } from 'vue'
import { Message } from '@arco-design/web-vue'
import { IconPlus, IconDelete, IconCheck, IconRefresh } from '@arco-design/web-vue/es/icon'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import AIChatbot from '@/components/ai/AIChatbot.vue'
import useBreadcrumb from '@/hooks/breadcrumb'

const { breadcrumbItems } = useBreadcrumb()

// Reference to the chatbot component
const chatbotRef = ref<InstanceType<typeof AIChatbot> | null>(null)

// Context settings
const contextSettings = ref({
  timeRange: '30_days',
  channel: 'all',
})

// Search query
const searchQuery = ref('')

// Mirror child state for template by subscribing to child's refs
const sessions = ref<Record<string, any>>({})
const currentSessionId = ref<string | ''>('')
const sessionNames = ref<Record<string, string>>({})

watchEffect(() => {
  const child: any = chatbotRef.value
  sessions.value = child?.chatSessions?.value || {}
  currentSessionId.value = child?.currentSessionId?.value || ''
  sessionNames.value = child?.sessionNames?.value || {}
})

function onSessionState(payload: { sessions: Record<string, any>; currentSessionId: string; sessionNames: Record<string, string> }) {
  sessions.value = payload.sessions || {}
  currentSessionId.value = payload.currentSessionId || ''
  sessionNames.value = payload.sessionNames || {}
}

// Sorted sessions for display
const sortedSessions = computed(() => {
  const ids = Object.keys(sessions.value)
  const list = ids.map((sessionId) => ({
    id: sessionId,
    name: sessionNames.value[sessionId] || 'Cuộc trò chuyện mới',
    messageCount: sessions.value[sessionId]?.length || 0,
    timestamp: parseInt(sessionId.split('_')[1], 10) || 0,
  }))

  // If nothing is persisted yet but we have an active session, show it
  if (list.length === 0 && currentSessionId.value) {
    const sid = currentSessionId.value
    list.push({
      id: sid,
      name: sessionNames.value[sid] || 'Cuộc trò chuyện mới',
      messageCount: 0,
      timestamp: parseInt(sid.split('_')[1] || '0', 10) || Date.now(),
    })
  }

  return list.sort((a, b) => b.timestamp - a.timestamp)
})

// Filtered sessions based on search query
const filteredSessions = computed(() => {
  if (!searchQuery.value.trim()) {
    return sortedSessions.value
  }

  const query = searchQuery.value.toLowerCase()
  return sortedSessions.value.filter((session) => session.name.toLowerCase().includes(query))
})

function onSwitchSession(sessionId: string) {
  if (chatbotRef.value && typeof (chatbotRef.value as any).switchToSession === 'function') {
    ;(chatbotRef.value as any).switchToSession(sessionId)
  }
}

function formatSessionTime(sessionId: string): string {
  try {
    const parts = sessionId.split('_')
    const ts = parseInt(parts[1])
    const date = new Date(ts)
    const now = new Date()
    const diffHours = (now.getTime() - date.getTime()) / (1000 * 60 * 60)
    if (diffHours < 1) return 'Vừa xong'
    if (diffHours < 24) return `${Math.floor(diffHours)}h trước`
    return date.toLocaleDateString('vi-VN')
  } catch {
    return 'Chat cũ'
  }
}

function createNewChat() {
  if (chatbotRef.value) {
    chatbotRef.value.createNewChat()
  }
}

function clearChatHistory() {
  if (chatbotRef.value) {
    chatbotRef.value.clearMessages()
  }
}

function applyContextSettings() {
  Message.success(
    `Đã áp dụng: ${contextSettings.value.timeRange === '30_days' ? '30 ngày qua' : contextSettings.value.timeRange}, Kênh: ${contextSettings.value.channel === 'all' ? 'Tất cả' : contextSettings.value.channel}`
  )
  // TODO: Gửi context settings cho AI service khi query
}

function resetContextSettings() {
  contextSettings.value = {
    timeRange: '30_days',
    channel: 'all',
  }
  Message.info('Đã đặt lại cài đặt mặc định')
}
</script>

<style scoped lang="less">
.ai-chatbot-page {
  padding: 20px;

  // Make left side chatbot fill viewport height so its input can stay pinned
  .chatbot-wrap {
    height: calc(100vh - 140px);
    min-height: 520px;
    display: flex;
  }

  .feature-list {
    .feature-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 8px 0;

      .icon {
        font-size: 20px;
      }

      span:not(.icon) {
        font-size: 14px;
        color: var(--color-text-2);
      }
    }
  }

  .tech-info {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }

  // Chat history styles
  .empty-history {
    padding: 20px 0;
    text-align: center;
  }

  .chat-history {
    .session-item {
      padding: 12px 16px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s ease;
      border: 1px solid transparent;
      margin-bottom: 4px;

      // Light mode hover
      &:hover {
        background-color: var(--color-fill-2);
        border-color: var(--color-border-2);
      }

      // Active state - different for light/dark
      &.active {
        background-color: rgb(var(--primary-1));
        border-color: rgb(var(--primary-6));
        box-shadow: 0 2px 8px rgba(var(--primary-6), 0.15);
      }

      .session-title {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 4px;

        .session-name {
          font-weight: 500;
          color: var(--color-text-1);
          font-size: 14px;
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          margin-right: 8px;
        }
      }

      .session-info {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 12px;
        color: var(--color-text-3);

        .session-time {
          font-weight: 400;
        }

        .message-count {
          background-color: var(--color-fill-3);
          color: var(--color-text-2);
          padding: 2px 6px;
          border-radius: 4px;
          font-weight: 500;
        }
      }
    }
  }
}

/* ========================================= */
/* DARK MODE SPECIFIC OVERRIDES */
/* ========================================= */
body[arco-theme='dark'] {
  .ai-chatbot-page {
    .chat-history {
      .session-item {
        // Dark mode hover - subtle overlay
        &:hover {
          background-color: var(--color-fill-2);
          border-color: var(--color-border-2);
        }

        // Active state in dark mode - brighter blue
        &.active {
          background-color: rgba(var(--primary-6), 0.2);
          border-color: rgb(var(--primary-5));
          box-shadow: 0 2px 8px rgba(var(--primary-5), 0.2);
        }
      }
    }

    .feature-list {
      .feature-item {
        span:not(.icon) {
          color: var(--color-text-2);
        }
      }
    }
  }
}
</style>
