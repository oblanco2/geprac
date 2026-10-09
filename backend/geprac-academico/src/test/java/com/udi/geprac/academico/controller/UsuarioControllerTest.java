package com.udi.geprac.academico.controller;

import com.udi.geprac.academico.config.SecurityConfig;
import com.udi.geprac.academico.dto.UsuarioDto;
import com.udi.geprac.academico.service.UsuarioService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Las cuentas con la seguridad real: la propia se consulta con cualquier token
 * válido, aunque todavía no tenga rol, y la lista de cuentas solo la abre el
 * director.
 *
 * @author Oscar Iván Blanco Díaz
 */
@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UsuarioService servicio;

    @MockitoBean
    private JwtDecoder decodificador;

    @Test
    void sinTokenRespondeNoAutorizado() throws Exception {
        mvc.perform(get("/usuarios/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void sinRolTambienConsultaSuCuenta() throws Exception {
        given(servicio.consultarCuenta()).willReturn(new UsuarioDto(UUID.randomUUID(), "Laura Gómez",
            "laura.gomez@udi.edu.co", null, null));

        mvc.perform(get("/usuarios/me").with(jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.correoInstitucional").value("laura.gomez@udi.edu.co"))
            .andExpect(jsonPath("$.rol").doesNotExist());
    }

    @Test
    void elDirectorListaLasCuentas() throws Exception {
        given(servicio.listarUsuarios()).willReturn(List.of(new UsuarioDto(UUID.randomUUID(), "Julián Ortiz",
            "julian.ortiz@udi.edu.co", "TUTOR", null)));

        mvc.perform(get("/usuarios").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_DIRECTOR"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nombrePresentacion").value("Julián Ortiz"))
            .andExpect(jsonPath("$[0].rol").value("TUTOR"));
    }

    @Test
    void otroRolNoListaLasCuentas() throws Exception {
        mvc.perform(get("/usuarios").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_TUTOR"))))
            .andExpect(status().isForbidden());
    }
}
