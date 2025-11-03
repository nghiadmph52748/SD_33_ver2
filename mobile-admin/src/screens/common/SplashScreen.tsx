import React from 'react'
import { ActivityIndicator, StyleSheet, View } from 'react-native'
import { Text } from 'react-native-paper'

const SplashScreen: React.FC = () => (
  <View style={styles.container}>
    <ActivityIndicator size="large" color="#2563eb" />
    <Text style={styles.text}>Đang chuẩn bị dữ liệu...</Text>
  </View>
)

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#0f172a',
  },
  text: {
    marginTop: 16,
    color: '#fff',
  },
})

export default SplashScreen
