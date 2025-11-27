import { ref } from 'vue'
import axios from 'axios'
import { fetchHoaDonList, type HoaDonApiModel } from '@/api/hoa-don'
import { getBienTheSanPhamList, type BienTheSanPham } from '@/api/san-pham/bien-the'
import type { HoaDon, SanPham, ChiTietSanPham } from '../types/thong-ke.types'

const dinhDangNgay = (raw: unknown): string => {
  if (!raw) return ''
  if (raw instanceof Date) return raw.toISOString()
  if (Array.isArray(raw) && raw.length >= 3) {
    const [year, month, day] = raw
    const date = new Date(Number(year), Number(month) - 1, Number(day))
    return Number.isNaN(date.getTime()) ? '' : date.toISOString()
  }
  if (typeof raw === 'object') {
    const obj = raw as Record<string, any>
    if (typeof obj.year !== 'undefined' && (obj.monthValue || obj.month) && (obj.dayOfMonth || obj.day)) {
      const year = Number(obj.year)
      const month = Number(obj.monthValue ?? obj.month) - 1
      const day = Number(obj.dayOfMonth ?? obj.day)
      const date = new Date(year, month, day)
      return Number.isNaN(date.getTime()) ? '' : date.toISOString()
    }
  }
  if (typeof raw === 'string') {
    const trimmed = raw.trim()
    if (!trimmed) return ''
    if (trimmed.includes('T')) return trimmed
    return `${trimmed}T00:00:00`
  }
  return ''
}

const layChiTietTuHoaDon = (hoaDon: HoaDonApiModel) => {
  if (Array.isArray(hoaDon.items) && hoaDon.items.length > 0) return hoaDon.items
  if (Array.isArray(hoaDon.chiTietSanPham) && hoaDon.chiTietSanPham.length > 0) return hoaDon.chiTietSanPham
  return []
}

const tinhTongTienChiTiet = (chiTiet: any[]): number => {
  return chiTiet.reduce((sum, item) => {
    const line = Number(item?.thanhTien) || Number(item?.giaBan || 0) * Number(item?.soLuong || 0)
    return sum + (Number.isNaN(line) ? 0 : line)
  }, 0)
}

const chuanHoaHoaDon = (hoaDon: HoaDonApiModel): HoaDon => {
  const chiTiet = layChiTietTuHoaDon(hoaDon)
  const tongTuChiTiet = tinhTongTienChiTiet(chiTiet)
  const tongTienGoc = Number(hoaDon.tongTien || 0)
  const tongTienSauGiam = Number(hoaDon.tongTienSauGiam || 0)
  const tongChuan = tongTienGoc > 0 ? tongTienGoc : tongTuChiTiet || tongTienSauGiam
  const tongSauChuan = tongTienSauGiam > 0 ? tongTienSauGiam : tongTuChiTiet || tongTienGoc
  const ngayTao = dinhDangNgay(hoaDon.ngayTao || hoaDon.createAt || hoaDon.updateAt || hoaDon.ngayThanhToan)
  const ngayThanhToan = dinhDangNgay(hoaDon.ngayThanhToan)

  return {
    id: hoaDon.id,
    maHoaDon: hoaDon.maHoaDon || undefined,
    ngayTao: ngayTao || new Date().toISOString(),
    ngayThanhToan: ngayThanhToan || undefined,
    tongTien: tongChuan,
    tongTienSauGiam: tongSauChuan,
    trangThai: hoaDon.trangThai ?? false,
    deleted: hoaDon.deleted ?? false,
    loaiDon: hoaDon.loaiDon ?? undefined,
    hoaDonChiTiets: chiTiet as any,
    items: chiTiet as any,
  }
}

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
      const hoaDon = await fetchHoaDonList()
      danhSachHoaDon.value = Array.isArray(hoaDon) ? hoaDon.map((item) => chuanHoaHoaDon(item)) : []
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
