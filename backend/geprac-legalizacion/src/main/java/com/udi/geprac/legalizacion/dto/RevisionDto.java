package com.udi.geprac.legalizacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * El resultado de la revisión del tutor: APROBADA, o DEVUELTA con su motivo.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public record RevisionDto(
    @NotBlank(message = "Escoja el resultado de la revisión.") String resultado,
    @Size(max = 600, message = "Admite hasta 600 caracteres.") String motivo
) { }
