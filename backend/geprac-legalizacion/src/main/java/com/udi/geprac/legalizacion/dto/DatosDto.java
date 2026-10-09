package com.udi.geprac.legalizacion.dto;

import com.udi.geprac.legalizacion.domain.InscripcionDatos;
import java.time.LocalDate;

/**
 * La copia de datos de una inscripción, como la presentan la revisión del
 * tutor (P-11) y el aval de la Dirección (P-18).
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public record DatosDto(
    String nombres,
    String apellidos,
    String tipoDocumento,
    String numeroDocumento,
    String lugarExpedicion,
    LocalDate fechaNacimiento,
    String lugarNacimiento,
    String genero,
    String estadoCivil,
    String eps,
    String direccion,
    String barrio,
    String ciudad,
    String telefonoFijo,
    String celular,
    String correoPersonal,
    String nombrePrograma,
    Integer semestreCursado,
    String perfilProfesional,
    String herramientasTrabajo,
    String institucionRazonSocial,
    String institucionNit,
    String institucionDireccion,
    String institucionCiudad,
    String institucionTelefono,
    String institucionCorreo,
    String institucionSitioWeb,
    String institucionRepresentante,
    String contactoNombre,
    String contactoCargo,
    String contactoTelefono,
    String contactoCelular,
    String contactoCorreo,
    String nombrePractica,
    String objetivoGeneral,
    String horarioEstandar
) {
    public static DatosDto de(InscripcionDatos d) {
        return new DatosDto(d.getNombres(), d.getApellidos(), d.getTipoDocumento(), d.getNumeroDocumento(), d.getLugarExpedicion(), d.getFechaNacimiento(), d.getLugarNacimiento(), d.getGenero(), d.getEstadoCivil(), d.getEps(), d.getDireccion(), d.getBarrio(), d.getCiudad(), d.getTelefonoFijo(), d.getCelular(), d.getCorreoPersonal(), d.getNombrePrograma(), d.getSemestreCursado(), d.getPerfilProfesional(), d.getHerramientasTrabajo(), d.getInstitucionRazonSocial(), d.getInstitucionNit(), d.getInstitucionDireccion(), d.getInstitucionCiudad(), d.getInstitucionTelefono(), d.getInstitucionCorreo(), d.getInstitucionSitioWeb(), d.getInstitucionRepresentante(), d.getContactoNombre(), d.getContactoCargo(), d.getContactoTelefono(), d.getContactoCelular(), d.getContactoCorreo(), d.getNombrePractica(), d.getObjetivoGeneral(), d.getHorarioEstandar());
    }
}
