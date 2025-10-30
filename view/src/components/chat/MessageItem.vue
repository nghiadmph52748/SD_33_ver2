<template>
  <div class="message-wrapper" :class="{ sent: isSent, received: !isSent }" :style="{ justifyContent: isSent ? 'flex-end' : 'flex-start' }">
    <div class="message-item">
      <!-- Avatar (chỉ hiển thị cho tin nhắn nhận) -->
      <a-avatar v-if="!isSent" :size="28" class="message-avatar">
        <icon-user />
      </a-avatar>

      <!-- Message bubble -->
      <div class="message-bubble">
        <!-- Sender name (chỉ hiện cho tin nhắn nhận) -->
        <div v-if="!isSent && showSenderName" class="message-sender">
          {{ message.senderName }}
        </div>

        <!-- Content -->
        <div class="message-content">
          {{ message.content }}
        </div>

        <!-- Timestamp and status -->
        <div class="message-meta">
          <span class="message-time">{{ formatTime(message.sentAt) }}</span>
          <!-- Read status (chỉ hiện cho tin nhắn gửi) -->
          <span v-if="isSent" class="message-status">
            <icon-check-circle-fill v-if="message.isRead" class="read-icon" />
            <icon-check-circle v-else class="sent-icon" />
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { IconUser, IconCheckCircle, IconCheckCircleFill } from '@arco-design/web-vue/es/icon'
import dayjs from 'dayjs'
import useUserStore from '@/store/modules/user'
import type { ChatMessage } from '@/api/chat'

interface Props {
  message: ChatMessage
  showSenderName?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showSenderName: false,
})

const userStore = useUserStore()

/**
 * Kiểm tra tin nhắn có phải do mình gửi không
 */
const isSent = computed(() => {
  return props.message.senderId === userStore.id
})

/**
 * Format thời gian
 */
function formatTime(timestamp: string): string {
  return dayjs(timestamp).format('HH:mm')
}
</script>

<style lang="less" scoped>
.message-wrapper {
  display: flex;
  margin-bottom: 12px;
  width: 100%;

  &.sent {
    justify-content: flex-end;

    .message-item {
      flex-direction: row-reverse;
    }
  }

  &.received {
    justify-content: flex-start;
  }

  &.sent {
    .message-bubble {
      background: rgb(var(--primary-6));
      color: #fff;
      border-radius: 18px 18px 4px 18px;

      .message-meta {
        flex-direction: row-reverse;

        .message-time {
          color: rgba(255, 255, 255, 0.8);
        }
      }
    }
  }

  &.received {
    .message-bubble {
      background: var(--color-fill-3);
      color: var(--color-text-1);
      border-radius: 18px 18px 18px 4px;
      border: 1px solid var(--color-border-2);

      .message-meta {
        .message-time {
          color: var(--color-text-3);
        }
      }
    }
  }
}

.message-item {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  max-width: 70%;

  .message-avatar {
    flex-shrink: 0;
  }

  .message-bubble {
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding: 8px 12px;
    min-width: 60px;
    max-width: 100%;
    word-wrap: break-word;

    .message-sender {
      font-size: 12px;
      font-weight: 500;
      opacity: 0.8;
      margin-bottom: 2px;
    }

    .message-content {
      font-size: 15px;
      line-height: 1.4;
      white-space: pre-wrap;
      word-break: break-word;
    }

    .message-meta {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 11px;
      margin-top: 4px;

      .message-time {
        flex-shrink: 0;
      }

      .message-status {
        display: flex;
        align-items: center;
        font-size: 14px;

        .read-icon {
          color: #fff;
        }

        .sent-icon {
          color: rgba(255, 255, 255, 0.6);
        }
      }
    }
  }
}
</style>
