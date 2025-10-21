declare module 'qr-scanner' {
  export interface ScanResult {
    data: string
    cornerPoints?: any[]
    duration?: number
  }

  export interface Options {
    highlightScanRegion?: boolean
    highlightCodeOutline?: boolean
    preferredCamera?: string
    maxScansPerSecond?: number
    returnDetailedScanResult?: boolean
  }

  export interface Camera {
    id: string
    label: string
  }

  export default class QrScanner {
    constructor(video: HTMLVideoElement, onDecode: (result: ScanResult) => void, options?: Options)

    static hasCamera(): Promise<boolean>

    static scanImage(
      imageOrFileOrBlob: HTMLImageElement | HTMLVideoElement | HTMLCanvasElement | File | Blob,
      options?: Options,
      canvas?: HTMLCanvasElement
    ): Promise<string | null>

    static listCameras(requestLabels?: boolean): Promise<Camera[]>

    static readonly NO_QR_CODE_FOUND: string

    start(): Promise<void>

    stop(): void

    destroy(): void

    setCamera(cameraId: string): Promise<void>
  }
}
