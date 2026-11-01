/**
 * Preguntas frecuentes, la pantalla del botón de ayuda («?») de la barra, que
 * está en todas las pantallas de los tres roles: las preguntas del rol de la
 * cuenta que ingresó —estudiante, tutor académico o director del programa—,
 * agrupadas por tema, y al final las comunes al ingreso y al uso del software.
 * Cada pregunta abre y cierra su respuesta sin salir de la pantalla, y la que
 * habla de una opción del menú lleva su enlace. Es una pantalla de consulta: no
 * modifica ningún dato.
 *
 * @author José Fernando Rincón Barrios
 */
import { Link } from 'react-router-dom'
import { useSesion } from '../../sesion/contexto'
import Marco from '../../componentes/Marco'
import { Bloque, Estado } from '../../componentes/controles'
import { ESTADOS } from '../../lib/formato'
import { COMUNES, PREGUNTAS } from '../../lib/preguntas'

/** Los estados de una inscripción, en el orden en que los recorre. */
const ORDEN = ['borrador', 'enviada', 'devuelta', 'aprobada', 'avalada']

function Pregunta({ p, r, ir, estados }) {
  return (
    <details className="pregunta">
      <summary>{p}</summary>
      <div className="respuesta">
        <p>{r}</p>
        {estados && (
          <ul className="estados-faq">
            {ORDEN.map((e) => <li key={e}><Estado estado={e} /><span>{ESTADOS[e].sentido[0].toUpperCase() + ESTADOS[e].sentido.slice(1)}.</span></li>)}
          </ul>
        )}
        {ir && <p><Link className="acc" to={ir[0]}>Ir a {ir[1]}</Link></p>}
      </div>
    </details>
  )
}

export default function PreguntasFrecuentes() {
  const { cuenta } = useSesion()
  const grupos = [...(PREGUNTAS[cuenta?.rol] || []), COMUNES]
  return (
    <Marco titulo="Preguntas frecuentes" ctx="Ayuda">
      <p className="faq-intro">Las respuestas a las dudas más comunes de su rol. Seleccione una pregunta para ver su respuesta.</p>
      {grupos.map((g) => (
        <Bloque key={g.titulo} titulo={g.titulo}>
          <div className="faq">{g.preguntas.map((q) => <Pregunta key={q.p} {...q} />)}</div>
        </Bloque>
      ))}
    </Marco>
  )
}
