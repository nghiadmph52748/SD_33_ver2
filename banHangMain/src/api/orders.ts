import axios from './interceptor'
import type { HttpResponse } from './interceptor'
import type { CartItem } from '@/stores/cart'

export interface OrderItem {
  idBienThe: number
  soLuong: number
  giaBan: number
}

export interface CreateOrderRequest {
  idKhachHang?: number // Optional for guest checkout
  hoaDonChiTiet: OrderItem[]
  idPhuongThucThanhToan?: number
  idPhieuGiamGia?: number
  ghiChu?: string
}

export interface OrderResponse {
  id: number
  maHoaDon: string
  tongTien: number
  trangThai: string
}

// Convert cart items to order items (need to map product variants)
export function convertCartToOrderItems(cartItems: CartItem[]): OrderItem[] {
  // This is a simplified conversion - in production, you'd need to:
  // 1. Match products to their variants (by size/color)
  // 2. Get the variant ID from the backend
  // For now, we'll need the variant IDs to be passed with cart items
  
  return cartItems.map(item => ({
    idBienThe: (item as any).idBienThe || 0, // Should be set when adding to cart
    soLuong: item.quantity,
    giaBan: item.price
  }))
}

export function createOrder(orderData: CreateOrderRequest): Promise<HttpResponse<OrderResponse>> {
  return axios.post('/api/hoa-don-management/add', orderData)
}

// Create order from cart items
export async function createOrderFromCart(
  cartItems: CartItem[],
  customerId?: number,
  paymentMethodId?: number,
  voucherId?: number,
  notes?: string
): Promise<OrderResponse | null> {
  try {
    const orderItems = convertCartToOrderItems(cartItems)
    
    const orderRequest: CreateOrderRequest = {
      idKhachHang: customerId,
      hoaDonChiTiet: orderItems,
      idPhuongThucThanhToan: paymentMethodId,
      idPhieuGiamGia: voucherId,
      ghiChu: notes
    }
    
    const response = await createOrder(orderRequest)
    return response.data as OrderResponse
  } catch (error) {
    console.error('Error creating order:', error)
    return null
  }
}

