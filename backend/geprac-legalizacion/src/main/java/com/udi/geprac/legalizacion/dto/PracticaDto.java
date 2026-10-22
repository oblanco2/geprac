package com.udi.geprac.legalizacion.dto;

import com.udi.geprac.legalizacion.domain.ActividadPractica;
import com.udi.geprac.legalizacion.domain.ObjetivoPractica;
import com.udi.geprac.legalizacion.domain.Practica;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Una práctica del catálogo con su contenido estandarizado: el que el software
 * precarga en cada inscripción (P-12 y P-13). Los objetivos específicos y las
 * actividades van en el orden en que salen impresos en el formato.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public record PracticaDto(
    Long id,
    @NotNull(message = ContactoDto.OBLIGATORIO)
    @Min(value = 1, message = PracticaDto.POSICION) @Max(value = 8, message = PracticaDto.POSICION) Integer orden,
    @NotBlank(message = ContactoDto.OBLIGATORIO) @Size(max = 150, message = "Admite hasta 150 caracteres.") String nombre,
    @NotBlank(message = ContactoDto.OBLIGATORIO) @Size(max = 600, message = "Admite hasta 600 caracteres.") String objetivoGeneral,
    @Size(max = 150, message = "Admite hasta 150 caracteres.") String horarioEstandar,
    @NotNull(message = ContactoDto.OBLIGATORIO) Boolean activa,
    @NotEmpty(message = "Añada al menos un objetivo específico.")
    List<@NotBlank(message = PracticaDto.ITEM) String> objetivos,
    @NotEmpty(message = "Añada al menos una actividad.")
    List<@NotBlank(message = PracticaDto.ITEM) String> actividades
) {
    public static final String POSICION = "La posición debe ser un número entero de 1 a 8.";
    public static final String ITEM = "Escriba el texto o quite el ítem.";

    public static PracticaDto de(Practica p) {
        return new PracticaDto(p.getId(), p.getOrden(), p.getNombre(), p.getObjetivoGeneral(), p.getHorarioEstandar(),
            p.getActiva(), p.getObjetivos().stream().map(ObjetivoPractica::getTexto).toList(),
            p.getActividades().stream().map(ActividadPractica::getTexto).toList());
    }
}
