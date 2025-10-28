import { DEFAULT_LAYOUT } from '../base'
import { AppRouteRecordRaw } from '../types'

const AI_CHATBOT: AppRouteRecordRaw = {
  path: '/ai-chatbot',
  name: 'ai-chatbot',
  component: DEFAULT_LAYOUT,
  redirect: { name: 'AIChatbotIndex' },
  meta: {
    locale: 'menu.ai-assistant',
    requiresAuth: false,
    icon: 'IconRobot',
    order: 7,
    roles: ['*'],
    hideChildrenInMenu: true
  },
  children: [
    {
      path: 'index',
      name: 'AIChatbotIndex',
      component: () => import('@/views/ai-chatbot/ai-chatbot-view.vue'),
      meta: {
        locale: 'menu.ai-assistant',
        requiresAuth: false,
        roles: ['*'],
        hideInMenu: true,
        activeMenu: 'ai-chatbot'
      }
    }
  ]
}

export default AI_CHATBOT
