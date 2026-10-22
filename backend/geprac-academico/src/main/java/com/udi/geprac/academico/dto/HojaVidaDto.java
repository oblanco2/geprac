package com.udi.geprac.academico.dto;

import java.util.List;

/**
 * La hoja de vida del estudiante (P-04): los dos bloques narrativos y las tres
 * tablas, cada una en el orden en que la presenta la pantalla —formación por
 * tipo y año, experiencia de la más reciente a la más antigua y referencias
 * en el orden en que se registraron—, con el indicador de completitud.
 *
 * @author Oscar Iván Blanco Díaz
 */
public record HojaVidaDto(
    String perfilProfesional,
    String herramientasTrabajo,
    List<EntradaDto.Formacion> formaciones,
    List<EntradaDto.Experiencia> experiencias,
    List<EntradaDto.Referencia> referencias,
    CompletitudDto completitud
) {
}
