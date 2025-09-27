import { DEFAULT_LAYOUT } from '../base'
import { AppRouteRecordRaw } from '../types'

const QUAN_LY_GIAM_GIA: AppRouteRecordRaw = {
  path: '/quan-ly-giam-gia',
  name: 'quan-ly-giam-gia',
  component: DEFAULT_LAYOUT,
  redirect: { name: 'QuanLyDotKhuyenMai' },
  meta: {
    locale: 'menu.quan-ly-giam-gia',
    requiresAuth: true,
    icon: 'IconStar',
    order: 6,
    roles: ['*'],
  },
  children: [
    {
      path: 'dot-khuyen-mai',
      name: 'QuanLyDotKhuyenMai',
      component: () => import('@/views/main-view/quan-ly-giam-gia/dot-khuyen-mai/dot-khuyen-mai.vue'),
      meta: {
        locale: 'menu.quan-ly-giam-gia.dot-khuyen-mai',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      path: 'dot-khuyen-mai/tao-moi',
      name: 'QuanLyDotKhuyenMaiCreate',
      component: () => import('@/views/main-view/quan-ly-giam-gia/dot-khuyen-mai/dot-khuyen-mai-create.vue'),
      meta: {
        locale: 'menu.quan-ly-giam-gia.dot-khuyen-mai.create',
        requiresAuth: true,
        roles: ['*'],
        hideInMenu: true,
        activeMenu: 'QuanLyDotKhuyenMai',
      },
    },
    {
      path: 'phieu-giam-gia',
      name: 'QuanLyPhieuGiamGia',
      component: () => import('@/views/main-view/quan-ly-giam-gia/phieu-giam-gia/phieu-giam-gia.vue'),
      meta: {
        locale: 'menu.quan-ly-giam-gia.phieu-giam-gia',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      path: 'phieu-giam-gia/tao-moi',
      name: 'QuanLyPhieuGiamGiaCreate',
      component: () => import('@/views/main-view/quan-ly-giam-gia/phieu-giam-gia/phieu-giam-gia-create.vue'),
      meta: {
        locale: 'menu.quan-ly-giam-gia.phieu-giam-gia.create',
        requiresAuth: true,
        roles: ['*'],
        hideInMenu: true,
        activeMenu: 'QuanLyPhieuGiamGia',
      },
    },
  ],
}

export default QUAN_LY_GIAM_GIA
