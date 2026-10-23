/**
 * P-03 · Mis datos personales (CU-01, RF-02). El registro único del estudiante,
 * que persiste entre semestres: la información personal, la residencia y el
 * contacto, y el programa con el semestre que cursa, que deciden qué práctica
 * le corresponde inscribir. El correo institucional viene de la cuenta y no se
 * edita. Se guarda con los datos mínimos —nombres, apellidos, tipo y número de
 * documento, programa y semestre—; el indicador mide el avance frente a lo que
 * exigen los cuatro formatos. Al crear el registro o cambiar de programa, la
 * sesión se renueva para que el token traiga el programa y el registro.
 *
 * @author José Fernando Rincón Barrios
 */
import { useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { consultarPerfil, guardarPerfil, listarProgramas } from '../../api/academico'
import { useAvisar, useSesion } from '../../sesion/contexto'
import { ESTADOS_CIVILES, GENEROS, MSJ_OBL, SEMESTRES_PLAN, TIPOS_DOCUMENTO, correoValido, documentoValido, hoy,
  telefonoValido } from '../../lib/formato'
import Marco from '../../componentes/Marco'
import { BLOQUES_HOJA, DATOS_PERSONALES } from '../../lib/perfil'
import AvancePerfil from '../../componentes/AvancePerfil'
import { useSalidaProtegida } from '../../componentes/salida'
import { Aviso, Bloque, Boton, Cargando, Entrada, Lectura, LeyendaObl, Seleccion } from '../../componentes/controles'

const VACIO = Object.fromEntries(DATOS_PERSONALES.map(([k]) => [k, '']))
const OBLIGATORIOS = ['nombres', 'apellidos', 'tipoDocumento', 'numeroDocumento', 'codigoPrograma', 'semestreActual']
const comoFormulario = (p) => Object.fromEntries(Object.keys(VACIO).map((k) => [k, p[k] == null ? '' : String(p[k])]))
const SEMESTRES = SEMESTRES_PLAN.map((s, i) => [String(i + 1), s])

/** Excepción 7.1: los datos mínimos, y los formatos de documento, fecha, teléfonos y correo. */
function comprobar(v) {
  const e = {}
  OBLIGATORIOS.forEach((k) => { if (!v[k].trim()) e[k] = MSJ_OBL })
  const documento = e.numeroDocumento ? true : documentoValido(v.numeroDocumento.trim())
  if (documento !== true) e.numeroDocumento = documento
  if (v.fechaNacimiento && v.fechaNacimiento >= hoy()) e.fechaNacimiento = 'La fecha de nacimiento debe ser anterior a hoy.'
  for (const k of ['telefonoFijo', 'celular']) {
    const t = telefonoValido(v[k].trim())
    if (t !== true) e[k] = t
  }
  const correo = correoValido(v.correoPersonal.trim())
  if (correo !== true) e.correoPersonal = correo
  return e
}

export default function MiPerfil() {
  const navigate = useNavigate()
  const location = useLocation()
  const avisar = useAvisar()
  const { claims, renovar } = useSesion()
  const [carga, setCarga] = useState({ lista: false, perfil: null, programas: [], error: null })
  const [v, setV] = useState(VACIO)
  const [inicial, setInicial] = useState(VACIO)
  const [errores, setErrores] = useState({})
  const [fallo, setFallo] = useState(null)
  const [guardando, setGuardando] = useState(false)
  // cambios sin guardar: al salir por el menú o con Cancelar, el software pregunta antes
  useSalidaProtegida(!guardando && JSON.stringify(v) !== JSON.stringify(inicial))

  useEffect(() => {
    let vigente = true
    Promise.all([consultarPerfil(), listarProgramas()])
      .then(([perfil, programas]) => {
        if (!vigente) return
        const formulario = comoFormulario(perfil)
        setV(formulario)
        setInicial(formulario)
        setCarga({ lista: true, perfil, programas, error: null })
      })
      .catch((e) => vigente && setCarga({ lista: true, perfil: null, programas: [], error: e.mensaje }))
    return () => { vigente = false }
  }, [])

  const marco = (contenido) => <Marco titulo="Mis datos personales" ctx="Mi perfil" menu="/mi-perfil">{contenido}</Marco>
  if (!carga.lista) return marco(<Cargando />)
  if (carga.error) return marco(<Aviso tipo="err" negrita="No fue posible consultar su perfil.">{carga.error}</Aviso>)

  // lo que falta: los datos personales como están en pantalla, y la hoja de vida como la tiene MS-01
  const faltan = [...DATOS_PERSONALES.filter(([k]) => !v[k].trim()).map(([, l]) => l),
    ...BLOQUES_HOJA.filter((b) => carga.perfil.completitud.faltan.includes(b))]

  const cambiar = (k) => (e) => {
    setV((x) => ({ ...x, [k]: e.target.value }))
    setErrores((x) => ({ ...x, [k]: null }))
  }

  const guardar = async () => {
    const e = comprobar(v)
    setErrores(e)
    if (Object.keys(e).length) {
      avisar('Revise los datos señalados: su registro no se guardó.', 'err')
      return
    }
    setGuardando(true)
    setFallo(null)
    const datos = Object.fromEntries(Object.entries(v).map(([k, x]) => [k, x.trim() || null]))
    datos.semestreActual = Number(v.semestreActual)
    try {
      const guardado = await guardarPerfil(datos)
      const formulario = comoFormulario(guardado)
      setV(formulario)
      setInicial(formulario)
      setCarga((c) => ({ ...c, perfil: guardado }))
      // el token trae el registro de estudiante y su programa: si cambiaron, se pide uno nuevo
      if (String(claims.estudiante_id ?? '') !== String(guardado.id) || claims.programa !== guardado.codigoPrograma) await renovar()
      // el aviso con que se llegó (lo que faltaba para inscribir) ya no corresponde: el indicador dice lo que falta ahora
      if (location.state?.aviso) navigate(location.pathname, { replace: true })
      avisar('Sus datos personales quedaron guardados.', 'ok')
    } catch (err) {
      setErrores(err.campos)
      if (err.campos.codigoPrograma) {
        // excepción 7.3: el programa ya no existe; se vuelve a presentar la lista para escoger de nuevo
        setV((x) => ({ ...x, codigoPrograma: '' }))
        listarProgramas().then((programas) => setCarga((c) => ({ ...c, programas }))).catch(() => {})
      }
      setFallo({ negrita: 'No se guardó su registro; lo que diligenció sigue en pantalla.', texto: err.mensaje })
      avisar('No se guardó su registro: revise el mensaje de la pantalla.', 'err')
    } finally {
      setGuardando(false)
    }
  }

  const campo = (k) => ({ value: v[k], onChange: cambiar(k), error: errores[k] })

  return marco(
    <>
      <AvancePerfil faltan={faltan} />
      {fallo && <Aviso tipo="err" negrita={fallo.negrita}>{fallo.texto}</Aviso>}
      <LeyendaObl />
      <Bloque titulo="Información personal">
        <div className="grid g3">
          <Entrada etiqueta="Nombres" obl maxLength={100} autoComplete="given-name" {...campo('nombres')} />
          <Entrada etiqueta="Apellidos" obl maxLength={100} autoComplete="family-name" {...campo('apellidos')} />
          <Lectura etiqueta="Correo institucional" tipo="email" valor={carga.perfil.correoInstitucional} />
          <Seleccion etiqueta="Tipo de documento" obl vacia="Seleccione" opciones={TIPOS_DOCUMENTO} {...campo('tipoDocumento')} />
          <Entrada etiqueta="Número de documento" obl inputMode="numeric" maxLength={20}
            ayuda="De 5 a 12 dígitos; los puntos son opcionales." {...campo('numeroDocumento')} />
          <Entrada etiqueta="Lugar de expedición" maxLength={80} {...campo('lugarExpedicion')} />
          <Entrada etiqueta="Fecha de nacimiento" tipo="date" max={hoy()} autoComplete="bday" {...campo('fechaNacimiento')} />
          <Entrada etiqueta="Lugar de nacimiento" maxLength={80} {...campo('lugarNacimiento')} />
          <Seleccion etiqueta="Género" vacia="Seleccione" opciones={GENEROS.map((g) => [g, g])} {...campo('genero')} />
          <Seleccion etiqueta="Estado civil" vacia="Seleccione" opciones={ESTADOS_CIVILES.map((e) => [e, e])} {...campo('estadoCivil')} />
          <Entrada etiqueta="EPS vigente" maxLength={80} {...campo('eps')} />
        </div>
      </Bloque>
      <Bloque titulo="Residencia y contacto">
        <div className="grid g3">
          <Entrada etiqueta="Dirección" maxLength={150} autoComplete="street-address" {...campo('direccion')} />
          <Entrada etiqueta="Barrio" maxLength={80} {...campo('barrio')} />
          <Entrada etiqueta="Ciudad" maxLength={80} autoComplete="address-level2" {...campo('ciudad')} />
          <Entrada etiqueta="Teléfono fijo" tipo="tel" maxLength={20} autoComplete="tel" {...campo('telefonoFijo')} />
          <Entrada etiqueta="Celular" tipo="tel" maxLength={20} autoComplete="tel" {...campo('celular')} />
          <Entrada etiqueta="Correo personal" tipo="email" maxLength={150} autoComplete="email"
            ayuda="Es el correo que imprimen los formatos." {...campo('correoPersonal')} />
        </div>
      </Bloque>
      <Bloque titulo="Programa">
        <div className="grid g2">
          <Seleccion etiqueta="Programa al que pertenece" obl vacia="Seleccione su programa"
            opciones={carga.programas.map((p) => [p.codigo, p.nombre])} {...campo('codigoPrograma')} />
          <Seleccion etiqueta="Semestre que cursa" obl vacia="Seleccione" opciones={SEMESTRES}
            ayuda="Con el programa, decide qué práctica le corresponde inscribir." {...campo('semestreActual')} />
        </div>
      </Bloque>
      <div className="acciones der">
        <Boton tipo="g" disabled={guardando} onClick={() => navigate('/mis-practicas')}>Cancelar</Boton>
        <Boton tipo="p" espera={guardando} onClick={guardar}>Guardar cambios</Boton>
      </div>
    </>,
  )
}
