// User Types
export interface User {
  id: number;
  email: string;
  name: string;
  role: 'ADMIN' | 'STAFF' | 'CUSTOMER';
  avatar?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  user: User;
}

// Dashboard/Statistics Types
export interface DashboardStats {
  revenue: {
    week: number;
    month: number;
    year: number;
  };
  productsSold: {
    week: number;
    month: number;
    year: number;
  };
  orders: {
    week: number;
    month: number;
    year: number;
  };
}

// Product Types
export interface Product {
  id: number;
  name: string;
  price: number;
  stock: number;
  image?: string;
  category: string;
  status: 'ACTIVE' | 'INACTIVE';
  description?: string;
}

// Order Types
export interface Order {
  id: number;
  orderCode: string;
  customerName: string;
  totalAmount: number;
  status: 'PENDING' | 'CONFIRMED' | 'SHIPPING' | 'DELIVERED' | 'CANCELLED';
  createdAt: string;
  items: OrderItem[];
}

export interface OrderItem {
  id: number;
  productName: string;
  quantity: number;
  price: number;
}

// Chat Types
export interface ChatConversation {
  id: string;
  participantName: string;
  lastMessage: string;
  lastMessageTime: string;
  unreadCount: number;
}

export interface ChatMessage {
  id: string;
  senderId: number;
  senderName: string;
  content: string;
  timestamp: string;
  read: boolean;
}

// AI Chatbot Types
export interface AIChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: string;
}

// Navigation Types
export type RootStackParamList = {
  Auth: undefined;
  Main: undefined;
  Login: undefined;
  Dashboard: undefined;
  Products: undefined;
  ProductDetail: { productId: number };
  Orders: undefined;
  OrderDetail: { orderId: number };
  Chat: undefined;
  ChatDetail: { conversationId: string };
  AIChat: undefined;
  Profile: undefined;
  Users: undefined;
  Vouchers: undefined;
};
