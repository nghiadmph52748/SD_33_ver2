import { CustomerModel, fetchCustomers } from './customers'
import { fetchOrders, OrderModel } from './orders'
import { fetchProductVariants, ProductVariant } from './products'

export interface DashboardMetricSummary {
  totalRevenue: number
  totalOrders: number
  averageOrderValue: number
  activeCustomers: number
  inventoryCount: number
}

export interface DashboardSummary {
  metrics: DashboardMetricSummary
  recentOrders: OrderModel[]
  topProducts: ProductVariant[]
  topCustomers: CustomerModel[]
}

const toNumber = (value?: number | string | null) => {
  if (typeof value === 'number') return value
  if (!value) return 0
  const parsed = Number(value)
  return Number.isNaN(parsed) ? 0 : parsed
}

export const fetchDashboardSummary = async (): Promise<DashboardSummary> => {
  const [orders, customers, productPage] = await Promise.all([
    fetchOrders(),
    fetchCustomers(),
    fetchProductVariants(0, 50),
  ])

  const totalRevenue = orders.reduce<number>(
    (acc, order) => acc + toNumber(order.tongTienSauGiam ?? order.tongTien),
    0
  )

  const totalOrders = orders.length
  const averageOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0
  const activeCustomers = customers.filter((customer) => customer.trangThai).length
  const inventoryCount = productPage.data.reduce<number>((acc, variant) => acc + toNumber(variant.soLuong), 0)

  const topProducts = [...productPage.data]
    .sort((a, b) => toNumber(b.soLuong) - toNumber(a.soLuong))
    .slice(0, 5)

  const topCustomers = [...customers]
    .sort((a, b) => toNumber(b.tongChiTieu) - toNumber(a.tongChiTieu))
    .slice(0, 5)

  const recentOrders = [...orders]
    .sort((a, b) => {
      const dateA = a.ngayTao ? new Date(a.ngayTao).getTime() : 0
      const dateB = b.ngayTao ? new Date(b.ngayTao).getTime() : 0
      return dateB - dateA
    })
    .slice(0, 5)

  return {
    metrics: {
      totalRevenue,
      totalOrders,
      averageOrderValue,
      activeCustomers,
      inventoryCount,
    },
    recentOrders,
    topProducts,
    topCustomers,
  }
}
