import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

/**
 * Configuración de Vite para el cliente web: React con el complemento oficial.
 * Referencia: https://vite.dev/config/
 *
 * @author José Fernando Rincón Barrios
 */
export default defineConfig({
  plugins: [react()],
})
