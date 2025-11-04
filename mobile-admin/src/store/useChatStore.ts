import { create } from 'zustand'

import { Conversation } from '../api/chat'

interface ChatState {
  conversations: Conversation[]
  unreadCount: number
  setConversations: (data: Conversation[]) => void
  pushOrUpdateConversation: (conversation: Conversation) => void
  setUnreadCount: (count: number) => void
  incrementUnread: (conversationId: number) => void
  clearUnreadForConversation: (partnerId: number, currentUserId: number) => void
  clear: () => void
}

const useChatStore = create<ChatState>((set, get) => ({
  conversations: [],
  unreadCount: 0,
  setConversations: (data) => set({ conversations: data }),
  pushOrUpdateConversation: (conversation) => {
    const { conversations } = get()
    const next = conversations.filter((item) => item.id !== conversation.id)
    set({ conversations: [conversation, ...next] })
  },
  setUnreadCount: (count) => set({ unreadCount: count }),
  incrementUnread: (conversationId) => {
    const { conversations, unreadCount } = get()
    const next = conversations.map((conversation) =>
      conversation.id === conversationId
        ? {
            ...conversation,
            unreadCountNv1: (conversation.unreadCountNv1 ?? 0) + 1,
          }
        : conversation
    )
    set({ conversations: next, unreadCount: unreadCount + 1 })
  },
  clearUnreadForConversation: (partnerId, currentUserId) => {
    const { conversations, unreadCount } = get()
    let totalUnreadToSubtract = 0
    
    const next = conversations.map((conversation) => {
      const isFirstUser = conversation.nhanVien1Id === currentUserId
      const isSecondUser = conversation.nhanVien2Id === currentUserId
      const matchesPartner =
        (isFirstUser && conversation.nhanVien2Id === partnerId) ||
        (isSecondUser && conversation.nhanVien1Id === partnerId)

      if (matchesPartner) {
        const oldUnread = isFirstUser ? conversation.unreadCountNv1 : conversation.unreadCountNv2
        totalUnreadToSubtract += oldUnread ?? 0
        
        return {
          ...conversation,
          unreadCountNv1: isFirstUser ? 0 : conversation.unreadCountNv1,
          unreadCountNv2: isSecondUser ? 0 : conversation.unreadCountNv2,
        }
      }
      return conversation
    })
    
    set({
      conversations: next,
      unreadCount: Math.max(0, unreadCount - totalUnreadToSubtract),
    })
  },
  clear: () => set({ conversations: [], unreadCount: 0 }),
}))

export default useChatStore
