import React from 'react'
import { createNativeStackNavigator } from '@react-navigation/native-stack'

import { SCREENS } from '../constants/routes'
import LoginScreen from '../screens/auth/LoginScreen'

export type AuthStackParamList = {
  [SCREENS.AUTH.LOGIN]: undefined
}

const Stack = createNativeStackNavigator<AuthStackParamList>()

const AuthNavigator = () => (
  <Stack.Navigator screenOptions={{ headerShown: false }}>
    <Stack.Screen name={SCREENS.AUTH.LOGIN} component={LoginScreen} />
  </Stack.Navigator>
)

export default AuthNavigator
