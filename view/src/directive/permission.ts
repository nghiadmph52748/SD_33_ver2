// directives/permission.ts
import { DirectiveBinding } from 'vue'
import { useUserStore } from '@/store'

function checkPermission(el: HTMLElement, binding: DirectiveBinding) {
  const { value } = binding
  const userStore = useUserStore()
  const userRoles = userStore.roles || []

  if (!Array.isArray(value)) {
    throw new Error(`v-permission expects an array like v-permission="['admin','user']"`)
  }

  // Nếu route/meta là '*' → luôn hiển thị
  const hasPermission = value.includes('*') || value.some((role) => userRoles.includes(role))

  if (!hasPermission && el.parentNode) {
    el.parentNode.removeChild(el)
  }
}

export default {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    checkPermission(el, binding)
  },
  updated(el: HTMLElement, binding: DirectiveBinding) {
    checkPermission(el, binding)
  },
}
