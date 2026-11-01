/**
 * Los tres roles del software: cómo se nombran en la barra, qué opciones tiene
 * el menú de cada uno —las de los nueve casos de uso, en el orden del
 * prototipo—, qué pantallas puede abrir y la pantalla con la que empieza.
 *
 * @author José Fernando Rincón Barrios
 */

export const PAPEL = { ESTUDIANTE: 'Estudiante', TUTOR: 'Tutor académico', DIRECTOR: 'Director del programa' }

export const MENUS = {
  ESTUDIANTE: [['/mis-practicas', 'Mis prácticas'], ['/mi-perfil', 'Mi perfil'], ['/mi-hoja-de-vida', 'Mi hoja de vida'],
    ['/inscribir', 'Inscribir práctica'], ['/mi-inscripcion', 'Estado de mi inscripción'], ['/mis-formatos', 'Mis formatos']],
  TUTOR: [['/bandeja', 'Bandeja de revisión'], ['/historial', 'Historial de mis prácticas']],
  DIRECTOR: [['/aval', 'Bandeja de aval'], ['/semestre', 'Preparación del semestre'], ['/practicas', 'Catálogo de prácticas'],
    ['/instituciones', 'Instituciones receptoras'], ['/historial', 'Historial del programa']],
}

/** Si el rol puede abrir la ruta: la de una opción de su menú o una pantalla que cuelga de ella, como /bandeja/12. */
export function puedeAbrir(rol, ruta) {
  return (MENUS[rol] || []).some(([base]) => ruta === base || ruta.startsWith(`${base}/`))
}

/** La primera pantalla de cada rol. */
export function inicioDe(cuenta) {
  if (!cuenta) return '/ingreso'
  return { ESTUDIANTE: '/mis-practicas', TUTOR: '/bandeja', DIRECTOR: '/aval' }[cuenta.rol] || '/ingreso'
}
