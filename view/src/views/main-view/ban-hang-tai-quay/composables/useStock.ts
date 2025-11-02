import { computed, type Ref } from 'vue'
import type { BienTheSanPham } from '@/api/san-pham/bien-the'

interface CartItem {
  id: string
  productId: string
  productName: string
  quantity: number
}

interface Order {
  items: CartItem[]
}

export function useStock(params: {
  currentOrder: Ref<Order | null>
  allProductVariants: Ref<BienTheSanPham[]>
}) {
  const { currentOrder, allProductVariants } = params

  const insufficientStockItems = computed(() => {
    if (!currentOrder.value) return [] as Array<{ id: string; productName: string; requiredQty: number; currentStock: number; shortageQty: number }>
    return currentOrder.value.items
      .map((cartItem) => {
        const product = allProductVariants.value.find((p) => p.id === parseInt(cartItem.productId))
        const stock = product?.soLuong || 0
        if (stock < 0) {
          return {
            id: cartItem.id,
            productName: cartItem.productName,
            requiredQty: cartItem.quantity,
            currentStock: Math.max(0, stock),
            shortageQty: Math.abs(stock),
          }
        }
        return null
      })
      .filter((x): x is NonNullable<typeof x> => x !== null)
  })

  const overStockItems = computed(() => {
    if (!currentOrder.value) return [] as Array<{ id: string; productName: string; requiredQty: number; currentStock: number; shortageQty: number }>
    return currentOrder.value.items
      .map((cartItem) => {
        const product = allProductVariants.value.find((p) => p.id === parseInt(cartItem.productId))
        const stock = product?.soLuong || 0
        if (cartItem.quantity > 0 && stock < 0) {
          return {
            id: cartItem.id,
            productName: cartItem.productName,
            requiredQty: cartItem.quantity,
            currentStock: Math.max(0, stock),
            shortageQty: Math.abs(stock),
          }
        }
        return null
      })
      .filter((x): x is NonNullable<typeof x> => x !== null)
  })

  return { insufficientStockItems, overStockItems }
}
