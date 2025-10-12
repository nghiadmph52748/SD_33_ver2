import { DEFAULT_LAYOUT } from '../base'
import { AppRouteRecordRaw } from '../types'

const QUAN_LY_GIAM_GIA: AppRouteRecordRaw = {
  path: '/quan-ly-giam-gia',
  name: 'quan-ly-giam-gia',
  component: DEFAULT_LAYOUT,
  redirect: { name: 'QuanLyDotGiamGia' },
  meta: {
    locale: 'menu.quan-ly-giam-gia',
    requiresAuth: true,
    icon: 'IconStar',
    order: 6,
    roles: ['*'],
  },
  children: [
    {
      path: 'dot-giam-gia',
      name: 'QuanLyDotGiamGia',
      component: () => import('@/views/main-view/quan-ly-giam-gia/dot-giam-gia/dot-giam-gia.vue'),
      meta: {
        locale: 'menu.quan-ly-giam-gia.dot-giam-gia',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      path: 'dot-giam-gia/tao-moi',
      name: 'QuanLyDotGiamGiaCreate',
      component: () => import('@/views/main-view/quan-ly-giam-gia/dot-giam-gia/dot-giam-gia-create.vue'),
      meta: {
        locale: 'menu.quan-ly-giam-gia.dot-giam-gia.create',
        requiresAuth: true,
        roles: ['*'],
        hideInMenu: true,
        activeMenu: 'QuanLyDotGiamGia',
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
