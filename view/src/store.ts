import { createPinia } from 'pinia'
import useAppStore from './store/modules/app'
import useTabBarStore from './store/modules/tab-bar'
import useUserStore from './store/modules/user'

const pinia = createPinia()

export { useAppStore, useTabBarStore, useUserStore }
export default pinia
