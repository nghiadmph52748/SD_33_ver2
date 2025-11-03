import { create } from 'zustand'

import { Conversation } from '../api/chat'

interface ChatState {
  conversations: Conversation[]
  unreadCount: number
  setConversations: (data: Conversation[]) => void
  pushOrUpdateConversation: (conversation: Conversation) => void
  setUnreadCount: (count: number) => void
  incrementUnread: (conversationId: number) => void
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
  clear: () => set({ conversations: [], unreadCount: 0 }),
}))

export default useChatStore
