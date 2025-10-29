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
        <!-- Chat History Card -->
        <a-card title="Lịch sử chat" :bordered="false" style="margin-bottom: 16px">
          <div v-if="sortedSessions.length === 0" class="empty-history">
            <a-empty description="Chưa có lịch sử chat" :image="false">
              <template #description>
                <span style="color: #86909c; font-size: 14px">
                  Tạo cuộc trò chuyện mới để bắt đầu
                </span>
              </template>
            </a-empty>
          </div>
          
          <div v-else class="chat-history">
            <a-list
              :bordered="false"
              :split="false"
              size="small"
              :data="sortedSessions"
            >
              <template #item="{ item }">
                <a-list-item
                  :class="['session-item', { active: item.id === currentSessionId }]"
                  @click="onSwitchSession(item.id)"
                >
                  <a-list-item-meta>
                    <template #title>
                      <div class="session-title">
                        <span class="session-name">{{ item.name }}</span>
                        <a-tag
                          v-if="item.id === currentSessionId"
                          size="small"
                          color="blue"
                        >
                          Đang chọn
                        </a-tag>
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
            <strong>GearUp AI</strong> là trợ lý thông minh giúp bạn tra cứu thông tin nhanh
            chóng trong hệ thống GearUp.
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
                <template #description>
                  Sử dụng các nút "Gợi ý nhanh" để truy vấn thông tin phổ biến
                </template>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta>
                <template #description>
                  Hỏi bằng ngôn ngữ tự nhiên, ví dụ: "Giày nào bán nhiều nhất?"
                </template>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta>
                <template #description>
                  AI sẽ truy vấn dữ liệu thực tế từ hệ thống và trả lời chính xác
                </template>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta>
                <template #description>
                  Dữ liệu được cache để tối ưu tốc độ phản hồi
                </template>
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
import { IconPlus, IconDelete } from '@arco-design/web-vue/es/icon'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import AIChatbot from '@/components/ai/AIChatbot.vue'
import useBreadcrumb from '@/hooks/breadcrumb'

const { breadcrumbItems } = useBreadcrumb()

// Reference to the chatbot component
const chatbotRef = ref<InstanceType<typeof AIChatbot> | null>(null)

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
  const list = ids.map(sessionId => ({
    id: sessionId,
    name: sessionNames.value[sessionId] || 'Cuộc trò chuyện mới',
    messageCount: sessions.value[sessionId]?.length || 0,
    timestamp: parseInt(sessionId.split('_')[1], 10) || 0
  }))

  // If nothing is persisted yet but we have an active session, show it
  if (list.length === 0 && currentSessionId.value) {
    const sid = currentSessionId.value
    list.push({
      id: sid,
      name: sessionNames.value[sid] || 'Cuộc trò chuyện mới',
      messageCount: 0,
      timestamp: parseInt((sid.split('_')[1] || '0'), 10) || Date.now()
    })
  }

  return list.sort((a, b) => b.timestamp - a.timestamp)
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
        color: #4e5969;
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

      &:hover {
        background-color: #f7f8fa;
        border-color: #e5e6eb;
      }

      &.active {
        background-color: #e8f4ff;
        border-color: #165dff;
        box-shadow: 0 2px 8px rgba(22, 93, 255, 0.1);
      }

      .session-title {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 4px;

        .session-name {
          font-weight: 500;
          color: #1d2129;
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
        color: #86909c;

        .session-time {
          font-weight: 400;
        }

        .message-count {
          background-color: #f2f3f5;
          padding: 2px 6px;
          border-radius: 4px;
          font-weight: 500;
        }
      }
    }
  }
}
</style>
