/**
 * P-11 · Revisar inscripción (CU-06, RF-11). Los datos de la inscripción, el
 * contenido precargado y la institución receptora, con las dos salidas de la
 * revisión: aprobarla, o devolverla con el motivo, que es obligatorio. Una
 * inscripción que no está en la bandeja del tutor no se presenta (excepción 3.1).
 *
 * @author José Fernando Rincón Barrios
 */
import { useEffect, useState } from 'react'
import { Navigate, useNavigate, useParams } from 'react-router-dom'
import { listarPendientes, registrarRevision } from '../../api/legalizacion'
import { useAvisar, useConfirmar } from '../../sesion/contexto'
import { SEMESTRES_PLAN, fecha } from '../../lib/formato'
import Marco from '../../componentes/Marco'
import Historial from '../../componentes/Historial'
import { useSalidaProtegida } from '../../componentes/salida'
import { Area, Aviso, Bloque, Boton, Cargando, Estado, Lectura, ListaNum } from '../../componentes/controles'

const FUERA = { tipo: 'warn', negrita: 'Esa inscripción no está en su bandeja de revisión:', texto: 'el software no la presenta. Esta es su bandeja.' }

export default function Revisar() {
  const { id } = useParams()
  const navigate = useNavigate()
  const avisar = useAvisar()
  const confirmar = useConfirmar()
  const [estado, setEstado] = useState({ lista: false, insc: null, error: null })
  const [motivo, setMotivo] = useState('')
  const [error, setError] = useState(null)
  const [espera, setEspera] = useState(null)
  const permitir = useSalidaProtegida(motivo.trim() !== '' && !espera)

  useEffect(() => {
    let vigente = true
    listarPendientes()
      .then((l) => vigente && setEstado({ lista: true, insc: l.find((i) => String(i.id) === id) || null, error: null }))
      .catch((e) => vigente && setEstado({ lista: true, insc: null, error: e.estado === 409 ? null : e.mensaje }))
    return () => { vigente = false }
  }, [id])

  const marco = (contenido) => (
    <Marco titulo="Revisar inscripción" ctx={estado.insc ? `Semestre ${estado.insc.semestre}` : 'Semestre abierto'} menu="/bandeja">{contenido}</Marco>
  )
  if (!estado.lista) return marco(<Cargando />)
  if (estado.error) return marco(<Aviso tipo="err" negrita="No fue posible consultar la inscripción.">{estado.error}</Aviso>)
  if (!estado.insc) return <Navigate to="/bandeja" replace state={{ aviso: FUERA }} />

  const i = estado.insc
  const d = i.datos
  const estudiante = `${d.nombres} ${d.apellidos}`

  const resolver = async (resultado) => {
    if (resultado === 'DEVUELTA' && !motivo.trim()) {
      setError('El motivo es obligatorio cuando la inscripción se devuelve.')
      avisar('Escriba el motivo: sin él la inscripción no se devuelve.', 'err')
      return
    }
    const si = await confirmar(resultado === 'DEVUELTA'
      ? { texto: 'La inscripción vuelve al estudiante con el motivo que escribió. ¿Confirma la devolución?', si: 'Devolver', peligro: true }
      : { texto: 'La inscripción quedará aprobada y pasará a la bandeja de aval de la Dirección. ¿Confirma la aprobación?'
          + (motivo.trim() ? ' El texto que escribió en el motivo no se guarda: el motivo solo se registra al devolver.' : ''),
        si: 'Aprobar', clase: 'ok' })
    if (!si) return
    setEspera(resultado)
    try {
      await registrarRevision(i.id, resultado, resultado === 'DEVUELTA' ? motivo.trim() : null)
      permitir()
      avisar(resultado === 'DEVUELTA' ? `La inscripción de ${estudiante} quedó devuelta con su motivo.`
        : `La inscripción de ${estudiante} quedó aprobada y pasó a la bandeja de aval.`, 'ok')
      navigate('/bandeja')
    } catch (e) {
      setEspera(null)
      if (e.estado === 409 || e.estado === 404) {
        // excepción 7.1: la resolvieron antes desde otra sesión
        permitir()
        navigate('/bandeja', { state: { aviso: { tipo: 'warn', negrita: 'No se registró la revisión.', texto: e.mensaje } } })
        return
      }
      if (e.campos.motivo) setError(e.campos.motivo)
      avisar(`${resultado === 'DEVUELTA' ? 'No se registró la devolución' : 'No se registró la aprobación'}: la inscripción conserva el estado que tenía. ${e.mensaje}`, 'err')
    }
  }

  return marco(
    <>
      <div className="avance">
        <h3 className="sec">{estudiante} · Práctica {i.ordenPractica}</h3>
        <span><Estado estado={i.estado} /> <span className="pill">Recibida el {fecha(i.enviadaEn)}</span></span>
      </div>
      <div className="sep" />
      <Bloque titulo="Estudiante">
        <div className="grid g4">
          <Lectura etiqueta="Documento" valor={`${d.tipoDocumento} ${d.numeroDocumento}`} />
          <Lectura etiqueta="Programa" valor={d.nombrePrograma} />
          <Lectura etiqueta="Semestre" valor={SEMESTRES_PLAN[d.semestreCursado - 1]} />
          <Lectura etiqueta="Celular" tipo="tel" valor={d.celular} />
        </div>
      </Bloque>
      <Bloque titulo="Institución receptora">
        <div className="grid g3">
          <Lectura etiqueta="Institución" valor={d.institucionRazonSocial} />
          <Lectura etiqueta="Contacto" valor={d.contactoNombre} />
          <Lectura etiqueta="Cargo" valor={d.contactoCargo} />
        </div>
      </Bloque>
      <Bloque titulo="Práctica">
        <div className="grid g3">
          <Lectura etiqueta="Práctica" valor={`${i.ordenPractica} · ${d.nombrePractica}`} />
          <Lectura etiqueta="Inicio" valor={fecha(i.fechaInicio)} />
          <Lectura etiqueta="Finalización" valor={fecha(i.fechaFin)} />
        </div>
        <div className="sep" />
        <details className="detalle">
          <summary>{i.objetivos.length} objetivos específicos y {i.actividades.length} actividades, tomados del catálogo del programa</summary>
          <p className="rotulo">Objetivo general</p>
          <p className="txt-largo">{d.objetivoGeneral}</p>
          <p className="rotulo">Objetivos específicos</p>
          <ListaNum items={i.objetivos} etiqueta="Objetivos específicos" />
          <p className="rotulo">Actividades a desarrollar</p>
          <ListaNum items={i.actividades} etiqueta="Actividades a desarrollar" />
        </details>
      </Bloque>
      {i.revisiones.length > 0 && <Bloque titulo="Historial"><Historial insc={i} estudiante={estudiante} /></Bloque>}
      <Bloque titulo="Decisión">
        <Area etiqueta="Motivo de la devolución · obligatorio si devuelve" maxlen={600} largo={motivo.length} maxLength={600}
          placeholder="Escriba qué debe corregir el estudiante…" value={motivo} error={error}
          onChange={(e) => { setMotivo(e.target.value); setError(null) }} />
      </Bloque>
      <div className="acciones der">
        <Boton tipo="g" disabled={!!espera} onClick={() => navigate('/bandeja')}>Volver a la bandeja</Boton>
        <Boton tipo="d" espera={espera === 'DEVUELTA'} textoEspera="Registrando…" disabled={!!espera} onClick={() => resolver('DEVUELTA')}>Devolver</Boton>
        <Boton tipo="ok" espera={espera === 'APROBADA'} textoEspera="Registrando…" disabled={!!espera} onClick={() => resolver('APROBADA')}>Aprobar</Boton>
      </div>
    </>,
  )
}
