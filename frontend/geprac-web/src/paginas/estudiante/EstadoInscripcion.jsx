/**
 * P-08 · Estado de mi inscripción (CU-05, RF-13, variación 16.1). La inscripción
 * del semestre abierto con su estado y su significado; si el tutor la devolvió,
 * el motivo que registró y la opción de corregirla para reenviarla sin volver a
 * empezar (variación 16.2). Presenta también los datos que la identifican y su
 * historial: el envío, cada revisión y el aval.
 *
 * @author José Fernando Rincón Barrios
 */
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { consultarEstado } from '../../api/legalizacion'
import { useSesion } from '../../sesion/contexto'
import { fecha, periodo } from '../../lib/formato'
import Marco from '../../componentes/Marco'
import Historial from '../../componentes/Historial'
import { Aviso, Bloque, Boton, Cargando, Estado, Lectura } from '../../componentes/controles'

/** La fecha de la última revisión con ese resultado. */
const revisada = (i, resultado) => [...i.revisiones].reverse().find((r) => r.resultado === resultado)?.fecha

export default function EstadoInscripcion() {
  const navigate = useNavigate()
  const { claims } = useSesion()
  const [carga, setCarga] = useState({ lista: false, insc: null, sin: null, error: null })

  useEffect(() => {
    let vigente = true
    consultarEstado()
      .then((insc) => vigente && setCarga({ lista: true, insc, sin: null, error: null }))
      // 404: no hay inscripción en el semestre; 409: no hay semestre abierto o todavía no hay perfil
      .catch((e) => vigente && setCarga({ lista: true, insc: null, sin: [404, 409].includes(e.estado) ? e.mensaje : null,
        error: [404, 409].includes(e.estado) ? null : e.mensaje }))
    return () => { vigente = false }
  }, [])

  const i = carga.insc
  const marco = (contenido) => (
    <Marco titulo="Estado de mi inscripción" ctx={i ? `Semestre ${i.semestre}` : 'Mi inscripción'} menu="/mi-inscripcion">{contenido}</Marco>
  )
  if (!carga.lista) return marco(<Cargando />)
  if (carga.error) return marco(<Aviso tipo="err" negrita="No fue posible consultar su inscripción.">{carga.error}</Aviso>)
  if (carga.sin) {
    const sinPerfil = !claims.estudiante_id
    return marco(
      <>
        <Aviso tipo="info" negrita={carga.sin} />
        <div className="acciones der">
          <Boton tipo="g" onClick={() => navigate('/mis-practicas')}>Volver a Mis prácticas</Boton>
          {sinPerfil
            ? <Boton tipo="p" onClick={() => navigate('/mi-perfil')}>Ir a Mi perfil</Boton>
            : <Boton tipo="p" onClick={() => navigate('/inscribir')}>Inscribir práctica</Boton>}
        </div>
      </>,
    )
  }

  const d = i.datos
  const presentacion = {
    BORRADOR: [<Aviso key="a" tipo="info" negrita="La inscripción está en borrador.">Todavía no la ha enviado al tutor académico.</Aviso>,
      <Boton key="b" tipo="p" onClick={() => navigate('/inscribir')}>Continuar la inscripción</Boton>],
    ENVIADA: [<Aviso key="a" tipo="info" negrita={`En revisión del tutor académico desde el ${fecha(i.enviadaEn)}.`}>
      Cuando la resuelva, el resultado aparece aquí.</Aviso>, null],
    APROBADA: [<Aviso key="a" tipo="warn" negrita={`Aprobada por el tutor académico el ${fecha(revisada(i, 'APROBADA'))}.`}>
      Espera el aval de la Dirección del Programa; después podrá obtener sus formatos.</Aviso>, null],
    DEVUELTA: [<Aviso key="a" tipo="err" negrita={`Devuelta por el tutor académico el ${fecha(revisada(i, 'DEVUELTA'))}.`}>
      <p>«{i.motivoDevolucion}»</p></Aviso>,
      <Boton key="b" tipo="p" onClick={() => navigate('/inscribir/2')}>Corregir inscripción</Boton>],
    AVALADA: [<Aviso key="a" tipo="ok" negrita={`Avalada por la Dirección del Programa el ${fecha(i.aval?.fecha)}.`}>
      Sus formatos están disponibles.</Aviso>,
      <Boton key="b" tipo="p" onClick={() => navigate(`/mis-formatos/${i.id}`)}>Ver mis formatos</Boton>],
  }
  const [aviso, boton] = presentacion[i.estado]

  return marco(
    <>
      <div className="avance">
        <h3 className="sec">Práctica {i.ordenPractica} · {d.nombrePractica}</h3>
        <span><Estado estado={i.estado} /> <span className="pill">Semestre {i.semestre}</span></span>
      </div>
      <div className="sep" />
      {aviso}
      <Bloque titulo="Datos de la inscripción">
        <div className="grid g3">
          <Lectura etiqueta="Institución" valor={d.institucionRazonSocial} />
          <Lectura etiqueta="Contacto" valor={d.contactoNombre ? `${d.contactoNombre} · ${d.contactoCargo}` : ''} />
          <Lectura etiqueta="Periodo" valor={periodo(i.fechaInicio, i.fechaFin)} />
        </div>
      </Bloque>
      <Bloque titulo="Historial de la inscripción"><Historial insc={i} estudiante="Usted" /></Bloque>
      <div className="acciones der">
        <Boton tipo="g" onClick={() => navigate('/mis-practicas')}>Volver a Mis prácticas</Boton>
        {boton}
      </div>
    </>,
  )
}
