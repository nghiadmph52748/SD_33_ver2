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
    hideChildrenInMenu: true, // Ẩn submenu trong sidebar
  },
  children: [
    {
      path: 'index',
      name: 'QuanLyHoaDonIndex',
      component: () => import('@/views/main-view/quan-ly-hoa-don/quan-ly-hoa-don.vue'),
      meta: {
        locale: 'menu.quan-ly-hoa-don',
        requiresAuth: true,
        roles: ['*'],
        hideInMenu: true, // Ẩn item này trong menu
      },
    },
    {
      path: 'chi-tiet/:id',
      name: 'HoaDonChiTiet',
      component: () => import('@/views/main-view/quan-ly-hoa-don/simple-test.vue'),
      meta: {
        locale: 'menu.hoa-don-chi-tiet',
        requiresAuth: true,
        roles: ['*'],
        hideInMenu: true, // Ẩn item này trong menu
      },
    },
  ],
}

export default QUAN_LY_HOA_DON
