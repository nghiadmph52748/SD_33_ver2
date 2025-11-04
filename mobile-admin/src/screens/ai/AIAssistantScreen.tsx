import React, { useState } from 'react'
import { FlatList, KeyboardAvoidingView, Platform, StyleSheet, View } from 'react-native'
import { Avatar, Button, Card, Text, TextInput } from 'react-native-paper'
import { useSafeAreaInsets } from 'react-native-safe-area-context'

import { chatWithAI } from '../../api/ai'
import { notifyError } from '../../utils/notifications'

interface ChatHistoryItem {
  id: string
  role: 'user' | 'assistant'
  content: string
}

const AIAssistantScreen: React.FC = () => {
  const insets = useSafeAreaInsets()
  const [messages, setMessages] = useState<ChatHistoryItem[]>([])
  const [input, setInput] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  const handleSend = async () => {
    if (!input.trim()) return

    const userMessage: ChatHistoryItem = {
      id: `${Date.now()}-user`,
      role: 'user',
      content: input.trim(),
    }

    setMessages((prev) => [...prev, userMessage])
    setInput('')

    try {
      setIsLoading(true)
      const response = await chatWithAI(userMessage.content)
      const assistantMessage: ChatHistoryItem = {
        id: `${Date.now()}-assistant`,
        role: 'assistant',
        content: response.message,
      }
      setMessages((prev) => [...prev, assistantMessage])
    } catch (error: any) {
      notifyError(error.message ?? 'Không thể kết nối AI')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <KeyboardAvoidingView style={styles.flex} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <FlatList
        data={messages}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => (
          <View style={[styles.messageRow, item.role === 'assistant' ? styles.assistantRow : styles.userRow]}>
            {item.role === 'assistant' ? <Avatar.Icon icon="robot" size={36} style={styles.avatarAssistant} /> : null}
            <Card style={styles.messageCard}>
              <Card.Content>
                <Text style={styles.messageText}>{item.content}</Text>
              </Card.Content>
            </Card>
            {item.role === 'user' ? <Avatar.Icon icon="account" size={36} style={styles.avatarUser} /> : null}
          </View>
        )}
        contentContainerStyle={[
          styles.listContent,
          messages.length === 0 && styles.emptyListContent,
          { paddingBottom: 80 + insets.bottom },
        ]}
        ListEmptyComponent={
          <View style={styles.emptyState}>
            <Avatar.Icon icon="robot" size={64} style={styles.emptyAvatar} />
            <Text variant="titleLarge" style={styles.emptyTitle}>
              AI Assistant
            </Text>
            <Text variant="bodyMedium" style={styles.emptyDescription}>
              Hỏi tôi về doanh thu, đơn hàng, sản phẩm hoặc bất kỳ thông tin kinh doanh nào bạn cần phân tích.
            </Text>
            <View style={styles.suggestionsContainer}>
              <Text variant="labelMedium" style={styles.suggestionsTitle}>
                Gợi ý câu hỏi:
              </Text>
              <View style={styles.suggestionChip}>
                <Text style={styles.suggestionText}>📊 Doanh thu hôm nay là bao nhiêu?</Text>
              </View>
              <View style={styles.suggestionChip}>
                <Text style={styles.suggestionText}>📦 Sản phẩm nào bán chạy nhất?</Text>
              </View>
              <View style={styles.suggestionChip}>
                <Text style={styles.suggestionText}>👥 Khách hàng nào mua nhiều nhất?</Text>
              </View>
            </View>
          </View>
        }
        showsVerticalScrollIndicator={false}
      />

      <View style={[styles.composer, { paddingBottom: 60 + insets.bottom }]}>
        <TextInput
          mode="outlined"
          placeholder="Hỏi AI về doanh thu, đơn hàng..."
          value={input}
          onChangeText={setInput}
          style={styles.input}
          multiline
        />
        <Button mode="contained" onPress={handleSend} loading={isLoading} disabled={isLoading || !input.trim()}>
          Gửi
        </Button>
      </View>
    </KeyboardAvoidingView>
  )
}

const styles = StyleSheet.create({
  flex: { flex: 1, backgroundColor: '#f8fafc' },
  listContent: {
    padding: 16,
  },
  emptyListContent: {
    flexGrow: 1,
    justifyContent: 'center',
  },
  emptyState: {
    alignItems: 'center',
    paddingHorizontal: 24,
    paddingVertical: 48,
  },
  emptyAvatar: {
    backgroundColor: '#6366f1',
    marginBottom: 16,
  },
  emptyTitle: {
    fontWeight: '700',
    marginBottom: 8,
    color: '#1e293b',
  },
  emptyDescription: {
    textAlign: 'center',
    color: '#64748b',
    marginBottom: 32,
    lineHeight: 22,
  },
  suggestionsContainer: {
    width: '100%',
    alignItems: 'flex-start',
  },
  suggestionsTitle: {
    fontWeight: '600',
    marginBottom: 12,
    color: '#475569',
  },
  suggestionChip: {
    backgroundColor: '#fff',
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderRadius: 12,
    marginBottom: 8,
    width: '100%',
    borderWidth: 1,
    borderColor: '#e2e8f0',
  },
  suggestionText: {
    fontSize: 14,
    color: '#334155',
  },
  messageRow: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    marginBottom: 16,
  },
  userRow: {
    justifyContent: 'flex-end',
  },
  assistantRow: {
    justifyContent: 'flex-start',
  },
  messageCard: {
    maxWidth: '70%',
    borderRadius: 18,
  },
  messageText: {
    fontSize: 16,
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
    maxHeight: 120,
    backgroundColor: '#fff',
  },
  avatarAssistant: {
    marginRight: 8,
    backgroundColor: '#6366f1',
  },
  avatarUser: {
    marginLeft: 8,
    backgroundColor: '#2563eb',
  },
})

export default AIAssistantScreen
