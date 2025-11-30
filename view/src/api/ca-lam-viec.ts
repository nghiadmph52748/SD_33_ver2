import axios from 'axios'
import { HttpResponse } from '@/api/interceptor'

export interface CaLamViec {
  id: number
  tenCa: string
  thoiGianBatDau: string
  thoiGianKetThuc: string
  trangThai: boolean
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

/**
 * Kiểm tra xem có ca nào cùng giờ không
 * So sánh giờ bắt đầu và kết thúc
 */
export async function checkTimeConflict(gioBatDau: string, gioKetThuc: string, excludeCaId?: number): Promise<boolean> {
  try {
    const res = await getCaLamViec()
    const list = Array.isArray(res.data) ? res.data : Array.isArray(res) ? res : []
    
    // Normalize time format (remove seconds if present)
    const normalizeTime = (time: string) => {
      if (!time) return ''
      return time.includes(':') ? time.split(':').slice(0, 2).join(':') : time
    }
    
    const newStart = normalizeTime(gioBatDau)
    const newEnd = normalizeTime(gioKetThuc)
    
    // Check for exact time conflict
    const hasConflict = list.some((ca: any) => {
      if (excludeCaId && ca.id === excludeCaId) return false
      
      const existingStart = normalizeTime(ca.thoiGianBatDau || ca.gioBatDau)
      const existingEnd = normalizeTime(ca.thoiGianKetThuc || ca.gioKetThuc)
      
      // Same shift time
      return existingStart === newStart && existingEnd === newEnd
    })
    
    return hasConflict
  } catch (err) {
    console.warn('Error checking time conflict:', err)
    return false
  }
}
