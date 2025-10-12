import { DEFAULT_LAYOUT } from '../base'
import { AppRouteRecordRaw } from '../types'

const QUAN_LY_SAN_PHAM: AppRouteRecordRaw = {
  path: '/quan-ly-san-pham',
  name: 'quan-ly-san-pham',
  component: DEFAULT_LAYOUT,
  redirect: { name: 'DanhMucSanPham' },
  meta: {
    locale: 'menu.quan-ly-san-pham',
    requiresAuth: true,
    icon: 'IconSettings',
    order: 4,
    roles: ['*'],
  },
  children: [
    {
      path: 'danh-muc',
      name: 'DanhMucSanPham',
      component: () => import('@/views/main-view/quan-ly-san-pham/danh-muc/danh-muc-san-pham.vue'),
      meta: {
        locale: 'menu.quan-ly-san-pham.danh-muc',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      path: 'them-san-pham',
      name: 'ThemSanPham',
      // eslint-disable-next-line import/no-unresolved
      component: () => import('@/views/main-view/quan-ly-san-pham/them-san-pham/them-san-pham.vue'),
      meta: {
        locale: 'menu.quan-ly-san-pham.them-san-pham',
        requiresAuth: true,
        hideInMenu: true,
        activeMenu: 'DanhMucSanPham',
        roles: ['*'],
      },
    },
    {
      path: 'bien-the',
      name: 'BienTheSanPham',
      component: () => import('@/views/main-view/quan-ly-san-pham/bien-the/bien-the-san-pham.vue'),
      meta: {
        locale: 'menu.quan-ly-san-pham.bien-the',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      path: 'bien-the/:productId',
      name: 'BienTheSanPhamTheoProd',
      component: () => import('@/views/main-view/quan-ly-san-pham/bien-the/bien-the-san-pham.vue'),
      meta: {
        locale: 'Biến thể sản phẩm',
        requiresAuth: true,
        hideInMenu: true,
        activeMenu: 'BienTheSanPham',
        roles: ['*'],
        isProductSpecific: true,
      },
    },
    {
      path: 'bien-the/detail/:id',
      name: 'BienTheDetail',
      component: () => import('@/views/main-view/quan-ly-san-pham/bien-the/bien-the-detail.vue'),
      meta: {
        locale: 'Chi tiết biến thể',
        requiresAuth: true,
        hideInMenu: true,
        activeMenu: 'BienTheSanPham',
        roles: ['*'],
      },
    },
    {
      path: 'bien-the/update/:id',
      name: 'BienTheUpdate',
      component: () => import('@/views/main-view/quan-ly-san-pham/bien-the/bien-the-update.vue'),
      meta: {
        locale: 'Chỉnh sửa biến thể',
        requiresAuth: true,
        hideInMenu: true,
        activeMenu: 'BienTheSanPham',
        roles: ['*'],
      },
    },
    {
      path: 'thuoc-tinh',
      name: 'ThuocTinhSanPham',
      redirect: { name: 'AnhSanPham' },
      meta: {
        locale: 'menu.quan-ly-san-pham.thuoc-tinh',
        requiresAuth: true,
        roles: ['*'],
      },
      children: [
        {
          path: 'anh-san-pham',
          name: 'AnhSanPham',
          component: () => import('@/views/main-view/quan-ly-san-pham/thuoc-tinh/anh-san-pham/anh-san-pham.vue'),
          meta: {
            locale: 'menu.quan-ly-san-pham.thuoc-tinh.anh-san-pham',
            requiresAuth: true,
            roles: ['*'],
          },
        },
        {
          path: 'nha-san-xuat',
          name: 'NhaSanXuat',
          component: () => import('@/views/main-view/quan-ly-san-pham/thuoc-tinh/nha-san-xuat/nha-san-xuat.vue'),
          meta: {
            locale: 'menu.quan-ly-san-pham.thuoc-tinh.nha-san-xuat',
            requiresAuth: true,
            roles: ['*'],
          },
        },
        {
          path: 'xuat-xu',
          name: 'XuatXu',
          component: () => import('@/views/main-view/quan-ly-san-pham/thuoc-tinh/xuat-xu/xuat-xu.vue'),
          meta: {
            locale: 'menu.quan-ly-san-pham.thuoc-tinh.xuat-xu',
            requiresAuth: true,
            roles: ['*'],
          },
        },
        {
          path: 'mau-sac',
          name: 'MauSac',
          component: () => import('@/views/main-view/quan-ly-san-pham/thuoc-tinh/mau-sac/mau-sac.vue'),
          meta: {
            locale: 'menu.quan-ly-san-pham.thuoc-tinh.mau-sac',
            requiresAuth: true,
            roles: ['*'],
          },
        },
        {
          path: 'kich-thuoc',
          name: 'KichThuoc',
          component: () => import('@/views/main-view/quan-ly-san-pham/thuoc-tinh/kich-thuoc/kich-thuoc.vue'),
          meta: {
            locale: 'menu.quan-ly-san-pham.thuoc-tinh.kich-thuoc',
            requiresAuth: true,
            roles: ['*'],
          },
        },
        {
          path: 'de-giay',
          name: 'DeGiay',
          component: () => import('@/views/main-view/quan-ly-san-pham/thuoc-tinh/de-giay/de-giay.vue'),
          meta: {
            locale: 'menu.quan-ly-san-pham.thuoc-tinh.de-giay',
            requiresAuth: true,
            roles: ['*'],
          },
        },
        {
          path: 'chat-lieu',
          name: 'ChatLieu',
          component: () => import('@/views/main-view/quan-ly-san-pham/thuoc-tinh/chat-lieu/chat-lieu.vue'),
          meta: {
            locale: 'menu.quan-ly-san-pham.thuoc-tinh.chat-lieu',
            requiresAuth: true,
            roles: ['*'],
          },
        },
        {
          path: 'trong-luong',
          name: 'TrongLuong',
          component: () => import('@/views/main-view/quan-ly-san-pham/thuoc-tinh/trong-luong/trong-luong.vue'),
          meta: {
            locale: 'menu.quan-ly-san-pham.thuoc-tinh.trong-luong',
            requiresAuth: true,
            roles: ['*'],
          },
        },
      ],
    },
  ],
}

export default QUAN_LY_SAN_PHAM
