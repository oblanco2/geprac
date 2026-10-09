package com.udi.geprac.legalizacion.dto;

import com.udi.geprac.legalizacion.domain.Institucion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * Una institución receptora con sus contactos (P-14 y P-15).
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public record InstitucionDto(
    Long id,
    @NotBlank(message = ContactoDto.OBLIGATORIO) @Size(max = 150) String razonSocial,
    @Size(max = 20) String nit,
    @NotBlank(message = ContactoDto.OBLIGATORIO) @Size(max = 150) String direccion,
    @NotBlank(message = ContactoDto.OBLIGATORIO) @Size(max = 80) String ciudad,
    @NotBlank(message = ContactoDto.OBLIGATORIO) @Size(max = 20)
    @Pattern(regexp = ContactoDto.TELEFONO, message = ContactoDto.SOLO_NUMEROS) String telefono,
    @Size(max = 150) @Email(message = ContactoDto.CORREO) String correo,
    @Size(max = 150) String sitioWeb,
    @Size(max = 150) String representanteLegal,
    @NotNull(message = ContactoDto.OBLIGATORIO) Boolean activa,
    @NotEmpty(message = "Añada al menos un contacto.") List<@Valid ContactoDto> contactos
) {
    public static InstitucionDto de(Institucion i) {
        return new InstitucionDto(i.getId(), i.getRazonSocial(), i.getNit(), i.getDireccion(), i.getCiudad(),
            i.getTelefono(), i.getCorreo(), i.getSitioWeb(), i.getRepresentanteLegal(), i.getActiva(),
            i.getContactos().stream().map(ContactoDto::de).toList());
    }
}
