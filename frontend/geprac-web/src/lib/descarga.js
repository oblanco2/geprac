/**
 * Entrega al navegador un archivo que llegó del servicio, con el nombre que le
 * corresponde: así se descargan los formatos en PDF (CU-08) y el historial en
 * CSV (CU-09).
 *
 * @author José Fernando Rincón Barrios
 */
export function guardarArchivo(contenido, nombre, tipo) {
  const blob = contenido instanceof Blob ? contenido : new Blob([contenido], { type: tipo })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = nombre
  document.body.appendChild(a)
  a.click()
  a.remove()
  setTimeout(() => URL.revokeObjectURL(url), 1000)
}
