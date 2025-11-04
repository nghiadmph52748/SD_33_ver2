import React from 'react'
import { Platform, StyleSheet } from 'react-native'
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs'
import { BlurView } from 'expo-blur'
import { useSafeAreaInsets } from 'react-native-safe-area-context'
import { MaterialCommunityIcons } from '@expo/vector-icons'

import { SCREENS } from '../constants/routes'
import ChatNavigator from './ChatNavigator'
import CustomersNavigator from './CustomersNavigator'
import DashboardNavigator from './DashboardNavigator'
import OrdersNavigator from './OrdersNavigator'
import ProductsNavigator from './ProductsNavigator'
import ProfileNavigator from './ProfileNavigator'
import useChatStore from '../store/useChatStore'

export type AppTabParamList = {
  [SCREENS.APP.DASHBOARD_TAB]: undefined
  [SCREENS.APP.ORDERS_TAB]: undefined
  [SCREENS.APP.PRODUCTS_TAB]: undefined
  [SCREENS.APP.CUSTOMERS_TAB]: undefined
  [SCREENS.APP.CHAT_TAB]: undefined
  [SCREENS.APP.PROFILE_TAB]: undefined
}

const Tab = createBottomTabNavigator<AppTabParamList>()

const AppTabs = () => {
  const unreadCount = useChatStore((state) => state.unreadCount)
  const insets = useSafeAreaInsets()

  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        headerShown: false,
        tabBarActiveTintColor: '#2563eb',
        tabBarInactiveTintColor: '#94a3b8',
        tabBarStyle: {
          position: 'absolute',
          backgroundColor: Platform.OS === 'ios' ? 'rgba(255, 255, 255, 0.8)' : 'rgba(255, 255, 255, 0.95)',
          borderTopWidth: 0,
          elevation: 0,
          height: 60 + insets.bottom,
          paddingBottom: insets.bottom > 0 ? insets.bottom : 8,
          paddingTop: 8,
          borderTopColor: 'transparent',
          ...Platform.select({
            ios: {
              shadowColor: '#000',
              shadowOffset: { width: 0, height: -2 },
              shadowOpacity: 0.1,
              shadowRadius: 8,
            },
            android: {
              elevation: 8,
            },
          }),
        },
        tabBarLabelStyle: {
          fontSize: 11,
          fontWeight: '600',
        },
        tabBarBackground: () => (
          <BlurView
            intensity={80}
            tint="light"
            style={StyleSheet.absoluteFill}
          />
        ),
        tabBarIcon: ({ color, size }) => {
          let iconName: string

          switch (route.name) {
            case SCREENS.APP.DASHBOARD_TAB:
              iconName = 'view-dashboard'
              break
            case SCREENS.APP.ORDERS_TAB:
              iconName = 'clipboard-text'
              break
            case SCREENS.APP.PRODUCTS_TAB:
              iconName = 'tshirt-crew'
              break
            case SCREENS.APP.CUSTOMERS_TAB:
              iconName = 'account-group'
              break
            case SCREENS.APP.CHAT_TAB:
              iconName = 'message'
              break
            case SCREENS.APP.PROFILE_TAB:
            default:
              iconName = 'account-circle'
              break
          }

          return <MaterialCommunityIcons name={iconName} size={size} color={color} />
        },
      })}
    >
      <Tab.Screen
        name={SCREENS.APP.DASHBOARD_TAB}
        component={DashboardNavigator}
        options={{ title: 'Tổng quan' }}
      />
      <Tab.Screen name={SCREENS.APP.ORDERS_TAB} component={OrdersNavigator} options={{ title: 'Đơn hàng' }} />
      <Tab.Screen name={SCREENS.APP.PRODUCTS_TAB} component={ProductsNavigator} options={{ title: 'Sản phẩm' }} />
      <Tab.Screen name={SCREENS.APP.CUSTOMERS_TAB} component={CustomersNavigator} options={{ title: 'Khách hàng' }} />
      <Tab.Screen
        name={SCREENS.APP.CHAT_TAB}
        component={ChatNavigator}
        options={{ title: 'Tin nhắn', tabBarBadge: unreadCount > 0 ? unreadCount : undefined }}
      />
      <Tab.Screen name={SCREENS.APP.PROFILE_TAB} component={ProfileNavigator} options={{ title: 'Tài khoản' }} />
    </Tab.Navigator>
  )
}

export default AppTabs
