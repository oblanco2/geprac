/**
 * Clientes HTTP de los dos microservicios. Cada petición lleva el token de la
 * sesión de Supabase Auth, y los errores se traducen a un mensaje legible en
 * error.mensaje: el detalle del problema que responde el servicio (RFC 9457) o,
 * si no hubo respuesta, la causa probable.
 *
 * @author José Fernando Rincón Barrios
 */
import axios from 'axios'
import { supabase } from '../lib/supabase'

function crearCliente(baseURL) {
  const cliente = axios.create({
    baseURL,
    headers: { 'Content-Type': 'application/json' },
    timeout: 60000,   // Render tarda hasta 50 s en despertar
  })

  // Supabase renueva el token solo; aquí basta con pedírselo
  // antes de cada petición.
  cliente.interceptors.request.use(async (config) => {
    const { data } = await supabase.auth.getSession()
    const token = data.session?.access_token
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  })

  cliente.interceptors.response.use(
    (r) => r,
    (error) => {
      const datos = error.response?.data
      error.mensaje =
        datos?.detail ??
        datos?.message ??
        (error.code === 'ECONNABORTED'
          ? 'El servidor tardó demasiado. Puede estar despertando; inténtalo de nuevo.'
          : 'No se pudo conectar con el servidor')
      return Promise.reject(error)
    },
  )

  return cliente
}

/** MS-01 Identidad y Perfil Académico. */
const cliente = crearCliente(import.meta.env.VITE_API_ACADEMICO)

/** MS-02 Legalización de Prácticas. */
export const legalizacion = crearCliente(import.meta.env.VITE_API_LEGALIZACION)

export default cliente
