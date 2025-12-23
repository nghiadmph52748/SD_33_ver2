// Kiểu dữ liệu cho module thống kê

export interface HoaDon {
  id?: number
  maHoaDon?: string
  ngayTao: string
  ngayThanhToan?: string
  tongTien: number
  tongTienSauGiam?: number
  trangThai: boolean
  deleted?: boolean
  loaiDon?: boolean
  hoaDonChiTiets?: HoaDonChiTiet[]
  items?: ChiTietSanPhamHoaDon[]
}

export interface HoaDonChiTiet {
  id?: number
  soLuong: number
  giaBan: number
  thanhTien?: number
  idChiTietSanPham?: {
    id?: number
    idSanPham?: {
      tenSanPham: string
      anh?: string
    }
  }
}

export interface ChiTietSanPhamHoaDon {
  tenSanPham: string
  soLuong: number
  giaBan: number
  thanhTien?: number
}

export interface SanPham {
  id: number
  tenSanPham: string
  anh?: string
  giaBan: number
}

export interface ChiTietSanPham {
  id: number
  maChiTietSanPham?: string
  tenSanPham?: string
  tenSanPhamChiTiet?: string
  soLuong?: number
  soLuongTon?: number
  giaBan: number
  anh?: string
  anhSanPham?: string | string[]
  hinhAnh?: string
  idSanPham?: SanPham
  idMauSac?: {
    tenMauSac: string
  }
  idKichThuoc?: {
    tenKichThuoc: string
  }
}

export interface DuLieuThongKe {
  productsSold: number
  orders: number
  revenue: number
  soTienGiamGia: number
  tangTruong: number
  title: string
}

export interface DuLieuDoanhThu {
  month: string
  revenue: number
}

export interface SanPhamBanChay {
  name: string
  value: number
  revenue: number
  image?: string
}

export interface DuLieuTrangThaiDonHang {
  name: string
  value: number
  color: string
}

export interface DuLieuDanhMuc {
  name: string
  value: number
  color: string
  products?: Array<{ name: string; quantity: number }>
}

export interface SanPhamBanChayNhat {
  id: number
  tenSanPham: string
  anh: string
  giaBan: number
  soLuongDaBan: number
}

export interface SanPhamSapHetHang {
  id: number
  tenSanPham: string
  anh: string
  giaBan: number
  soLuongTon: number
  trangThai: string
}

export interface DuLieuBangChiTiet {
  thoiGian: string
  doanhThu: number
  soDonHang: number
  giaTriTB: number
  soTienGiamGia: number
  tangTruong: number
  trangThai: string
}

export type KhoangThoiGian = 'today' | 'week' | 'month' | 'year' | 'custom'
export type LoaiBieuDo = 'line' | 'bar'
export type KyThongKe = 'day' | 'month' | 'year'
