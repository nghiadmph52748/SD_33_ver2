import type { RouteRecordNormalized } from 'vue-router'
import { DEFAULT_LAYOUT } from './routes/base'

const modules = import.meta.glob('./routes/modules/*.ts', { eager: true })
const externalModules = import.meta.glob('./routes/externalModules/*.ts', {
  eager: true,
})

function formatModules(_modules: any, result: RouteRecordNormalized[]) {
  Object.keys(_modules).forEach((key) => {
    const defaultModule = _modules[key].default
    if (!defaultModule || (typeof defaultModule === 'object' && !defaultModule.path && !defaultModule.name)) {
      return
    }
    const moduleList = Array.isArray(defaultModule) ? [...defaultModule] : [defaultModule]

    // Tự động thêm DEFAULT_LAYOUT cho các route quản lý nếu chưa có component
    moduleList.forEach((route) => {
      // Nếu route đã có component trực tiếp (không có children), không cần xử lý
      if (route.component && !route.children) {
        return
      }

      // Nếu route có children, tự động thêm DEFAULT_LAYOUT
      if (!route.component && route.children && route.children.length > 0) {
        route.component = DEFAULT_LAYOUT
      }
    })

    result.push(...moduleList)
  })
  return result
}

export const appRoutes: RouteRecordNormalized[] = formatModules(modules, [])

export const appExternalRoutes: RouteRecordNormalized[] = formatModules(externalModules, [])
