import axios from "./interceptor";
import type { HttpResponse } from "./interceptor";
import type { CartItem } from "@/stores/cart";

export interface OrderItem {
  idBienThe: number;
  soLuong: number;
  giaBan: number;
}

export interface HoaDonChiTietRequest {
  idChiTietSanPham?: number; // idBienThe from cart (ChiTietSanPham ID)
  idBienTheSanPham?: number; // Alternative field name
  idBienThe?: number; // Alternative field name (from cart)
  soLuong: number;
  giaBan: number;
  thanhTien?: number;
  ghiChu?: string;
  trangThai?: boolean;
  deleted?: boolean;
  createAt?: string;
}

export interface CreateOrderRequest {
  idKhachHang?: number;
  idPhieuGiamGia?: number;
  idPhuongThucThanhToan?: number;
  idNhanVien?: number;
  idTrangThaiDonHang?: number;
  tenNguoiNhan: string;
  diaChiNhanHang: string;
  soDienThoaiNguoiNhan: string;
  emailNguoiNhan: string;
  hoaDonChiTiet: HoaDonChiTietRequest[]; // Cart items
  loaiDon?: boolean;
  phiVanChuyen: number;
  tongTien: number;
  tongTienSauGiam: number;
  ghiChu?: string;
  trangThai?: boolean;
  deleted?: boolean;
  createAt: string;
}

export interface OrderResponse {
  id: number;
  maHoaDon: string;
  tongTien: number;
  trangThai: string;
}

export interface OrderTrackingDetail extends Partial<OrderResponse> {
  tenNguoiNhan?: string;
  tenKhachHang?: string;
  diaChiNhanHang?: string;
  diaChiNguoiNhan?: string;
  diaChi?: string;
  soDienThoaiNguoiNhan?: string;
  soDienThoai?: string;
  soDienThoaiKhachHang?: string;
  emailNguoiNhan?: string;
  email?: string;
  emailKhachHang?: string;
  tongTienSauGiam?: number;
  phiVanChuyen?: number;
  ngayTao?: string;
  ngayThanhToan?: string;
  createAt?: string;
  loaiDon?: boolean;
  ghiChu?: string;
  // Support both field names from different API versions
  hoaDonChiTiets?: Array<HoaDonChiTietItem>;
  items?: Array<HoaDonChiTietItem>;
}

export interface HoaDonChiTietItem {
  id?: number;
  soLuong?: number;
  giaBan?: number;
  thanhTien?: number;
  tenSanPhamChiTiet?: string;
  tenSanPham?: string;
  maHoaDonChiTiet?: string;
  maSanPhamChiTiet?: string;
  // Flat structure from HoaDonChiTietResponse
  tenMauSac?: string;
  tenKichThuoc?: string;
  // Nested structure for backward compatibility
  idChiTietSanPham?: {
    id?: number;
    maChiTietSanPham?: string;
    idSanPham?: {
      tenSanPham?: string;
      maSanPham?: string;
    };
    idMauSac?: {
      tenMauSac?: string;
      tenMau?: string;
    };
    idKichThuoc?: {
      tenKichThuoc?: string;
      kichThuoc?: string;
    };
  };
}

export interface OrderTimelineEntry {
  id: number;
  idHoaDon: number;
  idTrangThaiDonHang?: number;
  tenTrangThaiDonHang?: string; // Status/Action name
  maTrangThaiDonHang?: string;
  maThongTinDonHang?: string;
  thoiGian: string; // Time when this status was recorded
  ghiChu?: string; // Notes
  trangThai?: boolean;
  deleted?: boolean;
}

