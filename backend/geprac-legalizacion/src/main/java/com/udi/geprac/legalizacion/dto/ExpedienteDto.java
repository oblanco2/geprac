package com.udi.geprac.legalizacion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.util.List;

/**
 * El expediente del estudiante tal como lo responde MS-01: sus datos
 * personales con el indicador de completitud (GET /estudiantes/me) y su hoja
 * de vida (GET /estudiantes/me/hoja-vida). MS-02 lo copia en la inscripción al
 * crearla (CU-05, paso 4). Sin registro en MS-01, el perfil llega sin
 * identificador; con el registro incompleto, no se pide la hoja de vida y
 * llega nula. Los datos que MS-01 añada después no se leen.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExpedienteDto(Perfil perfil, HojaVida hojaVida) {

    /** Los datos personales, el programa y el semestre que cursa, con el indicador de completitud. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Perfil(
        Long id, String nombres, String apellidos, String tipoDocumento, String numeroDocumento,
        String lugarExpedicion, LocalDate fechaNacimiento, String lugarNacimiento, String genero,
        String estadoCivil, String eps, String direccion, String barrio, String ciudad, String telefonoFijo,
        String celular, String correoPersonal, String codigoPrograma, String nombrePrograma,
        Integer semestreActual, Completitud completitud
    ) { }

    /** El indicador de MS-01: si el registro tiene lo que exigen los formatos y, si no, qué falta. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Completitud(int porcentaje, boolean completo, List<String> faltan) { }

    /** Los dos bloques narrativos y las tres tablas de la hoja de vida. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HojaVida(String perfilProfesional, String herramientasTrabajo, List<Formacion> formaciones,
                           List<Experiencia> experiencias, List<Referencia> referencias) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Formacion(String tipo, String institucion, String nombre, Integer anio) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Experiencia(String empresa, String cargo, String jefeInmediato, String cargoJefe,
                              String telefonoEmpresa, LocalDate fechaInicio, LocalDate fechaFin,
                              String funciones, String logros) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Referencia(String nombre, String empresa, String cargo, String telefono, String ciudad) { }
}
