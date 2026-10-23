package com.udi.geprac.legalizacion.controller;

import com.udi.geprac.legalizacion.config.SecurityConfig;
import com.udi.geprac.legalizacion.service.HistorialService;
import java.util.List;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El historial con la seguridad real: lo consultan los tres roles, y una cuenta
 * sin rol no.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@WebMvcTest(HistorialController.class)
@Import(SecurityConfig.class)
class HistorialControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private HistorialService servicio;

    @MockitoBean
    private JwtDecoder decodificador;

    @Test
    void unaCuentaSinRolNoConsultaElHistorial() throws Exception {
        mvc.perform(get("/historial").with(jwt())).andExpect(status().isForbidden());
    }

    @Test
    void losTresRolesConsultanElHistorial() throws Exception {
        given(servicio.consultarHistorial()).willReturn(List.of());
        for (String rol : List.of("ESTUDIANTE", "TUTOR", "DIRECTOR"))
            mvc.perform(get("/historial").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_" + rol))))
                .andExpect(status().isOk());
    }
}
