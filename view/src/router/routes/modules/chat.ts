import { DEFAULT_LAYOUT } from '../base'
import { AppRouteRecordRaw } from '../types'

const CHAT: AppRouteRecordRaw = {
  path: '/chat',
  name: 'chat',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.chat',
    requiresAuth: true,
    icon: 'icon-message',
    order: 10,
    roles: ['*'],
  },
  children: [
    {
      path: '',
      name: 'ChatIndex',
      component: () => import('@/views/chat/index.vue'),
      meta: {
        locale: 'menu.chat',
        requiresAuth: true,
        roles: ['*'],
        hideInMenu: true,
      },
    },
  ],
}

export default CHAT
