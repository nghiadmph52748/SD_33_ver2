export interface ShippingLocation {
  thanhPho: string;
  quan: string;
  phuong: string;
  diaChiCuThe?: string;
}

export interface ShippingFeeResponse {
  fee: number;
  message: string;
  estimatedDays?: string;
}

export interface CalculateShippingFeePayload {
  location: ShippingLocation;
  weight?: number; // gram, default 500
  length?: number; // cm, default 10
  width?: number; // cm, default 10
  height?: number; // cm, default 10
  subtotal?: number; // for free shipping check
}

export interface GHNDistrict {
  DistrictID: number;
  DistrictName: string;
  Code: string;
}

export interface GHNWard {
  WardCode: string;
  WardName: string;
  DistrictID: number;
}

/**
 * Fetch all provinces from GHN (derived from districts)
 * GHN doesn't have province endpoint, so we get all districts and extract unique provinces
 */
export async function fetchGHNProvinces(): Promise<
  Array<{ name: string; code: number }>
> {
  try {
    const GHN_TOKEN = import.meta.env.VITE_GHN_TOKEN?.trim();
    if (!GHN_TOKEN) {
      console.warn("[Shipping] GHN token not configured");
      return [];
    }

    const res = await fetch(
      "https://online-gateway.ghn.vn/shiip/public-api/master-data/province",
      {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Token: GHN_TOKEN,
        },
      }
    );

    if (!res.ok) {
      console.error("[Shipping] Failed to fetch provinces:", res.status);
      return [];
    }

    const data = await res.json();
    // Map GHN response to our format
    return (data.data || []).map((p: any) => ({
      name: p.ProvinceName,
      code: p.ProvinceID,
    }));
  } catch (error) {
    console.error("[Shipping] Error fetching provinces:", error);
    return [];
  }
}

/**
 * Fetch GHN districts by province ID
 * Used for address selection in checkout
 */
export async function fetchGHNDistrictsByProvince(
  provinceId: number
): Promise<GHNDistrict[]> {
  try {
    const GHN_TOKEN = import.meta.env.VITE_GHN_TOKEN?.trim();
    if (!GHN_TOKEN) {
      console.warn("[Shipping] GHN token not configured");
      return [];
    }

    const res = await fetch(
      "https://online-gateway.ghn.vn/shiip/public-api/master-data/district",
      {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Token: GHN_TOKEN,
        },
      }
    );

    if (!res.ok) {
      console.error("[Shipping] Failed to fetch districts:", res.status);
      return [];
    }

    const data = await res.json();
    // Filter districts by province ID
    return (data.data || []).filter((d: any) => d.ProvinceID === provinceId);
  } catch (error) {
    console.error("[Shipping] Error fetching districts:", error);
    return [];
  }
}

/**
 * Fetch GHN districts
 * Used for address selection in checkout
 */
export async function fetchGHNDistricts(): Promise<GHNDistrict[]> {
  try {
    const GHN_TOKEN = import.meta.env.VITE_GHN_TOKEN?.trim();
    if (!GHN_TOKEN) {
      console.warn("[Shipping] GHN token not configured");
      return [];
    }

    const res = await fetch(
      "https://online-gateway.ghn.vn/shiip/public-api/master-data/district",
      {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Token: GHN_TOKEN,
        },
      }
    );

    if (!res.ok) {
      console.error("[Shipping] Failed to fetch districts:", res.status);
      return [];
    }

    const data = await res.json();
    return data.data || [];
  } catch (error) {
    console.error("[Shipping] Error fetching districts:", error);
    return [];
  }
}

/**
 * Fetch GHN wards by district ID
 * Used for address selection in checkout
 */
export async function fetchGHNWards(districtId: number): Promise<GHNWard[]> {
  try {
    const GHN_TOKEN = import.meta.env.VITE_GHN_TOKEN?.trim();
    if (!GHN_TOKEN) {
      console.warn("[Shipping] GHN token not configured");
      return [];
    }

    const res = await fetch(
      "https://online-gateway.ghn.vn/shiip/public-api/master-data/ward",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Token: GHN_TOKEN,
        },
        body: JSON.stringify({ district_id: districtId }),
      }
    );

    if (!res.ok) {
      console.error("[Shipping] Failed to fetch wards:", res.status);
      return [];
    }

    const data = await res.json();
    return data.data || [];
  } catch (error) {
    console.error("[Shipping] Error fetching wards:", error);
    return [];
  }
}

/**
 * Local shipping fee calculation as fallback
 * Uses predefined zones based on location with price tiers based on subtotal
 * Fees are adjusted based on real Vietnamese e-commerce market rates
 * Price tiers: Higher order value gets discount on shipping
 */
