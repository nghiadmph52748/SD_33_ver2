/**
 * Local Shipping Fee Calculator Service
 * Tính phí vận chuyển tự động dựa trên khoảng cách địa lý
 * Không gọi API bên ngoài
 */

export interface ShippingLocation {
  thanhPho: string
  quan: string
  phuong: string
  diaChiCuThe: string
}

// Địa chỉ cửa hàng: Văn Tiến Dũng, Bắc Từ Liêm, Hà Nội
const STORE_LOCATION = {
  thanhPho: 'Hà Nội',
  quan: 'Bắc Từ Liêm',
  phuong: 'Văn Tiến Dũng',
}

// Bảng giá vận chuyển dựa trên mức khoảng cách
interface ShippingZone {
  name: string
  basePrice: number
  perKm?: number
  description: string
}

// Định nghĩa các khu vực vận chuyển
const SHIPPING_ZONES: Record<string, ShippingZone> = {
  // Zone 1: Cùng quận - Giao nhanh (0-3km)
  SAME_DISTRICT: {
    name: 'Cùng quận',
    basePrice: 8000,
    description: 'Giao hàng cùng quận Bắc Từ Liêm',
  },
  // Zone 2: Hà Nội khác quận (3-15km)
  HANOI_OTHER_DISTRICT: {
    name: 'Hà Nội khác quận',
    basePrice: 15000,
    description: 'Giao hàng trong thành phố Hà Nội',
  },
  // Zone 3: Các tỉnh lân cận (15-50km)
  NEARBY_PROVINCE: {
    name: 'Tỉnh lân cận',
    basePrice: 25000,
    description: 'Giao hàng đến các tỉnh lân cận',
  },
  // Zone 4: Miền Bắc khác (>50km)
  NORTH_VIETNAM: {
    name: 'Miền Bắc',
    basePrice: 40000,
    description: 'Giao hàng đến miền Bắc',
  },
  // Zone 5: Miền Trung
  CENTRAL_VIETNAM: {
    name: 'Miền Trung',
    basePrice: 60000,
    description: 'Giao hàng đến miền Trung',
  },
  // Zone 6: Miền Nam
  SOUTH_VIETNAM: {
    name: 'Miền Nam',
    basePrice: 80000,
    description: 'Giao hàng đến miền Nam',
  },
}

// Danh sách tỉnh theo khu vực
const PROVINCES_BY_REGION: Record<string, string[]> = {
  HANOI: ['Hà Nội'],
  NEARBY_PROVINCES: ['Hải Dương', 'Hải Phòng', 'Vĩnh Phúc', 'Bắc Ninh', 'Thái Nguyên', 'Bắc Giang', 'Lạng Sơn', 'Cao Bằng'],
  NORTH_PROVINCES: ['Yên Bái', 'Sơn La', 'Hòa Bình', 'Hà Giang', 'Điện Biên', 'Lai Châu', 'Tuyên Quang'],
  CENTRAL_PROVINCES: [
    'Thanh Hóa',
    'Nghệ An',
    'Hà Tĩnh',
    'Quảng Bình',
    'Quảng Trị',
    'Thừa Thiên Huế',
    'Đà Nẵng',
    'Quảng Nam',
    'Quảng Ngãi',
    'Bình Định',
    'Phú Yên',
    'Khánh Hòa',
    'Ninh Thuận',
    'Bình Thuận',
  ],
  SOUTH_PROVINCES: [
    'TP. Hồ Chí Minh',
    'Long An',
    'Bình Dương',
    'Đồng Nai',
    'Bà Rịa - Vũng Tàu',
    'Tiền Giang',
    'Bến Tre',
    'Trà Vinh',
    'Vĩnh Long',
    'Cần Thơ',
    'An Giang',
    'Kiên Giang',
    'Hậu Giang',
    'Sóc Trăng',
    'Bạc Liêu',
    'Cà Mau',
  ],
}

// Danh sách quận của Hà Nội
const HANOI_DISTRICTS = [
  'Hoàn Kiếm',
  'Hai Bà Trưng',
  'Đống Đa',
  'Cầu Giấy',
  'Thanh Xuân',
  'Hoàng Mai',
  'Long Biên',
  'Bắc Từ Liêm',
  'Tây Hồ',
  'Hà Đông',
  'Bắc Hà',
  'Tây Hà',
  'Đông Hà',
]

