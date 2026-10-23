package com.udi.geprac.legalizacion.controller;

import com.udi.geprac.legalizacion.config.SecurityConfig;
import com.udi.geprac.legalizacion.service.AvalService;
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
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El aval con la seguridad real: solo el director abre la bandeja de aval, y
 * avalar una inscripción que no está aprobada responde 409 con el estado actual.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@WebMvcTest(AvalController.class)
@Import(SecurityConfig.class)
class AvalControllerTest {

    private static final SimpleGrantedAuthority DIRECTOR = new SimpleGrantedAuthority("ROLE_DIRECTOR");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AvalService servicio;

    @MockitoBean
    private JwtDecoder decodificador;

    @Test
    void elTutorNoAbreLaBandejaDeAval() throws Exception {
        mvc.perform(get("/avales/pendientes").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_TUTOR"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void elDirectorAbreSuBandeja() throws Exception {
        given(servicio.listarAprobadas()).willReturn(List.of());
        mvc.perform(get("/avales/pendientes").with(jwt().authorities(DIRECTOR)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void avalarUnaInscripcionQueNoEstaAprobadaRespondeConflicto() throws Exception {
        willThrow(new IllegalStateException("Solo se avala una inscripción aprobada por el tutor académico: esta está enviada."))
            .given(servicio).registrarAval(9L);
        mvc.perform(post("/avales/9").with(jwt().authorities(DIRECTOR)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("Solo se avala una inscripción aprobada por el tutor académico: esta está enviada."));
    }
}
