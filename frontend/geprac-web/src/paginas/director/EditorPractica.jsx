/**
 * P-13 · Editor de práctica (CU-02, RF-04). El nombre, la posición en el plan,
 * el estado, el objetivo general, el horario estándar y las dos listas
 * ordenables que el software precarga en cada inscripción: los objetivos
 * específicos y las actividades. La posición va de 1 a 8 y no la ocupa otra
 * práctica del programa (excepción 10.1), y cada lista lleva al menos un ítem
 * (excepción 9.1). Si la escritura falla, el editor conserva lo diligenciado.
 *
 * @author José Fernando Rincón Barrios
 */
import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { actualizarPractica, crearPractica, listarPracticas } from '../../api/legalizacion'
import { useAvisar, useSesion } from '../../sesion/contexto'
import { useNombrePrograma } from '../../lib/programa'
import { comoItems } from '../../lib/listas'
import Marco from '../../componentes/Marco'
import ListaOrdenable from '../../componentes/ListaOrdenable'
import { useSalidaProtegida } from '../../componentes/salida'
import { Area, Aviso, Boton, Cargando, Entrada, Lectura, LeyendaObl, Seleccion } from '../../componentes/controles'

const OBLIGATORIO = 'Este dato es obligatorio.'
const POSICION = 'La posición debe ser un número entero de 1 a 8.'
const VACIA = { orden: '', nombre: '', objetivoGeneral: '', horarioEstandar: '', activa: 'activa' }
const comoFormulario = (p) => ({ orden: String(p.orden), nombre: p.nombre, objetivoGeneral: p.objetivoGeneral,
  horarioEstandar: p.horarioEstandar || '', activa: p.activa ? 'activa' : 'inactiva' })
const textos = (items) => items.map((x) => x.texto)

