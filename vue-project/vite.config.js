import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite';
import { ElementPlusResolver } from "unplugin-vue-components/resolvers";

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    AutoImport({
        resolvers: [ElementPlusResolver()],
    }),
    Components({
        resolvers: [ElementPlusResolver()],
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
    server:{
      port: 5173,
        proxy: {
          '/api': {
              target: 'http://localhost:8080', // 后端地址
              changeOrigin: true,
              secure: false,
              // rewrite: (path) => path.replace(/^\/api/, '') // 如果需要重写路径，例如去掉/api前缀
          }
        }
    }
})
