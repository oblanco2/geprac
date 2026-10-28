/**
 * P-01 · Inicio de sesión (RF-01). Autentica contra Supabase Auth con el correo
 * institucional y muestra la espera que exige RNF-03 mientras el servicio
 * responde, porque la primera petición tras un rato sin uso puede tardar hasta
 * un minuto. Con la sesión abierta, lleva a la pantalla de entrada del rol.
 *
 * La pantalla tiene su propia composición, sin la barra de la aplicación: a la
 * izquierda, la sede de la universidad con sus estudiantes y el lema
 * institucional; a la derecha, el logo de GEPRAC y el formulario. En las
 * pantallas angostas, la sede queda como una banda arriba del formulario.
 *
 * @author José Fernando Rincón Barrios
 */
import { useEffect, useRef, useState } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { supabase } from '../lib/supabase'
import { useAvisar, useLimpiarAvisos, useSesion } from '../sesion/contexto'
import { inicioDe } from '../sesion/roles'
import Pie from '../componentes/Pie'
import { Aviso, Boton, Campo } from '../componentes/controles'
import arte from '../imagenes/ingreso-arte.webp'
import sede from '../imagenes/sede-udi.webp'
import logo from '../imagenes/geprac-logo.webp'

const OBLIGATORIO = 'Este dato es obligatorio.'
/** La composición de dos paneles; por debajo, la de una columna con la banda de la sede. */
const ANCHA = '(min-width: 1000px) and (min-aspect-ratio: 7/5)'

/** Los iconos de los campos: decorativos, del color de la marca. */
const icono = (trazo) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false" fill="none" stroke="currentColor" strokeWidth="1.9"
    strokeLinecap="round" strokeLinejoin="round">{trazo}</svg>
)
const SOBRE = icono(<><rect x="3" y="5" width="18" height="14" rx="2.5" /><path d="m4 7 8 6 8-6" /></>)
const CANDADO = icono(<><rect x="5" y="10.5" width="14" height="10" rx="2.2" fill="currentColor" stroke="none" />
  <path d="M8 10.5V8a4 4 0 0 1 8 0v2.5" /></>)
const OJO = icono(<><path d="M2.5 12S6 5.5 12 5.5 21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12z" /><circle cx="12" cy="12" r="3" /></>)
const OJO_TACHADO = icono(<><path d="M10.6 5.6A9.6 9.6 0 0 1 12 5.5c6 0 9.5 6.5 9.5 6.5a17 17 0 0 1-2.6 3.4M6.3 6.9C3.8 8.6 2.5 12 2.5 12S6 18.5 12 18.5c1.7 0 3.2-.5 4.5-1.2" />
  <path d="M9.9 9.9a3 3 0 0 0 4.2 4.2M3 3l18 18" /></>)

