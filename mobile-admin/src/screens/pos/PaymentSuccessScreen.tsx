import React, { useEffect, useState } from 'react'
import { View, Text, StyleSheet, ScrollView } from 'react-native'
import { useRoute, useNavigation } from '@react-navigation/native'
import { Card, Button, ActivityIndicator } from 'react-native-paper'
import { getQRSession, QRSession, CartItem } from '../../api/qrSession'
import type { NativeStackNavigationProp } from '@react-navigation/native-stack'

type QRPaymentStackParamList = {
  'QR.Payment': { sessionId?: string }
  'QR.Success': { sessionId: string }
}

type PaymentSuccessScreenRouteProp = {
  key: string
  name: 'QR.Success'
  params: { sessionId: string }
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

  // Check if sessionId is provided
  if (!sessionId) {
    return (
      <View style={styles.center}>
        <Text style={styles.errorIcon}>⚠️</Text>
        <Text style={styles.errorText}>Lỗi: Không có sessionId</Text>
        <Button
          mode="contained"
          onPress={() => navigation.goBack()}
          style={styles.backButton}
        >
          Quay lại
        </Button>
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
      } finally {
        setLoading(false)
      }
    }

    loadSession()
  }, [sessionId])

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
        <Button
          mode="contained"
          onPress={() => navigation.goBack()}
          style={styles.backButton}
        >
          Quay lại
        </Button>
      </View>
    )
  }

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
      {/* Success Icon & Message */}
      <Card style={styles.successCard}>
        <Card.Content style={styles.successContent}>
          <View style={styles.iconContainer}>
            <Text style={styles.successIcon}>✓</Text>
          </View>
          <Text style={styles.successTitle}>Thanh toán thành công!</Text>
          <Text style={styles.successSubtitle}>Cảm ơn bạn đã thanh toán</Text>
        </Card.Content>
      </Card>

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

      {/* Back Button */}
      <Button
        mode="contained"
        onPress={() => navigation.goBack()}
        style={styles.backButton}
        contentStyle={styles.backButtonContent}
      >
        Quay lại
      </Button>
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
  backButton: {
    marginTop: 8,
    marginBottom: 16,
  },
  backButtonContent: {
    paddingVertical: 8,
  },
})

