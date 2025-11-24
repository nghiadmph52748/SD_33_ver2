import { ref, computed, watch, type Ref } from 'vue'
import type {
  HoaDon,
  ChiTietSanPham,
  DuLieuThongKe,
  DuLieuDoanhThu,
  SanPhamBanChay,
  DuLieuTrangThaiDonHang,
  DuLieuDanhMuc,
  SanPhamBanChayNhat,
  SanPhamSapHetHang,
  DuLieuBangChiTiet,
  KhoangThoiGian,
} from '../types/thong-ke.types'

export function useTinhToanThongKe(
  danhSachHoaDon: Ref<HoaDon[]>,
  danhSachChiTietSanPham: Ref<ChiTietSanPham[]>,
  kyDoanhThu: Ref<string>,
  t: (key: string, params?: Record<string, any>) => string
) {
  // ============= HELPER FUNCTIONS =============
  // Chỉ tính doanh thu khi đơn hàng đã được thanh toán (có ngayThanhToan)
  const laHoaDonDaThanhToan = (hoaDon: HoaDon) => {
    // Kiểm tra có ngày thanh toán và trạng thái hoàn thành
    return !!hoaDon?.ngayThanhToan && hoaDon?.trangThai === true
  }

  // Tính tổng tiền gốc (trước khi giảm giá)
  const layTongTienGocHoaDon = (hoaDon: HoaDon) => {
    const soTien = Number(hoaDon?.tongTien ?? 0)
    return Number.isNaN(soTien) ? 0 : soTien
  }

  // Tính số tiền đã giảm giá
  const laySoTienGiamGiaHoaDon = (hoaDon: HoaDon) => {
    const tongTien = Number(hoaDon?.tongTien ?? 0)
    const tongTienSauGiam = Number(hoaDon?.tongTienSauGiam ?? hoaDon?.tongTien ?? 0)
    const soTienGiam = tongTien - tongTienSauGiam
    return Number.isNaN(soTienGiam) ? 0 : Math.max(0, soTienGiam)
  }

  // Tính doanh thu thực (tổng tiền - tiền giảm giá)
  const layDoanhThuThucHoaDon = (hoaDon: HoaDon) => {
    const tongTienGoc = layTongTienGocHoaDon(hoaDon)
    const tienGiamGia = laySoTienGiamGiaHoaDon(hoaDon)
    return tongTienGoc - tienGiamGia
  }

  const layChiTietHoaDon = (hoaDon: HoaDon) => {
    if (hoaDon.hoaDonChiTiets && Array.isArray(hoaDon.hoaDonChiTiets) && hoaDon.hoaDonChiTiets.length > 0) {
      return hoaDon.hoaDonChiTiets
    }
    if (hoaDon.items && Array.isArray(hoaDon.items) && hoaDon.items.length > 0) {
      return hoaDon.items as any[]
    }
    return []
  }

  const tinhSanPhamDaBan = (danhSachHoaDon: HoaDon[]) => {
    return danhSachHoaDon.reduce((tong: number, hoaDon: HoaDon) => {
      const chiTiet = layChiTietHoaDon(hoaDon)
      if (chiTiet.length === 0) return tong
      return (
        tong +
        chiTiet.reduce((sum: number, item: any) => {
          return sum + (Number(item?.soLuong) || 0)
        }, 0)
      )
    }, 0)
  }

  // Cập nhật hàm tính doanh thu thực
  const tinhDoanhThuThuc = (danhSachHoaDon: HoaDon[]) => {
    return danhSachHoaDon
      .filter((hoaDon) => laHoaDonDaThanhToan(hoaDon))
      .reduce((tong: number, hoaDon: HoaDon) => {
        return tong + layDoanhThuThucHoaDon(hoaDon)
      }, 0)
  }

  // Cập nhật hàm tính tổng tiền gốc
  const tinhTongTienGoc = (danhSachHoaDon: HoaDon[]) => {
    return danhSachHoaDon
      .filter((hoaDon) => laHoaDonDaThanhToan(hoaDon))
      .reduce((tong: number, hoaDon: HoaDon) => {
        return tong + layTongTienGocHoaDon(hoaDon)
      }, 0)
  }

  const tinhSoTienGiamGia = (danhSachHoaDon: HoaDon[]) => {
    return danhSachHoaDon
      .filter((hoaDon) => laHoaDonDaThanhToan(hoaDon))
      .reduce((tong: number, hoaDon: HoaDon) => {
        return tong + laySoTienGiamGiaHoaDon(hoaDon)
      }, 0)
  }

  // ============= DỮ LIỆU THỐNG KÊ CƠ BẢN =============
  const duLieuHomNay = ref<DuLieuThongKe>({
    productsSold: 0,
    orders: 0,
    revenue: 0,
    soTienGiamGia: 0,
    tangTruong: 0,
    title: t('thongKe.time.today'),
  })

  const duLieuTuan = ref<DuLieuThongKe>({
    productsSold: 0,
    orders: 0,
    revenue: 0,
    soTienGiamGia: 0,
    tangTruong: 0,
    title: t('thongKe.time.thisWeek'),
  })

  const duLieuThang = ref<DuLieuThongKe>({
    productsSold: 0,
    orders: 0,
    revenue: 0,
    soTienGiamGia: 0,
    tangTruong: 0,
    title: t('thongKe.time.thisMonth'),
  })

  const duLieuNam = ref<DuLieuThongKe>({
    productsSold: 0,
    orders: 0,
    revenue: 0,
    soTienGiamGia: 0,
    tangTruong: 0,
    title: t('thongKe.time.thisYear'),
  })

  const duLieuTuyChon = ref<DuLieuThongKe>({
    productsSold: 0,
    orders: 0,
    revenue: 0,
    soTienGiamGia: 0,
    tangTruong: 0,
    title: t('thongKe.time.custom'),
  })

  // ============= TÍNH TOÁN DỮ LIỆU THEO THỜI GIAN =============
  const layDuLieuHomNay = () => {
    const homNay = new Date()
    homNay.setHours(0, 0, 0, 0)
    const ngayMai = new Date(homNay)
    ngayMai.setDate(ngayMai.getDate() + 1)

    const hoaDonHomNay = danhSachHoaDon.value.filter((hoaDon) => {
      if (!hoaDon.ngayTao) return false
      const ngayHoaDon = new Date(hoaDon.ngayTao)
      return ngayHoaDon >= homNay && ngayHoaDon < ngayMai
    })

    // Tính hôm qua để so sánh
    const homQua = new Date(homNay)
    homQua.setDate(homQua.getDate() - 1)
    const hoaDonHomQua = danhSachHoaDon.value.filter((hoaDon) => {
      if (!hoaDon.ngayTao) return false
      const ngayHoaDon = new Date(hoaDon.ngayTao)
      return ngayHoaDon >= homQua && ngayHoaDon < homNay
    })
    const doanhThuHomQua = tinhDoanhThuThuc(hoaDonHomQua)
    const doanhThuHomNay = tinhDoanhThuThuc(hoaDonHomNay)
    const tangTruong = doanhThuHomQua > 0 
      ? Math.round(((doanhThuHomNay - doanhThuHomQua) / doanhThuHomQua) * 100 * 100) / 100
      : (doanhThuHomNay > 0 ? 100 : 0)

    return {
      orders: hoaDonHomNay,
      revenue: doanhThuHomNay,
      soTienGiamGia: tinhSoTienGiamGia(hoaDonHomNay),
      orderCount: hoaDonHomNay.length,
      tangTruong,
    }
  }

  const layDuLieuTuan = () => {
    const hienTai = new Date()
    const dauTuan = new Date(hienTai)
    dauTuan.setDate(hienTai.getDate() - hienTai.getDay())
    dauTuan.setHours(0, 0, 0, 0)
    const cuoiTuan = new Date(dauTuan)
    cuoiTuan.setDate(dauTuan.getDate() + 7)

    const hoaDonTuan = danhSachHoaDon.value.filter((hoaDon) => {
      if (!hoaDon.ngayTao) return false
      const ngayHoaDon = new Date(hoaDon.ngayTao)
      return ngayHoaDon >= dauTuan && ngayHoaDon < cuoiTuan
    })

    // Tính tuần trước để so sánh
    const dauTuanTruoc = new Date(dauTuan)
    dauTuanTruoc.setDate(dauTuanTruoc.getDate() - 7)
    const cuoiTuanTruoc = new Date(dauTuan)
    const hoaDonTuanTruoc = danhSachHoaDon.value.filter((hoaDon) => {
      if (!hoaDon.ngayTao) return false
      const ngayHoaDon = new Date(hoaDon.ngayTao)
      return ngayHoaDon >= dauTuanTruoc && ngayHoaDon < cuoiTuanTruoc
    })
    const doanhThuTuanTruoc = tinhDoanhThuThuc(hoaDonTuanTruoc)
    const doanhThuTuan = tinhDoanhThuThuc(hoaDonTuan)
    const tangTruong = doanhThuTuanTruoc > 0 
      ? Math.round(((doanhThuTuan - doanhThuTuanTruoc) / doanhThuTuanTruoc) * 100 * 100) / 100
      : (doanhThuTuan > 0 ? 100 : 0)

    return {
      orders: hoaDonTuan,
      revenue: doanhThuTuan,
      soTienGiamGia: tinhSoTienGiamGia(hoaDonTuan),
      orderCount: hoaDonTuan.length,
      tangTruong,
    }
  }

  const layDuLieuThang = () => {
    const hienTai = new Date()
    const dauThang = new Date(hienTai.getFullYear(), hienTai.getMonth(), 1)
    const cuoiThang = new Date(hienTai.getFullYear(), hienTai.getMonth() + 1, 1)

    const hoaDonThang = danhSachHoaDon.value.filter((hoaDon) => {
      if (!hoaDon.ngayTao) return false
      const ngayHoaDon = new Date(hoaDon.ngayTao)
      return ngayHoaDon >= dauThang && ngayHoaDon < cuoiThang
    })

    // Tính tháng trước để so sánh
    const dauThangTruoc = new Date(hienTai.getFullYear(), hienTai.getMonth() - 1, 1)
    const cuoiThangTruoc = new Date(hienTai.getFullYear(), hienTai.getMonth(), 1)
    const hoaDonThangTruoc = danhSachHoaDon.value.filter((hoaDon) => {
      if (!hoaDon.ngayTao) return false
      const ngayHoaDon = new Date(hoaDon.ngayTao)
      return ngayHoaDon >= dauThangTruoc && ngayHoaDon < cuoiThangTruoc
    })
    const doanhThuThangTruoc = tinhDoanhThuThuc(hoaDonThangTruoc)
    const doanhThuThang = tinhDoanhThuThuc(hoaDonThang)
    const tangTruong = doanhThuThangTruoc > 0 
      ? Math.round(((doanhThuThang - doanhThuThangTruoc) / doanhThuThangTruoc) * 100 * 100) / 100
      : (doanhThuThang > 0 ? 100 : 0)

    return {
      orders: hoaDonThang,
      revenue: doanhThuThang,
      soTienGiamGia: tinhSoTienGiamGia(hoaDonThang),
      orderCount: hoaDonThang.length,
      tangTruong,
    }
  }

  const layDuLieuNam = () => {
    const hienTai = new Date()
    const dauNam = new Date(hienTai.getFullYear(), 0, 1)
    const cuoiNam = new Date(hienTai.getFullYear() + 1, 0, 1)

    const hoaDonNam = danhSachHoaDon.value.filter((hoaDon) => {
      if (!hoaDon.ngayTao) return false
      const ngayHoaDon = new Date(hoaDon.ngayTao)
      return ngayHoaDon >= dauNam && ngayHoaDon < cuoiNam
    })

    // Tính năm trước để so sánh
    const dauNamTruoc = new Date(hienTai.getFullYear() - 1, 0, 1)
    const cuoiNamTruoc = new Date(hienTai.getFullYear(), 0, 1)
    const hoaDonNamTruoc = danhSachHoaDon.value.filter((hoaDon) => {
      if (!hoaDon.ngayTao) return false
      const ngayHoaDon = new Date(hoaDon.ngayTao)
      return ngayHoaDon >= dauNamTruoc && ngayHoaDon < cuoiNamTruoc
    })
    const doanhThuNamTruoc = tinhDoanhThuThuc(hoaDonNamTruoc)
    const doanhThuNam = tinhDoanhThuThuc(hoaDonNam)
    const tangTruong = doanhThuNamTruoc > 0 
      ? Math.round(((doanhThuNam - doanhThuNamTruoc) / doanhThuNamTruoc) * 100 * 100) / 100
      : (doanhThuNam > 0 ? 100 : 0)

    return {
      orders: hoaDonNam,
      revenue: doanhThuNam,
      soTienGiamGia: tinhSoTienGiamGia(hoaDonNam),
      orderCount: hoaDonNam.length,
      tangTruong,
    }
  }

  // ============= DỮ LIỆU DOANH THU THEO THÁNG =============
  const duLieuDoanhThu = ref<DuLieuDoanhThu[]>([])

  const xayDungDuLieuDoanhThu = () => {
    const hienTai = new Date()
    const cacThang: { label: string; year: number; monthIndex: number }[] = []
    const soThang = kyDoanhThu.value === '12months' ? 12 : 6

    for (let i = soThang - 1; i >= 0; i -= 1) {
      const ngay = new Date(hienTai.getFullYear(), hienTai.getMonth() - i, 1)
      cacThang.push({
        label: t('thongKe.time.month', { month: ngay.getMonth() + 1 }),
        year: ngay.getFullYear(),
        monthIndex: ngay.getMonth(),
      })
    }

    const tongTien: number[] = new Array(cacThang.length).fill(0)
    danhSachHoaDon.value
      .filter((hd) => laHoaDonDaThanhToan(hd))
      .forEach((hd) => {
        const ngayStr = hd?.ngayThanhToan ?? hd?.ngayTao
        if (!ngayStr) return
        const ngay = new Date(ngayStr)
        if (Number.isNaN(ngay.getTime())) return
        const nam = ngay.getFullYear()
        const thang = ngay.getMonth()
        const viTri = cacThang.findIndex((t) => t.year === nam && t.monthIndex === thang)
        if (viTri >= 0) tongTien[viTri] += layTongTienGocHoaDon(hd)
      })

    duLieuDoanhThu.value = cacThang.map((t, idx) => ({ month: t.label, revenue: tongTien[idx] }))
  }

  // ============= DỮ LIỆU SẢN PHẨM BÁN CHẠY =============
  const duLieuSanPhamBanChay = ref<SanPhamBanChay[]>([])

  const xayDungDuLieuSanPhamBanChay = (kyThongKe: string = 'month') => {
    const map: Record<string, SanPhamBanChay> = {}
    const hienTai = new Date()
    
    // Lọc hóa đơn theo kỳ thống kê
    let hoaDonLoc: HoaDon[] = []
    
    switch (kyThongKe) {
      case 'week': {
        const dauTuan = new Date(hienTai)
        dauTuan.setDate(hienTai.getDate() - hienTai.getDay())
        dauTuan.setHours(0, 0, 0, 0)
        const cuoiTuan = new Date(dauTuan)
        cuoiTuan.setDate(dauTuan.getDate() + 7)
        hoaDonLoc = danhSachHoaDon.value.filter((hd) => {
          const ngayStr = hd?.ngayThanhToan ?? hd?.ngayTao
          if (!ngayStr) return false
          const ngayHD = new Date(ngayStr)
          if (Number.isNaN(ngayHD.getTime())) return false
          ngayHD.setHours(0, 0, 0, 0)
          return ngayHD >= dauTuan && ngayHD < cuoiTuan
        })
        break
      }
      case 'month': {
        const dauThang = new Date(hienTai.getFullYear(), hienTai.getMonth(), 1)
        dauThang.setHours(0, 0, 0, 0)
        const cuoiThang = new Date(hienTai.getFullYear(), hienTai.getMonth() + 1, 1)
        cuoiThang.setHours(0, 0, 0, 0)
        hoaDonLoc = danhSachHoaDon.value.filter((hd) => {
          const ngayStr = hd?.ngayThanhToan ?? hd?.ngayTao
          if (!ngayStr) return false
          const ngayHD = new Date(ngayStr)
          if (Number.isNaN(ngayHD.getTime())) return false
          ngayHD.setHours(0, 0, 0, 0)
          return ngayHD >= dauThang && ngayHD < cuoiThang
        })
        break
      }
      case 'quarter1': {
        const dauQuy = new Date(hienTai.getFullYear(), 0, 1)
        dauQuy.setHours(0, 0, 0, 0)
        const cuoiQuy = new Date(hienTai.getFullYear(), 3, 1)
        cuoiQuy.setHours(0, 0, 0, 0)
        hoaDonLoc = danhSachHoaDon.value.filter((hd) => {
          const ngayStr = hd?.ngayThanhToan ?? hd?.ngayTao
          if (!ngayStr) return false
          const ngayHD = new Date(ngayStr)
          if (Number.isNaN(ngayHD.getTime())) return false
          ngayHD.setHours(0, 0, 0, 0)
          return ngayHD >= dauQuy && ngayHD < cuoiQuy
        })
        break
      }
      case 'quarter2': {
        const dauQuy = new Date(hienTai.getFullYear(), 3, 1)
        dauQuy.setHours(0, 0, 0, 0)
        const cuoiQuy = new Date(hienTai.getFullYear(), 6, 1)
        cuoiQuy.setHours(0, 0, 0, 0)
        hoaDonLoc = danhSachHoaDon.value.filter((hd) => {
          const ngayStr = hd?.ngayThanhToan ?? hd?.ngayTao
          if (!ngayStr) return false
          const ngayHD = new Date(ngayStr)
          if (Number.isNaN(ngayHD.getTime())) return false
          ngayHD.setHours(0, 0, 0, 0)
          return ngayHD >= dauQuy && ngayHD < cuoiQuy
        })
        break
      }
      case 'quarter3': {
        const dauQuy = new Date(hienTai.getFullYear(), 6, 1)
        dauQuy.setHours(0, 0, 0, 0)
        const cuoiQuy = new Date(hienTai.getFullYear(), 9, 1)
        cuoiQuy.setHours(0, 0, 0, 0)
        hoaDonLoc = danhSachHoaDon.value.filter((hd) => {
          const ngayStr = hd?.ngayThanhToan ?? hd?.ngayTao
          if (!ngayStr) return false
          const ngayHD = new Date(ngayStr)
          if (Number.isNaN(ngayHD.getTime())) return false
          ngayHD.setHours(0, 0, 0, 0)
          return ngayHD >= dauQuy && ngayHD < cuoiQuy
        })
        break
      }
      case 'quarter4': {
        const dauQuy = new Date(hienTai.getFullYear(), 9, 1)
        dauQuy.setHours(0, 0, 0, 0)
        const cuoiQuy = new Date(hienTai.getFullYear() + 1, 0, 1)
        cuoiQuy.setHours(0, 0, 0, 0)
        hoaDonLoc = danhSachHoaDon.value.filter((hd) => {
          const ngayStr = hd?.ngayThanhToan ?? hd?.ngayTao
          if (!ngayStr) return false
          const ngayHD = new Date(ngayStr)
          if (Number.isNaN(ngayHD.getTime())) return false
          ngayHD.setHours(0, 0, 0, 0)
          return ngayHD >= dauQuy && ngayHD < cuoiQuy
        })
        break
      }
      default: {
        hoaDonLoc = danhSachHoaDon.value
        break
      }
    }
    
    hoaDonLoc
      .filter((hd) => laHoaDonDaThanhToan(hd))
      .forEach((hd) => {
        const items = Array.isArray(hd.items) ? hd.items : []
        items.forEach((item: any) => {
          const ten = item?.tenSanPham ?? 'Không rõ'
          const soLuong = Number(item?.soLuong ?? 0)
          const doanhThuDong = Number(item?.thanhTien ?? Number(item?.giaBan ?? 0) * soLuong)
          if (!map[ten]) {
            map[ten] = { name: ten, value: 0, revenue: 0 }
          }
          map[ten].value += Number.isNaN(soLuong) ? 0 : soLuong
          map[ten].revenue += Number.isNaN(doanhThuDong) ? 0 : doanhThuDong
        })
      })

    duLieuSanPhamBanChay.value = Object.values(map)
      .sort((a, b) => b.value - a.value || b.revenue - a.revenue)
      .slice(0, 5)
  }

  // ============= DỮ LIỆU TRẠNG THÁI ĐƠN HÀNG =============
  const duLieuTrangThaiDonHang = ref<DuLieuTrangThaiDonHang[]>([
    { name: t('thongKe.status.pending'), value: 0, color: '#faad14' },
    { name: t('thongKe.status.processing'), value: 0, color: '#1890ff' },
    { name: t('thongKe.status.shipping'), value: 0, color: '#52c41a' },
    { name: t('thongKe.status.completed'), value: 0, color: '#13c2c2' },
    { name: t('thongKe.status.cancelled'), value: 0, color: '#ff4d4f' },
  ])

  const capNhatDuLieuTrangThai = (kyThongKe: string) => {
    const hienTai = new Date()
    let hoaDonLoc: HoaDon[] = []

    switch (kyThongKe) {
      case 'day': {
        const homNay = new Date(hienTai.getFullYear(), hienTai.getMonth(), hienTai.getDate())
        hoaDonLoc = danhSachHoaDon.value.filter((hd) => {
          const ngayHD = new Date(hd.ngayTao)
          return ngayHD >= homNay && ngayHD < new Date(homNay.getTime() + 24 * 60 * 60 * 1000)
        })
        break
      }
      case 'month': {
        const dauThang = new Date(hienTai.getFullYear(), hienTai.getMonth(), 1)
        const cuoiThang = new Date(hienTai.getFullYear(), hienTai.getMonth() + 1, 1)
        hoaDonLoc = danhSachHoaDon.value.filter((hd) => {
          const ngayHD = new Date(hd.ngayTao)
          return ngayHD >= dauThang && ngayHD < cuoiThang
        })
        break
      }
      case 'year': {
        const dauNam = new Date(hienTai.getFullYear(), 0, 1)
        const cuoiNam = new Date(hienTai.getFullYear() + 1, 0, 1)
        hoaDonLoc = danhSachHoaDon.value.filter((hd) => {
          const ngayHD = new Date(hd.ngayTao)
          return ngayHD >= dauNam && ngayHD < cuoiNam
        })
        break
      }
      default:
        hoaDonLoc = danhSachHoaDon.value
    }

    const pending = t('thongKe.status.pending')
    const processing = t('thongKe.status.processing')
    const shipping = t('thongKe.status.shipping')
    const completed = t('thongKe.status.completed')
    const cancelled = t('thongKe.status.cancelled')

    const demTrangThai: Record<string, number> = {
      [pending]: 0,
      [processing]: 0,
      [shipping]: 0,
      [completed]: 0,
      [cancelled]: 0,
    }

    hoaDonLoc.forEach((hd) => {
      if (hd.deleted === true) {
        demTrangThai[cancelled] += 1
      } else if (hd.ngayThanhToan && hd.trangThai === true) {
        demTrangThai[completed] += 1
      } else if (hd.trangThai === true && !hd.ngayThanhToan) {
        demTrangThai[processing] += 1
      } else if (hd.trangThai === false) {
        demTrangThai[pending] += 1
      } else {
        demTrangThai[shipping] += 1
      }
    })

    duLieuTrangThaiDonHang.value = [
      { name: pending, value: demTrangThai[pending], color: '#faad14' },
      { name: processing, value: demTrangThai[processing], color: '#1890ff' },
      { name: shipping, value: demTrangThai[shipping], color: '#52c41a' },
      { name: completed, value: demTrangThai[completed], color: '#13c2c2' },
      { name: cancelled, value: demTrangThai[cancelled], color: '#ff4d4f' },
    ]
  }

  // ============= DỮ LIỆU KÊNH PHÂN PHỐI =============
  const duLieuKenhPhanPhoi = ref<DuLieuTrangThaiDonHang[]>([
    { name: t('thongKe.channel.onlineValue'), value: 0, color: '#1890ff' },
    { name: t('thongKe.channel.offlineValue'), value: 0, color: '#52c41a' },
  ])

  const capNhatDuLieuKenhPhanPhoi = () => {
    let onlineCount = 0
    let offlineCount = 0

    danhSachHoaDon.value.forEach((hd) => {
      if (hd.loaiDon === true) {
        onlineCount += 1
      } else {
        offlineCount += 1
      }
    })

    duLieuKenhPhanPhoi.value = [
      { name: t('thongKe.channel.onlineValue'), value: onlineCount, color: '#1890ff' },
      { name: t('thongKe.channel.offlineValue'), value: offlineCount, color: '#52c41a' },
    ]
  }

  // ============= DỮ LIỆU DANH MỤC =============
  const duLieuDanhMuc = ref<DuLieuDanhMuc[]>([])

  const phanLoaiSanPham = (tenSanPham: string): string => {
    const ten = tenSanPham.toLowerCase()

    if (
      ten.includes('sneaker') ||
      ten.includes('giày thể thao') ||
      ten.includes('running') ||
      ten.includes('basketball') ||
      ten.includes('tennis')
    ) {
      return t('thongKe.category.sports')
    }
    if (ten.includes('chạy') || ten.includes('marathon') || ten.includes('jogging')) {
      return t('thongKe.category.running')
    }
    if (ten.includes('bóng đá') || ten.includes('football') || ten.includes('soccer')) {
      return t('thongKe.category.football')
    }
    if (ten.includes('bóng rổ') || ten.includes('basketball')) {
      return t('thongKe.category.basketball')
    }
    if (ten.includes('tennis') || ten.includes('quần vợt')) {
      return t('thongKe.category.tennis')
    }
    if (ten.includes('lifestyle') || ten.includes('casual') || ten.includes('street')) {
      return t('thongKe.category.lifestyle')
    }
    if (ten.includes('cao gót') || ten.includes('heels')) {
      return t('thongKe.category.heels')
    }
    if (ten.includes('boot') || ten.includes('bốt')) {
      return t('thongKe.category.boots')
    }
    if (ten.includes('sandal') || ten.includes('dép')) {
      return t('thongKe.category.sandals')
    }
    return t('thongKe.category.other')
  }

  const capNhatDuLieuDanhMuc = () => {
    const demDanhMuc = new Map<string, number>()

    danhSachHoaDon.value.forEach((hd) => {
      const chiTiet = layChiTietHoaDon(hd)
      if (chiTiet.length === 0) return
      chiTiet.forEach((item: any) => {
        const tenSP =
          item?.idChiTietSanPham?.idSanPham?.tenSanPham ||
          item?.sanPham?.tenSanPham ||
          item?.tenSanPham ||
          item?.tenSanPhamChiTiet
        if (!tenSP) return
        const danhMuc = phanLoaiSanPham(tenSP)
        const soLuong = Number(item?.soLuong || 0)
        const soLuongHopLe = Number.isNaN(soLuong) ? 0 : soLuong
        demDanhMuc.set(danhMuc, (demDanhMuc.get(danhMuc) || 0) + soLuongHopLe)
      })
    })

    const mauSac = ['#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1', '#13c2c2']
    duLieuDanhMuc.value = Array.from(demDanhMuc.entries())
      .map(([name, value]) => ({ name, value, color: '' }))
      .sort((a, b) => b.value - a.value)
      .slice(0, 6)
      .map((item, index) => ({ ...item, color: mauSac[index % mauSac.length] }))
  }

  // ============= SẢN PHẨM BÁN CHẠY NHẤT =============
  const sanPhamBanChayNhat = ref<SanPhamBanChayNhat[]>([])

  const capNhatSanPhamBanChayNhat = () => {
    if (duLieuSanPhamBanChay.value.length > 0) {
      sanPhamBanChayNhat.value = duLieuSanPhamBanChay.value.map((sp, index) => ({
        id: index + 1,
        tenSanPham: sp.name,
        anh: '/default-product.png',
        giaBan: sp.revenue / sp.value || 0,
        soLuongDaBan: sp.value,
      }))
    } else {
      sanPhamBanChayNhat.value = []
    }
  }

  // ============= SẢN PHẨM SẮP HẾT HÀNG =============
  const sanPhamSapHetHang = ref<SanPhamSapHetHang[]>([])

  const layTrangThaiKho = (soLuongTon: number): string => {
    if (soLuongTon === 0) return t('thongKe.lowStockTable.outOfStock')
    if (soLuongTon <= 2) return t('thongKe.lowStockTable.warning')
    return t('thongKe.lowStockTable.lowStock')
  }

  const capNhatSanPhamSapHetHang = () => {
    if (danhSachChiTietSanPham.value.length > 0) {
      const sanPhamThapTon = danhSachChiTietSanPham.value
        .filter((ct) => {
          const soLuongTon = ct.soLuongTon || ct.soLuong || 0
          return soLuongTon < 5
        })
        .map((ct, index) => {
          const sanPham = ct.idSanPham || {}
          const mauSac = ct.idMauSac?.tenMauSac || ''
          const kichThuoc = ct.idKichThuoc?.tenKichThuoc || ''

          let tenSanPham = ''
          if (ct.tenSanPhamChiTiet && ct.tenSanPhamChiTiet.trim()) {
            tenSanPham = ct.tenSanPhamChiTiet
          } else {
            const tenSP = sanPham.tenSanPham || 'Không rõ'
            const mauPart = mauSac ? ` + ${mauSac}` : ''
            const sizePart = kichThuoc ? ` + ${kichThuoc}` : ''
            tenSanPham = `${tenSP}${mauPart}${sizePart}`.trim()
          }

          return {
            id: index + 1,
            tenSanPham,
            anh: ct.anh || ct.anhSanPham || ct.hinhAnh || sanPham.anh || '/default-product.png',
            giaBan: ct.giaBan || sanPham.giaBan || 0,
            soLuongTon: ct.soLuongTon || ct.soLuong || 0,
            trangThai: layTrangThaiKho(ct.soLuongTon || ct.soLuong || 0),
          }
        })
        .sort((a, b) => a.soLuongTon - b.soLuongTon)

      sanPhamSapHetHang.value = sanPhamThapTon
    } else {
      sanPhamSapHetHang.value = []
    }
  }

  // ============= BẢNG THỐNG KÊ CHI TIẾT =============
  const duLieuBangChiTiet = ref<DuLieuBangChiTiet[]>([])

  const capNhatBangChiTiet = () => {
    const cacKyThongKe = [
      { key: 'today', label: t('thongKe.time.today'), getData: () => layDuLieuHomNay() },
      { key: 'week', label: t('thongKe.time.thisWeek'), getData: () => layDuLieuTuan() },
      { key: 'month', label: t('thongKe.time.thisMonth'), getData: () => layDuLieuThang() },
      { key: 'year', label: t('thongKe.time.thisYear'), getData: () => layDuLieuNam() },
    ]

    const duLieuBang = cacKyThongKe.map((ky) => {
      const data = ky.getData()
      const giaTriTB = data.orderCount > 0 ? data.revenue / data.orderCount : 0

      return {
        thoiGian: ky.label,
        doanhThu: data.revenue,
        soDonHang: data.orderCount,
        giaTriTB,
        soTienGiamGia: data.soTienGiamGia || 0,
        tangTruong: 0,
        trangThai: data.orderCount > 0 ? t('thongKe.detailTable.active') : t('thongKe.detailTable.inactive'),
      }
    })

    // Tính tăng trưởng
    for (let i = 0; i < duLieuBang.length; i += 1) {
      if (i === 0) {
        duLieuBang[i].tangTruong = 0
      } else {
        const doanhThuTruoc = duLieuBang[i - 1].doanhThu
        const doanhThuHienTai = duLieuBang[i].doanhThu
        if (doanhThuTruoc > 0) {
          duLieuBang[i].tangTruong = Math.round(((doanhThuHienTai - doanhThuTruoc) / doanhThuTruoc) * 100 * 100) / 100
        } else {
          duLieuBang[i].tangTruong = doanhThuHienTai > 0 ? 100 : 0
        }
      }
    }

    duLieuBangChiTiet.value = duLieuBang
  }

  // ============= CẬP NHẬT TẤT CẢ DỮ LIỆU =============
  const capNhatToanBoDuLieu = (kyThongKe: string = 'day') => {
    // Tính toán dữ liệu cơ bản
    const homNay = layDuLieuHomNay()
    duLieuHomNay.value = {
      productsSold: tinhSanPhamDaBan(homNay.orders),
      orders: homNay.orderCount,
      revenue: homNay.revenue,
      soTienGiamGia: homNay.soTienGiamGia || 0,
      tangTruong: homNay.tangTruong || 0,
      title: 'Hôm nay',
    }

    const tuan = layDuLieuTuan()
    duLieuTuan.value = {
      productsSold: tinhSanPhamDaBan(tuan.orders),
      orders: tuan.orderCount,
      revenue: tuan.revenue,
      soTienGiamGia: tuan.soTienGiamGia || 0,
      tangTruong: tuan.tangTruong || 0,
      title: 'Tuần này',
    }

    const thang = layDuLieuThang()
    duLieuThang.value = {
      productsSold: tinhSanPhamDaBan(thang.orders),
      orders: thang.orderCount,
      revenue: thang.revenue,
      soTienGiamGia: thang.soTienGiamGia || 0,
      tangTruong: thang.tangTruong || 0,
      title: 'Tháng này',
    }

    const nam = layDuLieuNam()
    duLieuNam.value = {
      productsSold: tinhSanPhamDaBan(nam.orders),
      orders: nam.orderCount,
      revenue: nam.revenue,
      soTienGiamGia: nam.soTienGiamGia || 0,
      tangTruong: nam.tangTruong || 0,
      title: 'Năm này',
    }

    // Cập nhật biểu đồ và bảng
    xayDungDuLieuDoanhThu()
    capNhatDuLieuTrangThai(kyThongKe)
    capNhatDuLieuKenhPhanPhoi()
    capNhatDuLieuDanhMuc()
    capNhatSanPhamSapHetHang()
    capNhatBangChiTiet()
    // xayDungDuLieuSanPhamBanChay() và capNhatSanPhamBanChayNhat() sẽ được gọi từ watch trong component
  }

  // Watch for data changes
  watch([danhSachHoaDon, danhSachChiTietSanPham, kyDoanhThu], () => {
    capNhatToanBoDuLieu()
  })

  return {
    // Dữ liệu thống kê cơ bản
    duLieuHomNay,
    duLieuTuan,
    duLieuThang,
    duLieuNam,
    duLieuTuyChon,

    // Dữ liệu biểu đồ
    duLieuDoanhThu,
    duLieuSanPhamBanChay,
    duLieuTrangThaiDonHang,
    duLieuKenhPhanPhoi,
    duLieuDanhMuc,

    // Dữ liệu bảng
    sanPhamBanChayNhat,
    sanPhamSapHetHang,
    duLieuBangChiTiet,

    // Methods
    capNhatToanBoDuLieu,
    capNhatDuLieuTrangThai,
    xayDungDuLieuSanPhamBanChay,
    capNhatSanPhamBanChayNhat,
    layTrangThaiKho,
  }
}
