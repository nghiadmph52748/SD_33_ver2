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
  },
  children: [
    {
      path: 'main',
      name: 'FaqMain',
      component: () => import('../../../views/faq/faq.vue'),
      meta: {
        locale: 'menu.faq.main',
        requiresAuth: true,
        roles: ['*'],
      },
    },
  ],
}

export default FAQ
