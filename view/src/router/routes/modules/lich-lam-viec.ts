import { DEFAULT_LAYOUT } from '../base'
import type { AppRouteRecordRaw } from '../types'

const LICH_LAM_VIEC: AppRouteRecordRaw = {
  path: '/lich-lam-viec',
  name: 'LichLamViec',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'Lịch làm việc',
    requiresAuth: true,
    icon: 'calendar',
    order: 6,
    roles: ['*'],
  },
  children: [
    {
      path: 'ca-lam-viec',
      name: 'CaLamViec',
      component: () => import('@/views/main-view/lich-lam-viec/ca-lam-viec.vue'),
      meta: {
        locale: 'Ca làm việc',
        requiresAuth: true,
        roles: ['admin'],
      },
    },
    {
      path: 'danh-sach', //  path con khác nhau
      name: 'QuanLyLichLamViec',
      component: () => import('@/views/main-view/lich-lam-viec/lich-lam-viec.vue'),
      meta: {
        locale: 'Lịch làm việc',
        requiresAuth: true,
        roles: ['admin'],
      },
    },

    {
      path: 'lich-nhan-vien',
      name: 'LichNhanVien',
      component: () => import('@/views/main-view/lich-lam-viec/lich-nhan-vien.vue'),
      meta: {
        locale: 'Lịch nhân viên',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      path: 'them-ca-lam-viec',
      name: 'themcalamviec',
      component: () => import('@/views/main-view/lich-lam-viec/themcalamviec.vue'),
      meta: {
        locale: 'Thêm Ca Làm Việc',
        requiresAuth: true,
        roles: ['*'],
        hideInMenu: true,
      },
    },
    {
      path: 'update-ca-lam-viec/:id',
      name: 'updatecalamviec',
      component: () => import('@/views/main-view/lich-lam-viec/updatecalamviec.vue'),
      meta: {
        locale: 'Update Ca Làm Việc',
        requiresAuth: true,
        roles: ['*'],
        hideInMenu: true,
      },
    },
    {
      path: 'them-lich-lam-viec',
      name: 'themlichlamviec',
      component: () => import('@/views/main-view/lich-lam-viec/themlichlamviec.vue'),
      meta: {
        locale: 'Thêm Lịch Làm Việc',
        requiresAuth: true,
        roles: ['*'],
        hideInMenu: true,
      },
    },
    {
      path: 'update-lich-lam-viec/:id',
      name: 'updatelichlamviec',
      component: () => import('@/views/main-view/lich-lam-viec/formupdatelichlamviec.vue'),
      meta: {
        locale: 'Cập Nhật Lịch Làm Việc',
        requiresAuth: true,
        roles: ['*'],
        hideInMenu: true,
      },
    },
    {
      path: 'giao-ca',
      name: 'giaoca',
      component: () => import('@/views/main-view/lich-lam-viec/giao-ca.vue'),
      meta: {
        locale: 'Giao Ca',
        requiresAuth: true,
        roles: ['user'],
      },
    },
   
  ],
}

export default LICH_LAM_VIEC
