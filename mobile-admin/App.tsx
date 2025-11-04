import 'react-native-gesture-handler'
import React, { useEffect } from 'react'
import { GestureHandlerRootView } from 'react-native-gesture-handler'
import { NavigationContainer } from '@react-navigation/native'
import { enableScreens } from 'react-native-screens'
import { StatusBar } from 'react-native'
import { SafeAreaProvider } from 'react-native-safe-area-context'
import { Provider as PaperProvider } from 'react-native-paper'

import RootNavigator from './src/navigation'
import useAuthStore from './src/store/useAuthStore'
import SplashScreen from './src/screens/common/SplashScreen'
import { paperTheme } from './src/theme'

enableScreens()

const App: React.FC = () => {
  const restoreSession = useAuthStore((state) => state.restoreSession)
  const isRestoring = useAuthStore((state) => state.isRestoring)

  useEffect(() => {
    restoreSession()
  }, [restoreSession])

  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <PaperProvider theme={paperTheme}>
          <NavigationContainer>
            {isRestoring ? <SplashScreen /> : <RootNavigator />}
          </NavigationContainer>
          <StatusBar barStyle="dark-content" backgroundColor="#ffffff" translucent={false} animated />
        </PaperProvider>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  )
}

export default App
