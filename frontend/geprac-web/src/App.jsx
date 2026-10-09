/**
 * El cliente web de GEPRAC: las rutas de los tres roles con las pantallas de los
 * cinco casos de uso del prototipo funcional —CU-03, CU-06, CU-07, CU-08 y
 * CU-09— y los proveedores que comparten todas: la sesión, los avisos y la
 * ventana de confirmación. Las direcciones van tras #, así el hospedaje estático
 * sirve cualquier pantalla y los botones atrás y adelante funcionan.
 *
 * @author José Fernando Rincón Barrios
 */
import { Navigate, RouterProvider, createHashRouter } from 'react-router-dom'
import SesionProveedor from './sesion/SesionProveedor'
import AvisosProveedor from './componentes/AvisosProveedor'
import DialogoProveedor from './componentes/DialogoProveedor'
import Protegida from './componentes/Protegida'
import Inicio from './paginas/Inicio'
import Ingreso from './paginas/Ingreso'
import MisPracticas from './paginas/estudiante/MisPracticas'
import MisFormatos from './paginas/estudiante/MisFormatos'
import Bandeja from './paginas/tutor/Bandeja'
import Revisar from './paginas/tutor/Revisar'
import BandejaAval from './paginas/director/BandejaAval'
import Avalar from './paginas/director/Avalar'
import Instituciones from './paginas/director/Instituciones'
import EditorInstitucion from './paginas/director/EditorInstitucion'
import HistorialPracticas from './paginas/comun/HistorialPracticas'

const rutas = createHashRouter([
  { path: '/', element: <Inicio /> },
  { path: '/ingreso', element: <Ingreso /> },                                 // P-01
  {
    element: <Protegida roles={['ESTUDIANTE']} />,
    children: [
      { path: '/mis-practicas', element: <MisPracticas /> },                  // P-02 · CU-09
      { path: '/mis-formatos', element: <MisFormatos /> },                    // P-09 · CU-08
      { path: '/mis-formatos/:id', element: <MisFormatos /> },
    ],
  },
  {
    element: <Protegida roles={['TUTOR']} />,
    children: [
      { path: '/bandeja', element: <Bandeja /> },                             // P-10 · CU-06
      { path: '/bandeja/:id', element: <Revisar /> },                         // P-11 · CU-06
    ],
  },
  {
    element: <Protegida roles={['DIRECTOR']} />,
    children: [
      { path: '/aval', element: <BandejaAval /> },                            // P-17 · CU-07
      { path: '/aval/:id', element: <Avalar /> },                             // P-18 · CU-07
      { path: '/instituciones', element: <Instituciones /> },                 // P-14 · CU-03
      { path: '/instituciones/:id', element: <EditorInstitucion /> },         // P-15 · CU-03
    ],
  },
  {
    element: <Protegida roles={['TUTOR', 'DIRECTOR']} />,
    children: [
      { path: '/historial', element: <HistorialPracticas /> },                // P-19 · CU-09
    ],
  },
  { path: '*', element: <Navigate to="/" replace /> },
])

export default function App() {
  return (
    <AvisosProveedor>
      <DialogoProveedor>
        <SesionProveedor>
          <RouterProvider router={rutas} />
        </SesionProveedor>
      </DialogoProveedor>
    </AvisosProveedor>
  )
}
