import React, { useCallback, useMemo, useState } from 'react'
import { FlatList, RefreshControl, StyleSheet, View } from 'react-native'
import { Avatar, Chip, List, Text } from 'react-native-paper'
import { useFocusEffect, useNavigation } from '@react-navigation/native'

import { fetchProductVariants, ProductVariant } from '../../api/products'
import { ListEmpty, SearchInput } from '../../components'
import { SCREENS } from '../../constants/routes'
import type { ProductsStackParamList } from '../../navigation/ProductsNavigator'
import type { NativeStackNavigationProp } from '@react-navigation/native-stack'

const currencyFormatter = new Intl.NumberFormat('vi-VN', {
  style: 'currency',
  currency: 'VND',
  maximumFractionDigits: 0,
})

const PAGE_SIZE = 20

const ProductsScreen: React.FC = () => {
  const navigation = useNavigation<NativeStackNavigationProp<ProductsStackParamList>>()
  const [query, setQuery] = useState('')
  const [variants, setVariants] = useState<ProductVariant[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [isLoading, setIsLoading] = useState(false)

  const loadProducts = useCallback(
    async (pageParam: number, append = false) => {
      if (pageParam >= totalPages && append) return
      try {
        setIsLoading(true)
        const response = await fetchProductVariants(pageParam, PAGE_SIZE)
        setTotalPages(response.totalPages)
        setPage(response.currentPage)
        setVariants((prev) => (append ? [...prev, ...response.data] : response.data))
      } finally {
        setIsLoading(false)
      }
    },
    [totalPages]
  )

  useFocusEffect(
    useCallback(() => {
      loadProducts(0, false)
    }, [loadProducts])
  )

  const filteredVariants = useMemo(() => {
    if (!query) return variants
    const normalized = query.toLowerCase()
    return variants.filter((variant) => {
      const name = variant.tenSanPham ?? ''
      const code = variant.maChiTietSanPham ?? ''
      return name.toLowerCase().includes(normalized) || code.toLowerCase().includes(normalized)
    })
  }, [variants, query])

  const handleLoadMore = () => {
    if (isLoading) return
    if (page + 1 >= totalPages) return
    loadProducts(page + 1, true)
  }

  return (
    <View style={styles.container}>
      <SearchInput query={query} onChangeQuery={setQuery} placeholder="Tìm theo tên hoặc mã sản phẩm" />

      <FlatList
        data={filteredVariants}
        keyExtractor={(item) => item.id.toString()}
        refreshControl={<RefreshControl refreshing={isLoading} onRefresh={() => loadProducts(0, false)} />}
        ListEmptyComponent={!isLoading ? <ListEmpty title="Chưa có sản phẩm" /> : null}
        onEndReachedThreshold={0.3}
        onEndReached={handleLoadMore}
        renderItem={({ item }) => (
          <List.Item
            title={item.tenSanPham ?? 'Sản phẩm'}
            description={`Mã biến thể: ${item.maChiTietSanPham ?? '—'}`}
            onPress={() => navigation.navigate(SCREENS.STACK.PRODUCT_DETAILS, { productId: item.id })}
            left={() => (
              <Avatar.Text
                label={item.tenSanPham?.substring(0, 2).toUpperCase() ?? 'SP'}
                size={42}
                style={styles.avatar}
              />
            )}
            right={() => (
              <View style={styles.productRight}>
                <Text style={styles.price}>{currencyFormatter.format(item.giaBan ?? 0)}</Text>
                <Chip icon="package-variant" style={styles.stockChip}>
                  {item.soLuong} tồn
                </Chip>
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
    backgroundColor: '#2563eb',
  },
  productRight: {
    alignItems: 'flex-end',
  },
  price: {
    fontWeight: '600',
  },
  stockChip: {
    marginTop: 4,
  },
})

export default ProductsScreen
