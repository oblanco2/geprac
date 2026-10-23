/**
 * P-12 · Catálogo de prácticas (CU-02, RF-04). Las prácticas del programa del
 * director, ordenadas por su posición en el plan de estudios, con cuántos
 * objetivos y actividades tiene cada una y su estado. Cada programa tiene su
 * propio catálogo, sin dos prácticas en la misma posición. Una práctica con
 * inscripciones no se elimina (excepción 2.2.4.1): se ofrece editarla, y para
 * retirarla se marca inactiva. Editar el catálogo no altera las inscripciones
 * ya creadas: cada una guarda su propia copia del contenido.
 *
 * @author José Fernando Rincón Barrios
 */
import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { eliminarPractica, listarPracticas } from '../../api/legalizacion'
import { useAvisar, useConfirmar, useSesion } from '../../sesion/contexto'
import { useNombrePrograma } from '../../lib/programa'
import Marco from '../../componentes/Marco'
import { Aviso, Boton, Cargando, Estado, Lectura, Tabla } from '../../componentes/controles'

const COLUMNAS = [{ l: 'Orden', w: 'xs' }, { l: 'Nombre' }, { l: 'Objetivos', w: 's' }, { l: 'Actividades', w: 's' }, { l: 'Estado', w: 's' },
  { l: '', w: 'l' }]
const titulo = (p) => `${p.orden} · ${p.nombre}`

export default function Practicas() {
  const navigate = useNavigate()
  const avisar = useAvisar()
  const confirmar = useConfirmar()
  const { claims } = useSesion()
  const programa = useNombrePrograma(claims.programa)
  const [estado, setEstado] = useState({ lista: false, practicas: [], error: null })
  const [ocupada, setOcupada] = useState(null)

  const cargar = useCallback(() => listarPracticas()
    .then((practicas) => setEstado({ lista: true, practicas, error: null }))
    .catch((e) => setEstado({ lista: true, practicas: [], error: e.mensaje })), [])

  useEffect(() => { cargar() }, [cargar])

  // variación 2.2: confirma nombrando la práctica y el servicio comprueba que no tenga inscripciones
  const eliminar = async (p) => {
    if (!await confirmar({ texto: `Se eliminará la práctica «${titulo(p)}» con sus objetivos y actividades. Esta acción no se deshace. `
      + '¿Confirma la eliminación?', peligro: true, si: 'Eliminar' })) return
    setOcupada(p.id)
    try {
      await eliminarPractica(p.id)
      avisar(`Se eliminó la práctica «${titulo(p)}».`, 'ok')
      await cargar()
    } catch (e) {
      if (e.estado !== 409) avisar(`No se eliminó la práctica. ${e.mensaje}`, 'err')
      else if (await confirmar({ titulo: 'No se puede eliminar', texto: e.mensaje, si: 'Editar la práctica' })) navigate(`/practicas/${p.id}`)
    } finally {
      setOcupada(null)
    }
  }

  const marco = (contenido) => <Marco titulo="Catálogo de prácticas" ctx={programa || 'Catálogo de prácticas'} menu="/practicas">{contenido}</Marco>
  if (!estado.lista) return marco(<Cargando />)
  if (estado.error) return marco(<Aviso tipo="err" negrita="No fue posible consultar el catálogo.">{estado.error}</Aviso>)

  return marco(
    <>
      <div className="herramientas">
        <Lectura etiqueta="Programa" valor={programa || claims.programa} />
        <span className="empuja"><Boton tipo="p" onClick={() => navigate('/practicas/nueva')}>Nueva práctica</Boton></span>
      </div>
      {estado.practicas.length > 0 && estado.practicas.length < 8 && (
        <p className="ayuda">El catálogo tiene {estado.practicas.length} de las ocho prácticas del plan.</p>
      )}
      <Tabla titulo="Catálogo de prácticas del programa" columnas={COLUMNAS}
        vacio="El catálogo todavía no tiene prácticas. Use «Nueva práctica»."
        filas={estado.practicas.map((p) => ({ clave: p.id, celdas: [
          <span key="o" className="num">{p.orden}</span>, <b key="n">{p.nombre}</b>, p.objetivos.length, p.actividades.length,
          <Estado key="e" estado={p.activa ? 'activa' : 'inactiva'} texto={p.activa ? 'Activa' : 'Inactiva'} />,
          <div key="a" className="acc-grupo">
            <Link className="acc" to={`/practicas/${p.id}`} aria-label={`Editar la práctica ${p.orden}`}>Editar</Link>
            <button type="button" className="acc quitar" disabled={ocupada != null} onClick={() => eliminar(p)}
              aria-label={`Eliminar la práctica ${p.orden}`}>
              {ocupada === p.id ? <><span className="spin" aria-hidden="true" />Eliminando…</> : 'Eliminar'}
            </button>
          </div>,
        ] }))} />
    </>,
  )
}
