// Loading skeleton placeholders for images (shimmer effect)
export const FALLBACK_IMAGE = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="400" height="400"%3E%3Crect width="400" height="400" fill="%23f5f5f5"/%3E%3C/svg%3E'

export const FALLBACK_SWATCH = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="44" height="44"%3E%3Crect width="44" height="44" fill="%23e5e7eb"/%3E%3C/svg%3E'

export function handleImageError(event: Event, fallback: string = FALLBACK_IMAGE) {
  const target = event.target as HTMLImageElement
  if (target && target.src !== fallback) {
    target.src = fallback
    // Add loading class for shimmer animation
    target.classList.add('image-placeholder')
    const parent = target.parentElement
    if (parent) {
      parent.classList.add('image-loading')
    }
  }
}

