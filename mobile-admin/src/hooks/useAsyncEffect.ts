import { useEffect } from 'react'

export const useAsyncEffect = (effect: () => Promise<void>, deps: React.DependencyList) => {
  useEffect(() => {
    let isMounted = true

    const run = async () => {
      try {
        await effect()
      } catch (error) {
        if (__DEV__) {
          // eslint-disable-next-line no-console
          console.error('[useAsyncEffect]', error)
        }
      }
    }

    if (isMounted) {
      run()
    }

    return () => {
      isMounted = false
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps)
}

export default useAsyncEffect
