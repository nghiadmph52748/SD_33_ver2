import React, { useEffect, useState, useCallback, useMemo } from 'react'
import { View, Text, StyleSheet, ScrollView, Image } from 'react-native'
import { useRoute, useNavigation, RouteProp } from '@react-navigation/native'
import { Card, ActivityIndicator, Divider } from 'react-native-paper'
import { Client } from '@stomp/stompjs'

import env from '../../config/env'
import { getQRSession, getLatestQRSession, QRSession, CartItem } from '../../api/qrSession'
import type { NativeStackNavigationProp } from '@react-navigation/native-stack'

type QRPaymentStackParamList = {
  'QR.Payment': { sessionId?: string }
  'QR.Success': { sessionId: string }
  'QR.Confirmation': { sessionId: string }
}

type QRPaymentScreenRouteProp = RouteProp<QRPaymentStackParamList, 'QR.Payment'>

type QRPaymentScreenNavigationProp = NativeStackNavigationProp<
  QRPaymentStackParamList,
  'QR.Payment'
>

const currencyFormatter = new Intl.NumberFormat('vi-VN', {
  style: 'currency',
  currency: 'VND',
  maximumFractionDigits: 0,
})

const BANK_SHORT_CODE = 'MB'
const BANK_ACCOUNT_NUMBER = '288579542666'

const logoSource = require('../../assets/logo-datn.png')

