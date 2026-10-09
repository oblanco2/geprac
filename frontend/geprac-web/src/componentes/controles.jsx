/**
 * Los controles de interfaz de todas las pantallas, con las clases y la
 * estructura del prototipo de alta fidelidad: cada campo con su etiqueta
 * asociada, su ayuda y su mensaje de error enlazados por aria-describedby, el
 * asterisco de lo obligatorio, los avisos que no dependen solo del color y las
 * tablas que en el celular se presentan como tarjetas.
 *
 * @author José Fernando Rincón Barrios
 */
import { useId } from 'react'
import { ESTADOS } from '../lib/formato'

const describir = (id, ayuda, error) => [ayuda && `${id}-ay`, error && `${id}-er`].filter(Boolean).join(' ') || undefined

/** Etiqueta, control, ayuda y mensaje de error, asociados por id. */
export function Campo({ id, etiqueta, obl, ayuda, error, clase, pie, children }) {
  return (
    <div className={clase ? `campo ${clase}` : 'campo'}>
      {etiqueta && (
        <label htmlFor={id}>{etiqueta}{obl && <span className="obl" aria-hidden="true">*</span>}</label>
      )}
      {children}
      {pie}
      {ayuda && !pie && <p className="ayuda" id={`${id}-ay`}>{ayuda}</p>}
      {error && <p className="err" id={`${id}-er`}>{error}</p>}
    </div>
  )
}

/** Campo de una línea: text, email, tel, number, date, search o password. */
export function Entrada({ etiqueta, obl, ayuda, error, clase, tipo = 'text', ref, ...resto }) {
  const id = useId()
  return (
    <Campo id={id} etiqueta={etiqueta} obl={obl} ayuda={ayuda} error={error} clase={clase}>
      <input id={id} type={tipo} ref={ref} aria-required={obl || undefined} aria-invalid={error ? true : undefined}
        aria-describedby={describir(id, ayuda, error)} {...resto} />
    </Campo>
  )
}

/** Lista desplegable; opciones: [[valor, texto]]. vacia añade la opción sin valor. */
export function Seleccion({ etiqueta, obl, ayuda, error, clase, opciones, vacia, ref, ...resto }) {
  const id = useId()
  return (
    <Campo id={id} etiqueta={etiqueta} obl={obl} ayuda={ayuda} error={error} clase={clase}>
      <select id={id} ref={ref} aria-required={obl || undefined} aria-invalid={error ? true : undefined}
        aria-describedby={describir(id, ayuda, error)} {...resto}>
        {vacia != null && <option value="">{vacia}</option>}
        {opciones.map(([valor, texto]) => <option key={valor} value={valor}>{texto}</option>)}
      </select>
    </Campo>
  )
}

/** Área de texto; con maxlen muestra el máximo y, en la misma línea, el contador. */
export function Area({ etiqueta, obl, ayuda, error, clase, maxlen, largo = 0, ref, ...resto }) {
  const id = useId()
  const pie = maxlen ? (
    <div className="linea-ayuda">
      <p className="ayuda" id={`${id}-ay`}>{ayuda || `Máximo ${maxlen} caracteres.`}</p>
      <p className={`contador${largo > maxlen ? ' lleno excede' : largo === maxlen ? ' lleno' : ''}`} aria-hidden="true">
        {largo} / {maxlen}
      </p>
    </div>
  ) : null
  return (
    <Campo id={id} etiqueta={etiqueta} obl={obl} ayuda={ayuda} error={error} clase={clase} pie={pie}>
      <textarea id={id} rows={3} ref={ref} aria-required={obl || undefined} aria-invalid={error ? true : undefined}
        aria-describedby={describir(id, ayuda || maxlen, error)} {...resto} />
    </Campo>
  )
}

/** Dato que el usuario no puede modificar (RNF-02). */
export function Lectura({ etiqueta, valor, tipo = 'text', clase }) {
  const id = useId()
  return (
    <Campo id={id} etiqueta={etiqueta} clase={clase}>
      <input id={id} type={tipo} readOnly value={valor == null || valor === '' ? '—' : valor} />
    </Campo>
  )
}

