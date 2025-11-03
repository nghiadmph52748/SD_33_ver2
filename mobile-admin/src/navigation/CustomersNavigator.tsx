import React from 'react'
import { createNativeStackNavigator } from '@react-navigation/native-stack'

import { SCREENS } from '../constants/routes'
import CustomerDetailScreen from '../screens/customers/CustomerDetailScreen'
import CustomersScreen from '../screens/customers/CustomersScreen'

export type CustomersStackParamList = {
  [SCREENS.STACK.CUSTOMERS_LIST]: undefined
  [SCREENS.STACK.CUSTOMER_DETAILS]: { customerId: number }
}

const Stack = createNativeStackNavigator<CustomersStackParamList>()

const CustomersNavigator = () => (
  <Stack.Navigator>
    <Stack.Screen
      name={SCREENS.STACK.CUSTOMERS_LIST}
      component={CustomersScreen}
      options={{ title: 'Khách hàng' }}
    />
    <Stack.Screen
      name={SCREENS.STACK.CUSTOMER_DETAILS}
      component={CustomerDetailScreen}
      options={{ title: 'Chi tiết khách hàng' }}
    />
  </Stack.Navigator>
)

export default CustomersNavigator
