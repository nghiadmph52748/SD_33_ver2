import { DEFAULT_LAYOUT } from '../base'
import { AppRouteRecordRaw } from '../types'

const BAN_HANG_TAI_QUAY: AppRouteRecordRaw = {
  path: '/ban-hang-tai-quay',
  name: 'ban-hang-tai-quay',
  component: DEFAULT_LAYOUT,
  redirect: { name: 'BanHangTaiQuayIndex' },
  meta: {
    locale: 'menu.ban-hang',
    requiresAuth: true,
    icon: 'IconHome',
    order: 2,
    roles: ['*'],
  },
  children: [
    {
      path: 'index',
      name: 'BanHangTaiQuayIndex',
      component: () => import('@/views/main-view/ban-hang-tai-quay/ban-hang-tai-quay.vue'),
      meta: {
        locale: 'menu.ban-hang.index',
        requiresAuth: true,
        roles: ['*'],
      },
    },
  ],
}

export default BAN_HANG_TAI_QUAY
