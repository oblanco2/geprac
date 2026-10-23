/**
 * Operaciones de MS-01 Identidad y Perfil Académico que usa el cliente web: la
 * cuenta de quien ingresa, los programas, con cuyo nombre la barra presenta el
 * contexto; para la Dirección del Programa, la lista de cuentas, con que
 * presenta el nombre del tutor que aprobó cada inscripción, y la asignación del
 * rol de cada cuenta (CU-04), y para el estudiante, su perfil y su hoja de vida
 * (CU-01).
 * Cada función lleva el nombre de su operación en el diseño.
 *
 * @author José Fernando Rincón Barrios
 */
import cliente from './cliente'

const datos = (r) => r.data

export const consultarCuenta = () => cliente.get('/usuarios/me').then(datos)
export const listarProgramas = () => cliente.get('/programas').then(datos)
export const listarUsuarios = () => cliente.get('/usuarios').then(datos)
/** El rol de una cuenta: DIRECTOR, TUTOR o ESTUDIANTE; el de director lleva el programa de quien lo asigna. */
export const asignarRol = (id, rol) => cliente.put(`/usuarios/${id}/rol`, { rol }).then(datos)

export const consultarPerfil = () => cliente.get('/estudiantes/me').then(datos)
export const guardarPerfil = (perfil) => cliente.put('/estudiantes/me', perfil).then(datos)
export const consultarHojaVida = () => cliente.get('/estudiantes/me/hoja-vida').then(datos)
export const guardarBloques = (bloques) => cliente.put('/estudiantes/me/hoja-vida', bloques).then(datos)

/** Las entradas de la hoja de vida. Al eliminar, el bloque es formaciones, experiencias o referencias. */
export const crearFormacion = (entrada) => cliente.post('/estudiantes/me/formaciones', entrada).then(datos)
export const crearExperiencia = (entrada) => cliente.post('/estudiantes/me/experiencias', entrada).then(datos)
export const crearReferencia = (entrada) => cliente.post('/estudiantes/me/referencias', entrada).then(datos)
export const actualizarFormacion = (id, entrada) => cliente.put(`/estudiantes/me/formaciones/${id}`, entrada).then(datos)
export const actualizarExperiencia = (id, entrada) => cliente.put(`/estudiantes/me/experiencias/${id}`, entrada).then(datos)
export const actualizarReferencia = (id, entrada) => cliente.put(`/estudiantes/me/referencias/${id}`, entrada).then(datos)
export const eliminarEntrada = (bloque, id) => cliente.delete(`/estudiantes/me/${bloque}/${id}`)
