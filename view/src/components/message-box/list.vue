<template>
  <div class="notification-list">
    <div class="list-content">
      <template v-for="group in grouped" :key="group.key">
        <div class="time-group">
          <div class="group-header">
            <span class="group-label">{{ group.label }}</span>
            <a-button v-if="hasUnread(group.items)" type="text" size="mini" @click="markGroup(group.items)">
              <template #icon>
                <icon-check />
              </template>
              Đánh dấu đã đọc
            </a-button>
          </div>

          <div class="group-items">
            <div
              v-for="item in group.items"
              :key="item.id"
              class="notification-item"
              :class="{ 'is-unread': !item.status }"
              @click="onItemClick(item)"
            >
              <div class="item-indicator">
                <span v-if="!item.status" class="unread-dot"></span>
              </div>

              <div class="item-avatar">
                <a-avatar :size="40">
                  <img v-if="item.avatar" :src="item.avatar" />
                  <component :is="getTypeIcon(item.messageType)" v-else />
                </a-avatar>
              </div>

              <div class="item-content">
                <div class="item-header">
                  <span class="item-title">{{ item.title }}</span>
                  <a-tag :color="getStatusColor(item.messageType)" size="small" class="item-status-tag">
                    {{ getStatusText(item.messageType) }}
                  </a-tag>
                </div>

                <div v-if="item.subTitle" class="item-subtitle">
                  {{ item.subTitle }}
                </div>

                <div class="item-description">
                  {{ item.content }}
                </div>

                <div class="item-footer">
                  <span class="item-time">
                    <icon-clock-circle />
                    {{ formatTime(item.time) }}
                  </span>
                  <a-space :size="8" class="item-actions">
                    <a-link :hoverable="false">
                      <icon-eye />
                      Xem
                    </a-link>
                    <a-link v-if="!item.status" :hoverable="false" @click.stop="onItemClick(item)">
                      <icon-check />
                      Đánh dấu
                    </a-link>
                  </a-space>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <div class="list-footer">
      <a-button type="text" long @click="allRead">
        <template #icon>
          <icon-check-circle />
        </template>
        {{ $t('messageBox.allRead') }}
      </a-button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { MessageListType, MessageRecord } from '@/api/message'
import { fetchInvoiceByCode } from '@/api/invoice'
import { mapApiErrorToMessage } from '@/utils/api-errors'
import { Message } from '@arco-design/web-vue'
import { PropType, computed, ref } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  renderList: {
    type: Array as PropType<MessageListType>,
    required: true,
  },
  unreadCount: {
    type: Number,
    default: 0,
  },
})
const emit = defineEmits(['itemClick', 'more'])
const router = useRouter()
const isNavigating = ref(false)

const allRead = () => {
  emit('itemClick', [...props.renderList])
}
const markGroup = (items: MessageListType) => {
  emit(
    'itemClick',
    items.filter((i: MessageRecord) => !i.status)
  )
}

const hasUnread = (items: MessageListType) => items.some((record: MessageRecord) => !record.status)

const onItemClick = async (item: MessageRecord) => {
  if (isNavigating.value) return
  isNavigating.value = true
  try {
    if (!item.status) {
      emit('itemClick', [item])
    }
    await navigateToOrderDetail(item)
  } finally {
    isNavigating.value = false
  }
}

function parseDate(s: string) {
  // Accept ISO or locale strings
  const iso = Date.parse(s)
  if (!Number.isNaN(iso)) return new Date(iso)
  // Try to sanitize dd/MM/yyyy HH:mm:ss
  const parts = s.match(/(\d{1,2})\/(\d{1,2})\/(\d{2,4})\s+(\d{1,2}:\d{2}:\d{2})/)
  if (parts) {
    const [_, d, m, y, t] = parts
    return new Date(`${y}-${m}-${d}T${t}`)
  }
  return new Date()
}

function getRelativeLabel(timeStr?: string) {
  try {
    if (!timeStr) return 'Khác'
    const d = parseDate(timeStr)
    const today = new Date()
    const diff = today.setHours(0, 0, 0, 0) - d.setHours(0, 0, 0, 0)
    const oneDay = 24 * 60 * 60 * 1000
    if (diff === 0) return 'Hôm nay'
    if (diff === oneDay) return 'Hôm qua'
    if (diff < 7 * oneDay) return 'Tuần này'
    return 'Cũ hơn'
  } catch {
    return 'Khác'
  }
}

function formatTime(timeStr?: string) {
  if (!timeStr) return ''
  try {
    const date = parseDate(timeStr)
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
  } catch {
    return timeStr
  }
}

