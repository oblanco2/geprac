package com.udi.geprac.legalizacion.service;

import com.udi.geprac.legalizacion.dto.ExpedienteDto;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Cliente REST de MS-01 Identidad y Perfil Académico. Reúne los dos puntos en
 * que los servicios se comunican, siempre de MS-02 hacia MS-01: el expediente
 * del estudiante que inicia la inscripción, que se copia en ella (CU-05,
 * paso 4), y el rol del usuario que la Dirección designa como tutor (CU-04,
 * paso 10). Cada petición lleva el mismo token de quien la hace, así que MS-01
 * aplica sus propias reglas de acceso: al estudiante solo le entrega su propio
 * registro.
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

    private static final String SIN_EXPEDIENTE = "No fue posible consultar su registro en el servicio de identidad: "
        + "no se creó la inscripción. Inténtelo de nuevo en un momento.";

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
     * El expediente del estudiante de la petición en curso: sus datos
     * personales con el indicador de completitud y, si el registro está
     * completo, su hoja de vida. Con el registro incompleto no se pide la hoja
     * de vida, porque la inscripción no se va a crear (CU-05, excepción 3.1).
     */
    public ExpedienteDto consultarExpediente() {
        try {
            ExpedienteDto.Perfil perfil = consultar("/estudiantes/me", ExpedienteDto.Perfil.class);
            boolean completo = perfil.completitud() != null && perfil.completitud().completo();
            ExpedienteDto.HojaVida hojaVida = completo
                ? consultar("/estudiantes/me/hoja-vida", ExpedienteDto.HojaVida.class) : null;
            return new ExpedienteDto(perfil, hojaVida);
        } catch (RestClientException e) {
            throw new IdentidadNoDisponibleException(SIN_EXPEDIENTE, e);
        }
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

    /** Una consulta del expediente con el token de quien la hace; si MS-01 responde con error, no hay expediente. */
    private <T> T consultar(String ruta, Class<T> tipo) {
        T cuerpo = http.get().uri(ruta)
            .headers(h -> h.setBearerAuth(UsuarioActual.token().getTokenValue()))
            .retrieve()
            .onStatus(HttpStatusCode::isError, (peticion, respuesta) -> {
                throw new IdentidadNoDisponibleException(SIN_EXPEDIENTE, null);
            })
            .body(tipo);
        if (cuerpo == null) throw new IdentidadNoDisponibleException(SIN_EXPEDIENTE, null);
        return cuerpo;
    }

    private static SimpleClientHttpRequestFactory fabrica() {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(Duration.ofSeconds(10));
        f.setReadTimeout(Duration.ofSeconds(55));
        return f;
    }
}
