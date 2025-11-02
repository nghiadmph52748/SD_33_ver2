import { ref, watch, type Ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { Html5Qrcode, Html5QrcodeSupportedFormats } from 'html5-qrcode'
import type { BienTheSanPham } from '@/api/san-pham/bien-the'

// Minimal typings matching the component
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
  orderCode: string
  items: CartItem[]
  customerId: string | null
  createdAt: Date
}

export function useQrScanner(params: {
  allProductVariants: Ref<BienTheSanPham[]>
  currentOrder: Ref<Order | null>
  loadAllProducts: () => Promise<void>
  refreshProductStock: () => Promise<void>
}) {
  const { allProductVariants, currentOrder, loadAllProducts, refreshProductStock } = params

  const showQRScanner = ref(false)
  const qrScannerInstance = ref<Html5Qrcode | null>(null)
  const isStarting = ref(false)

  const initQRScanner = async () => {
    if (isStarting.value || qrScannerInstance.value) return
    isStarting.value = true
    try {
      if (qrScannerInstance.value) {
        try {
          await qrScannerInstance.value.stop()
          await qrScannerInstance.value.clear()
        } catch (cleanupError) {
          console.warn('Cleanup error:', cleanupError)
        }
        qrScannerInstance.value = null
      }

      try {
        const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: false })
        stream.getTracks().forEach((track) => track.stop())
      } catch (permissionError: any) {
        console.error('Camera permission error:', permissionError)
        if (permissionError.name === 'NotAllowedError') {
          Message.error('Quyền truy cập camera bị từ chối. Vui lòng cho phép truy cập camera trong trình duyệt.')
        } else if (permissionError.name === 'NotFoundError') {
          Message.error('Không tìm thấy camera trên thiết bị. Vui lòng kết nối camera và thử lại.')
        } else {
          Message.error('Không thể truy cập camera. Vui lòng kiểm tra quyền truy cập.')
        }
        showQRScanner.value = false
        return
      }

      const qrReaderElement = document.getElementById('qr-reader')
      if (!qrReaderElement) {
        console.error('[DEBUG] QR reader element not found in DOM')
        throw new Error('QR reader element not found')
      }

      const html5QrCode = new Html5Qrcode('qr-reader')
      qrScannerInstance.value = html5QrCode

      try {
        await html5QrCode.start(
          { facingMode: 'environment' },
          {
            fps: 30,
            qrbox: { width: 400, height: 400 },
            aspectRatio: 1,
            formats: Html5QrcodeSupportedFormats.QR_CODE,
            supportedScanTypes: ['qr_code'] as any,
            showTorchButtonIfSupported: true,
            showZoomSliderIfSupported: true,
            defaultZoomValueIfSupported: 1.5,
            experimentalFeatures: {
              useBarCodeDetectorIfSupported: true,
            },
            showScanHighlighting: true,
            highlightScanRegion: true,
            highlightCodeOutline: true,
          },
          (decodedText: string) => {
            handleQRScanSuccess(decodedText)
          },
          (errorMessage: string) => {
            console.debug('QR Scan error:', errorMessage)
          }
        )
      } catch (envError) {
        console.warn('[DEBUG] Environment camera failed, trying any camera:', envError)

        await html5QrCode.start(
          { facingMode: 'user' },
          {
            fps: 30,
            qrbox: { width: 400, height: 400 },
            aspectRatio: 1,
            formats: Html5QrcodeSupportedFormats.QR_CODE,
            supportedScanTypes: ['qr_code'] as any,
            showTorchButtonIfSupported: true,
            showZoomSliderIfSupported: true,
            defaultZoomValueIfSupported: 1.5,
            experimentalFeatures: {
              useBarCodeDetectorIfSupported: true,
            },
            showScanHighlighting: true,
            highlightScanRegion: true,
            highlightCodeOutline: true,
          },
          (decodedText: string) => {
            handleQRScanSuccess(decodedText)
          },
          (errorMessage: string) => {
            console.debug('QR Scan error:', errorMessage)
          }
        )
      }
    } catch (error: any) {
      console.error('[DEBUG] Error initializing QR scanner:', error)
      console.error('[DEBUG] Error details:', {
        name: error.name,
        message: error.message,
        stack: error.stack,
      })

      if (error.name === 'NotAllowedError') {
        Message.error('Quyền truy cập camera bị từ chối. Vui lòng cho phép truy cập camera trong trình duyệt.')
      } else if (error.name === 'NotFoundError') {
        Message.error('Không tìm thấy camera trên thiết bị. Vui lòng kết nối camera và thử lại.')
      } else if (error.name === 'NotReadableError') {
        Message.error('Camera đang được sử dụng bởi ứng dụng khác. Vui lòng đóng ứng dụng khác và thử lại.')
      } else if (error.name === 'OverconstrainedError') {
        Message.error('Camera không hỗ trợ cấu hình yêu cầu. Vui lòng thử lại.')
      } else {
        Message.error('Không thể khởi tạo camera. Vui lòng kiểm tra quyền truy cập camera và thử lại.')
      }

      showQRScanner.value = false
    } finally {
      isStarting.value = false
    }
  }

  const handleQRScanSuccess = async (decodedText: string) => {
    try {
      if (qrScannerInstance.value) {
        try {
          await qrScannerInstance.value.stop()
        } catch {}
      }

      const matchedProduct = allProductVariants.value.find((product) => {
        if (product.qrcode) {
          const qrMatch =
            decodedText === product.qrcode ||
            product.qrcode === decodedText ||
            decodedText.includes(product.qrcode) ||
            product.qrcode.includes(decodedText) ||
            decodedText === product.id?.toString() ||
            decodedText === product.maChiTietSanPham
          return qrMatch
        }
        return false
      })

      if (!matchedProduct) {
        console.error('[DEBUG] No product found with QR code:', decodedText)
        console.error(
          '[DEBUG] Available QR codes in products:',
          allProductVariants.value
            .filter((p) => p.qrcode)
            .map((p) => ({ id: p.id, qrcode: p.qrcode, maChiTietSanPham: p.maChiTietSanPham }))
        )

        const fallbackId = parseInt(decodedText.trim(), 10)
        if (!isNaN(fallbackId) && fallbackId > 0) {
          const fallbackProduct = allProductVariants.value.find((p) => p.id === fallbackId)
          if (fallbackProduct) {
            await addProductToCart(fallbackProduct, 1)
            await closeQRScanner()
            Message.success(`Đã thêm sản phẩm "${fallbackProduct.tenSanPham}" vào giỏ hàng`)
            return
          }
        }

        Message.error(`Không tìm thấy sản phẩm với mã QR: "${decodedText}". Vui lòng thử lại.`)
        return
      }

      if ((matchedProduct.soLuong || 0) <= 0) {
        Message.error(`Sản phẩm "${matchedProduct.tenSanPham}" đã hết hàng`)
        return
      }

      await addProductToCart(matchedProduct, 1)
      await closeQRScanner()
      Message.success(`Đã thêm sản phẩm "${matchedProduct.tenSanPham}" vào giỏ hàng`)
    } catch (error: any) {
      console.error('Error processing QR scan:', error)
      console.error('Error details:', {
        name: error.name,
        message: error.message,
        stack: error.stack,
      })
      Message.error('Có lỗi xảy ra khi xử lý mã QR. Vui lòng thử lại.')
    }
  }

  const addProductToCart = async (product: BienTheSanPham, quantity: number) => {
    try {
      if (!currentOrder.value) {
        throw new Error('Không tìm thấy đơn hàng hiện tại')
      }

      const existingItem = currentOrder.value.items.find((item) => item.productId === product.id?.toString())

      if (existingItem) {
        const newQuantity = existingItem.quantity + quantity
        if (product.soLuong !== undefined && newQuantity > product.soLuong) {
          throw new Error(`Tổng số lượng (${newQuantity}) vượt quá tồn kho (${product.soLuong})`)
        }
        existingItem.quantity = newQuantity
        Message.success(`Cập nhật số lượng sản phẩm. Tổng cộng: ${newQuantity}`)
      } else {
        const item: CartItem = {
          id: `${Date.now()}_${Math.random()}`,
          productId: product.id?.toString() || '',
          productName: product.tenSanPham || '',
          price: product.giaBan || 0,
          discount: product.giaTriGiamGia || 0,
          quantity,
          image: product.anhSanPham?.[0] || '',
          tenChiTietSanPham: product.tenChiTietSanPham || '',
          tenMauSac: product.tenMauSac || '',
          maMau: product.maMau || '',
          tenKichThuoc: product.tenKichThuoc || '',
          tenDeGiay: product.tenDeGiay || '',
          tenChatLieu: product.tenChatLieu || '',
        }
        currentOrder.value.items.push(item)
        Message.success('Thêm sản phẩm vào giỏ hàng thành công')
      }

      const productInVariants = allProductVariants.value.find((p) => p.id === product.id)
      if (productInVariants && typeof productInVariants.soLuong === 'number') {
        productInVariants.soLuong = Math.max(0, productInVariants.soLuong - quantity)
      }

      try {
        const qrStockBroadcastChannel = new BroadcastChannel('stock-update-channel')
        qrStockBroadcastChannel.postMessage({
          type: 'STOCK_CHANGE',
          productId: product.id,
          needsRefresh: true,
        })
        qrStockBroadcastChannel.close()
      } catch (error) {
        console.warn('BroadcastChannel broadcast failed:', error)
      }

      await refreshProductStock()
    } catch (error) {
      console.error('Error adding product to cart:', error)
      throw error
    }
  }

  const closeQRScanner = async () => {
    isStarting.value = false
    if (qrScannerInstance.value) {
      try {
        await qrScannerInstance.value.stop()
        await qrScannerInstance.value.clear()
      } catch (cleanupError) {
        console.warn('Cleanup error:', cleanupError)
      }
      qrScannerInstance.value = null
    }
    showQRScanner.value = false
  }

  // Manage scanner lifecycle when modal visibility changes
  watch(showQRScanner, async (qrOpen) => {
    if (qrOpen) {
      if (allProductVariants.value.length === 0) {
        await loadAllProducts()
      }
      setTimeout(() => {
        initQRScanner()
      }, 100)
    } else {
      await closeQRScanner()
    }
  })

  return {
    showQRScanner,
    initQRScanner,
    closeQRScanner,
  }
}
