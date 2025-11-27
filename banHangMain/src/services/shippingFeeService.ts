/**
 * Shipping Fee Service - banHangMain
 * Tính phí vận chuyển từ GHN API như bên bán hàng tại quầy
 */

export interface ShippingLocation {
  thanhPho: string;
  quan: string;
  phuong: string;
  diaChiCuThe?: string;
}

export interface ShippingFeeResponse {
  fee: number;
  message: string;
}

/**
 * Local shipping fee calculation fallback
 */
function calculateLocalShippingFee(location: ShippingLocation): number {
  // Địa chỉ cửa hàng: Bắc Từ Liêm, Hà Nội
  const STORE_LOCATION = {
    thanhPho: "Hà Nội",
    quan: "Bắc Từ Liêm",
  };

  const destinationCity = location.thanhPho.toLowerCase();
  const destinationDistrict = location.quan.toLowerCase();

  // Nếu cùng thành phố
  if (destinationCity === STORE_LOCATION.thanhPho.toLowerCase()) {
    // Cùng quận - giao nhanh
    if (destinationDistrict === STORE_LOCATION.quan.toLowerCase()) {
      return 8000;
    }
    // Khác quận Hà Nội
    return 15000;
  }

  // Tỉnh lân cận Hà Nội
  const nearbyProvinces = [
    "Hải Dương",
    "Hải Phòng",
    "Vĩnh Phúc",
    "Bắc Ninh",
    "Thái Nguyên",
    "Bắc Giang",
  ];

  if (nearbyProvinces.some((p) => p.toLowerCase() === destinationCity)) {
    return 25000;
  }

  // Miền Bắc khác
  const northProvinces = [
    "Yên Bái",
    "Sơn La",
    "Hòa Bình",
    "Hà Giang",
    "Điện Biên",
    "Lai Châu",
    "Tuyên Quang",
    "Lạng Sơn",
    "Cao Bằng",
  ];

  if (northProvinces.some((p) => p.toLowerCase() === destinationCity)) {
    return 40000;
  }

  // Miền Trung
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
    "Bình Định",
    "Phú Yên",
    "Khánh Hòa",
    "Ninh Thuận",
    "Bình Thuận",
  ];

  if (centralProvinces.some((p) => p.toLowerCase() === destinationCity)) {
    return 60000;
  }

  // Miền Nam
  return 80000;
}

/**
 * Calculate shipping fee from GHN API
 * Falls back to local calculation if GHN is not configured or fails
 * Matches logic from /view/.../ shippingFeeService.ts
 */
export async function calculateShippingFeeFromGHN(
  location: ShippingLocation,
  weight: number = 500,
  length: number = 10,
  width: number = 10,
  height: number = 10
): Promise<ShippingFeeResponse> {
  try {
    // Validate location
    if (!location.thanhPho || !location.quan || !location.phuong) {
      return {
        fee: 0,
        message: "Địa chỉ không đầy đủ",
      };
    }

    const GHN_TOKEN = import.meta.env.VITE_GHN_TOKEN || "";
    const GHN_DEV_TOKEN = import.meta.env.VITE_GHN_DEV_TOKEN || "";

    if (!GHN_TOKEN) {
      // Fallback to local calculation if GHN token not configured
      const fallbackFee = calculateLocalShippingFee(location);
      return {
        fee: fallbackFee,
        message: "Sử dụng giá mặc định",
      };
    }

    // Fetch district info to get district ID
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
      const fallbackFee = calculateLocalShippingFee(location);
      return {
        fee: fallbackFee,
        message: "Không thể lấy thông tin quận huyện",
      };
    }

    const districtData = await districtRes.json();
    const destinationDistrict = districtData.data?.find((d: any) =>
      d.DistrictName.toLowerCase().includes(location.quan.toLowerCase())
    );

    if (!destinationDistrict) {
      const fallbackFee = calculateLocalShippingFee(location);
      return {
        fee: fallbackFee,
        message: "Không tìm thấy quận huyện",
      };
    }

    // Fetch ward info to get ward ID
    const wardRes = await fetch(
      "https://online-gateway.ghn.vn/shiip/public-api/master-data/ward",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Token: GHN_TOKEN,
        },
        body: JSON.stringify({
          district_id: destinationDistrict.DistrictID,
        }),
      }
    );

    if (!wardRes.ok) {
      const fallbackFee = calculateLocalShippingFee(location);
      return {
        fee: fallbackFee,
        message: "Không thể lấy thông tin phường xã",
      };
    }

    const wardData = await wardRes.json();
    const destinationWard = wardData.data?.find((w: any) =>
      w.WardName.toLowerCase().includes(location.phuong.toLowerCase())
    );

    if (!destinationWard) {
      const fallbackFee = calculateLocalShippingFee(location);
      return {
        fee: fallbackFee,
        message: "Không tìm thấy phường xã",
      };
    }

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
      service_type_id: 2, // Standard service (required by GHN API)
      insurance_value: 0,
      cod_failed_amount: 0,
      coupon: null,
    };

    // Try production API first
    let feeRes = await fetch(
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

    // If production fails, retry with dev server (if dev token is configured)
    if (!feeRes.ok && GHN_DEV_TOKEN) {
      console.warn(
        `[ShippingFee] Production API failed (${feeRes.status}), trying dev server with token...`
      );
      feeRes = await fetch(
        "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Token: GHN_DEV_TOKEN,
          },
          body: JSON.stringify(feePayload),
        }
      );
    }

    const feeData = await feeRes.json();

    if (!feeRes.ok) {
      const isDevServer = feeRes.url?.includes("dev-online-gateway");
      const tokenUsed = isDevServer ? GHN_DEV_TOKEN : GHN_TOKEN;
      console.error("[ShippingFee] GHN API Error:", {
        status: feeRes.status,
        server: isDevServer ? "dev-online-gateway" : "online-gateway",
        payload: feePayload,
        response: feeData,
        tokenExists: !!tokenUsed,
        tokenUsed: tokenUsed ? `${tokenUsed.substring(0, 8)}...` : "N/A",
      });
      const fallbackFee = calculateLocalShippingFee(location);
      return {
        fee: fallbackFee,
        message: "Không thể lấy phí vận chuyển từ GHN",
      };
    }

    if (!feeData.data || typeof feeData.data.total !== "number") {
      const fallbackFee = calculateLocalShippingFee(location);
      return {
        fee: fallbackFee,
        message: "Phí GHN không hợp lệ, sử dụng giá mặc định",
      };
    }

    return {
      fee: feeData.data.total,
      message: "Phí từ GHN",
    };
  } catch (error) {
    console.error("Error calculating shipping fee from GHN:", error);
    // Fallback to local calculation
    const fallbackFee = calculateLocalShippingFee(location);
    return {
      fee: fallbackFee,
      message: "Lỗi khi tính phí vận chuyển",
    };
  }
}
