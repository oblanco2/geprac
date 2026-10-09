package com.udi.geprac.academico.dto;

import com.udi.geprac.academico.domain.Usuario;
import java.util.UUID;

/**
 * Una cuenta: con su rol el cliente web decide el menú y la pantalla de inicio,
 * y con su nombre presenta a quien aprobó una inscripción. El programa solo
 * viene para el director.
 *
 * @author Oscar Iván Blanco Díaz
 */
public record UsuarioDto(UUID id, String nombrePresentacion, String correoInstitucional,
                         String rol, String programa) {

    public static UsuarioDto de(Usuario u) {
        return new UsuarioDto(u.getId(), u.getNombrePresentacion(), u.getCorreoInstitucional(),
            u.getRol() == null ? null : u.getRol().name(),
            u.getPrograma() == null ? null : u.getPrograma().getCodigo());
    }
}
