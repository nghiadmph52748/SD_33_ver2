declare module 'marked' {
  // Minimal typings to satisfy TS; runtime provided by the library
  export function parse(src: string): string
}
