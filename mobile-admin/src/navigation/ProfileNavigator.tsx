import React from 'react'
import { createNativeStackNavigator } from '@react-navigation/native-stack'

import { SCREENS } from '../constants/routes'
import ProfileScreen from '../screens/profile/ProfileScreen'
import SettingsScreen from '../screens/settings/SettingsScreen'

export type ProfileStackParamList = {
  [SCREENS.STACK.PROFILE_HOME]: undefined
  [SCREENS.STACK.SETTINGS]: undefined
}

const Stack = createNativeStackNavigator<ProfileStackParamList>()

const ProfileNavigator = () => (
  <Stack.Navigator>
    <Stack.Screen
      name={SCREENS.STACK.PROFILE_HOME}
      component={ProfileScreen}
      options={{ title: 'Tài khoản' }}
    />
    <Stack.Screen name={SCREENS.STACK.SETTINGS} component={SettingsScreen} options={{ title: 'Cài đặt' }} />
  </Stack.Navigator>
)

export default ProfileNavigator
