import { DEFAULT_LAYOUT } from '../base'
import { AppRouteRecordRaw } from '../types'

const QUAN_LY_HOA_DON: AppRouteRecordRaw = {
  path: '/quan-ly-hoa-don',
  name: 'quan-ly-hoa-don',
  component: DEFAULT_LAYOUT,
  redirect: { name: 'QuanLyHoaDonIndex' },
  meta: {
    locale: 'menu.quan-ly-hoa-don',
    requiresAuth: true,
    icon: 'IconFolder',
    order: 3,
    roles: ['*'],
  },
  children: [
    {
      path: 'index',
      name: 'QuanLyHoaDonIndex',
      component: () => import('@/views/main-view/quan-ly-hoa-don/quan-ly-hoa-don.vue'),
      meta: {
        locale: 'menu.quan-ly-hoa-don.index',
        requiresAuth: true,
        roles: ['*'],
      },
    },
  ],
}

export default QUAN_LY_HOA_DON
