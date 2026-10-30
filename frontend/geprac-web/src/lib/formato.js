/**
 * Formatos y catálogos que comparten las pantallas: las fechas como las
 * presenta el prototipo (dd/mm/aaaa), los cinco estados de la inscripción con
 * su significado, los semestres del plan de estudios, las opciones que
 * imprimen los formatos y las comprobaciones de correo, teléfono y documento.
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

/** Cuántos elementos de la lista hay de cada valor que devuelve clave: { valor: n }. */
export const contarPor = (lista, clave) => lista.reduce((c, x) => ({ ...c, [clave(x)]: (c[clave(x)] || 0) + 1 }), {})

/** Los estados en plural, para los botones del filtro por estado al pie de las tablas. */
export const PLURALES = { BORRADOR: 'En borrador', ENVIADA: 'Enviadas', APROBADA: 'Aprobadas', DEVUELTA: 'Devueltas', AVALADA: 'Avaladas' }

/** Las opciones de un filtro por estado, [valor, texto], en el orden de ESTADOS. */
export const opcionesEstado = (claves = Object.keys(ESTADOS)) => claves.map((k) => [k.toUpperCase(), ESTADOS[k].texto])

export const SEMESTRES_PLAN = ['Primero', 'Segundo', 'Tercero', 'Cuarto', 'Quinto', 'Sexto', 'Séptimo', 'Octavo']
export const ORDINALES = ['primer', 'segundo', 'tercer', 'cuarto', 'quinto', 'sexto', 'séptimo', 'octavo']

/** Las opciones que imprimen los formatos (CU-01, paso 7): las mismas que valida MS-01. */
export const TIPOS_DOCUMENTO = [['C.C.', 'C.C. · Cédula de ciudadanía'], ['T.I.', 'T.I. · Tarjeta de identidad'], ['C.E.', 'C.E. · Cédula de extranjería']]
export const GENEROS = ['Femenino', 'Masculino']
export const ESTADOS_CIVILES = ['Soltero(a)', 'Casado(a)', 'Unión marital de hecho', 'Separado(a)', 'Divorciado(a)', 'Viudo(a)']
export const TIPOS_FORMACION = ['Secundaria', 'Universitaria', 'Otros']

/** La fecha de hoy en el equipo de quien usa el software, como aaaa-mm-dd. */
export const hoy = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

/** El tiempo de una experiencia laboral, como lo pide la hoja de vida: «1 año y 3 meses»; sin retiro, hasta hoy. */
export function duracion(inicio, fin) {
  if (!inicio) return '—'
  const [a1, m1, d1] = inicio.split('-').map(Number)
  const [a2, m2, d2] = (fin || hoy()).split('-').map(Number)
  let meses = (a2 - a1) * 12 + (m2 - m1)
  if (d2 < d1) meses -= 1
  const anios = Math.floor(meses / 12)
  const resto = meses % 12
  const partes = meses < 1 ? ['menos de un mes'] : []
  if (anios > 0) partes.push(`${anios} ${anios === 1 ? 'año' : 'años'}`)
  if (resto > 0) partes.push(`${resto} ${resto === 1 ? 'mes' : 'meses'}`)
  return partes.join(' y ') + (fin ? '' : ' · vigente')
}

export const MSJ_OBL = 'Este dato es obligatorio: el formato institucional lo exige.'
export const MSJ_SALIR = 'Hay cambios sin guardar en esta pantalla. Si sale ahora se pierden. ¿Sale de todos modos?'
export const MSJ_TEL = 'Escriba un teléfono válido: de 7 a 15 dígitos, con espacios o guiones si quiere.'
export const MSJ_CORREO = 'Escriba un correo válido, como nombre@dominio.com.'

const CORREO = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/
/** Vacío o un correo bien formado. */
export const correoValido = (v) => !v || CORREO.test(v.trim()) || MSJ_CORREO
/** El número de documento: de 5 a 12 dígitos, con puntos opcionales entre ellos, como lo valida MS-01. */
export const documentoValido = (v) => {
  const digitos = v.replace(/\D/g, '').length
  return (/^\d+(\.\d+)*$/.test(v) && digitos >= 5 && digitos <= 12)
    || 'Escriba de 5 a 12 dígitos, sin letras ni otros signos; los puntos son opcionales.'
}
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
