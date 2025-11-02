import { ref, computed, nextTick, type Ref, watch } from 'vue'
import { Message } from '@arco-design/web-vue'
import type { BienTheSanPham } from '@/api/san-pham/bien-the'
import { addProductToInvoice, updateProductQuantityInInvoice, deleteProductsFromInvoice } from '../services/posService'

interface CartItem {
  id: string
  idHoaDonChiTiets?: number[]
  productId: string
  productName: string
  price: number
  discount: number
  quantity: number
  image?: string
  tenChiTietSanPham?: string
  tenMauSac?: string
  maMau?: string
  tenKichThuoc?: string
  tenDeGiay?: string
  tenChatLieu?: string
}

interface Order {
  id: string
  items: CartItem[]
}

export function useCartActions(params: {
  currentOrder: Ref<Order | null>
  allProductVariants: Ref<BienTheSanPham[]>
  cartTableKey: Ref<number>
  userId: number
  refreshProductStock: () => Promise<void>
  createNewInvoice: () => Promise<number>
}) {
  const { currentOrder, allProductVariants, cartTableKey, userId, refreshProductStock, createNewInvoice } = params

  const showAddProductConfirmModal = ref(false)
  const selectedProductForAdd = ref<BienTheSanPham | null>(null)
  const productToDelete = ref<CartItem | null>(null)
  const productQuantityInput = ref(1)
  const quantityInputRef = ref<any>(null)
  const showDeleteProductModal = ref(false)
  const addProductConfirmLoading = ref(false)

  const showAddProductConfirm = (product: BienTheSanPham) => {
    selectedProductForAdd.value = product
    productQuantityInput.value = 1
    showAddProductConfirmModal.value = true
    nextTick(() => {
      quantityInputRef.value?.focus?.()
    })
  }

  const isQuantityValid = computed(() => {
    const quantity = productQuantityInput.value
    const stock = selectedProductForAdd.value?.soLuong || 0
    return quantity > 0 && quantity <= stock
  })

  const handleQuantityChange = (val: number) => {
    productQuantityInput.value = val
    const stock = selectedProductForAdd.value?.soLuong || 0
    if (val && val > stock) {
      Message.warning(`⚠️ Số lượng vượt quá tồn kho! Tồn kho: ${stock}`)
    } else if (val && val < 1) {
      Message.warning('⚠️ Số lượng phải lớn hơn 0')
    }
  }

  const confirmAddProduct = async () => {
    try {
      addProductConfirmLoading.value = true
      if (!selectedProductForAdd.value || !currentOrder.value) {
        throw new Error('Dữ liệu sản phẩm hoặc đơn hàng không hợp lệ')
      }

      let invoiceId = parseInt(currentOrder.value.id)
      const quantity = productQuantityInput.value
      const productId = selectedProductForAdd.value.id

      if (!quantity || quantity < 1) {
        Message.error('Số lượng phải lớn hơn 0')
        return
      }
      if (!productId || Number.isNaN(productId)) {
        Message.error('ID sản phẩm không hợp lệ')
        return
      }

      if (Number.isNaN(invoiceId)) {
        invoiceId = await createNewInvoice()
        if (!invoiceId) throw new Error('Không thể tạo hóa đơn')
        currentOrder.value.id = invoiceId.toString()
      }

      const idHoaDonChiTiet = await addProductToInvoice(invoiceId, productId, quantity, userId)

      const existingItem = currentOrder.value.items.find((item) => item.productId === productId.toString())
      if (existingItem) {
        existingItem.quantity += quantity
        if (idHoaDonChiTiet) {
          if (!existingItem.idHoaDonChiTiets) existingItem.idHoaDonChiTiets = []
          existingItem.idHoaDonChiTiets.push(idHoaDonChiTiet)
        }
        Message.success(`Cập nhật số lượng sản phẩm. Tổng cộng: ${existingItem.quantity}`)
      } else {
        const item: CartItem = {
          id: `${Date.now()}_${Math.random()}`,
          idHoaDonChiTiets: idHoaDonChiTiet ? [idHoaDonChiTiet] : [],
          productId: productId.toString(),
          productName: selectedProductForAdd.value.tenSanPham || '',
          price: selectedProductForAdd.value.giaBan || 0,
          discount: selectedProductForAdd.value.giaTriGiamGia || 0,
          quantity,
          image: selectedProductForAdd.value.anhSanPham?.[0] || '',
          tenChiTietSanPham: selectedProductForAdd.value.tenChiTietSanPham || '',
          tenMauSac: selectedProductForAdd.value.tenMauSac || '',
          maMau: selectedProductForAdd.value.maMau || '',
          tenKichThuoc: selectedProductForAdd.value.tenKichThuoc || '',
          tenDeGiay: selectedProductForAdd.value.tenDeGiay || '',
          tenChatLieu: selectedProductForAdd.value.tenChatLieu || '',
        }
        currentOrder.value.items.push(item)
        Message.success('Thêm sản phẩm thành công')
      }

      await refreshProductStock()

      try {
        const bc = new BroadcastChannel('stock-update-channel')
        bc.postMessage({ type: 'STOCK_CHANGE', productId, needsRefresh: true })
        bc.close()
      } catch {}

      showAddProductConfirmModal.value = false
      selectedProductForAdd.value = null
      productQuantityInput.value = 1
    } catch (error: any) {
      console.error('Lỗi thêm sản phẩm:', error)
      Message.error(error.message || 'Có lỗi xảy ra khi thêm sản phẩm')
    } finally {
      addProductConfirmLoading.value = false
    }
  }

  const updateQuantity = async (itemId: string, quantity: number) => {
    let item: CartItem | undefined
    let oldQuantity = 1
    try {
      if (!currentOrder.value) throw new Error('Không tìm thấy đơn hàng hiện tại')
      item = currentOrder.value.items.find((i) => i.id === itemId)
      if (!item) throw new Error('Không tìm thấy sản phẩm trong giỏ hàng')

      oldQuantity = item.quantity
      const newQuantity = Math.max(1, quantity || 1)
      const diff = newQuantity - oldQuantity
      if (diff === 0) return

      const productId = parseInt(item.productId)
      if (Number.isNaN(productId)) throw new Error('ID sản phẩm không hợp lệ')

      const productInVariants = allProductVariants.value.find((p) => p.id === productId)
      if (!productInVariants) throw new Error('Không tìm thấy thông tin sản phẩm trong kho')

      try {
        const currentStockInWarehouse = productInVariants.soLuong || 0
        if (diff > 0 && currentStockInWarehouse < diff) {
          throw new Error(`Tồn kho không đủ! Yêu cầu tăng: ${diff} cái | Còn lại trong kho: ${currentStockInWarehouse} cái`)
        }
      } catch (stockError: any) {
        Message.error(`❌ ${stockError.message}`)
        item.quantity = oldQuantity
        cartTableKey.value++
        return
      }

      if (item.idHoaDonChiTiets && item.idHoaDonChiTiets.length > 0) {
        const quantityPerDetail = Math.max(1, Math.floor(newQuantity / item.idHoaDonChiTiets.length))
        const remainingQuantity = newQuantity % item.idHoaDonChiTiets.length
        for (let idx = 0; idx < item.idHoaDonChiTiets.length; idx++) {
          const detailId = item.idHoaDonChiTiets[idx]
          const detailQuantity = idx === item.idHoaDonChiTiets.length - 1 ? quantityPerDetail + remainingQuantity : quantityPerDetail
          await updateProductQuantityInInvoice(detailId, detailQuantity, userId)
        }
      }

      item.quantity = newQuantity
      await refreshProductStock()

      try {
        const bc = new BroadcastChannel('stock-update-channel')
        bc.postMessage({ type: 'STOCK_CHANGE', productId, needsRefresh: true })
        bc.close()
      } catch {}
    } catch (error: any) {
      console.error('Lỗi cập nhật số lượng:', error)
      Message.error(error.message || 'Có lỗi xảy ra khi cập nhật số lượng')
      if (item) {
        item.quantity = oldQuantity
        cartTableKey.value++
      }
    }
  }

  const removeFromCart = async (itemId: string) => {
    try {
      if (!currentOrder.value) {
        Message.error('Không tìm thấy đơn hàng hiện tại')
        throw new Error('Không tìm thấy đơn hàng hiện tại')
      }
      const index = currentOrder.value.items.findIndex((i) => i.id === itemId)
      if (index === -1) {
        Message.error('Không tìm thấy sản phẩm trong giỏ hàng')
        throw new Error('Không tìm thấy sản phẩm trong giỏ hàng')
      }
      const item = currentOrder.value.items[index]
      const productId = parseInt(item.productId)
      if (Number.isNaN(productId)) {
        Message.error('ID sản phẩm không hợp lệ')
        throw new Error('ID sản phẩm không hợp lệ')
      }

      const invoiceId = parseInt(currentOrder.value.id)
      if (!Number.isNaN(invoiceId) && item.idHoaDonChiTiets && item.idHoaDonChiTiets.length > 0) {
        await deleteProductsFromInvoice(item.idHoaDonChiTiets, userId)
      }

      currentOrder.value.items.splice(index, 1)
      await refreshProductStock()
      Message.success('Sản phẩm đã được xóa khỏi giỏ hàng')
    } catch (error: any) {
      console.error('Lỗi xóa sản phẩm:', error)
      Message.error(error.message || 'Có lỗi xảy ra khi xóa sản phẩm')
    }
  }

  const clearCart = async () => {
    try {
      if (!currentOrder.value) throw new Error('Không tìm thấy đơn hàng hiện tại')

      const invoiceDetailIds: number[] = []
      const affectedProductIds: number[] = []

      currentOrder.value.items.forEach((item) => {
        try {
          const productId = parseInt(item.productId)
          if (Number.isNaN(productId)) return
          affectedProductIds.push(productId)
          if (item.idHoaDonChiTiets && item.idHoaDonChiTiets.length > 0) {
            invoiceDetailIds.push(...item.idHoaDonChiTiets)
          }
          const productInVariants = allProductVariants.value.find((p) => p.id === productId)
          if (productInVariants) {
            productInVariants.soLuong = (productInVariants.soLuong || 0) + item.quantity
          }
        } catch {}
      })

      if (invoiceDetailIds.length > 0) {
        await deleteProductsFromInvoice(invoiceDetailIds, userId)
      }

      currentOrder.value.items = []

      try {
        const bc = new BroadcastChannel('stock-update-channel')
        for (const productId of affectedProductIds) {
          bc.postMessage({ type: 'STOCK_CHANGE', productId, needsRefresh: true })
        }
        bc.close()
      } catch {}

      await refreshProductStock()
      Message.success('Đã xóa tất cả sản phẩm khỏi giỏ hàng')
    } catch (error: any) {
      console.error('Lỗi xóa giỏ hàng:', error)
      Message.error(error.message || 'Có lỗi xảy ra khi xóa giỏ hàng')
    }
  }

  const showDeleteProductConfirm = (product: CartItem) => {
    productToDelete.value = product
    showDeleteProductModal.value = true
  }

  const confirmDeleteProduct = async () => {
    if (productToDelete.value) {
      try {
        const itemId = productToDelete.value.id
        await removeFromCart(itemId)
        showDeleteProductModal.value = false
        productToDelete.value = null
      } catch (error) {
        console.error('❌ Lỗi khi xóa sản phẩm:', error)
        Message.error('Có lỗi xảy ra khi xóa sản phẩm')
      }
    }
  }

  watch(showDeleteProductModal, (open) => {
    if (!open) productToDelete.value = null
  })

  return {
    // state
    showAddProductConfirmModal,
    selectedProductForAdd,
    productQuantityInput,
    isQuantityValid,
    quantityInputRef,
    showDeleteProductModal,
    addProductConfirmLoading,
    productToDelete,

    // actions
    showAddProductConfirm,
    handleQuantityChange,
    confirmAddProduct,
    showDeleteProductConfirm,
    confirmDeleteProduct,
    updateQuantity,
    removeFromCart,
    clearCart,
  }
}
