import type { AxiosError } from 'axios'

interface MapOptions {
  defaultMessage?: string
  entityLabel?: string
}

const DUPLICATE_PATTERNS = [/đã tồn tại/i, /already exists?/i, /duplicate/i, /trùng/i]

const ENTITY_LABEL_MAP: Record<string, string> = {
  NhaSanXuat: 'Nhà sản xuất',
  ChatLieu: 'Chất liệu',
  XuatXu: 'Xuất xứ',
  DeGiay: 'Đế giày',
  MauSac: 'Màu sắc',
  KichThuoc: 'Kích thước',
  DanhMucSanPham: 'Sản phẩm',
  SanPham: 'Sản phẩm',
  BienTheSanPham: 'Biến thể sản phẩm',
}

const FRIENDLY_MESSAGE_KEY = '__friendlyMessage'
const GLOBAL_MESSAGE_FLAG = '__hasGlobalMessage'

const toSentenceCase = (value: string) => {
  if (!value) return value
  return value.charAt(0).toUpperCase() + value.slice(1)
}

const toLowerLabel = (value?: string) => {
  if (!value) return value
  const trimmed = value.trim()
  if (trimmed.length === 0) return trimmed
  return trimmed.charAt(0).toLowerCase() + trimmed.slice(1)
}

const sanitizeBackendDetails = (message?: string) => {
  if (!message) return ''
  let sanitized = message
  sanitized = sanitized.replace(/class\s+[A-Za-z0-9_.]+/g, '')
  sanitized = sanitized.replace(/([\s:])+(org|com)\.[^\s]+/g, '$1')
  sanitized = sanitized.replace(/[:：]\s*$/g, '')
  sanitized = sanitized.replace(/\s{2,}/g, ' ')
  return sanitized.trim()
}

const detectEntityLabel = (message?: string, explicitLabel?: string) => {
  if (explicitLabel) {
    return explicitLabel
  }
  if (!message) {
    return undefined
  }
  return Object.entries(ENTITY_LABEL_MAP).find(([key]) => message.includes(key))?.[1]
}

const extractRawMessage = (error: any): string | undefined => {
  if (!error) {
    return undefined
  }

  const responseData = (error as AxiosError)?.response?.data as any

  if (typeof responseData === 'string') {
    return responseData
  }

  if (responseData && typeof responseData.message === 'string') {
    return responseData.message
  }

  if (responseData && typeof responseData.error === 'string') {
    return responseData.error
  }

  if (typeof error?.message === 'string') {
    return error.message
  }

  return undefined
}

const matchesDuplicatePattern = (message?: string) => {
  if (!message) return false
  return DUPLICATE_PATTERNS.some((pattern) => pattern.test(message))
}

const formatDuplicateMessage = (entityLabel?: string) => {
  if (entityLabel) {
    return `${toSentenceCase(entityLabel)} đã tồn tại trong hệ thống.`
  }
  return 'Dữ liệu này đã tồn tại trong hệ thống.'
}

export const mapApiErrorToMessage = (error: any, options?: MapOptions) => {
  const { defaultMessage, entityLabel: explicitLabel } = options || {}

  const status = typeof error?.response?.status === 'number' ? error.response.status : undefined
  const rawMessage = extractRawMessage(error)
  const sanitizedMessage = sanitizeBackendDetails(rawMessage)
  const entityLabel = detectEntityLabel(rawMessage, explicitLabel)

  let friendlyMessage: string | undefined

  if (error?.code === 'ERR_NETWORK' || error?.message === 'Network Error') {
    friendlyMessage = 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra mạng và thử lại.'
  }

  if (!friendlyMessage && status === 0) {
    friendlyMessage = 'Máy chủ không phản hồi. Vui lòng thử lại sau.'
  }

  if (!friendlyMessage && (status === 409 || matchesDuplicatePattern(rawMessage))) {
    friendlyMessage = formatDuplicateMessage(entityLabel)
  }

  if (!friendlyMessage && status === 400) {
    if (entityLabel) {
      friendlyMessage = `Thông tin ${toLowerLabel(entityLabel)} chưa hợp lệ. Vui lòng kiểm tra lại.`
    } else {
      friendlyMessage = 'Dữ liệu bạn vừa nhập chưa hợp lệ. Vui lòng kiểm tra lại.'
    }
  }

  if (!friendlyMessage && status === 404) {
    if (entityLabel) {
      friendlyMessage = `${toSentenceCase(entityLabel)} không tồn tại hoặc đã bị xóa.`
    } else {
      friendlyMessage = 'Không tìm thấy dữ liệu phù hợp với yêu cầu.'
    }
  }

  if (!friendlyMessage && status && status >= 500) {
    friendlyMessage = 'Máy chủ đang gặp sự cố. Vui lòng thử lại sau ít phút.'
  }

  if (!friendlyMessage && sanitizedMessage) {
    friendlyMessage = sanitizedMessage
  }

  if (!friendlyMessage) {
    friendlyMessage = defaultMessage || 'Không thể hoàn thành yêu cầu. Vui lòng thử lại sau.'
  }

  if (error && typeof error === 'object') {
    ;(error as any)[FRIENDLY_MESSAGE_KEY] = friendlyMessage
  }

  return friendlyMessage
}

export const hasErrorBeenNotified = (error: any) => {
  if (!error || typeof error !== 'object') {
    return false
  }
  return Boolean((error as any)[GLOBAL_MESSAGE_FLAG])
}

export const markErrorAsNotified = (error: any) => {
  if (!error || typeof error !== 'object') {
    return
  }
  ;(error as any)[GLOBAL_MESSAGE_FLAG] = true
}

export const getAttachedFriendlyMessage = (error: any) => {
  if (!error || typeof error !== 'object') {
    return undefined
  }
  const message = (error as any)[FRIENDLY_MESSAGE_KEY]
  return typeof message === 'string' ? message : undefined
}

export const sanitizeBackendMessage = (message?: string) => sanitizeBackendDetails(message)
