/**
 * P-17 · Bandeja de aval (CU-07, RF-12). Las inscripciones del programa que el
 * tutor académico ya aprobó, con el nombre de quien las aprobó y el número de
 * pendientes por avalar, y después las ya avaladas del semestre, para
 * consultarlas (variación 2.1). Ninguna llega aquí sin la revisión del tutor.
 *
 * @author José Fernando Rincón Barrios
 */
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listarAprobadas } from '../../api/legalizacion'
import { aprobacion, consultarNombres } from '../../lib/tutores'
import Marco from '../../componentes/Marco'
import { Aviso, Cargando, Estado, Seleccion, Tabla } from '../../componentes/controles'

const COLUMNAS = [{ l: 'Estudiante' }, { l: 'Práctica' }, { l: 'Institución' }, { l: 'Aprobada por', w: 'l' }, { l: 'Estado', w: 'm' }, { l: '', w: 's' }]
const nombre = (i) => `${i.datos.nombres} ${i.datos.apellidos}`
const practica = (i) => `${i.ordenPractica} · ${i.datos.nombrePractica}`

export default function BandejaAval() {
  const [estado, setEstado] = useState({ lista: false, inscripciones: [], nombres: {}, error: null })
  const [filtro, setFiltro] = useState({ practica: '', institucion: '' })

  useEffect(() => {
    let vigente = true
    Promise.all([listarAprobadas(), consultarNombres()])
      .then(([inscripciones, nombres]) => vigente && setEstado({ lista: true, inscripciones, nombres, error: null }))
      .catch((e) => vigente && setEstado({ lista: true, inscripciones: [], nombres: {}, error: e.mensaje }))
    return () => { vigente = false }
  }, [])

  const semestre = estado.inscripciones[0]?.semestre
  const marco = (contenido) => (
    <Marco titulo="Bandeja de aval" ctx={semestre ? `Semestre ${semestre}` : 'Semestre abierto'} menu="/aval">{contenido}</Marco>
  )
  if (!estado.lista) return marco(<Cargando />)
  if (estado.error) return marco(<Aviso tipo="err" negrita="No fue posible consultar la bandeja de aval.">{estado.error}</Aviso>)

  const todas = estado.inscripciones
  const practicas = [...new Map(todas.map((i) => [String(i.practicaId), practica(i)])).entries()]
  const instituciones = [...new Set(todas.map((i) => i.datos.institucionRazonSocial).filter(Boolean))].sort().map((n) => [n, n])
  const visibles = todas.filter((i) => (!filtro.practica || String(i.practicaId) === filtro.practica)
    && (!filtro.institucion || i.datos.institucionRazonSocial === filtro.institucion))
  const n = todas.filter((i) => i.estado === 'APROBADA').length
  const tutor = (i) => estado.nombres[aprobacion(i)?.revisorId] || 'Tutor académico'

  return marco(
    <>
      <div className="filtros">
        <Seleccion etiqueta="Práctica" opciones={practicas} vacia="Todas" value={filtro.practica}
          onChange={(e) => setFiltro((f) => ({ ...f, practica: e.target.value }))} />
        <Seleccion etiqueta="Institución" opciones={instituciones} vacia="Todas" value={filtro.institucion}
          onChange={(e) => setFiltro((f) => ({ ...f, institucion: e.target.value }))} />
        <span className="empuja"><span className="pill">{n} por avalar</span></span>
      </div>
      <Tabla titulo="Inscripciones aprobadas por el tutor académico" columnas={COLUMNAS}
        vacio={todas.length ? undefined : 'No hay inscripciones aprobadas por el tutor académico pendientes de aval.'}
        filas={visibles.map((i) => ({ clave: i.id, celdas: [
          <b key="n">{nombre(i)}</b>, practica(i), i.datos.institucionRazonSocial || '—', tutor(i), <Estado key="e" estado={i.estado} />,
          <Link key="a" className="acc" to={`/aval/${i.id}`}
            aria-label={`${i.estado === 'APROBADA' ? 'Avalar' : 'Ver'} la inscripción de ${nombre(i)}`}>
            {i.estado === 'APROBADA' ? 'Avalar' : 'Ver'}
          </Link>,
        ] }))} />
    </>,
  )
}
