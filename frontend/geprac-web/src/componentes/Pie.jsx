/**
 * El pie de página de todas las pantallas, con los derechos de autor: lo
 * comparten el marco de la aplicación y el inicio de sesión.
 *
 * @author José Fernando Rincón Barrios
 */
export default function Pie() {
  return (
    <footer className="pie-app">
      <span>© {new Date().getFullYear()} <b>GEPRAC</b> · Gestión de Prácticas Académicas</span>
      <span>Universidad de Investigación y Desarrollo (UDI) · Todos los derechos reservados</span>
    </footer>
  )
}
