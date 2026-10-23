/**
 * Tabla que se edita fila por fila, como en el prototipo: los contactos de una
 * institución (CU-03), las entradas de la hoja de vida (CU-01), y los roles de
 * las cuentas y los tutores de las prácticas (CU-04). Mientras una fila está en
 * edición, las demás acciones de la tabla esperan. Cada fila se comprueba antes
 * de quedar en la lista; según la pantalla, la lista se guarda con el
 * formulario al que pertenece o cada fila se guarda sola (guardaSola).
 *
 * Una columna se edita con un campo de texto, correo, teléfono, número o fecha,
 * con una lista desplegable (tipo 'sel', con sus ops) o, si es doble, con dos
 * campos rotulados en la misma celda, como el jefe inmediato con su cargo; una
 * columna fija se presenta pero no se edita. max limita las filas y presenta
 * su cuenta; validar(valores, fila) añade las comprobaciones que cruzan dos
 * campos o que dependen de la fila. Sin eliminar no hay botón de eliminar, y
 * editar puede nombrar la acción de cada fila, o quitarla.
 *
 * @author José Fernando Rincón Barrios
 */
import { useId, useState } from 'react'
import { useAvisar, useConfirmar } from '../sesion/contexto'
import { MSJ_TEL, correoValido, telefonoValido } from '../lib/formato'
import { Boton, Tabla } from './controles'

/** Los campos que se editan: los de cada columna, o los dos de una columna doble; las fijas no se editan. */
const campos = (columnas) => columnas.flatMap((c) => (c.fijo ? [] : c.doble || [c]))
const vacia = (columnas) => Object.fromEntries(campos(columnas).map((c) => [c.k, '']))

/** Comprobaciones de un campo: obligatorio, correo, teléfono y número entero. */
function comprobar(col, x) {
  if (col.obl && !x) return 'Este dato es obligatorio.'
  if (!x) return null
  if (col.tipo === 'email' && correoValido(x) !== true) return 'Escriba un correo válido.'
  if (col.tipo === 'tel' && telefonoValido(x) !== true) return MSJ_TEL
  if (col.tipo === 'number' && !/^\d+$/.test(x)) return 'Escriba un número entero.'
  return null
}

/** El control de un campo; con rotulo, su etiqueta se ve encima, como en las columnas dobles. */
function Editor({ col, valor, error, cambiar, rotulo }) {
  const id = useId()
  const comunes = {
    id, value: valor ?? '', onChange: (e) => cambiar(col.k, e.target.value), 'aria-label': rotulo ? undefined : col.l,
    'aria-required': col.obl || undefined, 'aria-invalid': error ? true : undefined, 'aria-describedby': error ? `${id}-er` : undefined,
  }
  const opciones = (col.ops || []).map((o) => (Array.isArray(o) ? o : [o, o]))
  return (
    <div className="ed">
      {rotulo && <label htmlFor={id}>{col.l}{col.obl && <span className="obl" aria-hidden="true">*</span>}</label>}
      {col.tipo === 'sel' ? (
        <select {...comunes}>
          <option value="">{col.ph || 'Seleccione'}</option>
          {opciones.map(([v, t]) => <option key={v} value={v}>{t}</option>)}
        </select>
      ) : (
        <input type={col.tipo || 'text'} maxLength={col.largo} min={col.min} max={col.max} placeholder={col.ph}
          inputMode={col.tipo === 'number' ? 'numeric' : undefined} {...comunes} />
      )}
      {error && <p className="err" id={`${id}-er`}>{error}</p>}
    </div>
  )
}

