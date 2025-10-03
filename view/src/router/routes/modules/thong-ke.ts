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
    roles: ['*'],
  },
  children: [
    {
      path: 'chung',
      name: 'ThongKeChung',
      component: () => import('@/views/main-view/thong-ke/chung/thong-ke-chung.vue'),
      meta: {
        locale: 'menu.thong-ke.chung',
        requiresAuth: true,
        roles: ['*'],
      },
    },
  ],
}

export default THONG_KE
