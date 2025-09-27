import { computed } from 'vue'
import { useRoute } from 'vue-router'

export default function useBreadcrumb() {
  const route = useRoute()

  const breadcrumbItems = computed(() => {
    const matched = route.matched.filter((item) => item.meta && item.meta.locale)

    return matched.map((item, index) => {
      // For navigation, use the route's path if it's not the current page and not hidden from menu
      const isCurrentPage = index === matched.length - 1
      let path = ''

      if (!isCurrentPage && !item.meta?.hideInMenu) {
        // Get the full path up to this route
        path = matched.slice(0, index + 1).reduce((acc, routeItem) => {
          const segment = routeItem.path
          if (segment.startsWith('/')) {
            return segment
          }
          return acc + (acc.endsWith('/') ? '' : '/') + segment
        }, '/')
      }

      return {
        locale: item.meta?.locale as string,
        path,
      }
    })
  })

  return {
    breadcrumbItems,
  }
}
