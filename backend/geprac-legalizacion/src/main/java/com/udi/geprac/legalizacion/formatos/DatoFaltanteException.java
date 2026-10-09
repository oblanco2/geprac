package com.udi.geprac.legalizacion.formatos;

/**
 * La inscripción no tiene un dato que el formato exige: no se emite ningún
 * formato y se informa cuál falta y en qué formato (CU-08, excepción 6.1).
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public class DatoFaltanteException extends IllegalStateException {

    public DatoFaltanteException(String mensaje) {
        super(mensaje);
    }
}
