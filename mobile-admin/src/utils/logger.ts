/* eslint-disable no-console */
export const createLogger = (namespace: string) => {
  const prefix = `[${namespace}]`
  return {
    log: (...args: unknown[]) => console.log(prefix, ...args),
    warn: (...args: unknown[]) => console.warn(prefix, ...args),
    error: (...args: unknown[]) => console.error(prefix, ...args),
  }
}