export default function Ingreso() {
  const { lista, ocupado, sesion, cuenta, aviso, limpiarAviso } = useSesion()
  const avisar = useAvisar()
  const limpiarAvisos = useLimpiarAvisos()
  const location = useLocation()
  const tit = useRef(null)
  const [correo, setCorreo] = useState('')
  const [clave, setClave] = useState('')
  const [ver, setVer] = useState(false)
  const [errores, setErrores] = useState({})
  const [fallo, setFallo] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const llegada = location.state?.aviso

  // lo que el marco hace en las demás pantallas: el título de la pestaña, el foco en el título y los avisos de la anterior
  useEffect(() => { limpiarAvisos() }, [location.pathname, limpiarAvisos])
  useEffect(() => {
    document.title = 'Inicio de sesión · GEPRAC'
    tit.current?.focus()
  }, [])

  if (lista && sesion && cuenta) return <Navigate to={location.state?.desde || inicioDe(cuenta)} replace />

  const ingresar = async (e) => {
    e.preventDefault()
    limpiarAviso()
    const faltan = { correo: correo.trim() ? null : OBLIGATORIO, clave: clave ? null : OBLIGATORIO }
    setErrores(faltan)
    if (faltan.correo || faltan.clave) {
      setFallo({ tipo: 'err', texto: 'Escriba su correo institucional y su contraseña.' })
      return
    }
    setFallo(null)
    setEnviando(true)
    const { error } = await supabase.auth.signInWithPassword({ email: correo.trim(), password: clave })
    setEnviando(false)
    if (error) setFallo(error.status === 400
      ? { tipo: 'err', negrita: 'El correo o la contraseña no coinciden con una cuenta del software.', texto: 'Revise que estén bien escritos; lo que escribió se conserva.' }
      : { tipo: 'err', negrita: 'No fue posible conectar con el servicio de identidad.', texto: 'Inténtelo de nuevo en un momento.' })
  }

  // el aviso del intento, o el de la sesión (cuenta sin rol) si no es el que ya trajo la llegada
  const presente = fallo || (aviso && aviso !== llegada ? aviso : null)
  const esperando = enviando || ocupado

  return (
    <div className="ingreso">
      <div className="ingreso-cuerpo">
        <div className="ingreso-arte">
          <picture>
            <source media={ANCHA} srcSet={arte} />
            <img src={sede} alt="" />
          </picture>
        </div>
        <main className="ingreso-panel" id="contenido">
          <div className="ingreso-contenido">
            <img className="ingreso-logo" src={logo} alt="GEPRAC · Gestión de Prácticas Académicas" width="835" height="354" />
            <h1 className="ingreso-tit" ref={tit} tabIndex={-1}>Inicio de sesión</h1>
            {llegada && <Aviso tipo={llegada.tipo} negrita={llegada.negrita}>{llegada.texto}</Aviso>}
            {presente && <Aviso tipo={presente.tipo} negrita={presente.negrita}>{presente.texto}</Aviso>}
            <form noValidate onSubmit={ingresar}>
              <Campo id="login-correo" etiqueta="Correo institucional" error={errores.correo}>
                <div className="grupo-ic">
                  <span className="ic">{SOBRE}</span>
                  <input type="email" id="login-correo" autoComplete="username" placeholder="nombre.apellido@udi.edu.co"
                    aria-required="true" aria-invalid={errores.correo ? true : undefined}
                    aria-describedby={errores.correo ? 'login-correo-er' : undefined}
                    value={correo} onChange={(e) => setCorreo(e.target.value)} />
                </div>
              </Campo>
              <Campo id="login-clave" etiqueta="Contraseña" error={errores.clave}>
                <div className="grupo-ic">
                  <span className="ic">{CANDADO}</span>
                  <input type={ver ? 'text' : 'password'} id="login-clave" autoComplete="current-password"
                    aria-required="true" aria-invalid={errores.clave ? true : undefined}
                    aria-describedby={errores.clave ? 'login-clave-er' : undefined}
                    value={clave} onChange={(e) => setClave(e.target.value)} />
                  <button type="button" className="ver" aria-label="Mostrar la contraseña" aria-controls="login-clave"
                    aria-pressed={ver} title={ver ? 'Ocultar la contraseña' : 'Mostrar la contraseña'}
                    onClick={() => setVer(!ver)}>{ver ? OJO : OJO_TACHADO}</button>
                </div>
              </Campo>
              <Boton tipo="p" ancho type="submit" espera={esperando} textoEspera="Conectando con el servicio…">Iniciar sesión</Boton>
              {esperando && (
                <p className="carga"><span className="spin" aria-hidden="true" />La primera conexión del día puede tardar hasta un minuto.</p>
              )}
              <p className="olvido">
                <button type="button" className="acc"
                  onClick={() => avisar('El restablecimiento de la contraseña lo resuelve el proveedor de identidad institucional.', '')}>
                  ¿Olvidó su contraseña?
                </button>
              </p>
            </form>
          </div>
        </main>
      </div>
      <Pie />
    </div>
  )
}
