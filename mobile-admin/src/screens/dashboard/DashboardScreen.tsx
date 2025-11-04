import { useNavigation } from '@react-navigation/native'
import React, { useEffect, useState } from 'react'
import { RefreshControl, ScrollView, StyleSheet, View } from 'react-native'
import { Button, Card, Divider, List, Text } from 'react-native-paper'
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons'

import { fetchDashboardSummary, DashboardSummary } from '../../api/dashboard'
import { SCREENS } from '../../constants/routes'
import { MetricCard, SectionHeader } from '../../components'
import type { DashboardStackParamList } from '../../navigation/DashboardNavigator'
import type { NativeStackNavigationProp } from '@react-navigation/native-stack'

const currencyFormatter = new Intl.NumberFormat('vi-VN', {
  style: 'currency',
  currency: 'VND',
  maximumFractionDigits: 0,
})

const DashboardScreen: React.FC = () => {
  const navigation = useNavigation<NativeStackNavigationProp<DashboardStackParamList>>()
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
  const [isLoading, setIsLoading] = useState(false)

  const loadSummary = async () => {
    try {
      setIsLoading(true)
      const data = await fetchDashboardSummary()
      setSummary(data)
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    loadSummary()
  }, [])

  const metrics = summary?.metrics

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.contentContainer}
      refreshControl={<RefreshControl refreshing={isLoading} onRefresh={loadSummary} />}
      showsVerticalScrollIndicator={false}
    >
      <View style={styles.header}>
        <Text variant="titleLarge" style={styles.title}>
          Xin chào!
        </Text>
        <Text variant="bodyMedium" style={styles.subtitle}>
          Cùng theo dõi hoạt động kinh doanh hôm nay.
        </Text>
      </View>

      <Card style={styles.aiCard} onPress={() => navigation.navigate(SCREENS.STACK.AI_ASSISTANT)}>
        <Card.Content style={styles.aiCardContent}>
          <View style={styles.aiCardHeader}>
            <View style={styles.aiIcon}>
              <MaterialCommunityIcons name="robot" size={28} color="#fff" />
            </View>
            <View style={styles.aiCardText}>
              <Text variant="titleMedium" style={styles.aiTitle}>
                AI Assistant
              </Text>
              <Text variant="bodySmall" style={styles.aiDescription}>
                Hỏi AI về doanh thu, đơn hàng, sản phẩm và phân tích kinh doanh
              </Text>
            </View>
          </View>
          <Button
            mode="contained"
            icon="arrow-right"
            contentStyle={styles.aiButtonContent}
            labelStyle={styles.aiButtonLabel}
            onPress={() => navigation.navigate(SCREENS.STACK.AI_ASSISTANT)}
          >
            Bắt đầu trò chuyện
          </Button>
        </Card.Content>
      </Card>

      <View style={styles.metricRow}>
        <MetricCard
          title="Tổng doanh thu"
          value={metrics ? currencyFormatter.format(metrics.totalRevenue) : '—'}
          accentColor="#2563eb"
        />
        <MetricCard
          title="Đơn hàng"
          value={metrics ? metrics.totalOrders.toString() : '—'}
          accentColor="#10b981"
        />
      </View>
      <View style={styles.metricRow}>
        <MetricCard
          title="Giá trị TB/đơn"
          value={metrics ? currencyFormatter.format(metrics.averageOrderValue) : '—'}
          accentColor="#f97316"
        />
        <MetricCard
          title="Khách hàng hoạt động"
          value={metrics ? metrics.activeCustomers.toString() : '—'}
          accentColor="#8b5cf6"
        />
      </View>
      <View style={styles.metricRow}>
        <MetricCard
          title="Tồn kho"
          value={metrics ? metrics.inventoryCount.toString() : '—'}
          accentColor="#ec4899"
        />
        <View style={styles.placeholderCard} />
      </View>

      <SectionHeader title="Đơn hàng gần đây" />
      <Card style={styles.card}>
        {summary && summary.recentOrders.length > 0 ? (
          summary.recentOrders.map((order) => (
            <React.Fragment key={order.id}>
              <List.Item
                title={`Đơn #${order.id}`}
                description={`Khách hàng: ${order.tenNguoiNhan ?? 'N/A'}`}
                right={() => (
                  <View style={styles.listRight}>
                    <Text style={styles.orderAmount}>
                      {currencyFormatter.format(order.tongTienSauGiam ?? order.tongTien ?? 0)}
                    </Text>
                    <Text style={styles.orderDate}>
                      {order.ngayTao ? new Date(order.ngayTao).toLocaleDateString('vi-VN') : '—'}
                    </Text>
                  </View>
                )}
              />
              <Divider inset leadingInset={16} />
            </React.Fragment>
          ))
        ) : (
          <List.Item title="Không có dữ liệu" description="Chưa có đơn hàng trong 7 ngày gần nhất." />
        )}
      </Card>

      <SectionHeader title="Sản phẩm bán chạy" />
      <Card style={styles.card}>
        {summary && summary.topProducts.length > 0 ? (
          summary.topProducts.map((product) => (
            <React.Fragment key={product.id}>
              <List.Item
                title={product.tenSanPham ?? 'Sản phẩm'}
                description={`Mã: ${product.maChiTietSanPham ?? '—'}`}
                right={() => (
                  <View style={styles.listRight}>
                    <Text style={styles.orderAmount}>{product.soLuong} tồn</Text>
                    <Text style={styles.orderDate}>{currencyFormatter.format(product.giaBan ?? 0)}</Text>
                  </View>
                )}
              />
              <Divider inset leadingInset={16} />
            </React.Fragment>
          ))
        ) : (
          <List.Item title="Không có dữ liệu" />
        )}
      </Card>

      <SectionHeader title="Khách hàng thân thiết" />
      <Card style={styles.cardLast}>
        {summary && summary.topCustomers.length > 0 ? (
          summary.topCustomers.map((customer) => (
            <React.Fragment key={customer.id}>
              <List.Item
                title={customer.tenKhachHang}
                description={`Đơn hàng: ${customer.tongDon}`}
                right={() => (
                  <Text style={styles.orderAmount}>{currencyFormatter.format(customer.tongChiTieu ?? 0)}</Text>
                )}
              />
              <Divider inset leadingInset={16} />
            </React.Fragment>
          ))
        ) : (
          <List.Item title="Không có dữ liệu" />
        )}
      </Card>
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8fafc',
  },
  contentContainer: {
    paddingBottom: 100,
  },
  header: {
    paddingHorizontal: 16,
    paddingTop: 24,
    paddingBottom: 16,
  },
  title: {
    fontWeight: '700',
  },
  subtitle: {
    marginBottom: 16,
    color: '#475569',
  },
  aiCard: {
    marginHorizontal: 16,
    marginBottom: 16,
    borderRadius: 16,
    backgroundColor: '#f0f9ff',
    borderWidth: 1,
    borderColor: '#bae6fd',
  },
  aiCardContent: {
    padding: 16,
  },
  aiCardHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 16,
  },
  aiIcon: {
    backgroundColor: '#6366f1',
    marginRight: 12,
    borderRadius: 24,
    width: 48,
    height: 48,
    justifyContent: 'center',
    alignItems: 'center',
    margin: 0,
  },
  aiCardText: {
    flex: 1,
  },
  aiTitle: {
    fontWeight: '700',
    marginBottom: 4,
    color: '#1e293b',
  },
  aiDescription: {
    color: '#64748b',
    lineHeight: 20,
  },
  aiButtonContent: {
    flexDirection: 'row-reverse',
  },
  aiButtonLabel: {
    fontSize: 14,
    fontWeight: '600',
  },
  metricRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingHorizontal: 8,
  },
  placeholderCard: {
    flex: 1,
    margin: 8,
  },
  card: {
    marginHorizontal: 16,
    marginBottom: 16,
    borderRadius: 16,
  },
  cardLast: {
    marginHorizontal: 16,
    marginBottom: 32,
    borderRadius: 16,
  },
  listRight: {
    alignItems: 'flex-end',
  },
  orderAmount: {
    fontWeight: '600',
  },
  orderDate: {
    color: '#64748b',
    marginTop: 2,
  },
})

export default DashboardScreen
