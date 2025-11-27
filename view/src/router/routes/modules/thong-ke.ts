import { DEFAULT_LAYOUT } from '../base'
import { AppRouteRecordRaw } from '../types'

const THONG_KE: AppRouteRecordRaw = {
  path: '/thong-ke',
  name: 'thong-ke',
  component: DEFAULT_LAYOUT,
  redirect: { name: 'ThongKeChung' },
  meta: {
    locale: 'menu.thong-ke',
    requiresAuth: true,
    icon: 'IconList',
    order: 0,
    roles: ['admin'], // Chỉ admin mới thấy menu này
    hideChildrenInMenu: true, // Ẩn submenu trong sidebar
  },
  children: [
    {
      path: 'chung',
      name: 'ThongKeChung',
      component: () => import('@/views/main-view/thong-ke/chung/thong-ke-chung.vue'),
      meta: {
        locale: 'menu.thong-ke',
        requiresAuth: true,
        roles: ['admin'],
        hideInMenu: true, // Ẩn item này trong menu
        activeMenu: 'thong-ke', // Highlight parent in menu
      },
    },
  ],
}

export default THONG_KE
