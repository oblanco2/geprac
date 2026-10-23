/**
 * P-05, P-06 y P-07 · Inscribir práctica, pasos 1 a 3 (CU-05, RF-07 a RF-10).
 * El software no le presenta al estudiante una lista para elegir: al entrar
 * le pide a MS-02 la inscripción de la práctica que le corresponde en el
 * semestre abierto, que se crea en borrador con la copia de su perfil, de su
 * hoja de vida y del contenido de la práctica, o se retoma si ya la había
 * iniciado (variación 4.1). El estudiante escoge la institución receptora
 * (paso 1), el contacto que lo acompañará y las fechas (paso 2), revisa el
 * contenido precargado y ajusta sus datos editables, y la envía a revisión del
 * tutor (paso 3). Lo de cada paso se guarda sin cambiar el estado: la nueva
 * sigue en borrador y la devuelta, devuelta, hasta que la envíe (variación 6.1).
 *
 * @author José Fernando Rincón Barrios
 */
import { useCallback, useEffect, useState } from 'react'
import { Navigate, useNavigate, useParams } from 'react-router-dom'
import { actualizarInscripcion, enviarARevision, iniciarInscripcion, listarInstituciones } from '../../api/legalizacion'
import { useAvisar, useConfirmar } from '../../sesion/contexto'
import { MSJ_OBL, ORDINALES, correoValido, fecha, periodo, telefonoValido } from '../../lib/formato'
import Marco from '../../componentes/Marco'
import { BLOQUES_HOJA } from '../../lib/perfil'
import { useSalidaProtegida } from '../../componentes/salida'
import { Aviso, Bloque, Boton, Cargando, Entrada, Lectura, LeyendaObl, ListaNum, Seleccion } from '../../componentes/controles'

const NOMBRES_PASOS = ['Práctica e institución', 'Contacto y fechas', 'Revisión y envío']
const EDITABLE = ['BORRADOR', 'DEVUELTA']

/**
 * La inscripción del semestre y el catálogo de instituciones, con una sola petición a la vez: en
 * desarrollo, React monta dos veces cada pantalla para revisar sus efectos, y dos peticiones simultáneas
 * de la misma inscripción chocarían en el servicio.
 */
let preparando = null
const preparar = () => {
  preparando ??= Promise.all([iniciarInscripcion(), listarInstituciones()]).finally(() => { preparando = null })
  return preparando
}

/** El primer paso que le falta a la inscripción: con ese la retoma el estudiante (variación 4.1). */
const pasoPendiente = (i) => (!i.institucionId ? 1 : !i.contactoId || !i.fechaInicio || !i.fechaFin ? 2 : 3)

/** La salida de «Guardar y salir»: el estado no cambia (variación 6.1). */
const guardadaEnBorrador = (i) => (i.estado === 'DEVUELTA'
  ? 'Los cambios quedaron guardados. La inscripción sigue devuelta hasta que la envíe.'
  : 'La inscripción quedó guardada en borrador. La retoma desde «Inscribir práctica», en el primer paso que le falta completar.')

