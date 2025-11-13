import { createApp } from "vue";
import { createPinia } from "pinia";
import ArcoVue from "@arco-design/web-vue";
import ArcoVueIcon from "@arco-design/web-vue/es/icon";
import App from "./App.vue";
import router from "./router";
import i18n from "./locale";
import "@/api/interceptor"; // Initialize axios interceptors
import "normalize.css";
import "@arco-design/web-vue/dist/arco.css";
import "./assets/main.scss";
import imgFallback from "./directives/imgFallback";
import { registerMessage } from '@/utils/message'

// Initialize theme from localStorage
const savedTheme = localStorage.getItem('arco-theme');
if (savedTheme === 'dark') {
  document.body.setAttribute('arco-theme', 'dark');
} else {
  document.body.removeAttribute('arco-theme');
}

const app = createApp(App);

app.use(ArcoVue, {});
app.use(ArcoVueIcon);
app.use(createPinia());
app.use(router);
app.use(i18n);

// Expose message API for non-setup modules (e.g., Axios interceptors)
registerMessage(app.config.globalProperties.$message);

app.directive('img-fallback', imgFallback);

app.mount("#app");
