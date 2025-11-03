import client from './client'

export interface AddressModel {
  tenDiaChi?: string
  thanhPho: string
  quan: string
  phuong: string
  diaChiCuThe: string
  macDinh: boolean
}

export interface CustomerModel {
  id: number
  maKhachHang: string
  tenKhachHang: string
  tenTaiKhoan: string
  email: string
  soDienThoai: string
  gioiTinh: boolean
  ngaySinh: string
  phanLoai: string | null
  deleted: boolean
  tongDon: number
  tongChiTieu: number
  trangThai: boolean
  phanLoaiText: string
  listDiaChi: AddressModel[]
}

export const fetchCustomers = () =>
  client.get<CustomerModel[], CustomerModel[]>(`/api/khach-hang-management/playlist`)

export const fetchCustomerById = (id: number) =>
  client.get<CustomerModel, CustomerModel>(`/api/khach-hang-management/detail/${id}`)
