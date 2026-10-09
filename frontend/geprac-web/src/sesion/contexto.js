/**
 * Los contextos que comparten todas las pantallas: la sesión de quien ingresó,
 * los avisos flotantes y la ventana de confirmación. Los proveedores están en
 * sus propios componentes; aquí quedan los contextos y los ganchos que los leen.
 *
 * @author José Fernando Rincón Barrios
 */
import { createContext, useContext } from 'react'

export const SesionContexto = createContext(null)
export const AvisosContexto = createContext({ avisar: () => {}, limpiarAnteriores: () => {} })
export const DialogoContexto = createContext(async () => false)

/** La sesión: { lista, ocupado, sesion, cuenta, claims, aviso, salir, limpiarAviso }. */
export const useSesion = () => useContext(SesionContexto)
/** avisar(texto, clase): el aviso flotante que confirma el resultado de una acción. */
export const useAvisar = () => useContext(AvisosContexto).avisar
/** Al cambiar de pantalla, retira el aviso que quedó de la anterior; el de la acción que trajo aquí se queda. */
export const useLimpiarAvisos = () => useContext(AvisosContexto).limpiarAnteriores
/** await confirmar({ titulo, texto, si, peligro }): true si la persona confirma. */
export const useConfirmar = () => useContext(DialogoContexto)
