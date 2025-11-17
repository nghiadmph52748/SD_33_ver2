export const notifyError = (message: string) => {
  if (__DEV__) {
    console.warn('[notifyError]', message)
  }
}

export const notifySuccess = (message: string) => {
  if (__DEV__) {
    console.log('[notifySuccess]', message)
  }
}
