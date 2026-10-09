package com.udi.geprac.legalizacion.controller;

import com.udi.geprac.legalizacion.config.SecurityConfig;
import com.udi.geprac.legalizacion.service.InstitucionService;
import com.udi.geprac.legalizacion.service.RevisionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * La seguridad real de MS-02: sin token ninguna ruta responde, y con token una
 * ruta que no existe responde 404 con el formato estándar de problema.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@WebMvcTest
@Import(SecurityConfig.class)
class SeguridadTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private JwtDecoder decodificador;

    @MockitoBean
    private InstitucionService instituciones;

    @MockitoBean
    private RevisionService revisiones;

    @Test
    void sinTokenRespondeNoAutorizado() throws Exception {
        mvc.perform(get("/inscripciones")).andExpect(status().isUnauthorized());
    }

    @Test
    void conTokenUnaRutaInexistenteRespondeProblema() throws Exception {
        mvc.perform(get("/no-existe").with(jwt()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }
}
