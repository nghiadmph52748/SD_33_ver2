import React, { useEffect, useState, useMemo } from 'react'
import { View, Text, StyleSheet, ScrollView, Image, Dimensions } from 'react-native'
import { useRoute, useNavigation, CommonActions } from '@react-navigation/native'
import { Card, Button, ActivityIndicator } from 'react-native-paper'
import { Client } from '@stomp/stompjs'
import env from '../../config/env'
import { getQRSession, QRSession, CartItem } from '../../api/qrSession'
import type { NativeStackNavigationProp } from '@react-navigation/native-stack'

const BANK_SHORT_CODE = 'MB'
const BANK_ACCOUNT_NUMBER = '288579542666'
const { width: SCREEN_WIDTH } = Dimensions.get('window')

type QRPaymentStackParamList = {
  'QR.Payment': { sessionId?: string }
  'QR.Success': { sessionId: string; paymentResult?: Record<string, string> }
}

type PaymentSuccessScreenRouteProp = {
  key: string
  name: 'QR.Success'
  params: { sessionId: string; paymentResult?: Record<string, string> }
}

type PaymentSuccessScreenNavigationProp = NativeStackNavigationProp<
  QRPaymentStackParamList,
  'QR.Success'
>

const currencyFormatter = new Intl.NumberFormat('vi-VN', {
  style: 'currency',
  currency: 'VND',
  maximumFractionDigits: 0,
})

