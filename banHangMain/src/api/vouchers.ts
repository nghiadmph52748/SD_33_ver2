import axios from "./interceptor";
import type { HttpResponse } from "./interceptor";

export interface Voucher {
  id: number;
  maPhieuGiamGia: string; // Voucher code
  tenPhieuGiamGia: string; // Voucher name
  moTa?: string; // Description
  loaiPhieuGiamGia: boolean; // true = fixed amount (số tiền VND), false = percentage (%)
  giaTriGiamGia: number; // fixed amount in VND or percentage (0-100)
  hoaDonToiThieu: number; // min order value to use voucher (from backend: hoa_don_toi_thieu)
  soTienToiDa?: number | null; // percentage vouchers: optional cap
  soLuongDung?: number; // Available quantity remaining (số lượng còn)
  soLuongDaDung?: number; // Already used count (số lượng đã dùng)
  ngayBatDau?: string; // Start date
  ngayKetThuc?: string; // End date
  trangThai?: boolean; // Active status
  moTa?: string; // Description
  deleted?: boolean; // Deleted flag
  featured?: boolean; // Public voucher flag (false = public, true = personal)
  personalUsed?: boolean; // Personal voucher: already used by customer
  createdAt?: string; // Creation date
  updatedAt?: string; // Updated date
}

export interface VoucherResponse {
  data: Voucher[];
  message?: string;
}

/**
 * Get all active vouchers for a customer
 * If customerId is 0, get public vouchers for guests
 */
export async function getActiveVouchersForCustomer(
  customerId: number
): Promise<Voucher[]> {
  try {
    let url: string;

    if (customerId === 0) {
      // For guests, get all public vouchers (same endpoint as POS uses for public)
      url = `/api/pos/coupons`;
    } else {
      // For logged-in customers, get their specific vouchers
      url = `/api/pos/coupons/for-customer/${customerId}`;
    }

    const response = await axios.get<HttpResponse<Voucher[]>>(url);
    // API returns { data: [], message: "...", success: true }
    // So we need to get response.data.data (first .data is axios response, second .data is our API response)
    const vouchersData = response.data.data || response.data;
    return Array.isArray(vouchersData) ? vouchersData : [];
  } catch (error: any) {
    console.error("Error fetching vouchers:", error);
    return [];
  }
}

/**
 * Get all available vouchers (admin view)
 */
export async function getAllVouchers(): Promise<Voucher[]> {
  try {
    const response = await axios.get<HttpResponse<Voucher[]>>(
      "/api/phieu-giam-gia-management/playlist"
    );
    return response.data.data || [];
  } catch (error) {
    console.error("Error fetching all vouchers:", error);
    return [];
  }
}

/**
 * Validate if voucher can be used for current order
 * Returns { valid: boolean, reason?: string }
 */
export function validateVoucherUsage(
  voucher: Voucher,
  subtotal: number
): { valid: boolean; reason?: string } {
  // Check if active
  if (voucher.trangThai === false || voucher.deleted) {
    return { valid: false, reason: "Phiếu giảm giá này đã hết hạn" };
  }

  // Check quantity
  if (!voucher.soLuongDung || voucher.soLuongDung <= 0) {
    return { valid: false, reason: "Phiếu giảm giá này đã hết lượt sử dụng" };
  }

  // Check minimum order value (hoaDonToiThieu from backend)
  if (subtotal < (voucher.hoaDonToiThieu ?? 0)) {
    const minText = new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(voucher.hoaDonToiThieu ?? 0);
    return {
      valid: false,
      reason: `Đơn hàng phải từ ${minText} trở lên`,
    };
  }

  // Check date range
  const now = new Date();
  if (voucher.ngayBatDau && new Date(voucher.ngayBatDau) > now) {
    return { valid: false, reason: "Phiếu giảm giá này chưa được áp dụng" };
  }
  if (voucher.ngayKetThuc && new Date(voucher.ngayKetThuc) < now) {
    return { valid: false, reason: "Phiếu giảm giá này đã hết hạn" };
  }

  return { valid: true };
}

/**
 * Calculate discount amount from voucher
 * loaiPhieuGiamGia:
 *   true = fixed amount discount (giaTriGiamGia is VND)
 *   false = percentage discount (giaTriGiamGia is 0-100)
 */
export function calculateVoucherDiscount(
  voucher: Voucher,
  subtotal: number,
  shippingFee: number
): number {
  // Validate usage conditions first
  const validation = validateVoucherUsage(voucher, subtotal);
  if (!validation.valid) {
    return 0;
  }

  if (voucher.loaiPhieuGiamGia) {
    // Fixed amount discount: cap at subtotal
    return Math.min(voucher.giaTriGiamGia, subtotal);
  } else {
    // Percentage discount: calculate percentage of subtotal
    const rawDiscount = Math.floor((subtotal * voucher.giaTriGiamGia) / 100);
    const cap =
      typeof voucher.soTienToiDa === "number" ? voucher.soTienToiDa : null;
    if (cap != null && cap > 0) {
      return Math.min(rawDiscount, cap);
    }
    return rawDiscount;
  }
}

/**
 * Get discount text description (short format for modal header)
 */
export function getVoucherDescription(voucher: Voucher): string {
  const discount = voucher.loaiPhieuGiamGia
    ? `${new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND",
      }).format(voucher.giaTriGiamGia)}`
    : `${voucher.giaTriGiamGia}%`;

  return `Giảm ${discount}`;
}

/**
 * Get detailed voucher info with all conditions
 */
export function getVoucherDetailedInfo(voucher: Voucher): {
  discount: string;
  type: string;
  minOrder: string;
  quantity: string;
  conditions: string[];
} {
  // Discount display
  const discount = voucher.loaiPhieuGiamGia
    ? `Giảm ${new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND",
      }).format(voucher.giaTriGiamGia)}`
    : `Giảm ${voucher.giaTriGiamGia}%`;

  // Discount type
  const type = voucher.loaiPhieuGiamGia ? "Giảm số tiền" : "Giảm phần trăm";

  // Minimum order
  const minOrder =
    (voucher.hoaDonToiThieu ?? 0) > 0
      ? new Intl.NumberFormat("vi-VN", {
          style: "currency",
          currency: "VND",
        }).format(voucher.hoaDonToiThieu ?? 0)
      : "Không";

  // Available quantity
  const quantity = `${voucher.soLuongDung ?? 0} còn`;

  // All conditions
  const conditions: string[] = [];
  if ((voucher.hoaDonToiThieu ?? 0) > 0) {
    conditions.push(
      `Tối thiểu ${new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND",
      }).format(voucher.hoaDonToiThieu ?? 0)}`
    );
  }

  return {
    discount,
    type,
    minOrder,
    quantity,
    conditions,
  };
}

/**
 * Get voucher by code
 * Backend: GET /api/phieu-giam-gia-management/detail/code/{code}
 */
export async function getVoucherByCode(code: string): Promise<Voucher | null> {
  try {
    if (!code || !code.trim()) return null;
    const res = await axios.get<HttpResponse<Voucher>>(
      `/api/phieu-giam-gia-management/detail/code/${encodeURIComponent(
        code.trim()
      )}`
    );
    const data = (res.data as any)?.data ?? (res.data as any);
    return (data as Voucher) || null;
  } catch (error) {
    console.error("Error fetching voucher by code:", error);
    return null;
  }
}
