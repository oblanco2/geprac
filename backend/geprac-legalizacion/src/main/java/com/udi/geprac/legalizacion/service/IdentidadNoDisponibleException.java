package com.udi.geprac.legalizacion.service;

/**
 * MS-01 no respondió a una consulta de MS-02: la operación no se completa y no
 * se guarda nada. El cliente web la presenta como una falla temporal, que se
 * resuelve intentando de nuevo.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public class IdentidadNoDisponibleException extends RuntimeException {

    public IdentidadNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
