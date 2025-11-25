import React from 'react'
import { createNativeStackNavigator } from '@react-navigation/native-stack'

import QRPaymentScreen from '../screens/pos/QRPaymentScreen'
import PaymentSuccessScreen from '../screens/pos/PaymentSuccessScreen'

export type QRPaymentStackParamList = {
  'QR.Payment': { sessionId?: string }
  'QR.Success': { sessionId: string }
}

const Stack = createNativeStackNavigator<QRPaymentStackParamList>()

const QRPaymentNavigator = () => {
  return (
    <Stack.Navigator
      screenOptions={{
        headerShown: true,
        headerTitle: 'Quầy Thanh Toán',
        headerBackTitle: 'Quay lại',
      }}
    >
      <Stack.Screen
        name="QR.Payment"
        component={QRPaymentScreen}
        options={{ title: 'Quét mã thanh toán' }}
      />
      <Stack.Screen
        name="QR.Success"
        component={PaymentSuccessScreen}
        options={{ title: 'Thanh toán thành công', headerLeft: () => null }}
      />
    </Stack.Navigator>
  )
}

export default QRPaymentNavigator

