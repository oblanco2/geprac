/**
 * Los tres roles del software: cómo se nombran en la barra, qué opciones tiene
 * el menú de cada uno —las de los cinco casos de uso del prototipo funcional:
 * CU-03, CU-06, CU-07, CU-08 y CU-09— y la pantalla con la que empieza.
 *
 * @author José Fernando Rincón Barrios
 */

export const PAPEL = { ESTUDIANTE: 'Estudiante', TUTOR: 'Tutor académico', DIRECTOR: 'Director del programa' }

export const MENUS = {
  ESTUDIANTE: [['/mis-practicas', 'Mis prácticas'], ['/mis-formatos', 'Mis formatos']],
  TUTOR: [['/bandeja', 'Bandeja de revisión'], ['/historial', 'Historial de mis prácticas']],
  DIRECTOR: [['/aval', 'Bandeja de aval'], ['/instituciones', 'Instituciones receptoras'], ['/historial', 'Historial del programa']],
}

/** La primera pantalla de cada rol. */
export function inicioDe(cuenta) {
  if (!cuenta) return '/ingreso'
  return { ESTUDIANTE: '/mis-practicas', TUTOR: '/bandeja', DIRECTOR: '/aval' }[cuenta.rol] || '/ingreso'
}
