/**
 * El historial de una inscripción, como lo guarda MS-02: el envío del
 * estudiante, cada revisión del tutor con su resultado y su motivo, y el aval
 * de la Dirección. Si la pantalla conoce el nombre de quien revisó o avaló, lo
 * presenta; si no, su papel.
 *
 * @author José Fernando Rincón Barrios
 */
import { fecha } from '../lib/formato'
import { Estado, Tabla } from './controles'

const COLUMNAS = [{ l: 'Fecha', w: 'm' }, { l: 'Quién', w: 'l' }, { l: 'Resultado', w: 'm' }, { l: 'Motivo' }]

/** nombres: { [identificador de la cuenta]: nombre }, cuando la pantalla los tiene. */
export default function Historial({ insc, estudiante = 'Usted', nombres = {} }) {
  const filas = (insc.revisiones || []).map((r) => ({ fecha: r.fecha, quien: nombres[r.revisorId] || 'Tutor académico',
    resultado: r.resultado, motivo: r.motivo }))
  if (insc.enviadaEn) {
    filas.push({ fecha: insc.enviadaEn, quien: estudiante, resultado: 'ENVIADA', texto: insc.revisiones?.length > 1 ? 'Reenviada' : 'Enviada' })
  }
  if (insc.aval) {
    filas.push({ fecha: insc.aval.fecha, quien: nombres[insc.aval.directorId] || 'Dirección del Programa', resultado: 'AVALADA' })
  }
  filas.sort((a, b) => String(a.fecha).localeCompare(String(b.fecha)))
  return (
    <Tabla titulo="Historial de la inscripción" columnas={COLUMNAS}
      vacio="La inscripción todavía no tiene movimientos: está en borrador."
      filas={filas.map((h, k) => ({ clave: k, celdas: [fecha(h.fecha), h.quien, <Estado key="e" estado={h.resultado} texto={h.texto} />, h.motivo || '—'] }))} />
  )
}
