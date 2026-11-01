/**
 * La sesión de quien usa el software. Supabase Auth autentica; después el
 * cliente consulta la cuenta en MS-01 (si es el primer ingreso, MS-01 la
 * registra) y lee del token el rol, el programa y el registro de estudiante,
 * que el hook de MS-01 añade al emitirlo. Si el token se emitió antes de que la
 * cuenta tuviera esos datos, lo renueva para que los traiga; lo mismo hace
 * renovar(), que llama Mi perfil cuando el estudiante crea su registro o cambia
 * de programa. Una cuenta sin rol no entra: la Dirección del Programa debe
 * asignárselo. Al cerrar la sesión con el botón queda marcada como cerrada,
 * para que el ingreso llegue limpio y pueda entrar otra cuenta.
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
  const [estado, setEstado] = useState({ ...VACIA, lista: false, ocupado: false, aviso: null, cerrada: false })

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

  /** Cierra la sesión. Sin aviso, la cierra la persona con «Cerrar sesión»: queda cerrada y el ingreso llega limpio, sin
      la pantalla de la que salió. Con aviso —el de otro rol—, el ingreso lo presenta y recuerda la pantalla, por si entra
      una cuenta que tenga ese rol. */
  const salir = useCallback(async (aviso = null) => {
    await supabase.auth.signOut()
    setEstado((e) => ({ ...e, ...VACIA, aviso, cerrada: !aviso }))
  }, [])

  const limpiarAviso = useCallback(() => setEstado((e) => (e.aviso ? { ...e, aviso: null } : e)), [])

  /** El ingreso ya llegó limpio: desde ahí, una pantalla protegida vuelve a llevar al ingreso con su aviso. */
  const olvidarCierre = useCallback(() => setEstado((e) => (e.cerrada ? { ...e, cerrada: false } : e)), [])

  /** Un token nuevo, con lo que el hook encuentre ahora en la cuenta: el programa y el registro de estudiante. */
  const renovar = useCallback(async () => {
    const { data, error } = await supabase.auth.refreshSession()
    if (!error && data.session) setEstado((e) => ({ ...e, sesion: data.session, claims: leerClaims(data.session.access_token) }))
  }, [])

  const valor = useMemo(() => ({ ...estado, salir, limpiarAviso, olvidarCierre, renovar }),
    [estado, salir, limpiarAviso, olvidarCierre, renovar])
  return <SesionContexto.Provider value={valor}>{children}</SesionContexto.Provider>
}