export default function TablaEditable({ titulo, entrada, anadir = 'Añadir', columnas, filas, nombre, vacio, guardar, eliminar,
  aviso, alEditar, max, ayuda, validar, guardaSola, editar: accionEditar = 'Editar', sinAnadir, antes }) {
  const avisar = useAvisar()
  const confirmar = useConfirmar()
  const idAyuda = useId()
  const [edicion, setEdicion] = useState(null)       // { id: null para la nueva, valores, errores }
  const [ocupada, setOcupada] = useState(null)        // la fila que espera mientras se guarda o se elimina
  const llena = max != null && filas.length >= max

  const editar = (fila) => {
    const nueva = !fila
    setEdicion({ id: nueva ? null : fila.id, valores: nueva ? vacia(columnas) : comoTexto(columnas, fila), errores: {} })
    alEditar?.(true)
    avisar(!nueva ? 'La fila quedó en edición.' : guardaSola ? 'Se añadió una fila: diligénciela y pulse «Listo» para guardarla.'
      : 'Se añadió una fila. Se guarda con el formulario.', '')
  }
  const terminar = (texto) => {
    setEdicion(null)
    alEditar?.(false)
    if (texto) avisar(texto, '')
  }
  const cambiar = (k, v) => setEdicion((e) => ({ ...e, valores: { ...e.valores, [k]: v }, errores: { ...e.errores, [k]: null } }))

  const listo = async () => {
    const v = Object.fromEntries(Object.entries(edicion.valores).map(([k, x]) => [k, typeof x === 'string' ? x.trim() : x]))
    const errores = {}
    campos(columnas).forEach((c) => { const m = comprobar(c, v[c.k]); if (m) errores[c.k] = m })
    Object.assign(errores, Object.keys(errores).length ? {} : validar?.(v, filas.find((f) => f.id === edicion.id)) || {})
    if (Object.keys(errores).length) {
      setEdicion((e) => ({ ...e, errores }))
      avisar('La fila tiene datos por corregir: no se guardó.', 'err')
      return
    }
    const numericos = campos(columnas).filter((c) => c.tipo === 'number').map((c) => c.k)
    const datos = Object.fromEntries(Object.entries(v).map(([k, x]) => [k, x === '' ? null : numericos.includes(k) ? Number(x) : x]))
    setOcupada(edicion.id ?? 'nueva')
    try {
      const guardada = await guardar(edicion.id, datos)
      setEdicion(null)
      alEditar?.(false)
      avisar(aviso ? aviso(guardada ?? datos) : 'Se guardó la entrada.', 'ok')
    } catch (e) {
      const errCampos = e.campos || {}
      if (Object.keys(errCampos).length) setEdicion((x) => ({ ...x, errores: errCampos }))
      avisar(e.mensaje || 'No se guardó la fila.', 'err')
    } finally {
      setOcupada(null)
    }
  }

  const quitar = async (fila) => {
    const n = nombre(fila)
    const si = await confirmar({
      texto: `Se eliminará ${entrada} «${n}».${guardaSola ? '' : ' El cambio se guarda con el formulario.'} ¿Confirma la eliminación?`,
      peligro: true, si: 'Eliminar',
    })
    if (!si) return
    setOcupada(fila.id)
    try {
      await eliminar(fila.id)
      avisar(`Se eliminó ${entrada} «${n}».`, 'ok')
    } catch (e) {
      avisar(e.mensaje || 'No se eliminó la fila.', 'err')
    } finally {
      setOcupada(null)
    }
  }

  const cols = [...columnas.map((c) => ({ l: c.l, w: c.w, obl: c.obl || c.doble?.some((d) => d.obl) })), { l: '', w: 'l' }]
  const enEdicion = (id) => edicion && edicion.id === id
  const celdaDeEdicion = (c, fila) => (c.fijo ? (fila ? vista(c, fila) : '—') : c.doble ? (
    <div className="doble" key={c.k}>
      {c.doble.map((d) => <Editor key={d.k} col={d} valor={edicion.valores[d.k]} error={edicion.errores[d.k]} cambiar={cambiar} rotulo />)}
    </div>
  ) : <Editor key={c.k} col={c} valor={edicion.valores[c.k]} error={edicion.errores[c.k]} cambiar={cambiar} />)
  const filaDeEdicion = (id) => ({
    clave: id ?? 'nueva',
    clase: 'edicion',
    celdas: [
      ...columnas.map((c) => celdaDeEdicion(c, filas.find((f) => f.id === id))),
      <div className="acc-grupo" key="acc">
        <button type="button" className="acc" onClick={listo} disabled={ocupada != null}
          aria-label={`Listo: guardar ${id == null ? 'la fila nueva' : nombre(filas.find((f) => f.id === id))}`}>
          {ocupada != null ? <><span className="spin" aria-hidden="true" />Guardando…</> : 'Listo'}
        </button>
        {id == null
          ? <button type="button" className="acc quitar" onClick={() => terminar('Se quitó la fila.')} disabled={ocupada != null}>Quitar</button>
          : <button type="button" className="acc" onClick={() => terminar('Se descartaron los cambios de la fila.')} disabled={ocupada != null}>Cancelar</button>}
      </div>,
    ],
  })

  const vistas = filas.map((f) => {
    if (enEdicion(f.id)) return filaDeEdicion(f.id)
    const rotulo = typeof accionEditar === 'function' ? accionEditar(f) : accionEditar
    return {
      clave: f.id,
      celdas: [
        ...columnas.map((c) => vista(c, f)),
        <div className="acc-grupo" key="acc">
          {rotulo && (
            <button type="button" className="acc" disabled={!!edicion || ocupada != null} onClick={() => editar(f)}
              aria-label={`${rotulo} ${nombre(f)}`}>{rotulo}</button>
          )}
          {eliminar && (
            <button type="button" className="acc quitar" disabled={!!edicion || ocupada != null} onClick={() => quitar(f)}
              aria-label={`Eliminar ${nombre(f)}`}>{ocupada === f.id ? <><span className="spin" aria-hidden="true" />Eliminando…</> : 'Eliminar'}</button>
          )}
        </div>,
      ],
    }
  })
  if (edicion && edicion.id == null) vistas.push(filaDeEdicion(null))

  const textoAyuda = typeof ayuda === 'function' ? ayuda(llena) : ayuda
  return (
    <div className="bloque">
      <div className="bloque-cab">
        <h3 className="sec">{titulo}{max != null && <span className="cuenta"> · {filas.length} de {max}</span>}</h3>
        {!sinAnadir && (
          <Boton tipo="s" disabled={llena} aria-describedby={textoAyuda ? idAyuda : undefined}
            onClick={() => (edicion ? avisar('Termine primero la fila en edición: pulse «Listo» o «Cancelar».', 'err') : editar(null))}
            aria-disabled={edicion ? 'true' : undefined}>{anadir}</Boton>
        )}
      </div>
      {textoAyuda && <p className="ayuda" id={idAyuda}>{textoAyuda}</p>}
      {antes}
      <Tabla columnas={cols} filas={vistas} titulo={titulo} vacio={vacio || 'Todavía no hay entradas. Use «Añadir».'} />
    </div>
  )
}

/** Lo que presenta una celda fuera de edición. */
const vista = (c, fila) => (c.ver ? c.ver(fila) : (fila[c.k] ?? '') === '' ? '—' : fila[c.k])

/** Los valores de los campos de una fila, como texto para los controles; los nulos quedan vacíos. */
function comoTexto(columnas, fila) {
  return Object.fromEntries(Object.keys(vacia(columnas)).map((k) => [k, fila[k] == null ? '' : String(fila[k])]))
}
