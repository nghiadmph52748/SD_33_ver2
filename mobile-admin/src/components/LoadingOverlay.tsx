import React from 'react'
import { ActivityIndicator, StyleSheet, View } from 'react-native'
import { Text } from 'react-native-paper'

interface LoadingOverlayProps {
  message?: string
}

const LoadingOverlay: React.FC<LoadingOverlayProps> = ({ message = 'Đang tải dữ liệu...' }) => (
  <View style={styles.container}>
    <ActivityIndicator size="large" color="#2563eb" />
    <Text style={styles.text}>{message}</Text>
  </View>
)

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 24,
  },
  text: {
    marginTop: 16,
    color: '#475569',
  },
})

export default LoadingOverlay
