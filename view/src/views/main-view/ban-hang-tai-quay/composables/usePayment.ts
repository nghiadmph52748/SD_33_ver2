import { ref, type Ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { useUserStore } from '@/store'
import {
  updatePaymentMethod as svcUpdatePaymentMethod,
  updateShippingMethod as svcUpdateShippingMethod,
  updateVoucher as svcUpdateVoucher,
} from '../services/posService'
import { fetchProvinces, fetchDistrictsByProvinceCode, fetchWardsByDistrictCode } from '../services/locationService'
import { calculateShippingFeeFromGHN } from '../services/shippingFeeService'

interface Customer {
  id: string
  name: string
  phone: string
  email?: string
  address?: string
  addressInfo?: {
    thanhPho: string
    quan: string
    phuong: string
    diaChiCuThe: string
  }
}

interface OrderItem {
  quantity: number
}
interface Order {
  items: OrderItem[]
  customerId: string | null
  id: string
}

interface ProvinceOption {
  value: string
  label: string
  code: number
}
interface DistrictOption {
  value: string
  label: string
  code: number
}
interface WardOption {
  value: string
  label: string
}

const normalizeLocationText = (value?: string | null) => {
  if (!value) return ''
  return value
    .toString()
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .replace(/\b(tp\.?|thanh pho|tinh|quan|q\.?|huyen|phuong|p\.?|xa|thi tran|tt\.?)\b/giu, ' ')
    .replace(/[^a-zA-Z0-9]+/g, '')
    .toLowerCase()
}

const findBestOptionMatch = <T extends { value: string }>(options: T[], target?: string | null): T | null => {
  if (!Array.isArray(options) || options.length === 0 || !target) return null
  const trimmed = target.trim()
  if (!trimmed) return null
  const directMatch = options.find((opt) => opt.value.localeCompare(trimmed, undefined, { sensitivity: 'accent' }) === 0)
  if (directMatch) return directMatch

  const normalizedTarget = normalizeLocationText(trimmed)
  if (!normalizedTarget) return null

  const normalizedMap = options.map((opt) => ({ option: opt, normalized: normalizeLocationText(opt.value) }))
  const exactNormalized = normalizedMap.find((item) => item.normalized === normalizedTarget)
  if (exactNormalized) return exactNormalized.option

  const partialMatches = normalizedMap
    .filter((item) => item.normalized && (item.normalized.includes(normalizedTarget) || normalizedTarget.includes(item.normalized)))
    .sort((a, b) => b.normalized.length - a.normalized.length)
  return partialMatches.length > 0 ? partialMatches[0].option : null
}

const parseAddressString = (raw?: string | null) => {
  const defaults = { diaChiCuThe: '', phuong: '', quan: '', thanhPho: '' }
  if (!raw) return defaults
  const segments = raw
    .split(',')
    .map((segment) => segment.trim())
    .filter((segment) => segment.length > 0)
  if (segments.length === 0) return defaults
  const parts = [...segments]
  const result = { ...defaults }
  result.thanhPho = parts.pop() ?? ''
  result.quan = parts.pop() ?? ''
  result.phuong = parts.length >= 1 ? (parts.pop() ?? '') : ''
  result.diaChiCuThe = parts.length > 0 ? parts.join(', ') : ''
  if (!result.diaChiCuThe && segments.length === 1) {
    result.diaChiCuThe = segments[0]
  }
  return result
}

const escapeRegex = (value: string) => value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')

export default function usePayment(params: { currentOrder: Ref<Order | null> }) {
  const { currentOrder } = params
  const userStore = useUserStore()

  // Core payment state
  const paymentForm = ref({
    discountCode: null as string | null,
    method: 'cash' as 'cash' | 'transfer' | 'both',
    cashReceived: 0 as number | null,
    transferReceived: 0 as number | null,
  })

  const orderType = ref<'counter' | 'delivery'>('counter')
  const shippingFee = ref(0)

  // Location data for walk-in customers
  const provinces = ref<ProvinceOption[]>([])
  const walkInLocation = ref({
    thanhPho: '',
    quan: '',
    phuong: '',
    diaChiCuThe: '',
    districts: [] as DistrictOption[],
    wards: [] as WardOption[],
  })

  // Derived state will be computed in component to avoid circular deps

  const handleCashAmountChange = (value: number | null | undefined) => {
    if (value === null || value === undefined || Number.isNaN(value)) {
      paymentForm.value.cashReceived = null
      return
    }
    paymentForm.value.cashReceived = value
  }

  const handleTransferAmountChange = (value: number | null | undefined) => {
    if (value === null || value === undefined || Number.isNaN(value)) {
      paymentForm.value.transferReceived = null
      return
    }
    paymentForm.value.transferReceived = value
  }

  const clearVoucher = () => {
    paymentForm.value.discountCode = null
    Message.info('Đã xóa voucher')
  }

  // Shipping helpers
  const loadProvinces = async () => {
    try {
      provinces.value = await fetchProvinces()
    } catch (error) {
      console.error('Error loading provinces:', error)
    }
  }

  const onWalkInProvinceChange = async (value: string) => {
    walkInLocation.value.thanhPho = value
    walkInLocation.value.districts = []
    walkInLocation.value.wards = []
    walkInLocation.value.quan = ''
    walkInLocation.value.phuong = ''

    const province = provinces.value.find((p) => p.value === value)
    if (province) {
      try {
        const districts = await fetchDistrictsByProvinceCode(province.code)
        walkInLocation.value.districts = [...districts]
      } catch {
        // District loading failed
      }
    }

    // Reset shipping fee when province changes
    shippingFee.value = 0
  }

  const onWalkInDistrictChange = async (value: string) => {
    walkInLocation.value.quan = value
    walkInLocation.value.wards = []
    walkInLocation.value.phuong = ''

    const district = walkInLocation.value.districts.find((d) => d.value === value)
    if (district) {
      try {
        const wards = await fetchWardsByDistrictCode(district.code)
        walkInLocation.value.wards = [...wards]
      } catch {
        // Ward loading failed
      }
    }

    // Recalculate shipping fee when district changes
    await recalculateShippingFee('POS:district-change')
  }

  const recalculateShippingFee = async (reason?: string): Promise<number> => {
    const logContext = reason || 'POS:unknown'
    const locationSnapshot = { ...walkInLocation.value }
    try {
      if (!locationSnapshot.thanhPho || !locationSnapshot.quan || !locationSnapshot.phuong) {
        shippingFee.value = 0
        return 0
      }
      const result = await calculateShippingFeeFromGHN(locationSnapshot)
      shippingFee.value = result.fee
      return result.fee
    } catch (error) {
      console.error('[Payment] Error recalculating shipping fee:', error, { context: logContext })
      return shippingFee.value ?? 0
    }
  }

  /**
   * Fill walk-in location when customer address is selected
   * Use structured address data (addressInfo) if available, or parse address string
   */
  const fillWalkInLocationFromCustomer = async (customer: Customer | null) => {
    if (!customer) {
      // Reset if no customer
      walkInLocation.value = {
        thanhPho: '',
        quan: '',
        phuong: '',
        diaChiCuThe: '',
        districts: [],
        wards: [],
      }
      return
    }
    const structuredAddress = customer.addressInfo || parseAddressString(customer.address)

    if (!provinces.value.length) {
      await loadProvinces()
    }

    const matchedProvince = findBestOptionMatch(provinces.value, structuredAddress.thanhPho)
    walkInLocation.value.thanhPho = matchedProvince?.value || structuredAddress.thanhPho || ''

    let matchedDistrict: DistrictOption | null = null
    if (matchedProvince) {
      try {
        const districts = await fetchDistrictsByProvinceCode(matchedProvince.code)
        walkInLocation.value.districts = [...districts]
        matchedDistrict = findBestOptionMatch(districts, structuredAddress.quan)
        walkInLocation.value.quan = matchedDistrict?.value || ''
      } catch {
        walkInLocation.value.districts = []
        walkInLocation.value.quan = ''
      }
    } else {
      walkInLocation.value.districts = []
      walkInLocation.value.quan = ''
    }
    if (!walkInLocation.value.quan) {
      walkInLocation.value.quan = structuredAddress.quan || ''
    }

    if (matchedDistrict) {
      try {
        const wards = await fetchWardsByDistrictCode(matchedDistrict.code)
        walkInLocation.value.wards = [...wards]
        const matchedWard = findBestOptionMatch(wards, structuredAddress.phuong)
        walkInLocation.value.phuong = matchedWard?.value || ''
      } catch {
        walkInLocation.value.wards = []
        walkInLocation.value.phuong = ''
      }
    } else {
      walkInLocation.value.wards = []
      walkInLocation.value.phuong = ''
    }
    if (!walkInLocation.value.phuong) {
      walkInLocation.value.phuong = structuredAddress.phuong || ''
    }

    const phuongLabel = walkInLocation.value.phuong || structuredAddress.phuong
    const quanLabel = walkInLocation.value.quan || structuredAddress.quan
    let specificAddress = structuredAddress.diaChiCuThe || ''
    if (specificAddress && (phuongLabel || quanLabel)) {
      if (phuongLabel) {
        specificAddress = specificAddress.replace(new RegExp(`,?\\s*${escapeRegex(phuongLabel)}$`, 'i'), '').trim()
      }
      if (quanLabel) {
        specificAddress = specificAddress.replace(new RegExp(`,?\\s*${escapeRegex(quanLabel)}$`, 'i'), '').trim()
      }
      specificAddress = specificAddress.replace(/,\s*$/, '').trim()
    }
    walkInLocation.value.diaChiCuThe = specificAddress
  }

  const updateInvoicePayment = async (invoiceId: number, paymentMethod: 'cash' | 'transfer' | 'both') => {
    try {
      await svcUpdatePaymentMethod(invoiceId, paymentMethod, userStore.id || 1)
      const methodName = paymentMethod === 'cash' ? 'Tiền mặt' : paymentMethod === 'transfer' ? 'Chuyển khoản' : 'Cả hai'
      Message.success(`Hình thức thanh toán (${methodName}) đã được cập nhật`)
    } catch (error: any) {
      console.error('Lỗi cập nhật thanh toán:', error)
      Message.error(error.message || 'Có lỗi xảy ra khi cập nhật hình thức thanh toán')
      throw error
    }
  }

  const updateInvoiceShipping = async (invoiceId: number) => {
    try {
      // Convert orderType to boolean: 'delivery' = true, 'counter' = false
      const loaiDon = orderType.value === 'delivery'
      const fee = loaiDon ? shippingFee.value : 0
      await svcUpdateShippingMethod(invoiceId, loaiDon, fee, userStore.id || 1)
      Message.success('Hình thức giao hàng đã được cập nhật')
    } catch (error: any) {
      console.error('Lỗi cập nhật giao hàng:', error)
      Message.error(error.message || 'Có lỗi xảy ra khi cập nhật giao hàng')
      throw error
    }
  }

  const updateInvoiceVoucher = async (invoiceId: number, voucherId: number) => {
    try {
      await svcUpdateVoucher(invoiceId, voucherId, userStore.id || 1)
      Message.success('Voucher đã được áp dụng')
    } catch (error: any) {
      console.error('Lỗi cập nhật voucher:', error)

      if (error.message && error.message.includes('chưa có sản phẩm')) {
        Message.error('Giỏ hàng trống hoặc dữ liệu chưa được tải. Vui lòng thử lại sau vài giây.')
      } else {
        Message.error(error.message || 'Có lỗi xảy ra khi cập nhật voucher')
      }
      throw error
    }
  }

  return {
    paymentForm,
    orderType,
    shippingFee,

    provinces,
    walkInLocation,
    loadProvinces,
    onWalkInProvinceChange,
    onWalkInDistrictChange,
    fillWalkInLocationFromCustomer,

    handleCashAmountChange,
    handleTransferAmountChange,
    clearVoucher,

    updateInvoicePayment,
    updateInvoiceShipping,
    updateInvoiceVoucher,
    recalculateShippingFee,
  }
}
