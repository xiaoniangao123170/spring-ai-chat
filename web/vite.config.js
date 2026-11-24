import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],

  server: {
    host: 'localhost', // ip地址
    port: 6051,        // 前端端口，后端在 5501
    open: true
  }
})
