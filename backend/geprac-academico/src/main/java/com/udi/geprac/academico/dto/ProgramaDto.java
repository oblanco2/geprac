package com.udi.geprac.academico.dto;

import com.udi.geprac.academico.domain.Programa;

/**
 * Programa académico tal como lo recibe el cliente web.
 *
 * @param codigo código institucional, por ejemplo LEI
 * @param nombre nombre completo del programa
 * @author Oscar Iván Blanco Díaz
 */
public record ProgramaDto(String codigo, String nombre) {

    /** Copia los datos de la entidad, sin exponerla. */
    public static ProgramaDto de(Programa programa) {
        return new ProgramaDto(programa.getCodigo(), programa.getNombre());
    }
}
