/**
 * El marco de todas las pantallas, como en el prototipo: la barra con la marca
 * del software, el contexto, el botón de ayuda y la cuenta, el menú del rol con
 * las opciones que nombran los casos de uso, el título de la pantalla —que
 * recibe el foco al llegar, para los lectores de pantalla—, el cuerpo y el pie
 * con los derechos de autor. Si el software llevó al usuario a esta pantalla con
 * un aviso, lo presenta arriba. El botón de ayuda es como el de Gmail: solo el
 * ícono «?», que lleva a las preguntas frecuentes, con el globo «Ayuda» al pasar
 * el mouse o llegar con el teclado.
 *
 * @author José Fernando Rincón Barrios
 */
import { useEffect, useRef, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useAvisar, useLimpiarAvisos, useSesion } from '../sesion/contexto'
import { AYUDA, MENUS, PAPEL } from '../sesion/roles'
import { iniciales } from '../lib/formato'
import { Aviso } from './controles'
import Pie from './Pie'

/** El ícono de la ayuda: un signo de interrogación dentro de un círculo. */
const ICONO_AYUDA = (
  <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
    <circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" strokeWidth="1.8" />
    <path d="M9.6 9.7a2.45 2.45 0 1 1 3.7 2.1c-.75.45-1.3.95-1.3 1.85v.25" fill="none" stroke="currentColor" strokeWidth="1.8"
      strokeLinecap="round" strokeLinejoin="round" />
    <circle cx="12" cy="16.7" r="1.15" fill="currentColor" />
  </svg>
)

export default function Marco({ titulo, ctx = '', menu, children }) {
  const { cuenta, salir } = useSesion()
  const avisar = useAvisar()
  const limpiarAvisos = useLimpiarAvisos()
  const location = useLocation()
  const tit = useRef(null)
  const llegada = location.state?.aviso
  // el globo «Ayuda» se cierra con Esc sin mover el mouse ni el foco, y vuelve al pasar otra vez (WCAG 1.4.13)
  const [globo, setGlobo] = useState(true)

  useEffect(() => { limpiarAvisos() }, [location.pathname, limpiarAvisos])

  useEffect(() => {
    document.title = `${titulo} · GEPRAC`
    tit.current?.focus()
  }, [titulo])

  useEffect(() => {
    const cerrarGlobo = (e) => { if (e.key === 'Escape') setGlobo(false) }
    document.addEventListener('keydown', cerrarGlobo)
    return () => document.removeEventListener('keydown', cerrarGlobo)
  }, [])

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
            <span className="marca-app">
              <span className="emblema" aria-hidden="true">
                <svg viewBox="0 0 24 24" focusable="false">
                  <rect x="5" y="3.5" width="14" height="17.5" rx="2.5" fill="none" stroke="currentColor" strokeWidth="1.7" />
                  <path d="M9 2.5h6v2.2a.8.8 0 0 1-.8.8H9.8a.8.8 0 0 1-.8-.8z" fill="currentColor" />
                  <path d="M8.6 13.2l2.3 2.3 4.6-4.9" fill="none" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
              </span>
              <span className="nombre">
                <span className="logo">GEPRAC</span>
                <span className="lema">Gestión de Prácticas Académicas</span>
              </span>
            </span>
            {ctx && <span className="ctx">{ctx}</span>}
            {cuenta && (
              <div className="rol">
                <Link to={AYUDA} className={globo ? 'btn-ayuda' : 'btn-ayuda sin-globo'} aria-label="Ayuda"
                  aria-current={location.pathname === AYUDA ? 'page' : undefined}
                  onMouseEnter={() => setGlobo(true)} onFocus={() => setGlobo(true)}>
                  {ICONO_AYUDA}
                  <span className="globo" aria-hidden="true">Ayuda</span>
                </Link>
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
          <Pie />
        </div>
      </main>
    </div>
  )
}
