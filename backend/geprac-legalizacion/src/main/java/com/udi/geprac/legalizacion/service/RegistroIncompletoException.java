package com.udi.geprac.legalizacion.service;

import java.util.List;

/**
 * Un registro no tiene todo lo que exige el paso, y el software enumera lo que
 * falta: el perfil o la hoja de vida del estudiante al iniciar la inscripción
 * (CU-05, excepción 3.1), o la institución, el contacto o las fechas al
 * enviarla (excepción 14.1). No se guarda nada, y el cliente web lleva al
 * estudiante a donde se diligencia lo que falta.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public class RegistroIncompletoException extends RuntimeException {

    private final List<String> faltan;

    public RegistroIncompletoException(String mensaje, List<String> faltan) {
        super(mensaje);
        this.faltan = List.copyOf(faltan);
    }

    /** Lo que falta, nombrado como lo ve el estudiante. */
    public List<String> getFaltan() { return faltan; }
}
