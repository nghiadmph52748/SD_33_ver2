import { createI18n } from 'vue-i18n'
import vi from './locale/vi-VN'

export const LOCALE_OPTIONS = [
  { label: 'Tiếng Việt', value: 'vi-VN' },
]
const defaultLocale = 'vi-VN'

const i18n = createI18n({
  locale: defaultLocale,
  fallbackLocale: 'vi-VN',
  legacy: false,
  allowComposition: true,
  messages: {
    'vi-VN': vi,
  },
})

export default i18n
