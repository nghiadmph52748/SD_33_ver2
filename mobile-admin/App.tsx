import 'react-native-gesture-handler'
import React, { useCallback, useEffect, useRef } from 'react'
import { GestureHandlerRootView } from 'react-native-gesture-handler'
import { NavigationContainer } from '@react-navigation/native'
import { enableScreens } from 'react-native-screens'
import { StatusBar } from 'react-native'
import { SafeAreaProvider } from 'react-native-safe-area-context'
import { Provider as PaperProvider } from 'react-native-paper'
import * as ExpoLinking from 'expo-linking'

import RootNavigator, { navigationRef } from './src/navigation'
import { paperTheme } from './src/theme'

enableScreens()

const App: React.FC = () => {
  const pendingSessionId = useRef<string | null>(null)

  const navigateToQrScreen = useCallback((sessionId: string) => {
    if (!sessionId) return
    if (navigationRef.isReady()) {
      navigationRef.navigate('QR.Stack', {
        screen: 'QR.Payment',
        params: { sessionId },
      })
      pendingSessionId.current = null
    } else {
      pendingSessionId.current = sessionId
    }
  }, [])

  const handleDeepLink = useCallback(
    (url: string | null) => {
      if (!url) return
      const parsed = ExpoLinking.parse(url)
      const normalizedPath = parsed?.path?.replace(/^\//, '') ?? ''
      if (normalizedPath !== 'qr') return

      const query = parsed.queryParams ?? {}
      const sessionParam = (query.session as string) || (query.sessionId as string)
      if (!sessionParam || typeof sessionParam !== 'string') return

      navigateToQrScreen(sessionParam)
    },
    [navigateToQrScreen]
  )

  useEffect(() => {
    const checkInitialUrl = async () => {
      const initialUrl = await ExpoLinking.getInitialURL()
      handleDeepLink(initialUrl)
    }

    checkInitialUrl()

    const subscription = ExpoLinking.addEventListener('url', ({ url }) => handleDeepLink(url))
    return () => subscription.remove()
  }, [handleDeepLink])

  const handleNavigationReady = useCallback(() => {
    if (pendingSessionId.current) {
      navigateToQrScreen(pendingSessionId.current)
    }
  }, [navigateToQrScreen])

  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <PaperProvider theme={paperTheme}>
          <NavigationContainer ref={navigationRef} onReady={handleNavigationReady}>
            <RootNavigator />
          </NavigationContainer>
          <StatusBar barStyle="dark-content" backgroundColor="#ffffff" translucent={false} animated />
        </PaperProvider>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  )
}

export default App
