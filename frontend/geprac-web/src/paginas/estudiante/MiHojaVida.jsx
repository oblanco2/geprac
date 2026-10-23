/**
 * P-04 · Mi hoja de vida (CU-01, RF-03). Los cinco bloques de la hoja de vida
 * institucional, que el software emite ya diligenciada en el formato PR-01: el
 * perfil profesional y las herramientas de trabajo, que se guardan juntos, y
 * las tablas de formación académica, experiencia laboral y referencias
 * personales, cuyas entradas se guardan una por una. La formación se ordena
 * por tipo y año, la experiencia de la más reciente a la más antigua con su
 * tiempo de trabajo, y las referencias llegan hasta tres, el máximo del formato.
 * La hoja de vida se guarda con el registro de datos personales, que debe
 * existir antes.
 *
 * @author José Fernando Rincón Barrios
 */
import { useCallback, useEffect, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { actualizarExperiencia, actualizarFormacion, actualizarReferencia, consultarHojaVida, crearExperiencia, crearFormacion,
  crearReferencia, eliminarEntrada, guardarBloques } from '../../api/academico'
import { useAvisar } from '../../sesion/contexto'
import { TIPOS_FORMACION, duracion, fecha, hoy } from '../../lib/formato'
import Marco from '../../componentes/Marco'
import { BLOQUES_HOJA } from '../../lib/perfil'
import AvancePerfil from '../../componentes/AvancePerfil'
import TablaEditable from '../../componentes/TablaEditable'
import { useSalidaProtegida } from '../../componentes/salida'
import { Area, Aviso, Bloque, Boton, Cargando } from '../../componentes/controles'

const ANIO = new Date().getFullYear()
const ordenFormacion = (a, b) => TIPOS_FORMACION.indexOf(a.tipo) - TIPOS_FORMACION.indexOf(b.tipo) || (b.anio || 9999) - (a.anio || 9999)
const ordenExperiencia = (a, b) => String(b.fechaInicio).localeCompare(String(a.fechaInicio))

const FORMACION = [
  { k: 'tipo', l: 'Tipo', w: 'm', tipo: 'sel', ops: TIPOS_FORMACION, obl: true },
  { k: 'institucion', l: 'Institución', obl: true, largo: 150 },
  { k: 'nombre', l: 'Nombre', obl: true, largo: 150, ph: 'Título o programa' },
  { k: 'anio', l: 'Año', w: 's', tipo: 'number', min: 1950, max: ANIO, ph: 'En curso', ver: (r) => r.anio || 'En curso' },
]
const EXPERIENCIA = [
  { k: 'empresa', l: 'Empresa', obl: true, largo: 150 },
  { k: 'cargo', l: 'Cargo', obl: true, largo: 100 },
  { k: 'jefe', l: 'Jefe inmediato', doble: [{ k: 'jefeInmediato', l: 'Nombre', largo: 150 }, { k: 'cargoJefe', l: 'Cargo', largo: 100 }],
    ver: (r) => (r.jefeInmediato ? `${r.jefeInmediato}${r.cargoJefe ? ` · ${r.cargoJefe}` : ''}` : '—') },
  { k: 'telefonoEmpresa', l: 'Teléfono empresa', w: 'm', tipo: 'tel', largo: 20 },
  { k: 'tiempo', l: 'Tiempo', w: 'l', doble: [{ k: 'fechaInicio', l: 'Desde', tipo: 'date', obl: true, max: hoy() },
    { k: 'fechaFin', l: 'Hasta', tipo: 'date', max: hoy() }],
    ver: (r) => <>{duracion(r.fechaInicio, r.fechaFin)}<span className="sub">{fecha(r.fechaInicio)} – {r.fechaFin ? fecha(r.fechaFin) : 'actual'}</span></> },
  { k: 'funcionesLogros', l: 'Funciones y logros', doble: [{ k: 'funciones', l: 'Funciones', largo: 600 }, { k: 'logros', l: 'Logros', largo: 600 }],
    ver: (r) => (r.funciones || r.logros ? <>{r.funciones || '—'}{r.logros && <span className="sub">Logro: {r.logros}</span>}</> : '—') },
]
const REFERENCIAS = [
  { k: 'nombre', l: 'Nombre', obl: true, largo: 150, ph: 'Con su tratamiento: Lic., Mg.…' },
  { k: 'empresa', l: 'Empresa', obl: true, largo: 150 },
  { k: 'cargo', l: 'Cargo', obl: true, largo: 100 },
  { k: 'telefono', l: 'Teléfono', w: 'm', tipo: 'tel', obl: true, largo: 20 },
  { k: 'ciudad', l: 'Ciudad', w: 'm', obl: true, largo: 80 },
]

/** Variación 16.1 y excepción 15.1: el año de la formación no es posterior al año en curso. */
const validarFormacion = (v) => {
  if (v.anio && Number(v.anio) > ANIO) return { anio: 'El año no puede ser posterior al año en curso.' }
  if (v.anio && Number(v.anio) < 1950) return { anio: 'Escriba un año desde 1950.' }
  return null
}
/** Variación 14.1: el retiro no es anterior al ingreso, y ninguna de las dos fechas es posterior a hoy. */
const validarExperiencia = (v) => {
  if (v.fechaInicio > hoy()) return { fechaInicio: 'La fecha de ingreso no puede ser posterior a hoy.' }
  if (v.fechaFin && v.fechaFin > hoy()) return { fechaFin: 'La fecha de retiro no puede ser posterior a hoy.' }
  if (v.fechaFin && v.fechaFin < v.fechaInicio) return { fechaFin: 'La fecha de retiro no puede ser anterior a la de ingreso.' }
  return null
}

export default function MiHojaVida() {
  const navigate = useNavigate()
  const location = useLocation()
  const avisar = useAvisar()
  const [carga, setCarga] = useState({ lista: false, personales: [], sinRegistro: null, error: null })
  const [textos, setTextos] = useState({ perfilProfesional: '', herramientasTrabajo: '' })
  const [inicial, setInicial] = useState(textos)
  const [formaciones, setFormaciones] = useState([])
  const [experiencias, setExperiencias] = useState([])
  const [referencias, setReferencias] = useState([])
  const [errores, setErrores] = useState({})
  const [abiertas, setAbiertas] = useState({})
  const [guardando, setGuardando] = useState(false)
  const filaAbierta = Object.values(abiertas).some(Boolean)
  useSalidaProtegida(!guardando && (filaAbierta || JSON.stringify(textos) !== JSON.stringify(inicial)))

  /** Las tres tablas como las tiene MS-01; los textos solo al cargar, para no perder lo que se esté escribiendo. */
  const aplicar = useCallback((hoja, conTextos) => {
    setFormaciones([...hoja.formaciones].sort(ordenFormacion))
    setExperiencias([...hoja.experiencias].sort(ordenExperiencia))
    setReferencias(hoja.referencias)
    if (conTextos) {
      const t = { perfilProfesional: hoja.perfilProfesional || '', herramientasTrabajo: hoja.herramientasTrabajo || '' }
      setTextos(t)
      setInicial(t)
    }
  }, [])

  useEffect(() => {
    let vigente = true
    consultarHojaVida()
      .then((hoja) => {
        if (!vigente) return
        aplicar(hoja, true)
        setCarga({ lista: true, sinRegistro: null, error: null, personales: hoja.completitud.faltan.filter((f) => !BLOQUES_HOJA.includes(f)) })
      })
      // sin datos personales no hay registro donde guardar la hoja de vida
      .catch((e) => vigente && setCarga({ lista: true, personales: [], sinRegistro: e.estado === 409 ? e.mensaje : null,
        error: e.estado === 409 ? null : e.mensaje }))
    return () => { vigente = false }
  }, [aplicar])

  const recargar = () => consultarHojaVida().then((hoja) => aplicar(hoja, false)).catch(() => {})

  const marco = (contenido) => <Marco titulo="Mi hoja de vida" ctx="Mi perfil" menu="/mi-hoja-de-vida">{contenido}</Marco>
  if (!carga.lista) return marco(<Cargando />)
  if (carga.error) return marco(<Aviso tipo="err" negrita="No fue posible consultar su hoja de vida.">{carga.error}</Aviso>)
  if (carga.sinRegistro) {
    return marco(
      <>
        <Aviso tipo="info" negrita={carga.sinRegistro} />
        <div className="acciones der"><Boton tipo="p" onClick={() => navigate('/mi-perfil')}>Ir a Mi perfil</Boton></div>
      </>,
    )
  }

  // lo que falta: los datos personales como los tiene MS-01, y la hoja de vida como está en pantalla
  const faltan = [...carga.personales,
    ...(textos.perfilProfesional.trim() ? [] : ['Perfil profesional']),
    ...(textos.herramientasTrabajo.trim() ? [] : ['Herramientas de trabajo']),
    ...(formaciones.length ? [] : ['Formación académica']),
    ...(referencias.length ? [] : ['Referencias personales'])]

  // el aviso con que se llegó (lo que faltaba para inscribir) deja de corresponder al guardar: el indicador dice lo que falta ahora
  const olvidarLlegada = () => { if (location.state?.aviso) navigate(location.pathname, { replace: true }) }

  /** Cada fila se guarda sola; si otra sesión la eliminó (excepción 16.2.4.1), la tabla vuelve a cargarse. */
  const guardarFila = (crear, actualizar, poner, orden) => async (id, datos) => {
    try {
      const guardada = id == null ? await crear(datos) : await actualizar(id, datos)
      poner((l) => (id == null ? [...l, guardada] : l.map((x) => (x.id === id ? guardada : x))).sort(orden || (() => 0)))
      olvidarLlegada()
      return guardada
    } catch (e) {
      if (e.estado === 404) recargar()
      throw e
    }
  }
  const eliminarFila = (bloque, poner) => async (id) => {
    try {
      await eliminarEntrada(bloque, id)
      poner((l) => l.filter((x) => x.id !== id))
    } catch (e) {
      if (e.estado === 404) recargar()
      throw e
    }
  }
  const abierta = (tabla) => (x) => setAbiertas((a) => ({ ...a, [tabla]: x }))

  const cambiar = (k) => (e) => {
    setTextos((t) => ({ ...t, [k]: e.target.value }))
    setErrores((x) => ({ ...x, [k]: null }))
  }

  const guardar = async () => {
    if (filaAbierta) {
      avisar('Hay una fila en edición: pulse «Listo» para guardarla, o «Cancelar» o «Quitar» para descartarla.', 'err')
      return
    }
    setGuardando(true)
    try {
      const hoja = await guardarBloques({ perfilProfesional: textos.perfilProfesional.trim() || null,
        herramientasTrabajo: textos.herramientasTrabajo.trim() || null })
      const t = { perfilProfesional: hoja.perfilProfesional || '', herramientasTrabajo: hoja.herramientasTrabajo || '' }
      setTextos(t)
      setInicial(t)
      olvidarLlegada()
      avisar('El perfil profesional y las herramientas de trabajo quedaron guardados.', 'ok')
    } catch (e) {
      setErrores(e.campos)
      avisar(`No se guardaron los textos y siguen en pantalla. ${e.mensaje}`, 'err')
    } finally {
      setGuardando(false)
    }
  }

  return marco(
    <>
      <AvancePerfil faltan={faltan} />
      <Bloque titulo="Perfil profesional">
        <Area etiqueta="Redactado en tercera persona" maxlen={800} largo={textos.perfilProfesional.length} maxLength={800}
          value={textos.perfilProfesional} onChange={cambiar('perfilProfesional')} error={errores.perfilProfesional} />
      </Bloque>
      <Bloque titulo="Herramientas de trabajo">
        <Area etiqueta="Programas y recursos que domina" maxlen={400} largo={textos.herramientasTrabajo.length} maxLength={400}
          value={textos.herramientasTrabajo} onChange={cambiar('herramientasTrabajo')} error={errores.herramientasTrabajo} />
      </Bloque>
      <p className="ayuda">Los dos textos se guardan con «Guardar hoja de vida». Cada entrada de las tablas se guarda sola, al pulsar «Listo».</p>
      <TablaEditable titulo="Formación académica" entrada="la entrada de formación académica" columnas={FORMACION}
        filas={formaciones} nombre={(r) => r.nombre} guardaSola validar={validarFormacion} alEditar={abierta('formaciones')}
        aviso={(r) => `Se guardó la formación «${r.nombre}».`}
        vacio="Todavía no hay formación académica. Use «Añadir»: el formato exige al menos una entrada."
        guardar={guardarFila(crearFormacion, actualizarFormacion, setFormaciones, ordenFormacion)}
        eliminar={eliminarFila('formaciones', setFormaciones)} />
      <TablaEditable titulo="Experiencia laboral" entrada="la experiencia laboral" columnas={EXPERIENCIA}
        filas={experiencias} nombre={(r) => r.empresa} guardaSola validar={validarExperiencia} alEditar={abierta('experiencias')}
        aviso={(r) => `Se guardó la experiencia en «${r.empresa}».`}
        vacio="Todavía no hay experiencia laboral. Es opcional en el formato."
        guardar={guardarFila(crearExperiencia, actualizarExperiencia, setExperiencias, ordenExperiencia)}
        eliminar={eliminarFila('experiencias', setExperiencias)} />
      <TablaEditable titulo="Referencias personales" entrada="la referencia personal" columnas={REFERENCIAS}
        filas={referencias} nombre={(r) => r.nombre} guardaSola max={3} alEditar={abierta('referencias')}
        ayuda={(llena) => (llena ? 'El formato PR-01 admite un máximo de tres referencias: elimine una para registrar otra.'
          : 'El formato PR-01 admite un máximo de tres referencias.')}
        aviso={(r) => `Se guardó la referencia de ${r.nombre}.`}
        vacio="Todavía no hay referencias personales. Use «Añadir»: el formato exige al menos una."
        guardar={guardarFila(crearReferencia, actualizarReferencia, setReferencias)}
        eliminar={eliminarFila('referencias', setReferencias)} />
      <p className="ayuda">Para corregir sus datos personales, use <Link className="acc" to="/mi-perfil">Mi perfil</Link>.</p>
      <div className="acciones der">
        <Boton tipo="g" disabled={guardando} onClick={() => navigate('/mis-practicas')}>Cancelar</Boton>
        <Boton tipo="p" espera={guardando} onClick={guardar}>Guardar hoja de vida</Boton>
      </div>
    </>,
  )
}
