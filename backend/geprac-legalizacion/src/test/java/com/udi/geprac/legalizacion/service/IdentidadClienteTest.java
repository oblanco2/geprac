package com.udi.geprac.legalizacion.service;

import com.udi.geprac.legalizacion.dto.ExpedienteDto;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
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
 * Las consultas a MS-01 sin red: cada petición lleva el token de quien la
 * hace; una cuenta sin rol o que no existe no es tutora, y el expediente del
 * estudiante trae su hoja de vida solo si el registro está completo. Si MS-01
 * no responde, la operación no se completa.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
class IdentidadClienteTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-0000000000b2");
    private static final String URL = "http://ms01/api/usuarios/" + ID + "/rol";
    private static final String PERFIL_URL = "http://ms01/api/estudiantes/me";
    private static final String HOJA_URL = "http://ms01/api/estudiantes/me/hoja-vida";

    /** Los datos personales como los responde MS-01, con el indicador por completar. */
    private static final String PERFIL = """
        {"id":7,"correoInstitucional":"vcruz@udi.edu.co","nombres":"Valeria","apellidos":"Cruz Rangel",
         "tipoDocumento":"C.C.","numeroDocumento":"1099331208","lugarExpedicion":"Bucaramanga",
         "fechaNacimiento":"2004-03-12","lugarNacimiento":"Bucaramanga","genero":"Femenino",
         "estadoCivil":"Unión marital de hecho","eps":"Nueva EPS","direccion":"Calle 45 # 27-18",
         "barrio":"Cabecera","ciudad":"Bucaramanga","telefonoFijo":"6076000000","celular":"3001234567",
         "correoPersonal":"valeria.cruz@correo.com","codigoPrograma":"LEI",
         "nombrePrograma":"Licenciatura en Educación Infantil","semestreActual":3,
         "completitud":{"porcentaje":%d,"completo":%s,"faltan":%s}}""";

    /** La hoja de vida como la responde MS-01, con los identificadores de sus entradas. */
    private static final String HOJA = """
        {"perfilProfesional":"Estudiante de licenciatura.","herramientasTrabajo":"Ofimática.",
         "formaciones":[{"id":1,"tipo":"Secundaria","institucion":"Colegio Santander",
                         "nombre":"Bachiller académico","anio":2021}],
         "experiencias":[{"id":2,"empresa":"Jardín Mis Pequeños","cargo":"Auxiliar pedagógica",
                          "jefeInmediato":null,"cargoJefe":null,"telefonoEmpresa":null,
                          "fechaInicio":"2024-02-01","fechaFin":null,"funciones":"Apoyo.","logros":null}],
         "referencias":[{"id":3,"nombre":"Lic. Carlos Gómez","empresa":"Colegio Santander","cargo":"Docente",
                         "telefono":"3009876543","ciudad":"Bucaramanga"}],
         "completitud":{"porcentaje":100,"completo":true,"faltan":[]}}""";

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

    @Test
    void elExpedienteSePideConElTokenDelEstudiante() {
        ms01.expect(requestTo(PERFIL_URL)).andExpect(method(HttpMethod.GET))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer t"))
            .andRespond(withSuccess(PERFIL.formatted(100, true, "[]"), MediaType.APPLICATION_JSON));
        ms01.expect(requestTo(HOJA_URL)).andExpect(method(HttpMethod.GET))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer t"))
            .andRespond(withSuccess(HOJA, MediaType.APPLICATION_JSON));

        ExpedienteDto e = cliente.consultarExpediente();

        assertThat(e.perfil().id()).isEqualTo(7L);
        assertThat(e.perfil().estadoCivil()).isEqualTo("Unión marital de hecho");
        assertThat(e.perfil().fechaNacimiento()).isEqualTo(LocalDate.of(2004, 3, 12));
        assertThat(e.perfil().semestreActual()).isEqualTo(3);
        assertThat(e.hojaVida().formaciones()).extracting(ExpedienteDto.Formacion::nombre)
            .containsExactly("Bachiller académico");
        assertThat(e.hojaVida().experiencias().get(0).fechaInicio()).isEqualTo(LocalDate.of(2024, 2, 1));
        assertThat(e.hojaVida().referencias()).extracting(ExpedienteDto.Referencia::nombre)
            .containsExactly("Lic. Carlos Gómez");
        ms01.verify();
    }

    @Test
    void conElRegistroIncompletoNoPideLaHojaDeVida() {
        ms01.expect(requestTo(PERFIL_URL))
            .andRespond(withSuccess(PERFIL.formatted(91, false, "[\"Celular\"]"), MediaType.APPLICATION_JSON));

        ExpedienteDto e = cliente.consultarExpediente();

        assertThat(e.perfil().completitud().faltan()).containsExactly("Celular");
        assertThat(e.hojaVida()).isNull();
        ms01.verify();
    }

    @Test
    void siMs01FallaNoHayExpediente() {
        ms01.expect(requestTo(PERFIL_URL)).andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThatThrownBy(cliente::consultarExpediente).isInstanceOf(IdentidadNoDisponibleException.class)
            .hasMessageContaining("no se creó la inscripción");
    }

    @Test
    void siMs01NoRespondeAlPedirLaHojaDeVidaTampoco() {
        ms01.expect(requestTo(PERFIL_URL))
            .andRespond(withSuccess(PERFIL.formatted(100, true, "[]"), MediaType.APPLICATION_JSON));
        ms01.expect(requestTo(HOJA_URL)).andRespond(withException(new SocketTimeoutException("sin respuesta")));

        assertThatThrownBy(cliente::consultarExpediente).isInstanceOf(IdentidadNoDisponibleException.class);
    }
}
