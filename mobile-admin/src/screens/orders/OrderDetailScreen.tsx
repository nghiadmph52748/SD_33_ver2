import { RouteProp, useRoute } from '@react-navigation/native'
import React, { useEffect, useState } from 'react'
import { ScrollView, StyleSheet, View } from 'react-native'
import { Card, Divider, List, Text } from 'react-native-paper'

import { fetchOrderById, OrderModel } from '../../api/orders'
import { LoadingOverlay } from '../../components'
import { SCREENS } from '../../constants/routes'
import { OrdersStackParamList } from '../../navigation/OrdersNavigator'

const currencyFormatter = new Intl.NumberFormat('vi-VN', {
  style: 'currency',
  currency: 'VND',
  maximumFractionDigits: 0,
})

const OrderDetailScreen: React.FC = () => {
  const route = useRoute<RouteProp<OrdersStackParamList, typeof SCREENS.STACK.ORDER_DETAILS>>()
  const [order, setOrder] = useState<OrderModel | null>(null)
  const [isLoading, setIsLoading] = useState(false)

  useEffect(() => {
    const loadOrder = async () => {
      try {
        setIsLoading(true)
        const data = await fetchOrderById(route.params.orderId)
        setOrder(data)
      } finally {
        setIsLoading(false)
      }
    }

    loadOrder()
  }, [route.params.orderId])

  if (isLoading || !order) {
    return <LoadingOverlay message="Đang tải chi tiết đơn hàng" />
  }

  const createdAt = order.ngayTao ? new Date(order.ngayTao).toLocaleString('vi-VN') : '—'

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Card style={styles.card}>
        <Card.Title title={`Đơn hàng #${order.id}`} subtitle={`Ngày tạo: ${createdAt}`} />
        <Card.Content>
          <List.Section>
            <List.Subheader>Thông tin nhận hàng</List.Subheader>
            <List.Item title="Khách hàng" description={order.tenNguoiNhan ?? '—'} left={(props) => <List.Icon {...props} icon="account" />} />
            <List.Item title="Email" description={order.emailNguoiNhan ?? '—'} left={(props) => <List.Icon {...props} icon="email" />} />
            <List.Item title="Số điện thoại" description={order.soDienThoaiNguoiNhan ?? '—'} left={(props) => <List.Icon {...props} icon="phone" />} />
            <List.Item title="Địa chỉ" description={order.diaChiNhanHang ?? '—'} left={(props) => <List.Icon {...props} icon="home" />} />
          </List.Section>

          <Divider style={styles.divider} />

          <List.Section>
            <List.Subheader>Thanh toán</List.Subheader>
            <List.Item
              title="Tổng tiền"
              description={currencyFormatter.format(order.tongTien ?? 0)}
              left={(props) => <List.Icon {...props} icon="cash" />}
            />
            <List.Item
              title="Sau giảm"
              description={currencyFormatter.format(order.tongTienSauGiam ?? order.tongTien ?? 0)}
              left={(props) => <List.Icon {...props} icon="sale" />}
            />
          </List.Section>

          <Divider style={styles.divider} />

          <List.Section>
            <List.Subheader>Khác</List.Subheader>
            <List.Item
              title="Nhân viên phụ trách"
              description={order.tenNhanVien ?? '—'}
              left={(props) => <List.Icon {...props} icon="account-tie" />}
            />
            <List.Item
              title="Ghi chú"
              description={order.ghiChu ?? 'Không có ghi chú'}
              left={(props) => <List.Icon {...props} icon="note-text" />}
            />
          </List.Section>
        </Card.Content>
      </Card>
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8fafc',
  },
  content: {
    padding: 16,
    paddingBottom: 100,
  },
  card: {
    borderRadius: 16,
  },
  divider: {
    marginVertical: 12,
  },
})

export default OrderDetailScreen
