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

/**
 * Tính phí vận chuyển từ GHN API
 * Thay thế local calculation bằng API call để lấy giá thực tế từ giao hàng nhanh
 * @param location - Địa chỉ giao hàng
 * @param weight - Cân nặng của đơn hàng (gram), mặc định 500g
 * @param length - Chiều dài (cm), mặc định 10cm
 * @param width - Chiều rộng (cm), mặc định 10cm
 * @param height - Chiều cao (cm), mặc định 10cm
 * @returns Promise<{fee: number, message: string}> - Phí vận chuyển hoặc error message
 */
export async function calculateShippingFeeFromGHN(
  location: ShippingLocation,
  weight: number = 500,
  length: number = 10,
  width: number = 10,
  height: number = 10
): Promise<{ fee: number; message: string }> {
  try {
    // Validate location
    if (!location.thanhPho || !location.quan || !location.phuong) {
      return { fee: 0, message: 'Địa chỉ không đầy đủ' }
    }

    const GHN_TOKEN = import.meta.env.VITE_GHN_TOKEN || ''
    const GHN_DEV_TOKEN = import.meta.env.VITE_GHN_DEV_TOKEN || ''
    if (!GHN_TOKEN) {
      // Fallback to local calculation if GHN token not configured
      const fallbackFee = calculateShippingFee(location, 0)
      return { fee: fallbackFee, message: 'Sử dụng giá mặc định' }
    }

    // GHN store ID - thay đổi theo cửa hàng của bạn
    const storeId = import.meta.env.VITE_GHN_STORE_ID || 0

    // Fetch district info to get district ID
    const districtRes = await fetch('https://online-gateway.ghn.vn/shiip/public-api/master-data/district', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        Token: GHN_TOKEN,
      },
    })

    if (!districtRes.ok) {
      const fallbackFee = calculateShippingFee(location, 0)
      return { fee: fallbackFee, message: 'Không thể lấy thông tin quận huyện' }
    }

    const districtData = await districtRes.json()
    const destinationDistrict = districtData.data?.find((d: any) => d.DistrictName.toLowerCase().includes(location.quan.toLowerCase()))

    if (!destinationDistrict) {
      const fallbackFee = calculateShippingFee(location, 0)
      return { fee: fallbackFee, message: 'Không tìm thấy quận huyện' }
    }

    console.log('[ShippingFee] District lookup:', {
      input: location.quan,
      found: destinationDistrict.DistrictName,
      districtId: destinationDistrict.DistrictID,
    })

    // Fetch ward info to get ward ID
    const wardRes = await fetch('https://online-gateway.ghn.vn/shiip/public-api/master-data/ward', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Token: GHN_TOKEN,
      },
      body: JSON.stringify({
        district_id: destinationDistrict.DistrictID,
      }),
    })

    if (!wardRes.ok) {
      const fallbackFee = calculateShippingFee(location, 0)
      return { fee: fallbackFee, message: 'Không thể lấy thông tin phường xã' }
    }

    const wardData = await wardRes.json()
    const destinationWard = wardData.data?.find((w: any) => w.WardName.toLowerCase().includes(location.phuong.toLowerCase()))

    if (!destinationWard) {
      const fallbackFee = calculateShippingFee(location, 0)
      return { fee: fallbackFee, message: 'Không tìm thấy phường xã' }
    }

    console.log('[ShippingFee] Ward lookup:', {
      input: location.phuong,
      found: destinationWard.WardName,
      wardCode: destinationWard.WardCode,
    })

    // Call GHN shipping fee calculation API
    const feePayload = {
      from_district_id: 1, // Hà Nội
      from_ward_id: 1442, // Bắc Từ Liêm
      to_district_id: destinationDistrict.DistrictID,
      to_ward_id: destinationWard.WardCode,
      height: height,
      length: length,
      weight: weight,
      width: width,
      service_type_id: 2, // Standard service
      insurance_value: 0,
      cod_failed_amount: 0,
      coupon: null,
    }

    console.log('[ShippingFee] GHN Fee Payload:', {
      ...feePayload,
      address: `${location.diaChiCuThe}, ${location.phuong}, ${location.quan}, ${location.thanhPho}`,
      destinationDistrictName: destinationDistrict.DistrictName,
      destinationWardName: destinationWard.WardName,
    })

    // Try production API first
    let feeRes = await fetch('https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Token: GHN_TOKEN,
      },
      body: JSON.stringify(feePayload),
    })

    // If production fails and we have dev token, retry with dev server
    if (!feeRes.ok && GHN_DEV_TOKEN) {
      console.warn(`[ShippingFee] Production API failed (${feeRes.status}), trying dev server with token...`)
      feeRes = await fetch('https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Token: GHN_DEV_TOKEN,
        },
        body: JSON.stringify(feePayload),
      })
      console.log('[ShippingFee] Retried with dev server.')
    }

    const feeData = await feeRes.json()

    console.log('[ShippingFee] GHN Fee Response:', feeData)

    if (!feeRes.ok) {
      const isDevServer = feeRes.url?.includes('dev-online-gateway')
      const tokenUsed = isDevServer ? GHN_DEV_TOKEN : GHN_TOKEN
      console.error('[ShippingFee] GHN API Error:', {
        status: feeRes.status,
        server: isDevServer ? 'dev-online-gateway' : 'online-gateway',
        payload: feePayload,
        response: feeData,
        tokenUsed: tokenUsed ? `${tokenUsed.substring(0, 8)}...` : 'N/A',
      })
      const fallbackFee = calculateShippingFee(location, 0)
      return { fee: fallbackFee, message: 'Không thể lấy phí vận chuyển từ GHN' }
    }

    if (!feeData.data || typeof feeData.data.total !== 'number') {
      const fallbackFee = calculateShippingFee(location, 0)
      return { fee: fallbackFee, message: 'Phí GHN không hợp lệ, sử dụng giá mặc định' }
    }

    return { fee: feeData.data.total, message: 'Phí từ GHN' }
  } catch (error) {
    console.error('Error calculating shipping fee from GHN:', error)
    // Fallback to local calculation
    const fallbackFee = calculateShippingFee(location, 0)
    return { fee: fallbackFee, message: 'Lỗi khi tính phí vận chuyển' }
  }
}
