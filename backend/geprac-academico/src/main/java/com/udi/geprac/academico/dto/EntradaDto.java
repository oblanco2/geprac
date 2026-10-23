package com.udi.geprac.academico.dto;

import static com.udi.geprac.academico.dto.EstudianteDto.OBLIGATORIO;
import static com.udi.geprac.academico.dto.EstudianteDto.SOLO_NUMEROS;
import static com.udi.geprac.academico.dto.EstudianteDto.TELEFONO;

import com.udi.geprac.academico.domain.ExperienciaLaboral;
import com.udi.geprac.academico.domain.FormacionAcademica;
import com.udi.geprac.academico.domain.ReferenciaPersonal;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * Una entrada de la hoja de vida (P-04): de formación académica, de
 * experiencia laboral o de referencias personales. Cada bloque tiene sus
 * datos y sus comprobaciones (CU-01, pasos 14 a 16 y variaciones 14.1 y 14.2);
 * las que dependen de la fecha en curso o de las demás entradas las aplica el
 * servicio.
 *
 * @author Oscar Iván Blanco Díaz
 */
public sealed interface EntradaDto permits EntradaDto.Formacion, EntradaDto.Experiencia, EntradaDto.Referencia {

    Long id();

    /** Formación académica: el tipo es una de las tres opciones del formato (excepción 15.2). */
    record Formacion(
        Long id,
        @NotBlank(message = OBLIGATORIO)
        @Pattern(regexp = "Secundaria|Universitaria|Otros",
                 message = "El tipo de formación no es uno de los tres del formato. Selecciónelo de nuevo.") String tipo,
        @NotBlank(message = OBLIGATORIO) @Size(max = 150) String institucion,
        @NotBlank(message = OBLIGATORIO) @Size(max = 150) String nombre,
        Integer anio
    ) implements EntradaDto {
        public static Formacion de(FormacionAcademica f) {
            return new Formacion(f.getId(), f.getTipo(), f.getInstitucion(), f.getNombre(), f.getAnio());
        }
    }

    /** Experiencia laboral: la fecha de retiro queda vacía si el empleo sigue vigente. */
    record Experiencia(
        Long id,
        @NotBlank(message = OBLIGATORIO) @Size(max = 150) String empresa,
        @NotBlank(message = OBLIGATORIO) @Size(max = 100) String cargo,
        @Size(max = 150) String jefeInmediato,
        @Size(max = 100) String cargoJefe,
        @Size(max = 20) @Pattern(regexp = TELEFONO, message = SOLO_NUMEROS) String telefonoEmpresa,
        @NotNull(message = OBLIGATORIO)
        @PastOrPresent(message = "La fecha de ingreso no puede ser posterior a hoy.") LocalDate fechaInicio,
        @PastOrPresent(message = "La fecha de retiro no puede ser posterior a hoy.") LocalDate fechaFin,
        String funciones,
        String logros
    ) implements EntradaDto {
        public static Experiencia de(ExperienciaLaboral x) {
            return new Experiencia(x.getId(), x.getEmpresa(), x.getCargo(), x.getJefeInmediato(), x.getCargoJefe(),
                x.getTelefonoEmpresa(), x.getFechaInicio(), x.getFechaFin(), x.getFunciones(), x.getLogros());
        }
    }

    /** Referencia personal: los cinco datos son obligatorios (variación 14.2). */
    record Referencia(
        Long id,
        @NotBlank(message = OBLIGATORIO) @Size(max = 150) String nombre,
        @NotBlank(message = OBLIGATORIO) @Size(max = 150) String empresa,
        @NotBlank(message = OBLIGATORIO) @Size(max = 100) String cargo,
        @NotBlank(message = OBLIGATORIO) @Size(max = 20) @Pattern(regexp = TELEFONO, message = SOLO_NUMEROS) String telefono,
        @NotBlank(message = OBLIGATORIO) @Size(max = 80) String ciudad
    ) implements EntradaDto {
        public static Referencia de(ReferenciaPersonal r) {
            return new Referencia(r.getId(), r.getNombre(), r.getEmpresa(), r.getCargo(), r.getTelefono(), r.getCiudad());
        }
    }
}
