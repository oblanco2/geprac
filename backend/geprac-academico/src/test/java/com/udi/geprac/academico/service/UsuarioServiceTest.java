package com.udi.geprac.academico.service;

import com.udi.geprac.academico.domain.Programa;
import com.udi.geprac.academico.domain.Rol;
import com.udi.geprac.academico.domain.Usuario;
import com.udi.geprac.academico.dto.UsuarioDto;
import com.udi.geprac.academico.repository.ProgramaRepository;
import com.udi.geprac.academico.repository.UsuarioRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * La cuenta del primer ingreso, la lista de cuentas y el rol que asigna la
 * Dirección (CU-04, variación 2.1), sin base de datos.
 *
 * @author Oscar Iván Blanco Díaz
 */
class UsuarioServiceTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1");

    private static final UUID DIRECTOR = UUID.fromString("00000000-0000-0000-0000-0000000000d1");

    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final ProgramaRepository programas = mock(ProgramaRepository.class);
    private final UsuarioService servicio = new UsuarioService(usuarios, programas);

    @BeforeEach
    void ingresa() {
        Jwt token = Jwt.withTokenValue("t").header("alg", "ES256").subject(ID.toString())
            .claim("email", "laura.gomez@udi.edu.co").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(token));
    }

    @AfterEach
    void cerrarSesion() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void enElPrimerIngresoRegistraLaCuentaSinRol() {
        given(usuarios.findById(ID)).willReturn(Optional.empty());
        given(usuarios.save(any(Usuario.class))).willAnswer(i -> i.getArgument(0));

        UsuarioDto cuenta = servicio.consultarCuenta();

        assertThat(cuenta.id()).isEqualTo(ID);
        assertThat(cuenta.correoInstitucional()).isEqualTo("laura.gomez@udi.edu.co");
        assertThat(cuenta.nombrePresentacion()).isEqualTo("Laura Gomez");
        assertThat(cuenta.rol()).isNull();
    }

    @Test
    void unaCuentaRegistradaNoSeVuelveACrear() {
        given(usuarios.findById(ID)).willReturn(Optional.of(new Usuario(ID, "Laura Gómez", "laura.gomez@udi.edu.co")));

        assertThat(servicio.consultarCuenta().nombrePresentacion()).isEqualTo("Laura Gómez");
        verify(usuarios, never()).save(any());
    }

    @Test
    void laListaDeCuentasSaleEnElOrdenDelRepositorio() {
        given(usuarios.findAllByOrderByNombrePresentacion()).willReturn(List.of(
            new Usuario(UUID.randomUUID(), "Adriana Mejía", "adriana.mejia@udi.edu.co"),
            new Usuario(UUID.randomUUID(), "Julián Ortiz", "julian.ortiz@udi.edu.co")));

        assertThat(servicio.listarUsuarios()).extracting(UsuarioDto::nombrePresentacion)
            .containsExactly("Adriana Mejía", "Julián Ortiz");
    }

    @Test
    void elDirectorAsignaElRolDeTutorSinPrograma() {
        ingresaElDirector();
        Usuario laura = cuenta();
        given(usuarios.findById(ID)).willReturn(Optional.of(laura));
        given(usuarios.save(laura)).willReturn(laura);

        UsuarioDto r = servicio.asignarRol(ID, " tutor ", null);

        assertThat(r.rol()).isEqualTo("TUTOR");
        assertThat(r.programa()).isNull();
    }

    @Test
    void laDireccionSeAsignaConElProgramaDeQuienAsigna() {
        ingresaElDirector();
        Usuario laura = cuenta();
        given(usuarios.findById(ID)).willReturn(Optional.of(laura));
        given(usuarios.save(laura)).willReturn(laura);
        Programa lei = lei();
        given(programas.findByCodigoIgnoreCase("LEI")).willReturn(Optional.of(lei));

        assertThat(servicio.asignarRol(ID, "DIRECTOR", null).programa()).isEqualTo("LEI");
    }

    @Test
    void noSeAsignaLaDireccionDeOtroPrograma() {
        ingresaElDirector();
        given(usuarios.findById(ID)).willReturn(Optional.of(cuenta()));

        assertThatThrownBy(() -> servicio.asignarRol(ID, "DIRECTOR", "LEF"))
            .isInstanceOfSatisfying(CampoInvalidoException.class, e -> assertThat(e.getCampo()).isEqualTo("programa"));
        verify(usuarios, never()).save(any());
    }

    @Test
    void alDejarLaDireccionLaCuentaQuedaSinPrograma() {
        ingresaElDirector();
        Usuario laura = cuenta();
        laura.asignarRol(Rol.DIRECTOR, lei());
        given(usuarios.findById(ID)).willReturn(Optional.of(laura));
        given(usuarios.save(laura)).willReturn(laura);

        UsuarioDto r = servicio.asignarRol(ID, "ESTUDIANTE", null);

        assertThat(r.rol()).isEqualTo("ESTUDIANTE");
        assertThat(r.programa()).as("solo el director lleva programa").isNull();
    }

    @Test
    void unRolQueNoExisteSeSenalaEnSuCampo() {
        ingresaElDirector();

        assertThatThrownBy(() -> servicio.asignarRol(ID, "ADMINISTRADOR", null))
            .isInstanceOfSatisfying(CampoInvalidoException.class, e -> assertThat(e.getCampo()).isEqualTo("rol"));
        verify(usuarios, never()).save(any());
    }

    @Test
    void noSeAsignaRolAUnaCuentaQueNoExiste() {
        ingresaElDirector();
        given(usuarios.findById(ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.asignarRol(ID, "TUTOR", null))
            .isInstanceOf(NoSuchElementException.class).hasMessageContaining("ya ingresó al menos una vez");
    }

    @Test
    void elDirectorNoSeQuitaSuPropioRol() {
        ingresaElDirector();
        given(usuarios.findById(DIRECTOR)).willReturn(Optional.of(new Usuario(DIRECTOR, "Oscar Blanco", "oblanco2@udi.edu.co")));

        assertThatThrownBy(() -> servicio.asignarRol(DIRECTOR, "TUTOR", null))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("quien asigne los roles");
        verify(usuarios, never()).save(any());
    }

    @Test
    void elRolDeUnaCuentaSeConsulta() {
        Usuario laura = cuenta();
        laura.asignarRol(Rol.TUTOR, null);
        given(usuarios.findById(ID)).willReturn(Optional.of(laura));

        assertThat(servicio.consultarRol(ID)).isEqualTo(Rol.TUTOR);
    }

    @Test
    void unaCuentaSinRolSeConsultaSinRol() {
        given(usuarios.findById(ID)).willReturn(Optional.of(cuenta()));

        assertThat(servicio.consultarRol(ID)).isNull();
    }

    @Test
    void elRolDeUnaCuentaQueNoExisteNoSeConsulta() {
        given(usuarios.findById(ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.consultarRol(ID)).isInstanceOf(NoSuchElementException.class);
    }

    // ------------------------------------------------------------------ apoyo

    /** Abre la sesión del director de LEI, con los datos que el hook de V4 pone en el token. */
    private static void ingresaElDirector() {
        Jwt token = Jwt.withTokenValue("t").header("alg", "ES256").subject(DIRECTOR.toString())
            .claim("email", "oblanco2@udi.edu.co").claim("rol", "DIRECTOR").claim("programa", "LEI").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(token));
    }

    private static Usuario cuenta() {
        return new Usuario(ID, "Laura Gómez", "laura.gomez@udi.edu.co");
    }

    private static Programa lei() {
        Programa p = mock(Programa.class);
        given(p.getCodigo()).willReturn("LEI");
        return p;
    }
}
