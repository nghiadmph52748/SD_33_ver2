import { createI18n } from 'vue-i18n'
import en from './locale/en-US'
import vi from './locale/vi-VN'

export const LOCALE_OPTIONS = [
  { label: 'English', value: 'en-US' },
  { label: 'Tiếng Việt', value: 'vi-VN' },
]

const defaultLocale = localStorage.getItem('arco-locale') || 'vi-VN'

const i18n = createI18n({
  locale: defaultLocale,
  fallbackLocale: 'vi-VN',
  legacy: false,
  allowComposition: true,
  messages: {
    'en-US': en,
    'vi-VN': vi,
  },
})

export default i18n
