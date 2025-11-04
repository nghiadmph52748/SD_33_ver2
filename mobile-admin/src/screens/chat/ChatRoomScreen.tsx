import { RouteProp, useFocusEffect, useRoute } from '@react-navigation/native'
import React, { useCallback, useEffect, useState } from 'react'
import { FlatList, KeyboardAvoidingView, Platform, StyleSheet, View } from 'react-native'
import { Button, Text, TextInput } from 'react-native-paper'
import { useSafeAreaInsets } from 'react-native-safe-area-context'

import { ChatMessage, fetchMessages, markMessagesAsRead, sendMessage } from '../../api/chat'
import { LoadingOverlay } from '../../components'
import { SCREENS } from '../../constants/routes'
import { ChatStackParamList } from '../../navigation/ChatNavigator'
import useAuthStore from '../../store/useAuthStore'
import useChatStore from '../../store/useChatStore'

interface MessageBubbleProps {
  message: ChatMessage
  isOwnMessage: boolean
}

const MessageBubble: React.FC<MessageBubbleProps> = ({ message, isOwnMessage }) => (
  <View style={[styles.messageContainer, isOwnMessage ? styles.messageOutgoing : styles.messageIncoming]}>
    <Text style={[styles.messageContent, isOwnMessage ? styles.messageOutgoingText : styles.messageIncomingText]}>
      {message.content}
    </Text>
    <Text style={styles.messageTime}>{new Date(message.sentAt).toLocaleTimeString('vi-VN')}</Text>
  </View>
)

const ChatRoomScreen: React.FC = () => {
  const route = useRoute<RouteProp<ChatStackParamList, typeof SCREENS.STACK.CHAT_ROOM>>()
  const currentUser = useAuthStore((state) => state.user)
  const clearUnreadForConversation = useChatStore((state) => state.clearUnreadForConversation)
  const insets = useSafeAreaInsets()
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [isLoading, setIsLoading] = useState(false)
  const [isSending, setIsSending] = useState(false)
  const [input, setInput] = useState('')

  const loadMessages = useCallback(
    async (pageParam: number, append = false) => {
      if (append && pageParam >= totalPages) return
      try {
        setIsLoading(true)
        const response = await fetchMessages(route.params.partnerId, pageParam)
        setTotalPages(response.totalPages)
        setPage(response.number)

        const sortedMessages = [...response.content].sort(
          (a, b) => new Date(b.sentAt).getTime() - new Date(a.sentAt).getTime()
        )

        setMessages((prev) => (append ? [...prev, ...sortedMessages] : sortedMessages))

        // Mark messages as read on the server
        await markMessagesAsRead(route.params.partnerId)
        
        // Update local store to clear unread count immediately
        if (currentUser?.id) {
          clearUnreadForConversation(route.params.partnerId, currentUser.id)
        }
      } finally {
        setIsLoading(false)
      }
    },
    [route.params.partnerId, totalPages, currentUser?.id, clearUnreadForConversation]
  )

  useEffect(() => {
    loadMessages(0, false)
  }, [loadMessages])

  // Mark messages as read when screen gains focus
  useFocusEffect(
    useCallback(() => {
      // Mark messages as read and update store when screen is focused
      const markAsRead = async () => {
        try {
          await markMessagesAsRead(route.params.partnerId)
          if (currentUser?.id) {
            clearUnreadForConversation(route.params.partnerId, currentUser.id)
          }
        } catch (error) {
          console.error('Error marking messages as read:', error)
        }
      }
      
      // Only mark as read if we have messages loaded
      if (messages.length > 0) {
        markAsRead()
      }
    }, [route.params.partnerId, currentUser?.id, clearUnreadForConversation, messages.length])
  )

  const handleSend = async () => {
    if (!input.trim()) return

    try {
      setIsSending(true)
      const payload = await sendMessage({ receiverId: route.params.partnerId, content: input.trim(), messageType: 'TEXT' })
      setMessages((prev) => [payload, ...prev])
      setInput('')
    } finally {
      setIsSending(false)
    }
  }

  const handleLoadMore = () => {
    if (isLoading || page + 1 >= totalPages) return
    loadMessages(page + 1, true)
  }

  if (isLoading && messages.length === 0) {
    return <LoadingOverlay message="Đang tải tin nhắn" />
  }

  return (
    <KeyboardAvoidingView style={styles.flex} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <FlatList
        data={messages}
        keyExtractor={(item) => item.id.toString()}
        renderItem={({ item }) => (
          <MessageBubble message={item} isOwnMessage={item.senderId === currentUser?.id} />
        )}
        contentContainerStyle={[styles.listContent, { paddingBottom: 80 + insets.bottom }]}
        inverted
        onEndReachedThreshold={0.3}
        onEndReached={handleLoadMore}
        showsVerticalScrollIndicator={false}
      />

      <View style={[styles.composer, { paddingBottom: 60 + insets.bottom }]}>
        <TextInput
          mode="outlined"
          placeholder="Nhập tin nhắn"
          value={input}
          onChangeText={setInput}
          style={styles.input}
        />
        <Button mode="contained" onPress={handleSend} loading={isSending} disabled={isSending || !input.trim()}>
          Gửi
        </Button>
      </View>
    </KeyboardAvoidingView>
  )
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  listContent: {
    padding: 16,
  },
  messageContainer: {
    maxWidth: '80%',
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderRadius: 16,
    marginBottom: 12,
  },
  messageIncoming: {
    alignSelf: 'flex-start',
    backgroundColor: '#e2e8f0',
  },
  messageOutgoing: {
    alignSelf: 'flex-end',
    backgroundColor: '#2563eb',
  },
  messageIncomingText: {
    color: '#0f172a',
  },
  messageOutgoingText: {
    color: '#fff',
  },
  messageContent: {
    fontSize: 16,
    marginBottom: 6,
  },
  messageTime: {
    fontSize: 12,
    color: '#94a3b8',
    textAlign: 'right',
  },
  composer: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 12,
    backgroundColor: '#fff',
    borderTopWidth: StyleSheet.hairlineWidth,
    borderColor: '#e2e8f0',
    columnGap: 8,
  },
  input: {
    flex: 1,
    backgroundColor: '#fff',
  },
})

export default ChatRoomScreen
