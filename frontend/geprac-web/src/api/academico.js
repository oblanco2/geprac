/**
 * Operaciones de MS-01 Identidad y Perfil Académico que usa el cliente web: la
 * cuenta de quien ingresa, los programas, con cuyo nombre la barra presenta el
 * contexto, y, para la Dirección del Programa, la lista de cuentas con que
 * presenta el nombre del tutor que aprobó cada inscripción.
 * Cada función lleva el nombre de su operación en el diseño.
 *
 * @author José Fernando Rincón Barrios
 */
import cliente from './cliente'

const datos = (r) => r.data

export const consultarCuenta = () => cliente.get('/usuarios/me').then(datos)
export const listarProgramas = () => cliente.get('/programas').then(datos)
export const listarUsuarios = () => cliente.get('/usuarios').then(datos)
