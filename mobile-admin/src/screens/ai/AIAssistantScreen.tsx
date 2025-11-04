import React, { useEffect, useRef, useState } from 'react'
import { FlatList, KeyboardAvoidingView, Platform, StyleSheet, View, Text as RNText, TouchableOpacity, ActivityIndicator, Animated, Easing } from 'react-native'
import { Avatar, Button, Text, TextInput } from 'react-native-paper'
import { useSafeAreaInsets } from 'react-native-safe-area-context'

import { chatWithAI, checkAIHealth, chatWithAIStream } from '../../api/ai'
import Markdown from '../../components/Markdown'
import { notifyError } from '../../utils/notifications'

interface ChatHistoryItem {
  id: string
  role: 'user' | 'assistant'
  content: string
  thinking?: string
  showThinking?: boolean
  thinkingLive?: string
  dataSource?: string
  followUpSuggestions?: string[]
  isThinking?: boolean
  processingStatus?: 'querying' | 'analyzing' | 'ready'
  rawStream?: string
}

type AssistantProcessingState = ChatHistoryItem['processingStatus']

const THINKING_COPY: Record<NonNullable<AssistantProcessingState> | 'default', { title: string; subtitle: string }> = {
  querying: {
    title: 'Đang kết nối dữ liệu',
    subtitle: 'AI đang kiểm tra nguồn dữ liệu phù hợp...',
  },
  analyzing: {
    title: 'Đang suy nghĩ',
    subtitle: 'AI đang phân tích thông tin và tạo câu trả lời tối ưu...',
  },
  ready: {
    title: 'Đang hoàn thiện phản hồi',
    subtitle: 'Gần xong! Chuẩn bị gửi câu trả lời cho bạn...',
  },
  default: {
    title: 'Đang suy nghĩ',
    subtitle: 'AI đang xử lý yêu cầu của bạn...',
  },
}

const getThinkingCopy = (status?: AssistantProcessingState) => THINKING_COPY[status ?? 'default']

const AnimatedDot: React.FC<{ delay: number }> = ({ delay }) => {
  const opacity = useRef(new Animated.Value(0.4)).current

  useEffect(() => {
    const animation = Animated.loop(
      Animated.sequence([
        Animated.delay(delay),
        Animated.timing(opacity, {
          toValue: 1,
          duration: 320,
          easing: Easing.inOut(Easing.ease),
          useNativeDriver: true,
        }),
        Animated.timing(opacity, {
          toValue: 0.35,
          duration: 320,
          easing: Easing.inOut(Easing.ease),
          useNativeDriver: true,
        }),
      ])
    )
    animation.start()
    return () => {
      animation.stop()
    }
  }, [delay, opacity])

  return <Animated.View style={[styles.dot, { opacity }]} />
}

const ThinkingDots: React.FC = () => (
  <View style={styles.dots}>
    <AnimatedDot delay={0} />
    <AnimatedDot delay={160} />
    <AnimatedDot delay={320} />
  </View>
)

const ThinkingSkeleton: React.FC = () => {
  const shimmer = useRef(new Animated.Value(0)).current

  useEffect(() => {
    const animation = Animated.loop(
      Animated.sequence([
        Animated.timing(shimmer, {
          toValue: 1,
          duration: 900,
          easing: Easing.inOut(Easing.ease),
          useNativeDriver: false,
        }),
        Animated.timing(shimmer, {
          toValue: 0,
          duration: 900,
          easing: Easing.inOut(Easing.ease),
          useNativeDriver: false,
        }),
      ])
    )
    animation.start()
    return () => {
      animation.stop()
    }
  }, [shimmer])

  const backgroundColor = shimmer.interpolate({
    inputRange: [0, 1],
    outputRange: ['rgba(148,163,184,0.15)', 'rgba(99,102,241,0.25)'],
  })

  return (
    <View style={styles.skeletonContainer}>
      <Animated.View style={[styles.skeletonLine, { width: '85%', backgroundColor }]} />
      <Animated.View style={[styles.skeletonLine, { width: '72%', backgroundColor }]} />
      <Animated.View style={[styles.skeletonLineShort, { backgroundColor }]} />
    </View>
  )
}

const ThinkingStateBubble: React.FC<{
  status?: AssistantProcessingState
  thinkingLive?: string
}> = ({ status, thinkingLive }) => {
  const copy = getThinkingCopy(status)
  const trimmed = thinkingLive?.trim()

  return (
    <View style={[styles.content, styles.contentAssistant, styles.thinkingBubble]}>
      <View style={styles.thinkingHeader}>
        <RNText style={styles.thinkingHeaderTitle}>{copy.title}</RNText>
        <ThinkingDots />
      </View>
      {trimmed ? (
        <RNText style={styles.thinkingLiveText}>{trimmed}</RNText>
      ) : (
        <ThinkingSkeleton />
      )}
      <RNText style={styles.thinkingSubText}>{copy.subtitle}</RNText>
    </View>
  )
}