function calculateLocalShippingFee(
  location: ShippingLocation,
  subtotal: number = 0
): number {
  // Địa chỉ cửa hàng mặc định: Bắc Từ Liêm, Hà Nội
  const STORE_DISTRICT = "Bắc Từ Liêm";
  const STORE_CITY = "Hà Nội";

  // Ensure subtotal is integer (VND doesn't have decimals)
  const subtotalInt = Math.round(subtotal || 0);

  // Miễn phí vận chuyển nếu >= 5 triệu
  if (subtotalInt >= 5000000) {
    return 0;
  }

  // Cùng quận - giảm 30% nếu >= 2 triệu
  if (location.quan === STORE_DISTRICT && location.thanhPho === STORE_CITY) {
    if (subtotalInt >= 2000000) return 12000;
    return 18000;
  }

  // Hà Nội khác quận
  if (location.thanhPho === STORE_CITY) {
    if (subtotalInt >= 3000000) return 18000;
    return 25000;
  }

  // Tỉnh lân cận (100km)
  const nearbyProvinces = [
    "Hải Dương",
    "Hải Phòng",
    "Vĩnh Phúc",
    "Bắc Ninh",
    "Thái Nguyên",
    "Hưng Yên",
  ];
  if (
    nearbyProvinces.some(
      (p) => p.toLowerCase() === location.thanhPho.toLowerCase()
    )
  ) {
    if (subtotalInt >= 3000000) return 28000;
    return 38000;
  }

  // Miền Bắc khác (200km+)
  const northProvinces = [
    "Yên Bái",
    "Sơn La",
    "Hòa Bình",
    "Hà Giang",
    "Cao Bằng",
    "Lạng Sơn",
    "Điện Biên",
  ];
  if (
    northProvinces.some(
      (p) => p.toLowerCase() === location.thanhPho.toLowerCase()
    )
  ) {
    if (subtotalInt >= 3000000) return 45000;
    return 60000;
  }

  // Miền Trung (300km+)
  const centralProvinces = [
    "Thanh Hóa",
    "Nghệ An",
    "Hà Tĩnh",
    "Quảng Bình",
    "Quảng Trị",
    "Thừa Thiên Huế",
    "Đà Nẵng",
    "Quảng Nam",
    "Quảng Ngãi",
  ];
  if (
    centralProvinces.some(
      (p) => p.toLowerCase() === location.thanhPho.toLowerCase()
    )
  ) {
    if (subtotalInt >= 3000000) return 48000;
    return 68000;
  }

  // Tây Nguyên (500km+)
  const tay_nguyen = ["Kon Tum", "Gia Lai", "Đắk Lắk", "Đắk Nông", "Lâm Đồng"];
  if (
    tay_nguyen.some((p) => p.toLowerCase() === location.thanhPho.toLowerCase())
  ) {
    if (subtotalInt >= 3000000) return 55000;
    return 75000;
  }

  // Miền Nam (600km+)
  const southProvinces = [
    "Bình Phước",
    "Bình Dương",
    "Đồng Nai",
    "TP. Hồ Chí Minh",
    "Hồ Chí Minh",
    "Vũng Tàu",
    "Bà Rịa-Vũng Tàu",
    "Long An",
    "Tiền Giang",
    "Bến Tre",
    "Trà Vinh",
    "Vĩnh Long",
    "Cần Thơ",
    "Đồng Tháp",
    "An Giang",
    "Kiên Giang",
    "Hậu Giang",
    "Sóc Trăng",
    "Bạc Liêu",
    "Cà Mau",
  ];
  if (
    southProvinces.some(
      (p) => p.toLowerCase() === location.thanhPho.toLowerCase()
    )
  ) {
    if (subtotalInt >= 3000000) return 58000;
    return 80000;
  }

  // Default fallback - quốc tế hoặc nơi khác
  return 100000;
}

/**
 * Calculate shipping fee from GHN API
 * Falls back to local calculation if GHN is not configured or fails
 */
