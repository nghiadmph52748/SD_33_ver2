import React, { useState } from 'react'
import { FlatList, KeyboardAvoidingView, Platform, StyleSheet, View } from 'react-native'
import { Avatar, Button, Card, Text, TextInput } from 'react-native-paper'

import { chatWithAI } from '../../api/ai'
import { notifyError } from '../../utils/notifications'

interface ChatHistoryItem {
  id: string
  role: 'user' | 'assistant'
  content: string
}

const AIAssistantScreen: React.FC = () => {
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
        contentContainerStyle={styles.listContent}
      />

      <View style={styles.composer}>
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
    paddingBottom: 32,
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
