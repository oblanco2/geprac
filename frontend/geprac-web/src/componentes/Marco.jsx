/**
 * El marco de todas las pantallas, como en el prototipo: la barra con el
 * contexto y la cuenta, el menú del rol con las opciones que nombran los casos
 * de uso, el título de la pantalla —que recibe el foco al llegar, para los
 * lectores de pantalla— y el cuerpo. Si el software llevó al usuario a esta
 * pantalla con un aviso, lo presenta arriba.
 *
 * @author José Fernando Rincón Barrios
 */
import { useEffect, useRef } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useAvisar, useLimpiarAvisos, useSesion } from '../sesion/contexto'
import { MENUS, PAPEL } from '../sesion/roles'
import { iniciales } from '../lib/formato'
import { Aviso } from './controles'

export default function Marco({ titulo, ctx = '', menu, children }) {
  const { cuenta, salir } = useSesion()
  const avisar = useAvisar()
  const limpiarAvisos = useLimpiarAvisos()
  const location = useLocation()
  const tit = useRef(null)
  const llegada = location.state?.aviso

  useEffect(() => { limpiarAvisos() }, [location.pathname, limpiarAvisos])

  useEffect(() => {
    document.title = `${titulo} · GEPRAC`
    tit.current?.focus()
  }, [titulo])

  const saltar = (e) => {
    e.preventDefault()
    document.getElementById('contenido')?.focus()
  }

  const cerrarSesion = async () => {
    await salir()
    avisar('Cerró la sesión.', 'ok')
  }

  return (
    <div className="app cliente">
      <a className="saltar" href="#contenido" onClick={saltar}>Saltar al contenido</a>
      <main className="lienzo" id="contenido" tabIndex={-1}>
        <h1 className="sr">GEPRAC · Gestión de Prácticas Académicas</h1>
        <div className="marco">
          <header className="barra">
            <span className="logo">GEPRAC</span>
            <span className="ctx">{ctx}</span>
            {cuenta && (
              <div className="rol">
                <span>{cuenta.nombrePresentacion} · {PAPEL[cuenta.rol]}</span>
                <span className="avatar" aria-hidden="true">{iniciales(cuenta.nombrePresentacion)}</span>
                <button type="button" className="salir" onClick={cerrarSesion}>Cerrar sesión</button>
              </div>
            )}
          </header>
          {cuenta && (
            <nav className="menuapp" aria-label="Menú de la aplicación">
              <ul>
                {MENUS[cuenta.rol].map(([ruta, texto]) => (
                  <li key={ruta}><Link to={ruta} aria-current={menu === ruta ? 'page' : undefined}>{texto}</Link></li>
                ))}
              </ul>
            </nav>
          )}
          <h2 className="tit-app" ref={tit} tabIndex={-1}>{titulo}</h2>
          <div className="cuerpo">
            {llegada && <Aviso tipo={llegada.tipo} negrita={llegada.negrita}>{llegada.texto}</Aviso>}
            {children}
          </div>
        </div>
      </main>
    </div>
  )
}
