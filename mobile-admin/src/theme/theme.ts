import { MD3LightTheme, MD3Theme } from 'react-native-paper'

export const paperTheme: MD3Theme = {
  ...MD3LightTheme,
  colors: {
    ...MD3LightTheme.colors,
    primary: '#2563eb',
    secondary: '#f97316',
    surface: '#ffffff',
    background: '#f8fafc',
    onSurface: '#0f172a',
  },
}

export default paperTheme
