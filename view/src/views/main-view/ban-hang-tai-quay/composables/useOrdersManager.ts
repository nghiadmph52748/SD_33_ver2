import { ref, type Ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import type { BienTheSanPham } from '@/api/san-pham/bien-the'
import { deleteInvoice, createInvoice as svcCreateInvoice } from '../services/posService'

interface CartItem {
  quantity: number
  productId: string
  productName: string
}
interface Order {
  id: string
  orderCode: string
  items: CartItem[]
  customerId: string | null
  createdAt: Date
}

export function useOrdersManager(params: {
  orders: Ref<Order[]>
  currentOrderIndex: Ref<string>
  allProductVariants: Ref<BienTheSanPham[]>
  soldQuantitiesByProductId: Ref<Record<string | number, number>>
  cartPagination: Ref<{ current: number }>
  userId: number
}) {
  const { orders, currentOrderIndex, allProductVariants, soldQuantitiesByProductId, cartPagination, userId } = params

  const showDeleteConfirmModal = ref(false)
  const deleteConfirmOrderIndex = ref<number | null>(null)

  const generateOrderCode = (): string => {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
    let code = ''
    for (let i = 0; i < 7; i += 1) code += chars.charAt(Math.floor(Math.random() * chars.length))
    return code
  }

  const createNewOrder = async () => {
    try {
      const invoiceId = await svcCreateInvoice(userId)
      if (!invoiceId) throw new Error('Không thể tạo hóa đơn')
      const newOrder: Order = {
        id: invoiceId.toString(),
        orderCode: generateOrderCode(),
        items: [],
        customerId: '',
        createdAt: new Date(),
      }
      orders.value.push(newOrder)
      currentOrderIndex.value = (orders.value.length - 1).toString()
      Message.success('Đơn hàng mới đã được tạo')
    } catch (error: any) {
      console.error('Lỗi tạo đơn hàng:', error)
      Message.error(error.message || 'Không thể tạo đơn hàng mới')
    }
  }

  const deleteOrderByIndex = (index: number) => {
    orders.value.splice(index, 1)
    if (orders.value.length > 0) currentOrderIndex.value = '0'
  }

  const showDeleteConfirm = (index: number) => {
    deleteConfirmOrderIndex.value = index
    showDeleteConfirmModal.value = true
  }

  const confirmDeleteOrder = async () => {
    if (deleteConfirmOrderIndex.value === null) return
    try {
      const orderIndex = deleteConfirmOrderIndex.value
      const orderToDelete = orders.value[orderIndex]
      if (orderToDelete?.id) {
        const invoiceId = parseInt(orderToDelete.id)
        if (!Number.isNaN(invoiceId)) {
          await deleteInvoice(invoiceId, userId)
          try {
            const orderBroadcastChannel = new BroadcastChannel('order-update-channel')
            orderBroadcastChannel.postMessage({
              type: 'ORDER_DELETED',
              invoiceId,
              orderCode: orderToDelete.orderCode,
              timestamp: new Date().toISOString(),
            })
            orderBroadcastChannel.close()
          } catch (err) {
            console.warn('BroadcastChannel broadcast failed:', err)
          }
        }
      }

      if (orderToDelete && orderToDelete.items.length > 0) {
        orderToDelete.items.forEach((item) => {
          try {
            const productId = parseInt(item.productId)
            if (Number.isNaN(productId)) return
            const productInVariants = allProductVariants.value.find((p) => p.id === productId)
            if (productInVariants) productInVariants.soLuong = (productInVariants.soLuong || 0) + item.quantity
            soldQuantitiesByProductId.value[productId] = (soldQuantitiesByProductId.value[productId] || 0) - item.quantity
          } catch {}
        })
      }

      deleteOrderByIndex(orderIndex)
      showDeleteConfirmModal.value = false
      deleteConfirmOrderIndex.value = null
      Message.success('Đơn hàng đã được xoá')
    } catch (error: any) {
      console.error('Lỗi xóa đơn hàng:', error)
      Message.error(error.message || 'Có lỗi xảy ra khi xóa đơn hàng')
    }
  }

  const handleOrderChange = (key: string) => {
    currentOrderIndex.value = key
    cartPagination.value.current = 1
  }

  return {
    showDeleteConfirmModal,
    showDeleteConfirm,
    confirmDeleteOrder,
    createNewOrder,
    handleOrderChange,
  }
}
