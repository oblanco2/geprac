/**
 * P-16 · Preparación del semestre (CU-04, RF-06 y RF-16). La apertura y el
 * cierre del semestre, el rol con el que cada cuenta opera el software
 * (variación 2.1) y el tutor académico de cada práctica en el semestre abierto
 * (pasos 9 a 11 y variación 9.1). El cliente compone la pantalla con los dos
 * servicios: el semestre y las designaciones vienen de MS-02, y las cuentas
 * con su rol, de MS-01. Por eso la comprobación de la variación 2.1.4 —no
 * quitarle el rol de tutor a quien tiene prácticas designadas en el semestre
 * abierto— la hace aquí, con los datos de los dos.
 *
 * @author José Fernando Rincón Barrios
 */
import { useCallback, useEffect, useState } from 'react'
import { asignarRol, listarUsuarios } from '../../api/academico'
import { abrirSemestre, cerrarSemestre, consultarPreparacion, designarTutor, reasignarTutor } from '../../api/legalizacion'
import { useAvisar, useConfirmar } from '../../sesion/contexto'
import { fecha, normal } from '../../lib/formato'
import Marco from '../../componentes/Marco'
import TablaEditable from '../../componentes/TablaEditable'
import { useSalidaProtegida } from '../../componentes/salida'
import { Aviso, Bloque, Boton, Cargando, Entrada, Estado, Lectura, LeyendaObl, NotaFiltros } from '../../componentes/controles'

const ROLES = [['DIRECTOR', 'Director'], ['TUTOR', 'Tutor académico'], ['ESTUDIANTE', 'Estudiante']]
const NOMBRE_ROL = Object.fromEntries(ROLES)
const OBLIGATORIO = 'Este dato es obligatorio para abrir el semestre.'
const NUEVO = { codigo: '', fechaInicio: '', fechaCierre: '' }

