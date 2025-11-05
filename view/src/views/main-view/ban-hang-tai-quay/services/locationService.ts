// GHN API for Vietnam location data
const GHN_TOKEN = import.meta.env.VITE_GHN_TOKEN || ''

export async function fetchProvinces() {
  try {
    const res = await fetch('https://online-gateway.ghn.vn/shiip/public-api/master-data/province', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        Token: GHN_TOKEN,
      },
    })
    if (!res.ok) return []
    const response = await res.json()
    if (!response.data) return []
    return response.data.map((p: any) => ({
      value: p.ProvinceName,
      label: p.ProvinceName,
      code: p.ProvinceID,
    }))
  } catch {
    return []
  }
}

export async function fetchDistrictsByProvinceCode(code: number) {
  try {
    const res = await fetch('https://online-gateway.ghn.vn/shiip/public-api/master-data/district', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        Token: GHN_TOKEN,
      },
    })
    if (!res.ok) return []
    const response = await res.json()
    if (!response.data) return []
    // Filter by ProvinceID since API doesn't filter client-side
    const filtered = response.data.filter((d: any) => d.ProvinceID === code)
    return filtered.map((d: any) => ({
      value: d.DistrictName,
      label: d.DistrictName,
      code: d.DistrictID,
    }))
  } catch {
    return []
  }
}

export async function fetchWardsByDistrictCode(code: number) {
  try {
    const res = await fetch('https://online-gateway.ghn.vn/shiip/public-api/master-data/ward', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Token: GHN_TOKEN,
      },
      body: JSON.stringify({
        district_id: code,
      }),
    })
    if (!res.ok) return []
    const response = await res.json()
    if (!response.data) return []
    return response.data.map((w: any) => ({
      value: w.WardName,
      label: w.WardName,
    }))
  } catch {
    return []
  }
}
