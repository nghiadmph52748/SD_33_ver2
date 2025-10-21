import * as XLSX from 'xlsx'
import type { DuLieuBangChiTiet, SanPhamBanChayNhat, SanPhamSapHetHang } from '../types/thong-ke.types'

export function useXuatExcel() {
  const dinhDangTien = (soTien: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(soTien)
  }

  const xuatBaoCaoThongKe = (
    duLieuBangChiTiet: DuLieuBangChiTiet[],
    sanPhamBanChayNhat: SanPhamBanChayNhat[],
    sanPhamSapHetHang: SanPhamSapHetHang[],
    tongDoanhThu: number,
    tongDonHang: number,
    doanhThuHomNay: number,
    doanhThuTuan: number,
    doanhThuThang: number,
    doanhThuNam: number
  ) => {
    try {
      // Tạo workbook mới
      const workbook = XLSX.utils.book_new()

      // Sheet 1: Bảng thống kê chi tiết
      const duLieuChiTiet = duLieuBangChiTiet.map((item, index) => ({
        STT: index + 1,
        'Thời gian': item.thoiGian,
        'Doanh thu': item.doanhThu,
        'Số đơn hàng': item.soDonHang,
        'Giá trị TB/đơn': item.giaTriTB,
        'Tăng trưởng': item.tangTruong,
        'Trạng thái': item.trangThai,
      }))

      const sheetChiTiet = XLSX.utils.json_to_sheet(duLieuChiTiet)
      XLSX.utils.book_append_sheet(workbook, sheetChiTiet, 'Thống kê chi tiết')

      // Sheet 2: Top sản phẩm bán chạy
      const duLieuTopSanPham = sanPhamBanChayNhat.map((item, index) => ({
        STT: index + 1,
        'Tên sản phẩm': item.tenSanPham,
        'Giá bán': item.giaBan,
        'Số lượng đã bán': item.soLuongDaBan,
      }))

      const sheetTopSanPham = XLSX.utils.json_to_sheet(duLieuTopSanPham)
      XLSX.utils.book_append_sheet(workbook, sheetTopSanPham, 'Top sản phẩm bán chạy')

      // Sheet 3: Sản phẩm sắp hết hàng
      const duLieuSapHetHang = sanPhamSapHetHang.map((item, index) => ({
        STT: index + 1,
        'Tên sản phẩm': item.tenSanPham,
        'Giá bán': item.giaBan,
        'Số lượng tồn': item.soLuongTon,
        'Trạng thái': item.trangThai,
      }))

      const sheetSapHetHang = XLSX.utils.json_to_sheet(duLieuSapHetHang)
      XLSX.utils.book_append_sheet(workbook, sheetSapHetHang, 'Sản phẩm sắp hết hàng')

      // Sheet 4: Tổng quan thống kê
      const duLieuTongQuan = [
        { 'Chỉ số': 'Tổng doanh thu', 'Giá trị': dinhDangTien(tongDoanhThu) },
        { 'Chỉ số': 'Tổng đơn hàng', 'Giá trị': tongDonHang },
        { 'Chỉ số': 'Doanh thu hôm nay', 'Giá trị': dinhDangTien(doanhThuHomNay) },
        { 'Chỉ số': 'Doanh thu tuần này', 'Giá trị': dinhDangTien(doanhThuTuan) },
        { 'Chỉ số': 'Doanh thu tháng này', 'Giá trị': dinhDangTien(doanhThuThang) },
        { 'Chỉ số': 'Doanh thu năm này', 'Giá trị': dinhDangTien(doanhThuNam) },
      ]

      const sheetTongQuan = XLSX.utils.json_to_sheet(duLieuTongQuan)
      XLSX.utils.book_append_sheet(workbook, sheetTongQuan, 'Tổng quan')

      // Xuất file
      const tenFile = `BaoCaoThongKe_${new Date().toISOString().split('T')[0]}.xlsx`
      XLSX.writeFile(workbook, tenFile)

      return { success: true, message: 'Xuất Excel thành công!' }
    } catch (error) {
      console.error('Lỗi khi xuất Excel:', error)
      return { success: false, message: 'Lỗi khi xuất Excel' }
    }
  }

  return {
    xuatBaoCaoThongKe,
    dinhDangTien,
  }
}
