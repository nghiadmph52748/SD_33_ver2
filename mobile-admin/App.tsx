import 'react-native-gesture-handler'
import React, { useCallback, useEffect, useRef } from 'react'
import { GestureHandlerRootView } from 'react-native-gesture-handler'
import { NavigationContainer, CommonActions } from '@react-navigation/native'
import { enableScreens } from 'react-native-screens'
import { StatusBar, AppState } from 'react-native'
import { SafeAreaProvider } from 'react-native-safe-area-context'
import { Provider as PaperProvider } from 'react-native-paper'
import * as ExpoLinking from 'expo-linking'

import RootNavigator, { navigationRef } from './src/navigation'
import { paperTheme } from './src/theme'

enableScreens()

const App: React.FC = () => {
  const pendingSessionId = useRef<string | null>(null)

  const navigateToQrScreen = useCallback((sessionId: string, screen: 'payment' | 'confirmation' = 'payment') => {
    if (!sessionId) return
    if (navigationRef.isReady()) {
      const targetScreen = screen === 'confirmation' ? 'QR.Success' : 'QR.Payment'
      navigationRef.navigate('QR.Stack', {
        screen: targetScreen,
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
      
      // Handle QR reset - reset navigation to main payment screen
      if (normalizedPath === 'qr/reset') {
        if (navigationRef.isReady()) {
          try {
            navigationRef.dispatch(
              CommonActions.reset({
                index: 0,
                routes: [
                  {
                    name: 'QR.Stack',
                    params: {
                      screen: 'QR.Payment',
                      params: {},
                    },
                  },
                ],
              })
            )
          } catch (error) {
            console.error('Error resetting navigation:', error)
            setTimeout(() => {
              if (navigationRef.isReady()) {
                try {
                  navigationRef.dispatch(
                    CommonActions.reset({
                      index: 0,
                      routes: [
                        {
                          name: 'QR.Stack',
                          params: {
                            screen: 'QR.Payment',
                            params: {},
                          },
                        },
                      ],
                    })
                  )
                } catch (retryError) {
                  console.error('Error resetting navigation (retry):', retryError)
                }
              }
            }, 500)
          }
        } else {
          setTimeout(() => {
            if (navigationRef.isReady()) {
              try {
                navigationRef.dispatch(
                  CommonActions.reset({
                    index: 0,
                    routes: [
                      {
                        name: 'QR.Stack',
                        params: {
                          screen: 'QR.Payment',
                          params: {},
                        },
                      },
                    ],
                  })
                )
              } catch (error) {
                console.error('Error resetting navigation (delayed):', error)
              }
            }
          }, 500)
        }
        return
      }

      // Handle QR session deep links
      if (normalizedPath === 'qr' || normalizedPath === 'payment/qr' || normalizedPath === 'qr/confirmation') {
        const query = parsed.queryParams ?? {}
        const sessionParam = (query.session as string) || (query.sessionId as string)
        const screen = normalizedPath === 'qr/confirmation' || query.screen === 'confirmation' ? 'confirmation' : 'payment'
        if (sessionParam && typeof sessionParam === 'string') {
          navigateToQrScreen(sessionParam, screen)
        }
        return
      }

      // Handle VNPAY payment result deep links
      if (normalizedPath === 'payment/vnpay/result' || normalizedPath.includes('vnpay')) {
        if (navigationRef.isReady()) {
          navigationRef.navigate('QR.Stack', {
            screen: 'QR.Success',
            params: { 
              sessionId: (parsed.queryParams?.txnRef as string) || 'unknown',
              paymentResult: parsed.queryParams
            },
          })
        }
        return
      }
    },
    [navigateToQrScreen]
  )

  useEffect(() => {
    const checkInitialUrl = async () => {
      const initialUrl = await ExpoLinking.getInitialURL()
      handleDeepLink(initialUrl)
    }

    checkInitialUrl()

    const subscription = ExpoLinking.addEventListener('url', ({ url }) => {
      console.log('Deep link received:', url)
      handleDeepLink(url)
    })
    
    // Also listen for app state changes to handle deep links when app comes to foreground
    const appStateSubscription = AppState.addEventListener('change', async (nextAppState) => {
      if (nextAppState === 'active') {
        const url = await ExpoLinking.getInitialURL()
        if (url) {
          console.log('App became active, checking deep link:', url)
          handleDeepLink(url)
        }
      }
    })
    
    return () => {
      subscription.remove()
      appStateSubscription.remove()
    }
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
