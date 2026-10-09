/**
 * P-02 · Mis prácticas (CU-09, RF-15). La pantalla de entrada del estudiante:
 * las ocho prácticas de su plan, las inscritas con su estado y las demás como no
 * inscritas. Es el historial acumulado que el punto 4 sostiene como evidencia
 * para la renovación del registro calificado. No modifica ningún dato.
 *
 * @author José Fernando Rincón Barrios
 */
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { consultarHistorial } from '../../api/legalizacion'
import { useConfirmar, useSesion } from '../../sesion/contexto'
import { useNombrePrograma } from '../../lib/programa'
import { ESTADOS, ORDINALES, fecha, periodo } from '../../lib/formato'
import Marco from '../../componentes/Marco'
import { Aviso, Cargando, Estado } from '../../componentes/controles'

export default function MisPracticas() {
  const confirmar = useConfirmar()
  const { claims } = useSesion()
  const programa = useNombrePrograma(claims.programa)
  const [estado, setEstado] = useState({ lista: false, historial: [], error: null, sinPerfil: null })

  useEffect(() => {
    let vigente = true
    consultarHistorial()
      .then((historial) => vigente && setEstado({ lista: true, historial, error: null, sinPerfil: null }))
      .catch((e) => vigente && setEstado({ lista: true, historial: [], error: e.estado === 409 ? null : e.mensaje,
        sinPerfil: e.estado === 409 ? e.mensaje : null }))
    return () => { vigente = false }
  }, [])

  const marco = (contenido) => <Marco titulo="Mis prácticas" ctx={programa || 'Mis prácticas'} menu="/mis-practicas">{contenido}</Marco>
  if (!estado.lista) return marco(<Cargando />)
  if (estado.error) return marco(<Aviso tipo="err" negrita="No fue posible consultar sus prácticas.">{estado.error}</Aviso>)
  // excepción 4.2: sin programa ni registro de estudiante no hay plan que presentar
  if (estado.sinPerfil) return marco(<Aviso tipo="info" negrita={estado.sinPerfil} />)

  // de cada práctica, la inscripción más reciente; el servicio las entrega en el orden del plan
  const porPractica = new Map()
  estado.historial.forEach((h) => { if (!porPractica.has(h.ordenPractica)) porPractica.set(h.ordenPractica, h) })
  const tarjetas = [...porPractica.values()]
  const devuelta = tarjetas.find((h) => h.estado === 'DEVUELTA')
  const avaladas = tarjetas.filter((h) => h.estado === 'AVALADA').length

  const verContenido = (h) => confirmar({
    titulo: `Práctica ${h.ordenPractica} · ${h.nombrePractica}`,
    texto: `Objetivo general: ${h.objetivoGeneral}`,
    soloCerrar: true,
  })

  return marco(
    <>
      {devuelta && (
        <Aviso tipo="err" negrita={`Su inscripción de la práctica ${devuelta.ordenPractica} fue devuelta por el tutor académico.`}>
          Motivo: {devuelta.motivoDevolucion}
        </Aviso>
      )}
      <h3 className="sec">Plan de prácticas{programa ? ` · ${programa}` : ''} · {avaladas} de {tarjetas.length} avaladas</h3>
      {tarjetas.length < 8 && (
        <p className="ayuda">El catálogo del programa tiene {tarjetas.length} de las ocho prácticas del plan; las demás aparecerán cuando la Dirección del Programa las registre.</p>
      )}
      <ul className="tarjetas" aria-label="Las prácticas del plan">
        {tarjetas.map((h) => {
          const cab = <><span className="n">Práctica {h.ordenPractica}</span><span className="t">{h.nombrePractica}</span></>
          if (!h.estado) {
            return (
              <li key={h.ordenPractica}>
                <button type="button" className="tarj mut" aria-haspopup="dialog" onClick={() => verContenido(h)}>
                  {cab}<Estado estado="sin" texto="No inscrita" />
                  <span className="sem">Corresponde al {ORDINALES[h.ordenPractica - 1]} semestre</span>
                  <span className="ir" aria-hidden="true">Ver el contenido</span>
                </button>
              </li>
            )
          }
          if (h.estado === 'AVALADA') {
            return (
              <li key={h.ordenPractica}>
                <Link className="tarj" to={`/mis-formatos/${h.inscripcionId}`}>
                  {cab}<Estado estado={h.estado} />
                  <span className="sem">Semestre {h.semestre} · avalada el {fecha(h.avaladaEn)}</span>
                  <span className="ir" aria-hidden="true">Ver los formatos</span>
                </Link>
              </li>
            )
          }
          return (
            <li key={h.ordenPractica}>
              <div className="tarj">
                {cab}<Estado estado={h.estado} />
                <span className="sem">Semestre {h.semestre} · {h.institucion || 'sin institución'}</span>
                <span className="sem">{periodo(h.fechaInicio, h.fechaFin)}</span>
              </div>
            </li>
          )
        })}
      </ul>
      <ul className="leyenda-estados" aria-label="Significado de los estados">
        {Object.keys(ESTADOS).map((k) => <li key={k}><Estado estado={k} /> {ESTADOS[k].sentido}</li>)}
      </ul>
    </>,
  )
}