export default function PreparacionSemestre() {
  const avisar = useAvisar()
  const confirmar = useConfirmar()
  const [carga, setCarga] = useState({ lista: false, semestre: null, practicas: [], usuarios: [], error: null })
  const [nuevo, setNuevo] = useState(null)        // el formulario del semestre nuevo, mientras está abierto
  const [errores, setErrores] = useState({})
  const [espera, setEspera] = useState(null)
  const [fallo, setFallo] = useState(null)
  const [buscar, setBuscar] = useState('')
  const [abiertas, setAbiertas] = useState({})
  useSalidaProtegida(!espera && (JSON.stringify(nuevo ?? NUEVO) !== JSON.stringify(NUEVO) || Object.values(abiertas).some(Boolean)))

  const cargar = useCallback(() => Promise.all([consultarPreparacion(), listarUsuarios()])
    .then(([p, usuarios]) => setCarga({ lista: true, semestre: p.semestre, practicas: p.practicas, error: null,
      usuarios: [...usuarios].sort((a, b) => a.nombrePresentacion.localeCompare(b.nombrePresentacion, 'es')) }))
    .catch((e) => setCarga((c) => ({ ...c, lista: true, error: e.mensaje }))), [])

  useEffect(() => { cargar() }, [cargar])

  const s = carga.semestre
  const marco = (contenido) => <Marco titulo="Preparación del semestre" ctx={s ? `Semestre ${s.codigo}` : 'Semestre'} menu="/semestre">{contenido}</Marco>
  if (!carga.lista) return marco(<Cargando />)
  if (carga.error && !carga.usuarios.length) {
    return marco(<Aviso tipo="err" negrita="No fue posible consultar la preparación del semestre.">{carga.error}</Aviso>)
  }

  const abierto = !!s?.abierto
  const nombreDe = (id) => carga.usuarios.find((u) => u.id === id)?.nombrePresentacion || 'Cuenta sin nombre'
  const tutores = carga.usuarios.filter((u) => u.rol === 'TUTOR').map((u) => [u.id, u.nombrePresentacion])

  /* ------------------------------------------------ semestre: pasos 3 a 8 y variaciones 2.2 y 5.1 */

  const cambiar = (k) => (e) => {
    setNuevo((x) => ({ ...x, [k]: e.target.value }))
    setErrores((x) => ({ ...x, [k]: null }))
  }

  /** Paso 6 y excepción 6.1: los tres datos y las fechas en orden. */
  const comprobar = () => {
    const e = {}
    for (const k of Object.keys(NUEVO)) if (!nuevo[k].trim()) e[k] = OBLIGATORIO
    if (!e.codigo && !/^\d{4}-[12]$/.test(nuevo.codigo.trim())) e.codigo = 'Escriba el año y el periodo, por ejemplo 2027-1.'
    if (!e.fechaInicio && !e.fechaCierre && nuevo.fechaCierre <= nuevo.fechaInicio) e.fechaCierre = 'La fecha de cierre debe ser posterior a la de inicio.'
    setErrores(e)
    return !Object.keys(e).length
  }

  const abrir = async () => {
    if (!comprobar()) {
      avisar('Revise los datos señalados: el semestre no se abrió.', 'err')
      return
    }
    setEspera('abrir')
    setFallo(null)
    try {
      const abiertoNuevo = await abrirSemestre({ codigo: nuevo.codigo.trim(), fechaInicio: nuevo.fechaInicio, fechaCierre: nuevo.fechaCierre })
      setNuevo(null)
      avisar(`El semestre ${abiertoNuevo.codigo} quedó abierto. Designe ahora el tutor de cada práctica.`, 'ok')
      await cargar()
    } catch (e) {
      if (Object.keys(e.campos).length) setErrores(e.campos)          // excepción 7.2: el código ya existe
      else {
        // excepción 7.1: ya hay un semestre abierto; el servicio dice cuál y que se cierre primero con la variación 2.2.
        // Su primera frase va en negrita, como en el prototipo: «No se abrió el semestre: el 2026-2 sigue abierto.»
        setNuevo(null)
        const corte = e.mensaje.indexOf('. ')
        setFallo(corte < 0 ? { negrita: e.mensaje, texto: '' }
          : { negrita: e.mensaje.slice(0, corte + 1), texto: e.mensaje.slice(corte + 2) })
      }
      avisar('No se abrió el semestre: revise el mensaje de la pantalla.', 'err')
    } finally {
      setEspera(null)
    }
  }

  const cerrar = async () => {
    if (!await confirmar({ texto: `Al cerrar el semestre ${s.codigo} ningún estudiante podrá inscribir su práctica mientras no se abra otro. `
      + '¿Confirma el cierre?', peligro: true, si: 'Cerrar el semestre' })) return
    setEspera('cerrar')
    setFallo(null)
    try {
      await cerrarSemestre(s.id)
      avisar(`El semestre ${s.codigo} quedó cerrado.`, 'ok')
      await cargar()
    } catch (e) {
      avisar(`No se cerró el semestre. ${e.mensaje}`, 'err')
    } finally {
      setEspera(null)
    }
  }

  /* ------------------------------------------------ roles: variación 2.1 */

  const filtro = normal(buscar)
  const cuentas = carga.usuarios.filter((u) => !filtro || normal(`${u.nombrePresentacion} ${u.correoInstitucional}`).includes(filtro))
  const sinRol = carga.usuarios.filter((u) => !u.rol).length

  /** Variación 2.1.4 y excepción 2.1.4.1: quien deja de ser tutor no puede tener prácticas designadas en el semestre abierto. */
  const validarRol = (v, fila) => {
    if (fila.rol !== 'TUTOR' || v.rol === 'TUTOR' || !abierto) return null
    const designadas = carga.practicas.filter((p) => p.tutorId === fila.id).map((p) => p.orden)
    return designadas.length
      ? { rol: `Primero reasigne sus prácticas del semestre ${s.codigo}: ${designadas.join(', ')}.` }
      : null
  }

  const guardarRol = async (id, datos) => {
    const cuenta = await asignarRol(id, datos.rol)
    setCarga((c) => ({ ...c, usuarios: c.usuarios.map((u) => (u.id === id ? cuenta : u)) }))
    return cuenta
  }

  /* ------------------------------------------------ tutores: pasos 9 a 11 y variación 9.1 */

  const guardarTutor = async (practicaId, datos) => {
    const actual = carga.practicas.find((p) => p.practicaId === practicaId)
    try {
      const d = actual.tutorId ? await reasignarTutor(practicaId, datos.tutorId) : await designarTutor(practicaId, datos.tutorId)
      setCarga((c) => ({ ...c, practicas: c.practicas.map((p) => (p.practicaId === practicaId ? d : p)) }))
      return d
    } catch (e) {
      // excepción 10.2: otra sesión ya la designó; se presenta la tabla actualizada
      if (e.estado === 409 && !Object.keys(e.campos).length) cargar()
      throw e
    }
  }

  const avisoTutor = (d) => {
    const n = d.enviadas
    return `${nombreDe(d.tutorId)} quedó como tutor de la práctica ${d.orden}.`
      + (n === 1 ? ' La inscripción enviada de esa práctica pasa a su bandeja.'
        : n > 1 ? ` Las ${n} inscripciones enviadas de esa práctica pasan a su bandeja.` : '')
  }

  return marco(
    <>
      {carga.error && <Aviso tipo="err" negrita="No fue posible actualizar la pantalla; presenta lo último que consultó.">{carga.error}</Aviso>}
      {fallo && <Aviso tipo="err" negrita={fallo.negrita}>{fallo.texto}</Aviso>}
      <Bloque titulo="Semestre académico">
        {s ? (
          <div className="grid g4">
            <Lectura etiqueta="Código" valor={s.codigo} />
            <Lectura etiqueta="Inicio" valor={fecha(s.fechaInicio)} />
            <Lectura etiqueta="Cierre" valor={fecha(s.fechaCierre)} />
            <div className="campo">
              <span className="lbl">Estado</span>
              <div className="estado-val">{abierto ? <Estado estado="abierto" texto="Abierto" /> : <Estado estado="cerrado" texto="Cerrado" />}</div>
            </div>
          </div>
        ) : (
          <Aviso tipo="info" negrita="Todavía no hay semestres registrados.">Abra el primero para que los estudiantes puedan inscribir.</Aviso>
        )}
        <div className="acciones suelta">
          <Boton tipo="s" disabled={!!nuevo || !!espera} aria-describedby="ay-semestre" onClick={() => { setNuevo(NUEVO); setErrores({}) }}>
            Abrir un semestre nuevo
          </Boton>
          <Boton tipo="g" disabled={!abierto || !!espera} espera={espera === 'cerrar'} textoEspera="Cerrando…" aria-describedby="ay-semestre"
            onClick={cerrar}>Cerrar el semestre vigente</Boton>
        </div>
        <p className="ayuda" id="ay-semestre">
          {abierto ? 'Solo puede haber un semestre abierto a la vez.' : 'No hay semestre abierto: mientras tanto, ningún estudiante puede inscribir su práctica.'}
        </p>
        {nuevo && (
          <div className="semestre-nuevo">
            <h4>Semestre nuevo</h4>
            <LeyendaObl />
            <div className="grid g3">
              <Entrada etiqueta="Código" obl placeholder="2027-1" maxLength={6} value={nuevo.codigo} onChange={cambiar('codigo')}
                error={errores.codigo} ayuda="Año y periodo, por ejemplo 2027-1." autoFocus />
              <Entrada etiqueta="Inicio" obl tipo="date" value={nuevo.fechaInicio} onChange={cambiar('fechaInicio')} error={errores.fechaInicio} />
              <Entrada etiqueta="Cierre" obl tipo="date" value={nuevo.fechaCierre} onChange={cambiar('fechaCierre')} error={errores.fechaCierre} />
            </div>
            <div className="acciones der">
              <Boton tipo="g" disabled={!!espera} onClick={() => { setNuevo(null); setErrores({}) }}>Cancelar</Boton>
              <Boton tipo="p" espera={espera === 'abrir'} textoEspera="Abriendo…" onClick={abrir}>Abrir semestre</Boton>
            </div>
          </div>
        )}
      </Bloque>

      <TablaEditable titulo="Roles de los usuarios" entrada="el rol" sinAnadir guardaSola filas={cuentas}
        columnas={[
          { k: 'nombrePresentacion', l: 'Usuario', fijo: true, ver: (u) => <b>{u.nombrePresentacion}</b> },
          { k: 'correoInstitucional', l: 'Correo', fijo: true },
          { k: 'rol', l: 'Rol', w: 'xl', tipo: 'sel', ops: ROLES, ph: 'Seleccione el rol', obl: true,
            ver: (u) => (u.rol ? <span className="pill">{NOMBRE_ROL[u.rol]}</span> : <Estado estado="sin" texto="Sin rol" />) },
        ]}
        nombre={(u) => u.nombrePresentacion} editar={(u) => (u.rol ? 'Cambiar' : 'Asignar')} validar={validarRol}
        alEditar={(x) => setAbiertas((a) => ({ ...a, roles: x }))} guardar={guardarRol}
        aviso={(u) => `El rol de ${u.nombrePresentacion} quedó como ${NOMBRE_ROL[u.rol].toLowerCase()}. Lo verá en su siguiente ingreso.`}
        vacio={carga.usuarios.length ? 'Ninguna cuenta coincide con la búsqueda.' : 'Todavía no hay cuentas: aparecen cuando cada usuario ingresa por primera vez.'}
        antes={(
          <>
            <NotaFiltros>Puede buscar por <b>nombre</b> o <b>correo</b>. Las cuentas aparecen cuando el usuario ingresa por primera vez.</NotaFiltros>
            <div className="filtros">
              <Entrada etiqueta="Buscar usuario" tipo="search" placeholder="Nombre o correo" value={buscar} disabled={!!abiertas.roles}
                onChange={(e) => setBuscar(e.target.value)} />
              <span className="empuja"><span className="pill">{sinRol} {sinRol === 1 ? 'cuenta sin rol' : 'cuentas sin rol'}</span></span>
            </div>
          </>
        )} />

      <TablaEditable titulo={s ? `Tutor de cada práctica · semestre ${s.codigo}` : 'Tutor de cada práctica'} entrada="la designación"
        sinAnadir guardaSola filas={carga.practicas.map((p) => ({ ...p, id: p.practicaId }))}
        columnas={[
          { k: 'orden', l: 'Orden', w: 'xs', fijo: true, ver: (p) => <span className="num">{p.orden}</span> },
          { k: 'nombre', l: 'Práctica', fijo: true,
            ver: (p) => <>{p.nombre}{!p.activa && <span className="sub">Inactiva: no se ofrece para inscripción</span>}</> },
          { k: 'tutorId', l: 'Tutor académico', w: 'xl', tipo: 'sel', ops: tutores, ph: 'Seleccione el tutor', obl: true,
            ver: (p) => (p.tutorId ? nombreDe(p.tutorId) : <Estado estado="sin" texto="Sin asignar" />) },
        ]}
        nombre={(p) => `la práctica ${p.orden}`} editar={(p) => (abierto ? (p.tutorId ? 'Reasignar' : 'Asignar') : null)}
        alEditar={(x) => setAbiertas((a) => ({ ...a, tutores: x }))} guardar={guardarTutor} aviso={avisoTutor}
        vacio="El catálogo del programa todavía no tiene prácticas."
        antes={!abierto && (
          <p className="ayuda">{s ? 'Con el semestre cerrado, las designaciones se consultan pero no se cambian.'
            : 'Las designaciones se hacen cuando haya un semestre abierto.'}</p>
        )} />
      {!tutores.length && abierto && (
        <p className="ayuda">Todavía no hay cuentas con el rol de tutor académico: asígnelo primero en la lista de roles.</p>
      )}
    </>,
  )
}
