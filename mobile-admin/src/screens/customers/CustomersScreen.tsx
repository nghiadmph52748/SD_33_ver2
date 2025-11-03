import { useFocusEffect, useNavigation } from '@react-navigation/native'
import React, { useCallback, useMemo, useState } from 'react'
import { FlatList, RefreshControl, StyleSheet, View } from 'react-native'
import { Avatar, List, Text } from 'react-native-paper'

import { CustomerModel, fetchCustomers } from '../../api/customers'
import { ListEmpty, SearchInput } from '../../components'
import { SCREENS } from '../../constants/routes'
import type { CustomersStackParamList } from '../../navigation/CustomersNavigator'
import type { NativeStackNavigationProp } from '@react-navigation/native-stack'

const CustomersScreen: React.FC = () => {
  const navigation = useNavigation<NativeStackNavigationProp<CustomersStackParamList>>()
  const [query, setQuery] = useState('')
  const [customers, setCustomers] = useState<CustomerModel[]>([])
  const [isLoading, setIsLoading] = useState(false)

  const loadCustomers = useCallback(async () => {
    try {
      setIsLoading(true)
      const data = await fetchCustomers()
      setCustomers(data)
    } finally {
      setIsLoading(false)
    }
  }, [])

  useFocusEffect(
    useCallback(() => {
      loadCustomers()
    }, [loadCustomers])
  )

  const filteredCustomers = useMemo(() => {
    if (!query) return customers
    const normalized = query.toLowerCase()
    return customers.filter((customer) => {
      const name = customer.tenKhachHang ?? ''
      const email = customer.email ?? ''
      return name.toLowerCase().includes(normalized) || email.toLowerCase().includes(normalized)
    })
  }, [customers, query])

  return (
    <View style={styles.container}>
      <SearchInput query={query} onChangeQuery={setQuery} placeholder="Tìm theo tên hoặc email" />

      <FlatList
        data={filteredCustomers}
        keyExtractor={(item) => item.id.toString()}
        refreshControl={<RefreshControl refreshing={isLoading} onRefresh={loadCustomers} />}
        ListEmptyComponent={!isLoading ? <ListEmpty title="Chưa có khách hàng" /> : null}
        renderItem={({ item }) => (
          <List.Item
            title={item.tenKhachHang}
            description={`${item.email} • ${item.soDienThoai}`}
            onPress={() => navigation.navigate(SCREENS.STACK.CUSTOMER_DETAILS, { customerId: item.id })}
            left={() => (
              <Avatar.Text
                label={item.tenKhachHang?.substring(0, 2).toUpperCase() ?? 'KH'}
                size={42}
                style={styles.avatar}
              />
            )}
            right={() => (
              <View style={styles.infoRight}>
                <Text style={styles.totalOrder}>{item.tongDon} đơn</Text>
                <Text style={styles.segment}>{item.phanLoaiText ?? 'Chưa phân loại'}</Text>
              </View>
            )}
          />
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
    paddingBottom: 32,
  },
  avatar: {
    backgroundColor: '#f97316',
  },
  infoRight: {
    alignItems: 'flex-end',
  },
  totalOrder: {
    fontWeight: '600',
  },
  segment: {
    color: '#64748b',
    fontSize: 12,
  },
})

export default CustomersScreen
