/**
 * Lista ordenable del editor de práctica (P-13): los objetivos específicos y
 * las actividades, en el orden en que salen impresos en el formato (CU-02,
 * variación 8.2). Cada ítem se escribe en su propio campo, que crece con el
 * texto, y se sube, se baja o se quita; el foco sigue al ítem que se movió y
 * los lectores de pantalla oyen su posición nueva.
 *
 * items: [{ k, texto }], con k como clave estable del ítem (lib/listas); cambiar(items) recibe la lista nueva.
 *
 * @author José Fernando Rincón Barrios
 */
import { useId, useState } from 'react'
import { comoItems } from '../lib/listas'
import { Boton } from './controles'

/** El campo crece con lo que se escribe, como en el prototipo. */
const ajustar = (t) => {
  if (!t) return
  t.style.height = 'auto'
  t.style.height = `${t.scrollHeight}px`
}

/** uno: «objetivo» o «actividad»; art: su artículo, «el» o «la». errores: el mensaje de cada ítem, por su clave. */
export default function ListaOrdenable({ titulo, uno, art, anadir, items, cambiar, errores = {}, error, obl }) {
  const base = useId()
  const [anuncio, setAnuncio] = useState('')
  const Uno = uno[0].toUpperCase() + uno.slice(1)
  const enfocar = (id) => requestAnimationFrame(() => document.getElementById(id)?.focus())

  const mover = (i, paso) => {
    const lista = [...items]
    const [item] = lista.splice(i, 1)
    lista.splice(i + paso, 0, item)
    cambiar(lista)
    const pos = i + paso
    // el foco sigue al ítem: en el mismo botón, o en el otro si ese ya no aplica
    const sube = paso < 0 ? pos > 0 : pos === lista.length - 1
    enfocar(`${base}-${item.k}-${sube ? 'sube' : 'baja'}`)
    setAnuncio(`${art === 'el' ? 'El' : 'La'} ${uno} quedó en la posición ${pos + 1}.`)
  }

  const quitar = (i) => {
    const lista = items.filter((_, j) => j !== i)
    cambiar(lista)
    const vecino = lista[i] || lista[i - 1]
    enfocar(vecino ? `${base}-${vecino.k}` : `${base}-anadir`)
    setAnuncio(`Se quitó ${art} ${uno} ${i + 1}.`)
  }

  const agregar = () => {
    const nuevo = comoItems([''])[0]
    cambiar([...items, nuevo])
    enfocar(`${base}-${nuevo.k}`)
  }

  return (
    <div className="bloque">
      <div className="bloque-cab">
        <h3 className="sec" id={`${base}-t`}>{titulo}{obl && <span className="obl" aria-hidden="true">*</span>}</h3>
        <Boton tipo="s" id={`${base}-anadir`} onClick={agregar}>{anadir}</Boton>
      </div>
      {items.length > 0 && (
        <ol className="lista" aria-labelledby={`${base}-t`}>
          {items.map((it, i) => {
            const id = `${base}-${it.k}`
            const err = errores[it.k]
            return (
              <li className="item" key={it.k}>
                <span className="num" aria-hidden="true">{i + 1}</span>
                <div className="txt">
                  <label className="sr" htmlFor={id}>{Uno} {i + 1}</label>
                  <textarea id={id} rows={1} value={it.texto} placeholder="Escriba el texto y ordénelo con ↑ ↓" ref={ajustar}
                    aria-invalid={err ? true : undefined} aria-describedby={err ? `${id}-er` : undefined}
                    onChange={(e) => { ajustar(e.target); cambiar(items.map((x) => (x.k === it.k ? { ...x, texto: e.target.value } : x))) }} />
                  {err && <p className="err" id={`${id}-er`}>{err}</p>}
                </div>
                <div className="item-acc">
                  <button type="button" className="acc mover" id={`${id}-sube`} disabled={i === 0} onClick={() => mover(i, -1)}
                    aria-label={`Subir ${art} ${uno} ${i + 1}`}>↑</button>
                  <button type="button" className="acc mover" id={`${id}-baja`} disabled={i === items.length - 1} onClick={() => mover(i, 1)}
                    aria-label={`Bajar ${art} ${uno} ${i + 1}`}>↓</button>
                  <button type="button" className="acc quitar" onClick={() => quitar(i)} aria-label={`Quitar ${art} ${uno} ${i + 1}`}>Quitar</button>
                </div>
              </li>
            )
          })}
        </ol>
      )}
      {!items.length && <p className="ayuda">Todavía no hay ítems. Use «{anadir}».</p>}
      {error && <p className="err">{error}</p>}
      <p className="sr" aria-live="polite">{anuncio}</p>
    </div>
  )
}
