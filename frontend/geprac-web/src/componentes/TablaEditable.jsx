/**
 * Tabla que se edita fila por fila, como en el prototipo: los contactos de una
 * institución (CU-03). Mientras una fila está en edición, las demás acciones de
 * la tabla esperan. Cada fila se comprueba antes de quedar en la lista, y la
 * lista se guarda con el formulario al que pertenece.
 *
 * @author José Fernando Rincón Barrios
 */
import { useId, useState } from 'react'
import { useAvisar, useConfirmar } from '../sesion/contexto'
import { MSJ_TEL, correoValido, telefonoValido } from '../lib/formato'
import { Boton, Tabla } from './controles'

const vacia = (columnas) => Object.fromEntries(columnas.map((c) => [c.k, '']))

/** Comprobaciones de una celda: obligatorio, correo y teléfono. */
function comprobar(col, x) {
  if (col.obl && !x) return 'Este dato es obligatorio.'
  if (!x) return null
  if (col.tipo === 'email' && correoValido(x) !== true) return 'Escriba un correo válido.'
  if (col.tipo === 'tel' && telefonoValido(x) !== true) return MSJ_TEL
  return null
}

function Editor({ col, valor, error, cambiar }) {
  const id = useId()
  return (
    <div className="ed">
      <input id={id} type={col.tipo || 'text'} maxLength={col.largo} value={valor ?? ''}
        onChange={(e) => cambiar(col.k, e.target.value)} aria-label={col.l} aria-required={col.obl || undefined}
        aria-invalid={error ? true : undefined} aria-describedby={error ? `${id}-er` : undefined} />
      {error && <p className="err" id={`${id}-er`}>{error}</p>}
    </div>
  )
}

export default function TablaEditable({ titulo, entrada, anadir = 'Añadir', columnas, filas, nombre, vacio, guardar, eliminar, aviso, alEditar }) {
  const avisar = useAvisar()
  const confirmar = useConfirmar()
  const [edicion, setEdicion] = useState(null)       // { id: null para la nueva, valores, errores }
  const [ocupada, setOcupada] = useState(null)        // la fila que espera mientras se guarda o se elimina

  const editar = (fila) => {
    const nueva = !fila
    setEdicion({ id: nueva ? null : fila.id, valores: nueva ? vacia(columnas) : comoTexto(columnas, fila), errores: {} })
    alEditar?.(true)
    avisar(nueva ? 'Se añadió una fila. Se guarda con el formulario.' : 'La fila quedó en edición.', '')
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
    columnas.forEach((c) => { const m = comprobar(c, v[c.k]); if (m) errores[c.k] = m })
    if (Object.keys(errores).length) {
      setEdicion((e) => ({ ...e, errores }))
      avisar('La fila tiene datos por corregir: no se guardó.', 'err')
      return
    }
    const datos = Object.fromEntries(Object.entries(v).map(([k, x]) => [k, x === '' ? null : x]))
    setOcupada(edicion.id ?? 'nueva')
    try {
      const guardada = await guardar(edicion.id, datos)
      setEdicion(null)
      alEditar?.(false)
      avisar(aviso ? aviso(guardada ?? datos) : 'Se guardó la entrada.', 'ok')
    } catch (e) {
      const campos = e.campos || {}
      if (Object.keys(campos).length) setEdicion((x) => ({ ...x, errores: campos }))
      avisar(e.mensaje || 'No se guardó la fila.', 'err')
    } finally {
      setOcupada(null)
    }
  }

  const quitar = async (fila) => {
    const n = nombre(fila)
    const si = await confirmar({ texto: `Se eliminará ${entrada} «${n}». El cambio se guarda con el formulario. ¿Confirma la eliminación?`, peligro: true, si: 'Eliminar' })
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

  const cols = [...columnas.map((c) => ({ l: c.l, w: c.w, obl: c.obl })), { l: '', w: 'l' }]
  const enEdicion = (id) => edicion && edicion.id === id
  const filaDeEdicion = (id) => ({
    clave: id ?? 'nueva',
    clase: 'edicion',
    celdas: [
      ...columnas.map((c) => <Editor key={c.k} col={c} valor={edicion.valores[c.k]} error={edicion.errores[c.k]} cambiar={cambiar} />),
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

  const vistas = filas.map((f) => (enEdicion(f.id) ? filaDeEdicion(f.id) : {
    clave: f.id,
    celdas: [
      ...columnas.map((c) => (c.ver ? c.ver(f) : (f[c.k] ?? '') === '' ? '—' : f[c.k])),
      <div className="acc-grupo" key="acc">
        <button type="button" className="acc" disabled={!!edicion || ocupada != null} onClick={() => editar(f)}
          aria-label={`Editar ${nombre(f)}`}>Editar</button>
        <button type="button" className="acc quitar" disabled={!!edicion || ocupada != null} onClick={() => quitar(f)}
          aria-label={`Eliminar ${nombre(f)}`}>{ocupada === f.id ? <><span className="spin" aria-hidden="true" />Eliminando…</> : 'Eliminar'}</button>
      </div>,
    ],
  }))
  if (edicion && edicion.id == null) vistas.push(filaDeEdicion(null))

  return (
    <div className="bloque">
      <div className="bloque-cab">
        <h3 className="sec">{titulo}</h3>
        <Boton tipo="s" onClick={() => (edicion ? avisar('Termine primero la fila en edición: pulse «Listo» o «Cancelar».', 'err') : editar(null))}
          aria-disabled={edicion ? 'true' : undefined}>{anadir}</Boton>
      </div>
      <Tabla columnas={cols} filas={vistas} titulo={titulo} vacio={vacio || 'Todavía no hay entradas. Use «Añadir».'} />
    </div>
  )
}

/** Los valores de las columnas de una fila, como texto para los controles; los nulos quedan vacíos. */
function comoTexto(columnas, fila) {
  return Object.fromEntries(Object.keys(vacia(columnas)).map((k) => [k, fila[k] == null ? '' : String(fila[k])]))
}
