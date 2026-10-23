package com.udi.geprac.academico.domain;

/**
 * Los tres roles con los que se opera el software. La Dirección del Programa
 * los asigna (CU-04); mientras no lo haga, la cuenta queda sin rol.
 *
 * @author Oscar Iván Blanco Díaz
 */
public enum Rol {
    DIRECTOR,
    TUTOR,
    ESTUDIANTE
}