export interface OrderStatusSnapshot {
  id?: number;
  maDonHang?: string;
  maThongTinDonHang?: string;
  tenTrangThaiDonHang?: string;
  thoiGian?: string; // Latest update time from thongTinDonHang table
  ghiChu?: string;
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

// Convert cart items to plain objects (removes Vue reactive proxies)
// Uses JSON stringify/parse to safely handle circular references
export function toPlainObject(obj: any): any {
  try {
    return JSON.parse(JSON.stringify(obj));
  } catch (error) {
    console.warn("Failed to convert object to plain object:", error);
    return obj;
  }
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

export function fetchOrderById(
  orderId: number
): Promise<HttpResponse<OrderTrackingDetail>> {
  return axios.get(`/api/hoa-don-management/${orderId}`);
}

export function fetchOrderByCode(
  orderCode: string
): Promise<HttpResponse<OrderTrackingDetail>> {
  return axios.get(`/api/hoa-don-management/code/${orderCode}`);
}

export function fetchOrderTimeline(
  orderId: number
): Promise<HttpResponse<OrderTimelineEntry[]>> {
  // Fetch all order timeline from thong_tin_don_hang table
  // Each entry has thoiGian (time) and tenTrangThaiDonHang (status/action)
  return axios.get(`/api/thong-tin-hoa-don-management/by-hoa-don/${orderId}`);
}

export function fetchOrderHistory(
  orderId: number
): Promise<HttpResponse<OrderTimelineEntry[]>> {
  // Fetch all order history from thongTinDonHang (not just latest)
  return axios.get(`/api/thong-tin-hoa-don-management/by-hoa-don/${orderId}`);
}

export function fetchLatestOrderStatus(
  orderId: number
): Promise<HttpResponse<OrderStatusSnapshot | null>> {
  // Fetch latest status from thongTinDonHang table
  return axios.get(
    `/api/thong-tin-hoa-don-management/latest-by-hoa-don/${orderId}`
  );
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

    // Validate cart items have required IDs
    const validCartItems = cartItems.filter((item) => {
      const hasId = (item as any).idBienThe && (item as any).idBienThe > 0;
      if (!hasId) {
        console.warn("[ORDER] Skipping cart item without idBienThe:", item);
      }
      return hasId;
    });

    if (validCartItems.length === 0) {
      throw new Error("Giỏ hàng không có sản phẩm hợp lệ");
    }

    // Convert Vue reactive cart items to plain objects to avoid circular reference errors
    const plainCartItems = validCartItems.map((item) =>
      toPlainObject(item)
    ) as CartItem[];

    // Extract primitive values from options to avoid circular references
    // Don't use toPlainObject on the whole options object as it contains reactive refs
    // Format: YYYY-MM-DDTHH:mm:ss (backend expects LocalDateTime in ISO 8601 format)
    const now = new Date();
    const createdAt = (options.createAt ??
      now.toISOString().split(".")[0]) as string;
    const shippingFee = Number(options.shippingFee ?? 0);
    const subtotal =
      Number(options.subtotal) ||
      plainCartItems.reduce(
        (total, item) => total + item.price * item.quantity,
        0
      );

    // totalAmount = subtotal + shippingFee (without VAT)
    const totalAmount = Number(options.totalAmount ?? 0);

    // tongTien = total invoice amount (subtotal + shipping fee)
    // tongTienSauGiam = final amount customer pays (same as tongTien, no discount/voucher yet)
    const tongTien = totalAmount > 0 ? totalAmount : subtotal + shippingFee;
    const tongTienSauGiam = tongTien;

    const orderRequest: CreateOrderRequest = {
      idKhachHang: options.customerId ? Number(options.customerId) : undefined,
      idPhuongThucThanhToan: options.paymentMethodId
        ? Number(options.paymentMethodId)
        : undefined,
      idPhieuGiamGia: options.voucherId ? Number(options.voucherId) : undefined,
      tenNguoiNhan: String(options.recipient.fullName),
      diaChiNhanHang: String(options.recipient.address),
      soDienThoaiNguoiNhan: String(options.recipient.phone),
      emailNguoiNhan: String(options.recipient.email),
      hoaDonChiTiet: plainCartItems.map((item) => {
        const idChiTietSanPham = (item as any).idBienThe || 0;
        if (idChiTietSanPham <= 0) {
          console.warn(
            "[ORDER] Invalid idChiTietSanPham:",
            idChiTietSanPham,
            "for item:",
            item.name
          );
        }
        const request: HoaDonChiTietRequest = {
          idBienThe: idChiTietSanPham, // Backend accepts this as idChiTietSanPham
          soLuong: item.quantity,
          giaBan: item.price,
          trangThai: true,
          deleted: false,
          createAt: createdAt,
        };
        return request;
      }),
      loaiDon: true,
      phiVanChuyen: Number(shippingFee), // Ensure it's a number
      tongTien: Number(tongTien),
      tongTienSauGiam: Number(tongTienSauGiam),
      ghiChu: String(options.notes ?? ""),
      trangThai: true,
      deleted: false,
      createAt: createdAt,
    };

    const response = await createOrder(orderRequest);
    const order = response.data as OrderResponse;

    if (!order?.id) {
      throw new Error("Không nhận được ID hoá đơn từ máy chủ");
    }

    // Backend API already creates hoaDonChiTiet items from the request payload
    // No need to call addProductToCart separately - it would create duplicates!
    // The items are already persisted via the createOrder() call above

    return order;
  } catch (error: any) {
    console.error("[ORDER] Error creating order:", error.message);
    if (error.response) {
      console.error("[ORDER] Backend error status:", error.response.status);
      console.error(
        "[ORDER] Backend error data:",
        JSON.stringify(error.response.data, null, 2)
      );
      console.error("[ORDER] Full response:", error.response);
    } else if (error.request) {
      console.error("[ORDER] No response from backend:", error.request);
    } else {
      console.error("[ORDER] Error details:", error);
    }
    return null;
  }
}
