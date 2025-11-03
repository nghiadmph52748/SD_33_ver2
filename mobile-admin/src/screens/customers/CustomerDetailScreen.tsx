import { RouteProp, useRoute } from '@react-navigation/native'
import React, { useEffect, useState } from 'react'
import { ScrollView, StyleSheet, View } from 'react-native'
import { Card, Divider, List, Text } from 'react-native-paper'

import { CustomerModel, fetchCustomerById } from '../../api/customers'
import { LoadingOverlay } from '../../components'
import { SCREENS } from '../../constants/routes'
import { CustomersStackParamList } from '../../navigation/CustomersNavigator'

const currencyFormatter = new Intl.NumberFormat('vi-VN', {
  style: 'currency',
  currency: 'VND',
  maximumFractionDigits: 0,
})

const CustomerDetailScreen: React.FC = () => {
  const route = useRoute<RouteProp<CustomersStackParamList, typeof SCREENS.STACK.CUSTOMER_DETAILS>>()
  const [customer, setCustomer] = useState<CustomerModel | null>(null)
  const [isLoading, setIsLoading] = useState(false)

  useEffect(() => {
    const loadCustomer = async () => {
      try {
        setIsLoading(true)
        const data = await fetchCustomerById(route.params.customerId)
        setCustomer(data)
      } finally {
        setIsLoading(false)
      }
    }

    loadCustomer()
  }, [route.params.customerId])

  if (isLoading || !customer) {
    return <LoadingOverlay message="Đang tải khách hàng" />
  }

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Card style={styles.card}>
        <Card.Title title={customer.tenKhachHang} subtitle={customer.email} />
        <Card.Content>
          <List.Section>
            <List.Subheader>Thông tin liên hệ</List.Subheader>
            <List.Item title="Số điện thoại" description={customer.soDienThoai} left={(props) => <List.Icon {...props} icon="phone" />} />
            <List.Item
              title="Giới tính"
              description={customer.gioiTinh ? 'Nam' : 'Nữ'}
              left={(props) => <List.Icon {...props} icon="gender-male-female" />}
            />
            <List.Item title="Ngày sinh" description={customer.ngaySinh} left={(props) => <List.Icon {...props} icon="calendar" />} />
          </List.Section>

          <Divider style={styles.divider} />

          <List.Section>
            <List.Subheader>Hiệu suất</List.Subheader>
            <List.Item
              title="Tổng đơn"
              description={`${customer.tongDon} đơn hàng`}
              left={(props) => <List.Icon {...props} icon="clipboard-text" />}
            />
            <List.Item
              title="Tổng chi tiêu"
              description={currencyFormatter.format(customer.tongChiTieu ?? 0)}
              left={(props) => <List.Icon {...props} icon="cash" />}
            />
            <List.Item
              title="Phân loại"
              description={customer.phanLoaiText ?? 'Chưa phân loại'}
              left={(props) => <List.Icon {...props} icon="star" />}
            />
          </List.Section>

          <Divider style={styles.divider} />

          <List.Section>
            <List.Subheader>Địa chỉ</List.Subheader>
            {customer.listDiaChi?.length ? (
              customer.listDiaChi.map((address, index) => (
                <List.Item
                  key={`${address.diaChiCuThe}-${index}`}
                  title={address.tenDiaChi ?? `Địa chỉ ${index + 1}`}
                  description={`${address.diaChiCuThe}, ${address.phuong}, ${address.quan}, ${address.thanhPho}`}
                  left={(props) => <List.Icon {...props} icon={address.macDinh ? 'map-marker-check' : 'map-marker'} />}
                />
              ))
            ) : (
              <Text>Chưa có địa chỉ</Text>
            )}
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
    paddingBottom: 32,
  },
  card: {
    borderRadius: 16,
  },
  divider: {
    marginVertical: 12,
  },
})

export default CustomerDetailScreen
