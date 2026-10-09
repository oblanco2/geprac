/**
 * Formatos y catálogos que comparten las pantallas: las fechas como las
 * presenta el prototipo (dd/mm/aaaa), los cinco estados de la inscripción con
 * su significado, los semestres del plan de estudios y las comprobaciones de
 * correo y teléfono.
 *
 * @author José Fernando Rincón Barrios
 */

/** dd/mm/aaaa a partir de una fecha ISO (aaaa-mm-dd, con hora o sin ella). */
export const fecha = (iso) => (iso ? String(iso).slice(0, 10).split('-').reverse().join('/') : '—')

/** Los cinco estados de la inscripción, con el significado que presenta la pantalla. */
export const ESTADOS = {
  borrador: { texto: 'Borrador', sentido: 'aún no se ha enviado' },
  enviada: { texto: 'Enviada', sentido: 'en revisión del tutor académico' },
  aprobada: { texto: 'Aprobada', sentido: 'espera el aval de la Dirección del Programa' },
  devuelta: { texto: 'Devuelta', sentido: 'el estudiante debe corregirla y reenviarla' },
  avalada: { texto: 'Avalada', sentido: 'sus formatos ya se pueden emitir' },
}

export const SEMESTRES_PLAN = ['Primero', 'Segundo', 'Tercero', 'Cuarto', 'Quinto', 'Sexto', 'Séptimo', 'Octavo']
export const ORDINALES = ['primer', 'segundo', 'tercer', 'cuarto', 'quinto', 'sexto', 'séptimo', 'octavo']

export const MSJ_OBL = 'Este dato es obligatorio: el formato institucional lo exige.'
export const MSJ_SALIR = 'Hay cambios sin guardar en esta pantalla. Si sale ahora se pierden. ¿Sale de todos modos?'
export const MSJ_TEL = 'Escriba un teléfono válido: de 7 a 15 dígitos, con espacios o guiones si quiere.'
export const MSJ_CORREO = 'Escriba un correo válido, como nombre@dominio.com.'

const CORREO = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/
/** Vacío o un correo bien formado. */
export const correoValido = (v) => !v || CORREO.test(v.trim()) || MSJ_CORREO
/** Vacío o un teléfono de 7 a 15 dígitos, con los signos que admiten los servicios. */
export const telefonoValido = (v) => {
  if (!v) return true
  const digitos = (v.match(/\d/g) || []).length
  return (/^\+?[\d\s()-]+$/.test(v) && digitos >= 7 && digitos <= 15) || MSJ_TEL
}

/** Las dos iniciales del nombre, sin artículos ni preposiciones. */
export const iniciales = (n) => String(n || '').split(/\s+/)
  .filter((w) => w && !['de', 'del', 'la', 'las', 'los', 'el', 'y'].includes(w.toLowerCase()))
  .slice(0, 2).map((w) => w[0].toUpperCase()).join('')

/** Sin tildes ni mayúsculas, para los buscadores y los filtros. */
export const normal = (t) => String(t ?? '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase().trim()

/** El periodo de la práctica: «03/08/2026 – 27/11/2026». */
export const periodo = (inicio, fin) => (inicio ? `${fecha(inicio)} – ${fecha(fin)}` : '—')

/** Lo que dice el token de acceso: el rol, el programa y el registro de estudiante. */
export function leerClaims(token) {
  try {
    const parte = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    const bytes = Uint8Array.from(atob(parte), (c) => c.charCodeAt(0))
    return JSON.parse(new TextDecoder().decode(bytes))
  } catch {
    return {}
  }
}
