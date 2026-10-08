package com.udi.geprac.academico.controller;

import com.udi.geprac.academico.config.SecurityConfig;
import com.udi.geprac.academico.dto.ProgramaDto;
import com.udi.geprac.academico.service.ProgramaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas del controlador de programas con la seguridad real del servicio:
 * sin token no responde, con token lista los programas y no permite crearlos.
 *
 * @author Oscar Iván Blanco Díaz
 */
@WebMvcTest(ProgramaController.class)
@Import(SecurityConfig.class)
class ProgramaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProgramaService servicio;

    @MockitoBean
    private JwtDecoder decodificador;

    @Test
    void sinTokenRespondeNoAutorizado() throws Exception {
        mvc.perform(get("/programas")).andExpect(status().isUnauthorized());
    }

    @Test
    void conTokenListaLosProgramas() throws Exception {
        given(servicio.listar()).willReturn(List.of(
            new ProgramaDto("LEI", "Licenciatura en Educación Infantil")));

        mvc.perform(get("/programas").with(jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value("LEI"))
            .andExpect(jsonPath("$[0].nombre").value("Licenciatura en Educación Infantil"));
    }

    @Test
    void noPermiteCrearProgramas() throws Exception {
        mvc.perform(post("/programas").with(jwt())
                .contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"XYZ\",\"nombre\":\"X\"}"))
            .andExpect(status().isMethodNotAllowed());
    }
}
