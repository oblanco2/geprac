package com.udi.geprac.legalizacion.controller;

import com.udi.geprac.legalizacion.config.SecurityConfig;
import com.udi.geprac.legalizacion.domain.EstadoInscripcion;
import com.udi.geprac.legalizacion.dto.InscripcionDto;
import com.udi.geprac.legalizacion.service.CampoInvalidoException;
import com.udi.geprac.legalizacion.service.IdentidadNoDisponibleException;
import com.udi.geprac.legalizacion.service.InscripcionService;
import com.udi.geprac.legalizacion.service.RegistroIncompletoException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.udi.geprac.legalizacion.Fabrica.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * La inscripción con la seguridad real: solo el estudiante inscribe; un dato
 * editable vacío o mal escrito no se guarda; y lo que falta, o lo que no
 * corresponde en cada campo, llega al cliente para señalarlo.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@WebMvcTest(InscripcionController.class)
@Import(SecurityConfig.class)
class InscripcionControllerTest {

    private static final SimpleGrantedAuthority ESTUDIANTE = new SimpleGrantedAuthority("ROLE_ESTUDIANTE");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private InscripcionService servicio;

    @MockitoBean
    private JwtDecoder decodificador;

    @Test
    void elEstudianteIniciaSuInscripcion() throws Exception {
        given(servicio.iniciarInscripcion()).willReturn(dto(EstadoInscripcion.BORRADOR));

        mvc.perform(post("/inscripciones").with(jwt().authorities(ESTUDIANTE)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("BORRADOR"))
            .andExpect(jsonPath("$.semestreInicio").value("2026-07-27"))
            .andExpect(jsonPath("$.datos.nombrePractica").value("Práctica pedagógica 3"));
    }

    @Test
    void otroRolNoInscribe() throws Exception {
        mvc.perform(post("/inscripciones").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_TUTOR"))))
            .andExpect(status().isForbidden());
        mvc.perform(get("/inscripciones/actual").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_DIRECTOR"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void conElRegistroIncompletoDiceLoQueFalta() throws Exception {
        given(servicio.iniciarInscripcion()).willThrow(new RegistroIncompletoException(
            "Complete su perfil y su hoja de vida para inscribir la práctica.", List.of("Celular", "Referencias personales")));

        mvc.perform(post("/inscripciones").with(jwt().authorities(ESTUDIANTE)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.faltan[0]").value("Celular"))
            .andExpect(jsonPath("$.faltan[1]").value("Referencias personales"));
    }

    @Test
    void siMs01NoRespondeNoSeCreaYSePuedeReintentar() throws Exception {
        given(servicio.iniciarInscripcion()).willThrow(new IdentidadNoDisponibleException(
            "No fue posible consultar su registro en el servicio de identidad.", null));

        mvc.perform(post("/inscripciones").with(jwt().authorities(ESTUDIANTE)))
            .andExpect(status().isServiceUnavailable());
    }

    @Test
    void elSegundoPasoSenalaCadaCampoQueNoCorresponde() throws Exception {
        Map<String, String> campos = new LinkedHashMap<>();
        campos.put("fechaInicio", "Este dato es obligatorio.");
        campos.put("fechaFin", "Este dato es obligatorio.");
        given(servicio.actualizarInscripcion(eq(40L), any())).willThrow(
            new CampoInvalidoException(campos, "Revise los datos señalados: el software no guardó el paso."));

        mvc.perform(put("/inscripciones/40").with(jwt().authorities(ESTUDIANTE))
                .contentType(MediaType.APPLICATION_JSON).content("{\"contactoId\":51}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.fechaInicio").value("Este dato es obligatorio."))
            .andExpect(jsonPath("$.campos.fechaFin").value("Este dato es obligatorio."));
    }

    @Test
    void unDatoEditableVacioOMalEscritoNoSeGuarda() throws Exception {
        mvc.perform(put("/inscripciones/40").with(jwt().authorities(ESTUDIANTE))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"direccion\":\"  \",\"celular\":\"abc\",\"correoPersonal\":\"no-es-correo\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.direccion").value("Este dato es obligatorio."))
            .andExpect(jsonPath("$.campos.celular").value("Escriba solo números; admite espacios."))
            .andExpect(jsonPath("$.campos.correoPersonal").value("Escriba un correo válido, como nombre@dominio.com."));
        then(servicio).should(never()).actualizarInscripcion(any(), any());
    }

    @Test
    void laIncompletaNoSeEnviaYDiceLoQueFalta() throws Exception {
        given(servicio.enviarARevision(40L)).willThrow(new RegistroIncompletoException(
            "La inscripción no se envió: está incompleta.", List.of("el contacto del escenario")));

        mvc.perform(post("/inscripciones/40/envio").with(jwt().authorities(ESTUDIANTE)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("La inscripción no se envió: está incompleta."))
            .andExpect(jsonPath("$.faltan[0]").value("el contacto del escenario"));
    }

    @Test
    void laCompletaSeEnvia() throws Exception {
        given(servicio.enviarARevision(40L)).willReturn(dto(EstadoInscripcion.ENVIADA));

        mvc.perform(post("/inscripciones/40/envio").with(jwt().authorities(ESTUDIANTE)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("ENVIADA"));
    }

    @Test
    void elEstudianteConsultaElEstadoDeSuInscripcion() throws Exception {
        given(servicio.consultarEstado()).willReturn(dto(EstadoInscripcion.ENVIADA));

        mvc.perform(get("/inscripciones/actual").with(jwt().authorities(ESTUDIANTE)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("ENVIADA"));
    }

    private static InscripcionDto dto(EstadoInscripcion estado) {
        return InscripcionDto.de(inscripcion(40L, 7L, practica(3L, 3), semestre(2L, "2026-2", true), estado));
    }
}
