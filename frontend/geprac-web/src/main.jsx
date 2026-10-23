/**
 * Punto de entrada del cliente web de GEPRAC: carga la hoja de estilos —la del
 * prototipo de alta fidelidad— y monta la aplicación en el elemento #root.
 *
 * @author José Fernando Rincón Barrios
 */
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './estilos/geprac.css'
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
