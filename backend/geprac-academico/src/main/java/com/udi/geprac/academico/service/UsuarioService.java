package com.udi.geprac.academico.service;

import com.udi.geprac.academico.domain.Programa;
import com.udi.geprac.academico.domain.Rol;
import com.udi.geprac.academico.domain.Usuario;
import com.udi.geprac.academico.dto.UsuarioDto;
import com.udi.geprac.academico.repository.ProgramaRepository;
import com.udi.geprac.academico.repository.UsuarioRepository;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Las cuentas del software. La primera vez que una persona entra, se registra
 * su cuenta sin rol: la Dirección del Programa se lo asigna en CU-04, y hasta
 * entonces el cliente web no le presenta ningún menú. MS-02 consulta aquí el
 * rol de una cuenta antes de designarla tutora de una práctica (CU-04, paso 10).
 *
 * @author Oscar Iván Blanco Díaz
 */
@Service
public class UsuarioService {

    private static final String NO_EXISTE =
        "La cuenta no existe: el software solo asigna rol a quien ya ingresó al menos una vez.";

    private final UsuarioRepository usuarios;
    private final ProgramaRepository programas;

    public UsuarioService(UsuarioRepository usuarios, ProgramaRepository programas) {
        this.usuarios = usuarios;
        this.programas = programas;
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

    /**
     * CU-04, variación 2.1: la Dirección asigna el rol de una cuenta. Al rol de
     * director lo acompaña el programa, que solo puede ser el de quien asigna,
     * porque cada director opera sobre su programa (RNF-06); con los demás
     * roles la cuenta queda sin programa. El director no puede quitarse su
     * propio rol: el programa quedaría sin quien asigne los roles.
     *
     * La regla 2.1.4 —no quitarle el rol de tutor a quien tiene prácticas
     * designadas en el semestre abierto— la comprueba el cliente web con las
     * designaciones de MS-02, porque MS-01 no las guarda y los servicios solo
     * se comunican de MS-02 hacia MS-01.
     */
    @Transactional
    public UsuarioDto asignarRol(UUID id, String rol, String programa) {
        Rol nuevo = rol(rol);
        Usuario u = usuarios.findById(id).orElseThrow(() -> new NoSuchElementException(NO_EXISTE));
        if (u.getId().equals(UsuarioActual.id()) && nuevo != Rol.DIRECTOR)
            throw new IllegalStateException("No puede quitarse el rol de director: el programa quedaría sin quien asigne los roles.");
        u.asignarRol(nuevo, nuevo == Rol.DIRECTOR ? programaDelDirector(programa) : null);
        return UsuarioDto.de(usuarios.save(u));
    }

    /** CU-04, paso 10: el rol de una cuenta, que MS-02 consulta antes de designarla tutora; nulo si aún no tiene. */
    @Transactional(readOnly = true)
    public Rol consultarRol(UUID id) {
        Usuario u = usuarios.findById(id).orElseThrow(() -> new NoSuchElementException(NO_EXISTE));
        return u.getRol();
    }

    // ------------------------------------------------------------------ apoyo

    private static Rol rol(String valor) {
        try {
            return Rol.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException e) {
            throw new CampoInvalidoException("rol", "Escoja el rol: director, tutor académico o estudiante.");
        }
    }

    /** El programa del director que asigna: el que indica la petición tiene que ser ese mismo. */
    private Programa programaDelDirector(String programa) {
        String suyo = UsuarioActual.programa();
        if (programa != null && !programa.isBlank() && !programa.trim().equalsIgnoreCase(suyo))
            throw new CampoInvalidoException("programa", "Solo puede asignar la Dirección de su propio programa.");
        if (suyo == null)
            throw new IllegalStateException("Su cuenta de director no tiene programa: no puede asignar la Dirección.");
        return programas.findByCodigoIgnoreCase(suyo)
            .orElseThrow(() -> new CampoInvalidoException("programa", "El programa no existe."));
    }
}
