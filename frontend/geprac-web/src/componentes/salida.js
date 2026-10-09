/**
 * Cambios sin guardar: si la persona intenta salir de la pantalla —por el menú,
 * por un botón o con atrás del navegador— el software le pregunta antes, y si
 * cierra o recarga la pestaña, el navegador también. Devuelve permitir(), que la
 * pantalla llama justo antes de salir por su cuenta después de guardar.
 *
 * @author José Fernando Rincón Barrios
 */
import { useCallback, useEffect, useRef } from 'react'
import { useBlocker } from 'react-router-dom'
import { useConfirmar } from '../sesion/contexto'
import { MSJ_SALIR } from '../lib/formato'

export function useSalidaProtegida(sucio) {
  const confirmar = useConfirmar()
  const permitida = useRef(false)
  const preguntando = useRef(false)
  const bloqueo = useBlocker(({ currentLocation, nextLocation }) =>
    sucio && !permitida.current && currentLocation.pathname !== nextLocation.pathname)

  useEffect(() => {
    if (bloqueo.state !== 'blocked' || preguntando.current) return
    preguntando.current = true
    confirmar({ texto: MSJ_SALIR, si: 'Salir sin guardar', peligro: true }).then((si) => {
      preguntando.current = false
      if (si) bloqueo.proceed()
      else bloqueo.reset()
    })
  }, [bloqueo, confirmar])

  useEffect(() => {
    if (!sucio) return undefined
    const avisar = (e) => { e.preventDefault(); e.returnValue = '' }
    window.addEventListener('beforeunload', avisar)
    return () => window.removeEventListener('beforeunload', avisar)
  }, [sucio])

  return useCallback(() => { permitida.current = true }, [])
}
