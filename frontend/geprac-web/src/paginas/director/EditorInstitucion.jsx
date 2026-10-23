/**
 * P-15 · Editor de institución (CU-03, RF-05). Los datos de la institución y la
 * lista de sus contactos; el contacto es quien figura como tutor del escenario
 * en el formato de solicitud y aprobación. El NIT es opcional, y la razón social
 * no se repite en la misma ciudad.
 *
 * @author José Fernando Rincón Barrios
 */
import { useEffect, useRef, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { crearInstitucion, actualizarInstitucion, listarInstituciones } from '../../api/legalizacion'
import { useAvisar, useConfirmar } from '../../sesion/contexto'
import { MSJ_OBL, correoValido, normal, telefonoValido } from '../../lib/formato'
import Marco from '../../componentes/Marco'
import TablaEditable from '../../componentes/TablaEditable'
import { useSalidaProtegida } from '../../componentes/salida'
import { Aviso, Bloque, Boton, Cargando, Entrada, LeyendaObl, Seleccion } from '../../componentes/controles'

const NUEVA = { razonSocial: '', nit: '', direccion: '', ciudad: '', telefono: '', correo: '', sitioWeb: '', representanteLegal: '', activa: 'activa' }
const comoFormulario = (i) => Object.fromEntries(Object.keys(NUEVA).map((k) => [k, k === 'activa' ? (i.activa ? 'activa' : 'inactiva') : i[k] || '']))
const CONTACTOS = [
  { k: 'nombre', l: 'Nombre', obl: true, largo: 150, ver: (r) => <b>{r.nombre}</b> },
  { k: 'cargo', l: 'Cargo', obl: true, largo: 100 },
  { k: 'telefono', l: 'Teléfono', w: 'm', tipo: 'tel', obl: true, largo: 20 },
  { k: 'celular', l: 'Celular', w: 'm', tipo: 'tel', largo: 20 },
  { k: 'correo', l: 'Correo', tipo: 'email', largo: 150 },
]

export default function EditorInstitucion() {
  const { id } = useParams()
  const nueva = id === 'nueva'
  const navigate = useNavigate()
  const avisar = useAvisar()
  const confirmar = useConfirmar()
  const [carga, setCarga] = useState({ lista: false, instituciones: [], original: null, error: null })
  const [v, setV] = useState(NUEVA)
  const [contactos, setContactos] = useState([])
  const [inicial, setInicial] = useState({ v: NUEVA, contactos: [] })
  const [errores, setErrores] = useState({})
  const [filaAbierta, setFilaAbierta] = useState(false)
  const [guardando, setGuardando] = useState(false)
  const provisional = useRef(-1)   // identificador de los contactos que todavía no se han guardado
  const sucio = JSON.stringify({ v, contactos }) !== JSON.stringify(inicial)
  const permitir = useSalidaProtegida(!guardando && (sucio || filaAbierta))

  useEffect(() => {
    let vigente = true
    listarInstituciones()
      .then((instituciones) => {
        if (!vigente) return
        const original = nueva ? null : instituciones.find((i) => String(i.id) === id) || null
        const formulario = original ? comoFormulario(original) : NUEVA
        setV(formulario)
        setContactos(original ? original.contactos : [])
        setInicial({ v: formulario, contactos: original ? original.contactos : [] })
        setCarga({ lista: true, instituciones, original, error: null })
      })
      .catch((e) => vigente && setCarga({ lista: true, instituciones: [], original: null, error: e.mensaje }))
    return () => { vigente = false }
  }, [id, nueva])

  const marco = (contenido) => <Marco titulo="Editor de institución" ctx="Catálogo de instituciones" menu="/instituciones">{contenido}</Marco>
  if (!carga.lista) return marco(<Cargando />)
  if (carga.error) return marco(<Aviso tipo="err" negrita="No fue posible consultar el catálogo.">{carga.error}</Aviso>)
  if (!nueva && !carga.original) {
    return marco(
      <>
        <Aviso tipo="warn" negrita="La institución ya no está en el catálogo." />
        <div className="acciones der"><Boton tipo="g" onClick={() => navigate('/instituciones')}>Volver al catálogo</Boton></div>
      </>,
    )
  }

  const cambiar = (k) => (e) => {
    setV((x) => ({ ...x, [k]: e.target.value }))
    setErrores((x) => ({ ...x, [k]: null }))
  }

  const comprobar = () => {
    const e = {}
    for (const k of ['razonSocial', 'direccion', 'ciudad', 'telefono']) if (!v[k].trim()) e[k] = MSJ_OBL
    const tel = telefonoValido(v.telefono.trim())
    if (!e.telefono && tel !== true) e.telefono = tel
    const correo = correoValido(v.correo.trim())
    if (correo !== true) e.correo = correo
    if (!contactos.length) e.contactos = 'Añada al menos un contacto: es quien figura como tutor del escenario en el formato PR-04.'
    setErrores(e)
    return !Object.keys(e).length
  }

  const guardar = async () => {
    if (filaAbierta) {
      avisar('Hay una fila en edición: pulse «Listo» para guardarla, o «Cancelar» o «Quitar» para descartarla.', 'err')
      return
    }
    if (!comprobar()) {
      avisar('Revise los datos señalados: la institución no se guardó.', 'err')
      return
    }
    // la razón social no se repite en la misma ciudad (CU-03, paso 9)
    const otra = carga.instituciones.find((i) => i.id !== carga.original?.id
      && normal(i.razonSocial) === normal(v.razonSocial) && normal(i.ciudad) === normal(v.ciudad))
    if (otra) {
      if (await confirmar({ titulo: 'La institución ya está registrada', si: 'Abrir la registrada',
        texto: `Ya existe «${otra.razonSocial}» en ${otra.ciudad}. Puede abrir la registrada en lugar de crear otra.` })) {
        permitir()
        navigate(`/instituciones/${otra.id}`)
      }
      return
    }
    setGuardando(true)
    const texto = (x) => x.trim() || null
    const datos = { razonSocial: v.razonSocial.trim(), nit: texto(v.nit), direccion: v.direccion.trim(), ciudad: v.ciudad.trim(),
      telefono: v.telefono.trim(), correo: texto(v.correo), sitioWeb: texto(v.sitioWeb), representanteLegal: texto(v.representanteLegal),
      activa: v.activa === 'activa', contactos: contactos.map((c) => ({ ...c, id: c.id > 0 ? c.id : null })) }
    try {
      if (nueva) await crearInstitucion(datos)
      else await actualizarInstitucion(carga.original.id, datos)
      permitir()
      avisar(nueva ? 'La institución quedó registrada en el catálogo.'
        : 'La institución quedó guardada. Las inscripciones ya creadas conservan los datos con los que se generaron.', 'ok')
      navigate('/instituciones')
    } catch (e) {
      setGuardando(false)
      setErrores(e.campos)
      avisar(`No se guardó la institución y el editor conserva lo que diligenció. ${e.mensaje}`, 'err')
    }
  }

  return marco(
    <>
      <LeyendaObl />
      <Bloque titulo="Datos de la institución">
        <div className="grid g2">
          <Entrada etiqueta="Razón social" obl maxLength={150} value={v.razonSocial} onChange={cambiar('razonSocial')} error={errores.razonSocial} />
          <Entrada etiqueta="NIT" maxLength={20} value={v.nit} onChange={cambiar('nit')} error={errores.nit}
            ayuda="Opcional: no toda institución de educación inicial lo aporta." />
        </div>
        <div className="sep" />
        <div className="grid g3">
          <Entrada etiqueta="Dirección" obl maxLength={150} value={v.direccion} onChange={cambiar('direccion')} error={errores.direccion} />
          <Entrada etiqueta="Ciudad" obl maxLength={80} value={v.ciudad} onChange={cambiar('ciudad')} error={errores.ciudad} />
          <Entrada etiqueta="Teléfono" obl tipo="tel" maxLength={20} value={v.telefono} onChange={cambiar('telefono')} error={errores.telefono} />
        </div>
        <div className="sep" />
        <div className="grid g3">
          <Entrada etiqueta="Correo" tipo="email" maxLength={150} value={v.correo} onChange={cambiar('correo')} error={errores.correo} />
          <Entrada etiqueta="Sitio web" inputMode="url" maxLength={150} placeholder="www.ejemplo.edu.co" value={v.sitioWeb}
            onChange={cambiar('sitioWeb')} error={errores.sitioWeb} />
          <Entrada etiqueta="Representante legal" maxLength={150} value={v.representanteLegal} onChange={cambiar('representanteLegal')}
            error={errores.representanteLegal} />
        </div>
        <div className="sep" />
        <div className="grid g3">
          <Seleccion etiqueta="Estado" opciones={[['activa', 'Activa'], ['inactiva', 'Inactiva']]} value={v.activa} onChange={cambiar('activa')}
            ayuda="Una institución inactiva no aparece en la selección del estudiante." />
        </div>
      </Bloque>
      <TablaEditable titulo="Contactos" entrada="el contacto" anadir="Añadir contacto" columnas={CONTACTOS}
        filas={contactos} nombre={(r) => r.nombre} vacio="La institución todavía no tiene contactos. Use «Añadir contacto»."
        alEditar={setFilaAbierta} aviso={() => 'El contacto quedó en la lista. Se guarda con «Guardar institución».'}
        guardar={async (idFila, datos) => {
          setContactos((l) => (idFila == null ? [...l, { ...datos, id: provisional.current-- }] : l.map((c) => (c.id === idFila ? { ...c, ...datos } : c))))
          setErrores((x) => ({ ...x, contactos: null }))
          return datos
        }}
        eliminar={async (idFila) => setContactos((l) => l.filter((c) => c.id !== idFila))} />
      {errores.contactos && <div className="campo"><p className="err">{errores.contactos}</p></div>}
      <div className="acciones der">
        <Boton tipo="g" disabled={guardando} onClick={() => navigate('/instituciones')}>Cancelar</Boton>
        <Boton tipo="p" espera={guardando} onClick={guardar}>Guardar institución</Boton>
      </div>
    </>,
  )
}
