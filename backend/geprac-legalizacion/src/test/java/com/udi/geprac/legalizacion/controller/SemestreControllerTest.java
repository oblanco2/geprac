package com.udi.geprac.legalizacion.controller;

import com.udi.geprac.legalizacion.config.SecurityConfig;
import com.udi.geprac.legalizacion.dto.DesignacionDto;
import com.udi.geprac.legalizacion.dto.PreparacionDto;
import com.udi.geprac.legalizacion.service.CampoInvalidoException;
import com.udi.geprac.legalizacion.service.IdentidadNoDisponibleException;
import com.udi.geprac.legalizacion.service.SemestreService;
import java.util.List;
import java.util.UUID;
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

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * La preparación del semestre con la seguridad real: solo el director la
 * opera, el código del semestre tiene su formato, la designación exige un
 * tutor, y una falla de MS-01 responde 503.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@WebMvcTest(SemestreController.class)
@Import(SecurityConfig.class)
class SemestreControllerTest {

    private static final SimpleGrantedAuthority DIRECTOR = new SimpleGrantedAuthority("ROLE_DIRECTOR");
    private static final UUID TUTOR = UUID.fromString("00000000-0000-0000-0000-0000000000b2");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SemestreService servicio;

    @MockitoBean
    private JwtDecoder decodificador;

    @Test
    void elDirectorConsultaLaPreparacion() throws Exception {
        given(servicio.consultarPreparacion()).willReturn(new PreparacionDto(null,
            List.of(new DesignacionDto(1L, 1, "Observación del ambiente pedagógico", true, null, 0))));

        mvc.perform(get("/semestres/preparacion").with(jwt().authorities(DIRECTOR)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.practicas[0].orden").value(1));
    }

    @Test
    void elEstudianteNoPreparaElSemestre() throws Exception {
        mvc.perform(get("/semestres/preparacion").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ESTUDIANTE"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void elCodigoDelSemestreTieneSuFormato() throws Exception {
        mvc.perform(post("/semestres").with(jwt().authorities(DIRECTOR)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":\"2027-3\",\"fechaInicio\":\"2027-02-01\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.codigo").value("Escriba el año y el periodo, por ejemplo 2027-1."))
            .andExpect(jsonPath("$.campos.fechaCierre").value("Este dato es obligatorio para abrir el semestre."));
    }

    @Test
    void laDesignacionExigeElTutor() throws Exception {
        mvc.perform(post("/semestres/abierto/practicas/3/tutor").with(jwt().authorities(DIRECTOR))
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.tutorId").value("Escoja el tutor académico."));
    }

    @Test
    void quienNoEsTutorSeSenalaEnSuCampo() throws Exception {
        given(servicio.designarTutor(3L, TUTOR)).willThrow(new CampoInvalidoException("tutorId",
            "El usuario escogido no tiene el rol de tutor académico: primero debe asignárselo en la lista de roles.",
            HttpStatus.CONFLICT));

        mvc.perform(post("/semestres/abierto/practicas/3/tutor").with(jwt().authorities(DIRECTOR))
                .contentType(MediaType.APPLICATION_JSON).content("{\"tutorId\":\"" + TUTOR + "\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.campos.tutorId").exists());
    }

    @Test
    void laReasignacionResponde() throws Exception {
        given(servicio.reasignarTutor(3L, TUTOR)).willReturn(new DesignacionDto(3L, 3, "Investigación en gerencia educativa",
            true, TUTOR, 3));

        mvc.perform(put("/semestres/abierto/practicas/3/tutor").with(jwt().authorities(DIRECTOR))
                .contentType(MediaType.APPLICATION_JSON).content("{\"tutorId\":\"" + TUTOR + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.enviadas").value(3));
    }

    @Test
    void siMs01NoRespondeLaDesignacionRespondeNoDisponible() throws Exception {
        given(servicio.designarTutor(3L, TUTOR)).willThrow(new IdentidadNoDisponibleException("Inténtelo de nuevo.", null));

        mvc.perform(post("/semestres/abierto/practicas/3/tutor").with(jwt().authorities(DIRECTOR))
                .contentType(MediaType.APPLICATION_JSON).content("{\"tutorId\":\"" + TUTOR + "\"}"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.detail").value("Inténtelo de nuevo."));
    }
}
