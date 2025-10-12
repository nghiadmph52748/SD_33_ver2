import { DEFAULT_LAYOUT } from '../base'
import { AppRouteRecordRaw } from '../types'

const QUAN_LY_HOA_DON: AppRouteRecordRaw = {
  path: '/quan-ly-hoa-don',
  name: 'quan-ly-hoa-don',
  component: DEFAULT_LAYOUT,
  redirect: '/quan-ly-hoa-don/index',
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
      component: () => import('@/views/main-view/quan-ly-hoa-don/chi-tiet-hoa-don.vue'),
      meta: {
        locale: 'menu.hoa-don-chi-tiet',
        requiresAuth: true,
        roles: ['*'],
        hideInMenu: true,
        activeMenu: 'QuanLyHoaDonIndex',
      },
    },
    {
      path: 'in/:id',
      name: 'InHoaDon',
      component: () => import('@/views/main-view/quan-ly-hoa-don/invoice-print.vue'),
      meta: {
        locale: 'In hóa đơn',
        requiresAuth: false,
        roles: ['*'],
        hideInMenu: true,
        activeMenu: 'QuanLyHoaDonIndex',
      },
    },
    {
      path: 'test-api',
      name: 'TestApi',
      component: () => import('@/views/main-view/quan-ly-hoa-don/test-api.vue'),
      meta: {
        locale: 'Test API',
        requiresAuth: true,
        roles: ['*'],
        hideInMenu: true,
      },
    },
  ],
}

export default QUAN_LY_HOA_DON
