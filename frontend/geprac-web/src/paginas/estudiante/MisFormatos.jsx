/**
 * P-09 · Mis formatos (CU-08, RF-14). Los cuatro formatos institucionales de
 * una inscripción avalada, emitidos en PDF desde sus plantillas: el estudiante
 * los genera, los genera de nuevo si hace falta y los descarga tal como se
 * emitieron. Antes del aval se presentan deshabilitados, con el estado actual
 * de la inscripción (excepción 2.1); una inscripción ajena no se presenta
 * (excepción 2.2).
 *
 * @author José Fernando Rincón Barrios
 */
import { useCallback, useEffect, useState } from 'react'
import { Link, Navigate, useParams } from 'react-router-dom'
import { consultarFormatos, consultarHistorial, descargarFormato, emitirFormatos } from '../../api/legalizacion'
import { useAvisar, useConfirmar } from '../../sesion/contexto'
import { ESTADOS, fecha } from '../../lib/formato'
import { guardarArchivo } from '../../lib/descarga'
import Marco from '../../componentes/Marco'
import { Aviso, Boton, Cargando } from '../../componentes/controles'

const FORMATOS = [['PR-01', 'Hoja de vida del estudiante'], ['PR-02', 'Acta de conocimiento de los términos'],
  ['PR-04', 'Solicitud y aprobación de práctica'], ['PR-05', 'Acta de compromiso']]
const AJENA = { tipo: 'warn', negrita: 'Esa inscripción no es suya:', texto: 'el software no presenta sus formatos. Estas son sus prácticas.' }
const masReciente = (a, b) => String(b.semestre).localeCompare(String(a.semestre))

