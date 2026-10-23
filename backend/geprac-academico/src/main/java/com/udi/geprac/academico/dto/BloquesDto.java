package com.udi.geprac.academico.dto;

import jakarta.validation.constraints.Size;

/**
 * Los dos bloques narrativos de la hoja de vida (CU-01, pasos 12 y 13): el
 * perfil profesional, redactado en tercera persona, y las herramientas de
 * trabajo. La extensión máxima es la que cabe en el formato PR-01
 * (excepción 12.1).
 *
 * @author Oscar Iván Blanco Díaz
 */
public record BloquesDto(
    @Size(max = 800, message = "Supera la extensión máxima de 800 caracteres.") String perfilProfesional,
    @Size(max = 400, message = "Supera la extensión máxima de 400 caracteres.") String herramientasTrabajo
) {
}
