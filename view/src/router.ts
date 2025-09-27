import NProgress from 'nprogress' // progress bar
import 'nprogress/nprogress.css'
import { createRouter, createWebHistory } from 'vue-router'

import createRouteGuard from './router/guard'
import { appRoutes } from './router/routes'
import { NOT_FOUND_ROUTE, REDIRECT_MAIN } from './router/routes/base'

NProgress.configure({ showSpinner: false }) // NProgress Configuration

const router = createRouter({
  history: createWebHistory('./'),
  routes: [
    {
      path: '/',
      redirect: 'login',
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/login.vue'),
      meta: {
        requiresAuth: false,
      },
    },
    ...appRoutes,
    REDIRECT_MAIN,
    NOT_FOUND_ROUTE,
  ],
  scrollBehavior() {
    return { top: 0 }
  },
})

createRouteGuard(router)

export default router