const AIAssistantScreen: React.FC = () => {
  const insets = useSafeAreaInsets()
  const [messages, setMessages] = useState<ChatHistoryItem[]>([])
  const [input, setInput] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [isAIReady, setIsAIReady] = useState<boolean>(true)
  const [aiHealthMessage, setAiHealthMessage] = useState<string>('')
  const listRef = useRef<FlatList<ChatHistoryItem>>(null)

  useEffect(() => {
    let isMounted = true
    ;(async () => {
      try {
        const health = await checkAIHealth()
        if (!isMounted) return
        setIsAIReady(true)
        setAiHealthMessage(health?.message || 'AI ready')
      } catch (e: any) {
        if (!isMounted) return
        setIsAIReady(false)
        setAiHealthMessage('AI service unavailable')
      }
    })()
    return () => {
      isMounted = false
    }
  }, [])

  const handleSend = async () => {
    if (!input.trim()) return
    // Allow sending even if health check failed; we'll surface errors from the call itself

    const userMessage: ChatHistoryItem = {
      id: `${Date.now()}-user`,
      role: 'user',
      content: input.trim(),
    }

    setMessages((prev) => [...prev, userMessage])
    setInput('')

    try {
      setIsLoading(true)
      const assistantId = `${Date.now()}-assistant`
      // Add placeholder assistant message with loading state
      setMessages((prev) => [
        ...prev,
        { id: assistantId, role: 'assistant', content: '', isThinking: true, processingStatus: 'querying', rawStream: '' },
      ])

      let receivedText = false
      await chatWithAIStream(
        userMessage.content,
        (text, metadata) => {
          if (metadata && metadata.type === 'start') {
            setMessages((prev) => {
              const next = [...prev]
              const idx = next.findIndex((m) => m.id === assistantId)
              if (idx !== -1) {
                next[idx] = {
                  ...next[idx],
                  dataSource: (metadata as any)?.data_source || next[idx].dataSource,
                  followUpSuggestions: (metadata as any)?.follow_up_suggestions || next[idx].followUpSuggestions,
                  isThinking: true,
                  processingStatus: 'analyzing',
                }
              }
              return next
            })
            return
          }
          if (text) {
            receivedText = true
            setMessages((prev) => {
              const next = [...prev]
              const idx = next.findIndex((m) => m.id === assistantId)
              if (idx !== -1) {
                const current = next[idx]
                const raw = (current.rawStream || '') + text

                // Extract final thinking (all closed segments)
                const thinkMatches = [...raw.matchAll(/<think>([\s\S]*?)<\/think>/g)]
                const thinking = thinkMatches.length > 0 ? thinkMatches.map((m) => m[1]).join('\n').trim() : undefined

                // Extract live thinking (after last <think> without closing)
                const lastOpen = raw.lastIndexOf('<think>')
                const lastClose = raw.lastIndexOf('</think>')
                const hasUnclosed = lastOpen !== -1 && (lastClose === -1 || lastClose < lastOpen)
                const thinkingLive = hasUnclosed ? raw.slice(lastOpen + 7) : undefined

                // Visible content: remove all closed think blocks; if currently inside think, also remove the trailing open block
                let visible = raw.replace(/<think>[\s\S]*?<\/think>/g, '')
                if (hasUnclosed) {
                  visible = visible.replace(/<think>[\s\S]*$/g, '')
                }
                visible = visible.trim()

                next[idx] = {
                  ...current,
                  rawStream: raw,
                  thinking: thinking,
                  thinkingLive: thinkingLive,
                  content: hasUnclosed ? '' : visible,
                  isThinking: hasUnclosed,
                  processingStatus: hasUnclosed ? 'analyzing' : (visible ? 'ready' : current.processingStatus),
                  showThinking: hasUnclosed ? true : current.showThinking,
                }
              }
              return next
            })
            // Scroll to end on new chunk
            requestAnimationFrame(() => {
              listRef.current?.scrollToEnd({ animated: true })
            })
          }
        },
        async () => {
          // If stream completed without text, fallback to non-stream to fetch final content
          if (!receivedText) {
            try {
              const res = await chatWithAI(userMessage.content)
              setMessages((prev) => {
                const next = [...prev]
                const idx = next.findIndex((m) => m.id === assistantId)
                if (idx !== -1) {
                  next[idx] = { ...next[idx], content: res?.message || '' }
                }
                return next
              })
            } catch {}
          }
          // Finalize parsing using the raw buffer to ensure all tags handled
          setMessages((prev) => {
            const next = [...prev]
            const idx = next.findIndex((m) => m.id === assistantId)
            if (idx !== -1) {
              const raw = next[idx].rawStream || ''
              const thinkMatches = [...raw.matchAll(/<think>([\s\S]*?)<\/think>/g)]
              const thinking = thinkMatches.length > 0 ? thinkMatches.map((m) => m[1]).join('\n').trim() : ''
              let visible = raw.replace(/<think>[\s\S]*?<\/think>/g, '')
              visible = visible.replace(/<think>[\s\S]*$/g, '')
              visible = visible.trim()
              next[idx] = { ...next[idx], content: visible, thinking, thinkingLive: undefined, showThinking: false, isThinking: false, processingStatus: 'ready' }
            }
            return next
          })
          setIsLoading(false)
        },
        (errorMsg) => {
          setIsLoading(false)
          // Remove the empty assistant bubble on error
          setMessages((prev) => prev.filter((m) => m.id !== assistantId))
          notifyError(errorMsg || 'Không thể kết nối AI')
        }
      )
    } catch (error: any) {
      notifyError(error.message ?? 'Không thể kết nối AI')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <KeyboardAvoidingView style={styles.flex} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <FlatList
        ref={listRef}
        data={messages}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => (
          <View style={[styles.messageWrapper, item.role === 'user' ? styles.rowReverse : null]}>
            <View style={styles.avatarSlot}>
              {item.role === 'assistant' ? (
                <Avatar.Icon icon="robot" size={40} style={styles.avatarAssistant} />
              ) : (
                <Avatar.Icon icon="account" size={40} style={styles.avatarUser} />
              )}
            </View>
            {item.role === 'assistant' && (item.isThinking || item.thinkingLive) ? (
              <ThinkingStateBubble status={item.processingStatus} thinkingLive={item.thinkingLive} />
            ) : (
              <View
                style={[
                  styles.content,
                  item.role === 'assistant' ? styles.contentAssistant : styles.contentUser,
                ]}
              >
                {!!item.content && (
                  item.role === 'user' ? (
                    <RNText style={[styles.text, styles.textOnPrimary]}>{item.content}</RNText>
                  ) : (
                    <Markdown content={item.content} />
                  )
                )}
                {item.role === 'assistant' && (!item.content || item.content.trim().length === 0) && item.isThinking && !item.thinkingLive && !item.thinking ? (
                  <View style={styles.processingRow}>
                    <ActivityIndicator size="small" color="#64748b" />
                    <RNText style={styles.processingText}>Đang suy nghĩ...</RNText>
                  </View>
                ) : null}
                {item.role === 'assistant' && !!item.content && !!item.dataSource ? (
                  <View style={styles.metaRow}>
                    <RNText style={styles.metaBadge}>Nguồn: {item.dataSource}</RNText>
                  </View>
                ) : null}
                {item.role === 'assistant' && !!item.content && Array.isArray(item.followUpSuggestions) && item.followUpSuggestions.length > 0 ? (
                  <View style={styles.suggestionsRow}>
                    {item.followUpSuggestions.map((s, idx) => (
                      <TouchableOpacity key={`${item.id}-s-${idx}`} style={styles.suggestionBtn} onPress={() => setInput(s)}>
                        <RNText style={styles.suggestionTextSm}>{s}</RNText>
                      </TouchableOpacity>
                    ))}
                  </View>
                ) : null}
                {item.role === 'assistant' && item.thinking ? (
                  <View style={styles.reasoningContainer}>
                    <TouchableOpacity
                      style={styles.reasoningToggle}
                      onPress={() =>
                        setMessages((prev) =>
                          prev.map((m) =>
                            m.id === item.id ? { ...m, showThinking: !m.showThinking } : m
                          )
                        )
                      }
                      activeOpacity={0.7}
                    >
                      <RNText style={styles.reasoningToggleText}>
                        {item.showThinking ? 'Ẩn quá trình suy nghĩ' : 'Xem quá trình suy nghĩ'}
                      </RNText>
                      <RNText style={styles.reasoningToggleHint}>{item.showThinking ? '-' : '+'}</RNText>
                    </TouchableOpacity>
                    {item.showThinking ? (
                      <View style={styles.reasoningBox}>
                        <RNText style={styles.reasoningText}>{item.thinking}</RNText>
                      </View>
                    ) : null}
                  </View>
                ) : null}
              </View>
            )}
          </View>
        )}
        contentContainerStyle={[
          styles.listContent,
          messages.length === 0 && styles.emptyListContent,
          { paddingBottom: 80 + insets.bottom },
        ]}
        ListHeaderComponent={
          !isAIReady ? (
            <View style={styles.healthBanner}>
              <Text style={styles.healthBannerText}>⚠️ {aiHealthMessage || 'AI service unavailable'}</Text>
            </View>
          ) : null
        }
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
          placeholder={isAIReady ? 'Hỏi AI về doanh thu, đơn hàng...' : 'AI service unavailable'}
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
  healthBanner: {
    backgroundColor: '#fff7ed',
    borderColor: '#fdba74',
    borderWidth: 1,
    paddingHorizontal: 12,
    paddingVertical: 10,
    borderRadius: 10,
    marginHorizontal: 16,
    marginTop: 12,
    marginBottom: 8,
  },
  healthBannerText: {
    color: '#9a3412',
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
  messageWrapper: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: 8,
    marginBottom: 16,
  },
  rowReverse: {
    flexDirection: 'row-reverse',
  },
  avatarSlot: {
    width: 40,
    height: 40,
  },
  content: {
    paddingHorizontal: 12,
    paddingVertical: 10,
    borderRadius: 14,
    maxWidth: '72%',
  },
  contentAssistant: {
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: '#e2e8f0',
  },
  contentUser: {
    backgroundColor: '#165dff',
  },
  metaRow: {
    marginTop: 8,
  },
  metaBadge: {
    fontSize: 12,
    color: '#64748b',
    backgroundColor: '#f1f5f9',
    borderWidth: 1,
    borderColor: '#e2e8f0',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 8,
    alignSelf: 'flex-start',
  },
  suggestionsRow: {
    marginTop: 8,
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 6,
  },
  suggestionBtn: {
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#e2e8f0',
    backgroundColor: '#ffffff',
  },
  suggestionTextSm: {
    fontSize: 12,
    color: '#334155',
  },
  processingRow: {
    marginTop: 2,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  processingText: {
    color: '#64748b',
  },
  text: {
    fontSize: 16,
    lineHeight: 22,
    color: '#0f172a',
  },
  textOnPrimary: {
    color: '#ffffff',
  },
  messageCard: {
    maxWidth: '78%',
    borderRadius: 18,
    flexShrink: 1,
  },
  messageCardAssistant: {
    alignSelf: 'flex-start',
  },
  messageCardUser: {
    alignSelf: 'flex-end',
  },
  bubble: {
    maxWidth: '78%',
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderRadius: 18,
    flexShrink: 1,
  },
  bubbleAssistant: {
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: '#e2e8f0',
    alignSelf: 'flex-start',
  },
  bubbleUser: {
    backgroundColor: '#e0edff',
    alignSelf: 'flex-end',
  },
  messageText: {
    fontSize: 16,
    color: '#0f172a',
    lineHeight: 22,
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
  thinkingBubble: {
    backgroundColor: '#eef2ff',
    borderColor: '#c7d2fe',
    borderWidth: 1,
  },
  thinkingHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 12,
  },
  thinkingHeaderTitle: {
    color: '#312e81',
    fontWeight: '600',
    fontSize: 14,
  },
  dots: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  dot: {
    width: 6,
    height: 6,
    borderRadius: 3,
    backgroundColor: '#6366f1',
  },
  thinkingLiveText: {
    marginTop: 10,
    fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace',
    fontSize: 13,
    color: '#312e81',
    lineHeight: 18,
  },
  thinkingSubText: {
    marginTop: 10,
    fontSize: 12,
    color: '#6366f1',
  },
  skeletonContainer: {
    marginTop: 12,
    gap: 8,
  },
  skeletonLine: {
    height: 10,
    borderRadius: 20,
    backgroundColor: '#e2e8f0',
  },
  skeletonLineShort: {
    height: 10,
    borderRadius: 20,
    width: '55%',
    backgroundColor: '#e2e8f0',
  },
  reasoningContainer: {
    marginTop: 12,
    borderTopWidth: 1,
    borderColor: '#e2e8f0',
    paddingTop: 10,
    gap: 8,
  },
  reasoningToggle: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  reasoningToggleText: {
    color: '#475569',
    fontSize: 13,
    fontWeight: '600',
  },
  reasoningToggleHint: {
    color: '#6366f1',
    fontWeight: '700',
    fontSize: 16,
    marginLeft: 12,
  },
  reasoningBox: {
    backgroundColor: '#f8fafc',
    borderWidth: 1,
    borderColor: '#e2e8f0',
    borderRadius: 12,
    padding: 10,
  },
  reasoningText: {
    fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace',
    fontSize: 12,
    color: '#475569',
    lineHeight: 18,
  },
  avatarUser: {
    marginLeft: 8,
    backgroundColor: '#2563eb',
  },
})

export default AIAssistantScreen
