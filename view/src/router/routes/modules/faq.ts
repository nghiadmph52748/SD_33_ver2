import { DEFAULT_LAYOUT } from '../base'
import { AppRouteRecordRaw } from '../types'

const FAQ: AppRouteRecordRaw = {
  path: '/faq',
  name: 'faq',
  component: DEFAULT_LAYOUT,
  redirect: '/faq/main',
  meta: {
    locale: 'menu.faq',
    requiresAuth: true,
    icon: 'icon-question-circle',
    order: 9,
    roles: ['*'],
    hideChildrenInMenu: true, // Ẩn submenu trong sidebar
  },
  children: [
    {
      path: 'main',
      name: 'FaqMain',
      component: () => import('../../../views/faq/faq.vue'),
      meta: {
        locale: 'menu.faq',
        requiresAuth: true,
        roles: ['*'],
        hideInMenu: true, // Ẩn item này trong menu
      },
    },
  ],
}

export default FAQ
