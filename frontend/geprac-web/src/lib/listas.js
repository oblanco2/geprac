/**
 * Los ítems de las listas ordenables (P-13): cada texto con una clave que no
 * cambia aunque el ítem se mueva, para que la pantalla no confunda un ítem con
 * otro al reordenarlos.
 *
 * @author José Fernando Rincón Barrios
 */

let siguiente = 0

/** Una lista de textos como ítems con su clave. */
export const comoItems = (textos) => textos.map((texto) => ({ k: ++siguiente, texto }))
