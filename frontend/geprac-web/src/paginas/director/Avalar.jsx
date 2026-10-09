/**
 * P-18 · Avalar inscripción (CU-07, RF-12). Los datos de la inscripción y la
 * constancia de la aprobación del tutor; al avalarla, el software habilita la
 * emisión de los cuatro formatos. Una inscripción ya avalada se presenta sin la
 * opción de avalar (variación 2.1), y una que no está en la bandeja del
 * programa no se presenta (excepción 3.1).
 *
 * @author José Fernando Rincón Barrios
 */
import { useEffect, useState } from 'react'
import { Navigate, useNavigate, useParams } from 'react-router-dom'
import { listarAprobadas, registrarAval } from '../../api/legalizacion'
import { useAvisar, useConfirmar } from '../../sesion/contexto'
import { SEMESTRES_PLAN, fecha, periodo } from '../../lib/formato'
import { aprobacion, consultarNombres } from '../../lib/tutores'
import Marco from '../../componentes/Marco'
import Historial from '../../componentes/Historial'
import { Aviso, Bloque, Boton, Cargando, Lectura } from '../../componentes/controles'

const FUERA = { tipo: 'warn', negrita: 'Esa inscripción no está en la bandeja de aval de su programa:', texto: 'el software no la presenta. Esta es su bandeja.' }

export default function Avalar() {
  const { id } = useParams()
  const navigate = useNavigate()
  const avisar = useAvisar()
  const confirmar = useConfirmar()
  const [estado, setEstado] = useState({ lista: false, insc: null, nombres: {}, error: null })
  const [espera, setEspera] = useState(false)

  useEffect(() => {
    let vigente = true
    Promise.all([listarAprobadas(), consultarNombres()])
      .then(([l, nombres]) => vigente && setEstado({ lista: true, insc: l.find((i) => String(i.id) === id) || null, nombres, error: null }))
      .catch((e) => vigente && setEstado({ lista: true, insc: null, nombres: {}, error: e.mensaje }))
    return () => { vigente = false }
  }, [id])

  const marco = (contenido) => (
    <Marco titulo="Avalar inscripción" ctx={estado.insc ? `Semestre ${estado.insc.semestre}` : 'Semestre abierto'} menu="/aval">{contenido}</Marco>
  )
  if (!estado.lista) return marco(<Cargando />)
  if (estado.error) return marco(<Aviso tipo="err" negrita="No fue posible consultar la inscripción.">{estado.error}</Aviso>)
  if (!estado.insc) return <Navigate to="/aval" replace state={{ aviso: FUERA }} />

  const i = estado.insc
  const d = i.datos
  const estudiante = `${d.nombres} ${d.apellidos}`
  const aprobada = aprobacion(i)
  const tutor = estado.nombres[aprobada?.revisorId] || 'el tutor académico'
  const avalada = i.estado === 'AVALADA'

  const avalar = async () => {
    const si = await confirmar({ texto: `Al avalar la inscripción de ${estudiante}, el software habilita la emisión de sus cuatro formatos. `
      + 'El aval queda registrado con su nombre y la fecha. ¿Confirma el aval?', si: 'Avalar inscripción', clase: 'ok' })
    if (!si) return
    setEspera(true)
    try {
      await registrarAval(i.id)
      avisar(`La inscripción de ${estudiante} quedó avalada: el estudiante ya puede emitir sus formatos.`, 'ok')
      navigate('/aval')
    } catch (e) {
      setEspera(false)
      if (e.estado === 409 || e.estado === 404) {
        // excepción 7.1: ya no está aprobada; se informa el estado actual
        navigate('/aval', { state: { aviso: { tipo: 'warn', negrita: 'No se registró el aval.', texto: e.mensaje } } })
        return
      }
      avisar(`No se registró el aval: la inscripción conserva el estado aprobada. ${e.mensaje}`, 'err')
    }
  }

  return marco(
    <>
      <div className="avance">
        <h3 className="sec">{estudiante} · Práctica {i.ordenPractica}</h3>
        <span className={`et ${avalada ? 'avalada' : 'aprobada'}`}>{avalada ? 'Avalada' : 'Aprobada por el tutor'}</span>
      </div>
      {aprobada && (
        <Aviso tipo="ok" negrita={`Aprobada por ${tutor} el ${fecha(aprobada.fecha)}.`}>Sin observaciones.</Aviso>
      )}
      {avalada && (
        <Aviso tipo="ok" negrita={`Avalada por ${estado.nombres[i.aval.directorId] || 'la Dirección del Programa'} el ${fecha(i.aval.fecha)}.`}>
          El estudiante ya puede emitir sus formatos.
        </Aviso>
      )}
      <Bloque titulo="Resumen de la inscripción">
        <div className="grid g3">
          <Lectura etiqueta="Estudiante" valor={`${estudiante} · ${d.tipoDocumento} ${d.numeroDocumento}`} />
          <Lectura etiqueta="Programa y semestre" valor={`${d.nombrePrograma} · ${SEMESTRES_PLAN[d.semestreCursado - 1] || d.semestreCursado}`} />
          <Lectura etiqueta="Práctica" valor={`${i.ordenPractica} · ${d.nombrePractica}`} />
          <Lectura etiqueta="Institución" valor={d.institucionRazonSocial} />
          <Lectura etiqueta="Contacto" valor={d.contactoNombre} />
          <Lectura etiqueta="Periodo" valor={periodo(i.fechaInicio, i.fechaFin)} />
        </div>
      </Bloque>
      <Bloque titulo="Historial"><Historial insc={i} estudiante={estudiante} nombres={estado.nombres} /></Bloque>
      {!avalada && (
        <Aviso tipo="warn">Al avalar, el estudiante podrá descargar los cuatro formatos institucionales. La acción queda registrada con su nombre y la fecha.</Aviso>
      )}
      <div className="sep" />
      <div className="acciones der">
        <Boton tipo="g" disabled={espera} onClick={() => navigate('/aval')}>Volver</Boton>
        {!avalada && <Boton tipo="ok" espera={espera} textoEspera="Registrando…" onClick={avalar}>Avalar inscripción</Boton>}
      </div>
    </>,
  )
}
