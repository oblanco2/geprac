package com.udi.geprac.legalizacion.service;

import java.net.SocketTimeoutException;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static com.udi.geprac.legalizacion.Fabrica.ingresa;
import static com.udi.geprac.legalizacion.Fabrica.sale;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * La consulta del rol a MS-01 sin red: la petición lleva el token de quien la
 * hace, y una cuenta sin rol o que no existe no es tutora. Si MS-01 no
 * responde, la operación no se completa.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
class IdentidadClienteTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-0000000000b2");
    private static final String URL = "http://ms01/api/usuarios/" + ID + "/rol";

    private MockRestServiceServer ms01;
    private IdentidadCliente cliente;

    @BeforeEach
    void prepara() {
        RestClient.Builder constructor = RestClient.builder();
        ms01 = MockRestServiceServer.bindTo(constructor).build();
        cliente = new IdentidadCliente(constructor, "http://ms01/api");
        ingresa(UUID.randomUUID(), "DIRECTOR", "programa", "LEI");
    }

    @AfterEach
    void cierra() {
        sale();
    }

    @Test
    void consultaElRolConElTokenDeQuienDesigna() {
        ms01.expect(requestTo(URL)).andExpect(method(HttpMethod.GET))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer t"))
            .andRespond(withSuccess("\"TUTOR\"", MediaType.APPLICATION_JSON));

        assertThat(cliente.consultarRol(ID)).isEqualTo("TUTOR");
        ms01.verify();
    }

    @Test
    void unaCuentaSinRolNoTieneRol() {
        ms01.expect(requestTo(URL)).andRespond(withSuccess());

        assertThat(cliente.consultarRol(ID)).isNull();
    }

    @Test
    void unaCuentaQueNoExisteNoTieneRol() {
        ms01.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(cliente.consultarRol(ID)).isNull();
    }

    @Test
    void siMs01FallaNoSeCompletaLaOperacion() {
        ms01.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> cliente.consultarRol(ID)).isInstanceOf(IdentidadNoDisponibleException.class)
            .hasMessageContaining("Inténtelo de nuevo");
    }

    @Test
    void siMs01NoRespondeTampoco() {
        ms01.expect(requestTo(URL)).andRespond(withException(new SocketTimeoutException("sin respuesta")));

        assertThatThrownBy(() -> cliente.consultarRol(ID)).isInstanceOf(IdentidadNoDisponibleException.class);
    }
}
