import { createApp } from 'vue'
import './style.css'
import App from './App.vue'

// ElementPlus组件库：核心对象、核心CSS、显隐CSS、国际化对象
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/display.css'
import { zhCn } from 'element-plus/es/locale/index'

// 使用ElementPlus组件库
const app = createApp(App)
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')
