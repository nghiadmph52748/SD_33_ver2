import React from 'react'
import { createNativeStackNavigator } from '@react-navigation/native-stack'

import { SCREENS } from '../constants/routes'
import useAuthStore from '../store/useAuthStore'
import AppTabs from './AppTabs'
import AuthNavigator from './AuthNavigator'

export type RootStackParamList = {
  [SCREENS.APP.ROOT]: undefined
  Auth: undefined
}

const Stack = createNativeStackNavigator<RootStackParamList>()

const RootNavigator = () => {
  const accessToken = useAuthStore((state) => state.accessToken)

  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      {accessToken ? (
        <Stack.Screen name={SCREENS.APP.ROOT} component={AppTabs} />
      ) : (
        <Stack.Screen name="Auth" component={AuthNavigator} />
      )}
    </Stack.Navigator>
  )
}

export default RootNavigator
