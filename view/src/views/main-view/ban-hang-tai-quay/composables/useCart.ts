import { ref, computed, type Ref } from 'vue'

// Minimal typings for cart context
interface CartItem {
  id: string
  productId: string
  productName: string
  price: number
  discount: number
  quantity: number
}

interface Order {
  items: CartItem[]
}

export function useCart(params: { currentOrder: Ref<Order | null> }) {
  const { currentOrder } = params

  const cartPagination = ref({
    current: 1,
    pageSize: 5,
  })

  // Force re-render key cho cart table khi có lỗi cập nhật quantity
  const cartTableKey = ref(0)

  const cartColumns = [
    {
      title: 'STT',
      dataIndex: 'stt',
      key: 'stt',
      width: 50,
      align: 'center' as const,
    },
    {
      title: 'Sản Phẩm',
      dataIndex: 'product',
      key: 'product',
      width: 300,
      slotName: 'product',
    },
    {
      title: 'Số Lượng',
      dataIndex: 'quantity',
      key: 'quantity',
      slotName: 'quantity',
      width: 100,
      align: 'center' as const,
    },
    {
      title: 'Giá Bán',
      dataIndex: 'price',
      key: 'price',
      slotName: 'price',
      width: 120,
      align: 'right' as const,
    },
    {
      title: 'Thành Tiền',
      dataIndex: 'subtotal',
      key: 'subtotal',
      slotName: 'subtotal',
      width: 130,
      align: 'right' as const,
    },
    {
      title: 'Thao Tác',
      dataIndex: 'action',
      key: 'action',
      slotName: 'action',
      width: 80,
      align: 'center' as const,
    },
  ]

  const paginatedCartItems = computed(() => {
    if (!currentOrder.value) return [] as Array<CartItem & { stt: number }>
    const start = (cartPagination.value.current - 1) * cartPagination.value.pageSize
    const end = start + cartPagination.value.pageSize
    const items = currentOrder.value.items.slice(start, end)

    // Thêm STT cho mỗi cart item
    return items.map((cartItem, index) => ({
      ...cartItem,
      stt: start + index + 1,
    }))
  })

  return {
    cartPagination,
    cartTableKey,
    cartColumns,
    paginatedCartItems,
  }
}
