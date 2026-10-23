package com.udi.geprac.legalizacion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Lo que el estudiante diligencia en los tres pasos de la inscripción (P-05 a
 * P-07): la institución receptora, el contacto del escenario con las fechas
 * de la práctica, y los datos de su registro que puede ajustar en ella. Cada
 * paso envía solo lo suyo: lo que no viene, queda como estaba (CU-05,
 * variaciones 6.1 y 11.1). Los tres datos editables, si vienen, no pueden
 * quedar vacíos, porque los formatos los imprimen.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public record InscripcionCambiosDto(
    Long institucionId,
    Long contactoId,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    @Pattern(regexp = CON_TEXTO, message = ContactoDto.OBLIGATORIO)
    @Size(max = 150, message = "Admite hasta 150 caracteres.") String direccion,
    @Pattern(regexp = CON_TEXTO, message = ContactoDto.OBLIGATORIO)
    @Size(max = 20, message = "Admite hasta 20 caracteres.")
    @Pattern(regexp = ContactoDto.TELEFONO, message = ContactoDto.SOLO_NUMEROS) String celular,
    @Pattern(regexp = CON_TEXTO, message = ContactoDto.OBLIGATORIO)
    @Size(max = 150, message = "Admite hasta 150 caracteres.")
    @Email(message = ContactoDto.CORREO) String correoPersonal
) {
    /** Un texto con al menos un carácter que no sea espacio; un dato que no viene no se valida. */
    public static final String CON_TEXTO = "(?s).*\\S.*";
}
