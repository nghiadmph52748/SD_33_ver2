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
    requiresAuth: true, // Bắt buộc xác thực để permission kiểm tra roles
    icon: 'IconUser',
    order: 5,
    roles: ['admin'], // Chỉ admin mới thấy menu này
    hideInMenu: true, // Ẩn hoàn toàn menu này trên sidebar
  },
  children: [
    {
      path: 'khach-hang',
      name: 'QuanLyKhachHang',
      component: () => import('@/views/main-view/khach-hang-nhan-su/khach-hang/quan-ly-khach-hang.vue'),
      meta: {
        locale: 'menu.quan-ly-tai-khoan.khach-hang',
        requiresAuth: true,
        roles: ['admin'],
      },
    },
    {
      path: 'nhan-vien/them',
      name: 'ThemNhanVien',
      component: ThemNhanVien,
      meta: { hideInMenu: true, activeMenu: 'QuanLyNhanVien', requiresAuth: true, roles: ['admin'] },
    },
    {
      path: 'nhan-vien/update/:id',
      name: 'Updatenhanvien',
      component: Updatenhanvien,
      meta: { hideInMenu: true, activeMenu: 'QuanLyNhanVien', requiresAuth: true, roles: ['admin'] },
    },
    {
      path: 'nhan-vien/detail/:id',
      name: 'DetailNhanVien',
      component: Detail,
      meta: { hideInMenu: true, activeMenu: 'QuanLyNhanVien', requiresAuth: true, roles: ['admin'] },
    },
    {
      path: 'khach-hang/them',
      name: 'ThemKhachHang',
      component: ThemKhachHang,
      meta: { hideInMenu: true, activeMenu: 'QuanLyKhachHang', requiresAuth: true, roles: ['admin'] },
    },
    {
      path: 'khach-hang/detail/:id',
      name: 'ChiTietKhachHang',
      component: DetailKhachHang,
      meta: { hideInMenu: true, activeMenu: 'QuanLyKhachHang', requiresAuth: true, roles: ['admin'] },
    },
    {
      path: 'khach-hang/update/:id',
      name: 'UpdateKhachHang',
      component: Updatekhachhang,
      meta: { hideInMenu: true, activeMenu: 'QuanLyKhachHang', requiresAuth: true, roles: ['admin'] },
    },
    {
      path: 'nhan-vien',
      name: 'QuanLyNhanVien',
      component: () => import('@/views/main-view/khach-hang-nhan-su/nhan-vien/quan-ly-nhan-vien.vue'),
      meta: {
        locale: 'menu.quan-ly-tai-khoan.nhan-vien',
        requiresAuth: true,
        roles: ['admin'],
      },
    },
  ],
}

export default KHACH_HANG_NHAN_SU
