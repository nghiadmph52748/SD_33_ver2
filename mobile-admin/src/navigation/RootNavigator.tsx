import React from 'react'
import { createNativeStackNavigator } from '@react-navigation/native-stack'
import { NavigatorScreenParams } from '@react-navigation/native'

import QRPaymentNavigator, { QRPaymentStackParamList } from './QRPaymentNavigator'

export type RootStackParamList = {
  'QR.Stack': NavigatorScreenParams<QRPaymentStackParamList>
}

const Stack = createNativeStackNavigator<RootStackParamList>()

const RootNavigator = () => {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }} initialRouteName="QR.Stack">
      <Stack.Screen name="QR.Stack" component={QRPaymentNavigator} options={{ presentation: 'modal' }} />
    </Stack.Navigator>
  )
}

export default RootNavigator