export default function Inscribir() {
  const { paso } = useParams()
  const navigate = useNavigate()
  const [carga, setCarga] = useState({ lista: false, insc: null, instituciones: [], problema: null })
  const [intento, setIntento] = useState(0)

  useEffect(() => {
    let vigente = true
    preparar()
      .then(([insc, instituciones]) => vigente && setCarga({ lista: true, insc, instituciones, problema: null }))
      .catch((e) => vigente && setCarga({ lista: true, insc: null, instituciones: [], problema: e }))
    return () => { vigente = false }
  }, [intento])

  const ponerInsc = useCallback((insc) => setCarga((c) => ({ ...c, insc })), [])
  const insc = carga.insc
  const titulo = `Inscribir práctica · paso ${paso && ['1', '2', '3'].includes(paso) ? paso : insc ? pasoPendiente(insc) : 1}`
  const marco = (contenido) => (
    <Marco titulo={titulo} ctx={insc ? `Semestre ${insc.semestre}` : 'Inscribir práctica'} menu="/inscribir">{contenido}</Marco>
  )

  if (!carga.lista) return marco(<Cargando texto="Preparando su inscripción…" />)
  const e = carga.problema
  if (e) {
    // excepción 3.1: el perfil o la hoja de vida están incompletos; lleva a donde se diligencia lo que falta
    if (e.estado === 409 && e.faltan) {
      const personal = e.faltan.some((f) => !BLOQUES_HOJA.includes(f))
      return <Navigate to={personal ? '/mi-perfil' : '/mi-hoja-de-vida'} replace state={{ aviso: { tipo: 'warn',
        negrita: 'Para inscribir su práctica, primero complete su perfil.',
        texto: `Falta${e.faltan.length === 1 ? '' : 'n'}: ${e.faltan.join(', ')}.` } }} />
    }
    // excepciones 2.1 y 2.2: sin semestre abierto, o sin la práctica en el catálogo
    if (e.estado === 409) {
      return marco(
        <>
          <Aviso tipo="warn" negrita="No se puede inscribir la práctica por ahora.">{e.mensaje}</Aviso>
          <div className="acciones der"><Boton tipo="g" onClick={() => navigate('/mis-practicas')}>Volver a Mis prácticas</Boton></div>
        </>,
      )
    }
    return marco(
      <>
        <Aviso tipo="err" negrita="No fue posible preparar su inscripción.">{e.mensaje}</Aviso>
        <div className="acciones der">
          <Boton tipo="g" onClick={() => navigate('/mis-practicas')}>Volver a Mis prácticas</Boton>
          <Boton tipo="p" onClick={() => { setCarga((c) => ({ ...c, lista: false })); setIntento((n) => n + 1) }}>Intentar de nuevo</Boton>
        </div>
      </>,
    )
  }

  // variación 4.1.1.1: ya la había enviado; no se crea otra y se presenta su estado
  if (!EDITABLE.includes(insc.estado)) {
    return <Navigate to="/mi-inscripcion" replace state={{ aviso: { tipo: 'info',
      negrita: `Su inscripción de la práctica ${insc.ordenPractica} ya fue enviada el ${fecha(insc.enviadaEn)}:`,
      texto: `no se puede crear otra en el semestre ${insc.semestre}. Este es su estado.` } }} />
  }
  // desde el menú, el borrador se retoma en el primer paso que le falta (variación 4.1)
  if (!['1', '2', '3'].includes(paso)) {
    const n = insc.estado === 'BORRADOR' ? pasoPendiente(insc) : 1
    return <Navigate to={`/inscribir/${n}`} replace state={n > 1 ? { aviso: { tipo: 'info',
      negrita: 'Se retoma la inscripción que dejó en borrador,', texto: 'en el primer paso que le falta completar.' } } : null} />
  }

  const props = { insc, ponerInsc, instituciones: carga.instituciones }
  if (paso === '1') return marco(<PasoInstitucion {...props} />)
  if (paso === '2') return marco(<PasoEscenario {...props} />)
  return marco(<PasoRevision {...props} />)
}

/** El indicador de los tres pasos de la inscripción. */
function Pasos({ actual }) {
  return (
    <ol className="pasos" aria-label="Pasos de la inscripción">
      {NOMBRES_PASOS.map((n, i) => (i + 1 < actual ? (
        <li key={n} className="paso hecho">{i + 1} · {n} <span aria-hidden="true">✓</span><span className="sr">, cumplido</span></li>
      ) : (
        <li key={n} className="paso" aria-current={i + 1 === actual ? 'step' : undefined}>{i + 1} · {n}</li>
      )))}
    </ol>
  )
}

/** Variación 16.2: la inscripción devuelta lleva el motivo del tutor en cada paso, hasta que se reenvíe. */
function Correccion({ insc }) {
  if (insc.estado !== 'DEVUELTA') return null
  const devolucion = [...insc.revisiones].reverse().find((r) => r.resultado === 'DEVUELTA')
  return (
    <Aviso tipo="err" negrita={`Inscripción devuelta por el tutor académico el ${fecha(devolucion?.fecha)}.`}>
      «{insc.motivoDevolucion}» Corrija lo que se indica y vuelva a enviarla desde el paso 3.
    </Aviso>
  )
}

/* ------------------------------------------------------------------ paso 1 · P-05 */

