import { useState } from 'react'
import { supabase } from './lib/supabase'
import cliente from './api/cliente'

export default function App() {
  const [correo, setCorreo] = useState('director@udi.edu.co')
  const [clave, setClave] = useState('')
  const [sesion, setSesion] = useState(null)
  const [programas, setProgramas] = useState(null)
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState(null)

  const entrar = async (e) => {
    e.preventDefault()
    setError(null)
    setCargando(true)
    const { data, error } = await supabase.auth.signInWithPassword({
      email: correo,
      password: clave,
    })
    setCargando(false)
    if (error) setError(error.message)
    else setSesion(data.session)
  }

  const consultar = async () => {
    setError(null)
    setCargando(true)
    try {
      const { data } = await cliente.get('/programas')
      setProgramas(data)
    } catch (err) {
      setError(err.mensaje)
    } finally {
      setCargando(false)
    }
  }

  const salir = async () => {
    await supabase.auth.signOut()
    setSesion(null)
    setProgramas(null)
  }

  return (
    <div className="container py-5" style={{ maxWidth: '640px' }}>
      <div className="mb-4">
        <span className="d-inline-block borde-oro ps-2">
          <h1 className="h3 fw-bold mb-0">GEPRAC</h1>
        </span>
        <p className="text-secondary small mb-0 mt-1">
          Software para la Gestión de Prácticas Académicas
        </p>
      </div>

      {!sesion ? (
        <div className="card border-0 shadow-sm">
          <div className="card-body p-4">
            <h2 className="h5 mb-3">Ingresa a tu cuenta</h2>
            <form onSubmit={entrar}>
              <div className="mb-3">
                <label htmlFor="correo" className="form-label small">Correo</label>
                <input
                  id="correo" type="email" className="form-control"
                  value={correo} onChange={(e) => setCorreo(e.target.value)} required
                />
              </div>
              <div className="mb-3">
                <label htmlFor="clave" className="form-label small">Contraseña</label>
                <input
                  id="clave" type="password" className="form-control"
                  value={clave} onChange={(e) => setClave(e.target.value)} required
                />
              </div>
              <button type="submit" className="btn btn-primary w-100" disabled={cargando}>
                {cargando ? 'Verificando…' : 'Entrar'}
              </button>
            </form>
          </div>
        </div>
      ) : (
        <div className="card border-0 shadow-sm">
          <div className="card-body p-4">
            <div className="d-flex justify-content-between align-items-start mb-3">
              <div>
                <p className="mb-0 fw-semibold">{sesion.user.email}</p>
                <p className="text-secondary small mb-0">Sesión activa</p>
              </div>
              <button className="btn btn-outline-secondary btn-sm" onClick={salir}>
                Salir
              </button>
            </div>

            <button className="btn btn-primary" onClick={consultar} disabled={cargando}>
              {cargando ? 'Consultando…' : 'Consultar programas'}
            </button>

            {programas && (
              <div className="alert alert-success mt-3 mb-0 small">
                Respuesta del microservicio: <code>{JSON.stringify(programas)}</code>
              </div>
            )}
          </div>
        </div>
      )}

      {error && <div className="alert alert-danger mt-3 small">{error}</div>}
    </div>
  )
}