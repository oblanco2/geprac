/**
 * El pie de página de todas las pantallas, en tres bloques: los derechos de
 * autor a la izquierda, los nombres de quienes desarrollaron el software en el
 * centro exacto y la universidad a la derecha. En la tableta y el celular van
 * uno debajo del otro, centrados. Los nombres no se parten al cambiar de línea.
 * Lo comparten el marco de la aplicación y el inicio de sesión.
 *
 * @author José Fernando Rincón Barrios
 */
export default function Pie() {
  return (
    <footer className="pie-app">
      <span className="pie-izq">© {new Date().getFullYear()} <b>GEPRAC</b><br />Gestión de Prácticas Académicas</span>
      <span className="pie-centro">Desarrollado por<br />Oscar&nbsp;Blanco - Darien&nbsp;Pesca - José&nbsp;Rincón</span>
      <span className="pie-der">Universidad de Investigación y Desarrollo (UDI)<br />Todos los derechos reservados</span>
    </footer>
  )
}
