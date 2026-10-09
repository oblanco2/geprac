/**
 * El aviso flotante con el que el software confirma el resultado de cada
 * acción que escribe en la base (RNF-03): aparece abajo, se anuncia a los
 * lectores de pantalla y se cierra solo a los seis segundos y medio, o al
 * cambiar de pantalla si era de la anterior.
 *
 * @author José Fernando Rincón Barrios
 */
import { useCallback, useEffect, useMemo, useState } from 'react'
import { AvisosContexto } from '../sesion/contexto'

export default function AvisosProveedor({ children }) {
  const [aviso, setAviso] = useState(null)

  useEffect(() => {
    if (!aviso) return undefined
    const reloj = setTimeout(() => setAviso(null), 6500)
    return () => clearTimeout(reloj)
  }, [aviso])

  const avisar = useCallback((texto, clase = 'ok') => setAviso({ texto, clase, n: Date.now() }), [])
  // el aviso de la acción que trajo a la pantalla nueva es de hace un instante; los demás son de la anterior
  const limpiarAnteriores = useCallback(() => setAviso((a) => (a && Date.now() - a.n > 1000 ? null : a)), [])
  const valor = useMemo(() => ({ avisar, limpiarAnteriores }), [avisar, limpiarAnteriores])

  return (
    <AvisosContexto.Provider value={valor}>
      {children}
      <div className="avisos" role="status" aria-live="polite" aria-atomic="true">
        {aviso && (
          <div className={`flash ${aviso.clase}`} key={aviso.n}>
            <span>{aviso.texto}</span>
            <button type="button" aria-label="Cerrar el aviso" onClick={() => setAviso(null)}>×</button>
          </div>
        )}
      </div>
    </AvisosContexto.Provider>
  )
}
