import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import uniPackage from '@dcloudio/vite-plugin-uni'

const uni = uniPackage.default || uniPackage

export default defineConfig({
  plugins: [uni()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
