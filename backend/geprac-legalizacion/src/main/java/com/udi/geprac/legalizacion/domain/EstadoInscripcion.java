package com.udi.geprac.legalizacion.domain;

/**
 * Los cinco estados de una inscripción. Nace en borrador; el estudiante la
 * envía; el tutor la aprueba o la devuelve con motivo; la devuelta se corrige y
 * se reenvía, y la aprobada la avala la Dirección del Programa.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public enum EstadoInscripcion {
    BORRADOR,
    ENVIADA,
    DEVUELTA,
    APROBADA,
    AVALADA
}
