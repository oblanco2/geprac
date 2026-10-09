package com.udi.geprac.legalizacion.controller;

import com.udi.geprac.legalizacion.config.SecurityConfig;
import com.udi.geprac.legalizacion.service.RevisionService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * La revisión con la seguridad real: solo el tutor abre la bandeja y una
 * revisión sin resultado no se registra.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@WebMvcTest(RevisionController.class)
@Import(SecurityConfig.class)
class RevisionControllerTest {

    private static final SimpleGrantedAuthority TUTOR = new SimpleGrantedAuthority("ROLE_TUTOR");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RevisionService servicio;

    @MockitoBean
    private JwtDecoder decodificador;

    @Test
    void elEstudianteNoAbreLaBandejaDeRevision() throws Exception {
        mvc.perform(get("/revisiones/pendientes").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ESTUDIANTE"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void elTutorAbreSuBandeja() throws Exception {
        given(servicio.listarPendientes()).willReturn(List.of());

        mvc.perform(get("/revisiones/pendientes").with(jwt().authorities(TUTOR)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void unaRevisionSinResultadoNoSeRegistra() throws Exception {
        mvc.perform(post("/revisiones/4").with(jwt().authorities(TUTOR)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"resultado\":\" \",\"motivo\":\"Falta la EPS\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.resultado").value("Escoja el resultado de la revisión."));
    }
}
