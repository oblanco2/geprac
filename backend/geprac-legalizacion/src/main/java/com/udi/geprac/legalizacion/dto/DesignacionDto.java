package com.udi.geprac.legalizacion.dto;

import com.udi.geprac.legalizacion.domain.Practica;
import java.util.UUID;

/**
 * Una práctica del programa con el tutor académico designado en el semestre
 * (P-16): el tutor es una cuenta de MS-01, y su nombre lo pone el cliente web
 * con la lista de cuentas. Las enviadas son las inscripciones de la práctica
 * que esperan revisión: las que pasan a la bandeja del tutor designado.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public record DesignacionDto(Long practicaId, Integer orden, String nombre, Boolean activa, UUID tutorId, long enviadas) {

    public static DesignacionDto de(Practica p, UUID tutorId, long enviadas) {
        return new DesignacionDto(p.getId(), p.getOrden(), p.getNombre(), p.getActiva(), tutorId, enviadas);
    }
}
