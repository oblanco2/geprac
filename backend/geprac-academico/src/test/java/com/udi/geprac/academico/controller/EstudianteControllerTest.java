package com.udi.geprac.academico.controller;

import com.udi.geprac.academico.config.SecurityConfig;
import com.udi.geprac.academico.dto.CompletitudDto;
import com.udi.geprac.academico.dto.EntradaDto;
import com.udi.geprac.academico.dto.EstudianteDto;
import com.udi.geprac.academico.service.CampoInvalidoException;
import com.udi.geprac.academico.service.EstudianteService;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El perfil y la hoja de vida con la seguridad real: solo el estudiante los
 * opera, los datos que no pasan la validación vuelven con el mensaje de cada
 * campo y las reglas que fallan en un campo, con su estado y en ese campo.
 *
 * @author Oscar Iván Blanco Díaz
 */
@WebMvcTest(EstudianteController.class)
@Import(SecurityConfig.class)
class EstudianteControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private EstudianteService servicio;

    @MockitoBean
    private JwtDecoder decodificador;

    private static RequestPostProcessor estudiante() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_ESTUDIANTE"));
    }

    @Test
    void sinTokenRespondeNoAutorizado() throws Exception {
        mvc.perform(get("/estudiantes/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void otroRolNoAbreElPerfil() throws Exception {
        mvc.perform(get("/estudiantes/me").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_TUTOR"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void elEstudianteConsultaSuPerfil() throws Exception {
        given(servicio.consultarPerfil()).willReturn(new EstudianteDto(null, "jrincon32@udi.edu.co", null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            new CompletitudDto(0, false, List.of("Nombres"))));

        mvc.perform(get("/estudiantes/me").with(estudiante()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.correoInstitucional").value("jrincon32@udi.edu.co"))
            .andExpect(jsonPath("$.completitud.porcentaje").value(0))
            .andExpect(jsonPath("$.completitud.faltan[0]").value("Nombres"));
    }

    @Test
    void losDatosInvalidosRespondenConElMensajeDeCadaCampo() throws Exception {
        String cuerpo = """
            {"nombres": " ", "apellidos": "Rincón Barrios", "tipoDocumento": "C.C.", "numeroDocumento": "12",
             "fechaNacimiento": "2999-01-01", "genero": "Otro", "codigoPrograma": "LEI", "semestreActual": 9}""";

        mvc.perform(put("/estudiantes/me").with(estudiante()).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.nombres").value("Este dato es obligatorio."))
            .andExpect(jsonPath("$.campos.numeroDocumento").exists())
            .andExpect(jsonPath("$.campos.fechaNacimiento").value("La fecha de nacimiento debe ser anterior a hoy."))
            .andExpect(jsonPath("$.campos.genero").exists())
            .andExpect(jsonPath("$.campos.semestreActual").exists());
    }

    @Test
    void unDocumentoDeOtroEstudianteRespondeConflictoEnSuCampo() throws Exception {
        given(servicio.guardarPerfil(any())).willThrow(new CampoInvalidoException("numeroDocumento",
            "Ese número de documento ya corresponde a otro estudiante.", HttpStatus.CONFLICT));
        String cuerpo = """
            {"nombres": "José Fernando", "apellidos": "Rincón Barrios", "tipoDocumento": "C.C.",
             "numeroDocumento": "1.098.765.432", "codigoPrograma": "LEI", "semestreActual": 3}""";

        mvc.perform(put("/estudiantes/me").with(estudiante()).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.campos.numeroDocumento").value("Ese número de documento ya corresponde a otro estudiante."));
    }

    @Test
    void unaFormacionNuevaRespondeCreada() throws Exception {
        given(servicio.crearFormacion(any())).willReturn(new EntradaDto.Formacion(5L, "Secundaria",
            "Institución Educativa Santander", "Bachiller académico", 2022));
        String cuerpo = """
            {"tipo": "Secundaria", "institucion": "Institución Educativa Santander", "nombre": "Bachiller académico", "anio": 2022}""";

        mvc.perform(post("/estudiantes/me/formaciones").with(estudiante()).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void unTipoDeFormacionQueNoEsDelFormatoNoPasa() throws Exception {
        String cuerpo = """
            {"tipo": "Técnica", "institucion": "SENA", "nombre": "Técnico en primera infancia"}""";

        mvc.perform(post("/estudiantes/me/formaciones").with(estudiante()).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.tipo")
                .value("El tipo de formación no es uno de los tres del formato. Selecciónelo de nuevo."));
    }

    @Test
    void unaCuartaReferenciaRespondeConflicto() throws Exception {
        given(servicio.crearReferencia(any())).willThrow(new IllegalStateException("La hoja de vida ya tiene tres referencias."));
        String cuerpo = """
            {"nombre": "Lic. Ana Ruiz", "empresa": "UDI", "cargo": "Docente", "telefono": "300 000 0000", "ciudad": "Bucaramanga"}""";

        mvc.perform(post("/estudiantes/me/referencias").with(estudiante()).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("La hoja de vida ya tiene tres referencias."));
    }

    @Test
    void unPerfilProfesionalMasLargoQueElFormatoNoPasa() throws Exception {
        String cuerpo = "{\"perfilProfesional\": \"" + "a".repeat(801) + "\"}";

        mvc.perform(put("/estudiantes/me/hoja-vida").with(estudiante()).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.perfilProfesional").value("Supera la extensión máxima de 800 caracteres."));
    }

    @Test
    void unBloqueQueNoExisteRespondeQueNoExiste() throws Exception {
        mvc.perform(delete("/estudiantes/me/otros/1").with(estudiante()))
            .andExpect(status().isNotFound());
    }
}