async function calculateShippingFeeFromGHN(
  payload: CalculateShippingFeePayload
): Promise<ShippingFeeResponse> {
  try {
    const {
      location,
      weight = 500,
      length = 10,
      width = 10,
      height = 10,
      subtotal = 0,
    } = payload;

    // Validate location
    if (!location.thanhPho || !location.quan || !location.phuong) {
      return {
        fee: 0,
        message: "Địa chỉ không đầy đủ",
        estimatedDays: "N/A",
      };
    }

    const GHN_TOKEN = import.meta.env.VITE_GHN_TOKEN?.trim();

    // Store location - hardcoded for GHN API
    // IMPORTANT: These IDs must match the shop's actual location in GHN portal
    // If you get SHOP_INFO_ERROR, you need to:
    // 1. Go to https://khachhang.ghn.vn/
    // 2. Find your shop's registered district & ward ID
    // 3. Update STORE_LOCATION below
    // Current config assumes shop is in Ha Noi, Bac Tu Liem (district 1, ward 1442)
    // For other locations, update these values
    const STORE_LOCATION = {
      fromDistrictId: 1, // Ha Noi - change if shop is elsewhere
      fromWardId: 1442, // Bac Tu Liem - change if shop ward differs
    };

    // If GHN token not configured, use local calculation
    if (!GHN_TOKEN) {
      const localFee = calculateLocalShippingFee(location, subtotal);
      return {
        fee: localFee,
        message: "Sử dụng giá mặc định (không có GHN token)",
        estimatedDays: "3-5 ngày",
      };
    }

    // Step 1: Get district ID from destination
    let toDistrictId: number | null = null;
    try {
      const districtRes = await fetch(
        "https://online-gateway.ghn.vn/shiip/public-api/master-data/district",
        {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Token: GHN_TOKEN,
          },
        }
      );

      if (!districtRes.ok) {
        throw new Error(`District API failed: ${districtRes.status}`);
      }

      const districtData = await districtRes.json();
      const targetDistrict = districtData.data?.find((d: any) =>
        d.DistrictName.toLowerCase().includes(location.quan.toLowerCase())
      );

      if (!targetDistrict) {
        throw new Error(`District not found: ${location.quan}`);
      }

      toDistrictId = targetDistrict.DistrictID;
    } catch (error) {
      console.warn("[Shipping] Failed to get district from GHN:", error);
      const localFee = calculateLocalShippingFee(location, subtotal);
      return {
        fee: localFee,
        message: "Không thể lấy thông tin từ GHN, sử dụng giá mặc định",
        estimatedDays: "3-5 ngày",
      };
    }

    // Step 2: Get ward ID from destination
    let toWardId: string | number | null = null;
    try {
      const wardRes = await fetch(
        "https://online-gateway.ghn.vn/shiip/public-api/master-data/ward",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Token: GHN_TOKEN,
          },
          body: JSON.stringify({
            district_id: toDistrictId,
          }),
        }
      );

      if (!wardRes.ok) {
        throw new Error(`Ward API failed: ${wardRes.status}`);
      }

      const wardData = await wardRes.json();
      const targetWard = wardData.data?.find((w: any) =>
        w.WardName.toLowerCase().includes(location.phuong.toLowerCase())
      );

      if (!targetWard) {
        throw new Error(`Ward not found: ${location.phuong}`);
      }

      toWardId = targetWard.WardCode;
    } catch (error) {
      console.warn("[Shipping] Failed to get ward from GHN:", error);
      const localFee = calculateLocalShippingFee(location, subtotal);
      return {
        fee: localFee,
        message: "Không thể lấy thông tin từ GHN, sử dụng giá mặc định",
        estimatedDays: "3-5 ngày",
      };
    }

    // Step 3: Calculate shipping fee
    const feePayload = {
      from_district_id: STORE_LOCATION.fromDistrictId,
      from_ward_id: STORE_LOCATION.fromWardId,
      to_district_id: toDistrictId,
      to_ward_id: toWardId,
      height: height,
      length: length,
      weight: weight,
      width: width,
      insurance_value: 0,
      cod_failed_amount: 0,
      coupon: null,
    };

    const feeRes = await fetch(
      "https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Token: GHN_TOKEN,
        },
        body: JSON.stringify(feePayload),
      }
    );

    const feeData = await feeRes.json();

    if (!feeData.data || typeof feeData.data.total !== "number") {
      throw new Error("Invalid fee response format");
    }

    // Ensure fee is integer
    const shippingFeeInt = Math.round(feeData.data.total);

    return {
      fee: shippingFeeInt,
      message: "Phí từ GHN",
      estimatedDays: "1-3 ngày",
    };
  } catch (error) {
    const { location, subtotal = 0 } = payload;
    const localFee = calculateLocalShippingFee(location, subtotal);
    return {
      fee: localFee,
      message: "Lỗi khi lấy phí từ GHN, sử dụng giá mặc định",
      estimatedDays: "3-5 ngày",
    };
  }
}
/**
 * Public API endpoint to calculate shipping fee
 * This can be called from backend or frontend
 */
export async function calculateShippingFee(
  payload: CalculateShippingFeePayload
): Promise<ShippingFeeResponse> {
  return calculateShippingFeeFromGHN(payload);
}

/**
 * Quick local calculation without API call
 * Used for quick estimates before calling the full API
 */
export function estimateShippingFee(
  location: ShippingLocation,
  subtotal: number = 0
): number {
  return calculateLocalShippingFee(location, subtotal);
}
