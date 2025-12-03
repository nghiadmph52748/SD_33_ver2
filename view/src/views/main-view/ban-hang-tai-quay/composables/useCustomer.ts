import { ref, computed, type Ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import type { KhachHangResponse } from '@/api/khach-hang'
import { useUserStore } from '@/store'
import { fetchCustomers } from '../services/customerService'
import { updateCustomerForInvoice } from '../services/posService'

interface AddressInfo {
  thanhPho: string
  quan: string
  phuong: string
  diaChiCuThe: string
}

interface Customer {
  id: string
  name: string
  phone: string
  email?: string
  address?: string
  addressInfo?: AddressInfo
}

interface Order {
  customerId: string | null
}

interface WalkInContact {
  name?: string
  phone?: string
  email?: string
}

export default function useCustomer(params: { currentOrder: Ref<Order | null> }) {
  const { currentOrder } = params
  const userStore = useUserStore()

  const customers = ref<Customer[]>([])
  const customerSearchText = ref('')
  const showAddCustomerModal = ref(false)
  const newCustomerForm = ref<{ name: string; phone: string; email?: string; address?: string }>({
    name: '',
    phone: '',
    email: '',
    address: '',
  })

  const filteredCustomers = computed(() => {
    if (!customerSearchText.value) return customers.value
    const query = customerSearchText.value.toLowerCase()
    return customers.value.filter((c) => c.name.toLowerCase().includes(query) || c.phone.toLowerCase().includes(query))
  })

  const selectedCustomer = computed(() => {
    if (!currentOrder.value?.customerId) return null
    return customers.value.find((c) => c.id === currentOrder.value?.customerId) || null
  })

  const updateCustomerId = (customerId: string) => {
    if (currentOrder.value) {
      currentOrder.value.customerId = customerId === '' ? '' : customerId || null
    }
  }

  const loadCustomers = async () => {
    try {
      const customersResponse = await fetchCustomers()
      if (customersResponse) {
        customers.value = customersResponse.map((c: KhachHangResponse) => {
          const defaultAddress = c.listDiaChi?.[0]
          const addressParts = [defaultAddress?.diaChiCuThe, defaultAddress?.phuong, defaultAddress?.quan, defaultAddress?.thanhPho].filter(
            (part) => typeof part === 'string' && part.trim().length > 0
          )

          return {
            id: c.id.toString(),
            name: c.tenKhachHang,
            phone: c.soDienThoai,
            email: c.email,
            address: addressParts.join(', '),
            addressInfo: {
              thanhPho: defaultAddress?.thanhPho || '',
              quan: defaultAddress?.quan || '',
              phuong: defaultAddress?.phuong || '',
              diaChiCuThe: defaultAddress?.diaChiCuThe || '',
            },
          }
        })
      }
    } catch (error: any) {
      console.error('Không thể tải danh sách khách hàng:', error)
      Message.error('Không thể tải danh sách khách hàng')
    }
  }

  const updateInvoiceCustomer = async (
    invoiceId: number,
    walkInLocation?: Ref<{ thanhPho: string; quan: string; phuong: string; diaChiCuThe: string }>,
    walkInContact?: WalkInContact
  ) => {
    try {
      let walkInAddress = ''
      const rawCustomerId = currentOrder.value?.customerId
      const isWalkIn = !selectedCustomer.value && (rawCustomerId === '' || rawCustomerId === null || rawCustomerId === undefined)
      if (isWalkIn && walkInLocation) {
        const addressParts = [
          walkInLocation.value.diaChiCuThe,
          walkInLocation.value.phuong,
          walkInLocation.value.quan,
          walkInLocation.value.thanhPho,
        ]
          .map((part) => (typeof part === 'string' ? part.trim() : part))
          .filter(Boolean)
        walkInAddress = addressParts.join(', ')
      }

      const customerId = selectedCustomer.value?.id ? parseInt(selectedCustomer.value.id) : undefined
      const contactName = (walkInContact?.name ?? '').trim()
      const contactPhone = (walkInContact?.phone ?? '').trim()
      const contactEmail = (walkInContact?.email ?? '').trim()
      const updateCustomerRequest: UpdateCustomerRequest = {
        idHoaDon: invoiceId,
        idKhachHang: customerId,
        tenKhachHang: selectedCustomer.value?.name || (isWalkIn ? contactName || 'Khách lẻ' : 'Khách lẻ'),
        soDienThoai: selectedCustomer.value?.phone ?? (isWalkIn ? contactPhone || null : null),
        diaChiKhachHang: selectedCustomer.value?.address || walkInAddress || null,
        emailKhachHang: selectedCustomer.value?.email ?? (isWalkIn ? contactEmail || null : null),
        idNhanVien: userStore.id,
      }

      await updateCustomerForInvoice(updateCustomerRequest)
      if (selectedCustomer.value) {
        Message.success('Thông tin khách hàng đã được cập nhật')
      }
    } catch (error: any) {
      console.error('Lỗi cập nhật khách hàng:', error)
      Message.error(error.message || 'Có lỗi xảy ra khi cập nhật khách hàng')
      throw error
    }
  }

  return {
    customers,
    customerSearchText,
    filteredCustomers,
    selectedCustomer,
    showAddCustomerModal,
    newCustomerForm,
    updateCustomerId,
    loadCustomers,
    updateInvoiceCustomer,
  }
}
