/**
 * El indicador de completitud del perfil (CU-01, paso 9), arriba de Mi perfil y
 * de Mi hoja de vida: cuánto del registro exigen los cuatro formatos y qué
 * falta, nombrado como lo nombra MS-01. Lo calcula cada pantalla con lo que
 * está escrito en ella, aunque todavía no se haya guardado, y con lo que MS-01
 * dice del resto del registro. Es la inscripción (CU-05) la que reclama el 100 %.
 *
 * @author José Fernando Rincón Barrios
 */
import { TOTAL_PERFIL } from '../lib/perfil'

/** faltan: los nombres de lo que falta, en el orden en que la pantalla los presenta. */
export default function AvancePerfil({ faltan }) {
  const pct = Math.round(((TOTAL_PERFIL - faltan.length) / TOTAL_PERFIL) * 100)
  return (
    <>
      <div className="avance">
        <h3 className="sec">Avance del perfil</h3>
        <span className="pill">Perfil completo al {pct} %</span>
      </div>
      <div className={`barraprog${pct < 100 ? ' parcial' : ''}`} role="progressbar" aria-label="Avance del perfil"
        aria-valuemin={0} aria-valuemax={100} aria-valuenow={pct}>
        <i style={{ width: `${pct}%` }} />
      </div>
      <p className="ayuda">
        {faltan.length
          ? `${faltan.length === 1 ? 'Falta un dato' : `Faltan ${faltan.length} datos`} que exigen los formatos: ${faltan.join(', ')}.`
          : 'Tiene todos los datos que exigen los cuatro formatos.'}
      </p>
      <div className="sep" />
    </>
  )
}
