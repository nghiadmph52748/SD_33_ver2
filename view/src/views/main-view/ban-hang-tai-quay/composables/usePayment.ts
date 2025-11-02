import { ref, type Ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { useUserStore } from '@/store'
import { updatePaymentMethod as svcUpdatePaymentMethod, updateShippingMethod as svcUpdateShippingMethod, updateVoucher as svcUpdateVoucher } from '../services/posService'
import { fetchProvinces, fetchDistrictsByProvinceCode, fetchWardsByDistrictCode } from '../services/locationService'

interface Customer {
  id: string
  name: string
  phone: string
  email?: string
  address?: string
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

export function usePayment(params: { currentOrder: Ref<Order | null> }) {
  const { currentOrder } = params
  const userStore = useUserStore()

  // Core payment state
  const paymentForm = ref({
    discountCode: null as string | null,
    method: 'cash' as 'cash' | 'transfer' | 'both',
    cashReceived: 0,
    transferReceived: 0,
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

  const handleCashAmountChange = (value: number) => {
    paymentForm.value.cashReceived = value || 0
  }

  const handleTransferAmountChange = (value: number) => {
    paymentForm.value.transferReceived = value || 0
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
        walkInLocation.value.districts = await fetchDistrictsByProvinceCode(province.code)
      } catch (error) {
        console.error('Error loading districts:', error)
      }
    }
  }

  const onWalkInDistrictChange = async (value: string) => {
    walkInLocation.value.quan = value
    walkInLocation.value.wards = []
    walkInLocation.value.phuong = ''

    const district = walkInLocation.value.districts.find((d) => d.value === value)
    if (district) {
      try {
        walkInLocation.value.wards = await fetchWardsByDistrictCode(district.code)
      } catch (error) {
        console.error('Error loading wards:', error)
      }
    }
  }

  const updateInvoicePayment = async (invoiceId: number, paymentMethod: 'cash' | 'transfer' | 'both') => {
    try {
      await svcUpdatePaymentMethod(invoiceId, paymentMethod, userStore.id)
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
      if (orderType.value !== 'delivery') {
        Message.info('Đơn hàng tại quầy, không cần cập nhật giao hàng')
        return
      }
      await svcUpdateShippingMethod(invoiceId, userStore.id)
      Message.success('Hình thức giao hàng đã được cập nhật')
    } catch (error: any) {
      console.error('Lỗi cập nhật giao hàng:', error)
      Message.error(error.message || 'Có lỗi xảy ra khi cập nhật giao hàng')
      throw error
    }
  }

  const updateInvoiceVoucher = async (invoiceId: number, voucherId: number) => {
    try {
      await svcUpdateVoucher(invoiceId, voucherId, userStore.id)
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

    handleCashAmountChange,
    handleTransferAmountChange,
    clearVoucher,

    updateInvoicePayment,
    updateInvoiceShipping,
    updateInvoiceVoucher,
  }
}
