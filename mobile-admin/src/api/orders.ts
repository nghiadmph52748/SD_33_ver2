import client from './client'

export interface OrderModel {
  id: number
  idKhachHang?: number
  idPhieuGiamGia?: number
  idNhanVien?: number
  idPhuongThucThanhToan?: number
  idTrangThaiDonHang?: number
  phiVanChuyen?: number
  tongTien?: number
  tongTienSauGiam?: number
  tenNguoiNhan?: string
  diaChiNhanHang?: string
  soDienThoaiNguoiNhan?: string
  emailNguoiNhan?: string
  ngayTao?: string
  ngayThanhToan?: string
  loaiDon?: boolean
  trangThai?: boolean
  deleted?: boolean
  ghiChu?: string
  createAt?: string
  createBy?: number
  updateAt?: string
  updateBy?: number
  tenNhanVien?: string
  maNhanVien?: string
  tenPhieuGiamGia?: string
  maPhieuGiamGia?: string
}

export const fetchOrders = () =>
  client.get<OrderModel[], OrderModel[]>('/api/hoa-don-management/playlist')

export const fetchOrderById = (id: number) =>
  client.get<OrderModel, OrderModel>(`/api/hoa-don-management/${id}`)
