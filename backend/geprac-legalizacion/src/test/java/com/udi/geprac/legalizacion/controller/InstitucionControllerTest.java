package com.udi.geprac.legalizacion.controller;

import com.udi.geprac.legalizacion.config.SecurityConfig;
import com.udi.geprac.legalizacion.service.InstitucionService;
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
 * Las instituciones con la seguridad real: solo el director las consulta y las
 * mantiene, y una institución sin contactos o con un correo mal escrito no se
 * guarda.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@WebMvcTest(InstitucionController.class)
@Import(SecurityConfig.class)
class InstitucionControllerTest {

    private static final SimpleGrantedAuthority DIRECTOR = new SimpleGrantedAuthority("ROLE_DIRECTOR");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private InstitucionService servicio;

    @MockitoBean
    private JwtDecoder decodificador;

    @Test
    void elDirectorConsultaElCatalogo() throws Exception {
        given(servicio.listarInstituciones()).willReturn(List.of());
        mvc.perform(get("/instituciones").with(jwt().authorities(DIRECTOR))).andExpect(status().isOk());
    }

    @Test
    void otroRolNoEntraAlCatalogo() throws Exception {
        mvc.perform(get("/instituciones").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_TUTOR"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void sinContactosNoSeGuarda() throws Exception {
        mvc.perform(post("/instituciones").with(jwt().authorities(DIRECTOR))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"razonSocial\":\"Jardín Los Cerezos\",\"direccion\":\"Calle 1\",\"ciudad\":\"Bucaramanga\","
                    + "\"telefono\":\"6076543210\",\"correo\":\"no-es-correo\",\"activa\":true,\"contactos\":[]}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.contactos").value("Añada al menos un contacto."))
            .andExpect(jsonPath("$.campos.correo").exists());
    }
}
