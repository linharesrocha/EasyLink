import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Qualquer requisição que comece com '/api' será redirecionada
      // para o nosso API Gateway na porta 8080.
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})