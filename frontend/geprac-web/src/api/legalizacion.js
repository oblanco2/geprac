/**
 * Operaciones de MS-02 Legalización de Prácticas que usa el cliente web: las
 * instituciones receptoras (CU-03), la revisión (CU-06), el aval (CU-07), los
 * formatos (CU-08) y el historial (CU-09). Cada función lleva el nombre de su
 * operación en el diseño.
 *
 * @author José Fernando Rincón Barrios
 */
import { legalizacion as api } from './cliente'

const datos = (r) => r.data

export const listarInstituciones = () => api.get('/instituciones').then(datos)
export const crearInstitucion = (institucion) => api.post('/instituciones', institucion).then(datos)
export const actualizarInstitucion = (id, institucion) => api.put(`/instituciones/${id}`, institucion).then(datos)
export const eliminarInstitucion = (id) => api.delete(`/instituciones/${id}`)

export const listarPendientes = () => api.get('/revisiones/pendientes').then(datos)
export const registrarRevision = (id, resultado, motivo) =>
  api.post(`/revisiones/${id}`, { resultado, motivo }).then(datos)

export const listarAprobadas = () => api.get('/avales/pendientes').then(datos)
export const registrarAval = (id) => api.post(`/avales/${id}`).then(datos)

export const consultarFormatos = (inscripcionId) => api.get('/formatos', { params: { inscripcion: inscripcionId } }).then(datos)
export const emitirFormatos = (inscripcionId) => api.post('/formatos', null, { params: { inscripcion: inscripcionId } }).then(datos)
/** El PDF tal como fue emitido, como Blob, para entregarlo con su nombre. */
export const descargarFormato = (formatoId) => api.get(`/formatos/${formatoId}/archivo`, { responseType: 'blob' }).then(datos)

export const consultarHistorial = () => api.get('/historial').then(datos)
