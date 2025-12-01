import axios from 'axios'

/**
 * Kiểu dữ liệu phản ánh chính xác payload từ Postman bạn gửi:
 * {
 *   id: number,
 *   nhanVien: { id, tenNhanVien, ... },
 *   caLamViec: { id, tenCa, thoiGianBatDau, thoiGianKetThuc, ... },
 *   ngayLamViec: string,
 *   trangThai: string,
 *   ghiChu?: string | null
 * }
 */

export interface ApiNhanVien {
  id: number
  idQuyenHan?: any
  maNhanVien?: string
  tenNhanVien: string
  tenTaiKhoan?: string
  matKhau?: string
  email?: string
  soDienThoai?: string
  anhNhanVien?: string
  ngaySinh?: string
  ghiChu?: string | null
  thanhPho?: string
  quan?: string
  phuong?: string
  diaChiCuThe?: string
  gioiTinh?: boolean
  cccd?: string
  trangThai?: boolean
  deleted?: boolean
  createAt?: string | null
  createBy?: string | null
  updateAt?: string | null
  updateBy?: string | null
  resetToken?: string | null
  resetTokenExpiry?: string | null
  hoaDons?: any[]
}

export interface ApiCaLamViec {
  id: number
  tenCa: string
  thoiGianBatDau: string
  thoiGianKetThuc: string
  ghiChu?: string | null
  trangThai?: boolean
}

export interface ApiLichLamViec {
  id: number
  nhanVien: ApiNhanVien
  caLamViec: ApiCaLamViec
  ngayLamViec: string
  trangThai: string // "Làm việc", "Nghỉ phép", ...
  ghiChu?: string | null
}

/**
 * Gọi API: trả về AxiosResponse<ApiLichLamViec[]>
 * -> Trong component hiện tại bạn làm: const res = await getLichLamViec(); const list = res.data ?? []
 * Điều này sẽ tiếp tục hoạt động.
 */

export function getLichLamViec() {
  return axios.get<ApiLichLamViec[]>('/api/lich-lam-viec')
}

export function getLichLamViecById(id: number) {
  return axios.get<ApiLichLamViec>(`/api/lich-lam-viec/${id}`)
}

export function themLichLamViec(data: Omit<ApiLichLamViec, 'id'>) {
  return axios.post<ApiLichLamViec>('/api/lich-lam-viec', data)
}

export function suaLichLamViec(id: number, data: Partial<ApiLichLamViec>) {
  return axios.put<ApiLichLamViec>(`/api/lich-lam-viec/${id}`, data)
}

export function timKiemLichLamViec(params: { ngayLamViec?: string; nhanVienId?: number }) {
  // Nếu backend thực sự có endpoint /search, giữ; nếu không, đổi thành query trên /api/lich-lam-viec
  return axios.get<ApiLichLamViec[]>('/api/lich-lam-viec/search', { params })
}

/**
 * Kiểm tra xem nhân viên có lịch trùng giờ trong ngày không
 */
export async function checkScheduleConflict(nhanVienId: number, caLamViecId: number, ngayLamViec: string, excludeScheduleId?: number): Promise<boolean> {
  try {
    const res = await getLichLamViec()
    const list = Array.isArray(res.data) ? res.data : Array.isArray(res) ? res : []
    
    const existingSchedules = list.filter((item: any) => {
      const empId = item.nhanVien?.id || item.nhanVienId
      const itemDate = item.ngayLamViec || item.ngayLam
      const existingCaId = item.caLamViec?.id || item.caLamViecId
      
      if (excludeScheduleId && item.id === excludeScheduleId) return false
      
      return empId === nhanVienId && itemDate === ngayLamViec && existingCaId === caLamViecId
    })
    
    return existingSchedules.length > 0
  } catch (err) {
    console.warn('Error checking schedule conflict:', err)
    return false
  }
}