export default function EditorPractica() {
  const { id } = useParams()
  const nueva = id === 'nueva'
  const navigate = useNavigate()
  const avisar = useAvisar()
  const { claims } = useSesion()
  const programa = useNombrePrograma(claims.programa)
  const [carga, setCarga] = useState({ lista: false, practicas: [], original: null, error: null })
  const [v, setV] = useState(VACIA)
  const [objetivos, setObjetivos] = useState([])
  const [actividades, setActividades] = useState([])
  const [inicial, setInicial] = useState(JSON.stringify({ v: VACIA, objetivos: [], actividades: [] }))
  const [errores, setErrores] = useState({})
  const [fallo, setFallo] = useState(null)
  const [guardando, setGuardando] = useState(false)
  const actual = JSON.stringify({ v, objetivos: textos(objetivos), actividades: textos(actividades) })
  const permitir = useSalidaProtegida(!guardando && actual !== inicial)

  useEffect(() => {
    let vigente = true
    listarPracticas()
      .then((practicas) => {
        if (!vigente) return
        const original = nueva ? null : practicas.find((p) => String(p.id) === id) || null
        const formulario = original ? comoFormulario(original) : VACIA
        setV(formulario)
        setObjetivos(comoItems(original ? original.objetivos : []))
        setActividades(comoItems(original ? original.actividades : []))
        setInicial(JSON.stringify({ v: formulario, objetivos: original ? original.objetivos : [], actividades: original ? original.actividades : [] }))
        setCarga({ lista: true, practicas, original, error: null })
      })
      .catch((e) => vigente && setCarga({ lista: true, practicas: [], original: null, error: e.mensaje }))
    return () => { vigente = false }
  }, [id, nueva])

  const marco = (contenido) => <Marco titulo="Editor de práctica" ctx={programa || 'Catálogo de prácticas'} menu="/practicas">{contenido}</Marco>
  if (!carga.lista) return marco(<Cargando />)
  if (carga.error) return marco(<Aviso tipo="err" negrita="No fue posible consultar el catálogo.">{carga.error}</Aviso>)
  if (!nueva && !carga.original) {
    return marco(
      <>
        <Aviso tipo="warn" negrita="La práctica ya no está en el catálogo." />
        <div className="acciones der"><Boton tipo="g" onClick={() => navigate('/practicas')}>Volver al catálogo</Boton></div>
      </>,
    )
  }

  const cambiar = (k) => (e) => {
    setV((x) => ({ ...x, [k]: e.target.value }))
    setErrores((x) => ({ ...x, [k]: null }))
  }
  const cambiarLista = (poner, clave) => (items) => {
    poner(items)
    setErrores((x) => ({ ...x, [clave]: null, items: {} }))
  }

  /** Pasos 9 y 10, y excepciones 9.1 y 10.1: los datos, la posición libre y las dos listas con texto. */
  const comprobar = () => {
    const e = { items: {} }
    for (const k of ['nombre', 'objetivoGeneral']) if (!v[k].trim()) e[k] = OBLIGATORIO
    const orden = Number(v.orden)
    if (!v.orden.trim()) e.orden = OBLIGATORIO
    else if (!Number.isInteger(orden) || orden < 1 || orden > 8) e.orden = POSICION
    else {
      const otra = carga.practicas.find((p) => p.orden === orden && p.id !== carga.original?.id)
      if (otra) e.orden = `Ya hay una práctica en esa posición del plan: ${otra.orden} · ${otra.nombre}.`
    }
    for (const [lista, clave, falta] of [[objetivos, 'objetivos', 'Añada al menos un objetivo específico.'],
      [actividades, 'actividades', 'Añada al menos una actividad.']]) {
      if (!lista.length) e[clave] = falta
      lista.forEach((it) => { if (!it.texto.trim()) e.items[it.k] = 'Escriba el texto o quite el ítem.' })
    }
    setErrores(e)
    return Object.keys(e).length === 1 && !Object.keys(e.items).length
  }

  /** Los mensajes del servicio por campo; los de un ítem llegan como «objetivos[2]». */
  const erroresDelServicio = (campos) => {
    const e = { items: {} }
    Object.entries(campos).forEach(([campo, msj]) => {
      const m = campo.match(/^(objetivos|actividades)\[(\d+)\]$/)
      if (!m) e[campo] = msj
      else {
        const it = (m[1] === 'objetivos' ? objetivos : actividades)[Number(m[2])]
        if (it) e.items[it.k] = msj
      }
    })
    setErrores(e)
  }

  const guardar = async () => {
    if (!comprobar()) {
      avisar('Revise los datos señalados: la práctica no se guardó.', 'err')
      return
    }
    setGuardando(true)
    setFallo(null)
    const datos = { orden: Number(v.orden), nombre: v.nombre.trim(), objetivoGeneral: v.objetivoGeneral.trim(),
      horarioEstandar: v.horarioEstandar.trim() || null, activa: v.activa === 'activa',
      objetivos: objetivos.map((x) => x.texto.trim()), actividades: actividades.map((x) => x.texto.trim()) }
    try {
      if (nueva) await crearPractica(datos)
      else await actualizarPractica(carga.original.id, datos)
      permitir()
      avisar(nueva ? 'La práctica quedó registrada en el catálogo.'
        : 'La práctica quedó guardada. Las inscripciones ya creadas conservan su copia del contenido.', 'ok')
      navigate('/practicas')
    } catch (e) {
      setGuardando(false)
      erroresDelServicio(e.campos)
      // excepción 11.1: no se guarda nada y el editor conserva lo diligenciado
      setFallo({ negrita: 'No se guardó la práctica y el editor conserva lo que diligenció.', texto: e.mensaje })
      avisar('No se guardó la práctica: revise el mensaje de la pantalla.', 'err')
    }
  }

  const llenas = nueva && carga.practicas.length >= 8
  return marco(
    <>
      {llenas && (
        <Aviso tipo="info" negrita="Las ocho posiciones del plan ya están ocupadas.">
          Para cambiar una práctica, edítela desde el catálogo; para registrar otra, elimine primero la que ocupa su posición.
        </Aviso>
      )}
      {fallo && <Aviso tipo="err" negrita={fallo.negrita}>{fallo.texto}</Aviso>}
      <LeyendaObl />
      <div className="grid g3">
        <Lectura etiqueta="Programa" valor={programa || claims.programa} />
        <Entrada etiqueta="Orden en el plan" obl tipo="number" min={1} max={8} inputMode="numeric" value={v.orden}
          onChange={cambiar('orden')} error={errores.orden} ayuda="Posición de 1 a 8 en el plan de estudios." />
        <Seleccion etiqueta="Estado" opciones={[['activa', 'Activa'], ['inactiva', 'Inactiva']]} value={v.activa} onChange={cambiar('activa')}
          ayuda="Una práctica inactiva no se ofrece para inscripción." />
      </div>
      <div className="sep" />
      <Entrada etiqueta="Nombre de la práctica" obl maxLength={150} value={v.nombre} onChange={cambiar('nombre')} error={errores.nombre} />
      <div className="sep" />
      <Area etiqueta="Objetivo general" obl maxlen={600} largo={v.objetivoGeneral.length} maxLength={600} value={v.objetivoGeneral}
        onChange={cambiar('objetivoGeneral')} error={errores.objetivoGeneral} />
      <div className="sep" />
      <Entrada etiqueta="Horario estándar" maxLength={150} placeholder="Ej.: Martes y jueves de 7:00 a 12:00" value={v.horarioEstandar}
        onChange={cambiar('horarioEstandar')} error={errores.horarioEstandar} />
      <div className="sep" />
      <ListaOrdenable titulo="Objetivos específicos" uno="objetivo" art="el" anadir="Añadir objetivo" obl items={objetivos}
        cambiar={cambiarLista(setObjetivos, 'objetivos')} errores={errores.items} error={errores.objetivos} />
      <ListaOrdenable titulo="Actividades a desarrollar" uno="actividad" art="la" anadir="Añadir actividad" obl items={actividades}
        cambiar={cambiarLista(setActividades, 'actividades')} errores={errores.items} error={errores.actividades} />
      <div className="acciones der">
        <Boton tipo="g" disabled={guardando} onClick={() => navigate('/practicas')}>Cancelar</Boton>
        <Boton tipo="p" espera={guardando} onClick={guardar}>Guardar práctica</Boton>
      </div>
    </>,
  )
}
