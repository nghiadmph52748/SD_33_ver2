import { ref, computed, type Ref } from 'vue'
import { type BienTheSanPham, type ChatLieu, type DeGiay, type MauSac, type KichThuoc } from '@/api/san-pham/bien-the'
import { fetchFilterOptions, fetchAllAvailableProducts } from '../services/productService'
import { Message } from '@arco-design/web-vue'

export function useProductModal(params: { allProductVariants: Ref<BienTheSanPham[]> }) {
  const { allProductVariants } = params

  const showProductModal = ref(false)
  const productSearchText = ref('')
  const productFilters = ref({
    tenChatLieu: null as string | null,
    tenDeGiay: null as string | null,
    tenNhaSanXuat: null as string | null,
    tenXuatXu: null as string | null,
    tenMauSac: null as string | null,
    tenKichThuoc: null as string | null,
  })
  const filterOptionsData = ref({
    chatLieu: [] as ChatLieu[],
    deGiay: [] as DeGiay[],
    mauSac: [] as MauSac[],
    kichThuoc: [] as KichThuoc[],
  })

  const productPagination = ref({
    current: 1,
    pageSize: 10,
    total: 0,
  })

  const productMaterialOptions = computed(() => [
    { label: 'Tất cả', value: null },
    ...filterOptionsData.value.chatLieu.map((m) => ({ label: m.tenChatLieu, value: m.tenChatLieu })),
  ])
  const productSoleOptions = computed(() => [
    { label: 'Tất cả', value: null },
    ...filterOptionsData.value.deGiay.map((s) => ({ label: s.tenDeGiay, value: s.tenDeGiay })),
  ])
  const productColorOptions = computed(() => [
    { label: 'Tất cả', value: null },
    ...filterOptionsData.value.mauSac.map((c) => ({ label: c.tenMauSac, value: c.tenMauSac })),
  ])
  const productSizeOptions = computed(() => [
    { label: 'Tất cả', value: null },
    ...filterOptionsData.value.kichThuoc.map((k) => ({ label: k.tenKichThuoc, value: k.tenKichThuoc })),
  ])

  const productManufacturerOptions = computed(() => {
    const manufacturers = [...new Set(allProductVariants.value.map((p) => p.tenNhaSanXuat).filter(Boolean))] as string[]
    return [{ label: 'Tất cả', value: null }, ...manufacturers.map((m) => ({ label: m, value: m }))]
  })

  const productOriginOptions = computed(() => {
    const origins = [...new Set(allProductVariants.value.map((p) => p.tenXuatXu).filter(Boolean))] as string[]
    return [{ label: 'Tất cả', value: null }, ...origins.map((o) => ({ label: o, value: o }))]
  })

  const filteredProductVariants = computed(() => {
    let result = allProductVariants.value

    if (productSearchText.value) {
      const query = productSearchText.value.toLowerCase()
      result = result.filter(
        (p) =>
          p.tenSanPham?.toLowerCase().includes(query) ||
          p.maChiTietSanPham?.toLowerCase().includes(query) ||
          p.tenChatLieu?.toLowerCase().includes(query) ||
          p.tenDeGiay?.toLowerCase().includes(query) ||
          p.tenNhaSanXuat?.toLowerCase().includes(query) ||
          p.tenXuatXu?.toLowerCase().includes(query) ||
          p.tenMauSac?.toLowerCase().includes(query) ||
          p.tenKichThuoc?.toLowerCase().includes(query) ||
          p.giaBan?.toString().includes(query)
      )
    }

    const filters = productFilters.value
    if (filters.tenChatLieu) result = result.filter((p) => p.tenChatLieu === filters.tenChatLieu)
    if (filters.tenDeGiay) result = result.filter((p) => p.tenDeGiay === filters.tenDeGiay)
    if (filters.tenNhaSanXuat) result = result.filter((p) => p.tenNhaSanXuat === filters.tenNhaSanXuat)
    if (filters.tenXuatXu) result = result.filter((p) => p.tenXuatXu === filters.tenXuatXu)
    if (filters.tenMauSac) result = result.filter((p) => p.tenMauSac === filters.tenMauSac)
    if (filters.tenKichThuoc) result = result.filter((p) => p.tenKichThuoc === filters.tenKichThuoc)

    productPagination.value.total = result.length

    const startIndex = (productPagination.value.current - 1) * productPagination.value.pageSize
    const endIndex = startIndex + productPagination.value.pageSize
    return result.slice(startIndex, endIndex)
  })

  const loadFilterOptions = async () => {
    try {
      const opts = await fetchFilterOptions()
      filterOptionsData.value = opts
    } catch (error) {
      console.error('Error loading filter options:', error)
    }
  }

  const loadProductPage = async (page: number) => {
    try {
      productPagination.value.current = page
    } catch (error) {
      console.error('Error changing page:', error)
      Message.error('Không thể chuyển trang')
    }
  }

  const loadAllProducts = async () => {
    try {
      const availableProducts = await fetchAllAvailableProducts(100)
      allProductVariants.value = availableProducts
      productPagination.value.total = availableProducts.length
      productPagination.value.current = 1
    } catch (error) {
      console.error('Error loading all products:', error)
      Message.error('Không thể tải sản phẩm')
    }
  }

  const resetProductFilters = () => {
    productSearchText.value = ''
    productFilters.value = {
      tenChatLieu: null,
      tenDeGiay: null,
      tenNhaSanXuat: null,
      tenXuatXu: null,
      tenMauSac: null,
      tenKichThuoc: null,
    }
  }

  const openProductModal = async () => {
    await loadAllProducts()
    showProductModal.value = true
  }

  const handleProductModalCancel = () => {
    showProductModal.value = false
    resetProductFilters()
  }

  return {
    showProductModal,
    productSearchText,
    productFilters,
    filterOptionsData,
    productPagination,
    productMaterialOptions,
    productSoleOptions,
    productManufacturerOptions,
    productOriginOptions,
    productColorOptions,
    productSizeOptions,
    filteredProductVariants,
    loadFilterOptions,
    loadProductPage,
    loadAllProducts,
    resetProductFilters,
    openProductModal,
    handleProductModalCancel,
  }
}
