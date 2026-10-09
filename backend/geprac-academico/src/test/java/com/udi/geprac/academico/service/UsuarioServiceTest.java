package com.udi.geprac.academico.service;

import com.udi.geprac.academico.domain.Usuario;
import com.udi.geprac.academico.dto.UsuarioDto;
import com.udi.geprac.academico.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * La cuenta del primer ingreso y la lista de cuentas, sin base de datos.
 *
 * @author Oscar Iván Blanco Díaz
 */
class UsuarioServiceTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1");

    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final UsuarioService servicio = new UsuarioService(usuarios);

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
}
