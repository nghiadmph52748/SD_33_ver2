import ArcoVue from '@arco-design/web-vue'
import ArcoVueIcon from '@arco-design/web-vue/es/icon'
import { createApp } from 'vue'
import '@/api/interceptor'
import '@/assets/style/global.less'
import App from './App.vue'
import globalComponents from './components'
import directive from './directive'
import i18n from './locale'
import './mock'
import router from './router'
import store from './store'

const app = createApp(App)

app.use(ArcoVue, {})
app.use(ArcoVueIcon)

app.use(router)
app.use(store)
app.use(i18n)
app.use(globalComponents)
app.use(directive)

app.mount('#app')
