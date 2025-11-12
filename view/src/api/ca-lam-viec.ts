import axios from 'axios'
import { HttpResponse } from '@/api/interceptor'

export interface CaLamViec {
  id: number;
  tenCa: string;
  thoiGianBatDau: string;
  thoiGianKetThuc: string;
  trangThai: boolean;
}



export function getCaLamViec() {
  return axios.get<HttpResponse<CaLamViec[]>>('/api/ca-lam-viec')
}

export function themCaLamViec(data: Omit<CaLamViec, 'id'>) {
  return axios.post<HttpResponse<CaLamViec>>('/api/ca-lam-viec', data)
}

export function getCaLamViecById(id: number) {
  return axios.get<HttpResponse<CaLamViec>>(`/api/ca-lam-viec/${id}`)
}

export function suaCaLamViec(id: number, data: Partial<CaLamViec>) {
  return axios.put<HttpResponse<CaLamViec>>(`/api/ca-lam-viec/${id}`, data)
}

export async function updateTrangThaiCa(id: number, trangThai: boolean) {
  return axios.patch(`/api/ca-lam-viec/${id}/trang-thai`, { trangThai })
}


