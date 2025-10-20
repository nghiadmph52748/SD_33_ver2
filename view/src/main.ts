import ArcoVue from '@arco-design/web-vue'
import ArcoVueIcon from '@arco-design/web-vue/es/icon'
import { createApp } from 'vue'
import '@/api/interceptor'
import '@/assets/style/global.less'
import { getToken } from '@/utils/auth'
import App from './App.vue'
import globalComponents from './components'
import directive from './directive'
import i18n from './locale'
import './mock'
import router from './router'
import store from './store'

const app = createApp(App)

// Increase recursive updates limit for development
// eslint-disable-next-line no-console
app.config.errorHandler = (err, instance, info) => {
  if (typeof err === 'string' && err.includes('Maximum recursive updates exceeded')) {
    // eslint-disable-next-line no-console
    console.warn('[Vue warn]:', err)
    return // Suppress the error
  }
  // eslint-disable-next-line no-console
  console.error(err, info)
}

app.use(ArcoVue, {})
app.use(ArcoVueIcon)

app.use(router)
app.use(store)
app.use(i18n)
app.use(globalComponents)
app.use(directive)

// Handle page unload - logout when user closes browser/tab
window.addEventListener('beforeunload', async () => {
  const token = getToken()
  if (token) {
    try {
      // Use fetch with keepalive for more reliable cleanup when page is closing
      fetch('/api/auth/logout', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        keepalive: true,
      }).catch(() => {
        // Ignore errors - page is closing anyway
      })
    } catch {
      // Ignore errors during cleanup
    }
  }
})

app.mount('#app')
