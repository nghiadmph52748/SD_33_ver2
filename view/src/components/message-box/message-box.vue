<template>
  <div class="notification-box">
    <a-spin :loading="loading" style="width: 100%">
      <!-- Header -->
      <div class="notification-header">
        <div class="header-left">
          <icon-notification class="header-icon" />
          <span class="header-title">Thông báo</span>
          <a-badge
            v-if="notificationStore.totalUnread > 0"
            :count="notificationStore.totalUnread"
            :max-count="99"
            class="header-badge"
          />
        </div>
        <a-space :size="4">
          <a-tooltip content="Đánh dấu tất cả đã đọc">
            <a-button
              type="text"
              size="small"
              :disabled="notificationStore.totalUnread === 0"
              @click="markAll"
            >
              <template #icon>
                <icon-check-circle />
              </template>
            </a-button>
          </a-tooltip>
          <a-tooltip content="Xóa tất cả">
            <a-button
              type="text"
              size="small"
              status="danger"
              :disabled="!renderList.length"
              @click="confirmClearAll"
            >
              <template #icon>
                <icon-delete />
              </template>
            </a-button>
          </a-tooltip>
        </a-space>
      </div>

      <!-- Tabs -->
      <a-tabs
        v-model:active-key="messageType"
        type="line"
        size="small"
        class="notification-tabs"
        destroy-on-hide
      >
        <a-tab-pane v-for="item in tabList" :key="item.key">
          <template #title>
            <a-space :size="4">
              <component :is="item.icon" v-if="item.icon" />
              <span>{{ item.title }}</span>
              <a-badge
                v-if="unreadCountByType(item.key) > 0"
                :count="unreadCountByType(item.key)"
                :max-count="99"
                :offset="[6, -2]"
              />
            </a-space>
          </template>

          <!-- Empty State -->
          <div v-if="!renderList.length" class="empty-state">
            <a-empty :description="$t('messageBox.noContent')">
              <template #image>
                <icon-folder-delete :style="{ fontSize: '48px', color: 'var(--color-text-4)' }" />
              </template>
            </a-empty>
          </div>

          <!-- List -->
          <List
            v-else
            :render-list="renderList"
            :unread-count="unreadCount"
            @item-click="handleItemClick"
            @more="openAll"
          />
        </a-tab-pane>
      </a-tabs>
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
import { MessageListType } from '@/api/message'
import useNotificationStore from '@/store/modules/notification'
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Modal, Message } from '@arco-design/web-vue'
import List from './list.vue'

interface TabItem {
  key: string
  title: string
  icon?: any
}

const messageType = ref('message')
const { t } = useI18n()
const notificationStore = useNotificationStore()

const tabList: TabItem[] = [
  {
    key: 'message',
    title: t('messageBox.tab.title.message'),
    icon: 'icon-message',
  },
  {
    key: 'notice',
    title: t('messageBox.tab.title.notice'),
    icon: 'icon-notification',
  },
  {
    key: 'todo',
    title: t('messageBox.tab.title.todo'),
    icon: 'icon-calendar-clock',
  },
]

// Computed values from store
const loading = computed(() => notificationStore.loading)
const renderList = computed(() => notificationStore.messagesByType(messageType.value))
const unreadCount = computed(() => {
  return renderList.value.filter((item) => !item.status).length
})

const unreadCountByType = (type: string) => {
  return notificationStore.unreadCountByType(type)
}

const handleItemClick = async (items: MessageListType) => {
  if (items.length > 0) {
    const ids = items.map((item) => item.id)
    await notificationStore.markAsRead(ids)
  }
}

const confirmClearAll = () => {
  Modal.warning({
    title: 'Xác nhận xóa',
    content: 'Bạn có chắc chắn muốn xóa tất cả thông báo?',
    okText: 'Xóa',
    cancelText: 'Hủy',
    onOk: () => {
      notificationStore.clearAll()
      Message.success('Đã xóa tất cả thông báo')
    },
  })
}

const markAll = async () => {
  try {
    await notificationStore.markAllAsRead()
    Message.success('Đã đánh dấu tất cả là đã đọc')
  } catch (error) {
    Message.error('Không thể đánh dấu thông báo')
  }
}

const openAll = () => {
  // navigate to a notifications center if exists; fallback to messages page
  window.location.hash = '#/notifications'
}

// Fetch notifications and connect WebSocket on mount
onMounted(() => {
  notificationStore.fetchNotifications()
  notificationStore.connectWebSocket()
})
</script>

<style scoped lang="less">
.notification-box {
  width: 420px;
  max-height: 600px;
  display: flex;
  flex-direction: column;
  background: var(--color-bg-2);
}

.notification-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px 12px;
  border-bottom: 1px solid var(--color-border-2);

  .header-left {
    display: flex;
    align-items: center;
    gap: 8px;

    .header-icon {
      font-size: 18px;
      color: rgb(var(--primary-6));
    }

    .header-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--color-text-1);
    }

    .header-badge {
      :deep(.arco-badge-number) {
        background: rgb(var(--primary-6));
      }
    }
  }
}

.notification-tabs {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  :deep(.arco-tabs-nav) {
    padding: 0 16px;
    margin: 0;
  }

  :deep(.arco-tabs-content) {
    flex: 1;
    overflow: hidden;
  }

  :deep(.arco-tabs-content-list) {
    height: 100%;
  }

  :deep(.arco-tabs-pane) {
    height: 100%;
    display: flex;
    flex-direction: column;
  }
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  min-height: 300px;

  :deep(.arco-empty-description) {
    color: var(--color-text-3);
    font-size: 14px;
  }
}

:deep(.arco-popover-popup-content) {
  padding: 0;
}
</style>
