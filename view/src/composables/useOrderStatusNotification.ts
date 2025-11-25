import { ref, Ref, onUnmounted } from 'vue'
import { Message, Notification } from '@arco-design/web-vue'
import SockJS from 'sockjs-client'
import { Stomp } from '@stomp/stompjs'

export interface OrderStatusNotification {
  orderId: number
  orderCode: string
  oldStatus: string
  newStatus: string
  statusId: number
  message: string
  timestamp: string
  type: 'status_update' | 'inventory_shortage' | 'success' | 'error'
  severity: 'info' | 'warning' | 'error' | 'success'
  details?: string
  actionRequired?: boolean
}

export interface NotificationOptions {
  onStatusChange?: (notification: OrderStatusNotification) => void
  onError?: (error: any) => void
  autoReload?: boolean
}

let stompClient: any = null
let isConnecting = false

/**
 * Real-time WebSocket notification for order status updates
 * Connects to backend WebSocket server and listens for order status changes
 */
export function useOrderStatusNotification(orderId: number | Ref<number>, options: NotificationOptions = {}) {
  const isConnected = ref(false)
  const notifications: Ref<OrderStatusNotification[]> = ref([])
  const lastNotification: Ref<OrderStatusNotification | null> = ref(null)

  const connectWebSocket = async () => {
    if (isConnecting || isConnected.value || stompClient?.connected) return

    isConnecting = true
    try {
      const socket = new SockJS('http://localhost:8080/ws-chat')
      stompClient = Stomp.over(socket)

      // Get JWT token from localStorage or sessionStorage
      const token = localStorage.getItem('access_token') || sessionStorage.getItem('access_token')

      stompClient.connect(
        { Authorization: `Bearer ${token}` },
        (frame: any) => {
          // eslint-disable-next-line no-console
          console.log('✅ WebSocket connected for order notifications:', frame)
          isConnected.value = true
          isConnecting = false

          // Get the current orderId value
          const currentOrderId = typeof orderId === 'number' ? orderId : orderId.value

          // Subscribe to order status updates for this specific order
          // Topic: /topic/orders/{orderId}/status
          const subscription = stompClient.subscribe(
            `/topic/orders/${currentOrderId}/status`,
            (message: any) => {
              try {
                const notification: OrderStatusNotification = JSON.parse(message.body)
                console.log('📨 Order status notification received:', notification)

                // Add to notifications list
                notifications.value.push(notification)
                lastNotification.value = notification

                // Show notification based on type and severity
                showNotificationUI(notification)

                // Call user callback if provided
                if (options.onStatusChange) {
                  options.onStatusChange(notification)
                }

                // Auto-reload page if configured
                if (options.autoReload && notification.type === 'status_update') {
                  // Reload after a short delay to allow animations
                  setTimeout(() => {
                    window.location.reload()
                  }, 1500)
                }
              } catch (error) {
                console.error('Failed to parse notification:', error)
              }
            },
            { id: `sub-order-${currentOrderId}` }
          )

          // Subscribe to personal notifications (for errors, etc.)
          stompClient.subscribe('/user/queue/notifications', (message: any) => {
            try {
              const notification = JSON.parse(message.body)
              console.log('📨 Personal notification received:', notification)

              notifications.value.push(notification)
              lastNotification.value = notification

              showNotificationUI(notification)

              if (options.onStatusChange) {
                options.onStatusChange(notification)
              }
            } catch (error) {
              console.error('Failed to parse personal notification:', error)
            }
          })
        },
        (error: any) => {
          console.error('❌ WebSocket connection error:', error)
          isConnecting = false
          isConnected.value = false

          if (options.onError) {
            options.onError(error)
          }

          // Retry connection after 5 seconds
          setTimeout(() => {
            connectWebSocket()
          }, 5000)
        }
      )
    } catch (error) {
      console.error('Failed to initialize WebSocket:', error)
      isConnecting = false
      isConnected.value = false

      if (options.onError) {
        options.onError(error)
      }

      // Retry after 5 seconds
      setTimeout(() => {
        connectWebSocket()
      }, 5000)
    }
  }

  const disconnectWebSocket = () => {
    if (stompClient && stompClient.connected) {
      stompClient.disconnect(() => {
        console.log('✅ WebSocket disconnected')
        isConnected.value = false
      })
    }
  }

  const showNotificationUI = (notification: OrderStatusNotification) => {
    const notificationConfig: any = {
      title: notification.message || 'Thông báo cập nhật đơn hàng',
      duration: 5,
      closable: true,
    }

    // Add additional content based on type
    if (notification.details) {
      notificationConfig.content = notification.details
    }

    // Choose notification type based on severity
    switch (notification.severity) {
      case 'success':
        notificationConfig.title = `✅ ${notification.message}`
        Notification.success(notificationConfig)
        Message.success(notification.message)
        break

      case 'error':
        notificationConfig.title = `❌ ${notification.message}`
        Notification.error(notificationConfig)
        Message.error(notification.message)
        break

      case 'warning':
        notificationConfig.title = `⚠️ ${notification.message}`
        Notification.warning(notificationConfig)
        Message.warning(notification.message)
        break

      case 'info':
      default:
        notificationConfig.title = `ℹ️ ${notification.message}`
        Notification.info(notificationConfig)
        Message.info(notification.message)
        break
    }

    // Special handling for inventory shortage
    if (notification.type === 'inventory_shortage') {
      Notification.warning({
        title: '⚠️ Sự cố về số lượng sản phẩm',
        content: notification.details || 'Một số sản phẩm không đủ số lượng. Cửa hàng đang cố gắng nhập hàng.',
        duration: 8,
        closable: true,
      })
    }
  }

  // Auto-connect on mount
  connectWebSocket()

  // Cleanup on unmount
  onUnmounted(() => {
    disconnectWebSocket()
  })

  return {
    isConnected,
    notifications,
    lastNotification,
    connect: connectWebSocket,
    disconnect: disconnectWebSocket,
  }
}
