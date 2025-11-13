import { ref, type Ref } from 'vue'

const DEFAULT_PLACEHOLDER = '/products/placeholder.jpg'
const FALLBACK_IMAGE = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="400" height="400"%3E%3Crect width="400" height="400" fill="%23f5f5f5"/%3E%3Ctext x="50%25" y="50%25" text-anchor="middle" dy=".3em" fill="%23999" font-family="sans-serif" font-size="14"%3EImage not available%3C/text%3E%3C/svg%3E'

export function useImageLoader() {
  const imageError = ref<Record<string, boolean>>({})
  const imageLoading = ref<Record<string, boolean>>({})

  function handleImageError(productId: string, event: Event) {
    const target = event.target as HTMLImageElement
    if (!target) return

    imageError.value[productId] = true
    imageLoading.value[productId] = false

    // Try fallback image
    if (target.src !== FALLBACK_IMAGE && target.src !== DEFAULT_PLACEHOLDER) {
      target.src = DEFAULT_PLACEHOLDER
      // If placeholder also fails, use SVG fallback
      target.onerror = () => {
        target.src = FALLBACK_IMAGE
      }
    }
  }

  function handleImageLoad(productId: string) {
    imageLoading.value[productId] = false
    imageError.value[productId] = false
  }

  function handleImageStart(productId: string) {
    imageLoading.value[productId] = true
  }

  function isImageError(productId: string): boolean {
    return imageError.value[productId] === true
  }

  function isLoading(productId: string): boolean {
    return imageLoading.value[productId] === true
  }

  return {
    handleImageError,
    handleImageLoad,
    handleImageStart,
    isImageError,
    isLoading
  }
}

