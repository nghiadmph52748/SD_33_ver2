import Detail from '@/views/main-view/khach-hang-nhan-su/nhan-vien/detail.vue'
import DetailKhachHang from '@/views/main-view/khach-hang-nhan-su/khach-hang/detailkhachang.vue'
import ThemKhachHang from '@/views/main-view/khach-hang-nhan-su/khach-hang/themkhachhang.vue'
import ThemNhanVien from '@/views/main-view/khach-hang-nhan-su/nhan-vien/themnhanvien.vue'
import Updatekhachhang from '@/views/main-view/khach-hang-nhan-su/khach-hang/updatekhachhang.vue'
import Updatenhanvien from '@/views/main-view/khach-hang-nhan-su/nhan-vien/updatenhanvien.vue'
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
      path: '/themnhanvien',
      name: 'ThemNhanVien',
      component: ThemNhanVien,
      meta: { hideInMenu: true },
    },
    {
      path: '/updatenhanvien/:id',
      name: 'Updatenhanvien',
      component: Updatenhanvien,
      meta: { hideInMenu: true },
    },
    {
      path: '/detail/:id', // dynamic route
      name: 'DetailNhanVien',
      component: Detail,
      meta: { hideInMenu: true },
    },
    {
      path: '/themkhachhang',
      name: 'ThemKhachHang',
      component: ThemKhachHang,
      meta: { hideInMenu: true },
    },
    {
      path: '/detailkhachhang/:id',
      name: 'ChiTietKhachHang',
      component: DetailKhachHang,
      meta: { hideInMenu: true },
    },
    {
      path: '/updatekhachhang/:id',
      name: 'UpdateKhachHang',
      component: Updatekhachhang,
      meta: { hideInMenu: true },
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
