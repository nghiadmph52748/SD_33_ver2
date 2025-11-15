import axios from "./interceptor";
import type { HttpResponse } from "./interceptor";
import type { CartItem } from "@/stores/cart";

export interface OrderItem {
  idBienThe: number;
  soLuong: number;
  giaBan: number;
}

export interface CreateOrderRequest {
  idKhachHang?: number;
  idPhieuGiamGia?: number;
  idPhuongThucThanhToan?: number;
  tenNguoiNhan: string;
  diaChiNhanHang: string;
  soDienThoaiNguoiNhan: string;
  emailNguoiNhan: string;
  loaiDon?: boolean;
  giaoHang: boolean;
  phiVanChuyen: number;
  tongTien: number;
  tongTienSauGiam: number;
  ghiChu?: string;
  trangThai: boolean;
  deleted: boolean;
  createAt: string;
}

export interface OrderResponse {
  id: number;
  maHoaDon: string;
  tongTien: number;
  trangThai: string;
}

export interface AddProductToCartRequest {
  idHoaDon: number;
  idBienThe: number;
  soLuong: number;
  giaBan: number;
  trangThai: boolean;
  deleted: boolean;
  createAt: string;
}

export interface AddProductToCartResponse {
  idHoaDon: number;
  idChiTietHoaDon: number;
  soLuong: number;
  giaBan: number;
  thanhTien: number;
}

export interface CreateOrderOptions {
  customerId?: number;
  paymentMethodId?: number;
  voucherId?: number;
  notes?: string;
  shippingFee?: number;
  subtotal?: number;
  totalAmount?: number;
  createAt?: string;
  recipient: {
    fullName: string;
    phone: string;
    email: string;
    address: string;
  };
}

// Convert cart items to order items (need to map product variants)
export function convertCartToOrderItems(cartItems: CartItem[]): OrderItem[] {
  // This is a simplified conversion - in production, you'd need to:
  // 1. Match products to their variants (by size/color)
  // 2. Get the variant ID from the backend
  // For now, we'll need the variant IDs to be passed with cart items

  return cartItems.map((item) => ({
    idBienThe: (item as any).idBienThe || 0, // Should be set when adding to cart
    soLuong: item.quantity,
    giaBan: item.price,
  }));
}

export function createOrder(
  orderData: CreateOrderRequest
): Promise<HttpResponse<OrderResponse>> {
  return axios.post("/api/hoa-don-management/add", orderData);
}

export function addProductToCart(
  request: AddProductToCartRequest
): Promise<HttpResponse<AddProductToCartResponse>> {
  return axios.post("/api/hoa-don-chi-tiet-management/add-to-cart", request);
}

// Create order from cart items
export async function createOrderFromCart(
  cartItems: CartItem[],
  options: CreateOrderOptions
): Promise<OrderResponse | null> {
  try {
    if (!options?.recipient) {
      throw new Error("Thiếu thông tin người nhận để tạo hoá đơn");
    }

    const createdAt =
      options.createAt ?? new Date().toISOString().split("T")[0];
    const shippingFee = options.shippingFee ?? 0;
    const subtotal =
      options.subtotal ??
      cartItems.reduce((total, item) => total + item.price * item.quantity, 0);
    const baseTotal = subtotal + shippingFee;
    const requestedFinal = options.totalAmount ?? baseTotal;

    // SQL check constraint enforces tong_tien_sau_giam <= tong_tien
    const tongTien = Math.max(baseTotal, requestedFinal);
    const tongTienSauGiam = Math.min(requestedFinal, tongTien);

    const orderRequest: CreateOrderRequest = {
      idKhachHang: options.customerId,
      idPhuongThucThanhToan: options.paymentMethodId,
      idPhieuGiamGia: options.voucherId,
      tenNguoiNhan: options.recipient.fullName,
      diaChiNhanHang: options.recipient.address,
      soDienThoaiNguoiNhan: options.recipient.phone,
      emailNguoiNhan: options.recipient.email,
      loaiDon: true,
      giaoHang: true,
      phiVanChuyen: shippingFee,
      tongTien,
      tongTienSauGiam,
      ghiChu: options.notes,
      trangThai: true,
      deleted: false,
      createAt: createdAt,
    };

    const response = await createOrder(orderRequest);
    const order = response.data as OrderResponse;

    if (!order?.id) {
      throw new Error("Không nhận được ID hoá đơn từ máy chủ");
    }

    // Persist line items using invoice detail endpoint
    await Promise.all(
      cartItems.map((item) => {
        if (!(item as any).idBienThe) {
          throw new Error(`Thiếu mã biến thể cho sản phẩm ${item.id}`);
        }

        const detailPayload: AddProductToCartRequest = {
          idHoaDon: order.id,
          idBienThe: Number((item as any).idBienThe),
          soLuong: item.quantity,
          giaBan: item.price,
          trangThai: true,
          deleted: false,
          createAt: createdAt,
        };

        return addProductToCart(detailPayload);
      })
    );

    return order;
  } catch (error) {
    console.error("Error creating order:", error);
    return null;
  }
}
