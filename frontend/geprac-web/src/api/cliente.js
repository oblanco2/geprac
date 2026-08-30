import axios from 'axios'
import { supabase } from '../lib/supabase'

const cliente = axios.create({
  baseURL: import.meta.env.VITE_API_ACADEMICO,
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
      datos?.message ??
      (error.code === 'ECONNABORTED'
        ? 'El servidor tardó demasiado. Puede estar despertando; inténtalo de nuevo.'
        : 'No se pudo conectar con el servidor')
    return Promise.reject(error)
  },
)

export default cliente