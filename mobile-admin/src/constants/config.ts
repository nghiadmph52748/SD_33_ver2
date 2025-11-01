// API Configuration
export const API_BASE_URL = process.env.API_BASE_URL || 'http://192.168.1.100:8080';
export const AI_SERVICE_URL = process.env.AI_SERVICE_URL || 'http://192.168.1.100:8001';
export const WS_URL = process.env.WS_URL || 'ws://192.168.1.100:8080/ws-chat';

// App Configuration
export const APP_NAME = 'GearUp Admin';
export const APP_VERSION = '1.0.0';

// Storage Keys
export const STORAGE_KEYS = {
  TOKEN: '@gearup_token',
  USER: '@gearup_user',
  REFRESH_TOKEN: '@gearup_refresh_token',
} as const;

// API Endpoints
export const ENDPOINTS = {
  // Auth
  LOGIN: '/api/auth/login',
  LOGOUT: '/api/auth/logout',
  REFRESH: '/api/auth/refresh',
  
  // Dashboard/Statistics
  STATS: '/api/thongke',
  
  // Products
  PRODUCTS: '/api/sanpham',
  PRODUCT_DETAIL: (id: number) => `/api/sanpham/${id}`,
  
  // Orders
  ORDERS: '/api/donhang',
  ORDER_DETAIL: (id: number) => `/api/donhang/${id}`,
  UPDATE_ORDER_STATUS: (id: number) => `/api/donhang/${id}/status`,
  
  // Chat
  CHAT_CONVERSATIONS: '/api/chat/conversations',
  CHAT_MESSAGES: (conversationId: string) => `/api/chat/${conversationId}/messages`,
  
  // AI
  AI_CHAT: '/api/chatbot/chat',
  AI_HEALTH: '/api/chatbot/health',
  
  // Users
  USERS: '/api/users',
  STAFF: '/api/staff',
  
  // Vouchers
  VOUCHERS: '/api/vouchers',
} as const;
