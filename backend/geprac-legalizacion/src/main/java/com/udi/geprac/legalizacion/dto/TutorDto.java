package com.udi.geprac.legalizacion.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * El tutor académico que la Dirección designa para una práctica (CU-04, paso 9
 * y variación 9.1): una cuenta de MS-01 con el rol de tutor.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public record TutorDto(@NotNull(message = "Escoja el tutor académico.") UUID tutorId) { }
