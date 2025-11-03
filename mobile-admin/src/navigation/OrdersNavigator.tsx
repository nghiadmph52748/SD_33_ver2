import React from 'react'
import { createNativeStackNavigator } from '@react-navigation/native-stack'

import { SCREENS } from '../constants/routes'
import OrderDetailScreen from '../screens/orders/OrderDetailScreen'
import OrdersScreen from '../screens/orders/OrdersScreen'

export type OrdersStackParamList = {
  [SCREENS.STACK.ORDERS_LIST]: undefined
  [SCREENS.STACK.ORDER_DETAILS]: { orderId: number }
}

const Stack = createNativeStackNavigator<OrdersStackParamList>()

const OrdersNavigator = () => (
  <Stack.Navigator>
    <Stack.Screen
      name={SCREENS.STACK.ORDERS_LIST}
      component={OrdersScreen}
      options={{ title: 'Đơn hàng' }}
    />
    <Stack.Screen
      name={SCREENS.STACK.ORDER_DETAILS}
      component={OrderDetailScreen}
      options={{ title: 'Chi tiết đơn hàng' }}
    />
  </Stack.Navigator>
)

export default OrdersNavigator
