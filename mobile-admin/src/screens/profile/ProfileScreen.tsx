import { useNavigation } from '@react-navigation/native'
import React from 'react'
import { StyleSheet, View } from 'react-native'
import { Avatar, Button, Card, List, Text } from 'react-native-paper'

import { SCREENS } from '../../constants/routes'
import type { ProfileStackParamList } from '../../navigation/ProfileNavigator'
import type { NativeStackNavigationProp } from '@react-navigation/native-stack'
import useAuthStore from '../../store/useAuthStore'
import { notifySuccess } from '../../utils/notifications'

const ProfileScreen: React.FC = () => {
  const navigation = useNavigation<NativeStackNavigationProp<ProfileStackParamList>>()
  const user = useAuthStore((state) => state.user)
  const signOut = useAuthStore((state) => state.signOut)

  const initials = user?.tenNhanVien?.split(' ')
    .map((part) => part[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()

  const handleLogout = async () => {
    await signOut()
    notifySuccess('Đã đăng xuất')
  }

  return (
    <View style={styles.container}>
      <Card style={styles.card}>
        <Card.Content style={styles.profileHeader}>
          <Avatar.Text size={68} label={initials || 'AD'} style={styles.avatar} />
          <View style={styles.profileInfo}>
            <Text variant="titleMedium" style={styles.name}>
              {user?.tenNhanVien ?? 'Quản trị viên'}
            </Text>
            <Text variant="bodyMedium" style={styles.role}>
              {user?.tenQuyenHan ?? '—'}
            </Text>
            <Text variant="bodySmall" style={styles.email}>
              {user?.email}
            </Text>
          </View>
        </Card.Content>
      </Card>

      <Card style={styles.card}>
        <List.Section>
          <List.Item
            title="Thiết lập tài khoản"
            description="Cập nhật quyền, thông tin liên hệ"
            left={(props) => <List.Icon {...props} icon="cog-outline" />}
            onPress={() => navigation.navigate(SCREENS.STACK.SETTINGS)}
          />
          <List.Item
            title="Đăng xuất"
            description="Thoát khỏi ứng dụng"
            left={(props) => <List.Icon {...props} icon="logout" />}
            onPress={handleLogout}
          />
        </List.Section>
      </Card>
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8fafc',
    padding: 16,
  },
  card: {
    borderRadius: 16,
    marginBottom: 16,
  },
  profileHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    columnGap: 16,
  },
  avatar: {
    backgroundColor: '#2563eb',
  },
  profileInfo: {
    flex: 1,
  },
  name: {
    fontWeight: '600',
  },
  role: {
    color: '#475569',
  },
  email: {
    color: '#64748b',
  },
  logoutButton: {
    marginTop: 16,
  },
})

export default ProfileScreen
