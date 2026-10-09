/**
 * La sesión de quien usa el software. Supabase Auth autentica; después el
 * cliente consulta la cuenta en MS-01 (si es el primer ingreso, MS-01 la
 * registra) y lee del token el rol, el programa y el registro de estudiante,
 * que el hook de MS-01 añade al emitirlo. Si el token se emitió antes de que la
 * cuenta tuviera esos datos, lo renueva para que los traiga. Una cuenta sin rol
 * no entra: la Dirección del Programa debe asignárselo.
 *
 * @author José Fernando Rincón Barrios
 */
import { useCallback, useEffect, useMemo, useState } from 'react'
import { supabase } from '../lib/supabase'
import { consultarCuenta } from '../api/academico'
import { leerClaims } from '../lib/formato'
import { SesionContexto } from './contexto'

const VACIA = { sesion: null, cuenta: null, claims: {} }

export default function SesionProveedor({ children }) {
  const [estado, setEstado] = useState({ ...VACIA, lista: false, ocupado: false, aviso: null })

  const cargar = useCallback(async (sesion) => {
    if (!sesion) {
      setEstado((e) => ({ ...e, ...VACIA, lista: true, ocupado: false }))
      return
    }
    setEstado((e) => ({ ...e, ocupado: true }))
    try {
      const cuenta = await consultarCuenta()
      if (!cuenta.rol) {
        await supabase.auth.signOut()
        setEstado({ ...VACIA, lista: true, ocupado: false, aviso: { tipo: 'warn', negrita: 'Su cuenta todavía no tiene rol.',
          texto: 'La Dirección del Programa debe asignárselo.' } })
        return
      }
      let vigente = sesion
      let claims = leerClaims(vigente.access_token)
      if (claims.rol !== cuenta.rol || (cuenta.programa && claims.programa !== cuenta.programa)
          || (cuenta.rol === 'ESTUDIANTE' && !claims.estudiante_id)) {
        const { data, error } = await supabase.auth.refreshSession()
        if (!error && data.session) {
          vigente = data.session
          claims = leerClaims(vigente.access_token)
        }
      }
      setEstado({ sesion: vigente, cuenta, claims, lista: true, ocupado: false, aviso: null })
    } catch (e) {
      await supabase.auth.signOut()
      setEstado({ ...VACIA, lista: true, ocupado: false, aviso: { tipo: 'err', negrita: 'No fue posible abrir su sesión.', texto: e.mensaje } })
    }
  }, [])

  useEffect(() => {
    const { data } = supabase.auth.onAuthStateChange((evento, sesion) => {
      // supabase-js pide no llamar a sus métodos dentro de este aviso: se difiere
      setTimeout(() => {
        if (evento === 'INITIAL_SESSION' || evento === 'SIGNED_IN') cargar(sesion)
        else if (evento === 'SIGNED_OUT') cargar(null)
        else if (evento === 'TOKEN_REFRESHED' && sesion)
          setEstado((e) => ({ ...e, sesion, claims: leerClaims(sesion.access_token) }))
      }, 0)
    })
    return () => data.subscription.unsubscribe()
  }, [cargar])

  const salir = useCallback(async (aviso = null) => {
    await supabase.auth.signOut()
    setEstado((e) => ({ ...e, ...VACIA, aviso }))
  }, [])

  const limpiarAviso = useCallback(() => setEstado((e) => (e.aviso ? { ...e, aviso: null } : e)), [])

  const valor = useMemo(() => ({ ...estado, salir, limpiarAviso }), [estado, salir, limpiarAviso])
  return <SesionContexto.Provider value={valor}>{children}</SesionContexto.Provider>
}
