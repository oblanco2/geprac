/**
 * Operaciones de MS-02 Legalización de Prácticas que usa el cliente web: el
 * catálogo de prácticas (CU-02), las instituciones receptoras (CU-03), el
 * semestre y sus tutores (CU-04), la inscripción (CU-05), la revisión (CU-06),
 * el aval (CU-07), los formatos (CU-08) y el historial (CU-09). Cada función
 * lleva el nombre de su operación en el diseño.
 *
 * @author José Fernando Rincón Barrios
 */
import { legalizacion as api } from './cliente'

const datos = (r) => r.data

export const listarPracticas = () => api.get('/practicas').then(datos)
export const crearPractica = (practica) => api.post('/practicas', practica).then(datos)
export const actualizarPractica = (id, practica) => api.put(`/practicas/${id}`, practica).then(datos)
export const eliminarPractica = (id) => api.delete(`/practicas/${id}`)

export const listarInstituciones = () => api.get('/instituciones').then(datos)
export const crearInstitucion = (institucion) => api.post('/instituciones', institucion).then(datos)
export const actualizarInstitucion = (id, institucion) => api.put(`/instituciones/${id}`, institucion).then(datos)
export const eliminarInstitucion = (id) => api.delete(`/instituciones/${id}`)

/** El semestre abierto, o el último, con las prácticas del programa y su tutor; la lista de cuentas viene de MS-01. */
export const consultarPreparacion = () => api.get('/semestres/preparacion').then(datos)
export const abrirSemestre = (semestre) => api.post('/semestres', semestre).then(datos)
export const cerrarSemestre = (id) => api.post(`/semestres/${id}/cierre`).then(datos)
export const designarTutor = (practicaId, tutorId) => api.post(`/semestres/abierto/practicas/${practicaId}/tutor`, { tutorId }).then(datos)
export const reasignarTutor = (practicaId, tutorId) => api.put(`/semestres/abierto/practicas/${practicaId}/tutor`, { tutorId }).then(datos)

/**
 * La inscripción del semestre abierto: la crea en borrador con la copia del
 * expediente, o devuelve la que el estudiante ya había iniciado.
 */
export const iniciarInscripcion = () => api.post('/inscripciones').then(datos)
/** Lo diligenciado en un paso: lo que no viene, queda como estaba. */
export const actualizarInscripcion = (id, cambios) => api.put(`/inscripciones/${id}`, cambios).then(datos)
export const enviarARevision = (id) => api.post(`/inscripciones/${id}/envio`).then(datos)
export const consultarEstado = () => api.get('/inscripciones/actual').then(datos)

/** La bandeja del tutor con todos sus estados, menos los borradores, para filtrarla por estado. */
export const listarBandeja = () => api.get('/revisiones').then(datos)
export const registrarRevision = (id, resultado, motivo) =>
  api.post(`/revisiones/${id}`, { resultado, motivo }).then(datos)

export const listarAprobadas = () => api.get('/avales/pendientes').then(datos)
export const registrarAval = (id) => api.post(`/avales/${id}`).then(datos)

export const consultarFormatos = (inscripcionId) => api.get('/formatos', { params: { inscripcion: inscripcionId } }).then(datos)
export const emitirFormatos = (inscripcionId) => api.post('/formatos', null, { params: { inscripcion: inscripcionId } }).then(datos)
/** El PDF tal como fue emitido, como Blob, para entregarlo con su nombre. */
export const descargarFormato = (formatoId) => api.get(`/formatos/${formatoId}/archivo`, { responseType: 'blob' }).then(datos)

export const consultarHistorial = () => api.get('/historial').then(datos)
