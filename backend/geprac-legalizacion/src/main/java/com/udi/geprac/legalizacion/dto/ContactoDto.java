package com.udi.geprac.legalizacion.dto;

import com.udi.geprac.legalizacion.domain.ContactoInstitucion;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Un contacto de una institución receptora.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
public record ContactoDto(
    Long id,
    @NotBlank(message = OBLIGATORIO) @Size(max = 150) String nombre,
    @NotBlank(message = OBLIGATORIO) @Size(max = 100) String cargo,
    @NotBlank(message = OBLIGATORIO) @Size(max = 20) @Pattern(regexp = TELEFONO, message = SOLO_NUMEROS) String telefono,
    @Size(max = 20) @Pattern(regexp = TELEFONO, message = SOLO_NUMEROS) String celular,
    @Size(max = 150) @Email(message = CORREO) String correo
) {
    public static final String OBLIGATORIO = "Este dato es obligatorio.";
    public static final String TELEFONO = "[0-9 +()-]*";
    public static final String SOLO_NUMEROS = "Escriba solo números; admite espacios.";
    public static final String CORREO = "Escriba un correo válido, como nombre@dominio.com.";

    public static ContactoDto de(ContactoInstitucion c) {
        return new ContactoDto(c.getId(), c.getNombre(), c.getCargo(), c.getTelefono(), c.getCelular(), c.getCorreo());
    }
}
