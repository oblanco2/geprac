package com.udi.geprac.academico.controller;

import com.udi.geprac.academico.domain.Rol;
import com.udi.geprac.academico.dto.RolDto;
import com.udi.geprac.academico.dto.UsuarioDto;
import com.udi.geprac.academico.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Las cuentas: la de quien ingresa, que el cliente web pide después de
 * autenticar para resolver con el rol el menú y la pantalla de inicio (RF-01),
 * la lista de cuentas que consulta la Dirección del Programa y el rol de cada
 * cuenta, que la Dirección asigna y MS-02 consulta (CU-04).
 *
 * @author Oscar Iván Blanco Díaz
 */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService servicio;

    public UsuarioController(UsuarioService servicio) {
        this.servicio = servicio;
    }

    /** GET /api/usuarios/me: la cuenta; en el primer ingreso, la registra sin rol. */
    @GetMapping("/me")
    public UsuarioDto consultarCuenta() {
        return servicio.consultarCuenta();
    }

    /** GET /api/usuarios: las cuentas con su rol, solo para el director. */
    @GetMapping
    public List<UsuarioDto> listarUsuarios() {
        return servicio.listarUsuarios();
    }

    /** PUT /api/usuarios/{id}/rol: la Dirección asigna el rol de la cuenta {id} (P-16, CU-04 2.1). */
    @PutMapping("/{id}/rol")
    public UsuarioDto asignarRol(@PathVariable("id") UUID id, @Valid @RequestBody RolDto datos) {
        return servicio.asignarRol(id, datos.rol(), datos.programa());
    }

    /** GET /api/usuarios/{id}/rol: el rol de la cuenta {id}, que MS-02 consulta al designar un tutor (CU-04, paso 10). */
    @GetMapping("/{id}/rol")
    public Rol consultarRol(@PathVariable("id") UUID id) {
        return servicio.consultarRol(id);
    }
}
