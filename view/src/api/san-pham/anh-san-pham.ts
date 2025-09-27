import axios from 'axios'

// ==================== QUẢN LÝ ẢNH SẢN PHẨM ====================
export interface AnhSanPham {
  id: number
  duongDanAnh: string
  tenAnh?: string
  mauAnh?: string
  trangThai: boolean
  deleted: boolean
  createAt?: string
  createBy?: number
  updateAt?: string
  updateBy?: number
}

export interface AnhSanPhamParams {
  page?: number
  size?: number
  search?: string
  trangThai?: boolean
}

export interface AnhSanPhamResponse {
  content: AnhSanPham[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

// ==================== CRUD ẢNH SẢN PHẨM ====================
export function getAnhSanPhamList(params: AnhSanPhamParams = {}) {
  return axios.get<AnhSanPhamResponse>('/api/anh-san-pham-management/paging', { params })
}

export function getAnhSanPhamById(id: number) {
  return axios.get<AnhSanPham>(`/api/anh-san-pham-management/detail/${id}`)
}

export function uploadAnhSanPham(file: File, tenAnh?: string) {
  const formData = new FormData()
  formData.append('file', file)
  if (tenAnh) {
    formData.append('tenAnh', tenAnh)
  }

  return axios.post<AnhSanPham>('/api/anh-san-pham-management/add', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export function uploadNhieuAnhSanPham(files: File[]) {
  const formData = new FormData()
  files.forEach((file, index) => {
    formData.append(`file`, file)
  })

  return axios.post<number[]>('/api/anh-san-pham-management/add-multi-image/cloud', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export function updateAnhSanPham(id: number, data: Partial<AnhSanPham>) {
  return axios.put<AnhSanPham>(`/api/anh-san-pham-management/update/${id}`, data)
}

export function deleteAnhSanPham(id: number) {
  return axios.put(`/api/anh-san-pham-management/update/status/${id}`)
}

export function getAllAnhSanPham() {
  return axios.get<AnhSanPham[]>('/api/anh-san-pham-management/playlist')
}
