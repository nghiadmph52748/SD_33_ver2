export interface ResponseObject {
  status: boolean
  message: string
  data: any[]
}

export interface PagingResponse {
  data: any[]
  totalPages: number
  currentPage: number
  pageSize: number
  totalElements: number
}

export interface PagedResponseObject extends ResponseObject {
  data: PagingResponse
}
