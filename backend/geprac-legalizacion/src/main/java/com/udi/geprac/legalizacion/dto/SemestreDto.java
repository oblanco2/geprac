package com.udi.geprac.legalizacion.dto;

import com.udi.geprac.legalizacion.domain.Semestre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * Un semestre académico: su código, sus fechas y si está abierto (P-16). Al
 * abrir uno nuevo solo se envían el código y las dos fechas.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public record SemestreDto(
    Long id,
    @NotBlank(message = SemestreDto.OBLIGATORIO)
    @Pattern(regexp = "\\d{4}-[12]", message = "Escriba el año y el periodo, por ejemplo 2027-1.") String codigo,
    @NotNull(message = SemestreDto.OBLIGATORIO) LocalDate fechaInicio,
    @NotNull(message = SemestreDto.OBLIGATORIO) LocalDate fechaCierre,
    Boolean abierto
) {
    public static final String OBLIGATORIO = "Este dato es obligatorio para abrir el semestre.";

    public static SemestreDto de(Semestre s) {
        return new SemestreDto(s.getId(), s.getCodigo(), s.getFechaInicio(), s.getFechaCierre(), s.getAbierto());
    }
}