function PasoInstitucion({ insc, ponerInsc, instituciones }) {
  const navigate = useNavigate()
  const avisar = useAvisar()
  const d = insc.datos
  const actual = insc.institucionId ? String(insc.institucionId) : ''
  const [elegida, setElegida] = useState(actual)
  const [error, setError] = useState(null)
  const [espera, setEspera] = useState(null)
  const permitir = useSalidaProtegida(!espera && elegida !== actual)

  // las activas del catálogo, y la que ya tenía escogida aunque después la hayan retirado
  const opciones = instituciones.some((i) => i.id === insc.institucionId) || !insc.institucionId ? instituciones
    : [...instituciones, { id: insc.institucionId, razonSocial: d.institucionRazonSocial, nit: d.institucionNit,
      direccion: d.institucionDireccion, ciudad: d.institucionCiudad, representanteLegal: d.institucionRepresentante }]
  const inst = opciones.find((i) => String(i.id) === elegida)

  /** Paso 6: guarda la institución si cambió y devuelve si se pudo seguir. */
  const guardar = async (accion) => {
    setEspera(accion)
    try {
      if (elegida && elegida !== actual) ponerInsc(await actualizarInscripcion(insc.id, { institucionId: Number(elegida) }))
      permitir()
      return true
    } catch (e) {
      setEspera(null)
      if (e.campos.institucionId) setError(e.campos.institucionId)
      avisar(`No se guardó la institución: lo que escogió sigue en pantalla. ${e.mensaje}`, 'err')
      return false
    }
  }

  const continuar = async () => {
    if (!elegida) {
      setError('Seleccione la institución receptora: el formato de solicitud y aprobación la exige.')
      avisar('Seleccione la institución receptora para continuar.', 'err')
      return
    }
    if (await guardar('continuar')) navigate('/inscribir/2')
  }

  const salir = async () => {
    if (await guardar('salir')) {
      avisar(guardadaEnBorrador(insc), 'ok')
      navigate('/mis-practicas')
    }
  }

  return (
    <>
      <Pasos actual={1} />
      <Correccion insc={insc} />
      <Aviso tipo="info">
        Le corresponde la <b>práctica {insc.ordenPractica}</b> de su plan, por estar cursando {ORDINALES[d.semestreCursado - 1]} semestre
        de {d.nombrePrograma}.
      </Aviso>
      <Bloque titulo="Práctica del semestre">
        <div className="grid g2">
          <Lectura etiqueta="Práctica" valor={`${insc.ordenPractica} · ${d.nombrePractica}`} />
          <Lectura etiqueta="Semestre académico" valor={insc.semestre} />
        </div>
        <div className="sep" />
        <p className="rotulo">Objetivo general</p>
        <p className="txt-largo">{d.objetivoGeneral}</p>
      </Bloque>
      <LeyendaObl />
      <Bloque titulo="Institución receptora">
        <div className="grid g2">
          <Seleccion etiqueta="Institución del catálogo" obl vacia="Seleccione la institución" value={elegida} error={error}
            opciones={opciones.map((i) => [String(i.id), i.razonSocial])}
            ayuda="Solo aparecen las instituciones activas. Si la suya no está, pídale al director del programa que la registre."
            onChange={(ev) => { setElegida(ev.target.value); setError(null) }} />
          <Lectura etiqueta="NIT" valor={inst ? inst.nit || 'No registra' : ''} />
        </div>
        <div className="sep" />
        <div className="grid g3">
          <Lectura etiqueta="Dirección" valor={inst?.direccion} />
          <Lectura etiqueta="Ciudad" valor={inst?.ciudad} />
          <Lectura etiqueta="Representante legal" valor={inst?.representanteLegal} />
        </div>
      </Bloque>
      <div className="acciones der">
        <Boton tipo="g" disabled={!!espera} espera={espera === 'salir'} onClick={salir}>Guardar y salir</Boton>
        <Boton tipo="p" disabled={!!espera} espera={espera === 'continuar'} onClick={continuar}>Continuar</Boton>
      </div>
    </>
  )
}

/* ------------------------------------------------------------------ paso 2 · P-06 */

