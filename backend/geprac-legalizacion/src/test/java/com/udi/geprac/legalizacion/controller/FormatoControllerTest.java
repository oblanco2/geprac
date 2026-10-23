package com.udi.geprac.legalizacion.controller;

import com.udi.geprac.legalizacion.config.SecurityConfig;
import com.udi.geprac.legalizacion.service.FormatoService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Los formatos con la seguridad real: solo el estudiante los consulta y los
 * descarga, y la descarga es un PDF.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@WebMvcTest(FormatoController.class)
@Import(SecurityConfig.class)
class FormatoControllerTest {

    private static final SimpleGrantedAuthority ESTUDIANTE = new SimpleGrantedAuthority("ROLE_ESTUDIANTE");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private FormatoService servicio;

    @MockitoBean
    private JwtDecoder decodificador;

    @Test
    void elTutorNoVeFormatos() throws Exception {
        mvc.perform(get("/formatos").param("inscripcion", "5").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_TUTOR"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void sinLaInscripcionLaConsultaNoProcede() throws Exception {
        mvc.perform(get("/formatos").with(jwt().authorities(ESTUDIANTE))).andExpect(status().isBadRequest());
    }

    @Test
    void laDescargaEsUnPdf() throws Exception {
        given(servicio.descargarFormato(3L)).willReturn("%PDF-1.4".getBytes());
        mvc.perform(get("/formatos/3/archivo").with(jwt().authorities(ESTUDIANTE)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }
}
