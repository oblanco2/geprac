/**
 * P-19 · Historial del programa o de mis prácticas (CU-09, RF-15). La consulta
 * por estudiante, práctica, institución y semestre, con el estado de cada
 * inscripción. Es el mismo caso de uso con el alcance de cada rol: la Dirección
 * ve todas las inscripciones de su programa y el tutor las de las prácticas que
 * tuvo designadas. Ningún filtro amplía ese alcance, porque lo fija el servicio.
 * El historial se exporta en CSV con los filtros aplicados.
 *
 * @author José Fernando Rincón Barrios
 */
import { useEffect, useState } from 'react'
import { consultarHistorial } from '../../api/legalizacion'
import { useAvisar, useSesion } from '../../sesion/contexto'
import { ESTADOS, fecha, normal } from '../../lib/formato'
import { guardarArchivo } from '../../lib/descarga'
import { useNombrePrograma } from '../../lib/programa'
import Marco from '../../componentes/Marco'
import { Aviso, Boton, Cargando, Entrada, Estado, Seleccion, Tabla } from '../../componentes/controles'

const COLUMNAS = [{ l: 'Estudiante' }, { l: 'Semestre', w: 's' }, { l: 'Práctica', w: 's' }, { l: 'Institución' }, { l: 'Estado', w: 'm' }, { l: 'Avalada', w: 'm' }]
const PLURAL = { AVALADA: ['avalada', 'avaladas'], APROBADA: ['aprobada', 'aprobadas'], ENVIADA: ['enviada', 'enviadas'],
  DEVUELTA: ['devuelta', 'devueltas'], BORRADOR: ['en borrador', 'en borrador'] }

export default function HistorialPracticas() {
  const { cuenta, claims } = useSesion()
  const avisar = useAvisar()
  const tutor = cuenta?.rol === 'TUTOR'
  const programa = useNombrePrograma(tutor ? null : claims.programa)
  const [estado, setEstado] = useState({ lista: false, historial: [], error: null })
  const [filtro, setFiltro] = useState({ semestre: '', practica: '', institucion: '', buscar: '' })

  useEffect(() => {
    let vigente = true
    consultarHistorial()
      .then((historial) => vigente && setEstado({ lista: true, historial, error: null }))
      .catch((e) => vigente && setEstado({ lista: true, historial: [], error: e.mensaje }))
    return () => { vigente = false }
  }, [])

  const titulo = tutor ? 'Historial de mis prácticas' : 'Historial del programa'
  const marco = (contenido) => (
    <Marco titulo={titulo} ctx={tutor ? 'Prácticas designadas' : programa || 'Historial del programa'} menu="/historial">{contenido}</Marco>
  )
  if (!estado.lista) return marco(<Cargando />)
  if (estado.error) return marco(<Aviso tipo="err" negrita="No fue posible consultar el historial.">{estado.error}</Aviso>)

  const todas = estado.historial
  const semestres = [...new Set(todas.map((h) => h.semestre))].sort().reverse().map((s) => [s, s])
  const practicas = [...new Map(todas.map((h) => [String(h.ordenPractica), `${h.ordenPractica} · ${h.nombrePractica}`])).entries()]
    .sort((a, b) => Number(a[0]) - Number(b[0]))
  const instituciones = [...new Set(todas.map((h) => h.institucion).filter(Boolean))].sort().map((n) => [n, n])
  const texto = normal(filtro.buscar)
  const visibles = todas.filter((h) => (!filtro.semestre || h.semestre === filtro.semestre)
    && (!filtro.practica || String(h.ordenPractica) === filtro.practica)
    && (!filtro.institucion || h.institucion === filtro.institucion)
    && (!texto || normal(`${h.estudiante} ${h.documento} ${h.documento.replace(/\D/g, '')}`).includes(texto)))

  const n = visibles.length
  const partes = Object.keys(PLURAL).map((k) => [k, visibles.filter((h) => h.estado === k).length]).filter(([, x]) => x)
    .map(([k, x]) => `${x} ${PLURAL[k][x === 1 ? 0 : 1]}`)

  const exportar = () => {
    const celda = (v) => `"${String(v ?? '').replace(/"/g, '""')}"`
    const filas = [['Estudiante', 'Documento', 'Semestre', 'Práctica', 'Institución', 'Estado', 'Avalada'],
      ...visibles.map((h) => [h.estudiante, h.documento, h.semestre, `${h.ordenPractica} · ${h.nombrePractica}`, h.institucion || '',
        ESTADOS[h.estado.toLowerCase()].texto, h.avaladaEn ? fecha(h.avaladaEn) : ''])]
    guardarArchivo('﻿' + filas.map((f) => f.map(celda).join(';')).join('\r\n'), 'GEPRAC_historial.csv', 'text/csv;charset=utf-8')
    avisar('El historial se exportó con los filtros que estén aplicados.', 'ok')
  }

  return marco(
    <>
      {tutor && (
        <Aviso tipo="info">Historial de las prácticas que tiene designadas: solo aparecen las inscripciones enviadas de esas prácticas, en los semestres en que tuvo la designación.</Aviso>
      )}
      <div className="filtros">
        <Seleccion etiqueta="Semestre" opciones={semestres} vacia="Todos" value={filtro.semestre}
          onChange={(e) => setFiltro((f) => ({ ...f, semestre: e.target.value }))} />
        <Seleccion etiqueta="Práctica" opciones={practicas} vacia="Todas" value={filtro.practica}
          onChange={(e) => setFiltro((f) => ({ ...f, practica: e.target.value }))} />
        <Seleccion etiqueta="Institución" opciones={instituciones} vacia="Todas" value={filtro.institucion}
          onChange={(e) => setFiltro((f) => ({ ...f, institucion: e.target.value }))} />
        <Entrada etiqueta="Buscar estudiante" tipo="search" placeholder="Nombre o documento" value={filtro.buscar}
          onChange={(e) => setFiltro((f) => ({ ...f, buscar: e.target.value }))} />
        <span className="empuja"><Boton tipo="s" disabled={!n} onClick={exportar}>Exportar</Boton></span>
      </div>
      <Tabla titulo="Historial de inscripciones" columnas={COLUMNAS}
        vacio={todas.length ? undefined : tutor ? 'Todavía no hay inscripciones enviadas de las prácticas que tiene designadas.'
          : 'Todavía no hay inscripciones en el programa.'}
        filas={visibles.map((h) => ({ clave: h.inscripcionId, celdas: [
          <span key="e"><b>{h.estudiante}</b><span className="sub">{h.documento}</span></span>,
          h.semestre, <span key="p" className="num">{h.ordenPractica}</span>, h.institucion || '—',
          <Estado key="s" estado={h.estado} />, h.avaladaEn ? fecha(h.avaladaEn) : '—',
        ] }))} />
      <p className="pie conteo" aria-live="polite">{n} {n === 1 ? 'inscripción' : 'inscripciones'}{partes.length ? ` · ${partes.join(' · ')}` : ''}</p>
    </>,
  )
}