function PasoEscenario({ insc, ponerInsc, instituciones }) {
  const navigate = useNavigate()
  const avisar = useAvisar()
  const d = insc.datos
  const inicial = { contactoId: insc.contactoId ? String(insc.contactoId) : '', fechaInicio: insc.fechaInicio || '', fechaFin: insc.fechaFin || '' }
  const [v, setV] = useState(inicial)
  const [errores, setErrores] = useState({})
  const [espera, setEspera] = useState(false)
  const permitir = useSalidaProtegida(!espera && JSON.stringify(v) !== JSON.stringify(inicial))

  if (!insc.institucionId) {
    return (
      <>
        <Pasos actual={2} />
        <Aviso tipo="warn" negrita="Primero escoja la institución receptora.">
          El contacto se elige entre las personas registradas en la institución.
        </Aviso>
        <div className="acciones der"><Boton tipo="p" onClick={() => navigate('/inscribir/1')}>Ir al paso 1</Boton></div>
      </>
    )
  }

  // los contactos de la institución; si ya no está activa, el que tenía escogido
  const inst = instituciones.find((i) => i.id === insc.institucionId)
  const contactos = inst ? inst.contactos : insc.contactoId ? [{ id: insc.contactoId, nombre: d.contactoNombre, cargo: d.contactoCargo,
    telefono: d.contactoTelefono, celular: d.contactoCelular, correo: d.contactoCorreo }] : []
  const ct = contactos.find((c) => String(c.id) === v.contactoId)
  const dentro = `La fecha debe quedar dentro del semestre ${insc.semestre}: del ${fecha(insc.semestreInicio)} al ${fecha(insc.semestreCierre)}.`

  const cambiar = (k) => (ev) => {
    setV((x) => ({ ...x, [k]: ev.target.value }))
    setErrores((x) => ({ ...x, [k]: null }))
  }

  /** Paso 9 y excepción 9.1: el contacto y las dos fechas, dentro del semestre y en orden. */
  const comprobar = () => {
    const e = {}
    if (!v.contactoId) e.contactoId = 'Seleccione la persona que lo acompañará: el formato de solicitud y aprobación la exige.'
    for (const k of ['fechaInicio', 'fechaFin']) {
      if (!v[k]) e[k] = MSJ_OBL
      else if (v[k] < insc.semestreInicio || v[k] > insc.semestreCierre) e[k] = dentro
    }
    if (!e.fechaInicio && !e.fechaFin && v.fechaFin <= v.fechaInicio) e.fechaFin = 'La fecha de finalización debe ser posterior a la de inicio.'
    setErrores(e)
    return !Object.keys(e).length
  }

  const continuar = async () => {
    if (!comprobar()) {
      avisar('Revise los datos señalados: el paso no se guardó.', 'err')
      return
    }
    setEspera(true)
    try {
      ponerInsc(await actualizarInscripcion(insc.id, { contactoId: Number(v.contactoId), fechaInicio: v.fechaInicio, fechaFin: v.fechaFin }))
      permitir()
      navigate('/inscribir/3')
    } catch (e) {
      setEspera(false)
      setErrores(e.campos)
      avisar(`No se guardó el paso: lo que diligenció sigue en pantalla. ${e.mensaje}`, 'err')
    }
  }

  return (
    <>
      <Pasos actual={2} />
      <Correccion insc={insc} />
      {!contactos.length && (
        <Aviso tipo="warn" negrita="La institución que escogió ya no tiene contactos disponibles.">
          Vuelva al paso 1 y escoja otra, o pídale al director del programa que la actualice.
        </Aviso>
      )}
      <LeyendaObl />
      <Bloque titulo={`Contacto en ${d.institucionRazonSocial}`}>
        <div className="grid g2">
          <Seleccion etiqueta="Persona que lo acompañará" obl vacia="Seleccione el contacto" value={v.contactoId}
            opciones={contactos.map((c) => [String(c.id), c.nombre])} error={errores.contactoId} onChange={cambiar('contactoId')}
            ayuda="Es quien figura como tutor del escenario en el formato de solicitud y aprobación." />
          <Lectura etiqueta="Cargo" valor={ct?.cargo} />
        </div>
        <div className="sep" />
        <div className="grid g3">
          <Lectura etiqueta="Teléfono" tipo="tel" valor={ct?.telefono} />
          <Lectura etiqueta="Celular" tipo="tel" valor={ct?.celular} />
          <Lectura etiqueta="Correo" tipo="email" valor={ct?.correo} />
        </div>
      </Bloque>
      <Bloque titulo="Fechas de la práctica">
        <div className="grid g2">
          <Entrada etiqueta="Fecha de inicio" obl tipo="date" min={insc.semestreInicio} max={insc.semestreCierre}
            value={v.fechaInicio} onChange={cambiar('fechaInicio')} error={errores.fechaInicio} />
          <Entrada etiqueta="Fecha de finalización" obl tipo="date" min={insc.semestreInicio} max={insc.semestreCierre}
            value={v.fechaFin} onChange={cambiar('fechaFin')} error={errores.fechaFin} />
        </div>
        <p className="ayuda">
          La fecha de finalización debe ser posterior a la de inicio, y las dos deben quedar dentro del semestre {insc.semestre}: del{' '}
          {fecha(insc.semestreInicio)} al {fecha(insc.semestreCierre)}.
        </p>
      </Bloque>
      <div className="acciones der">
        <Boton tipo="g" disabled={espera} onClick={() => navigate('/inscribir/1')}>Atrás</Boton>
        <Boton tipo="p" espera={espera} textoEspera="Comprobando…" onClick={continuar}>Continuar</Boton>
      </div>
    </>
  )
}

