import { DEFAULT_LAYOUT } from '../base'
import type { AppRouteRecordRaw } from '../types'

const THANH_TOAN: AppRouteRecordRaw = {
  path: '/payment',
  name: 'payment-root',
  component: DEFAULT_LAYOUT,
  meta: {
    hideInMenu: true,
    requiresAuth: false,
  },
  children: [
    {
      path: 'vnpay/result',
      name: 'VnpayResult',
      component: () => import('@/views/payment/vnpay-result.vue'),
      meta: {
        hideInMenu: true,
        requiresAuth: false,
      },
    },
  ],
}

export default THANH_TOAN
