/**
 * Lo que mide el indicador de completitud del perfil (CU-01, paso 9): los
 * dieciocho datos personales y los cuatro bloques obligatorios de la hoja de
 * vida que exigen los formatos, con el nombre que les da MS-01. La experiencia
 * laboral es opcional en el formato y no cuenta.
 *
 * @author José Fernando Rincón Barrios
 */

/** Los datos personales: [campo del formulario, nombre]. */
export const DATOS_PERSONALES = [['nombres', 'Nombres'], ['apellidos', 'Apellidos'], ['tipoDocumento', 'Tipo de documento'],
  ['numeroDocumento', 'Número de documento'], ['lugarExpedicion', 'Lugar de expedición'], ['fechaNacimiento', 'Fecha de nacimiento'],
  ['lugarNacimiento', 'Lugar de nacimiento'], ['genero', 'Género'], ['estadoCivil', 'Estado civil'], ['eps', 'EPS vigente'],
  ['direccion', 'Dirección'], ['barrio', 'Barrio'], ['ciudad', 'Ciudad'], ['telefonoFijo', 'Teléfono fijo'], ['celular', 'Celular'],
  ['correoPersonal', 'Correo personal'], ['codigoPrograma', 'Programa'], ['semestreActual', 'Semestre que cursa']]

/** Los bloques obligatorios de la hoja de vida. */
export const BLOQUES_HOJA = ['Perfil profesional', 'Herramientas de trabajo', 'Formación académica', 'Referencias personales']

export const TOTAL_PERFIL = DATOS_PERSONALES.length + BLOQUES_HOJA.length
