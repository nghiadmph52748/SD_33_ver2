import React, { useState } from 'react'
import { StyleSheet, View } from 'react-native'
import { List, Switch, Text } from 'react-native-paper'

const SettingsScreen: React.FC = () => {
  const [pushEnabled, setPushEnabled] = useState(true)
  const [analyticsEnabled, setAnalyticsEnabled] = useState(true)

  return (
    <View style={styles.container}>
      <List.Section>
        <List.Subheader>Thông báo</List.Subheader>
        <List.Item
          title="Thông báo đẩy"
          description="Nhận cảnh báo đơn hàng và chat"
          right={() => <Switch value={pushEnabled} onValueChange={setPushEnabled} />}
        />
        <List.Subheader>Tùy chọn khác</List.Subheader>
        <List.Item
          title="Báo cáo nâng cao"
          description="Cho phép thu thập dữ liệu hiệu suất để phân tích"
          right={() => <Switch value={analyticsEnabled} onValueChange={setAnalyticsEnabled} />}
        />
      </List.Section>
      <Text style={styles.note}>
        Các thiết lập này chỉ áp dụng cho thiết bị hiện tại. Tích hợp thông báo hoạt động khi bạn đăng nhập bằng tài khoản GearUp.
      </Text>
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  note: {
    paddingHorizontal: 16,
    paddingTop: 8,
    color: '#64748b',
  },
})

export default SettingsScreen
