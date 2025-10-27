import { DEFAULT_LAYOUT } from '../base'
import { AppRouteRecordRaw } from '../types'

const CHAT: AppRouteRecordRaw = {
  path: '/chat',
  name: 'chat',
  component: DEFAULT_LAYOUT,
  redirect: { name: 'ChatIndex' },
  meta: {
    locale: 'menu.chat',
    requiresAuth: true,
    icon: 'IconMessage',
    order: 7,
    roles: ['*'],
    hideChildrenInMenu: true,
  },
  children: [
    {
      path: 'index',
      name: 'ChatIndex',
      component: () => import('@/views/chat/index.vue'),
      meta: {
        locale: 'menu.chat',
        requiresAuth: true,
        roles: ['*'],
        hideInMenu: true,
        activeMenu: 'chat',
      },
    },
  ],
}

export default CHAT
