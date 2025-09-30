declare module 'color-namer' {
  interface ColorResult {
    name: string
    hex: string
    distance: number
  }

  interface ColorPalette {
    [index: number]: ColorResult
  }

  interface ColorNames {
    html?: ColorPalette
    ntc?: ColorPalette
    pantone?: ColorPalette
    roygbiv?: ColorPalette
    basic?: ColorPalette
  }

  function colorNamer(color: string): ColorNames
  export = colorNamer
}
