import { useNavigation, useFocusEffect } from '@react-navigation/native'
import React, { useCallback, useMemo, useState } from 'react'
import { FlatList, RefreshControl, StyleSheet, View } from 'react-native'
import { List, Text } from 'react-native-paper'

import { fetchOrders, OrderModel } from '../../api/orders'
import { SearchInput, ListEmpty } from '../../components'
import { SCREENS } from '../../constants/routes'
import type { OrdersStackParamList } from '../../navigation/OrdersNavigator'
import type { NativeStackNavigationProp } from '@react-navigation/native-stack'

const currencyFormatter = new Intl.NumberFormat('vi-VN', {
  style: 'currency',
  currency: 'VND',
  maximumFractionDigits: 0,
})

const OrdersScreen: React.FC = () => {
  const navigation = useNavigation<NativeStackNavigationProp<OrdersStackParamList>>()
  const [query, setQuery] = useState('')
  const [orders, setOrders] = useState<OrderModel[]>([])
  const [isLoading, setIsLoading] = useState(false)

  const loadOrders = useCallback(async () => {
    try {
      setIsLoading(true)
      const data = await fetchOrders()
      setOrders(data)
    } finally {
      setIsLoading(false)
    }
  }, [])

  useFocusEffect(
    useCallback(() => {
      loadOrders()
    }, [loadOrders])
  )

  const filteredOrders = useMemo(() => {
    if (!query) return orders
    const normalized = query.toLowerCase()
    return orders.filter((order) => {
      const orderCode = order.maPhieuGiamGia ?? ''
      const receiver = order.tenNguoiNhan ?? ''
      return orderCode.toLowerCase().includes(normalized) || receiver.toLowerCase().includes(normalized)
    })
  }, [orders, query])

  return (
    <View style={styles.container}>
      <SearchInput
        query={query}
        onChangeQuery={setQuery}
        placeholder="Tìm theo mã đơn hoặc tên khách hàng"
      />

      <FlatList
        data={filteredOrders}
        keyExtractor={(item) => item.id.toString()}
        refreshControl={<RefreshControl refreshing={isLoading} onRefresh={loadOrders} />}
        ListEmptyComponent={!isLoading ? <ListEmpty title="Chưa có đơn hàng" /> : null}
        showsVerticalScrollIndicator={false}
        renderItem={({ item }) => (
          <View style={styles.listItem}>
            <List.Item
              title={`Đơn hàng #${item.id}`}
              description={`Khách hàng: ${item.tenNguoiNhan ?? 'N/A'}`}
              onPress={() => navigation.navigate(SCREENS.STACK.ORDER_DETAILS, { orderId: item.id })}
              right={(props) => (
                <View style={styles.itemRight}>
                  <Text style={styles.amount}>
                    {currencyFormatter.format(item.tongTienSauGiam ?? item.tongTien ?? 0)}
                  </Text>
                  <Text style={styles.date}>
                    {item.ngayTao ? new Date(item.ngayTao).toLocaleDateString('vi-VN') : '—'}
                  </Text>
                </View>
              )}
              style={styles.listItemInner}
            />
          </View>
        )}
        contentContainerStyle={styles.listContent}
      />
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
    paddingLeft: 8,
    paddingRight: 8,
  },
  itemRight: {
    alignItems: 'flex-end',
    justifyContent: 'center',
    marginRight: 8,
  },
  amount: {
    fontWeight: '600',
    fontSize: 15,
  },
  date: {
    color: '#64748b',
    fontSize: 12,
    marginTop: 2,
  },
})

export default OrdersScreen
