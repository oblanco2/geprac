/**
 * Los nombres de las cuentas, para la Dirección del Programa: con ellos la
 * bandeja de aval presenta quién aprobó cada inscripción (CU-07, paso 2). Los
 * consulta a MS-01; si no llegan, las pantallas nombran el papel en su lugar.
 *
 * @author José Fernando Rincón Barrios
 */
import { listarUsuarios } from '../api/academico'

/** { [identificador de la cuenta]: nombre }; vacío si MS-01 no responde. */
export const consultarNombres = () => listarUsuarios()
  .then((l) => Object.fromEntries(l.map((u) => [u.id, u.nombrePresentacion])))
  .catch(() => ({}))

/** La última aprobación del tutor: su revisión con resultado APROBADA más reciente. */
export const aprobacion = (insc) => [...(insc.revisiones || [])].reverse().find((r) => r.resultado === 'APROBADA') || null
