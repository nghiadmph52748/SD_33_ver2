import { RouteProp, useRoute } from '@react-navigation/native'
import React, { useEffect, useState } from 'react'
import { ScrollView, StyleSheet, View } from 'react-native'
import { Card, Chip, Divider, List, Text } from 'react-native-paper'

import { fetchProductVariantById, ProductVariant } from '../../api/products'
import { LoadingOverlay } from '../../components'
import { SCREENS } from '../../constants/routes'
import { ProductsStackParamList } from '../../navigation/ProductsNavigator'

const currencyFormatter = new Intl.NumberFormat('vi-VN', {
  style: 'currency',
  currency: 'VND',
  maximumFractionDigits: 0,
})

const ProductDetailScreen: React.FC = () => {
  const route = useRoute<RouteProp<ProductsStackParamList, typeof SCREENS.STACK.PRODUCT_DETAILS>>()
  const [product, setProduct] = useState<ProductVariant | null>(null)
  const [isLoading, setIsLoading] = useState(false)

  useEffect(() => {
    const loadProduct = async () => {
      try {
        setIsLoading(true)
        const response = await fetchProductVariantById(route.params.productId)
        setProduct(response)
      } finally {
        setIsLoading(false)
      }
    }

    loadProduct()
  }, [route.params.productId])

  if (isLoading || !product) {
    return <LoadingOverlay message="Đang tải chi tiết sản phẩm" />
  }

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
      <Card style={styles.card}>
        <Card.Title title={product.tenSanPham ?? 'Sản phẩm'} subtitle={`Mã biến thể: ${product.maChiTietSanPham ?? '—'}`} />
        <Card.Content>
          <List.Section>
            <List.Subheader>Thông tin chung</List.Subheader>
            <List.Item
              title="Giá bán"
              description={currencyFormatter.format(product.giaBan ?? 0)}
              left={(props) => <List.Icon {...props} icon="cash" />}
            />
            <List.Item
              title="Tồn kho"
              description={`${product.soLuong} sản phẩm`}
              left={(props) => <List.Icon {...props} icon="package-variant" />}
            />
          </List.Section>

          <Divider style={styles.divider} />

          <List.Section>
            <List.Subheader>Thuộc tính</List.Subheader>
            <View style={styles.chipRow}>
              {product.tenMauSac ? <Chip icon="palette">{product.tenMauSac}</Chip> : null}
              {product.tenKichThuoc ? <Chip icon="resize">{product.tenKichThuoc}</Chip> : null}
              {product.tenChatLieu ? <Chip icon="format-texture">{product.tenChatLieu}</Chip> : null}
              {product.tenTrongLuong ? <Chip icon="weight">{product.tenTrongLuong}</Chip> : null}
            </View>
            <View style={styles.chipRow}>
              {product.tenNhaSanXuat ? <Chip>{`NSX: ${product.tenNhaSanXuat}`}</Chip> : null}
              {product.tenXuatXu ? <Chip>{`Xuất xứ: ${product.tenXuatXu}`}</Chip> : null}
              {product.tenDeGiay ? <Chip>{`Đế: ${product.tenDeGiay}`}</Chip> : null}
            </View>
          </List.Section>

          <Divider style={styles.divider} />

          <List.Section>
            <List.Subheader>Khuyến mãi</List.Subheader>
            <List.Item
              title={product.tenDotGiamGia ? `Đợt: ${product.tenDotGiamGia}` : 'Không có khuyến mãi'}
              description={product.giaTriGiamGia ? `${product.giaTriGiamGia}%` : undefined}
              left={(props) => <List.Icon {...props} icon="sale" />}
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
  chipRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    rowGap: 8,
    columnGap: 8,
  },
})

export default ProductDetailScreen
