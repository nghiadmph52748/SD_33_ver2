<template>
  <a-spin style="display: block" :loading="loading">
    <a-tabs v-model:activeKey="messageType" type="rounded" destroy-on-hide>
      <a-tab-pane v-for="item in tabList" :key="item.key">
        <template #title>
          <span>{{ item.title }}{{ formatUnreadLength(item.key) }}</span>
        </template>
        <a-result v-if="!renderList.length" status="404">
          <template #subtitle>{{ $t('messageBox.noContent') }}</template>
        </a-result>
        <List :render-list="renderList" :unread-count="unreadCount" @item-click="handleItemClick" />
      </a-tab-pane>
      <template #extra>
        <a-button type="text" @click="emptyList">
          {{ $t('messageBox.tab.button') }}
        </a-button>
      </template>
    </a-tabs>
  </a-spin>
</template>

<script lang="ts" setup>
import { MessageListType } from '@/api/message'
import useNotificationStore from '@/store/modules/notification'
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import List from './list.vue'

interface TabItem {
  key: string
  title: string
  avatar?: string
}

const messageType = ref('message')
const { t } = useI18n()
const notificationStore = useNotificationStore()

const tabList: TabItem[] = [
  {
    key: 'message',
    title: t('messageBox.tab.title.message'),
  },
  {
    key: 'notice',
    title: t('messageBox.tab.title.notice'),
  },
  {
    key: 'todo',
    title: t('messageBox.tab.title.todo'),
  },
]

// Computed values from store
const loading = computed(() => notificationStore.loading)
const renderList = computed(() => notificationStore.messagesByType(messageType.value))
const unreadCount = computed(() => {
  return renderList.value.filter((item) => !item.status).length
})

const formatUnreadLength = (type: string) => {
  const count = notificationStore.unreadCountByType(type)
  return count > 0 ? `(${count})` : ''
}

const handleItemClick = async (items: MessageListType) => {
  if (items.length > 0) {
    const ids = items.map((item) => item.id)
    await notificationStore.markAsRead(ids)
  }
}

const emptyList = () => {
  notificationStore.clearAll()
}

// Fetch notifications on mount
onMounted(() => {
  notificationStore.fetchNotifications()
})
</script>

<style scoped lang="less">
:deep(.arco-popover-popup-content) {
  padding: 0;
}

:deep(.arco-list-item-meta) {
  align-items: flex-start;
}
:deep(.arco-tabs-nav) {
  padding: 14px 0 12px 16px;
  border-bottom: 1px solid var(--color-neutral-3);
}
:deep(.arco-tabs-content) {
  padding-top: 0;
  .arco-result-subtitle {
    color: rgb(var(--gray-6));
  }
}
</style>
