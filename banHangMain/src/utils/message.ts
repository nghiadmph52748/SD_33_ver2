type MessageApi = {
  error?: (config: { content: string; duration?: number }) => void
  success?: (config: { content: string; duration?: number }) => void
  warning?: (config: { content: string; duration?: number }) => void
}

let messageApi: MessageApi | null = null

export function registerMessage(api: MessageApi) {
  messageApi = api
}

export function showMessageError(content: string, duration = 5000) {
  messageApi?.error?.({ content, duration })
}

export function showMessageSuccess(content: string, duration = 3000) {
  messageApi?.success?.({ content, duration })
}

export function showMessageWarning(content: string, duration = 3000) {
  messageApi?.warning?.({ content, duration })
}
