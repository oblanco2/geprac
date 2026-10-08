/**
 * Cliente de Supabase Auth, el proveedor de identidad único del software.
 * Toma la URL y la llave pública (anon) del archivo .env; nunca una llave
 * privada, porque este código viaja al navegador.
 *
 * @author José Fernando Rincón Barrios
 */
import { createClient } from '@supabase/supabase-js'

const url = import.meta.env.VITE_SUPABASE_URL
const llave = import.meta.env.VITE_SUPABASE_ANON_KEY

if (!url || !llave) {
  throw new Error(
    'Faltan VITE_SUPABASE_URL o VITE_SUPABASE_ANON_KEY. Revisa el archivo .env'
  )
}

export const supabase = createClient(url, llave)