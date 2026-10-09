/**
 * La ventana de confirmación de las acciones que no se deshacen o que cambian
 * un estado. Atrapa el foco mientras está abierta, se cierra con Esc y devuelve
 * el foco al botón que la abrió. En una acción peligrosa, el foco empieza en
 * «Cancelar».
 *
 * @author José Fernando Rincón Barrios
 */
import { useCallback, useEffect, useRef, useState } from 'react'
import { DialogoContexto } from '../sesion/contexto'

export default function DialogoProveedor({ children }) {
  const [dialogo, setDialogo] = useState(null)
  const actual = useRef(null)
  const caja = useRef(null)
  const anterior = useRef(null)

  const confirmar = useCallback((opciones) => new Promise((resolver) => {
    anterior.current = document.activeElement
    actual.current = { titulo: 'Confirme la acción', si: 'Confirmar', ...opciones, resolver }
    setDialogo(actual.current)
  }), [])

  const cerrar = useCallback((valor) => {
    const d = actual.current
    actual.current = null
    setDialogo(null)
    d?.resolver(valor)
    const volver = anterior.current
    setTimeout(() => { if (volver && document.body.contains(volver)) volver.focus() }, 0)
  }, [])

  useEffect(() => {
    if (!dialogo) return undefined
    const app = document.querySelector('.app')
    if (app) app.inert = true
    const botones = [...caja.current.querySelectorAll('button')]
    ;(dialogo.peligro || dialogo.soloCerrar ? botones[0] : botones[botones.length - 1]).focus()
    const teclas = (e) => {
      if (e.key === 'Escape') { e.preventDefault(); cerrar(false) }
      if (e.key === 'Tab') {
        const i = botones.indexOf(document.activeElement)
        const j = e.shiftKey ? (i <= 0 ? botones.length - 1 : i - 1) : (i === botones.length - 1 ? 0 : i + 1)
        e.preventDefault()
        botones[j].focus()
      }
    }
    document.addEventListener('keydown', teclas, true)
    return () => {
      document.removeEventListener('keydown', teclas, true)
      if (app) app.inert = false
    }
  }, [dialogo, cerrar])

  return (
    <DialogoContexto.Provider value={confirmar}>
      {children}
      {dialogo && (
        <div className="velo" onMouseDown={(e) => { if (e.target === e.currentTarget) cerrar(false) }}>
          <div className="dialogo" role="dialog" aria-modal="true" aria-labelledby="dlg-t" aria-describedby="dlg-d" ref={caja}>
            <h2 id="dlg-t">{dialogo.titulo}</h2>
            <div id="dlg-d" className="dlg-cuerpo"><p>{dialogo.texto}</p></div>
            <div className="acciones der">
              <button type="button" className="btn g" onClick={() => cerrar(false)}>
                {dialogo.soloCerrar ? (dialogo.cerrar || 'Entendido') : 'Cancelar'}
              </button>
              {!dialogo.soloCerrar && (
                <button type="button" className={`btn ${dialogo.clase || (dialogo.peligro ? 'd' : 'p')}`} onClick={() => cerrar(true)}>
                  {dialogo.si}
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </DialogoContexto.Provider>
  )
}
