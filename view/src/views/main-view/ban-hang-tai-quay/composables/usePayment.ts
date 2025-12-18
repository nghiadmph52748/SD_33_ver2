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
    await recalculateShippingFee()
  }

  const recalculateShippingFee = async (): Promise<number> => {
    try {
      if (!walkInLocation.value.thanhPho || !walkInLocation.value.quan || !walkInLocation.value.phuong) {
        shippingFee.value = 0
        return 0
      }
      const result = await calculateShippingFeeFromGHN(walkInLocation.value)
      shippingFee.value = result.fee
      return result.fee
    } catch (error) {
      console.error('[Payment] Error recalculating shipping fee:', error)
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
    // Prefer structured address data if available
    if (customer.addressInfo) {
      walkInLocation.value.thanhPho = customer.addressInfo.thanhPho || ''
      walkInLocation.value.quan = customer.addressInfo.quan || ''
      walkInLocation.value.phuong = customer.addressInfo.phuong || ''

      // Extract only specific address (Số nhà, Đường) - remove ward/district if present
      let specificAddress = customer.addressInfo.diaChiCuThe || ''
      // If address contains ward/district names, try to remove them
      if (specificAddress && (customer.addressInfo.phuong || customer.addressInfo.quan)) {
        const phuongName = customer.addressInfo.phuong
        const quanName = customer.addressInfo.quan
        // Remove ward and district from end of address
        if (phuongName) {
          specificAddress = specificAddress.replace(`, ${phuongName}`, '').replace(`${phuongName}`, '')
        }
        if (quanName) {
          specificAddress = specificAddress.replace(`, ${quanName}`, '').replace(`${quanName}`, '')
        }
        // Clean up extra commas
        specificAddress = specificAddress.replace(/,\s*$/, '').trim()
      }
      walkInLocation.value.diaChiCuThe = specificAddress

      // Load districts and wards for the selected province
      const province = provinces.value.find((p) => p.value === customer.addressInfo!.thanhPho)
      if (province) {
        try {
          const districts = await fetchDistrictsByProvinceCode(province.code)
          walkInLocation.value.districts = [...districts]

          // Load wards for the selected district
          const district = districts.find((d: DistrictOption) => d.value === customer.addressInfo!.quan)
          if (district) {
            try {
              const wards = await fetchWardsByDistrictCode(district.code)
              walkInLocation.value.wards = [...wards]
            } catch {
              console.warn('Failed to load wards')
            }
          }
        } catch {
          console.warn('Failed to load districts')
        }
      }
      return
    }

    // Fallback to parsing address string
    if (!customer.address) {
      walkInLocation.value.diaChiCuThe = ''
      return
    }
    // Store address in diaChiCuThe (already should be just specific address from loadCustomers)
    walkInLocation.value.diaChiCuThe = customer.address

    // Try to extract province from address by matching with provinces list
    const addressParts = customer.address.split(',').map((p) => p.trim())
    const matchedProvince = addressParts
      .map((part) => provinces.value.find((p) => p.value.toLowerCase() === part.toLowerCase()))
      .find((p) => p !== undefined)

    if (matchedProvince) {
      walkInLocation.value.thanhPho = matchedProvince.value
    }
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
