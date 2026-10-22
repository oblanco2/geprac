package com.udi.geprac.academico.dto;

import java.util.List;

/**
 * El indicador de completitud del perfil (CU-01, paso 9): qué tanto del
 * registro exigen los cuatro formatos institucionales y qué falta, nombrado
 * como lo ve el estudiante. Se mide sobre los dieciocho datos personales y los
 * cuatro bloques obligatorios de la hoja de vida; la experiencia laboral es
 * opcional en el formato y no cuenta. MS-02 consulta este mismo indicador
 * antes de crear una inscripción (CU-05, paso 3).
 *
 * @author Oscar Iván Blanco Díaz
 */
public record CompletitudDto(int porcentaje, boolean completo, List<String> faltan) {
}