/**
 * Xác định khu vực giao hàng và trả về mức phí
 */
function getShippingZone(location: ShippingLocation): ShippingZone {
  const destinationCity = location.thanhPho.trim()
  const destinationDistrict = location.quan.trim()

  // Zone 1: Cùng quận Bắc Từ Liêm
  if (destinationCity === STORE_LOCATION.thanhPho && destinationDistrict === STORE_LOCATION.quan) {
    return SHIPPING_ZONES.SAME_DISTRICT
  }

  // Zone 2: Hà Nội nhưng khác quận
  if (destinationCity === STORE_LOCATION.thanhPho) {
    return SHIPPING_ZONES.HANOI_OTHER_DISTRICT
  }

  // Zone 3: Tỉnh lân cận
  if (PROVINCES_BY_REGION.NEARBY_PROVINCES.some((p) => p.toLowerCase() === destinationCity.toLowerCase())) {
    return SHIPPING_ZONES.NEARBY_PROVINCE
  }

  // Zone 4: Miền Bắc khác
  if (PROVINCES_BY_REGION.NORTH_PROVINCES.some((p) => p.toLowerCase() === destinationCity.toLowerCase())) {
    return SHIPPING_ZONES.NORTH_VIETNAM
  }

  // Zone 5: Miền Trung
  if (PROVINCES_BY_REGION.CENTRAL_PROVINCES.some((p) => p.toLowerCase() === destinationCity.toLowerCase())) {
    return SHIPPING_ZONES.CENTRAL_VIETNAM
  }

  // Zone 6: Miền Nam (default)
  return SHIPPING_ZONES.SOUTH_VIETNAM
}

/**
 * Tính phí vận chuyển dựa trên địa chỉ giao hàng
 * Miễn phí vận chuyển nếu đơn hàng >= 5.000.000 đồng
 * @param location - Địa chỉ giao hàng (thanhPho, quan, phuong, diaChiCuThe)
 * @param subtotal - Tổng tiền hàng (để kiểm tra miễn phí)
 * @returns Phí vận chuyển (đồng)
 */
export function calculateShippingFee(location: ShippingLocation, subtotal: number = 0): number {
  // Validate location
  if (!location.thanhPho || !location.quan) {
    return 0
  }

  // Free shipping for orders >= 5 million
  if (subtotal >= 5000000) {
    return 0
  }

  const zone = getShippingZone(location)
  return zone.basePrice
}

/**
 * Lấy thông tin chi tiết vận chuyển
 */
export function getShippingInfo(location: ShippingLocation): {
  zone: string
  basePrice: number
  description: string
  estimatedDays: string
} {
  const zone = getShippingZone(location)
  const destinationCity = location.thanhPho.trim()

  // Ước tính thời gian giao hàng
  let estimatedDays = '1-2 ngày'
  if (destinationCity === STORE_LOCATION.thanhPho) {
    if (location.quan === STORE_LOCATION.quan) {
      estimatedDays = 'Hôm nay - Ngày mai'
    } else {
      estimatedDays = '1-2 ngày'
    }
  } else if (PROVINCES_BY_REGION.NEARBY_PROVINCES.some((p) => p.toLowerCase() === destinationCity.toLowerCase())) {
    estimatedDays = '2-3 ngày'
  } else if (PROVINCES_BY_REGION.NORTH_PROVINCES.some((p) => p.toLowerCase() === destinationCity.toLowerCase())) {
    estimatedDays = '3-4 ngày'
  } else {
    estimatedDays = '5-7 ngày'
  }

  return {
    zone: zone.name,
    basePrice: zone.basePrice,
    description: zone.description,
    estimatedDays,
  }
}

/**
 * Lấy danh sách hướng dẫn tính phí
 */
export function getShippingFeeGuide(): ShippingZone[] {
  return Object.values(SHIPPING_ZONES)
}

/**
 * Check nếu địa chỉ cửa hàng
 */
export function isStoreLocation(location: ShippingLocation): boolean {
  return location.thanhPho === STORE_LOCATION.thanhPho && location.quan === STORE_LOCATION.quan && location.phuong === STORE_LOCATION.phuong
}
