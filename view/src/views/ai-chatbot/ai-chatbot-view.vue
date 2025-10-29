<template>
  <div class="ai-chatbot-page">
    <Breadcrumb :items="breadcrumbItems" />

    <a-row :gutter="16">
      <a-col :span="16">
        <AIChatbot ref="chatbotRef" />
      </a-col>

      <a-col :span="8">
        <!-- Chat History Card -->
        <a-card title="📚 Lịch sử chat" :bordered="false" style="margin-bottom: 16px">
          <a-space direction="vertical" :size="8" style="width: 100%">
            <a-alert v-if="Object.keys(sessions).length <= 1" type="normal" :closable="false">
              Tạo nhiều cuộc trò chuyện để xuất hiện ở đây
            </a-alert>
            <a-space wrap :size="8">
              <a-button
                v-for="(session, sessionId) in sessions"
                :key="sessionId"
                size="small"
                :type="sessionId === currentSessionId ? 'primary' : 'outline'"
                @click="onSwitchSession(sessionId)"
              >
                {{ sessionNames[sessionId] || formatSessionTime(sessionId) }}
              </a-button>
            </a-space>
          </a-space>
        </a-card>

        <!-- Quick Actions Card -->
        <a-card title="🚀 Hành động nhanh" :bordered="false" style="margin-bottom: 16px">
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
        <a-card title="ℹ️ Giới thiệu" :bordered="false" style="margin-bottom: 16px">
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
import { ref, computed } from 'vue'
import { Message } from '@arco-design/web-vue'
import { IconPlus, IconDelete } from '@arco-design/web-vue/es/icon'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import AIChatbot from '@/components/ai/AIChatbot.vue'
import useBreadcrumb from '@/hooks/breadcrumb'

const { breadcrumbItems } = useBreadcrumb()

// Reference to the chatbot component
const chatbotRef = ref<InstanceType<typeof AIChatbot> | null>(null)

// Mirror child state for template
const sessions = computed<Record<string, any>>(() => chatbotRef.value?.chatSessions || {})
const currentSessionId = computed<string | ''>(() => chatbotRef.value?.currentSessionId || '')
const sessionNames = computed<Record<string, string>>(() => chatbotRef.value?.sessionNames || {})

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
}
</style>
