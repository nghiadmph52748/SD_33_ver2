import { layDanhSachKhachHang, luuDiaChiKhachHangNeuChuaCo, type DiaChiKhachHangResponse, type KhachHangResponse } from '@/api/khach-hang'

export async function fetchCustomers(): Promise<KhachHangResponse[]> {
  const res = await layDanhSachKhachHang()
  return res?.data || []
}

export interface SaveCustomerAddressParams {
  customerId: number
  location: {
    thanhPho: string
    quan: string
    phuong: string
    diaChiCuThe: string
  }
  tenDiaChi?: string
}

export async function saveCustomerAddressIfMissing(params: SaveCustomerAddressParams): Promise<DiaChiKhachHangResponse | undefined> {
  const response = await luuDiaChiKhachHangNeuChuaCo({
    idKhachHang: params.customerId,
    tenDiaChi: params.tenDiaChi,
    thanhPho: params.location.thanhPho,
    quan: params.location.quan,
    phuong: params.location.phuong,
    diaChiCuThe: params.location.diaChiCuThe,
    deleted: false,
  })
  return response?.data?.data
}
