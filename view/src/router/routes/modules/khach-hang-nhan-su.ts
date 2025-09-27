import { DEFAULT_LAYOUT } from '../base'
import { AppRouteRecordRaw } from '../types'

const KHACH_HANG_NHAN_SU: AppRouteRecordRaw = {
  path: '/khach-hang-nhan-su',
  name: 'khach-hang-nhan-su',
  component: DEFAULT_LAYOUT,
  redirect: { name: 'QuanLyKhachHang' },
  meta: {
    locale: 'menu.quan-ly-tai-khoan',
    requiresAuth: true,
    icon: 'IconUser',
    order: 5,
    roles: ['*'],
  },
  children: [
    {
      path: 'khach-hang',
      name: 'QuanLyKhachHang',
      component: () => import('@/views/main-view/khach-hang-nhan-su/khach-hang/quan-ly-khach-hang.vue'),
      meta: {
        locale: 'menu.quan-ly-tai-khoan.khach-hang',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      path: 'nhan-vien',
      name: 'QuanLyNhanVien',
      component: () => import('@/views/main-view/khach-hang-nhan-su/nhan-vien/quan-ly-nhan-vien.vue'),
      meta: {
        locale: 'menu.quan-ly-tai-khoan.nhan-vien',
        requiresAuth: true,
        roles: ['*'],
      },
    },
  ],
}

export default KHACH_HANG_NHAN_SU
