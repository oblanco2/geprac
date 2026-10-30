/**
 * P-14 · Instituciones receptoras (CU-03, RF-05). El catálogo de escenarios de
 * práctica, con buscador y filtros de ciudad y estado —el de estado se repite
 * al pie de la tabla, con el número de instituciones de cada uno, junto a la
 * paginación—; de él escoge el estudiante en el primer paso de su
 * inscripción. Una institución con
 * inscripciones no se elimina: se marca inactiva para retirarla de la
 * selección (variación 2.2).
 *
 * @author José Fernando Rincón Barrios
 */
import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { actualizarInstitucion, eliminarInstitucion, listarInstituciones } from '../../api/legalizacion'
import { useAvisar, useConfirmar } from '../../sesion/contexto'
import { contarPor, normal } from '../../lib/formato'
import Marco from '../../componentes/Marco'
import { Aviso, Boton, Cargando, Entrada, Estado, NotaFiltros, Seleccion, TablaPaginada } from '../../componentes/controles'

const COLUMNAS = [{ l: 'Razón social' }, { l: 'NIT', w: 'l' }, { l: 'Ciudad', w: 'm' }, { l: 'Contactos', w: 's' }, { l: 'Estado', w: 's' }, { l: '', w: 'l' }]
const ESTADOS_CATALOGO = [['activa', 'Activa'], ['inactiva', 'Inactiva']]
const BOTONES = [['activa', 'Activas'], ['inactiva', 'Inactivas']]

export default function Instituciones() {
  const navigate = useNavigate()
  const avisar = useAvisar()
  const confirmar = useConfirmar()
  const [estado, setEstado] = useState({ lista: false, instituciones: [], error: null })
  const [filtro, setFiltro] = useState({ buscar: '', ciudad: '', estado: '' })
  const [ocupada, setOcupada] = useState(null)

  const cargar = useCallback(() => listarInstituciones()
    .then((instituciones) => setEstado({ lista: true, instituciones, error: null }))
    .catch((e) => setEstado({ lista: true, instituciones: [], error: e.mensaje })), [])

  useEffect(() => { cargar() }, [cargar])

  const eliminar = async (i) => {
    const contactos = i.contactos.length === 1 ? 'su contacto' : `sus ${i.contactos.length} contactos`
    if (!await confirmar({ texto: `Se eliminará «${i.razonSocial}» con ${contactos}. Esta acción no se deshace. ¿Confirma la eliminación?`,
      peligro: true, si: 'Eliminar' })) return
    setOcupada(i.id)
    try {
      await eliminarInstitucion(i.id)
      avisar(`Se eliminó «${i.razonSocial}» del catálogo.`, 'ok')
      await cargar()
    } catch (e) {
      if (e.estado !== 409) avisar(e.mensaje, 'err')
      else if (!i.activa) await confirmar({ titulo: 'No se puede eliminar', soloCerrar: true,
        texto: `${e.mensaje} Ya está inactiva, así que no aparece en la selección del estudiante.` })
      else if (await confirmar({ titulo: 'No se puede eliminar', texto: e.mensaje, si: 'Marcar como inactiva' })) {
        try {
          await actualizarInstitucion(i.id, { ...i, activa: false })
          avisar(`«${i.razonSocial}» quedó inactiva: ya no aparece en la selección del estudiante.`, 'ok')
          await cargar()
        } catch (x) {
          avisar(x.mensaje, 'err')
        }
      }
    } finally {
      setOcupada(null)
    }
  }

  const marco = (contenido) => <Marco titulo="Instituciones receptoras" ctx="Catálogo de instituciones" menu="/instituciones">{contenido}</Marco>
  if (!estado.lista) return marco(<Cargando />)
  if (estado.error) return marco(<Aviso tipo="err" negrita="No fue posible consultar el catálogo.">{estado.error}</Aviso>)

  const buscar = normal(filtro.buscar)
  const ciudades = [...new Set(estado.instituciones.map((i) => i.ciudad).filter(Boolean))]
    .sort((a, b) => a.localeCompare(b, 'es')).map((c) => [c, c])
  const base = estado.instituciones.filter((i) => (!buscar || normal(`${i.razonSocial} ${i.nit || ''}`).includes(buscar))
    && (!filtro.ciudad || i.ciudad === filtro.ciudad))
  const visibles = base.filter((i) => !filtro.estado || (filtro.estado === 'activa') === i.activa)
  return marco(
    <>
      <NotaFiltros>
        Puede buscar por <b>nombre</b> o <b>NIT</b>, y filtrar por <b>ciudad</b> y <b>estado</b>. El estado también se escoge con los botones
        al pie de la tabla, que dicen cuántas hay de cada uno.
      </NotaFiltros>
      <div className="filtros">
        <Entrada etiqueta="Buscar por nombre o NIT" tipo="search" placeholder="Ej.: Las Américas o 890.205" value={filtro.buscar}
          onChange={(e) => setFiltro((f) => ({ ...f, buscar: e.target.value }))} />
        <Seleccion etiqueta="Ciudad" opciones={ciudades} vacia="Todas" value={filtro.ciudad}
          onChange={(e) => setFiltro((f) => ({ ...f, ciudad: e.target.value }))} />
        <Seleccion etiqueta="Estado" opciones={ESTADOS_CATALOGO} vacia="Todas" value={filtro.estado}
          onChange={(e) => setFiltro((f) => ({ ...f, estado: e.target.value }))} />
        <span className="empuja"><Boton tipo="p" onClick={() => navigate('/instituciones/nueva')}>Nueva institución</Boton></span>
      </div>
      <TablaPaginada titulo="Catálogo de instituciones receptoras" columnas={COLUMNAS} firma={JSON.stringify(filtro)}
        estados={BOTONES} conteos={contarPor(base, (i) => (i.activa ? 'activa' : 'inactiva'))} total={base.length}
        estado={filtro.estado} alCambiarEstado={(e) => setFiltro((f) => ({ ...f, estado: e }))}
        vacio={estado.instituciones.length ? undefined : 'El catálogo todavía no tiene instituciones.'}
        filas={visibles.map((i) => ({ clave: i.id, celdas: [
          <b key="r">{i.razonSocial}</b>, i.nit || <span key="n" className="sub">No registra</span>, i.ciudad, i.contactos.length,
          <Estado key="e" estado={i.activa ? 'activa' : 'inactiva'} texto={i.activa ? 'Activa' : 'Inactiva'} />,
          <div key="a" className="acc-grupo">
            <Link className="acc" to={`/instituciones/${i.id}`} aria-label={`Editar ${i.razonSocial}`}>Editar</Link>
            <button type="button" className="acc quitar" disabled={ocupada != null} onClick={() => eliminar(i)} aria-label={`Eliminar ${i.razonSocial}`}>
              {ocupada === i.id ? <><span className="spin" aria-hidden="true" />Eliminando…</> : 'Eliminar'}
            </button>
          </div>,
        ] }))} />
    </>,
  )
}
