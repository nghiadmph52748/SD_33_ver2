<template>
  <a-card class="general-card" :title="$t('userInfo.title.latestNotification')">
    <a-skeleton v-if="notificationStore.loading" :animation="true">
      <a-skeleton-line :rows="3" />
    </a-skeleton>

    <div v-else-if="latestNotifications.length > 0" class="notifications-list">
      <div
        v-for="notification in latestNotifications"
        :key="notification.id"
        class="notification-item"
        :class="{ unread: notification.status === 0 }"
        @click="handleNotificationClick(notification)"
      >
        <div class="notification-header">
          <div class="notification-title">
            <icon-notification v-if="notification.type === 'notice'" class="icon" />
            <icon-message v-else-if="notification.type === 'message'" class="icon" />
            <icon-calendar-clock v-else class="icon" />
            <span class="title-text">{{ notification.title }}</span>
          </div>
          <span class="notification-time">{{ formatTime(notification.time) }}</span>
        </div>
        <div class="notification-content">
          <div v-if="notification.subTitle" class="subtitle">{{ notification.subTitle }}</div>
          <div class="content">{{ notification.content }}</div>
        </div>
      </div>

      <a-divider />

      <div class="view-all">
        <a-link @click="viewAllNotifications">
          {{ $t('userInfo.viewAll') || 'Xem tất cả' }}
          <icon-right />
        </a-link>
      </div>
    </div>

    <a-result v-else status="404">
      <template #subtitle>
        {{ $t('userInfo.nodata') }}
      </template>
    </a-result>
  </a-card>
</template>

<script lang="ts" setup>
import { computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import useNotificationStore from '@/store/modules/notification'
import type { MessageRecord } from '@/api/message'

const router = useRouter()
const notificationStore = useNotificationStore()

// Get latest 5 notifications (sorted by time desc)
const latestNotifications = computed(() => {
  return [...notificationStore.messages]
    .sort((a, b) => new Date(b.time).getTime() - new Date(a.time).getTime())
    .slice(0, 5)
})

// Format time to relative format
const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return 'Vừa xong'
  if (minutes < 60) return `${minutes} phút trước`
  if (hours < 24) return `${hours} giờ trước`
  if (days < 7) return `${days} ngày trước`

  return date.toLocaleDateString('vi-VN')
}

const handleNotificationClick = async (notification: MessageRecord) => {
  // Mark as read if unread
  if (notification.status === 0) {
    await notificationStore.markAsRead([notification.id])
  }

  // Navigate based on notification type
  if (notification.type === 'message') {
    router.push('/chat')
    return
  }

  // default: go to user info page
  router.push('/user/info')
}

const viewAllNotifications = () => {
  router.push('/user/info')
}

// Fetch notifications and connect WebSocket on mount
onMounted(() => {
  notificationStore.fetchNotifications()
  notificationStore.connectWebSocket()
})

// Disconnect WebSocket on unmount
onUnmounted(() => {
  notificationStore.disconnectWebSocket()
})
</script>

<style lang="less" scoped>
:deep(.arco-result) {
  padding: 40px 32px 108px;
}

.notifications-list {
  .notification-item {
    padding: 12px 0;
    border-bottom: 1px solid var(--color-border-2);
    cursor: pointer;
    transition: background-color 0.2s;

    &:hover {
      background-color: var(--color-fill-2);
      border-radius: 4px;
      padding-left: 8px;
      padding-right: 8px;
    }

    &.unread {
      background-color: var(--color-primary-light-1);
      border-left: 3px solid rgb(var(--primary-6));
      padding-left: 8px;
    }

    &:last-child {
      border-bottom: none;
    }

    .notification-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .notification-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: 500;
        color: var(--color-text-1);

        .icon {
          font-size: 16px;
          color: rgb(var(--primary-6));
        }

        .title-text {
          font-size: 14px;
        }
      }

      .notification-time {
        font-size: 12px;
        color: var(--color-text-3);
        white-space: nowrap;
      }
    }

    .notification-content {
      padding-left: 24px;

      .subtitle {
        font-size: 12px;
        color: var(--color-text-2);
        margin-bottom: 4px;
      }

      .content {
        font-size: 13px;
        color: var(--color-text-2);
        line-height: 1.5;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
      }
    }
  }

  .view-all {
    text-align: center;
    padding: 12px 0 4px;

    :deep(.arco-link) {
      font-size: 14px;
      display: inline-flex;
      align-items: center;
      gap: 4px;
    }
  }
}
</style>
