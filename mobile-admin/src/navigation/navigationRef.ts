import { createNavigationContainerRef } from '@react-navigation/native'

import type { RootStackParamList } from './RootNavigator'

export const navigationRef = createNavigationContainerRef<RootStackParamList>()

export const navigateToQrPayment = (sessionId: string) => {
  if (!navigationRef.isReady()) return

  navigationRef.navigate('QR.Stack', {
    screen: 'QR.Payment',
    params: { sessionId },
  })
}

