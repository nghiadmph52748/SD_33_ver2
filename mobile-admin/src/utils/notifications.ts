import { Alert, Platform, ToastAndroid } from 'react-native'

export const notifyError = (message: string) => {
  const content = message || 'Đã xảy ra lỗi không xác định'
  if (Platform.OS === 'android') {
    ToastAndroid.show(content, ToastAndroid.LONG)
    return
  }
  Alert.alert('Lỗi', content)
}

export const notifySuccess = (message: string) => {
  const content = message || 'Thành công'
  if (Platform.OS === 'android') {
    ToastAndroid.show(content, ToastAndroid.SHORT)
    return
  }
  Alert.alert('Thông báo', content)
}
