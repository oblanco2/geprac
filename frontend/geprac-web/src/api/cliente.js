/**
 * Clientes HTTP de los dos microservicios. Cada petición lleva el token de la
 * sesión de Supabase Auth, y los errores se traducen a un mensaje legible en
 * error.mensaje: el detalle del problema que responde el servicio (RFC 9457) o,
 * si no hubo respuesta, la causa probable. error.estado trae el código HTTP y
 * error.campos, el mensaje de cada campo que no pasó la validación. Un 401
 * significa que la sesión ya no vale: se cierra, y las rutas llevan al ingreso.
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
    async (error) => {
      let datos = error.response?.data
      // una descarga que falla trae el problema dentro de un Blob
      if (datos instanceof Blob) {
        try { datos = JSON.parse(await datos.text()) } catch { datos = null }
      }
      error.estado = error.response?.status
      error.campos = datos?.campos ?? {}
      if (error.estado === 401) supabase.auth.signOut({ scope: 'local' })
      error.mensaje =
        datos?.detail ??
        datos?.message ??
        (error.estado === 403
          ? 'Su rol no permite esta operación.'
          : error.response
            ? `El servicio no pudo atender la petición (código ${error.estado}). Inténtelo de nuevo en un momento.`
            : error.code === 'ECONNABORTED'
              ? 'El servidor tardó demasiado. Puede estar despertando; inténtelo de nuevo en un momento.'
              : 'No se pudo conectar con el servidor. Revise su conexión e inténtelo de nuevo.')
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