/** La leyenda del asterisco: va antes de los campos, para que se lea primero. */
export function LeyendaObl() {
  return (
    <p className="leyenda">
      Los campos marcados con <span className="obl" aria-hidden="true">*</span><span className="sr">asterisco</span> son obligatorios.
    </p>
  )
}

/** Botón: p primario · s secundario · g neutro · ok confirmación · d destructivo. Mientras espera, lo dice (RNF-03). */
export function Boton({ tipo = 'p', espera, textoEspera = 'Guardando…', ancho, children, ...resto }) {
  return (
    <button type="button" className={`btn ${tipo}${ancho ? ' ancho' : ''}`} disabled={espera || undefined}
      aria-busy={espera || undefined} {...resto}>
      {espera ? <><span className="spin" aria-hidden="true" />{textoEspera}</> : children}
    </button>
  )
}

const ICONO = { info: ['i', 'Información'], ok: ['✓', 'Hecho'], warn: ['!', 'Advertencia'], err: ['!', 'Atención'] }

/** Aviso en la pantalla: info · ok · warn · err. El significado no depende solo del color. */
export function Aviso({ tipo = 'info', negrita, children }) {
  return (
    <div className={`aviso ${tipo}`}>
      <span className="ic" aria-hidden="true">{ICONO[tipo][0]}</span>
      <div><span className="sr">{ICONO[tipo][1]}: </span>{negrita && <b>{negrita}</b>}{negrita && children ? ' ' : ''}{children}</div>
    </div>
  )
}

/** Etiqueta de estado de una inscripción, o activa e inactiva en los catálogos. */
export function Estado({ estado, texto }) {
  const clave = String(estado || '').toLowerCase()
  return <span className={`et ${clave}`}>{texto || ESTADOS[clave]?.texto || estado}</span>
}

/** Encabezado de un bloque, con una acción opcional a la derecha. */
export function Bloque({ titulo, accion, children }) {
  return (
    <div className="bloque">
      {accion ? <div className="bloque-cab"><h3 className="sec">{titulo}</h3>{accion}</div> : <h3 className="sec">{titulo}</h3>}
      {children}
    </div>
  )
}

/** Lista numerada de solo lectura. */
export function ListaNum({ items, etiqueta, ...resto }) {
  return (
    <ol className="lista" aria-label={etiqueta} {...resto}>
      {items.map((t, i) => (
        <li className="item" key={i}><span className="num" aria-hidden="true">{i + 1}</span><span className="txt">{t}</span></li>
      ))}
    </ol>
  )
}

/** Tabla de datos; en el celular cada fila se presenta como tarjeta con sus rótulos.
    columnas: [{ l: 'Estudiante', w: 'm', obl }] · filas: [{ clave, celdas: [...], clase }] */
export function Tabla({ columnas, filas, titulo, vacio = 'Ninguna fila coincide con los filtros aplicados.' }) {
  return (
    <div className="tabla-cont">
      <table>
        {titulo && <caption className="sr">{titulo}</caption>}
        <thead>
          <tr>
            {columnas.map((c, i) => (
              <th scope="col" key={i} className={c.w ? `w-${c.w}` : undefined}>
                {c.l ? <>{c.l}{c.obl && <span className="obl" aria-hidden="true">*</span>}</> : <span className="sr">Acciones</span>}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {filas.map((f) => (
            <tr key={f.clave} className={f.clase}>
              {f.celdas.map((c, i) => <td key={i} data-label={columnas[i].l || 'Acciones'}><div className="v">{c}</div></td>)}
            </tr>
          ))}
          {!filas.length && <tr className="vacio"><td colSpan={columnas.length}>{vacio}</td></tr>}
        </tbody>
      </table>
    </div>
  )
}

/** Indicador de espera mientras el servicio responde; el primero del día puede tardar un minuto (RNF-03). */
export function Cargando({ texto = 'Consultando…' }) {
  return (
    <p className="carga" role="status">
      <span className="spin" aria-hidden="true" />{texto} La primera conexión del día puede tardar hasta un minuto.
    </p>
  )
}
