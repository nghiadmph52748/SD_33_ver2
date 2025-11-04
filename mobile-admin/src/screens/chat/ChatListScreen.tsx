import { useFocusEffect, useNavigation } from '@react-navigation/native'
import React, { useCallback, useMemo, useState } from 'react'
import { FlatList, RefreshControl, StyleSheet, View } from 'react-native'
import { Avatar, List, Text } from 'react-native-paper'

import { Conversation, fetchConversations, getUnreadCount } from '../../api/chat'
import { ListEmpty, SearchInput } from '../../components'
import { SCREENS } from '../../constants/routes'
import useAuthStore from '../../store/useAuthStore'
import useChatStore from '../../store/useChatStore'
import type { ChatStackParamList } from '../../navigation/ChatNavigator'
import type { NativeStackNavigationProp } from '@react-navigation/native-stack'

const ChatListScreen: React.FC = () => {
  const navigation = useNavigation<NativeStackNavigationProp<ChatStackParamList>>()
  const currentUser = useAuthStore((state) => state.user)
  const { conversations, setConversations, setUnreadCount, unreadCount } = useChatStore()
  const [query, setQuery] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  const loadConversations = useCallback(async () => {
    try {
      setIsLoading(true)
      const [conversationData, unread] = await Promise.all([fetchConversations(), getUnreadCount()])
      setConversations(conversationData)
      setUnreadCount(unread)
    } finally {
      setIsLoading(false)
    }
  }, [setConversations, setUnreadCount])

  useFocusEffect(
    useCallback(() => {
      loadConversations()
    }, [loadConversations])
  )

  const filteredConversations = useMemo(() => {
    if (!query) return conversations
    const normalized = query.toLowerCase()
    return conversations.filter((conversation) => {
      const partnerName = conversation.nhanVien1Id === currentUser?.id ? conversation.nhanVien2Name : conversation.nhanVien1Name
      return partnerName.toLowerCase().includes(normalized)
    })
  }, [conversations, query, currentUser?.id])

  return (
    <View style={styles.container}>
      <SearchInput query={query} onChangeQuery={setQuery} placeholder="Tìm kiếm cuộc trò chuyện" />

      <FlatList
        data={filteredConversations}
        keyExtractor={(item) => item.id.toString()}
        refreshControl={<RefreshControl refreshing={isLoading} onRefresh={loadConversations} />}
        ListEmptyComponent={!isLoading ? <ListEmpty title="Chưa có cuộc trò chuyện" /> : null}
        showsVerticalScrollIndicator={false}
        renderItem={({ item }) => {
          const isFirstUser = item.nhanVien1Id === currentUser?.id
          const partnerId = isFirstUser ? item.nhanVien2Id : item.nhanVien1Id
          const partnerName = isFirstUser ? item.nhanVien2Name : item.nhanVien1Name
          const unread = isFirstUser ? item.unreadCountNv1 : item.unreadCountNv2

          return (
            <View style={styles.listItem}>
              <List.Item
                title={partnerName}
                description={item.lastMessageContent ?? 'Chưa có tin nhắn'}
                onPress={() => navigation.navigate(SCREENS.STACK.CHAT_ROOM, { partnerId, partnerName })}
                left={(props) => (
                  <Avatar.Text
                    label={partnerName?.substring(0, 2).toUpperCase() ?? 'NV'}
                    size={44}
                    style={[styles.avatar, { marginLeft: 8 }]}
                  />
                )}
                right={(props) => (
                  <View style={styles.rightContent}>
                    <Text style={styles.timestamp}>
                      {item.lastMessageTime ? new Date(item.lastMessageTime).toLocaleTimeString('vi-VN') : ''}
                    </Text>
                    {unread > 0 ? <View style={styles.badge}><Text style={styles.badgeText}>{unread}</Text></View> : null}
                  </View>
                )}
                style={styles.listItemInner}
              />
            </View>
          )
        }}
        contentContainerStyle={styles.listContent}
      />

      <View style={styles.footer}>
        <Text variant="bodySmall" style={styles.unreadSummary}>
          Tổng số tin chưa đọc: {unreadCount}
        </Text>
      </View>
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  listContent: {
    paddingBottom: 100,
  },
  listItem: {
    paddingHorizontal: 0,
  },
  listItemInner: {
    paddingLeft: 0,
    paddingRight: 8,
  },
  avatar: {
    backgroundColor: '#2563eb',
  },
  rightContent: {
    alignItems: 'flex-end',
    justifyContent: 'center',
    marginRight: 8,
  },
  timestamp: {
    color: '#64748b',
    fontSize: 12,
  },
  badge: {
    marginTop: 6,
    minWidth: 22,
    paddingHorizontal: 6,
    paddingVertical: 2,
    backgroundColor: '#ef4444',
    borderRadius: 12,
    alignItems: 'center',
  },
  badgeText: {
    color: '#fff',
    fontWeight: '600',
    fontSize: 12,
  },
  footer: {
    padding: 12,
    paddingBottom: 100,
    borderTopWidth: StyleSheet.hairlineWidth,
    borderColor: '#e2e8f0',
  },
  unreadSummary: {
    textAlign: 'center',
    color: '#475569',
  },
})

export default ChatListScreen
