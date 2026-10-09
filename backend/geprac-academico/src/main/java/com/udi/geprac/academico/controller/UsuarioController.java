package com.udi.geprac.academico.controller;

import com.udi.geprac.academico.dto.UsuarioDto;
import com.udi.geprac.academico.service.UsuarioService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Las cuentas: la de quien ingresa, que el cliente web pide después de
 * autenticar para resolver con el rol el menú y la pantalla de inicio (RF-01),
 * y la lista de cuentas que consulta la Dirección del Programa.
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
}
