import axios from 'axios'
import { type ResponseObject, type PagingResponse, type PagedResponseObject } from '../ResponseObject'
// ==================== QUẢN LÝ ẢNH SẢN PHẨM ====================
export interface AnhSanPham {
  id?: number
  duongDanAnh: string
  tenAnh?: string
  mauAnh?: string
  trangThai: boolean
  deleted: boolean
  createAt: string
  createBy: number
  updateAt?: string
  updateBy?: number
}

export interface AnhSanPhamPagingResponse extends PagingResponse {
  data: AnhSanPham[]
}

export interface AnhSanPhamResponse extends PagedResponseObject {
  data: AnhSanPhamPagingResponse
}

export interface UploadResponse extends ResponseObject {
  success: boolean
  data: string
  message: string
}

// ==================== CRUD ẢNH SẢN PHẨM ====================
export function getAnhSanPhamList(page: number) {
  return axios.get<AnhSanPhamResponse>(`/api/anh-san-pham-management/paging?page=${page}`)
}

export function uploadMutipartFile(files: File[], tenAnh: string, createBy: number) {
  const formData = new FormData()
  files.forEach((file) => {
    formData.append(`file`, file)
  })
  formData.append('tenAnh', tenAnh)
  formData.append('createBy', createBy)

  return axios.post<number[]>('/api/anh-san-pham-management/add-multi-image/cloud', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export function updateMutipartFile(id: number, files: File[], tenAnh: string, updateBy: number) {
  const formData = new FormData()
  files.forEach((file) => {
    formData.append(`file`, file)
  })
  formData.append('tenAnh', tenAnh)
  formData.append('updateBy', updateBy.toString())
  return axios.post<number[]>(`/api/anh-san-pham-management/update-multi-image/cloud/${id}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export function deleteAnhSanPham(id: number) {
  return axios.put(`/api/anh-san-pham-management/update/status/${id}`)
}

export function getAllAnhSanPham() {
  return axios.get<AnhSanPham[]>('/api/anh-san-pham-management/playlist')
}

export function getAnhSanPhamByTenMau(mauAnh: string) {
  return axios.get<AnhSanPham[]>(`/api/anh-san-pham-management/filter?mauAnh=${mauAnh}`)
}
