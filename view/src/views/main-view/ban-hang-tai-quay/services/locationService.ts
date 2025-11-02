export async function fetchProvinces() {
  const res = await fetch('https://provinces.open-api.vn/api/p/')
  const data = await res.json()
  return data.map((p: any) => ({ value: p.name, label: p.name, code: p.code }))
}

export async function fetchDistrictsByProvinceCode(code: number) {
  const res = await fetch(`https://provinces.open-api.vn/api/p/${code}?depth=2`)
  const data = await res.json()
  return data.districts.map((d: any) => ({ value: d.name, label: d.name, code: d.code }))
}

export async function fetchWardsByDistrictCode(code: number) {
  const res = await fetch(`https://provinces.open-api.vn/api/d/${code}?depth=2`)
  const data = await res.json()
  return data.wards.map((w: any) => ({ value: w.name, label: w.name }))
}