export default function QRPaymentScreen() {
  const route = useRoute<QRPaymentScreenRouteProp>()
  const navigation = useNavigation<QRPaymentScreenNavigationProp>()

  const [sessionId, setSessionId] = useState<string | null>(route.params?.sessionId ?? null)
  const [session, setSession] = useState<QRSession | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [initializing, setInitializing] = useState(!route.params?.sessionId)
  const [countdown, setCountdown] = useState<string | null>(null)

  const wsUrl = useMemo(() => {
    try {
      const apiUrl = new URL(env.API_BASE_URL)
      const protocol = apiUrl.protocol === 'https:' ? 'wss:' : 'ws:'
      return `${protocol}//${apiUrl.host}/ws-chat`
    } catch {
      return null
    }
  }, [])

  const fetchLatestSession = useCallback(async () => {
    setInitializing(true)
    try {
      const latest = await getLatestQRSession()
      if (latest) {
        setSessionId(latest.qrSessionId)
        setSession(latest)
        setError(null)
      } else {
        setSessionId(null)
        setSession(null)
        setError('Vui lòng chờ...')
      }
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Không thể tìm thấy phiên thanh toán'
      setError(message)
    } finally {
      setInitializing(false)
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    if (route.params?.sessionId) {
      setSessionId(route.params.sessionId)
      setInitializing(false)
      return
    }
    fetchLatestSession()
  }, [route.params?.sessionId, fetchLatestSession])

  const loadSessionById = useCallback(
    async (targetSessionId: string, options: { showLoader?: boolean } = {}) => {
      if (!targetSessionId) return
      if (options.showLoader !== false) {
        setLoading(true)
      }
      try {
        const data = await getQRSession(targetSessionId)
        setSessionId(targetSessionId)
        if (
          data.status === 'EXPIRED' ||
          data.status === 'CANCELLED' ||
          !data.items?.length ||
          !data.finalPrice
        ) {
          setSession(null)
          setSessionId(null)
          setError('Vui lòng chờ...')
          return
        }

        setSession(data)
        setError(null)

        if (data.status === 'PAID') {
          navigation.replace('QR.Success', { sessionId: targetSessionId })
        }
      } catch (err) {
        const message = err instanceof Error ? err.message : 'Không thể tải thông tin phiên thanh toán'
        setError(message)
        console.error('Failed to load session:', err)

        if (message.includes('Session not found') || message.includes('Không thể lấy thông tin session')) {
          setSession(null)
          setSessionId(null)
          return
        }
      } finally {
        if (options.showLoader !== false) {
          setLoading(false)
        }
      }
    },
    [fetchLatestSession, navigation]
  )

  useEffect(() => {
    if (!sessionId) {
      setLoading(false)
      return
    }

    loadSessionById(sessionId)

    const interval = setInterval(() => {
      if (session?.status === 'PENDING') {
        loadSessionById(sessionId, { showLoader: false })
      } else {
        clearInterval(interval)
      }
    }, 1000)

    return () => clearInterval(interval)
  }, [sessionId, session?.status, loadSessionById])

  useEffect(() => {
    if (!wsUrl) return
    const client = new Client({
      brokerURL: wsUrl,
      reconnectDelay: 5000,
      connectHeaders: {
        'x-qr-anon': 'true',
      },
      debug: (str) => {
        if (__DEV__) {
          console.log('[STOMP]', str)
        }
      },
      onConnect: () => {
        client.subscribe('/topic/qr-session', (frame) => {
          try {
            const payload = JSON.parse(frame.body) as { sessionId?: string }
            if (!payload?.sessionId) return
            setInitializing(false)
            setError(null)
            loadSessionById(payload.sessionId, { showLoader: false })
          } catch (err) {
            console.warn('Failed to parse QR session event', err)
          }
        })
      },
      onStompError: (frame) => {
        console.warn('STOMP error:', frame.body)
      },
    })

    client.activate()

    return () => {
      client.deactivate()
    }
  }, [wsUrl, loadSessionById])

  useEffect(() => {
    const interval = setInterval(async () => {
      if (initializing) return
      try {
        const latest = await getLatestQRSession()

        // Check if we should update to this session:
        // 1. Different sessionId (new order created)
        // 2. Same sessionId but different orderCode (shouldn't happen, but defensive)
        // 3. Session became "latest" again (staff switched back to it)
        const shouldUpdate = latest && (
          latest.qrSessionId !== sessionId ||
          (session && latest.orderCode !== session.orderCode)
        )

        if (shouldUpdate) {
          loadSessionById(latest.qrSessionId, { showLoader: false })
          return
        }

        if (!latest && sessionId) {
          setSessionId(null)
          setSession(null)
          setError('Vui lòng chờ...')
        }
      } catch (err) {
        if (__DEV__) {
          console.warn('latest session polling failed', err)
        }
      }
    }, 4000)

    return () => clearInterval(interval)
  }, [initializing, sessionId, session, loadSessionById])

  useEffect(() => {
    if (session?.status === 'EXPIRED') {
      setSession(null)
      setSessionId(null)
      setError('Chưa có phiên QR thanh toán đang hoạt động')
    }
  }, [session?.status])

  const getProductDisplayName = (item: CartItem) => item.productName
  const getVariantText = (item: CartItem) => {
    const variants = [item.tenMauSac, item.tenKichThuoc ? `Size ${item.tenKichThuoc}` : null, item.tenDeGiay, item.tenChatLieu].filter(
      Boolean
    )
    return variants.join(' • ')
  }

  const getUnitPrice = (item: CartItem) => {
    if (item.discount && item.discount > 0) {
      return item.price * (1 - item.discount / 100)
    }
    return item.price
  }

  const formattedDate = useMemo(() => {
    if (!session?.createdAt) return '--'
    try {
      return new Date(session.createdAt).toLocaleString('vi-VN', {
        hour12: false,
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      })
    } catch {
      return session.createdAt
    }
  }, [session?.createdAt])

  useEffect(() => {
    const canShowCountdown = Boolean(session?.qrCodeUrl) && session?.status === 'PENDING' && session?.expiresAt
    if (!canShowCountdown) {
      setCountdown(null)
      return
    }

    const updateCountdown = () => {
      try {
        const expires = new Date(session.expiresAt).getTime()
        const diff = expires - Date.now()
        if (diff <= 0) {
          setCountdown('00:00')
          return
        }
        const minutes = Math.floor(diff / 60000)
        const seconds = Math.floor((diff % 60000) / 1000)
        setCountdown(`${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`)
      } catch {
        setCountdown(null)
      }
    }

    updateCountdown()
    const timer = setInterval(updateCountdown, 1000)
    return () => clearInterval(timer)
  }, [session?.expiresAt, session?.status, session?.qrCodeUrl])

  const qrImageUrl = useMemo(() => {
    if (!session?.qrCodeUrl || !session?.finalPrice || !session.orderCode) {
      return null
    }
    try {
      const amount = Math.round(session.finalPrice)
      const info = encodeURIComponent(`${session.orderCode}`)
      return `https://img.vietqr.io/image/${BANK_SHORT_CODE}-${BANK_ACCOUNT_NUMBER}-qr_only.png?amount=${amount}&addInfo=${info}`
    } catch {
      return null
    }
  }, [session?.qrCodeUrl, session?.finalPrice, session?.orderCode])

  if (loading && !session) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" />
        <Text style={styles.loadingText}>Đang tải...</Text>
      </View>
    )
  }

  if (initializing) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" />
        <Text style={styles.loadingText}>Đang chuẩn bị dữ liệu...</Text>
      </View>
    )
  }

  const renderWelcome = (message?: string) => (
    <ScrollView style={styles.container} contentContainerStyle={styles.centerContent}>
      <View style={styles.welcomeWrapper}>
        <Card style={styles.welcomeCard}>
          <Card.Content>
            <Image source={logoSource} style={styles.welcomeLogo} resizeMode="contain" />
            <Text style={styles.welcomeTitle}>Chào mừng đến với GearUp Store!</Text>
            {message ? <Text style={styles.welcomeHint}>{message}</Text> : null}
          </Card.Content>
        </Card>
      </View>
    </ScrollView>
  )

  if (!sessionId) {
    return renderWelcome('Vui lòng chờ...')
  }

  if (!session) {
    return renderWelcome(error || 'Không tìm thấy phiên VietQR đang hoạt động')
  }

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
      <Card style={styles.qrCard}>
        <Card.Content style={styles.qrContent}>
          {qrImageUrl ? (
            <Image source={{ uri: qrImageUrl }} style={styles.qrImage} resizeMode="contain" />
          ) : (
            <Text style={styles.placeholderText}>Nhân viên sẽ hiển thị mã QR ngay khi sẵn sàng</Text>
          )}
        </Card.Content>
      </Card>

      <View style={styles.customerHeader}>
        <Text style={styles.customerGreeting}>GearUp Store</Text>
        <Text style={styles.customerSubtext}>Quý khách vui lòng kiểm tra giỏ hàng bên dưới</Text>
        <View style={styles.sessionPills}>
          <Text style={styles.sessionChip}>Đơn hàng: {session.orderCode}</Text>
          {session.qrCodeUrl && countdown ? (
            <Text style={styles.sessionChip}>QR hết hạn sau {countdown}</Text>
          ) : null}
        </View>
        <Text style={styles.amountLarge}>{currencyFormatter.format(session.finalPrice)}</Text>
      </View>

      <Card style={styles.itemsCard}>
        <Card.Title title="Giỏ hàng tại quầy" />
        <Card.Content>
          {session.items && session.items.length > 0 ? (
            session.items.map((item, index) => {
              const unitPrice = getUnitPrice(item)
              const lineTotal = unitPrice * item.quantity
              return (
                <View key={`${item.productId}-${index}`} style={styles.itemCard}>
                  <View style={styles.itemHeader}>
                    <Text style={styles.itemName}>{getProductDisplayName(item)}</Text>
                    <Text style={styles.lineTotal}>{currencyFormatter.format(lineTotal)}</Text>
                  </View>
                  {getVariantText(item) ? <Text style={styles.variantText}>{getVariantText(item)}</Text> : null}
                  <View style={styles.itemMetaRow}>
                    <Text style={styles.metaText}>Đơn giá: {currencyFormatter.format(unitPrice)}</Text>
                    <Text style={styles.quantityChip}>x{item.quantity}</Text>
                  </View>
                  {item.discount ? <Text style={styles.discountTag}>Giảm {item.discount}%</Text> : null}
                </View>
              )
            })
          ) : (
            <Text style={styles.emptyText}>Không có sản phẩm trong giỏ</Text>
          )}
        </Card.Content>
      </Card>

      <Card style={styles.summaryCard}>
        <Card.Title title="Tổng kết đơn hàng" />
        <Card.Content>
          <View style={styles.summaryRow}>
            <Text style={styles.summaryLabel}>Tạm tính</Text>
            <Text style={styles.summaryValue}>{currencyFormatter.format(session.subtotal)}</Text>
          </View>
          <View style={styles.summaryRow}>
            <Text style={styles.summaryLabel}>Giảm giá</Text>
            <Text style={[styles.summaryValue, session.discountAmount > 0 ? styles.negativeValue : null]}>
              {session.discountAmount > 0 ? `- ${currencyFormatter.format(session.discountAmount)}` : '0₫'}
            </Text>
          </View>
          <Divider style={styles.horizontalDivider} />
          <View style={[styles.summaryRow, styles.totalRow]}>
            <Text style={styles.totalLabel}>Khách cần thanh toán</Text>
            <Text style={styles.totalAmount}>{currencyFormatter.format(session.finalPrice)}</Text>
          </View>
        </Card.Content>
      </Card>
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  contentContainer: {
    padding: 16,
  },
  centerContent: {
    flexGrow: 1,
    justifyContent: 'center',
    padding: 24,
  },
  center: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f5f5f5',
  },
  loadingText: {
    marginTop: 16,
    fontSize: 16,
    color: '#666',
  },
  errorText: {
    fontSize: 16,
    color: '#d32f2f',
    textAlign: 'center',
    padding: 16,
    fontWeight: '600',
  },
  errorSubtext: {
    fontSize: 14,
    color: '#666',
    textAlign: 'center',
    paddingHorizontal: 16,
    marginTop: 8,
  },
  welcomeWrapper: {
    width: '100%',
  },
  welcomeCard: {
    paddingVertical: 16,
    borderRadius: 16,
    elevation: 2,
  },
  welcomeLogo: {
    width: 140,
    height: 60,
    alignSelf: 'center',
    marginBottom: 12,
  },
  welcomeTitle: {
    fontSize: 20,
    fontWeight: '700',
    textAlign: 'center',
    marginBottom: 8,
    color: '#111',
  },
  welcomeSubtitle: {
    fontSize: 15,
    color: '#555',
    textAlign: 'center',
    marginBottom: 16,
  },
  welcomeSteps: {
    backgroundColor: '#f4f6fb',
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
  },
  welcomeStep: {
    fontSize: 14,
    color: '#333',
    marginBottom: 8,
  },
  welcomeHint: {
    fontSize: 13,
    color: '#d14343',
    textAlign: 'center',
    marginTop: 4,
  },
  customerHeader: {
    alignItems: 'center',
    marginBottom: 16,
    paddingHorizontal: 12,
  },
  qrCard: {
    marginBottom: 16,
    elevation: 2,
  },
  qrContent: {
    paddingVertical: 16,
    alignItems: 'center',
  },
  qrImage: {
    width: 260,
    height: 260,
  },
  customerGreeting: {
    fontSize: 24,
    fontWeight: '700',
    color: '#0f172a',
  },
  customerSubtext: {
    fontSize: 15,
    color: '#475569',
    marginTop: 4,
    textAlign: 'center',
  },
  sessionPills: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
    marginTop: 12,
    justifyContent: 'center',
  },
  sessionChip: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 999,
    backgroundColor: '#e2e8f0',
    fontSize: 13,
    fontWeight: '600',
    color: '#0f172a',
  },
  placeholderText: {
    fontSize: 14,
    color: '#475569',
    textAlign: 'center',
  },
  amountLarge: {
    marginTop: 12,
    fontSize: 28,
    fontWeight: '800',
    color: '#2563eb',
  },
  itemsCard: {
    marginBottom: 16,
    elevation: 2,
  },
  itemCard: {
    borderWidth: 1,
    borderColor: '#ececec',
    borderRadius: 12,
    padding: 12,
    marginBottom: 12,
    backgroundColor: '#fff',
  },
  itemHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 6,
  },
  itemName: {
    fontSize: 15,
    fontWeight: '600',
    color: '#222',
    marginBottom: 2,
  },
  variantText: {
    fontSize: 12,
    color: '#777',
  },
  lineTotal: {
    fontSize: 16,
    fontWeight: '700',
    color: '#111',
  },
  itemMetaRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 4,
  },
  metaText: {
    fontSize: 13,
    color: '#555',
  },
  quantityChip: {
    fontSize: 13,
    fontWeight: '600',
    color: '#111',
    backgroundColor: '#f1f5f9',
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 999,
  },
  discountTag: {
    marginTop: 6,
    fontSize: 12,
    color: '#d84315',
    fontWeight: '600',
  },
  emptyText: {
    fontSize: 14,
    color: '#999',
    textAlign: 'center',
    paddingVertical: 16,
  },
  summaryCard: {
    marginBottom: 16,
    elevation: 2,
  },
  summaryRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 8,
  },
  summaryLabel: {
    fontSize: 15,
    color: '#666',
  },
  summaryValue: {
    fontSize: 15,
    color: '#333',
    fontWeight: '500',
  },
  negativeValue: {
    color: '#4caf50',
  },
  horizontalDivider: {
    marginVertical: 8,
  },
  totalRow: {
    borderTopWidth: 1,
    borderTopColor: '#e0e0e0',
    marginTop: 8,
    paddingTop: 12,
  },
  totalLabel: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
  },
  totalAmount: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#2563eb',
  },
})

