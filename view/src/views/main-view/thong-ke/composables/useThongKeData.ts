import { ref } from 'vue'
import axios from 'axios'
import { fetchHoaDonList, type HoaDonApiModel } from '@/api/hoa-don'
import { getBienTheSanPhamList, type BienTheSanPham } from '@/api/san-pham/bien-the'
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
      // Use official API helper (returns envelope-normalized data)
      const hoaDon = await fetchHoaDonList()
      danhSachHoaDon.value = Array.isArray(hoaDon) ? (hoaDon as unknown as HoaDon[]) : []
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
      danhSachSanPham.value = Array.isArray(sanPham) ? (sanPham as SanPham[]) : []
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
      // Use official product variant API
      const res = await getBienTheSanPhamList(0)
      const chiTietSanPham = res.data ?? []
      danhSachChiTietSanPham.value = Array.isArray(chiTietSanPham) ? (chiTietSanPham as unknown as ChiTietSanPham[]) : []
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
