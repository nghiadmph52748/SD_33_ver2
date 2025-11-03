import React from 'react'
import { createNativeStackNavigator } from '@react-navigation/native-stack'

import { SCREENS } from '../constants/routes'
import DashboardScreen from '../screens/dashboard/DashboardScreen'
import AIAssistantScreen from '../screens/ai/AIAssistantScreen'

export type DashboardStackParamList = {
  [SCREENS.STACK.DASHBOARD]: undefined
  [SCREENS.STACK.AI_ASSISTANT]: undefined
}

const Stack = createNativeStackNavigator<DashboardStackParamList>()

const DashboardNavigator = () => (
  <Stack.Navigator>
    <Stack.Screen name={SCREENS.STACK.DASHBOARD} component={DashboardScreen} options={{ title: 'Tổng quan' }} />
    <Stack.Screen
      name={SCREENS.STACK.AI_ASSISTANT}
      component={AIAssistantScreen}
      options={{ title: 'AI Assistant' }}
    />
  </Stack.Navigator>
)

export default DashboardNavigator
