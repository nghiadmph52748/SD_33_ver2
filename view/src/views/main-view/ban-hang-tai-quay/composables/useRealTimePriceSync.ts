import { ref, watch, type Ref } from 'vue'
import type { BienTheSanPham } from '@/api/san-pham/bien-the'

interface CartItem {
  id: string
  productId: string
  price: number
  discount: number
  quantity: number
  productName?: string
  lastServerPriceNotified?: number
  lastServerDiscountNotified?: number
}

interface Order {
  id: string
  items: CartItem[]
}

interface PriceChange {
  id: string
  productName: string
  oldPrice: number
  newPrice: number
  oldDiscount: number
  newDiscount: number
}

const normalizeNumber = (value: unknown, fallback = 0) => {
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : fallback
}

export default function useRealTimePriceSync(params: {
  currentOrder: Ref<Order | null>
  allProductVariants: Ref<BienTheSanPham[]>
  increaseCartTableKey?: () => void
}) {
  const { currentOrder, allProductVariants, increaseCartTableKey } = params

  const showPriceChangeModal = ref(false)
  const priceChanges = ref<Map<string, PriceChange>>(new Map())
  const priceChangeConfirmLoading = ref(false)

  const updateCartPricesFromServer = () => {
    if (!currentOrder.value || !currentOrder.value.items.length) return

    const changes = new Map<string, PriceChange>()

    currentOrder.value.items.forEach((cartItem) => {
      const productId = parseInt(cartItem.productId, 10)
      if (Number.isNaN(productId)) return

      const serverProduct = allProductVariants.value.find((p) => p.id === productId)
      if (!serverProduct) return

      const currentPrice = cartItem.price
      const currentDiscount = cartItem.discount
      const serverPrice = normalizeNumber(serverProduct.giaBan, currentPrice)
      const serverDiscount = normalizeNumber(serverProduct.giaTriGiamGia, currentDiscount)
      const currentNetPrice = currentPrice * (1 - (currentDiscount > 0 ? currentDiscount / 100 : 0))
      const serverNetPrice = serverPrice * (1 - (serverDiscount > 0 ? serverDiscount / 100 : 0))
      const netPriceUnchanged = Math.abs(currentNetPrice - serverNetPrice) < 0.0001

      if (cartItem.lastServerPriceNotified === serverPrice && cartItem.lastServerDiscountNotified === serverDiscount) {
        return
      }

      if (currentPrice === serverPrice && currentDiscount === serverDiscount) {
        cartItem.lastServerPriceNotified = serverPrice
        cartItem.lastServerDiscountNotified = serverDiscount
        return
      }

      if (netPriceUnchanged) {
        cartItem.lastServerPriceNotified = serverPrice
        cartItem.lastServerDiscountNotified = serverDiscount
        return
      }

      if (currentPrice !== serverPrice || currentDiscount !== serverDiscount) {
        changes.set(cartItem.id, {
          id: cartItem.id,
          productName: cartItem.productName || '',
          oldPrice: currentPrice,
          newPrice: serverPrice,
          oldDiscount: currentDiscount,
          newDiscount: serverDiscount,
        })

        cartItem.lastServerPriceNotified = serverPrice
        cartItem.lastServerDiscountNotified = serverDiscount
      }
    })

    if (changes.size > 0) {
      priceChanges.value = changes
      showPriceChangeModal.value = true
      // Force CartTable to re-render with new prices
      increaseCartTableKey?.()
    }
  }

  const confirmPriceChangeModal = () => {
    priceChangeConfirmLoading.value = true
    setTimeout(() => {
      showPriceChangeModal.value = false
      priceChanges.value.clear()
      priceChangeConfirmLoading.value = false
    }, 300)
  }

  // Watch for product price/discount changes with optimized approach
  let lastCheck = 0
  const CHECK_INTERVAL = 1000

  watch(
    () => allProductVariants.value.length,
    () => {
      const now = Date.now()
      if (now - lastCheck > CHECK_INTERVAL) {
        lastCheck = now
        updateCartPricesFromServer()
      }
    }
  )

  // Also watch direct price changes on products
  watch(
    () => allProductVariants.value.map((p) => `${p.id}_${p.giaBan}_${p.giaTriGiamGia}`),
    () => {
      updateCartPricesFromServer()
    }
  )

  return {
    showPriceChangeModal,
    priceChanges,
    priceChangeConfirmLoading,
    updateCartPricesFromServer,
    confirmPriceChangeModal,
  }
}
