package com.udi.geprac.academico.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * El rol que la Dirección del Programa le asigna a una cuenta (CU-04,
 * variación 2.1). El programa solo cuenta cuando el rol es DIRECTOR; si no
 * viene, es el del director que hace la asignación.
 *
 * @author Oscar Iván Blanco Díaz
 */
public record RolDto(
    @NotBlank(message = "Escoja el rol: director, tutor académico o estudiante.") String rol,
    String programa
) { }
