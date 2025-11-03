import React from 'react'
import { StyleSheet, View } from 'react-native'
import { Text } from 'react-native-paper'

interface ListEmptyProps {
  title?: string
  description?: string
}

const ListEmpty: React.FC<ListEmptyProps> = ({
  title = 'Không có dữ liệu',
  description = 'Bắt đầu bằng cách đồng bộ hoặc tạo mới dữ liệu.',
}) => (
  <View style={styles.container}>
    <Text variant="titleMedium" style={styles.title}>
      {title}
    </Text>
    <Text variant="bodyMedium" style={styles.description}>
      {description}
    </Text>
  </View>
)

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 24,
  },
  title: {
    fontWeight: '600',
    marginBottom: 8,
    textAlign: 'center',
  },
  description: {
    textAlign: 'center',
    color: '#64748b',
  },
})

export default ListEmpty
