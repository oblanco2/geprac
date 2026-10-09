/**
 * P-01 · Inicio de sesión (RF-01). Autentica contra Supabase Auth con el correo
 * institucional y muestra la espera que exige RNF-03 mientras el servicio
 * responde, porque la primera petición tras un rato sin uso puede tardar hasta
 * un minuto. Con la sesión abierta, lleva a la pantalla de entrada del rol.
 *
 * @author José Fernando Rincón Barrios
 */
import { useState } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { supabase } from '../lib/supabase'
import { useAvisar, useSesion } from '../sesion/contexto'
import { inicioDe } from '../sesion/roles'
import Marco from '../componentes/Marco'
import { Aviso, Boton, Campo } from '../componentes/controles'

const OBLIGATORIO = 'Este dato es obligatorio.'

export default function Ingreso() {
  const { lista, ocupado, sesion, cuenta, aviso, limpiarAviso } = useSesion()
  const avisar = useAvisar()
  const location = useLocation()
  const [correo, setCorreo] = useState('')
  const [clave, setClave] = useState('')
  const [ver, setVer] = useState(false)
  const [errores, setErrores] = useState({})
  const [fallo, setFallo] = useState(null)
  const [enviando, setEnviando] = useState(false)

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
  const presente = fallo || (aviso && aviso !== location.state?.aviso ? aviso : null)
  const esperando = enviando || ocupado

  return (
    <Marco titulo="Inicio de sesión">
      <form className="login" noValidate onSubmit={ingresar}>
        <div className="marca"><b>GEPRAC</b><span>Gestión de Prácticas Académicas · UDI</span></div>
        {presente && <Aviso tipo={presente.tipo} negrita={presente.negrita}>{presente.texto}</Aviso>}
        <Campo id="login-correo" etiqueta="Correo institucional" error={errores.correo}>
          <input type="email" id="login-correo" autoComplete="username" placeholder="nombre.apellido@udi.edu.co"
            aria-required="true" aria-invalid={errores.correo ? true : undefined}
            aria-describedby={errores.correo ? 'login-correo-er' : undefined}
            value={correo} onChange={(e) => setCorreo(e.target.value)} />
        </Campo>
        <Campo id="login-clave" etiqueta="Contraseña" error={errores.clave}>
          <div className="clave">
            <input type={ver ? 'text' : 'password'} id="login-clave" autoComplete="current-password"
              aria-required="true" aria-invalid={errores.clave ? true : undefined}
              aria-describedby={errores.clave ? 'login-clave-er' : undefined}
              value={clave} onChange={(e) => setClave(e.target.value)} />
            <button type="button" className="btn g" aria-controls="login-clave" aria-pressed={ver}
              onClick={() => setVer(!ver)}>{ver ? 'Ocultar' : 'Mostrar'}</button>
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
    </Marco>
  )
}
