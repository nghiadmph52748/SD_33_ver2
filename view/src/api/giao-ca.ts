import axios from 'axios'

/**
 * =============================
 * 🧩 CẤU TRÚC DỮ LIỆU GIAO CA
 * =============================
 * Ví dụ payload backend (Postman):
 * {
 *   id: 1,
 *   nhanVienNhanCa: { id: 1, tenNhanVien: "Nguyễn A", ... },
 *   nhanVienGiaoCa: { id: 2, tenNhanVien: "Trần B", ... },
 *   thoiGianBatDau: "2025-11-11T08:00:00",
 *   thoiGianKetThuc: "2025-11-11T17:00:00",
 *   tongTienMat: 1500000,
 *   tongTienChuyenKhoan: 2000000,
 *   tongTienKhac: 0,
 *   tongDoanhThu: 3500000,
 *   tienBanDau: 500000,
 *   tienPhatSinh: 100000,
 *   tongTienThucTe: 3600000,
 *   chenhlech: 100000,
 *   tienMatNopLai: 1500000,
 *   trangThai: "Đã kết thúc",
 *   ghiChu: "Ca làm việc ổn định, không vấn đề",
 *   createAt: "2025-11-11T08:00:00",
 *   updateAt: "2025-11-11T17:00:00"
 * }
 */

/** =============================
 * 👤 Kiểu dữ liệu Nhân Viên
 * ============================= */
export interface ApiNhanVien {
  id: number
  maNhanVien?: string
  tenNhanVien: string
  tenTaiKhoan?: string
  email?: string
  soDienThoai?: string
  anhNhanVien?: string
  gioiTinh?: boolean
}

/** =============================
 * ⏰ Kiểu dữ liệu Giao Ca
 * ============================= */
export interface ApiGiaoCa {
  id: number
  nguoiNhan: ApiNhanVien
  nguoiGiao?: ApiNhanVien | null
  thoiGianGiaoCa: string
  tongTienBanDau?: number
  tongTienCuoiCa?: number
  tongTienMat?: number | null
  tongTienChuyenKhoan?: number | null
  tongTienKhac?: number | null
  tongDoanhThu?: number | null
  tienPhatSinh?: number | null
  tongTienThucTe?: number | null
  chenhLech?: number | null
  tienMatNopLai?: number | null
  trangThai?: string
  ghiChu?: string | null
  createAt?: string | null
  updateAt?: string | null
}



/** =============================
 * 📊 Kiểu dữ liệu Thống Kê Giao Ca
 * ============================= */
export interface ApiThongKeGiaoCa {
  tongCaLam: number
  tongDoanhThu: number
  tongTienMat: number
  tongTienChuyenKhoan: number
  tongTienKhac: number
  tongChenhlech: number
  ngayBatDau?: string
  ngayKetThuc?: string
}

/**
 * =============================
 * 🚀 CÁC HÀM GỌI API GIAO CA
 * =============================
 */

/** Lấy danh sách tất cả giao ca */
export function getGiaoCa() {
  return axios.get<ApiGiaoCa[]>('/api/giao-ca')
}

/** Lấy giao ca theo ID */
export function getGiaoCaById(id: number) {
  return axios.get<ApiGiaoCa>(`/api/giao-ca/${id}`)
}

/** Tạo giao ca mới */
export function themGiaoCa(data: Omit<ApiGiaoCa, 'id'>) {
  return axios.post<ApiGiaoCa>('/api/giao-ca', data)
}

/** Cập nhật thông tin giao ca (ví dụ kết thúc ca) */
export function suaGiaoCa(id: number, data: Partial<ApiGiaoCa>) {
  return axios.put<ApiGiaoCa>(`/api/giao-ca/${id}`, data)
}

/** Xóa giao ca */
export function xoaGiaoCa(id: number) {
  return axios.delete(`/api/giao-ca/${id}`)
}

/** Tìm kiếm / lọc giao ca */
export function timKiemGiaoCa(params: {
  ngayBatDau?: string
  ngayKetThuc?: string
  nhanVienId?: number
  trangThai?: string
}) {
  // Nếu backend có /api/giao-ca/search thì dùng endpoint này,
  // nếu không backend đọc query param từ /api/giao-ca
  return axios.get<ApiGiaoCa[]>('/api/giao-ca/search', { params })
}

/** Lấy thống kê giao ca (tổng doanh thu, tiền mặt, chênh lệch,...) */
export function thongKeGiaoCa(params?: {
  ngayBatDau?: string
  ngayKetThuc?: string
}) {
  return axios.get<ApiThongKeGiaoCa>('/api/giao-ca/thong-ke', { params })
}

/** Kết thúc giao ca — endpoint riêng (nếu backend có) */
export function ketThucGiaoCa(id: number, data?: Partial<ApiGiaoCa>) {
  return axios.put<ApiGiaoCa>(`/api/giao-ca/${id}/ket-thuc`, data)
}
