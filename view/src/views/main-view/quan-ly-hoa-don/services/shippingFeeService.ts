/**
 * Service để tính toán và quản lý phụ phí/hoàn phí khi thay đổi địa chỉ giao hàng
 */

export interface ShippingFeeChangeResult {
  feeChanged: boolean
  currentFee: number
  newFee: number
  difference: number
  isExtra: boolean // true = phụ phí (tăng), false = hoàn phí (giảm)
  amountToUpdate: number // Giá trị để cập nhật (dương cho phụ phí, 0 hoặc âm cho hoàn phí)
  description: string
}

/**
 * Tính toán thay đổi phí giao hàng khi thay đổi địa chỉ
 *
 * @param currentFee - Phí giao hàng hiện tại
 * @param newFee - Phí giao hàng mới từ GHN API
 * @returns ShippingFeeChangeResult với thông tin chi tiết
 */
export function calculateShippingFeeChange(currentFee: number, newFee: number): ShippingFeeChangeResult {
  const difference = newFee - currentFee
  const feeChanged = difference !== 0
  const isExtra = difference > 0

  let description = ''
  let amountToUpdate = 0

  if (!feeChanged) {
    description = 'Địa chỉ mới có phí giao hàng tương tự'
    amountToUpdate = 0
  } else if (isExtra) {
    // Phụ phí: phí tăng
    amountToUpdate = difference
    description = `Phụ phí phát sinh: +${difference.toLocaleString('vi-VN')}đ`
  } else {
    // Hoàn phí: phí giảm
    amountToUpdate = 0 // Không cộng phí nếu giảm
    description = `Hoàn phí do thay đổi địa chỉ: ${Math.abs(difference).toLocaleString('vi-VN')}đ`
  }

  return {
    feeChanged,
    currentFee,
    newFee,
    difference,
    isExtra,
    amountToUpdate,
    description,
  }
}

/**
 * Kiểm tra cần update phụ phí hay hoàn phí
 * Trả về đúng nếu có thay đổi cần xử lý
 */
export function shouldUpdateShippingFee(result: ShippingFeeChangeResult): boolean {
  return result.feeChanged
}

/**
 * Format thông tin phụ phí/hoàn phí để hiển thị
 */
export function formatShippingFeeInfo(result: ShippingFeeChangeResult): {
  label: string
  value: string
  color: string
  icon: string
} {
  if (!result.feeChanged) {
    return {
      label: 'Phí giao hàng',
      value: `${result.newFee.toLocaleString('vi-VN')}đ (không thay đổi)`,
      color: '#86909c',
      icon: '✅',
    }
  }

  if (result.isExtra) {
    return {
      label: 'Phụ phí phát sinh',
      value: `+${result.difference.toLocaleString('vi-VN')}đ (phí tăng từ ${result.currentFee.toLocaleString('vi-VN')}đ lên ${result.newFee.toLocaleString('vi-VN')}đ)`,
      color: '#f53f3f',
      icon: '💰',
    }
  }

  return {
    label: 'Hoàn phí',
    value: `${Math.abs(result.difference).toLocaleString('vi-VN')}đ (phí giảm từ ${result.currentFee.toLocaleString('vi-VN')}đ xuống ${result.newFee.toLocaleString('vi-VN')}đ)`,
    color: '#13c2c2',
    icon: '💚',
  }
}

/**
 * Tạo đối tượng cập nhật cho API
 * Sử dụng để update `phuPhi` (phụ phí) hoặc `hoanPhi` (hoàn phí)
 */
export function createFeeUpdatePayload(result: ShippingFeeChangeResult): {
  phuPhi?: number
  hoanPhi?: number
} {
  const payload: any = {}

  if (!result.feeChanged) {
    return payload
  }

  if (result.isExtra) {
    // Phụ phí: thêm vào hóa đơn
    payload.phuPhi = result.difference
    payload.hoanPhi = 0
  } else {
    // Hoàn phí: hoàn lại cho khách
    payload.phuPhi = 0
    payload.hoanPhi = Math.abs(result.difference)
  }

  return payload
}
