package com.udi.geprac.legalizacion.controller;

import com.udi.geprac.legalizacion.config.SecurityConfig;
import com.udi.geprac.legalizacion.dto.PracticaDto;
import com.udi.geprac.legalizacion.service.CampoInvalidoException;
import com.udi.geprac.legalizacion.service.PracticaService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El catálogo de prácticas con la seguridad real: solo el director lo opera, y
 * una práctica sin sus datos o sin sus listas no se guarda.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@WebMvcTest(PracticaController.class)
@Import(SecurityConfig.class)
class PracticaControllerTest {

    private static final SimpleGrantedAuthority DIRECTOR = new SimpleGrantedAuthority("ROLE_DIRECTOR");
    private static final String VALIDA = """
        {"orden":8,"nombre":"Práctica en espacios no convencionales","objetivoGeneral":"Diseñar experiencias.",
         "activa":true,"objetivos":["Reconocer el contexto."],"actividades":["Observar una jornada."]}""";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PracticaService servicio;

    @MockitoBean
    private JwtDecoder decodificador;

    @Test
    void elDirectorConsultaElCatalogo() throws Exception {
        given(servicio.listarPracticas()).willReturn(List.of(new PracticaDto(1L, 1, "Observación del ambiente pedagógico",
            "Observar.", null, true, List.of("Reconocer."), List.of("Observar."))));

        mvc.perform(get("/practicas").with(jwt().authorities(DIRECTOR)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].objetivos[0]").value("Reconocer."));
    }

    @Test
    void otroRolNoAbreElCatalogo() throws Exception {
        mvc.perform(get("/practicas").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_TUTOR"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void unaPracticaSinPosicionValidaNiObjetivosSeSenala() throws Exception {
        mvc.perform(post("/practicas").with(jwt().authorities(DIRECTOR)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"orden\":9,\"nombre\":\"X\",\"objetivoGeneral\":\"Y\",\"activa\":true,\"objetivos\":[],\"actividades\":[\" \"]}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.orden").value("La posición debe ser un número entero de 1 a 8."))
            .andExpect(jsonPath("$.campos.objetivos").value("Añada al menos un objetivo específico."))
            .andExpect(jsonPath("$.campos['actividades[0]']").value("Escriba el texto o quite el ítem."));
    }

    @Test
    void unaPracticaNuevaRespondeCreada() throws Exception {
        given(servicio.crearPractica(any())).willReturn(new PracticaDto(9L, 8, "Práctica en espacios no convencionales",
            "Diseñar experiencias.", null, true, List.of("Reconocer el contexto."), List.of("Observar una jornada.")));

        mvc.perform(post("/practicas").with(jwt().authorities(DIRECTOR)).contentType(MediaType.APPLICATION_JSON).content(VALIDA))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(9));
    }

    @Test
    void laPosicionOcupadaSeSenalaEnSuCampo() throws Exception {
        given(servicio.crearPractica(any())).willThrow(new CampoInvalidoException("orden",
            "Ya hay una práctica en esa posición del plan: 8 · Implementación en institución educativa II.", HttpStatus.CONFLICT));

        mvc.perform(post("/practicas").with(jwt().authorities(DIRECTOR)).contentType(MediaType.APPLICATION_JSON).content(VALIDA))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.campos.orden").value("Ya hay una práctica en esa posición del plan: 8 · Implementación en institución educativa II."));
    }

    @Test
    void eliminarRespondeSinContenido() throws Exception {
        mvc.perform(delete("/practicas/8").with(jwt().authorities(DIRECTOR)))
            .andExpect(status().isNoContent());
    }
}
