import React from 'react'
import { createNativeStackNavigator } from '@react-navigation/native-stack'

import { SCREENS } from '../constants/routes'
import ChatListScreen from '../screens/chat/ChatListScreen'
import ChatRoomScreen from '../screens/chat/ChatRoomScreen'

export type ChatStackParamList = {
  [SCREENS.STACK.CHAT_LIST]: undefined
  [SCREENS.STACK.CHAT_ROOM]: { partnerId: number; partnerName: string }
}

const Stack = createNativeStackNavigator<ChatStackParamList>()

const ChatNavigator = () => (
  <Stack.Navigator>
    <Stack.Screen
      name={SCREENS.STACK.CHAT_LIST}
      component={ChatListScreen}
      options={{ title: 'Tin nhắn' }}
    />
    <Stack.Screen
      name={SCREENS.STACK.CHAT_ROOM}
      component={ChatRoomScreen}
      options={({ route }) => ({ title: route.params.partnerName })}
    />
  </Stack.Navigator>
)

export default ChatNavigator
