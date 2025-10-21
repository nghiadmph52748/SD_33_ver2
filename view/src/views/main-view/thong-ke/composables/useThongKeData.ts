import { ref } from 'vue'
import axios from 'axios'
import type { HoaDon, SanPham, ChiTietSanPham } from '../types/thong-ke.types'

export function useThongKeData() {
  const danhSachHoaDon = ref<HoaDon[]>([])
  const danhSachSanPham = ref<SanPham[]>([])
  const danhSachChiTietSanPham = ref<ChiTietSanPham[]>([])
  const dangTai = ref(false)
  const loi = ref<string | null>(null)

  const layDanhSachHoaDon = async () => {
    try {
      dangTai.value = true
      loi.value = null
      const res = await axios.get('/api/hoa-don-management/playlist')
      const hoaDon = res.data ?? []
      danhSachHoaDon.value = Array.isArray(hoaDon) ? hoaDon : []
    } catch (error) {
      loi.value = 'Không thể tải danh sách hóa đơn'
      danhSachHoaDon.value = []
      console.error('Lỗi khi tải hóa đơn:', error)
    } finally {
      dangTai.value = false
    }
  }

  const layDanhSachSanPham = async () => {
    try {
      dangTai.value = true
      loi.value = null
      const res = await axios.get('/api/san-pham-management/playlist')
      const sanPham = res.data ?? []
      danhSachSanPham.value = Array.isArray(sanPham) ? sanPham : []
    } catch (error) {
      loi.value = 'Không thể tải danh sách sản phẩm'
      danhSachSanPham.value = []
      console.error('Lỗi khi tải sản phẩm:', error)
    } finally {
      dangTai.value = false
    }
  }

  const layDanhSachChiTietSanPham = async () => {
    try {
      dangTai.value = true
      loi.value = null
      const res = await axios.get('/api/chi-tiet-san-pham-management/playlist')
      const chiTietSanPham = res.data ?? []
      danhSachChiTietSanPham.value = Array.isArray(chiTietSanPham) ? chiTietSanPham : []
    } catch (error) {
      loi.value = 'Không thể tải chi tiết sản phẩm'
      danhSachChiTietSanPham.value = []
      console.error('Lỗi khi tải chi tiết sản phẩm:', error)
    } finally {
      dangTai.value = false
    }
  }

  const taiToanBoDuLieu = async () => {
    await Promise.all([layDanhSachHoaDon(), layDanhSachSanPham(), layDanhSachChiTietSanPham()])
  }

  return {
    // State
    danhSachHoaDon,
    danhSachSanPham,
    danhSachChiTietSanPham,
    dangTai,
    loi,

    // Methods
    layDanhSachHoaDon,
    layDanhSachSanPham,
    layDanhSachChiTietSanPham,
    taiToanBoDuLieu,
  }
}
