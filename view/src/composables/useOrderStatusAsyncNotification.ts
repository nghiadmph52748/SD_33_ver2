import { ref, Ref } from 'vue'
import { Message, Notification } from '@arco-design/web-vue'

export interface AsyncStatusNotification {
  orderId: number
  orderCode: string
  oldStatus: string
  newStatus: string
  message: string
  timestamp: string
  type: 'pending' | 'success' | 'error' | 'warning'
}

/**
 * Simple async notification for order status updates
 * Shows loading, success/error states during status change
 * Can be used alongside WebSocket or independently
 */
export function useOrderStatusAsyncNotification() {
  const isUpdating = ref(false)
  const lastNotification: Ref<AsyncStatusNotification | null> = ref(null)

  const showLoadingNotification = (message: string = 'Đang cập nhật trạng thái...') => {
    isUpdating.value = true
    const hideLoader = Message.loading(message, 0)
    return hideLoader
  }

  const showSuccessNotification = (notification: Partial<AsyncStatusNotification> & { message: string }, duration: number = 5) => {
    isUpdating.value = false
    lastNotification.value = {
      orderId: notification.orderId || 0,
      orderCode: notification.orderCode || '',
      oldStatus: notification.oldStatus || '',
      newStatus: notification.newStatus || '',
      message: notification.message,
      timestamp: new Date().toISOString(),
      type: 'success',
    }

    Message.success({
      content: `✅ ${notification.message}`,
      duration: duration,
    })

    Notification.success({
      title: '✅ Cập nhật thành công',
      content: notification.message,
      duration: duration,
      closable: true,
    })
  }

  const showErrorNotification = (notification: Partial<AsyncStatusNotification> & { message: string }, duration: number = 5) => {
    isUpdating.value = false
    lastNotification.value = {
      orderId: notification.orderId || 0,
      orderCode: notification.orderCode || '',
      oldStatus: notification.oldStatus || '',
      newStatus: notification.newStatus || '',
      message: notification.message,
      timestamp: new Date().toISOString(),
      type: 'error',
    }

    Message.error({
      content: `❌ ${notification.message}`,
      duration: duration,
    })

    Notification.error({
      title: '❌ Cập nhật thất bại',
      content: notification.message,
      duration: duration,
      closable: true,
    })
  }

  const showWarningNotification = (message: string, details?: string, duration: number = 5) => {
    Message.warning({
      content: `⚠️ ${message}`,
      duration: duration,
    })

    Notification.warning({
      title: '⚠️ Cảnh báo',
      content: details || message,
      duration: duration,
      closable: true,
    })
  }

  const showInfoNotification = (message: string, details?: string, duration: number = 4) => {
    Message.info({
      content: `ℹ️ ${message}`,
      duration: duration,
    })

    Notification.info({
      title: 'ℹ️ Thông báo',
      content: details || message,
      duration: duration,
      closable: true,
    })
  }

  const showInventoryShortageNotification = (insufficientProducts: string, duration: number = 8) => {
    Message.warning({
      content: '⚠️ Sự cố về số lượng sản phẩm',
      duration: duration,
    })

    Notification.warning({
      title: '⚠️ Sự cố về số lượng sản phẩm',
      content: `Các sản phẩm sau không đủ số lượng: ${insufficientProducts}. Cửa hàng đang cố gắng nhập hàng để hoàn thành đơn.`,
      duration: duration,
      closable: true,
    })
  }

  return {
    isUpdating,
    lastNotification,
    showLoadingNotification,
    showSuccessNotification,
    showErrorNotification,
    showWarningNotification,
    showInfoNotification,
    showInventoryShortageNotification,
  }
}
