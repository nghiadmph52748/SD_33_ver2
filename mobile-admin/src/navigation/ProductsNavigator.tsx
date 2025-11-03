import React from 'react'
import { createNativeStackNavigator } from '@react-navigation/native-stack'

import { SCREENS } from '../constants/routes'
import ProductDetailScreen from '../screens/products/ProductDetailScreen'
import ProductsScreen from '../screens/products/ProductsScreen'

export type ProductsStackParamList = {
  [SCREENS.STACK.PRODUCTS_LIST]: undefined
  [SCREENS.STACK.PRODUCT_DETAILS]: { productId: number }
}

const Stack = createNativeStackNavigator<ProductsStackParamList>()

const ProductsNavigator = () => (
  <Stack.Navigator>
    <Stack.Screen
      name={SCREENS.STACK.PRODUCTS_LIST}
      component={ProductsScreen}
      options={{ title: 'Sản phẩm' }}
    />
    <Stack.Screen
      name={SCREENS.STACK.PRODUCT_DETAILS}
      component={ProductDetailScreen}
      options={{ title: 'Chi tiết sản phẩm' }}
    />
  </Stack.Navigator>
)

export default ProductsNavigator
