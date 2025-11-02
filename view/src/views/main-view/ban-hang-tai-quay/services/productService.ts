import {
  getBienTheSanPhamPage,
  getChatLieuOptions,
  getDeGiayOptions,
  getMauSacOptions,
  getKichThuocOptions,
  type BienTheSanPham,
  type ChatLieu,
  type DeGiay,
  type MauSac,
  type KichThuoc,
} from '@/api/san-pham/bien-the'

export async function fetchAllProducts(pageSize = 100): Promise<BienTheSanPham[]> {
  const firstPageResponse = await getBienTheSanPhamPage(0, undefined, pageSize)
  const total = firstPageResponse?.data?.total || 0
  let all: BienTheSanPham[] = [...(firstPageResponse?.data?.data || [])]
  const totalPages = Math.ceil(total / pageSize)
  if (totalPages > 1) {
    const pagePromises: Promise<any>[] = []
    for (let pageIndex = 1; pageIndex < totalPages; pageIndex++) {
      pagePromises.push(getBienTheSanPhamPage(pageIndex, undefined, pageSize))
    }
    const results = await Promise.all(pagePromises)
    for (const result of results) {
      if (result?.data?.data && Array.isArray(result.data.data)) {
        all = all.concat(result.data.data)
      }
    }
  }
  return all
}

export async function fetchAllAvailableProducts(pageSize = 100): Promise<BienTheSanPham[]> {
  const all = await fetchAllProducts(pageSize)
  return all.filter((p) => (p.soLuong ?? 0) > 0)
}

export async function fetchFilterOptions(): Promise<{
  chatLieu: ChatLieu[]
  deGiay: DeGiay[]
  mauSac: MauSac[]
  kichThuoc: KichThuoc[]
}> {
  const [chatLieuRes, deGiayRes, mauSacRes, kichThuocRes] = await Promise.all([
    getChatLieuOptions(),
    getDeGiayOptions(),
    getMauSacOptions(),
    getKichThuocOptions(),
  ])
  return {
    chatLieu: chatLieuRes?.data || [],
    deGiay: deGiayRes?.data || [],
    mauSac: mauSacRes?.data || [],
    kichThuoc: kichThuocRes?.data || [],
  }
}
