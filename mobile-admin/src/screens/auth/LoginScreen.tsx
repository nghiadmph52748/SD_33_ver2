import React, { useState } from 'react'
import {
  ImageBackground,
  KeyboardAvoidingView,
  Platform,
  StyleSheet,
  View,
} from 'react-native'
import { Button, HelperText, Surface, Text, TextInput } from 'react-native-paper'

import { notifyError, notifySuccess } from '../../utils/notifications'
import useAuthStore from '../../store/useAuthStore'

const LoginScreen: React.FC = () => {
  const signIn = useAuthStore((state) => state.signIn)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const hasError = !username || !password

  const handleSubmit = async () => {
    if (hasError) {
      notifyError('Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu')
      return
    }

    try {
      setIsSubmitting(true)
      await signIn({ username, password })
      notifySuccess('Đăng nhập thành công')
    } catch (error: any) {
      notifyError(error.message ?? 'Đăng nhập thất bại')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <ImageBackground
      source={{ uri: 'https://images.unsplash.com/photo-1475180098004-ca77a66827be?auto=format&fit=crop&w=1200&q=80' }}
      style={styles.background}
      resizeMode="cover"
    >
      <KeyboardAvoidingView
        style={styles.flex}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      >
        <View style={styles.overlay}>
          <Surface style={styles.card} elevation={4}>
            <Text variant="headlineSmall" style={styles.title}>
              GearUp Mobile Admin
            </Text>
            <Text variant="bodyMedium" style={styles.subtitle}>
              Quản trị cửa hàng từ mọi nơi
            </Text>

            <TextInput
              label="Tên đăng nhập"
              value={username}
              onChangeText={setUsername}
              autoCapitalize="none"
              autoCorrect={false}
              left={<TextInput.Icon icon="account" />}
              style={styles.input}
            />
            <TextInput
              label="Mật khẩu"
              value={password}
              onChangeText={setPassword}
              secureTextEntry
              left={<TextInput.Icon icon="lock" />}
              style={styles.input}
            />

            <HelperText type="info" visible>
              Sử dụng tài khoản quản trị viên đã cấp.
            </HelperText>

            <Button
              mode="contained"
              onPress={handleSubmit}
              loading={isSubmitting}
              disabled={isSubmitting}
              style={styles.submitButton}
            >
              Đăng nhập
            </Button>
          </Surface>
        </View>
      </KeyboardAvoidingView>
    </ImageBackground>
  )
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  background: {
    flex: 1,
  },
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(15, 23, 42, 0.65)',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 24,
  },
  card: {
    width: '100%',
    maxWidth: 380,
    padding: 24,
    borderRadius: 20,
  },
  title: {
    textAlign: 'center',
    marginBottom: 8,
    fontWeight: '700',
  },
  subtitle: {
    textAlign: 'center',
    marginBottom: 24,
    color: '#475569',
  },
  input: {
    marginBottom: 16,
    backgroundColor: 'white',
  },
  submitButton: {
    marginTop: 12,
    borderRadius: 8,
    paddingVertical: 4,
  },
})

export default LoginScreen
