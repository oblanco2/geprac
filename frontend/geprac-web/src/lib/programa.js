/**
 * El nombre del programa del token, para el contexto de la barra. Lo consulta
 * una vez a MS-01 y lo recuerda mientras la página esté abierta; si no llega,
 * la barra se queda con el texto que tenía.
 *
 * @author José Fernando Rincón Barrios
 */
import { useEffect, useState } from 'react'
import { listarProgramas } from '../api/academico'

let programas = null

export function useNombrePrograma(codigo) {
  const [nombre, setNombre] = useState(() => programas?.find((p) => p.codigo === codigo)?.nombre || null)
  useEffect(() => {
    if (!codigo || nombre) return undefined
    let vigente = true
    ;(programas ? Promise.resolve(programas) : listarProgramas().then((l) => (programas = l)))
      .then((l) => { if (vigente) setNombre(l.find((p) => p.codigo === codigo)?.nombre || null) })
      .catch(() => {})
    return () => { vigente = false }
  }, [codigo, nombre])
  return nombre
}
