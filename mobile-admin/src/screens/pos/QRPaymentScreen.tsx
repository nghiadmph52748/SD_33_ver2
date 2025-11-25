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

export default function QRPaymentScreen() {
  const route = useRoute<QRPaymentScreenRouteProp>()
  const navigation = useNavigation<QRPaymentScreenNavigationProp>()

  const [sessionId, setSessionId] = useState<string | null>(route.params?.sessionId ?? null)
  const [session, setSession] = useState<QRSession | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [initializing, setInitializing] = useState(!route.params?.sessionId)

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
        if (latest && latest.qrSessionId !== sessionId) {
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

  const qrImageUrl = useMemo(() => {
    // Only build QR when backend says staff has chosen to display it (qrCodeUrl present)
    if (!session?.finalPrice || !session.orderCode || !session.qrCodeUrl) return null
    try {
      const amount = Math.round(session.finalPrice)
      const info = encodeURIComponent(`${session.orderCode}`)
      // VietQR static image for MB Bank account
      return `https://img.vietqr.io/image/${BANK_SHORT_CODE}-${BANK_ACCOUNT_NUMBER}-qr_only.png?amount=${amount}&addInfo=${info}`
    } catch {
      return null
    }
  }, [session?.finalPrice, session?.orderCode, session?.qrCodeUrl])

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
            <Text style={styles.welcomeEmoji}>🤝</Text>
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

  const hasQrCode = Boolean(qrImageUrl)

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
      {hasQrCode ? (
        <Card style={styles.qrCard}>
          <Card.Content style={styles.qrContent}>
            <Text style={styles.title}>Thanh toán</Text>
            <Text style={styles.subtitle}>Quét bằng app ngân hàng để hoàn tất giao dịch</Text>

            <View style={styles.qrContainer}>
              {qrImageUrl ? (
                <Image source={{ uri: qrImageUrl }} style={styles.qrImage} resizeMode="contain" />
              ) : (
                <Text style={styles.errorText}>Không thể tạo mã QR thanh toán</Text>
              )}
            </View>

            <Text style={styles.amount}>{currencyFormatter.format(session.finalPrice)}</Text>
            <Text style={styles.orderCode}>Mã đơn hàng: {session.orderCode}</Text>
            {session.status === 'PENDING' ? (
              <View style={styles.statusContainer}>
                <ActivityIndicator size="small" style={styles.statusIndicator} />
                <Text style={styles.statusText}>Đang chờ khách thanh toán</Text>
              </View>
            ) : (
              <Text
                style={[
                  styles.statusBadge,
                  session.status === 'PAID'
                    ? styles.statusPaid
                    : session.status === 'EXPIRED'
                      ? styles.statusExpired
                      : styles.statusPending,
                ]}
              >
                {session.status}
              </Text>
            )}
            <Text style={styles.createdText}>Tạo lúc: {formattedDate}</Text>
          </Card.Content>
        </Card>
      ) : (
        <Card style={styles.qrPlaceholderCard}>
          <Card.Content style={styles.qrPlaceholderContent}>
            <Text style={styles.placeholderTitle}>Vui lòng chờ mã QR từ nhân viên...</Text>
          </Card.Content>
        </Card>
      )}

      <Card style={styles.itemsCard}>
        <Card.Title title="Giỏ hàng tại quầy" />
        <Card.Content>
          <View style={styles.tableHeader}>
            <Text style={[styles.cellProduct, styles.tableHeaderText]}>Sản phẩm</Text>
            <Text style={[styles.cellQty, styles.tableHeaderText]}>SL</Text>
            <Text style={[styles.cellPrice, styles.tableHeaderText]}>Đơn giá</Text>
            <Text style={[styles.cellTotal, styles.tableHeaderText]}>Thành tiền</Text>
          </View>
          {session.items && session.items.length > 0 ? (
            session.items.map((item, index) => {
              const unitPrice = getUnitPrice(item)
              const lineTotal = unitPrice * item.quantity
              return (
                <View key={`${item.productId}-${index}`} style={styles.tableRow}>
                  <View style={styles.cellProduct}>
                    <Text style={styles.itemName}>{getProductDisplayName(item)}</Text>
                    {getVariantText(item) ? <Text style={styles.variantText}>{getVariantText(item)}</Text> : null}
                    {item.discount ? (
                      <Text style={styles.discountTag}>Giảm {item.discount}%</Text>
                    ) : null}
                  </View>
                  <Text style={[styles.cellQty, styles.tableCellText]}>x{item.quantity}</Text>
                  <Text style={[styles.cellPrice, styles.tableCellText]}>{currencyFormatter.format(unitPrice)}</Text>
                  <Text style={[styles.cellTotal, styles.tableCellText]}>{currencyFormatter.format(lineTotal)}</Text>
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
          <View style={styles.summaryRow}>
            <Text style={styles.summaryLabel}>Phí vận chuyển</Text>
            <Text style={styles.summaryValue}>{currencyFormatter.format(session.shippingFee)}</Text>
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
  welcomeEmoji: {
    fontSize: 42,
    textAlign: 'center',
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
  qrCard: {
    marginBottom: 16,
    elevation: 2,
  },
  qrContent: {
    paddingVertical: 24,
    alignItems: 'center',
  },
  panelTitle: {
    fontSize: 16,
    fontWeight: '700',
    marginBottom: 12,
    color: '#111',
  },
  infoRow: {
    marginBottom: 10,
  },
  infoLabel: {
    fontSize: 13,
    color: '#666',
    marginBottom: 4,
  },
  infoValue: {
    fontSize: 15,
    color: '#111',
    fontWeight: '500',
  },
  title: {
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 8,
    color: '#333',
  },
  subtitle: {
    fontSize: 14,
    color: '#666',
    marginBottom: 24,
  },
  createdText: {
    marginTop: 12,
    fontSize: 13,
    color: '#777',
  },
  qrContainer: {
    width: 280,
    height: 280,
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#e0e0e0',
  },
  qrImage: {
    width: '100%',
    height: '100%',
  },
  qrPlaceholder: {
    alignItems: 'center',
  },
  qrPlaceholderCard: {
    marginBottom: 16,
    borderStyle: 'dashed',
    borderWidth: 1,
    borderColor: '#d6d3d1',
    backgroundColor: '#fafafa',
  },
  qrPlaceholderContent: {
    paddingVertical: 24,
  },
  placeholderTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: '#444',
    marginBottom: 8,
  },
  placeholderText: {
    fontSize: 14,
    color: '#777',
    lineHeight: 20,
  },
  qrPlaceholderText: {
    marginTop: 12,
    fontSize: 14,
    color: '#666',
  },
  orderCode: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
    marginBottom: 8,
  },
  amount: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#2563eb',
    marginBottom: 16,
  },
  statusContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 8,
  },
  statusIndicator: {
    marginRight: 8,
  },
  statusText: {
    fontSize: 14,
    color: '#ff9800',
    fontWeight: '500',
  },
  statusBadge: {
    fontSize: 13,
    fontWeight: '600',
    textTransform: 'uppercase',
  },
  statusPaid: {
    color: '#2e7d32',
  },
  statusExpired: {
    color: '#f57c00',
  },
  statusPending: {
    color: '#0d47a1',
  },
  itemsCard: {
    marginBottom: 16,
    elevation: 2,
  },
  tableHeader: {
    flexDirection: 'row',
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: '#ececec',
  },
  tableHeaderText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#666',
    textTransform: 'uppercase',
  },
  tableRow: {
    flexDirection: 'row',
    paddingVertical: 14,
    borderBottomWidth: 1,
    borderBottomColor: '#f4f4f4',
  },
  tableCellText: {
    fontSize: 14,
    color: '#111',
  },
  cellProduct: {
    flex: 2.6,
    paddingRight: 8,
  },
  cellQty: {
    flex: 0.5,
    textAlign: 'center',
  },
  cellPrice: {
    flex: 1,
    textAlign: 'right',
  },
  cellTotal: {
    flex: 1,
    textAlign: 'right',
    fontWeight: '600',
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

