export interface ResponseObject {
  status: boolean
  message: string
  data: any[]
}

export interface PagingResponse {
  data: any[]
  totalPages: number
  number: number  // Spring Boot uses 'number' for current page (0-indexed)
  size: number    // Spring Boot uses 'size' for page size
  totalElements: number
}

export interface PagedResponseObject extends ResponseObject {
  data: PagingResponse
}
