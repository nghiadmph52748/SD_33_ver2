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
            {/* Row 1: Color | Size */}
            <View style={styles.chipRowLine}>
              <View style={styles.chipHalf}>
                {product.tenMauSac ? (
                  <Chip icon="palette" compact mode="flat" style={[styles.chip, styles.chipContent]}>
                    <Text style={styles.chipText}>{product.tenMauSac}</Text>
                  </Chip>
                ) : null}
              </View>
              <View style={styles.chipHalf}>
                {product.tenKichThuoc ? (
                  <Chip icon="resize" compact mode="flat" style={[styles.chip, styles.chipContent]}>
                    <Text style={styles.chipText}>{product.tenKichThuoc}</Text>
                  </Chip>
                ) : null}
              </View>
            </View>

            {/* Row 2: Material | Weight */}
            <View style={styles.chipRowLine}>
              <View style={styles.chipHalf}>
                {product.tenChatLieu ? (
                  <Chip icon="format-texture" compact mode="flat" style={[styles.chip, styles.chipContent]}>
                    <Text style={styles.chipText}>{product.tenChatLieu}</Text>
                  </Chip>
                ) : null}
              </View>
              <View style={styles.chipHalf}>
                {product.tenTrongLuong ? (
                  <Chip icon="weight" compact mode="flat" style={[styles.chip, styles.chipContent]}>
                    <Text style={styles.chipText}>{product.tenTrongLuong}</Text>
                  </Chip>
                ) : null}
              </View>
            </View>

            {/* Row 3: Manufacturer | Origin */}
            <View style={styles.chipRowLine}>
              <View style={styles.chipHalf}>
                {product.tenNhaSanXuat ? (
                  <Chip compact mode="flat" style={[styles.chip, styles.chipContent]}>
                    <Text style={styles.chipText}>{`NSX: ${product.tenNhaSanXuat}`}</Text>
                  </Chip>
                ) : null}
              </View>
              <View style={styles.chipHalf}>
                {product.tenXuatXu ? (
                  <Chip compact mode="flat" style={[styles.chip, styles.chipContent]}>
                    <Text style={styles.chipText}>{`Xuất xứ: ${product.tenXuatXu}`}</Text>
                  </Chip>
                ) : null}
              </View>
            </View>

            {/* Row 4: Sole (full width) */}
            {product.tenDeGiay ? (
              <View style={styles.chipRowLine}>
                <View style={styles.chipFull}>
                  <Chip compact mode="flat" style={[styles.chip, styles.chipContent]}>
                    <Text style={styles.chipText}>{`Đế: ${product.tenDeGiay}`}</Text>
                  </Chip>
                </View>
              </View>
            ) : null}
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
  chipRowLine: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 12,
  },
  chipHalf: {
    width: '48%',
  },
  chipFull: {
    width: '100%',
  },
  chipRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    rowGap: 8,
    columnGap: 8,
    justifyContent: 'flex-start',
  },
  chip: {
    borderRadius: 13,
    alignSelf: 'flex-start',
    marginRight: 0,
    marginBottom: 0,
    width: '100%',
  },
  chipCol: {
    flexBasis: '48%',
    maxWidth: '48%',
    alignItems: 'flex-start',
    marginBottom: 12,
  },
  chipContent: {
    paddingVertical: 4,
    paddingHorizontal: 10,
    minHeight: 28,
  },
  chipText: {
    fontSize: 13,
    lineHeight: 18,
  },
})

export default ProductDetailScreen
