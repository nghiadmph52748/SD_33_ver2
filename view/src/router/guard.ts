import { setRouteEmitter } from '@/utils/route-listener'
import type { Router } from 'vue-router'
import setupPermissionGuard from './guard/permission'
import setupUserLoginInfoGuard from './guard/userLoginInfo'

function setupPageGuard(router: Router) {
  router.beforeEach(async (to) => {
    // emit route change
    setRouteEmitter(to)
  })
}

export default function createRouteGuard(router: Router) {
  setupPageGuard(router)
  setupUserLoginInfoGuard(router)
  setupPermissionGuard(router)
}
