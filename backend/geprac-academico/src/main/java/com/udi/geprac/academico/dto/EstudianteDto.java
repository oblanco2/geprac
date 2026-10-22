package com.udi.geprac.academico.dto;

import com.udi.geprac.academico.domain.Estudiante;
import com.udi.geprac.academico.domain.Usuario;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * Los datos personales del estudiante (P-03): información personal, residencia
 * y contacto, y programa. Llegan del cliente web con estas validaciones, que
 * repiten las de la pantalla; el correo institucional, el nombre del programa
 * y el indicador de completitud solo salen hacia el cliente.
 *
 * Las opciones del tipo de documento y del género son las que imprimen los
 * formatos institucionales (CU-01, paso 7).
 *
 * @author Oscar Iván Blanco Díaz
 */
public record EstudianteDto(
    Long id,
    String correoInstitucional,
    @NotBlank(message = OBLIGATORIO) @Size(max = 100) String nombres,
    @NotBlank(message = OBLIGATORIO) @Size(max = 100) String apellidos,
    @NotBlank(message = OBLIGATORIO) @Pattern(regexp = TIPOS_DOCUMENTO, message = MSJ_TIPO_DOCUMENTO) String tipoDocumento,
    @NotBlank(message = OBLIGATORIO) @Pattern(regexp = DOCUMENTO, message = MSJ_DOCUMENTO) String numeroDocumento,
    @Size(max = 80) String lugarExpedicion,
    @Past(message = "La fecha de nacimiento debe ser anterior a hoy.") LocalDate fechaNacimiento,
    @Size(max = 80) String lugarNacimiento,
    @Pattern(regexp = GENEROS, message = MSJ_GENERO) String genero,
    @Pattern(regexp = ESTADOS_CIVILES, message = MSJ_OPCION) String estadoCivil,
    @Size(max = 80) String eps,
    @Size(max = 150) String direccion,
    @Size(max = 80) String barrio,
    @Size(max = 80) String ciudad,
    @Size(max = 20) @Pattern(regexp = TELEFONO, message = SOLO_NUMEROS) String telefonoFijo,
    @Size(max = 20) @Pattern(regexp = TELEFONO, message = SOLO_NUMEROS) String celular,
    @Size(max = 150) @Email(message = CORREO) String correoPersonal,
    @NotBlank(message = OBLIGATORIO) @Size(max = 10) String codigoPrograma,
    String nombrePrograma,
    @NotNull(message = OBLIGATORIO) @Min(value = 1, message = MSJ_SEMESTRE) @Max(value = 8, message = MSJ_SEMESTRE)
    Integer semestreActual,
    CompletitudDto completitud
) {
    public static final String OBLIGATORIO = "Este dato es obligatorio.";
    public static final String TIPOS_DOCUMENTO = "C\\.C\\.|T\\.I\\.|C\\.E\\.";
    public static final String MSJ_TIPO_DOCUMENTO = "Seleccione C.C., T.I. o C.E., que son las opciones de los formatos.";
    /** De 5 a 12 dígitos, con puntos opcionales entre ellos. */
    public static final String DOCUMENTO = "(?=(?:\\.?\\d){5,12}$)\\d+(?:\\.\\d+)*";
    public static final String MSJ_DOCUMENTO = "Escriba de 5 a 12 dígitos, sin letras ni otros signos; los puntos son opcionales.";
    public static final String GENEROS = "|Femenino|Masculino";
    public static final String MSJ_GENERO = "Seleccione Femenino o Masculino, que son las opciones de PR-04.";
    public static final String ESTADOS_CIVILES =
        "|Soltero\\(a\\)|Casado\\(a\\)|Unión marital de hecho|Separado\\(a\\)|Divorciado\\(a\\)|Viudo\\(a\\)";
    public static final String MSJ_OPCION = "Seleccione una de las opciones de la lista.";
    public static final String TELEFONO = "[0-9 +()-]*";
    public static final String SOLO_NUMEROS = "Escriba solo números; admite espacios.";
    public static final String CORREO = "Escriba un correo válido, como nombre@dominio.com.";
    public static final String MSJ_SEMESTRE = "Seleccione un semestre del primero al octavo.";

    /** El registro guardado, con su indicador. */
    public static EstudianteDto de(Estudiante e, CompletitudDto completitud) {
        return new EstudianteDto(e.getId(), e.getUsuario().getCorreoInstitucional(), e.getNombres(), e.getApellidos(),
            e.getTipoDocumento(), e.getNumeroDocumento(), e.getLugarExpedicion(), e.getFechaNacimiento(),
            e.getLugarNacimiento(), e.getGenero(), e.getEstadoCivil(), e.getEps(), e.getDireccion(), e.getBarrio(),
            e.getCiudad(), e.getTelefonoFijo(), e.getCelular(), e.getCorreoPersonal(), e.getPrograma().getCodigo(),
            e.getPrograma().getNombre(), e.getSemestreActual(), completitud);
    }

    /** Todavía sin registro (CU-01, paso 2): el formulario vacío, solo con el correo de la cuenta. */
    public static EstudianteDto vacio(Usuario u, CompletitudDto completitud) {
        return new EstudianteDto(null, u.getCorreoInstitucional(), null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, completitud);
    }
}
