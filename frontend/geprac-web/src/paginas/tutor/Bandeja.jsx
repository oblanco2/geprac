/**
 * P-10 · Bandeja de revisión (CU-06, RF-11). Las inscripciones enviadas de las
 * prácticas que el tutor tiene designadas en el semestre abierto, de la que
 * lleva más tiempo esperando a la más reciente, con sus filtros y el número de
 * pendientes (variación 2.1). Ningún filtro amplía el alcance: lo fijan las
 * designaciones.
 *
 * @author José Fernando Rincón Barrios
 */
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listarPendientes } from '../../api/legalizacion'
import { fecha } from '../../lib/formato'
import Marco from '../../componentes/Marco'
import { Aviso, Cargando, Estado, Seleccion, Tabla } from '../../componentes/controles'

const COLUMNAS = [{ l: 'Estudiante' }, { l: 'Práctica' }, { l: 'Institución' }, { l: 'Enviada', w: 'm' }, { l: 'Estado', w: 'm' }, { l: '', w: 's' }]
const nombre = (i) => `${i.datos.nombres} ${i.datos.apellidos}`
const practica = (i) => `${i.ordenPractica} · ${i.datos.nombrePractica}`

export default function Bandeja() {
  const [estado, setEstado] = useState({ lista: false, pendientes: [], sinDesignacion: null, error: null })
  const [filtro, setFiltro] = useState({ practica: '', institucion: '' })

  useEffect(() => {
    let vigente = true
    listarPendientes()
      .then((pendientes) => vigente && setEstado({ lista: true, pendientes, sinDesignacion: null, error: null }))
      .catch((e) => vigente && setEstado({ lista: true, pendientes: [], sinDesignacion: e.estado === 409 ? e.mensaje : null,
        error: e.estado === 409 ? null : e.mensaje }))
    return () => { vigente = false }
  }, [])

  const semestre = estado.pendientes[0]?.semestre
  const marco = (contenido) => (
    <Marco titulo="Bandeja de revisión" ctx={semestre ? `Semestre ${semestre}` : 'Semestre abierto'} menu="/bandeja">{contenido}</Marco>
  )
  if (!estado.lista) return marco(<Cargando />)
  if (estado.error) return marco(<Aviso tipo="err" negrita="No fue posible consultar su bandeja.">{estado.error}</Aviso>)

  const todas = estado.pendientes
  const practicas = [...new Map(todas.map((i) => [String(i.practicaId), practica(i)])).entries()]
  const instituciones = [...new Set(todas.map((i) => i.datos.institucionRazonSocial).filter(Boolean))].sort().map((n) => [n, n])
  const visibles = todas.filter((i) => (!filtro.practica || String(i.practicaId) === filtro.practica)
    && (!filtro.institucion || i.datos.institucionRazonSocial === filtro.institucion))
  const n = todas.length

  return marco(
    <>
      {estado.sinDesignacion && (
        <Aviso tipo="info" negrita={estado.sinDesignacion}>Su bandeja se llena cuando le designe una.</Aviso>
      )}
      <div className="filtros">
        <Seleccion etiqueta="Práctica" opciones={practicas} vacia="Todas las asignadas" value={filtro.practica}
          onChange={(e) => setFiltro((f) => ({ ...f, practica: e.target.value }))} />
        <Seleccion etiqueta="Institución" opciones={instituciones} vacia="Todas" value={filtro.institucion}
          onChange={(e) => setFiltro((f) => ({ ...f, institucion: e.target.value }))} />
        <span className="empuja"><span className="pill">{n} {n === 1 ? 'pendiente' : 'pendientes'}</span></span>
      </div>
      <Tabla titulo="Inscripciones de las prácticas designadas" columnas={COLUMNAS}
        vacio={estado.sinDesignacion ? 'La bandeja está vacía: no tiene prácticas designadas en el semestre.'
          : n ? undefined : 'No tiene inscripciones pendientes de revisión.'}
        filas={visibles.map((i) => ({ clave: i.id, celdas: [
          <b key="n">{nombre(i)}</b>, practica(i), i.datos.institucionRazonSocial || '—', fecha(i.enviadaEn), <Estado key="e" estado={i.estado} />,
          <Link key="r" className="acc" to={`/bandeja/${i.id}`} aria-label={`Revisar la inscripción de ${nombre(i)}`}>Revisar</Link>,
        ] }))} />
    </>,
  )
}
