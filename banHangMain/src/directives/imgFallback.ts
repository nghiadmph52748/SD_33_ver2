import type { Directive } from 'vue'
import { handleImageError } from '@/utils/imageFallback'

function isLoaded(img: HTMLImageElement): boolean {
  return img.complete && img.naturalWidth > 0
}

const addSkeleton = (el: HTMLElement) => {
  el.classList.add('skeleton', 'skeleton--image')
}

const removeSkeleton = (el: HTMLElement) => {
  el.classList.remove('skeleton', 'skeleton--image')
}

const directive: Directive<HTMLImageElement, void> = {
  mounted(el) {
    addSkeleton(el)

    const onLoad = () => {
      removeSkeleton(el)
      el.classList.add('loaded')
      el.removeEventListener('load', onLoad)
      el.removeEventListener('error', onError)
    }
    const onError = () => {
      // Use centralized fallback that keeps shimmer via image-placeholder
      handleImageError({ target: el } as unknown as Event)
      // Keep skeleton classes for consistent look on error
      // Do not remove listeners to retry if src changes
    }

    el.addEventListener('load', onLoad)
    el.addEventListener('error', onError)

    // If browser already loaded the image before mount
    if (isLoaded(el)) {
      onLoad()
    }
  },
  beforeUnmount(el) {
    el.onload = null
    el.onerror = null
  }
}

export default directive


