/**
 * La dirección raíz: lleva al ingreso o a la pantalla de entrada del rol de
 * quien ya tiene la sesión abierta.
 *
 * @author José Fernando Rincón Barrios
 */
import { Navigate } from 'react-router-dom'
import { useSesion } from '../sesion/contexto'
import { inicioDe } from '../sesion/roles'
import { Cargando } from '../componentes/controles'

export default function Inicio() {
  const { lista, sesion, cuenta } = useSesion()
  if (!lista) return <Cargando texto="Verificando su sesión…" />
  return <Navigate to={sesion && cuenta ? inicioDe(cuenta) : '/ingreso'} replace />
}
