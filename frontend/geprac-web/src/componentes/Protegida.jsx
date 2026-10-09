/**
 * Excepción 1.1 de los casos de uso: sin sesión, o con la de otro rol, el
 * software no presenta la pantalla y lleva al ingreso con el aviso que
 * corresponde. Mientras se conoce la sesión, espera.
 *
 * @author José Fernando Rincón Barrios
 */
import { useEffect } from 'react'
import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useSesion } from '../sesion/contexto'
import { Cargando } from './controles'

const SIN_SESION = { tipo: 'warn', negrita: 'Su sesión no está iniciada o ya terminó.',
  texto: 'Ingrese con su correo institucional para continuar.' }
const OTRO_ROL = { tipo: 'warn', negrita: 'La opción que intentó abrir corresponde a otro rol.',
  texto: 'Ingrese con una cuenta que tenga ese rol.' }

/** roles: los que pueden abrir las pantallas de esta rama de rutas. */
export default function Protegida({ roles }) {
  const { lista, sesion, cuenta, aviso, salir } = useSesion()
  const location = useLocation()
  const ajena = lista && cuenta && !roles.includes(cuenta.rol)

  useEffect(() => { if (ajena) salir(OTRO_ROL) }, [ajena, salir])

  if (!lista || ajena) return <Cargando texto="Verificando su sesión…" />
  if (!sesion) return <Navigate to="/ingreso" replace state={{ aviso: aviso || SIN_SESION, desde: location.pathname }} />
  return <Outlet />
}