export default function MisFormatos() {
  const { id } = useParams()
  const avisar = useAvisar()
  const confirmar = useConfirmar()
  const [estado, setEstado] = useState({ lista: false, inscripciones: [], error: null })
  // los formatos de la inscripción que se está viendo, con la inscripción a la que pertenecen
  const [consulta, setConsulta] = useState({ para: null, formatos: null })
  const [fallo, setFallo] = useState(null)
  const [espera, setEspera] = useState(null)

  useEffect(() => {
    let vigente = true
    consultarHistorial()
      .then((h) => vigente && setEstado({ lista: true, inscripciones: h.filter((x) => x.inscripcionId).sort(masReciente), error: null }))
      .catch((e) => vigente && setEstado({ lista: true, inscripciones: [], error: e.mensaje }))
    return () => { vigente = false }
  }, [])

  const todas = estado.inscripciones
  const avaladas = todas.filter((x) => x.estado === 'AVALADA')
  const elegida = id ? todas.find((x) => String(x.inscripcionId) === id) : avaladas[0] || todas[0]
  const reciente = todas[0]

  const cargarFormatos = useCallback((insc) => {
    consultarFormatos(insc.inscripcionId)
      .then((formatos) => setConsulta({ para: insc.inscripcionId, formatos }))
      .catch((e) => setFallo({ negrita: 'No fue posible consultar sus formatos.', texto: e.mensaje }))
  }, [])

  useEffect(() => {
    if (elegida?.estado === 'AVALADA') cargarFormatos(elegida)
  }, [elegida, cargarFormatos])

  const marco = (contenido) => (
    <Marco titulo="Mis formatos" ctx={elegida ? `Semestre ${elegida.semestre}` : 'Mis formatos'} menu="/mis-formatos">{contenido}</Marco>
  )
  if (!estado.lista) return marco(<Cargando />)
  if (estado.error) return marco(<Aviso tipo="err" negrita="No fue posible consultar sus inscripciones.">{estado.error}</Aviso>)
  if (id && !elegida) return <Navigate to="/mis-practicas" replace state={{ aviso: AJENA }} />
  if (!elegida) {
    return marco(<Aviso tipo="info" negrita="Todavía no tiene inscripciones.">Los formatos se emiten cuando una inscripción queda avalada.</Aviso>)
  }

  const cab = <h3 className="sec">Práctica {elegida.ordenPractica} · {elegida.nombrePractica} · Semestre {elegida.semestre}</h3>
  const otras = avaladas.filter((x) => x.inscripcionId !== elegida.inscripcionId)
  const enlacesOtras = otras.length > 0 && (
    <p className="ayuda">Otras inscripciones avaladas: {otras.map((x, k) => (
      <span key={x.inscripcionId}>{k > 0 && ' · '}<Link className="acc" to={`/mis-formatos/${x.inscripcionId}`}>práctica {x.ordenPractica}, semestre {x.semestre}</Link></span>
    ))}.</p>
  )

  // excepción 2.1: antes del aval, los cuatro formatos deshabilitados y el estado actual
  if (elegida.estado !== 'AVALADA') {
    const e = ESTADOS[elegida.estado.toLowerCase()]
    return marco(
      <>
        {cab}
        <Aviso tipo="info" negrita="Los formatos se habilitan cuando la Dirección del Programa avale la inscripción.">
          Estado actual: «{e.texto}», {e.sentido}.
        </Aviso>
        <div className="docs">
          {FORMATOS.map(([codigo, nombre]) => (
            <div className="doc off" key={codigo}>
              <span className="ic" aria-hidden="true">PDF</span>
              <div className="txt"><b>{nombre}</b><span>{codigo} · disponible tras el aval</span></div>
            </div>
          ))}
        </div>
        {enlacesOtras}
      </>,
    )
  }

  const formatos = consulta.para === elegida.inscripcionId ? consulta.formatos : null
  const emitidos = formatos?.every((f) => f.id) ?? false
  const emitidoEl = formatos?.find((f) => f.fechaEmision)?.fechaEmision

  const generar = async () => {
    if (emitidos && !(await confirmar({ texto: `Los cuatro formatos se generan de nuevo con la plantilla vigente y reemplazan a los emitidos el ${fecha(emitidoEl)}. ¿Confirma la generación?`, si: 'Generar de nuevo' }))) return
    setEspera('generar')
    setFallo(null)
    try {
      setConsulta({ para: elegida.inscripcionId, formatos: await emitirFormatos(elegida.inscripcionId) })
      avisar('Los cuatro formatos se emitieron en PDF: ya puede descargarlos.', 'ok')
    } catch (e) {
      setFallo({ negrita: 'No se generó ningún formato.', texto: e.mensaje })
      avisar('No se generó ningún formato: revise el mensaje de la pantalla.', 'err')
    } finally {
      setEspera(null)
    }
  }

  const descargar = async (f) => {
    setEspera(f.tipo)
    try {
      guardarArchivo(await descargarFormato(f.id), f.archivo, 'application/pdf')
      avisar(`Se descargó «${f.tipo} · ${f.nombre}», emitido el ${fecha(f.fechaEmision)}.`, 'ok')
    } catch (e) {
      avisar(`No se descargó «${f.tipo} · ${f.nombre}». ${e.mensaje}`, 'err')
    } finally {
      setEspera(null)
    }
  }

  const descargarTodos = async () => {
    setEspera('todos')
    try {
      for (const f of formatos) {
        guardarArchivo(await descargarFormato(f.id), f.archivo, 'application/pdf')
        await new Promise((r) => setTimeout(r, 400))
      }
      avisar('Se descargaron los cuatro formatos.', 'ok')
    } catch (e) {
      avisar(`No se completó la descarga. ${e.mensaje}`, 'err')
    } finally {
      setEspera(null)
    }
  }

  const recordatorio = reciente && reciente.inscripcionId !== elegida.inscripcionId && reciente.estado !== 'AVALADA' && (
    <Aviso tipo="info">
      Los formatos de la práctica {reciente.ordenPractica} (semestre {reciente.semestre}) se habilitan cuando la Dirección del
      Programa avale esa inscripción; hoy está «{ESTADOS[reciente.estado.toLowerCase()].texto.toLowerCase()}».
    </Aviso>
  )

  return marco(
    <>
      {recordatorio}
      {cab}
      {fallo && <Aviso tipo="err" negrita={fallo.negrita}>{fallo.texto}</Aviso>}
      <Aviso tipo="ok" negrita={`Inscripción avalada el ${fecha(elegida.avaladaEn)} por la Dirección del Programa.`}>
        {emitidos ? 'Sus formatos están listos para imprimir y firmar.' : 'Genere sus formatos para imprimirlos y firmarlos.'}
      </Aviso>
      {!formatos && !fallo ? <Cargando texto="Consultando sus formatos…" /> : (
        <div className="docs">
          {(formatos || []).map((f) => (
            <div className="doc" key={f.tipo}>
              <span className="ic" aria-hidden="true">PDF</span>
              <div className="txt">
                <b>{f.nombre}</b>
                <span>{f.tipo} · {f.id ? `emitido el ${fecha(f.fechaEmision)} · plantilla v${f.version}` : 'sin emitir'}</span>
              </div>
              <Boton tipo="s" disabled={!f.id || !!espera} espera={espera === f.tipo} textoEspera="Descargando…"
                aria-label={`Descargar ${f.tipo} ${f.nombre}`} aria-describedby={f.id ? undefined : 'ay-generar'}
                onClick={() => descargar(f)}>Descargar</Boton>
            </div>
          ))}
        </div>
      )}
      {formatos && !emitidos && <p className="ayuda" id="ay-generar">Genere primero los cuatro formatos; después podrá descargarlos uno por uno.</p>}
      {formatos && (
        <div className="acciones der">
          {emitidos ? (
            <>
              <Boton tipo="g" disabled={!!espera} espera={espera === 'generar'} textoEspera="Emitiendo…" onClick={generar}>Generar de nuevo</Boton>
              <Boton tipo="p" disabled={!!espera} espera={espera === 'todos'} textoEspera="Descargando…" onClick={descargarTodos}>Descargar los cuatro</Boton>
            </>
          ) : (
            <Boton tipo="p" disabled={!!espera} espera={espera === 'generar'} textoEspera="Emitiendo…" onClick={generar}>Generar los cuatro formatos</Boton>
          )}
        </div>
      )}
      {enlacesOtras}
    </>,
  )
}
