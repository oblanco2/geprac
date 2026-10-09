package com.udi.geprac.academico.service;

import com.udi.geprac.academico.domain.Usuario;
import com.udi.geprac.academico.dto.UsuarioDto;
import com.udi.geprac.academico.repository.UsuarioRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Las cuentas del software. La primera vez que una persona entra, se registra
 * su cuenta sin rol: la Dirección del Programa se lo asigna en CU-04, y hasta
 * entonces el cliente web no le presenta ningún menú.
 *
 * @author Oscar Iván Blanco Díaz
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;

    public UsuarioService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    /** La cuenta de quien hizo la petición; si es su primer ingreso, la registra. */
    @Transactional
    public UsuarioDto consultarCuenta() {
        UUID id = UsuarioActual.id();
        Usuario u = usuarios.findById(id)
            .orElseGet(() -> usuarios.save(new Usuario(id, UsuarioActual.nombre(), UsuarioActual.correo())));
        return UsuarioDto.de(u);
    }

    /**
     * Las cuentas por nombre, con su rol. Solo la consulta el director: con ella
     * el cliente web presenta el nombre del tutor que aprobó cada inscripción de
     * la bandeja de aval (CU-07, paso 2).
     */
    @Transactional(readOnly = true)
    public List<UsuarioDto> listarUsuarios() {
        return usuarios.findAllByOrderByNombrePresentacion().stream().map(UsuarioDto::de).toList();
    }
}