function getTypeIcon(messageType?: number) {
  switch (messageType) {
    case 0:
      return 'icon-clock-circle'
    case 1:
      return 'icon-check-circle'
    case 2:
      // Đơn hàng đang tiến hành: dùng icon tĩnh, không animation
      return 'icon-clock-circle'
    case 3:
      return 'icon-exclamation-circle'
    default:
      return 'icon-notification'
  }
}

function getStatusColor(messageType?: number) {
  switch (messageType) {
    case 0:
      return 'gray'
    case 1:
      return 'green'
    case 2:
      return 'blue'
    case 3:
      return 'orangered'
    default:
      return 'arcoblue'
  }
}

function getStatusText(messageType?: number) {
  switch (messageType) {
    case 0:
      return 'Chưa bắt đầu'
    case 1:
      return 'Đã mở'
    case 2:
      return 'Tiến hành'
    case 3:
      return 'Cảnh báo'
    default:
      return 'Thông báo'
  }
}

// Group by relative day label
const grouped = computed(() => {
  const groups: Record<string, any> = {}
  const items = props.renderList || []

  // Use forEach instead of for...of to avoid iterator issues
  items.forEach((it: MessageRecord) => {
    const label = getRelativeLabel(it.time)
    if (!groups[label]) groups[label] = []
    groups[label].push(it)
  })

  return Object.keys(groups).map((k) => ({ key: k, label: k, items: groups[k] }))
})

function extractOrderCode(item: MessageRecord) {
  const source = `${item.title || ''} ${item.content || ''}`.trim()
  const match = source.match(/#?(HD\d+)/i)
  return match ? match[1].toUpperCase() : null
}

async function navigateToOrderDetail(item: MessageRecord) {
  const orderCode = extractOrderCode(item)
  if (!orderCode) {
    Message.warning('Không tìm thấy mã đơn hàng trong thông báo này')
    return
  }

  try {
    const invoice = await fetchInvoiceByCode(orderCode)
    if (!invoice?.id) {
      Message.warning('Không tìm thấy hóa đơn tương ứng')
      return
    }

    await router.push({
      name: 'HoaDonChiTiet',
      params: { id: invoice.id },
      query: { code: orderCode },
    })
  } catch (error) {
    const friendlyMessage = mapApiErrorToMessage(error, {
      defaultMessage: 'Không thể mở chi tiết hóa đơn. Vui lòng thử lại.',
    })
    Message.error(friendlyMessage)
  }
}
</script>

<style scoped lang="less">
.notification-list {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.list-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--color-fill-3);
    border-radius: 3px;

    &:hover {
      background: var(--color-fill-4);
    }
  }
}

.time-group {
  .group-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px 8px;
    position: sticky;
    top: 0;
    background: var(--color-bg-2);
    z-index: 1;

    .group-label {
      font-size: 12px;
      font-weight: 600;
      color: var(--color-text-3);
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
  }

  .group-items {
    padding: 0;
  }
}

.notification-item {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-bottom: 1px solid var(--color-border-2);
  position: relative;

  &:hover {
    background: var(--color-fill-2);
  }

  &.is-unread {
    background: var(--color-primary-light-1);

    &:hover {
      background: var(--color-primary-light-2);
    }

    .item-title {
      font-weight: 600;
    }
  }

  .item-indicator {
    display: flex;
    align-items: flex-start;
    padding-top: 16px;

    .unread-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: rgb(var(--primary-6));
      display: block;
    }
  }

  .item-avatar {
    flex-shrink: 0;
  }

  .item-content {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 6px;

    .item-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 8px;

      .item-title {
        font-size: 14px;
        color: var(--color-text-1);
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .item-status-tag {
        flex-shrink: 0;
      }
    }

    .item-subtitle {
      font-size: 12px;
      color: var(--color-text-3);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .item-description {
      font-size: 13px;
      color: var(--color-text-2);
      line-height: 1.5;
      display: -webkit-box;
      line-clamp: 2;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .item-footer {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-top: 2px;

      .item-time {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: var(--color-text-3);

        .arco-icon {
          font-size: 12px;
        }
      }

      .item-actions {
        opacity: 0;
        transition: opacity 0.2s ease;

        .arco-link {
          font-size: 12px;
          display: flex;
          align-items: center;
          gap: 4px;

          .arco-icon {
            font-size: 12px;
          }
        }
      }
    }
  }

  &:hover .item-actions {
    opacity: 1;
  }
}

.list-footer {
  display: flex;
  align-items: stretch;
  border-top: 1px solid var(--color-border-2);
  padding: 8px;
  background: var(--color-bg-2);

  .arco-btn {
    flex: 1;
    height: 32px;
    font-size: 13px;
  }

  .arco-divider-vertical {
    height: auto;
    margin: 0 4px;
  }
}
</style>