/* ------------------------------------------------------------------ paso 3 · P-07 */

function PasoRevision({ insc, ponerInsc }) {
  const navigate = useNavigate()
  const avisar = useAvisar()
  const confirmar = useConfirmar()
  const d = insc.datos
  const correccion = insc.estado === 'DEVUELTA'
  const inicial = { direccion: d.direccion || '', celular: d.celular || '', correoPersonal: d.correoPersonal || '' }
  const [v, setV] = useState(inicial)
  const [errores, setErrores] = useState({})
  const [espera, setEspera] = useState(null)
  const sucio = JSON.stringify(v) !== JSON.stringify(inicial)
  const permitir = useSalidaProtegida(!espera && sucio)

  const cambiar = (k) => (ev) => {
    setV((x) => ({ ...x, [k]: ev.target.value }))
    setErrores((x) => ({ ...x, [k]: null }))
  }

  /** Paso 11: sus datos se ajustan solo en la inscripción; los formatos los imprimen, así que no quedan vacíos. */
  const comprobar = () => {
    const e = {}
    for (const k of ['direccion', 'celular', 'correoPersonal']) if (!v[k].trim()) e[k] = MSJ_OBL
    const tel = telefonoValido(v.celular.trim())
    if (!e.celular && tel !== true) e.celular = tel
    const correo = correoValido(v.correoPersonal.trim())
    if (!e.correoPersonal && correo !== true) e.correoPersonal = correo
    setErrores(e)
    return !Object.keys(e).length
  }

  /** Guarda los datos editables si cambiaron; devuelve si se pudo seguir. */
  const guardarDatos = async (accion) => {
    if (!comprobar()) {
      avisar('Revise sus datos señalados: no se guardaron.', 'err')
      return false
    }
    if (!sucio) return true
    setEspera(accion)
    try {
      ponerInsc(await actualizarInscripcion(insc.id, { direccion: v.direccion.trim(), celular: v.celular.trim(),
        correoPersonal: v.correoPersonal.trim() }))
      return true
    } catch (e) {
      setEspera(null)
      setErrores(e.campos)
      avisar(`No se guardaron sus datos: siguen en pantalla. ${e.mensaje}`, 'err')
      return false
    }
  }

  // variación 11.1: antes de volver a un paso anterior se guarda lo que ajustó en sus datos
  const ir = (n) => async () => {
    if (await guardarDatos(`paso${n}`)) {
      permitir()
      navigate(`/inscribir/${n}`)
    }
  }

  const salir = async () => {
    if (await guardarDatos('salir')) {
      permitir()
      avisar(guardadaEnBorrador(insc), 'ok')
      navigate('/mis-practicas')
    }
  }

  // pasos 12 a 15: confirmación, comprobación del servicio y registro del envío
  const enviar = async () => {
    if (!comprobar()) {
      avisar('La inscripción no se envió: corrija los datos señalados.', 'err')
      return
    }
    const si = await confirmar(correccion
      ? { texto: 'La inscripción corregida vuelve al tutor académico. ¿Confirma el reenvío?', si: 'Reenviar' }
      : { texto: 'La inscripción pasará al tutor académico designado para esta práctica y dejará de admitir cambios. ¿Confirma el envío?',
        si: 'Enviar' })
    if (!si || !(await guardarDatos('enviar'))) return
    setEspera('enviar')
    try {
      await enviarARevision(insc.id)
      permitir()
      avisar(correccion ? 'Su inscripción corregida volvió al tutor académico.' : 'Su inscripción quedó enviada al tutor académico.', 'ok')
      navigate('/mi-inscripcion')
    } catch (e) {
      setEspera(null)
      permitir()
      if (e.estado === 409 && e.faltan) {
        // excepción 14.1: lo que falta se diligencia en el paso donde se pide
        navigate(e.faltan.includes('la institución receptora') ? '/inscribir/1' : '/inscribir/2', { state: { aviso: { tipo: 'err',
          negrita: 'La inscripción no se envió: está incompleta.', texto: `Falta ${e.faltan.join(', ')}.` } } })
      } else if (e.estado === 409) {
        // excepción 14.2: ya se había enviado desde otra sesión; se presenta su estado actual
        navigate('/mi-inscripcion', { state: { aviso: { tipo: 'warn', negrita: 'La inscripción no se envió de nuevo.', texto: e.mensaje } } })
      } else {
        // excepción 15.1: el envío no se registró; se puede reintentar
        avisar(`${correccion ? 'No se registró el reenvío: la inscripción sigue devuelta.' : 'No se registró el envío: la inscripción permanece en borrador.'} ${e.mensaje}`, 'err')
      }
    }
  }

  return (
    <>
      <Pasos actual={3} />
      <Correccion insc={insc} />
      <Aviso tipo="info">El contenido académico proviene del catálogo del programa y no es editable. Sus datos personales sí puede corregirlos aquí.</Aviso>
      <Bloque titulo="Contenido de la práctica · del catálogo">
        <p className="rotulo">Objetivo general</p>
        <p className="txt-largo">{d.objetivoGeneral}</p>
        <div className="sep" />
        <p className="rotulo">Objetivos específicos</p>
        <ListaNum items={insc.objetivos} etiqueta="Objetivos específicos" />
        <div className="sep" />
        <p className="rotulo">Actividades a desarrollar</p>
        <ListaNum items={insc.actividades} etiqueta="Actividades a desarrollar" />
        <div className="sep" />
        <div className="grid g2"><Lectura etiqueta="Horario estándar" valor={d.horarioEstandar} /></div>
      </Bloque>
      <Bloque titulo="Institución, contacto y fechas · de los pasos 1 y 2">
        <div className="grid g3">
          <Lectura etiqueta="Institución" valor={d.institucionRazonSocial} />
          <Lectura etiqueta="Contacto" valor={d.contactoNombre ? `${d.contactoNombre} · ${d.contactoCargo}` : ''} />
          <Lectura etiqueta="Periodo" valor={periodo(insc.fechaInicio, insc.fechaFin)} />
        </div>
        <p className="ayuda">
          <button type="button" className="acc" disabled={!!espera} onClick={ir(1)}>Modificar la institución (paso 1)</button>
          {' · '}
          <button type="button" className="acc" disabled={!!espera} onClick={ir(2)}>Modificar el contacto o las fechas (paso 2)</button>
        </p>
      </Bloque>
      <LeyendaObl />
      <Bloque titulo="Sus datos · editables">
        <div className="grid g3">
          <Entrada etiqueta="Dirección" obl maxLength={150} autoComplete="street-address" value={v.direccion}
            onChange={cambiar('direccion')} error={errores.direccion} />
          <Entrada etiqueta="Celular" obl tipo="tel" maxLength={20} autoComplete="tel" value={v.celular}
            onChange={cambiar('celular')} error={errores.celular} />
          <Entrada etiqueta="Correo personal" obl tipo="email" maxLength={150} autoComplete="email" value={v.correoPersonal}
            onChange={cambiar('correoPersonal')} error={errores.correoPersonal} />
        </div>
        <p className="ayuda">Estos cambios solo valen para esta inscripción; para actualizar su perfil use «Mi perfil».</p>
      </Bloque>
      <div className="acciones der">
        <Boton tipo="g" disabled={!!espera} espera={espera === 'paso2'} onClick={ir(2)}>Atrás</Boton>
        <Boton tipo="g" disabled={!!espera} espera={espera === 'salir'} onClick={salir}>
          {correccion ? 'Guardar sin enviar' : 'Guardar como borrador'}
        </Boton>
        <Boton tipo="p" disabled={!!espera} espera={espera === 'enviar'} textoEspera="Enviando…" onClick={enviar}>
          {correccion ? 'Reenviar a revisión' : 'Enviar a revisión'}
        </Boton>
      </div>
    </>
  )
}
