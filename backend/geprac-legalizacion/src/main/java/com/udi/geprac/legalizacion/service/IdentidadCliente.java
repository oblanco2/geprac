package com.udi.geprac.legalizacion.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Cliente REST de MS-01 Identidad y Perfil Académico. Es uno de los dos puntos
 * en que los servicios se comunican, siempre de MS-02 hacia MS-01: aquí, la
 * consulta del rol del usuario que la Dirección designa como tutor (CU-04,
 * paso 10). La petición lleva el mismo token de quien la hace, así que MS-01
 * aplica sus propias reglas de acceso.
 *
 * La dirección de MS-01 sale de la variable IDENTIDAD_URL; sin ella, es la
 * de Render. Como MS-01 puede tardar hasta un minuto en despertar, la espera
 * de la respuesta llega a 55 segundos.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Component
public class IdentidadCliente {

    private static final String NO_RESPONDE = "No fue posible consultar el rol del usuario en el servicio de "
        + "identidad: no se guardó nada. Inténtelo de nuevo en un momento.";

    private final RestClient http;

    @Autowired
    public IdentidadCliente(@Value("${geprac.identidad.url}") String url) {
        this(RestClient.builder().requestFactory(fabrica()), url);
    }

    /** Con el constructor de clientes que se le indique: el de las pruebas. */
    IdentidadCliente(RestClient.Builder constructor, String url) {
        this.http = constructor.baseUrl(url).build();
    }

    /**
     * El rol de una cuenta de MS-01: DIRECTOR, TUTOR o ESTUDIANTE; nulo si
     * todavía no tiene rol o si la cuenta no existe.
     */
    public String consultarRol(UUID usuarioId) {
        try {
            return http.get().uri("/usuarios/{id}/rol", usuarioId)
                .headers(h -> h.setBearerAuth(UsuarioActual.token().getTokenValue()))
                .exchange((peticion, respuesta) -> {
                    if (respuesta.getStatusCode().value() == 404) return null;
                    if (respuesta.getStatusCode().isError()) throw new IdentidadNoDisponibleException(NO_RESPONDE, null);
                    // MS-01 responde el rol como texto JSON, entre comillas; sin rol, el cuerpo viene vacío
                    String rol = new String(respuesta.getBody().readAllBytes(), StandardCharsets.UTF_8).replace("\"", "").trim();
                    return rol.isEmpty() || rol.equals("null") ? null : rol;
                });
        } catch (RestClientException e) {
            throw new IdentidadNoDisponibleException(NO_RESPONDE, e);
        }
    }

    private static SimpleClientHttpRequestFactory fabrica() {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(Duration.ofSeconds(10));
        f.setReadTimeout(Duration.ofSeconds(55));
        return f;
    }
}
