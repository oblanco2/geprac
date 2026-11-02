import react from '@vitejs/plugin-react'
import { readFileSync } from 'node:fs'
import { defineConfig } from 'vite'

/** La versión del cliente web, la de su package.json. */
const { version } = JSON.parse(readFileSync(new URL('./package.json', import.meta.url), 'utf8'))

/**
 * Configuración de Vite para el cliente web: React con el complemento oficial, y la versión del package.json como
 * la constante __APP_VERSION__, que Vite reemplaza al compilar y que el pie de página muestra. Así la versión en
 * pantalla es siempre la del package.json, la misma de la etiqueta de cada versión publicada en GitHub.
 * Referencia: https://vite.dev/config/shared-options#define
 *
 * @author José Fernando Rincón Barrios
 */
export default defineConfig({
  plugins: [react()],
  define: { __APP_VERSION__: JSON.stringify(version) },
})
