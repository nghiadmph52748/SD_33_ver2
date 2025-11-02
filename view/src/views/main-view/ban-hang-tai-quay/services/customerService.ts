import { layDanhSachKhachHang, type KhachHangResponse } from '@/api/khach-hang'

export async function fetchCustomers(): Promise<KhachHangResponse[]> {
  const res = await layDanhSachKhachHang()
  return res?.data || []
}