export default function PaymentSuccessScreen() {
  const route = useRoute<PaymentSuccessScreenRouteProp>()
  const navigation = useNavigation<PaymentSuccessScreenNavigationProp>()
  const sessionId = route.params?.sessionId

  const [session, setSession] = useState<QRSession | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const wsUrl = useMemo(() => {
    try {
      const apiUrl = new URL(env.API_BASE_URL)
      const protocol = apiUrl.protocol === 'https:' ? 'wss:' : 'ws:'
      return `${protocol}//${apiUrl.host}/ws-chat`
    } catch {
      return null
    }
  }, [])

  // Check if sessionId is provided
  if (!sessionId) {
    return (
      <View style={styles.center}>
        <Text style={styles.errorIcon}>⚠️</Text>
        <Text style={styles.errorText}>Lỗi: Không có sessionId</Text>
      </View>
    )
  }

  useEffect(() => {
    if (!sessionId) return

    const loadSession = async () => {
      try {
        const data = await getQRSession(sessionId)
        setSession(data)
        setError(null)
      } catch (err) {
        const message = err instanceof Error ? err.message : 'Không thể tải thông tin phiên thanh toán'
        setError(message)
        console.error('Failed to load session:', err)
        
        // If session not found or cancelled, reset to main screen
        if (message.includes('Session not found') || message.includes('Không tìm thấy')) {
          setTimeout(() => {
            navigation.dispatch(
              CommonActions.reset({
                index: 0,
                routes: [
                  {
                    name: 'QR.Payment',
                    params: {},
                  },
                ],
              })
            )
          }, 1000)
        }
      } finally {
        setLoading(false)
      }
    }

    loadSession()
  }, [sessionId])

  // Reset to main screen function
  const resetToMainScreen = useMemo(() => {
    return () => {
      navigation.dispatch(
        CommonActions.reset({
          index: 0,
          routes: [
            {
              name: 'QR.Payment',
              params: {},
            },
          ],
        })
      )
    }
  }, [navigation])

  // Polling to check session status and reset if cancelled/expired (fallback if WebSocket fails)
  useEffect(() => {
    if (!sessionId) return

    let consecutiveErrors = 0
    const MAX_CONSECUTIVE_ERRORS = 2 // Reset after 2 consecutive errors (session likely deleted)

    const checkSessionStatus = async () => {
      try {
        const data = await getQRSession(sessionId)
        consecutiveErrors = 0 // Reset error counter on success
        // If session status changed to EXPIRED or CANCELLED, reset to main screen
        if (data.status === 'EXPIRED' || data.status === 'CANCELLED') {
          resetToMainScreen()
          return
        }
        setSession(data)
      } catch (err) {
        consecutiveErrors++
        // Session might be deleted, reset to main screen
        const message = err instanceof Error ? err.message : ''
        if (
          message.includes('Session not found') || 
          message.includes('Không tìm thấy') ||
          message.includes('not found') ||
          consecutiveErrors >= MAX_CONSECUTIVE_ERRORS
        ) {
          resetToMainScreen()
        }
      }
    }

    // Check immediately
    checkSessionStatus()

    // Then poll every 1 second (as fallback to WebSocket)
    const interval = setInterval(checkSessionStatus, 1000)

    return () => clearInterval(interval)
  }, [sessionId, resetToMainScreen])

  // Watch for session status EXPIRED and reset to welcome screen (same as QRPaymentScreen)
  useEffect(() => {
    if (session?.status === 'EXPIRED' || session?.status === 'CANCELLED') {
      resetToMainScreen()
    }
  }, [session?.status, resetToMainScreen])

  // WebSocket listener for session updates and reset signals
  useEffect(() => {
    if (!wsUrl || !sessionId) return

    const client = new Client({
      brokerURL: wsUrl,
      reconnectDelay: 5000,
      connectHeaders: {
        'x-qr-anon': 'true',
      },
      debug: (str) => {
        if (__DEV__) {
          console.log('[STOMP PaymentSuccess]', str)
        }
      },
      onConnect: () => {
        client.subscribe('/topic/qr-session', (frame) => {
          try {
            const payload = JSON.parse(frame.body) as { sessionId?: string; status?: string }
            
            // If this is the current session and it's been cancelled/expired, reset to main screen
            if (payload?.sessionId === sessionId) {
              if (payload.status === 'EXPIRED' || payload.status === 'CANCELLED') {
                setTimeout(() => {
                  resetToMainScreen()
                }, 100)
              }
            }
          } catch (err) {
            console.warn('[PaymentSuccess] Failed to parse QR session event', err)
          }
        })
      },
      onStompError: (frame) => {
        console.warn('[PaymentSuccess] STOMP error:', frame.body)
      },
      onWebSocketError: (event) => {
        console.error('[PaymentSuccess] WebSocket error:', event)
      },
    })

    client.activate()

    return () => {
      client.deactivate()
    }
  }, [wsUrl, sessionId, resetToMainScreen])

  const getProductDisplayName = (item: CartItem) => {
    const parts = [item.productName]
    if (item.tenMauSac) parts.push(item.tenMauSac)
    if (item.tenKichThuoc) parts.push(`Size ${item.tenKichThuoc}`)
    return parts.join(' - ')
  }

  const formatDate = (dateString: string) => {
    try {
      const date = new Date(dateString)
      return new Intl.DateTimeFormat('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
      }).format(date)
    } catch {
      return dateString
    }
  }

  const qrImageUrl = React.useMemo(() => {
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

  if (loading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" />
        <Text style={styles.loadingText}>Đang tải...</Text>
      </View>
    )
  }

  if (error || !session) {
    return (
      <View style={styles.center}>
        <Text style={styles.errorIcon}>⚠️</Text>
        <Text style={styles.errorText}>{error || 'Không tìm thấy thông tin thanh toán'}</Text>
      </View>
    )
  }

  return (
    <View style={styles.container}>
      <ScrollView 
        style={styles.scrollView} 
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={true}
      >
        {/* Success Icon & Message */}
        <Card style={styles.successCard}>
          <Card.Content style={styles.successContent}>
            <View style={styles.iconContainer}>
              <Text style={styles.successIcon}>{session.status === 'PAID' ? '✓' : '📱'}</Text>
            </View>
            <Text style={styles.successTitle}>
              {session.status === 'PAID' ? 'Thanh toán thành công!' : 'Mã QR thanh toán'}
            </Text>
            <Text style={styles.successSubtitle}>
              {session.status === 'PAID' ? 'Cảm ơn bạn đã thanh toán' : 'Vui lòng quét mã QR để thanh toán'}
            </Text>
          </Card.Content>
        </Card>

        {/* Customer Info */}
        {session.customerName && (
          <Card style={styles.customerCard}>
            <Card.Title title="Thông tin khách hàng" />
            <Card.Content>
              <View style={styles.infoRow}>
                <Text style={styles.infoLabel}>Tên khách hàng:</Text>
                <Text style={styles.infoValue}>{session.customerName}</Text>
              </View>
              {session.customerPhone && (
                <View style={styles.infoRow}>
                  <Text style={styles.infoLabel}>Số điện thoại:</Text>
                  <Text style={styles.infoValue}>{session.customerPhone}</Text>
                </View>
              )}
              {session.customerEmail && (
                <View style={styles.infoRow}>
                  <Text style={styles.infoLabel}>Email:</Text>
                  <Text style={styles.infoValue}>{session.customerEmail}</Text>
                </View>
              )}
            </Card.Content>
          </Card>
        )}

        {/* Order Info */}
        <Card style={styles.infoCard}>
        <Card.Title title="Thông tin đơn hàng" />
        <Card.Content>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Mã đơn hàng:</Text>
            <Text style={styles.infoValue}>{session.orderCode}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Số tiền đã thanh toán:</Text>
            <Text style={[styles.infoValue, styles.amountValue]}>
              {currencyFormatter.format(session.finalPrice)}
            </Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Thời gian thanh toán:</Text>
            <Text style={styles.infoValue}>{formatDate(session.createdAt)}</Text>
          </View>
        </Card.Content>
      </Card>

      {/* Products List */}
      <Card style={styles.itemsCard}>
        <Card.Title title="Sản phẩm đã mua" />
        <Card.Content>
          {session.items && session.items.length > 0 ? (
            session.items.map((item, index) => (
              <View key={index} style={styles.itemRow}>
                <View style={styles.itemInfo}>
                  <Text style={styles.itemName}>{getProductDisplayName(item)}</Text>
                  <Text style={styles.itemQuantity}>Số lượng: {item.quantity}</Text>
                  <Text style={styles.itemPrice}>
                    {currencyFormatter.format(item.price * item.quantity)}
                  </Text>
                </View>
              </View>
            ))
          ) : (
            <Text style={styles.emptyText}>Không có sản phẩm</Text>
          )}
        </Card.Content>
      </Card>

      {/* Summary */}
      <Card style={styles.summaryCard}>
        <Card.Title title="Tổng thanh toán" />
        <Card.Content>
          <View style={styles.summaryRow}>
            <Text style={styles.summaryLabel}>Tạm tính:</Text>
            <Text style={styles.summaryValue}>{currencyFormatter.format(session.subtotal)}</Text>
          </View>
          {session.discountAmount > 0 && (
            <View style={styles.summaryRow}>
              <Text style={styles.summaryLabel}>Giảm giá:</Text>
              <Text style={[styles.summaryValue, styles.discount]}>
                -{currencyFormatter.format(session.discountAmount)}
              </Text>
            </View>
          )}
          {session.shippingFee > 0 && (
            <View style={styles.summaryRow}>
              <Text style={styles.summaryLabel}>Phí vận chuyển:</Text>
              <Text style={styles.summaryValue}>{currencyFormatter.format(session.shippingFee)}</Text>
            </View>
          )}
          <View style={[styles.summaryRow, styles.totalRow]}>
            <Text style={styles.totalLabel}>Tổng cộng:</Text>
            <Text style={styles.totalAmount}>
              {currencyFormatter.format(session.finalPrice)}
            </Text>
          </View>
        </Card.Content>
      </Card>
      </ScrollView>

      {/* QR Code - Fixed at bottom - Only show for PENDING status */}
      {session.status === 'PENDING' && (
        <View style={styles.qrContainer}>
          <Card style={styles.qrCard}>
            <Card.Content style={styles.qrContent}>
              <Text style={styles.qrTitle}>Mã QR thanh toán</Text>
              {qrImageUrl ? (
                <Image source={{ uri: qrImageUrl }} style={styles.qrImage} resizeMode="contain" />
              ) : (
                <Text style={styles.placeholderText}>Đang tải mã QR...</Text>
              )}
            </Card.Content>
          </Card>
        </View>
      )}
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  scrollView: {
    flex: 1,
  },
  scrollContent: {
    padding: 16,
    paddingBottom: 8,
  },
  center: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f5f5f5',
    padding: 16,
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
    marginTop: 16,
    marginBottom: 24,
  },
  successCard: {
    marginBottom: 16,
    elevation: 2,
    backgroundColor: '#f1f8f4',
  },
  successContent: {
    alignItems: 'center',
    paddingVertical: 32,
  },
  iconContainer: {
    marginBottom: 16,
  },
  successIcon: {
    fontSize: 80,
    color: '#4caf50',
    fontWeight: 'bold',
  },
  errorIcon: {
    fontSize: 64,
    marginBottom: 16,
  },
  successTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#4caf50',
    marginBottom: 8,
  },
  successSubtitle: {
    fontSize: 16,
    color: '#666',
  },
  customerCard: {
    marginBottom: 16,
    elevation: 2,
    backgroundColor: '#f0f7ff',
  },
  infoCard: {
    marginBottom: 16,
    elevation: 2,
  },
  infoRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
  },
  infoLabel: {
    fontSize: 15,
    color: '#666',
    flex: 1,
  },
  infoValue: {
    fontSize: 15,
    color: '#333',
    fontWeight: '500',
    flex: 1,
    textAlign: 'right',
  },
  amountValue: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#2563eb',
  },
  itemsCard: {
    marginBottom: 16,
    elevation: 2,
  },
  itemRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
  },
  itemInfo: {
    flex: 1,
  },
  itemName: {
    fontSize: 15,
    fontWeight: '500',
    color: '#333',
    marginBottom: 4,
  },
  itemQuantity: {
    fontSize: 13,
    color: '#666',
    marginBottom: 4,
  },
  itemPrice: {
    fontSize: 14,
    fontWeight: '600',
    color: '#2563eb',
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
  discount: {
    color: '#4caf50',
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
  qrContainer: {
    backgroundColor: '#fff',
    borderTopWidth: 1,
    borderTopColor: '#e0e0e0',
    paddingTop: 12,
    paddingBottom: 16,
    paddingHorizontal: 16,
    elevation: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: -2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  qrCard: {
    elevation: 2,
  },
  qrContent: {
    alignItems: 'center',
    paddingVertical: 12,
  },
  qrTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
    marginBottom: 12,
  },
  qrImage: {
    width: Math.min(SCREEN_WIDTH - 80, 240),
    height: Math.min(SCREEN_WIDTH - 80, 240),
  },
  placeholderText: {
    fontSize: 14,
    color: '#999',
    textAlign: 'center',
    paddingVertical: 20,
  },
})

