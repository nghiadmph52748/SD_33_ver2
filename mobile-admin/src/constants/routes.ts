export const SCREENS = {
  AUTH: {
    LOGIN: 'Auth.Login',
  },
  APP: {
    ROOT: 'App.Root',
    DASHBOARD_TAB: 'App.Tab.Dashboard',
    ORDERS_TAB: 'App.Tab.Orders',
    PRODUCTS_TAB: 'App.Tab.Products',
    CUSTOMERS_TAB: 'App.Tab.Customers',
    CHAT_TAB: 'App.Tab.Chat',
    PROFILE_TAB: 'App.Tab.Profile',
  },
  STACK: {
    DASHBOARD: 'Dashboard.Home',
    AI_ASSISTANT: 'Dashboard.AI',
    ORDERS_LIST: 'Orders.List',
    ORDER_DETAILS: 'Orders.Details',
    PRODUCTS_LIST: 'Products.List',
    PRODUCT_DETAILS: 'Products.Details',
    CUSTOMERS_LIST: 'Customers.List',
    CUSTOMER_DETAILS: 'Customers.Details',
    CHAT_LIST: 'Chat.List',
    CHAT_ROOM: 'Chat.Room',
    PROFILE_HOME: 'Profile.Home',
    SETTINGS: 'Profile.Settings',
  },
} as const

export type ScreenNames = typeof SCREENS
